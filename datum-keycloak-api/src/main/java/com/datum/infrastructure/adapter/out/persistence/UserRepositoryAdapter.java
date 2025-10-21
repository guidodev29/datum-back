package com.datum.infrastructure.adapter.out.persistence;

import com.datum.domain.model.User;
import com.datum.domain.ports.out.UserRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserRepositoryAdapter implements UserRepositoryPort {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        
        if (entity.getId() == null) {
            entityManager.persist(entity); // Create
        } else {
            entity = entityManager.merge(entity); // Update
        }
        
        return toDomain(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        UserEntity entity = entityManager.find(UserEntity.class, id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String nickname) {
        List<UserEntity> results = entityManager
            .createQuery("SELECT u FROM UserEntity u WHERE u.nickname = :nickname", UserEntity.class)
            .setParameter("nickname", nickname)
            .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(toDomain(results.get(0)));
    }

    @Override
    public Optional<User> findByKeycloakId(String keycloakId) {
        List<UserEntity> results = entityManager
            .createQuery("SELECT u FROM UserEntity u WHERE u.keycloakId = :keycloakId", UserEntity.class)
            .setParameter("keycloakId", keycloakId)
            .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(toDomain(results.get(0)));
    }

    @Override
    public List<User> findAll() {
        return entityManager
            .createQuery("SELECT u FROM UserEntity u", UserEntity.class)
            .getResultList()
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        UserEntity entity = entityManager.find(UserEntity.class, id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public boolean existsByUsername(String nickname) {
        Long count = entityManager
            .createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.nickname = :nickname", Long.class)
            .setParameter("nickname", nickname)
            .getSingleResult();
        
        return count > 0;
    }

    // Mapper: Entity -> Domain
    private User toDomain(UserEntity entity) {
        return new User(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getNickname(),
            entity.getEmail(),
            entity.getKeycloakId()
        );
    }

    // Mapper: Domain -> Entity
    private UserEntity toEntity(User user) {
        return new UserEntity(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getNickname(),
            user.getEmail(),
            user.getKeycloakId()
        );
    }
}