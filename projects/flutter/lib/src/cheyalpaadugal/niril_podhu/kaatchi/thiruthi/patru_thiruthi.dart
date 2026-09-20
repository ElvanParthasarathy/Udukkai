import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_pothan.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_thalaippu.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import 'koorugal/elvan_thiruthi_paguthi.dart';
import 'koorugal/elvan_thiruthi_keezhvirivu.dart';
import '../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_niruvanam_oadu.dart';
import '../../../niril_podhu/kalanjiyam/patru_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../../niril_podhu/tharavuru/seluthi_vagai.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import 'koorugal/patru_pattiyal_theervu_maeladukku.dart';
import 'koorugal/patru_thiruthi_paguthigal.dart';
import '../koorugal/pattiyal_naal_kooru.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../koorugal/ulleedugal/elvan_aavana_enn_kooru.dart';
import '../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

/// Shared Receipt Editor — used by both Coolie and Silk modes.
/// Three sections: ① Invoice picker, ② Receipt data, ③ Payment details.
class PatruThiruthi extends ConsumerStatefulWidget {
  final PatrugalTharavuru? editingEntry;
  final void Function(BuildContext context, PatrugalTharavuru savedPatru)? onSaved;

  const PatruThiruthi({
    super.key, 
    this.editingEntry,
    this.onSaved,
  });

  @override
  ConsumerState<PatruThiruthi> createState() => _PatruThiruthiState();
}

class _PatruThiruthiState extends ConsumerState<PatruThiruthi> {
  // ── Company Profile ──
  int? _selectedNiruvanamId;
  NiruvanaTharavugal? _selectedProfile;

  // ── Customer ──
  int? _selectedVaangunarId;
  Map<String, String> _vaangunarPeyarMap = {};
  Map<String, String> _vaangunarMunvariMap = {};

  // ── Receipt Data ──
  DateTime _patruNaal = DateTime.now();
  String _patruEn = '';
  int _vanakkam = 1;

  // ── Payment ──
  double _thogai = 0.0;
  SeluthiVagai? _seluthiVagai = SeluthiVagai.vangiMaatram;
  String _suttruEn = '';
  String _ullkurippu = '';

  // ── Linked Invoices ──
  List<PattiyalTharavuru> _selectedInvoices = [];

  // ── Controllers ──
  final _thogaiCtrl = TextEditingController();
  final _suttruEnCtrl = TextEditingController();
  final _ullkurippuCtrl = TextEditingController();
  final _patruEnCtrl = TextEditingController();

  bool _isSaving = false;
  bool _isInitialized = false;
  bool _isLoadingInvoices = false;
  bool _isPatruEnEditing = false;
  String _previewPatruEn = '';

  @override
  void initState() {
    super.initState();
    _initializeForm();
    _loadLinkedInvoices();
  }

  void _initializeForm() {
    final entry = widget.editingEntry;
    if (entry != null) {
      _selectedNiruvanamId = entry.niruvanamId;
      _selectedVaangunarId = entry.vaangunarId;
      _vaangunarPeyarMap = entry.vaangunarPeyar.cast<String, String>();
      _patruNaal = entry.patruNaal;
      _patruEn = entry.patruEn;
      _vanakkam = entry.vanakkam;
      _thogai = entry.thogai;
      _seluthiVagai = SeluthiVagaiX.fromStored(entry.seluthumMurai);
      _suttruEn = entry.parivarthanaiEn; // Map parivarthanaiEn to UI's suttruEn
      _ullkurippu = entry.ullkurippu;

      _thogaiCtrl.text = _thogai > 0 ? _thogai.toStringAsFixed(2) : '';
      _suttruEnCtrl.text = _suttruEn;
      _ullkurippuCtrl.text = _ullkurippu;
      _patruEnCtrl.text = _patruEn;
    } else {
      // New receipt: Do NOT pre-select business profile (user requested manual selection)
    }
  }

  @override
  void dispose() {
    _thogaiCtrl.dispose();
    _suttruEnCtrl.dispose();
    _ullkurippuCtrl.dispose();
    _patruEnCtrl.dispose();
    super.dispose();
  }

