/// English Number to Words Converter
/// 
/// Exact 1:1 port of moolam/Payanpadu.ts English logic
class EzhuthenEnglish {
  static const List<String> _a = [
    '', 'One ', 'Two ', 'Three ', 'Four ', 'Five ', 'Six ', 'Seven ', 'Eight ', 'Nine ', 'Ten ',
    'Eleven ', 'Twelve ', 'Thirteen ', 'Fourteen ', 'Fifteen ', 'Sixteen ', 'Seventeen ', 'Eighteen ', 'Nineteen '
  ];

  static const List<String> _b = [
    '', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'
  ];

  static String _convertToWords(int n) {
    if (n < 20) return _a[n];
    return _b[n ~/ 10] + (n % 10 != 0 ? ' ${_a[n % 10]}' : '');
  }

  static String convert(int n) {
    if (n == 0) return 'Zero';
    
    String res = '';
    
    int crore = n ~/ 10000000;
    n -= crore * 10000000;
    
    int lakh = n ~/ 100000;
    n -= lakh * 100000;
    
    int thousand = n ~/ 1000;
    n -= thousand * 1000;
    
    int hundred = n ~/ 100;
    n -= hundred * 100;

    if (crore > 0) res += '${_convertToWords(crore)} Crore ';
    if (lakh > 0) res += '${_convertToWords(lakh)} Lakh ';
    if (thousand > 0) res += '${_convertToWords(thousand)} Thousand ';
    if (hundred > 0) res += '${_convertToWords(hundred)} Hundred ';
    
    if (n > 0) {
      res += (res.isNotEmpty ? 'and ' : '') + _convertToWords(n);
    }
    
    return res.trim();
  }
}
