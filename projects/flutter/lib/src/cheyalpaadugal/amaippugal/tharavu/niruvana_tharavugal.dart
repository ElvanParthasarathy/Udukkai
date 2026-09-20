/// நிறுவனத் தரவுகள் — Business profile data model.
library;

/// நிறுவனத் தரவுகள் — Business profile data model.
///
/// This is the domain model used by UI code. It maps to/from
/// the Drift-generated `NiruvanaTharavugalEntry` for database storage.
///
/// All bilingual fields are stored as `Map<String, String>` where the key is
/// the language name (e.g., 'ta', 'English'). This allows easy scaling to
/// new languages without changing the model.
class NiruvanaTharavugal {
  // ── Database ID (for multi-profile support) ──
  int? id; // Database row ID — null for unsaved profiles

  // ── Language Config (மொழி அமைப்பு) ──
  String mudhanMozhi; // முதன் மொழி — Primary data language
  String thunaiMozhi; // துணை மொழி — Secondary data language
  bool iruMozhi; // இரு மொழி — Bilingual enabled
  bool gstPirippugal; // GST பிரிப்புகள் — GST Split enabled

  // ── Business Details (நிறுவனத் தரவு) ──
  Map<String, String> niruvanathinPeyar; // நிறுவனத்தின் பெயர் — Business name
  String kurumPeyar; // குறும்பெயர் — Short business name
  String tholaipaesi1; // தொலைபேசி 1
  String tholaipaesi2; // தொலைபேசி 2
  String minnanjal; // மின்னஞ்சல் — Email
  String gstin; // GSTIN (acronym, silk only)

  // ── Address (முகவரி) ──
  Map<String, String> mugavari; // முகவரி — Street address
  Map<String, String> oor; // ஊர் — City
  Map<String, String> maavattam; // மாவட்டம் — District
  Map<String, String> maanilam; // மாநிலம் — State (silk only)
  Map<String, String> naadu; // நாடு — Country (silk only)
  String anjalKuriyeedu; // அஞ்சல் குறியீடு — Pincode

  // ── Bank Details (வங்கி) ──
  Map<String, String> vangiPeyar; // வங்கிப் பெயர் — Bank name
  Map<String, String> kilai; // கிளை — Branch
  String vangiKanakku; // வங்கிக் கணக்கு — Account number
  String ifsc; // IFSC (acronym)

  // ── Branding (அடையாளங்கள்) ──
  String oavuru; // ஓவுரு — Business logo
  String agalaOavuru; // அகல ஓவுரு — Wide logo (silk only)
  String thalaippuVadivu; // தலைப்பு வடிவு — Header style (silk only)
  String kaiyoppam; // கையொப்பம் — Signature image
  String oppamPeyar; // ஒப்பம் பெயர் — Signatory name

  // ── Additional ──
  Map<String, String> adaimozhi; // அடைமொழி — Tagline (silk only)
  String upiId; // UPI (acronym, silk only)
  String thoatraNiram; // தோற்ற நிறம் — Theme color (coolie only)

  NiruvanaTharavugal({
    this.id,
    this.mudhanMozhi = 'ta',
    this.thunaiMozhi = 'en',
    this.iruMozhi = true,
    this.gstPirippugal = false,
    Map<String, String>? niruvanathinPeyar,
    this.kurumPeyar = '',
    this.tholaipaesi1 = '',
    this.tholaipaesi2 = '',
    this.minnanjal = '',
    this.gstin = '',
    Map<String, String>? mugavari,
    Map<String, String>? oor,
    Map<String, String>? maavattam,
    Map<String, String>? maanilam,
    Map<String, String>? naadu,
    this.anjalKuriyeedu = '',
    Map<String, String>? vangiPeyar,
    Map<String, String>? kilai,
    this.vangiKanakku = '',
    this.ifsc = '',
    this.oavuru = '',
    this.agalaOavuru = '',
    this.thalaippuVadivu = 'small',
    this.kaiyoppam = '',
    this.oppamPeyar = '',
    Map<String, String>? adaimozhi,
    this.upiId = '',
    this.thoatraNiram = '',
  })  : niruvanathinPeyar = niruvanathinPeyar ?? {},
        mugavari = mugavari ?? {},
        oor = oor ?? {},
        maavattam = maavattam ?? {},
        maanilam = maanilam ?? {},
        naadu = {'en': 'India', 'ta': 'இந்தியா'},
        vangiPeyar = vangiPeyar ?? {},
        kilai = kilai ?? {},
        adaimozhi = adaimozhi ?? {};

  // ── Helper: Get the primary language value of a bilingual field ──
  String getPrimary(String fieldName) {
    if (fieldName == 'naadu') return mudhanMozhi.toLowerCase().startsWith('ta') ? 'இந்தியா' : 'India'; // LOCKED
    final map = _getBilingualMap(fieldName);
    return map[mudhanMozhi] ?? map['ta'] ?? '';
  }

  // ── Helper: Get the secondary language value of a bilingual field ──
  String getSecondary(String fieldName) {
    if (fieldName == 'naadu') return thunaiMozhi.toLowerCase().startsWith('ta') ? 'இந்தியா' : 'India'; // LOCKED
    final map = _getBilingualMap(fieldName);
    return map[thunaiMozhi] ?? map['en'] ?? '';
  }

