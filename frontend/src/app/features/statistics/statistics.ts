import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { categoryStyle } from '../../core/categories';
import { Month, currentMonth, monthLabel, monthRange } from '../../core/dates';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { ExpenseSummary } from '../../core/models';
import { UiState } from '../../core/ui-state';
import { CategoryIcon } from '../../shared/category-icon/category-icon';
import { CountUp } from '../../shared/count-up/count-up';
import { MonthPicker } from '../../shared/month-picker/month-picker';

interface Slice {
  code: string;
  label: string;
  total: number;
  count: number;
  share: number;
  /** Punto di partenza dell'arco, in percentuale della circonferenza. */
  offset: number;
  color: string;
}

/** Dove vanno i soldi: ciambella per categoria e classifica con barre proporzionali. */
@Component({
  selector: 'app-statistics',
  imports: [CurrencyPipe, DecimalPipe, CategoryIcon, CountUp, MonthPicker],
  templateUrl: './statistics.html',
  styleUrl: './statistics.css',
})
export class Statistics {
  private readonly api = inject(ExpenseApi);
  protected readonly ui = inject(UiState);

  protected readonly month = signal<Month>(currentMonth());
  protected readonly summary = signal<ExpenseSummary | null>(null);
  protected readonly error = signal<string | null>(null);
  /** Diventa true al frame successivo al caricamento: è ciò che fa partire le animazioni CSS. */
  protected readonly drawn = signal(false);
  protected readonly monthName = computed(() => monthLabel(this.month()));

  protected readonly slices = computed<Slice[]>(() => {
    const s = this.summary();
    if (!s || s.total === 0) {
      return [];
    }
    let offset = 0;
    return s.categories.map((c) => {
      const share = (c.total / s.total) * 100;
      const slice = {
        code: c.category,
        label: c.categoryLabel,
        total: c.total,
        count: c.count,
        share,
        offset,
        color: categoryStyle(c.category).color,
      };
      offset += share;
      return slice;
    });
  });

  private readonly max = computed(() => Math.max(0, ...this.slices().map((s) => s.share)));

  constructor() {
    effect(() => {
      const range = monthRange(this.month());
      this.ui.version();
      this.error.set(null);
      this.drawn.set(false);
      this.api.summary(range).subscribe({
        next: (s) => {
          this.summary.set(s);
          requestAnimationFrame(() => requestAnimationFrame(() => this.drawn.set(true)));
        },
        error: (e) => this.error.set(errorMessage(e)),
      });
    });
  }

  /** Lunghezza dell'arco: lascio un piccolo spazio tra le fette. */
  protected arc(slice: Slice): string {
    const visible = this.drawn()
      ? Math.max(0, slice.share - (this.slices().length > 1 ? 0.6 : 0))
      : 0;
    return `${visible} ${100 - visible}`;
  }

  protected barScale(slice: Slice): number {
    const max = this.max();
    return this.drawn() && max > 0 ? slice.share / max : 0;
  }
}
