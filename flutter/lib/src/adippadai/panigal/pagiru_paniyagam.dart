import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:share_plus/share_plus.dart';

final shareServiceProvider = Provider<ShareService>((ref) {
  return ShareService();
});

class ShareService {
  /// Shares a local file using the native OS share sheet (e.g. to WhatsApp, Email).
  /// [filePath] must be an absolute path to the file.
  /// [text] is an optional message to send alongside the file.
  Future<void> shareFile(String filePath,
      {String? text, String? subject}) async {
    try {
      await Share.shareXFiles(
        [XFile(filePath)],
        text: text,
        subject: subject,
      );
    } catch (e) {
      print('Failed to share file: $e');
    }
  }

  /// Shares a simple text message.
  Future<void> shareText(String text, {String? subject}) async {
    try {
      await Share.share(text, subject: subject);
    } catch (e) {
      print('Failed to share text: $e');
    }
  }
}
