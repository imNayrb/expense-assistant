import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  it('mostra le quattro sezioni nella navigazione', async () => {
    TestBed.configureTestingModule({ imports: [App], providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const links = [...fixture.nativeElement.querySelectorAll('nav a')].map((a) =>
      a.textContent.trim(),
    );
    expect(links).toEqual(['Aggiungi', 'Spese', 'Riepilogo', 'Chiedi']);
  });
});
