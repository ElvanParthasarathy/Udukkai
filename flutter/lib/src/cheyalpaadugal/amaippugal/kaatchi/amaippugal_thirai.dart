import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/cupertino.dart';
import 'dart:io';

import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../adippadai/vazhikaattal/niril_nav.dart';
import '../../../adippadai/panigal/seyali_oaviyangal.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import 'thiraigal/niruvana_amaippugal_thirai.dart';
import 'thiraigal/mugavari_thirai.dart';
import 'thiraigal/vangi_thirai.dart';
import 'thiraigal/kooli_niruvana_adaiyalangal_thirai.dart';
import 'thiraigal/pattu_niruvana_adaiyalangal_thirai.dart';
import 'thiraigal/uruvaakki_patri_thirai.dart';
import 'thiraigal/uruvakku_amaippugal_thirai.dart';
import 'thiraigal/kaatchi_amaippugal_thirai.dart';
import 'thiraigal/mozhi_amaippugal_thirai.dart';
import 'koorugal/elvan_amaippu_pagudhi.dart';
import 'thiraigal/paadhugaappu_amaippugal_thirai.dart';
import 'thiraigal/chaemippu_matrum_kaappu_thirai.dart';
import 'thiraigal/seyali_patri_thirai.dart';
import 'thiraigal/payanar_amaippugal_thirai.dart';

import '../../chattagam/kaatchi/kanini/elvan_kanini_utpakkach_chattagam.dart';
import '../../ulnuzhaivu/kaatchi/muraimai_thaervu_thirai.dart';

class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  Widget? _selectedDetail;

  void _onMenuSelected(Widget detailPage) {
    setState(() {
      _selectedDetail = detailPage;
    });
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final isWideDesktop = constraints.maxWidth >= 800;

        if (isWideDesktop) {
          final isDark = Theme.of(context).brightness == Brightness.dark;
          final bgColor =
              isDark ? const Color(0xFF000000) : const Color(0xFFF3F4F6);

          return Scaffold(
            backgroundColor: bgColor,
            body: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Left Panel (Hub)
                Expanded(
                  child: ElvanSubpagePadding(
                    padding: const EdgeInsets.only(left: 24, right: 16),
                    child: SettingsHubScreen(onPageSelected: _onMenuSelected),
                  ),
                ),

                // Right Panel (Detail)
                Expanded(
                  child: ElvanSubpagePadding(
                    padding: const EdgeInsets.only(left: 24, right: 24),
                    child: _selectedDetail ?? const NiruvanaAmaippugalPage(),
                  ),
                ),
              ],
            ),
          );
        }
        // Mobile / Narrow layout — renders with ElvanSubpageShell back button
        return const SettingsHubScreen();
      },
    );
  }
}

class SettingsHubScreen extends ConsumerWidget {
  final void Function(Widget page)? onPageSelected;

  const SettingsHubScreen({super.key, this.onPageSelected});

