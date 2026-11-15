package com.fcolucasvieira.central_solicitacoes_api.request.entity;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import com.fcolucasvieira.central_solicitacoes_api.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requests_status_history")
public class RequestHistoryStatus {
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
}
