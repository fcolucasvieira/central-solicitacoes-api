# Central de Solicitações

API para gerenciamento de solicitações internas, desenvolvida como parte do desafio técnico do processo seletivo da **Loading Desenvolvimento Jr.**

O projeto tem como objetivo centralizar o registro e o acompanhamento de solicitações, permitindo organizar demandas por categoria, prioridade e status, além de manter o histórico de suas alterações.

> **Status:** Em desenvolvimento.

---

## Objetivo

Construir uma aplicação para gerenciar solicitações internas de uma organização, substituindo controles informais por um fluxo estruturado e persistente.

O fluxo principal de uma solicitação contempla três etapas:

`ABERTA → EM_ANDAMENTO → CONCLUIDA`

A aplicação deverá garantir que as transições respeitem essa sequência, evitando mudanças inválidas no estado das solicitações.

---

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 17 |
| Versionamento do banco | Flyway |
| Build | Maven |
| Ambiente do banco | Docker Compose |
| Front-End planejado | HTML, CSS e JavaScript |

---

## Arquitetura e organização

O Back-End utiliza uma organização modular orientada aos domínios da aplicação, agrupando componentes relacionados à mesma responsabilidade de negócio.

Estrutura geral:

```text
src/
├── main/
│   ├── java/
│   │   └── com/fcolucasvieira/central_solicitacoes_api/
│   │       ├── auth/
│   │       ├── config/
│   │       ├── exception/
│   │       ├── jwt/
│   │       ├── request/
│   │       ├── category/
│   │       └── user/
│   │
│   └── resources/
│       ├── db/
│       │   └── migration/
│       └── application.yaml
│
└── test/
```

Os módulos representam as principais áreas do sistema:

- **Auth:** registro e autenticação de usuários com geração de token JWT.
- **Config:** configurações de segurança (Spring Security) e documentação (Swagger/OpenAPI).
- **Exception:** tratamento global de exceções com respostas padronizadas.
- **JWT:** serviço de geração e validação de tokens e filtro de autenticação por requisição.
- **Request:** gerenciamento das solicitações, seus status e histórico.
- **Category:** organização das categorias utilizadas pelas solicitações.
- **User:** representação dos usuários do sistema.

A estrutura busca manter as responsabilidades organizadas por domínio, facilitando a manutenção e a evolução da aplicação.

---

## Modelagem do banco de dados

O modelo relacional foi estruturado para representar as principais entidades do domínio e preservar a integridade dos dados e dos relacionamentos.

O diagrama abaixo apresenta o schema inicial da aplicação:

![Diagrama do schema inicial do banco de dados](docs/initial_schema.png)

As principais entidades são:

- **Request:** representa uma solicitação, contendo título, descrição, categoria, prioridade e status atual.
- **Category:** representa as categorias utilizadas para classificar as solicitações.
- **User:** representa os usuários do sistema.
- **RequestStatusHistory:** registra os status alcançados por uma solicitação e o usuário responsável pela alteração.

O histórico é mantido separadamente da solicitação, permitindo preservar os registros das mudanças de status ao longo do tempo.

---

## Configuração e execução

### Pré-requisitos

- JDK 25;
- Maven;
- Docker e Docker Compose;
- Git.

### 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd central-solicitacoes-api
```

### 2. Configurar as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto, seguindo o modelo disponível em `.env.example`.

```env
POSTGRES_DB=central_solicitacoes
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha

JWT_SECRET_KEY=sua_chave_secreta_com_no_minimo_32_caracteres
EXPIRATION_MS=86400000
JWT_ISSUER=central-solicitacoes-api
```

> A chave `JWT_SECRET_KEY` deve ter no mínimo 32 caracteres para garantir a segurança da assinatura HMAC-SHA256.

### 3. Iniciar o banco de dados

```bash
docker compose up -d
```

O PostgreSQL será iniciado em um container Docker, com persistência dos dados por meio de um volume nomeado.

### 4. Executar a aplicação

Com o banco de dados em execução, inicie a aplicação pela sua IDE ou utilizando o Maven:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

As migrations do Flyway são responsáveis pelo versionamento e pela criação da estrutura do banco de dados.

---

## Documentação da API (Swagger / OpenAPI)

A aplicação conta com documentação interativa gerada via **Springdoc OpenAPI (Swagger UI)**.

Com a aplicação em execução, a documentação pode ser acessada em:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html` (ou `http://localhost:8080/swagger-ui.html`)
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

A documentação detalha esquemas de requisição/resposta, exemplos e códigos de status HTTP para todos os endpoints da API (autenticação, categorias, solicitações, etc.).

### Autenticação no Swagger UI

Os endpoints protegidos exigem um token JWT. Para autenticar no Swagger UI:

1. Use `POST /auth/register` ou `POST /auth/login` para obter um token.
2. Clique no botão **Authorize** no topo da página.
3. No campo **Value**, informe: `<seu_token_jwt>` (sem o prefixo `Bearer`).
4. Clique em **Authorize** e feche o modal.

