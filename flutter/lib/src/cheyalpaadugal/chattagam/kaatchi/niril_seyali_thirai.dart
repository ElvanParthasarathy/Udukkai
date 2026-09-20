import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/nilaimai/thaedal_nilaimai.dart';
import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../adippadai/vazhikaattal/navigation_destination.dart';
import '../../../adippadai/vazhikaattal/navigation_provider.dart';

import 'mugappu_thirai.dart';
import 'uruvakku_thirai.dart';
import 'porul_thirai.dart';
import 'vaangunar_thirai.dart';

import '../../niril_pattu/kaatchi/thiraigal/pattu_pattiyalgal_thirai.dart';
import '../../niril_pattu/kaatchi/thiraigal/pattu_patrucheettugal_thirai.dart';
import '../../niril_pattu/kaatchi/thiruthi/pattiyal/niril_pattu_pattiyal_thiruthi.dart';
import '../../niril_pattu/kaatchi/thiruthi/patrucheettu/niril_pattu_patrucheettu_thiruthi.dart';
import '../../niril_pattu/kaatchi/thiruthi/vaangunar/niril_pattu_vaangunar_thiruthi.dart';
import '../../niril_pattu/kaatchi/thiruthi/porul/niril_pattu_porul_thiruthi.dart';


import '../../niril_kooli/kaatchi/thiraigal/kooli_pattiyalgal_thirai.dart';
import 'kaippaesi/elvan_selection_overlay_helper.dart';
import '../../niril_kooli/kaatchi/thiraigal/kooli_patrucheettugal_thirai.dart';
import '../../niril_kooli/kaatchi/thiruthi/pattiyal/niril_kooli_pattiyal_thiruthi.dart';
import '../../niril_kooli/kaatchi/thiruthi/patrucheettu/niril_kooli_patrucheettu_thiruthi.dart';
import '../../niril_kooli/kaatchi/thiruthi/vaangunar/niril_kooli_vaangunar_thiruthi.dart';
import '../../niril_kooli/kaatchi/thiruthi/porul/niril_kooli_porul_thiruthi.dart';

import '../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../niril_podhu/kalanjiyam/patru_nilaimai.dart';

import '../../amaippugal/kaatchi/amaippugal_thirai.dart';
import '../../amaippugal/kaatchi/koorugal/chaemippu_matrum_kaappu_pagudhi.dart';

import 'kaippaesi/elvan_chattagam.dart';
import 'kaippaesi/koorugal/elvan_maeladukku_pattiyal.dart';
import 'kaippaesi/koorugal/elvan_mel_pattai_chinnam.dart';
import 'kanini/elvan_kanini_chattagam.dart';
import 'kanini/elvan_kanini_karuvipattai.dart';
import '../../../adippadai/vazhikaattal/niril_nav.dart';

// ─────────────────────────────────────────────────────────────────────────────
// NIRIL APP SCREEN — The main app dashboard
// ─────────────────────────────────────────────────────────────────────────────
// Uses [NirilNavigationNotifier] as the single source of truth.
// Both mobile and desktop layouts read from the same state.

class NirilSeyaliThirai extends ConsumerStatefulWidget {
  const NirilSeyaliThirai({super.key});

  @override
  ConsumerState<NirilSeyaliThirai> createState() => _NirilSeyaliThiraiState();
}

class _NirilSeyaliThiraiState extends ConsumerState<NirilSeyaliThirai> {
  // ── Navigation helpers ─────────────────────────────────────────────

  void _onAddPressed() {
    final navState = ref.read(nirilNavigationProvider);
    final mode = ref.read(appModeProvider);
    final dest = navState.destination;
    Widget? editor;

    if (dest == NirilDestination.mugappu ||
        dest == NirilDestination.pattiyal) {
      editor = mode == AppMode.coolie
          ? const CoolieInvoiceEditor()
          : const SilkInvoiceEditor();
    } else if (dest == NirilDestination.raseethu) {
      editor = mode == AppMode.coolie
          ? const CoolieReceiptEditor()
          : const SilkReceiptEditor();
    } else if (dest == NirilDestination.vaangunar) {
      editor = mode == AppMode.coolie
          ? const CoolieMerchantEditor()
          : const SilkMerchantEditor();
    } else if (dest == NirilDestination.porul) {
      editor = mode == AppMode.coolie
          ? const CoolieItemEditor()
          : const SilkItemEditor();
    }

    if (editor != null) {
      NirilNav.push(context, editor);
    }
  }

