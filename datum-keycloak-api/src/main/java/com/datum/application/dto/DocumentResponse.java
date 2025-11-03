package com.datum.application.dto;

/**
 * Response DTO for document operations
 */
public class DocumentResponse {

    public Long purchaseId;
    public String fileName;
    public String mimeType;
    public Long fileSize;
    public String openkmPath;
    public String uploadDate;
    public String message;

    public DocumentResponse() {
    }

    public DocumentResponse(Long purchaseId, String fileName, String mimeType, Long fileSize, String openkmPath) {
        this.purchaseId = purchaseId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.openkmPath = openkmPath;
    }

    public static DocumentResponse success(Long purchaseId, String fileName, String mimeType, Long fileSize, String openkmPath, String message) {
        DocumentResponse response = new DocumentResponse(purchaseId, fileName, mimeType, fileSize, openkmPath);
        response.message = message;
        return response;
    }
}
