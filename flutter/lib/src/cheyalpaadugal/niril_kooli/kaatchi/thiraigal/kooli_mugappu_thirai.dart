import 'package:elvan_niril/src/cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import '../koorugal/kooli_mugappu_attai.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/vazhikaattal/navigation_provider.dart';
import '../../../../adippadai/vazhikaattal/navigation_destination.dart';
import '../../../../adippadai/vazhikaattal/niril_nav.dart';
import '../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../../niril_podhu/kaatchi/koorugal/mugappu_tharavugal_kooru.dart';
import '../paarvai/kooli_pattiyal_paarvai.dart';

/// Coolie Home Page — pixel-perfect port of React CoolieDashboard.
/// Shows: 3 stats cards (bento grid) + 6 recent invoice cards.
class CoolieHomePage extends ConsumerWidget {
  const CoolieHomePage({super.key});

  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pattiyalgalAsync = ref.watch(pattiyalgalProvider);
    final profilesAsync = ref.watch(kooliNiruvanaTharavugalListProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return pattiyalgalAsync.when(
      loading: () => SliverPadding(
        padding: const EdgeInsets.only(left: 16, right: 16, bottom: 120),
        sliver: SliverToBoxAdapter(
          child: Column(
            children: [
              _buildStatsGrid(context, ref, [], [], isDark, true),
              const SizedBox(height: 24),
              const MugappuLoadingSkeleton(),
            ],
          ),
        ),
      ),
      error: (e, _) => SliverFillRemaining(
        child: Center(child: Text('Error: $e')),
      ),
      data: (pattiyalgal) {
        final profiles = profilesAsync;

        // Sort by date descending, take top 8
        final sorted = [...pattiyalgal]
          ..sort((a, b) => b.pattiyalNaal.compareTo(a.pattiyalNaal));
        final recentBills = sorted.take(8).toList();

        return SliverPadding(
          padding: const EdgeInsets.only(left: 16, right: 16, bottom: 120),
          sliver: SliverToBoxAdapter(
            child: Column(
              children: [
                // Stats bento grid
                _buildStatsGrid(
                    context, ref, pattiyalgal, profiles, isDark, false),
                const SizedBox(height: 8),

                // Recent activity header
                RecentActivityHeader(
                  onSeeAll: () {
                    ref
                        .read(nirilNavigationProvider.notifier)
                        .goTo(NirilDestination.pattiyal);
                  },
                ),

                // Recent invoice cards
                if (pattiyalgal.isEmpty)
                  const MugappuEmptyState()
                else
                  LayoutBuilder(
                    builder: (context, constraints) {
                      final isDesktop = constraints.maxWidth >= 800;
                      if (isDesktop) {
                        return _buildDesktopGrid(recentBills, context, ref);
                      }
                      return _buildMobileList(recentBills, context, ref);
                    },
                  ),
              ],
            ),
          ),
        );
      },
    );
  }

  // ── Stats Bento Grid ────────────────────────────────────────────────────

