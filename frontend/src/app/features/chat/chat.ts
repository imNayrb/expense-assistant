import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ExpenseApi, errorMessage } from '../../core/expense-api';
import { ChatMessage } from '../../core/models';

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
  selector: 'app-chat',
  imports: [FormsModule],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat implements OnInit {
  private readonly api = inject(ExpenseApi);

  protected readonly suggestions = [
    'Quanto ho speso questo mese?',
    'In cosa spendo di più?',
    'Che spese ho fatto la settimana scorsa?',
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
            sources: reply.toolsUsed.map((name) => TOOL_LABELS[name] ?? name),
          },
        ]);
        this.thinking.set(false);
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

  protected uniqueSources(sources: string[] = []): string {
    return [...new Set(sources)].join(', ');
  }
}
