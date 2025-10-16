package com.datum.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase que representa la respuesta de autenticación que devuelve la API.
 * Encapsula toda la información necesaria sobre el resultado de una operación de autenticación,
 * tanto para casos exitosos como fallidos.
 */
public class AuthResponse {
    /** Indica si la autenticación fue exitosa o falló */
    public boolean success;

    /** Mensaje descriptivo del resultado (éxito o error) */
    public String message;

    /** Token JWT para acceder a recursos protegidos */
    @JsonProperty("access_token")
    public String accessToken;

    /** Token para renovar el accessToken cuando expire */
    @JsonProperty("refresh_token")
    public String refreshToken;

    /** Tipo de token (normalmente "Bearer") */
    @JsonProperty("token_type")
    public String tokenType;

    /** Tiempo de expiración del token en segundos */
    @JsonProperty("expires_in")
    public int expiresIn;

    /** Información del usuario autenticado */
    public UserInfo user;

    /** Indica si el usuario existe en el sistema */
    public boolean userExists;

    /**
     * Constructor para autenticación exitosa.
     * Establece success = true, userExists = true y mensaje automático de éxito.
     *
     * @param success indica que la autenticación fue exitosa (debe ser true)
     * @param accessToken token JWT para acceder a recursos protegidos
     * @param refreshToken token para renovar el accessToken cuando expire
     * @param tokenType tipo de token (normalmente "Bearer")
     * @param expiresIn tiempo de expiración en segundos
     * @param user información del usuario autenticado
     */
    public AuthResponse(boolean success, String accessToken, String refreshToken, String tokenType, int expiresIn, UserInfo user) {
        this.success = success;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
        this.userExists = true;
        this.message = "Authentication successful";
    }

    /**
     * Constructor para autenticación fallida.
     * Establece success = false, userExists = false y tokens como null.
     *
     * @param success indica que la autenticación falló (debe ser false)
     * @param message mensaje de error personalizado que describe la falla
     */
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.userExists = false;
    }
}