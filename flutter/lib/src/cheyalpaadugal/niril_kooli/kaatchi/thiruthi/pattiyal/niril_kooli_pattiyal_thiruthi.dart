import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kalanjiyam/kooli_pattiyal_kalanjiyam.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';


import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_niruvanam_oadu.dart';
import '../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../../../../niril_podhu/kalanjiyam/pattiyal_kanakku.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_keezhvirivu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import '../../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import 'package:elvan_niril/src/cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import 'package:elvan_niril/src/cheyalpaadugal/chattagam/kaatchi/kaippaesi/koorugal/elvan_cheyal_pothan.dart';
import '../../paarvai/kooli_pattiyal_paarvai.dart';
import '../../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../porul/niril_kooli_porul_thiruthi.dart';
import 'koorugal/koorugal.dart';
import '../../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../../../niril_podhu/kaatchi/koorugal/elvan_pattiyal_tharavugal_kooru.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_asai_pattiyal.dart';

/// Coolie Invoice Editor — weight-based billing with setharam, courier,
/// ahimsa, and other charges. Uses floor(kg × rate) truncation.
class CoolieInvoiceEditor extends ConsumerStatefulWidget {
  final PattiyalTharavuru? editingEntry;

  const CoolieInvoiceEditor({super.key, this.editingEntry});

  @override
  ConsumerState<CoolieInvoiceEditor> createState() => _CoolieInvoiceEditorState();
}

class _CoolieInvoiceEditorState extends ConsumerState<CoolieInvoiceEditor> {
  // ── Company Profile ──
  int? _selectedNiruvanamId;
  NiruvanaTharavugal? _selectedProfile;

  // ── Customer ──
  int? _selectedVaangunarId;
  Map<String, String> _selectedVaangunarPeyarMap = const {};
  Map<String, String> _selectedVaangunarMunvariMap = const {};

  // ── Metadata ──
  DateTime _pattiyalNaal = DateTime.now();

  // ── Line Items ──
  List<KooliUrupadi> _items = [const KooliUrupadi()];

  // ── Additional Charges ──
  double _setharamGrams = 0;
  double _thabaalThogai = 0;
  double _ahimsaPattuThogai = 0;
  List<PiraVarivu> _piraVarivugal = [];

  // ── Totals ──
  final ValueNotifier<KooliMothangal> _totalsNotifier =
      ValueNotifier(const KooliMothangal());

  // ── Controllers ──
  final _setharamCtrl = TextEditingController();
  final _thabaalCtrl = TextEditingController();
  final _ahimsaCtrl = TextEditingController();
  bool _saving = false;

  // ── Unsaved Changes & Draft ──
  bool _hasUnsavedChanges = false;
  Timer? _draftDebounce;

  // ── Bill Number ──
  String _previewBillNumber = '';
  String _invoiceNumberOverride = '';
  String _profilePrefix = 'CB';

  bool get _isEditing => widget.editingEntry != null;

