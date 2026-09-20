import 'dart:io';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'semaippu_paniyagam.dart';

final downloadServiceProvider = Provider<DownloadService>((ref) {
  return DownloadService(ref.read(storageServiceProvider));
});

class DownloadService {
  final StorageService _storageService;
  final Dio _dio = Dio();

  DownloadService(this._storageService);

  /// Downloads a file from [url] and saves it to [folderName]/[fileName].
  /// Provides progress updates via the [onProgress] callback.
  Future<File?> downloadFile({
    required String url,
    required String folderName,
    required String fileName,
    void Function(double progress)? onProgress,
  }) async {
    try {
      final folder = await _storageService.getAppFolder(folderName);
      final savePath = '${folder.path}${Platform.pathSeparator}$fileName';

      await _dio.download(
        url,
        savePath,
        onReceiveProgress: (received, total) {
          if (total != -1 && onProgress != null) {
            onProgress(received / total);
          }
        },
      );

      return File(savePath);
    } catch (e) {
      print('Download failed: $e');
      return null;
    }
  }
}
