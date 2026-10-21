import { Component, computed, input } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';
import { categoryStyle } from '../../core/categories';

/** Cerchio colorato con l'icona della categoria. */
@Component({
  selector: 'app-category-icon',
  imports: [LucideAngularModule],
  template: `<lucide-icon [img]="style().icon" [size]="size() * 0.45" [strokeWidth]="2.2" />`,
  host: {
    'aria-hidden': 'true',
    '[style.--c]': 'style().color',
    '[style.width.px]': 'size()',
    '[style.height.px]': 'size()',
  },
  styles: `
    :host {
      flex: none;
      display: grid;
      place-items: center;
      border-radius: 50%;
      color: var(--c);
      background: color-mix(in srgb, var(--c) 16%, transparent);
    }
  `,
})
export class CategoryIcon {
  readonly code = input.required<string>();
  readonly size = input(44);

  protected readonly style = computed(() => categoryStyle(this.code()));
}
