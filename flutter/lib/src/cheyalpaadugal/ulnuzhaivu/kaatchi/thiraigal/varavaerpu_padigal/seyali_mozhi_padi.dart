import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../koorugal/ullnuzhaivu_koorugal.dart';
import '../../koorugal/mozhi_chathuram.dart';

class AppLanguageStep extends ConsumerWidget {
  final VoidCallback onLanguageSelected;
  final VoidCallback onBack;

  const AppLanguageStep({
    super.key,
    required this.onLanguageSelected,
    required this.onBack,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final textColor = isDark ? Colors.white : Colors.black;
    final inputBg = isDark ? const Color(0xFF1E1E1E) : Colors.grey[100]!;
    final dividerColor = isDark ? Colors.white12 : Colors.black12;

    final currentLocale = ref.watch(localeProvider);
    final currentLang = currentLocale?.languageCode ?? 'ta';

    return KeyedSubtree(
      key: const ValueKey('language'),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          AuthBackButton(
            onPressed: onBack,
          ),
          Icon(
            CupertinoIcons.globe,
            size: 80,
            color: textColor,
          ),
          const SizedBox(height: 24),
          AuthHeader(
            title: 'mozhiThaervu'.trWithLang(currentLang),
            subtitle: 'viruppaMozhiThaervu'.trWithLang(currentLang),
          ),
          const SizedBox(height: 32),
          AuthAnimatedElement(
            delayIndex: 2,
            child: Container(
              width: double.infinity,
              decoration: BoxDecoration(
                color: inputBg,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                children: [
                  LanguageTile(
                    title: 'தமிழ்',
                    isSelected: currentLang == 'ta',
                    textColor: textColor,
                    onTap: () {
                      ref
                          .read(localeProvider.notifier)
                          .setLocale(const Locale('ta'));
                    },
                  ),
                  Divider(
                      height: 1,
                      color: dividerColor,
                      indent: 24,
                      endIndent: 24),
                  LanguageTile(
                    title: 'en',
                    isSelected: currentLang == 'en',
                    textColor: textColor,
                    onTap: () {
                      ref
                          .read(localeProvider.notifier)
                          .setLocale(const Locale('en'));
                    },
                  ),
                  Divider(
                      height: 1,
                      color: dividerColor,
                      indent: 24,
                      endIndent: 24),
                  LanguageTile(
                    title: 'Thamizh Latin',
                    isSelected: currentLang == 'ta-Latn',
                    textColor: textColor,
                    onTap: () {
                      ref
                          .read(localeProvider.notifier)
                          .setLocale(const Locale.fromSubtags(languageCode: 'ta', scriptCode: 'Latn'));
                    },
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          AuthButton(
            text: 'thodaravum'.trWithLang(currentLang),
            onPressed: () {
              // If they hit continue without picking, explicitly save the default Tamil
              if (ref.read(localeProvider) == null) {
                ref.read(localeProvider.notifier).setLocale(const Locale('ta'));
              }
              onLanguageSelected();
            },
          ),
        ],
      ),
    );
  }
}
