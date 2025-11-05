package com.datum.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Folder {
    private Long id;
    private Long userId;
    private String folderName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private FolderStatus validationStatus;
    private LocalDateTime validatedDate;
    private Long validatedBy;
    private String validationNotes;

    public Folder() {
        this.validationStatus = FolderStatus.DRAFT;
    }

    // Business validation
    public boolean canEdit() {
        return this.validationStatus == FolderStatus.DRAFT;
    }

    public boolean canSubmitForReview() {
        return this.validationStatus == FolderStatus.DRAFT || this.validationStatus == FolderStatus.REJECTED;
    }

    public void submitForReview() {
        if (!canSubmitForReview()) {
            throw new IllegalStateException("Cannot submit folder with status: " + this.validationStatus);
        }

        // Clear previous validation data if resubmitting a rejected folder
        if (this.validationStatus == FolderStatus.REJECTED) {
            this.validatedBy = null;
            this.validatedDate = null;
            this.validationNotes = null;
        }

        this.validationStatus = FolderStatus.UNDER_REVIEW;
    }

    public void validateDateRange() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public FolderStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(FolderStatus validationStatus) {
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
}