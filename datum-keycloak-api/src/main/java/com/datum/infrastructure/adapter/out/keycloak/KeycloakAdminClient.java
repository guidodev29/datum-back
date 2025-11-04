package com.datum.infrastructure.adapter.out.keycloak;

import com.datum.infrastructure.adapter.out.keycloak.dto.KeycloakUserRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "keycloak-admin-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeycloakAdminClient {

    @POST
    @Path("/admin/realms/datum/users")
    Response createUser(
        @HeaderParam("Authorization") String authorization,
        KeycloakUserRequest userRequest
    );

    @GET
    @Path("/admin/realms/datum/users/{userId}")
    Response getUser(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId
    );

    @PUT
    @Path("/admin/realms/datum/users/{userId}/reset-password")
    Response resetPassword(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId,
        KeycloakUserRequest.CredentialRepresentation credential
    );

    @GET
    @Path("/admin/realms/datum/roles/{roleName}")
    Map<String, Object> getRoleByName(
        @HeaderParam("Authorization") String authorization,
        @PathParam("roleName") String roleName
    );

    @POST
    @Path("/admin/realms/datum/users/{userId}/role-mappings/realm")
    Response assignRole(
        @HeaderParam("Authorization") String authorization,
        @PathParam("userId") String userId,
        List<Map<String, Object>> roles
    );
}