  // ── Load linked invoices for editing ──
  Future<void> _loadLinkedInvoices() async {
    if (widget.editingEntry == null || _isInitialized) return;
    _isInitialized = true;

    if (mounted) setState(() => _isLoadingInvoices = true);

    final kalanjiyam = ref.read(patruKalanjiyamProvider);
    final links = await kalanjiyam.getLinksForPatru(widget.editingEntry!.id);
    final pattiyalKal = ref.read(pattiyalKalanjiyamProvider);

    final futures = links.map((link) => pattiyalKal.getById(link.pattiyalId));
    final results = await Future.wait(futures);
    final invoices = results.whereType<PattiyalTharavuru>().toList();

    if (mounted) {
      setState(() {
        _selectedInvoices = invoices;
        _isLoadingInvoices = false;
      });
      _autoFillFromInvoices();
    }
  }

  // ── Auto-generate receipt number ──
  Future<void> _generatePatruEn() async {
    if (widget.editingEntry != null) return; // Don't regenerate for edits

    final kalanjiyam = ref.read(patruKalanjiyamProvider);

    String bizShort = '';
    if (_selectedProfile?.kurumPeyar.isNotEmpty == true) {
      bizShort = _selectedProfile!.kurumPeyar;
    } else {
      final bizName = _selectedProfile?.niruvanathinPeyar['en'] ??
          _selectedProfile?.niruvanathinPeyar['ta'] ??
          'BIZ';
      bizShort = bizName
          .split(' ')
          .where((w) => w.trim().isNotEmpty)
          .map((w) => w[0])
          .join('')
          .toUpperCase();
      if (bizShort.length > 4) bizShort = bizShort.substring(0, 4);
      if (bizShort.isEmpty) bizShort = 'BIZ';
    }

    _vanakkam = await kalanjiyam.getNextVanakkam(_selectedNiruvanamId);

    // Format is RCP/bizShort/01
    final formatted = kalanjiyam.formatPatruEn(bizShort, _vanakkam);
    if (_isPatruEnEditing) {
      _previewPatruEn = formatted;
    } else {
      _patruEn = formatted;
      _previewPatruEn = '';
    }

    if (mounted) setState(() {});
  }

