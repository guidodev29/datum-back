package com.datum.infrastructure.adapter.out.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class KeycloakUserRequest {
    
    @JsonProperty("username")
    public String username;
    
    @JsonProperty("email")
    public String email;
    
    @JsonProperty("firstName")
    public String firstName;
    
    @JsonProperty("lastName")
    public String lastName;
    
    @JsonProperty("enabled")
    public boolean enabled;
    
    @JsonProperty("credentials")
    public List<CredentialRepresentation> credentials;

    @JsonProperty("attributes")
    public Map<String, List<String>> attributes;
    
    public KeycloakUserRequest() {
        this.enabled = true;
    }
    
    public static class CredentialRepresentation {
        @JsonProperty("type")
        public String type;
        
        @JsonProperty("value")
        public String value;
        
        @JsonProperty("temporary")
        public boolean temporary;
        
        public CredentialRepresentation(String password, boolean temporary) {
            this.type = "password";
            this.value = password;
            this.temporary = temporary;
        }
    }
}