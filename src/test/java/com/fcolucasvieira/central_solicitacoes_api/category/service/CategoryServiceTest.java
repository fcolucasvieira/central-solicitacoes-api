package com.fcolucasvieira.central_solicitacoes_api.category.service;

import com.fcolucasvieira.central_solicitacoes_api.category.dto.CategoryResponse;
import com.fcolucasvieira.central_solicitacoes_api.category.dto.CreateCategoryRequest;
import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.category.repository.CategoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.exception.ResourceAlreadyExistsException;
import com.fcolucasvieira.central_solicitacoes_api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Testes Unitários")
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    @Nested
    @DisplayName("Criação de Categoria")
    class CreateCategoryTests {

        @Test
        @DisplayName("Deve criar uma categoria com sucesso quando o nome for único")
        void shouldCreateCategorySuccessfully() {
            CreateCategoryRequest request = new CreateCategoryRequest("Infraestrutura");
            when(repository.existsByName("Infraestrutura")).thenReturn(false);

            CategoryResponse response = service.create(request);

            assertNotNull(response);
            assertEquals("Infraestrutura", response.name());

            ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
            verify(repository, times(1)).save(categoryCaptor.capture());

            Category savedCategory = categoryCaptor.getValue();
            assertEquals("Infraestrutura", savedCategory.getName());
            assertTrue(savedCategory.isActive());
        }

        @Test
        @DisplayName("Deve lançar ResourceAlreadyExistsException quando já existir categoria com o mesmo nome")
        void shouldThrowExceptionWhenCategoryAlreadyExists() {
            CreateCategoryRequest request = new CreateCategoryRequest("Infraestrutura");
            when(repository.existsByName("Infraestrutura")).thenReturn(true);

            ResourceAlreadyExistsException exception = assertThrows(
                    ResourceAlreadyExistsException.class,
                    () -> service.create(request)
            );

            assertEquals("Categoria já cadastrada com o nome: Infraestrutura", exception.getMessage());
            verify(repository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Busca de Categoria por ID")
    class FindCategoryByIdTests {

        @Test
        @DisplayName("Deve retornar a categoria correspondente quando o ID existir")
        void shouldReturnCategoryWhenIdExists() {
            UUID categoryId = UUID.randomUUID();
            Category category = new Category("Financeiro");
            when(repository.findByIdAndActiveIsTrue(categoryId)).thenReturn(Optional.of(category));

            CategoryResponse response = service.findById(categoryId);

            assertNotNull(response);
            assertEquals("Financeiro", response.name());
            verify(repository, times(1)).findByIdAndActiveIsTrue(categoryId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o ID não for encontrado")
        void shouldThrowExceptionWhenIdDoesNotExist() {
            UUID categoryId = UUID.randomUUID();
            when(repository.findByIdAndActiveIsTrue(categoryId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findById(categoryId)
            );

            assertEquals("Categoria não encontrada para o ID informado: " + categoryId, exception.getMessage());
            verify(repository, times(1)).findByIdAndActiveIsTrue(categoryId);
        }
    }

    @Nested
    @DisplayName("Listagem de Categorias")
    class FindAllCategoriesTests {

        @Test
        @DisplayName("Deve listar categorias ativas com paginação formatada corretamente")
        void shouldReturnPagedActiveCategories() {
            Category category1 = new Category("Financeiro");
            Category category2 = new Category("RH");
            List<Category> categories = List.of(category1, category2);

            Pageable expectedPageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(categories, expectedPageable, categories.size());

            when(repository.findAllByActiveIsTrue(expectedPageable)).thenReturn(categoryPage);

            Page<CategoryResponse> result = service.findAll(1, 10);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());
            assertEquals("Financeiro", result.getContent().get(0).name());
            assertEquals("RH", result.getContent().get(1).name());

            verify(repository, times(1)).findAllByActiveIsTrue(expectedPageable);
        }

        @Test
        @DisplayName("Deve normalizar valores de página e tamanho menores que o limite permitido")
        void shouldNormalizeNegativeOrZeroPageAndSize() {
            Pageable normalizedPageable = PageRequest.of(0, 1);
            when(repository.findAllByActiveIsTrue(normalizedPageable)).thenReturn(Page.empty());

            Page<CategoryResponse> result = service.findAll(0, 0);

            assertNotNull(result);
            verify(repository, times(1)).findAllByActiveIsTrue(normalizedPageable);
        }

        @Test
        @DisplayName("Deve limitar o tamanho máximo por página para 25 elementos")
        void shouldClampMaxSizeTo25() {
            Pageable clampedPageable = PageRequest.of(0, 25);
            when(repository.findAllByActiveIsTrue(clampedPageable)).thenReturn(Page.empty());

            Page<CategoryResponse> result = service.findAll(1, 100);

            assertNotNull(result);
            verify(repository, times(1)).findAllByActiveIsTrue(clampedPageable);
        }
    }

    @Nested
    @DisplayName("Exclusão de Categoria")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Deve realizar a exclusão lógica (desativar) quando o ID existir")
        void shouldSoftDeleteCategoryWhenIdExists() {
            UUID categoryId = UUID.randomUUID();
            Category category = new Category("Manutenção");
            assertTrue(category.isActive());

            when(repository.findById(categoryId)).thenReturn(Optional.of(category));

            service.delete(categoryId);

            assertFalse(category.isActive());
            verify(repository, times(1)).findById(categoryId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o ID da categoria não existir")
        void shouldThrowExceptionWhenCategoryNotFound() {
            UUID categoryId = UUID.randomUUID();
            when(repository.findById(categoryId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.delete(categoryId)
            );

            assertEquals("Categoria não encontrada para o ID informado: " + categoryId, exception.getMessage());
            verify(repository, times(1)).findById(categoryId);
        }
    }
}
