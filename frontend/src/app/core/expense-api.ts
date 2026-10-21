import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AssistantStatus,
  Category,
  ChatMessage,
  ChatReply,
  DateRange,
  Expense,
  ExpenseDraft,
  ExpenseInput,
  ExpenseSummary,
} from './models';

/**
 * Accesso all'API REST del backend. Gli URL sono relativi (`/api/...`): in sviluppo li
 * inoltra il proxy di `ng serve`, in Docker nginx. Così non servono configurazioni CORS.
 */
@Injectable({ providedIn: 'root' })
export class ExpenseApi {
  private readonly http = inject(HttpClient);

  list(range: DateRange, category?: string | null): Observable<Expense[]> {
    let params = new HttpParams().set('from', range.from).set('to', range.to);
    if (category) {
      params = params.set('category', category);
    }
    return this.http.get<Expense[]>('/api/expenses', { params });
  }

  summary(range: DateRange): Observable<ExpenseSummary> {
    const params = new HttpParams().set('from', range.from).set('to', range.to);
    return this.http.get<ExpenseSummary>('/api/expenses/summary', { params });
  }

  create(input: ExpenseInput): Observable<Expense> {
    return this.http.post<Expense>('/api/expenses', input);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`/api/expenses/${id}`);
  }

  categories(): Observable<Category[]> {
    return this.http.get<Category[]>('/api/categories');
  }

  assistantStatus(): Observable<AssistantStatus> {
    return this.http.get<AssistantStatus>('/api/assistant/status');
  }

  /** Testo libero → bozza di spesa. Il backend non salva nulla. */
  parse(text: string): Observable<ExpenseDraft> {
    return this.http.post<ExpenseDraft>('/api/assistant/parse', { text });
  }

  chat(messages: ChatMessage[]): Observable<ChatReply> {
    return this.http.post<ChatReply>('/api/assistant/chat', { messages });
  }
}

/** Messaggio da mostrare all'utente per un errore HTTP (il backend risponde con problem details). */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Il backend non risponde: controlla che sia avviato su localhost:8080.';
    }
    const detail = (error.error as { detail?: string } | null)?.detail;
    if (detail) {
      return detail;
    }
    return `Errore del server (HTTP ${error.status}).`;
  }
  return 'Qualcosa è andato storto. Riprova.';
}
