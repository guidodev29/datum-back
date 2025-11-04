package com.datum.application.dto;

/**
 * Request DTO for rejecting a folder
 */
public class RejectFolderRequest {

    public String notes;

    public RejectFolderRequest() {
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
