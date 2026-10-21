"""Test del server MCP con un backend finto (httpx.MockTransport) e un client MCP in-process."""

from __future__ import annotations

import json
from typing import Any

import httpx
import pytest
from mcp import Client

from expense_mcp.api import ExpenseApi
from expense_mcp.server import create_server

pytestmark = pytest.mark.anyio

EXPENSE = {
    "id": 1,
    "description": "Benzina",
    "amount": 30.0,
    "currency": "EUR",
    "category": "FUEL",
    "categoryLabel": "Carburante",
    "date": "2026-10-20",
    "createdAt": "2026-10-20T08:00:00Z",
}
SUMMARY = {
    "from": "2026-10-01",
    "to": "2026-10-31",
    "total": 30.0,
    "count": 1,
    "categories": [{"category": "FUEL", "categoryLabel": "Carburante", "total": 30.0, "count": 1}],
}


@pytest.fixture
def anyio_backend() -> str:
    return "asyncio"


class FakeBackend:
    """Simula il backend Spring Boot e registra le richieste ricevute."""

    def __init__(self) -> None:
        self.requests: list[httpx.Request] = []

    def handler(self, request: httpx.Request) -> httpx.Response:
        self.requests.append(request)
        path, method = request.url.path, request.method
        if method == "GET" and path == "/api/expenses":
            return httpx.Response(200, json=[EXPENSE])
        if method == "GET" and path == "/api/expenses/summary":
            return httpx.Response(200, json=SUMMARY)
        if method == "POST" and path == "/api/expenses":
            body = json.loads(request.content)
            return httpx.Response(201, json={**EXPENSE, **body, "id": 2})
        if method == "DELETE" and path == "/api/expenses/1":
            return httpx.Response(204)
        if method == "DELETE":
            return httpx.Response(404, json={"title": "Spesa non trovata", "detail": "Spesa 99 non trovata"})
        if method == "GET" and path == "/api/categories":
            return httpx.Response(200, json=[{"code": "FUEL", "label": "Carburante"}])
        return httpx.Response(500)


@pytest.fixture
def backend() -> FakeBackend:
    return FakeBackend()


@pytest.fixture
def server(backend: FakeBackend):
    api = ExpenseApi("http://backend.test", transport=httpx.MockTransport(backend.handler))
    return create_server(api)


async def test_exposes_tools_resources_and_prompts(server) -> None:
    async with Client(server) as client:
        tools = {tool.name: tool for tool in (await client.list_tools()).tools}
        resources = {str(r.uri) for r in (await client.list_resources()).resources}
        prompts = {p.name for p in (await client.list_prompts()).prompts}

    assert set(tools) == {"list_expenses", "get_summary", "add_expense", "delete_expense"}
    assert tools["get_summary"].annotations.read_only_hint is True
    assert tools["delete_expense"].annotations.destructive_hint is True
    assert resources == {"expenses://categories", "expenses://summary/current-month"}
    assert prompts == {"monthly_review"}


async def test_list_expenses_forwards_filters(server, backend: FakeBackend) -> None:
    async with Client(server) as client:
        result = await client.call_tool(
            "list_expenses", {"from_date": "2026-10-01", "to_date": "2026-10-31", "category": "FUEL"}
        )

    assert not result.is_error
    params = backend.requests[-1].url.params
    assert (params["from"], params["to"], params["category"]) == ("2026-10-01", "2026-10-31", "FUEL")
    assert "Benzina" in result.content[0].text


async def test_get_summary_without_dates_sends_no_params(server, backend: FakeBackend) -> None:
    async with Client(server) as client:
        result = await client.call_tool("get_summary", {})

    assert not result.is_error
    assert dict(backend.requests[-1].url.params) == {}
    assert result.structured_content["total"] == 30.0


async def test_add_expense_normalizes_payload_and_defaults_date(server, backend: FakeBackend) -> None:
    async with Client(server) as client:
        result = await client.call_tool(
            "add_expense", {"description": " Pizza ", "amount": 12.499, "category": "restaurants"}
        )

    assert not result.is_error
    sent = json.loads(backend.requests[-1].content)
    assert sent["description"] == "Pizza"
    assert sent["amount"] == 12.5
    assert sent["category"] == "RESTAURANTS"
    assert sent["currency"] == "EUR"
    assert len(sent["date"]) == 10  # data di oggi, YYYY-MM-DD


async def test_add_expense_rejects_non_positive_amount_before_calling_backend(server, backend) -> None:
    async with Client(server) as client:
        result = await client.call_tool("add_expense", {"description": "X", "amount": 0, "category": "OTHER"})

    assert result.is_error
    assert backend.requests == []


async def test_backend_errors_become_readable_tool_errors(server) -> None:
    async with Client(server) as client:
        ok = await client.call_tool("delete_expense", {"expense_id": 1})
        missing = await client.call_tool("delete_expense", {"expense_id": 99})

    assert not ok.is_error
    assert missing.is_error
    assert "Spesa 99 non trovata" in missing.content[0].text


async def test_unreachable_backend_is_reported() -> None:
    def refuse(request: httpx.Request) -> httpx.Response:
        raise httpx.ConnectError("connection refused", request=request)

    server = create_server(ExpenseApi("http://backend.test", transport=httpx.MockTransport(refuse)))
    async with Client(server) as client:
        result = await client.call_tool("get_summary", {})

    assert result.is_error
    assert "non raggiungibile" in result.content[0].text


async def test_categories_resource(server) -> None:
    async with Client(server) as client:
        result = await client.read_resource("expenses://categories")

    data: list[dict[str, Any]] = json.loads(result.contents[0].text)
    assert data == [{"code": "FUEL", "label": "Carburante"}]


async def test_monthly_review_prompt_mentions_the_month(server) -> None:
    async with Client(server) as client:
        result = await client.get_prompt("monthly_review", {"month": "2026-10"})

    assert "2026-10" in result.messages[0].content.text