  void _onSelectTapped() {
    final dest = ref.read(nirilNavigationProvider).destination;
    if (dest == NirilDestination.vaangunar) {
      ref.read(vaangunarSelectionModeProvider.notifier).state = true;
      ref.read(selectedVaangunarIdsProvider.notifier).state = {};
    } else {
      ref.read(porulSelectionModeProvider.notifier).state = true;
      ref.read(selectedPorulIdsProvider.notifier).state = {};
    }
  }

  // ── Mobile nav items (4 tabs) ──────────────────────────────────────

  List<CustomNavItem> get _mobileNavItems => [
        CustomNavItem(
          svgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M219.31,108.68l-80-80a16,16,0,0,0-22.62,0l-80,80A15.87,15.87,0,0,0,32,120v96a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V160h32v56a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V120A15.87,15.87,0,0,0,219.31,108.68ZM208,208H160V152a8,8,0,0,0-8-8H104a8,8,0,0,0-8,8v56H48V120l80-80,80,80Z"></path></svg>',
          activeSvgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M224,120v96a8,8,0,0,1-8,8H160a8,8,0,0,1-8-8V164a4,4,0,0,0-4-4H108a4,4,0,0,0-4,4v52a8,8,0,0,1-8,8H40a8,8,0,0,1-8-8V120a16,16,0,0,1,4.69-11.31l80-80a16,16,0,0,1,22.62,0l80,80A16,16,0,0,1,224,120Z"></path></svg>',
          label: K.mugappu.tr(context, ref),
          headerLabel: K.niril.tr(context, ref),
        ),
        CustomNavItem(
          icon: CupertinoIcons.plus_app,
          activeIcon: CupertinoIcons.plus_app_fill,
          label: K.aakku.tr(context, ref),
          headerLabel: K.uruvaakkuPtn.tr(context, ref),
        ),
        CustomNavItem(
          icon: CupertinoIcons.cube_box,
          activeIcon: CupertinoIcons.cube_box_fill,
          label: K.porul.tr(context, ref),
          headerLabel: K.porutkal.tr(context, ref),
        ),
        CustomNavItem(
          svgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M117.25,157.92a60,60,0,1,0-66.5,0A95.83,95.83,0,0,0,3.53,195.63a8,8,0,1,0,13.4,8.74,80,80,0,0,1,134.14,0,8,8,0,0,0,13.4-8.74A95.83,95.83,0,0,0,117.25,157.92ZM40,108a44,44,0,1,1,44,44A44.05,44.05,0,0,1,40,108Zm210.14,98.7a8,8,0,0,1-11.07-2.33A79.83,79.83,0,0,0,172,168a8,8,0,0,1,0-16,44,44,0,1,0-16.34-84.87,8,8,0,1,1-5.94-14.85,60,60,0,0,1,55.53,105.64,95.83,95.83,0,0,1,47.22,37.71A8,8,0,0,1,250.14,206.7Z"></path></svg>',
          activeSvgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M164.47,195.63a8,8,0,0,1-6.7,12.37H10.23a8,8,0,0,1-6.7-12.37,95.83,95.83,0,0,1,47.22-37.71,60,60,0,1,1,66.5,0A95.83,95.83,0,0,1,164.47,195.63Zm87.91-.15a95.87,95.87,0,0,0-47.13-37.56A60,60,0,0,0,144.7,54.59a4,4,0,0,0-1.33,6A75.83,75.83,0,0,1,147,150.53a4,4,0,0,0,1.07,5.53,112.32,112.32,0,0,1,29.85,30.83,23.92,23.92,0,0,1,3.65,16.47,4,4,0,0,0,3.95,4.64h60.3a8,8,0,0,0,7.73-5.93A8.22,8.22,0,0,0,252.38,195.48Z"></path></svg>',
          label: K.vaangunar.tr(context, ref),
          headerLabel: K.vaangunargal.tr(context, ref),
        ),
      ];

  // ── Desktop nav items (6 tabs) ─────────────────────────────────────

