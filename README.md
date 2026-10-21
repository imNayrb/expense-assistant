# Expense Assistant

Registra le tue spese scrivendole come le diresti, *"ieri 30€ di benzina"*, e fai
domande sui tuoi dati in linguaggio naturale. Claude trasforma le frasi in spese
strutturate tramite tool use, e un server MCP rende gli stessi dati disponibili a
Claude Desktop e Claude Code.

> 🚧 Lavori in corso: i componenti vengono aggiunti una pull request alla volta.
> Vedi il [CHANGELOG](CHANGELOG.md).

## Componenti

| Cartella      | Stack                      | Ruolo                                        |
|---------------|----------------------------|----------------------------------------------|
| `backend/`    | Java 21, Spring Boot 3     | API REST, persistenza, integrazione con Claude |
| `mcp-server/` | Python 3.11+, MCP SDK      | Espone le spese ai client MCP                |
| `frontend/`   | Angular                    | Interfaccia web                              |

## Licenza

[MIT](LICENSE)
