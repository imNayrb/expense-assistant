# Changelog

Tutte le modifiche rilevanti del progetto sono documentate in questo file, dalla più recente.

Il formato si basa su [Keep a Changelog](https://keepachangelog.com/it-IT/1.1.0/)
e il progetto segue il [Versionamento Semantico](https://semver.org/lang/it/).

## [1.0.4] - 2026-10-22

### Modificato
- nginx del frontend aggiornato da 1.27 a 1.31 (Dependabot, PR #16).
- GitHub Actions aggiornate all'ultima versione maggiore: `checkout` 7, `setup-java` 6,
  `setup-python` 7, `setup-node` 7, `dependency-review-action` 5 (Dependabot, PR #17).
- Dependabot propone solo aggiornamenti minori e patch: le versioni maggiori (Spring Boot 4,
  Angular 22, TypeScript 7…) richiedono migrazioni e si fanno a mano. Le immagini Docker
  restano su Java 21 e Node 24 LTS.

## [1.0.3] - 2026-10-22

### Aggiunto
- `SECURITY.md` con la politica di sicurezza, la segnalazione privata delle vulnerabilità
  e il modello di sicurezza del progetto.
- `CONTRIBUTING.md` con le regole per proporre modifiche tramite fork e pull request.
- `.github/CODEOWNERS`: il maintainer è revisore di tutte le modifiche.
- Dependabot per le dipendenze di backend, server MCP, frontend, immagini Docker e
  GitHub Actions, con aggiornamenti raggruppati ogni settimana.
- Workflow "Sicurezza": analisi CodeQL di Java, Python, TypeScript e workflow, e revisione
  delle dipendenze nelle pull request (attivi quando il repository è pubblico).
- Modelli per le issue (errore e proposta), con le issue vuote disattivate.

### Modificato
- CI: permessi minimi documentati, token non salvato nel checkout, timeout su ogni job.

## [1.0.2] - 2026-10-22

### Modificato
- README riscritto come pagina di presentazione del progetto: banner, GIF animata del
  flusso principale, funzionalità, motivazioni e attestati Anthropic Academy collegati al
  codice in cui sono applicati, diagrammi di architettura, stack, avvio rapido, test,
  struttura e prossimi passi.
- Immagini spostate in `docs/media/`.

## [1.0.1] - 2026-10-22

### Aggiunto
- Test end-to-end con Docker Compose (`scripts/smoke-test.sh`): PostgreSQL con le
  migrazioni Flyway, API attraverso nginx, routing lato client, assistente spento senza
  chiave.
- Test end-to-end del server MCP avviato via stdio contro il backend reale
  (`mcp-server/tests/test_e2e.py`).

### Modificato
- CI: il job end-to-end sostituisce la sola build delle immagini Docker.

## [1.0.0] - 2026-10-21

### Modificato
- **Interfaccia ridisegnata** in stile app fintech:
  - tema scuro di default con tema chiaro automatico; font Manrope con cifre tabulari;
  - tab bar mobile con "+" centrale e barra laterale su desktop;
  - nuove pagine Panoramica, Movimenti, Statistiche e Assistente (percorsi `/`,
    `/movimenti`, `/statistiche`, `/assistente`);
  - inserimento in un bottom sheet raggiungibile da ogni pagina, con chiusura trascinando;
  - icone e colori per ogni categoria; importo dello scontrino in formato italiano.
- La pagina "Aggiungi" è sostituita dal bottom sheet.

### Aggiunto
- Panoramica con totale del mese animato, confronto con lo stesso periodo del mese
  precedente e barra segmentata per categoria.
- Statistiche con grafico a ciambella animato e classifica delle categorie.
- Eliminazione con swipe e notifica con "Annulla"; skeleton durante il caricamento.
- Animazioni: View Transitions tra le pagine, contatori, barre, spunta di conferma,
  indicatore di scrittura in chat; tutte disattivate con `prefers-reduced-motion`.
- Screenshot nel README.
- Test per bottom sheet, eliminazione annullabile, importi e tab bar (20 test).

## [0.5.0] - 2026-10-21

### Aggiunto
- `docker-compose.yml` con PostgreSQL, backend e frontend; `Dockerfile` multi-stage per
  backend (JRE, utente non root) e frontend (nginx con proxy verso `/api`).
- `.env.example` con le variabili d'ambiente.
- `docs/ARCHITETTURA.md`: scelte di progetto, flussi, test, limiti noti e domande tipiche
  da colloquio.
- Job di CI che costruisce le immagini Docker.

### Modificato
- README riscritto con funzionalità, architettura, avvio con Docker e sviluppo locale.

## [0.4.0] - 2026-10-21

### Aggiunto
- **Frontend** (Angular 21, componenti standalone, signals, zoneless), in italiano:
  - pagina **Aggiungi**: la spesa scritta a parole viene interpretata da Claude e
    mostrata come uno scontrino modificabile da confermare; inserimento manuale quando
    l'assistente non è attivo;
  - pagina **Spese**: spese del mese raggruppate per giorno, filtro per categoria,
    eliminazione con conferma;
  - pagina **Riepilogo**: totale del mese e barre proporzionali per categoria;
  - pagina **Chiedi**: chat sulle spese con indicazione dei dati consultati da Claude.
  - Selettore del mese, formati italiani per date e importi, tema chiaro e scuro,
    proxy di sviluppo verso il backend.
  - Test con Vitest (utility delle date, client dell'API, flusso di inserimento).
- Job di CI per il frontend (Prettier, test, build di produzione).

## [0.3.0] - 2026-10-21

### Aggiunto
- **Server MCP** (Python, SDK MCP 2.x) che espone le spese ai client MCP
  (Claude Desktop, Claude Code):
  - tool `list_expenses`, `get_summary`, `add_expense`, `delete_expense`, con annotations
    MCP di sola lettura e distruttive;
  - resource `expenses://categories` ed `expenses://summary/current-month`;
  - prompt `monthly_review` per l'analisi guidata di un mese.
  - Il server chiama l'API REST del backend e converte gli errori in messaggi leggibili
    dal modello; trasporto stdio.
  - Test con backend finto (`httpx.MockTransport`) e client MCP in-process.
- Job di CI per il server MCP (ruff e pytest).

## [0.2.0] - 2026-10-21

### Aggiunto
- **Backend** (Spring Boot 3.5, Java 21):
  - API REST per il CRUD delle spese, con validazione e filtri per date e categoria.
  - Endpoint di riepilogo con i totali per categoria, aggregati nel database.
  - `/api/categories` con le etichette in italiano.
  - Persistenza con Spring Data JPA; schema gestito da Flyway; H2 di default,
    PostgreSQL con il profilo `postgres`.
  - Parsing del linguaggio naturale (`POST /api/assistant/parse`) con Claude e il tool
    `record_expense` forzato: restituisce una bozza da confermare prima del salvataggio.
  - Domande in forma di chat (`POST /api/assistant/chat`) con un loop agentico di tool use
    su `list_expenses`, `get_summary`, `add_expense`, limitato a 5 giri.
  - Porta `LlmClient` con un client HTTP per la Messages API di Claude.
  - Errori in formato RFC 9457 (problem details); configurazione CORS per il frontend.
  - Test di integrazione e unitari; i test non chiamano mai l'API di Claude.
- Workflow di CI (GitHub Actions) che compila e testa il backend.

## [0.1.0] - 2026-10-21

### Aggiunto
- `CLAUDE.md` con panoramica del progetto, struttura prevista (`backend/`, `mcp-server/`,
  `frontend/`, `docs/`), comandi, convenzioni e regole di architettura.
- `CHANGELOG.md` per tenere la cronostoria delle modifiche.
- Impostazioni condivise per editor e git (`.editorconfig`, `.gitattributes`, `.gitignore`).
- Template per le pull request e licenza MIT.
