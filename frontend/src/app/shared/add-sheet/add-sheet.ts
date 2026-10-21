import { CurrencyPipe } from '@angular/common';
import {
  Component,
  ElementRef,
  OnInit,
  effect,
  inject,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ArrowUp, Check, LucideAngularModule, Sparkles, X } from 'lucide-angular';
import { todayIso } from '../../core/dates';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { Category, Expense, ExpenseInput } from '../../core/models';
import { UiState } from '../../core/ui-state';
import { Receipt } from '../receipt/receipt';

type Step = 'input' | 'parsing' | 'draft' | 'saved';

const CLOSE_MS = 220;
const DRAG_CLOSE_PX = 110;

/**
 * Pannello di inserimento: bottom sheet su mobile, finestra centrata su desktop.
 * Flusso: frase → Claude la interpreta → scontrino da confermare → spunta di conferma.
 * Usa <dialog> nativo: focus trap, Esc e sfondo inerte arrivano gratis dal browser.
 */
@Component({
  selector: 'app-add-sheet',
  imports: [FormsModule, Receipt, CurrencyPipe, LucideAngularModule],
  templateUrl: './add-sheet.html',
  styleUrl: './add-sheet.css',
})
export class AddSheet implements OnInit {
  private readonly api = inject(ExpenseApi);
  private readonly ui = inject(UiState);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly textInput = viewChild<ElementRef<HTMLInputElement>>('textInput');

  protected readonly icons = { ArrowUp, Check, Sparkles, X };
  protected readonly examples = [
    'ieri 30€ di benzina',
    'pizza con gli amici 18,50',
    'abbonamento palestra 39 euro',
    'spesa al supermercato 64,20',
  ];

  protected readonly step = signal<Step>('input');
  protected readonly text = signal('');
  protected readonly draft = signal<ExpenseInput | null>(null);
  protected readonly manual = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly saved = signal<Expense | null>(null);
  protected readonly categories = signal<Category[]>([]);
  protected readonly assistantEnabled = signal<boolean | null>(null);
  protected readonly closing = signal(false);
  protected readonly dragY = signal(0);

  private dragStart: number | null = null;
  private autoClose?: ReturnType<typeof setTimeout>;

  constructor() {
    effect(() => {
      const open = this.ui.sheetOpen();
      untracked(() => (open ? this.show() : this.hide()));
    });
  }

  ngOnInit(): void {
    this.api.categories().subscribe({ next: (c) => this.categories.set(c) });
    this.api.assistantStatus().subscribe({
      next: (s) => this.assistantEnabled.set(s.enabled),
      error: () => this.assistantEnabled.set(false),
    });
  }

  protected interpret(value = this.text()): void {
    const text = value.trim();
    if (!text || this.step() === 'parsing') {
      return;
    }
    this.text.set(text);
    this.error.set(null);
    this.step.set('parsing');
    this.api.parse(text).subscribe({
      next: (draft) => {
        this.manual.set(false);
        this.draft.set(draft);
        this.step.set('draft');
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.step.set('input');
      },
    });
  }

  protected startManual(): void {
    this.error.set(null);
    this.manual.set(true);
    this.draft.set({
      description: '',
      amount: 0,
      currency: 'EUR',
      category: 'OTHER',
      date: todayIso(),
    });
    this.step.set('draft');
  }

  protected save(input: ExpenseInput): void {
    this.saving.set(true);
    this.error.set(null);
    this.api.create(input).subscribe({
      next: (expense) => {
        this.saving.set(false);
        this.saved.set(expense);
        this.step.set('saved');
        this.ui.expensesChanged();
        this.autoClose = setTimeout(() => this.ui.closeSheet(), 1400);
      },
      error: (e) => {
        this.saving.set(false);
        this.error.set(errorMessage(e));
      },
    });
  }

  protected backToInput(): void {
    this.draft.set(null);
    this.step.set('input');
    queueMicrotask(() => this.textInput()?.nativeElement.focus());
  }

  protected close(): void {
    this.ui.closeSheet();
  }

  /** Esc: lascio chiudere con l'animazione invece della chiusura istantanea del browser. */
  protected onCancel(event: Event): void {
    event.preventDefault();
    this.close();
  }

  /** Un clic sullo sfondo arriva al <dialog> stesso: chiude. */
  protected onDialogClick(event: MouseEvent): void {
    if (event.target === this.dialog().nativeElement) {
      this.close();
    }
  }

  // Trascinamento verso il basso per chiudere (dalla maniglia o dall'intestazione).
  protected dragBegin(event: PointerEvent): void {
    this.dragStart = event.clientY;
    (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
  }

  protected dragMove(event: PointerEvent): void {
    if (this.dragStart !== null) {
      this.dragY.set(Math.max(0, event.clientY - this.dragStart));
    }
  }

  protected dragEnd(): void {
    if (this.dragStart === null) {
      return;
    }
    this.dragStart = null;
    if (this.dragY() > DRAG_CLOSE_PX) {
      this.close();
    } else {
      this.dragY.set(0);
    }
  }

  private show(): void {
    const dialog = this.dialog().nativeElement;
    clearTimeout(this.autoClose);
    this.step.set('input');
    this.text.set('');
    this.draft.set(null);
    this.saved.set(null);
    this.error.set(null);
    this.dragY.set(0);
    this.closing.set(false);
    if (!dialog.open) {
      if (typeof dialog.showModal === 'function') {
        dialog.showModal();
      } else {
        dialog.setAttribute('open', '');
      }
    }
    queueMicrotask(() => this.textInput()?.nativeElement.focus());
  }

  private hide(): void {
    const dialog = this.dialog().nativeElement;
    if (!dialog.open) {
      return;
    }
    this.closing.set(true);
    setTimeout(() => {
      if (typeof dialog.close === 'function') {
        dialog.close();
      } else {
        dialog.removeAttribute('open');
      }
      this.closing.set(false);
      this.dragY.set(0);
    }, CLOSE_MS);
  }
}
