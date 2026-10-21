# Frontend

Web app Angular 21 (componenti standalone, signals, zoneless) con l'interfaccia in
italiano, pensata come un'app bancaria moderna: prima per il telefono, poi per il desktop.

## Pagine

| Percorso       | Pagina       | Cosa fa                                                       |
|----------------|--------------|---------------------------------------------------------------|
| `/`            | Panoramica   | Card con il totale del mese (contatore animato), confronto con lo stesso periodo del mese scorso, ripartizione per categoria, ultimi movimenti |
| `/movimenti`   | Movimenti    | Spese del mese per giorno, filtri a chip per categoria, swipe per eliminare con "Annulla" |
| `/statistiche` | Statistiche  | Ciambella animata per categoria e classifica con barre proporzionali |
| `/assistente`  | Assistente   | Chat sulle spese, con i dati consultati da Claude sotto ogni risposta |

L'inserimento è un **bottom sheet** apribile da ogni pagina (il "+" nella tab bar, o
"Nuova spesa" nella barra laterale su desktop): scrivi la frase, Claude la interpreta e
la bozza appare come uno **scontrino** da confermare o correggere.

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

## Override delle dipendenze

`package.json` contiene degli `overrides` che forzano `vite` e `@modelcontextprotocol/sdk`
a versioni senza vulnerabilità note: sono dipendenze indirette degli strumenti di Angular 21,
che le fissa a versioni vulnerabili. Vanno rimossi quando una patch di Angular 21 includerà
versioni corrette (verifica con `npm audit` dopo averli tolti).

## Struttura

```
src/app/
├── core/          modelli, client dell'API, date, stile delle categorie, stato condiviso (UiState, Toasts)
├── shared/        bottom sheet, scontrino, riga con swipe, icona categoria, contatore, toast, selettore del mese
└── features/      una cartella per pagina, caricata in lazy loading
```

## Design e animazioni

- **Tema scuro di default** (notte blu a strati) e tema chiaro automatico; un solo
  gradiente, sulla card del saldo. Lo scontrino resta di carta in entrambi i temi.
- **Navigazione da app**: tab bar in basso con il "+" centrale su mobile, barra laterale
  su desktop; rispetta le *safe area* dei telefoni con notch.
- **Movimento**: transizioni tra pagine con la View Transitions API, sheet che sale con
  una curva a molla e si chiude trascinando, contatore del totale, barre e ciambella che
  crescono, ingresso sfalsato delle liste, spunta disegnata al salvataggio, puntini di
  attesa in chat, feedback alla pressione. Tutto disattivato con `prefers-reduced-motion`.
- **Prestazioni**: si animano solo `transform`, `opacity` e proprietà SVG (niente reflow);
  pagine in lazy loading; il contatore aggiorna il testo a ogni frame senza passare dal
  change detection; icone importate una per una (tree shaking).
- **Accessibilità**: `<dialog>` nativo per il pannello (focus trap ed Esc), etichette su
  tutti i controlli, `aria-live` per chat e notifiche, focus visibile, eliminazione anche
  da tastiera e mouse oltre che con lo swipe.