  List<CustomNavItem> _desktopNavItems(BuildContext context) => [
        CustomNavItem(
          svgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M219.31,108.68l-80-80a16,16,0,0,0-22.62,0l-80,80A15.87,15.87,0,0,0,32,120v96a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V160h32v56a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V120A15.87,15.87,0,0,0,219.31,108.68ZM208,208H160V152a8,8,0,0,0-8-8H104a8,8,0,0,0-8,8v56H48V120l80-80,80,80Z"></path></svg>',
          activeSvgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M224,120v96a8,8,0,0,1-8,8H160a8,8,0,0,1-8-8V164a4,4,0,0,0-4-4H108a4,4,0,0,0-4,4v52a8,8,0,0,1-8,8H40a8,8,0,0,1-8-8V120a16,16,0,0,1,4.69-11.31l80-80a16,16,0,0,1,22.62,0l80,80A16,16,0,0,1,224,120Z"></path></svg>',
          label: K.mugappu.tr(context, ref),
          headerLabel: K.niril.tr(context, ref),
        ),
        CustomNavItem(
          icon: CupertinoIcons.doc_text,
          activeIcon: CupertinoIcons.doc_text_fill,
          label: K.pattiyal.tr(context, ref),
          headerLabel: K.pattiyalgal.tr(context, ref),
        ),
        CustomNavItem(
          svgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M72,104a8,8,0,0,1,8-8h96a8,8,0,0,1,0,16H80A8,8,0,0,1,72,104Zm8,40h96a8,8,0,0,0,0-16H80a8,8,0,0,0,0,16ZM232,56V208a8,8,0,0,1-11.58,7.15L192,200.94l-28.42,14.21a8,8,0,0,1-7.16,0L128,200.94,99.58,215.15a8,8,0,0,1-7.16,0L64,200.94,35.58,215.15A8,8,0,0,1,24,208V56A16,16,0,0,1,40,40H216A16,16,0,0,1,232,56Zm-16,0H40V195.06l20.42-10.22a8,8,0,0,1,7.16,0L96,199.06l28.42-14.22a8,8,0,0,1,7.16,0L160,199.06l28.42-14.22a8,8,0,0,1,7.16,0L216,195.06Z"></path></svg>',
          activeSvgString:
              '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M216,40H40A16,16,0,0,0,24,56V208a8,8,0,0,0,11.58,7.15L64,200.94l28.42,14.21a8,8,0,0,0,7.16,0L128,200.94l28.42,14.21a8,8,0,0,0,7.16,0L192,200.94l28.42,14.21A8,8,0,0,0,232,208V56A16,16,0,0,0,216,40ZM176,144H80a8,8,0,0,1,0-16h96a8,8,0,0,1,0,16Zm0-32H80a8,8,0,0,1,0-16h96a8,8,0,0,1,0,16Z"></path></svg>',
          label: K.patrucheettu.tr(context, ref),
          headerLabel: K.patrucheettugal.tr(context, ref),
        ),
        _mobileNavItems[2], // Products
        _mobileNavItems[3], // Merchants
      ];

  // ── Search routing ─────────────────────────────────────────────────

  void _routeSearchQuery(String query, NirilDestination dest) {
    final mode = ref.read(appModeProvider);
    switch (dest) {
      case NirilDestination.pattiyal:
        if (mode == AppMode.coolie) {
          ref.read(coolieInvoicesSearchQueryProvider.notifier).state = query;
        } else {
          ref.read(silkInvoicesSearchQueryProvider.notifier).state = query;
        }
        break;
      case NirilDestination.raseethu:
        if (mode == AppMode.coolie) {
          ref.read(coolieReceiptsSearchQueryProvider.notifier).state = query;
        } else {
          ref.read(silkReceiptsSearchQueryProvider.notifier).state = query;
        }
        break;
      case NirilDestination.vaangunar:
        if (mode == AppMode.coolie) {
          ref.read(coolieMerchantsSearchQueryProvider.notifier).state = query;
        } else {
          ref.read(silkMerchantsSearchQueryProvider.notifier).state = query;
        }
        break;
      case NirilDestination.porul:
        if (mode == AppMode.coolie) {
          ref.read(coolieItemsSearchQueryProvider.notifier).state = query;
        } else {
          ref.read(silkItemsSearchQueryProvider.notifier).state = query;
        }
        break;
      default:
        break;
    }
  }

  // ── Mobile search routing via tab index ─────────────────────────────

