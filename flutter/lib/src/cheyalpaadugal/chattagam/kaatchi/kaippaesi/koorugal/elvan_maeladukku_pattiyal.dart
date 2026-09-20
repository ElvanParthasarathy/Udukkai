import 'package:flutter/cupertino.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../amaippugal/kaatchi/amaippugal_thirai.dart';
import 'elvan_mel_pattai_chinnam.dart';

import 'package:flutter_svg/flutter_svg.dart';
import '../../../../niril_podhu/kaatchi/thiraigal/meetpagam_thirai.dart';
import '../../../../../adippadai/vazhikaattal/niril_nav.dart';

const String _settingsOutlineSvg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M128,80a48,48,0,1,0,48,48A48.05,48.05,0,0,0,128,80Zm0,80a32,32,0,1,1,32-32A32,32,0,0,1,128,160Zm109.94-52.79a8,8,0,0,0-3.89-5.4l-29.83-17-.12-33.62a8,8,0,0,0-2.83-6.08,111.91,111.91,0,0,0-36.72-20.67,8,8,0,0,0-6.46.59L128,41.85,97.88,25a8,8,0,0,0-6.47-.6A112.1,112.1,0,0,0,54.73,45.15a8,8,0,0,0-2.83,6.07l-.15,33.65-29.83,17a8,8,0,0,0-3.89,5.4,106.47,106.47,0,0,0,0,41.56,8,8,0,0,0,3.89,5.4l29.83,17,.12,33.62a8,8,0,0,0,2.83,6.08,111.91,111.91,0,0,0,36.72,20.67,8,8,0,0,0,6.46-.59L128,214.15,158.12,231a7.91,7.91,0,0,0,3.9,1,8.09,8.09,0,0,0,2.57-.42,112.1,112.1,0,0,0,36.68-20.73,8,8,0,0,0,2.83-6.07l.15-33.65,29.83-17a8,8,0,0,0,3.89-5.4A106.47,106.47,0,0,0,237.94,107.21Zm-15,34.91-28.57,16.25a8,8,0,0,0-3,3c-.58,1-1.19,2.06-1.81,3.06a7.94,7.94,0,0,0-1.22,4.21l-.15,32.25a95.89,95.89,0,0,1-25.37,14.3L134,199.13a8,8,0,0,0-3.91-1h-.19c-1.21,0-2.43,0-3.64,0a8.08,8.08,0,0,0-4.1,1l-28.84,16.1A96,96,0,0,1,67.88,201l-.11-32.2a8,8,0,0,0-1.22-4.22c-.62-1-1.23-2-1.8-3.06a8.09,8.09,0,0,0-3-3.06l-28.6-16.29a90.49,90.49,0,0,1,0-28.26L61.67,97.63a8,8,0,0,0,3-3c.58-1,1.19-2.06,1.81-3.06a7.94,7.94,0,0,0,1.22-4.21l.15-32.25a95.89,95.89,0,0,1,25.37-14.3L122,56.87a8,8,0,0,0,4.1,1c1.21,0,2.43,0,3.64,0a8.08,8.08,0,0,0,4.1-1l28.84-16.1A96,96,0,0,1,188.12,55l.11,32.2a8,8,0,0,0,1.22,4.22c.62,1,1.23,2,1.8,3.06a8.09,8.09,0,0,0,3,3.06l28.6,16.29A90.49,90.49,0,0,1,222.9,142.12Z"></path></svg>';

final popupMenuOpenProvider = StateProvider<bool>((ref) => false);

/// A highly optimized custom popup component.
/// Completely bypasses Material's PopupMenuButton to provide precise styling,
/// animations, and zero-padding logic.
class ElvanPopupMenu extends ConsumerStatefulWidget {
  final bool showSelectOption;
  final bool isSilkHome;
  final VoidCallback? onSelectTapped;

  const ElvanPopupMenu({
    super.key,
    this.showSelectOption = false,
    this.isSilkHome = false,
    this.onSelectTapped,
  });

  @override
  ConsumerState<ElvanPopupMenu> createState() => _ElvanPopupMenuState();
}

class _ElvanPopupMenuState extends ConsumerState<ElvanPopupMenu> {
  OverlayEntry? _overlayEntry;
  bool _isOpen = false;

  void _toggleMenu() {
    if (_isOpen) {
      _closeMenu();
    } else {
      _showMenu();
    }
  }

  void _closeMenu() {
    _overlayEntry?.remove();
    _overlayEntry = null;
    ref.read(popupMenuOpenProvider.notifier).state = false;
    setState(() {
      _isOpen = false;
    });
  }

