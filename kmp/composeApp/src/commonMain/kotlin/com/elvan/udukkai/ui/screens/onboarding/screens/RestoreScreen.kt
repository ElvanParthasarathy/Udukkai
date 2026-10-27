package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.components.shell.ElvanLoadingOverlay
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.screens.onboarding.components.AuthAnimatedElement
import com.elvan.udukkai.ui.screens.onboarding.components.AuthButton
import com.elvan.udukkai.ui.screens.onboarding.components.AuthHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Restore Page matching Flutter's RestorePage (restore_thirai.dart) 1:1.
 */
@Composable
fun RestorePage(
    onStartFresh: () -> Unit,
    onRestoreComplete: () -> Unit
) {
    val isDark = ThemeManager.isDark()
    val textColor = if (isDark) Color.White else Color(0xFF1A1A1A)
    val scope = rememberCoroutineScope()
    var restoring by remember { mutableStateOf(false) }

    val restoringText = K.restoringData.tr()
    val successText = K.restoreSuccess.tr()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthAnimatedElement(delayIndex = 0) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF0F0F0),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.HistoryFill,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        AuthHeader(
            title = K.oldDataAvailable.tr(),
            subtitle = K.restorePreviousDataPrompt.tr()
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthButton(
            text = K.restoreDataBtn.tr(),
            loading = restoring,
            onClick = {
                if (restoring) return@AuthButton
                restoring = true
                scope.launch {
                    val success = try {
                        withContext(Dispatchers.Default) { getUdukkaiBackupService().restoreFromBackup() }
                    } catch (e: Exception) {
                        false
                    }
                    if (success) {
                        ElvanSnackbar.show(successText)
                        onRestoreComplete()
                    } else {
                        ElvanSnackbar.show("Restore failed. Please try again.")
                        restoring = false
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthAnimatedElement(delayIndex = 3) {
            Text(
                text = K.startFreshBtn.tr(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF999999),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(enabled = !restoring, onClick = onStartFresh)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            )
        }
    }

    // Flutter shows a loading overlay while restoring
    if (restoring) {
        ElvanLoadingOverlay(text = restoringText)
    }
}
