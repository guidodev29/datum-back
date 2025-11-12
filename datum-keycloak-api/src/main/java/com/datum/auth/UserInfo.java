package com.datum.auth;

import java.util.List;

/**
 * Clase que encapsula la información del usuario autenticado.
 */
public class UserInfo {
    /** ID único del usuario */
    public String id;

    /** Nombre de usuario */
    public String username;

    public Long dbUserId; 

    /** Correo electrónico del usuario */
    public String email;

    /** Lista de roles asignados al usuario */
    public List<String> roles;

    /**
     * Constructor completo para UserInfo
     *
     * @param id ID único del usuario
     * @param username nombre de usuario
     * @param email correo electrónico
     * @param dbUserId ID del usuario en la base de datos
     * @param roles lista de roles del usuario
     */
    public UserInfo(String id, String username, String email, Long dbUserId, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.dbUserId = dbUserId;
        this.roles = roles;
    }

    /**
     * Constructor por defecto
     */
    public UserInfo() {
    }
}