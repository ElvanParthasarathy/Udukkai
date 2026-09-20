import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'package:elvan_niril/src/koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';

Future<void> showElvanImagePickerSheet({
  required BuildContext context,
  required WidgetRef ref,
  required Function(String path) onImagePicked,
}) async {
  final ImagePicker picker = ImagePicker();

  return showElvanActionSheet<void>(
    context: context,
    title: K.padammoolam.tr(context, ref), // We will fallback to english if translation missing
    cancelText: K.kaividuPtn.tr(context, ref),
    confirmText: '', // No confirm button, using customContent
    onConfirm: () {}, // Handled individually
    customContent: Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _buildActionOption(
          context: context,
          icon: Icons.photo_library_outlined,
          label: K.pugaippadathThoguppu.tr(context, ref),
          onTap: () async {
            Navigator.pop(context);
            final XFile? image =
                await picker.pickImage(source: ImageSource.gallery);
            if (image != null) {
              onImagePicked(image.path);
            }
          },
        ),
        _buildActionOption(
          context: context,
          icon: Icons.camera_alt_outlined,
          label: K.pugaippadakKaruvi.tr(context, ref),
          onTap: () async {
            Navigator.pop(context);
            final XFile? image =
                await picker.pickImage(source: ImageSource.camera);
            if (image != null) {
              onImagePicked(image.path);
            }
          },
        ),
      ],
    ),
  );
}

Widget _buildActionOption({
  required BuildContext context,
  required IconData icon,
  required String label,
  required VoidCallback onTap,
}) {
  return InkWell(
    onTap: onTap,
    borderRadius: BorderRadius.circular(12),
    child: Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Row(
        children: [
          Icon(icon, size: 24, color: Theme.of(context).colorScheme.onSurface),
          const SizedBox(width: 16),
          Text(
            label,
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w500,
              color: Theme.of(context).colorScheme.onSurface,
            ),
          ),
        ],
      ),
    ),
  );
}
