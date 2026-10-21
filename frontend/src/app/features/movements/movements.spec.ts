import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Toasts } from '../../core/ui-state';
import { Movements } from './movements';

registerLocaleData(localeIt);

const expense = (id: number, description: string, date: string) => ({
  id,
  description,
  amount: 10,
  currency: 'EUR',
  category: 'OTHER',
  categoryLabel: 'Altro',
  date,
  createdAt: '',
});

describe('Movements', () => {
  let fixture: ComponentFixture<Movements>;
  let http: HttpTestingController;
  let el: HTMLElement;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Movements],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'it-IT' },
      ],
    });
    fixture = TestBed.createComponent(Movements);
    http = TestBed.inject(HttpTestingController);
    el = fixture.nativeElement;
    fixture.detectChanges();
    http.expectOne('/api/categories').flush([{ code: 'OTHER', label: 'Altro' }]);
    await fixture.whenStable();
    http
      .expectOne((r) => r.url === '/api/expenses')
      .flush([
        expense(2, 'Caffè', '2026-10-20'),
        expense(1, 'Giornale', '2026-10-20'),
        expense(3, 'Treno', '2026-10-18'),
      ]);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('raggruppa le spese per giorno', () => {
    expect(el.querySelectorAll('section.day').length).toBe(2);
    expect(el.querySelectorAll('app-expense-row').length).toBe(3);
  });

  it('elimina subito la riga e permette di annullare', async () => {
    el.querySelector<HTMLButtonElement>('.quick-delete')!.click();
    await fixture.whenStable();
    expect(el.querySelectorAll('app-expense-row').length).toBe(2);

    http.expectOne('/api/expenses/2').flush(null, { status: 204, statusText: 'No Content' });
    const toast = TestBed.inject(Toasts).items()[0];
    expect(toast.text).toContain('Caffè');

    toast.action!.run();
    const restore = http.expectOne((r) => r.method === 'POST' && r.url === '/api/expenses');
    expect(restore.request.body).toMatchObject({ description: 'Caffè', amount: 10 });
    restore.flush(expense(4, 'Caffè', '2026-10-20'));
    await fixture.whenStable();

    // Dopo il ripristino la pagina ricarica l'elenco.
    http.expectOne((r) => r.method === 'GET' && r.url === '/api/expenses').flush([]);
  });
});
