package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.FolderResponse;
import com.datum.domain.model.Folder;
import com.datum.domain.ports.in.FolderUseCasePort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/folders/review")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FolderReviewResource {

    @Inject
    FolderUseCasePort folderService;

    /**
     * Get all folders that are under review
     * GET /api/folders/review
     */
    @GET
    @RolesAllowed({"administrator", "finance"})
    public Response getFoldersUnderReview() {
        List<FolderResponse> folders = folderService.getFoldersUnderReview().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        return Response.ok(folders).build();
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
}
