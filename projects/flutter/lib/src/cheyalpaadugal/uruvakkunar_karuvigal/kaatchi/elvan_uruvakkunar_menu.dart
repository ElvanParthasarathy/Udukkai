import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';


import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

import '../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../tharavu/sodhanai_tharavu_uruvakki.dart';
import '../tharavu/sodhanai_tharavugal.dart';
import 'elvan_uruvakkunar_nira_kaatti.dart';
import '../../../adippadai/vazhikaattal/niril_nav.dart';

/// A global floating developer menu that only appears in Debug mode.
class ElvanUruvakkunarMenu extends ConsumerStatefulWidget {
  final Widget child;

  const ElvanUruvakkunarMenu({
    super.key,
    required this.child,
  });

  @override
  ConsumerState<ElvanUruvakkunarMenu> createState() => _ElvanUruvakkunarMenuState();
}

class _ElvanUruvakkunarMenuState extends ConsumerState<ElvanUruvakkunarMenu> {
  bool _isExpanded = false;
  Offset _position = const Offset(20, 100);

  void _seedAllData() async {
    // ── Step 0: Erase all existing data across both modes ──
    await SodhanaiTharavuUruvakki.eraseData(ref);

    // ── Step 1: Seed Silk Data ──
    ref.read(appModeProvider.notifier).setMode(AppMode.silk);
    // Yield to let providers rebuild
    await Future.delayed(const Duration(milliseconds: 100));

    await SodhanaiTharavuUruvakki.seedData(ref, AppMode.silk);
    await SodhanaiTharavuUruvakki.seedPorulAndVaangunar(ref, mode: AppMode.silk);
    await SodhanaiTharavuUruvakki.seedPattiyalgal(ref, mode: AppMode.silk);
    await SodhanaiTharavuUruvakki.seedPatrugal(ref, mode: AppMode.silk);

    // ── Step 2: Seed Coolie Data ──
    ref.read(appModeProvider.notifier).setMode(AppMode.coolie);
    // Yield to let providers rebuild
    await Future.delayed(const Duration(milliseconds: 100));

    await SodhanaiTharavuUruvakki.seedData(ref, AppMode.coolie);
    await SodhanaiTharavuUruvakki.seedPorulAndVaangunar(ref, mode: AppMode.coolie);
    await SodhanaiTharavuUruvakki.seedPattiyalgal(ref, mode: AppMode.coolie);
    await SodhanaiTharavuUruvakki.seedPatrugal(ref, mode: AppMode.coolie);

    if (mounted) {
      ref.read(appModeProvider.notifier).setMode(AppMode.silk);
      ref.read(isLoggedInProvider.notifier).setLoggedIn(true);
      ElvanSnackbar.show(context, 'All Data Seeded Successfully ✓');
    }
  }

  void _eraseAllData() async {
    await SodhanaiTharavuUruvakki.eraseData(ref);
    ref.read(appModeProvider.notifier).setMode(null);
    if (mounted) {
      Navigator.popUntil(context, (route) => route.isFirst);
    }
  }

  /// Toggle extra Silk profile (EPS) — add if missing, remove if exists.
  void _toggleExtraSilk() async {
    final currentMode = ref.read(appModeProvider);
    ref.read(appModeProvider.notifier).setMode(AppMode.silk);
    final notifier = ref.read(niruvanaTharavugalNotifierProvider);
    final profiles = ref.read(NiruvanaTharavugalListProvider);

    // Check if EPS already exists
    final existing = profiles.where((p) => p.kurumPeyar == 'EPS').firstOrNull;
    if (existing != null) {
      // Hide it instead of deleting
      await notifier.hideProfile(existing.id!);
      if (currentMode != null) {
        ref.read(appModeProvider.notifier).setMode(currentMode);
      }
      if (mounted) ElvanSnackbar.show(context, 'Silk EPS Removed ✗');
    } else {
      // Try to restore it first, if not found then add it
      final restored = await notifier.restoreProfile('EPS');
      if (!restored) {
        final data = mockSilkProfiles.length > 1 ? mockSilkProfiles[1] : null;
        if (data != null) {
          await notifier.createProfile(_profileFromMap(data));
        }
      }
      if (currentMode != null) {
        ref.read(appModeProvider.notifier).setMode(currentMode);
      }
      if (mounted) ElvanSnackbar.show(context, 'Silk EPS Added ✓');
    }
  }

