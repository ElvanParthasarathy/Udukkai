import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../../../chattagam/kaatchi/kaippaesi/koorugal/elvan_cheyal_pothan.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
/// The Universal Editor Shell — wraps all creator/editor forms.
///
/// Mobile: existing navbar with text save button.
/// Desktop: Cancel + Save pill buttons at the bottom of the form.
/// Content is constrained to maxWidth 1200 matching React's ElvanEditorLayout.
///
/// Set [hasUnsavedChanges] to `true` to guard against accidental back-nav.
/// When the user confirms discard, [onDiscard] is called before popping.
class ElvanEditorShell extends ConsumerStatefulWidget {
  const ElvanEditorShell({
    super.key,
    required this.title,
    required this.child,
    this.onSave,
    this.hasUnsavedChanges = false,
    this.onDiscard,
  });

  final String title;
  final Widget child;
  final VoidCallback? onSave;

  /// Whether the form has unsaved edits. When `true`, back-nav shows a
  /// confirmation dialog instead of popping immediately.
  final bool hasUnsavedChanges;

  /// Called when the user confirms discarding unsaved changes.
  final VoidCallback? onDiscard;

  @override
  ConsumerState<ElvanEditorShell> createState() => _ElvanEditorShellState();
}

class _ElvanEditorShellState extends ConsumerState<ElvanEditorShell> {
  // ── Unsaved-changes confirmation dialog ──
  Future<void> _showUnsavedChangesDialog() async {
    await showElvanActionSheet<void>(
      context: context,
      title: K.chaemippuNiluvai.tr(context, ref),
      cancelText: K.thodarPtn.tr(context, ref),
      tertiaryText: K.purakkaniPtn.tr(context, ref),
      onTertiary: () {
        widget.onDiscard?.call();
        Navigator.of(context).pop();
      },
      confirmText: K.chaemiPtn.tr(context, ref),
      onConfirm: () {
        widget.onSave?.call();
      },
    );
  }

  /// Handles Cancel button / back-nav: if unsaved → dialog, else pop.
  void _handleCancel() {
    if (widget.hasUnsavedChanges) {
      _showUnsavedChangesDialog();
    } else {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDesktop = MediaQuery.sizeOf(context).width >= 800;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final shell = PopScope(
      canPop: !widget.hasUnsavedChanges,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop && widget.hasUnsavedChanges) {
          _showUnsavedChangesDialog();
        }
      },
      child: ElvanSubpageShell(
        title: widget.title,
        maxWidth: double.infinity,
        hideHeaderOnDesktop: true,
        navActions: [
          if (widget.onSave != null)
            ElvanCheyalPothan(
              label: K.chaemiPtn.tr(context, ref),
              onPressed: widget.onSave,
            ),
        ],
        slivers: [
          SliverToBoxAdapter(
            child: Align(
              alignment: Alignment.topLeft,
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: double.infinity),
                child: Padding(
                  padding: EdgeInsets.only(
                    left: 16,
                    right: 16,
                    top: isDesktop ? 32 : 8,
                    bottom: isDesktop ? 16 : 32,
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                  // ── Desktop React-Style Header ──
                  if (isDesktop) ...[
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        Container(
                          decoration: BoxDecoration(
                            color: isDark
                                ? Theme.of(context).colorScheme.surface
                                : Colors.white,
                            shape: BoxShape.circle,
                          ),
                          child: IconButton(
                            icon: const Icon(Icons.chevron_left_rounded),
                            onPressed: _handleCancel,
                          ),
                        ),
                        const SizedBox(width: 16),
                        Text(
                          widget.title,
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        if (widget.onSave != null) ...[
                          const Spacer(),
                          FilledButton(
                            onPressed: widget.onSave,
                            style: FilledButton.styleFrom(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 32, vertical: 18),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(50),
                              ),
                                backgroundColor: isDark
                                    ? Theme.of(context).colorScheme.surface
                                    : Colors.white,
                                foregroundColor: Theme.of(context)
                                    .colorScheme
                                    .onSurface,
                              ),
                              child: Text(K.chaemiPtn.tr(context, ref)),
                            ),
                        ],
                      ],
                    ),
                        const SizedBox(height: 32),
                      ],

                      // ── Main Form Content ──
                      widget.child,
                      
                      if (isDesktop) const SizedBox(height: 48),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );

    return shell;
  }
}
