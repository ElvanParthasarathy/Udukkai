import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../../koorugal/pulan_koorugal/elvan_irumozhi_pulan.dart';
import '../../../../../koorugal/pulan_koorugal/elvan_irumozhi_autocomplete.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import '../../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../../../niril_podhu/kaatchi/paarvai/vaangunar_paarvai.dart';
import '../../../../../adippadai/idangal_kalanjiyam/idangal_kalanjiyam.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';

// ─────────────────────────────────────────────────────────────────────────────
// SILK / GST MERCHANT EDITOR
// ─────────────────────────────────────────────────────────────────────────────
// 3 sections:
//   ① Business Details   — Bilingual name (required)
//   ② Address            — India: full fields + state dropdown
//                          Other: multiline bilingual address
//   ③ Contact & Tax      — GSTIN (validated), email, phone
// ─────────────────────────────────────────────────────────────────────────────

class SilkMerchantEditor extends ConsumerStatefulWidget {
  final VaangunarTharavuru? vaangunar;
  final void Function(BuildContext context, VaangunarTharavuru savedVaangunar)? onSaved;

  const SilkMerchantEditor({
    super.key, 
    this.vaangunar,
    this.onSaved,
  });

  @override
  ConsumerState<SilkMerchantEditor> createState() =>
      _SilkMerchantEditorState();
}

class _SilkMerchantEditorState extends ConsumerState<SilkMerchantEditor> {

  // ── Bilingual fields ──
  Map<String, String> _peyar = {};
  Map<String, String> _mugavari = {};
  Map<String, String> _oor = {};
  Map<String, String> _maavattam = {};
  Map<String, String> _maanilam = {};
  Map<String, String> _naadu = {};
  Map<String, String> _velinaadMugavari = {};

  // ── Single-value fields ──
  final _anjalKuriyeeduController = TextEditingController();
  final _gstinController = TextEditingController();
  final _minnanjalController = TextEditingController();
  final _tholaipaesiController = TextEditingController();

  String? _gstinError;

  bool get _isIndia {
    if (_naadu.isEmpty) return true; // Default: India
    // Check both key systems ('en'/'ta' from picker, 'Tamil'/'English' from DB)
    final enName = (_naadu['en'] ?? _naadu['en'] ?? '').trim().toLowerCase();
    final taName = (_naadu['ta'] ?? _naadu['ta'] ?? '').trim();
    
    if (enName.isEmpty && taName.isEmpty) return true;
    return enName == 'india' || taName == K.india.tr(context, ref);
  }

  @override
  void initState() {
    super.initState();
    if (widget.vaangunar != null) {
      final v = widget.vaangunar!;
      _peyar = Map<String, String>.from(v.peyar);
      _mugavari = Map<String, String>.from(v.mugavari);
      _oor = Map<String, String>.from(v.oor);
      _maavattam = Map<String, String>.from(v.maavattam);
      _maanilam = Map<String, String>.from(v.maanilam);
      _naadu = Map<String, String>.from(v.naadu);
      _velinaadMugavari = Map<String, String>.from(v.velinaadMugavari);
      _anjalKuriyeeduController.text = v.anjalKuriyeedu;
      _gstinController.text = v.gstin;
      _minnanjalController.text = v.minnanjal;
      _tholaipaesiController.text = v.tholaipaesi;
    } else {
      final pLang = ref.read(primaryLanguageProvider);
      final sLang = ref.read(secondaryLanguageProvider);
      _naadu = {sLang: 'India', pLang: K.india.tr(context, ref)};
    }
  }

  @override
  void dispose() {
    _anjalKuriyeeduController.dispose();
    _gstinController.dispose();
    _minnanjalController.dispose();
    _tholaipaesiController.dispose();
    super.dispose();
  }

  bool get _isEditing => widget.vaangunar != null;

  // ── GSTIN Validation ──────────────────────────────────────────────────

  bool _validateGstin(String gstin) {
    if (gstin.isEmpty) return true; // Optional field
    // GSTIN format: 2 digits state code + 10 char PAN + 1 entity + 1 'Z' + 1 check
    final regex = RegExp(r'^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$');
    return regex.hasMatch(gstin.toUpperCase());
  }

  // ── Save ───────────────────────────────────────────────────────────────

