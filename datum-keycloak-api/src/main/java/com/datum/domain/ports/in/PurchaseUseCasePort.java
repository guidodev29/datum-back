package com.datum.domain.ports.in;

import com.datum.domain.model.Purchase;

import java.util.List;

/**
 * Port for Purchase use cases (business operations)
 * Defines the contract for purchase-related operations
 */
public interface PurchaseUseCasePort {

    /**
     * Create a new purchase
     */
    Purchase createPurchase(Purchase purchase);

    /**
     * Get purchase by ID
     */
    Purchase getPurchaseById(Long id);

    /**
     * Get all purchases by user
     */
    List<Purchase> getPurchasesByUserId(Long userId);

    /**
     * Get all purchases by folder
     */
    List<Purchase> getPurchasesByFolderId(Long folderId);

    /**
     * Update purchase
     */
    Purchase updatePurchase(Long id, Purchase purchase);

    /**
     * Delete purchase (only if DRAFT)
     */
    void deletePurchase(Long id);

    /**
     * Attach document to purchase (update img_url)
     */
    void attachDocument(Long purchaseId, String documentUrl);

    /**
     * Remove document from purchase
     */
    void removeDocument(Long purchaseId);
}
