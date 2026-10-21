import { Component, computed, input, linkedSignal, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Category, ExpenseInput } from '../../core/models';

/**
 * La bozza di una spesa, disegnata come uno scontrino da confermare.
 * Tutti i campi sono modificabili: l'interpretazione di Claude è un punto di partenza,
 * l'ultima parola è dell'utente.
 */
@Component({
  selector: 'app-receipt',
  imports: [FormsModule],
  templateUrl: './receipt.html',
  styleUrl: './receipt.css',
})
export class Receipt {
  readonly draft = input.required<ExpenseInput>();
  readonly categories = input<Category[]>([]);
  readonly saving = input(false);
  /** Titolo dello scontrino: cambia tra bozza di Claude e inserimento manuale. */
  readonly heading = input('Da confermare');

  readonly save = output<ExpenseInput>();
  readonly discard = output<void>();

  /** Copia modificabile della bozza: si riallinea quando arriva una nuova bozza. */
  protected readonly form = linkedSignal(() => ({ ...this.draft() }));

  /** L'importo si scrive e si legge all'italiana ("30,00"): lo tengo come testo. */
  protected readonly amountText = linkedSignal(() => formatAmount(this.draft().amount));
  private readonly amount = computed(() => parseAmount(this.amountText()));

  protected readonly valid = computed(() => {
    const f = this.form();
    return f.description.trim().length > 0 && this.amount() > 0 && !!f.date && !!f.category;
  });

  protected submit(): void {
    if (!this.valid() || this.saving()) {
      return;
    }
    const f = this.form();
    this.save.emit({ ...f, description: f.description.trim(), amount: this.amount() });
  }

  /** All'uscita dal campo l'importo viene riscritto con due decimali. */
  protected tidyAmount(): void {
    const value = this.amount();
    if (value > 0) {
      this.amountText.set(formatAmount(value));
    }
  }

  /** ngModel modifica l'oggetto sul posto: lo rimpiazzo per notificare i computed. */
  protected touch(): void {
    this.form.update((f) => ({ ...f }));
  }
}

const amountFormat = new Intl.NumberFormat('it-IT', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
  useGrouping: false,
});

export function formatAmount(value: number): string {
  return value > 0 ? amountFormat.format(value) : '';
}

/** Accetta "30", "30,5", "30.50" e "1.250,00"; restituisce 0 se il testo non è un importo. */
export function parseAmount(text: string): number {
  const cleaned = text.trim().replace(/\s|€/g, '');
  const normalized = cleaned.includes(',') ? cleaned.replace(/\./g, '').replace(',', '.') : cleaned;
  const value = Number(normalized);
  return Number.isFinite(value) && value > 0 ? Math.round(value * 100) / 100 : 0;
}
