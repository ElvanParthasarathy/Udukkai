import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/viruppangal_paniyagam.dart';
import '../koorugal/ullnuzhaivu_koorugal.dart';

// Import the split components
import 'varavaerpu_padigal/vanakkam_padi.dart';
import 'varavaerpu_padigal/kaappu_sodhanai_padi.dart';
import 'restore_thirai.dart';
import 'varavaerpu_padigal/seyali_mozhi_padi.dart';
import 'varavaerpu_padigal/pattiyal_mozhi_padi.dart';
import 'vanakkam_thirai.dart';

enum WelcomePhase {
  greeting,
  checkingBackup,
  restore,
  language,
  billingLanguage,
  businessName
}

class NalvaravuWelcomePage extends ConsumerStatefulWidget {
  const NalvaravuWelcomePage({super.key});

  @override
  ConsumerState<NalvaravuWelcomePage> createState() =>
      _NalvaravuWelcomePageState();
}

class _NalvaravuWelcomePageState extends ConsumerState<NalvaravuWelcomePage> {
  WelcomePhase _phase = WelcomePhase.greeting;
  String _billingLanguage = 'ta'; // 'Tamil' or 'English'

  @override
  void initState() {
    super.initState();
    // We must wait until after build to read providers safely that might trigger navigation
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final missing = ref.read(missingProfilesProvider);
      if (missing.length == 1) {
        // Only one profile is missing. Skip the grand greeting and language selection.
        if (mounted) {
          setState(() {
            _phase = WelcomePhase.businessName;
          });
        }
      }
    });
  }

  Widget _buildPhaseContent(BuildContext context, ThemeData theme) {
    switch (_phase) {
      case WelcomePhase.greeting:
        return GreetingStep(
          onComplete: () {
            setState(() {
              _phase = WelcomePhase.checkingBackup;
            });
          },
        );

      case WelcomePhase.checkingBackup:
        return BackupCheckStep(
          onBackupFound: () {
            setState(() {
              _phase = WelcomePhase.restore;
            });
          },
          onNoBackupFound: () {
            setState(() {
              _phase = WelcomePhase.language;
            });
          },
        );

      case WelcomePhase.restore:
        return RestorePage(
          onStartFresh: () {
            setState(() {
              _phase = WelcomePhase.language;
            });
          },
        );

      case WelcomePhase.language:
        return AppLanguageStep(
          onBack: () {
            setState(() {
              _phase = WelcomePhase.checkingBackup;
            });
          },
          onLanguageSelected: () {
            setState(() {
              _phase = WelcomePhase.billingLanguage;
            });
          },
        );

      case WelcomePhase.billingLanguage:
        return BillingLanguageStep(
          billingLanguage: _billingLanguage,
          onBack: () {
            setState(() {
              _phase = WelcomePhase.language;
            });
          },
          onLanguageSelected: (lang) {
            setState(() {
              _billingLanguage = lang;
            });
          },
          onContinue: () async {
            final prefs = ref.read(sharedPreferencesProvider);
            await prefs.setString('elvanniril_setup_billingLang', _billingLanguage);
            
            if (!context.mounted) return;
            
            setState(() {
              _phase = WelcomePhase.businessName;
            });
          },
        );

      case WelcomePhase.businessName:
        return VanakkamPage(
          key: const ValueKey('business_name'),
          billingLanguage: _billingLanguage,
          onBack: ref.watch(missingProfilesProvider).length == 1
              ? null // Don't allow back if we skipped directly here
              : () {
                  setState(() {
                    _phase = WelcomePhase.billingLanguage;
                  });
                },
        );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return PopScope(
      canPop: _phase == WelcomePhase.greeting || 
              _phase == WelcomePhase.checkingBackup ||
              _phase == WelcomePhase.restore,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;

        if (_phase == WelcomePhase.language) {
          setState(() {
            _phase = WelcomePhase.checkingBackup;
          });
        } else if (_phase == WelcomePhase.billingLanguage) {
          setState(() {
            _phase = WelcomePhase.language;
          });
        } else if (_phase == WelcomePhase.businessName) {
          final missing = ref.read(missingProfilesProvider);
          if (missing.length != 1) {
            setState(() {
              _phase = WelcomePhase.billingLanguage;
            });
          }
        } else if (_phase == WelcomePhase.restore) {
          setState(() {
            _phase = WelcomePhase.language;
          });
        }
      },
      child: AuthLayout(
        hideLogo: true,
        child: AnimatedSwitcher(
          duration: const Duration(milliseconds: 900),
          reverseDuration: Duration.zero,
          transitionBuilder: (Widget child, Animation<double> animation) {
            // Only delay animation for children other than greeting/checking
            final isInitialPhase = child.key == const ValueKey('greeting_step') ||
                child.key == const ValueKey('checking_backup');

            // Apply a slight upward slide and fade
            final positionAnimation = Tween<Offset>(
              begin: const Offset(0.0, 0.05),
              end: Offset.zero,
            ).animate(CurvedAnimation(
              parent: animation,
              curve: isInitialPhase
                  ? Curves.easeOutCubic
                  : const Interval(0.4, 1.0, curve: Curves.easeOutCubic),
            ));

            final opacityAnimation = CurvedAnimation(
              parent: animation,
              curve: isInitialPhase
                  ? Curves.easeIn
                  : const Interval(0.4, 1.0, curve: Curves.easeIn),
            );

            return FadeTransition(
              opacity: opacityAnimation,
              child: SlideTransition(
                position: positionAnimation,
                child: child,
              ),
            );
          },
          child: _buildPhaseContent(context, theme),
        ),
      ),
    );
  }
}
