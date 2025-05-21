
package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    private Long id;
    private String name;
    private String color;
    private double price;
    private int stock;

    public Item() {
    }

    public Item(Long id, String name, String color, double price, int stock) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.price = price;
        this.stock = stock;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @JsonProperty
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @JsonProperty
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
