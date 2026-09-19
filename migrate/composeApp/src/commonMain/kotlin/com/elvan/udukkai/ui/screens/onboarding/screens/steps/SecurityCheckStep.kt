package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.backup.getNirilBackupService
import kotlinx.coroutines.delay

@Composable
fun BackupCheckStep(
    onBackupFound: () -> Unit,
    onNoBackupFound: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        val backupService = getNirilBackupService()
        if (backupService.hasBackup()) {
            onBackupFound()
        } else {
            onNoBackupFound()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "காப்புப்பிரதி சரிபார்க்கிறது...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
