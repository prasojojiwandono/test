
package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Purchase {
    private Long id;
    private Long itemId;
    private String customerIdCard; // Encrypted in DB
    private String customerName;   // Encrypted in DB
    private String customerPhone;  // Encrypted in DB
    private int quantity;
    private double pricePaid;
    private Long promotionId;
    private String purchaseDate;

    public Purchase() {
    }

    public Purchase(Long id, Long itemId, String customerIdCard, String customerName, String customerPhone, int quantity, double pricePaid, Long promotionId, String purchaseDate) {
        this.id = id;
        this.itemId = itemId;
        this.customerIdCard = customerIdCard;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
        this.promotionId = promotionId;
        this.purchaseDate = purchaseDate;
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
    public String getCustomerIdCard() {
        return customerIdCard;
    }

    public void setCustomerIdCard(String customerIdCard) {
        this.customerIdCard = customerIdCard;
    }

    @JsonProperty
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @JsonProperty
    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    @JsonProperty
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @JsonProperty
    public double getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(double pricePaid) {
        this.pricePaid = pricePaid;
    }

    @JsonProperty
    public Long getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }

    @JsonProperty
    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
