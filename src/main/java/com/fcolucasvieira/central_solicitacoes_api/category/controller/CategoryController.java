package com.fcolucasvieira.central_solicitacoes_api.category.controller;

import com.fcolucasvieira.central_solicitacoes_api.category.dto.CategoryResponse;
import com.fcolucasvieira.central_solicitacoes_api.category.dto.CreateCategoryRequest;
import com.fcolucasvieira.central_solicitacoes_api.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias de solicitações")
public class CategoryController {
    private final CategoryService service;

    @Operation(summary = "Criar nova categoria", description = "Registra uma nova categoria no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Categoria com este nome já existe")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar categoria por ID", description = "Retorna os detalhes de uma categoria através do seu identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("{id}")
    public ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "ID da categoria", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Listar categorias", description = "Retorna uma lista paginada de categorias ativas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> findAll(
            @Parameter(description = "Número da página (1-based)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Quantidade de itens por página (máximo 25)", example = "25")
            @RequestParam(defaultValue = "25") int size) {
        Page<CategoryResponse> response = service.findAll(page, size);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir categoria", description = "Realiza exclusão lógica (soft delete) da categoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
