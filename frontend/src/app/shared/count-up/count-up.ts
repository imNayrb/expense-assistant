import { DestroyRef, Directive, ElementRef, effect, inject, input } from '@angular/core';

const euro = new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' });

/**
 * Mostra un importo in euro che "conta" fino al valore nuovo (easing esponenziale).
 * Aggiorna il testo direttamente a ogni frame, senza passare dal change detection.
 * Con `prefers-reduced-motion` mostra subito il valore finale.
 */
@Directive({ selector: '[appCountUp]' })
export class CountUp {
  readonly appCountUp = input.required<number>();
  readonly duration = input(900);

  private readonly el = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private current = 0;
  private frame = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => cancelAnimationFrame(this.frame));
    effect(() => this.animateTo(this.appCountUp()));
  }

  private animateTo(target: number): void {
    cancelAnimationFrame(this.frame);
    const reduced =
      typeof matchMedia !== 'function' || matchMedia('(prefers-reduced-motion: reduce)').matches;
    const from = this.current;
    if (reduced || typeof requestAnimationFrame !== 'function') {
      this.render(target);
      return;
    }
    const start = performance.now();
    const step = (now: number) => {
      const t = Math.min(1, (now - start) / this.duration());
      const eased = t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
      this.render(from + (target - from) * eased);
      if (t < 1) {
        this.frame = requestAnimationFrame(step);
      }
    };
    this.frame = requestAnimationFrame(step);
  }

  private render(value: number): void {
    this.current = value;
    this.el.textContent = euro.format(value);
  }
}
