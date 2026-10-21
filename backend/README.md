# Backend

Spring Boot 3 REST API (Java 21). It stores expenses and integrates Claude for
natural-language input and conversational queries.

## Run

```bash
export ANTHROPIC_API_KEY=sk-ant-...   # optional: without it, only CRUD works
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. By default data lives in a local
H2 file (`./data/`). To use PostgreSQL:

```bash
SPRING_PROFILES_ACTIVE=postgres DB_HOST=localhost DB_USER=expenses DB_PASSWORD=expenses mvn spring-boot:run
```

## Test

```bash
mvn verify
```

Tests never call the Claude API: the `LlmClient` interface is replaced by fakes,
and the HTTP contract is checked with `MockRestServiceServer`.

## Configuration

| Variable               | Default                  | Purpose                                  |
|------------------------|--------------------------|------------------------------------------|
| `ANTHROPIC_API_KEY`    | *(empty)*                | Enables the assistant endpoints          |
| `ANTHROPIC_MODEL`      | `claude-haiku-5-5`       | Model used for parsing and chat          |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200`  | Comma-separated origins for the browser  |
| `SPRING_PROFILES_ACTIVE` | *(none)*               | `postgres` to use PostgreSQL             |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | see `application-postgres.yml` | PostgreSQL connection |

## API

| Method | Path                        | Description                                           |
|--------|-----------------------------|-------------------------------------------------------|
| GET    | `/api/expenses`             | List, filters: `from`, `to` (ISO dates), `category`. Defaults to the current month |
| GET    | `/api/expenses/summary`     | Total and per-category totals for `from`–`to`         |
| GET    | `/api/expenses/{id}`        | One expense                                           |
| POST   | `/api/expenses`             | Create                                                |
| PUT    | `/api/expenses/{id}`        | Update                                                |
| DELETE | `/api/expenses/{id}`        | Delete                                                |
| GET    | `/api/categories`           | Category codes with Italian labels                    |
| GET    | `/api/assistant/status`     | Whether the assistant is enabled, and the model       |
| POST   | `/api/assistant/parse`      | `{"text": "ieri 30€ di benzina"}` → draft expense (not saved) |
| POST   | `/api/assistant/chat`       | `{"messages": [{"role":"user","content":"..."}]}` → answer |

Errors follow RFC 9457 (`application/problem+json`): 400 validation, 404 not
found, 422 text not recognized as an expense, 502 Claude API error, 503 assistant
disabled.

## Example

```bash
curl -s localhost:8080/api/assistant/parse -H 'Content-Type: application/json' \
  -d '{"text":"ieri 30€ di benzina"}'
# {"description":"Benzina","amount":30.00,"currency":"EUR","category":"FUEL",
#  "categoryLabel":"Carburante","date":"2026-10-20"}
```

## Package structure

```
com.imnayrb.expenseassistant
├── config/        properties, beans (Clock, LlmClient, CORS)
├── expense/       entity, repository, service, REST controllers, DTOs
├── assistant/     ExpenseParser (forced tool use), ChatAssistant (agentic loop), ExpenseTools
│   └── llm/       LlmClient port + Messages API client and wire models
└── web/           error handling (problem details)
```
