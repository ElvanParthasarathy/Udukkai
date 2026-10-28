package com.elvan.udukkai.ui.screens.onboarding.screens.steps

import com.elvan.udukkai.ui.components.ExpressiveContainedLoader
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.screens.onboarding.components.AuthButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class BackupStatus { CHECKING, FOUND, NOT_FOUND }

/**
 * Backup Check Step matching Flutter's BackupCheckStep (kaappu_sodhanai_padi.dart) 1:1.
 * Shows "Checking for backups..." for 1.5s, then either reports the backup and moves on,
 * or reports none and waits for Continue / Scan Again.
 */
@Composable
fun BackupCheckStep(
    onBackupFound: () -> Unit,
    onNoBackupFound: () -> Unit
) {
    val isDark = ThemeManager.isDark()
    val textColor = if (isDark) Color.White else Color(0xFF1A1A1A)
    val textSecondary = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF666666)

    var status by remember { mutableStateOf(BackupStatus.CHECKING) }
    var scanRun by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(scanRun) {
        status = BackupStatus.CHECKING
        // Artificial delay to let the user read the checking message
        delay(1500)
        val hasBackup = withContext(Dispatchers.Default) { getUdukkaiBackupService().hasBackup() }
        if (hasBackup && !AuthManager.skipRestore) {
            status = BackupStatus.FOUND
            delay(1200)
            onBackupFound()
        } else {
            // Do not auto-forward. Wait for user input.
            status = BackupStatus.NOT_FOUND
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (status == BackupStatus.CHECKING) {
            ExpressiveContainedLoader(modifier = Modifier.size(44.dp), containerColor = textColor.copy(alpha = 0.12f), indicatorColor = textColor)
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Icon(
                imageVector = if (status == BackupStatus.FOUND) MaterialSymbols.Rounded.CheckCircleFill else MaterialSymbols.Rounded.Info,
                contentDescription = null,
                tint = if (status == BackupStatus.FOUND) textColor else textSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "backup_status"
        ) { s ->
            Text(
                text = when (s) {
                    BackupStatus.CHECKING -> K.checkingForBackups.tr()
                    BackupStatus.FOUND -> K.backupFound.tr()
                    BackupStatus.NOT_FOUND -> K.noBackupFound.tr()
                },
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp,
                color = textColor
            )
        }

        if (status == BackupStatus.NOT_FOUND) {
            Spacer(modifier = Modifier.height(32.dp))
            AuthButton(text = K.continueText.tr(), onClick = onNoBackupFound)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { scope.launch { scanRun++ } }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Autorenew,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = K.scanAgain.tr(), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textSecondary)
            }
        }
    }
}
