# Central de Solicitações

Aplicação web para gerenciamento de solicitações internas, permitindo registrar, consultar e acompanhar solicitações ao longo de seu processo de resolução.

O projeto está sendo desenvolvido como parte do desafio técnico da Loading Jr., com foco em organização, regras de negócio, persistência de dados e integração entre Back-End e Front-End.

## Tecnologias

### Back-End
- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Hibernate
- Bean Validation
- Maven
- Lombok

### Banco de dados e infraestrutura
- PostgreSQL 17
- Docker
- Docker Compose
- Flyway Migration

### Front-End
- HTML
- CSS
- JavaScript

## Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

- JDK 25
- Docker e Docker Compose
- Git

## Configuração e execução

### 1. Clone o repositório

Clone o projeto e acesse seu diretório.

### 2. Configure as variáveis de ambiente

Na raiz do projeto, copie o arquivo `.env.example`, renomeando a cópia para `.env`.

Preencha as variáveis com as credenciais que serão utilizadas pelo PostgreSQL:

| Variável | Descrição |
|---|---|
| `POSTGRES_DB` | Nome do banco de dados |
| `POSTGRES_USER` | Usuário do banco |
| `POSTGRES_PASSWORD` | Senha do banco |

**Importante:** o arquivo `.env` contém informações locais e não deve ser enviado ao repositório.

### 3. Inicie o banco de dados

Com o Docker em execução, utilize o Docker Compose para iniciar o serviço PostgreSQL.

### 4. Execute a aplicação

A aplicação pode ser iniciada pela IDE ou utilizando o Maven Wrapper disponibilizado no projeto.

## Estrutura do projeto

A organização do Back-End seguirá uma abordagem modular, agrupando componentes de acordo com seus respectivos domínios e responsabilidades.

## Status do desenvolvimento

**Em desenvolvimento.**

Atualmente, o projeto possui a configuração inicial do ambiente de desenvolvimento e a integração com o PostgreSQL por meio do Docker Compose.

As funcionalidades da Central de Solicitações serão implementadas progressivamente, priorizando os requisitos obrigatórios do desafio.

## Observações

- O banco de dados utiliza um volume Docker nomeado para persistência dos dados.
- As configurações sensíveis são fornecidas por variáveis de ambiente.
- O Flyway será responsável pelo versionamento e gerenciamento das alterações do esquema do banco de dados.
