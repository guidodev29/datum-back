package com.datum.application.dto;

/**
 * DTO for updating a purchase with optional document replacement
 * Used in PUT endpoint to update purchase data and/or its attached document
 */
public class UpdatePurchaseRequest {
    private Long idPType;
    private Long idPaymentMethod;
    private Long idCostCenter;
    private java.math.BigDecimal totalAmount;
    private String description;
    private String guestName;
    private String purchaseDate; // Format: "2025-10-30"

    // Constructors
    public UpdatePurchaseRequest() {}

    // Getters and Setters
    public Long getIdPType() {
        return idPType;
    }

    public void setIdPType(Long idPType) {
        this.idPType = idPType;
    }

    public Long getIdPaymentMethod() {
        return idPaymentMethod;
    }

    public void setIdPaymentMethod(Long idPaymentMethod) {
        this.idPaymentMethod = idPaymentMethod;
    }

    public Long getIdCostCenter() {
        return idCostCenter;
    }

    public void setIdCostCenter(Long idCostCenter) {
        this.idCostCenter = idCostCenter;
    }

    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
