package com.datum.infrastructure.adapter.out.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FolderPanacheRepository implements PanacheRepository<FolderEntity> {

    public List<FolderEntity> findByUserId(Long userId) {
        return list("userId", userId);
    }

    public List<FolderEntity> findByStatus(String status) {
        return list("validationStatus", status);
    }
}