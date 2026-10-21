"""Client HTTP per l'API REST del backend.

Il server MCP non accede mai al database: ogni operazione passa dal backend, che resta
l'unica fonte di verità e applica le sue regole di validazione.
"""

from __future__ import annotations

from typing import Any

import httpx


class ExpenseApiError(Exception):
    """Il backend ha risposto con un errore, o non è raggiungibile."""


class ExpenseApi:
    """Wrapper minimale e tipizzato attorno agli endpoint `/api/...` del backend."""

    def __init__(
        self, base_url: str, *, transport: httpx.AsyncBaseTransport | None = None, timeout: float = 15.0
    ) -> None:
        # `transport` permette ai test di usare httpx.MockTransport al posto della rete.
        self._client = httpx.AsyncClient(base_url=base_url.rstrip("/"), transport=transport, timeout=timeout)

    async def aclose(self) -> None:
        await self._client.aclose()

    async def list_expenses(
        self, from_date: str | None, to_date: str | None, category: str | None
    ) -> list[dict[str, Any]]:
        params = {k: v for k, v in {"from": from_date, "to": to_date, "category": category}.items() if v}
        return await self._request("GET", "/api/expenses", params=params)

    async def summary(self, from_date: str | None, to_date: str | None) -> dict[str, Any]:
        params = {k: v for k, v in {"from": from_date, "to": to_date}.items() if v}
        return await self._request("GET", "/api/expenses/summary", params=params)

    async def create_expense(self, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request("POST", "/api/expenses", json=payload)

    async def delete_expense(self, expense_id: int) -> None:
        await self._request("DELETE", f"/api/expenses/{expense_id}")

    async def categories(self) -> list[dict[str, Any]]:
        return await self._request("GET", "/api/categories")

    async def _request(self, method: str, path: str, **kwargs: Any) -> Any:
        try:
            response = await self._client.request(method, path, **kwargs)
        except httpx.TransportError as e:
            raise ExpenseApiError(f"Backend non raggiungibile su {self._client.base_url}: è avviato?") from e

        if response.is_error:
            raise ExpenseApiError(_error_message(response))
        if response.status_code == 204 or not response.content:
            return None
        return response.json()


def _error_message(response: httpx.Response) -> str:
    """Estrae un messaggio leggibile da una risposta di errore (RFC 9457 problem details)."""
    try:
        problem = response.json()
    except ValueError:
        return f"Il backend ha risposto con HTTP {response.status_code}"
    detail = problem.get("detail") or problem.get("title") or "errore sconosciuto"
    return f"HTTP {response.status_code}: {detail}"
