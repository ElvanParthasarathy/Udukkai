import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'elvan_thiruthi_ulleedu.dart';
import 'elvan_thiruthi_pothan.dart';
import 'elvan_thiruthi_thalaippu.dart';
import 'elvan_ulleedu_vadivamaippigal.dart';

/// A shared, self-contained component for editing document numbers (Invoices, Receipts).
/// It handles its own 'editing' state, switching between a locked pill (ElvanThiruthiPothan)
/// and an editable field (ElvanThiruthiUlleedu) automatically.
class ElvanAavanaEnnKooru extends StatefulWidget {
  final String label;
  final String prefix;
  final String initialFullNumber;
  final ValueChanged<String> onFullNumberChanged;
  final VoidCallback? onDirty;

  const ElvanAavanaEnnKooru({
    super.key,
    required this.label,
    required this.prefix,
    required this.initialFullNumber,
    required this.onFullNumberChanged,
    this.onDirty,
  });

  @override
  State<ElvanAavanaEnnKooru> createState() => _ElvanAavanaEnnKooruState();
}

class _ElvanAavanaEnnKooruState extends State<ElvanAavanaEnnKooru> {
  late bool _isEditing;
  late TextEditingController _ctrl;
  late String _currentFullNumber;

  @override
  void initState() {
    super.initState();
    _currentFullNumber = widget.initialFullNumber;
    _isEditing = false;
    _ctrl = TextEditingController();
  }

  @override
  void didUpdateWidget(ElvanAavanaEnnKooru oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.initialFullNumber != widget.initialFullNumber && !_isEditing) {
      _currentFullNumber = widget.initialFullNumber;
    }
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  void _saveChanges() {
    final numPart = _ctrl.text.trim();
    if (numPart.isNotEmpty) {
      _currentFullNumber = '${widget.prefix}$numPart';
      widget.onFullNumberChanged(_currentFullNumber);
      widget.onDirty?.call();
    }
    setState(() {
      _isEditing = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        ElvanThiruthiThalaippu(label: widget.label),
        if (_isEditing)
          ElvanThiruthiUlleedu(
            controller: _ctrl,
            prefixText: widget.prefix,
            keyboardType: TextInputType.number,
            inputFormatters: ElvanVadivamaippigal.enngalMattum,
            autofocus: true,
            suffixIcon: IconButton(
              icon: Icon(Icons.check, size: 20, color: cs.primary),
              onPressed: _saveChanges,
            ),
            onChanged: (_) {
              // Trigger onDirty when typing, but wait for save to commit full number
              widget.onDirty?.call();
            },
          )
        else
          ElvanThiruthiPothan(
            onTap: null,
            padding: const EdgeInsets.only(left: 20, right: 6),
            child: Row(
              children: [
                Expanded(
                  child: Consumer(
                    builder: (context, ref, child) {
                      return Text(
                        _currentFullNumber.isNotEmpty
                            ? _currentFullNumber
                            : K.thaaniyangkiUruvaam.tr(context, ref),
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                          color: _currentFullNumber.isNotEmpty
                              ? cs.onSurface
                              : cs.onSurfaceVariant,
                        ),
                      );
                    },
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.edit_outlined),
                  iconSize: 16,
                  color: cs.onSurface,
                  style: IconButton.styleFrom(
                    padding: const EdgeInsets.all(8),
                    minimumSize: const Size(0, 0),
                  ),
                  onPressed: () {
                    setState(() {
                      _isEditing = true;
                      final currentNum = _currentFullNumber.replaceFirst(widget.prefix, '');
                      _ctrl.text = currentNum;
                    });
                  },
                ),
              ],
            ),
          ),
      ],
    );
  }
}
