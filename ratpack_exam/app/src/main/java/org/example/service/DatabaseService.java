
package org.example.service;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseService {
    private final Jdbi jdbi;

    public DatabaseService() {
        // Initialize SQLite DB
        String dbPath = "company_samsung_store.db";
        
        // Create database file if it doesn't exist
        if (!Files.exists(Paths.get(dbPath))) {
            try {
                Path path = Paths.get(dbPath).toAbsolutePath();
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create database file", e);
            }
        }

        // Initialize Jdbi with SQLite
        this.jdbi = Jdbi.create("jdbc:sqlite:" + dbPath);
        this.jdbi.installPlugin(new SqlObjectPlugin());
        
        // Initialize tables
        initializeTables();
    }

    private void initializeTables() {
        jdbi.useTransaction(handle -> {
            // Items table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    color TEXT NOT NULL,
                    price REAL NOT NULL,
                    stock INTEGER NOT NULL
                )
            """);
            
            // Promotions table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS promotions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    discount_percentage REAL NOT NULL,
                    quota INTEGER NOT NULL,
                    limit_per_customer INTEGER NOT NULL,
                    enabled BOOLEAN NOT NULL,
                    used_quota INTEGER DEFAULT 0,
                    FOREIGN KEY (item_id) REFERENCES items(id)
                )
            """);
            
            // Purchases table with encrypted sensitive data
            handle.execute("""
                CREATE TABLE IF NOT EXISTS purchases (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_id INTEGER NOT NULL,
                    customer_id_card TEXT NOT NULL,
                    customer_name TEXT NOT NULL,
                    customer_phone TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    price_paid REAL NOT NULL,
                    promotion_id INTEGER,
                    purchase_date TEXT NOT NULL,
                    FOREIGN KEY (item_id) REFERENCES items(id),
                    FOREIGN KEY (promotion_id) REFERENCES promotions(id)
                )
            """);
        });
    }

    public Jdbi getJdbi() {
        return jdbi;
    }
}
