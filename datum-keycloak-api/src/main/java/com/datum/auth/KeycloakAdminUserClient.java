package com.datum.auth;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

@RegisterRestClient(configKey = "keycloak-admin-api")
@Path("/admin/realms/datum/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeycloakAdminUserClient {

    @GET
    @Path("/{userId}")
    Map<String, Object> getUser(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId
    );

    @PUT
    @Path("/{userId}/reset-password")
    Uni<Response> resetPassword(  // ← Change Response to Uni<Response>
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId,
        com.datum.infrastructure.adapter.out.keycloak.dto.KeycloakUserRequest.CredentialRepresentation credential
    );
}