import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:shared_preferences/shared_preferences.dart';

// Provider that holds the initialized SharedPreferences instance.
// We override this in main() after initialization.
final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError();
});

class PreferencesService {
  PreferencesService(this._prefs);
  final SharedPreferences _prefs;

  static const _themeKey = 'app_theme_mode';
  static const _localeKey = 'app_locale';
  static const _isLoggedInKey = 'is_logged_in';

  static const _isSidebarCollapsedKey = 'is_sidebar_collapsed';

  // ── Payanar (User Profile) ──
  static const _payanarMudhalPeyarKey = 'payanar_mudhal_peyar';
  static const _payanarIrudhiPeyarKey = 'payanar_irudhi_peyar';
  static const _payanarPirandhaThaedhiKey = 'payanar_pirandha_thaedhi';

  // --- Auth ---
  bool getIsLoggedIn() {
    return _prefs.getBool(_isLoggedInKey) ?? false;
  }

  Future<void> setIsLoggedIn(bool value) async {
    await _prefs.setBool(_isLoggedInKey, value);
  }

  // --- Sidebar ---
  bool getIsSidebarCollapsed() {
    return _prefs.getBool(_isSidebarCollapsedKey) ?? false;
  }

  Future<void> setIsSidebarCollapsed(bool value) async {
    await _prefs.setBool(_isSidebarCollapsedKey, value);
  }

  // --- Theme ---
  ThemeMode getThemeMode() {
    final themeString = _prefs.getString(_themeKey);
    switch (themeString) {
      case 'light':
        return ThemeMode.light;
      case 'dark':
        return ThemeMode.dark;
      case 'system':
      default:
        return ThemeMode.system;
    }
  }

  Future<void> setThemeMode(ThemeMode mode) async {
    await _prefs.setString(_themeKey, mode.name);
  }

  // --- Locale ---
  Locale? getLocale() {
    final localeString = _prefs.getString(_localeKey);
    if (localeString == null || localeString.isEmpty) {
      return null;
    }
    final parts = localeString.split('-');
    if (parts.length == 2) {
      return Locale.fromSubtags(languageCode: parts[0], scriptCode: parts[1]);
    }
    return Locale(localeString);
  }

  Future<void> setLocale(Locale? locale) async {
    if (locale == null) {
      await _prefs.remove(_localeKey);
    } else {
      await _prefs.setString(_localeKey, locale.toLanguageTag());
    }
  }

  // --- Payanar (User Profile) ---
  String getPayanarMudhalPeyar() {
    return _prefs.getString(_payanarMudhalPeyarKey) ?? '';
  }

  Future<void> setPayanarMudhalPeyar(String value) async {
    await _prefs.setString(_payanarMudhalPeyarKey, value);
  }

  String getPayanarIrudhiPeyar() {
    return _prefs.getString(_payanarIrudhiPeyarKey) ?? '';
  }

  Future<void> setPayanarIrudhiPeyar(String value) async {
    await _prefs.setString(_payanarIrudhiPeyarKey, value);
  }

  String getPayanarPirandhaThaedhi() {
    return _prefs.getString(_payanarPirandhaThaedhiKey) ?? '';
  }

  Future<void> setPayanarPirandhaThaedhi(String value) async {
    await _prefs.setString(_payanarPirandhaThaedhiKey, value);
  }

  /// Returns the user's display name for the home screen.
  /// Combines first + last name, or returns empty string if not set.
  String getPayanarKaatchiPeyar() {
    final first = getPayanarMudhalPeyar().trim();
    final last = getPayanarIrudhiPeyar().trim();
    if (first.isEmpty && last.isEmpty) return '';
    return '$first $last'.trim();
  }
}

// Provide the PreferencesService using the sharedPreferencesProvider
final preferencesServiceProvider = Provider<PreferencesService>((ref) {
  return PreferencesService(ref.watch(sharedPreferencesProvider));
});

/// Reactive provider for the user's display name on the home screen.
/// Updated manually via `ref.read(payanarKaatchiPeyarProvider.notifier).state = newName`
/// after saving in the Payanar settings page.
final payanarKaatchiPeyarProvider = StateProvider<String>((ref) {
  final prefs = ref.watch(preferencesServiceProvider);
  return prefs.getPayanarKaatchiPeyar();
});
