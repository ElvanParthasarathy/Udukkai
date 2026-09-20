import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'koorugal/ullnuzhaivu_koorugal.dart';
import '../../../adippadai/panigal/seyali_oaviyangal.dart';
import 'package:flutter_svg/flutter_svg.dart';

class ModeSelectorScreen extends ConsumerWidget {
  const ModeSelectorScreen({
    super.key,
    required this.onModeSelected,
  });

  final ValueChanged<AppMode> onModeSelected;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return AuthLayout(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          AuthHeader(
            title: K.endhachCheyalmurai.tr(context, ref),
            subtitle: K.ungalCheyalmuraiyaithThaerndhedukkavum.tr(context, ref),
          ),
          const SizedBox(height: 60),
          AuthAnimatedElement(
            delayIndex: 2,
            child: Wrap(
              alignment: WrapAlignment.center,
              spacing: 24,
              runSpacing: 32,
              children: [
                _NetflixProfileCard(
                  title: K.nirilKooli.tr(context, ref),
                  iconBuilder: (color, size) => SvgPicture.string(
                    AppSvgs.coolieMode,
                    width: size,
                    height: size,
                    colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
                  ),
                  boxColor: isDark ? const Color(0xFF222222) : Colors.white,
                  iconColor: isDark ? Colors.white : const Color(0xFF111111),
                  isDark: isDark,
                  onTap: () => onModeSelected(AppMode.coolie),
                ),
                _NetflixProfileCard(
                  title: K.nirilPattu.tr(context, ref),
                  iconBuilder: (color, size) => SvgPicture.string(
                    AppSvgs.silkMode,
                    width: size,
                    height: size,
                    colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
                  ),
                  boxColor: isDark ? const Color(0xFF222222) : Colors.white,
                  iconColor: isDark ? Colors.white : const Color(0xFF111111),
                  isDark: isDark,
                  onTap: () => onModeSelected(AppMode.silk),
                ),
              ],
            ),
          ),
          const SizedBox(height: 40),
        ],
      ),
    );
  }
}

class _NetflixProfileCard extends StatefulWidget {
  const _NetflixProfileCard({
    required this.title,
    required this.iconBuilder,
    required this.boxColor,
    required this.iconColor,
    required this.isDark,
    required this.onTap,
  });

  final String title;
  final Widget Function(Color color, double size) iconBuilder;
  final Color boxColor;
  final Color iconColor;
  final bool isDark;
  final VoidCallback onTap;

  @override
  State<_NetflixProfileCard> createState() => _NetflixProfileCardState();
}

class _NetflixProfileCardState extends State<_NetflixProfileCard> {
  bool _isHovered = false;
  bool _isPressed = false;

  @override
  Widget build(BuildContext context) {
    final textColor =
        widget.isDark ? Colors.grey.shade400 : Colors.grey.shade600;
    final textHoverColor = widget.isDark ? Colors.white : Colors.black;

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovered = true),
      onExit: (_) => setState(() => _isHovered = false),
      cursor: SystemMouseCursors.click,
      child: Listener(
        onPointerDown: (_) => setState(() => _isPressed = true),
        onPointerUp: (_) => setState(() => _isPressed = false),
        onPointerCancel: (_) => setState(() => _isPressed = false),
        child: GestureDetector(
          onTap: widget.onTap,
          behavior: HitTestBehavior.opaque,
          child: AnimatedScale(
            scale: _isPressed
                ? 0.94
                : 1.0, // Slightly deeper press for better mobile feedback
            duration: const Duration(milliseconds: 150),
            curve: Curves.easeOutCubic,
            child: Column(
              children: [
                // Netflix-style Square Avatar Box
                AnimatedContainer(
                  duration: const Duration(milliseconds: 300),
                  curve: Curves.easeOutCubic,
                  width: 120,
                  height: 120,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: _isHovered ? textHoverColor : Colors.transparent,
                      width:
                          2, // 2px transparent-to-solid transition like React
                    ),
                    boxShadow: _isHovered
                        ? [
                            BoxShadow(
                              color: widget.isDark
                                  ? Colors.white.withValues(alpha: 0.1)
                                  : Colors.black.withValues(alpha: 0.15),
                              blurRadius: 40,
                              offset: const Offset(0, 20),
                            )
                          ]
                        : [
                            BoxShadow(
                              color: widget.isDark
                                  ? Colors.black.withValues(alpha: 0.2)
                                  : Colors.black.withValues(alpha: 0.08),
                              blurRadius: 16,
                              offset: const Offset(0, 8),
                            )
                          ],
                  ),
                  child: Material(
                    color: widget.boxColor,
                    shape: const CircleBorder(),
                    clipBehavior: Clip.antiAlias,
                    child: Center(
                      child: AnimatedDefaultTextStyle(
                        duration: const Duration(milliseconds: 300),
                        style: TextStyle(
                          color: widget.iconColor,
                        ),
                        child: widget.iconBuilder(widget.iconColor, 56),
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                // Netflix-style title
                AnimatedDefaultTextStyle(
                  duration: const Duration(milliseconds: 300),
                  curve: Curves.easeOutCubic,
                  style: TextStyle(
                    fontFamily: 'ElvanSans',
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    fontFeatures: const [FontFeature.disable('liga')],
                    color: _isHovered ? textHoverColor : textColor,
                  ),
                  child: Text(widget.title),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
