import { DateRange } from './models';

/** Un mese di calendario; `month` va da 1 a 12. */
export interface Month {
  year: number;
  month: number;
}

const pad = (n: number) => String(n).padStart(2, '0');

/** Data locale in formato YYYY-MM-DD (non UTC: alle 00:30 in Italia è già "oggi"). */
export function toIsoDate(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function todayIso(now: Date = new Date()): string {
  return toIsoDate(now);
}

export function currentMonth(now: Date = new Date()): Month {
  return { year: now.getFullYear(), month: now.getMonth() + 1 };
}

export function shiftMonth({ year, month }: Month, delta: number): Month {
  const index = year * 12 + (month - 1) + delta;
  return { year: Math.floor(index / 12), month: (index % 12) + 1 };
}

export function monthRange({ year, month }: Month): DateRange {
  // Il giorno 0 del mese successivo è l'ultimo giorno del mese corrente.
  const lastDay = new Date(year, month, 0).getDate();
  return { from: `${year}-${pad(month)}-01`, to: `${year}-${pad(month)}-${pad(lastDay)}` };
}

export function sameMonth(a: Month, b: Month): boolean {
  return a.year === b.year && a.month === b.month;
}

const monthFormat = new Intl.DateTimeFormat('it-IT', { month: 'long', year: 'numeric' });

/** "ottobre 2026" */
export function monthLabel({ year, month }: Month): string {
  return monthFormat.format(new Date(year, month - 1, 1));
}
