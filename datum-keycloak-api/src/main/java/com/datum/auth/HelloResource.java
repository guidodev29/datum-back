package com.datum.auth;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;  // ADD THIS
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;  // ADD THIS

@ApplicationScoped

@Path("/hello")
public class HelloResource {

    // ADD THIS - dummy injection to force CDI registration
    @Inject
    @ConfigProperty(name = "quarkus.http.port")
    String port;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll 
    public String hello() {
        return "Hello from Quarkus REST on port " + port;
    }
}