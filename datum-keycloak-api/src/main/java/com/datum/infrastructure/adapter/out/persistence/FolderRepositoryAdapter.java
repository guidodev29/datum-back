package com.datum.infrastructure.adapter.out.persistence;

import com.datum.domain.model.Folder;
import com.datum.domain.model.FolderStatus;
import com.datum.domain.ports.out.FolderRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class FolderRepositoryAdapter implements FolderRepositoryPort {

    @Inject
    FolderPanacheRepository folderPanacheRepository;

    @Override
    public Folder save(Folder folder) {
        FolderEntity entity = toEntity(folder);
        
        // If entity has ID, it's an update (use merge)
        // If no ID, it's a create (use persist)
        if (entity.getId() != null) {
            entity = folderPanacheRepository.getEntityManager().merge(entity);
        } else {
            folderPanacheRepository.persist(entity);
        }
        
        return toDomain(entity);
    }

    @Override
    public Optional<Folder> findById(Long id) {
        return folderPanacheRepository.findByIdOptional(id)
            .map(this::toDomain);
    }

    @Override
    public List<Folder> findAll() {
        return folderPanacheRepository.listAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Folder> findByUserId(Long userId) {
        return folderPanacheRepository.findByUserId(userId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Folder> findByStatus(FolderStatus status) {
        return folderPanacheRepository.findByStatus(status.name()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Folder> findByUserIdAndStatus(Long userId, FolderStatus status) {
        return folderPanacheRepository.findByUserIdAndStatus(userId, status.name()).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        folderPanacheRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return folderPanacheRepository.findByIdOptional(id).isPresent();
    }

    // Conversion: Entity → Domain
    private Folder toDomain(FolderEntity entity) {
        Folder folder = new Folder();
        folder.setId(entity.getId());
        folder.setUserId(entity.getUserId());
        folder.setFolderName(entity.getFolderName());
        folder.setDescription(entity.getDescription());
        folder.setStartDate(entity.getStartDate());
        folder.setEndDate(entity.getEndDate());
        
        if (entity.getValidationStatus() != null) {
            folder.setValidationStatus(FolderStatus.valueOf(entity.getValidationStatus()));
        }
        
        folder.setValidatedDate(entity.getValidatedDate());
        folder.setValidatedBy(entity.getValidatedBy());
        folder.setValidationNotes(entity.getValidationNotes());
        
        return folder;
    }

    // Conversion: Domain → Entity
    private FolderEntity toEntity(Folder folder) {
        FolderEntity entity = new FolderEntity();
        entity.setId(folder.getId());
        entity.setUserId(folder.getUserId());
        entity.setFolderName(folder.getFolderName());
        entity.setDescription(folder.getDescription());
        entity.setStartDate(folder.getStartDate());
        entity.setEndDate(folder.getEndDate());
        
        if (folder.getValidationStatus() != null) {
            entity.setValidationStatus(folder.getValidationStatus().name());
        }
        
        entity.setValidatedDate(folder.getValidatedDate());
        entity.setValidatedBy(folder.getValidatedBy());
        entity.setValidationNotes(folder.getValidationNotes());
        
        return entity;
    }
}