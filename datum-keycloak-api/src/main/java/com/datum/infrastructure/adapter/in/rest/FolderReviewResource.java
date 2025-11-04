package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.FolderResponse;
import com.datum.application.dto.PurchaseResponse;
import com.datum.application.service.PurchaseService;
import com.datum.domain.model.Folder;
import com.datum.domain.ports.in.FolderUseCasePort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/folders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FolderReviewResource {

    @Inject
    FolderUseCasePort folderService;

    @Inject
    PurchaseService purchaseService;

    /**
     * Get folders that are under review
     * GET /api/folders/review
     * Optional query parameter: userId (filter by specific user)
     * Example: GET /api/folders/review?userId=123
     */
    @GET
    @Path("/review")
    @RolesAllowed({"administrator", "finance"})
    public Response getFoldersUnderReview(@QueryParam("userId") Long userId) {
        List<FolderResponse> folders;

        if (userId != null) {
            // Filter by specific user
            folders = folderService.getFoldersUnderReviewByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        } else {
            // Get all folders under review
            folders = folderService.getFoldersUnderReview().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        }

        return Response.ok(folders).build();
    }

    /**
     * Get all purchases from a specific folder
     * GET /api/folders/{folderId}/purchases
     * Accessible by: employee, finance, administrator
     */
    @GET
    @Path("/{folderId}/purchases")
    @RolesAllowed({"employee", "finance", "administrator"})
    public Response getPurchasesByFolder(@PathParam("folderId") Long folderId) {
        try {
            // Get all purchases for the folder
            List<PurchaseResponse> purchases = purchaseService.getPurchasesByFolderId(folderId).stream()
                .map(PurchaseResponse::fromDomain)
                .collect(Collectors.toList());

            return Response.ok(purchases).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error retrieving purchases: " + e.getMessage()))
                .build();
        }
    }

    // Conversion: Domain → Response
    private FolderResponse toResponse(Folder folder) {
        FolderResponse response = new FolderResponse();
        response.id = folder.getId();
        response.userId = folder.getUserId();
        response.folderName = folder.getFolderName();
        response.description = folder.getDescription();

        if (folder.getStartDate() != null) {
            response.startDate = folder.getStartDate().toString();
        }

        if (folder.getEndDate() != null) {
            response.endDate = folder.getEndDate().toString();
        }

        response.validationStatus = folder.getValidationStatus() != null
            ? folder.getValidationStatus().name()
            : "DRAFT";

        if (folder.getValidatedDate() != null) {
            response.validatedDate = folder.getValidatedDate().toString();
        }

        response.validatedBy = folder.getValidatedBy();
        response.validationNotes = folder.getValidationNotes();
        response.canEdit = folder.canEdit();

        return response;
    }

    // Helper class for error responses
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
