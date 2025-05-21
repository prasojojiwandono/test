
package org.example.dto;

public class PromotionRequest {
    private Long itemId;
    private double discountPercentage;
    private int quota;
    private int limitPerCustomer;
    private boolean enabled;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getLimitPerCustomer() {
        return limitPerCustomer;
    }

    public void setLimitPerCustomer(int limitPerCustomer) {
        this.limitPerCustomer = limitPerCustomer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