  void _routeMobileSearch(String query, int tabIndex) {
    final navState = ref.read(nirilNavigationProvider);
    switch (tabIndex) {
      case 1: // Uruvakku tab — depends on segment
        if (navState.uruvakkuSegment == 1) {
          _routeSearchQuery(query, NirilDestination.raseethu);
        } else {
          _routeSearchQuery(query, NirilDestination.pattiyal);
        }
        break;
      case 2:
        _routeSearchQuery(query, NirilDestination.porul);
        break;
      case 3:
        _routeSearchQuery(query, NirilDestination.vaangunar);
        break;
    }
  }

  // ── Build ──────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    // Eagerly load storageStatsProvider in the background so it's instantly ready in Settings -> Storage
    ref.listen(storageStatsProvider, (_, __) {});

    final navState = ref.watch(nirilNavigationProvider);
    final nav = ref.read(nirilNavigationProvider.notifier);
    final mode = ref.watch(appModeProvider);

    return PopScope(
      canPop: navState.destination == NirilDestination.mugappu,
      onPopInvokedWithResult: (didPop, _) {
        if (didPop) return;

        // If selection mode is active, intercept back to clear selection
        final isPattiyalSelecting = ref.read(pattiyalSelectionModeProvider);
        final isPatruSelecting = ref.read(patruSelectionModeProvider);
        final isPorulSelecting = ref.read(porulSelectionModeProvider);
        final isVaangunarSelecting = ref.read(vaangunarSelectionModeProvider);

        if (isPattiyalSelecting || isPatruSelecting || isPorulSelecting || isVaangunarSelecting) {
          ref.read(pattiyalSelectionModeProvider.notifier).state = false;
          ref.read(patruSelectionModeProvider.notifier).state = false;
          ref.read(porulSelectionModeProvider.notifier).state = false;
          ref.read(vaangunarSelectionModeProvider.notifier).state = false;
          
          ref.read(selectedPattiyalIdsProvider.notifier).state = {};
          ref.read(selectedPatruIdsProvider.notifier).state = {};
          ref.read(selectedPorulIdsProvider.notifier).state = {};
          ref.read(selectedVaangunarIdsProvider.notifier).state = {};
          return; // Exit selection mode without navigating back
        }

        nav.goBack();
      },
      child: LayoutBuilder(
        builder: (context, constraints) {
          // On desktop OS, always use desktop layout.
          // LayoutBuilder breakpoint only matters for tablets.
          final isDesktop = isDesktopLayout(constraints.maxWidth);

          if (isDesktop) {
            return _buildDesktopLayout(context, navState, mode);
          } else {
            return _buildMobileLayout(context, navState, mode);
          }
        },
      ),
    );
  }

  // ── Desktop Layout ─────────────────────────────────────────────────

  Widget _buildDesktopLayout(
    BuildContext context,
    NirilNavigationState navState,
    AppMode? mode,
  ) {
    final nav = ref.read(nirilNavigationProvider.notifier);
    final dest = navState.destination;
    final desktopIndex = navState.desktopSidebarIndex;
    final desktopNavItems = _desktopNavItems(context);


    // Build desktop toolbar for non-home, non-custom-view pages
    Widget? desktopToolbar;
    if (!navState.isCustomView && desktopIndex != 0) {
      String btnText = K.chaer.tr(context, ref);
      if (desktopIndex == 1) {
        btnText = mode == AppMode.coolie
            ? K.pudhiyaPattiyal.tr(context, ref)
            : K.pudhiyaPattiyalPtn.tr(context, ref);
      } else if (desktopIndex == 2) {
        btnText = K.pudhiyaPatrucheettuPtn.tr(context, ref);
      } else if (desktopIndex == 3) {
        btnText = K.porulaichChaerPtn.tr(context, ref);
      } else if (desktopIndex == 4) {
        btnText = K.vaangunaraichChaer.tr(context, ref);
      }

      final selectionBar = SelectionOverlayHelper.buildOverlay(context, ref, desktopIndex, navState);

      desktopToolbar = Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        mainAxisSize: MainAxisSize.min,
        children: [
          ElvanDesktopToolbar(
            searchPlaceholder: K.thaeduga.tr(context, ref),
            addButtonText: btnText,
            onSearchChanged: (query) =>
                _routeSearchQuery(query, dest),
            onAdd: _onAddPressed,
          ),
          if (selectionBar != null) ...[
            const SizedBox(height: 16),
            selectionBar,
          ],
        ],
      );
    }

    // Resolve custom content (state-driven for desktop only)
    Widget? customContent;
    String? title;
    if (dest == NirilDestination.settings) {
      customContent = const SettingsScreen();
      title = K.amaippugal.tr(context, ref);
    } else {
      title = desktopIndex >= 0 && desktopIndex < desktopNavItems.length
          ? (desktopIndex == 0
              ? desktopNavItems[desktopIndex].label
              : (desktopNavItems[desktopIndex].headerLabel ??
                  desktopNavItems[desktopIndex].label))
          : '';
    }

    return ElvanDesktopShell(
      currentIndex: navState.isCustomView ? -1 : desktopIndex,
      onTabSelected: (index) {
        // Map desktop sidebar index to NirilDestination
        final destinations = [
          NirilDestination.mugappu,
          NirilDestination.pattiyal,
          NirilDestination.raseethu,
          NirilDestination.porul,
          NirilDestination.vaangunar,
        ];
        if (index >= 0 && index < destinations.length) {
          nav.goTo(destinations[index]);
        }
      },
      onSettingsPressed: () => nav.goTo(NirilDestination.settings),

      customContent: customContent,
      title: title,
      toolbar: desktopToolbar,
      navItems: desktopNavItems,
      slivers: [
        SliverOffstage(
          offstage: desktopIndex != 0,
          sliver: const MugappuPage(),
        ),
        SliverOffstage(
          offstage: desktopIndex != 1,
          sliver: mode == AppMode.coolie
              ? const CoolieInvoicesPage()
              : const SilkInvoicesPage(),
        ),
        SliverOffstage(
          offstage: desktopIndex != 2,
          sliver: mode == AppMode.coolie
              ? const CoolieReceiptsPage()
              : const SilkReceiptsPage(),
        ),
        SliverOffstage(
          offstage: desktopIndex != 3,
          sliver: const PorulPage(),
        ),
        SliverOffstage(
          offstage: desktopIndex != 4,
          sliver: const VaangunarPage(),
        ),
      ],
    );
  }

  // ── Mobile Layout ──────────────────────────────────────────────────

  Widget _buildMobileLayout(
    BuildContext context,
    NirilNavigationState navState,
    AppMode? mode,
  ) {
    final nav = ref.read(nirilNavigationProvider.notifier);

    final mobileTabIndex = navState.mobileTabIndex;
    final navItems = _mobileNavItems;

    return Scaffold(
      body: Stack(
        children: [
          // Tab-based shell
          IndexedStack(
            index: mobileTabIndex,
              children: [
                for (int i = 0; i < 4; i++)
                  Builder(
                    builder: (context) {
                      final overlayWidget = SelectionOverlayHelper.buildOverlay(context, ref, i, navState);
                      final isOverlayActive = overlayWidget != null;

                      return ElvanShell(
                        assignedIndex: i,
                        title: navItems[i].headerLabel ?? navItems[i].label,
                        currentIndex: mobileTabIndex,
                        onTabSelected: (index) {
                          // Map mobile tab index to NirilDestination
                          final destinations = [
                            NirilDestination.mugappu,
                            navState.uruvakkuSegment == 1
                                ? NirilDestination.raseethu
                                : NirilDestination.pattiyal,
                            NirilDestination.porul,
                            NirilDestination.vaangunar,
                          ];
                          if (index >= 0 && index < destinations.length) {
                            nav.goTo(destinations[index]);
                          }
                        },
                        showSearchIcon: i != 0,
                        onSearchChanged: (query) => _routeMobileSearch(query, i),
                        isOverlayActive: isOverlayActive,
                        overlayWidget: overlayWidget,
                        navActions: [
                          const SizedBox(width: 7),

                          ElvanTopBarIcon(
                            icon: CupertinoIcons.add,
                            onTap: _onAddPressed,
                          ),
                          const SizedBox(width: 14),
                          ElvanPopupMenu(
                            showSelectOption: i > 0,
                            isSilkHome:
                                ref.watch(appModeProvider) != AppMode.coolie &&
                                    i == 0,
                            onSelectTapped: () {
                              _onSelectTapped();
                            },
                          ),
                          const SizedBox(width: 7),
                        ],
                        navItems: navItems,
                        slivers: [
                          if (i == 0) const MugappuPage(),
                          if (i == 1) const UruvakkuPage(),
                          if (i == 2) const PorulPage(),
                          if (i == 3) const VaangunarPage(),
                        ],
                      );
                    },
                  ),
              ],
            ),
        ],
      ),

    );
  }
}



