package com.fcolucasvieira.central_solicitacoes_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para registrar um novo usuário.")
public record RegisterRequest(
        @Schema(description = "Nome completo do usuário.", example = "Lucas Vieira")
        @NotBlank
        String name,

        @Schema(description = "E-mail do usuário. Deve ser único no sistema.", example = "lucas@email.com")
        @Email(message = "E-mail com formato inválido.")
        @NotBlank(message = "E-mail é obrigatório.")
        String email,

        @Schema(description = "Senha do usuário. Mínimo de 8 caracteres.", example = "senha@123")
        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, message = "A senha deve conter no mínimo 8 caracteres.")
        String password
) {
}
