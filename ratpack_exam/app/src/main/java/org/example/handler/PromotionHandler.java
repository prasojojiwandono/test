
package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ratpack.handling.Context;
import io.ratpack.handling.Handler;
import org.example.dto.ErrorResponse;
import org.example.dto.PromotionRequest;
import org.example.model.Promotion;
import org.example.repository.ItemRepository;
import org.example.repository.PromotionRepository;

public class PromotionHandler implements Handler {
    private final PromotionRepository promotionRepository;
    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public PromotionHandler(PromotionRepository promotionRepository, ItemRepository itemRepository, ObjectMapper objectMapper) {
        this.promotionRepository = promotionRepository;
        this.itemRepository = itemRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String path = ctx.getPathBinding().getPastBinding();
        String method = ctx.getRequest().getMethod().getName();
        
        if (path.isEmpty()) {
            switch (method) {
                case "GET":
                    getAllPromotions(ctx);
                    break;
                case "POST":
                    createPromotion(ctx);
                    break;
                default:
                    ctx.clientError(405);
            }
        } else if (path.matches("/\\d+")) {
            Long id = Long.parseLong(path.substring(1));
            switch (method) {
                case "GET":
                    getPromotion(ctx, id);
                    break;
                case "PUT":
                    updatePromotion(ctx, id);
                    break;
                case "DELETE":
                    deletePromotion(ctx, id);
                    break;
                default:
                    ctx.clientError(405);
            }
        } else {
            ctx.next();
        }
    }

    private void getAllPromotions(Context ctx) {
        ctx.render(objectMapper.writeValueAsString(promotionRepository.findAll()));
    }

    private void getPromotion(Context ctx, Long id) {
        promotionRepository.findById(id)
            .ifPresentOrElse(
                promotion -> ctx.render(objectMapper.valueToTree(promotion)),
                () -> {
                    ErrorResponse error = new ErrorResponse("Promotion not found", 404);
                    ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
                }
            );
    }

    private void createPromotion(Context ctx) {
        ctx.parse(PromotionRequest.class).then(request -> {
            // Validate that the item exists
            if (!itemRepository.findById(request.getItemId()).isPresent()) {
                ErrorResponse error = new ErrorResponse("Item not found", 404);
                ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            if (request.getDiscountPercentage() <= 0 || request.getDiscountPercentage() >= 100) {
                ErrorResponse error = new ErrorResponse("Discount percentage must be between 0 and 100", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            if (request.getQuota() <= 0) {
                ErrorResponse error = new ErrorResponse("Quota must be positive", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            if (request.getLimitPerCustomer() <= 0) {
                ErrorResponse error = new ErrorResponse("Limit per customer must be positive", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            Promotion promotion = new Promotion();
            promotion.setItemId(request.getItemId());
            promotion.setDiscountPercentage(request.getDiscountPercentage());
            promotion.setQuota(request.getQuota());
            promotion.setLimitPerCustomer(request.getLimitPerCustomer());
            promotion.setEnabled(request.isEnabled());
            promotion.setUsedQuota(0);
            
            Long id = promotionRepository.insert(promotion);
            ctx.getResponse().status(201).send(objectMapper.valueToTree(promotion).toString());
        });
    }

    private void updatePromotion(Context ctx, Long id) {
        promotionRepository.findById(id).ifPresentOrElse(
            existingPromotion -> {
                ctx.parse(PromotionRequest.class).then(request -> {
                    // Validate that the item exists
                    if (!itemRepository.findById(request.getItemId()).isPresent()) {
                        ErrorResponse error = new ErrorResponse("Item not found", 404);
                        ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
                        return;
                    }
                    
                    // Cannot reduce quota below used quota
                    if (request.getQuota() < existingPromotion.getUsedQuota()) {
                        ErrorResponse error = new ErrorResponse(
                            "Cannot update quota below " + existingPromotion.getUsedQuota() + " as these have been used", 
                            400
                        );
                        ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                        return;
                    }
                    
                    existingPromotion.setItemId(request.getItemId());
                    existingPromotion.setDiscountPercentage(request.getDiscountPercentage());
                    existingPromotion.setQuota(request.getQuota());
                    existingPromotion.setLimitPerCustomer(request.getLimitPerCustomer());
                    existingPromotion.setEnabled(request.isEnabled());
                    
                    promotionRepository.update(existingPromotion);
                    ctx.render(objectMapper.valueToTree(existingPromotion));
                });
            },
            () -> {
                ErrorResponse error = new ErrorResponse("Promotion not found", 404);
                ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
            }
        );
    }

    private void deletePromotion(Context ctx, Long id) {
        if (promotionRepository.hasBeenUsed(id)) {
            ErrorResponse error = new ErrorResponse("Cannot delete promotion that has been used", 400);
            ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
            return;
        }
        
        promotionRepository.findById(id).ifPresentOrElse(
            promotion -> {
                promotionRepository.delete(id);
                ctx.getResponse().status(204).send();
            },
            () -> {
                ErrorResponse error = new ErrorResponse("Promotion not found", 404);
                ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
            }
        );
    }
}
