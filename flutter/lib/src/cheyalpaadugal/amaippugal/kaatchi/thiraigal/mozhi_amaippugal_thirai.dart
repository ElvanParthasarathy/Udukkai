import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../koorugal/elvan_amaippu_pagudhi.dart';

class LanguageSettingsPage extends ConsumerWidget {
  const LanguageSettingsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentLocale = ref.watch(localeProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanSubpageShell(
      title: K.cheyaliMozhi.tr(context, ref),
      backgroundColor:
          isDark ? const Color(0xFF000000) : const Color(0xFFF3F4F6),
      slivers: [
        SliverPadding(
          padding: const EdgeInsets.only(
            left: 16,
            right: 16,
            top: 0,
            bottom: 32,
          ),
          sliver: SliverList.list(
            children: [
              ElvanSettingsSection(
                dividerIndent: 16.0,
                children: [
                  ElvanRadioSettingsRow<Locale?>(
                    title: K.thaaniyangiAmaippu.tr(context, ref),
                    value: null,
                    groupValue: currentLocale,
                    onChanged: (val) {
                      ref.read(localeProvider.notifier).setLocale(val);
                    },
                  ),
                  ElvanRadioSettingsRow<Locale?>(
                    title: K.thamizh.tr(context, ref),
                    value: const Locale('ta'),
                    groupValue: currentLocale,
                    onChanged: (val) {
                      ref.read(localeProvider.notifier).setLocale(val);
                    },
                  ),
                  ElvanRadioSettingsRow<Locale?>(
                    title: K.aangilam.tr(context, ref),
                    value: const Locale('en'),
                    groupValue: currentLocale,
                    onChanged: (val) {
                      ref.read(localeProvider.notifier).setLocale(val);
                    },
                  ),
                  ElvanRadioSettingsRow<Locale?>(
                    title: K.tamilLatin.tr(context, ref),
                    value: const Locale.fromSubtags(languageCode: 'ta', scriptCode: 'Latn'),
                    groupValue: currentLocale,
                    onChanged: (val) {
                      ref.read(localeProvider.notifier).setLocale(val);
                    },
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}
