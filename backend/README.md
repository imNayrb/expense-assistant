# Backend

API REST Spring Boot 3 (Java 21). Salva le spese e integra Claude per l'inserimento
in linguaggio naturale e le domande in forma di chat.

## Avvio

```bash
export ANTHROPIC_API_KEY=sk-ant-...   # facoltativa: senza, funziona solo il CRUD
mvn spring-boot:run
```

L'API risponde su `http://localhost:8080`. Per impostazione predefinita i dati
stanno in un file H2 locale (`./data/`). Per usare PostgreSQL:

```bash
SPRING_PROFILES_ACTIVE=postgres DB_HOST=localhost DB_USER=expenses DB_PASSWORD=expenses mvn spring-boot:run
```

## Test

```bash
mvn verify
```

I test non chiamano mai l'API di Claude: l'interfaccia `LlmClient` viene sostituita
da fake e il contratto HTTP viene verificato con `MockRestServiceServer`.

## Configurazione

| Variabile              | Default                  | A cosa serve                               |
|------------------------|--------------------------|--------------------------------------------|
| `ANTHROPIC_API_KEY`    | *(vuota)*                | Abilita gli endpoint dell'assistente       |
| `ANTHROPIC_MODEL`      | `claude-haiku-5-5`       | Modello usato per parsing e chat           |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200`  | Origini consentite dal browser, separate da virgola |
| `SPRING_PROFILES_ACTIVE` | *(nessuno)*            | `postgres` per usare PostgreSQL            |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | vedi `application-postgres.yml` | Connessione a PostgreSQL |

## API

| Metodo | Percorso                    | Descrizione                                            |
|--------|-----------------------------|--------------------------------------------------------|
| GET    | `/api/expenses`             | Elenco, filtri: `from`, `to` (date ISO), `category`. Default: mese corrente |
| GET    | `/api/expenses/summary`     | Totale e totali per categoria tra `from` e `to`        |
| GET    | `/api/expenses/{id}`        | Una spesa                                              |
| POST   | `/api/expenses`             | Crea                                                   |
| PUT    | `/api/expenses/{id}`        | Aggiorna                                               |
| DELETE | `/api/expenses/{id}`        | Elimina                                                |
| GET    | `/api/categories`           | Codici delle categorie con etichetta in italiano       |
| GET    | `/api/assistant/status`     | Se l'assistente è attivo e quale modello usa           |
| POST   | `/api/assistant/parse`      | `{"text": "ieri 30€ di benzina"}` → bozza di spesa (non salvata) |
| POST   | `/api/assistant/chat`       | `{"messages": [{"role":"user","content":"..."}]}` → risposta |

Gli errori seguono la RFC 9457 (`application/problem+json`): 400 validazione,
404 non trovata, 422 testo non riconosciuto come spesa, 502 errore dell'API di
Claude, 503 assistente disattivato.

## Esempio

```bash
curl -s localhost:8080/api/assistant/parse -H 'Content-Type: application/json' \
  -d '{"text":"ieri 30€ di benzina"}'
# {"description":"Benzina","amount":30.00,"currency":"EUR","category":"FUEL",
#  "categoryLabel":"Carburante","date":"2026-10-20"}
```

## Struttura dei package

```
com.imnayrb.expenseassistant
├── config/        proprietà e bean (Clock, LlmClient, CORS)
├── expense/       entity, repository, service, controller REST, DTO
├── assistant/     ExpenseParser (tool use forzato), ChatAssistant (loop agentico), ExpenseTools
│   └── llm/       porta LlmClient + client della Messages API e modelli del protocollo
└── web/           gestione degli errori (problem details)
```
