package com.datum.domain.ports.out;

import com.datum.domain.model.Purchase;

import java.util.List;
import java.util.Optional;

/**
 * Port for Purchase repository operations
 * Defines the contract for purchase data persistence
 */
public interface PurchaseRepositoryPort {

    /**
     * Save a purchase (create or update)
     */
    Purchase save(Purchase purchase);

    /**
     * Find purchase by ID
     */
    Optional<Purchase> findById(Long id);

    /**
     * Find all purchases by user ID
     */
    List<Purchase> findByUserId(Long userId);

    /**
     * Find all purchases by folder ID
     */
    List<Purchase> findByFolderId(Long folderId);

    /**
     * Find all purchases by validation status
     */
    List<Purchase> findByStatus(String status);

    /**
     * Delete purchase by ID
     */
    void deleteById(Long id);

    /**
     * Check if purchase exists
     */
    boolean existsById(Long id);

    /**
     * Find all purchases
     */
    List<Purchase> findAll();
}
