import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/** Guscio dell'app: intestazione con la navigazione e area delle pagine. */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="bar">
      <a class="brand" routerLink="/">Spese</a>
      <nav aria-label="Sezioni">
        @for (link of links; track link.path) {
          <a
            [routerLink]="link.path"
            routerLinkActive="active"
            [routerLinkActiveOptions]="{ exact: true }"
            ariaCurrentWhenActive="page"
            >{{ link.label }}</a
          >
        }
      </nav>
    </header>
    <main>
      <router-outlet />
    </main>
  `,
  styles: `
    .bar {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
      max-width: var(--page-width);
      margin: 0 auto;
      padding: 1.25rem var(--gutter) 0.5rem;
    }
    .brand {
      font-size: 1.5rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: var(--ink);
      text-decoration: none;
    }
    nav {
      display: flex;
      gap: 0.25rem;
      flex-wrap: wrap;
    }
    nav a {
      padding: 0.35rem 0.7rem;
      border-radius: 999px;
      color: var(--muted);
      text-decoration: none;
      font-weight: 500;
    }
    nav a:hover {
      color: var(--ink);
    }
    nav a.active {
      background: var(--ink);
      color: var(--paper);
    }
    @media (max-width: 30rem) {
      nav {
        width: 100%;
        justify-content: space-between;
        flex-wrap: nowrap;
      }
      nav a {
        padding: 0.35rem 0.55rem;
      }
    }
    main {
      max-width: var(--page-width);
      margin: 0 auto;
      padding: 1rem var(--gutter) 4rem;
    }
  `,
})
export class App {
  protected readonly links = [
    { path: '/', label: 'Aggiungi' },
    { path: '/spese', label: 'Spese' },
    { path: '/riepilogo', label: 'Riepilogo' },
    { path: '/chiedi', label: 'Chiedi' },
  ];
}
