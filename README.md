# Expense Assistant

Registra le tue spese scrivendole come le diresti, *"ieri 30€ di benzina"*, e fai
domande sui tuoi dati in linguaggio naturale. Claude trasforma le frasi in spese
strutturate tramite **tool use**, e un **server MCP** rende gli stessi dati disponibili
a Claude Desktop e Claude Code.

<p align="center">
  <img src="docs/screenshots/panoramica.png" alt="Panoramica con il totale del mese" width="24%" />
  <img src="docs/screenshots/scontrino.png" alt="Spesa interpretata da Claude come scontrino da confermare" width="24%" />
  <img src="docs/screenshots/statistiche.png" alt="Statistiche per categoria" width="24%" />
  <img src="docs/screenshots/assistente.png" alt="Chat con l'assistente" width="24%" />
</p>

<p align="center">
  <img src="docs/screenshots/desktop.png" alt="Versione desktop" width="90%" />
</p>

## Funzionalità

- **Inserimento a parole**: la frase diventa una bozza (descrizione, importo, categoria,
  data) da confermare o correggere prima del salvataggio.
- **Chat sulle spese**: "quanto ho speso in ristoranti a ottobre?" — Claude interroga i
  dati reali con un loop agentico di tool use.
- **Riepilogo mensile** per categoria ed elenco delle spese con filtri.
- **Server MCP** con tool, resource e prompt per usare le spese da qualsiasi client MCP.
- Funziona anche **senza chiave API**, con l'inserimento manuale.

## Architettura

```mermaid
flowchart LR
    FE[Frontend Angular] -->|REST| BE[Backend Spring Boot]
    MCP[Server MCP Python] -->|REST| BE
    BE --> DB[(PostgreSQL / H2)]
    BE -->|Messages API| AI[Claude]
```

| Cartella | Stack | Ruolo |
|----------|-------|-------|
| [`backend/`](backend/README.md) | Java 21, Spring Boot 3, JPA, Flyway | API REST, persistenza, integrazione con Claude |
| [`mcp-server/`](mcp-server/README.md) | Python 3.11+, SDK MCP 2.x | Espone le spese ai client MCP |
| [`frontend/`](frontend/README.md) | Angular 21 | Interfaccia web |

Le scelte di progetto sono spiegate in [docs/ARCHITETTURA.md](docs/ARCHITETTURA.md).

## Avvio rapido con Docker

```bash
cp .env.example .env        # inserisci ANTHROPIC_API_KEY (facoltativa)
docker compose up --build
```

- App: http://localhost:4200
- API: http://localhost:8080

## Sviluppo locale

```bash
# Backend (H2 su file, nessun database da installare)
cd backend && ANTHROPIC_API_KEY=sk-ant-... mvn spring-boot:run

# Frontend (con proxy verso il backend)
cd frontend && npm ci && npm start

# Server MCP
cd mcp-server && pip install -e ".[dev]"
claude mcp add expense-assistant -- "$(pwd)/.venv/bin/expense-mcp-server"
```

## Qualità

Ogni pull request passa dalla CI: test del backend (integrazione con H2, parser, loop
della chat, contratto HTTP), test del server MCP, test e build del frontend, formattazione
e un **test end-to-end** che avvia tutto con Docker Compose (PostgreSQL, backend, nginx) e
usa il server MCP contro il backend reale. Nessun test chiama l'API di Claude.

```bash
docker compose up -d --build && ./scripts/smoke-test.sh
```

Le modifiche sono raccolte nel [CHANGELOG](CHANGELOG.md); le convenzioni del repository
sono in [CLAUDE.md](CLAUDE.md).

## Licenza

[MIT](LICENSE)
