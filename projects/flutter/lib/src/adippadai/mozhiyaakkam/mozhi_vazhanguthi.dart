import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'language_keys/ta.dart';
import 'language_keys/en.dart';
import 'language_keys/ta_latn.dart';
import '../viruppangal_paniyagam.dart';

class LocaleNotifier extends Notifier<Locale?> {
  @override
  Locale? build() {
    return ref.watch(preferencesServiceProvider).getLocale();
  }

  void setLocale(Locale? locale) {
    state = locale;
    ref.read(preferencesServiceProvider).setLocale(locale);
  }
}

/// Manages the selected app language.
/// Null means "System Default".
final localeProvider = NotifierProvider<LocaleNotifier, Locale?>(() {
  return LocaleNotifier();
});

/// Simple translation engine.
extension StringLocalization on String {
  /// Translates this string based on the current locale provider state.
  /// If [ref] is provided, it watches the provider. Otherwise it attempts a read.
  String tr(BuildContext context, WidgetRef ref) {
    final currentLocale = ref.watch(localeProvider);

    // If system default, try to guess from context
    Locale effectiveLocale = currentLocale ?? Localizations.localeOf(context);

    // Our keys in translations.dart are mainly English strings, mapped to Tamil in `ta`.
    if (effectiveLocale.languageCode == 'ta') {
      if (effectiveLocale.scriptCode == 'Latn') {
        return taLatn[this] ?? this;
      }
      return ta[this] ?? this;
    }

    // Fallback to english map if it exists, otherwise just return the key.
    return en[this] ?? this;
  }

  /// Translates this string explicitly into the requested language code.
  String trWithLang(String langCode) {
    if (langCode == 'ta') {
      return ta[this] ?? this;
    }
    if (langCode == 'ta-Latn') {
      return taLatn[this] ?? this;
    }
    return en[this] ?? this;
  }
}
