package com.datum.application.service;

import com.datum.domain.model.User;
import com.datum.domain.ports.in.UserUseCasePort;
import com.datum.domain.ports.out.UserRepositoryPort;
import com.datum.infrastructure.adapter.out.keycloak.KeycloakService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserService implements UserUseCasePort {

    @Inject
    UserRepositoryPort userRepository;

    @Inject
    KeycloakService keycloakService; 

@Override
@Transactional
public User createUser(String firstName, String lastName, String nickname, String email, String keycloakId) {
    // Business validation
    if (userRepository.existsByUsername(nickname)) {
        throw new RuntimeException("Nickname already exists: " + nickname);
    }

    // Generate temporary password: FirstName@Datum2025
    String temporaryPassword = keycloakService.generateTemporaryPassword(firstName);

    // Create user in Keycloak FIRST
    String keycloakUserId = keycloakService.createUser(email, firstName, lastName, temporaryPassword);
    
    if (keycloakUserId == null) {
        throw new RuntimeException("Failed to create user in Keycloak");
    }

    // Create domain object with Keycloak ID
    User user = new User(null, firstName, lastName, nickname, email, keycloakUserId);
    
    // Save in Oracle
    return userRepository.save(user);
}

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByNickname(String nickname) {
        return userRepository.findByUsername(nickname);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(Long id, String firstName, String lastName, String nickname, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setNickname(nickname);
        user.setEmail(email);
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}