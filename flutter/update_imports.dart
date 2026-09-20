import 'dart:io';

void main() {
  final dir = Directory('lib/src');
  final files = dir.listSync(recursive: true).whereType<File>().where((f) => f.path.endsWith('.dart'));

  for (final file in files) {
    String content = file.readAsStringSync();
    bool changed = false;

    // Replace class names
    if (content.contains('SilkAchuMozhigal')) {
      content = content.replaceAll('SilkAchuMozhigal', 'IruMozhi');
      changed = true;
    }
    if (content.contains('KooliAchuMozhigal')) {
      content = content.replaceAll('KooliAchuMozhigal', 'OruMozhi');
      changed = true;
    }

    // Replace old constants file import
    if (content.contains('achu_mozhigal.dart')) {
      // Find the exact line and remove it
      final lines = content.split('\n');
      final newLines = lines.where((line) => !line.contains('achu_mozhigal.dart')).toList();
      content = newLines.join('\n');
      changed = true;
    }

    // Add necessary imports if providers or classes are used
    final needsIruMozhi = content.contains('IruMozhi.');
    final needsOruMozhi = content.contains('OruMozhi.');
    final needsIruMozhiProvider = content.contains('bilingualProvider') || content.contains('silkMudhanmaiMozhiProvider') || content.contains('silkIrandaamMozhiProvider');
    final needsOruMozhiProvider = content.contains('kooliAchuMozhiProvider');
    final needsFacade = content.contains('primaryLanguageProvider') || content.contains('secondaryLanguageProvider');

    if (needsIruMozhi && !content.contains('iru_mozhi.dart')) {
      content = "import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi.dart';\n" + content;
      changed = true;
    }
    if (needsOruMozhi && !content.contains('oru_mozhi.dart')) {
      content = "import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi.dart';\n" + content;
      changed = true;
    }
    if (needsIruMozhiProvider && !content.contains('iru_mozhi_vazhanguthigal.dart')) {
      content = "import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';\n" + content;
      changed = true;
    }
    if (needsOruMozhiProvider && !content.contains('oru_mozhi_vazhanguthigal.dart')) {
      content = "import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';\n" + content;
      changed = true;
    }
    if (needsFacade && !content.contains('achu_mozhi_facade.dart')) {
      content = "import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';\n" + content;
      changed = true;
    }

    if (changed) {
      file.writeAsStringSync(content);
      print("Updated ${file.path}");
    }
  }
}
