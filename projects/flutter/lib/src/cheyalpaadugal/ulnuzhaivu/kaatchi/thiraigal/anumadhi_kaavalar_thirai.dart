import 'dart:io';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:permission_handler/permission_handler.dart';

import '../koorugal/ullnuzhaivu_koorugal.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

/// Provider to track storage permission status.
final storagePermissionProvider = FutureProvider<bool>((ref) async {
  if (!Platform.isAndroid) return true; // Only required on Android

  if (await Permission.manageExternalStorage.isGranted) return true;
  if (await Permission.storage.isGranted) return true;

  return false;
});

class PermissionGuardScreen extends ConsumerStatefulWidget {
  final Widget child;

  const PermissionGuardScreen({super.key, required this.child});

  @override
  ConsumerState<PermissionGuardScreen> createState() =>
      _PermissionGuardScreenState();
}

class _PermissionGuardScreenState extends ConsumerState<PermissionGuardScreen>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  // Re-check permissions when returning from settings
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      ref.invalidate(storagePermissionProvider);
    }
  }

  Future<void> _requestPermission() async {
    if (!Platform.isAndroid) return;

    // Request MANAGE_EXTERNAL_STORAGE on Android 11+ for robust file access in Documents
    // Fallback to standard storage on older Android versions
    PermissionStatus status;
    
    // Check if we are on Android 11+
    // A simple heuristic: try manageExternalStorage first
    final manageStatus = await Permission.manageExternalStorage.request();
    
    if (manageStatus.isGranted) {
       status = manageStatus;
    } else {
       status = await Permission.storage.request();
    }

    if (status.isPermanentlyDenied) {
      // If permanently denied, open app settings
      await openAppSettings();
    }

    ref.invalidate(storagePermissionProvider);
  }

  @override
  Widget build(BuildContext context) {
    // If not Android, just return the app content
    if (!Platform.isAndroid) return widget.child;

    final permissionAsync = ref.watch(storagePermissionProvider);

    return permissionAsync.when(
      loading: () => Scaffold(
        body: AuthLayout(
          hideLogo: true,
          showBranding: true,
          child: Center(
            child: Text(
              K.niril.tr(context, ref),
              style: TextStyle(
                fontSize: 36,
                fontWeight: FontWeight.w800,
                letterSpacing: -1.0,
                color: Theme.of(context).colorScheme.onSurface,
              ),
            ),
          ),
        ),
      ),
      error: (_, __) => widget.child,
      data: (isGranted) {
        if (isGranted) {
          // Permission is granted, show the actual app!
          return widget.child;
        }

        // Permission denied, show the blocking screen
        final theme = Theme.of(context);
        final isDark = theme.brightness == Brightness.dark;
        final textColor = isDark ? Colors.white : const Color(0xFF1A1A1A);

        // Check system language
        final locale = Localizations.localeOf(context).languageCode;
        final isTamil = locale == 'ta';

        final title = isTamil ? 'சேமிப்பக அனுமதி தேவை' : 'Storage Permission Required';
        final subtitle = isTamil 
            ? 'காப்புப்பிரதி மற்றும் பில்களை சேமிக்க இந்த அனுமதி அவசியம்.'
            : 'Storage access is required to save backups and bills.';
        final buttonText = isTamil ? 'அனுமதி வழங்கு' : 'Allow Access';

        return Scaffold(
          backgroundColor: theme.scaffoldBackgroundColor,
          body: AuthLayout(
            hideLogo: true,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                AuthAnimatedElement(
                  delayIndex: 0,
                  child: Container(
                    width: 96,
                    height: 96,
                    decoration: BoxDecoration(
                      color: isDark
                          ? Colors.white.withValues(alpha: 0.08)
                          : const Color(0xFFF0F0F0),
                      borderRadius: BorderRadius.circular(28),
                    ),
                    child: Center(
                      child: Icon(
                        CupertinoIcons.folder_badge_plus,
                        size: 48,
                        color: textColor,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 40),
                AuthHeader(
                  title: title,
                  subtitle: subtitle,
                ),
                const SizedBox(height: 40),
                AuthButton(
                  text: buttonText,
                  onPressed: _requestPermission,
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
