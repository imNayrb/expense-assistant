import { currentMonth, monthLabel, monthRange, sameMonth, shiftMonth, toIsoDate } from './dates';

describe('dates', () => {
  it('calcola primo e ultimo giorno del mese, anche a febbraio bisestile', () => {
    expect(monthRange({ year: 2026, month: 10 })).toEqual({ from: '2026-10-01', to: '2026-10-31' });
    expect(monthRange({ year: 2028, month: 2 })).toEqual({ from: '2028-02-01', to: '2028-02-29' });
  });

  it('sposta i mesi attraversando il cambio di anno', () => {
    expect(shiftMonth({ year: 2026, month: 1 }, -1)).toEqual({ year: 2025, month: 12 });
    expect(shiftMonth({ year: 2026, month: 12 }, 1)).toEqual({ year: 2027, month: 1 });
    expect(shiftMonth({ year: 2026, month: 10 }, -13)).toEqual({ year: 2025, month: 9 });
  });

  it('usa la data locale e non UTC', () => {
    expect(toIsoDate(new Date(2026, 9, 21, 0, 30))).toBe('2026-10-21');
  });

  it('riconosce il mese corrente e scrive il nome in italiano', () => {
    const now = new Date(2026, 9, 21);
    expect(sameMonth(currentMonth(now), { year: 2026, month: 10 })).toBe(true);
    expect(monthLabel({ year: 2026, month: 10 })).toBe('ottobre 2026');
  });
});
