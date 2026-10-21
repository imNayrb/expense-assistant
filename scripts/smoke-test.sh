#!/usr/bin/env bash
# Test end-to-end dello stack avviato con Docker Compose.
#
# Verifica che i pezzi funzionino davvero insieme: PostgreSQL con le migrazioni Flyway,
# backend con il profilo "postgres", nginx che serve l'app e inoltra /api, routing lato
# client. Non usa l'API di Claude: controlla che senza chiave l'assistente risulti spento.
#
# Uso:  docker compose up -d --build && ./scripts/smoke-test.sh

set -euo pipefail

APP_URL="${APP_URL:-http://localhost:4200}"
API_URL="${API_URL:-http://localhost:8080}"
TIMEOUT_S="${TIMEOUT_S:-180}"

fail() {
  echo "ERRORE: $*" >&2
  exit 1
}

# Esegue una richiesta e controlla lo stato HTTP; il corpo finisce in $BODY.
request() {
  local method=$1 url=$2 expected=$3 data=${4:-}
  local args=(-sS -o /tmp/smoke-body -w '%{http_code}' -X "$method" "$url")
  if [[ -n "$data" ]]; then
    args+=(-H 'Content-Type: application/json' -d "$data")
  fi
  local status
  status=$(curl "${args[@]}")
  BODY=$(cat /tmp/smoke-body)
  [[ "$status" == "$expected" ]] || fail "$method $url: atteso HTTP $expected, ricevuto $status. Corpo: $BODY"
  echo "  ok  $method $url -> $status"
}

# Estrae un campo dal JSON in $BODY (es. json '["total"]').
json() {
  python3 -c "import json,sys; d=json.load(sys.stdin); print(eval('d' + sys.argv[1]))" "$1" <<<"$BODY"
}

echo "Attendo il backend (max ${TIMEOUT_S}s)..."
deadline=$((SECONDS + TIMEOUT_S))
until curl -fsS "$API_URL/actuator/health" 2>/dev/null | grep -q '"UP"'; do
  ((SECONDS < deadline)) || fail "il backend non è diventato pronto in ${TIMEOUT_S}s"
  sleep 3
done
echo "Backend pronto."

echo "Frontend servito da nginx:"
request GET "$APP_URL/" 200
grep -q '<app-root' <<<"$BODY" || fail "la pagina principale non contiene l'app Angular"
# Un link diretto a una pagina interna deve restituire comunque l'app (routing lato client).
request GET "$APP_URL/movimenti" 200
grep -q '<app-root' <<<"$BODY" || fail "il routing lato client non è configurato in nginx"

echo "API attraverso il proxy di nginx:"
request GET "$APP_URL/api/categories" 200
[[ $(json '[0]["code"]') == "GROCERIES" ]] || fail "categorie inattese: $BODY"

request POST "$APP_URL/api/expenses" 201 \
  '{"description":"Benzina","amount":30.5,"category":"FUEL","date":"2026-10-20"}'
id=$(json '["id"]')
[[ $(json '["currency"]') == "EUR" ]] || fail "la valuta predefinita non è EUR: $BODY"

request POST "$APP_URL/api/expenses" 201 \
  '{"description":"Spesa","amount":12.25,"category":"GROCERIES","date":"2026-10-21"}'

request GET "$APP_URL/api/expenses?from=2026-10-01&to=2026-10-31" 200
[[ $(json '[0]["description"]') == "Spesa" ]] || fail "ordinamento per data errato: $BODY"

request GET "$APP_URL/api/expenses/summary?from=2026-10-01&to=2026-10-31" 200
[[ $(json '["total"]') == "42.75" ]] || fail "totale errato: $BODY"
[[ $(json '["categories"][0]["category"]') == "FUEL" ]] || fail "aggregazione errata: $BODY"

request POST "$APP_URL/api/expenses" 400 '{"description":"","amount":-1,"category":"FUEL","date":"2026-10-20"}'

request DELETE "$APP_URL/api/expenses/$id" 204
request GET "$APP_URL/api/expenses/$id" 404

echo "Assistente senza chiave API:"
request GET "$APP_URL/api/assistant/status" 200
[[ $(json '["enabled"]') == "False" ]] || fail "l'assistente risulta attivo senza chiave: $BODY"
request POST "$APP_URL/api/assistant/parse" 503 '{"text":"ieri 30 euro di benzina"}'

echo "Tutti i controlli sono passati."
