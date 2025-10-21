package com.datum.application.dto;

public class CreateFolderRequest {
    public String folderName;
    public String description;
    public String startDate;  // Format: "2025-10-01"
    public String endDate;    // Format: "2025-10-31"

    public CreateFolderRequest() {
    }
}