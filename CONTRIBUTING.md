# Contribuire

Grazie dell'interesse. Expense Assistant è un progetto personale, ma suggerimenti e pull
request sono benvenuti.

## Come funziona

- Solo il maintainer può modificare direttamente il repository. Chiunque altro propone
  modifiche con un **fork** e una **pull request**, che il maintainer valuta e decide se
  unire.
- Ogni pull request deve passare la CI (test di backend, server MCP, frontend ed
  end-to-end) e richiede la revisione del maintainer (vedi `.github/CODEOWNERS`).
- Per i problemi di sicurezza **non aprire issue pubbliche**: segui [SECURITY.md](SECURITY.md).

## Prima di iniziare

Per modifiche non banali apri prima una issue per discuterne: eviti di lavorare su qualcosa
che non verrebbe unito.

## Passaggi

1. Fai un fork del repository e crea un branch dal `main` aggiornato:
   `feat/<argomento>`, `fix/<argomento>`, `docs/<argomento>` o `chore/<argomento>`,
   con l'argomento in italiano (es. `fix/riepilogo-mese-vuoto`).
2. Avvia il progetto come indicato nel [README](README.md#avvio-rapido).
3. Fai la modifica seguendo le convenzioni di [CLAUDE.md](CLAUDE.md):
   - commenti, documentazione e testi dell'interfaccia in italiano, identificatori in inglese;
   - importi come `BigDecimal` nel backend;
   - nessuna chiamata all'API di Claude nei test.
4. Aggiungi o aggiorna i test, poi verifica in locale:

   ```bash
   cd backend && mvn verify
   cd mcp-server && pytest && ruff check . && ruff format --check .
   cd frontend && npm test && npm run format:check
   ```

5. Aggiungi una voce in [CHANGELOG.md](CHANGELOG.md).
6. Scrivi i commit con [Conventional Commits](https://www.conventionalcommits.org/it/), in
   italiano e all'imperativo: `fix(frontend): correggi il totale del mese vuoto`.
7. Apri la pull request compilando il modello proposto.

## Cosa non viene accettato

- Chiavi API, file `.env` o altri segreti nel codice o nella cronologia.
- Modifiche che fanno accedere frontend o server MCP direttamente al database: il backend
  resta l'unica fonte di verità.
- Pull request che mescolano più modifiche indipendenti: meglio più PR piccole.
