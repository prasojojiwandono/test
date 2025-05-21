
package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ratpack.handling.Context;
import io.ratpack.handling.Handler;
import org.example.dto.ErrorResponse;
import org.example.dto.ItemRequest;
import org.example.model.Item;
import org.example.repository.ItemRepository;

public class ItemHandler implements Handler {
    private final ItemRepository itemRepository;
    private final ObjectMapper objectMapper;

    public ItemHandler(ItemRepository itemRepository, ObjectMapper objectMapper) {
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
                    getAllItems(ctx);
                    break;
                case "POST":
                    createItem(ctx);
                    break;
                default:
                    ctx.clientError(405);
            }
        } else if (path.matches("/\\d+")) {
            Long id = Long.parseLong(path.substring(1));
            switch (method) {
                case "GET":
                    getItem(ctx, id);
                    break;
                case "PUT":
                    updateItem(ctx, id);
                    break;
                case "DELETE":
                    deleteItem(ctx, id);
                    break;
                default:
                    ctx.clientError(405);
            }
        } else {
            ctx.next();
        }
    }

    private void getAllItems(Context ctx) {
        ctx.render(objectMapper.writeValueAsString(itemRepository.findAll()));
    }

    private void getItem(Context ctx, Long id) {
        itemRepository.findById(id)
            .ifPresentOrElse(
                item -> ctx.render(objectMapper.valueToTree(item)),
                () -> {
                    ErrorResponse error = new ErrorResponse("Item not found", 404);
                    ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
                }
            );
    }

    private void createItem(Context ctx) {
        ctx.parse(ItemRequest.class).then(request -> {
            Item item = new Item();
            item.setName(request.getName());
            item.setColor(request.getColor());
            item.setPrice(request.getPrice());
            item.setStock(request.getStock());
            
            if (item.getStock() < 0) {
                ErrorResponse error = new ErrorResponse("Stock cannot be negative", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            if (item.getPrice() <= 0) {
                ErrorResponse error = new ErrorResponse("Price must be positive", 400);
                ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                return;
            }
            
            Long id = itemRepository.insert(item);
            ctx.getResponse().status(201).send(objectMapper.valueToTree(item).toString());
        });
    }

    private void updateItem(Context ctx, Long id) {
        itemRepository.findById(id).ifPresentOrElse(
            existingItem -> {
                ctx.parse(ItemRequest.class).then(request -> {
                    // Check if the item has been sold
                    int minimumStock = itemRepository.getMinimumRequiredStock(id);
                    
                    if (request.getStock() < minimumStock) {
                        ErrorResponse error = new ErrorResponse(
                            "Cannot update stock below " + minimumStock + " as these items have been sold", 
                            400
                        );
                        ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
                        return;
                    }
                    
                    existingItem.setName(request.getName());
                    existingItem.setColor(request.getColor());
                    existingItem.setPrice(request.getPrice());
                    existingItem.setStock(request.getStock());
                    
                    itemRepository.update(existingItem);
                    ctx.render(objectMapper.valueToTree(existingItem));
                });
            },
            () -> {
                ErrorResponse error = new ErrorResponse("Item not found", 404);
                ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
            }
        );
    }

    private void deleteItem(Context ctx, Long id) {
        if (itemRepository.hasBeenSold(id)) {
            ErrorResponse error = new ErrorResponse("Cannot delete item that has been sold", 400);
            ctx.getResponse().status(400).send(objectMapper.valueToTree(error).toString());
            return;
        }
        
        itemRepository.findById(id).ifPresentOrElse(
            item -> {
                itemRepository.delete(id);
                ctx.getResponse().status(204).send();
            },
            () -> {
                ErrorResponse error = new ErrorResponse("Item not found", 404);
                ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
            }
        );
    }
}