  void _navigateTo(BuildContext context, Widget page) {
    if (onPageSelected != null) {
      onPageSelected!(page);
    } else {
      NirilNav.push(context, page);
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentAppMode = ref.watch(appModeProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final iconBgColor = isDark
        ? Colors.white.withValues(alpha: 0.08)
        : Colors.black.withValues(alpha: 0.06);

    return ElvanSubpageShell(
      title: K.amaippugal.tr(context, ref),
      startCollapsed: true,
      hideHeaderOnDesktop: true,
      backgroundColor:
          isDark ? const Color(0xFF000000) : const Color(0xFFF3F4F6),
      slivers: [
        SliverList.list(
          children: [
            // ── Mode Switcher & Merchant Settings (Big Pill) ──
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElvanSettingsSection(
                borderRadius: 999.0,
                children: [
                  Stack(
                    children: [
                      // The main pill body (Navigates to Merchant Settings)
                      InkWell(
                        onTap: () {
                          _navigateTo(context, const NiruvanaAmaippugalPage());
                        },
                        child: Padding(
                          padding: const EdgeInsets.only(
                              left: 98, right: 32, top: 14, bottom: 14),
                          child: SizedBox(
                            width: double.infinity,
                            height: 64,
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text(
                                  K.niruvanaAmaippugal.tr(context, ref),
                                  style: const TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w600,
                                    height: 1.2,
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  ref.watch(appModeProvider) == AppMode.coolie
                                      ? K.nirilKooli.tr(context, ref)
                                      : K.nirilPattu.tr(context, ref),
                                  style: TextStyle(
                                    fontSize: 14,
                                    fontWeight: FontWeight.w600,
                                    color: Theme.of(context)
                                        .colorScheme
                                        .onSurface
                                        .withValues(alpha: 0.5),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),

                      // The circular Mode Switcher on the left
                      Positioned(
                        left: 14,
                        top: 14,
                        child: Material(
                          color: iconBgColor,
                          shape: const CircleBorder(),
                          clipBehavior: Clip.hardEdge,
                          child: InkWell(
                            onTap: () {
                              Navigator.of(context, rootNavigator: true).push(
                                PageRouteBuilder(
                                  pageBuilder: (context, animation,
                                          secondaryAnimation) =>
                                      ModeSelectorScreen(
                                    onModeSelected: (mode) {
                                      ref
                                          .read(appModeProvider.notifier)
                                          .setMode(mode);
                                      Navigator.of(context, rootNavigator: true).pop();
                                    },
                                  ),
                                  transitionsBuilder: (context, animation,
                                      secondaryAnimation, child) {
                                    return FadeTransition(
                                        opacity: animation, child: child);
                                  },
                                  fullscreenDialog: true,
                                  transitionDuration:
                                      const Duration(milliseconds: 300),
                                ),
                              );
                            },
                            child: SizedBox(
                              width: 64,
                              height: 64,
                              child: Center(
                                child: SvgPicture.string(
                                  ref.watch(appModeProvider) == AppMode.coolie
                                      ? AppSvgs.coolieMode
                                      : AppSvgs.silkMode,
                                  width: 32,
                                  height: 32,
                                  colorFilter: ColorFilter.mode(
                                    Theme.of(context).colorScheme.onSurface,
                                    BlendMode.srcIn,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // ── Section 1: Business Profile & Identity ──
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElvanSettingsSection(
                children: [
                  if (currentAppMode == AppMode.coolie)
                    ElvanSettingsRow(
                      icon: CupertinoIcons.briefcase_fill,
                      iconBgColor: iconBgColor,
                      title: K.adaiyaalam.tr(context, ref),
                      description: K.kooliNiruvanaAdaiyaalangal.tr(context, ref),
                      onTap: () => _navigateTo(
                          context, const CoolieNiruvanaAdaiyalangalPage()),
                    )
                  else
                    ElvanSettingsRow(
                      icon: CupertinoIcons.briefcase_fill,
                      iconBgColor: iconBgColor,
                      title: K.adaiyaalam.tr(context, ref),
                      description: K.pattuNiruvanaAdaiyaalangal.tr(context, ref),
                      onTap: () => _navigateTo(
                          context, const SilkNiruvanaAdaiyalangalPage()),
                    ),
                  ElvanSettingsRow(
                    icon: CupertinoIcons.location_solid,
                    iconBgColor: iconBgColor,
                    title: K.mugavari.tr(context, ref),
                    description: K.mugavaritharavugal.tr(context, ref),
                    onTap: () => _navigateTo(context, const MugavariPage()),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // ── Section 2: Finance & Creation ──
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElvanSettingsSection(
                children: [
                  ElvanSettingsRow(
                    icon: CupertinoIcons.creditcard_fill,
                    iconBgColor: iconBgColor,
                    title: K.vangi.tr(context, ref),
                    description: K.kanakkuEnIfsc.tr(context, ref),
                    onTap: () => _navigateTo(context, const VangiPage()),
                  ),
                  ElvanSettingsRow(
                    icon: CupertinoIcons.doc_text_fill,
                    iconBgColor: iconBgColor,
                    title: K.uruvaakkuPtn.tr(context, ref),
                    description: K.pilVadivamaippuViruppangal.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const UruvakkuAmaippugalPage()),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // ── Section 3: User & Display ──
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElvanSettingsSection(
                children: [
                  ElvanSettingsRow(
                    icon: CupertinoIcons.person_fill,
                    iconBgColor: iconBgColor,
                    title: K.payanar.tr(context, ref),
                    description: K.payanarAmaippugal.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const PayanarAmaippugalPage()),
                  ),
                  if (!Platform.isWindows)
                    ElvanSettingsRow(
                      icon: CupertinoIcons.sun_max_fill,
                      iconBgColor: iconBgColor,
                      title: K.thirai.tr(context, ref),
                      description: K.olirIrulThaaniyangu.tr(context, ref),
                      onTap: () =>
                          _navigateTo(context, const DisplaySettingsPage()),
                    ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // ── Section 4: Language & Security ──
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElvanSettingsSection(
                children: [
                  ElvanSettingsRow(
                    icon: Icons.language,
                    iconBgColor: iconBgColor,
                    title: K.cheyaliMozhi.tr(context, ref),
                    description: K.thamizhAangilamThaaniyangu.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const LanguageSettingsPage()),
                  ),
                                    ElvanSettingsRow(
                    icon: CupertinoIcons.folder_solid,
                    iconBgColor: iconBgColor,
                    title: K.chaemippuMatrumKaappu.tr(context, ref),
                    description: K.tharavuthalangalKaappaikKaiyaalu.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const ChaemippuMatrumKaappuThirai()),
                  ),
ElvanSettingsRow(
                    icon: CupertinoIcons.lock_fill,
                    iconBgColor: iconBgColor,
                    title: K.paadhugaappu.tr(context, ref),
                    description: K.thaekkagathaiazhi.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const PathugappuAmaippugalPage()),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // ── Section 5: System & About ──
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: ElvanSettingsSection(
                children: [
                  ElvanSettingsRow(
                    icon: CupertinoIcons.info_circle_fill,
                    iconBgColor: iconBgColor,
                    title: K.menporulVadivaalar.tr(context, ref),
                    description: K.elvanPatriMaelumAriga.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const AboutDeveloperPage()),
                  ),
                  ElvanSettingsRow(
                    icon: Icons.smartphone_rounded,
                    iconBgColor: iconBgColor,
                    title: K.cheyaliPatri.tr(context, ref),
                    description: K.cheyalipadhippu.tr(context, ref),
                    onTap: () =>
                        _navigateTo(context, const SeyaliPatriPage()),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 48),
          ],
        ),
      ],
    );
  }
}
