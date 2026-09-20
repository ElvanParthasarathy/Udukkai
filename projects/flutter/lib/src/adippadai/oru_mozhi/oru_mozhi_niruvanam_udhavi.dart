import '../../cheyalpaadugal/amaippugal/tharavu/niruvana_tharavugal.dart';

/// Helper to unify the monolingual display logic for Niruvanam UI components in Kooli
class OruMozhiNiruvanamUdhavi {
  /// Returns the primary display name based on the Kooli achu mozhi.
  static String mudhanmaiPeyar(NiruvanaTharavugal p, String kooliAchuMozhi) {
    return p.niruvanathinPeyar[kooliAchuMozhi] ??
        p.niruvanathinPeyar['ta'] ??
        p.niruvanathinPeyar.values.firstOrNull ??
        '';
  }

  /// Returns the secondary display name based on the first available alternative in niruvanathinPeyar.
  static String thunaiPeyar(NiruvanaTharavugal p, String kooliAchuMozhi) {
    final otherKeys = p.niruvanathinPeyar.keys.where((k) => k != kooliAchuMozhi).toList();
    return otherKeys.isNotEmpty ? p.niruvanathinPeyar[otherKeys.first] ?? '' : '';
  }
}
