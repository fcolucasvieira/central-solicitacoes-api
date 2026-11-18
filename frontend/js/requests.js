/**
 * REQUESTS.JS - GERENCIAMENTO DE SOLICITAÇÕES, LISTAGEM, FILTROS, TRANSIÇÃO DE STATUS E HISTÓRICO
 * 
 * Responsabilidades:
 * 1. Carregar categorias paginadas do Back-End (GET /categories)
 * 2. Cadastrar nova solicitação (POST /requests)
 * 3. Listar solicitações com paginação e filtros (GET /requests)
 * 4. Transição de status estrita ABERTA -> EM_ANDAMENTO -> CONCLUIDA (PATCH /requests/{id}/status) [RF04]
 * 5. Linha do tempo com histórico de mudanças (GET /requests/{id}/history) [Opcional / Diferencial]
 * 6. Notificações Toast para feedback fluido
 */

import { apiFetch } from './api.js';

// Estado global da tela
let currentPage = 1;
const pageSize = 10;
let totalPages = 1;

/**
 * 1. Busca as categorias ativas no Back-End (GET /categories?page=1&size=25)
 */
export async function loadCategories() {
  try {
    const response = await apiFetch('/categories?page=1&size=25');
    const categories = response?.content || [];

    const filterSelect = document.getElementById('filter-category');
    const modalSelect = document.getElementById('req-category');

    categories.forEach(category => {
      const optFilter = document.createElement('option');
      optFilter.value = category.id;
      optFilter.textContent = category.name;
      filterSelect.appendChild(optFilter);

      const optModal = document.createElement('option');
      optModal.value = category.id;
      optModal.textContent = category.name;
      modalSelect.appendChild(optModal);
    });
  } catch (error) {
    console.error('Falha ao carregar categorias:', error);
  }
}

/**
 * 2. Consulta paginada com filtros combinados (GET /requests)
 */
export async function loadRequests() {
  const tableBody = document.getElementById('requests-table-body');
  const loadingState = document.getElementById('table-loading');
  const emptyState = document.getElementById('table-empty');
  const paginationInfo = document.getElementById('pagination-info');
  const paginationCurrentPage = document.getElementById('pagination-current-page');
  const btnPrev = document.getElementById('btn-prev-page');
  const btnNext = document.getElementById('btn-next-page');

  const status = document.getElementById('filter-status').value;
  const priority = document.getElementById('filter-priority').value;
  const categoryId = document.getElementById('filter-category').value;
  const title = document.getElementById('filter-title').value.trim();

  loadingState.classList.remove('hidden');
  emptyState.classList.add('hidden');
  tableBody.innerHTML = '';

  try {
    const params = new URLSearchParams({
      page: currentPage.toString(),
      size: pageSize.toString()
    });

    if (status) params.append('status', status);
    if (priority) params.append('priority', priority);
    if (categoryId) params.append('categoryId', categoryId);
    if (title) params.append('title', title);

    const response = await apiFetch(`/requests?${params.toString()}`);
    const requests = response?.content || [];
    const pageMeta = response?.page || {};

    totalPages = pageMeta.totalPages || 1;
    const totalElements = pageMeta.totalElements || requests.length;

    paginationInfo.textContent = `Mostrando ${requests.length} de ${totalElements} solicitação(ões)`;
    paginationCurrentPage.textContent = `Página ${currentPage} de ${Math.max(totalPages, 1)}`;
    btnPrev.disabled = currentPage <= 1;
    btnNext.disabled = currentPage >= totalPages;

    if (requests.length === 0) {
      emptyState.classList.remove('hidden');
      return;
    }

    requests.forEach(req => {
      const tr = document.createElement('tr');
      const dateFormatted = req.createdAt ? formatDate(req.createdAt) : '-';
      const statusLabel = formatStatusLabel(req.status);
      const priorityLabel = formatPriorityLabel(req.priority);

      // Botão de Ação contextual com base no status atual (RF04)
      let actionButtonHtml = '';
      if (req.status === 'ABERTA') {
        actionButtonHtml = `
          <button 
            class="btn btn-sm btn-warning btn-action-status" 
            data-id="${req.id}" 
            data-next-status="EM_ANDAMENTO" 
            title="Iniciar atendimento da solicitação"
            type="button"
          >
            Iniciar
          </button>
        `;
      } else if (req.status === 'EM_ANDAMENTO') {
        actionButtonHtml = `
          <button 
            class="btn btn-sm btn-success btn-action-status" 
            data-id="${req.id}" 
            data-next-status="CONCLUIDA" 
            title="Concluir a solicitação"
            type="button"
          >
            Concluir
          </button>
        `;
      } else if (req.status === 'CONCLUIDA') {
        actionButtonHtml = `
          <span style="font-size: 0.75rem; color: var(--color-success); font-weight: 600; display: inline-flex; align-items: center; gap: 0.25rem;">
            ✓ Finalizada
          </span>
        `;
      }

      tr.innerHTML = `
        <td>
          <span class="req-table-title">${escapeHtml(req.title)}</span>
          <span class="req-table-desc" title="${escapeHtml(req.description)}">${escapeHtml(req.description)}</span>
        </td>
        <td>
          <span class="badge badge-category">${escapeHtml(req.categoryName || 'Geral')}</span>
        </td>
        <td>
          <span class="badge badge-priority-${req.priority}">${priorityLabel}</span>
        </td>
        <td>
          <span class="badge badge-status-${req.status}">${statusLabel}</span>
        </td>
        <td style="color: var(--color-text-muted); font-size: 0.8125rem;">
          ${dateFormatted}
        </td>
        <td style="text-align: center;">
          <div class="table-actions-cell">
            ${actionButtonHtml}
            <button 
              class="btn btn-sm btn-outline btn-open-history" 
              data-id="${req.id}" 
              data-title="${escapeHtml(req.title)}"
              title="Ver histórico de transições"
              type="button"
            >
              Histórico
            </button>
          </div>
        </td>
      `;

      tableBody.appendChild(tr);
    });

  } catch (error) {
    console.error('Falha ao carregar solicitações:', error);
    showToast(error.message, 'danger');
  } finally {
    loadingState.classList.add('hidden');
  }
}

