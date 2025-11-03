package com.datum.application.dto;

import com.datum.domain.model.Purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for Purchase entity
 */
public class PurchaseResponse {

    public Long idPurchase;
    public Long idUser;
    public Long idFolder;
    public Long idPType;
    public Long idPaymentMethod;
    public Long idCostCenter;
    public BigDecimal totalAmount;
    public String description;
    public String guestName;
    public LocalDateTime purchaseDate;
    public String imgUrl;
    public String validationStatus;
    public LocalDateTime validatedDate;
    public Long validatedBy;
    public String validationNotes;
    public LocalDateTime createdDate;
    public boolean hasDocument;

    public PurchaseResponse() {
    }

    /**
     * Factory method to create response from domain model
     */
    public static PurchaseResponse fromDomain(Purchase purchase) {
        PurchaseResponse response = new PurchaseResponse();
        response.idPurchase = purchase.getIdPurchase();
        response.idUser = purchase.getIdUser();
        response.idFolder = purchase.getIdFolder();
        response.idPType = purchase.getIdPType();
        response.idPaymentMethod = purchase.getIdPaymentMethod();
        response.idCostCenter = purchase.getIdCostCenter();
        response.totalAmount = purchase.getTotalAmount();
        response.description = purchase.getDescription();
        response.guestName = purchase.getGuestName();
        response.purchaseDate = purchase.getPurchaseDate();
        response.imgUrl = purchase.getImgUrl();
        response.validationStatus = purchase.getValidationStatus();
        response.validatedDate = purchase.getValidatedDate();
        response.validatedBy = purchase.getValidatedBy();
        response.validationNotes = purchase.getValidationNotes();
        response.createdDate = purchase.getCreatedDate();
        response.hasDocument = purchase.hasDocument();
        return response;
    }
}
