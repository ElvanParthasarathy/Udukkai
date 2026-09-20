import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/thiruthi/elvan_thiruthi_niruvanam_oadu.dart';
import '../../../../niril_podhu/kalanjiyam/pattu_pattiyal_kalanjiyam.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../../niril_podhu/kalanjiyam/pattiyal_kanakku.dart';
import '../../../../niril_podhu/kalanjiyam/pattiyal_kalanjiyam.dart';
import '../../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../vaangunar/niril_pattu_vaangunar_thiruthi.dart';
import '../porul/niril_pattu_porul_thiruthi.dart';
import 'koorugal/koorugal.dart';
import '../../../../niril_podhu/kaatchi/koorugal/elvan_pattiyal_tharavugal_kooru.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_asai_pattiyal.dart';
import '../../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../paarvai/pattu_pattiyal_paarvai.dart';


/// Silk (GST) Invoice Editor — full form with line items, tax calculation,
/// auto-numbering, and FK-based customer storage.
class SilkInvoiceEditor extends ConsumerStatefulWidget {
  final PattiyalTharavuru? editingEntry;
  final PattiyalTharavuru? duplicateFrom;

  const SilkInvoiceEditor({super.key, this.editingEntry, this.duplicateFrom});

  @override
  ConsumerState<SilkInvoiceEditor> createState() => _SilkInvoiceEditorState();
}

class _SilkInvoiceEditorState extends ConsumerState<SilkInvoiceEditor> {
  // ── Company Profile ──
  int? _selectedNiruvanamId;
  NiruvanaTharavugal? _selectedProfile;

  // ── Customer ──
  int? _selectedVaangunarId;
  String _selectedVaangunarPeyar = '';
  String _customerState = '';

  // ── Metadata ──
  String _pattiyalVagai = 'tax-invoice';
  DateTime _pattiyalNaal = DateTime.now();
  String _placeOfSupply = '';
  String _placeOfSupplyTa = ''; // Tamil bilingual for Place of Supply
  String _invoiceNumberOverride = ''; // Manual override for inv number
  String _previewInvoiceNumber = ''; // Preview of next auto-generated number

  // ── Line Items ──
  List<PattuUrupadi> _items = [const PattuUrupadi()];

  // ── Global Discount ──
  double _globalDiscountValue = 0;
  String _globalDiscountType = '%';



  // ── Calculated Totals ──
  PattuMothangal _totals = const PattuMothangal();

  // ── Controllers ──

  final _globalDiscountController = TextEditingController();
  bool _saving = false;

  // ── Unsaved Changes & Draft ──
  bool _hasUnsavedChanges = false;
  Timer? _draftDebounce;
  bool get _isEditing => widget.editingEntry != null;

