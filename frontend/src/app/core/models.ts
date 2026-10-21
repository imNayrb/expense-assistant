// Tipi che rispecchiano i DTO JSON del backend (vedi backend/README.md).

export interface Category {
  code: string;
  label: string;
}

export interface Expense {
  id: number;
  description: string;
  amount: number;
  currency: string;
  category: string;
  categoryLabel: string;
  /** Data della spesa, YYYY-MM-DD. */
  date: string;
  createdAt: string;
}

/** Dati per creare o modificare una spesa. */
export interface ExpenseInput {
  description: string;
  amount: number;
  currency: string;
  category: string;
  date: string;
}

/** Spesa interpretata da Claude, non ancora salvata. */
export interface ExpenseDraft extends ExpenseInput {
  categoryLabel: string;
}

export interface CategoryTotal {
  category: string;
  categoryLabel: string;
  total: number;
  count: number;
}

export interface ExpenseSummary {
  from: string;
  to: string;
  total: number;
  count: number;
  categories: CategoryTotal[];
}

export interface DateRange {
  from: string;
  to: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatReply {
  reply: string;
  toolsUsed: string[];
}

export interface AssistantStatus {
  enabled: boolean;
  model: string;
}
