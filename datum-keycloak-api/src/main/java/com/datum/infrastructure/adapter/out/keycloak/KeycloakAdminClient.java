package com.datum.infrastructure.adapter.out.keycloak;

import com.datum.infrastructure.adapter.out.keycloak.dto.KeycloakUserRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "keycloak-admin-api")
@Path("/admin/realms/datum/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeycloakAdminClient {

    @POST
    Response createUser(
        @HeaderParam("Authorization") String authorization,
        KeycloakUserRequest userRequest
    );

    @GET
    @Path("/{userId}")
    Response getUser(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId
    );

    @PUT
    @Path("/{userId}/reset-password")
    Response resetPassword(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId,
        KeycloakUserRequest.CredentialRepresentation credential
    );

    @POST
    @Path("/users/{userId}/role-mappings/realm")
    Response assignRole(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId,
        List<Map<String, Object>> roles
    );
}