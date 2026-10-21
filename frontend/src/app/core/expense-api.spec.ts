import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ExpenseApi, errorMessage } from './expense-api';

describe('ExpenseApi', () => {
  let api: ExpenseApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(ExpenseApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('passa intervallo e categoria come parametri', () => {
    api.list({ from: '2026-10-01', to: '2026-10-31' }, 'FUEL').subscribe();

    const req = http.expectOne((r) => r.url === '/api/expenses');
    expect(req.request.params.get('from')).toBe('2026-10-01');
    expect(req.request.params.get('to')).toBe('2026-10-31');
    expect(req.request.params.get('category')).toBe('FUEL');
    req.flush([]);
  });

  it('non invia la categoria quando non è filtrata', () => {
    api.list({ from: '2026-10-01', to: '2026-10-31' }, null).subscribe();

    const req = http.expectOne((r) => r.url === '/api/expenses');
    expect(req.request.params.has('category')).toBe(false);
    req.flush([]);
  });

  it("invia il testo da interpretare all'assistente", () => {
    api.parse('ieri 30€ di benzina').subscribe();

    const req = http.expectOne('/api/assistant/parse');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ text: 'ieri 30€ di benzina' });
    req.flush({});
  });
});

describe('errorMessage', () => {
  it('usa il campo detail dei problem details', () => {
    const error = new HttpErrorResponse({ status: 422, error: { detail: 'Importo mancante' } });
    expect(errorMessage(error)).toBe('Importo mancante');
  });

  it('spiega quando il backend non risponde', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('non risponde');
  });

  it('ha un messaggio generico per gli errori sconosciuti', () => {
    expect(errorMessage(new Error('boom'))).toBe('Qualcosa è andato storto. Riprova.');
  });
});
