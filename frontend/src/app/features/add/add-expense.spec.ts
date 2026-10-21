import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AddExpense } from './add-expense';

registerLocaleData(localeIt);

describe('AddExpense', () => {
  let fixture: ComponentFixture<AddExpense>;
  let http: HttpTestingController;
  let page: HTMLElement;

  async function setup(assistantEnabled: boolean) {
    TestBed.configureTestingModule({
      imports: [AddExpense],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: LOCALE_ID, useValue: 'it-IT' },
      ],
    });
    fixture = TestBed.createComponent(AddExpense);
    http = TestBed.inject(HttpTestingController);
    page = fixture.nativeElement;
    fixture.detectChanges();

    http.expectOne('/api/categories').flush([
      { code: 'FUEL', label: 'Carburante' },
      { code: 'OTHER', label: 'Altro' },
    ]);
    http.expectOne('/api/assistant/status').flush({ enabled: assistantEnabled, model: 'test' });
    await fixture.whenStable();
  }

  afterEach(() => http.verify());

  it('interpreta un esempio, mostra lo scontrino e salva la spesa confermata', async () => {
    await setup(true);

    const example = page.querySelector<HTMLButtonElement>('.chip')!;
    example.click();
    http.expectOne('/api/assistant/parse').flush({
      description: 'Benzina',
      amount: 30,
      currency: 'EUR',
      category: 'FUEL',
      categoryLabel: 'Carburante',
      date: '2026-10-20',
    });
    await fixture.whenStable();

    expect(page.querySelector('app-receipt')).not.toBeNull();
    expect(page.querySelector<HTMLInputElement>('#r-description')!.value).toBe('Benzina');

    page.querySelector<HTMLFormElement>('app-receipt form')!.dispatchEvent(new Event('submit'));
    const create = http.expectOne('/api/expenses');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toMatchObject({
      description: 'Benzina',
      amount: 30,
      category: 'FUEL',
    });
    create.flush({ id: 1, ...create.request.body, categoryLabel: 'Carburante', createdAt: '' });
    await fixture.whenStable();

    expect(page.querySelector('app-receipt')).toBeNull();
    expect(page.textContent).toContain('Salvata: Benzina');
  });

  it("mostra l'errore del backend quando il testo non è una spesa", async () => {
    await setup(true);

    page.querySelector<HTMLButtonElement>('.chip')!.click();
    http
      .expectOne('/api/assistant/parse')
      .flush(
        { detail: 'Non ho trovato un importo valido nel testo' },
        { status: 422, statusText: 'x' },
      );
    await fixture.whenStable();

    expect(page.querySelector('[role="alert"]')!.textContent).toContain('importo valido');
  });

  it("senza assistente propone l'inserimento manuale", async () => {
    await setup(false);

    expect(page.textContent).toContain("L'assistente non è attivo");
    expect(page.querySelector('#expense-text')).toBeNull();
  });
});
