package com.datum.application.dto;

public class FolderResponse {
    public Long id;
    public Long userId;
    public String folderName;
    public String description;
    public String startDate;
    public String endDate;
    public String validationStatus;
    public String validatedDate;
    public Long validatedBy;
    public String validationNotes;
    public boolean canEdit;

    public FolderResponse() {
    }
}