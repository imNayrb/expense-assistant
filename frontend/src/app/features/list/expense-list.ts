import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { Month, currentMonth, monthLabel, monthRange } from '../../core/dates';
import { Category, Expense } from '../../core/models';
import { MonthPicker } from '../../shared/month-picker/month-picker';

interface DayGroup {
  date: string;
  total: number;
  expenses: Expense[];
}

/** Elenco delle spese di un mese, raggruppate per giorno, con filtro per categoria. */
@Component({
  selector: 'app-expense-list',
  imports: [FormsModule, RouterLink, CurrencyPipe, DatePipe, MonthPicker],
  templateUrl: './expense-list.html',
  styleUrl: './expense-list.css',
})
export class ExpenseList {
  private readonly api = inject(ExpenseApi);

  protected readonly month = signal<Month>(currentMonth());
  protected readonly category = signal<string>('');
  protected readonly categories = signal<Category[]>([]);
  protected readonly expenses = signal<Expense[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly monthName = computed(() => monthLabel(this.month()));

  /** Le spese arrivano già ordinate dalla più recente: basta raggrupparle per data. */
  protected readonly days = computed<DayGroup[]>(() => {
    const groups = new Map<string, DayGroup>();
    for (const expense of this.expenses()) {
      const group = groups.get(expense.date) ?? { date: expense.date, total: 0, expenses: [] };
      group.total += expense.amount;
      group.expenses.push(expense);
      groups.set(expense.date, group);
    }
    return [...groups.values()];
  });

  constructor() {
    this.api.categories().subscribe({ next: (c) => this.categories.set(c) });
    // Ricarica quando cambiano mese o categoria.
    effect(() => this.load(this.month(), this.category()));
  }

  protected remove(expense: Expense): void {
    if (!confirm(`Eliminare "${expense.description}"?`)) {
      return;
    }
    this.api.remove(expense.id).subscribe({
      next: () => this.expenses.update((list) => list.filter((e) => e.id !== expense.id)),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(month: Month, category: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.list(monthRange(month), category || null).subscribe({
      next: (list) => {
        this.expenses.set(list);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.loading.set(false);
      },
    });
  }
}
