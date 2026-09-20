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
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';

class SilkVangiPage extends ConsumerStatefulWidget {
  const SilkVangiPage({super.key});

  @override
  ConsumerState<SilkVangiPage> createState() => _SilkVangiPageState();
}

class _SilkVangiPageState extends ConsumerState<SilkVangiPage> {
  String? _editingSection;

  // Temp State for editing
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
      case 'vangiKanakku':
        updatedProfile.vangiKanakku = _tempPrimary;
        break;
      case 'ifsc':
        updatedProfile.ifsc = _tempPrimary;
        break;
    }
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(updatedProfile);
    setState(() => _editingSection = null);
    _showSuccessToast();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final isBilingual = ref.watch(bilingualProvider);
    final primaryLang = ref.watch(primaryLanguageProvider).toLowerCase();
    final secondaryLang = ref.watch(secondaryLanguageProvider).toLowerCase();

    final profile = ref.watch(NiruvanaTharavugalProvider);
    final currentProfile = profile ?? NiruvanaTharavugal();

    final vangiPeyarPrimary = currentProfile.getPrimary('vangiPeyar');
    final vangiPeyarSecondary = currentProfile.getSecondary('vangiPeyar');
    final vangiKilaiPrimary = currentProfile.getPrimary('kilai');
    final vangiKilaiSecondary = currentProfile.getSecondary('kilai');

    return ElvanSubpageShell(
      title: K.vangi.tr(context, ref),
      backgroundColor:
          isDark ? const Color(0xFF000000) : const Color(0xFFF3F4F6),
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
                  // --- BANK NAME ---
                  ElvanSettingsAnimatedExpand(
                    isEditing: _editingSection == 'vangiPeyar',
                    keyPrefix: 'vangiPeyar',
                    editChild: _buildEditContainer(
                      title: K.vangiyinPeyar.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label:
                              '${K.vangiyinPeyar.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                          initialValue: _tempPrimary,
                          onChanged: (v) => _tempPrimary = v,
                        ),
                        if (isBilingual) const SizedBox(height: 16),
                        if (isBilingual)
                          ElvanSettingsTextField(
                            label:
                                '${K.vangiyinPeyar.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (v) => _tempSecondary = v,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveBilingualField(currentProfile, 'vangiPeyar'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.vangiyinPeyar.tr(context, ref),
                      primaryValue: vangiPeyarPrimary,
                      secondaryValue: isBilingual ? vangiPeyarSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary(
                          'vangiPeyar', vangiPeyarPrimary, vangiPeyarSecondary),
                    ),
                  ),
                  // --- BRANCH NAME ---
                  ElvanSettingsAnimatedExpand(
                    isEditing: _editingSection == 'vangiKilai',
                    keyPrefix: 'vangiKilai',
                    editChild: _buildEditContainer(
                      title: K.kilaipPeyar.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label:
                              '${K.kilaipPeyar.tr(context, ref)} (${primaryLang.tr(context, ref)})',
                          initialValue: _tempPrimary,
                          onChanged: (v) => _tempPrimary = v,
                        ),
                        if (isBilingual) const SizedBox(height: 16),
                        if (isBilingual)
                          ElvanSettingsTextField(
                            label:
                                '${K.kilaipPeyar.tr(context, ref)} (${secondaryLang.tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (v) => _tempSecondary = v,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveBilingualField(currentProfile, 'kilai'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.kilaipPeyar.tr(context, ref),
                      primaryValue: vangiKilaiPrimary,
                      secondaryValue: isBilingual ? vangiKilaiSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary(
                          'vangiKilai', vangiKilaiPrimary, vangiKilaiSecondary),
                    ),
                  ),
                  // --- ACCOUNT NUMBER ---
                  ElvanSettingsAnimatedExpand(
                    isEditing: _editingSection == 'vangiKanakku',
                    keyPrefix: 'vangiKanakku',
                    editChild: _buildEditContainer(
                      title: K.kanakkuEn.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.kanakkuEn.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (v) => _tempPrimary = v,
                          keyboardType: TextInputType.number,
                          inputFormatters: ElvanVadivamaippigal.enngalMattum,
                          maxLength: 18,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveSingleField(currentProfile, 'vangiKanakku'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.kanakkuEn.tr(context, ref),
                      primaryValue: currentProfile.vangiKanakku,
                      onEdit: () => _beginEditSingle(
                          'vangiKanakku', currentProfile.vangiKanakku),
                    ),
                  ),
                  // --- IFSC CODE ---
                  ElvanSettingsAnimatedExpand(
                    isEditing: _editingSection == 'ifsc',
                    keyPrefix: 'ifsc',
                    editChild: _buildEditContainer(
                      title: K.ifscKuriyeedu.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.ifscKuriyeedu.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (v) => _tempPrimary = v,
                          inputFormatters: ElvanVadivamaippigal.periyaEzhuthuEnngal,
                          maxLength: 11,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveSingleField(currentProfile, 'ifsc'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.ifscKuriyeedu.tr(context, ref),
                      primaryValue: currentProfile.ifsc,
                      onEdit: () =>
                          _beginEditSingle('ifsc', currentProfile.ifsc),
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
