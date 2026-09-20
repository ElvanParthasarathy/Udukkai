import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class ElvanMaeladukkuThaedal extends ConsumerWidget {
  final TextEditingController controller;
  final ValueChanged<String> onChanged;

  const ElvanMaeladukkuThaedal({
    super.key,
    required this.controller,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Padding(
      padding: const EdgeInsets.only(left: 16, right: 16, top: 0, bottom: 8),
      child: CupertinoSearchTextField(
        controller: controller,
        onChanged: onChanged,
        placeholder: K.thaeduga.tr(context, ref),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        prefixIcon: const Padding(
          padding: EdgeInsets.only(top: 4, bottom: 4, left: 8, right: 4),
          child: Icon(
            CupertinoIcons.search,
            color: Colors.grey,
            size: 20,
          ),
        ),
        decoration: BoxDecoration(
          color:
              Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.05),
          borderRadius: BorderRadius.circular(100),
        ),
        style: TextStyle(
          color: Theme.of(context).colorScheme.onSurface,
          fontSize: 14,
        ),
      ),
    );
  }
}
