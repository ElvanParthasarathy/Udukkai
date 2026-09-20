import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../niril_pattu/kaatchi/thiraigal/pattu_mugappu_thirai.dart';
import '../../../adippadai/vazhikaattal/niril_nav.dart';
import '../../niril_kooli/kaatchi/thiraigal/kooli_mugappu_thirai.dart';
import '../../niril_podhu/kaatchi/koorugal/vanakkam_pill.dart';
import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../niril_pattu/kaatchi/thiruthi/pattiyal/niril_pattu_pattiyal_thiruthi.dart';
import '../../niril_kooli/kaatchi/thiruthi/pattiyal/niril_kooli_pattiyal_thiruthi.dart';

class MugappuPage extends ConsumerWidget {
  const MugappuPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = ref.watch(appModeProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return SliverMainAxisGroup(
      slivers: [
        SliverToBoxAdapter(
          child: LayoutBuilder(
            builder: (context, constraints) {
              final isMobile = constraints.maxWidth < 600;
              
              final pill = VanakkamPill(
                subtitleKey: mode == AppMode.coolie ? 'nirilKooli' : 'nirilPattu',
              );

              if (isMobile) return pill;

              return Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  pill,
                  Padding(
                    padding: const EdgeInsets.only(right: 16, bottom: 32),
                    child: _MugappuAddButton(
                      isDark: isDark,
                      label: K.pudhiyaPattiyalPtn.tr(context, ref),
                      onPressed: () {
                        final editor = mode == AppMode.coolie
                            ? const CoolieInvoiceEditor()
                            : const SilkInvoiceEditor();
                        NirilNav.push(context, editor);
                      },
                    ),
                  ),
                ],
              );
            },
          ),
        ),
        if (mode == AppMode.coolie)
          const CoolieHomePage()
        else
          const SilkHomePage(),
      ],
    );
  }
}

class _MugappuAddButton extends StatefulWidget {
  final VoidCallback onPressed;
  final String label;
  final bool isDark;

  const _MugappuAddButton({
    required this.onPressed,
    required this.label,
    required this.isDark,
  });

  @override
  State<_MugappuAddButton> createState() => _MugappuAddButtonState();
}

class _MugappuAddButtonState extends State<_MugappuAddButton> {
  bool _isHoveredAdd = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) => setState(() => _isHoveredAdd = true),
      onExit: (_) => setState(() => _isHoveredAdd = false),
      cursor: SystemMouseCursors.click,
      child: GestureDetector(
        onTap: widget.onPressed,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          height: 48,
          padding: const EdgeInsets.symmetric(horizontal: 24),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(999),
            color: _isHoveredAdd
                ? (widget.isDark
                    ? const Color(0xFFE5E5E5)
                    : const Color(0xFF333333))
                : (widget.isDark ? Colors.white : Colors.black),
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
                color: widget.isDark ? Colors.black : Colors.white,
              ),
              const SizedBox(width: 8),
              Text(
                widget.label,
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  color: widget.isDark ? Colors.black : Colors.white,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
