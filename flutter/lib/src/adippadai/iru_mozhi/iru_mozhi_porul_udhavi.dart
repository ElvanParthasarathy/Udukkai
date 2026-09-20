
/// Helper to unify the bilingual display logic for Porul UI components in Silk
class IruMozhiPorulUdhavi {
  /// Returns the primary display name based on the Silk mudhanmai mozhi.
  static String mudhanmaiPeyar(Map<String, dynamic> mozhiMap, String mudhanmaiLang) {
    return (mozhiMap[mudhanmaiLang] as String?) ?? '';
  }

  /// Returns the secondary display name if bilingual mode is enabled.
  static String thunaiPeyar(Map<String, dynamic> mozhiMap, String thunaiLang, bool isBilingual) {
    if (!isBilingual) return '';
    return (mozhiMap[thunaiLang] as String?) ?? '';
  }
}
