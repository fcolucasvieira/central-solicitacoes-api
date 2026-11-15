package com.fcolucasvieira.central_solicitacoes_api.category.dto;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados detalhados de retorno de uma categoria")
public record CategoryResponse(
        @Schema(description = "Identificador único da categoria", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID id,

        @Schema(description = "Nome da categoria", example = "Infraestrutura e TI")
        String name,

        @Schema(description = "Data e hora de criação no formato ISO-8601", example = "2026-11-15T10:00:00Z")
        Instant createdAt
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt()
        );
    }
}
