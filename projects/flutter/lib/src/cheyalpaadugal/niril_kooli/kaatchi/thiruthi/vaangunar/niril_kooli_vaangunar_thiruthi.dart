import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import 'package:elvan_niril/src/koorugal/pulan_koorugal/elvan_kooli_irumozhi_pulan.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import '../../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../../../niril_podhu/kaatchi/paarvai/vaangunar_paarvai.dart';

class CoolieMerchantEditor extends ConsumerStatefulWidget {
  final VaangunarTharavuru? vaangunar;
  final void Function(BuildContext context, VaangunarTharavuru savedVaangunar)? onSaved;

  const CoolieMerchantEditor({
    super.key, 
    this.vaangunar,
    this.onSaved,
  });

  @override
  ConsumerState<CoolieMerchantEditor> createState() =>
      _CoolieMerchantEditorState();
}

class _CoolieMerchantEditorState extends ConsumerState<CoolieMerchantEditor> {
  Map<String, String> _peyar = {};
  Map<String, String> _oor = {};
  Map<String, String> _mugavari = {};

  @override
  void initState() {
    super.initState();
    // Pre-fill if editing
    if (widget.vaangunar != null) {
      _peyar = Map<String, String>.from(widget.vaangunar!.peyar);
      _oor = Map<String, String>.from(widget.vaangunar!.oor);
      _mugavari = Map<String, String>.from(widget.vaangunar!.mugavari);
    }
  }

  bool get _isEditing => widget.vaangunar != null;

  void _handleSave() {
    final primaryLang = ref.read(primaryLanguageProvider);
    final primaryName = _peyar[primaryLang]?.trim() ?? '';

    // Validation: primary name required
    if (primaryName.isEmpty) {
      ElvanSnackbar.show(context, K.vaangunarPeyarThaevai.tr(context, ref));
      return;
    }

    final kalanjiyam = ref.read(vaangunarKalanjiyamProvider);
    final tharavuruToSave = VaangunarTharavuru(
      id: _isEditing ? widget.vaangunar!.id : -1,
      peyar: _peyar,
      oor: _oor,
      mugavari: _mugavari,
      maavattam: {},
      maanilam: {},
      naadu: {},
      velinaadMugavari: {},
      anjalKuriyeedu: '',
      gstin: '',
      minnanjal: '',
      tholaipaesi: '',
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
                      builder: (_) => CoolieMerchantEditor(
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

  @override
  Widget build(BuildContext context) {
    final primaryLang = ref.watch(primaryLanguageProvider);
    final peyarText = _peyar[primaryLang]?.trim().isNotEmpty == true ? _peyar[primaryLang]! : '-';

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
              // 1. Bilingual data entry for Kooli Merchant
              ElvanKooliIrumozhiPulan(
                label: K.vaangunarPeyar.tr(context, ref),
                value: _peyar,
                autofocus: !_isEditing,
                onChanged: (map) => setState(() => _peyar = map),
              ),
            ],
          ),

          ElvanEditorSection(
            index: 1,
            title: K.mugavaritharavugal.tr(context, ref),
            displayChild: Text(_oor[primaryLang]?.trim().isNotEmpty == true ? _oor[primaryLang]! : '-'),
            children: [
              // 2. Bilingual data entry for Kooli City
              ElvanKooliIrumozhiPulan(
                label: K.oor.tr(context, ref),
                value: _oor,
                onChanged: (map) => setState(() => _oor = map),
              ),

              // 3. Bilingual data entry for Kooli Address
              ElvanFullWidth(
                child: ElvanKooliIrumozhiPulan(
                  label: K.mugavari.tr(context, ref),
                  value: _mugavari,
                  maxLines: 4,
                  onChanged: (map) => setState(() => _mugavari = map),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
