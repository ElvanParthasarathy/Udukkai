import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thooiya_ulleedu.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_thalaippu.dart';

/// A standard pill-shaped text field component designed specifically for Elvan Editors (Thiruthi).
/// Uses ElvanThooiyaUlleedu to bypass InputDecoration constraints, offering perfect 45px vertical centering.
class ElvanThiruthiUlleedu extends StatefulWidget {
  final String? label;
  final TextEditingController? controller;
  final String? initialValue;
  final bool enabled;
  final bool autofocus;
  final TextInputType? keyboardType;
  final String? prefixText;
  final String? suffixText;
  final String? errorText;
  final TextCapitalization textCapitalization;
  final ValueChanged<String>? onChanged;
  final List<TextInputFormatter>? inputFormatters;
  final int? maxLength;
  final int? maxLines;
  final Widget? prefixIcon;
  final Widget? suffixIcon;
  final FocusNode? focusNode;
  final String? hintText;
  final bool readOnly;
  final VoidCallback? onTap;
  final Color? backgroundColor;

  const ElvanThiruthiUlleedu({
    super.key,
    this.label,
    this.controller,
    this.initialValue,
    this.enabled = true,
    this.autofocus = false,
    this.keyboardType,
    this.prefixText,
    this.suffixText,
    this.errorText,
    this.textCapitalization = TextCapitalization.none,
    this.onChanged,
    this.inputFormatters,
    this.maxLength,
    this.maxLines = 1,
    this.prefixIcon,
    this.suffixIcon,
    this.focusNode,
    this.hintText,
    this.readOnly = false,
    this.onTap,
    this.backgroundColor,
  });

  @override
  State<ElvanThiruthiUlleedu> createState() => _ElvanThiruthiUlleeduState();
}

class _ElvanThiruthiUlleeduState extends State<ElvanThiruthiUlleedu> {
  late FocusNode _focusNode;
  bool _isFocused = false;

  @override
  void initState() {
    super.initState();
    _focusNode = widget.focusNode ?? FocusNode();
    _focusNode.addListener(_onFocusChange);
  }

  @override
  void didUpdateWidget(ElvanThiruthiUlleedu oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.focusNode != oldWidget.focusNode) {
      _focusNode.removeListener(_onFocusChange);
      if (oldWidget.focusNode == null) {
        _focusNode.dispose();
      }
      _focusNode = widget.focusNode ?? FocusNode();
      _focusNode.addListener(_onFocusChange);
    }
  }

  @override
  void dispose() {
    _focusNode.removeListener(_onFocusChange);
    if (widget.focusNode == null) {
      _focusNode.dispose();
    }
    super.dispose();
  }

  void _onFocusChange() {
    setState(() {
      _isFocused = _focusNode.hasFocus;
    });
  }

  @override
  Widget build(BuildContext context) {
    final isSingleLine = widget.maxLines == 1;
    final borderRadius = isSingleLine ? 100.0 : 16.0;
    final cs = Theme.of(context).colorScheme;

    // The raw text field without any padding or decorations
    final rawInput = ElvanThooiyaUlleedu(
      controller: widget.controller,
      initialValue: widget.initialValue,
      focusNode: _focusNode,
      enabled: widget.enabled,
      keyboardType: widget.keyboardType,
      onChanged: widget.onChanged,
      inputFormatters: widget.inputFormatters,
      readOnly: widget.readOnly,
      onTap: widget.onTap,
      maxLength: widget.maxLength,
      maxLines: widget.maxLines,
      style: const TextStyle(fontSize: 14),
    );

    // Build the custom input area
    Widget inputArea;
    if (isSingleLine) {
      inputArea = SizedBox(
        height: 45.0,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            if (widget.prefixIcon != null) ...[
              widget.prefixIcon!,
              const SizedBox(width: 8),
            ] else
              const SizedBox(width: 20),
            
            if (widget.prefixText != null) ...[
              Text(widget.prefixText!, style: TextStyle(fontSize: 14, color: cs.onSurfaceVariant)),
              const SizedBox(width: 4),
            ],

            Expanded(
              child: Stack(
                alignment: Alignment.centerLeft,
                children: [
                  // Hint Text
                  if (widget.hintText != null)
                    ValueListenableBuilder<TextEditingValue>(
                      valueListenable: widget.controller ?? TextEditingController(),
                      builder: (context, value, child) {
                        if (value.text.isEmpty && (widget.initialValue == null || widget.initialValue!.isEmpty)) {
                          return Text(
                            widget.hintText!,
                            style: TextStyle(
                              fontSize: 14,
                              color: cs.onSurface.withValues(alpha: 0.4),
                            ),
                          );
                        }
                        return const SizedBox.shrink();
                      },
                    ),
                  // Raw Editable Text
                  Align(
                    alignment: Alignment.centerLeft,
                    child: rawInput,
                  ),
                ],
              ),
            ),

            if (widget.suffixText != null) ...[
              const SizedBox(width: 4),
              Text(widget.suffixText!, style: TextStyle(fontSize: 14, color: cs.onSurfaceVariant)),
            ],

            if (widget.suffixIcon != null) ...[
              const SizedBox(width: 8),
              widget.suffixIcon!,
              const SizedBox(width: 16),
            ] else
              const SizedBox(width: 20),
          ],
        ),
      );
    } else {
      inputArea = Padding(
        padding: const EdgeInsets.all(16.0),
        child: Stack(
          alignment: Alignment.topLeft,
          children: [
            if (widget.hintText != null)
              ValueListenableBuilder<TextEditingValue>(
                valueListenable: widget.controller ?? TextEditingController(),
                builder: (context, value, child) {
                  if (value.text.isEmpty && (widget.initialValue == null || widget.initialValue!.isEmpty)) {
                    return Text(
                      widget.hintText!,
                      style: TextStyle(
                        fontSize: 14,
                        color: cs.onSurface.withValues(alpha: 0.4),
                      ),
                    );
                  }
                  return const SizedBox.shrink();
                },
              ),
            rawInput,
          ],
        ),
      );
    }

    final isInsideCard = ElvanAttaiSoolal.check(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (widget.label != null && widget.label!.isNotEmpty) ElvanThiruthiThalaippu(label: widget.label!),
        AnimatedContainer(
          duration: const Duration(milliseconds: 150),
          curve: Curves.easeInOut,
          decoration: BoxDecoration(
            color: widget.backgroundColor ?? (_isFocused 
                ? (cs.brightness == Brightness.light && !isInsideCard ? Colors.white : cs.onSurface.withValues(alpha: 0.12))
                : (cs.brightness == Brightness.light && !isInsideCard ? Colors.white : cs.onSurface.withValues(alpha: 0.08))),
            borderRadius: BorderRadius.circular(borderRadius),
          ),
          clipBehavior: Clip.antiAlias,
          child: Material(
            color: Colors.transparent,
            child: inputArea,
          ),
        ),
        if (widget.errorText != null && widget.errorText!.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(top: 8, left: 12),
            child: Text(
              widget.errorText!,
              style: TextStyle(
                color: cs.error,
                fontSize: 12,
              ),
            ),
          ),
      ],
    );
  }
}
