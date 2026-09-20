import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../../adippadai/tharavuru/uruvugal.dart';
import 'package:elvan_niril/src/koorugal/pulan_koorugal/elvan_kooli_irumozhi_pulan.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import '../../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../../../niril_podhu/kaatchi/paarvai/porul_paarvai.dart';

class CoolieItemEditor extends ConsumerStatefulWidget {
  final PorulTharavuru? product;
  final void Function(BuildContext context, PorulTharavuru savedPorul)? onSaved;

  const CoolieItemEditor({
    super.key, 
    this.product,
    this.onSaved,
  });

  @override
  ConsumerState<CoolieItemEditor> createState() => _CoolieItemEditorState();
}

class _CoolieItemEditorState extends ConsumerState<CoolieItemEditor> {
  Map<String, String> _porulPeyar = {};

  @override
  void initState() {
    super.initState();
    // Pre-fill if editing
    if (widget.product != null) {
      _porulPeyar = Map<String, String>.from(widget.product!.porulPeyar);
    }
  }

  bool get _isEditing => widget.product != null;

  void _handleSave() {
    final primaryLang = ref.read(primaryLanguageProvider);
    final primaryName = _porulPeyar[primaryLang]?.trim() ?? '';

    // Validation: primary name required
    if (primaryName.isEmpty) {
      ElvanSnackbar.show(context, K.porulPeyarThaevai.tr(context, ref));
      return;
    }

    final kalanjiyam = ref.read(porulKalanjiyamProvider);

    final tharavuru = PorulTharavuru(
      id: widget.product?.id ?? 0,
      porulPeyar: _porulPeyar,
      hsnCode: '',
      vilai: 0.0,
      variVeetham: 0.0,
      alavuVagai: '',
      alagu: '',
      createdAt: widget.product?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
      isDeleted: widget.product?.isDeleted ?? false,
    );

    kalanjiyam.savePorul(tharavuru).then((savedId) {
      if (mounted) {
        ref.invalidate(porulgalProvider);
        ElvanSnackbar.show(
          context,
          K.porulChaemikkappattadhu.tr(context, ref),
          showAboveNavbar: true,
        );
        
        final finalObject = PorulTharavuru(
          id: savedId,
          porulPeyar: tharavuru.porulPeyar,
          hsnCode: tharavuru.hsnCode,
          vilai: tharavuru.vilai,
          variVeetham: tharavuru.variVeetham,
          alavuVagai: tharavuru.alavuVagai,
          alagu: tharavuru.alagu,
          createdAt: tharavuru.createdAt,
          updatedAt: tharavuru.updatedAt,
          isDeleted: tharavuru.isDeleted,
        );

        if (widget.onSaved != null) {
          widget.onSaved!(context, finalObject);
        } else {
          final primaryLang = ref.read(primaryLanguageProvider);
          Navigator.of(context).pushReplacement(
            MaterialPageRoute(
              builder: (_) => PorulPaarvai(
                porul: finalObject,
                achuMozhi: primaryLang,
                onEdit: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => CoolieItemEditor(
                        product: finalObject,
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
    final peyarText = _porulPeyar[primaryLang]?.isNotEmpty == true
        ? _porulPeyar[primaryLang]!
        : '';

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
            title: K.porulTharavugal.tr(context, ref),
            displayChild: Text(peyarText),
            children: [
              // Bilingual data entry for Kooli Product
              ElvanKooliIrumozhiPulan(
                label: K.porul.tr(context, ref),
                value: _porulPeyar,
                autofocus: !_isEditing,
                onChanged: (map) => setState(() => _porulPeyar = map),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