  void _toggleLanguage() {
    final currentLocale = ref.read(localeProvider);
    if (currentLocale?.languageCode == 'ta' && currentLocale?.scriptCode != 'Latn') {
      ref.read(localeProvider.notifier).setLocale(const Locale('en'));
      if (mounted) ElvanSnackbar.show(context, 'Language: English');
    } else if (currentLocale?.languageCode == 'en') {
      ref.read(localeProvider.notifier).setLocale(const Locale.fromSubtags(languageCode: 'ta', scriptCode: 'Latn'));
      if (mounted) ElvanSnackbar.show(context, 'Language: Tanglish');
    } else {
      ref.read(localeProvider.notifier).setLocale(const Locale('ta'));
      if (mounted) ElvanSnackbar.show(context, 'மொழி: தமிழ்');
    }
  }

  void _toggleBilingual() {
    final profile = ref.read(NiruvanaTharavugalProvider);
    if (profile == null) return;
    
    final newProfile = profile.copyWith(iruMozhi: !profile.iruMozhi);
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(newProfile);
    
    if (mounted) {
      ElvanSnackbar.show(context, newProfile.iruMozhi ? 'Bilingual Mode: ON ✓' : 'Bilingual Mode: OFF ✗');
    }
  }

  void _swapDataLanguages() {
    final profile = ref.read(NiruvanaTharavugalProvider);
    if (profile == null) return;

    final currentPrimary = profile.mudhanMozhi;
    final currentSecondary = profile.thunaiMozhi;

    final newProfile = profile.copyWith(
      mudhanMozhi: currentSecondary,
      thunaiMozhi: currentPrimary,
    );

    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(newProfile);

    if (mounted) {
      ElvanSnackbar.show(
        context,
        'Swapped: $currentSecondary ↔️ $currentPrimary',
      );
    }
  }

  /// Toggle extra Coolie profile (PVS) — add if missing, remove if exists.
  void _toggleCoolieSingle() async {
    final currentMode = ref.read(appModeProvider);
    ref.read(appModeProvider.notifier).setMode(AppMode.coolie);
    final notifier = ref.read(niruvanaTharavugalNotifierProvider);
    final profiles = ref.read(NiruvanaTharavugalListProvider);

    // Check if PVS exists
    final pvs = profiles.where((p) => p.kurumPeyar == 'PVS').firstOrNull;
    if (pvs != null) {
      // Hide PVS → single profile mode
      await notifier.hideProfile(pvs.id!);
      if (currentMode != null) {
        ref.read(appModeProvider.notifier).setMode(currentMode);
      }
      if (mounted) ElvanSnackbar.show(context, 'Coolie PVS Removed (1 biz) ✗');
    } else {
      // Try to restore it first, if not found then add it
      final restored = await notifier.restoreProfile('PVS');
      if (!restored) {
        final data = mockCoolieProfiles.length > 1 ? mockCoolieProfiles[1] : null;
        if (data != null) {
          await notifier.createProfile(_profileFromMap(data));
        }
      }
      if (currentMode != null) {
        ref.read(appModeProvider.notifier).setMode(currentMode);
      }
      if (mounted) ElvanSnackbar.show(context, 'Coolie PVS Added (2 biz) ✓');
    }
  }

