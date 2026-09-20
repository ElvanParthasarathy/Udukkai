import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../adippadai/panigal/urai_thervu_karuvi.dart';

/// A pure, raw text field that bypasses Flutter's stubborn InputDecoration completely.
/// It uses a standard TextField but strictly enforces decoration: null, turning it into
/// a raw EditableText under the hood while retaining native selection & context menus.
class ElvanThooiyaUlleedu extends StatefulWidget {
  final TextEditingController? controller;
  final String? initialValue;
  final FocusNode? focusNode;
  final TextInputType? keyboardType;
  final TextStyle? style;
  final TextAlign textAlign;
  final bool obscureText;
  final int? maxLines;
  final int? minLines;
  final ValueChanged<String>? onChanged;
  final VoidCallback? onEditingComplete;
  final bool enabled;
  final List<TextInputFormatter>? inputFormatters;
  final int? maxLength;
  final bool readOnly;
  final VoidCallback? onTap;

  const ElvanThooiyaUlleedu({
    super.key,
    this.controller,
    this.initialValue,
    this.focusNode,
    this.keyboardType,
    this.style,
    this.textAlign = TextAlign.start,
    this.obscureText = false,
    this.maxLines = 1,
    this.minLines,
    this.onChanged,
    this.onEditingComplete,
    this.enabled = true,
    this.inputFormatters,
    this.maxLength,
    this.readOnly = false,
    this.onTap,
  });

  @override
  State<ElvanThooiyaUlleedu> createState() => _ElvanThooiyaUlleeduState();
}

class _ElvanThooiyaUlleeduState extends State<ElvanThooiyaUlleedu> {
  late TextEditingController _controller;
  late FocusNode _focusNode;

  @override
  void initState() {
    super.initState();
    _controller = widget.controller ?? TextEditingController(text: widget.initialValue);
    _focusNode = widget.focusNode ?? FocusNode();
  }

  @override
  void didUpdateWidget(ElvanThooiyaUlleedu oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.controller != null && widget.controller != _controller) {
      if (oldWidget.controller == null) _controller.dispose();
      _controller = widget.controller!;
    }
    if (widget.focusNode != null && widget.focusNode != _focusNode) {
      if (oldWidget.focusNode == null) _focusNode.dispose();
      _focusNode = widget.focusNode!;
    }
  }

  @override
  void dispose() {
    if (widget.controller == null) _controller.dispose();
    if (widget.focusNode == null) _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // By passing decoration: null, Flutter drops the entire InputDecoration framework.
    // It renders a pure EditableText but with all the nice native scaffolding
    // (context menu, cursor styling, text selection) intact!
    return TextField(
      contextMenuBuilder: buildElvanContextMenu,
      controller: _controller,
      focusNode: _focusNode,
      decoration: null, // THIS IS THE MAGIC - NO DECORATION!
      keyboardType: widget.keyboardType,
      style: widget.style ?? const TextStyle(fontSize: 14),
      textAlign: widget.textAlign,
      obscureText: widget.obscureText,
      maxLines: widget.maxLines,
      minLines: widget.minLines,
      onChanged: widget.onChanged,
      onEditingComplete: widget.onEditingComplete,
      enabled: widget.enabled,
      readOnly: widget.readOnly,
      onTap: widget.onTap,
      inputFormatters: widget.inputFormatters,
      maxLength: widget.maxLength,
      maxLengthEnforcement: widget.maxLength != null ? MaxLengthEnforcement.enforced : null,
    );
  }
}
