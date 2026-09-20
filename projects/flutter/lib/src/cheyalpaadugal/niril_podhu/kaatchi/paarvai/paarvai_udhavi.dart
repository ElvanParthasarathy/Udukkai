import 'dart:io';
import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:elvan_niril/src/cheyalpaadugal/amaippugal/tharavu/niruvana_tharavugal.dart';

/// Helper class for invoice and receipt preview functionality.
class PaarvaiUdhavi {
  /// Converts local file paths in the profile to Base64 strings for robust rendering in WebView.
  /// 
  /// WebView struggles to resolve local file paths (especially SVGs) accurately due to MIME type inference issues.
  /// Sending Base64 Data URIs directly bypasses all file access and MIME type complexities.
  static Future<NiruvanaTharavugal> convertProfileImagesToBase64(NiruvanaTharavugal profile) async {
    String newOavuru = profile.oavuru;
    if (newOavuru.isNotEmpty) {
      newOavuru = await _convertImageToBase64(newOavuru);
    }
    
    String newAgalaOavuru = profile.agalaOavuru;
    if (newAgalaOavuru.isNotEmpty) {
      newAgalaOavuru = await _convertImageToBase64(newAgalaOavuru);
    }
    
    String newKaiyoppam = profile.kaiyoppam;
    if (newKaiyoppam.isNotEmpty) {
      newKaiyoppam = await _convertImageToBase64(newKaiyoppam);
    }
    
    return profile.copyWith(
      oavuru: newOavuru,
      agalaOavuru: newAgalaOavuru,
      kaiyoppam: newKaiyoppam,
    );
  }

  static Future<String> _convertImageToBase64(String path) async {
    if (path.isEmpty || path.startsWith('data:')) return path; // Already base64 or empty
    try {
      final file = File(path);
      if (await file.exists()) {
        final bytes = await file.readAsBytes();
        final base64String = base64Encode(bytes);
        final ext = path.split('.').last.toLowerCase();
        String mimeType = 'image/png';
        
        if (ext == 'svg') {
          mimeType = 'image/svg+xml';
        } else if (ext == 'jpg' || ext == 'jpeg') {
          mimeType = 'image/jpeg';
        }
        
        return 'data:$mimeType;base64,$base64String';
      }
    } catch (e) {
      debugPrint('Failed to convert image to base64: $e');
    }
    return path;
  }
}