  void _showMenu() {
    ref.read(popupMenuOpenProvider.notifier).state = true;
    final RenderBox renderBox = context.findRenderObject() as RenderBox;
    final size = renderBox.size;
    final globalOffset = renderBox.localToGlobal(Offset.zero);

    final isDark = Theme.of(context).brightness == Brightness.dark;
    final sharedHighlightColor = isDark
        ? Colors.white.withValues(alpha: 0.35)
        : Colors.black.withValues(alpha: 0.08);
    final sharedSplashColor = isDark
        ? Colors.white.withValues(alpha: 0.15)
        : Colors.black.withValues(alpha: 0.04);

    _overlayEntry = OverlayEntry(
      builder: (context) => Stack(
        children: [
          // Invisible dismiss layer covering the whole screen
          // We use a translucent Listener instead of GestureDetector so that the touch event
          // physically passes through this layer down to the ScrollView beneath it!
          // This allows the user to scroll or tap the background while closing the popup seamlessly.
          Positioned.fill(
            child: Listener(
              behavior: HitTestBehavior.translucent,
              onPointerDown: (_) => _closeMenu(),
              child: Container(),
            ),
          ),
          Positioned(
            right: MediaQuery.sizeOf(context).width -
                (globalOffset.dx + size.width + 11),
            top: globalOffset.dy - 12,
            child: Material(
              color: Colors.transparent,
              child: TweenAnimationBuilder<double>(
                tween: Tween(begin: 0.0, end: 1.0),
                duration: const Duration(milliseconds: 150),
                curve: Curves.easeOutCubic,
                builder: (context, value, child) {
                  return Transform.scale(
                    scale: 0.2 + (0.8 * value), // Grows from 20% to 100% size
                    alignment: Alignment
                        .topRight, // Originates from the top right corner (where the 3 dots are)
                    child: Opacity(
                      opacity: value,
                      child: child,
                    ),
                  );
                },
                child: Container(
                  decoration: BoxDecoration(
                    color: (Theme.of(context).brightness == Brightness.dark
                            ? const Color(0xFF1E1E1E)
                            : Colors.white)
                        .withValues(alpha: 0.88),
                    borderRadius: BorderRadius.circular(24),
                    border: Border.all(
                      color: Theme.of(context).brightness == Brightness.dark
                          ? const Color(0xFF333333).withValues(alpha: 0.15)
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
                  child: Material(
                    color: Colors.transparent,
                    child: IntrinsicWidth(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          InkWell(
                            borderRadius:
                                const BorderRadius.vertical(
                                    top: Radius.circular(24)),
                            highlightColor: sharedHighlightColor,
                            splashColor: sharedSplashColor,
                            onTap: () {
                              _closeMenu();
                              NirilNav.push(context, const SettingsScreen());
                            },
                            child: Padding(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 16,
                                  vertical:
                                      14), // Exactly 50px tall (14 + 22 + 14)
                              child: Row(
                                children: [
                                  SvgPicture.string(
                                    _settingsOutlineSvg,
                                    width: 22,
                                    height: 22,
                                    colorFilter: ColorFilter.mode(
                                      Theme.of(context).brightness ==
                                              Brightness.dark
                                          ? Colors.white
                                          : Colors.black,
                                      BlendMode.srcIn,
                                    ),
                                  ),
                                  const SizedBox(width: 12),
                                  Text(K.amaippugal.tr(context, ref),
                                      style: const TextStyle(
                                          fontSize: 14,
                                          fontWeight: FontWeight.w500)),
                                ],
                              ),
                            ),
                          ),
                          // ── Meetpagam (Recycle Bin) — Always shown ──
                          InkWell(
                            borderRadius: (widget.showSelectOption || widget.isSilkHome)
                                ? BorderRadius.zero
                                : const BorderRadius.vertical(
                                    bottom: Radius.circular(24)),
                            highlightColor: sharedHighlightColor,
                            splashColor: sharedSplashColor,
                            onTap: () {
                              _closeMenu();
                              NirilNav.push(context, const MeetpagamThirai());
                            },
                            child: Padding(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 16, vertical: 14),
                              child: Row(
                                children: [
                                  Icon(
                                    CupertinoIcons.trash,
                                    size: 22,
                                    color: Theme.of(context).brightness ==
                                            Brightness.dark
                                        ? Colors.white
                                        : Colors.black,
                                  ),
                                  const SizedBox(width: 12),
                                  Text(K.meetpagam.tr(context, ref),
                                      style: const TextStyle(
                                          fontSize: 14,
                                          fontWeight: FontWeight.w500)),
                                ],
                              ),
                            ),
                          ),

                          if (widget.showSelectOption) ...[
                            InkWell(
                              borderRadius: const BorderRadius.vertical(
                                  bottom: Radius.circular(24)),
                              highlightColor: sharedHighlightColor,
                              splashColor: sharedSplashColor,
                              onTap: () {
                                _closeMenu();
                                widget.onSelectTapped?.call();
                              },
                              child: Padding(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 16, vertical: 14),
                                child: Row(
                                  children: [
                                    Icon(
                                      CupertinoIcons.checkmark_circle,
                                      size: 22,
                                      color: Theme.of(context).brightness ==
                                              Brightness.dark
                                          ? Colors.white
                                          : Colors.black,
                                    ),
                                    const SizedBox(width: 12),
                                    Text(K.thaerndhedu.tr(context, ref),
                                        style: const TextStyle(
                                            fontSize: 14,
                                            fontWeight: FontWeight.w500)),
                                  ],
                                ),
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );

    Overlay.of(context).insert(_overlayEntry!);
    setState(() {
      _isOpen = true;
    });
  }

  @override
  void dispose() {
    _overlayEntry?.remove();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ElvanTopBarIcon(
      icon: CupertinoIcons.ellipsis_vertical,
      onTap: _toggleMenu,
    );
  }
}
