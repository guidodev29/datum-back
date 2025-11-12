package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.DocumentResponse;
import com.datum.application.service.PurchaseService;
import com.datum.domain.model.Purchase;
import com.datum.infrastructure.adapter.out.openkm.OpenKMService;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
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
            "application/pdf");

    // Max file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Create purchase and upload document in a single request
     * POST /api/purchases/document
     */
    @POST
    @Path("/document")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    //@RolesAllowed({ "employee", "administrator" })
    public Response createPurchaseWithDocument(
            @RestForm("idUser") Long idUser,
            @RestForm("idFolder") Long idFolder,
            @RestForm("idPType") Long idPType,
            @RestForm("idPaymentMethod") Long idPaymentMethod,
            @RestForm("idCostCenter") Long idCostCenter,  // Now using @RestForm instead of @FormParam
            @RestForm("totalAmount") String totalAmountStr,  // Changed to String for better parsing
            @RestForm("description") String description,
            @RestForm("guestName") String guestName,
            @RestForm("purchaseDate") String purchaseDateStr,
            @RestForm("file") FileUpload file) {
        try {
            // 1. Validate required fields
            if (idUser == null || idFolder == null || totalAmountStr == null || purchaseDateStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("idUser, idFolder, totalAmount, and purchaseDate are required"))
                        .build();
            }

            // 2. Parse totalAmount
            java.math.BigDecimal totalAmount;
            try {
                totalAmount = new java.math.BigDecimal(totalAmountStr);
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid totalAmount format: " + totalAmountStr))
                        .build();
            }

            // 3. Validate file is provided
            if (file == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("File is required"))
                        .build();
            }

            // 4. Validate file size
            File uploadedFile = file.uploadedFile().toFile();
            if (uploadedFile.length() > MAX_FILE_SIZE) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("File size exceeds 10MB limit"))
                        .build();
            }

            // 5. Validate file type (images or PDF only)
            String mimeType = file.contentType();
            if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Only images (JPG, PNG, HEIC) and PDF files are allowed. Got: " + mimeType))
                        .build();
            }

            // 6. Parse purchase date (only date, time will be current moment)
            java.time.LocalDateTime purchaseDate;
            try {
                // Parse only the date part (e.g., "2025-10-30")
                java.time.LocalDate date = java.time.LocalDate.parse(purchaseDateStr);
                // Combine with current time
                purchaseDate = java.time.LocalDateTime.of(date, java.time.LocalTime.now());
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid purchaseDate format. Use format: 2025-10-30. Error: " + e.getMessage()))
                        .build();
            }

            // 7. Create Purchase in database (ID will be auto-generated)
            Purchase purchase = new Purchase();
            purchase.setIdUser(idUser);
            purchase.setIdFolder(idFolder);
            purchase.setIdPType(idPType);
            purchase.setIdPaymentMethod(idPaymentMethod);
            purchase.setTotalAmount(totalAmount);
            purchase.setDescription(description);
            purchase.setGuestName(guestName);
            purchase.setPurchaseDate(purchaseDate);
            purchase.setValidationStatus("DRAFT");
            purchase.setCreatedDate(java.time.LocalDateTime.now());

            // Handle nullable idCostCenter
            if (idCostCenter != null && idCostCenter > 0) {
                purchase.setIdCostCenter(idCostCenter);
            } else {
                purchase.setIdCostCenter(null);
            }

            System.out.println("Creating purchase: " + purchase);
            Purchase savedPurchase = purchaseService.createPurchase(purchase);
            Long generatedId = savedPurchase.getIdPurchase();
            System.out.println("Purchase created with ID: " + generatedId);

            // 8. Upload document to OpenKM
            String fileName = file.fileName();
            System.out.println("Uploading document: " + fileName + " to OpenKM...");
            String openkmPath = openKMService.uploadDocument(generatedId, purchaseDate, fileName, file);
            System.out.println("Document uploaded to: " + openkmPath);

            // 9. Update purchase with document path
            purchaseService.attachDocument(generatedId, openkmPath);

            // 10. Return success response
            DocumentResponse response = DocumentResponse.success(
                    generatedId,
                    fileName,
                    mimeType,
                    uploadedFile.length(),
                    openkmPath,
                    "Purchase created and document uploaded successfully");

            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Validation error: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
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
    @PermitAll
    //@RolesAllowed({ "employee", "administrator" })
    public Response uploadDocument(
            @PathParam("purchaseId") Long purchaseId,
            @RestForm("file") FileUpload file) {
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
                    "Document uploaded successfully");

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
    @PermitAll
    //@RolesAllowed({ "employee", "administrator", "finance" })
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
     * Delete the purchase and its attached document
     * DELETE /api/purchases/{purchaseId}/document
     */
    @DELETE
    @Path("/{purchaseId}/document")
    @PermitAll
    //@RolesAllowed({ "employee", "administrator" })
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