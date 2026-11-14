package com.fcolucasvieira.central_solicitacoes_api.category.controller;

import com.fcolucasvieira.central_solicitacoes_api.category.dto.CategoryResponse;
import com.fcolucasvieira.central_solicitacoes_api.category.dto.CreateCategoryRequest;
import com.fcolucasvieira.central_solicitacoes_api.category.service.CategoryService;
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
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> findAll(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "25") int size) {
        Page<CategoryResponse> response = service.findAll(page, size);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
