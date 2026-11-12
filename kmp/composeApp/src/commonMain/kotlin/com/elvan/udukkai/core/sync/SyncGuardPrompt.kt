package com.elvan.udukkai.core.sync

import androidx.compose.runtime.Composable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet

/** Shown when the sync guard paused a sync that would overwrite unusually many records; the user decides. */
@Composable
fun SyncGuardPrompt() {
    val sync = getFirebaseSyncManager()
    val detail = sync.guardAlertMessage.value ?: return
    ElvanActionSheet(
        title = K.syncGuardPrompt.tr() + "\n" + detail,
        cancelText = K.syncGuardStop.tr(),
        confirmText = K.continueBtn.tr(),
        onDismissRequest = { sync.dismissGuardAlert() },
        onConfirm = { sync.approveGuard() }
    )
}
