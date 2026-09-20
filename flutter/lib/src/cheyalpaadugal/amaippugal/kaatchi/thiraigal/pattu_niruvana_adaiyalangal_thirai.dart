import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_oavuru_kaatchi.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../koorugal/maeladukkugal/elvan_kizh_maeladukku/elvan_kizh_maeladukku.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../koorugal/elvan_amaippu_pagudhi.dart';
import '../koorugal/elvan_amaippu_thirutha_attai.dart';
import '../../tharavu/niruvana_tharavugal_provider.dart';
import '../../tharavu/niruvana_tharavugal.dart';

class SilkNiruvanaAdaiyalangalPage extends ConsumerStatefulWidget {
  const SilkNiruvanaAdaiyalangalPage({super.key});

  @override
  ConsumerState<SilkNiruvanaAdaiyalangalPage> createState() =>
      _SilkNiruvanaAdaiyalangalPageState();
}

class _SilkNiruvanaAdaiyalangalPageState
    extends ConsumerState<SilkNiruvanaAdaiyalangalPage> {
  String? _editingSection;
  final ImagePicker _picker = ImagePicker();

  String _tempHeaderStyle = '';
  String _tempSignatoryName = '';
  String? _tempImagePath;

  void _beginEditSingle(String sectionId, String val) {
    setState(() {
      _editingSection = sectionId;
      if (sectionId == 'header_style') {
        _tempHeaderStyle = val;
      } else if (sectionId == 'kaiyoppam') {
        _tempSignatoryName = val;
      }
    });
  }

  void _beginEditImage(String sectionId, String? currentPath) {
    setState(() {
      _editingSection = sectionId;
      _tempImagePath = currentPath;
    });
  }

  void _beginEditSignature(String currentPath, String currentName) {
    setState(() {
      _editingSection = 'kaiyoppam';
      _tempImagePath = currentPath;
      _tempSignatoryName = currentName;
    });
  }

  void _cancelEdit() {
    setState(() {
      _editingSection = null;
    });
  }

  void _showSuccessToast() {
    if (context.mounted) {
      ElvanSnackbar.show(context, K.tharavugalChaemippuVetri.tr(context, ref));
    }
  }

  void _saveSingleField(
      NiruvanaTharavugal profile, String fieldName, String value) {
    final updatedProfile = profile.copyWith();
    switch (fieldName) {
      case 'thalaippuVadivu':
        updatedProfile.thalaippuVadivu = value;
        break;
      case 'oavuru':
        updatedProfile.oavuru = value;
        break;
      case 'agalaOavuru':
        updatedProfile.agalaOavuru = value;
        break;
    }
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(updatedProfile);
    setState(() => _editingSection = null);
    _showSuccessToast();
  }

  void _saveSignatureField(NiruvanaTharavugal profile, String path, String name) {
    final updatedProfile = profile.copyWith();
    updatedProfile.kaiyoppam = path;
    updatedProfile.oppamPeyar = name;
    ref.read(niruvanaTharavugalNotifierProvider).updateProfile(updatedProfile);
    setState(() => _editingSection = null);
    _showSuccessToast();
  }

  void _showHeaderStyleActionSheet() {
    final smallLabel = K.chiriyaOavuruPeyar.tr(context, ref);
    final wideLabel = K.agalamaanaOavuruMattum.tr(context, ref);

    showElvanSelectionBottomSheet<String>(
      context: context,
      title: K.chinnathinVadivam.tr(context, ref),
      items: [smallLabel, wideLabel],
      currentValue: _tempHeaderStyle == 'wide' ? wideLabel : smallLabel,
      itemLabelBuilder: (ctx, ref, item) => item,
      onSelected: (val) {
        setState(() {
          _tempHeaderStyle = val == wideLabel ? 'wide' : 'small';
        });
      },
    );
  }

  Widget _buildEditContainer({
    required String title,
    required Widget customContent,
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
          customContent,
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(
                onPressed: _cancelEdit,
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

  Future<void> _pickImage(Function(String?) onPicked) async {
    final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
    if (image != null) {
      onPicked(image.path);
    }
  }

  Widget _buildImageUploader(
      {required String? imagePath, required Function(String?) onChange}) {
    final bool hasImage = imagePath != null && imagePath.isNotEmpty;

    return Stack(
      children: [
        Material(
          color:
              Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05),
          borderRadius: BorderRadius.circular(16),
          child: InkWell(
            onTap: () {
              if (!hasImage) {
                _pickImage(onChange);
              }
            },
            borderRadius: BorderRadius.circular(16),
            child: Container(
              height: 120,
              width: double.infinity,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
              ),
              child: hasImage
                  ? ClipRRect(
                      borderRadius: BorderRadius.circular(16),
                      child: ElvanOavuruKaatchi(
                        value: imagePath,
                        height: 120,
                        fit: BoxFit.contain,
                        alignment: Alignment.center,
                      ),
                    )
                  : Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            CupertinoIcons.cloud_upload,
                            size: 32,
                            color: Theme.of(context)
                                .colorScheme
                                .onSurface
                                .withValues(alpha: 0.4),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            K.padhivaetru.tr(context, ref),
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w500,
                              color: Theme.of(context)
                                  .colorScheme
                                  .onSurface
                                  .withValues(alpha: 0.5),
                            ),
                          ),
                        ],
                      ),
                    ),
            ),
          ),
        ),
        if (hasImage)
          Positioned(
            top: 8,
            right: 8,
            child: GestureDetector(
              onTap: () => onChange(null),
              child: Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.surface,
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.1),
                      blurRadius: 4,
                      offset: const Offset(0, 2),
                    ),
                  ],
                ),
                child: Icon(
                  CupertinoIcons.delete_solid,
                  size: 16,
                  color: Theme.of(context).colorScheme.error,
                ),
              ),
            ),
          ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final profile = ref.watch(NiruvanaTharavugalProvider);
    final currentProfile = profile ?? NiruvanaTharavugal();

    final headerStyle = currentProfile.thalaippuVadivu.isEmpty
        ? 'small'
        : currentProfile.thalaippuVadivu;
    final logoPath = currentProfile.oavuru.isEmpty ? null : currentProfile.oavuru;
    final wideLogoPath =
        currentProfile.agalaOavuru.isEmpty ? null : currentProfile.agalaOavuru;
    final signaturePath =
        currentProfile.kaiyoppam.isEmpty ? null : currentProfile.kaiyoppam;
    final signatoryName = currentProfile.oppamPeyar;

    return ElvanSubpageShell(
      title: K.adaiyaalam.tr(context, ref),
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding:
                const EdgeInsets.only(left: 16, right: 16, top: 0, bottom: 32),
            child: ElvanSettingsSection(
              children: [
                // 1. Logo
                ElvanSettingsAnimatedExpand(
                  keyPrefix: 'logo',
                  isEditing: _editingSection == 'logo',
                  editChild: _buildEditContainer(
                    title: K.niruvanathinOavuru.tr(context, ref),
                    customContent: _buildImageUploader(
                      imagePath: _tempImagePath,
                      onChange: (path) => setState(() => _tempImagePath = path),
                    ),
                    onSave: () => _saveSingleField(
                        currentProfile, 'oavuru', _tempImagePath ?? ''),
                  ),
                  displayChild: ElvanSettingsDisplayRow(
                    title: K.niruvanathinOavuru.tr(context, ref),
                    primaryValue:
                        logoPath != null ? '' : K.oavuruIllai.tr(context, ref),
                    primaryWidget: logoPath != null
                        ? ElvanOavuruKaatchi(
                            value: logoPath,
                            height: 36,
                          )
                        : null,
                    onEdit: () => _beginEditImage('logo', logoPath),
                  ),
                ),

                // 2. Wide Logo
                ElvanSettingsAnimatedExpand(
                  keyPrefix: 'wide_logo',
                  isEditing: _editingSection == 'wide_logo',
                  editChild: _buildEditContainer(
                    title: K.agalamaanaoavuru.tr(context, ref),
                    customContent: _buildImageUploader(
                      imagePath: _tempImagePath,
                      onChange: (path) => setState(() => _tempImagePath = path),
                    ),
                    onSave: () => _saveSingleField(
                        currentProfile, 'agalaOavuru', _tempImagePath ?? ''),
                  ),
                  displayChild: ElvanSettingsDisplayRow(
                    title: K.agalamaanaoavuru.tr(context, ref),
                    primaryValue: wideLogoPath != null
                        ? ''
                        : K.illai.tr(context, ref),
                    primaryWidget: wideLogoPath != null
                        ? ElvanOavuruKaatchi(
                            value: wideLogoPath,
                            height: 36,
                          )
                        : null,
                    onEdit: () => _beginEditImage('wide_logo', wideLogoPath),
                  ),
                ),

                // 3. Header Style
                ElvanSettingsAnimatedExpand(
                  keyPrefix: 'header_style',
                  isEditing: _editingSection == 'header_style',
                  editChild: _buildEditContainer(
                    title: K.chinnathinVadivam.tr(context, ref),
                    customContent: InkWell(
                      onTap: _showHeaderStyleActionSheet,
                      borderRadius: BorderRadius.circular(100),
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 16, vertical: 14),
                        decoration: BoxDecoration(
                          color: Theme.of(context)
                              .colorScheme
                              .onSurface
                              .withValues(alpha: 0.08),
                          borderRadius: BorderRadius.circular(100),
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              _tempHeaderStyle == 'wide'
                                  ? K.agalamaanaOavuruMattum.tr(context, ref)
                                  : K.chiriyaOavuruPeyar.tr(context, ref),
                              style: TextStyle(
                                fontSize: 14.0,
                                color: Theme.of(context).colorScheme.onSurface,
                              ),
                            ),
                            Icon(CupertinoIcons.chevron_down,
                                size: 16,
                                color: Theme.of(context)
                                    .colorScheme
                                    .onSurface
                                    .withValues(alpha: 0.6)),
                          ],
                        ),
                      ),
                    ),
                    onSave: () => _saveSingleField(
                        currentProfile, 'thalaippuVadivu', _tempHeaderStyle),
                  ),
                  displayChild: ElvanSettingsDisplayRow(
                    title: K.chinnathinVadivam.tr(context, ref),
                    primaryValue: headerStyle == 'wide'
                        ? K.agalamaanaOavuruMattum.tr(context, ref)
                        : K.chiriyaOavuruPeyar.tr(context, ref),
                    onEdit: () => _beginEditSingle('header_style', headerStyle),
                  ),
                ),

                // 4. Signature
                ElvanSettingsAnimatedExpand(
                  keyPrefix: 'kaiyoppam',
                  isEditing: _editingSection == 'kaiyoppam',
                  editChild: _buildEditContainer(
                    title: K.kaiyoppam.tr(context, ref),
                    customContent: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        _buildImageUploader(
                          imagePath: _tempImagePath,
                          onChange: (path) =>
                              setState(() => _tempImagePath = path),
                        ),
                        const SizedBox(height: 16),
                        ElvanTextField(
                          textAlign: TextAlign.center,
                          controller:
                              TextEditingController(text: _tempSignatoryName),
                          decoration: InputDecoration(
                            hintText:
                                K.aluvalkaiyoppam.tr(context, ref),
                            hintStyle: TextStyle(
                              color: Theme.of(context)
                                  .colorScheme
                                  .onSurface
                                  .withValues(alpha: 0.3),
                            ),
                            filled: true,
                            fillColor: WidgetStateColor.resolveWith((states) {
                              if (states.contains(WidgetState.focused)) {
                                return Theme.of(context)
                                    .colorScheme
                                    .onSurface
                                    .withValues(alpha: 0.12);
                              }
                              return Theme.of(context)
                                  .colorScheme
                                  .onSurface
                                  .withValues(alpha: 0.08);
                            }),
                            contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                            constraints: const BoxConstraints(minHeight: 45),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(100),
                              borderSide: BorderSide.none,
                            ),
                          ),
                          style: TextStyle(
                            fontSize: 14,
                            color: Theme.of(context).colorScheme.onSurface,
                          ),
                          onChanged: (val) => _tempSignatoryName = val,
                        ),
                      ],
                    ),
                    onSave: () => _saveSignatureField(currentProfile,
                        _tempImagePath ?? '', _tempSignatoryName),
                  ),
                  displayChild: ElvanSettingsDisplayRow(
                    title: K.kaiyoppam.tr(context, ref),
                    primaryValue: signaturePath != null
                        ? signatoryName
                        : K.kaiyoppamIllai.tr(context, ref),
                    primaryWidget: signaturePath != null
                        ? ElvanOavuruKaatchi(
                            value: signaturePath,
                            height: 48,
                          )
                        : null,
                    onEdit: () =>
                        _beginEditSignature(signaturePath ?? '', signatoryName),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
