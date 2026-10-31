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
