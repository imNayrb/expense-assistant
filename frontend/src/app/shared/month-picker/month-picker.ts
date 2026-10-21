import { Component, computed, model } from '@angular/core';
import { Month, currentMonth, monthLabel, sameMonth, shiftMonth } from '../../core/dates';

/** Selettore del mese: frecce avanti/indietro e nome del mese. Non va oltre il mese corrente. */
@Component({
  selector: 'app-month-picker',
  template: `
    <div class="picker" role="group" aria-label="Mese">
      <button type="button" class="btn ghost" (click)="move(-1)" aria-label="Mese precedente">
        ‹
      </button>
      <span class="label" aria-live="polite">{{ label() }}</span>
      <button
        type="button"
        class="btn ghost"
        (click)="move(1)"
        [disabled]="isCurrent()"
        aria-label="Mese successivo"
      >
        ›
      </button>
    </div>
  `,
  styles: `
    .picker {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
    }
    .label {
      min-width: 9.5rem;
      text-align: center;
      font-weight: 600;
    }
    .btn {
      padding: 0.3rem 0.8rem;
      font-size: 1.1rem;
    }
  `,
})
export class MonthPicker {
  readonly month = model.required<Month>();

  protected readonly label = computed(() => monthLabel(this.month()));
  protected readonly isCurrent = computed(() => sameMonth(this.month(), currentMonth()));

  protected move(delta: number): void {
    this.month.update((m) => shiftMonth(m, delta));
  }
}
