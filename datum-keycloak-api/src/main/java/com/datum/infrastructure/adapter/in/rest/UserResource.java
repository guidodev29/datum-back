package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.CreateEmployeeResponse;
import com.datum.application.dto.CreateUserRequest;
import com.datum.application.dto.UserResponse;
import com.datum.domain.model.User;
import com.datum.domain.ports.in.UserUseCasePort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserUseCasePort userUseCase;

    @GET
    @RolesAllowed({"administrator"})
    public Response getAllUsers() {
        List<UserResponse> users = userUseCase.getAllUsers()
            .stream()
            .map(UserResponse::fromDomain)
            .collect(Collectors.toList());
        
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"administrator"})
    public Response getUserById(@PathParam("id") Long id) {
        return userUseCase.getUserById(id)
            .map(UserResponse::fromDomain)
            .map(user -> Response.ok(user).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/nickname/{nickname}")
    @RolesAllowed({"administrator"})
    public Response getUserByNickname(@PathParam("nickname") String nickname) {
        return userUseCase.getUserByNickname(nickname)
            .map(UserResponse::fromDomain)
            .map(user -> Response.ok(user).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @RolesAllowed({"administrator"})
    public Response createUser(@Valid CreateUserRequest request) {
        try {
            // Create user (which now also creates in Keycloak)
            User user = userUseCase.createUser(
                request.getFirstName(),
                request.getLastName(),
                request.getNickname(),
                request.getEmail(),
                request.getKeycloakId()
            );
            
            // Generate the temporary password again to show to admin
            // (we don't store it in the database for security)
            String temporaryPassword = request.getFirstName() + "@Datum" + java.time.Year.now().getValue();
            
            // Create response with user data AND temporary password
            CreateEmployeeResponse response = new CreateEmployeeResponse(
                UserResponse.fromDomain(user),
                temporaryPassword
            );
            
            return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
                
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"administrator"})
    public Response updateUser(@PathParam("id") Long id, @Valid CreateUserRequest request) {
        try {
            User user = userUseCase.updateUser(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getNickname(),
                request.getEmail()
            );
            
            return Response.ok(UserResponse.fromDomain(user)).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"administrator"})
    public Response deleteUser(@PathParam("id") Long id) {
        try {
            userUseCase.deleteUser(id);
            return Response.noContent().build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        }
    }

    // Inner class for error responses
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}