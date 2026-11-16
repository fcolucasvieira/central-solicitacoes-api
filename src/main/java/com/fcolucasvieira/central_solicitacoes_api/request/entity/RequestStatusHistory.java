package com.fcolucasvieira.central_solicitacoes_api.request.entity;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import com.fcolucasvieira.central_solicitacoes_api.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requests_status_history")
@Getter
public class RequestStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt = Instant.now();

    protected RequestStatusHistory() {}

    public RequestStatusHistory(Request request, User changedBy, Status status) {
        this.request = request;
        this.changedBy = changedBy;
        this.status = status;
    }
}
