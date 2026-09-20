import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../koorugal/maeladukkugal/elvan_kizh_maeladukku.dart'
    as legacy_sheet;
import '../../../../../koorugal/maeladukkugal/elvan_kizh_maeladukku/koorugal/elvan_maeladukku_thaedal.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_kavanam.dart';
import '../../../kalanjiyam/vaangunar_nilaimai.dart';
import 'package:elvan_niril/src/adippadai/vazhikaattal/niril_nav.dart';

/// Bottom-sheet dialog for picking one or more invoices.
/// Returns the selected list via [onConfirmed].
class PatruPattiyalTheervuMaeladukku {
  static void show({
    required BuildContext context,
    required WidgetRef ref,
    required List<PattiyalTharavuru> invoices,
    required Set<int> initialSelectedIds,
    required void Function(List<PattiyalTharavuru> selected) onConfirmed,
  }) {
    final searchCtrl = TextEditingController();
    var searchQuery = '';
    final selectedIds = Set<int>.from(initialSelectedIds);
    final customers = ref.read(vaangunargalProvider).asData?.value ?? [];

    legacy_sheet.showElvanBottomSheet(
      context: context,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setDialogState) {
            // Smart Filtering: Lock to the first selected invoice's company, customer, and exact address
            PattiyalTharavuru? firstSelected;
            if (selectedIds.isNotEmpty) {
              final firstId = selectedIds.first;
              for (var i in invoices) {
                if (i.id == firstId) {
                  firstSelected = i;
                  break;
                }
              }
            }

            final availableInvoices = firstSelected != null
                ? invoices.where((inv) {
                    return inv.niruvanamId == firstSelected!.niruvanamId &&
                           inv.vaangunarId == firstSelected.vaangunarId;
                  }).toList()
                : invoices;

            final filtered = searchQuery.isEmpty
                ? availableInvoices
                : availableInvoices.where((inv) {
                    final q = searchQuery.toLowerCase();
                    final pName = (inv.vaangunarPeyar['ta'] ??
                            inv.vaangunarPeyar['en'] ??
                            '')
                        .toLowerCase();
                    return inv.patrucheettuEn.toLowerCase().contains(q) ||
                        pName.contains(q);
                  }).toList();

            Widget buildDesktop() {
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 24.0),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    ElvanMaeladukkuThaedal(
                      controller: searchCtrl,
                      onChanged: (val) {
                        setDialogState(() => searchQuery = val);
                      },
                    ),
                    const SizedBox(height: 16),
                    Flexible(
                      child: filtered.isEmpty
                          ? Center(
                              child: Text(K.pattiyalgalIllai.tr(context, ref),
                                  style: const TextStyle(color: Colors.grey)))
                          : ListView.builder(
                              keyboardDismissBehavior: ElvanKavanam.surulNadathai,
                              shrinkWrap: true,
                              itemCount: filtered.length,
                              itemBuilder: (_, index) {
                                final inv = filtered[index];
                                final isSelected = selectedIds.contains(inv.id);
                                final currFmt = NumberFormat.currency(
                                    locale: 'en_IN', symbol: '₹');
                                final dateFmt = DateFormat('dd/MM/yyyy');
                                final customer = customers.where((c) => c.id == inv.vaangunarId).firstOrNull;
                                final oorText = customer?.oor['ta'] ?? customer?.oor['en'] ?? '';
                                final amountText = oorText.isNotEmpty
                                    ? '$oorText • ${currFmt.format(inv.mothaThogai)}'
                                    : currFmt.format(inv.mothaThogai);

                                return ListTile(
                                  horizontalTitleGap: 8.0,
                                  minLeadingWidth: 0,
                                  leading: Checkbox(
                                    value: isSelected,
                                    shape: const CircleBorder(),
                                    activeColor: Theme.of(context).colorScheme.onSurface,
                                    checkColor: Theme.of(context).colorScheme.surface,
                                    onChanged: (val) {
                                      setDialogState(() {
                                        if (val == true) {
                                          selectedIds.add(inv.id);
                                        } else {
                                          selectedIds.remove(inv.id);
                                        }
                                      });
                                    },
                                  ),
                                  title: Row(
                                    children: [
                                      Text(
                                        inv.patrucheettuEn,
                                        style: const TextStyle(
                                            fontWeight: FontWeight.w600,
                                            fontSize: 14),
                                      ),
                                      const SizedBox(width: 8),
                                      Text(
                                        dateFmt.format(inv.pattiyalNaal),
                                        style: TextStyle(
                                          fontSize: 12,
                                          color: Colors.grey.shade500,
                                        ),
                                      ),
                                    ],
                                  ),
                                  subtitle: (inv.vaangunarPeyar['ta'] ??
                                              inv.vaangunarPeyar['en'] ??
                                              '')
                                          .isNotEmpty
                                      ? Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Text(
                                                inv.vaangunarPeyar['ta'] ??
                                                    inv.vaangunarPeyar[
                                                        'English'] ??
                                                    '',
                                                style: const TextStyle(
                                                    fontSize: 13),
                                                maxLines: 1,
                                                overflow:
                                                    TextOverflow.ellipsis),
                                            if ((inv.vaangunarMunvari[
                                                        'Tamil'] ??
                                                    inv.vaangunarMunvari[
                                                        'English'] ??
                                                    '')
                                                .isNotEmpty)
                                              Text(
                                                  inv.vaangunarMunvari[
                                                          'Tamil'] ??
                                                      inv.vaangunarMunvari[
                                                          'English'] ??
                                                      '',
                                                  style: TextStyle(
                                                      fontSize: 11,
                                                      color:
                                                          Colors.grey.shade500),
                                                  maxLines: 1,
                                                  overflow:
                                                      TextOverflow.ellipsis),
                                            const SizedBox(height: 2),
                                            Text(
                                              amountText,
                                              style: TextStyle(
                                                fontSize: 12,
                                                color: Colors.grey.shade600,
                                              ),
                                            ),
                                          ],
                                        )
                                      : Text(
                                          amountText,
                                          style: TextStyle(
                                            fontSize: 12,
                                            color: Colors.grey.shade600,
                                          ),
                                        ),
                                  onTap: () {
                                    setDialogState(() {
                                      if (isSelected) {
                                        selectedIds.remove(inv.id);
                                      } else {
                                        selectedIds.add(inv.id);
                                      }
                                    });
                                  },
                                );
                              },
                            ),
                    ),
                    const SizedBox(height: 24),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 24.0),
                      child: Align(
                        alignment: Alignment.centerRight,
                        child: SizedBox(
                          height: 40,
                          child: FilledButton(
                            onPressed: () {
                              final selected = invoices
                                  .where((i) => selectedIds.contains(i.id))
                                  .toList();
                              onConfirmed(selected);
                              Navigator.of(ctx).pop();
                            },
                            style: FilledButton.styleFrom(
                              backgroundColor: Theme.of(context).colorScheme.onSurface,
                              foregroundColor: Theme.of(context).colorScheme.surface,
                              padding: const EdgeInsets.symmetric(horizontal: 24),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(50),
                              ),
                            ),
                            child: Text(K.mudindhadhu.tr(context, ref)),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              );
            }

            Widget buildMobile() {
              return ConstrainedBox(
                constraints: BoxConstraints(
                  maxHeight: MediaQuery.of(context).size.height * 0.5,
                ),
                child: SafeArea(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const SizedBox(height: 8),
                      // Search
                      ElvanMaeladukkuThaedal(
                        controller: searchCtrl,
                        onChanged: (val) {
                          setDialogState(() => searchQuery = val);
                        },
                      ),
                      const SizedBox(height: 8),
                      // Invoice list
                      Flexible(
                        child: filtered.isEmpty
                            ? Center(
                                child: Text(K.pattiyalgalIllai.tr(context, ref),
                                    style: const TextStyle(color: Colors.grey)))
                            : ListView.builder(
                                keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.manual,
                                shrinkWrap: true,
                                itemCount: filtered.length,
                                itemBuilder: (_, index) {
                                  final inv = filtered[index];
                                  final isSelected = selectedIds.contains(inv.id);
                                  final currFmt = NumberFormat.currency(
                                      locale: 'en_IN', symbol: '₹');
                                  final dateFmt = DateFormat('dd/MM/yyyy');
                                  final customer = customers.where((c) => c.id == inv.vaangunarId).firstOrNull;
                                  final oorText = customer?.oor['ta'] ?? customer?.oor['en'] ?? '';
                                  final amountText = oorText.isNotEmpty
                                      ? '$oorText • ${currFmt.format(inv.mothaThogai)}'
                                      : currFmt.format(inv.mothaThogai);

                                  return ListTile(
                                    horizontalTitleGap: 8.0,
                                    minLeadingWidth: 0,
                                    leading: Checkbox(
                                      value: isSelected,
                                      shape: const CircleBorder(),
                                      activeColor: Theme.of(context).colorScheme.onSurface,
                                      checkColor: Theme.of(context).colorScheme.surface,
                                      onChanged: (val) {
                                        setDialogState(() {
                                          if (val == true) {
                                            selectedIds.add(inv.id);
                                          } else {
                                            selectedIds.remove(inv.id);
                                          }
                                        });
                                      },
                                    ),
                                    title: Row(
                                      children: [
                                        Text(
                                          inv.patrucheettuEn,
                                          style: const TextStyle(
                                              fontWeight: FontWeight.w600,
                                              fontSize: 14),
                                        ),
                                        const SizedBox(width: 8),
                                        Text(
                                          dateFmt.format(inv.pattiyalNaal),
                                          style: TextStyle(
                                            fontSize: 12,
                                            color: Colors.grey.shade500,
                                          ),
                                        ),
                                      ],
                                    ),
                                    subtitle: (inv.vaangunarPeyar['ta'] ??
                                                inv.vaangunarPeyar['en'] ??
                                                '')
                                            .isNotEmpty
                                        ? Column(
                                            crossAxisAlignment:
                                                CrossAxisAlignment.start,
                                            children: [
                                              Text(
                                                  inv.vaangunarPeyar['ta'] ??
                                                      inv.vaangunarPeyar[
                                                          'English'] ??
                                                      '',
                                                  style: const TextStyle(
                                                      fontSize: 13),
                                                  maxLines: 1,
                                                  overflow:
                                                      TextOverflow.ellipsis),
                                              if ((inv.vaangunarMunvari[
                                                          'Tamil'] ??
                                                      inv.vaangunarMunvari[
                                                          'English'] ??
                                                      '')
                                                  .isNotEmpty)
                                                Text(
                                                    inv.vaangunarMunvari[
                                                            'Tamil'] ??
                                                        inv.vaangunarMunvari[
                                                            'English'] ??
                                                        '',
                                                    style: TextStyle(
                                                        fontSize: 11,
                                                        color:
                                                            Colors.grey.shade500),
                                                    maxLines: 1,
                                                    overflow:
                                                        TextOverflow.ellipsis),
                                              const SizedBox(height: 2),
                                              Text(
                                                amountText,
                                                style: TextStyle(
                                                  fontSize: 12,
                                                  color: Colors.grey.shade600,
                                                ),
                                              ),
                                            ],
                                          )
                                        : Text(
                                            amountText,
                                            style: TextStyle(
                                              fontSize: 12,
                                              color: Colors.grey.shade600,
                                            ),
                                          ),
                                    onTap: () {
                                      setDialogState(() {
                                        if (isSelected) {
                                          selectedIds.remove(inv.id);
                                        } else {
                                          selectedIds.add(inv.id);
                                        }
                                      });
                                    },
                                  );
                                },
                              ),
                      ),
                      Column(
                        children: [
                          Divider(
                            height: 1,
                            thickness: 1,
                            color: Theme.of(context)
                                .colorScheme
                                .outline
                                .withValues(alpha: 0.1),
                          ),
                          InkWell(
                            onTap: () {
                              final selected = invoices
                                  .where((i) => selectedIds.contains(i.id))
                                  .toList();
                              onConfirmed(selected);
                              Navigator.of(ctx).pop();
                            },
                            child: Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 32,
                                vertical: 16,
                              ),
                              width: double.infinity,
                              alignment: Alignment.center,
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Text(
                                    K.mudindhadhu.tr(context, ref),
                                    style: TextStyle(
                                      fontSize: 15,
                                      fontWeight: FontWeight.w500,
                                      color: Theme.of(context)
                                          .colorScheme
                                          .onSurface
                                          .withValues(alpha: 0.8),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            }

            if (isDesktopLayoutContext(context)) {
              return buildDesktop();
            }
            return buildMobile();
          },
        );
      },
    );
  }
}
