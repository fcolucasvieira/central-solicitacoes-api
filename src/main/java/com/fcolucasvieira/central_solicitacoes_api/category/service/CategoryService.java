package com.fcolucasvieira.central_solicitacoes_api.category.service;

import com.fcolucasvieira.central_solicitacoes_api.category.dto.CategoryResponse;
import com.fcolucasvieira.central_solicitacoes_api.category.dto.CreateCategoryRequest;
import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (repository.existsByName(request.name())) {
            throw new IllegalStateException("Conflict (403): Categoria já existe.");
        }

        Category category = new Category(request.name());

        repository.save(category);

        return CategoryResponse.from(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(int page, int size) {
        page = Math.max(page - 1, 0);
        size = Math.min(Math.max(size, 1), 25);

        Pageable pageable = PageRequest.of(page, size);

        return repository.findAllByActiveIsTrue(pageable)
                .map(CategoryResponse::from);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found (401): Categoria não encontrada"));

        category.delete();
    }
}
