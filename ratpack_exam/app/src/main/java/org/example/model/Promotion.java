
package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Promotion {
    private Long id;
    private Long itemId;
    private double discountPercentage;
    private int quota;
    private int limitPerCustomer;
    private boolean enabled;
    private int usedQuota;

    public Promotion() {
    }

    public Promotion(Long id, Long itemId, double discountPercentage, int quota, int limitPerCustomer, boolean enabled, int usedQuota) {
        this.id = id;
        this.itemId = itemId;
        this.discountPercentage = discountPercentage;
        this.quota = quota;
        this.limitPerCustomer = limitPerCustomer;
        this.enabled = enabled;
        this.usedQuota = usedQuota;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @JsonProperty
    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @JsonProperty
    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    @JsonProperty
    public int getLimitPerCustomer() {
        return limitPerCustomer;
    }

    public void setLimitPerCustomer(int limitPerCustomer) {
        this.limitPerCustomer = limitPerCustomer;
    }

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public int getUsedQuota() {
        return usedQuota;
    }

    public void setUsedQuota(int usedQuota) {
        this.usedQuota = usedQuota;
    }
}
