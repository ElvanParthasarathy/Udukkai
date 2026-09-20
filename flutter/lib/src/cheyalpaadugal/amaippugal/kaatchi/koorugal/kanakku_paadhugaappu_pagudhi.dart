import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'elvan_amaippu_pagudhi.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../../koorugal/maeladukkugal/elvan_aetrum_maeladukku.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../../../../adippadai/viruppangal_paniyagam.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/thoatra_vazhanguthi.dart';
import '../../../../adippadai/vazhikaattal/navigation_provider.dart';
import '../../tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../../tharavu/kooli_niruvana_tharavugal_provider.dart';

import '../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/patru_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';

class AccountSecuritySection extends ConsumerWidget {
  const AccountSecuritySection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final iconBgColor = theme.colorScheme.onSurface.withValues(alpha: 0.05);

    return ElvanSettingsSection(
      children: [



        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.arrow_2_circlepath,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.orunginaiCheyaliPtn.tr(context, ref),
          description: K.orunginaiCheyaliVilakkam.tr(context, ref),
          onTap: () {
            _showSyncFlow(context, ref);
          },
        ),
        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.square_arrow_right_fill,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.veliyaeru.tr(context, ref),
          description: K.cheyaliyilirundhuVeliyaeravum.tr(context, ref),
          onTap: () {
            _showSignOutFlow(context, ref);
          },
        ),
        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.delete_solid,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.cheyalithTharavaiAzhi.tr(context, ref),
          description: K.tharavaiazhi.tr(context, ref),
          onTap: () {
            _showEraseDataFlow(context, ref);
          },
        ),

      ],
    );
  }



  void _showSignOutFlow(BuildContext context, WidgetRef ref) {
    showElvanActionSheet(
      context: context,
      title: K.veliyaeraVaendumaa.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.veliyaeruPtn.tr(context, ref),
      onConfirm: () {
        // Mock Sign Out
        showElvanLoadingOverlay(
            context: context, text: K.veliyaerugiradhu.tr(context, ref));
        Future.delayed(const Duration(seconds: 1), () {
          if (context.mounted) {
            final successMsg = K.veliyaetramvetri.tr(context, ref);
            
            // Show snackbar first before we unmount this context
            ElvanSnackbar.show(context, successMsg);
            
            // Update global state
            ref.read(appModeProvider.notifier).setMode(null);
            ref.read(isLoggedInProvider.notifier).setLoggedIn(false);

            // Instantly wipe in-memory settings so UI snaps back to default
            ref.invalidate(themeModeProvider);
            ref.invalidate(localeProvider);
            ref.invalidate(nirilNavigationProvider);

            // Pop all dialogs and screens back to the root route.
            Navigator.of(context, rootNavigator: true)
                .popUntil((route) => route.isFirst);
          }
        });
      },
    );
  }












  void _showEraseDataFlow(BuildContext context, WidgetRef ref) {
    // Step 1: Email Confirmation
    showElvanActionSheet(
      context: context,
      title: K.cheyalithTharavaiAzhi.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.thodaravum.tr(context, ref),
      customContent: ElvanTextField(
        textAlign: TextAlign.center,
        decoration: InputDecoration(
          hintText: K.minnanjalaiUrudhiseiga.tr(context, ref),
          hintStyle: TextStyle(
            fontSize: 14.0,
            color:
                Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.4),
          ),
          filled: true,
          fillColor: WidgetStateColor.resolveWith((states) {
            if (states.contains(WidgetState.focused)) {
              return Theme.of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.12);
            }
            return Theme.of(context)
                .colorScheme
                .onSurface
                .withValues(alpha: 0.08);
          }),
          contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
          constraints: const BoxConstraints(minHeight: 45),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(100),
            borderSide: BorderSide.none,
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(100),
            borderSide: BorderSide.none,
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(100),
            borderSide: BorderSide.none,
          ),
        ),
        style: TextStyle(
          fontSize: 14.0,
          color: Theme.of(context).colorScheme.onSurface,
        ),
      ),
      onConfirm: () {
        // Step 2: Password Confirmation
        showElvanActionSheet(
          context: context,
          title: K.kadavuchollaiUllidavum.tr(context, ref),
          cancelText: K.kaividuPtn.tr(context, ref),
          confirmText: K.tharavaiAzhiPtn.tr(context, ref),
          customContent: ElvanTextField(
            textAlign: TextAlign.center,
            obscureText: true,
            decoration: InputDecoration(
              hintText: K.kadavuchol.tr(context, ref),
              hintStyle: TextStyle(
                fontSize: 14.0,
                color: Theme.of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.4),
              ),
              filled: true,
              fillColor: WidgetStateColor.resolveWith((states) {
                if (states.contains(WidgetState.focused)) {
                  return Theme.of(context)
                      .colorScheme
                      .onSurface
                      .withValues(alpha: 0.12);
                }
                return Theme.of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.08);
              }),
              contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              constraints: const BoxConstraints(minHeight: 45),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(100),
                borderSide: BorderSide.none,
              ),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(100),
                borderSide: BorderSide.none,
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(100),
                borderSide: BorderSide.none,
              ),
            ),
            style: TextStyle(
              fontSize: 14.0,
              color: Theme.of(context).colorScheme.onSurface,
            ),
          ),
          onConfirm: () async {
            // Step 3: Show Loading Animation
            showElvanLoadingOverlay(
                context: context, text: K.azhikkiradhu.tr(context, ref));

            // Simulate erasing delay
            await Future.delayed(const Duration(seconds: 2));

            if (context.mounted) {
              // We do not manually pop dialogs here, as that triggers overlapping
              // pop animations that crash the Navigator (!_debugLocked).
              // pushAndRemoveUntil below will cleanly destroy all dialogs instantly.

              // Wipe BOTH databases by switching modes and deleting all tables
              
              // 1. Wipe Silk Database
              final pattuDb = ref.read(pattuDatabaseProvider);
              await pattuDb.delete(pattuDb.pattuNiruvanaTharavugalTable).go();
              await pattuDb.delete(pattuDb.pattuPorulTable).go();
              await pattuDb.delete(pattuDb.pattuVaangunarTable).go();
              await pattuDb.delete(pattuDb.pattuPattiyalTable).go();
              await pattuDb.delete(pattuDb.pattuPatrugalTable).go();
              await pattuDb.delete(pattuDb.pattuPatruPattiyalTable).go();

              // 2. Wipe Coolie Database
              final kooliDb = ref.read(kooliDatabaseProvider);
              await kooliDb.delete(kooliDb.kooliNiruvanaTharavugalTable).go();
              await kooliDb.delete(kooliDb.kooliPorulTable).go();
              await kooliDb.delete(kooliDb.kooliVaangunarTable).go();
              await kooliDb.delete(kooliDb.kooliPattiyalTable).go();
              await kooliDb.delete(kooliDb.kooliPatrugalTable).go();
              await kooliDb.delete(kooliDb.kooliPatruPattiyalTable).go();

              // Clear SharedPreferences
              final prefs = ref.read(sharedPreferencesProvider);
              await prefs.clear();

              if (context.mounted) {
                final successMsg = K.azhippuvetri.tr(context, ref);

                // Show snackbar first before we unmount this context
                ElvanSnackbar.show(context, successMsg);

                // Mock fresh install redirect by resetting mode and auth
                ref.read(appModeProvider.notifier).setMode(null);
                ref.read(isLoggedInProvider.notifier).setLoggedIn(false);

                // Instantly wipe in-memory settings so UI snaps back to default
                ref.invalidate(pattuNiruvanaTharavugalListProvider);
                ref.invalidate(kooliNiruvanaTharavugalListProvider);
                ref.invalidate(themeModeProvider);
                ref.invalidate(localeProvider);
                ref.invalidate(skipRestoreProvider);
                ref.invalidate(nirilNavigationProvider);

                // Pop all dialogs and screens back to the root route.
                Navigator.of(context, rootNavigator: true)
                    .popUntil((route) => route.isFirst);
              }
            }
          },
        );
      },
    );
  }

  void _showSyncFlow(BuildContext context, WidgetRef ref) {
    showElvanLoadingOverlay(
      context: context,
      text: K.orunginaikkiRadhu.tr(context, ref),
    );

    // Invalidate all data providers to force a fresh DB read
    ref.invalidate(pattiyalgalProvider);
    ref.invalidate(patrugalProvider);
    ref.invalidate(porulgalProvider);
    ref.invalidate(vaangunargalProvider);
    ref.invalidate(pattuNiruvanaTharavugalListProvider);

    // Small delay for UX so it doesn't flash instantly
    Future.delayed(const Duration(milliseconds: 800), () {
      if (context.mounted) {
        Navigator.of(context, rootNavigator: true).pop();
        ElvanSnackbar.show(
          context,
          K.orunginaikkappattadhu.tr(context, ref),
        );
      }
    });
  }

}
