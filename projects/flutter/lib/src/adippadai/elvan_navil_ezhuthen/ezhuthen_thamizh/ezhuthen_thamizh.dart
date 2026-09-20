/// Tamil Number to Words Converter
/// 
/// Converts numeric values to Tamil words with proper Sandhi rules (grammar joining).
/// Example: 57 -> ஐம்பத்தேழு (not ஐம்பத்து ஏழு)
/// Example: 500 oblique -> ஐந்நூற்று
/// 
/// Exact 1:1 port of moolam/mozhi/tamilNumbers.ts
class EzhuthenThamizh {
  static const List<String> _ones = [
    '', 'ஒன்று', 'இரண்டு', 'மூன்று', 'நான்கு',
    'ஐந்து', 'ஆறு', 'ஏழு', 'எட்டு', 'ஒன்பது',
    'பத்து', 'பதினொன்று', 'பன்னிரண்டு', 'பதிமூன்று', 'பதினான்கு',
    'பதினைந்து', 'பதினாறு', 'பதினேழு', 'பதினெட்டு', 'பத்தொன்பது'
  ];

  static const List<String> _tensBase = [
    '', '', 'இருபது', 'முப்பது', 'நாற்பது',
    'ஐம்பது', 'அறுபது', 'எழுபது', 'எண்பது', 'தொண்ணூறு'
  ];

  static const List<String> _tensOblique = [
    '', '', 'இருபத்து', 'முப்பத்து', 'நாற்பத்து',
    'ஐம்பத்து', 'அறுபத்து', 'எழுபத்து', 'எண்பத்து', 'தொண்ணூற்று'
  ];

  static const Map<int, String> _hundredsBase = {
    1: 'நூறு',
    2: 'இருநூறு',
    3: 'முந்நூறு',
    4: 'நானூறு',
    5: 'ஐந்நூறு',
    6: 'அறுநூறு',
    7: 'எழுநூறு',
    8: 'எண்ணூறு',
    9: 'தொள்ளாயிரம்'
  };

  static const Map<int, String> _hundredsOblique = {
    1: 'நூற்று',
    2: 'இருநூற்று',
    3: 'முந்நூற்று',
    4: 'நானூற்று',
    5: 'ஐந்நூற்று',
    6: 'அறுநூற்று',
    7: 'எழுநூற்று',
    8: 'எண்ணூற்று',
    9: 'தொள்ளாயிரத்து'
  };

  static String _joinTamil(String word1, String word2) {
    if (word1.isEmpty) return word2;
    if (word2.isEmpty) return word1;
    return '$word1 $word2';
  }

  static String _convertUnder100(int n) {
    if (n < 20) return _ones[n];

    final tensDigit = n ~/ 10;
    final onesDigit = n % 10;

    if (onesDigit == 0) {
      return _tensBase[tensDigit];
    }

    return _joinTamil(_tensOblique[tensDigit], _ones[onesDigit]);
  }

  static String _convertUnder1000(int n) {
    if (n < 100) return _convertUnder100(n);

    final hundredsDigit = n ~/ 100;
    final remainder = n % 100;

    if (remainder == 0) {
      return _hundredsBase[hundredsDigit] ?? '';
    }

    return _joinTamil(_hundredsOblique[hundredsDigit] ?? '', _convertUnder100(remainder));
  }

  static String _convertUnder100000(int n) {
    if (n < 1000) return _convertUnder1000(n);

    final thousands = n ~/ 1000;
    final remainder = n % 1000;

    String thousandsStr = _convertUnder100(thousands);
    String suffix = (remainder == 0) ? 'ஆயிரம்' : 'ஆயிரத்து';

    String thousandPart = _joinTamil(thousandsStr, suffix);

    if (remainder == 0) return thousandPart;

    return '$thousandPart ${_convertUnder1000(remainder)}';
  }

  static String _convertUnderCrore(int n) {
    if (n < 100000) return _convertUnder100000(n);

    final lakhs = n ~/ 100000;
    final remainder = n % 100000;

    String lakhPart = '';

    if (lakhs == 1) {
      lakhPart = (remainder == 0) ? 'ஒரு லட்சம்' : 'ஒரு லட்சத்து';
    } else {
      String lakhsStr = _convertUnder100(lakhs);
      String suffix = (remainder == 0) ? 'லட்சம்' : 'லட்சத்து';
      lakhPart = _joinTamil(lakhsStr, suffix);
    }

    if (remainder == 0) return lakhPart;

    return '$lakhPart ${_convertUnder100000(remainder)}';
  }

  static String convert(int n) {
    if (n == 0) return 'சுழியம்';
    if (n < 10000000) return _convertUnderCrore(n);

    final crores = n ~/ 10000000;
    final remainder = n % 10000000;

    String crorePart = '';
    
    if (crores == 1) {
      crorePart = (remainder == 0) ? 'ஒரு கோடி' : 'ஒரு கோடியே';
    } else {
      String croresStr = convert(crores); 
      String suffix = (remainder == 0) ? 'கோடி' : 'கோடியே';
      crorePart = _joinTamil(croresStr, suffix);
    }

    if (remainder == 0) return crorePart;

    return '$crorePart ${_convertUnderCrore(remainder)}';
  }

  static String numberToWordsTamil(double num, [String suffix = 'ரூபாய்']) {
    if (num == 0) return 'சுழியம்';
    
    final intPart = num.truncate().abs();
    final words = convert(intPart);
    
    if (suffix.isEmpty) return words;
    return '$words $suffix';
  }
}
