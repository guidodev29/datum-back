package com.datum.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity for TB_PURCHASE table
 * Maps to Oracle database table
 */
@Entity
@Table(name = "TB_PURCHASE", schema = "C##DATUM")
public class PurchaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PURCHASE")
    private Long idPurchase;

    @Column(name = "ID_USER", nullable = false)
    private Long idUser;

    @Column(name = "ID_FOLDER", nullable = false)
    private Long idFolder;

    @Column(name = "ID_P_TYPE", nullable = false)
    private Long idPType;

    @Column(name = "ID_PAYMENT_METHOD", nullable = false)
    private Long idPaymentMethod;

    @Column(name = "ID_COST_CENTER")
    private Long idCostCenter;

    @Column(name = "TOTAL_AMOUNT", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "P_DESCRIPTION", length = 75)
    private String description;

    @Column(name = "GUEST_NAME", length = 100)
    private String guestName;

    @Column(name = "P_DATE")
    private LocalDateTime purchaseDate;

    @Column(name = "IMG_URL", length = 255)
    private String imgUrl;

    @Column(name = "VALIDATION_STATUS", length = 20)
    private String validationStatus;

    @Column(name = "VALIDATED_DATE")
    private LocalDateTime validatedDate;

    @Column(name = "VALIDATED_BY")
    private Long validatedBy;

    @Column(name = "VALIDATION_NOTES", length = 200)
    private String validationNotes;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    // Constructors
    public PurchaseEntity() {
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
