import {
  Bus,
  CircleEllipsis,
  Clapperboard,
  Fuel,
  GraduationCap,
  HeartPulse,
  House,
  LucideIconData,
  Plane,
  Repeat,
  ShoppingBag,
  ShoppingCart,
  UtensilsCrossed,
  Zap,
} from 'lucide-angular';

export interface CategoryStyle {
  icon: LucideIconData;
  color: string;
}

/** Icona e colore di ogni categoria: rendono la lista leggibile a colpo d'occhio. */
const STYLES: Record<string, CategoryStyle> = {
  GROCERIES: { icon: ShoppingCart, color: '#34c38f' },
  RESTAURANTS: { icon: UtensilsCrossed, color: '#ff8a4c' },
  TRANSPORT: { icon: Bus, color: '#4ca6ff' },
  FUEL: { icon: Fuel, color: '#ffc145' },
  HOUSING: { icon: House, color: '#a78bfa' },
  UTILITIES: { icon: Zap, color: '#2dd4bf' },
  HEALTH: { icon: HeartPulse, color: '#ff6b8b' },
  ENTERTAINMENT: { icon: Clapperboard, color: '#f472b6' },
  SHOPPING: { icon: ShoppingBag, color: '#e879f9' },
  TRAVEL: { icon: Plane, color: '#38bdf8' },
  EDUCATION: { icon: GraduationCap, color: '#818cf8' },
  SUBSCRIPTIONS: { icon: Repeat, color: '#c084fc' },
  OTHER: { icon: CircleEllipsis, color: '#94a3b8' },
};

export function categoryStyle(code: string): CategoryStyle {
  return STYLES[code] ?? STYLES['OTHER'];
}
