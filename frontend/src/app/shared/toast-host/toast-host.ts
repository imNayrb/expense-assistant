import { Component, inject } from '@angular/core';
import { Toasts } from '../../core/ui-state';

/** Pila delle notifiche, sopra la tab bar su mobile. */
@Component({
  selector: 'app-toast-host',
  template: `
    <div class="stack" role="status" aria-live="polite">
      @for (toast of toasts.items(); track toast.id) {
        <div class="toast">
          <span>{{ toast.text }}</span>
          @if (toast.action; as action) {
            <button type="button" (click)="action.run(); toasts.dismiss(toast.id)">
              {{ action.label }}
            </button>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .stack {
      position: fixed;
      left: 50%;
      bottom: calc(var(--tabbar) + 1rem + env(safe-area-inset-bottom));
      z-index: 60;
      display: grid;
      gap: 0.5rem;
      width: min(26rem, calc(100% - 2rem));
      transform: translateX(-50%);
      pointer-events: none;
    }
    @media (min-width: 60rem) {
      .stack {
        bottom: 2rem;
      }
    }
    .toast {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      padding: 0.85rem 1rem 0.85rem 1.1rem;
      border-radius: var(--radius);
      background: var(--text);
      color: var(--bg);
      font-weight: 600;
      box-shadow: var(--shadow);
      pointer-events: auto;
      animation: toast-in 380ms var(--ease-spring) both;
    }
    button {
      border: none;
      background: none;
      color: var(--accent);
      font-weight: 800;
      padding: 0.25rem 0.5rem;
    }
    @keyframes toast-in {
      from {
        opacity: 0;
        transform: translateY(16px) scale(0.96);
      }
    }
  `,
})
export class ToastHost {
  protected readonly toasts = inject(Toasts);
}
