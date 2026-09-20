import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/viruppangal_paniyagam.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../koorugal/elvan_amaippu_pagudhi.dart';
import '../koorugal/elvan_amaippu_thirutha_attai.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

class PayanarAmaippugalPage extends ConsumerStatefulWidget {
  const PayanarAmaippugalPage({super.key});

  @override
  ConsumerState<PayanarAmaippugalPage> createState() =>
      _PayanarAmaippugalPageState();
}

class _PayanarAmaippugalPageState extends ConsumerState<PayanarAmaippugalPage> {
  String? _editingSection;
  String _tempPrimary = '';

  String _mudhalPeyar = '';
  String _irudhiPeyar = '';
  String _pirandhaThaedhi = '';

  static final _dateFormat = DateFormat('dd/MM/yyyy');

  @override
  void initState() {
    super.initState();
    final prefs = ref.read(preferencesServiceProvider);
    _mudhalPeyar = prefs.getPayanarMudhalPeyar();
    _irudhiPeyar = prefs.getPayanarIrudhiPeyar();
    _pirandhaThaedhi = prefs.getPayanarPirandhaThaedhi();
  }

  void _beginEditSingle(String section, String val) {
    setState(() {
      _tempPrimary = val;
      _editingSection = section;
    });
  }

  Future<void> _saveField(String fieldName) async {
    final prefs = ref.read(preferencesServiceProvider);
    if (fieldName == 'mudhalPeyar') {
      _mudhalPeyar = _tempPrimary;
      await prefs.setPayanarMudhalPeyar(_mudhalPeyar);
    } else if (fieldName == 'irudhiPeyar') {
      _irudhiPeyar = _tempPrimary;
      await prefs.setPayanarIrudhiPeyar(_irudhiPeyar);
    } else if (fieldName == 'pirandhaThaedhi') {
      _pirandhaThaedhi = _tempPrimary;
      await prefs.setPayanarPirandhaThaedhi(_pirandhaThaedhi);
    }
    
    // Update the reactive provider so VanakkamPill rebuilds
    ref.read(payanarKaatchiPeyarProvider.notifier).state =
        prefs.getPayanarKaatchiPeyar();
        
    setState(() => _editingSection = null);
    if (mounted) {
      ElvanSnackbar.show(context, K.tharavugalChaemippuVetri.tr(context, ref));
    }
  }

  Future<void> _pickDate() async {
    DateTime initialDate = DateTime.now();
    if (_tempPrimary.isNotEmpty) {
      try {
        initialDate = _dateFormat.parse(_tempPrimary);
      } catch (_) {}
    } else if (_pirandhaThaedhi.isNotEmpty) {
      try {
        initialDate = _dateFormat.parse(_pirandhaThaedhi);
      } catch (_) {}
    }

    final picked = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: DateTime(1920),
      lastDate: DateTime.now(),
      helpText: '', // Removes the unnecessary 'Select date' text
      fieldLabelText: '', // Removes the 'Enter Date' label in input mode
      builder: (context, child) {
        final isDark = Theme.of(context).brightness == Brightness.dark;
        final bgColor = isDark ? const Color(0xFF1E1E1E) : Colors.white; // Pure dark grey or white
        return Theme(
          data: Theme.of(context).copyWith(
            dialogBackgroundColor: bgColor,
            dialogTheme: DialogThemeData(
              backgroundColor: bgColor,
              surfaceTintColor: Colors.transparent,
            ),
            datePickerTheme: Theme.of(context).datePickerTheme.copyWith(
              backgroundColor: bgColor,
              surfaceTintColor: Colors.transparent,
              headerBackgroundColor: bgColor,
            ),
            colorScheme: Theme.of(context).colorScheme.copyWith(
              surfaceTint: Colors.transparent,
              surfaceContainerHigh: bgColor,
              surface: bgColor,
            ),
          ),
          child: child!,
        );
      },
    );

    if (picked != null) {
      final newDate = _dateFormat.format(picked);
      setState(() {
        _tempPrimary = newDate;
      });
    }
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

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final theme = Theme.of(context);

    return ElvanSubpageShell(
      title: K.payanar.tr(context, ref),
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
              // Form section
              ElvanSettingsSection(
                dividerIndent: 16.0,
                children: [
                  // First Name
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'mudhalPeyar',
                    isEditing: _editingSection == 'mudhalPeyar',
                    editChild: _buildEditContainer(
                      title: K.mudhalPeyar.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.mudhalPeyar.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveField('mudhalPeyar'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.mudhalPeyar.tr(context, ref),
                      primaryValue: _mudhalPeyar,
                      onEdit: () => _beginEditSingle('mudhalPeyar', _mudhalPeyar),
                    ),
                  ),

                  // Last Name
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'irudhiPeyar',
                    isEditing: _editingSection == 'irudhiPeyar',
                    editChild: _buildEditContainer(
                      title: K.irudhiPeyar.tr(context, ref),
                      inputFields: [
                        ElvanSettingsTextField(
                          label: K.irudhiPeyar.tr(context, ref),
                          initialValue: _tempPrimary,
                          onChanged: (val) => _tempPrimary = val,
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveField('irudhiPeyar'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.irudhiPeyar.tr(context, ref),
                      primaryValue: _irudhiPeyar,
                      onEdit: () => _beginEditSingle('irudhiPeyar', _irudhiPeyar),
                    ),
                  ),

                  // Date of Birth
                  ElvanSettingsAnimatedExpand(
                    keyPrefix: 'pirandhaThaedhi',
                    isEditing: _editingSection == 'pirandhaThaedhi',
                    editChild: _buildEditContainer(
                      title: K.pirandhaThaedhi.tr(context, ref),
                      inputFields: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Padding(
                              padding: const EdgeInsets.only(left: 16, bottom: 8),
                              child: Text(
                                K.pirandhaThaedhi.tr(context, ref),
                                style: TextStyle(
                                  fontSize: 12,
                                  fontWeight: FontWeight.w400,
                                  letterSpacing: 0.3,
                                  color: theme.colorScheme.onSurface.withValues(alpha: 0.5),
                                ),
                              ),
                            ),
                            InkWell(
                              onTap: _pickDate,
                              borderRadius: BorderRadius.circular(100),
                              child: Container(
                                decoration: BoxDecoration(
                                  color: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                                  borderRadius: BorderRadius.circular(100),
                                ),
                                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                                child: Row(
                                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                  children: [
                                    Text(
                                      _tempPrimary.isNotEmpty ? _tempPrimary : '...',
                                      style: TextStyle(
                                        fontSize: 15,
                                        fontWeight: FontWeight.w400,
                                        color: theme.colorScheme.onSurface,
                                      ),
                                    ),
                                    Icon(
                                      CupertinoIcons.calendar,
                                      color: theme.colorScheme.onSurface.withValues(alpha: 0.5),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                      onCancel: () => setState(() => _editingSection = null),
                      onSave: () => _saveField('pirandhaThaedhi'),
                    ),
                    displayChild: ElvanSettingsDisplayRow(
                      title: K.pirandhaThaedhi.tr(context, ref),
                      primaryValue: _pirandhaThaedhi,
                      onEdit: () => _beginEditSingle('pirandhaThaedhi', _pirandhaThaedhi),
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
