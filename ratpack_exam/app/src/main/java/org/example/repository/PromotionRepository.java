
package org.example.repository;

import org.example.model.Promotion;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class PromotionRepository {
    private final Jdbi jdbi;

    public PromotionRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Promotion> findAll() {
        return jdbi.withHandle(handle -> 
            handle.createQuery("""
                SELECT id, item_id as itemId, discount_percentage as discountPercentage, 
                       quota, limit_per_customer as limitPerCustomer, enabled, used_quota as usedQuota 
                FROM promotions
            """)
            .mapToBean(Promotion.class)
            .list()
        );
    }

    public Optional<Promotion> findById(Long id) {
        return jdbi.withHandle(handle -> 
            handle.createQuery("""
                SELECT id, item_id as itemId, discount_percentage as discountPercentage, 
                       quota, limit_per_customer as limitPerCustomer, enabled, used_quota as usedQuota 
                FROM promotions 
                WHERE id = :id
            """)
            .bind("id", id)
            .mapToBean(Promotion.class)
            .findOne()
        );
    }

    public Optional<Promotion> findActivePromotionForItem(Long itemId) {
        return jdbi.withHandle(handle -> 
            handle.createQuery("""
                SELECT id, item_id as itemId, discount_percentage as discountPercentage, 
                       quota, limit_per_customer as limitPerCustomer, enabled, used_quota as usedQuota 
                FROM promotions 
                WHERE item_id = :itemId AND enabled = 1 AND quota > used_quota
            """)
            .bind("itemId", itemId)
            .mapToBean(Promotion.class)
            .findOne()
        );
    }

    public Long insert(Promotion promotion) {
        return jdbi.withHandle(handle -> {
            Long id = handle.createUpdate("""
                INSERT INTO promotions (item_id, discount_percentage, quota, limit_per_customer, enabled, used_quota) 
                VALUES (:itemId, :discountPercentage, :quota, :limitPerCustomer, :enabled, :usedQuota)
            """)
                .bind("itemId", promotion.getItemId())
                .bind("discountPercentage", promotion.getDiscountPercentage())
                .bind("quota", promotion.getQuota())
                .bind("limitPerCustomer", promotion.getLimitPerCustomer())
                .bind("enabled", promotion.isEnabled())
                .bind("usedQuota", promotion.getUsedQuota())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one();
            
            promotion.setId(id);
            return id;
        });
    }

    public int update(Promotion promotion) {
        return jdbi.withHandle(handle -> 
            handle.createUpdate("""
                UPDATE promotions 
                SET item_id = :itemId, discount_percentage = :discountPercentage, 
                    quota = :quota, limit_per_customer = :limitPerCustomer, 
                    enabled = :enabled, used_quota = :usedQuota 
                WHERE id = :id
            """)
                .bind("id", promotion.getId())
                .bind("itemId", promotion.getItemId())
                .bind("discountPercentage", promotion.getDiscountPercentage())
                .bind("quota", promotion.getQuota())
                .bind("limitPerCustomer", promotion.getLimitPerCustomer())
                .bind("enabled", promotion.isEnabled())
                .bind("usedQuota", promotion.getUsedQuota())
                .execute()
        );
    }

    public int delete(Long id) {
        return jdbi.withHandle(handle ->
            handle.createUpdate("DELETE FROM promotions WHERE id = :id")
                .bind("id", id)
                .execute()
        );
    }

    public int incrementUsedQuota(Long id, int amount) {
        return jdbi.withHandle(handle ->
            handle.createUpdate("UPDATE promotions SET used_quota = used_quota + :amount WHERE id = :id")
                .bind("id", id)
                .bind("amount", amount)
                .execute()
        );
    }

    public boolean hasBeenUsed(Long promotionId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT used_quota > 0 FROM promotions WHERE id = :id")
                .bind("id", promotionId)
                .mapTo(Boolean.class)
                .findOne()
                .orElse(false)
        );
    }
}
