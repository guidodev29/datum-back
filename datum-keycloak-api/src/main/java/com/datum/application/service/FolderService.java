package com.datum.application.service;

import com.datum.domain.model.Folder;
import com.datum.domain.ports.in.FolderUseCasePort;
import com.datum.domain.ports.out.FolderRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
@Transactional
public class FolderService implements FolderUseCasePort {

    @Inject
    FolderRepositoryPort folderRepository;

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
}