package com.datum.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase domain entity
 * Represents a purchase/expense made by a user
 */
public class Purchase {

    private Long idPurchase;
    private Long idUser;
    private Long idFolder;
    private Long idPType;
    private Long idPaymentMethod;
    private Long idCostCenter;
    private BigDecimal totalAmount;
    private String description;
    private String guestName;
    private LocalDateTime purchaseDate;
    private String imgUrl;  // Document path in OpenKM
    private String validationStatus;  // DRAFT, UNDER_REVIEW, VALIDATED, REJECTED
    private LocalDateTime validatedDate;
    private Long validatedBy;
    private String validationNotes;
    private LocalDateTime createdDate;

    // Constructor
    public Purchase() {
    }

    public Purchase(Long idUser, Long idFolder, Long idPType, Long idPaymentMethod,
                   BigDecimal totalAmount, String description, LocalDateTime purchaseDate) {
        this.idUser = idUser;
        this.idFolder = idFolder;
        this.idPType = idPType;
        this.idPaymentMethod = idPaymentMethod;
        this.totalAmount = totalAmount;
        this.description = description;
        this.purchaseDate = purchaseDate;
        this.validationStatus = "DRAFT";
        this.createdDate = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Check if purchase can be edited
     * Only DRAFT purchases can be edited
     */
    public boolean canEdit() {
        return "DRAFT".equals(this.validationStatus);
    }

    /**
     * Check if purchase can be deleted
     * Only DRAFT and REJECTED purchases can be deleted
     */
    public boolean canDelete() {
        return "DRAFT".equals(this.validationStatus) || "REJECTED".equals(this.validationStatus);
    }

    /**
     * Check if purchase can be submitted for review
     * Only DRAFT purchases can be submitted
     */
    public boolean canSubmitForReview() {
        return "DRAFT".equals(this.validationStatus);
    }

    /**
     * Submit purchase for review
     */
    public void submitForReview() {
        if (!canSubmitForReview()) {
            throw new IllegalStateException("Cannot submit purchase with status: " + this.validationStatus);
        }
        this.validationStatus = "UNDER_REVIEW";
    }

    /**
     * Validate purchase amount is positive
     */
    public void validateAmount() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase amount must be greater than zero");
        }
    }

    /**
     * Set document URL (OpenKM path)
     */
    public void setDocumentUrl(String url) {
        if (!canEdit()) {
            throw new IllegalStateException("Cannot modify document for non-DRAFT purchase");
        }
        this.imgUrl = url;
    }

    /**
     * Check if purchase has a document attached
     */
    public boolean hasDocument() {
        return imgUrl != null && !imgUrl.trim().isEmpty();
    }

    // Getters and Setters

    public Long getIdPurchase() {
        return idPurchase;
    }

    public void setIdPurchase(Long idPurchase) {
        this.idPurchase = idPurchase;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getIdFolder() {
        return idFolder;
    }

    public void setIdFolder(Long idFolder) {
        this.idFolder = idFolder;
    }

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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
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

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public LocalDateTime getValidatedDate() {
        return validatedDate;
    }

    public void setValidatedDate(LocalDateTime validatedDate) {
        this.validatedDate = validatedDate;
    }

    public Long getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(Long validatedBy) {
        this.validatedBy = validatedBy;
    }

    public String getValidationNotes() {
        return validationNotes;
    }

    public void setValidationNotes(String validationNotes) {
        this.validationNotes = validationNotes;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