  @override
  void initState() {
    super.initState();
    if (_isEditing) {
      _loadEditingData();
    } else {
      _tryRestoreDraft();
    }
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_isEditing && _selectedNiruvanamId == null) {
        _computePreviewBillNumber();
      }
    });
  }

  void _loadEditingData() {
    final snapshot = KooliPattiyalUthavi.loadFromEntry(widget.editingEntry!);
    _selectedVaangunarId = snapshot.selectedVaangunarId;
    _selectedVaangunarPeyarMap = snapshot.selectedVaangunarPeyarMap;
    _selectedVaangunarMunvariMap = snapshot.selectedVaangunarMunvariMap;
    _selectedNiruvanamId = snapshot.selectedNiruvanamId;
    _pattiyalNaal = snapshot.pattiyalNaal;
    _items = snapshot.items.isNotEmpty ? snapshot.items : [const KooliUrupadi()];
    _setharamGrams = snapshot.setharamGrams;
    _thabaalThogai = snapshot.thabaalThogai;
    _ahimsaPattuThogai = snapshot.ahimsaPattuThogai;
    _piraVarivugal = snapshot.piraVarivugal;

    // Load existing bill number for display + override
    _previewBillNumber = widget.editingEntry!.patrucheettuEn;
    _invoiceNumberOverride = widget.editingEntry!.patrucheettuEn;

    _setharamCtrl.text = _setharamGrams > 0 ? _cleanNum(_setharamGrams) : '';
    _thabaalCtrl.text = _thabaalThogai > 0 ? _cleanNum(_thabaalThogai) : '';
    _ahimsaCtrl.text = _ahimsaPattuThogai > 0 ? _cleanNum(_ahimsaPattuThogai) : '';

    // Load profile match + derive prefix
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final profiles = ref.read(NiruvanaTharavugalListProvider);
      if (_selectedNiruvanamId != null) {
        final match = profiles.where((p) => p.id == _selectedNiruvanamId).firstOrNull;
        if (match != null) {
          setState(() {
            _selectedProfile = match;
            final shortName = match.kurumPeyar.isNotEmpty ? match.kurumPeyar : 'CB';
            _profilePrefix = '$shortName-';
          });
        }
      }
    });

    _backfillItems();
    _recalculate();
  }

  Future<void> _backfillItems() async {
    final products = await ref.read(porulgalProvider.future);
    bool changed = false;
    final newItems = _items.map((item) {
      if (item.porulPeyarEn.isEmpty && (item.porulId?.isNotEmpty == true)) {
        final product = products.where((p) => p.id.toString() == item.porulId).firstOrNull;
        if (product != null) {
          final enName = product.porulPeyar['en'] ?? '';
          if (enName.isNotEmpty) {
            changed = true;
            return item.copyWith(porulPeyarEn: enName);
          }
        }
      }
      return item;
    }).toList();
    if (changed && mounted) {
      setState(() => _items = newItems);
    }
  }

  @override
  void dispose() {
    _setharamCtrl.dispose();
    _thabaalCtrl.dispose();
    _ahimsaCtrl.dispose();
    _totalsNotifier.dispose();
    _draftDebounce?.cancel();
    super.dispose();
  }

  /// Clean number display: 100 → '100', 100.5 → '100.5' (no trailing .0)
  static String _cleanNum(double v) =>
      v == v.truncateToDouble() ? v.toInt().toString() : v.toString();

  void _recalculate() {
    setState(() {
      _totalsNotifier.value = KooliKanakku.calculate(
        items: _items,
        setharamGrams: _setharamGrams,
        thabaalThogai: _thabaalThogai,
        ahimsaPattuThogai: _ahimsaPattuThogai,
        piraVarivugal: _piraVarivugal,
      );
    });
    _scheduleDraftSave();
  }

  /// Lightweight recalculation that updates the totals notifier
  /// WITHOUT calling setState — only the ValueListenableBuilder
  /// around the totals card rebuilds. This prevents the AnimatedList
  /// in Section 3 from being rebuilt on every keystroke.
  void _recalculateQuiet() {
    _totalsNotifier.value = KooliKanakku.calculate(
      items: _items,
      setharamGrams: _setharamGrams,
      thabaalThogai: _thabaalThogai,
      ahimsaPattuThogai: _ahimsaPattuThogai,
      piraVarivugal: _piraVarivugal,
    );
    _scheduleDraftSave();
  }

  // ── Draft (delegates to helper) ──
  void _scheduleDraftSave() {
    _draftDebounce?.cancel();
    _draftDebounce = Timer(const Duration(seconds: 2), () {
      if (_isEditing) return;
      KooliPattiyalUthavi.saveDraft(_currentSnapshot());
    });
  }

  Future<void> _tryRestoreDraft() async {
    final snapshot = await KooliPattiyalUthavi.tryRestoreDraft(context, ref);
    if (snapshot != null && mounted) {
      setState(() {
        _selectedVaangunarId = snapshot.selectedVaangunarId;
        _selectedVaangunarPeyarMap = snapshot.selectedVaangunarPeyarMap;
        _selectedVaangunarMunvariMap = snapshot.selectedVaangunarMunvariMap;
        _selectedNiruvanamId = snapshot.selectedNiruvanamId;
        _pattiyalNaal = snapshot.pattiyalNaal;
        _items = snapshot.items.isNotEmpty
            ? snapshot.items
            : [const KooliUrupadi()];
        _setharamGrams = snapshot.setharamGrams;
        _thabaalThogai = snapshot.thabaalThogai;
        _ahimsaPattuThogai = snapshot.ahimsaPattuThogai;
        _piraVarivugal = snapshot.piraVarivugal;
        _invoiceNumberOverride = snapshot.invoiceNumberOverride;

        _setharamCtrl.text =
            _setharamGrams > 0 ? _cleanNum(_setharamGrams) : '';
        _thabaalCtrl.text =
            _thabaalThogai > 0 ? _cleanNum(_thabaalThogai) : '';
        _ahimsaCtrl.text =
            _ahimsaPattuThogai > 0 ? _cleanNum(_ahimsaPattuThogai) : '';
      });
      _backfillItems();
      _recalculate();
    }
  }

  /// Builds a snapshot from current state (for draft / save).
  KooliThiruththiNilaimai _currentSnapshot() => KooliThiruththiNilaimai(
        selectedNiruvanamId: _selectedNiruvanamId,
        selectedProfile: _selectedProfile,
        selectedVaangunarId: _selectedVaangunarId,
        selectedVaangunarPeyarMap: _selectedVaangunarPeyarMap,
        selectedVaangunarMunvariMap: _selectedVaangunarMunvariMap,
        pattiyalNaal: _pattiyalNaal,
        items: _items,
        setharamGrams: _setharamGrams,
        thabaalThogai: _thabaalThogai,
        ahimsaPattuThogai: _ahimsaPattuThogai,
        piraVarivugal: _piraVarivugal,
        invoiceNumberOverride: _invoiceNumberOverride,
      );

  // ── Bill Number Preview ──
  Future<void> _computePreviewBillNumber() async {
    if (_isEditing) return;
    try {
      final kalanjiyam = ref.read(pattiyalKalanjiyamProvider);
      final finYear = KooliPattiyalKalanjiyam.getCurrentFinYear();
      final shortName = (_selectedProfile?.kurumPeyar.isNotEmpty == true
          ? _selectedProfile!.kurumPeyar
          : 'CB');
      _profilePrefix = '$shortName-';
      final vanakkam = await kalanjiyam.getNextVanakkam(
        _selectedNiruvanamId,
        finYear,
      );
      if (mounted) {
        setState(() {
          _previewBillNumber =
              kalanjiyam.formatPattiyalEn(shortName, vanakkam);
        });
      }
    } catch (_) {}
  }

  Future<void> _handleSave() async {
    // Unfocus active field → triggers blur commit on ItemFieldWidget
    FocusManager.instance.primaryFocus?.unfocus();
    // Let blur handler run before we read field values
    await Future.delayed(const Duration(milliseconds: 50));
    if (!mounted) return;

    if (_selectedProfile == null) {
      ElvanSnackbar.show(context, K.niruvanamThaerodhu.tr(context, ref));
      return;
    }
    if (_selectedVaangunarId == null && _selectedVaangunarPeyarMap.isEmpty) {
      ElvanSnackbar.show(context, K.vaangunaraiThaerodhu.tr(context, ref));
      return;
    }
    final validItems = _items.where((i) => i.edai > 0 && i.vilai > 0).toList();
    if (validItems.isEmpty) {
      ElvanSnackbar.show(context, K.kuriaindhOruPorul.tr(context, ref));
      return;
    }

    setState(() => _saving = true);

    try {
      final kalanjiyam = ref.read(pattiyalKalanjiyamProvider);
      final prefix = _selectedProfile!.kurumPeyar.isNotEmpty
          ? _selectedProfile!.kurumPeyar
          : 'CB';

      final result = await KooliPattiyalUthavi.save(
        kalanjiyam: kalanjiyam,
        state: KooliThiruththiNilaimai(
          selectedNiruvanamId: _selectedNiruvanamId,
          selectedProfile: _selectedProfile,
          selectedVaangunarId: _selectedVaangunarId,
          selectedVaangunarPeyarMap: _selectedVaangunarPeyarMap,
          selectedVaangunarMunvariMap: _selectedVaangunarMunvariMap,
          pattiyalNaal: _pattiyalNaal,
          items: validItems,
          setharamGrams: _setharamGrams,
          thabaalThogai: _thabaalThogai,
          ahimsaPattuThogai: _ahimsaPattuThogai,
          piraVarivugal: _piraVarivugal,
          invoiceNumberOverride: _invoiceNumberOverride,
        ),
        totals: _totalsNotifier.value,
        profilePrefix: prefix,
        profile: _selectedProfile!,
        editingEntry: widget.editingEntry,
      );

      if (result is String) {
        if (mounted) {
          setState(() => _saving = false);
          ElvanSnackbar.show(context, result);
        }
        return;
      }

      final savedPattiyal = result as PattiyalTharavuru;

      await KooliPattiyalUthavi.clearDraft();
      _hasUnsavedChanges = false;
      if (mounted) {
        ref.invalidate(pattiyalgalProvider);
        ElvanSnackbar.show(
          context,
          K.porulChaemikkappattadhu.tr(context, ref),
          showAboveNavbar: true,
        );
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => KooliPattiyalPaarvai(
              pattiyal: savedPattiyal,
            ),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ElvanSnackbar.show(context, 'Error: $e');
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // BUILD — Flat layout (receipt editor pattern)
  //
  // Each section is an independent widget in a flat Column. No shared
  // Opacity/IgnorePointer wrapper coupling sections together. This prevents
  // focus changes in Section 4 from causing layout shifts in Section 3.
  // ═══════════════════════════════════════════════════════════════════════════

  @override
  Widget build(BuildContext context) {
    final profiles = ref.watch(NiruvanaTharavugalListProvider);
    final baseIndex = profiles.length > 1 ? 1 : 0;
    final formatter = NumberFormat('#,##0', 'en_IN');
    final vaangunargalAsync = ref.watch(vaangunargalProvider);
    final VaangunarTharavuru? selectedVaangunar = vaangunargalAsync.whenOrNull(
      data: (list) => _selectedVaangunarId != null
          ? list.cast<VaangunarTharavuru?>().firstWhere(
                (v) => v!.id == _selectedVaangunarId,
                orElse: () => null)
          : null,
    );

    final bool isDisabled = _selectedNiruvanamId == null;

    return ElvanEditorShell(
      title: _isEditing
          ? K.maatriyamai.tr(context, ref)
          : K.pudhiyaAakkam.tr(context, ref),
      onSave: _saving ? null : _handleSave,
      hasUnsavedChanges: _hasUnsavedChanges,
      onDiscard: () async {
        await KooliPattiyalUthavi.clearDraft();
      },
      child: ElvanThiruthiNiruvanamOadu(
        selectedNiruvanamId: _selectedNiruvanamId,
        onChanged: (p) {
          setState(() {
            _selectedNiruvanamId = p?.id;
            _selectedProfile = p;
            _profilePrefix = p?.kurumPeyar.isNotEmpty == true
                ? p!.kurumPeyar
                : 'CB';
            _invoiceNumberOverride = '';
            _hasUnsavedChanges = true;
          });
          _computePreviewBillNumber();
        },
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ──────────────────────────────────────────────────────────
            // Section 1: ① Customer
            // ──────────────────────────────────────────────────────────
            ElvanEditorSection(
              index: baseIndex,
              title: K.perunar.tr(context, ref),
              displayChild: const SizedBox(),
              initiallyExpanded: true,
              children: [
                KooliVaangunarKooru(
                  selectedVaangunarId: _selectedVaangunarId,
                  selectedVaangunarPeyarMap: _selectedVaangunarPeyarMap,
                  selectedVaangunar: selectedVaangunar,
                  onVaangunarSelected: (entry) {
                    setState(() {
                      _selectedVaangunarId = entry.id;
                      _selectedVaangunarPeyarMap =
                          entry.peyar.cast<String, String>();
                      _selectedVaangunarMunvariMap =
                          entry.mugavari.cast<String, String>();
                      _hasUnsavedChanges = true;
                    });
                  },
                  onVaangunarCleared: () {
                    setState(() {
                      _selectedVaangunarId = null;
                      _selectedVaangunarPeyarMap = const {};
                      _selectedVaangunarMunvariMap = const {};
                      _hasUnsavedChanges = true;
                    });
                  },
                ),
              ],
            ),

            // ──────────────────────────────────────────────────────────
            // Section 2: ② Invoice Details
            // ──────────────────────────────────────────────────────────
            _disabledWrap(
              isDisabled: isDisabled,
              child: ElvanEditorSection(
                index: baseIndex + 1,
                title: K.pattiyalTharavugal.tr(context, ref),
                displayChild: const SizedBox(),
                initiallyExpanded: true,
                children: [
                  ...buildElvanPattiyalTharavugalKooru(
                    context: context,
                    ref: ref,
                    isEditing: _isEditing,
                    invoiceNumberOverride: _invoiceNumberOverride,
                    previewInvoiceNumber: _previewBillNumber,
                    profilePrefix: _profilePrefix,
                    pattiyalNaal: _pattiyalNaal,
                    onInvNumberChanged: (v) {
                      setState(() {
                        _invoiceNumberOverride = v;
                        _hasUnsavedChanges = true;
                      });
                    },
                    onDateChanged: (d) => setState(() {
                      _pattiyalNaal = d;
                      _hasUnsavedChanges = true;
                    }),
                    onDirty: () =>
                        setState(() => _hasUnsavedChanges = true),
                  ),
                ],
              ),
            ),

            // ──────────────────────────────────────────────────────────
            // Section 3: ③ Items
            // ──────────────────────────────────────────────────────────
            _disabledWrap(
              isDisabled: isDisabled,
              child: ElvanEditorSection(
                index: baseIndex + 2,
                title: K.porutkal.tr(context, ref),
                displayChild: const SizedBox(),
                initiallyExpanded: true,
                contentTopPadding: 0,
                headerBottomPadding: 0,
                children: [
                  ElvanFullWidth(
                    child: ElvanAsaiPattiyal(
                      itemCount: _items.length,
                      itemBuilder: (context, i) {
                        return KooliUrupadiKooru(
                          key: ValueKey('item_$i'),
                          index: i,
                          item: _items[i],
                          itemCount: _items.length,
                          formatter: formatter,
                          onUpdated: (updated) {
                            setState(() {
                              _items = List.from(_items)..[i] = updated;
                              _hasUnsavedChanges = true;
                            });
                            _recalculateQuiet();
                          },
                          onDeleted: () => _deleteItem(i, formatter),
                          onRequestAddNewProduct: () async {
                            await Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const CoolieItemEditor(),
                              ),
                            );
                          },
                          onAddNewItem: () => setState(() {
                            _items = [..._items, const KooliUrupadi()];
                            _hasUnsavedChanges = true;
                          }),
                          onAddNewCharge: () => setState(() {
                            _piraVarivugal = [..._piraVarivugal, const PiraVarivu()];
                            _hasUnsavedChanges = true;
                          }),
                        );
                      },
                    ),
                  ),
                  ElvanFullWidth(
                    child: ElvanAsaiPattiyal(
                      itemCount: _piraVarivugal.length,
                      itemBuilder: (context, i) {
                        return KooliPiraVarivuKooru(
                          key: ValueKey('pira_$i'),
                          index: i,
                          charge: _piraVarivugal[i],
                          onUpdated: (updated) {
                            setState(() {
                              _piraVarivugal = List.from(_piraVarivugal)..[i] = updated;
                              _hasUnsavedChanges = true;
                            });
                            _recalculateQuiet();
                          },
                          onDeleted: () {
                            setState(() {
                              _piraVarivugal = List.from(_piraVarivugal)..removeAt(i);
                              _hasUnsavedChanges = true;
                            });
                            _recalculateQuiet();
                          },
                          onRecalculate: _recalculateQuiet,
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),

            // ──────────────────────────────────────────────────────────
            // Section 4: ④ Totals — INDEPENDENT from Section 3
            // ──────────────────────────────────────────────────────────
            _disabledWrap(
              isDisabled: isDisabled,
              child: ElvanEditorSection(
                index: baseIndex + 3,
                title: K.mothangal.tr(context, ref),
                displayChild: const SizedBox(),
                initiallyExpanded: true,
                children: [
                  // Extra Charges
                  KooliMelthogaiKooru(
                    setharamCtrl: _setharamCtrl,
                    ahimsaCtrl: _ahimsaCtrl,
                    thabaalCtrl: _thabaalCtrl,
                    onSetharamChanged: (v) {
                      _setharamGrams = double.tryParse(v) ?? 0;
                      _hasUnsavedChanges = true;
                      _recalculateQuiet();
                    },
                    onAhimsaChanged: (v) {
                      _ahimsaPattuThogai = double.tryParse(v) ?? 0;
                      _hasUnsavedChanges = true;
                      _recalculateQuiet();
                    },
                    onThabaalChanged: (v) {
                      _thabaalThogai = double.tryParse(v) ?? 0;
                      _hasUnsavedChanges = true;
                      _recalculateQuiet();
                    },
                  ),

                  // Totals Card
                  ElvanFullWidth(
                    child: ValueListenableBuilder<KooliMothangal>(
                      valueListenable: _totalsNotifier,
                      builder: (context, totals, _) {
                        return KooliMothangalKooru(
                          totals: totals,
                          setharamGrams: _setharamGrams,
                          ahimsaPattuThogai: _ahimsaPattuThogai,
                          thabaalThogai: _thabaalThogai,
                          piraVarivugal: _piraVarivugal,
                          formatter: formatter,
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // HELPERS
  // ═══════════════════════════════════════════════════════════════════════════

  /// Per-section disabled wrapper — isolates each section so that
  /// focus/rebuild in one cannot cause layout shifts in another.
  Widget _disabledWrap({required bool isDisabled, required Widget child}) {
    if (!isDisabled) return child;
    return Opacity(
      opacity: 0.4,
      child: IgnorePointer(child: child),
    );
  }

  /// Handles item deletion cleanly.
  void _deleteItem(int i, NumberFormat formatter) {
    setState(() {
      _items = List.from(_items)..removeAt(i);
      _hasUnsavedChanges = true;
    });
    _recalculate();
  }
}
