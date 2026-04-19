# Task Manager API

API REST para gerenciamento de tarefas pessoais, construída com Java 21 e Spring Boot 3.4.x.

## Tecnologias

- Java 21
- Spring Boot 3.4.5
- Spring Security 6 + JWT (jjwt 0.12.6)
- Spring Data JPA + Hibernate 6
- PostgreSQL 16
- Flyway
- MapStruct 1.5.5
- Lombok
- Springdoc OpenAPI 2.8.8
- JUnit 5 + Mockito

## Pré-requisitos

- Java 21+
- Docker e Docker Compose

## Como executar

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/taskmanager.git
cd taskmanager

# 2. Suba o banco de dados
docker compose up -d

# 3. Execute a aplicação
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080/api`.
O Swagger UI estará em `http://localhost:8080/api/swagger-ui/index.html`.

## Diagrama de entidades

```
┌─────────────────────────┐         ┌──────────────────────────────┐
│          users          │         │            tasks             │
├─────────────────────────┤         ├──────────────────────────────┤
│ id          BIGSERIAL PK│◄────┐   │ id           BIGSERIAL PK   │
│ name        VARCHAR(100)│     └───│ user_id      BIGINT FK      │
│ email       VARCHAR(255)│         │ title        VARCHAR(200)   │
│ password    VARCHAR(255)│         │ description  VARCHAR(1000)  │
│ created_at  TIMESTAMP   │         │ status       VARCHAR(20)    │
└─────────────────────────┘         │ due_date     DATE           │
                                    │ created_at   TIMESTAMP      │
                                    │ updated_at   TIMESTAMP      │
                                    └──────────────────────────────┘

TaskStatus: PENDING → IN_PROGRESS → DONE  (fluxo unidirecional)
```

## Endpoints

### Autenticação

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | `/auth/register` | Cadastra novo usuário | Não |
| POST | `/auth/login` | Autentica e retorna token JWT | Não |

### Tarefas

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | `/tasks` | Cria nova tarefa | Sim |
| GET | `/tasks` | Lista tarefas com filtros e paginação | Sim |
| GET | `/tasks/{id}` | Busca tarefa por ID | Sim |
| PUT | `/tasks/{id}` | Atualiza título, descrição e prazo | Sim |
| PATCH | `/tasks/{id}/status` | Avança o status da tarefa | Sim |
| DELETE | `/tasks/{id}` | Remove uma tarefa | Sim |

### Usuário

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| GET | `/users/me` | Dados do usuário autenticado | Sim |
| GET | `/users/me/summary` | Contagem de tarefas por status | Sim |

## Exemplos de uso

### Registrar usuário
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@email.com","password":"senha123"}' | jq
```

### Login e captura do token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","password":"senha123"}' | jq -r '.token')
```

### Criar tarefa
```bash
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Estudar Spring Boot","description":"Foco em segurança JWT","dueDate":"2026-12-01"}' | jq
```

### Listar tarefas com filtro
```bash
curl -s "http://localhost:8080/api/tasks?status=PENDING&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Avançar status
```bash
curl -s -X PATCH http://localhost:8080/api/tasks/1/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}' | jq
```

### Resumo do usuário
```bash
curl -s http://localhost:8080/api/users/me/summary \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Regras de negócio

- Usuário só acessa suas próprias tarefas (403 caso contrário)
- Prazo não pode ser uma data no passado
- Tarefa com status `DONE` não pode ser editada
- Fluxo de status é unidirecional: `PENDING → IN_PROGRESS → DONE`
- Listagem suporta filtros por `status` e `dueBefore`, com paginação máxima de 50 itens

## Estrutura do projeto

```
src/main/java/br/leetjourney/taskmanager/
├── config/          # OpenApiConfig
├── controller/      # AuthController, TaskController, UserController
├── domain/
│   ├── enums/       # TaskStatus
│   └── model/       # User, Task
├── dto/
│   ├── request/     # RegisterRequest, LoginRequest, TaskRequest, TaskStatusRequest
│   └── response/    # UserResponse, AuthResponse, TaskResponse, UserSummaryResponse
├── exception/       # BusinessException, ResourceNotFoundException,
│                    # ForbiddenException, GlobalExceptionHandler
├── mapper/          # UserMapper, TaskMapper
├── repository/      # UserRepository, TaskRepository
├── security/        # JwtService, JwtAuthenticationFilter, SecurityConfig
└── service/         # AuthService, TaskService, UserService, UserDetailsServiceImpl
```