package com.datum.application.dto;

/**
 * Request DTO for approving a purchase
 */
public class ApproveRequest {

    public String notes;

    public ApproveRequest() {
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
