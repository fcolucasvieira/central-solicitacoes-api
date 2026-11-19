package com.fcolucasvieira.central_solicitacoes_api.user.dto;

import com.fcolucasvieira.central_solicitacoes_api.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados de resposta do usuário cadastrado.")
public record UserResponse(
        @Schema(description = "Identificador único do usuário.", example = "b72b6495-2d41-4770-bf7d-53c8ea4bc48f")
        UUID id,

        @Schema(description = "Nome completo do usuário.", example = "Lucas Vieira")
        String name,

        @Schema(description = "E-mail de acesso do usuário.", example = "lucas.vieira@loadingjr.com")
        String email,

        @Schema(description = "Indica se a conta do usuário está ativa.", example = "true")
        boolean active,

        @Schema(description = "Data e hora de criação do cadastro.")
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