  @override
  void initState() {
    super.initState();
    if (_isEditing) {
      _applySnapshot(
        PattuPattiyalUthavi.loadFromEntry(widget.editingEntry!),
      );
      _backfillItems();
    } else if (widget.duplicateFrom != null) {
      _applySnapshot(
        PattuPattiyalUthavi.loadFromEntry(widget.duplicateFrom!,
            isDuplicate: true),
      );
      _backfillItems();
    } else {
      _tryRestoreDraft();
    }
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_isEditing && _selectedNiruvanamId == null) {
        _computePreviewInvoiceNumber();
      }
      _resolveCustomerState();
      _recalculate();
    });
  }

  /// Applies a loaded snapshot to all state fields + controllers.
  void _applySnapshot(PattuThiruththiNilaimai s) {
    _selectedNiruvanamId = s.selectedNiruvanamId;
    _selectedVaangunarId = s.selectedVaangunarId;
    _selectedVaangunarPeyar = s.selectedVaangunarPeyarMap[ref.read(silkMudhanmaiMozhiProvider)] ?? s.selectedVaangunarPeyarMap[ref.read(silkThunaiMozhiProvider)] ?? s.selectedVaangunarPeyarMap.values.firstOrNull ?? '';
    _customerState = s.customerState;
    _pattiyalVagai = s.pattiyalVagai;
    _pattiyalNaal = s.pattiyalNaal;
    _placeOfSupply = s.placeOfSupply;
    _placeOfSupplyTa = s.placeOfSupplyTa;
    _invoiceNumberOverride = s.invoiceNumberOverride;
    _items = s.items.isNotEmpty ? s.items : [const PattuUrupadi()];
    _globalDiscountValue = s.globalDiscountValue;
    _globalDiscountType = s.globalDiscountType;
    _globalDiscountController.text =
        _globalDiscountValue > 0 ? _globalDiscountValue.toString() : '';
  }

  Future<void> _backfillItems() async {
    final products = await ref.read(porulgalProvider.future);
    bool changed = false;
    final newItems = _items.map((item) {
      if (item.porulPeyarEn.isEmpty && (item.porulId?.isNotEmpty == true)) {
        final product = products.where((p) => p.id.toString() == item.porulId).firstOrNull;
        if (product != null) {
          final enName = product.porulPeyar[ref.read(silkThunaiMozhiProvider)] ?? '';
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
      // Wait for next frame to safely rebuild AnimatedList if needed, though initialItemCount usually handles startup.
    }
  }


  /// Resolves _selectedProfile and _customerState from provider data.
  void _resolveCustomerState() {
    final profiles = ref.read(NiruvanaTharavugalListProvider);
    if (_selectedNiruvanamId != null) {
      final match =
          profiles.where((p) => p.id == _selectedNiruvanamId).firstOrNull;
      if (match != null) setState(() => _selectedProfile = match);
    }
    if (_selectedVaangunarId != null) {
      final vaangunargalData = ref.read(vaangunargalProvider);
      final vaangunargal =
          vaangunargalData.whenOrNull(data: (list) => list) ?? [];
      final vaangunar =
          vaangunargal.where((v) => v.id == _selectedVaangunarId).firstOrNull;
      if (vaangunar != null) {
        setState(() {
          _customerState = (vaangunar.maanilam['en'] ?? vaangunar.maanilam['en'] ??
                      vaangunar.maanilam[ref.read(silkMudhanmaiMozhiProvider)] ??
                      '')
                  .trim()
                  .toLowerCase();
          
          if (_placeOfSupply.isEmpty) {
            _placeOfSupply = (vaangunar.maanilam['en'] ?? vaangunar.maanilam['en'] ?? '').trim();
            _placeOfSupplyTa = (vaangunar.maanilam['ta'] ?? vaangunar.maanilam['ta'] ?? vaangunar.maanilam[ref.read(silkMudhanmaiMozhiProvider)] ?? '').trim();
          }
        });
      }
    }
  }

  Future<void> _computePreviewInvoiceNumber() async {
    if (_isEditing) return;
    try {
      final kalanjiyam = ref.read(pattiyalKalanjiyamProvider);
      final finYear = PattuPattiyalKalanjiyam.getCurrentFinYear();
      final prefix = _selectedProfile?.kurumPeyar.isNotEmpty == true
          ? _selectedProfile!.kurumPeyar
          : 'INV';
      final vanakkam = await kalanjiyam.getNextVanakkam(_selectedNiruvanamId, finYear);
      if (mounted) {
        setState(() {
          _previewInvoiceNumber =
              kalanjiyam.formatPattiyalEn(prefix, vanakkam);
        });
      }
    } catch (_) {}
  }

  @override
  void dispose() {

    _globalDiscountController.dispose();
    _draftDebounce?.cancel();
    super.dispose();
  }

  // ── Recalculate totals ──
  void _recalculate() {
    String businessState = '';
    if (_selectedProfile != null) {
      businessState = (_selectedProfile!.maanilam['en'] ??
              _selectedProfile!.maanilam[ref.read(silkMudhanmaiMozhiProvider)] ??
              '')
          .trim()
          .toLowerCase();
    }
    final effectiveCustomerState = _placeOfSupply.isNotEmpty
        ? _placeOfSupply.toLowerCase()
        : _customerState;

    setState(() {
      _totals = PattuKanakku.calculate(
        items: _items,
        globalDiscountValue: _globalDiscountValue,
        globalDiscountType: _globalDiscountType,
        businessState: businessState,
        customerState: effectiveCustomerState,
        country: 'India',
      );
    });
    _scheduleDraftSave();
  }

  // ── Draft (delegates to helper) ──
  void _scheduleDraftSave() {
    _draftDebounce?.cancel();
    _draftDebounce = Timer(const Duration(seconds: 2), () {
      if (_isEditing) return;
      PattuPattiyalUthavi.saveDraft(_currentSnapshot());
    });
  }

  Future<void> _tryRestoreDraft() async {
    final snapshot = await PattuPattiyalUthavi.tryRestoreDraft(context, ref);
    if (snapshot != null && mounted) {
      setState(() => _applySnapshot(snapshot));
      _backfillItems();
      _resolveCustomerState();
      _recalculate();
    }
  }

  /// Builds a snapshot from current state (for draft / save).
  PattuThiruththiNilaimai _currentSnapshot() => PattuThiruththiNilaimai(
        selectedNiruvanamId: _selectedNiruvanamId,
        selectedVaangunarId: _selectedVaangunarId,
        selectedVaangunarPeyarMap: {ref.read(silkMudhanmaiMozhiProvider): _selectedVaangunarPeyar, ref.read(silkThunaiMozhiProvider): _selectedVaangunarPeyar},
        customerState: _customerState,
        pattiyalVagai: _pattiyalVagai,
        pattiyalNaal: _pattiyalNaal,
        placeOfSupply: _placeOfSupply,
        placeOfSupplyTa: _placeOfSupplyTa,
        invoiceNumberOverride: _invoiceNumberOverride,
        items: _items,
        globalDiscountValue: _globalDiscountValue,
        globalDiscountType: _globalDiscountType,

      );

  // ── Save (delegates to helper) ──
  Future<void> _handleSave() async {
    // Unfocus active field → triggers blur commit on ItemFieldWidget
    FocusManager.instance.primaryFocus?.unfocus();
    // Let blur handler run before we read field values
    await Future.delayed(const Duration(milliseconds: 50));
    if (!mounted) return;

    if (_selectedVaangunarId == null && _selectedVaangunarPeyar.isEmpty) {
      ElvanSnackbar.show(context, K.vaangunaraiThaerodhu.tr(context, ref));
      return;
    }
    final validItems =
        _items.where((i) => i.alavu > 0 && i.vilai > 0).toList();
    if (validItems.isEmpty) {
      ElvanSnackbar.show(context, K.kuriaindhOruPorul.tr(context, ref));
      return;
    }

    setState(() => _saving = true);
    try {
      final kalanjiyam = ref.read(pattiyalKalanjiyamProvider);
      final prefix = _selectedProfile?.kurumPeyar.isNotEmpty == true
          ? _selectedProfile!.kurumPeyar
          : 'INV';

      final result = await PattuPattiyalUthavi.save(
        kalanjiyam: kalanjiyam,
        state: _currentSnapshot(),
        totals: _totals,
        profilePrefix: prefix,
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

      await PattuPattiyalUthavi.clearDraft();
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
            builder: (_) => PattuPattiyalPaarvai(
              pattiyal: savedPattiyal,
            ),
          ),
        );
      }
    } catch (e) {
      if (mounted) ElvanSnackbar.show(context, 'Error: $e');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // ── UI ── (Rewritten to match React InvoiceEditorV2)
  // ═══════════════════════════════════════════════════════════════════════════

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    // Resolve selected customer from stream for "Saved Details" card
    final vaangunargalAsync = ref.watch(vaangunargalProvider);
    final VaangunarTharavuru? selectedVaangunar = vaangunargalAsync.whenOrNull(
      data: (list) => _selectedVaangunarId != null
          ? list.cast<VaangunarTharavuru?>().firstWhere(
                (v) => v!.id == _selectedVaangunarId,
                orElse: () => null,
              )
          : null,
    );

    final profiles = ref.watch(NiruvanaTharavugalListProvider);
    final baseIndex = profiles.length > 1 ? 1 : 0;

    return ElvanEditorShell(
      title: _isEditing
          ? K.maatriyamai.tr(context, ref)
          : K.pudhiyaAakkam.tr(context, ref),
      onSave: _saving ? null : _handleSave,
      hasUnsavedChanges: _hasUnsavedChanges,
      onDiscard: () async {
        await PattuPattiyalUthavi.clearDraft();
      },
      child: ElvanThiruthiNiruvanamOadu(
        selectedNiruvanamId: _selectedNiruvanamId,
        onChanged: (p) {
          setState(() {
            _selectedNiruvanamId = p?.id;
            _selectedProfile = p;
          });
          _recalculate();
          _computePreviewInvoiceNumber();
        },
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
          // ───────────────────────────────────────────────────────────────
          // Section 1: ① Billed To
          // ───────────────────────────────────────────────────────────────
          ElvanEditorSection(
            index: baseIndex,
            title: K.perunar.tr(context, ref),
            displayChild: const SizedBox(),
            initiallyExpanded: true,
            children: [
              PattuVaangunargalKooru(
                data: PattuVaangunargalData(
                  selectedVaangunarId: _selectedVaangunarId,
                  selectedVaangunarPeyarMap: {ref.watch(silkMudhanmaiMozhiProvider): _selectedVaangunarPeyar, ref.watch(silkThunaiMozhiProvider): _selectedVaangunarPeyar},
                  placeOfSupply: _placeOfSupply,
                  placeOfSupplyTa: _placeOfSupplyTa,
                ),
                callbacks: PattuVaangunargalCallbacks(
                  onCustomerSelected: (entry) {
                    setState(() {
                      _selectedVaangunarId = entry.id;
                      _selectedVaangunarPeyar =
                          entry.peyar[ref.read(silkMudhanmaiMozhiProvider)] ?? entry.peyar[ref.read(silkThunaiMozhiProvider)] ?? entry.peyar.values.firstOrNull ?? '';
                      _customerState = (entry.maanilam['en'] ?? entry.maanilam['en'] ??
                                  entry.maanilam[ref.read(silkMudhanmaiMozhiProvider)] ??
                                  '')
                              .trim()
                              .toLowerCase();
                      
                      _placeOfSupply = (entry.maanilam['en'] ?? entry.maanilam['en'] ?? '').trim();
                      _placeOfSupplyTa = (entry.maanilam['ta'] ?? entry.maanilam['ta'] ?? entry.maanilam[ref.read(silkMudhanmaiMozhiProvider)] ?? '').trim();
                    });
                    _hasUnsavedChanges = true;
                    _recalculate();
                  },
                  onCustomerCleared: () {
                    setState(() {
                      _selectedVaangunarId = null;
                      _selectedVaangunarPeyar = '';
                      _customerState = '';
                      _placeOfSupply = '';
                      _placeOfSupplyTa = '';
                    });
                    setState(() => _hasUnsavedChanges = true);
                    _recalculate();
                  },
                  onRequestAddNewCustomer: () async {
                    await Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => const SilkMerchantEditor(),
                      ),
                    );
                  },

                  onPlaceOfSupplyChanged: (en, ta) {
                    setState(() {
                      _placeOfSupply = en;
                      _placeOfSupplyTa = ta;
                    });
                    _recalculate();
                  },
                  onPlaceOfSupplyCleared: () {
                    setState(() {
                      _placeOfSupply = '';
                      _placeOfSupplyTa = '';
                    });
                    _recalculate();
                  },
                ),
                selectedVaangunar: selectedVaangunar,
              ),
            ],
          ),

          const SizedBox(height: 24),

          // ── Disabled wrapper when no company selected ──
          Opacity(
            opacity: _selectedNiruvanamId == null ? 0.4 : 1.0,
            child: IgnorePointer(
              ignoring: _selectedNiruvanamId == null,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
          ElvanEditorSection(
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
                previewInvoiceNumber: _previewInvoiceNumber,
                profilePrefix: _selectedProfile?.kurumPeyar.isNotEmpty == true
                    ? '${_selectedProfile!.kurumPeyar}-'
                    : 'INV-',
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
                onDirty: () => setState(() => _hasUnsavedChanges = true),
              ),
              
              // ─── Place of Supply ───
              ElvanFullWidth(
                child: PattuVilippiIdam(
                  placeOfSupply: _placeOfSupply,
                  placeOfSupplyTa: _placeOfSupplyTa,
                  onSelected: (en, ta) {
                    setState(() {
                      _placeOfSupply = en;
                      _placeOfSupplyTa = ta;
                    });
                    _recalculate();
                  },
                  onCleared: () {
                    setState(() {
                      _placeOfSupply = '';
                      _placeOfSupplyTa = '';
                    });
                    _recalculate();
                  },
                ),
              ),
            ],
          ),

          const SizedBox(height: 24),

          // ───────────────────────────────────────────────────────────────
          // Section 3: ③ Line Items (uses PattuUrupadiAttai component)
          // ───────────────────────────────────────────────────────────────
          ElvanEditorSection(
            index: baseIndex + 2,
            title: K.porutkal.tr(context, ref),
            displayChild: const SizedBox(),
            initiallyExpanded: true,
            contentTopPadding: 0,
            headerBottomPadding: 0,
            children: [
              ElvanFullWidth(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    ElvanAsaiPattiyal(
                      itemCount: _items.length,
                      itemBuilder: (context, i) {
                        return PattuUrupadiAttai(
                          key: ValueKey('item_$i'),
                          item: _items[i],
                          index: i,
                          itemCount: _items.length,
                          seyaliVagai: 'silk',
                          onItemUpdated: (updated) {
                            setState(() {
                              _items = List.from(_items)..[i] = updated;
                              _hasUnsavedChanges = true;
                            });
                            _recalculate();
                          },
                          onItemDeleted: () {
                            setState(() {
                              _items = List.from(_items)..removeAt(i);
                              _hasUnsavedChanges = true;
                            });
                            _recalculate();
                          },
                          onItemCleared: () {
                            setState(() {
                              _items = List.from(_items)
                                ..[i] = const PattuUrupadi();
                              _hasUnsavedChanges = true;
                            });
                            _recalculate();
                          },
                          onDirty: () {
                            setState(() {
                              _hasUnsavedChanges = true;
                            });
                          },
                          onRequestAddNewProduct: () async {
                            await Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => const SilkItemEditor(),
                              ),
                            );
                          },
                          onAddNewItem: () => setState(() {
                            _items = [..._items, const PattuUrupadi()];
                            _hasUnsavedChanges = true;
                          }),
                        );
                      },
                    ),


                  ],
                ),
              ),
            ],
          ),

          // ───────────────────────────────────────────────────────────────
          // Section 4: ④ Totals (uses PattuMothangalKooru component)
          // ───────────────────────────────────────────────────────────────
          ElvanEditorSection(
            index: baseIndex + 3,
            title: K.mothangal.tr(context, ref),
            displayChild: const SizedBox(),
            initiallyExpanded: true,
            children: [
              PattuThallupadiKooru(
                controller: _globalDiscountController,
                discountType: _globalDiscountType,
                onValueChanged: (v) {
                  _globalDiscountValue = double.tryParse(v) ?? 0;
                  _recalculate();
                },
                onTypeChanged: (type) {
                  setState(() => _globalDiscountType = type);
                  _recalculate();
                },
              ),
              const SizedBox(height: 24),
              ElvanFullWidth(
                child: PattuMothangalKooru(totals: _totals),
              ),
            ],
          ),

          // ───────────────────────────────────────────────────────────────
          // Section 5: ⑤ Invoice Type
          // ───────────────────────────────────────────────────────────────
          ElvanEditorSection(
            index: baseIndex + 4,
            title: K.pattiyalVagai.tr(context, ref),
            displayChild: const SizedBox(),
            initiallyExpanded: true,
            children: [
              ElvanFullWidth(
                child: PattuPattiyalVagaiKooru(
                  pattiyalVagai: _pattiyalVagai,
                  onChanged: (v) {
                    final newType = v ?? 'tax-invoice';
                    setState(() => _pattiyalVagai = newType);
                    setState(() => _hasUnsavedChanges = true);
                    _scheduleDraftSave();
                  },
                ),
              ),
            ],
          ),

          const SizedBox(height: 24),
                ],
              ),
            ),
          ),
        ],
      ),
    ),
  );
  }
}
