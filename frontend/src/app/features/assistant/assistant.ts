import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ArrowUp, LucideAngularModule, Sparkles } from 'lucide-angular';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { ChatMessage } from '../../core/models';
import { UiState } from '../../core/ui-state';

interface Turn extends ChatMessage {
  /** Per le risposte: quali dati ha consultato Claude. */
  sources?: string[];
}

/** Nomi comprensibili per i tool che Claude può usare nel backend. */
const TOOL_LABELS: Record<string, string> = {
  get_summary: 'riepilogo',
  list_expenses: 'elenco delle spese',
  add_expense: 'nuova spesa',
};

/**
 * Domande libere sulle spese. La cronologia vive qui, nel client: a ogni domanda
 * il backend riceve l'intera conversazione (è stateless).
 */
@Component({
  selector: 'app-assistant',
  imports: [FormsModule, LucideAngularModule],
  templateUrl: './assistant.html',
  styleUrl: './assistant.css',
})
export class Assistant implements OnInit {
  private readonly api = inject(ExpenseApi);
  private readonly ui = inject(UiState);
  private readonly end = viewChild<ElementRef<HTMLElement>>('end');

  protected readonly icons = { ArrowUp, Sparkles };
  protected readonly suggestions = [
    'Quanto ho speso questo mese?',
    'In cosa spendo di più?',
    'Che spese ho fatto la settimana scorsa?',
    'Confronta ottobre con settembre',
  ];

  protected readonly turns = signal<Turn[]>([]);
  protected readonly question = signal('');
  protected readonly thinking = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly enabled = signal<boolean | null>(null);

  ngOnInit(): void {
    this.api.assistantStatus().subscribe({
      next: (s) => this.enabled.set(s.enabled),
      error: () => this.enabled.set(false),
    });
  }

  protected ask(text = this.question()): void {
    const content = text.trim();
    if (!content || this.thinking()) {
      return;
    }
    this.turns.update((t) => [...t, { role: 'user', content }]);
    this.question.set('');
    this.thinking.set(true);
    this.error.set(null);
    this.scrollToEnd();

    // Al backend vanno solo ruolo e testo. Tengo le ultime 19 battute: un numero dispari,
    // così la cronologia inizia (e finisce) con un messaggio dell'utente, come richiede l'API.
    const history = this.turns()
      .slice(-19)
      .map(({ role, content }) => ({ role, content }));
    this.api.chat(history).subscribe({
      next: (reply) => {
        this.turns.update((t) => [
          ...t,
          {
            role: 'assistant',
            content: reply.reply,
            sources: [...new Set(reply.toolsUsed.map((name) => TOOL_LABELS[name] ?? name))],
          },
        ]);
        this.thinking.set(false);
        // Se Claude ha registrato una spesa, le altre pagine devono saperlo.
        if (reply.toolsUsed.includes('add_expense')) {
          this.ui.expensesChanged();
        }
        this.scrollToEnd();
      },
      error: (e) => {
        // Tolgo la domanda senza risposta, così la conversazione resta valida, e la rimetto nel campo.
        this.turns.update((t) => t.slice(0, -1));
        this.question.set(content);
        this.error.set(errorMessage(e));
        this.thinking.set(false);
      },
    });
  }

  private scrollToEnd(): void {
    setTimeout(() =>
      this.end()?.nativeElement.scrollIntoView?.({ behavior: 'smooth', block: 'end' }),
    );
  }
}