  void _handleSave() {
    final primaryLang = ref.read(primaryLanguageProvider);
    final primaryName = _peyar[primaryLang]?.trim() ?? '';

    if (primaryName.isEmpty) {
      ElvanSnackbar.show(context, K.vaangunarPeyarThaevai.tr(context, ref));
      return;
    }

    // Validate GSTIN if provided
    final gstin = _gstinController.text.trim();
    if (gstin.isNotEmpty && !_validateGstin(gstin)) {
      setState(() => _gstinError = K.gstinTavaru.tr(context, ref));
      return;
    }

    final kalanjiyam = ref.read(vaangunarKalanjiyamProvider);

    final tharavuruToSave = VaangunarTharavuru(
      id: _isEditing ? widget.vaangunar!.id : -1,
      peyar: _peyar,
      oor: _oor,
      mugavari: _mugavari,
      maavattam: _maavattam,
      maanilam: _maanilam,
      naadu: _naadu,
      velinaadMugavari: _velinaadMugavari,
      anjalKuriyeedu: _anjalKuriyeeduController.text,
      gstin: _gstinController.text,
      minnanjal: _minnanjalController.text,
      tholaipaesi: _tholaipaesiController.text,
      isDeleted: false,
      createdAt: widget.vaangunar?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
    );

    kalanjiyam.saveVaangunar(tharavuruToSave).then((savedId) {
      if (mounted) {
        ref.invalidate(vaangunargalProvider);
        ElvanSnackbar.show(
          context,
          K.vaangunarChaemikkappattadhu.tr(context, ref),
          showAboveNavbar: true,
        );
        
        final finalObject = VaangunarTharavuru(
          id: savedId,
          peyar: tharavuruToSave.peyar,
          oor: tharavuruToSave.oor,
          mugavari: tharavuruToSave.mugavari,
          maavattam: tharavuruToSave.maavattam,
          maanilam: tharavuruToSave.maanilam,
          naadu: tharavuruToSave.naadu,
          velinaadMugavari: tharavuruToSave.velinaadMugavari,
          anjalKuriyeedu: tharavuruToSave.anjalKuriyeedu,
          gstin: tharavuruToSave.gstin,
          minnanjal: tharavuruToSave.minnanjal,
          tholaipaesi: tharavuruToSave.tholaipaesi,
          createdAt: tharavuruToSave.createdAt,
          updatedAt: tharavuruToSave.updatedAt,
          isDeleted: tharavuruToSave.isDeleted,
        );

        if (widget.onSaved != null) {
          widget.onSaved!(context, finalObject);
        } else {
          final primaryLang = ref.read(primaryLanguageProvider);
          Navigator.of(context).pushReplacement(
            MaterialPageRoute(
              builder: (_) => VaangunarPaarvai(
                vaangunar: finalObject,
                achuMozhi: primaryLang,
                onEdit: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => SilkMerchantEditor(
                        vaangunar: finalObject,
                      ),
                    ),
                  );
                },
              ),
            ),
          );
        }
      }
    });
  }

  // ── Build ──────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = ref.watch(primaryLanguageProvider);
    
    final peyarText = _peyar[primaryLang]?.trim().isNotEmpty == true ? _peyar[primaryLang]! : '-';
    
    final mugavariParts = _isIndia ? [
      _maanilam[primaryLang],
      _maavattam[primaryLang],
      _oor[primaryLang],
      _mugavari[primaryLang],
      _anjalKuriyeeduController.text,
    ] : [
      _velinaadMugavari[primaryLang],
    ];
    final mugavariText = mugavariParts.where((e) => e != null && e.toString().trim().isNotEmpty).join(', ');
    
    final thodarpuParts = [
      _gstinController.text.isNotEmpty ? 'GSTIN: ${_gstinController.text}' : null,
      _minnanjalController.text,
      _tholaipaesiController.text,
    ].where((e) => e != null && e.toString().trim().isNotEmpty).join(' | ');

    return ElvanEditorShell(
      title: _isEditing
          ? K.maatriyamai.tr(context, ref)
          : K.pudhiyaAakkam.tr(context, ref),
      onSave: _handleSave,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
                children: [
          ElvanEditorSection(
            index: 0,
            title: K.vaangunarTharavugal.tr(context, ref),
            displayChild: Text(peyarText),
            children: [
              ElvanIrumozhiPulan(
                label: K.vaangunarPeyar.tr(context, ref),
                value: _peyar,
                autofocus: !_isEditing,
                onChanged: (map) => setState(() => _peyar = map),
              ),
            ],
          ),

          ElvanEditorSection(
            index: 1,
            title: K.mugavari.tr(context, ref),
            displayChild: Text(mugavariText.isNotEmpty ? mugavariText : '-'),
            children: [
              ElvanIrumozhiAutocomplete(
                label: K.naadu.tr(context, ref),
                value: _naadu,
                onChanged: (map) => setState(() => _naadu = map),
                options: ulagaNaadugal,
              ),
              if (_isIndia) ...[
                ElvanIrumozhiAutocomplete(
                  label: K.maanilam.tr(context, ref),
                  value: _maanilam,
                  enabled: _naadu.values.any((v) => v.trim().isNotEmpty),
                  onChanged: (map) => setState(() => _maanilam = map),
                  options: indhiyaMaanilangal,
                ),
                if (((_maanilam['en'] ?? _maanilam['en'] ?? '').trim() == 'Tamil Nadu') || ((_maanilam['ta'] ?? _maanilam['ta'] ?? '').trim() == 'தமிழ்நாடு'))
                  ElvanIrumozhiAutocomplete(
                    label: K.maavattam.tr(context, ref),
                    value: _maavattam,
                    enabled: _maanilam.values.any((v) => v.trim().isNotEmpty),
                    onChanged: (map) => setState(() => _maavattam = map),
                    options: tamizhnaattuMaavattangal,
                  )
                else
                  ElvanIrumozhiPulan(
                    label: K.maavattam.tr(context, ref),
                    value: _maavattam,
                    enabled: _maanilam.values.any((v) => v.trim().isNotEmpty),
                    onChanged: (map) => setState(() => _maavattam = map),
                  ),
                ElvanIrumozhiPulan(
                  label: K.oor.tr(context, ref),
                  value: _oor,
                  onChanged: (map) => setState(() => _oor = map),
                ),
                ElvanIrumozhiPulan(
                  label: K.mugavari.tr(context, ref),
                  value: _mugavari,
                  onChanged: (map) => setState(() => _mugavari = map),
                ),
                _buildTextField(
                  context: context,
                  isDark: isDark,
                  label: K.anjalKuriyeedu.tr(context, ref),
                  controller: _anjalKuriyeeduController,
                  keyboardType: TextInputType.number,
                  inputFormatters: ElvanVadivamaippigal.enngalMattum,
                  maxLength: 6,
                ),
              ] else ...[
                ElvanIrumozhiPulan(
                  label: K.velinaadMugavari.tr(context, ref),
                  value: _velinaadMugavari,
                  maxLines: 4,
                  onChanged: (map) => setState(() => _velinaadMugavari = map),
                ),
              ],
            ],
          ),

          ElvanEditorSection(
            index: 2,
            title: K.thodarpuVari.tr(context, ref),
            displayChild: Text(thodarpuParts.isNotEmpty ? thodarpuParts : '-'),
            children: [
              _buildTextField(
                context: context,
                isDark: isDark,
                label: 'GSTIN',
                controller: _gstinController,
                textCapitalization: TextCapitalization.characters,
                errorText: _gstinError,
                onChanged: (_) {
                  if (_gstinError != null) setState(() => _gstinError = null);
                },
                inputFormatters: ElvanVadivamaippigal.periyaEzhuthuEnngal,
                maxLength: 15,
              ),
              _buildTextField(
                context: context,
                isDark: isDark,
                label: K.minnanjal.tr(context, ref),
                controller: _minnanjalController,
                keyboardType: TextInputType.emailAddress,
              ),
              _buildTextField(
                context: context,
                isDark: isDark,
                label: K.tholaipaesi.tr(context, ref),
                controller: _tholaipaesiController,
                keyboardType: TextInputType.phone,
                inputFormatters: ElvanVadivamaippigal.tholaippaesi,
                maxLength: 13,
              ),
            ],
          ),
        ],
      ),
    );
  }

  // ── Text Field Builder (matches SilkItemEditor style) ─────────────────

  Widget _buildTextField({
    required BuildContext context,
    required bool isDark,
    required String label,
    required TextEditingController controller,
    TextInputType? keyboardType,
    String? prefixText,
    String? suffixText,
    String? errorText,
    TextCapitalization textCapitalization = TextCapitalization.none,
    ValueChanged<String>? onChanged,
    List<TextInputFormatter>? inputFormatters,
    int? maxLength,
  }) {
    return ElvanThiruthiUlleedu(
      label: label,
      controller: controller,
      keyboardType: keyboardType,
      prefixText: prefixText,
      suffixText: suffixText,
      errorText: errorText,
      textCapitalization: textCapitalization,
      onChanged: onChanged,
      inputFormatters: inputFormatters,
      maxLength: maxLength,
    );
  }
}
