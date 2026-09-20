import 'dart:io';

void main() {
  final dir = Directory('lib/src');
  final files = dir.listSync(recursive: true).whereType<File>().where((f) => f.path.endsWith('.dart'));
  
  final replacements = {
    'saemiththaTharavugal': 'chaemiththaTharavugal',
    'suttruOppu': 'chuttruOppu',
    'saetharamGiraam': 'chaetharamGiraam',
    'saetharam': 'chaetharam',
    'saemikkaadhaVaraivu': 'chaemikkaadhaVaraivu',
  };

  for (final file in files) {
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
