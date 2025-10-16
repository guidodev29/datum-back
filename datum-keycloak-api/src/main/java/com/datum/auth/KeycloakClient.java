package com.datum.auth;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;

@RegisterRestClient(configKey="keycloak-api")
@Path("/realms/datum/protocol/openid-connect")
public interface KeycloakClient {
    
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Uni<TokenResponse> getToken(
        @FormParam("grant_type") String grantType,
        @FormParam("client_id") String clientId,
        @FormParam("username") String username,
        @FormParam("password") String password
    );
}