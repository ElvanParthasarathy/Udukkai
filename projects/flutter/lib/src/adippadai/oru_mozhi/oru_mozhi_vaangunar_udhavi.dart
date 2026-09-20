import '../tharavuru/uruvugal.dart';

/// Helper to unify the monolingual display logic for Vaangunar (Customer) UI components in Kooli
class OruMozhiVaangunarUdhavi {
  /// Returns the primary display name based on the Kooli achu mozhi.
  static String mudhanmaiPeyar(VaangunarTharavuru v, String kooliAchuMozhi) {
    if (v.peyar[kooliAchuMozhi]?.isNotEmpty == true) {
      return v.peyar[kooliAchuMozhi];
    }
    if (v.peyar['ta']?.isNotEmpty == true) {
      return v.peyar['ta'];
    }
    return v.peyar.values.firstOrNull ?? '';
  }

  /// Returns the primary display name from a raw Map (useful when pulling from snapshot).
  static String mudhanmaiPeyarFromMap(Map<String, dynamic> peyar, String kooliAchuMozhi) {
    if (peyar[kooliAchuMozhi]?.toString().isNotEmpty == true) {
      return peyar[kooliAchuMozhi].toString();
    }
    if (peyar['ta']?.toString().isNotEmpty == true) {
      return peyar['ta'].toString();
    }
    return peyar.values.firstOrNull?.toString() ?? '';
  }

  /// Returns the secondary display name based on the first available alternative.
  static String thunaiPeyar(VaangunarTharavuru v, String kooliAchuMozhi) {
    final otherKeys = v.peyar.keys.where((k) => k != kooliAchuMozhi).toList();
    if (otherKeys.isNotEmpty) {
      for (final key in otherKeys) {
        if (v.peyar[key]?.isNotEmpty == true) {
          return v.peyar[key];
        }
      }
    }
    return '';
  }

  /// Returns the secondary display name from a raw Map.
  static String thunaiPeyarFromMap(Map<String, dynamic> peyar, String kooliAchuMozhi) {
    final otherKeys = peyar.keys.where((k) => k != kooliAchuMozhi).toList();
    if (otherKeys.isNotEmpty) {
      for (final key in otherKeys) {
        if (peyar[key]?.toString().isNotEmpty == true) {
          return peyar[key].toString();
        }
      }
    }
    return '';
  }
}
