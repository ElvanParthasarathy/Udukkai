import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import '../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_niruvanam_udhavi.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_pagudhi.dart';
import '../../../../amaippugal/kaatchi/koorugal/elvan_amaippu_thirutha_attai.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../../niril_podhu/kaatchi/koorugal/maeladukkugal/thannuru_maeladukkugal.dart';
import '../../../../niril_podhu/kaatchi/koorugal/elvan_niruvanam_keezhvirivu_kooru.dart';

class SilkNiruvanaAmaippuPage extends ConsumerStatefulWidget {
  const SilkNiruvanaAmaippuPage({super.key});

  @override
  ConsumerState<SilkNiruvanaAmaippuPage> createState() =>
      _SilkNiruvanaAmaippuPageState();
}

class _SilkNiruvanaAmaippuPageState extends ConsumerState<SilkNiruvanaAmaippuPage> {
  String? _editingSection;

  String _tempPrimary = '';
  String _tempSecondary = '';
  bool _showExtraPhone = false;

  void _savePhoneNumbers(NiruvanaTharavugal currentProfile) {
    currentProfile.tholaipaesi1 = _tempPrimary;
    currentProfile.tholaipaesi2 = _showExtraPhone ? _tempSecondary : '';
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(currentProfile);
    setState(() {
      _editingSection = null;
      _showExtraPhone = false;
    });
    _showSuccessToast();
  }

  void _showSuccessToast() {
    ElvanSnackbar.show(context, K.thannuruChaemikkappattadhu.tr(context, ref));
  }

  void _showManageProfilesModal() {
    showManageProfilesModal(
      context: context,
      ref: ref,
      onNewProfile: () => showNewProfileModal(
        context: context,
        ref: ref,
        onSuccess: _showSuccessToast,
      ),
      primaryNameBuilder: (p, innerRef) {
        final primaryLang = innerRef.watch(primaryLanguageProvider);
        final secondaryLang = innerRef.watch(secondaryLanguageProvider);
        return IruMozhiNiruvanamUdhavi.mudhanmaiPeyar(p, primaryLang, secondaryLang);
      },
      secondaryNameBuilder: (p, innerRef) {
        final isBilingual = innerRef.watch(bilingualProvider);
        final secondaryLang = innerRef.watch(secondaryLanguageProvider);
        return IruMozhiNiruvanamUdhavi.thunaiPeyar(p, isBilingual, true, secondaryLang);
      },
    );
  }

