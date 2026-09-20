import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal'
pagudhi_path = os.path.join(base_path, 'kanakku_paadhugaappu_pagudhi.dart')

methods_to_insert = """
  void _showBackupFlow(BuildContext context, WidgetRef ref) {
    showElvanActionSheet(
      context: context,
      title: K.tharavuKaappu.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.kaappuCheiPtn.tr(context, ref),
      onConfirm: () async {
        showElvanLoadingOverlay(
            context: context, text: K.chaemikkappadugiradhu.tr(context, ref));
            
        // Wait a small amount of time for UX purposes so it doesn't flash instantly
        await Future.delayed(const Duration(milliseconds: 800));
        
        final backupService = ref.read(backupServiceProvider);
        await backupService.createBackup();

        if (context.mounted) {
          // Pop the loading dialog
          Navigator.of(context, rootNavigator: true).pop();
          ElvanSnackbar.show(context, K.tharavuchaemippuvetri.tr(context, ref));
          ref.invalidate(storageStatsProvider);
        }
      },
    );
  }

  void _showEraseDataFlow(BuildContext context, WidgetRef ref) {
    // Step 1: Email Confirmation
    showElvanActionSheet(
      context: context,
      title: K.cheyalithTharavaiAzhi.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.thodaravum.tr(context, ref),
      customContent: ElvanTextField(
        textAlign: TextAlign.center,
        decoration: InputDecoration(
          hintText: K.minnanjalaiUrudhiseiga.tr(context, ref),
          hintStyle: TextStyle(
            fontSize: 13,
            color:
                Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.4),
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
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
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
          fontSize: 13,
          color: Theme.of(context).colorScheme.onSurface,
        ),
      ),
      onConfirm: () {
        // Step 2: Password Confirmation
        showElvanActionSheet(
          context: context,
          title: K.kadavuchollaiUllidavum.tr(context, ref),
          cancelText: K.kaividuPtn.tr(context, ref),
          confirmText: K.tharavaiAzhiPtn.tr(context, ref),
          customContent: ElvanTextField(
            textAlign: TextAlign.center,
            obscureText: true,
            decoration: InputDecoration(
              hintText: K.kadavuchol.tr(context, ref),
              hintStyle: TextStyle(
                fontSize: 13,
                color: Theme.of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.4),
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
              contentPadding:
                  const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
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
              fontSize: 13,
              color: Theme.of(context).colorScheme.onSurface,
            ),
          ),
          onConfirm: () async {
            // Step 3: Show Loading Animation
            showElvanLoadingOverlay(
                context: context, text: K.azhikkiradhu.tr(context, ref));

            // Simulate erasing delay
            await Future.delayed(const Duration(seconds: 2));

            if (context.mounted) {
              // We do not manually pop dialogs here, as that triggers overlapping
              // pop animations that crash the Navigator (!_debugLocked).
              // pushAndRemoveUntil below will cleanly destroy all dialogs instantly.

              // Wipe BOTH databases by switching modes and deleting all tables
              
              // 1. Wipe Silk Database
              final pattuDb = ref.read(pattuDatabaseProvider);
              await pattuDb.delete(pattuDb.pattuNiruvanaTharavugalTable).go();
              await pattuDb.delete(pattuDb.pattuPorulTable).go();
              await pattuDb.delete(pattuDb.pattuVaangunarTable).go();
              await pattuDb.delete(pattuDb.pattuPatrucheettuTable).go();
              await pattuDb.delete(pattuDb.pattuPatrugalTable).go();
              await pattuDb.delete(pattuDb.pattuPatruPattiyalTable).go();

              // 2. Wipe Coolie Database
              final kooliDb = ref.read(kooliDatabaseProvider);
              await kooliDb.delete(kooliDb.kooliNiruvanaTharavugalTable).go();
              await kooliDb.delete(kooliDb.kooliPorulTable).go();
              await kooliDb.delete(kooliDb.kooliVaangunarTable).go();
              await kooliDb.delete(kooliDb.kooliPatrucheettuTable).go();
              await kooliDb.delete(kooliDb.kooliPatrugalTable).go();
              await kooliDb.delete(kooliDb.kooliPatruPattiyalTable).go();

              // Clear SharedPreferences
              final prefs = ref.read(sharedPreferencesProvider);
              await prefs.clear();

              if (context.mounted) {
                final successMsg = K.azhippuvetri.tr(context, ref);

                // Show snackbar first before we unmount this context
                ElvanSnackbar.show(context, successMsg);

                // Mock fresh install redirect by resetting mode and auth
                ref.read(appModeProvider.notifier).setMode(null);
                ref.read(isLoggedInProvider.notifier).setLoggedIn(false);

                // Instantly wipe in-memory settings so UI snaps back to default
                ref.invalidate(pattuNiruvanaTharavugalListProvider);
                ref.invalidate(kooliNiruvanaTharavugalListProvider);
                ref.invalidate(themeModeProvider);
                ref.invalidate(localeProvider);
                ref.invalidate(skipRestoreProvider);
                ref.invalidate(nirilNavigationProvider);

                // Pop all dialogs and screens back to the root route.
                Navigator.of(context, rootNavigator: true)
                    .popUntil((route) => route.isFirst);
              }
            }
          },
        );
      },
    );
  }

  void _showSyncFlow(BuildContext context, WidgetRef ref) {
    showElvanLoadingOverlay(
      context: context,
      text: K.orunginaikkiRadhu.tr(context, ref),
    );

    // Invalidate all data providers to force a fresh DB read
    ref.invalidate(pattiyalgalProvider);
    ref.invalidate(patrugalProvider);
    ref.invalidate(porulgalProvider);
    ref.invalidate(vaangunargalProvider);
    ref.invalidate(pattuNiruvanaTharavugalListProvider);

    // Small delay for UX so it doesn't flash instantly
    Future.delayed(const Duration(milliseconds: 800), () {
      if (context.mounted) {
        Navigator.of(context, rootNavigator: true).pop();
        ElvanSnackbar.show(
          context,
          K.orunginaikkappattadhu.tr(context, ref),
        );
      }
    });
  }
"""

with open(pagudhi_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Insert before the last `}`
end_index = content.rfind('}')
content = content[:end_index] + methods_to_insert + '\n}\n'

with open(pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed methods placement completely.")