  // ── Helper: Set a bilingual field value for a specific language ──
  void setBilingual(String fieldName, String language, String value) {
    if (fieldName == 'naadu') return; // LOCKED
    final map = _getBilingualMap(fieldName);
    map[language] = value;
  }

  Map<String, String> _getBilingualMap(String fieldName) {
    switch (fieldName) {
      case 'niruvanathinPeyar':
        return niruvanathinPeyar;
      case 'mugavari':
        return mugavari;
      case 'oor':
        return oor;
      case 'maavattam':
        return maavattam;
      case 'maanilam':
        return maanilam;
      case 'naadu':
        return naadu;
      case 'vangiPeyar':
        return vangiPeyar;
      case 'kilai':
        return kilai;
      case 'adaimozhi':
        return adaimozhi;
      default:
        return {};
    }
  }

  // ── Supported languages for language tag resolution ──
  static const List<String> supportedLanguages = [
    'Tamil',
    'English',
  ];

  /// Creates a deep copy.
  NiruvanaTharavugal copyWith({
    int? id,
    String? mudhanMozhi,
    String? thunaiMozhi,
    bool? iruMozhi,
    bool? gstPirippugal,
    Map<String, String>? niruvanathinPeyar,
    String? kurumPeyar,
    String? tholaipaesi1,
    String? tholaipaesi2,
    String? minnanjal,
    String? gstin,
    Map<String, String>? mugavari,
    Map<String, String>? oor,
    Map<String, String>? maavattam,
    Map<String, String>? maanilam,
    Map<String, String>? naadu,
    String? anjalKuriyeedu,
    Map<String, String>? vangiPeyar,
    Map<String, String>? kilai,
    String? vangiKanakku,
    String? ifsc,
    String? oavuru,
    String? agalaOavuru,
    String? thalaippuVadivu,
    String? kaiyoppam,
    String? oppamPeyar,
    Map<String, String>? adaimozhi,
    String? upiId,
    String? thoatraNiram,
  }) {
    return NiruvanaTharavugal(
      id: id ?? this.id,
      mudhanMozhi: mudhanMozhi ?? this.mudhanMozhi,
      thunaiMozhi: thunaiMozhi ?? this.thunaiMozhi,
      iruMozhi: iruMozhi ?? this.iruMozhi,
      gstPirippugal: gstPirippugal ?? this.gstPirippugal,
      niruvanathinPeyar:
          niruvanathinPeyar ?? Map<String, String>.from(this.niruvanathinPeyar),
      kurumPeyar: kurumPeyar ?? this.kurumPeyar,
      tholaipaesi1: tholaipaesi1 ?? this.tholaipaesi1,
      tholaipaesi2: tholaipaesi2 ?? this.tholaipaesi2,
      minnanjal: minnanjal ?? this.minnanjal,
      gstin: gstin ?? this.gstin,
      mugavari: mugavari ?? Map<String, String>.from(this.mugavari),
      oor: oor ?? Map<String, String>.from(this.oor),
      maavattam: maavattam ?? Map<String, String>.from(this.maavattam),
      maanilam: maanilam ?? Map<String, String>.from(this.maanilam),
      naadu: {'en': 'India', 'ta': 'இந்தியா'}, // LOCKED
      anjalKuriyeedu: anjalKuriyeedu ?? this.anjalKuriyeedu,
      vangiPeyar: vangiPeyar ?? Map<String, String>.from(this.vangiPeyar),
      kilai: kilai ?? Map<String, String>.from(this.kilai),
      vangiKanakku: vangiKanakku ?? this.vangiKanakku,
      ifsc: ifsc ?? this.ifsc,
      oavuru: oavuru ?? this.oavuru,
      agalaOavuru: agalaOavuru ?? this.agalaOavuru,
      thalaippuVadivu: thalaippuVadivu ?? this.thalaippuVadivu,
      kaiyoppam: kaiyoppam ?? this.kaiyoppam,
      oppamPeyar: oppamPeyar ?? this.oppamPeyar,
      adaimozhi: adaimozhi ?? Map<String, String>.from(this.adaimozhi),
      upiId: upiId ?? this.upiId,
      thoatraNiram: thoatraNiram ?? this.thoatraNiram,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'mudhanMozhi': mudhanMozhi,
      'thunaiMozhi': thunaiMozhi,
      'iruMozhi': iruMozhi,
      'gstPirippugal': gstPirippugal,
      'niruvanathinPeyar': niruvanathinPeyar,
      'kurumPeyar': kurumPeyar,
      'tholaipaesi1': tholaipaesi1,
      'tholaipaesi2': tholaipaesi2,
      'minnanjal': minnanjal,
      'gstin': gstin,
      'mugavari': mugavari,
      'oor': oor,
      'maavattam': maavattam,
      'maanilam': maanilam,
      'naadu': naadu,
      'anjalKuriyeedu': anjalKuriyeedu,
      'vangiPeyar': vangiPeyar,
      'kilai': kilai,
      'vangiKanakku': vangiKanakku,
      'ifsc': ifsc,
      'oavuru': oavuru,
      'agalaOavuru': agalaOavuru,
      'thalaippuVadivu': thalaippuVadivu,
      'kaiyoppam': kaiyoppam,
      'oppamPeyar': oppamPeyar,
      'adaimozhi': adaimozhi,
      'upiId': upiId,
      'thoatraNiram': thoatraNiram,
    };
  }
}
