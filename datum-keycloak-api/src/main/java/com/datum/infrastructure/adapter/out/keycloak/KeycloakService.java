package com.datum.infrastructure.adapter.out.keycloak;

import com.datum.auth.TokenResponse;
import com.datum.infrastructure.adapter.out.keycloak.dto.KeycloakUserRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class KeycloakService {

    @Inject
    @RestClient
    KeycloakAdminClient keycloakAdminClient;

    @Inject
    @RestClient
    com.datum.auth.KeycloakClient keycloakClient;  // For getting admin token

    @ConfigProperty(name = "keycloak.admin.username", defaultValue = "admin")
    String adminUsername;

    @ConfigProperty(name = "keycloak.admin.password", defaultValue = "admin")
    String adminPassword;

    /**
     * Creates a user in Keycloak with a temporary password
     * @return Keycloak user ID if successful, null otherwise
     */
    public String createUser(String email, String firstName, String lastName, String temporaryPassword) {
        try {
            // Create user request
            KeycloakUserRequest userRequest = new KeycloakUserRequest();
            userRequest.username = email;
            userRequest.email = email;
            userRequest.firstName = firstName;
            userRequest.lastName = lastName;
            userRequest.enabled = true;
            
            // Set temporary password
            KeycloakUserRequest.CredentialRepresentation credential = 
                new KeycloakUserRequest.CredentialRepresentation(temporaryPassword, false);
            userRequest.credentials = List.of(credential);

            // Set custom attribute for temporary password
            userRequest.attributes = new java.util.HashMap<>();
            userRequest.attributes.put("temporary_password", List.of("true"));

            System.out.println("DEBUG: Creating user with attributes: " + userRequest.attributes);


            // Get admin token
            String authHeader = getAdminAuthToken();
            
            if (authHeader == null) {
                System.err.println("Failed to get admin token");
                return null;
            }

            // Create user in Keycloak
            Response response = keycloakAdminClient.createUser(authHeader, userRequest);

            System.out.println("Keycloak create user response status: " + response.getStatus());

            String keycloakUserId = null;
            
            if (response.getStatus() == 201) {
                // Extract user ID from Location header
                String location = response.getHeaderString("Location");
                keycloakUserId = location.substring(location.lastIndexOf('/') + 1);
                System.out.println("User created in Keycloak with ID: " + keycloakUserId);
                
                // Assign employee role
                assignEmployeeRole(keycloakUserId, authHeader);
                
                return keycloakUserId;
            } else {
                System.err.println("Failed to create user. Status: " + response.getStatus());
            }

            return null;
        } catch (Exception e) {
            System.err.println("Exception creating user in Keycloak: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void assignEmployeeRole(String userId, String authHeader) {
    try {
        // Role representation
        java.util.Map<String, Object> roleRepresentation = new java.util.HashMap<>();
        roleRepresentation.put("id", "employee"); // Role name
        roleRepresentation.put("name", "employee");
        
        // Assign role to user
        Response roleResponse = keycloakAdminClient.assignRole(
            authHeader,
            userId,
            List.of(roleRepresentation)
        );
        
        System.out.println("Role assignment status: " + roleResponse.getStatus());
        
    } catch (Exception e) {
        System.err.println("Error assigning employee role: " + e.getMessage());
        // Don't fail user creation if role assignment fails
    }
}

    /**
     * Generates temporary password: FirstName@Datum2025
     */
    public String generateTemporaryPassword(String firstName) {
        int currentYear = java.time.Year.now().getValue();
        return firstName + "@Datum" + currentYear;
    }

    /**
     * Gets admin JWT token from Keycloak
     */
    private String getAdminAuthToken() {
        try {
            TokenResponse tokenResponse = keycloakClient.getToken(
                "password",
                "admin-cli",  // Use admin-cli client
                adminUsername,
                adminPassword
            ).await().indefinitely();
            
            return "Bearer " + tokenResponse.access_token;
        } catch (Exception e) {
            System.err.println("Failed to get admin token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}