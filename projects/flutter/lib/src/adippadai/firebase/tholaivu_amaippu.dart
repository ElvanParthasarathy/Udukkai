// ─────────────────────────────────────────────────────────────────────────────
// REMOTE CONFIG STUB — Returns defaults for now
// ─────────────────────────────────────────────────────────────────────────────

// import 'package:firebase_remote_config/firebase_remote_config.dart';

/// Get a remote config value. Returns the default until Firebase is configured.
String tholaivuMathippu(String key, {String defaultValue = ''}) {
  // final remoteConfig = FirebaseRemoteConfig.instance;
  // return remoteConfig.getString(key);
  return defaultValue;
}

/// Fetch and activate remote config. Currently a no-op.
Future<void> tholaivuAmaippuPudhuppi() async {
  // final remoteConfig = FirebaseRemoteConfig.instance;
  // await remoteConfig.setConfigSettings(RemoteConfigSettings(
  //   fetchTimeout: const Duration(minutes: 1),
  //   minimumFetchInterval: const Duration(hours: 1),
  // ));
  // await remoteConfig.fetchAndActivate();
}
