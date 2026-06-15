import { Severity } from './models';

/** Shared presentation helpers. */

const MXN = new Intl.NumberFormat('es-MX', {
  style: 'currency',
  currency: 'MXN',
});

export function money(amount: number): string {
  return MXN.format(amount);
}

/** Masks the middle of a CLABE for display, mirroring the backend's masked() form. */
export function maskClabe(clabe: string): string {
  if (!clabe || clabe.length < 18) {
    return clabe;
  }
  return `${clabe.slice(0, 3)}***********${clabe.slice(13)}`;
}

export const SEVERITY_COLOR: Record<Severity, string> = {
  LOW: '#3fb950',
  MEDIUM: '#d29922',
  HIGH: '#f0883e',
  CRITICAL: '#f85149',
};

export function scoreColor(score: number): string {
  if (score >= 80) return '#f85149';
  if (score >= 50) return '#f0883e';
  if (score >= 25) return '#d29922';
  return '#3fb950';
}
