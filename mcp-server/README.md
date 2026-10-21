# Server MCP

Server [Model Context Protocol](https://modelcontextprotocol.io) che espone le spese
di Expense Assistant a qualsiasi client MCP: Claude Desktop, Claude Code o altri.
Così puoi chiedere a Claude *"quanto ho speso in ristoranti questo mese?"* direttamente
dal client che usi già.

Il server non accede al database: chiama l'API REST del backend, che deve essere avviato.

## Cosa espone

| Primitiva | Nome                                | Descrizione                                   |
|-----------|-------------------------------------|-----------------------------------------------|
| Tool      | `list_expenses`                     | Spese in un intervallo, filtro per categoria (sola lettura) |
| Tool      | `get_summary`                       | Totale e totali per categoria (sola lettura)  |
| Tool      | `add_expense`                       | Registra una spesa                            |
| Tool      | `delete_expense`                    | Elimina una spesa (distruttivo)               |
| Resource  | `expenses://categories`             | Categorie con etichetta in italiano           |
| Resource  | `expenses://summary/current-month`  | Riepilogo del mese corrente                   |
| Prompt    | `monthly_review`                    | Analisi guidata delle spese di un mese        |

I tool dichiarano le *annotations* MCP (`read_only_hint`, `destructive_hint`), così il
client può chiedere conferma prima delle operazioni distruttive.

## Installazione

```bash
cd mcp-server
python -m venv .venv && source .venv/bin/activate
pip install -e ".[dev]"
```

## Configurazione nei client

Variabili d'ambiente: `EXPENSE_API_URL` (default `http://localhost:8080`) ed
`EXPENSE_TIMEZONE` (default `Europe/Rome`).

**Claude Code**

```bash
claude mcp add expense-assistant -e EXPENSE_API_URL=http://localhost:8080 -- \
  /percorso/assoluto/mcp-server/.venv/bin/expense-mcp-server
```

**Claude Desktop** (`claude_desktop_config.json`)

```json
{
  "mcpServers": {
    "expense-assistant": {
      "command": "/percorso/assoluto/mcp-server/.venv/bin/expense-mcp-server",
      "env": { "EXPENSE_API_URL": "http://localhost:8080" }
    }
  }
}
```

## Test

```bash
pytest
ruff check .
```

I test usano un backend finto (`httpx.MockTransport`) e il `Client` dell'SDK MCP
collegato al server in-process: nessuna rete, nessun processo esterno.
