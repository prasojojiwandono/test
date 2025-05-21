
package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ratpack.server.RatpackServer;
import org.example.auth.BasicAuthHandler;
import org.example.handler.*;
import org.example.repository.ItemRepository;
import org.example.repository.PromotionRepository;
import org.example.repository.PurchaseRepository;
import org.example.service.DatabaseService;
import org.example.util.EncryptionUtil;

public class App {
    public static void main(String[] args) throws Exception {
        // Initialize services and repositories
        DatabaseService databaseService = new DatabaseService();
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        ObjectMapper objectMapper = new ObjectMapper();
        
        ItemRepository itemRepository = new ItemRepository(databaseService.getJdbi());
        PromotionRepository promotionRepository = new PromotionRepository(databaseService.getJdbi());
        PurchaseRepository purchaseRepository = new PurchaseRepository(databaseService.getJdbi(), encryptionUtil);
        
        // Initialize API handlers
        ItemHandler itemHandler = new ItemHandler(itemRepository, objectMapper);
        PromotionHandler promotionHandler = new PromotionHandler(promotionRepository, itemRepository, objectMapper);
        CustomerHandler customerHandler = new CustomerHandler(itemRepository, promotionRepository, purchaseRepository, objectMapper);
        AdminCustomerHandler adminCustomerHandler = new AdminCustomerHandler(itemRepository, purchaseRepository, objectMapper);
        
        // Initialize auth handler
        BasicAuthHandler basicAuthHandler = new BasicAuthHandler();
        
        RatpackServer.start(server -> server
            .serverConfig(config -> config
                .port(8080)
                .development(true)
            )
            .handlers(chain -> {
                // Public API endpoints
                chain
                    .path("api/v1/customer/items", ctx -> customerHandler.handle(ctx))
                    .path("api/v1/customer/purchase", ctx -> customerHandler.handle(ctx))
                    
                    // Admin API endpoints (protected by Basic Auth)
                    .prefix("api/v1/admin", adminChain -> {
                        adminChain
                            .all(basicAuthHandler)
                            .prefix("items", itemChain -> {
                                itemChain.all(ctx -> itemHandler.handle(ctx));
                            })
                            .prefix("promotions", promotionChain -> {
                                promotionChain.all(ctx -> promotionHandler.handle(ctx));
                            })
                            .prefix("customers", customerChain -> {
                                customerChain.all(ctx -> adminCustomerHandler.handle(ctx));
                            });
                    })
                    
                    // Root endpoint
                    .get(ctx -> ctx.render("Company samsung Online Store API"));
            })
        );
    }
}
