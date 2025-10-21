package com.datum.domain.ports.in;

import com.datum.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserUseCasePort {
    
    User createUser(String firstName, String lastName, String nickname, String email, String keycloakId);
    
    Optional<User> getUserById(Long id);
    
    Optional<User> getUserByNickname(String nickname);
    
    List<User> getAllUsers();
    
    User updateUser(Long id, String firstName, String lastName, String nickname, String email);
    
    void deleteUser(Long id);
}