package com.fcolucasvieira.central_solicitacoes_api.request.service;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.category.repository.CategoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.exception.InvalidStatusTransitionException;
import com.fcolucasvieira.central_solicitacoes_api.exception.ResourceNotFoundException;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.CreateRequestRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.RequestResponse;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.RequestStatusHistoryResponse;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.UpdateStatusRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.Request;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.RequestStatusHistory;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestStatusHistoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User("Lucas Vieira", "lucas@email.com", "hash");
        var auth = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

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

            ArgumentCaptor<RequestStatusHistory> historyCaptor = ArgumentCaptor.forClass(RequestStatusHistory.class);
            verify(historyRepository, times(1)).save(historyCaptor.capture());

            RequestStatusHistory savedHistory = historyCaptor.getValue();
            assertEquals(Status.ABERTA, savedHistory.getStatus());
            assertEquals(authenticatedUser, savedHistory.getChangedBy());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando categoria não existir ou for inativa")
        void shouldThrowExceptionWhenCategoryNotFound() {
            UUID categoryId = UUID.randomUUID();
            CreateRequestRequest requestDTO = new CreateRequestRequest(
                    "Problema de rede",
                    "Sem conexão",
                    categoryId,
                    Priority.MEDIA
            );

            when(categoryRepository.findByIdAndActiveIsTrue(categoryId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.create(requestDTO)
            );

            assertEquals("Categoria não encontrada para o ID informado: " + categoryId, exception.getMessage());
            verify(requestRepository, never()).save(any(Request.class));
            verify(historyRepository, never()).save(any(RequestStatusHistory.class));
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
        @DisplayName("Deve lançar ResourceNotFoundException quando solicitação não for encontrada")
        void shouldThrowExceptionWhenRequestNotFound() {
            UUID requestId = UUID.randomUUID();
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findById(requestId)
            );

            assertEquals("Solicitação não encontrada para o ID informado: " + requestId, exception.getMessage());
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

    @Nested
    @DisplayName("Atualização de Status da Solicitação")
    class UpdateStatusTests {

        @Test
        @DisplayName("Deve atualizar status de ABERTA para EM_ANDAMENTO com sucesso")
        void shouldUpdateStatusFromAbertaToEmAndamentoSuccessfully() {
            UUID requestId = UUID.randomUUID();
            Category category = new Category("TI");
            Request request = new Request("Falha no login", "Usuário bloqueado", category, Priority.ALTA);
            assertEquals(Status.ABERTA, request.getStatus());

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

            RequestResponse response = service.updateStatus(requestId, new UpdateStatusRequest(Status.EM_ANDAMENTO));

            assertNotNull(response);
            assertEquals(Status.EM_ANDAMENTO, response.status());
            assertEquals(Status.EM_ANDAMENTO, request.getStatus());
            verify(requestRepository, times(1)).findById(requestId);

            ArgumentCaptor<RequestStatusHistory> historyCaptor = ArgumentCaptor.forClass(RequestStatusHistory.class);
            verify(historyRepository, times(1)).save(historyCaptor.capture());

            RequestStatusHistory savedHistory = historyCaptor.getValue();
            assertEquals(Status.EM_ANDAMENTO, savedHistory.getStatus());
            assertEquals(authenticatedUser, savedHistory.getChangedBy());
        }

        @Test
        @DisplayName("Deve atualizar status de EM_ANDAMENTO para CONCLUIDA com sucesso")
        void shouldUpdateStatusFromEmAndamentoToConcluidaSuccessfully() {
            UUID requestId = UUID.randomUUID();
            Category category = new Category("TI");
            Request request = new Request("Falha no login", "Usuário bloqueado", category, Priority.ALTA);
            request.updateStatus(Status.EM_ANDAMENTO);
            assertEquals(Status.EM_ANDAMENTO, request.getStatus());

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

            RequestResponse response = service.updateStatus(requestId, new UpdateStatusRequest(Status.CONCLUIDA));

            assertNotNull(response);
            assertEquals(Status.CONCLUIDA, response.status());
            assertEquals(Status.CONCLUIDA, request.getStatus());
            verify(requestRepository, times(1)).findById(requestId);

            ArgumentCaptor<RequestStatusHistory> historyCaptor = ArgumentCaptor.forClass(RequestStatusHistory.class);
            verify(historyRepository, times(1)).save(historyCaptor.capture());

            RequestStatusHistory savedHistory = historyCaptor.getValue();
            assertEquals(Status.CONCLUIDA, savedHistory.getStatus());
            assertEquals(authenticatedUser, savedHistory.getChangedBy());
        }

        @Test
        @DisplayName("Deve lançar InvalidStatusTransitionException ao tentar saltar de ABERTA direto para CONCLUIDA")
        void shouldThrowExceptionWhenSkippingFromAbertaToConcluida() {
            UUID requestId = UUID.randomUUID();
            Category category = new Category("TI");
            Request request = new Request("Falha no login", "Usuário bloqueado", category, Priority.ALTA);
            assertEquals(Status.ABERTA, request.getStatus());

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

            InvalidStatusTransitionException exception = assertThrows(
                    InvalidStatusTransitionException.class,
                    () -> service.updateStatus(requestId, new UpdateStatusRequest(Status.CONCLUIDA))
            );

            assertEquals("Solicitação deve realizar transição de status de ABERTA para EM_ANDAMENTO.", exception.getMessage());
            assertEquals(Status.ABERTA, request.getStatus());
            verify(requestRepository, times(1)).findById(requestId);
            verify(historyRepository, never()).save(any(RequestStatusHistory.class));
        }

        @Test
        @DisplayName("Deve lançar InvalidStatusTransitionException ao tentar transição inválida em solicitação já CONCLUIDA")
        void shouldThrowExceptionWhenUpdatingConcluidaRequest() {
            UUID requestId = UUID.randomUUID();
            Category category = new Category("TI");
            Request request = new Request("Falha no login", "Usuário bloqueado", category, Priority.ALTA);
            request.updateStatus(Status.EM_ANDAMENTO);
            request.updateStatus(Status.CONCLUIDA);
            assertEquals(Status.CONCLUIDA, request.getStatus());

            when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

            InvalidStatusTransitionException exception = assertThrows(
                    InvalidStatusTransitionException.class,
                    () -> service.updateStatus(requestId, new UpdateStatusRequest(Status.EM_ANDAMENTO))
            );

            assertEquals("Não é possível realizar transição de status para solicitação já CONCLUIDA.", exception.getMessage());
            assertEquals(Status.CONCLUIDA, request.getStatus());
            verify(requestRepository, times(1)).findById(requestId);
            verify(historyRepository, never()).save(any(RequestStatusHistory.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando ID da solicitação não for encontrado")
        void shouldThrowExceptionWhenRequestNotFoundOnUpdateStatus() {
            UUID requestId = UUID.randomUUID();
            when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.updateStatus(requestId, new UpdateStatusRequest(Status.EM_ANDAMENTO))
            );

            assertEquals("Solicitação não encontrada para o ID informado: " + requestId, exception.getMessage());
            verify(requestRepository, times(1)).findById(requestId);
            verify(historyRepository, never()).save(any(RequestStatusHistory.class));
        }
    }

    @Nested
    @DisplayName("Consulta de Histórico de Status")
    class FindHistoryByRequestIdTests {

        @Test
        @DisplayName("Deve retornar o histórico de status em ordem cronológica quando solicitação existir")
        void shouldReturnHistoryWhenRequestExists() {
            UUID requestId = UUID.randomUUID();
            Category category = new Category("TI");
            Request request = new Request("Falha no login", "Usuário bloqueado", category, Priority.ALTA);

            RequestStatusHistory historyEntry = new RequestStatusHistory(request, authenticatedUser, Status.ABERTA);

            when(requestRepository.existsById(requestId)).thenReturn(true);
            when(historyRepository.findByRequestIdOrderByChangedAt(requestId)).thenReturn(List.of(historyEntry));

            List<RequestStatusHistoryResponse> result = service.findHistoryByRequestId(requestId);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(Status.ABERTA, result.get(0).status());
            assertEquals(authenticatedUser.getName(), result.get(0).changedByName());

            verify(requestRepository, times(1)).existsById(requestId);
            verify(historyRepository, times(1)).findByRequestIdOrderByChangedAt(requestId);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando solicitação não for encontrada")
        void shouldThrowExceptionWhenRequestNotFound() {
            UUID requestId = UUID.randomUUID();
            when(requestRepository.existsById(requestId)).thenReturn(false);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> service.findHistoryByRequestId(requestId)
            );

            assertEquals("Solicitação não encontrada para o ID informado: " + requestId, exception.getMessage());
            verify(historyRepository, never()).findByRequestIdOrderByChangedAt(any());
        }
    }
}
