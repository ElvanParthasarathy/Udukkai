package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.platformSendPasswordReset
import com.elvan.udukkai.core.sync.SyncStatus
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch

/**
 * Account & Cloud Sync section shown at the top of the Security/Account settings screen.
 * Shows the signed-in account profile card, password reset, Google disconnect, and sync status.
 */
@Composable
fun CloudSyncSection(colors: ShellColors) {
    val sync = getFirebaseSyncManager()
    val status by sync.syncStatus
    val lastSync by sync.lastSyncTime
    val scope = rememberCoroutineScope()
    val ff = LocalAppFontFamily.current

    val email = AuthManager.signedInEmail
    val displayName = AuthManager.signedInDisplayName
    val isGoogle = AuthManager.isGoogleProvider

    val statusText = when (status) {
        SyncStatus.SYNCED -> K.syncStatusSynced.tr()
        SyncStatus.SYNCING -> K.syncStatusSyncing.tr()
        SyncStatus.OFFLINE -> K.syncStatusOffline.tr()
        SyncStatus.ERROR, SyncStatus.GUARD_TRIGGERED -> K.syncStatusError.tr()
        SyncStatus.IDLE -> K.syncStatusIdle.tr()
    }

    val resetSuccessMsg = K.resetMailSent.tr()
    val resetErrorMsg = K.syncStatusError.tr()

    RestoreBanner()

    // ── Account Profile Card ──
    if (email != null) {
        ElvanSettingsSection(colors = colors) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar circle — photo if available, initials fallback
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(colors.textPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    AccountAvatar(fallback = {
                        val initials = (displayName ?: email)
                            .split(" ", ".", "_", "@")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .joinToString("") { it.first().uppercaseChar().toString() }
                            .take(2)
                        Text(
                            text = initials,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textPrimary
                        )
                    })
                }

                // Name + email
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (!displayName.isNullOrBlank()) {
                        Text(
                            text = displayName,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textPrimary
                        )
                    }
                    Text(
                        text = email,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SectionSpacing))
    }

    // ── Account Management Section ──
    ElvanSettingsSection(title = K.cloudSync.tr(), colors = colors) {
        // Password Reset
        if (email != null) {
            ElvanSettingsRow(
                icon = MaterialSymbols.Rounded.Lock,
                title = K.resetPasswordTitle.tr(),
                description = K.sendPasswordReset.tr(),
                onClick = {
                    scope.launch {
                        val sent = platformSendPasswordReset(email)
                        ElvanSnackbar.show(if (sent) resetSuccessMsg else resetErrorMsg)
                    }
                },
                colors = colors
            )
            ElvanSettingsDivider(colors = colors)
        }

        // Google Account row (only shown when signed in via Google)
        if (isGoogle && email != null) {
            ElvanSettingsRow(
                icon = MaterialSymbols.Rounded.CloudUpload,
                title = K.googleAccount.tr(),
                description = email,
                onClick = {},
                colors = colors
            )
            ElvanSettingsDivider(colors = colors)
        }

        // Sync status
        ElvanSettingsRow(
            icon = MaterialSymbols.Rounded.Sync,
            title = statusText,
            description = if (lastSync > 0L) "${K.lastSyncedAt.tr()}: ${DateUtils.formatDateTime(lastSync)}" else null,
            onClick = { sync.triggerManualSync() },
            colors = colors
        )
    }
}
