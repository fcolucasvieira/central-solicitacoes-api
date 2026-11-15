package com.fcolucasvieira.central_solicitacoes_api.request.controller;

import com.fcolucasvieira.central_solicitacoes_api.request.dto.CreateRequestRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.RequestResponse;
import com.fcolucasvieira.central_solicitacoes_api.request.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Tag(name = "Solicitações", description = "Endpoints para gerenciamento e acompanhamento de solicitações internas")
public class RequestController {
    private final RequestService service;

    @Operation(summary = "Criar solicitação", description = "Registra uma nova solicitação no sistema com status inicial ABERTA.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Categoria informada não encontrada ou inativa")
    })
    @PostMapping
    public ResponseEntity<RequestResponse> create(@Valid @RequestBody CreateRequestRequest request) {
        RequestResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar solicitação por ID", description = "Retorna os detalhes de uma solicitação através do seu identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    @GetMapping("{id}")
    public ResponseEntity<RequestResponse> findById(
            @Parameter(description = "ID da solicitação", example = "b72b6495-2d41-4770-bf7d-53c8ea4bc48f")
            @PathVariable UUID id) {
        RequestResponse response = service.findById(id);

        return ResponseEntity.ok(response);
    }
}
