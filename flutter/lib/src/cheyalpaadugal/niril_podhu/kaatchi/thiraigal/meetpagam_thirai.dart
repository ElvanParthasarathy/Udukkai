import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../kalanjiyam/porul_nilaimai.dart';
import '../../kalanjiyam/vaangunar_nilaimai.dart';
import 'koorugal/meetpagam_koorugal.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

// ─────────────────────────────────────────────────────────────────────────────
// MEETPAGAM — Recycle Bin Screen
// ─────────────────────────────────────────────────────────────────────────────
// Mode-aware: shows deleted items for the current mode (Coolie / Silk).
// Currently supports: Products (Porul).
// Future: Merchants (Vaangunar), Invoices (Pattiyal), Receipts (Patrucheettu).

class MeetpagamThirai extends ConsumerStatefulWidget {
  const MeetpagamThirai({super.key});

  @override
  ConsumerState<MeetpagamThirai> createState() => _MeetpagamThiraiState();
}

class _MeetpagamThiraiState extends ConsumerState<MeetpagamThirai> {
  @override
  void initState() {
    super.initState();
    // Auto-purge items older than 30 days on screen open
    Future.microtask(() {
      ref.read(porulKalanjiyamProvider).purgeExpiredPorulgal(days: 30);
      ref.read(vaangunarKalanjiyamProvider).purgeExpiredVaangunargal(days: 30);
    });
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final deletedPorulgalAsync = ref.watch(deletedPorulgalProvider);
    final deletedVaangunargalAsync = ref.watch(deletedVaangunargalProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);

    return ElvanSubpageShell(
      title: K.meetpagam.tr(context, ref),
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      slivers: [
        deletedPorulgalAsync.when(
          loading: () => const SliverFillRemaining(child: Center(child: CupertinoActivityIndicator())),
          error: (e, _) => SliverFillRemaining(child: Center(child: Text('Error: $e'))),
          data: (deletedPorulgal) {
            final deletedVaangunargal = deletedVaangunargalAsync.value ?? [];

            if (deletedPorulgal.isEmpty && deletedVaangunargal.isEmpty) {
              return SliverFillRemaining(
                hasScrollBody: false,
                child: MeetpagamVeliNilai(isDark: isDark)
              );
            }

            return SliverPadding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              sliver: SliverList.list(
                children: [
              // ── 30-day auto-delete info ──
              MeetpagamThanniyakkaNaattam(isDark: isDark),
              const SizedBox(height: 12),

              // ── Section: Deleted Products ──
              if (deletedPorulgal.isNotEmpty) ...[
                MeetpagamPaguthiThalaipu(
                  title: K.azhikkappattaPorulgal.tr(context, ref),
                  count: deletedPorulgal.length,
                  isDark: isDark,
                ),
                const SizedBox(height: 8),
                ...deletedPorulgal.map(
                  (porul) => MeetpagamAzhippuAttai(
                    primaryText: porul.porulPeyar[primaryLang] ?? '',
                    secondaryText: porul.porulPeyar[secondaryLang] ?? '',
                    icon: CupertinoIcons.cube_box,
                    deletedAt: porul.deletedAt,
                    isDark: isDark,
                    onRestore: () =>
                        _restorePorul(context, ref, porul),
                    onPermanentDelete: () =>
                        _permanentDeletePorul(context, ref, porul),
                  ),
                ),
                const SizedBox(height: 16),
              ],

              // ── Section: Deleted Merchants ──
              if (deletedVaangunargal.isNotEmpty) ...[
                MeetpagamPaguthiThalaipu(
                  title: K.azhikkappattaVaangunargal.tr(context, ref),
                  count: deletedVaangunargal.length,
                  isDark: isDark,
                ),
                const SizedBox(height: 8),
                ...deletedVaangunargal.map(
                  (vaangunar) => MeetpagamAzhippuAttai(
                    primaryText: vaangunar.peyar[primaryLang] ?? '',
                    secondaryText: vaangunar.peyar[secondaryLang] ?? '',
                    icon: CupertinoIcons.person,
                    deletedAt: vaangunar.deletedAt,
                    isDark: isDark,
                    onRestore: () =>
                        _restoreVaangunar(context, ref, vaangunar),
                    onPermanentDelete: () =>
                        _permanentDeleteVaangunar(context, ref, vaangunar),
                  ),
                ),
              ],

              const SizedBox(height: 100),
                ],
              ),
            );
          },
        ),
      ],
    );
  }

  void _restorePorul(
      BuildContext context, WidgetRef ref, PorulTharavuru porul) {
    ref.read(porulKalanjiyamProvider).restorePorul(porul.id);
    ref.invalidate(porulgalProvider);
    ref.invalidate(deletedPorulgalProvider);
    ElvanSnackbar.show(
      context,
      K.meeteduppuVetri.tr(context, ref),
      showAboveNavbar: true,
    );
  }

  void _permanentDeletePorul(
      BuildContext context, WidgetRef ref, PorulTharavuru porul) {
    showElvanActionSheet(
      context: context,
      title: K.nirandharaAzhippuUrudhi.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.neekkuPtn.tr(context, ref),
      confirmColor: Colors.red,
      onConfirm: () {
        ref.read(porulKalanjiyamProvider).permanentDeletePorul(porul.id);
        ref.invalidate(deletedPorulgalProvider);
      },
    );
  }

  void _restoreVaangunar(
      BuildContext context, WidgetRef ref, VaangunarTharavuru vaangunar) {
    ref.read(vaangunarKalanjiyamProvider).restoreVaangunar(vaangunar.id);
    ref.invalidate(vaangunargalProvider);
    ref.invalidate(deletedVaangunargalProvider);
    ElvanSnackbar.show(
      context,
      K.meeteduppuVetri.tr(context, ref),
      showAboveNavbar: true,
    );
  }

  void _permanentDeleteVaangunar(
      BuildContext context, WidgetRef ref, VaangunarTharavuru vaangunar) {
    showElvanActionSheet(
      context: context,
      title: K.nirandharaAzhippuUrudhi.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.neekkuPtn.tr(context, ref),
      confirmColor: Colors.red,
      onConfirm: () {
        ref.read(vaangunarKalanjiyamProvider).permanentDeleteVaangunar(vaangunar.id);
        ref.invalidate(deletedVaangunargalProvider);
      },
    );
  }
}
