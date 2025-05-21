
package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ratpack.handling.Context;
import io.ratpack.handling.Handler;
import org.example.dto.ErrorResponse;
import org.example.model.Purchase;
import org.example.repository.ItemRepository;
import org.example.repository.PurchaseRepository;

import java.util.List;

public class AdminCustomerHandler implements Handler {
    private final ItemRepository itemRepository;
    private final PurchaseRepository purchaseRepository;
    private final ObjectMapper objectMapper;

    public AdminCustomerHandler(ItemRepository itemRepository, PurchaseRepository purchaseRepository, ObjectMapper objectMapper) {
        this.itemRepository = itemRepository;
        this.purchaseRepository = purchaseRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        String path = ctx.getPathBinding().getPastBinding();
        String method = ctx.getRequest().getMethod().getName();
        
        if (path.matches("/items/\\d+/customers")) {
            if (method.equals("GET")) {
                Long itemId = Long.parseLong(path.split("/")[2]);
                getCustomersForItem(ctx, itemId);
            } else {
                ctx.clientError(405);
            }
        } else {
            ctx.next();
        }
    }

    private void getCustomersForItem(Context ctx, Long itemId) {
        // Verify that the item exists
        if (itemRepository.findById(itemId).isEmpty()) {
            ErrorResponse error = new ErrorResponse("Item not found", 404);
            ctx.getResponse().status(404).send(objectMapper.valueToTree(error).toString());
            return;
        }
        
        List<Purchase> purchases = purchaseRepository.findAllByItemId(itemId);
        ctx.render(objectMapper.writeValueAsString(purchases));
    }
}
