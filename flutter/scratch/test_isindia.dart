void main() {
  Map<String, String> _naadu = {'ta': 'அமெரிக்க ஒன்றிணைந்த நாடுகள்', 'en': 'United States'};
  
  bool getIsIndia() {
    if (_naadu.isEmpty) return true;
    final enName = (_naadu['en'] ?? _naadu['English'] ?? '').trim().toLowerCase();
    final taName = (_naadu['ta'] ?? _naadu['Tamil'] ?? '').trim();
    
    if (enName.isEmpty && taName.isEmpty) return true;
    return enName == 'india' || taName == 'இந்தியா';
  }

  print('isIndia: ${getIsIndia()}');
}
