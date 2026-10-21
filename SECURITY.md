# Politica di sicurezza

## Versioni supportate

Le correzioni di sicurezza vengono applicate solo all'ultima versione sul branch `main`.

| Versione | Supportata |
|----------|------------|
| `main` (ultima release in [CHANGELOG](CHANGELOG.md)) | ✅ |
| Versioni precedenti | ❌ |

## Segnalare una vulnerabilità

**Non aprire una issue pubblica** per segnalare un problema di sicurezza.

Usa invece la segnalazione privata di GitHub: scheda **Security** del repository →
**Report a vulnerability**. La segnalazione resta visibile solo a te e al maintainer.

Nella segnalazione indica, se possibile:

- il componente coinvolto (`backend`, `mcp-server`, `frontend`, configurazione Docker);
- i passaggi per riprodurre il problema;
- l'impatto che hai osservato o che ti aspetti.

Di norma riceverai una prima risposta entro una settimana. Se la segnalazione è confermata, la
correzione sarà rilasciata il prima possibile e citata nel changelog, con il tuo nome se lo
desideri.

## Modello di sicurezza del progetto

Expense Assistant è pensato per un **uso personale in locale** (`docker compose up`). Alcune
scelte derivano da questo e non sono vulnerabilità:

- **non c'è autenticazione**: chi raggiunge l'API può leggere e modificare le spese. Non
  esporre backend o frontend su Internet senza aggiungere un livello di autenticazione;
- la password di PostgreSQL di default (`expenses`) è pensata per lo sviluppo: in qualsiasi
  altro contesto impostala con `DB_PASSWORD` nel file `.env`.

Sono invece considerati problemi di sicurezza, da segnalare come sopra:

- esposizione della chiave `ANTHROPIC_API_KEY` (nei log, nelle risposte, nel frontend);
- iniezioni (SQL, prompt injection che porta a eseguire operazioni non richieste, XSS);
- vulnerabilità nelle dipendenze non ancora segnalate da Dependabot.

## Buone pratiche adottate

- La chiave API arriva solo da variabili d'ambiente; `.env` è escluso da git e la chiave
  non viene mai registrata nei log né inviata al browser.
- Claude non accede mai al database: i tool che può usare passano dalla stessa validazione
  dell'API REST, con un limite di 5 giri per conversazione.
- Il container del backend gira con un utente non privilegiato.
- I workflow di CI hanno permessi in sola lettura.
- Dependabot tiene aggiornate le dipendenze; CodeQL analizza il codice a ogni pull request.