  Widget _buildStatsGrid(
    BuildContext context,
    WidgetRef ref,
    List<PattiyalTharavuru> pattiyalgal,
    List<NiruvanaTharavugal> profiles,
    bool isDark,
    bool isLoading,
  ) {
    // Compute stats
    double overallTotal = 0;
    final byCompany = <int, ({String name, double total, int count})>{};

    for (final p in profiles) {
      if (p.id != null) {
        byCompany[p.id!] = (
          name: p.kurumPeyar.isNotEmpty
              ? p.kurumPeyar
              : (p.niruvanathinPeyar.values.firstOrNull ?? ''),
          total: 0.0,
          count: 0,
        );
      }
    }

    for (final b in pattiyalgal) {
      overallTotal += b.mothaThogai;
      if (b.niruvanamId != null && byCompany.containsKey(b.niruvanamId)) {
        final prev = byCompany[b.niruvanamId]!;
        byCompany[b.niruvanamId!] = (
          name: prev.name,
          total: prev.total + b.mothaThogai,
          count: prev.count + 1,
        );
      }
    }

    final activeCompanies = byCompany.values
        .where((c) => c.count > 0)
        .toList();

    // Company names value
    final companiesValue = activeCompanies.isEmpty
        ? '—'
        : activeCompanies.map((c) => c.name).join(', ');

    // Invoice count value (detailed per-company breakdown)
    String invoiceCountValue;
    if (activeCompanies.length == 1) {
      invoiceCountValue =
          '${activeCompanies[0].count} · ${activeCompanies[0].name}';
    } else if (activeCompanies.length > 1) {
      invoiceCountValue = '${activeCompanies.map((c) => '${c.count} · ${c.name}').join('  +  ')}  =  ${pattiyalgal.length}';
    } else {
      invoiceCountValue = '${pattiyalgal.length}';
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        final isDesktop = constraints.maxWidth >= 800;
        final gap = isDesktop ? 24.0 : 16.0;

        if (isDesktop) {
          // 3-column grid
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: ElvanStatsCard(
                  icon: CupertinoIcons.money_dollar_circle,
                  label: K.mothaKanakku.tr(context, ref),
                  value: _currencyFormat.format(overallTotal),
                  isLoading: isLoading,
                ),
              ),
              SizedBox(width: gap),
              Expanded(
                child: ElvanStatsCard(
                  icon: CupertinoIcons.building_2_fill,
                  label: K.niruvanangal.tr(context, ref),
                  value: companiesValue,
                  isLoading: isLoading,
                ),
              ),
              SizedBox(width: gap),
              Expanded(
                child: ElvanStatsCard(
                  icon: CupertinoIcons.doc_text,
                  label: K.mothaPattiyalgal.tr(context, ref),
                  value: invoiceCountValue,
                  isLoading: isLoading,
                ),
              ),
            ],
          );
        }

        // Mobile: 2-col top row + full-width third card
        return Column(
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: ElvanStatsCard(
                    icon: CupertinoIcons.money_dollar_circle,
                    label: K.mothaKanakku.tr(context, ref),
                    value: _currencyFormat.format(overallTotal),
                    isLoading: isLoading,
                  ),
                ),
                SizedBox(width: gap),
                Expanded(
                  child: ElvanStatsCard(
                    icon: CupertinoIcons.building_2_fill,
                    label: K.niruvanangal.tr(context, ref),
                    value: companiesValue,
                    isLoading: isLoading,
                  ),
                ),
              ],
            ),
            SizedBox(height: gap),
            ElvanStatsCard(
              icon: CupertinoIcons.doc_text,
              label: K.mothaPattiyalgal.tr(context, ref),
              value: invoiceCountValue,
              isLoading: isLoading,
            ),
          ],
        );
      },
    );
  }

  // ── Desktop 2-col grid ──────────────────────────────────────────────────

  Widget _buildDesktopGrid(
    List<PattiyalTharavuru> items,
    BuildContext context,
    WidgetRef ref,
  ) {
    final rows = <Widget>[];
    for (var i = 0; i < items.length; i += 2) {
      rows.add(
        Padding(
          padding: const EdgeInsets.only(bottom: 16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: KooliMugappuAttai(
                  index: i,
                  pattiyal: items[i],
                  onTap: () => _openEditor(context, items[i]),
                ),
              ),
              const SizedBox(width: 16),
              if (i + 1 < items.length)
                Expanded(
                  child: KooliMugappuAttai(
                    index: i + 1,
                    pattiyal: items[i + 1],
                    onTap: () => _openEditor(context, items[i + 1]),
                  ),
                )
              else
                const Expanded(child: SizedBox()),
            ],
          ),
        ),
      );
    }
    return Column(children: rows);
  }

  // ── Mobile list ─────────────────────────────────────────────────────────

  Widget _buildMobileList(
    List<PattiyalTharavuru> items,
    BuildContext context,
    WidgetRef ref,
  ) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 80),
      child: Column(
        children: items.asMap().entries.map((entry) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: KooliMugappuAttai(
              index: entry.key,
              pattiyal: entry.value,
              onTap: () => _openEditor(context, entry.value),
            ),
          );
        }).toList(),
      ),
    );
  }

  void _openEditor(BuildContext context, PattiyalTharavuru pattiyal) {
    NirilNav.push(
      context,
      KooliPattiyalPaarvai(pattiyal: pattiyal),
    );
  }
}
