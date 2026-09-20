import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_pagudhi.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_thirutha_attai.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';

class CoolieMugavariPage extends ConsumerStatefulWidget {
  const CoolieMugavariPage({super.key});

  @override
  ConsumerState<CoolieMugavariPage> createState() => _CoolieMugavariPageState();
}

class _CoolieMugavariPageState extends ConsumerState<CoolieMugavariPage> {
  String? _editingSection;

  String _tempPrimary = '';
  String _tempSecondary = '';

  void _showSuccessToast() {
    ElvanSnackbar.show(context, K.tharavugalChaemippuVetri.tr(context, ref));
  }

  Widget _buildEditContainer({
    required String title,
    required List<Widget> inputFields,
    required VoidCallback onCancel,
    required VoidCallback onSave,
  }) {
    return ElvanSettingsEditContainer(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Theme.of(context).colorScheme.onSurface,
            ),
          ),
          const SizedBox(height: 16),
          ...inputFields,
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(
                onPressed: onCancel,
                style: TextButton.styleFrom(
                  foregroundColor: Theme.of(context)
                      .colorScheme
                      .onSurface
                      .withValues(alpha: 0.7),
                ),
                child: Text(K.kaividuPtn.tr(context, ref)),
              ),
              const SizedBox(width: 8),
              FilledButton(
                onPressed: onSave,
                style: FilledButton.styleFrom(
                  backgroundColor: Theme.of(context).colorScheme.onSurface,
                  foregroundColor: Theme.of(context).colorScheme.surface,
                ),
                child: Text(K.chaemiPtn.tr(context, ref)),
              ),
            ],
          ),
        ],
      ),
    );
  }

  void _beginEditPrimarySecondary(String section, String p, String s) {
    setState(() {
      _tempPrimary = p;
      _tempSecondary = s;
      _editingSection = section;
    });
  }

  void _beginEditSingle(String section, String val) {
    setState(() {
      _tempPrimary = val;
      _editingSection = section;
    });
  }

  void _saveBilingualField(NiruvanaTharavugal profile, String fieldName) {
    final updatedProfile = profile.copyWith();
    updatedProfile.setBilingual(fieldName, profile.mudhanMozhi, _tempPrimary);
    updatedProfile.setBilingual(fieldName, profile.thunaiMozhi, _tempSecondary);
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(updatedProfile);
    setState(() => _editingSection = null);
    _showSuccessToast();
  }

  void _saveSingleField(NiruvanaTharavugal profile, String fieldName) {
    final updatedProfile = profile.copyWith();
    switch (fieldName) {
      case 'anjalKuriyeedu':
        updatedProfile.anjalKuriyeedu = _tempPrimary;
        break;
    }
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(updatedProfile);
    setState(() => _editingSection = null);
    _showSuccessToast();
  }

  @override
  Widget build(BuildContext context) {
    final primaryLang = ref.watch(primaryLanguageProvider).toLowerCase();
    final secondaryLang = ref.watch(secondaryLanguageProvider).toLowerCase();

    final profile = ref.watch(NiruvanaTharavugalProvider);
    final currentProfile = profile ?? NiruvanaTharavugal();

    final mugavariPrimary = currentProfile.getPrimary('mugavari');
    final mugavariSecondary = currentProfile.getSecondary('mugavari');
    final oorPrimary = currentProfile.getPrimary('oor');
    final oorSecondary = currentProfile.getSecondary('oor');
    final maavattamPrimary = currentProfile.getPrimary('maavattam');
    final maavattamSecondary = currentProfile.getSecondary('maavattam');

    return ElvanSubpageShell(
      title: K.mugavari.tr(context, ref),
      slivers: [
        SliverPadding(
          padding:
              const EdgeInsets.only(left: 16, right: 16, top: 0, bottom: 32),
          sliver: SliverList.list(
            children: [
              ElvanSettingsSection(
                dividerIndent: 16.0,
                children: [
                  // 1. Address (Mugavari)
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'mugavari',
                    isEditing: _editingSection == 'mugavari',
                    editChild: _buildEditContainer(
                      title: K.mugavari.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label:
                              '${K.mugavari.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                          maxLines: 2,
                        ),
                        const SizedBox(height: 16),
                        ElvanSettingsTextField(
                            label:
                                '${K.mugavari.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (val) => _tempSecondary = val,
                            maxLines: 2,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveBilingualField(currentProfile, 'mugavari'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.mugavari.tr(context, ref),
                      primaryValue: mugavariPrimary,
                      secondaryValue: mugavariSecondary ,
                      onEdit: () => _beginEditPrimarySecondary(
                          'mugavari', mugavariPrimary, mugavariSecondary),
                    ),
                  ),

                  // 2. City (Oor)
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'oor',
                    isEditing: _editingSection == 'oor',
                    editChild: _buildEditContainer(
                      title: K.oor.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label:
                              '${K.oor.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                        const SizedBox(height: 16),
                        ElvanSettingsTextField(
                            label:
                                '${K.oor.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (val) => _tempSecondary = val,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveBilingualField(currentProfile, 'oor'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.oor.tr(context, ref),
                      primaryValue: oorPrimary,
                      secondaryValue: oorSecondary ,
                      onEdit: () => _beginEditPrimarySecondary(
                          'oor', oorPrimary, oorSecondary),
                    ),
                  ),

                  // 3. District (Maavattam)
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'maavattam',
                    isEditing: _editingSection == 'maavattam',
                    editChild: _buildEditContainer(
                      title: K.maavattam.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label:
                              '${K.maavattam.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                        const SizedBox(height: 16),
                        ElvanSettingsTextField(
                            label:
                                '${K.maavattam.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (val) => _tempSecondary = val,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveBilingualField(currentProfile, 'maavattam'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.maavattam.tr(context, ref),
                      primaryValue: maavattamPrimary,
                      secondaryValue: maavattamSecondary ,
                      onEdit: () => _beginEditPrimarySecondary(
                          'maavattam', maavattamPrimary, maavattamSecondary),
                    ),
                  ),

                  // 4. Pincode
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'anjalKuriyeedu',
                    isEditing: _editingSection == 'anjalKuriyeedu',
                    editChild: _buildEditContainer(
                      title: K.anjalKuriyeedu.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.anjalKuriyeedu.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                          keyboardType: TextInputType.number,
                          inputFormatters: ElvanVadivamaippigal.enngalMattum,
                          maxLength: 6,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveSingleField(currentProfile, 'anjalKuriyeedu'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.anjalKuriyeedu.tr(context, ref),
                      primaryValue: currentProfile.anjalKuriyeedu,
                      onEdit: () => _beginEditSingle(
                          'anjalKuriyeedu', currentProfile.anjalKuriyeedu),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}
