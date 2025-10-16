package com.datum.auth;

public class LoginRequest {
    public String username;
    public String password;
    
    // Constructor vacío
    public LoginRequest() {}
    
    // Constructor con parámetros
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}