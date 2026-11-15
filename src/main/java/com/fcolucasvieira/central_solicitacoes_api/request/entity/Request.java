package com.fcolucasvieira.central_solicitacoes_api.request.entity;

import com.fcolucasvieira.central_solicitacoes_api.category.entity.Category;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "requests")
@Getter
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ABERTA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Request() {}

    public Request(String title, String description, Category category, Priority priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
    }

    public void updateStatus(Status status) {
        switch (this.status) {
            case ABERTA:
                if (!status.equals(Status.EM_ANDAMENTO)) {
                    throw new IllegalArgumentException("Conflict (409): Solicitação deve realizar transição de status de ABERTA para EM_ANDAMENTO.");
                }
                this.status = status;
                break;
            case EM_ANDAMENTO:
                if (!status.equals(Status.CONCLUIDA)) {
                    throw new IllegalArgumentException("Conflict (409): Solicitação deve realizar transição de status de EM_ANDAMENTO para CONCLUIDA.");
                }
                this.status = status;
                break;
            case CONCLUIDA:
                throw new IllegalArgumentException("Conflict (409): Não é possível realizar transição de status como CONCLUIDA.");
        }
    }
}
