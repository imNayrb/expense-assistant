import { CurrencyPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { Month, currentMonth, monthLabel, monthRange } from '../../core/dates';
import { ExpenseSummary } from '../../core/models';
import { MonthPicker } from '../../shared/month-picker/month-picker';

/** Totale del mese e ripartizione per categoria, con barre proporzionali. */
@Component({
  selector: 'app-summary',
  imports: [CurrencyPipe, RouterLink, MonthPicker],
  template: `
    <h1>Riepilogo</h1>
    <app-month-picker [(month)]="month" />

    @if (error()) {
      <p class="notice error" role="alert">{{ error() }}</p>
    }

    @if (summary(); as s) {
      @if (s.count === 0) {
        <p class="muted empty">
          Nessuna spesa a {{ monthName() }}. <a routerLink="/">Aggiungine una</a>
        </p>
      } @else {
        <p class="total">
          <span class="amount">{{ s.total | currency: 'EUR' }}</span>
          <span class="muted"
            >{{ s.count === 1 ? '1 spesa' : s.count + ' spese' }} a {{ monthName() }}</span
          >
        </p>
        <ul class="bars">
          @for (c of s.categories; track c.category) {
            <li>
              <div class="line">
                <span>{{ c.categoryLabel }}</span>
                <span class="value">{{ c.total | currency: 'EUR' }}</span>
              </div>
              <div class="track" aria-hidden="true">
                <div class="fill" [style.width.%]="share(c.total)"></div>
              </div>
            </li>
          }
        </ul>
      }
    } @else if (!error()) {
      <p class="muted">Calcolo il riepilogo…</p>
    }
  `,
  styles: `
    h1 {
      margin: 1.5rem 0 1rem;
    }
    .total {
      display: flex;
      flex-direction: column;
      margin: 1.5rem 0 2rem;
    }
    .total .amount {
      font-size: clamp(2.5rem, 2rem + 3vw, 3.5rem);
      font-weight: 700;
      letter-spacing: -0.03em;
      line-height: 1;
      font-variant-numeric: tabular-nums;
    }
    .empty {
      margin-top: 1.5rem;
    }
    .bars {
      list-style: none;
      padding: 0;
      margin: 0;
      display: grid;
      gap: 1rem;
    }
    .line {
      display: flex;
      justify-content: space-between;
      margin-bottom: 0.3rem;
    }
    .value {
      font-weight: 600;
      font-variant-numeric: tabular-nums;
    }
    .track {
      height: 0.6rem;
      background: var(--line);
      border-radius: 999px;
      overflow: hidden;
    }
    .fill {
      height: 100%;
      background: var(--accent);
      border-radius: 999px;
    }
  `,
})
export class Summary {
  private readonly api = inject(ExpenseApi);

  protected readonly month = signal<Month>(currentMonth());
  protected readonly summary = signal<ExpenseSummary | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly monthName = computed(() => monthLabel(this.month()));

  /** La categoria più alta occupa tutta la barra: le altre sono in proporzione. */
  private readonly max = computed(() =>
    Math.max(0, ...(this.summary()?.categories ?? []).map((c) => c.total)),
  );

  constructor() {
    effect(() => {
      const range = monthRange(this.month());
      this.summary.set(null);
      this.error.set(null);
      this.api.summary(range).subscribe({
        next: (s) => this.summary.set(s),
        error: (e) => this.error.set(errorMessage(e)),
      });
    });
  }

  protected share(total: number): number {
    const max = this.max();
    return max > 0 ? (total / max) * 100 : 0;
  }
}
