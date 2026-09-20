import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';

class ElvanParindhuraiUlleedu extends StatefulWidget {
  final TextEditingController controller;
  final String label;
  final List<String> suggestions;
  final TextInputType? keyboardType;
  final List<TextInputFormatter>? inputFormatters;
  final int? maxLength;
  final String? suffixText;

  const ElvanParindhuraiUlleedu({
    super.key,
    required this.controller,
    required this.label,
    required this.suggestions,
    this.keyboardType,
    this.inputFormatters,
    this.maxLength,
    this.suffixText,
  });

  @override
  State<ElvanParindhuraiUlleedu> createState() =>
      _ElvanParindhuraiUlleeduState();
}

class _ElvanParindhuraiUlleeduState extends State<ElvanParindhuraiUlleedu> {
  final FocusNode _focusNode = FocusNode();
  final OverlayPortalController _overlayController = OverlayPortalController();
  final LayerLink _layerLink = LayerLink();
  bool _isHovering = false;

  @override
  void initState() {
    super.initState();
    _focusNode.addListener(_handleFocusChange);
  }

  @override
  void dispose() {
    _focusNode.removeListener(_handleFocusChange);
    _focusNode.dispose();
    super.dispose();
  }

  void _handleFocusChange() {
    if (_focusNode.hasFocus) {
      _overlayController.show();
    } else {
      if (!_isHovering) {
        _overlayController.hide();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 16, bottom: 8),
          child: Text(
            widget.label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
              letterSpacing: 0.3,
              color: Theme.of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.5),
            ),
          ),
        ),
        CompositedTransformTarget(
          link: _layerLink,
          child: OverlayPortal(
            controller: _overlayController,
            overlayChildBuilder: (context) {
              final RenderBox? renderBox = _focusNode.context?.findRenderObject() as RenderBox?;
              bool showAbove = false;
              if (renderBox != null) {
                final offset = renderBox.localToGlobal(Offset.zero);
                final screenHeight = MediaQuery.of(context).size.height;
                final keyboardHeight = MediaQuery.of(context).viewInsets.bottom;
                final availableSpaceBelow = screenHeight - keyboardHeight - offset.dy - renderBox.size.height;
                if (availableSpaceBelow < 200) {
                  showAbove = true;
                }
              }

              return CompositedTransformFollower(
                link: _layerLink,
                showWhenUnlinked: false,
                targetAnchor: showAbove ? Alignment.topLeft : Alignment.bottomLeft,
                followerAnchor: showAbove ? Alignment.bottomLeft : Alignment.topLeft,
                offset: showAbove ? const Offset(0, -12) : const Offset(0, 12),
                child: Align(
                  alignment: showAbove ? Alignment.bottomLeft : Alignment.topLeft,
                  child: MouseRegion(
                      onEnter: (_) => _isHovering = true,
                      onExit: (_) => _isHovering = false,
                      child: Material(
                        elevation: 8,
                        borderRadius: BorderRadius.circular(12),
                        color: Theme.of(context)
                            .colorScheme
                            .surfaceContainerHigh,
                        child: CustomPaint(
                          painter: _ArrowPainter(
                            color: Theme.of(context).colorScheme.surfaceContainerHigh,
                            isAbove: showAbove,
                          ),
                          child: Container(
                            width: 250,
                            padding: const EdgeInsets.symmetric(vertical: 8.0),
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: widget.suggestions.map((s) {
                                return InkWell(
                                  onTap: () {
                                    widget.controller.text = s;
                                    _overlayController.hide();
                                    _focusNode.unfocus();
                                  },
                                  child: Container(
                                    width: double.infinity,
                                    padding: const EdgeInsets.symmetric(
                                        vertical: 12, horizontal: 20),
                                    child: Text(s,
                                        style: const TextStyle(fontSize: 14)),
                                  ),
                                );
                              }).toList(),
                            ),
                          ),
                        ),
                      ),
                  ),
                ),
              );
            },
            child: ElvanThiruthiUlleedu(
              label: '', // Label is already rendered above by the Column
              controller: widget.controller,
              focusNode: _focusNode,
              keyboardType: widget.keyboardType,
              inputFormatters: widget.inputFormatters,
              maxLength: widget.maxLength,
              suffixText: widget.suffixText,
              suffixIcon: Icon(Icons.arrow_drop_down, size: 20.0),
            ),
          ),
        ),
      ],
    );
  }
}

class _ArrowPainter extends CustomPainter {
  final Color color;
  final bool isAbove;

  _ArrowPainter({required this.color, this.isAbove = false});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    final path = Path();
    if (isAbove) {
      path.moveTo(24, size.height); 
      path.lineTo(32, size.height + 10); // Arrow tip pointing down
      path.lineTo(40, size.height);
    } else {
      path.moveTo(24, 0); 
      path.lineTo(32, -10); // Arrow tip pointing up
      path.lineTo(40, 0); 
    }
    path.close();

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
