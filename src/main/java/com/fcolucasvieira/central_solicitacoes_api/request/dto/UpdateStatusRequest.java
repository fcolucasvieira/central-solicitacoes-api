package com.fcolucasvieira.central_solicitacoes_api.request.dto;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualização do status de uma solicitação")
public record UpdateStatusRequest(
        @Schema(description = "Novo status desejado para a solicitação", example = "EM_ANDAMENTO")
        @NotNull(message = "O status é obrigatório.")
        Status status
) {
}