/**
 * 3. Gerenciamento de cliques na Tabela (Avançar Status e Abrir Histórico)
 */
export function initTableActions() {
  const tableBody = document.getElementById('requests-table-body');

  tableBody.addEventListener('click', async (e) => {
    // 1. Clique para Avançar Status (RF04)
    const btnStatus = e.target.closest('.btn-action-status');
    if (btnStatus) {
      const requestId = btnStatus.dataset.id;
      const nextStatus = btnStatus.dataset.nextStatus;

      btnStatus.disabled = true;
      btnStatus.textContent = '...';

      try {
        await apiFetch(`/requests/${requestId}/status`, {
          method: 'PATCH',
          body: JSON.stringify({ status: nextStatus })
        });

        const statusName = nextStatus === 'EM_ANDAMENTO' ? 'Em andamento' : 'Concluída';
        showToast(`Status atualizado para: ${statusName}`, 'success');

        // Recarrega a tabela para atualizar badge e botão
        await loadRequests();
      } catch (error) {
        showToast(error.message, 'danger');
        btnStatus.disabled = false;
        btnStatus.textContent = nextStatus === 'EM_ANDAMENTO' ? 'Iniciar' : 'Concluir';
      }
      return;
    }

    // 2. Clique para Abrir Histórico de Transições
    const btnHistory = e.target.closest('.btn-open-history');
    if (btnHistory) {
      const requestId = btnHistory.dataset.id;
      const requestTitle = btnHistory.dataset.title;
      openHistoryModal(requestId, requestTitle);
    }
  });
}

/**
 * 4. Modal de Histórico de Mudanças de Status (Linha do Tempo)
 */
async function openHistoryModal(requestId, requestTitle) {
  const modal = document.getElementById('modal-history');
  const subtitle = document.getElementById('history-modal-subtitle');
  const timeline = document.getElementById('history-timeline');
  const loading = document.getElementById('history-loading');

  subtitle.textContent = `Demanda: "${requestTitle}"`;
  timeline.innerHTML = '';
  loading.classList.remove('hidden');
  modal.classList.remove('hidden');

  try {
    const historyList = await apiFetch(`/requests/${requestId}/history`);
    loading.classList.add('hidden');

    if (!historyList || historyList.length === 0) {
      timeline.innerHTML = '<p style="color: var(--color-text-muted); font-size: 0.875rem;">Nenhum histórico registrado.</p>';
      return;
    }

    historyList.forEach(item => {
      const itemEl = document.createElement('div');
      itemEl.className = 'timeline-item';

      const dateFormatted = item.changedAt ? formatDate(item.changedAt) : '-';
      const statusLabel = formatStatusLabel(item.status);

      itemEl.innerHTML = `
        <div class="timeline-dot dot-${item.status}"></div>
        <div class="timeline-content">
          <div class="timeline-header">
            <span class="badge badge-status-${item.status}">${statusLabel}</span>
            <span class="timeline-time">${dateFormatted}</span>
          </div>
          <span class="timeline-user">
            Alterado por: <strong>${escapeHtml(item.changedByName || 'Usuário')}</strong>
          </span>
        </div>
      `;

      timeline.appendChild(itemEl);
    });

  } catch (error) {
    loading.classList.add('hidden');
    timeline.innerHTML = `<p style="color: var(--color-danger); font-size: 0.875rem;">Erro ao carregar histórico: ${escapeHtml(error.message)}</p>`;
  }
}

