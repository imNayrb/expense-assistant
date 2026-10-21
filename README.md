# Expense Assistant

Track your expenses by writing them the way you'd say them — *"ieri 30€ di benzina"* —
and ask questions about them in plain language. Claude turns sentences into
structured expenses through tool use, and an MCP server makes the same data
available to Claude Desktop and Claude Code.

> 🚧 Work in progress — components are being added one pull request at a time.
> See [CHANGELOG.md](CHANGELOG.md).

## Components

| Folder        | Stack                      | Role                                         |
|---------------|----------------------------|----------------------------------------------|
| `backend/`    | Java 21, Spring Boot 3     | REST API, persistence, Claude integration    |
| `mcp-server/` | Python 3.11+, MCP SDK      | Exposes expenses to MCP clients              |
| `frontend/`   | Angular                    | Web UI (Italian)                             |

## License

[MIT](LICENSE)
