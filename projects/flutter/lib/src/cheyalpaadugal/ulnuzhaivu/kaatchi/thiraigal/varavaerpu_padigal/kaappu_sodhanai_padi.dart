import 'dart:async';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/panigal/niril_backup_service.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../koorugal/ullnuzhaivu_koorugal.dart';

class BackupCheckStep extends ConsumerStatefulWidget {
  final VoidCallback onBackupFound;
  final VoidCallback onNoBackupFound;

  const BackupCheckStep({
    super.key,
    required this.onBackupFound,
    required this.onNoBackupFound,
  });

  @override
  ConsumerState<BackupCheckStep> createState() => _BackupCheckStepState();
}

class _BackupCheckStepState extends ConsumerState<BackupCheckStep> {
  String _checkingStatusKey = 'kaapputhTharavuThaedal';
  bool _isCheckingSpinnerVisible = true;

  @override
  void initState() {
    super.initState();
    _startCheckSequence();
  }

  Future<void> _startCheckSequence() async {
    setState(() {
      _checkingStatusKey = 'kaapputhTharavuThaedal';
      _isCheckingSpinnerVisible = true;
    });
    // Artificial delay to let user read the checking message
    await Future.delayed(const Duration(milliseconds: 1500));

    if (!mounted) return;

    // Aggressive Scan: Invalidate cached Riverpod memory to force a physical hard drive scan
    ref.invalidate(hasBackupProvider);

    // Wait for the future to complete
    final hasBackup = await ref.read(hasBackupProvider.future);
    final skipRestore = ref.read(skipRestoreProvider);

    if (hasBackup && !skipRestore) {
      setState(() {
        _checkingStatusKey = 'kaapputhTharavuKidaithadhu';
        _isCheckingSpinnerVisible = false;
      });
      
      await Future.delayed(const Duration(milliseconds: 1200));
      if (!mounted) return;
      widget.onBackupFound();
    } else {
      setState(() {
        _checkingStatusKey = 'kaapputhTharavuIllai';
        _isCheckingSpinnerVisible = false;
      });
      // Do not auto-forward. Wait for user input.
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final textColor = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final textSecondary =
        isDark ? Colors.white.withValues(alpha: 0.6) : const Color(0xFF666666);

    return Center(
      key: const ValueKey('checking_backup'),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          if (_isCheckingSpinnerVisible) ...[
            CircularProgressIndicator(
              color: theme.colorScheme.onSurface,
              strokeWidth: 4,
            ),
            const SizedBox(height: 24),
          ] else ...[
            Icon(
              _checkingStatusKey == 'kaapputhTharavuKidaithadhu'
                  ? CupertinoIcons.checkmark_alt_circle_fill
                  : CupertinoIcons.info_circle_fill,
              size: 48,
              color: _checkingStatusKey == 'kaapputhTharavuKidaithadhu'
                  ? textColor
                  : textSecondary,
            ),
            const SizedBox(height: 16),
          ],
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 300),
            child: Text(
              _checkingStatusKey.tr(context, ref),
              key: ValueKey(_checkingStatusKey),
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w500,
                color: textColor,
                letterSpacing: -0.5,
              ),
            ),
          ),
          if (_checkingStatusKey == 'kaapputhTharavuIllai') ...[
            const SizedBox(height: 32),
            AuthButton(
              text: K.thodaravum.tr(context, ref),
              onPressed: () {
                widget.onNoBackupFound();
              },
            ),
            const SizedBox(height: 16),
            TextButton(
              onPressed: _startCheckSequence,
              style: TextButton.styleFrom(
                foregroundColor: textSecondary,
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(CupertinoIcons.arrow_2_circlepath, size: 18, color: textSecondary),
                  const SizedBox(width: 8),
                  Text(K.meendumThaedu.tr(context, ref)),
                ],
              ),
            ),
          ],
        ],
      ),
    );
  }
}
