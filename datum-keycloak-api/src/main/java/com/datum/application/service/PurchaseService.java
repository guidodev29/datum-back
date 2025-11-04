package com.datum.application.service;

import com.datum.domain.model.Purchase;
import com.datum.domain.ports.in.PurchaseUseCasePort;
import com.datum.domain.ports.out.PurchaseRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Application service implementing Purchase use cases
 * Orchestrates business logic for purchase operations
 */
@ApplicationScoped
public class PurchaseService implements PurchaseUseCasePort {

    @Inject
    PurchaseRepositoryPort purchaseRepository;

    @Override
    @Transactional
    public Purchase createPurchase(Purchase purchase) {
        // Validate business rules
        if (purchase.getIdUser() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (purchase.getIdFolder() == null) {
            throw new IllegalArgumentException("Folder ID is required");
        }

        purchase.validateAmount();

        // Set default status if not provided
        if (purchase.getValidationStatus() == null) {
            purchase.setValidationStatus("DRAFT");
        }

        return purchaseRepository.save(purchase);
    }

    @Override
    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found with ID: " + id));
    }

    @Override
    public List<Purchase> getPurchasesByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return purchaseRepository.findByUserId(userId);
    }

    @Override
    public List<Purchase> getPurchasesByFolderId(Long folderId) {
        if (folderId == null) {
            throw new IllegalArgumentException("Folder ID cannot be null");
        }
        return purchaseRepository.findByFolderId(folderId);
    }

    @Override
    @Transactional
    public Purchase updatePurchase(Long id, Purchase purchase) {
        Purchase existing = getPurchaseById(id);

        // Only DRAFT purchases can be edited
        if (!existing.canEdit()) {
            throw new IllegalStateException("Cannot edit purchase with status: " + existing.getValidationStatus());
        }

        // Update fields
        existing.setIdFolder(purchase.getIdFolder());
        existing.setIdPType(purchase.getIdPType());
        existing.setIdPaymentMethod(purchase.getIdPaymentMethod());
        existing.setIdCostCenter(purchase.getIdCostCenter());
        existing.setTotalAmount(purchase.getTotalAmount());
        existing.setDescription(purchase.getDescription());
        existing.setGuestName(purchase.getGuestName());
        existing.setPurchaseDate(purchase.getPurchaseDate());

        existing.validateAmount();

        return purchaseRepository.save(existing);
    }

    @Override
    @Transactional
    public void deletePurchase(Long id) {
        Purchase purchase = getPurchaseById(id);

        // Only DRAFT purchases can be deleted
        if (!purchase.canDelete()) {
            throw new IllegalStateException("Cannot delete purchase with status: " + purchase.getValidationStatus());
        }

        purchaseRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void attachDocument(Long purchaseId, String documentUrl) {
        Purchase purchase = getPurchaseById(purchaseId);

        // Set document URL using business logic
        purchase.setDocumentUrl(documentUrl);

        purchaseRepository.save(purchase);
    }

    @Override
    @Transactional
    public void removeDocument(Long purchaseId) {
        Purchase purchase = getPurchaseById(purchaseId);

        if (!purchase.canEdit()) {
            throw new IllegalStateException("Cannot remove document from non-DRAFT purchase");
        }

        purchase.setImgUrl(null);
        purchaseRepository.save(purchase);
    }

    /**
     * Submit all purchases in a folder for review
     * Changes status from DRAFT to UNDER_REVIEW
     */
    @Transactional
    public int submitFolderForReview(Long folderId) {
        List<Purchase> purchases = purchaseRepository.findByFolderId(folderId);

        if (purchases.isEmpty()) {
            throw new IllegalArgumentException("No purchases found in folder: " + folderId);
        }

        int submittedCount = 0;
        for (Purchase purchase : purchases) {
            if (purchase.canSubmitForReview()) {
                purchase.submitForReview();
                purchaseRepository.save(purchase);
                submittedCount++;
            }
        }

        if (submittedCount == 0) {
            throw new IllegalStateException("No DRAFT purchases found to submit in folder");
        }

        return submittedCount;
    }

    /**
     * Approve a purchase
     * Changes status from UNDER_REVIEW to APPROVED
     * @param purchaseId ID of the purchase to approve
     * @param validatorId ID of the user approving (finance/admin)
     * @param notes Optional notes about the approval
     * @return The approved purchase
     */
    @Transactional
    public Purchase approvePurchase(Long purchaseId, Long validatorId, String notes) {
        Purchase purchase = getPurchaseById(purchaseId);

        // Validate: can only approve UNDER_REVIEW purchases
        if (!"UNDER_REVIEW".equals(purchase.getValidationStatus())) {
            throw new IllegalStateException("Can only approve purchases with UNDER_REVIEW status. Current status: " + purchase.getValidationStatus());
        }

        // Update purchase status to VALIDATED (approved)
        purchase.setValidationStatus("VALIDATED");
        purchase.setValidatedBy(validatorId);
        purchase.setValidatedDate(java.time.LocalDateTime.now());
        purchase.setValidationNotes(notes);

        return purchaseRepository.save(purchase);
    }

    /**
     * Reject a purchase
     * Changes status from UNDER_REVIEW to REJECTED
     * @param purchaseId ID of the purchase to reject
     * @param validatorId ID of the user rejecting (finance/admin)
     * @param notes Optional notes about why it was rejected
     * @return The rejected purchase
     */
    @Transactional
    public Purchase rejectPurchase(Long purchaseId, Long validatorId, String notes) {
        Purchase purchase = getPurchaseById(purchaseId);

        // Validate: can only reject UNDER_REVIEW purchases
        if (!"UNDER_REVIEW".equals(purchase.getValidationStatus())) {
            throw new IllegalStateException("Can only reject purchases with UNDER_REVIEW status. Current status: " + purchase.getValidationStatus());
        }

        // Update purchase status to REJECTED
        purchase.setValidationStatus("REJECTED");
        purchase.setValidatedBy(validatorId);
        purchase.setValidatedDate(java.time.LocalDateTime.now());
        purchase.setValidationNotes(notes);

        return purchaseRepository.save(purchase);
    }
}