  Widget _buildProfileSwitcher() {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardColor = isDark ? const Color(0xFF111111) : Colors.white;
    final profile = ref.watch(NiruvanaTharavugalProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);
    final primaryName = profile != null ? IruMozhiNiruvanamUdhavi.mudhanmaiPeyar(profile, primaryLang, secondaryLang) : '';
    final displayName =
        primaryName.isEmpty ? K.tharpoadhaiyaNiruvanam.tr(context, ref) : primaryName;

    return Padding(
      padding: const EdgeInsets.only(bottom: 24.0),
      child: IntrinsicHeight(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            AspectRatio(
              aspectRatio: 1.0,
              child: IconButton(
                onPressed: _showManageProfilesModal,
                padding: EdgeInsets.zero,
                icon: Icon(
                  CupertinoIcons.briefcase_fill,
                  size: 24,
                  color: Theme.of(context)
                      .colorScheme
                      .onSurface
                      .withValues(alpha: 0.7),
                ),
                style: IconButton.styleFrom(
                  backgroundColor: cardColor,
                  foregroundColor: Theme.of(context)
                      .colorScheme
                      .onSurface
                      .withValues(alpha: 0.7),
                  shape: const CircleBorder(),
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: ElvanNiruvanamKeezhvirivuKooru(
                selectedNiruvanamId: profile?.id,
                hideLabel: true,
                onChanged: (p) {
                  if (p != null && p.id != null) {
                    ref.read(niruvanaTharavugalNotifierProvider).setActiveProfile(p.id!);
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEditContainer({
    required String title,
    required List<Widget> inputFields,
    required VoidCallback onCancel,
    required VoidCallback onSave,
    Widget? extraAction,
  }) {
    return ElvanSettingsEditContainer(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 16,
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
              if (extraAction != null) ...[
                extraAction,
              ],
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
      case 'kurumPeyar':
        updatedProfile.kurumPeyar = _tempPrimary;
        break;
      case 'tholaipesi_1':
        updatedProfile.tholaipaesi1 = _tempPrimary;
        break;
      case 'tholaipesi_2':
        updatedProfile.tholaipaesi2 = _tempPrimary;
        break;
      case 'minnanjal':
        updatedProfile.minnanjal = _tempPrimary;
        break;
      case 'gstin':
        updatedProfile.gstin = _tempPrimary;
        break;
    }
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(updatedProfile);
    setState(() => _editingSection = null);
    _showSuccessToast();
  }

  @override
  Widget build(BuildContext context) {
    final title = K.niruvanam.tr(context, ref);
    final isBilingual = ref.watch(bilingualProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);

    final profile = ref.watch(NiruvanaTharavugalProvider);
    final currentProfile = profile ?? NiruvanaTharavugal();

    final niruvanathinPeyarPrimary = IruMozhiNiruvanamUdhavi.mudhanmaiPeyar(currentProfile, primaryLang, secondaryLang);
    final niruvanathinPeyarSecondary = IruMozhiNiruvanamUdhavi.thunaiPeyar(currentProfile, isBilingual, true, secondaryLang);

    final adaimozhiPrimary = currentProfile.getPrimary('adaimozhi');
    final adaimozhiSecondary = currentProfile.getSecondary('adaimozhi');

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
              _buildProfileSwitcher(),
              ElvanSettingsSection(
                dividerIndent: 16.0,
                children: [
                  // 1. Business Name (Niruvanathin Peyar)
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'business_name',
                    isEditing: _editingSection == 'niruvanathinPeyar',
                    editChild: _buildEditContainer(
                      title: K.niruvanathinPeyar.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label:
                              '${K.niruvanathinPeyar.tr(context, ref)} (${primaryLang.toLowerCase().tr(context, ref)})',
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                        if (isBilingual) const SizedBox(height: 16),
                        if (isBilingual)
                          ElvanSettingsTextField(
                            label:
                                '${K.niruvanathinPeyar.tr(context, ref)} (${secondaryLang.toLowerCase().tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (val) => _tempSecondary = val,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveBilingualField(
                          currentProfile, 'niruvanathinPeyar'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.niruvanathinPeyar.tr(context, ref),
                      primaryValue: niruvanathinPeyarPrimary,
                      secondaryValue:
                          isBilingual ? niruvanathinPeyarSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary(
                          'niruvanathinPeyar',
                          niruvanathinPeyarPrimary,
                          niruvanathinPeyarSecondary),
                    ),
                  ),

                  // 2. Short Business Name
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'short_business_name',
                    isEditing: _editingSection == 'kurumPeyar',
                    editChild: _buildEditContainer(
                      title: K.kurugiyaNiruvanaPeyar.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.kurugiyaNiruvanaPeyar.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveSingleField(currentProfile, 'kurumPeyar'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.kurugiyaNiruvanaPeyar.tr(context, ref),
                      primaryValue: currentProfile.kurumPeyar,
                      onEdit: () => _beginEditSingle(
                          'kurumPeyar', currentProfile.kurumPeyar),
                    ),
                  ),

                  // Tagline
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'adaimozhi',
                    isEditing: _editingSection == 'adaimozhi',
                    editChild: _buildEditContainer(
                      title: K.adaimozhi.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: isBilingual
                              ? '${K.adaimozhi.tr(context, ref)} (${primaryLang.toLowerCase().tr(context, ref)})'
                              : K.adaimozhi.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                        if (isBilingual) const SizedBox(height: 16),
                        if (isBilingual)
                          ElvanSettingsTextField(
                            label:
                                '${K.adaimozhi.tr(context, ref)} (${secondaryLang.toLowerCase().tr(context, ref)})',
                            initialValue: _tempSecondary,
                            onChanged: (val) => _tempSecondary = val,
                          ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveBilingualField(currentProfile, 'adaimozhi'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.adaimozhi.tr(context, ref),
                      primaryValue: adaimozhiPrimary,
                      secondaryValue: isBilingual ? adaimozhiSecondary : null,
                      onEdit: () => _beginEditPrimarySecondary(
                          'adaimozhi', adaimozhiPrimary, adaimozhiSecondary),
                    ),
                  ),

                  // 3. Phone Numbers
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'tholaipesigal',
                    isEditing: _editingSection == 'tholaipesigal',
                    editChild: _buildEditContainer(
                      title: K.paesiEnkal.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.paesiEn.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                          keyboardType: TextInputType.phone,
                          inputFormatters: ElvanVadivamaippigal.tholaippaesi,
                          maxLength: 13,
                        ),
                        if (_showExtraPhone) const SizedBox(height: 16),
                        if (_showExtraPhone)
                          ElvanSettingsTextField(
                            label: K.maatruPaesiEn.tr(context, ref),
                            initialValue: _tempSecondary,
                            onChanged: (val) => _tempSecondary = val,
                            keyboardType: TextInputType.phone,
                            inputFormatters: ElvanVadivamaippigal.tholaippaesi,
                            maxLength: 13,
                            suffixIcon: IconButton(
                              icon: const Icon(CupertinoIcons.clear, size: 18),
                              onPressed: () {
                                setState(() {
                                  _tempSecondary = '';
                                  _showExtraPhone = false;
                                });
                              },
                            ),
                          ),
                      ],
                      extraAction: !_showExtraPhone
                          ? TextButton(
                              onPressed: () => setState(() => _showExtraPhone = true),
                              style: TextButton.styleFrom(
                                foregroundColor: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.7),
                              ),
                              child: Text(K.chaer.tr(context, ref)),
                            )
                          : null,
                      onCancel: () => setState(() {
                        _editingSection = null;
                        _showExtraPhone = false;
                      }),
                      onSave: () => _savePhoneNumbers(currentProfile),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.paesiEnkal.tr(context, ref),
                      primaryValue: currentProfile.tholaipaesi1,
                      secondaryValue: currentProfile.tholaipaesi2.isNotEmpty ? currentProfile.tholaipaesi2 : null,
                      onEdit: () => setState(() {
                        _editingSection = 'tholaipesigal';
                        _tempPrimary = currentProfile.tholaipaesi1;
                        _tempSecondary = currentProfile.tholaipaesi2;
                        _showExtraPhone = currentProfile.tholaipaesi2.isNotEmpty;
                      }),
                    ),
                  ),

                  // 5. Email
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'minnanjal',
                    isEditing: _editingSection == 'minnanjal',
                    editChild: _buildEditContainer(
                      title: K.minnanjal.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.minnanjal.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                          keyboardType: TextInputType.emailAddress,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () =>
                          _saveSingleField(currentProfile, 'minnanjal'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.minnanjal.tr(context, ref),
                      primaryValue: currentProfile.minnanjal,
                      onEdit: () => _beginEditSingle(
                          'minnanjal', currentProfile.minnanjal),
                    ),
                  ),

                  // 6. GSTIN
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'gstin',
                    isEditing: _editingSection == 'gstin',
                    editChild: _buildEditContainer(
                      title: K.gstinVariAdaiyaalaEn.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.gstinVariAdaiyaalaEn.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                          inputFormatters: ElvanVadivamaippigal.periyaEzhuthuEnngal,
                          maxLength: 15,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveSingleField(currentProfile, 'gstin'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.gstinVariAdaiyaalaEn.tr(context, ref),
                      primaryValue: currentProfile.gstin,
                      onEdit: () =>
                          _beginEditSingle('gstin', currentProfile.gstin),
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
