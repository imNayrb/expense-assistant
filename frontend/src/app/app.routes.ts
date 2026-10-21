import { Routes } from '@angular/router';

// Ogni pagina è caricata solo quando serve (lazy loading).
export const routes: Routes = [
  {
    path: '',
    title: 'Panoramica · Spese',
    loadComponent: () => import('./features/overview/overview').then((m) => m.Overview),
  },
  {
    path: 'movimenti',
    title: 'Movimenti · Spese',
    loadComponent: () => import('./features/movements/movements').then((m) => m.Movements),
  },
  {
    path: 'statistiche',
    title: 'Statistiche · Spese',
    loadComponent: () => import('./features/statistics/statistics').then((m) => m.Statistics),
  },
  {
    path: 'assistente',
    title: 'Assistente · Spese',
    loadComponent: () => import('./features/assistant/assistant').then((m) => m.Assistant),
  },
  { path: '**', redirectTo: '' },
];
