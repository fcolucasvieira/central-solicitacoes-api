package com.fcolucasvieira.central_solicitacoes_api.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de uma nova categoria")
public record CreateCategoryRequest(
        @Schema(description = "Nome único da categoria", example = "Infraestrutura e TI")
        @NotBlank(message = "O nome da categoria é obrigatório.")
        @Size(max = 100, message = "O nome da categoria não pode exceder 100 caracteres.")
        String name
) {
}
