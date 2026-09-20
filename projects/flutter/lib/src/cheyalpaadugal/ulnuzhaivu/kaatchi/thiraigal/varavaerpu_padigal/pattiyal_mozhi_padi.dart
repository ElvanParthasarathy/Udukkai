import 'package:flutter/cupertino.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../koorugal/ullnuzhaivu_koorugal.dart';
import '../../koorugal/mozhi_chathuram.dart';

class BillingLanguageStep extends ConsumerWidget {
  final String billingLanguage;
  final ValueChanged<String> onLanguageSelected;
  final VoidCallback onContinue;
  final VoidCallback onBack;

  const BillingLanguageStep({
    super.key,
    required this.billingLanguage,
    required this.onLanguageSelected,
    required this.onContinue,
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
      key: const ValueKey('billingLanguage'),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          AuthBackButton(
            onPressed: onBack,
          ),
          Icon(
            CupertinoIcons.doc_text_fill,
            size: 80,
            color: textColor,
          ),
          const SizedBox(height: 24),
          AuthHeader(
            title: K.pattiyalMudhanmozhi.tr(context, ref),
            subtitle: K.pattiyalmozhi.tr(context, ref),
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
                    isSelected: billingLanguage == 'ta',
                    textColor: textColor,
                    onTap: () {
                      // We handle state outside, but in the old design it just called setState.
                      // Wait, we need to pass back the selection instantly.
                      // I'll change the caller to handle the continuous updating if needed,
                      // but old design just let user pick, then hit continue.
                      // I need to change NalvaravuWelcomePage so it remembers the temp state.
                      // But the easiest way is to pass it back via onLanguageSelected instantly and let caller store it.
                      onLanguageSelected('ta');
                    },
                  ),
                  Divider(
                      height: 1,
                      color: dividerColor,
                      indent: 24,
                      endIndent: 24),
                  LanguageTile(
                    title: 'en',
                    isSelected: billingLanguage == 'en',
                    textColor: textColor,
                    onTap: () {
                      onLanguageSelected('en');
                    },
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          AuthButton(
            text: K.thodaravum.tr(context, ref),
            onPressed: onContinue,
          ),
        ],
      ),
    );
  }
}
