
package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ratpack.handling.Context;
import io.ratpack.handling.Handler;
import org.example.dto.ErrorResponse;
import org.example.dto.PurchaseRequest;
import org.example.model.Item;
import org.example.model.ItemWithPrice;
import org.example.model.Promotion;
import org.example.model.Purchase;
import org.example.repository.ItemRepository;
import org.example.repository.PromotionRepository;
import org.example.repository.PurchaseRepository;

import java.util.List;
import java.util.Optional;

public class CustomerHandler implements Handler {
    private final ItemRepository itemRepository;
    private final PromotionRepository promotionRepository;
    private final PurchaseRepository purchaseRepository;
    private final ObjectMapper objectMapper;

    public CustomerHandler(ItemRepository itemRepository, PromotionRepository promotionRepository, 
                          PurchaseRepository purchaseRepository, ObjectMapper objectMapper) {
        this.itemRepository = itemRepository;
        this.promotionRepository = promotionRepository;
        this.purchaseRepository = purchaseRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String path = ctx.getPathBinding().getPastBinding();
        String method = ctx.getRequest().getMethod().getName();
        
        if (path.equals("/items")) {
            if (method.equals("GET")) {
                getAvailableItems(ctx);
            } else {
                ctx.clientError(405);
            }
        } else if (path.equals("/purchase")) {
            if (method.equals("POST")) {
                purchaseItem(ctx);
            } else {
                ctx.clientError(405);
            }
        } else {
            ctx.next();
        }
    }

    private void getAvailableItems(Context ctx) {
        List<ItemWithPrice> items = itemRepository.findAvailableItemsWithPrice();
        ctx.render(objectMapper.writeValueAsString(items));
    }

    private void purchaseItem(Context ctx) {
        ctx.parse(PurchaseRequest.class).then(request -> {
            // Validate item exists and has stock
            Optional<Item> itemOpt = itemRepository.findById(request.getItemId());
            if (itemOpt.isEmpty()) {
                ErrorResponse error = new ErrorResponse("Item not found", 404);
                ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            Item item = itemOpt.get();
            if (item.getStock() < request.getQuantity()) {
                ErrorResponse error = new ErrorResponse("Insufficient stock", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            if (request.getQuantity() <= 0) {
                ErrorResponse error = new ErrorResponse("Quantity must be positive", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            // Calculate price and apply promotion if available
            double pricePaid;
            Long promotionId = request.getPromotionId();
            
            if (promotionId != null) {
                Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
                
                if (promotionOpt.isEmpty()) {
                    ErrorResponse error = new ErrorResponse("Promotion not found", 404);
                    ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
                    return;
                }
                
                Promotion promotion = promotionOpt.get();
                
                if (!promotion.isEnabled()) {
                    ErrorResponse error = new ErrorResponse("Promotion is not enabled", 400);
                    ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                    return;
                }
                
                if (!promotion.getItemId().equals(item.getId())) {
                    ErrorResponse error = new ErrorResponse("Promotion is not valid for this item", 400);
                    ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                    return;
                }
                
                if (promotion.getQuota() - promotion.getUsedQuota() < request.getQuantity()) {
                    ErrorResponse error = new ErrorResponse("Promotion quota exceeded", 400);
                    ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                    return;
                }
                
                if (promotion.getLimitPerCustomer() < request.getQuantity()) {
                    ErrorResponse error = new ErrorResponse("Exceeds promotion limit per customer", 400);
                    ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                    return;
                }
                
                // Apply discount
                double discountMultiplier = 1 - (promotion.getDiscountPercentage() / 100.0);
                pricePaid = item.getPrice() * discountMultiplier * request.getQuantity();
                
                // Update promotion quota
                promotionRepository.incrementUsedQuota(promotionId, request.getQuantity());
            } else {
                pricePaid = item.getPrice() * request.getQuantity();
            }
            
            // Create purchase record
            Purchase purchase = new Purchase();
            purchase.setItemId(request.getItemId());
            purchase.setCustomerIdCard(request.getCustomerIdCard());
            purchase.setCustomerName(request.getCustomerName());
            purchase.setCustomerPhone(request.getCustomerPhone());
            purchase.setQuantity(request.getQuantity());
            purchase.setPricePaid(pricePaid);
            purchase.setPromotionId(promotionId);
            
            Long purchaseId = purchaseRepository.insert(purchase);
            
            // Update item stock
            item.setStock(item.getStock() - request.getQuantity());
            itemRepository.update(item);
            
            // Return purchase confirmation
            ctx.getResponse().status(201).send(objectMapper.valueToTree(purchase).toString());
        });
    }
}


