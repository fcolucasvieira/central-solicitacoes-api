package com.fcolucasvieira.central_solicitacoes_api.request.specification;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.Request;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Priority;
import com.fcolucasvieira.central_solicitacoes_api.request.entity.enums.Status;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class RequestSpecs {
    public static Specification<Request> hasStatus(Status status) {
        return (root, query, cb) -> status == null
                ? null
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Request> hasPriority(Priority priority) {
        return (root, query, cb) -> priority == null
                ? null
                : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Request> hasCategoryId(UUID categoryId) {
        return (root, query, cb) -> categoryId == null
                ? null
                : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Request> titleContains(String title) {
        return (root, query, cb) -> {
                if (title == null || title.isBlank()) return null;
                String searchPattern = "%" + title.trim().toLowerCase() + "%";
                return cb.like(cb.lower(root.get("title")), searchPattern);
                };
    }
}
