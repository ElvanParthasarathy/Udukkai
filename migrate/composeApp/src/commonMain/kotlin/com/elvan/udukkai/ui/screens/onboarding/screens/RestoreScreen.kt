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

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        AuthHeader(
            title = "காப்புப்பிரதி கிடைத்தது",
            subtitle = "உங்களது முந்தைய தரவுகள் கண்டறியப்பட்டுள்ளன"
        )

        Spacer(modifier = Modifier.weight(1f))

        AuthButton(
            text = "மீட்டெடு",
            loading = isLoading,
            onClick = {
                if (!isLoading) {
                    isLoading = true
                    coroutineScope.launch {
                        backupService.restoreFromBackup()
                        isLoading = false
                        onRestoreComplete()
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

