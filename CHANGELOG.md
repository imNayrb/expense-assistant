# Changelog

All notable changes to this project are documented in this file, newest first.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-10-21

### Added
- **Backend** (Spring Boot 3.5, Java 21):
  - Expense CRUD REST API with validation, date-range and category filters.
  - Summary endpoint with per-category totals aggregated in the database.
  - `/api/categories` with Italian labels.
  - Persistence with Spring Data JPA; schema managed by Flyway; H2 by default,
    PostgreSQL via the `postgres` profile.
  - Natural-language parsing (`POST /api/assistant/parse`) using Claude with a
    forced `record_expense` tool, returning a draft to confirm before saving.
  - Conversational queries (`POST /api/assistant/chat`) with an agentic tool-use
    loop over `list_expenses`, `get_summary`, `add_expense`, capped at 5 rounds.
  - `LlmClient` port with an HTTP client for the Claude Messages API.
  - RFC 9457 problem details for errors; CORS configuration for the frontend.
  - Integration and unit tests; the Claude API is never called in tests.
- CI workflow (GitHub Actions) building and testing the backend.

## [0.1.0] - 2026-10-21

### Added
- `CLAUDE.md` with project overview, planned layout (`backend/`, `mcp-server/`,
  `frontend/`, `docs/`), commands, conventions and architecture rules.
- `CHANGELOG.md` to keep a chronological history of changes.
- Shared editor and git settings (`.editorconfig`, `.gitattributes`, `.gitignore`).
- Pull request template and MIT license.
