
/// Helper to unify the monolingual display logic for Porul UI components in Kooli
class OruMozhiPorulUdhavi {
  /// Returns the primary display name based on the Kooli achu mozhi.
  static String mudhanmaiPeyar(Map<String, dynamic> mozhiMap, String kooliLang) {
    return (mozhiMap[kooliLang] as String?) ?? '';
  }

  /// Returns the secondary display name based on the first available alternative in mozhiMap.
  static String thunaiPeyar(Map<String, dynamic> mozhiMap, String kooliLang) {
    final otherKeys = mozhiMap.keys.where((k) => k != kooliLang).toList();
    return otherKeys.isNotEmpty ? (mozhiMap[otherKeys.first] as String?) ?? '' : '';
  }
}
