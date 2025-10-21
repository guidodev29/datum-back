package com.datum.application.dto;

import com.datum.domain.model.User;

public class UserResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;
    private String keycloakId;

    // Constructors
    public UserResponse() {}

    public UserResponse(Long id, String firstName, String lastName, String nickname, String email, String keycloakId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.email = email;
        this.keycloakId = keycloakId;
    }

    // Factory method to create from Domain
    public static UserResponse fromDomain(User user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getNickname(),
            user.getEmail(),
            user.getKeycloakId()
        );
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String keycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }
}