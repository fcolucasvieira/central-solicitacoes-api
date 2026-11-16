package com.fcolucasvieira.central_solicitacoes_api.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
    public static AuthResponse from(String accessToken) {
        return new AuthResponse(accessToken, "Bearer");
    }
}
