import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { todayIso } from '../../core/dates';
import { Category, Expense, ExpenseInput } from '../../core/models';
import { Receipt } from '../../shared/receipt/receipt';

/**
 * Pagina principale: scrivi la spesa a parole, Claude la interpreta in una bozza,
 * tu la confermi. Se l'assistente è spento, resta l'inserimento manuale.
 */
@Component({
  selector: 'app-add-expense',
  imports: [FormsModule, Receipt, RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './add-expense.html',
  styleUrl: './add-expense.css',
})
export class AddExpense implements OnInit {
  private readonly api = inject(ExpenseApi);

  protected readonly examples = [
    'ieri 30€ di benzina',
    'pizza con gli amici 18,50',
    'abbonamento palestra 39 euro',
  ];

  protected readonly text = signal('');
  protected readonly categories = signal<Category[]>([]);
  protected readonly assistantEnabled = signal<boolean | null>(null);
  protected readonly draft = signal<ExpenseInput | null>(null);
  protected readonly manual = signal(false);
  protected readonly parsing = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly saved = signal<Expense | null>(null);

  ngOnInit(): void {
    this.api.categories().subscribe({
      next: (c) => this.categories.set(c),
      error: (e) => this.error.set(errorMessage(e)),
    });
    this.api.assistantStatus().subscribe({
      next: (s) => this.assistantEnabled.set(s.enabled),
      error: () => this.assistantEnabled.set(false),
    });
  }

  protected interpret(value = this.text()): void {
    const text = value.trim();
    if (!text || this.parsing()) {
      return;
    }
    this.text.set(text);
    this.parsing.set(true);
    this.error.set(null);
    this.saved.set(null);
    this.api.parse(text).subscribe({
      next: (draft) => {
        this.manual.set(false);
        this.draft.set(draft);
        this.parsing.set(false);
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.parsing.set(false);
      },
    });
  }

  protected startManual(): void {
    this.saved.set(null);
    this.error.set(null);
    this.manual.set(true);
    this.draft.set({
      description: '',
      amount: 0,
      currency: 'EUR',
      category: 'OTHER',
      date: todayIso(),
    });
  }

  protected save(input: ExpenseInput): void {
    this.saving.set(true);
    this.error.set(null);
    this.api.create(input).subscribe({
      next: (expense) => {
        this.saved.set(expense);
        this.draft.set(null);
        this.text.set('');
        this.saving.set(false);
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.saving.set(false);
      },
    });
  }

  protected discard(): void {
    this.draft.set(null);
  }
}
