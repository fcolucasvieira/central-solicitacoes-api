package com.fcolucasvieira.central_solicitacoes_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação do usuário.")
public record LoginRequest(
        @Schema(description = "E-mail cadastrado do usuário.", example = "lucas@email.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Senha do usuário.", example = "senha@123")
        @NotBlank
        String password
) {
}
