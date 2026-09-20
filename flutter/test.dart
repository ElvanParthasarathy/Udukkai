import 'dart:convert';
void main() {
  Map<String, String> myMap = {'ta': 'தமிழ்'};
  Map<String, dynamic> profileJsonConverted = {'niruvanathinPeyar': myMap};
  print(jsonEncode(profileJsonConverted));
}
