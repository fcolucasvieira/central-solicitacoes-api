package com.fcolucasvieira.central_solicitacoes_api.user.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {
}
