# Changelog

Tutte le modifiche rilevanti del progetto sono documentate in questo file, dalla più recente.

Il formato si basa su [Keep a Changelog](https://keepachangelog.com/it-IT/1.1.0/)
e il progetto segue il [Versionamento Semantico](https://semver.org/lang/it/).

## [0.2.0] - 2026-10-21

### Aggiunto
- **Backend** (Spring Boot 3.5, Java 21):
  - API REST per il CRUD delle spese, con validazione e filtri per date e categoria.
  - Endpoint di riepilogo con i totali per categoria, aggregati nel database.
  - `/api/categories` con le etichette in italiano.
  - Persistenza con Spring Data JPA; schema gestito da Flyway; H2 di default,
    PostgreSQL con il profilo `postgres`.
  - Parsing del linguaggio naturale (`POST /api/assistant/parse`) con Claude e il tool
    `record_expense` forzato: restituisce una bozza da confermare prima del salvataggio.
  - Domande in forma di chat (`POST /api/assistant/chat`) con un loop agentico di tool use
    su `list_expenses`, `get_summary`, `add_expense`, limitato a 5 giri.
  - Porta `LlmClient` con un client HTTP per la Messages API di Claude.
  - Errori in formato RFC 9457 (problem details); configurazione CORS per il frontend.
  - Test di integrazione e unitari; i test non chiamano mai l'API di Claude.
- Workflow di CI (GitHub Actions) che compila e testa il backend.

## [0.1.0] - 2026-10-21

### Aggiunto
- `CLAUDE.md` con panoramica del progetto, struttura prevista (`backend/`, `mcp-server/`,
  `frontend/`, `docs/`), comandi, convenzioni e regole di architettura.
- `CHANGELOG.md` per tenere la cronostoria delle modifiche.
- Impostazioni condivise per editor e git (`.editorconfig`, `.gitattributes`, `.gitignore`).
- Template per le pull request e licenza MIT.
