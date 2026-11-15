package com.fcolucasvieira.central_solicitacoes_api.request.service;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.category.repository.CategoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.CreateRequestRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.RequestResponse;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.UpdateStatusRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.Request;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestStatusHistoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.specification.RequestSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final RequestStatusHistoryRepository historyRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public RequestResponse create(CreateRequestRequest request) {
        Category category = categoryRepository.findByIdAndActiveIsTrue(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Not Found (404): Categoria não encontrada."));

        Request req = new Request(request.title(), request.description(), category, request.priority());

        requestRepository.save(req);

        return RequestResponse.from(req);
    }

    @Transactional(readOnly = true)
    public RequestResponse findById(UUID id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found (404): Solicitação não encontrada."));

        return RequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public Page<RequestResponse> findAll(
            Status status,
            Priority priority,
            UUID categoryId,
            String title,
            int page,
            int size
            ) {
        page = Math.max(page - 1, 0);
        size = Math.min(Math.max(size, 1), 25);

        Pageable pageable = PageRequest.of(page, size);

        Specification<Request> spec = Specification
                .where(RequestSpecs.hasStatus(status))
                .and(RequestSpecs.hasPriority(priority))
                .and(RequestSpecs.hasCategoryId(categoryId))
                .and(RequestSpecs.titleContains(title));

        return requestRepository.findAll(spec, pageable)
                .map(RequestResponse::from);
    }

    @Transactional
    public RequestResponse updateStatus(UUID id, UpdateStatusRequest request) {
        Request req = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found (404): Solicitação não encontrada."));

        req.updateStatus(request.status());

        return RequestResponse.from(req);
    }
}
