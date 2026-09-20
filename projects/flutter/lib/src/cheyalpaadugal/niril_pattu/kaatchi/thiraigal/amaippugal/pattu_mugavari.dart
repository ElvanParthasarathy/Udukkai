import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_pagudhi.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_thirutha_attai.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_kattupadugal.dart';
import '../../../../../adippadai/idangal_kalanjiyam/idangal_kalanjiyam.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../../../koorugal/pulan_koorugal/elvan_irumozhi_autocomplete.dart';
import '../../../../../koorugal/pulan_koorugal/elvan_irumozhi_pulan.dart';

class SilkMugavariPage extends ConsumerStatefulWidget {
  const SilkMugavariPage({super.key});

  @override
  ConsumerState<SilkMugavariPage> createState() => _SilkMugavariPageState();
}

class _SilkMugavariPageState extends ConsumerState<SilkMugavariPage> {
  String? _editingSection;

  // Temp State for editing
  String _tempPrimary = '';
  String _tempSecondary = '';

  final TextEditingController _primaryController = TextEditingController();
  final TextEditingController _secondaryController = TextEditingController();

  @override
  void dispose() {
    _primaryController.dispose();
    _secondaryController.dispose();
    super.dispose();
  }

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
      _primaryController.text = p;
      _secondaryController.text = s;
      _editingSection = section;
    });
  }

  void _beginEditSingle(String section, String val) {
    setState(() {
      _tempPrimary = val;
      _editingSection = section;
    });
  }

  void _handleAutoFill(
      String val,
      bool isPrimary,
      List<Map<String, String>> dataSet,
      String primaryKey,
      String secondaryKey) {
    if (isPrimary) {
      final match = dataSet.where((d) => d[primaryKey] == val).firstOrNull;
      if (match != null) {
        setState(() {
          _tempSecondary = match[secondaryKey]!;
          _secondaryController.text = _tempSecondary;
        });
      }
    } else {
      final match = dataSet.where((d) => d[secondaryKey] == val).firstOrNull;
      if (match != null) {
        setState(() {
          _tempPrimary = match[primaryKey]!;
          _primaryController.text = _tempPrimary;
        });
      }
    }
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
    final title = K.mugavari.tr(context, ref);
    final isBilingual = ref.watch(bilingualProvider);
    final primaryLang = ref.watch(primaryLanguageProvider).toLowerCase();
    final secondaryLang = ref.watch(secondaryLanguageProvider).toLowerCase();

    final primaryKey = primaryLang == 'aangilam' ? 'en' : 'ta';
    final secondaryKey = secondaryLang == 'aangilam' ? 'en' : 'ta';

    final profile = ref.watch(NiruvanaTharavugalProvider);
    final currentProfile = profile ?? NiruvanaTharavugal();

    final mugavariPrimary = currentProfile.getPrimary('mugavari');
    final mugavariSecondary = currentProfile.getSecondary('mugavari');
    final oorPrimary = currentProfile.getPrimary('oor');
    final oorSecondary = currentProfile.getSecondary('oor');
    final maavattamPrimary = currentProfile.getPrimary('maavattam');
    final maavattamSecondary = currentProfile.getSecondary('maavattam');
    final maanilamPrimary = currentProfile.getPrimary('maanilam');
    final maanilamSecondary = currentProfile.getSecondary('maanilam');
    final naaduPrimary = currentProfile.getPrimary('naadu');
    final naaduSecondary = currentProfile.getSecondary('naadu');
    final velinaadMugavariPrimary = currentProfile.getPrimary('velinaadMugavari');
    final velinaadMugavariSecondary = currentProfile.getSecondary('velinaadMugavari');

    final enName = naaduPrimary.trim().toLowerCase();
    final taName = naaduSecondary.trim();
    final isIndia = true; // Country is permanently locked to India

    return ElvanSubpageShell(
      title: title,
      slivers: [
        SliverPadding(
          padding: const EdgeInsets.only(
            left: 16,
            right: 16,
            top: 0,
            bottom: 32,
          ),
          sliver: SliverList.list(
            children: [
              ElvanSettingsSection(
                dividerIndent: 16.0,
                children: [
                  // 5. Country (Locked to India)
                  ElvanSettingsDisplayRow(
                    title: K.naadu.tr(context, ref),
                    primaryValue: naaduPrimary,
                    secondaryValue: isBilingual ? naaduSecondary : null,
                    onEdit: null,
                  ),

                  if (!isIndia)
                    // Foreign Address
                    ElvanSettingsAnimatedExpand(
                      keyPrefix: 'velinaadMugavari',
                      isEditing: _editingSection == 'velinaadMugavari',
                      editChild: _buildEditContainer(
                        title: K.velinaadMugavari.tr(context, ref),
                        inputFields: [
                          ElvanSettingsTextField(
                            label: '${K.velinaadMugavari.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                            initialValue: _tempPrimary,
                            onChanged: (val) => _tempPrimary = val,
                            maxLines: 4,
                          ),
                          if (isBilingual) const SizedBox(height: 16),
                          if (isBilingual)
                            ElvanSettingsTextField(
                              label: '${K.velinaadMugavari.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                              initialValue: _tempSecondary,
                              onChanged: (val) => _tempSecondary = val,
                              maxLines: 4,
                            ),
                        ],
                        onCancel: () => setState(() => _editingSection = null),
                        onSave: () => _saveBilingualField(currentProfile, 'velinaadMugavari'),
                      ),
                      displayChild: ElvanSettingsDisplayRow(
                        title: K.velinaadMugavari.tr(context, ref),
                        primaryValue: velinaadMugavariPrimary,
                        secondaryValue: isBilingual ? velinaadMugavariSecondary : null,
                        onEdit: () => _beginEditPrimarySecondary('velinaadMugavari', velinaadMugavariPrimary, velinaadMugavariSecondary),
                      ),
                    ),

                  if (isIndia) ...[
                                    // 4. State (Maanilam)
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'maanilam',
                    isEditing: _editingSection == 'maanilam',
                    editChild: _buildEditContainer(
                      title: K.maanilam.tr(context, ref),
                      inputFields: [
                        ElvanIrumozhiAutocomplete(
                          label: K.maanilam.tr(context, ref),
                          value: {ref.read(primaryLanguageProvider): _tempPrimary, ref.read(secondaryLanguageProvider): _tempSecondary},
                          onChanged: (map) {
                            setState(() {
                              _tempPrimary = map[ref.read(primaryLanguageProvider)] ?? '';
                              _tempSecondary = map[ref.read(secondaryLanguageProvider)] ?? '';
                            });
                          },
                          options: indhiyaMaanilangal,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveBilingualField(currentProfile, 'maanilam'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.maanilam.tr(context, ref),
                      primaryValue: maanilamPrimary,
                      secondaryValue: isBilingual ? maanilamSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary('maanilam', maanilamPrimary, maanilamSecondary),
                    ),
                  ),

                                    // 3. District (Maavattam)
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'maavattam',
                    isEditing: _editingSection == 'maavattam',
                    editChild: _buildEditContainer(
                      title: K.maavattam.tr(context, ref),
                      inputFields: [
                        if (maanilamPrimary == 'Tamil Nadu' || maanilamPrimary == 'தமிழ்நாடு' || maanilamSecondary == 'Tamil Nadu' || maanilamSecondary == 'தமிழ்நாடு')
                          ElvanIrumozhiAutocomplete(
                            label: K.maavattam.tr(context, ref),
                            value: {
                              ref.read(primaryLanguageProvider): _tempPrimary,
                              ref.read(secondaryLanguageProvider): _tempSecondary
                            },
                            onChanged: (map) {
                              setState(() {
                                _tempPrimary = map[ref.read(primaryLanguageProvider)] ?? '';
                                _tempSecondary = map[ref.read(secondaryLanguageProvider)] ?? '';
                              });
                            },
                            options: tamizhnaattuMaavattangal,
                          )
                        else
                          ElvanIrumozhiPulan(
                            label: K.maavattam.tr(context, ref),
                            value: {ref.read(primaryLanguageProvider): _tempPrimary, ref.read(secondaryLanguageProvider): _tempSecondary},
                            onChanged: (map) {
                              setState(() {
                                _tempPrimary = map[ref.read(primaryLanguageProvider)] ?? '';
                                _tempSecondary = map[ref.read(secondaryLanguageProvider)] ?? '';
                              });
                            },
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveBilingualField(currentProfile, 'maavattam'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.maavattam.tr(context, ref),
                      primaryValue: maavattamPrimary,
                      secondaryValue: isBilingual ? maavattamSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary('maavattam', maavattamPrimary, maavattamSecondary),
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
                        if (isBilingual) const SizedBox(height: 16),
                        if (isBilingual)
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
                      secondaryValue: isBilingual ? oorSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary(
                          'oor', oorPrimary, oorSecondary),
                    ),
                  ),

                    // 1. Address (Mugavari)
                    ElvanSettingsAnimatedExpand(
                      keyPrefix: 'mugavari',
                      isEditing: _editingSection == 'mugavari',
                      editChild: _buildEditContainer(
                        title: K.mugavari.tr(context, ref),
                        inputFields: [
                          ElvanSettingsTextField(
                            label: '${K.mugavari.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                            initialValue: _tempPrimary,
                            onChanged: (val) => _tempPrimary = val,
                            maxLines: 2,
                          ),
                          if (isBilingual) const SizedBox(height: 16),
                          if (isBilingual)
                            ElvanSettingsTextField(
                              label: '${K.mugavari.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                              initialValue: _tempSecondary,
                              onChanged: (val) => _tempSecondary = val,
                              maxLines: 2,
                            ),
                        ],
                        onCancel: () => setState(() => _editingSection = null),
                        onSave: () => _saveBilingualField(currentProfile, 'mugavari'),
                      ),
                      displayChild: ElvanSettingsDisplayRow(
                        title: K.mugavari.tr(context, ref),
                        primaryValue: mugavariPrimary,
                        secondaryValue: isBilingual ? mugavariSecondary : null,
                        onEdit: () => _beginEditPrimarySecondary('mugavari', mugavariPrimary, mugavariSecondary),
                      ),
                    ),

                  // 6. Pincode
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



                  ], // End isIndia block
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}
