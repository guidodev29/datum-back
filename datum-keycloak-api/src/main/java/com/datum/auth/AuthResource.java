package com.datum.auth;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import java.util.Arrays;

import com.datum.domain.ports.out.UserRepositoryPort;
import com.datum.domain.model.User;

@Path("/auth")
public class AuthResource {

    @Inject
    @RestClient
    KeycloakClient keycloakClient;

    @Inject
    @RestClient
    KeycloakAdminUserClient keycloakAdminUserClient;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Uni<Response> login(LoginRequest loginRequest) {

        return keycloakClient.getToken(
                "password",
                "datum-react-app",
                loginRequest.username,
                loginRequest.password)
                .onItem().transform(tokenResponse -> {
                    // Login successful - extract user info
                    UserInfo userInfo = extractUserInfoFromToken(tokenResponse.access_token, loginRequest.username);

                    // Check if password change is required
                    boolean passwordChangeRequired = false;

                    AuthResponse authResponse = new AuthResponse(
                            true,
                            tokenResponse.access_token,
                            tokenResponse.refresh_token,
                            tokenResponse.token_type != null ? tokenResponse.token_type : "Bearer",
                            tokenResponse.expires_in,
                            userInfo);
                    authResponse.passwordChangeRequired = passwordChangeRequired; // Add this field

                    return Response.ok(authResponse).build();
                })
                .onFailure().recoverWithItem(throwable -> {
                    // Login failed
                    AuthResponse authResponse = new AuthResponse(
                            false,
                            "Invalid credentials or user does not exist");
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(authResponse).build();
                });
    }

    // Endpoint de prueba para hello
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    public String hello() {
        return "Hello, world!";
    }

    /**
     * Extrae información del usuario desde el JWT token.
     * Como el token de Keycloak puede no contener toda la información,
     * creamos un UserInfo básico con los datos disponibles.
     */
    private UserInfo extractUserInfoFromToken(String token, String username) {
        try {
            // Decode JWT token payload
            String[] chunks = token.split("\\.");
            if (chunks.length >= 2) {
                // Decode Base64 payload
                String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));

                // Parse JSON
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);

                // Extract roles from realm_access.roles
                java.util.List<String> roles = new java.util.ArrayList<>();
                if (jsonNode.has("realm_access") && jsonNode.get("realm_access").has("roles")) {
                    jsonNode.get("realm_access").get("roles").forEach(role -> roles.add(role.asText()));
                }

                // Extract user info
                String extractedUsername = jsonNode.has("preferred_username")
                        ? jsonNode.get("preferred_username").asText()
                        : username;
                String email = jsonNode.has("email")
                        ? jsonNode.get("email").asText()
                        : username + "@example.com";
                String userId = jsonNode.has("sub")
                        ? jsonNode.get("sub").asText()
                        : "user_" + username.hashCode();

                UserInfo userInfo = new UserInfo();
                userInfo.username = extractedUsername;
                userInfo.email = email;
                userInfo.id = userId; // ⭐ Just return Keycloak ID (we'll use hardcoded 81 in frontend)
                userInfo.roles = roles.isEmpty() ? Arrays.asList("basic") : roles;

                return userInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback
        UserInfo fallbackInfo = new UserInfo();
        fallbackInfo.username = username;
        fallbackInfo.email = username + "@example.com";
        fallbackInfo.id = "user_" + username.hashCode();
        fallbackInfo.roles = Arrays.asList("basic");

        return fallbackInfo;
    }

    // NOW add the new method AFTER
    private boolean checkPasswordChangeRequired(String userId, String accessToken) {
        try {
            // Get admin token
            TokenResponse adminToken = keycloakClient.getToken(
                    "password",
                    "admin-cli",
                    "admin",
                    "admin").await().indefinitely();

            // Get user details
            Map<String, Object> user = keycloakAdminUserClient.getUser(
                    "Bearer " + adminToken.access_token,
                    userId);

            // Check required actions
            List<String> requiredActions = (List<String>) user.get("requiredActions");
            return requiredActions != null && requiredActions.contains("UPDATE_PASSWORD");

        } catch (Exception e) {
            System.err.println("Error checking required actions: " + e.getMessage());
            return false;
        }
    }

    @POST
    @Path("/change-password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Response> changePassword(
            @HeaderParam("Authorization") String authorization,
            ChangePasswordRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("success", false, "message", "Missing or invalid token"))
                            .build());
        }

        String token = authorization.substring(7);

        try {
            // Extract user ID from token
            String[] chunks = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(payload);
            String userId = jsonNode.get("sub").asText();

            // Get admin token to update password
            return keycloakClient.getToken(
                    "password",
                    "datum-react-app",
                    "admin",
                    "datum2025")
                    .onItem().transformToUni(adminToken -> {
                        // Update password in Keycloak
                        com.datum.infrastructure.adapter.out.keycloak.dto.KeycloakUserRequest.CredentialRepresentation newCredential = new com.datum.infrastructure.adapter.out.keycloak.dto.KeycloakUserRequest.CredentialRepresentation(
                                request.newPassword,
                                false);

                        return keycloakAdminUserClient.resetPassword(
                                "Bearer " + adminToken.access_token,
                                userId,
                                newCredential);
                    })
                    .onItem().transform(keycloakResponse -> {
                        int status = keycloakResponse.getStatus();
                        System.out.println("DEBUG: Keycloak resetPassword status: " + status);

                        if (status == 204 || status == 200) {
                            return Response.ok(Map.of(
                                    "success", true,
                                    "message", "Password changed successfully")).build();
                        } else {
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity(Map.of("success", false, "message", "Keycloak error: " + status))
                                    .build();
                        }
                    })
                    .onFailure().recoverWithItem(error -> {
                        System.err.println("ERROR: " + error.getMessage());
                        error.printStackTrace();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(Map.of("success", false, "message", "Failed: " + error.getMessage()))
                                .build();
                    });

        } catch (Exception e) {
            return Uni.createFrom().item(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("success", false, "message", "Invalid token: " + e.getMessage()))
                            .build());
        }
    }

}
