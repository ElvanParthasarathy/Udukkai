class FinancialYearUtil {
  /// Returns the Indian Financial Year string for a given date.
  /// Example: If date is 2026-06-23, returns "2026-27".
  /// If date is 2026-02-15, returns "2025-26".
  static String getFinancialYearString(DateTime date) {
    int startYear = date.year;
    if (date.month < 4) {
      startYear -= 1;
    }
    int endYear = (startYear + 1) % 100;
    return '$startYear-${endYear.toString().padLeft(2, '0')}';
  }

  /// Returns the start year integer for the financial year of a given date.
  /// Example: If date is 2026-06-23, returns 2026.
  static int getFinancialYear(DateTime date) {
    if (date.month < 4) {
      return date.year - 1;
    }
    return date.year;
  }
}
