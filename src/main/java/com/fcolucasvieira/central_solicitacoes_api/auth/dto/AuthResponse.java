package com.fcolucasvieira.central_solicitacoes_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação contendo o token JWT gerado.")
public record AuthResponse(
        @Schema(description = "Token JWT de acesso.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tipo do token. Sempre 'Bearer'.", example = "Bearer")
        String tokenType,

        @Schema(description = "Nome do usuário.", example = "Lucas Vieira")
        String name
) {
    public static AuthResponse from(String accessToken, String name) {
        return new AuthResponse(accessToken, "Bearer", name);
    }
}
