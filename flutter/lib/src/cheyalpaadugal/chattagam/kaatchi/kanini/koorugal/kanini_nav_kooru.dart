import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

import '../../kaippaesi/elvan_kizh_pattai.dart'; // For CustomNavItem

/// Desktop sidebar navigation item with expand/collapse animations.
/// Handles both expanded (pill with label) and collapsed (icon-only) states.
class KaniniNavKooru extends StatefulWidget {
  final CustomNavItem item;
  final bool isSelected;
  final bool isDark;
  final bool isCollapsed;
  final VoidCallback onTap;

  const KaniniNavKooru({
    super.key,
    required this.item,
    required this.isSelected,
    required this.isDark,
    required this.isCollapsed,
    required this.onTap,
  });

  @override
  State<KaniniNavKooru> createState() => _KaniniNavKooruState();
}

class _KaniniNavKooruState extends State<KaniniNavKooru> {
  bool _isHovered = false;
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final expandedBgColor = widget.isSelected
        ? (widget.isDark
            ? Colors.white.withValues(alpha: 0.1)
            : Colors.black.withValues(alpha: 0.06))
        : (_isHovered
            ? (widget.isDark
                ? Colors.white.withValues(alpha: 0.08)
                : Colors.black.withValues(alpha: 0.04))
            : Colors.transparent);

    final collapsedBgColor = widget.isSelected
        ? (widget.isDark
            ? Colors.white.withValues(alpha: 0.12)
            : Colors.black.withValues(alpha: 0.06))
        : (_isHovered
            ? (widget.isDark
                ? Colors.white.withValues(alpha: 0.08)
                : Colors.black.withValues(alpha: 0.04))
            : Colors.transparent);

    final fgColor = widget.isSelected || _isHovered
        ? (widget.isDark ? Colors.white : Colors.black)
        : (widget.isDark ? const Color(0xFFAAAAAA) : const Color(0xFF666666));

    final expandedSplash = widget.isDark
        ? Colors.white.withValues(alpha: 0.12)
        : Colors.black.withValues(alpha: 0.12);
    final expandedHighlight = widget.isDark
        ? Colors.white.withValues(alpha: 0.04)
        : Colors.black.withValues(alpha: 0.04);

