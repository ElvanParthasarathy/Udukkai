import '../../cheyalpaadugal/amaippugal/tharavu/niruvana_tharavugal.dart';

/// Helper to unify the bilingual display logic for Niruvanam UI components
class IruMozhiNiruvanamUdhavi {
  /// Returns the primary display name based on the chosen primary language.
  /// Falls back to secondary language, then Tamil, then whatever is available.
  static String mudhanmaiPeyar(
      NiruvanaTharavugal p, String primaryLang, String secondaryLang) {
    return (p.niruvanathinPeyar[primaryLang]?.isNotEmpty == true
            ? p.niruvanathinPeyar[primaryLang]
            : p.niruvanathinPeyar[secondaryLang]) ??
        p.niruvanathinPeyar['ta'] ??
        p.niruvanathinPeyar.values.firstOrNull ??
        '';
  }

  /// Returns the secondary (subtitle) display name if bilingual mode is enabled
  /// and the app is in Silk mode.
  static String thunaiPeyar(
      NiruvanaTharavugal p, bool isBilingual, bool isSilk, String secondaryLang) {
    if (!isBilingual || !isSilk) return '';
    return p.niruvanathinPeyar[secondaryLang] ??
        p.niruvanathinPeyar['en'] ??
        '';
  }
}
