import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UiState } from '../../core/ui-state';
import { AddSheet } from './add-sheet';

registerLocaleData(localeIt);

describe('AddSheet', () => {
  let fixture: ComponentFixture<AddSheet>;
  let http: HttpTestingController;
  let ui: UiState;
  let el: HTMLElement;

  async function open(assistantEnabled: boolean) {
    TestBed.configureTestingModule({
      imports: [AddSheet],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'it-IT' },
      ],
    });
    fixture = TestBed.createComponent(AddSheet);
    http = TestBed.inject(HttpTestingController);
    ui = TestBed.inject(UiState);
    el = fixture.nativeElement;
    fixture.detectChanges();
    http.expectOne('/api/categories').flush([
      { code: 'FUEL', label: 'Carburante' },
      { code: 'OTHER', label: 'Altro' },
    ]);
    http.expectOne('/api/assistant/status').flush({ enabled: assistantEnabled, model: 'test' });
    ui.openSheet();
    await fixture.whenStable();
  }

  afterEach(() => http.verify());

  it('interpreta la frase, mostra lo scontrino e salva la spesa confermata', async () => {
    await open(true);
    const versionBefore = ui.version();

    el.querySelector<HTMLButtonElement>('.chip')!.click();
    http.expectOne('/api/assistant/parse').flush({
      description: 'Benzina',
      amount: 30,
      currency: 'EUR',
      category: 'FUEL',
      categoryLabel: 'Carburante',
      date: '2026-10-20',
    });
    await fixture.whenStable();

    expect(el.querySelector<HTMLInputElement>('#r-description')!.value).toBe('Benzina');
    expect(el.querySelector<HTMLInputElement>('#r-amount')!.value).toBe('30,00');

    el.querySelector<HTMLFormElement>('app-receipt form')!.dispatchEvent(new Event('submit'));
    const create = http.expectOne('/api/expenses');
    expect(create.request.body).toMatchObject({
      description: 'Benzina',
      amount: 30,
      category: 'FUEL',
    });
    create.flush({ id: 1, ...create.request.body, categoryLabel: 'Carburante', createdAt: '' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Spesa salvata');
    expect(ui.version()).toBe(versionBefore + 1);
  });

  it("mostra l'errore del backend e resta sulla frase", async () => {
    await open(true);

    el.querySelector<HTMLButtonElement>('.chip')!.click();
    http
      .expectOne('/api/assistant/parse')
      .flush({ detail: 'Non ho trovato un importo valido' }, { status: 422, statusText: 'x' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')!.textContent).toContain('importo valido');
    expect(el.querySelector('#sheet-text')).not.toBeNull();
  });

  it("senza assistente offre l'inserimento manuale", async () => {
    await open(false);

    expect(el.textContent).toContain("L'assistente non è attivo");
    el.querySelector<HTMLButtonElement>('.btn.block')!.click();
    await fixture.whenStable();

    expect(el.querySelector('app-receipt')).not.toBeNull();
  });
});
