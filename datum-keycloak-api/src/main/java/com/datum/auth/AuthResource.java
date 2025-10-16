package com.datum.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;

import java.util.Arrays;

@Path("/auth")
public class AuthResource {

    @Inject
    @RestClient
    KeycloakClient keycloakClient;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> login(LoginRequest loginRequest) {
        
        return keycloakClient.getToken(
            "password",
            "datum-react-app",
            loginRequest.username,
            loginRequest.password
        )
        .onItem().transform(tokenResponse -> {
            // Login exitoso
            UserInfo userInfo = extractUserInfoFromToken(tokenResponse.access_token, loginRequest.username);

            AuthResponse authResponse = new AuthResponse(
                true,
                tokenResponse.access_token,
                tokenResponse.refresh_token,
                tokenResponse.token_type != null ? tokenResponse.token_type : "Bearer",
                tokenResponse.expires_in,
                userInfo
            );
            return Response.ok(authResponse).build();
        })
        .onFailure().recoverWithItem(throwable -> {
            // Login fallido
            AuthResponse authResponse = new AuthResponse(
                false, 
                "Invalid credentials or user does not exist"
            );
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(authResponse).build();
        });
    }

    // Endpoint de prueba para hello
    @GET
    @Path("/hello")
    @Produces(MediaType.TEXT_PLAIN)
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
            // Decodificar el JWT sin verificar la firma (solo para extraer claims)
            String[] chunks = token.split("\\.");
            if (chunks.length >= 2) {
                // Por ahora, creamos información básica del usuario
                // En un escenario real, podrías decodificar el JWT o hacer una llamada a Keycloak para obtener más info
                UserInfo userInfo = new UserInfo();
                userInfo.username = username;
                userInfo.email = username + "@example.com"; // Placeholder
                userInfo.id = "user_" + username.hashCode(); // ID temporal basado en username
                userInfo.roles = Arrays.asList("basic", "user"); // Roles por defecto

                return userInfo;
            }
        } catch (Exception e) {
            // En caso de error, devolver información básica
        }

        // Fallback: información mínima
        UserInfo fallbackInfo = new UserInfo();
        fallbackInfo.username = username;
        fallbackInfo.email = username + "@example.com";
        fallbackInfo.id = "user_" + username.hashCode();
        fallbackInfo.roles = Arrays.asList("basic");

        return fallbackInfo;
    }
}