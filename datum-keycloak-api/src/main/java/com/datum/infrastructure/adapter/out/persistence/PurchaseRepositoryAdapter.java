package com.datum.infrastructure.adapter.out.persistence;

import com.datum.domain.model.Purchase;
import com.datum.domain.ports.out.PurchaseRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter for Purchase persistence operations
 * Implements PurchaseRepositoryPort using JPA/Hibernate
 */
@ApplicationScoped
public class PurchaseRepositoryAdapter implements PurchaseRepositoryPort {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseEntity entity = toEntity(purchase);

        if (entity.getIdPurchase() == null) {
            entityManager.persist(entity); // Create
        } else {
            entity = entityManager.merge(entity); // Update
        }

        return toDomain(entity);
    }

    @Override
    public Optional<Purchase> findById(Long id) {
        PurchaseEntity entity = entityManager.find(PurchaseEntity.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<Purchase> findByUserId(Long userId) {
        return entityManager
            .createQuery("SELECT p FROM PurchaseEntity p WHERE p.idUser = :userId ORDER BY p.purchaseDate DESC", PurchaseEntity.class)
            .setParameter("userId", userId)
            .getResultList()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Purchase> findByFolderId(Long folderId) {
        return entityManager
            .createQuery("SELECT p FROM PurchaseEntity p WHERE p.idFolder = :folderId ORDER BY p.purchaseDate DESC", PurchaseEntity.class)
            .setParameter("folderId", folderId)
            .getResultList()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Purchase> findByStatus(String status) {
        return entityManager
            .createQuery("SELECT p FROM PurchaseEntity p WHERE p.validationStatus = :status ORDER BY p.purchaseDate DESC", PurchaseEntity.class)
            .setParameter("status", status)
            .getResultList()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        // Use JPQL delete query for immediate execution
        int deletedCount = entityManager
            .createQuery("DELETE FROM PurchaseEntity p WHERE p.idPurchase = :id")
            .setParameter("id", id)
            .executeUpdate();

        if (deletedCount == 0) {
            throw new IllegalArgumentException("Purchase with ID " + id + " not found");
        }
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager
            .createQuery("SELECT COUNT(p) FROM PurchaseEntity p WHERE p.idPurchase = :id", Long.class)
            .setParameter("id", id)
            .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Purchase> findAll() {
        return entityManager
            .createQuery("SELECT p FROM PurchaseEntity p ORDER BY p.purchaseDate DESC", PurchaseEntity.class)
            .getResultList()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    // Mapper: Entity -> Domain
    private Purchase toDomain(PurchaseEntity entity) {
        Purchase purchase = new Purchase();
        purchase.setIdPurchase(entity.getIdPurchase());
        purchase.setIdUser(entity.getIdUser());
        purchase.setIdFolder(entity.getIdFolder());
        purchase.setIdPType(entity.getIdPType());
        purchase.setIdPaymentMethod(entity.getIdPaymentMethod());
        purchase.setIdCostCenter(entity.getIdCostCenter());
        purchase.setTotalAmount(entity.getTotalAmount());
        purchase.setDescription(entity.getDescription());
        purchase.setGuestName(entity.getGuestName());
        purchase.setPurchaseDate(entity.getPurchaseDate());
        purchase.setImgUrl(entity.getImgUrl());
        purchase.setValidationStatus(entity.getValidationStatus());
        purchase.setValidatedDate(entity.getValidatedDate());
        purchase.setValidatedBy(entity.getValidatedBy());
        purchase.setValidationNotes(entity.getValidationNotes());
        purchase.setCreatedDate(entity.getCreatedDate());
        return purchase;
    }

    // Mapper: Domain -> Entity
    private PurchaseEntity toEntity(Purchase purchase) {
        PurchaseEntity entity = new PurchaseEntity();
        entity.setIdPurchase(purchase.getIdPurchase());
        entity.setIdUser(purchase.getIdUser());
        entity.setIdFolder(purchase.getIdFolder());
        entity.setIdPType(purchase.getIdPType());
        entity.setIdPaymentMethod(purchase.getIdPaymentMethod());
        entity.setIdCostCenter(purchase.getIdCostCenter());
        entity.setTotalAmount(purchase.getTotalAmount());
        entity.setDescription(purchase.getDescription());
        entity.setGuestName(purchase.getGuestName());
        entity.setPurchaseDate(purchase.getPurchaseDate());
        entity.setImgUrl(purchase.getImgUrl());
        entity.setValidationStatus(purchase.getValidationStatus());
        entity.setValidatedDate(purchase.getValidatedDate());
        entity.setValidatedBy(purchase.getValidatedBy());
        entity.setValidationNotes(purchase.getValidationNotes());
        entity.setCreatedDate(purchase.getCreatedDate());
        return entity;
    }
}
