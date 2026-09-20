import 'ezhuthen_thamizh/ezhuthen_thamizh.dart';
import 'ezhuthen_english/ezhuthen_english.dart';

/// Elvan Navil Ezhuthen Engine
/// 
/// Core utility to convert numeric amounts to words.
/// Returns a hot-swappable mozhiMap (Map<String, dynamic>) containing
/// both Tamil and English strings, mimicking the exact behavior of the React app.
class ElvanNavilEzhuthen {
  /// Converts a number to a Map containing 'ta' and 'en' keys.
  static Map<String, dynamic> convertToMozhiMap(double num) {
    final roundedNum = (num * 100).roundToDouble() / 100;
    final rupees = roundedNum.truncate();
    final paise = ((roundedNum - rupees) * 100).round();

    // English Logic
    String enResult = '';
    if (num == 0) {
      enResult = 'Zero Rupees Only';
    } else {
      enResult = '${EzhuthenEnglish.convert(rupees)} Rupees';
      if (paise > 0) {
        enResult += ' and ${EzhuthenEnglish.convert(paise)} Paise';
      }
      enResult += ' Only';
    }

    // Tamil Logic
    String taResult = '';
    if (num == 0) {
      taResult = 'சுழியம் ரூபாய் மட்டும்';
    } else {
      taResult = EzhuthenThamizh.numberToWordsTamil(rupees.toDouble(), 'ரூபாய்').trim();
      if (paise > 0) {
        taResult += ' மற்றும் ${EzhuthenThamizh.numberToWordsTamil(paise.toDouble(), 'காசுகள்').trim()}';
      }
      taResult += ' மட்டும்';
    }

    return {
      'en': enResult,
      'ta': taResult,
    };
  }
}
