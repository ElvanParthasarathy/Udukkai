import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import '../../../../adippadai/panigal/urai_thervu_karuvi.dart';

/// A pixel-perfect port of the React `ElvanListView` toolbar row.
/// Layout: [Search Pill (flex:1, max 400)] [gap:16] [Edit Pill 48x48] [ml:auto → + Add Button]
///
/// Designed for desktop only — mobile retains its own AppBar-based UX.
class ElvanDesktopToolbar extends StatefulWidget {
  final String searchPlaceholder;
  final String addButtonText;
  final ValueChanged<String> onSearchChanged;
  final VoidCallback? onAdd;
  final VoidCallback? onEditToggle;
  final bool isEditMode;

  const ElvanDesktopToolbar({
    super.key,
    required this.searchPlaceholder,
    required this.addButtonText,
    required this.onSearchChanged,
    this.onAdd,
    this.onEditToggle,
    this.isEditMode = false,
  });

  @override
  State<ElvanDesktopToolbar> createState() => _ElvanDesktopToolbarState();
}

class _ElvanDesktopToolbarState extends State<ElvanDesktopToolbar> {
  final TextEditingController _controller = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  bool _isHoveredEdit = false;
  bool _isHoveredAdd = false;

  @override
  void initState() {
    super.initState();
    _controller.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    // React: py: 4 (32px), px: 4 (32px), maxWidth: 1200, mx: auto
    // Then inner Box: mb: 4 (32px)
    // The row: display: flex, gap: 2 (16px), alignItems: center
    return LayoutBuilder(
      builder: (context, constraints) {
        final bool showLabel = constraints.maxWidth >= 550;

        return Padding(
          padding: const EdgeInsets.only(left: 16, right: 16, top: 0, bottom: 32),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // ── Search Pill ──────────────────────────────────────────────
          // React: flex: 1, minWidth: 0, maxWidth: 400, height: 48,
          //        borderRadius: 100, px: 2.5 (20px), gap: 1.25 (10px),
          //        bgcolor: isDark ? rgba(255,255,255,0.03) : #FFFFFF
          //        boxShadow: none, border: none
          Flexible(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 400),
              child: Container(
                height: 48,
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  color: isDark
                      ? Colors.white.withValues(alpha: 0.08)
                      : Colors.white,
                  borderRadius: BorderRadius.circular(100),
                ),
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  children: [
                    // React: <svg width="16" height="16" opacity: 0.5 stroke="currentColor">
                    Icon(
                      CupertinoIcons.search,
                      size: 16,
                      color: isDark
                          ? Colors.white.withValues(alpha: 0.5)
                          : Colors.black.withValues(alpha: 0.5),
                    ),
                    // React: gap: 1.25 = 10px
                    const SizedBox(width: 0),
                    // React: <input> flex: 1, fontSize: 0.95rem (~15px), padding: 12px 0
                    Expanded(
                      child: TextField(
                        contextMenuBuilder: buildElvanContextMenu,
                        controller: _controller,
                        focusNode: _focusNode,
                        onChanged: widget.onSearchChanged,
                        style: TextStyle(
                          fontSize: 15,
                          color: isDark ? Colors.white : Colors.black,
                          height: 1.4,
                        ),
                        textAlignVertical: TextAlignVertical.center,
                        decoration: InputDecoration(
                          hintText: widget.searchPlaceholder,
                          hintStyle: TextStyle(
                            fontSize: 15,
                            color: isDark
                                ? Colors.white.withValues(alpha: 0.4)
                                : Colors.black.withValues(alpha: 0.4),
                          ),
                          border: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          filled: false,
                          isCollapsed: true,
                        ),
                      ),
                    ),
                    // React: <IconButton size="small"><X size={14}/></IconButton>
                    if (_controller.text.isNotEmpty)
                      MouseRegion(
                        cursor: SystemMouseCursors.click,
                        child: GestureDetector(
                          onTap: () {
                            _controller.clear();
                            widget.onSearchChanged('');
                            _focusNode.requestFocus();
                          },
                          child: Icon(
                            CupertinoIcons.xmark,
                            size: 14,
                            color: isDark
                                ? Colors.white.withValues(alpha: 0.5)
                                : Colors.black.withValues(alpha: 0.5),
                          ),
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ),
          
          const SizedBox(width: 16),

          // ── Right Side Controls ──────────────────────────────────────────────
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              // ── Edit/Pencil Pill ─────────────────────────────────────────
              if (widget.onEditToggle != null) ...[
                MouseRegion(
                  onEnter: (_) => setState(() => _isHoveredEdit = true),
                  onExit: (_) => setState(() => _isHoveredEdit = false),
                  cursor: SystemMouseCursors.click,
                  child: GestureDetector(
                    onTap: widget.onEditToggle,
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: widget.isEditMode
                            ? (isDark
                                ? Colors.white.withValues(alpha: 0.15)
                                : Colors.black.withValues(alpha: 0.1))
                            : _isHoveredEdit
                                ? (isDark
                                    ? Colors.white.withValues(alpha: 0.12)
                                    : Colors.black.withValues(alpha: 0.05))
                                : (isDark
                                    ? Colors.white.withValues(alpha: 0.08)
                                    : Colors.white),
                      ),
                      alignment: Alignment.center,
                      child: Icon(
                        CupertinoIcons.pencil,
                        size: 18,
                        color: isDark ? Colors.white : Colors.black,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
              ],

              // ── + Add Button ────────────────────────────────────────────
              if (widget.onAdd != null)
                MouseRegion(
                  onEnter: (_) => setState(() => _isHoveredAdd = true),
                  onExit: (_) => setState(() => _isHoveredAdd = false),
                  cursor: SystemMouseCursors.click,
                  child: GestureDetector(
                    onTap: widget.onAdd,
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      height: 48,
                      padding: EdgeInsets.symmetric(horizontal: showLabel ? 24 : 14),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(999),
                        color: _isHoveredAdd
                            ? (isDark
                                ? const Color(0xFFE5E5E5)
                                : const Color(0xFF333333))
                            : (isDark ? Colors.white : Colors.black),
                        boxShadow: _isHoveredAdd
                            ? [
                                BoxShadow(
                                  color: Colors.black.withValues(alpha: 0.1),
                                  blurRadius: 12,
                                  offset: const Offset(0, 4),
                                ),
                              ]
                            : [],
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            CupertinoIcons.plus,
                            size: 18,
                            color: isDark ? Colors.black : Colors.white,
                          ),
                          if (showLabel) ...[
                            const SizedBox(width: 8),
                            Text(
                              widget.addButtonText,
                              style: TextStyle(
                                fontSize: 15,
                                fontWeight: FontWeight.w700,
                                color: isDark ? Colors.black : Colors.white,
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  });
 }
}
