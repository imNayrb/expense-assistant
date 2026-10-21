import { Injectable, signal } from '@angular/core';

/**
 * Stato condiviso dell'interfaccia:
 * - `version` cambia a ogni modifica delle spese, così le pagine sanno quando ricaricare;
 * - `sheetOpen` apre il pannello di inserimento da qualsiasi punto (tab bar, home, pagine vuote).
 */
@Injectable({ providedIn: 'root' })
export class UiState {
  readonly version = signal(0);
  readonly sheetOpen = signal(false);

  expensesChanged(): void {
    this.version.update((v) => v + 1);
  }

  openSheet(): void {
    this.sheetOpen.set(true);
  }

  closeSheet(): void {
    this.sheetOpen.set(false);
  }
}

export interface Toast {
  id: number;
  text: string;
  action?: { label: string; run: () => void };
}

/** Notifiche brevi in basso, con un'azione facoltativa (es. "Annulla"). */
@Injectable({ providedIn: 'root' })
export class Toasts {
  readonly items = signal<Toast[]>([]);
  private nextId = 1;

  show(text: string, action?: Toast['action'], durationMs = 4500): void {
    const toast: Toast = { id: this.nextId++, text, action };
    this.items.update((list) => [...list.slice(-2), toast]);
    setTimeout(() => this.dismiss(toast.id), durationMs);
  }

  dismiss(id: number): void {
    this.items.update((list) => list.filter((t) => t.id !== id));
  }
}
