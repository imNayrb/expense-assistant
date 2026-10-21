import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Sparkles, TrendingDown, TrendingUp } from 'lucide-angular';
import { categoryStyle } from '../../core/categories';
import { currentMonth, monthLabel, monthRange, shiftMonth, todayIso } from '../../core/dates';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { Expense, ExpenseSummary } from '../../core/models';
import { UiState } from '../../core/ui-state';
import { CountUp } from '../../shared/count-up/count-up';
import { ExpenseRow } from '../../shared/expense-row/expense-row';

interface Segment {
  code: string;
  label: string;
  share: number;
  color: string;
}

/**
 * Prima schermata: quanto ho speso questo mese, com'è andata rispetto al mese scorso,
 * dove sono finiti i soldi e gli ultimi movimenti.
 */
@Component({
  selector: 'app-overview',
  imports: [RouterLink, DatePipe, DecimalPipe, LucideAngularModule, CountUp, ExpenseRow],
  templateUrl: './overview.html',
  styleUrl: './overview.css',
})
export class Overview {
  private readonly api = inject(ExpenseApi);
  protected readonly ui = inject(UiState);

  protected readonly icons = { Sparkles, TrendingDown, TrendingUp };
  protected readonly month = currentMonth();
  protected readonly monthName = monthLabel(this.month).split(' ')[0];
  protected readonly previousName = monthLabel(shiftMonth(this.month, -1)).split(' ')[0];

  protected readonly summary = signal<ExpenseSummary | null>(null);
  protected readonly previousTotal = signal<number | null>(null);
  protected readonly recent = signal<Expense[] | null>(null);
  protected readonly error = signal<string | null>(null);

  /** Variazione percentuale rispetto allo stesso periodo del mese scorso. */
  protected readonly delta = computed(() => {
    const current = this.summary()?.total ?? 0;
    const previous = this.previousTotal();
    if (!previous) {
      return null;
    }
    const pct = Math.round(((current - previous) / previous) * 100);
    return { pct: Math.abs(pct), lower: pct <= 0 };
  });

  /** Le prime quattro categorie; il resto confluisce in un unico segmento. */
  protected readonly segments = computed<Segment[]>(() => {
    const s = this.summary();
    if (!s || s.total === 0) {
      return [];
    }
    const top = s.categories.slice(0, 4).map((c) => ({
      code: c.category,
      label: c.categoryLabel,
      share: (c.total / s.total) * 100,
      color: categoryStyle(c.category).color,
    }));
    const rest = s.categories.slice(4).reduce((sum, c) => sum + c.total, 0);
    if (rest > 0) {
      top.push({ code: 'OTHER', label: 'Altro', share: (rest / s.total) * 100, color: '#94a3b8' });
    }
    return top;
  });

  constructor() {
    effect(() => {
      this.ui.version(); // ricarica dopo ogni modifica alle spese
      this.load();
    });
  }

  private load(): void {
    const range = monthRange(this.month);
    // Confronto equo: il mese scorso fino allo stesso giorno di oggi.
    const previous = monthRange(shiftMonth(this.month, -1));
    const day = Math.min(Number(todayIso().slice(8)), Number(previous.to.slice(8)));
    const previousToSameDay = {
      from: previous.from,
      to: previous.from.slice(0, 8) + String(day).padStart(2, '0'),
    };

    this.error.set(null);
    this.api.summary(range).subscribe({
      next: (s) => this.summary.set(s),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.api.summary(previousToSameDay).subscribe({
      next: (s) => this.previousTotal.set(s.total),
      error: () => this.previousTotal.set(null),
    });
    this.api.list(range).subscribe({
      next: (list) => this.recent.set(list.slice(0, 6)),
      error: () => this.recent.set([]),
    });
  }
}