Todos os endpoints protegidos passarão a enviar o header `Authorization: Bearer <token>` automaticamente.

---

## Interface Web (Front-End)

A aplicação conta com uma interface web moderna, responsiva e desenvolvida com **HTML5 semântico, CSS3 e JavaScript puro (ES6 Modules)**, aplicando a identidade visual e o Brandbook oficial da **Loading Desenvolvimento Jr.**

### Funcionalidades da Interface:
- **Autenticação:** Telas de Login e Criação de Conta integradas à API via JWT, com persistência da sessão no `localStorage` e fluxo de logout.
- **Painel de Solicitações:** Listagem paginada consumindo `GET /requests`, com badges temáticos de status, prioridade e data formatada.
- **Filtros Combináveis & Busca:** Filtro simultâneo por Status, Prioridade, Categoria (carregada dinamicamente via `GET /categories`) e Busca textual no título com técnica de debounce (350ms).
- **Cadastro Dinâmico (RF01):** Modal flutuante para abertura de novas demandas (`POST /requests`) com validação de campos e feedback amigável.
- **Transição de Status (RF04):** Botões de ação contextuais na tabela para avançar o fluxo estrito (`ABERTA → EM_ANDAMENTO → CONCLUIDA`) com chamadas `PATCH /requests/{id}/status`.
- **Linha do Tempo de Histórico (Opcional):** Modal com timeline cronológica completa de todas as alterações de status, exibindo o responsável e o timestamp exato (`GET /requests/{id}/history`).
- **Fluidez & UX:** Notificações flutuantes (*Toasts*) não intrusivas para feedback de operações e estados de *Loading* e *Empty State*.

### Como Executar o Front-End:
1. Certifique-se de que a API Back-End esteja em execução na porta `8080`.
2. Abra a pasta do projeto no **VS Code**.
3. Com a extensão **Live Server** instalada, clique com o botão direito no arquivo `frontend/index.html` e selecione **"Open with Live Server"**.
4. A aplicação será aberta no navegador em `http://127.0.0.1:5500/frontend/index.html`.

---

## Tratamento de Concorrência (Lock Otimista)

Para assegurar a consistência dos dados e a integridade das transições de status em cenários de requisições simultâneas, a aplicação adota **bloqueio otimista (*Optimistic Locking*)**:

- **Mecanismo:** A entidade `Request` possui um atributo `@Version private Long version`, mapeado para a coluna `version` gerenciada no schema via migração do Flyway (`V3`).
- **Comportamento Concorrente:** Caso duas instâncias ou usuários tentem atualizar a mesma solicitação simultaneamente, o Hibernate detecta o conflito de versão e lança a exceção `ObjectOptimisticLockingFailureException`.
- **Tratamento Global:** A exceção é capturada pelo `GlobalExceptionHandler` e devolvida com status HTTP `409 Conflict`, no padrão RFC 7807 (`ProblemDetail`), impedindo sobrescrita inadvertida e garantindo feedback transparente para que o cliente recarregue o recurso.

---

## Endpoints e Funcionalidades da API

A tabela abaixo sintetiza os principais endpoints disponibilizados pela API:

| Método | Endpoint | Módulo | Descrição | Autenticação |
| :--- | :--- | :--- | :--- | :---: |
| `POST` | `/auth/register` | Auth | Cadastra um novo usuário no sistema e retorna seus dados | Não |
| `POST` | `/auth/login` | Auth | Autentica um usuário existente e emite o token JWT de acesso | Não |
| `POST` | `/categories` | Categorias | Cria uma nova categoria de solicitação | Sim (Bearer) |
| `GET` | `/categories` | Categorias | Lista categorias ativas de forma paginada | Sim (Bearer) |
| `GET` | `/categories/{id}` | Categorias | Consulta os detalhes de uma categoria por ID | Sim (Bearer) |
| `DELETE` | `/categories/{id}` | Categorias | Realiza a exclusão lógica (desativação) de uma categoria | Sim (Bearer) |
| `POST` | `/requests` | Solicitações | Registra uma nova solicitação com status inicial `ABERTA` | Sim (Bearer) |
| `GET` | `/requests` | Solicitações | Lista solicitações com paginação, ordenação decrescente e filtros combináveis | Sim (Bearer) |
| `GET` | `/requests/{id}` | Solicitações | Consulta os detalhes de uma solicitação por ID | Sim (Bearer) |
| `PATCH` | `/requests/{id}/status` | Solicitações | Executa a transição estrita de status (`ABERTA → EM_ANDAMENTO → CONCLUIDA`) | Sim (Bearer) |
| `GET` | `/requests/{id}/history` | Solicitações | Retorna a linha do tempo com o histórico de transições e responsáveis | Sim (Bearer) |

---

## Autor

**Lucas Vieira**:
Estudante de Engenharia de Computação — UFC Sobral

- GitHub: [github.com/fcolucasvieira](https://github.com/fcolucasvieira)
- LinkedIn: [linkedin.com/in/fco-lucas-vieira](https://www.linkedin.com/in/fco-lucas-vieira/)