package com.fcolucasvieira.central_solicitacoes_api.request.service;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.category.repository.CategoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.CreateRequestRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.RequestResponse;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.Request;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestStatusHistoryRepository;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestService - Testes Unitários")
class RequestServiceTest {
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestStatusHistoryRepository historyRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private RequestService service;

    @Nested
    @DisplayName("Criação de Solicitação")
    class CreateRequestTests {
        @Test
        @DisplayName("Deve criar uma solicitação com sucesso quando categoria existir")
        void shouldCreateRequestSuccessfully() {
            UUID categoryId = UUID.randomUUID();
            Category category = new Category("Infraestrutura");
            CreateRequestRequest requestDTO = new CreateRequestRequest(
                    "Ar-condicionado quebrado",
                    "Aparelho do bloco B não liga",
                    categoryId,
                    Priority.ALTA
            );

            when(categoryRepository.findByIdAndActiveIsTrue(categoryId)).thenReturn(Optional.of(category));

            RequestResponse response = service.create(requestDTO);

            assertNotNull(response);
            assertEquals("Ar-condicionado quebrado", response.title());
            assertEquals("Aparelho do bloco B não liga", response.description());
            assertEquals("Infraestrutura", response.categoryName());
            assertEquals(Priority.ALTA, response.priority());
            assertEquals(Status.ABERTA, response.status());

            ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            verify(requestRepository, times(1)).save(requestCaptor.capture());

            Request savedRequest = requestCaptor.getValue();
            assertEquals("Ar-condicionado quebrado", savedRequest.getTitle());
            assertEquals("Aparelho do bloco B não liga", savedRequest.getDescription());
            assertEquals(category, savedRequest.getCategory());
            assertEquals(Priority.ALTA, savedRequest.getPriority());
            assertEquals(Status.ABERTA, savedRequest.getStatus());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando categoria não existir ou for inativa")
        void shouldThrowExceptionWhenCategoryNotFound() {
            UUID categoryId = UUID.randomUUID();
            CreateRequestRequest requestDTO = new CreateRequestRequest(
                    "Problema de rede",
                    "Sem conexão",
                    categoryId,
                    Priority.MEDIA
            );

            when(categoryRepository.findByIdAndActiveIsTrue(categoryId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.create(requestDTO)
            );

            assertEquals("Not Found (404): Categoria não existe.", exception.getMessage());
            verify(requestRepository, never()).save(any(Request.class));
        }
    }

    @Nested
    @DisplayName("Busca de Solicitação por ID")
    class FindRequestByIdTests {
        @Test
        @DisplayName("Deve retornar a solicitação correspondente quando o ID existir")
        void shouldReturnRequestWhenIdExists() {
            UUID requestId = UUID.randomUUID();
            Category category = new Category("Manutenção");
            Request request = new Request("Vazamento", "Vazamento na pia", category, Priority.BAIXA);

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

            RequestResponse response = service.findById(requestId);

            assertNotNull(response);
            assertEquals("Vazamento", response.title());
            assertEquals("Vazamento na pia", response.description());
            assertEquals("Manutenção", response.categoryName());
            assertEquals(Priority.BAIXA, response.priority());
            assertEquals(Status.ABERTA, response.status());

            verify(requestRepository, times(1)).findById(requestId);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando solicitação não for encontrada")
        void shouldThrowExceptionWhenRequestNotFound() {
            UUID requestId = UUID.randomUUID();
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.findById(requestId)
            );

            assertEquals("Not Found (404): Solicitação não encontrada.", exception.getMessage());
            verify(requestRepository, times(1)).findById(requestId);
        }
    }

    @Nested
    @DisplayName("Listagem de Solicitações com Filtros")
    class FindAllRequestsTests {
        @Test
        @DisplayName("Deve listar solicitações com paginação e filtros com sucesso")
        void shouldReturnPagedRequestsWithFilters() {
            Category category = new Category("TI");
            Request request1 = new Request("Monitor piscando", "Monitor do lab 1", category, Priority.MEDIA);
            Request request2 = new Request("Teclado quebrado", "Teclado do lab 2", category, Priority.BAIXA);
            List<Request> requests = List.of(request1, request2);

            Pageable expectedPageable = PageRequest.of(0, 10);
            Page<Request> requestPage = new PageImpl<>(requests, expectedPageable, requests.size());

            when(requestRepository.findAll(any(Specification.class), eq(expectedPageable))).thenReturn(requestPage);

            Page<RequestResponse> result = service.findAll(
                    Status.ABERTA,
                    Priority.MEDIA,
                    category.getId(),
                    "Monitor",
                    1,
                    10
            );

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(2, result.getContent().size());
            assertEquals("Monitor piscando", result.getContent().get(0).title());
            assertEquals("Teclado quebrado", result.getContent().get(1).title());

            verify(requestRepository, times(1)).findAll(any(Specification.class), eq(expectedPageable));
        }

        @Test
        @DisplayName("Deve normalizar valores de página e tamanho menores que o limite permitido")
        void shouldNormalizeNegativeOrZeroPageAndSize() {
            Pageable normalizedPageable = PageRequest.of(0, 1);
            when(requestRepository.findAll(any(Specification.class), eq(normalizedPageable))).thenReturn(Page.empty());

            Page<RequestResponse> result = service.findAll(null, null, null, null, 0, 0);

            assertNotNull(result);
            verify(requestRepository, times(1)).findAll(any(Specification.class), eq(normalizedPageable));
        }

        @Test
        @DisplayName("Deve limitar o tamanho máximo por página para 25 elementos")
        void shouldClampMaxSizeTo25() {
            Pageable clampedPageable = PageRequest.of(0, 25);
            when(requestRepository.findAll(any(Specification.class), eq(clampedPageable))).thenReturn(Page.empty());

            Page<RequestResponse> result = service.findAll(null, null, null, null, 1, 100);

            assertNotNull(result);
            verify(requestRepository, times(1)).findAll(any(Specification.class), eq(clampedPageable));
        }
    }
}
