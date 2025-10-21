package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.CreateFolderRequest;
import com.datum.application.dto.FolderResponse;
import com.datum.domain.model.Folder;
import com.datum.domain.ports.in.FolderUseCasePort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/users/{userId}/folders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserFolderResource {

    @Inject
    FolderUseCasePort folderService;

    @POST
    @RolesAllowed({"administrator", "employee"})
    public Response createFolder(
        @PathParam("userId") Long userId,
        CreateFolderRequest request
    ) {
        try {
            Folder folder = toDomain(request, userId);
            Folder created = folderService.createFolder(folder);
            
            return Response.status(Response.Status.CREATED)
                .entity(toResponse(created))
                .build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @GET
    @RolesAllowed({"administrator", "employee"})
    public Response getUserFolders(@PathParam("userId") Long userId) {
        List<FolderResponse> folders = folderService.getFoldersByUserId(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
            
        return Response.ok(folders).build();
    }

    @GET
    @Path("/{folderId}")
    @RolesAllowed({"administrator", "employee"})
    public Response getFolderById(
        @PathParam("userId") Long userId,
        @PathParam("folderId") Long folderId
    ) {
        try {
            Folder folder = folderService.getFolderById(folderId);
            
            // Verify folder belongs to user
            if (!folder.getUserId().equals(userId)) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("Folder does not belong to this user")
                    .build();
            }
            
            return Response.ok(toResponse(folder)).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{folderId}")
    @RolesAllowed({"administrator", "employee"})
    public Response updateFolder(
        @PathParam("userId") Long userId,
        @PathParam("folderId") Long folderId,
        CreateFolderRequest request
    ) {
        try {
            Folder existing = folderService.getFolderById(folderId);
            
            // Verify folder belongs to user
            if (!existing.getUserId().equals(userId)) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("Folder does not belong to this user")
                    .build();
            }
            
            Folder folder = toDomain(request, userId);
            Folder updated = folderService.updateFolder(folderId, folder);
            
            return Response.ok(toResponse(updated)).build();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/{folderId}")
    @RolesAllowed({"administrator", "employee"})
    public Response deleteFolder(
        @PathParam("userId") Long userId,
        @PathParam("folderId") Long folderId
    ) {
        try {
            Folder folder = folderService.getFolderById(folderId);
            
            // Verify folder belongs to user
            if (!folder.getUserId().equals(userId)) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("Folder does not belong to this user")
                    .build();
            }
            
            folderService.deleteFolder(folderId);
            return Response.noContent().build();
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        }
    }

    // Conversion: Request → Domain
    private Folder toDomain(CreateFolderRequest request, Long userId) {
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setFolderName(request.folderName);
        folder.setDescription(request.description);
        
        if (request.startDate != null && !request.startDate.isEmpty()) {
            folder.setStartDate(LocalDate.parse(request.startDate));
        }
        
        if (request.endDate != null && !request.endDate.isEmpty()) {
            folder.setEndDate(LocalDate.parse(request.endDate));
        }
        
        return folder;
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