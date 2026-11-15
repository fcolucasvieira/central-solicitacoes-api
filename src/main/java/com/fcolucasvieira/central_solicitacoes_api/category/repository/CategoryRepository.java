package com.fcolucasvieira.central_solicitacoes_api.category.repository;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);
    Page<Category> findAllByActiveIsTrue(Pageable pageable);
    Optional<Category> findByIdAndActiveIsTrue(UUID id);
}
