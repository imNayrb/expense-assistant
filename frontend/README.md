# Frontend

Web app Angular 21 (componenti standalone, signals, zoneless) con l'interfaccia in
italiano.

## Pagine

| Percorso     | Pagina     | Cosa fa                                                         |
|--------------|------------|-----------------------------------------------------------------|
| `/`          | Aggiungi   | Scrivi la spesa a parole; Claude la interpreta in uno scontrino da confermare o correggere. Senza assistente resta l'inserimento manuale |
| `/spese`     | Spese      | Spese del mese raggruppate per giorno, filtro per categoria, eliminazione |
| `/riepilogo` | Riepilogo  | Totale del mese e barre proporzionali per categoria             |
| `/chiedi`    | Chiedi     | Domande libere sulle spese; mostra quali dati ha consultato Claude |

## Avvio

Con il backend avviato su `localhost:8080`:

```bash
npm ci
npm start          # http://localhost:4200
```

Le chiamate partono verso `/api/...` e il dev server le inoltra al backend
(`proxy.conf.json`): non servono URL assoluti né configurazioni CORS.

## Test e qualità

```bash
npm test               # Vitest + jsdom
npm run build
npm run format:check   # Prettier
```

## Struttura

```
src/app/
├── core/          modelli, client dell'API, utility per le date
├── shared/        componenti riusabili: scontrino (Receipt), selettore del mese
└── features/      una cartella per pagina, caricata in lazy loading
```

## Note di design

- Palette fredda "inchiostro e carta" con un unico accento verde; tema scuro automatico.
- L'elemento distintivo è lo **scontrino**: la bozza di Claude appare come uno scontrino
  stampato (bordo seghettato, importo in monospace), con una sola animazione di "stampa"
  che rispetta `prefers-reduced-motion`.
- Focus visibile, etichette accessibili, layout a colonna singola pensato per il telefono.
