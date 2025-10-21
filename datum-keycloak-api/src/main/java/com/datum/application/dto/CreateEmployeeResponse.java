package com.datum.application.dto;

public class CreateEmployeeResponse {
    
    private UserResponse user;
    private String temporaryPassword;
    private String message;

    public CreateEmployeeResponse() {
    }

    public CreateEmployeeResponse(UserResponse user, String temporaryPassword) {
        this.user = user;
        this.temporaryPassword = temporaryPassword;
        this.message = "Employee created successfully. Share these credentials with the employee.";
    }

    // Getters and Setters
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public String getTemporaryPassword() { return temporaryPassword; }
    public void setTemporaryPassword(String temporaryPassword) { this.temporaryPassword = temporaryPassword; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}