import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../ulnuzhaivu/kaatchi/muraimai_thaervu_thirai.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/panigal/seyali_oaviyangal.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../../adippadai/viruppangal_paniyagam.dart';

class VanakkamPill extends ConsumerWidget {
  final String subtitleKey;

  const VanakkamPill({
    super.key,
    this.subtitleKey = 'nirilPattu',
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final appMode = ref.watch(appModeProvider);
    final userName = ref.watch(payanarKaatchiPeyarProvider);

    return LayoutBuilder(
      builder: (context, constraints) {
        final isMobile = constraints.maxWidth < 600;

        return Align(
          alignment: Alignment.centerLeft,
          child: Container(
            width: isMobile ? double.infinity : null,
            margin: const EdgeInsets.only(left: 16, right: 16, bottom: 32),
            padding: EdgeInsets.only(
              left: isMobile ? 16 : 12,
              top: isMobile ? 16 : 12,
              bottom: isMobile ? 16 : 12,
              right: 32,
            ),
            decoration: BoxDecoration(
              color: isDark ? const Color(0xFF111111) : Colors.white,
              borderRadius: BorderRadius.circular(999),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  margin: EdgeInsets.only(right: isMobile ? 20 : 16),
                  child: Material(
                    color: isDark
                        ? Colors.white.withValues(alpha: 0.08)
                        : Colors.black.withValues(alpha: 0.06),
                    shape: const CircleBorder(),
                    clipBehavior: Clip.hardEdge,
                    child: InkWell(
                      onTap: () {
                        Navigator.of(context, rootNavigator: true).push(
                          PageRouteBuilder(
                            pageBuilder: (context, animation, secondaryAnimation) =>
                                ModeSelectorScreen(
                              onModeSelected: (selectedMode) {
                                ref.read(appModeProvider.notifier).setMode(selectedMode);
                                Navigator.of(context, rootNavigator: true).pop();
                              },
                            ),
                            transitionsBuilder: (context, animation, secondaryAnimation, child) {
                              return FadeTransition(opacity: animation, child: child);
                            },
                          ),
                        );
                      },
                      child: SizedBox(
                        width: isMobile ? 64 : 48,
                        height: isMobile ? 64 : 48,
                        child: Center(
                          child: SvgPicture.string(
                            appMode == AppMode.coolie
                                ? AppSvgs.coolieMode
                                : AppSvgs.silkMode,
                            width: isMobile ? 32 : 20,
                            height: isMobile ? 32 : 20,
                            colorFilter: ColorFilter.mode(
                              isDark ? Colors.white : Colors.black,
                              BlendMode.srcIn,
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
                Flexible(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            K.vanakkam.tr(context, ref),
                            style: TextStyle(
                              fontWeight: FontWeight.w800,
                              letterSpacing: -0.02,
                              height: 1.2,
                              fontSize: isMobile ? 22 : 20,
                              color: isDark ? Colors.white : Colors.black,
                            ),
                          ),
                          const SizedBox(width: 4),
                          const Icon(
                            Icons.auto_awesome,
                            size: 20,
                            color: Color(0xFFFFC107),
                          ),
                        ],
                      ),
                      const SizedBox(height: 2),
                      Text(
                        userName.isNotEmpty
                            ? userName
                            : subtitleKey.tr(context, ref),
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: isDark ? const Color(0xFF9BA1A6) : const Color(0xFF666666),
                          fontSize: isMobile ? 14.4 : 13,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
