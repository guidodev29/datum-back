package com.datum.application.dto;

/**
 * Request DTO for rejecting a purchase
 */
public class RejectRequest {

    public String notes;

    public RejectRequest() {
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
