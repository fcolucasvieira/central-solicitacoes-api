/**
 * REQUESTS.JS - GERENCIAMENTO DE SOLICITAÇÕES, LISTAGEM, FILTROS E PAGINAÇÃO
 * 
 * Responsabilidades:
 * 1. Carregar categorias paginadas do Back-End (GET /categories)
 * 2. Abrir, fechar e submeter o modal de cadastro (POST /requests)
 * 3. Listar solicitações com paginação e filtros combinados (GET /requests)
 */

import { apiFetch } from './api.js';

// Estado global de paginação e filtros da tela
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
    console.error('Erro ao carregar categorias:', error);
  }
}

/**
 * 2. Consulta paginada com filtros combinados (GET /requests)
 * Integrado à Specification (RequestSpecs) do Spring Boot
 */
export async function loadRequests() {
  const tableBody = document.getElementById('requests-table-body');
  const loadingState = document.getElementById('table-loading');
  const emptyState = document.getElementById('table-empty');
  const paginationInfo = document.getElementById('pagination-info');
  const paginationCurrentPage = document.getElementById('pagination-current-page');
  const btnPrev = document.getElementById('btn-prev-page');
  const btnNext = document.getElementById('btn-next-page');

  // Captura os valores selecionados nos filtros da tela
  const status = document.getElementById('filter-status').value;
  const priority = document.getElementById('filter-priority').value;
  const categoryId = document.getElementById('filter-category').value;
  const title = document.getElementById('filter-title').value.trim();

  // Exibe o estado de loading e limpa linhas antigas
  loadingState.classList.remove('hidden');
  emptyState.classList.add('hidden');
  tableBody.innerHTML = '';

  try {
    // Monta os parâmetros de URL dinamicamente
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

    // Atualiza controles de paginação
    paginationInfo.textContent = `Mostrando ${requests.length} de ${totalElements} solicitação(ões)`;
    paginationCurrentPage.textContent = `Página ${currentPage} de ${Math.max(totalPages, 1)}`;
    btnPrev.disabled = currentPage <= 1;
    btnNext.disabled = currentPage >= totalPages;

    // Se não houver solicitações, mostra o estado vazio
    if (requests.length === 0) {
      emptyState.classList.remove('hidden');
      return;
    }

    // Renderiza cada solicitação na tabela
    requests.forEach(req => {
      const tr = document.createElement('tr');

      // Formatação amigável da data (dd/MM/yyyy às HH:mm)
      const dateFormatted = req.createdAt ? formatDate(req.createdAt) : '-';

      // Textos amigáveis para status e prioridade
      const statusLabel = formatStatusLabel(req.status);
      const priorityLabel = formatPriorityLabel(req.priority);

      tr.innerHTML = `
        <td>
          <span class="req-table-title">${escapeHtml(req.title)}</span>
          <span class="req-table-desc" title="${escapeHtml(req.description)}">${escapeHtml(req.description)}</span>
        </td>
        <td>
          <span class="badge badge-category">${escapeHtml(req.category?.name || 'Geral')}</span>
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
        <td style="text-align: right;">
          <span style="font-size: 0.75rem; color: var(--color-text-muted); font-weight: 500;">
            #${req.id.substring(0, 8)}
          </span>
        </td>
      `;

      tableBody.appendChild(tr);
    });

  } catch (error) {
    console.error('Falha ao carregar solicitações:', error);
  } finally {
    loadingState.classList.add('hidden');
  }
}

/**
 * 3. Inicializa os ouvintes da barra de filtros e da paginação
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

  // Ao alterar qualquer select de filtro, reseta para página 1 e recarrega
  filterInputs.forEach(select => {
    select.addEventListener('change', () => {
      currentPage = 1;
      loadRequests();
    });
  });

  // Busca textual com debounce suave ao digitar
  let debounceTimeout;
  searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(() => {
      currentPage = 1;
      loadRequests();
    }, 350); // Aguarda 350ms após o usuário parar de digitar
  });

  // Botão Limpar Filtros
  btnClear.addEventListener('click', () => {
    filterInputs.forEach(select => select.value = '');
    searchInput.value = '';
    currentPage = 1;
    loadRequests();
  });

  // Botões de Paginação
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
 * 4. Inicializa o Modal de Cadastro (abrir, fechar e submeter POST /requests)
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

      // Recarrega a tabela imediatamente para exibir a nova solicitação
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

// Funções Auxiliares de Formatação
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

// Proteção básica contra injeção de HTML (XSS)
function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text || '';
  return div.innerHTML;
}
