package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.DocumentResponse;
import com.datum.application.service.PurchaseService;
import com.datum.domain.model.Purchase;
import com.datum.infrastructure.adapter.out.OpenKM.OpenKMService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * REST Resource for managing documents attached to purchases
 * Handles upload, download, and deletion of purchase documents via OpenKM
 */
@Path("/api/purchases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PurchaseDocumentResource {

    @Inject
    PurchaseService purchaseService;

    @Inject
    OpenKMService openKMService;

    // Allowed MIME types: images and PDFs
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/heic",
        "application/pdf"
    );

    // Max file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Create purchase and upload document in a single request
     * POST /api/purchases/document
     */
    @POST
    @Path("/document")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"employee", "administrator"})
    public Response createPurchaseWithDocument(
        @FormParam("idUser") Long idUser,
        @FormParam("idFolder") Long idFolder,
        @FormParam("idPType") Long idPType,
        @FormParam("idPaymentMethod") Long idPaymentMethod,
        @FormParam("idCostCenter") Long idCostCenter,
        @FormParam("totalAmount") java.math.BigDecimal totalAmount,
        @FormParam("description") String description,
        @FormParam("guestName") String guestName,
        @FormParam("purchaseDate") String purchaseDateStr,
        @FormParam("file") FileUpload file
    ) {
        try {
            // 1. Validate required fields
            if (idUser == null || idFolder == null || totalAmount == null || purchaseDateStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("idUser, idFolder, totalAmount, and purchaseDate are required"))
                    .build();
            }

            // 2. Validate file is provided
            if (file == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File is required"))
                    .build();
            }

            // 3. Validate file size
            File uploadedFile = file.uploadedFile().toFile();
            if (uploadedFile.length() > MAX_FILE_SIZE) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File size exceeds 10MB limit"))
                    .build();
            }

            // 4. Validate file type (images or PDF only)
            String mimeType = file.contentType();
            if (!ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Only images (JPG, PNG, HEIC) and PDF files are allowed"))
                    .build();
            }

            // 5. Parse purchase date (only date, time will be current moment)
            java.time.LocalDateTime purchaseDate;
            try {
                // Parse only the date part (e.g., "2025-10-30")
                java.time.LocalDate date = java.time.LocalDate.parse(purchaseDateStr);
                // Combine with current time
                purchaseDate = java.time.LocalDateTime.of(date, java.time.LocalTime.now());
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid purchaseDate format. Use format: 2025-10-30"))
                    .build();
            }

            // 6. Create Purchase in database (ID will be auto-generated)
            Purchase purchase = new Purchase();
            purchase.setIdUser(idUser);
            purchase.setIdFolder(idFolder);
            purchase.setIdPType(idPType);
            purchase.setIdPaymentMethod(idPaymentMethod);
            purchase.setIdCostCenter(idCostCenter);
            purchase.setTotalAmount(totalAmount);
            purchase.setDescription(description);
            purchase.setGuestName(guestName);
            purchase.setPurchaseDate(purchaseDate);
            purchase.setValidationStatus("DRAFT");
            purchase.setCreatedDate(java.time.LocalDateTime.now());

            Purchase savedPurchase = purchaseService.createPurchase(purchase);
            Long generatedId = savedPurchase.getIdPurchase();

            // 7. Upload document to OpenKM
            String fileName = file.fileName();
            String openkmPath = openKMService.uploadDocument(generatedId, purchaseDate, fileName, file);

            // 8. Update purchase with document path
            purchaseService.attachDocument(generatedId, openkmPath);

            // 9. Return success response
            DocumentResponse response = DocumentResponse.success(
                generatedId,
                fileName,
                mimeType,
                uploadedFile.length(),
                openkmPath,
                "Purchase created and document uploaded successfully"
            );

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("Validation error: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error creating purchase with document: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Upload a document (image or PDF) for an existing purchase
     * POST /api/purchases/{purchaseId}/document
     */
    @POST
    @Path("/{purchaseId}/document")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"employee", "administrator"})
    public Response uploadDocument(
        @PathParam("purchaseId") Long purchaseId,
        @FormParam("file") FileUpload file
    ) {
        try {
            // 1. Validate purchase exists
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            // 2. Validate file is provided
            if (file == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File is required"))
                    .build();
            }

            // 3. Validate file size
            File uploadedFile = file.uploadedFile().toFile();
            if (uploadedFile.length() > MAX_FILE_SIZE) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File size exceeds 10MB limit"))
                    .build();
            }

            // 4. Validate file type (images or PDF only)
            String mimeType = file.contentType();
            if (!ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Only images (JPG, PNG, HEIC) and PDF files are allowed"))
                    .build();
            }

            // 5. Delete old document if exists
            if (purchase.hasDocument()) {
                try {
                    openKMService.deleteDocument(purchase.getImgUrl());
                } catch (Exception e) {
                    // Log but continue - old document might already be deleted
                    System.err.println("Warning: Could not delete old document: " + e.getMessage());
                }
            }

            // 6. Upload document to OpenKM
            String fileName = file.fileName();
            String openkmPath = openKMService.uploadDocument(purchaseId, purchase.getPurchaseDate(), fileName, file);

            // 7. Update purchase with document path
            purchaseService.attachDocument(purchaseId, openkmPath);

            // 8. Return success response
            DocumentResponse response = DocumentResponse.success(
                purchaseId,
                fileName,
                mimeType,
                uploadedFile.length(),
                openkmPath,
                "Document uploaded successfully"
            );

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Purchase not found: " + e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error uploading document: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Download the document attached to a purchase
     * GET /api/purchases/{purchaseId}/document
     */
    @GET
    @Path("/{purchaseId}/document")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"employee", "administrator", "finance"})
    public Response downloadDocument(@PathParam("purchaseId") Long purchaseId) {
        try {
            // 1. Get purchase
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            // 2. Check if document exists
            if (!purchase.hasDocument()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("No document attached to this purchase"))
                    .build();
            }

            // 3. Download from OpenKM
            byte[] documentBytes = openKMService.downloadDocument(purchase.getImgUrl());

            // 4. Extract filename from path
            String filename = extractFilename(purchase.getImgUrl());

            // 5. Return file
            return Response.ok(documentBytes)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Purchase not found: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error downloading document: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Update purchase and its document
     * PUT /api/purchases/{purchaseId}/update
     *
     * - Can edit DRAFT and REJECTED purchases
     * - If purchase is REJECTED, it will be reset to DRAFT after editing
     * - Employee must resubmit the purchase for review after editing
     * - Can update data with or without uploading a new document
     */
    @PUT
    @Path("/{purchaseId}/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"employee", "administrator"})
    public Response updatePurchaseWithDocument(
        @PathParam("purchaseId") Long purchaseId,
        @FormParam("idPType") Long idPType,
        @FormParam("idPaymentMethod") Long idPaymentMethod,
        @FormParam("idCostCenter") Long idCostCenter,
        @FormParam("totalAmount") java.math.BigDecimal totalAmount,
        @FormParam("description") String description,
        @FormParam("guestName") String guestName,
        @FormParam("purchaseDate") String purchaseDateStr,
        @FormParam("file") FileUpload file
    ) {
        try {
            // 1. Get existing purchase
            Purchase existingPurchase = purchaseService.getPurchaseById(purchaseId);

            // 2. Validate purchase can be edited (must be DRAFT or REJECTED)
            if (!existingPurchase.canEdit()) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorResponse("Cannot edit purchase with status: " + existingPurchase.getValidationStatus()))
                    .build();
            }

            // 3. Prepare updated purchase data
            Purchase updatedData = new Purchase();
            updatedData.setIdPType(idPType);
            updatedData.setIdPaymentMethod(idPaymentMethod);
            updatedData.setIdCostCenter(idCostCenter);
            updatedData.setTotalAmount(totalAmount);
            updatedData.setDescription(description);
            updatedData.setGuestName(guestName);

            // 4. Parse purchase date if provided
            if (purchaseDateStr != null && !purchaseDateStr.isEmpty()) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(purchaseDateStr);
                    updatedData.setPurchaseDate(java.time.LocalDateTime.of(date, java.time.LocalTime.now()));
                } catch (Exception e) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid purchaseDate format. Use format: 2025-10-30"))
                        .build();
                }
            }

            String newDocumentPath = null;
            String fileName = null;
            String mimeType = null;
            long fileSize = 0;

            // 5. Update document if file is provided
            if (file != null) {
                // Validate file size
                File uploadedFile = file.uploadedFile().toFile();
                if (uploadedFile.length() > MAX_FILE_SIZE) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("File size exceeds 10MB limit"))
                        .build();
                }

                // Validate file type
                mimeType = file.contentType();
                if (!ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                    return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Only images (JPG, PNG, HEIC) and PDF files are allowed"))
                        .build();
                }

                fileName = file.fileName();
                fileSize = uploadedFile.length();

                // Update document in OpenKM (delete old, upload new)
                String oldDocPath = existingPurchase.getImgUrl();
                java.time.LocalDateTime purchaseDate = updatedData.getPurchaseDate() != null
                    ? updatedData.getPurchaseDate()
                    : existingPurchase.getPurchaseDate();

                newDocumentPath = openKMService.updateDocument(
                    oldDocPath,
                    purchaseId,
                    purchaseDate,
                    fileName,
                    file
                );
            }

            // 6. Update purchase in database
            Purchase updatedPurchase = purchaseService.updatePurchaseWithDocument(
                purchaseId,
                updatedData,
                newDocumentPath
            );

            // 7. Return success response
            if (file != null) {
                DocumentResponse response = DocumentResponse.success(
                    purchaseId,
                    fileName,
                    mimeType,
                    fileSize,
                    newDocumentPath,
                    "Purchase and document updated successfully"
                );
                return Response.ok(response).build();
            } else {
                return Response.ok()
                    .entity(new SuccessResponse("Purchase updated successfully"))
                    .build();
            }

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Purchase not found: " + e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error updating purchase: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Delete the purchase and its attached document
     * DELETE /api/purchases/{purchaseId}/document
     */
    @DELETE
    @Path("/{purchaseId}/document")
    @RolesAllowed({"employee", "administrator"})
    public Response deleteDocument(@PathParam("purchaseId") Long purchaseId) {
        try {
            // 1. Get purchase
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            // 2. Delete document from OpenKM if exists
            if (purchase.hasDocument()) {
                try {
                    openKMService.deleteDocument(purchase.getImgUrl());
                } catch (Exception e) {
                    // Log but continue - document might already be deleted
                    System.err.println("Warning: Could not delete document from OpenKM: " + e.getMessage());
                }
            }

            // 3. Delete purchase record from database
            purchaseService.deletePurchase(purchaseId);

            // 4. Return success
            return Response.ok()
                .entity(new SuccessResponse("Purchase and document deleted successfully"))
                .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Purchase not found: " + e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error deleting purchase: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Extract filename from OpenKM path
     */
    private String extractFilename(String path) {
        if (path == null || path.isEmpty()) {
            return "document";
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    // Helper classes for responses
    public static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    public static class SuccessResponse {
        public String message;
        public SuccessResponse(String message) {
            this.message = message;
        }
    }
}
