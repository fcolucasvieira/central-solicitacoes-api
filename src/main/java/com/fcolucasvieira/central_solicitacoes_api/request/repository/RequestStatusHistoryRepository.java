package com.fcolucasvieira.central_solicitacoes_api.request.repository;

import com.fcolucasvieira.central_solicitacoes_api.request.entity.RequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, UUID> {}
