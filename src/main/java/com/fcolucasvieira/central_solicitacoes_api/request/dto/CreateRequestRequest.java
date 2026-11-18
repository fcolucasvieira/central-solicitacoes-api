package com.fcolucasvieira.central_solicitacoes_api.request.dto;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Dados para abertura de uma nova solicitação")
public record CreateRequestRequest(
        @Schema(description = "Título da solicitação", example = "Problema no ar-condicionado")
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 150, message = "O título não pode exceder 150 caracteres.")
        String title,

        @Schema(description = "Descrição detalhada da solicitação", example = "O aparelho do bloco B sala 3 não está refrigerando.")
        @NotBlank(message = "A descrição é obrigatória.")
        String description,

        @Schema(description = "Identificador único da categoria existente e ativa", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        @NotNull(message = "A categoria é obrigatória.")
        UUID categoryId,

        @Schema(description = "Prioridade da solicitação", example = "ALTA")
        @NotNull(message = "A prioridade é obrigatória.")
        Priority priority
) {}
