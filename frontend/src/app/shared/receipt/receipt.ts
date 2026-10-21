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

  protected readonly valid = computed(() => {
    const f = this.form();
    return f.description.trim().length > 0 && Number(f.amount) > 0 && !!f.date && !!f.category;
  });

  protected submit(): void {
    if (!this.valid() || this.saving()) {
      return;
    }
    const f = this.form();
    this.save.emit({ ...f, description: f.description.trim(), amount: Number(f.amount) });
  }

  /** ngModel modifica l'oggetto sul posto: lo rimpiazzo per notificare i computed. */
  protected touch(): void {
    this.form.update((f) => ({ ...f }));
  }
}
