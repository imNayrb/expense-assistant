"""Test end-to-end del server MCP contro un backend reale.

Avvia il server come farebbe Claude Desktop (processo separato, trasporto stdio) e lo
usa con il client dell'SDK MCP. Viene eseguito solo se `EXPENSE_E2E_API_URL` punta a un
backend avviato, per esempio con `docker compose up`; altrimenti è saltato.
"""

from __future__ import annotations

import json
import os
import sys

import pytest
from mcp import Client, StdioServerParameters

pytestmark = [
    pytest.mark.anyio,
    pytest.mark.skipif(
        not os.environ.get("EXPENSE_E2E_API_URL"),
        reason="imposta EXPENSE_E2E_API_URL per eseguire i test end-to-end",
    ),
]


@pytest.fixture
def anyio_backend() -> str:
    return "asyncio"


@pytest.fixture
def server() -> StdioServerParameters:
    return StdioServerParameters(
        command=sys.executable,
        args=["-m", "expense_mcp"],
        env={**os.environ, "EXPENSE_API_URL": os.environ["EXPENSE_E2E_API_URL"]},
    )


async def test_add_summarize_and_delete_through_mcp(server: StdioServerParameters) -> None:
    async with Client(server) as client:
        added = await client.call_tool(
            "add_expense",
            {
                "description": "Cinema",
                "amount": 9.5,
                "category": "ENTERTAINMENT",
                "expense_date": "2026-09-12",
            },
        )
        assert not added.is_error, added.content
        expense_id = added.structured_content["id"]

        summary = await client.call_tool("get_summary", {"from_date": "2026-09-01", "to_date": "2026-09-30"})
        assert not summary.is_error, summary.content
        assert summary.structured_content["total"] == 9.5

        listed = await client.call_tool("list_expenses", {"from_date": "2026-09-01", "to_date": "2026-09-30"})
        assert "Cinema" in listed.content[0].text

        categories = await client.read_resource("expenses://categories")
        codes = {c["code"] for c in json.loads(categories.contents[0].text)}
        assert "ENTERTAINMENT" in codes

        deleted = await client.call_tool("delete_expense", {"expense_id": expense_id})
        assert not deleted.is_error, deleted.content

        missing = await client.call_tool("delete_expense", {"expense_id": expense_id})
        assert missing.is_error
        assert "non trovata" in missing.content[0].text
