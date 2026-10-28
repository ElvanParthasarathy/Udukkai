package com.elvan.udukkai.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.sync.SyncStatus
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/** Who is signed in and whether the live Firebase sync is up to date; tapping the status syncs now. */
@Composable
fun CloudSyncSection(colors: ShellColors) {
    val sync = getFirebaseSyncManager()
    val status by sync.syncStatus
    val lastSync by sync.lastSyncTime

    val statusText = when (status) {
        SyncStatus.SYNCED -> K.syncStatusSynced.tr()
        SyncStatus.SYNCING -> K.syncStatusSyncing.tr()
        SyncStatus.OFFLINE -> K.syncStatusOffline.tr()
        SyncStatus.ERROR, SyncStatus.GUARD_TRIGGERED -> K.syncStatusError.tr()
        SyncStatus.IDLE -> K.syncStatusIdle.tr()
    }

    ElvanSettingsSection(title = K.cloudSync.tr(), colors = colors) {
        ElvanSettingsRow(
            icon = MaterialSymbols.Rounded.CloudUpload,
            title = K.signedInAs.tr(),
            description = AuthManager.signedInEmail ?: "-",
            onClick = {},
            colors = colors
        )
        ElvanSettingsDivider(colors = colors)
        ElvanSettingsRow(
            icon = MaterialSymbols.Rounded.Sync,
            title = statusText,
            description = if (lastSync > 0L) "${K.lastSyncedAt.tr()}: ${DateUtils.formatDateTime(lastSync)}" else null,
            onClick = { sync.triggerManualSync() },
            colors = colors
        )
    }
}