export function initHistoryModal() {
  const modal = document.getElementById('modal-history');
  const btnCloseX = document.getElementById('btn-close-history-modal');
  const btnClose = document.getElementById('btn-close-history');

  const closeModal = () => modal.classList.add('hidden');

  btnCloseX.addEventListener('click', closeModal);
  btnClose.addEventListener('click', closeModal);
  modal.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
  });
}

/**
 * 5. Inicializa Filtros, Busca e Paginação
 */
export function initFiltersAndPagination() {
  const filterInputs = [
    document.getElementById('filter-status'),
    document.getElementById('filter-priority'),
    document.getElementById('filter-category')
  ];

  const searchInput = document.getElementById('filter-title');
  const btnClear = document.getElementById('btn-clear-filters');
  const btnPrev = document.getElementById('btn-prev-page');
  const btnNext = document.getElementById('btn-next-page');

  filterInputs.forEach(select => {
    select.addEventListener('change', () => {
      currentPage = 1;
      loadRequests();
    });
  });

  let debounceTimeout;
  searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => {
      currentPage = 1;
      loadRequests();
    }, 350);
  });

  btnClear.addEventListener('click', () => {
    filterInputs.forEach(select => select.value = '');
    searchInput.value = '';
    currentPage = 1;
    loadRequests();
    showToast('Filtros redefinidos', 'info');
  });

  btnPrev.addEventListener('click', () => {
    if (currentPage > 1) {
      currentPage--;
      loadRequests();
    }
  });

  btnNext.addEventListener('click', () => {
    if (currentPage < totalPages) {
      currentPage++;
      loadRequests();
    }
  });
}

/**
 * 6. Modal de Cadastro de Solicitações (POST /requests)
 */
export function initRequestModal(onSuccessCallback) {
  const modal = document.getElementById('modal-new-request');
  const btnOpen = document.getElementById('btn-open-modal');
  const btnClose = document.getElementById('btn-close-modal');
  const btnCancel = document.getElementById('btn-cancel-modal');
  const form = document.getElementById('form-new-request');
  const alertBox = document.getElementById('modal-alert');
  const submitBtn = document.getElementById('btn-submit-request');

  btnOpen.addEventListener('click', () => {
    hideAlert();
    form.reset();
    modal.classList.remove('hidden');
    document.getElementById('req-title').focus();
  });

  const closeModal = () => {
    modal.classList.add('hidden');
    hideAlert();
    form.reset();
  };

  btnClose.addEventListener('click', closeModal);
  btnCancel.addEventListener('click', closeModal);

  modal.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();

    const title = document.getElementById('req-title').value.trim();
    const categoryId = document.getElementById('req-category').value;
    const priority = document.getElementById('req-priority').value;
    const description = document.getElementById('req-description').value.trim();

    if (!title || !categoryId || !priority || !description) {
      showAlert('Preencha todos os campos obrigatórios (*)', 'danger');
      return;
    }

    try {
      setLoading(submitBtn, true, 'Registrando...');

      await apiFetch('/requests', {
        method: 'POST',
        body: JSON.stringify({
          title,
          description,
          categoryId,
          priority
        })
      });

      closeModal();
      showToast('Solicitação registrada com sucesso!', 'success');

      if (typeof onSuccessCallback === 'function') {
        onSuccessCallback();
      }

    } catch (error) {
      if (error.data?.errors) {
        const errorMessages = Object.values(error.data.errors).join('<br>');
        showAlert(errorMessages, 'danger', true);
      } else {
        showAlert(error.message, 'danger');
      }
    } finally {
      setLoading(submitBtn, false, 'Registrar Solicitação');
    }
  });

  function showAlert(msg, type = 'danger', isHtml = false) {
    alertBox.className = `alert alert-${type}`;
    if (isHtml) {
      alertBox.innerHTML = msg;
    } else {
      alertBox.textContent = msg;
    }
    alertBox.style.display = 'block';
  }

  function hideAlert() {
    alertBox.style.display = 'none';
    alertBox.textContent = '';
  }

  function setLoading(btn, isLoading, text) {
    btn.disabled = isLoading;
    btn.textContent = text;
  }
}

/**
 * 7. Notificações Toast Flutuantes (Fluidez e Feedback)
 */
export function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;

  container.appendChild(toast);

  // Remove automaticamente após 3.5 segundos
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(16px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// Funções Auxiliares de Formatação e Segurança
function formatDate(isoString) {
  try {
    const date = new Date(isoString);
    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  } catch {
    return isoString;
  }
}

function formatStatusLabel(status) {
  const map = {
    'ABERTA': 'Aberta',
    'EM_ANDAMENTO': 'Em andamento',
    'CONCLUIDA': 'Concluída'
  };
  return map[status] || status;
}

function formatPriorityLabel(priority) {
  const map = {
    'BAIXA': 'Baixa',
    'MEDIA': 'Média',
    'ALTA': 'Alta'
  };
  return map[priority] || priority;
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text || '';
  return div.innerHTML;
}
