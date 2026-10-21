# Changelog

Tutte le modifiche rilevanti del progetto sono documentate in questo file, dalla più recente.

Il formato si basa su [Keep a Changelog](https://keepachangelog.com/it-IT/1.1.0/)
e il progetto segue il [Versionamento Semantico](https://semver.org/lang/it/).

## [0.4.0] - 2026-10-21

### Aggiunto
- **Frontend** (Angular 21, componenti standalone, signals, zoneless), in italiano:
  - pagina **Aggiungi**: la spesa scritta a parole viene interpretata da Claude e
    mostrata come uno scontrino modificabile da confermare; inserimento manuale quando
    l'assistente non è attivo;
  - pagina **Spese**: spese del mese raggruppate per giorno, filtro per categoria,
    eliminazione con conferma;
  - pagina **Riepilogo**: totale del mese e barre proporzionali per categoria;
  - pagina **Chiedi**: chat sulle spese con indicazione dei dati consultati da Claude.
  - Selettore del mese, formati italiani per date e importi, tema chiaro e scuro,
    proxy di sviluppo verso il backend.
  - Test con Vitest (utility delle date, client dell'API, flusso di inserimento).
- Job di CI per il frontend (Prettier, test, build di produzione).

## [0.3.0] - 2026-10-21

### Aggiunto
- **Server MCP** (Python, SDK MCP 2.x) che espone le spese ai client MCP
  (Claude Desktop, Claude Code):
  - tool `list_expenses`, `get_summary`, `add_expense`, `delete_expense`, con annotations
    MCP di sola lettura e distruttive;
  - resource `expenses://categories` ed `expenses://summary/current-month`;
  - prompt `monthly_review` per l'analisi guidata di un mese.
  - Il server chiama l'API REST del backend e converte gli errori in messaggi leggibili
    dal modello; trasporto stdio.
  - Test con backend finto (`httpx.MockTransport`) e client MCP in-process.
- Job di CI per il server MCP (ruff e pytest).

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
