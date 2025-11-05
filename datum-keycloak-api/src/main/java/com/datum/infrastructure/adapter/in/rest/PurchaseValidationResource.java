package com.datum.infrastructure.adapter.in.rest;

import com.datum.application.dto.ApproveRequest;
import com.datum.application.dto.PurchaseResponse;
import com.datum.application.dto.RejectRequest;
import com.datum.application.service.FolderService;
import com.datum.application.service.PurchaseService;
import com.datum.domain.model.Purchase;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Resource for purchase validation (approve/reject)
 * Handles approval and rejection of purchases by finance/admin users
 */
@Path("/api/purchases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PurchaseValidationResource {

    @Inject
    PurchaseService purchaseService;

    @Inject
    FolderService folderService;

    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Approve a purchase
     * POST /api/purchases/{purchaseId}/approve
     *
     * Request body example:
     * {
     *   "notes": "Receipt matches the description and amount"
     * }
     */
    @POST
    @Path("/{purchaseId}/approve")
    @RolesAllowed({"finance", "administrator"})
    public Response approvePurchase(
        @PathParam("purchaseId") Long purchaseId,
        ApproveRequest request
    ) {
        try {
            // Get validator user ID from JWT token
            Long validatorId = getUserIdFromToken();

            // Approve the purchase
            Purchase approvedPurchase = purchaseService.approvePurchase(
                purchaseId,
                validatorId,
                request != null ? request.getNotes() : null
            );

            // Check if all purchases in the folder are approved
            // If so, automatically update folder to VALIDATED
            folderService.checkAndUpdateFolderStatus(approvedPurchase.getIdFolder(), validatorId);

            // Return success response
            PurchaseResponse response = PurchaseResponse.fromDomain(approvedPurchase);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Purchase not found: " + e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error approving purchase: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Reject a purchase
     * POST /api/purchases/{purchaseId}/reject
     *
     * Request body example:
     * {
     *   "notes": "Receipt is not clear, please upload a better quality image"
     * }
     */
    @POST
    @Path("/{purchaseId}/reject")
    @RolesAllowed({"finance", "administrator"})
    public Response rejectPurchase(
        @PathParam("purchaseId") Long purchaseId,
        RejectRequest request
    ) {
        try {
            // Get validator user ID from JWT token
            Long validatorId = getUserIdFromToken();

            // Reject the purchase
            Purchase rejectedPurchase = purchaseService.rejectPurchase(
                purchaseId,
                validatorId,
                request != null ? request.getNotes() : null
            );

            // Check if all purchases in the folder are rejected
            // If so, automatically update folder to REJECTED
            folderService.checkAndRejectFolderIfAllRejected(rejectedPurchase.getIdFolder(), validatorId);

            // Return success response
            PurchaseResponse response = PurchaseResponse.fromDomain(rejectedPurchase);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("Purchase not found: " + e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error rejecting purchase: " + e.getMessage()))
                .build();
        }
    }

    /**
     * Extract user ID from JWT token
     * The user ID is stored in the "sub" claim of the JWT
     */
    private Long getUserIdFromToken() {
        // Get the subject (user ID) from the JWT token
        String subject = securityIdentity.getPrincipal().getName();

        try {
            // In Keycloak, the subject is usually the user UUID
            // For now, we'll return a placeholder
            // TODO: Implement proper user ID extraction from Keycloak token
            return 1L; // Placeholder - needs proper implementation
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract user ID from token");
        }
    }

    // Helper class for error responses
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
