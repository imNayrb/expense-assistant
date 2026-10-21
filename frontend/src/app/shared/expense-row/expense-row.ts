import { CurrencyPipe } from '@angular/common';
import { Component, input, output, signal } from '@angular/core';
import { LucideAngularModule, Trash2 } from 'lucide-angular';
import { Expense } from '../../core/models';
import { CategoryIcon } from '../category-icon/category-icon';

const REVEAL = 88; // larghezza dell'azione "Elimina", in px

/**
 * Riga di un movimento. Su touch si trascina a sinistra per scoprire "Elimina";
 * con mouse e tastiera il pulsante compare al passaggio o al focus.
 */
@Component({
  selector: 'app-expense-row',
  imports: [CurrencyPipe, CategoryIcon, LucideAngularModule],
  template: `
    @if (deletable()) {
      <button
        type="button"
        class="delete"
        [class.revealed]="offset() < 0"
        (click)="remove.emit(expense())"
        [attr.aria-label]="'Elimina ' + expense().description"
        [attr.tabindex]="offset() < 0 ? 0 : -1"
      >
        <lucide-icon [img]="Trash2" [size]="20" />
        <span>Elimina</span>
      </button>
    }
    <div
      class="content"
      [class.dragging]="dragging()"
      [style.transform]="'translateX(' + offset() + 'px)'"
      (pointerdown)="start($event)"
      (pointermove)="move($event)"
      (pointerup)="end()"
      (pointercancel)="end()"
    >
      <app-category-icon [code]="expense().category" />
      <div class="what">
        <span class="title">{{ expense().description }}</span>
        <span class="muted">{{ expense().categoryLabel }}</span>
      </div>
      <span class="amount">
        −{{ expense().amount | currency: expense().currency }}
        @if (caption()) {
          <small class="muted">{{ caption() }}</small>
        }
      </span>
      @if (deletable()) {
        <button
          type="button"
          class="quick-delete"
          (click)="remove.emit(expense())"
          [attr.aria-label]="'Elimina ' + expense().description"
        >
          <lucide-icon [img]="Trash2" [size]="18" />
        </button>
      }
    </div>
  `,
  styleUrl: './expense-row.css',
})
export class ExpenseRow {
  readonly expense = input.required<Expense>();
  readonly deletable = input(true);
  /** Testo secondario sotto l'importo (es. la data nella panoramica). */
  readonly caption = input<string | null>(null);
  readonly remove = output<Expense>();

  protected readonly Trash2 = Trash2;
  protected readonly offset = signal(0);
  protected readonly dragging = signal(false);

  private startX = 0;
  private startY = 0;
  private base = 0;
  private horizontal: boolean | null = null;

  protected start(event: PointerEvent): void {
    if (!this.deletable() || event.pointerType === 'mouse') {
      return;
    }
    this.startX = event.clientX;
    this.startY = event.clientY;
    this.base = this.offset();
    this.horizontal = null;
  }

  protected move(event: PointerEvent): void {
    if (!this.deletable() || event.pointerType === 'mouse' || this.startX === 0) {
      return;
    }
    const dx = event.clientX - this.startX;
    const dy = event.clientY - this.startY;
    // Decido la direzione al primo movimento evidente: lo scroll verticale non va bloccato.
    if (this.horizontal === null && Math.abs(dx) + Math.abs(dy) > 8) {
      this.horizontal = Math.abs(dx) > Math.abs(dy);
      if (this.horizontal) {
        (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
        this.dragging.set(true);
      }
    }
    if (this.horizontal) {
      const next = Math.min(0, Math.max(-REVEAL * 1.25, this.base + dx));
      this.offset.set(next);
    }
  }

  protected end(): void {
    if (this.dragging()) {
      // Scatta aperto o chiuso a seconda di quanto è stato trascinato.
      this.offset.set(this.offset() < -REVEAL / 2 ? -REVEAL : 0);
    }
    this.dragging.set(false);
    this.startX = 0;
    this.horizontal = null;
  }
}