    return GestureDetector(
      onTap: widget.onTap,
      onTapDown: (_) => setState(() => _isPressed = true),
      onTapCancel: () => setState(() => _isPressed = false),
      onTapUp: (_) => setState(() => _isPressed = false),
      behavior: HitTestBehavior.opaque,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 300),
        curve: const Cubic(0.2, 0.0, 0.0, 1.0),
        width: widget.isCollapsed ? 56 : 236,
        height: widget.isCollapsed ? 78 : 40,
        child: Stack(
          children: [
            // Expanded Pill Background
            IgnorePointer(
              ignoring: widget.isCollapsed,
              child: AnimatedOpacity(
                duration: const Duration(milliseconds: 150),
                opacity: widget.isCollapsed ? 0.0 : 1.0,
                child: MouseRegion(
                  onEnter: (_) => setState(() => _isHovered = true),
                  onExit: (_) => setState(() => _isHovered = false),
                  cursor: SystemMouseCursors.click,
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 300),
                    curve: const Cubic(0.2, 0.0, 0.0, 1.0),
                    width: double.infinity,
                    height: 40,
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(100),
                      color: expandedBgColor,
                    ),
                    child: Material(
                      color: Colors.transparent,
                      child: InkWell(
                        borderRadius: BorderRadius.circular(100),
                        onTap: widget.isCollapsed ? null : widget.onTap,
                        onTapDown: widget.isCollapsed
                            ? null
                            : (_) => setState(() => _isPressed = true),
                        onTapCancel: widget.isCollapsed
                            ? null
                            : () => setState(() => _isPressed = false),
                        onHighlightChanged: widget.isCollapsed
                            ? null
                            : (h) {
                                if (!h) setState(() => _isPressed = false);
                              },
                        hoverColor: expandedHighlight,
                        splashColor: expandedSplash,
                        highlightColor: expandedHighlight,
                      ),
                    ),
                  ),
                ),
              ),
            ),

            // Collapsed Pill Background
            AnimatedAlign(
              duration: const Duration(milliseconds: 300),
              curve: const Cubic(0.2, 0.0, 0.0, 1.0),
              alignment: Alignment.topCenter,
              child: IgnorePointer(
                ignoring: !widget.isCollapsed,
                child: AnimatedOpacity(
                  duration: const Duration(milliseconds: 150),
                  opacity: widget.isCollapsed ? 1.0 : 0.0,
                  child: Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: MouseRegion(
                      onEnter: (_) => setState(() => _isHovered = true),
                      onExit: (_) => setState(() => _isHovered = false),
                      cursor: SystemMouseCursors.click,
                      child: AnimatedContainer(
                        duration: const Duration(milliseconds: 300),
                        curve: const Cubic(0.2, 0.0, 0.0, 1.0),
                        width: 56,
                        height: 32,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.circular(16),
                          color: collapsedBgColor,
                        ),
                        child: Material(
                          color: Colors.transparent,
                          child: InkWell(
                            borderRadius: BorderRadius.circular(16),
                            onTap: widget.isCollapsed ? widget.onTap : null,
                            onTapDown: widget.isCollapsed
                                ? (_) => setState(() => _isPressed = true)
                                : null,
                            onTapCancel: widget.isCollapsed
                                ? () => setState(() => _isPressed = false)
                                : null,
                            onHighlightChanged: widget.isCollapsed
                                ? (h) {
                                    if (!h) setState(() => _isPressed = false);
                                  }
                                : null,
                            hoverColor: Colors.transparent,
                            splashColor: expandedSplash,
                            highlightColor: expandedHighlight,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),

            // Expanded Text
            AnimatedAlign(
              duration: const Duration(milliseconds: 300),
              curve: const Cubic(0.2, 0.0, 0.0, 1.0),
              alignment: Alignment.centerLeft,
              child: Padding(
                padding: const EdgeInsets.only(left: 48),
                child: IgnorePointer(
                  child: AnimatedOpacity(
                    duration: const Duration(milliseconds: 150),
                    opacity: widget.isCollapsed ? 0.0 : 1.0,
                    child: Text(
                      widget.item.label,
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: widget.isSelected
                            ? FontWeight.w700
                            : FontWeight.w500,
                        color: fgColor,
                        height: 1.2,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.visible,
                    ),
                  ),
                ),
              ),
            ),

            // Collapsed Text
            AnimatedAlign(
              duration: const Duration(milliseconds: 300),
              curve: const Cubic(0.2, 0.0, 0.0, 1.0),
              alignment: Alignment.bottomCenter,
              child: IgnorePointer(
                child: AnimatedOpacity(
                  duration: const Duration(milliseconds: 150),
                  opacity: widget.isCollapsed ? 1.0 : 0.0,
                  child: Padding(
                    padding: const EdgeInsets.only(bottom: 6),
                    child: Stack(
                      clipBehavior: Clip.none,
                      alignment: Alignment.topCenter,
                      children: [
                        const SizedBox(height: 26, width: 56),
                        Positioned(
                          left: -10,
                          width: 76,
                          child: Text(
                            widget.item.label.replaceAll(' ', '\n'),
                            textAlign: TextAlign.center,
                            style: TextStyle(
                              fontFamily: 'ElvanSans',
                              fontSize: 10.5,
                              fontWeight: widget.isSelected
                                  ? FontWeight.w700
                                  : FontWeight.w500,
                              color: fgColor,
                              height: 1.2,
                              letterSpacing: 0.2,
                            ),
                            maxLines: 2,
                            softWrap: true,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),

            // The Flying Icon
            AnimatedAlign(
              duration: const Duration(milliseconds: 300),
              curve: const Cubic(0.2, 0.0, 0.0, 1.0),
              alignment: widget.isCollapsed
                  ? Alignment.topCenter
                  : Alignment.centerLeft,
              child: AnimatedPadding(
                duration: const Duration(milliseconds: 300),
                curve: const Cubic(0.2, 0.0, 0.0, 1.0),
                padding: EdgeInsets.only(
                  left: widget.isCollapsed ? 0 : 18,
                  top: widget.isCollapsed ? 12 : 0,
                ),
                child: IgnorePointer(
                  child: AnimatedScale(
                    scale: _isPressed ? 0.85 : 1.0,
                    duration: const Duration(milliseconds: 150),
                    curve: const Cubic(0.4, 0.0, 0.2, 1.0),
                    child:
                        _buildIcon(fgColor, widget.isCollapsed ? 24.0 : 20.0),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildIcon(Color color, double size) {
    if (widget.item.svgString != null) {
      return SvgPicture.string(
        widget.isSelected && widget.item.activeSvgString != null
            ? widget.item.activeSvgString!
            : widget.item.svgString!,
        width: size,
        height: size,
        colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
      );
    } else {
      return Icon(
        widget.isSelected && widget.item.activeIcon != null
            ? widget.item.activeIcon
            : widget.item.icon,
        size: size,
        color: color,
      );
    }
  }
}
