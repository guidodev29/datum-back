package com.datum.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    @JsonProperty("access_token")
    public String access_token;
    
    @JsonProperty("refresh_token")
    public String refresh_token;
    
    @JsonProperty("expires_in")
    public int expires_in;
    
    @JsonProperty("token_type")
    public String token_type;
}