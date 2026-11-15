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
}
