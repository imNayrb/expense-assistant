# CLAUDE.md

Guidance for Claude (and any other contributor) working in this repository.

## What this project is

**Expense Assistant** is a personal expense tracker where expenses are entered in
natural language ("30€ di benzina ieri") and turned into structured records by
Claude through tool use. The same data can be queried conversationally, both from
the web app and from any MCP client (Claude Desktop, Claude Code) through a
dedicated MCP server.

## Repository layout

```
expense-assistant/
├── backend/      Spring Boot 3 REST API (Java 21) — persistence + Claude integration
├── mcp-server/   Python MCP server exposing the expenses as tools, resources, prompts
├── frontend/     Angular web app (UI in Italian)
├── docs/         Architecture notes (ARCHITETTURA.md, in Italian)
├── .github/      CI workflows and PR template
├── CHANGELOG.md  Chronological history of every change
└── CLAUDE.md     This file
```

Each component is self-contained: it has its own build, tests and README, and talks
to the others only through the backend's HTTP API.

## Commands

| Component  | Build / run                                   | Tests                     |
|------------|-----------------------------------------------|---------------------------|
| backend    | `cd backend && mvn spring-boot:run`           | `cd backend && mvn verify`|
| mcp-server | `cd mcp-server && pip install -e ".[dev]"`    | `cd mcp-server && pytest` |
| frontend   | `cd frontend && npm ci && npm start`          | `cd frontend && npm test` |
| everything | `docker compose up --build`                   | CI on every pull request  |

The backend needs `ANTHROPIC_API_KEY` in the environment for the assistant
features; CRUD endpoints work without it.

## Conventions

- **Language**: code, identifiers, commits and repo docs in English; user-facing
  UI text in Italian; `docs/ARCHITETTURA.md` in Italian.
- **Commits**: [Conventional Commits](https://www.conventionalcommits.org/)
  (`feat(backend): ...`, `fix(frontend): ...`, `docs: ...`, `chore: ...`, `ci: ...`).
- **Branches**: `feat/<topic>`, `fix/<topic>`, `chore/<topic>`, `docs/<topic>`.
  One pull request per coherent change, scoped to one component when possible.
- **Changelog**: every pull request adds an entry to `CHANGELOG.md`
  (Keep a Changelog format, newest first). A PR without a changelog entry is
  incomplete.
- **Secrets**: never commit API keys or `.env` files. Configuration comes from
  environment variables; `.env.example` documents them.
- **Tests**: new behaviour comes with tests. The Claude API is never called in
  tests — use the `LlmClient` fake in the backend and `httpx.MockTransport` in the
  MCP server.

## Architecture rules

- The backend is the **single source of truth**: the MCP server and the frontend
  never touch the database directly.
- All calls to the Claude API go through `LlmClient` in the backend, so the model,
  API version and HTTP details live in one place.
- Money is `BigDecimal` in Java and a decimal string/number in JSON — never `double`.
- Dates are `LocalDate` in the `Europe/Rome` zone by default (`app.zone`), so
  relative expressions like "ieri" resolve correctly.
