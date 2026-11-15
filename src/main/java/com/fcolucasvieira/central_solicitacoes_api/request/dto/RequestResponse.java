package com.fcolucasvieira.central_solicitacoes_api.request.dto;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.Request;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Dados detalhados de retorno de uma solicitação")
public record RequestResponse(
        @Schema(description = "Identificador único da solicitação", example = "b72b6495-2d41-4770-bf7d-53c8ea4bc48f")
        UUID id,

        @Schema(description = "Título da solicitação", example = "Problema no ar-condicionado")
        String title,

        @Schema(description = "Descrição da solicitação", example = "O aparelho do bloco B sala 3 não está refrigerando.")
        String description,

        @Schema(description = "Nome da categoria associada", example = "Manutenção")
        String categoryName,

        @Schema(description = "Prioridade definida", example = "ALTA")
        Priority priority,

        @Schema(description = "Status atual da solicitação", example = "ABERTA")
        Status status,

        @Schema(description = "Data e hora de criação", example = "2026-11-15T10:00:00Z")
        Instant createdAt
){
    public static RequestResponse from(Request request){
        return new RequestResponse(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategory().getName(),
                request.getPriority(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
