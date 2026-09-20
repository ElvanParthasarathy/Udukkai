import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../mozhiyaakkam/k.dart';
import '../mozhiyaakkam/mozhi_vazhanguthi.dart';

class ElvanNaatkaatti {
  static final _monthKeys = [
    K.janavari, K.fibravari, K.maarch, K.aepral,
    K.mae, K.joon, K.joolai, K.aagast,
    K.septambar, K.aktobar, K.navambar, K.disambar
  ];

  static String formatDateTime(BuildContext context, WidgetRef ref, DateTime date) {
    final monthKey = _monthKeys[date.month - 1];
    final monthStr = monthKey.tr(context, ref);

    final hour12 = date.hour == 0 ? 12 : (date.hour > 12 ? date.hour - 12 : date.hour);
    final amPmKey = date.hour < 12 ? K.murpagal : K.pirpagal;
    final amPmStr = amPmKey.tr(context, ref);
    
    final min = date.minute.toString().padLeft(2, '0');
    
    return '$monthStr ${date.day}, ${date.year} - $hour12:$min $amPmStr';
  }
}