  /// Builds a NiruvanaTharavugal from a mock map.
  NiruvanaTharavugal _profileFromMap(Map<String, dynamic> d) {
    return NiruvanaTharavugal(
      mudhanMozhi: d['mudhanMozhi'] ?? 'ta',
      thunaiMozhi: d['thunaiMozhi'] ?? 'en',
      iruMozhi: d['iruMozhi'] ?? true,
      niruvanathinPeyar: {
        'ta': d['niruvanathinPeyar_ta'] ?? '',
        'en': d['niruvanathinPeyar_en'] ?? '',
      },
      kurumPeyar: d['kurumPeyar'] ?? '',
      tholaipaesi1: d['tholaipesi_1'] ?? '',
      tholaipaesi2: d['tholaipesi_2'] ?? '',
      minnanjal: d['minnanjal'] ?? '',
      gstin: d['gstin'] ?? '',
      mugavari: {'ta': d['mugavari_ta'] ?? '', 'en': d['mugavari_en'] ?? ''},
      oor: {'ta': d['oor_ta'] ?? '', 'en': d['oor_en'] ?? ''},
      maavattam: {'ta': d['maavattam_ta'] ?? '', 'en': d['maavattam_en'] ?? ''},
      maanilam: {'ta': d['maanilam_ta'] ?? '', 'en': d['maanilam_en'] ?? ''},
      naadu: {'ta': d['naadu_ta'] ?? '', 'en': d['naadu_en'] ?? ''},
      anjalKuriyeedu: d['anjalKuriyeedu'] ?? '',
      vangiPeyar: {'ta': d['vangiPeyar_ta'] ?? '', 'en': d['vangiPeyar_en'] ?? ''},
      kilai: {'ta': d['kilai_ta'] ?? '', 'en': d['kilai_en'] ?? ''},
      vangiKanakku: d['vangiKanakku'] ?? '',
      ifsc: d['ifsc'] ?? '',
      oavuru: d['oavuru'] ?? '',
      agalaOavuru: d['agalaOavuru'] ?? '',
      thalaippuVadivu: d['thalaippuVadivu'] ?? 'small',
      kaiyoppam: d['kaiyoppam'] ?? '',
      oppamPeyar: d['oppamPeyar'] ?? '',
      adaimozhi: {'ta': d['adaimozhi_ta'] ?? '', 'en': d['adaimozhi_en'] ?? ''},
      upiId: d['upiId'] ?? '',
      thoatraNiram: d['thoatraNiram'] ?? '',
    );
  }

  Widget _buildCompactAction({
    required String label,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Icon(icon, color: color, size: 20),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                label,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: Theme.of(context).colorScheme.onSurface,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // If not in debug mode, simply return the app
    if (!kDebugMode) return widget.child;

    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Stack(
      children: [
        widget.child,
        Positioned(
          left: _position.dx,
          top: _position.dy,
          child: GestureDetector(
            onPanUpdate: (details) {
              setState(() {
                _position += details.delta;
              });
            },
            child: Material(
              color: Colors.transparent,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  FloatingActionButton(
                    heroTag: 'dev_menu_toggle',
                    mini: true,
                    backgroundColor: _isExpanded ? Colors.redAccent : Colors.black87,
                    elevation: 4,
                    onPressed: () {
                      setState(() {
                        _isExpanded = !_isExpanded;
                      });
                    },
                    child: Icon(
                      _isExpanded ? CupertinoIcons.clear : Icons.developer_mode, 
                      color: Colors.white,
                      size: 20,
                    ),
                  ),
                  if (_isExpanded) ...[
                    const SizedBox(height: 12),
                    Material(
                      color: Colors.transparent,
                      elevation: 8,
                      shadowColor: Colors.black.withValues(alpha: 0.2),
                      borderRadius: BorderRadius.circular(16),
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: Container(
                          width: 220,
                          decoration: BoxDecoration(
                            color: isDark ? const Color(0xFF1A1A1A) : Colors.white,
                            border: Border.all(
                              color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.1),
                            ),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              _buildCompactAction(
                                label: 'Seed All',
                                icon: CupertinoIcons.rocket,
                                color: Colors.green,
                                onTap: _seedAllData,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Erase Data',
                                icon: CupertinoIcons.trash,
                                color: Colors.red,
                                onTap: _eraseAllData,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Toggle Silk ±EPS',
                                icon: CupertinoIcons.repeat,
                                color: Colors.deepPurple,
                                onTap: _toggleExtraSilk,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Toggle Coolie ±PVS',
                                icon: CupertinoIcons.repeat,
                                color: Colors.teal,
                                onTap: _toggleCoolieSingle,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Toggle UI Lang',
                                icon: Icons.language,
                                color: Colors.blueAccent,
                                onTap: _toggleLanguage,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Toggle Bilingual',
                                icon: Icons.translate,
                                color: Colors.orange,
                                onTap: _toggleBilingual,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Swap Data Langs',
                                icon: CupertinoIcons.arrow_right_arrow_left,
                                color: Colors.pink,
                                onTap: _swapDataLanguages,
                              ),
                              Divider(height: 1, thickness: 1, color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05)),
                              _buildCompactAction(
                                label: 'Color Analyzer',
                                icon: Icons.color_lens,
                                color: Colors.amber,
                                onTap: () {
                                  setState(() => _isExpanded = false);
                                  final navContext = globalRootNavigatorKey.currentContext;
                                  if (navContext != null) {
                                    ElvanUruvakkunarNiraKaatti.show(navContext);
                                  }
                                },
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}
