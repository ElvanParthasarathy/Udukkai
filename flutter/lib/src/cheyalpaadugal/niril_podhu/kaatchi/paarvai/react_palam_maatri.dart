import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';

/// ReactPalamMaatri — Flutter → React Bridge Converter
///
/// Converts a [NiruvanaTharavugal] (Drift database model) into the exact
/// flat profile shape that the original standalone React app expects from
/// Avanam.ts → getProfile().
///
/// This ensures the React app receives data in its native format and
/// requires ZERO modifications to the original React codebase.
class ReactPalamMaatri {
  // Language code → Language name mapping
  static const _mozhiPeyar = {'ta': 'Tamil', 'en': 'English'};

  /// Convert a [NiruvanaTharavugal] profile into the React-compatible profile map.
  ///
  /// The returned map can be directly passed through `jsonEncode()` and sent
  /// over the MethodChannel to the WebView. React will receive it as a
  /// perfectly structured Javascript object identical to what Avanam.ts provides.
  static Map<String, dynamic> profileToReact(NiruvanaTharavugal profile) {
    final primary = _mozhiPeyar[profile.mudhanMozhi] ?? 'Tamil';
    final secondary = _mozhiPeyar[profile.thunaiMozhi] ?? 'English';

    return {
      // ── Language Config ──
      'primaryDataLanguage': primary,
      'secondaryDataLanguage': secondary,
      'enableBilingual': profile.iruMozhi,

      // ── Business Name (bilingual, flattened) ──
      ..._flattenMozhi('niruvanathinPeyar', profile.niruvanathinPeyar, primary),
      'shortBusinessName': profile.kurumPeyar,

      // ── Address (bilingual, flattened) ──
      ..._flattenMozhi('mugavari', profile.mugavari, primary),
      ..._flattenMozhi('oor', profile.oor, primary),
      ..._flattenMozhi('maavattam', profile.maavattam, primary),
      ..._flattenMozhi('maanilam', profile.maanilam, primary),
      ..._flattenMozhi('country', profile.naadu, primary),
      'pin': profile.anjalKuriyeedu,

      // ── Contact (plain strings) ──
      'tholaipesi': profile.tholaipaesi1,
      'mobileNumber': profile.tholaipaesi2,
      'email': profile.minnanjal,
      'gstin': profile.gstin,

      // ── Bank Details (bilingual + plain) ──
      // React uses 'vangiPeyar' for bank name (same key)
      ..._flattenMozhi('vangiPeyar', profile.vangiPeyar, primary),
      // React uses 'bankBranch' instead of Flutter's 'kilai'
      ..._flattenMozhi('bankBranch', profile.kilai, primary),
      // React uses 'kanakkuEn' instead of Flutter's 'vangiKanakku'
      'kanakkuEn': profile.vangiKanakku,
      'ifsc': profile.ifsc,

      // ── Branding (plain strings, already base64 from PaarvaiUdhavi) ──
      // React uses 'logo' instead of Flutter's 'oavuru'
      'logo': profile.oavuru,
      // React uses 'wideLogo' instead of Flutter's 'agalaOavuru'
      'wideLogo': profile.agalaOavuru,
      // React uses 'billHeaderStyle' instead of Flutter's 'thalaippuVadivu'
      'billHeaderStyle': profile.thalaippuVadivu,
      // React uses 'signature' instead of Flutter's 'kaiyoppam'
      'signature': profile.kaiyoppam,
      // React uses 'authorizedSignatoryName' instead of Flutter's 'oppamPeyar'
      'authorizedSignatoryName': profile.oppamPeyar,

      // ── Additional ──
      ..._flattenMozhi('terms', profile.adaimozhi, primary),
      'upiId': profile.upiId,
      // React uses 'themeColor' instead of Flutter's 'thoatraNiram'
      'themeColor': profile.thoatraNiram,
      'defaultPrintLanguage': profile.mudhanMozhi,
      'receiptLanguage': profile.mudhanMozhi,
      'logoHeight': 120,
      'wideLogoX': 0,
      'wideLogoY': 0,
      'wideLogoScale': 1,
      'pan': profile.gstin.length >= 12 ? profile.gstin.substring(2, 12) : '',
      'tholaipaesi1': profile.tholaipaesi1,
      'tholaipaesi2': profile.tholaipaesi2,
      'minnanjal': profile.minnanjal,
      'country': profile.naadu['en'] ?? profile.naadu['ta'] ?? '',
      'country_English': profile.naadu['en'] ?? '',
    };
  }

  /// Flatten a bilingual map `{ta: "...", en: "..."}` into the React format:
  /// ```
  /// {
  ///   "fieldName": "primary language value",
  ///   "fieldName_Tamil": "...",
  ///   "fieldName_English": "...",
  /// }
  /// ```
  static Map<String, dynamic> _flattenMozhi(
    String fieldName,
    Map<String, String> map,
    String primaryLang,
  ) {
    final ta = map['ta'] ?? '';
    final en = map['en'] ?? '';
    // Base value is the primary language value
    final baseValue = primaryLang == 'Tamil' ? ta : en;

    return {
      fieldName: baseValue.isNotEmpty ? baseValue : (ta.isNotEmpty ? ta : en),
      '${fieldName}_Tamil': ta,
      '${fieldName}_English': en,
    };
  }
}
