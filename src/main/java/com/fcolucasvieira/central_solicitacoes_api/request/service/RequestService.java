package com.fcolucasvieira.central_solicitacoes_api.request.service;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.category.repository.CategoryRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.CreateRequestRequest;
import com.fcolucasvieira.central_solicitacoes_api.request.dto.RequestResponse;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.Request;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestRepository;
import com.fcolucasvieira.central_solicitacoes_api.request.repository.RequestStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new IllegalArgumentException("Not Found (404): Categoria não existe."));

        Request newRequest = new Request(request.title(), request.description(), category, request.priority());

        requestRepository.save(newRequest);

        return RequestResponse.from(newRequest);
    }

    @Transactional(readOnly = true)
    public RequestResponse findById(UUID id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Not Found (404): Solicitação não encontrada."));

        return RequestResponse.from(request);
    }
}
