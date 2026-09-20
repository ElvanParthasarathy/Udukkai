// ─────────────────────────────────────────────────────────────────────────────
// CRASHLYTICS STUB — Delegates to local ElvanPizhaipadhivu for now
// ─────────────────────────────────────────────────────────────────────────────
// When Firebase is configured, this will forward errors to both:
// 1. Local file logger (ElvanPizhaipadhivu)
// 2. Firebase Crashlytics (remote)

import '../panigal/elvan_pizhaipadhivu.dart';
// import 'package:firebase_crashlytics/firebase_crashlytics.dart';

/// Report a non-fatal error.
/// Currently logs locally. Will also send to Crashlytics when Firebase is live.
Future<void> vizhuppuPathivu(dynamic error, StackTrace stack, {String? reason}) async {
  // Local logging (always active)
  ElvanPizhaipadhivu.logError(error.toString(), stackTrace: stack);

  // Remote logging (enable when Firebase is configured)
  // await FirebaseCrashlytics.instance.recordError(error, stack, reason: reason);
}
