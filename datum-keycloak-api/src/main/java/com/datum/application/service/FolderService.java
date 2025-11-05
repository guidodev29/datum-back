package com.datum.application.service;

import com.datum.domain.model.Folder;
import com.datum.domain.model.Purchase;
import com.datum.domain.ports.in.FolderUseCasePort;
import com.datum.domain.ports.out.FolderRepositoryPort;
import com.datum.domain.ports.out.PurchaseRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class FolderService implements FolderUseCasePort {

    @Inject
    FolderRepositoryPort folderRepository;

    @Inject
    PurchaseRepositoryPort purchaseRepository;

    @Override
    public Folder createFolder(Folder folder) {
        // Business validations
        if (folder.getFolderName() == null || folder.getFolderName().trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name is required");
        }

        folder.validateDateRange();

        return folderRepository.save(folder);
    }

    @Override
    public Folder getFolderById(Long id) {
        return folderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Folder not found: " + id));
    }

    @Override
    public List<Folder> getAllFolders() {
        return folderRepository.findAll();
    }

    @Override
    public List<Folder> getFoldersByUserId(Long userId) {
        return folderRepository.findByUserId(userId);
    }

    @Override
    public List<Folder> getFoldersUnderReview() {
        return folderRepository.findByStatus(com.datum.domain.model.FolderStatus.UNDER_REVIEW);
    }

    @Override
    public List<Folder> getFoldersUnderReviewByUserId(Long userId) {
        return folderRepository.findByUserIdAndStatus(userId, com.datum.domain.model.FolderStatus.UNDER_REVIEW);
    }

    @Override
    public Folder updateFolder(Long id, Folder folder) {
        Folder existing = getFolderById(id);

        // Business rule: Can only edit DRAFT folders
        if (!existing.canEdit()) {
            throw new IllegalStateException("Cannot edit folder in " + existing.getValidationStatus() + " status");
        }

        // Update fields
        existing.setFolderName(folder.getFolderName());
        existing.setDescription(folder.getDescription());
        existing.setStartDate(folder.getStartDate());
        existing.setEndDate(folder.getEndDate());

        existing.validateDateRange();

        return folderRepository.save(existing);
    }

    @Override
    public void deleteFolder(Long id) {
        Folder folder = getFolderById(id);

        // Business rule: Can only delete DRAFT folders
        if (!folder.canEdit()) {
            throw new IllegalStateException("Cannot delete folder in " + folder.getValidationStatus() + " status");
        }

        folderRepository.deleteById(id);
    }

    // Method to update folder status without edit validation
    public Folder updateFolderStatus(Folder folder) {
        return folderRepository.save(folder);
    }

    /**
     * Check if all purchases in a folder are approved
     * If so, automatically update folder status to VALIDATED
     * @param folderId ID of the folder to check
     * @param validatorId ID of the user who validated the last purchase
     */
    public void checkAndUpdateFolderStatus(Long folderId, Long validatorId) {
        // Get all purchases in the folder
        List<Purchase> purchases = purchaseRepository.findByFolderId(folderId);

        if (purchases.isEmpty()) {
            return; // Nothing to validate
        }

        // Check if ALL purchases are VALIDATED (approved)
        boolean allApproved = purchases.stream()
            .allMatch(p -> "VALIDATED".equals(p.getValidationStatus()));

        if (allApproved) {
            // Update folder to VALIDATED
            Folder folder = getFolderById(folderId);
            folder.setValidationStatus(com.datum.domain.model.FolderStatus.VALIDATED);
            folder.setValidatedBy(validatorId);
            folder.setValidatedDate(java.time.LocalDateTime.now());

            folderRepository.save(folder);
        }
    }

    /**
     * Check if all purchases in a folder are rejected
     * If so, automatically update folder status to REJECTED
     * @param folderId ID of the folder to check
     * @param validatorId ID of the user who rejected the last purchase
     */
    public void checkAndRejectFolderIfAllRejected(Long folderId, Long validatorId) {
        // Get all purchases in the folder
        List<Purchase> purchases = purchaseRepository.findByFolderId(folderId);

        if (purchases.isEmpty()) {
            return; // Nothing to validate
        }

        // Check if ALL purchases are REJECTED
        boolean allRejected = purchases.stream()
            .allMatch(p -> "REJECTED".equals(p.getValidationStatus()));

        if (allRejected) {
            // Update folder to REJECTED
            Folder folder = getFolderById(folderId);
            folder.setValidationStatus(com.datum.domain.model.FolderStatus.REJECTED);
            folder.setValidatedBy(validatorId);
            folder.setValidatedDate(java.time.LocalDateTime.now());

            folderRepository.save(folder);
        }
    }

    /**
     * Manually reject a folder
     * Can be used by finance/admin to reject a folder even if not all purchases are rejected
     * @param folderId ID of the folder to reject
     * @param validatorId ID of the user rejecting the folder
     * @param notes Reason for rejection
     * @return The rejected folder
     */
    public Folder rejectFolder(Long folderId, Long validatorId, String notes) {
        Folder folder = getFolderById(folderId);

        // Validate: can only reject UNDER_REVIEW folders
        if (folder.getValidationStatus() != com.datum.domain.model.FolderStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Can only reject folders with UNDER_REVIEW status. Current status: " + folder.getValidationStatus());
        }

        // Update folder to REJECTED
        folder.setValidationStatus(com.datum.domain.model.FolderStatus.REJECTED);
        folder.setValidatedBy(validatorId);
        folder.setValidatedDate(java.time.LocalDateTime.now());
        folder.setValidationNotes(notes);

        return folderRepository.save(folder);
    }
}