  // ── Save ──
  Future<void> _handleSave() async {
    // Validation
    if (_selectedProfile == null ||
        _selectedProfile!.kurumPeyar.trim().isEmpty) {
      ElvanSnackbar.show(context,
          '${K.amaippugal.tr(context, ref)} - Kurum Peyar is required to save receipts.');
      return;
    }

    if (_selectedInvoices.isEmpty) {
      ElvanSnackbar.show(context, K.pattiyalaiThaernhedu.tr(context, ref));
      return;
    }

    final peyarTamil = _vaangunarPeyarMap['ta'] ?? '';
    if (peyarTamil.trim().isEmpty) {
      ElvanSnackbar.show(context, K.vaangunarPeyarThaevai.tr(context, ref));
      return;
    }
    if (_thogai <= 0) {
      ElvanSnackbar.show(context,
          K.thogaiChuzhiyaththaiVidaMigudhiyaagaIrukkaVaendum.tr(context, ref));
      return;
    }
    if (_seluthiVagai == null) {
      ElvanSnackbar.show(context, K.cheluthumMuraiThaernhedu.tr(context, ref));
      return;
    }

    if (_seluthiVagai == SeluthiVagai.panam) {
      _suttruEn = ''; // Cash logic: clear reference number
    }

    setState(() => _isSaving = true);

    try {
      final kalanjiyam = ref.read(patruKalanjiyamProvider);

      // Build junction links — distribute amount across invoices (oldest first)
      final links = <PatruPattiyalInaippuTharavuru>[];
      double remaining = _thogai;

      for (final inv in _selectedInvoices) {
        if (remaining <= 0) break;

        final apply = remaining.clamp(0.0, inv.mothaThogai);

        if (apply > 0) {
          links.add(PatruPattiyalInaippuTharavuru(
            pattiyalId: inv.id,
            poruthiyaThogai: apply,
          ));
          remaining -= apply;
        }
      }

      // Validate links
      final validationError = await kalanjiyam.validateLinks(
        links,
        excludePatruId: widget.editingEntry?.id,
      );
      if (validationError != null) {
        if (mounted) {
          ElvanSnackbar.show(context, validationError);
          setState(() => _isSaving = false);
        }
        return;
      }

      // Check for duplicate receipt number
      final isDuplicate = await kalanjiyam.isPatruEnDuplicate(
        _selectedNiruvanamId,
        _patruEn,
        excludeId: widget.editingEntry?.id,
      );

      if (isDuplicate) {
        if (mounted) {
          ElvanSnackbar.show(context,
              '$_patruEn - ${K.patrucheettuEnYaerkanavaeUlladhu.tr(context, ref)}');
          setState(() => _isSaving = false);
        }
        return;
      }

      // Build receipt companion
      final companion = PatrugalTharavuru(
        id: widget.editingEntry?.id ?? 0,
        niruvanamId: _selectedNiruvanamId,
        patruEn: _patruEn,
        vanakkam: _vanakkam,
        finYear: widget.editingEntry?.finYear ?? DateTime.now().year,
        vaangunarId: _selectedVaangunarId,
        vaangunarPeyar: _vaangunarPeyarMap,
        patruNaal: _patruNaal,
        thogai: _thogai,
        seluthumMurai: _seluthiVagai!.storedValue,
        vangiPeyar: widget.editingEntry?.vangiPeyar ?? '',
        parivarthanaiEn: widget.editingEntry?.parivarthanaiEn ?? '',
        ullkurippu: _ullkurippu,
        createdAt: widget.editingEntry?.createdAt ?? DateTime.now(),
        updatedAt: DateTime.now(),
        isDeleted: false,
      );

      int finalId = widget.editingEntry?.id ?? 0;
      if (widget.editingEntry != null) {
        await kalanjiyam.updatePatru(widget.editingEntry!.id, companion, links);
      } else {
        finalId = await kalanjiyam.insertPatru(companion, links);
      }

      final savedCompanion = PatrugalTharavuru(
        id: finalId,
        niruvanamId: companion.niruvanamId,
        patruEn: companion.patruEn,
        vanakkam: companion.vanakkam,
        finYear: companion.finYear,
        vaangunarId: companion.vaangunarId,
        vaangunarPeyar: companion.vaangunarPeyar,
        patruNaal: companion.patruNaal,
        thogai: companion.thogai,
        seluthumMurai: companion.seluthumMurai,
        vangiPeyar: companion.vangiPeyar,
        parivarthanaiEn: companion.parivarthanaiEn,
        ullkurippu: companion.ullkurippu,
        createdAt: companion.createdAt,
        updatedAt: companion.updatedAt,
        isDeleted: companion.isDeleted,
      );

      if (mounted) {
        ref.invalidate(patrugalProvider);
        ref.invalidate(pattiyalgalProvider);
        ElvanSnackbar.show(
          context,
          K.patrucheettuChaemikkappattadhu.tr(context, ref),
          showAboveNavbar: true,
        );
        if (widget.onSaved != null) {
          widget.onSaved!(context, savedCompanion);
        } else {
          Navigator.of(context).pop();
        }
      }
    } catch (e) {
      if (mounted) {
        ElvanSnackbar.show(
            context, '${K.chaemikkaIyalavillai.tr(context, ref)} $e');
      }
    } finally {
      if (mounted) setState(() => _isSaving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final invoicesAsync = ref.watch(pattiyalgalProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    if (widget.editingEntry == null &&
        _patruEn.isEmpty &&
        _selectedNiruvanamId != null) {
      _generatePatruEn();
    }

    final isEditing = widget.editingEntry != null;
    final title = isEditing
        ? K.maatriyamai.tr(context, ref)
        : K.pudhiyaAakkam.tr(context, ref);

    final profiles = ref.watch(NiruvanaTharavugalListProvider);
    final baseIndex = profiles.length > 1 ? 1 : 0;

    return ElvanEditorShell(
      title: title,
      onSave: _isSaving ? null : _handleSave,
      child: ElvanThiruthiNiruvanamOadu(
        selectedNiruvanamId: _selectedNiruvanamId,
        onChanged: (p) {
          setState(() {
            _selectedNiruvanamId = p?.id;
            _selectedProfile = p;
          });
          if (p != null) {
            _generatePatruEn();
          }
        },
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // ── Section 1: Linked Invoice (Required) ──
            ElvanEditorSection(
              index: baseIndex,
              title: K.endhapPattiyalukku.tr(context, ref),
              displayChild: const SizedBox(),
              initiallyExpanded: true,
              children: [
                invoicesAsync.isLoading
                    ? const Center(
                        child: Padding(
                          padding: EdgeInsets.all(24.0),
                          child: CupertinoActivityIndicator(),
                        ),
                      )
                    : _buildInvoicePickerSection(invoicesAsync, isDark),
              ],
            ),

            // ── Section 2: Receipt Data ──
            ElvanEditorSection(
              index: baseIndex + 1,
              title: K.patrucheettuTharavugal.tr(context, ref),
              displayChild: const SizedBox(),
              initiallyExpanded: true,
              children: _buildReceiptDataFields(isDark),
            ),

            // ── Section 3: Payment Details ──
            ElvanEditorSection(
              index: baseIndex + 2,
              title: K.cheluthiyaTharavu.tr(context, ref),
              displayChild: const SizedBox(),
              initiallyExpanded: true,
              children: _buildPaymentFields(isDark),
            ),
          ],
        ),
      ),
    );
  }

  // ── Section 1: Invoice Picker ──
  Widget _buildInvoicePickerSection(
      AsyncValue<List<PattiyalTharavuru>> invoicesAsync, bool isDark) {
    return PatruPattiyalTheervuPagudhi(
      selectedInvoices: _selectedInvoices,
      isDark: isDark,
      onPickInvoices: () => _showInvoicePickerDialog(invoicesAsync),
      onRemoveInvoice: (inv) {
        setState(() {
          _selectedInvoices.remove(inv);
          _recalculateAmount();
        });
      },
    );
  }

  // ── Section 2: Receipt Data ──
  List<Widget> _buildReceiptDataFields(bool isDark) {
    String profilePrefix = 'RCP/BIZ/';
    if (_patruEn.isNotEmpty) {
      // If we already have a receipt number (e.g., editing), extract the prefix directly
      // by removing all trailing digits. E.g., 'RCP/SJPS/04' -> 'RCP/SJPS/'
      profilePrefix = _patruEn.replaceAll(RegExp(r'\d+$'), '');
    } else {
      String bizShort = '';
      if (_selectedProfile?.kurumPeyar.isNotEmpty == true) {
        bizShort = _selectedProfile!.kurumPeyar;
      } else {
        final bizName = _selectedProfile?.niruvanathinPeyar['en'] ??
            _selectedProfile?.niruvanathinPeyar['ta'] ??
            'BIZ';
        bizShort = bizName
            .split(' ')
            .where((w) => w.trim().isNotEmpty)
            .map((w) => w[0])
            .join('')
            .toUpperCase();
        if (bizShort.length > 4) bizShort = bizShort.substring(0, 4);
        if (bizShort.isEmpty) bizShort = 'BIZ';
      }
      profilePrefix = 'RCP/$bizShort/';
    }

    final tt = Theme.of(context).textTheme;
    final cs = Theme.of(context).colorScheme;

    return [
      // 1. Receipt Date
      Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ElvanThiruthiThalaippu(label: K.patrucheettuNaal.tr(context, ref)),
          PattiyalNaalKooru(
            selectedDate: _patruNaal,
            onDateChanged: (date) {
              setState(() => _patruNaal = date);
            },
          ),
        ],
      ),

      // 2. Customer Selector / Info
      Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.only(left: 16, bottom: 8),
            child: Text(
              K.vaangunar.tr(context, ref),
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w400,
                letterSpacing: 0.3,
                color: cs.onSurface.withValues(alpha: 0.5),
              ),
            ),
          ),
          if (_selectedInvoices.isNotEmpty)
            _buildCustomerCard(isDark, isLocked: true)
          else
            ElvanThiruthiPothan(
              onTap: null,
              child: Row(
                children: [
                  Icon(CupertinoIcons.info_circle_fill,
                      size: 18,
                      color: isDark ? Colors.white54 : Colors.black45),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      K.pattiyalThaervukkuPinVaangunarTharavugalNirappappadum
                          .tr(context, ref),
                      style: TextStyle(
                          fontSize: 14,
                          color: isDark ? Colors.white70 : Colors.black87),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),

      // 3. Receipt Number (locked by default, tap edit to unlock)
      Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          ElvanAavanaEnnKooru(
            label: K.patrucheettuEn.tr(context, ref),
            prefix: profilePrefix,
            initialFullNumber: _patruEn.isNotEmpty ? _patruEn : _previewPatruEn,
            onFullNumberChanged: (v) {
              setState(() {
                _patruEn = v;
                _previewPatruEn = v;
              });
            },
          ),
        ],
      ),
    ];
  }

  // ── Helper: Customer Info Card ──
  Widget _buildCustomerCard(bool isDark, {required bool isLocked}) {
    return ElvanThiruthiPothan(
      onTap: null,
      child: Row(
        children: [
          Expanded(
            child: Text(
              _vaangunarPeyarMap['ta'] ?? '',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: Theme.of(context).colorScheme.onSurface,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          if (isLocked)
            Icon(CupertinoIcons.lock_fill,
                size: 16, color: isDark ? Colors.white30 : Colors.black26)
          else
            GestureDetector(
              onTap: () {
                setState(() {
                  _selectedVaangunarId = null;
                  _vaangunarPeyarMap.clear();
                  _vaangunarMunvariMap.clear();
                });
              },
              child: Icon(CupertinoIcons.clear_circled_solid,
                  size: 18, color: isDark ? Colors.white54 : Colors.black45),
            ),
        ],
      ),
    );
  }

  // ── Section 3: Payment Details ──
  List<Widget> _buildPaymentFields(bool isDark) {
    return [
      // Amount field
      ElvanThiruthiUlleedu(
        controller: _thogaiCtrl,
        label: K.thogaiVinmeen.tr(context, ref),
        prefixText: '₹ ',
        keyboardType: const TextInputType.numberWithOptions(decimal: true),
        inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
        onChanged: (val) {
          _thogai = double.tryParse(val) ?? 0.0;
        },
      ),

      // Payment mode dropdown
      ElvanThiruthiKeezhvirivu<SeluthiVagai>(
        label: K.cheluthumMuraiVinmeen.tr(context, ref),
        value: _seluthiVagai,
        items: SeluthiVagai.values,
        itemLabelBuilder: (ctx, ref, mode) => mode.label(ctx, ref),
        leadingBuilder: (ctx, ref, mode) => Icon(mode.icon,
            size: 18,
            color: Theme.of(ctx).colorScheme.onSurface.withValues(alpha: 0.7)),
        onChanged: (val) {
          setState(() => _seluthiVagai = val);
        },
      ),

      // Reference number (shown only when not Cash)
      if (_seluthiVagai != null && _seluthiVagai!.needsReference)
        ElvanThiruthiUlleedu(
          controller: _suttruEnCtrl,
          label: K.kurippuEnParimaatraEn.tr(context, ref),
          onChanged: (val) => _suttruEn = val,
        ),

      // Note
      ElvanFullWidth(
        child: Builder(
          builder: (context) {
            final isDesktop = MediaQuery.sizeOf(context).width >= 800;
            final noteField = ElvanThiruthiUlleedu(
              controller: _ullkurippuCtrl,
              label: K.kurippu.tr(context, ref),
              maxLines: 3,
              onChanged: (val) => _ullkurippu = val,
            );

            if (isDesktop) {
              return Row(
                children: [
                  Expanded(child: noteField),
                  const SizedBox(width: 16),
                  const Expanded(child: SizedBox()),
                ],
              );
            }
            return noteField;
          }
        ),
      ),
    ];
  }

  // ── Invoice Picker Dialog ──
  void _showInvoicePickerDialog(
      AsyncValue<List<PattiyalTharavuru>> invoicesAsync) {
    final allInvoices = invoicesAsync.value ?? [];
    var filtered = allInvoices;
    if (_selectedNiruvanamId != null) {
      filtered = filtered.where((i) => i.niruvanamId == _selectedNiruvanamId).toList();
    }

    PatruPattiyalTheervuMaeladukku.show(
      context: context,
      ref: ref,
      invoices: filtered,
      initialSelectedIds: Set<int>.from(_selectedInvoices.map((i) => i.id)),
      onConfirmed: (selected) {
        setState(() {
          _selectedInvoices = selected;
          _recalculateAmount();
          _autoFillFromInvoices();
        });
      },
    );
  }

  // ── Helpers ──

  void _recalculateAmount() {
    if (_selectedInvoices.isEmpty) {
      _thogaiCtrl.text = '';
      _thogai = 0.0;
      return;
    }

    double total = 0;
    for (final inv in _selectedInvoices) {
      total += inv.mothaThogai;
    }

    _thogai = total;
    _thogaiCtrl.text = total > 0
        ? (total.truncateToDouble() == total
            ? total.toInt().toString()
            : total.toStringAsFixed(2))
        : '';
  }

  void _autoFillFromInvoices() {
    if (_selectedInvoices.isEmpty) return;
    final first = _selectedInvoices.first;

    // Auto-fill customer from first invoice (if not already set)
    if (_selectedVaangunarId == null && first.vaangunarId != null) {
      _selectedVaangunarId = first.vaangunarId;
      _vaangunarPeyarMap = first.vaangunarPeyar.cast<String, String>();
      _vaangunarMunvariMap = first.vaangunarMunvari.cast<String, String>();
    }
  }
}
