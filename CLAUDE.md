# CLAUDE.md

Linee guida per Claude (e per chiunque contribuisca) che lavora su questo repository.

## Cos'è il progetto

**Expense Assistant** è un tracker delle spese personali in cui le spese si inseriscono
in linguaggio naturale ("30€ di benzina ieri") e vengono trasformate in dati strutturati
da Claude tramite tool use. Gli stessi dati si possono interrogare in modo conversazionale,
sia dall'app web sia da qualsiasi client MCP (Claude Desktop, Claude Code) attraverso un
server MCP dedicato.

## Struttura del repository

```
expense-assistant/
├── backend/      API REST Spring Boot 3 (Java 21): persistenza + integrazione con Claude
├── mcp-server/   Server MCP in Python che espone le spese come tool, resource e prompt
├── frontend/     Web app Angular (interfaccia in italiano)
├── docs/         Note di architettura (ARCHITETTURA.md) e screenshot
├── scripts/      Test end-to-end dello stack (smoke-test.sh)
├── .github/      Workflow di CI e template delle PR
├── CHANGELOG.md  Cronostoria di tutte le modifiche
└── CLAUDE.md     Questo file
```

Ogni componente è autonomo: ha build, test e README propri, e comunica con gli altri
solo tramite l'API HTTP del backend.

## Comandi

| Componente | Build / avvio                                 | Test                       |
|------------|-----------------------------------------------|----------------------------|
| backend    | `cd backend && mvn spring-boot:run`           | `cd backend && mvn verify` |
| mcp-server | `cd mcp-server && pip install -e ".[dev]"`    | `cd mcp-server && pytest`  |
| frontend   | `cd frontend && npm ci && npm start`          | `cd frontend && npm test`  |
| tutto      | `docker compose up --build`                   | `./scripts/smoke-test.sh`  |

Il backend richiede `ANTHROPIC_API_KEY` nell'ambiente per le funzioni dell'assistente;
gli endpoint CRUD funzionano anche senza.

## Convenzioni

- **Lingua**:
  - in **italiano**: messaggi di commit, nomi dei branch, titoli e descrizioni delle PR,
    commenti e Javadoc/docstring, documentazione, testi dell'interfaccia, CHANGELOG;
  - in **inglese**: identificatori del codice (classi, metodi, variabili, endpoint) e
    i prompt di sistema inviati a Claude.
- **Commit**: [Conventional Commits](https://www.conventionalcommits.org/it/) con il tipo
  standard e la descrizione in italiano, all'imperativo:
  `feat(backend): aggiungi il riepilogo per categoria`, `fix(frontend): correggi ...`,
  `docs: ...`, `chore: ...`, `ci: ...`, `test: ...`.
- **Branch**: `feat/<argomento>`, `fix/<argomento>`, `chore/<argomento>`, `docs/<argomento>`,
  con l'argomento in italiano (es. `feat/riepilogo-mensile`).
  Una pull request per ogni modifica coerente, limitata a un componente quando possibile.
- **Changelog**: ogni pull request aggiunge una voce a `CHANGELOG.md` (formato
  Keep a Changelog, dalla più recente). Una PR senza voce nel changelog è incompleta.
- **Segreti**: mai committare chiavi API o file `.env`. La configurazione arriva dalle
  variabili d'ambiente; `.env.example` le documenta.
- **Test**: ogni nuovo comportamento ha i suoi test. I test non chiamano mai l'API di
  Claude: nel backend si usa un fake di `LlmClient`, nel server MCP `httpx.MockTransport`.

## Regole di architettura

- Il backend è l'**unica fonte di verità**: server MCP e frontend non accedono mai
  direttamente al database.
- Tutte le chiamate all'API di Claude passano da `LlmClient` nel backend, così modello,
  versione dell'API e dettagli HTTP stanno in un solo punto.
- Gli importi sono `BigDecimal` in Java e numeri decimali in JSON, mai `double`.
- Le date sono `LocalDate` nel fuso `Europe/Rome` per impostazione predefinita
  (`app.zone`), così espressioni come "ieri" vengono risolte correttamente.
