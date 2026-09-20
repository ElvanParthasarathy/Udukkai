import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';

void showElvanDeleteConfirmModal(
    BuildContext context, WidgetRef ref, VoidCallback onDelete) {
  showElvanActionSheet(
    context: context,
    title: K.thannuruvaiMutrilumNeekkavaa.tr(context, ref),
    cancelText: K.kaividuPtn.tr(context, ref),
    confirmText: K.neekkuPtn.tr(context, ref),
    customContent: ElvanTextField(
      textAlign: TextAlign.center,
      obscureText: true,
      decoration: InputDecoration(
        hintText: K.kadavuchol.tr(context, ref),
        hintStyle: TextStyle(
          fontSize: 14.0,
          color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.4),
        ),
        filled: true,
        fillColor: WidgetStateColor.resolveWith((states) {
          if (states.contains(WidgetState.focused)) {
            return Theme.of(context)
                .colorScheme
                .onSurface
                .withValues(alpha: 0.12);
          }
          return Theme.of(context)
              .colorScheme
              .onSurface
              .withValues(alpha: 0.08);
        }),
        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        constraints: const BoxConstraints(minHeight: 45),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(100),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(100),
          borderSide: BorderSide.none,
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(100),
          borderSide: BorderSide.none,
        ),
      ),
      style: TextStyle(
        fontSize: 14,
        color: Theme.of(context).colorScheme.onSurface,
      ),
    ),
    onConfirm: onDelete,
  );
}
