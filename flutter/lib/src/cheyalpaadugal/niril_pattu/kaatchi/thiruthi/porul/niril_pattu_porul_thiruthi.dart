import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../../koorugal/pulan_koorugal/elvan_irumozhi_pulan.dart';
import '../../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../koorugal/ulleedugal/elvan_parindhurai_ulleedu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import '../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_keezhvirivu.dart';
import '../../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../../../niril_podhu/kaatchi/paarvai/porul_paarvai.dart';


class SilkItemEditor extends ConsumerStatefulWidget {
  final PorulTharavuru? product;
  final void Function(BuildContext context, PorulTharavuru savedPorul)? onSaved;

  const SilkItemEditor({
    super.key, 
    this.product,
    this.onSaved,
  });

  @override
  ConsumerState<SilkItemEditor> createState() => _SilkItemEditorState();
}

class _SilkItemEditorState extends ConsumerState<SilkItemEditor> {
  Map<String, String> _porulPeyar = {};
  String _alavuVagai = 'quantity';
  final _hsnController = TextEditingController();
  final _vilaiController = TextEditingController();
  final _variController = TextEditingController();

  @override
  void initState() {
    super.initState();
    // Pre-fill if editing
    if (widget.product != null) {
      final p = widget.product!;
      _porulPeyar = Map<String, String>.from(p.porulPeyar);
      _alavuVagai = p.alavuVagai;
      _hsnController.text = p.hsnCode;
      _vilaiController.text = p.vilai > 0 ? p.vilai.toStringAsFixed(0) : '';
      _variController.text = p.variVeetham.toStringAsFixed(0);
    }
  }

  @override
  void dispose() {
    _hsnController.dispose();
    _vilaiController.dispose();
    _variController.dispose();
    super.dispose();
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
    final alagu = _alavuVagai == 'weight' ? 'kg' : 'Nos';

    final tharavuruToSave = PorulTharavuru(
      id: _isEditing ? widget.product!.id : -1,
      porulPeyar: _porulPeyar,
      hsnCode: _hsnController.text.trim(),
      vilai: double.tryParse(_vilaiController.text) ?? 0.0,
      variVeetham: double.tryParse(_variController.text) ?? 0.0,
      alavuVagai: _alavuVagai,
      alagu: alagu,
      createdAt: widget.product?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
      isDeleted: widget.product?.isDeleted ?? false,
    );

    kalanjiyam.savePorul(tharavuruToSave).then((savedId) {
      if (mounted) {
        ref.invalidate(porulgalProvider);
        ElvanSnackbar.show(
          context,
          K.porulChaemikkappattadhu.tr(context, ref),
          showAboveNavbar: true,
        );
        
        final finalObject = PorulTharavuru(
          id: savedId,
          porulPeyar: tharavuruToSave.porulPeyar,
          hsnCode: tharavuruToSave.hsnCode,
          vilai: tharavuruToSave.vilai,
          variVeetham: tharavuruToSave.variVeetham,
          alavuVagai: tharavuruToSave.alavuVagai,
          alagu: tharavuruToSave.alagu,
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
              builder: (_) => PorulPaarvai(
                porul: finalObject,
                achuMozhi: primaryLang,
                onEdit: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => SilkItemEditor(
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
    final isDark = Theme.of(context).brightness == Brightness.dark;
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

              // Bilingual product name
              ElvanIrumozhiPulan(
                label: K.porul.tr(context, ref),
                value: _porulPeyar,
                autofocus: !_isEditing,
                onChanged: (map) => setState(() => _porulPeyar = map),
              ),

              // Measurement Method Dropdown
              ElvanThiruthiKeezhvirivu<String>(
                label: K.alaveeduMurai.tr(context, ref),
                value: _alavuVagai == 'weight' ? K.edai : K.alavu,
                items: const [K.alavu, K.edai],
                itemLabelBuilder: (ctx, ref, item) => item.tr(ctx, ref),
                onChanged: (String newValue) {
                  setState(() {
                    _alavuVagai = newValue == K.edai ? 'weight' : 'quantity';
                  });
                },
              ),
            ],
          ),
          ElvanEditorSection(
            index: 1,
            title: K.vilaiMatrumVari.tr(context, ref),
            displayChild: Text(
              '₹ ${_vilaiController.text.isNotEmpty ? _vilaiController.text : '0'} / ${_variController.text}%',
            ),
            children: [
              // HSN Code
              ElvanParindhuraiUlleedu(
                label: K.hsnSacKuriyeedu.tr(context, ref),
                controller: _hsnController,
                suggestions: const ['50020010', '50040010', '50072010'],
                keyboardType: TextInputType.number,
                inputFormatters: ElvanVadivamaippigal.enngalMattum,
                maxLength: 8,
              ),

              // Selling Rate
              ElvanThiruthiUlleedu(
                label: K.vilai.tr(context, ref),
                controller: _vilaiController,
                keyboardType:
                    const TextInputType.numberWithOptions(decimal: true),
                prefixText: '₹ ',
                inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
              ),

              // GST %
              ElvanParindhuraiUlleedu(
                label: K.gstVeedham.tr(context, ref),
                controller: _variController,
                suggestions: const ['0', '5', '12', '18'],
                keyboardType: TextInputType.number,
                suffixText: '%',
                inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
                maxLength: 5,
              ),
            ],
          ),
        ],
      ),
    );
  }

}

