package com.datum.domain.ports.out;

import com.datum.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    
    User save(User user);
    
    Optional<User> findById(Long id);
    
    Optional<User> findByUsername(String nickname);  // Using nickname
    
    Optional<User> findByKeycloakId(String keycloakId);
    
    List<User> findAll();
    
    void deleteById(Long id);
    
    boolean existsByUsername(String nickname);  // Using nickname
}