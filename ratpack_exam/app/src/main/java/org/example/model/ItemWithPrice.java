
package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemWithPrice {
    private Long id;
    private String name;
    private String color;
    private double regularPrice;
    private double currentPrice;
    private int stock;
    private boolean hasPromotion;
    private double discountPercentage;

    public ItemWithPrice() {
    }

    public ItemWithPrice(Long id, String name, String color, double regularPrice, double currentPrice, int stock, boolean hasPromotion, double discountPercentage) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.regularPrice = regularPrice;
        this.currentPrice = currentPrice;
        this.stock = stock;
        this.hasPromotion = hasPromotion;
        this.discountPercentage = discountPercentage;
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
    public double getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(double regularPrice) {
        this.regularPrice = regularPrice;
    }

    @JsonProperty
    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    @JsonProperty
    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @JsonProperty
    public boolean isHasPromotion() {
        return hasPromotion;
    }

    public void setHasPromotion(boolean hasPromotion) {
        this.hasPromotion = hasPromotion;
    }

    @JsonProperty
    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
