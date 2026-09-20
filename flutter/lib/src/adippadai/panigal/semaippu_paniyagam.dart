import 'dart:io';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';

final storageServiceProvider = Provider<StorageService>((ref) {
  return StorageService();
});

class StorageService {
  /// Gets the root directory where the app should store documents.
  /// Using ApplicationDocumentsDirectory keeps the files private to the app
  /// and automatically removes them if the app is uninstalled, requiring no permissions.
  Future<Directory> getAppDocumentsDirectory() async {
    return await getApplicationDocumentsDirectory();
  }

  /// Gets a specific folder inside the documents directory (creates it if it doesn't exist)
  Future<Directory> getAppFolder(String folderName) async {
    final rootDir = await getAppDocumentsDirectory();
    final folder =
        Directory('${rootDir.path}${Platform.pathSeparator}$folderName');

    if (!await folder.exists()) {
      await folder.create(recursive: true);
    }

    return folder;
  }

  /// Lists all files inside a specific app folder
  Future<List<FileSystemEntity>> listFilesInFolder(String folderName) async {
    final folder = await getAppFolder(folderName);
    return folder.listSync();
  }

  /// Checks if a specific file exists
  Future<bool> fileExists(String folderName, String fileName) async {
    final folder = await getAppFolder(folderName);
    final file = File('${folder.path}${Platform.pathSeparator}$fileName');
    return await file.exists();
  }

  /// Deletes a specific file
  Future<void> deleteFile(String folderName, String fileName) async {
    final folder = await getAppFolder(folderName);
    final file = File('${folder.path}${Platform.pathSeparator}$fileName');
    if (await file.exists()) {
      await file.delete();
    }
  }
}
