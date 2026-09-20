import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../cheyalpaadugal/amaippugal/kaatchi/koorugal/elvan_amaippu_kattupadugal.dart';

class ElvanIrumozhiAutocomplete extends ConsumerStatefulWidget {
  const ElvanIrumozhiAutocomplete({
    super.key,
    required this.label,
    required this.value,
    required this.onChanged,
    required this.options,
    this.enabled = true,
  });

  final String label;
  final Map<String, String> value;
  final ValueChanged<Map<String, String>> onChanged;
  final List<Map<String, String>> options;
  final bool enabled;

  @override
  ConsumerState<ElvanIrumozhiAutocomplete> createState() => _ElvanIrumozhiAutocompleteState();
}

class _ElvanIrumozhiAutocompleteState extends ConsumerState<ElvanIrumozhiAutocomplete> {
  late TextEditingController _primaryController;
  late TextEditingController _secondaryController;

  bool _initialized = false;
  bool _isCustomMode = false;

  @override
  void initState() {
    super.initState();
    _primaryController = TextEditingController();
    _secondaryController = TextEditingController();
  }

  @override
  void dispose() {
    _primaryController.dispose();
    _secondaryController.dispose();
    super.dispose();
  }

  @override
  void didUpdateWidget(ElvanIrumozhiAutocomplete oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (_initialized) {
      final primaryLang = ref.read(primaryLanguageProvider);
      final secondaryLang = ref.read(secondaryLanguageProvider);
      
      final pText = widget.value[primaryLang] ?? '';
      final sText = widget.value[secondaryLang] ?? '';
      
      if (pText != _primaryController.text) {
        _primaryController.text = pText;
      }
      if (sText != _secondaryController.text) {
        _secondaryController.text = sText;
      }
      
      // Update custom mode if it's no longer custom
      if (_isCustomMode && pText.isNotEmpty) {
         final pKey = _mapLangToKey(primaryLang);
         final match = widget.options.any((d) => d[pKey] == pText);
         if (match) _isCustomMode = false;
      }
    }
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_initialized) {
      final primaryLang = ref.watch(primaryLanguageProvider);
      final secondaryLang = ref.watch(secondaryLanguageProvider);
      
      final pText = widget.value[primaryLang] ?? '';
      final sText = widget.value[secondaryLang] ?? '';
      
      _primaryController.text = pText;
      _secondaryController.text = sText;
      
      // If we have text but it doesn't match any option, we are in custom mode
      if (pText.isNotEmpty) {
         final pKey = _mapLangToKey(primaryLang);
         final match = widget.options.any((d) => d[pKey] == pText);
         if (!match) _isCustomMode = true;
      }
      
      _initialized = true;
    }
  }

  
  String _mapLangToKey(String lang) {
    final lower = lang.toLowerCase();
    if (lower == 'aangilam' || lower == 'english' || lower == 'en') return 'en';
    if (lower == 'thamizh' || lower == 'tamil' || lower == 'ta') return 'ta';
    return 'en'; // default
  }

  void _updateValue(String primaryLang, String secondaryLang, {String? pVal, String? sVal}) {
    final updated = Map<String, String>.from(widget.value);
    if (pVal != null) updated[primaryLang] = pVal;
    if (sVal != null) updated[secondaryLang] = sVal;
    widget.onChanged(updated);
  }

  void _handleAutoFill(String val, bool isPrimary, String primaryLang, String secondaryLang) {
    if (val == 'Custom' || val == 'தனிப்பயன்') {
      setState(() {
        _isCustomMode = true;
      });
      _primaryController.clear();
      _secondaryController.clear();
      _updateValue(primaryLang, secondaryLang, pVal: '', sVal: '');
      return;
    }

    final searchKey = _mapLangToKey(isPrimary ? primaryLang : secondaryLang);
    final fillKey = _mapLangToKey(isPrimary ? secondaryLang : primaryLang);

    final match = widget.options.firstWhere(
      (d) => d[searchKey] == val,
      orElse: () => {},
    );

    if (match.isNotEmpty) {
      setState(() {
        _isCustomMode = false;
      });
      if (isPrimary) {
        _secondaryController.text = match[fillKey] ?? '';
        _updateValue(primaryLang, secondaryLang, pVal: val, sVal: match[fillKey]);
      } else {
        _primaryController.text = match[fillKey] ?? '';
        _updateValue(primaryLang, secondaryLang, pVal: match[fillKey], sVal: val);
      }
    } else {
      if (isPrimary) {
        _updateValue(primaryLang, secondaryLang, pVal: val);
      } else {
        _updateValue(primaryLang, secondaryLang, sVal: val);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final isBilingual = ref.watch(bilingualProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);

    ref.listen(primaryLanguageProvider, (previous, next) {
      if (previous != next) {
        _primaryController.text = widget.value[next] ?? '';
      }
    });
    ref.listen(secondaryLanguageProvider, (previous, next) {
      if (previous != next) {
        _secondaryController.text = widget.value[next] ?? '';
      }
    });

    final translatedPrimaryLang = primaryLang.toLowerCase().tr(context, ref);
    final translatedSecondaryLang = secondaryLang.toLowerCase().tr(context, ref);

    final isDesktop = MediaQuery.sizeOf(context).width >= 800;

    final primaryAutocomplete = ElvanSettingsAutocomplete(
      label: '${widget.label} ($translatedPrimaryLang)',
      controller: _primaryController,
      enabled: widget.enabled,
      options: widget.options.map((d) => d[_mapLangToKey(primaryLang)] ?? '').where((s) => s.isNotEmpty).toList(),
      onChanged: (val) {
        if (val.isEmpty && isBilingual) {
          _secondaryController.clear();
          _updateValue(primaryLang, secondaryLang, pVal: '', sVal: '');
        } else {
          _updateValue(primaryLang, secondaryLang, pVal: val);
        }
      },
      onSelected: (val) => _handleAutoFill(val, true, primaryLang, secondaryLang),
      searchMatch: (option, query) {
        final match = widget.options.firstWhere(
            (d) => d[_mapLangToKey(primaryLang)] == option,
            orElse: () => {});
        if (match.isEmpty) return false;
        final q = query.toLowerCase();
        return (match['ta']?.toLowerCase().contains(q) ?? false) ||
               (match['en']?.toLowerCase().contains(q) ?? false);
      },
      subtitleBuilder: isBilingual ? (option) {
        final match = widget.options.firstWhere(
            (d) => d[_mapLangToKey(primaryLang)] == option,
            orElse: () => {});
        return match[_mapLangToKey(secondaryLang)] ?? '';
      } : null,
    );

    final secondaryAutocomplete = ElvanSettingsAutocomplete(
      label: '${widget.label} ($translatedSecondaryLang)',
      controller: _secondaryController,
      options: widget.options.map((d) => d[_mapLangToKey(secondaryLang)] ?? '').where((s) => s.isNotEmpty).toList(),
      onChanged: (val) => _updateValue(primaryLang, secondaryLang, sVal: val),
      onSelected: (val) => _handleAutoFill(val, false, primaryLang, secondaryLang),
      enabled: widget.enabled && _isCustomMode,
      searchMatch: (option, query) {
        final match = widget.options.firstWhere(
            (d) => d[_mapLangToKey(secondaryLang)] == option,
            orElse: () => {});
        if (match.isEmpty) return false;
        final q = query.toLowerCase();
        return (match['ta']?.toLowerCase().contains(q) ?? false) ||
               (match['en']?.toLowerCase().contains(q) ?? false);
      },
      subtitleBuilder: isBilingual ? (option) {
        final match = widget.options.firstWhere(
            (d) => d[_mapLangToKey(secondaryLang)] == option,
            orElse: () => {});
        return match[_mapLangToKey(primaryLang)] ?? '';
      } : null,
    );

    if (!isBilingual) return primaryAutocomplete;

    if (isDesktop) {
      return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(child: primaryAutocomplete),
          const SizedBox(width: 16),
          Expanded(child: secondaryAutocomplete),
        ],
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        primaryAutocomplete,
        const SizedBox(height: 12),
        secondaryAutocomplete,
      ],
    );
  }
}
