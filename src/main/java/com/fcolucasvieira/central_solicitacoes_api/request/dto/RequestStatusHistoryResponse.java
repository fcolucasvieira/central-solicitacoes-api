package com.fcolucasvieira.central_solicitacoes_api.request.dto;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.RequestStatusHistory;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;

import java.time.Instant;
import java.util.UUID;

public record RequestStatusHistoryResponse(
        UUID id,
        String changedByName,
        Status status,
        Instant changedAt
) {
    public static RequestStatusHistoryResponse from(RequestStatusHistory statusHistory) {
        return new RequestStatusHistoryResponse(
                statusHistory.getId(),
                statusHistory.getChangedBy().getName(),
                statusHistory.getStatus(),
                statusHistory.getChangedAt()
        );
    }
}
