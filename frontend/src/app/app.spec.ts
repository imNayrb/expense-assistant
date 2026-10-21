import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { UiState } from './core/ui-state';

describe('App', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('mostra le quattro sezioni nella tab bar', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const tabs = [...fixture.nativeElement.querySelectorAll('.tabbar a')].map((a: Element) =>
      a.textContent!.trim(),
    );
    expect(tabs).toEqual(['Panoramica', 'Movimenti', 'Statistiche', 'Assistente']);
  });

  it('il pulsante "+" apre il pannello di inserimento', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    fixture.nativeElement.querySelector('.fab').click();
    expect(TestBed.inject(UiState).sheetOpen()).toBe(true);
  });
});
