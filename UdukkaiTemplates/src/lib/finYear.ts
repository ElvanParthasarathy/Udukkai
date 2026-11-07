/** "26-27" for a date in April 2026 to March 2027. Takes "YYYY-MM-DD" or "DD/MM/YYYY"; '' when it is neither. */
export const fyLabel = (date: unknown): string => {
  const s = String(date ?? '');
  const iso = /^(\d{4})-(\d{2})/.exec(s);
  const dmy = /^\d{2}\/(\d{2})\/(\d{4})/.exec(s);
  const year = iso ? Number(iso[1]) : dmy ? Number(dmy[2]) : NaN;
  const month = iso ? Number(iso[2]) : dmy ? Number(dmy[1]) : NaN;
  if (!year || !month) return '';
  const start = month >= 4 ? year : year - 1;
  return `${String(start).slice(2)}-${String(start + 1).slice(2)}`;
};

/** "2026-27", for the line that names the financial year in full. */
export const fyFull = (date: unknown): string => {
  const short = fyLabel(date);
  return short ? `20${short.slice(0, 2)}-${short.slice(3)}` : '';
};

/** Normalizes any financial year representation ("2026", "2026-27", "26-27", or empty) to "2026-27". */
export const normalizeFy = (fy: unknown, date?: unknown): string => {
  const s = String(fy ?? '').trim();
  if (/^\d{4}-\d{2}$/.test(s)) return s;
  if (/^\d{2}-\d{2}$/.test(s)) return `20${s}`;
  const y = Number(s);
  if (!isNaN(y) && y >= 1900 && y <= 2100) {
    const end = (y + 1) % 100;
    return `${y}-${end < 10 ? '0' : ''}${end}`;
  }
  return date ? fyFull(date) : '';
};
