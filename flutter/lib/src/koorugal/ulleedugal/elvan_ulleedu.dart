import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../adippadai/panigal/urai_thervu_karuvi.dart';

class ElvanTextField extends StatelessWidget {
  final TextEditingController? controller;
  final String? initialValue;
  final FocusNode? focusNode;
  final InputDecoration? decoration;
  final TextInputType? keyboardType;
  final TextStyle? style;
  final TextAlign textAlign;
  final TextAlignVertical? textAlignVertical;
  final bool obscureText;
  final int? maxLines;
  final int? minLines;
  final ValueChanged<String>? onChanged;
  final VoidCallback? onEditingComplete;
  final bool? enabled;
  final List<TextInputFormatter>? inputFormatters;
  final int? maxLength;

  const ElvanTextField({
    super.key,
    this.controller,
    this.initialValue,
    this.focusNode,
    this.decoration,
    this.keyboardType,
    this.style,
    this.textAlign = TextAlign.start,
    this.textAlignVertical,
    this.obscureText = false,
    this.maxLines = 1,
    this.minLines,
    this.onChanged,
    this.onEditingComplete,
    this.enabled,
    this.inputFormatters,
    this.maxLength,
  });

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      contextMenuBuilder: buildElvanContextMenu,
      controller: controller,
      initialValue: initialValue,
      focusNode: focusNode,
      decoration: decoration != null
          ? decoration!.copyWith(
              counterText: decoration!.counterText ??
                  (maxLength != null ? '' : null),
            )
          : (maxLength != null ? const InputDecoration(counterText: '') : null),
      keyboardType: keyboardType,
      style: style,
      textAlign: textAlign,
      textAlignVertical: textAlignVertical,
      obscureText: obscureText,
      maxLines: maxLines,
      minLines: minLines,
      onChanged: onChanged,
      onEditingComplete: onEditingComplete,
      enabled: enabled,
      inputFormatters: inputFormatters,
      maxLength: maxLength,
      maxLengthEnforcement: maxLength != null ? MaxLengthEnforcement.enforced : null,
    );
  }
}
