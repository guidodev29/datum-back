package com.datum.domain.ports.out;

import com.datum.domain.model.Folder;
import com.datum.domain.model.FolderStatus;
import java.util.List;
import java.util.Optional;

public interface FolderRepositoryPort {
    Folder save(Folder folder);
    Optional<Folder> findById(Long id);
    List<Folder> findAll();
    List<Folder> findByUserId(Long userId);
    List<Folder> findByStatus(FolderStatus status);
    void deleteById(Long id);
    boolean existsById(Long id);
}