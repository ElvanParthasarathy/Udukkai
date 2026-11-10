package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.components.shell.ElvanLoadingOverlay
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Offers to bring back an earlier backup, laid out like the Welcome page: the tile and the words in the middle, the buttons at the foot. */
@Composable
fun RestorePage(
    onStartFresh: () -> Unit,
    onRestoreComplete: () -> Unit
) {
    val dark = ThemeManager.isDark()
    val scope = rememberCoroutineScope()
    var restoring by remember { mutableStateOf(false) }

    val restoringText = K.restoringData.tr()
    val successText = K.restoreSuccess.tr()

    Box(Modifier.fillMaxSize()) {
        AuthBackdrop()
        NeramIntroPage(
            horizontalPadding = 16.dp,
            header = {
                NeramElement(delayIndex = 0) {
                    Box(
                        modifier = Modifier
                            .size(if (LocalAuthLandscape.current) 72.dp else 120.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(if (dark) Color.White else Color(0xFF111111)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.HistoryFill,
                            contentDescription = null,
                            tint = if (dark) Color.Black else Color.White,
                            modifier = Modifier.size(if (LocalAuthLandscape.current) 36.dp else 60.dp)
                        )
                    }
                }

                Spacer(Modifier.height(if (LocalAuthLandscape.current) 12.dp else 24.dp))

                NeramElement(delayIndex = 1) {
                    NeramHeader(title = K.oldDataAvailable.tr(), subtitle = K.restorePreviousDataPrompt.tr())
                }

            },
            bottom = {
                NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NeramButton(
                            text = K.restoreDataBtn.tr(),
                            loading = restoring,
                            onClick = {
                                if (restoring) return@NeramButton
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

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = K.startFreshBtn.tr(),
                            style = TextStyle(fontFamily = ElvanSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = NeramColors.textSecondary(),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(enabled = !restoring, role = Role.Button, onClick = onStartFresh)
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        )

                        Spacer(Modifier.height(if (LocalAuthLandscape.current) 0.dp else 32.dp))
                    }
                }
            }
        )
    }

    // Flutter shows a loading overlay while restoring
    if (restoring) {
        ElvanLoadingOverlay(text = restoringText)
    }
}
