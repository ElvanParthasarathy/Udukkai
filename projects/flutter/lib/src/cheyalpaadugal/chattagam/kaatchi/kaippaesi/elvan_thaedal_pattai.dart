import 'package:flutter/cupertino.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../koorugal/ulleedugal/elvan_thooiya_ulleedu.dart';

class ElvanSearchBar extends ConsumerStatefulWidget {
  const ElvanSearchBar({
    super.key,
    required this.focusNode,
    required this.onChanged,
    required this.onClose,
    this.isExpanded = true,
  });

  final FocusNode focusNode;
  final ValueChanged<String>? onChanged;
  final VoidCallback onClose;
  final bool isExpanded;

  @override
  ConsumerState<ElvanSearchBar> createState() => _ElvanSearchBarState();
}

class _ElvanSearchBarState extends ConsumerState<ElvanSearchBar> {
  late final TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bool isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      height: 60,
      decoration: BoxDecoration(
        color: isDark
            ? const Color(0xFF1E1E1E).withValues(alpha: 0.88)
            : const Color(0xFFFFFFFF).withValues(alpha: 0.88),
        borderRadius: BorderRadius.circular(100),
        border: Border.all(
          color: isDark
              ? const Color(0xFF333333).withValues(alpha: 0.6)
              : const Color(0xFFFFFFFF).withValues(alpha: 0.6),
          width: 0.5,
        ),
        boxShadow: [
          BoxShadow(
            blurRadius: 16,
            spreadRadius: 0,
            offset: const Offset(0, 4),
            color: Colors.black.withValues(alpha: 0.05),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0),
        child: AnimatedOpacity(
          duration: const Duration(milliseconds: 200),
          opacity: widget.isExpanded ? 1.0 : 0.0,
          child: Row(
            children: [
              const Icon(CupertinoIcons.search, size: 20, color: Colors.grey),
              const SizedBox(width: 10), // Increased padding to separate icon from cursor
              Expanded(
                child: Stack(
                  alignment: Alignment.centerLeft,
                  children: [
                    // Manual hint text locked in place
                    ValueListenableBuilder<TextEditingValue>(
                      valueListenable: _controller,
                      builder: (context, value, child) {
                        if (value.text.isEmpty) {
                          return Text(
                            K.thaeduga.tr(context, ref),
                            style: const TextStyle(
                              fontSize: 18,
                              color: Colors.grey,
                              height: 1.2,
                            ),
                          );
                        }
                        return const SizedBox.shrink();
                      },
                    ),
                    // Raw input field with no decoration
                    ElvanThooiyaUlleedu(
                      controller: _controller,
                      focusNode: widget.focusNode,
                      onChanged: widget.onChanged,
                      style: const TextStyle(fontSize: 18, height: 1.2),
                    ),
                  ],
                ),
              ),
              GestureDetector(
                onTap: widget.onClose,
                behavior: HitTestBehavior.opaque,
                child: const Padding(
                  padding: EdgeInsets.symmetric(vertical: 8.0, horizontal: 8.0),
                  child: Icon(CupertinoIcons.clear_circled_solid,
                      size: 24, color: Colors.grey),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

