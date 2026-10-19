package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.backup.getNirilBackupService
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.screens.onboarding.components.AuthButton
import com.elvan.udukkai.ui.screens.onboarding.components.AuthHeader
import kotlinx.coroutines.launch

@Composable
fun RestorePage(
    onStartFresh: () -> Unit,
    onRestoreComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val backupService = getNirilBackupService()

    val failedMsg = K.backupFailed.tr()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AuthHeader(
            title = K.oldDataAvailable.tr(),
            subtitle = K.restorePreviousDataPrompt.tr()
        )

        Spacer(modifier = Modifier.weight(1f))

        AuthButton(
            text = K.restoreDataBtn.tr(),
            loading = isLoading,
            onClick = {
                if (!isLoading) {
                    isLoading = true
                    coroutineScope.launch {
                        val success = backupService.restoreFromBackup()
                        isLoading = false
                        if (success) {
                            onRestoreComplete()
                        } else {
                            com.elvan.udukkai.ui.components.shell.ElvanSnackbar.show(failedMsg)
                        }
                    }
                }
            }
        )

        Text(
            text = "புதிதாகத் தொடங்கு",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { onStartFresh() }
                .padding(8.dp)
        )
    }
}

