
package org.example.repository;

import org.example.model.Item;
import org.example.model.ItemWithPrice;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class ItemRepository {
    private final Jdbi jdbi;

    public ItemRepository(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<Item> findAll() {
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT id, name, color, price, stock FROM items")
                .mapToBean(Item.class)
                .list()
        );
    }

    public List<ItemWithPrice> findAvailableItemsWithPrice() {
        String query = """
            SELECT i.id, i.name, i.color, i.price AS regularPrice, 
                CASE 
                    WHEN p.id IS NOT NULL AND p.enabled = 1 THEN i.price * (1 - p.discount_percentage / 100.0) 
                    ELSE i.price 
                END AS currentPrice, 
                i.stock, 
                CASE WHEN p.id IS NOT NULL AND p.enabled = 1 THEN 1 ELSE 0 END AS hasPromotion,
                COALESCE(p.discount_percentage, 0) as discountPercentage
            FROM items i
            LEFT JOIN promotions p ON i.id = p.item_id AND p.enabled = 1
            WHERE i.stock > 0
        """;

        return jdbi.withHandle(handle -> 
            handle.createQuery(query)
                .mapToBean(ItemWithPrice.class)
                .list()
        );
    }

    public Optional<Item> findById(Long id) {
        return jdbi.withHandle(handle -> 
            handle.createQuery("SELECT id, name, color, price, stock FROM items WHERE id = :id")
                .bind("id", id)
                .mapToBean(Item.class)
                .findOne()
        );
    }

    public Long insert(Item item) {
        return jdbi.withHandle(handle -> {
            Long id = handle.createUpdate("INSERT INTO items (name, color, price, stock) VALUES (:name, :color, :price, :stock)")
                .bind("name", item.getName())
                .bind("color", item.getColor())
                .bind("price", item.getPrice())
                .bind("stock", item.getStock())
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one();
            
            item.setId(id);
            return id;
        });
    }

    public int update(Item item) {
        return jdbi.withHandle(handle -> 
            handle.createUpdate("UPDATE items SET name = :name, color = :color, price = :price, stock = :stock WHERE id = :id")
                .bind("id", item.getId())
                .bind("name", item.getName())
                .bind("color", item.getColor())
                .bind("price", item.getPrice())
                .bind("stock", item.getStock())
                .execute()
        );
    }

    public int delete(Long id) {
        return jdbi.withHandle(handle ->
            handle.createUpdate("DELETE FROM items WHERE id = :id")
                .bind("id", id)
                .execute()
        );
    }

    public int getItemSoldCount(Long itemId) {
        return jdbi.withHandle(handle ->
            handle.createQuery("SELECT SUM(quantity) FROM purchases WHERE item_id = :itemId")
                .bind("itemId", itemId)
                .mapTo(Integer.class)
                .findOne()
                .orElse(0)
        );
    }

    public boolean hasBeenSold(Long itemId) {
        return getItemSoldCount(itemId) > 0;
    }

    public int getMinimumRequiredStock(Long itemId) {
        return getItemSoldCount(itemId);
    }
}
