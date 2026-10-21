"""Server MCP che espone le spese con le tre primitive del protocollo.

- **Tool**: azioni che il modello può eseguire (leggere, riepilogare, aggiungere, eliminare spese).
- **Resource**: dati di sola lettura che il client può allegare al contesto (categorie, mese corrente).
- **Prompt**: modelli di richiesta pronti all'uso, scelti dall'utente (revisione mensile).

Le docstring dei tool e le descrizioni dei parametri sono lette dal modello, quindi spiegano
quando usare ciascun tool.
"""

from __future__ import annotations

import json
import os
from datetime import date, datetime
from typing import Annotated, Any
from zoneinfo import ZoneInfo

from mcp.server.mcpserver import MCPServer
from mcp.server.mcpserver.exceptions import ResourceError, ToolError
from mcp.types import ToolAnnotations
from pydantic import Field

from expense_mcp.api import ExpenseApi, ExpenseApiError

DEFAULT_API_URL = "http://localhost:8080"
ZONE = ZoneInfo(os.environ.get("EXPENSE_TIMEZONE", "Europe/Rome"))

IsoDate = Annotated[str | None, Field(description="Data in formato YYYY-MM-DD.")]


def today() -> date:
    return datetime.now(ZONE).date()


def create_server(api: ExpenseApi) -> MCPServer:
    """Costruisce il server attorno a un client dell'API: nei test si passa un client finto."""
    mcp = MCPServer(
        name="expense-assistant",
        instructions=(
            "Tool per consultare e registrare le spese personali dell'utente. "
            "Gli importi sono in euro salvo diversa indicazione. "
            "Usa get_summary per le domande su quanto è stato speso, list_expenses per il dettaglio."
        ),
    )

    async def call(operation: Any) -> Any:
        # Gli errori del backend diventano ToolError: il modello legge il messaggio e può reagire.
        try:
            return await operation
        except ExpenseApiError as e:
            raise ToolError(str(e)) from e

    @mcp.tool(annotations=ToolAnnotations(read_only_hint=True))
    async def list_expenses(
        from_date: IsoDate = None,
        to_date: IsoDate = None,
        category: Annotated[str | None, Field(description="Codice categoria, es. FUEL.")] = None,
    ) -> list[dict[str, Any]]:
        """Elenca le spese in un intervallo di date, dalla più recente.

        Senza date restituisce il mese corrente. Usalo quando serve il dettaglio delle singole spese.
        """
        return await call(api.list_expenses(from_date, to_date, category))

    @mcp.tool(annotations=ToolAnnotations(read_only_hint=True))
    async def get_summary(from_date: IsoDate = None, to_date: IsoDate = None) -> dict[str, Any]:
        """Totale speso in un intervallo di date, con il totale per ogni categoria.

        Senza date usa il mese corrente. È il tool giusto per le domande "quanto ho speso".
        """
        return await call(api.summary(from_date, to_date))

    @mcp.tool(annotations=ToolAnnotations(destructive_hint=False, idempotent_hint=False))
    async def add_expense(
        description: Annotated[str, Field(description="Descrizione breve, in italiano.", min_length=1)],
        amount: Annotated[float, Field(description="Importo positivo.", gt=0)],
        category: Annotated[
            str, Field(description="Codice categoria (vedi la resource expenses://categories).")
        ],
        expense_date: IsoDate = None,
        currency: Annotated[str, Field(description="Codice ISO 4217.", pattern="^[A-Za-z]{3}$")] = "EUR",
    ) -> dict[str, Any]:
        """Registra una nuova spesa. Se la data manca, usa oggi.

        Usalo solo quando l'utente chiede esplicitamente di salvare una spesa.
        """
        payload = {
            "description": description.strip(),
            "amount": round(amount, 2),
            "currency": currency.upper(),
            "category": category.upper(),
            "date": expense_date or today().isoformat(),
        }
        return await call(api.create_expense(payload))

    @mcp.tool(annotations=ToolAnnotations(destructive_hint=True, idempotent_hint=True))
    async def delete_expense(
        expense_id: Annotated[int, Field(description="Id della spesa da eliminare.", gt=0)],
    ) -> str:
        """Elimina definitivamente una spesa. Chiedi conferma all'utente prima di usarlo."""
        await call(api.delete_expense(expense_id))
        return f"Spesa {expense_id} eliminata."

    @mcp.resource("expenses://categories", mime_type="application/json")
    async def categories() -> str:
        """Le categorie disponibili, con codice ed etichetta in italiano."""
        try:
            return json.dumps(await api.categories(), ensure_ascii=False)
        except ExpenseApiError as e:
            raise ResourceError(str(e)) from e

    @mcp.resource("expenses://summary/current-month", mime_type="application/json")
    async def current_month_summary() -> str:
        """Riepilogo delle spese del mese corrente."""
        try:
            return json.dumps(await api.summary(None, None), ensure_ascii=False)
        except ExpenseApiError as e:
            raise ResourceError(str(e)) from e

    @mcp.prompt()
    def monthly_review(
        month: Annotated[str, Field(description="Mese da analizzare, formato YYYY-MM.")],
    ) -> str:
        """Analisi guidata delle spese di un mese."""
        return (
            f"Analizza le mie spese del mese {month}. "
            "Usa get_summary per i totali e list_expenses per il dettaglio, poi dimmi: "
            "quanto ho speso in tutto, quali sono le tre categorie principali, "
            "se ci sono spese insolite e un consiglio concreto per il mese prossimo. "
            "Rispondi in italiano, in modo sintetico."
        )

    return mcp


def main() -> None:
    """Avvio da riga di comando: trasporto stdio, come si aspettano Claude Desktop e Claude Code."""
    api = ExpenseApi(os.environ.get("EXPENSE_API_URL", DEFAULT_API_URL))
    create_server(api).run("stdio")


if __name__ == "__main__":
    main()
