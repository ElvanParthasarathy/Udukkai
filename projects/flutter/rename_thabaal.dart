import 'dart:io';

void main() {
  final dir = Directory('lib/src');
  final files = dir.listSync(recursive: true).whereType<File>().where((f) => f.path.endsWith('.dart'));
  
  final replacements = {
    'thapaal': 'thabaal',
    'Thapaal': 'Thabaal',
  };

  for (final file in files) {
    if (file.path.endsWith('.g.dart')) continue; // skip generated files
    String content = file.readAsStringSync();
    bool changed = false;
    for (final entry in replacements.entries) {
      if (content.contains(entry.key)) {
        content = content.replaceAll(entry.key, entry.value);
        changed = true;
      }
    }
    if (changed) {
      file.writeAsStringSync(content);
      print('Updated ${file.path}');
    }
  }
}
