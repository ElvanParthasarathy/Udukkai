import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:elvan_niril/src/adippadai/vazhikaattal/niril_nav.dart';
import 'package:flutter_colorpicker/flutter_colorpicker.dart' as fcp;
import 'package:cyclop/cyclop.dart';
import '../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

class ElvanUruvakkunarNiraKaatti extends StatelessWidget {
  const ElvanUruvakkunarNiraKaatti({super.key});

  static void show(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => const ElvanUruvakkunarNiraKaatti(),
    );
  }

  void _copyColor(BuildContext context, String name, Color color) {
    final hex = '#${color.toARGB32().toRadixString(16).padLeft(8, '0').toUpperCase()}';
    Clipboard.setData(ClipboardData(text: hex));
    ElvanSnackbar.show(context, 'Copied $name: $hex');
  }

  Widget _buildColorTile(BuildContext context, String name, Color color) {
    return ListTile(
      leading: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: color,
          shape: BoxShape.circle,
          border: Border.all(color: Colors.grey.withValues(alpha: 0.5)),
        ),
      ),
      title: Text(name),
      subtitle: Text('#${color.toARGB32().toRadixString(16).padLeft(8, '0').toUpperCase()}'),
      onTap: () => _copyColor(context, name, color),
    );
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final colors = {
      'Primary': cs.primary,
      'On Primary': cs.onPrimary,
      'Primary Container': cs.primaryContainer,
      'Secondary': cs.secondary,
      'On Secondary': cs.onSecondary,
      'Surface': cs.surface,
      'On Surface': cs.onSurface,
      'On Surface (alpha 0.08)': cs.onSurface.withValues(alpha: 0.08),
      'On Surface (alpha 0.12)': cs.onSurface.withValues(alpha: 0.12),
      'Surface Container Highest': cs.surfaceContainerHighest,
      'Surface Container': cs.surfaceContainer,
      'Surface Bright': cs.surfaceBright,
      'Error': cs.error,
      'Outline': cs.outline,
      'Outline Variant': cs.outlineVariant,
    };

    return DraggableScrollableSheet(
      initialChildSize: 0.7,
      minChildSize: 0.4,
      maxChildSize: 0.9,
      builder: (context, scrollController) {
        return Container(
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF1A1A1A) : Colors.white,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
          ),
          child: Column(
            children: [
              const SizedBox(height: 12),
              Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Colors.grey.withValues(alpha: 0.5),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              const Padding(
                padding: EdgeInsets.all(16.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'Theme Color Analyzer',
                      style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                    ),
                    _InteractiveColorPickerBtn(),
                  ],
                ),
              ),
              Expanded(
                child: ListView.builder(
                  controller: scrollController,
                  itemCount: colors.length,
                  itemBuilder: (context, index) {
                    final key = colors.keys.elementAt(index);
                    return _buildColorTile(context, key, colors[key]!);
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _InteractiveColorPickerBtn extends StatefulWidget {
  const _InteractiveColorPickerBtn();

  @override
  State<_InteractiveColorPickerBtn> createState() => _InteractiveColorPickerBtnState();
}

class _InteractiveColorPickerBtnState extends State<_InteractiveColorPickerBtn> {
  Color _selectedColor = Colors.blue;

  void _showColorPicker() {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Pick a color'),
          content: SingleChildScrollView(
            child: fcp.ColorPicker(
              pickerColor: _selectedColor,
              onColorChanged: (color) {
                setState(() => _selectedColor = color);
              },
            ),
          ),
          actions: [
            TextButton(
              child: const Text('Copy Hex'),
              onPressed: () {
                final hex = '#${_selectedColor.toARGB32().toRadixString(16).padLeft(8, '0').toUpperCase()}';
                Clipboard.setData(ClipboardData(text: hex));
                ElvanSnackbar.show(context, 'Copied: $hex');
                Navigator.of(context).pop();
              },
            ),
            TextButton(
              child: const Text('Close'),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        );
      },
    );
  }

  void _startEyedropper() async {
    final rootContext = globalRootNavigatorKey.currentContext!;
    Navigator.of(context).pop(); // Close the bottom sheet first so they can pick from the UI behind it
    await Future.delayed(const Duration(milliseconds: 300)); // wait for animation
    EyeDrop.of(rootContext).capture(rootContext, (color) {
      final hex = '#${color.toARGB32().toRadixString(16).padLeft(8, '0').toUpperCase()}';
      Clipboard.setData(ClipboardData(text: hex));
      ElvanSnackbar.show(rootContext, 'Eyedropper Picked: $hex');
    }, (color) {});
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        IconButton(
          icon: const Icon(Icons.colorize_outlined, color: Colors.blue),
          tooltip: 'Eyedropper Tool',
          onPressed: _startEyedropper,
        ),
        TextButton.icon(
          icon: const Icon(Icons.palette),
          label: const Text('Custom Picker'),
          onPressed: _showColorPicker,
        ),
      ],
    );
  }
}


