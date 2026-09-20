import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_muzhuthirai_maeladukku.dart';
import '../../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_pagudhi.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_thirutha_attai.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_azhippu_urudhi_maeladukku.dart';
import '../../../../../koorugal/ulleedugal/elvan_ulleedu.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../../../koorugal/maeladukkugal/elvan_kizh_maeladukku.dart';

/// Maximum number of business profiles allowed.
const int maxProfiles = 5;



/// Shows the "Manage Profiles" full-screen dialog — lists all profiles
/// with delete and switch-active actions.
void showManageProfilesModal({
  required BuildContext context,
  required WidgetRef ref,
  required VoidCallback onNewProfile,
  required String Function(NiruvanaTharavugal, WidgetRef) primaryNameBuilder,
  String Function(NiruvanaTharavugal, WidgetRef)? secondaryNameBuilder,
}) {
  showGeneralDialog(
    context: context,
    barrierDismissible: true,
    barrierLabel: 'Manage Profiles',
    transitionDuration: const Duration(milliseconds: 300),
    pageBuilder: (context, animation, secondaryAnimation) {
      return StatefulBuilder(
        builder: (context, setModalState) {
          return Dialog.fullscreen(
            child: Scaffold(
              backgroundColor: Colors.transparent,
              floatingActionButton: Consumer(builder: (context, ref, _) {
                final profiles = ref.watch(NiruvanaTharavugalListProvider);
                if (profiles.length >= maxProfiles) return const SizedBox.shrink();
                return Padding(
                  padding: const EdgeInsets.only(bottom: 48.0),
                  child: FloatingActionButton(
                    onPressed: onNewProfile,
                    backgroundColor: Theme.of(context).colorScheme.onSurface,
                    foregroundColor: Theme.of(context).colorScheme.surface,
                    child: const Icon(Icons.add),
                  ),
                );
              }),
              body: Consumer(builder: (context, ref, child) {
                final profiles = ref.watch(NiruvanaTharavugalListProvider);
                final activeProfile = ref.watch(NiruvanaTharavugalProvider);
                final hasProfiles = profiles.isNotEmpty;

                return ElvanFullscreenPopup(
                  title: K.kaiyaalu.tr(context, ref),
                  slivers: [
                    SliverPadding(
                      padding: const EdgeInsets.all(16),
                      sliver: SliverToBoxAdapter(
                        child: !hasProfiles
                            ? Center(
                                child: Padding(
                                  padding: const EdgeInsets.all(32.0),
                                  child: Text(
                                    K.thannurukkalIllai.tr(context, ref),
                                    style: TextStyle(
                                      color: Theme.of(context)
                                          .colorScheme
                                          .onSurface
                                          .withValues(alpha: 0.5),
                                    ),
                                  ),
                                ),
                              )
                            : Column(
                                children: [
                                  ElvanSettingsSection(
                                    children: [
                                      for (final profile in profiles)
                                        ElvanSettingsDisplayRow(
                                          title: profile.id == activeProfile?.id
                                              ? K.tharpoadhaiyaNiruvanam.tr(context, ref)
                                              : '',
                                          primaryValue: primaryNameBuilder(profile, ref).isEmpty
                                              ? K.tharpoadhaiyaNiruvanam.tr(context, ref)
                                              : primaryNameBuilder(profile, ref),
                                          secondaryValue: secondaryNameBuilder != null && secondaryNameBuilder(profile, ref).isNotEmpty 
                                              ? secondaryNameBuilder(profile, ref) 
                                              : null,
                                          icon: CupertinoIcons.delete_solid,
                                          onTap: profile.id != activeProfile?.id
                                              ? () {
                                                  ref.read(niruvanaTharavugalNotifierProvider)
                                                      .setActiveProfile(profile.id!);
                                                }
                                              : null,
                                          onEdit: () =>
                                              showElvanDeleteConfirmModal(
                                                  context, ref, () {
                                            ref
                                                .read(niruvanaTharavugalNotifierProvider)
                                                .deleteProfile(profile.id!);
                                            ElvanSnackbar.show(
                                                context,
                                                K.thannuruNeekkappattadhu
                                                    .tr(context, ref));
                                          }),
                                        ),
                                    ],
                                  ),
                                ],
                              ),
                      ),
                    ),
                  ],
                );
              }),
            ),
          );
        },
      );
    },
    transitionBuilder: (context, animation, secondaryAnimation, child) {
      const begin = Offset(0.0, 1.0);
      const end = Offset.zero;
      const curve = Curves.easeOutCubic;
      final tween =
          Tween(begin: begin, end: end).chain(CurveTween(curve: curve));
      return SlideTransition(
        position: animation.drive(tween),
        child: child,
      );
    },
  );
}

/// Shows a bottom-sheet action sheet to create a new business profile.
void showNewProfileModal({
  required BuildContext context,
  required WidgetRef ref,
  required VoidCallback onSuccess,
}) {
  String newNamePrimary = '';
  String newNameSecondary = '';
  showElvanActionSheet(
    context: context,
    title: K.pudhiyaThannuruvaiUruvaakku.tr(context, ref),
    cancelText: K.kaividuPtn.tr(context, ref),
    confirmText: K.uruvaakkuPtn.tr(context, ref),
    customContent: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        ElvanTextField(
          textAlign: TextAlign.center,
          onChanged: (val) => newNamePrimary = val,
          decoration: InputDecoration(
            hintText:
                '${K.niruvanathinPeyar.tr(context, ref)} (${K.thamizh.tr(context, ref)})',
            hintStyle: TextStyle(
              fontSize: 13,
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
            fontSize: 14,
            color: Theme.of(context).colorScheme.onSurface,
          ),
        ),
        const SizedBox(height: 12),
        ElvanTextField(
          textAlign: TextAlign.center,
          onChanged: (val) => newNameSecondary = val,
          decoration: InputDecoration(
            hintText:
                '${K.niruvanathinPeyar.tr(context, ref)} (${K.aangilam.tr(context, ref)})',
            hintStyle: TextStyle(
              fontSize: 13,
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
            fontSize: 14,
            color: Theme.of(context).colorScheme.onSurface,
          ),
        ),
      ],
    ),
    onConfirm: () {
      if (newNamePrimary.trim().isNotEmpty) {
        final profiles = ref.read(NiruvanaTharavugalListProvider);
        if (profiles.length >= maxProfiles) {
          ElvanSnackbar.show(context, K.perumalavu5thannuru.tr(context, ref));
          return;
        }

        final newProfile = NiruvanaTharavugal();
        newProfile.setBilingual('niruvanathinPeyar', 'ta', newNamePrimary);
        newProfile.setBilingual(
            'niruvanathinPeyar', 'en', newNameSecondary);
        ref.read(niruvanaTharavugalNotifierProvider).createProfile(newProfile);
        onSuccess();
      }
    },
  );
}
