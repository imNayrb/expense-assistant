import { formatAmount, parseAmount } from './receipt';

describe('importi dello scontrino', () => {
  it("legge gli importi scritti all'italiana e non", () => {
    expect(parseAmount('30')).toBe(30);
    expect(parseAmount('12,5')).toBe(12.5);
    expect(parseAmount('12.50')).toBe(12.5);
    expect(parseAmount('1.250,00 €')).toBe(1250);
  });

  it('rifiuta testi che non sono importi positivi', () => {
    expect(parseAmount('')).toBe(0);
    expect(parseAmount('abc')).toBe(0);
    expect(parseAmount('-5')).toBe(0);
  });

  it('scrive sempre due decimali con la virgola', () => {
    expect(formatAmount(30)).toBe('30,00');
    expect(formatAmount(1250.5)).toBe('1250,50');
    expect(formatAmount(0)).toBe('');
  });
});
