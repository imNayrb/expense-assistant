import { Component, computed, model } from '@angular/core';
import { ChevronLeft, ChevronRight, LucideAngularModule } from 'lucide-angular';
import { Month, currentMonth, monthLabel, sameMonth, shiftMonth } from '../../core/dates';

/** Selettore del mese a pillola. Non va oltre il mese corrente. */
@Component({
  selector: 'app-month-picker',
  imports: [LucideAngularModule],
  template: `
    <div class="picker" role="group" aria-label="Mese">
      <button type="button" class="pressable" (click)="move(-1)" aria-label="Mese precedente">
        <lucide-icon [img]="ChevronLeft" [size]="18" />
      </button>
      <span class="label" aria-live="polite">{{ label() }}</span>
      <button
        type="button"
        class="pressable"
        (click)="move(1)"
        [disabled]="isCurrent()"
        aria-label="Mese successivo"
      >
        <lucide-icon [img]="ChevronRight" [size]="18" />
      </button>
    </div>
  `,
  styles: `
    .picker {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.25rem;
      border-radius: 999px;
      background: var(--bg-elev);
      border: 1px solid var(--stroke);
    }
    .label {
      min-width: 8.5rem;
      text-align: center;
      font-weight: 700;
      text-transform: capitalize;
    }
    button {
      display: grid;
      place-items: center;
      width: 2.25rem;
      height: 2.25rem;
      border: none;
      border-radius: 50%;
      background: var(--bg-elev-2);
      transition: transform 160ms var(--ease-out);
    }
    button:disabled {
      opacity: 0.35;
      cursor: not-allowed;
    }
  `,
})
export class MonthPicker {
  readonly month = model.required<Month>();

  protected readonly ChevronLeft = ChevronLeft;
  protected readonly ChevronRight = ChevronRight;
  protected readonly label = computed(() => monthLabel(this.month()));
  protected readonly isCurrent = computed(() => sameMonth(this.month(), currentMonth()));

  protected move(delta: number): void {
    this.month.update((m) => shiftMonth(m, delta));
  }
}
