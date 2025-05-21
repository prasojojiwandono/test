
package org.example.repository;

import org.example.model.Purchase;
import org.example.util.EncryptionUtil;
import org.jdbi.v3.core.Jdbi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PurchaseRepository {
    private final Jdbi jdbi;
    private final EncryptionUtil encryptionUtil;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PurchaseRepository(Jdbi jdbi, EncryptionUtil encryptionUtil) {
        this.jdbi = jdbi;
        this.encryptionUtil = encryptionUtil;
    }

    public List<Purchase> findAllByItemId(Long itemId) {
        List<Purchase> purchases = jdbi.withHandle(handle -> 
            handle.createQuery("""
                SELECT id, item_id as itemId, customer_id_card as customerIdCard, 
                       customer_name as customerName, customer_phone as customerPhone, 
                       quantity, price_paid as pricePaid, promotion_id as promotionId, 
                       purchase_date as purchaseDate
                FROM purchases 
                WHERE item_id = :itemId
            """)
            .bind("itemId", itemId)
            .mapToBean(Purchase.class)
            .list()
        );
        
        // Decrypt sensitive data for each purchase
        purchases.forEach(this::decryptPurchase);
        
        return purchases;
    }

    public Long insert(Purchase purchase) {
        // Encrypt sensitive data
        Purchase encryptedPurchase = encryptPurchase(purchase);
        
        return jdbi.withHandle(handle -> {
            String now = LocalDateTime.now().format(formatter);
            
            Long id = handle.createUpdate("""
                INSERT INTO purchases 
                (item_id, customer_id_card, customer_name, customer_phone, quantity, price_paid, promotion_id, purchase_date) 
                VALUES (:itemId, :customerIdCard, :customerName, :customerPhone, :quantity, :pricePaid, :promotionId, :purchaseDate)
            """)
                .bind("itemId", encryptedPurchase.getItemId())
                .bind("customerIdCard", encryptedPurchase.getCustomerIdCard())
                .bind("customerName", encryptedPurchase.getCustomerName())
                .bind("customerPhone", encryptedPurchase.getCustomerPhone())
                .bind("quantity", encryptedPurchase.getQuantity())
                .bind("pricePaid", encryptedPurchase.getPricePaid())
                .bind("promotionId", encryptedPurchase.getPromotionId())
                .bind("purchaseDate", now)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one();
            
            purchase.setId(id);
            purchase.setPurchaseDate(now);
            return id;
        });
    }

    private Purchase encryptPurchase(Purchase purchase) {
        Purchase encryptedPurchase = new Purchase();
        encryptedPurchase.setId(purchase.getId());
        encryptedPurchase.setItemId(purchase.getItemId());
        encryptedPurchase.setCustomerIdCard(encryptionUtil.encrypt(purchase.getCustomerIdCard()));
        encryptedPurchase.setCustomerName(encryptionUtil.encrypt(purchase.getCustomerName()));
        encryptedPurchase.setCustomerPhone(encryptionUtil.encrypt(purchase.getCustomerPhone()));
        encryptedPurchase.setQuantity(purchase.getQuantity());
        encryptedPurchase.setPricePaid(purchase.getPricePaid());
        encryptedPurchase.setPromotionId(purchase.getPromotionId());
        encryptedPurchase.setPurchaseDate(purchase.getPurchaseDate());
        return encryptedPurchase;
    }

    private void decryptPurchase(Purchase purchase) {
        purchase.setCustomerIdCard(encryptionUtil.decrypt(purchase.getCustomerIdCard()));
        purchase.setCustomerName(encryptionUtil.decrypt(purchase.getCustomerName()));
        purchase.setCustomerPhone(encryptionUtil.decrypt(purchase.getCustomerPhone()));
    }
}
