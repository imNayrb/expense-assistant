import { Routes } from '@angular/router';

// Ogni pagina è caricata solo quando serve (lazy loading).
export const routes: Routes = [
  {
    path: '',
    title: 'Aggiungi una spesa',
    loadComponent: () => import('./features/add/add-expense').then((m) => m.AddExpense),
  },
  {
    path: 'spese',
    title: 'Spese',
    loadComponent: () => import('./features/list/expense-list').then((m) => m.ExpenseList),
  },
  {
    path: 'riepilogo',
    title: 'Riepilogo',
    loadComponent: () => import('./features/summary/summary').then((m) => m.Summary),
  },
  {
    path: 'chiedi',
    title: 'Chiedi',
    loadComponent: () => import('./features/chat/chat').then((m) => m.Chat),
  },
  { path: '**', redirectTo: '' },
];
