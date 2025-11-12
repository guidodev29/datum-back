package com.datum.infrastructure.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Handle incoming requests - intercept OPTIONS before authentication
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // If this is an OPTIONS request, respond immediately with 200 OK
        // This bypasses authentication for CORS preflight
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(
                Response.ok()
                    .header("Access-Control-Allow-Origin", "http://localhost:5173")
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Headers", 
                        "origin, content-type, accept, authorization, x-requested-with")
                    .header("Access-Control-Allow-Methods", 
                        "GET, POST, PUT, DELETE, OPTIONS, PATCH")
                    .header("Access-Control-Max-Age", "3600")
                    .build()
            );
        }
    }

    /**
     * Add CORS headers to all responses
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {
        
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:5173");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "origin, content-type, accept, authorization, x-requested-with");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        responseContext.getHeaders().add("Access-Control-Max-Age", "3600");
    }
}