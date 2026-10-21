import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  ChartPie,
  LayoutGrid,
  List,
  LucideAngularModule,
  LucideIconData,
  MessageCircle,
  Plus,
  Wallet,
} from 'lucide-angular';
import { UiState } from './core/ui-state';
import { AddSheet } from './shared/add-sheet/add-sheet';
import { ToastHost } from './shared/toast-host/toast-host';

interface NavLink {
  path: string;
  label: string;
  icon: LucideIconData;
}

/**
 * Guscio dell'app. Su mobile: tab bar in basso con il "+" al centro, come un'app nativa.
 * Su desktop: barra laterale. Il pannello di inserimento e le notifiche vivono qui,
 * così sono raggiungibili da ogni pagina.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LucideAngularModule, AddSheet, ToastHost],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly ui = inject(UiState);
  protected readonly icons = { Plus, Wallet };

  protected readonly links: NavLink[] = [
    { path: '/', label: 'Panoramica', icon: LayoutGrid },
    { path: '/movimenti', label: 'Movimenti', icon: List },
    { path: '/statistiche', label: 'Statistiche', icon: ChartPie },
    { path: '/assistente', label: 'Assistente', icon: MessageCircle },
  ];

  /** Nella tab bar il "+" sta in mezzo: due link a sinistra, due a destra. */
  protected readonly leftLinks = this.links.slice(0, 2);
  protected readonly rightLinks = this.links.slice(2);
}
