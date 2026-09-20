import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../../../adippadai/panigal/niril_backup_service.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';

import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../koorugal/ullnuzhaivu_koorugal.dart';

class VanakkamPage extends ConsumerStatefulWidget {
  final VoidCallback? onBack;
  final String billingLanguage;
  const VanakkamPage({super.key, required this.billingLanguage, this.onBack});

  @override
  ConsumerState<VanakkamPage> createState() => _VanakkamPageState();
}

class _VanakkamPageState extends ConsumerState<VanakkamPage> {
  String _gstBusinessName = '';
  String _coolieBusinessName = '';
  bool _saving = false;

  void _handleFinish() async {
    final needsSilk = ref.read(missingProfilesProvider).contains('silk');
    final needsCoolie = ref.read(missingProfilesProvider).contains('coolie');

    if ((needsSilk && _gstBusinessName.trim().isEmpty) ||
        (needsCoolie && _coolieBusinessName.trim().isEmpty)) {
      ElvanSnackbar.show(context, K.niruvanapeyaraiullidu.tr(context, ref));
      return;
    }

    setState(() {
      _saving = true;
    });

    try {
      debugPrint('STEP 1: Starting DB insert...');
      // Insert initial profiles into the database
      // Insert Silk profile if needed
      if (needsSilk) {
        debugPrint('STEP 2: Inserting silk...');
        final silkNotifier = ref.read(pattuNiruvanaTharavugalListProvider.notifier);
        await silkNotifier.createProfile(
          NiruvanaTharavugal(
            niruvanathinPeyar: {widget.billingLanguage: _gstBusinessName},
            mudhanMozhi: widget.billingLanguage,
            thunaiMozhi: widget.billingLanguage == 'en' ? 'ta' : 'en',
            iruMozhi: true,
            kurumPeyar: '',
            tholaipaesi1: '',
            tholaipaesi2: '',
            minnanjal: '',
            gstin: '',
            mugavari: {},
            oor: {},
            maavattam: {},
            maanilam: {},
            naadu: {},
            anjalKuriyeedu: '',
            vangiPeyar: {},
            kilai: {},
            vangiKanakku: '',
            ifsc: '',
            upiId: '',
          )
        );
        debugPrint('STEP 2: Silk inserted OK');
      }

      // Insert Coolie profile if needed
      if (needsCoolie) {
        debugPrint('STEP 3: Inserting coolie...');
        final coolieNotifier = ref.read(kooliNiruvanaTharavugalListProvider.notifier);
        await coolieNotifier.createProfile(
          NiruvanaTharavugal(
            niruvanathinPeyar: {widget.billingLanguage: _coolieBusinessName},
            mudhanMozhi: widget.billingLanguage,
            thunaiMozhi: widget.billingLanguage == 'en' ? 'ta' : 'en',
            iruMozhi: false,
            kurumPeyar: '',
            tholaipaesi1: '',
            tholaipaesi2: '',
            minnanjal: '',
            gstin: '',
            mugavari: {},
            oor: {},
            maavattam: {},
            maanilam: {},
            naadu: {},
            anjalKuriyeedu: '',
            vangiPeyar: {},
            kilai: {},
            vangiKanakku: '',
            ifsc: '',
            upiId: '',
          )
        );
        debugPrint('STEP 3: Coolie inserted OK');
      }

      debugPrint('STEP 4: Creating backup...');
      // Trigger an immediate backup so the initial setup is saved safely
      final backupService = ref.read(backupServiceProvider);
      await backupService.createBackup();
      debugPrint('STEP 4: Backup done');

      // Force the profiles stream to re-fetch from DB
      // (NativeDatabase.createInBackground uses a separate isolate — stream
      // notifications travel via SendPort and are asynchronous)
      ref.invalidate(profilesStreamProvider);
      ref.invalidate(pattuNiruvanaTharavugalListProvider);
      ref.invalidate(kooliNiruvanaTharavugalListProvider);
      
      // Wait until the router can see the inserted profiles
      for (int i = 0; i < 20; i++) {
        await Future.delayed(const Duration(milliseconds: 50));
        if (!mounted) return;
        if (ref.read(isSetupCompleteProvider)) break;
      }

      debugPrint('STEP 5: isSetupComplete = ${ref.read(isSetupCompleteProvider)}');

      if (!mounted) return;

      // Trigger router re-evaluation
      ref.read(appModeProvider.notifier).setMode(null);
      debugPrint('STEP 6: Done!');
    } catch (e, stack) {
      debugPrint('ERROR in _handleFinish: $e');
      debugPrint('STACK: $stack');
      if (!mounted) return;
      setState(() {
        _saving = false;
      });
      ElvanSnackbar.show(context, 'Error: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    final needsSilk = ref.watch(missingProfilesProvider).contains('silk');
    final needsCoolie = ref.watch(missingProfilesProvider).contains('coolie');

    return KeyedSubtree(
      key: const ValueKey('businessName'),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (widget.onBack != null) AuthBackButton(onPressed: widget.onBack!),
          AuthHeader(
            title: K.tharavugalaiUlliduga.tr(context, ref),
            subtitle: '',
          ),
          const SizedBox(height: 32),
          if (needsSilk)
            AuthInput(
              label: K.niruvanathinPeyar.tr(context, ref).isNotEmpty
                  ? K.niruvanathinPeyar.tr(context, ref)
                  : 'GST Business Name',
              placeholder: K.peyaraiUlliduga.tr(context, ref),
              helperText: K.gstpattiyalukku.tr(context, ref),
              value: _gstBusinessName,
              onChange: (val) => setState(() => _gstBusinessName = val),
            ),
          if (needsCoolie)
            AuthInput(
              label: K.kooliNiruvanaPeyar.tr(context, ref),
              placeholder: K.peyaraiUlliduga.tr(context, ref),
              helperText:
                  K.koolipattiyalukku.tr(context, ref),
              value: _coolieBusinessName,
              onChange: (val) => setState(() => _coolieBusinessName = val),
            ),
          const SizedBox(height: 32),
          AuthButton(
            text: K.thodaravum.tr(context, ref),
            loading: _saving,
            disabled: (needsSilk && _gstBusinessName.trim().isEmpty) ||
                (needsCoolie && _coolieBusinessName.trim().isEmpty),
            onPressed: _handleFinish,
          ),
        ],
      ),
    );
  }
}
