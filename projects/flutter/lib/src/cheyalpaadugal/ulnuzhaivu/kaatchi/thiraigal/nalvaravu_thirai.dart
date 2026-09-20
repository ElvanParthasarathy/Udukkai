import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'ullnuzhaivu_thirai.dart';
import '../koorugal/ullnuzhaivu_koorugal.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

class WelcomePage extends ConsumerWidget {
  const WelcomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return AuthLayout(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // LOGO SECTION
          AuthAnimatedElement(
            delayIndex: 0,
            child: Container(
              width: 96,
              height: 96,
              decoration: BoxDecoration(
                color: isDark ? Colors.white : const Color(0xFF111111),
                borderRadius: BorderRadius.circular(28),
                boxShadow: [
                  BoxShadow(
                    color: isDark
                        ? Colors.white.withValues(alpha: 0.15)
                        : Colors.black.withValues(alpha: 0.08),
                    blurRadius: 40,
                    offset: const Offset(0, 20),
                  ),
                ],
              ),
              child: Center(
                child: Icon(
                  CupertinoIcons
                      .square_stack_3d_up_fill, // Approximation of Database icon
                  size: 48,
                  color: isDark ? Colors.black : Colors.white,
                ),
              ),
            ),
          ),

          const SizedBox(height: 40),

          // TEXT SECTION
          AuthHeader(
            title: K.nirilirkuNalvaravu.tr(context, ref),
            subtitle: K.pattiyalgstelidhu.tr(context, ref),
          ),

          const SizedBox(height: 40),

          // BUTTON SECTION
          AuthAnimatedElement(
            delayIndex: 2,
            child: Text(
              K.nirilthodanga.tr(context, ref),
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 12,
                color: isDark
                    ? Colors.white.withValues(alpha: 0.4)
                    : const Color(0xFF999999),
                height: 1.5,
              ),
            ),
          ),

          AuthButton(
            text: K.thodangugaPtn.tr(context, ref),
            onPressed: () {
              Navigator.push(
                context,
                PageRouteBuilder(
                  pageBuilder: (context, animation, secondaryAnimation) =>
                      const LoginPage(),
                  transitionsBuilder:
                      (context, animation, secondaryAnimation, child) {
                    return FadeTransition(opacity: animation, child: child);
                  },
                  transitionDuration: const Duration(milliseconds: 300),
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}
