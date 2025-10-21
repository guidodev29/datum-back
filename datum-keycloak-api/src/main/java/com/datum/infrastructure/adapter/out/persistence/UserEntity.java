package com.datum.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "TB_USER", schema = "C##DATUM")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private Long id;

    @Column(name = "F_NAME", length = 50)
    private String firstName;

    @Column(name = "L_NAME", length = 50)
    private String lastName;

    @Column(name = "NICKNAME", length = 50, unique = true)
    private String nickname;

    @Column(name = "EMAIL", length = 100, nullable = false)
    private String email;

    @Column(name = "ID_AUTH", length = 128)
    private String keycloakId;

    // Constructors
    public UserEntity() {
    }

    public UserEntity(Long id, String firstName, String lastName, String nickname, String email, String keycloakId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.email = email;
        this.keycloakId = keycloakId;
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

    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }
}