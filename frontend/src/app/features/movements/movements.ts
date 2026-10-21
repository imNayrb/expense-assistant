import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { categoryStyle } from '../../core/categories';
import { Month, currentMonth, monthLabel, monthRange } from '../../core/dates';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { Category, Expense } from '../../core/models';
import { Toasts, UiState } from '../../core/ui-state';
import { ExpenseRow } from '../../shared/expense-row/expense-row';
import { MonthPicker } from '../../shared/month-picker/month-picker';

interface DayGroup {
  date: string;
  total: number;
  expenses: Expense[];
}

/** Tutti i movimenti di un mese, per giorno, con filtro per categoria ed eliminazione annullabile. */
@Component({
  selector: 'app-movements',
  imports: [CurrencyPipe, DatePipe, LucideAngularModule, ExpenseRow, MonthPicker],
  templateUrl: './movements.html',
  styleUrl: './movements.css',
})
export class Movements {
  private readonly api = inject(ExpenseApi);
  private readonly toasts = inject(Toasts);
  protected readonly ui = inject(UiState);

  protected readonly month = signal<Month>(currentMonth());
  protected readonly category = signal<string>('');
  protected readonly categories = signal<Category[]>([]);
  protected readonly expenses = signal<Expense[] | null>(null);
  protected readonly error = signal<string | null>(null);

  protected readonly monthName = computed(() => monthLabel(this.month()));
  protected readonly total = computed(() =>
    (this.expenses() ?? []).reduce((sum, e) => sum + e.amount, 0),
  );
  protected readonly style = categoryStyle;

  /** Le spese arrivano dalla più recente: basta raggrupparle per data. */
  protected readonly days = computed<DayGroup[]>(() => {
    const groups = new Map<string, DayGroup>();
    for (const expense of this.expenses() ?? []) {
      const group = groups.get(expense.date) ?? { date: expense.date, total: 0, expenses: [] };
      group.total += expense.amount;
      group.expenses.push(expense);
      groups.set(expense.date, group);
    }
    return [...groups.values()];
  });

  constructor() {
    this.api.categories().subscribe({ next: (c) => this.categories.set(c) });
    effect(() => this.load(this.month(), this.category(), this.ui.version()));
  }

  protected toggleCategory(code: string): void {
    this.category.update((current) => (current === code ? '' : code));
  }

  /** Rimozione ottimistica: la riga sparisce subito, con la possibilità di annullare. */
  protected remove(expense: Expense): void {
    this.expenses.update((list) => (list ?? []).filter((e) => e.id !== expense.id));
    this.api.remove(expense.id).subscribe({
      next: () => {
        this.toasts.show(`"${expense.description}" eliminata`, {
          label: 'Annulla',
          run: () => this.restore(expense),
        });
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.ui.expensesChanged();
      },
    });
  }

  private restore(expense: Expense): void {
    const { description, amount, currency, category, date } = expense;
    this.api.create({ description, amount, currency, category, date }).subscribe({
      next: () => this.ui.expensesChanged(),
      error: (e) => this.error.set(errorMessage(e)),
    });
  }

  private load(month: Month, category: string, _version: number): void {
    this.error.set(null);
    this.api.list(monthRange(month), category || null).subscribe({
      next: (list) => this.expenses.set(list),
      error: (e) => {
        this.error.set(errorMessage(e));
        this.expenses.set([]);
      },
    });
  }
}
