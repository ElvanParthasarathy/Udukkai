package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.shell.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * பெயர் திருத்தும் திரை — Edit Display Name sub-page.
 * Clean single-field card to update the user's display name.
 */
@Composable
fun EditNameScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    val currentName = AuthManager.signedInDisplayName ?: ""
    var name by remember { mutableStateOf(currentName) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val nameSavedMsg = K.nameSaved.tr()
    val errorMsg = K.error.tr()
    val canSave = name.trim().isNotEmpty() && name.trim() != currentName && !isProcessing

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── Name Field ──
        item(key = "name_card") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        ElvanSettingsTextField(
                            label = K.displayName.tr(),
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = null
                            },
                            placeholder = K.enterDisplayName.tr(),
                            colors = colors
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage!!,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                isProcessing = true
                                errorMessage = null
                                scope.launch {
                                    val res = AuthManager.updateDisplayName(name.trim())
                                    isProcessing = false
                                    if (res.isSuccess) {
                                        ElvanSnackbar.show(nameSavedMsg)
                                        delay(300)
                                        onBack()
                                    } else {
                                        errorMessage = errorMsg
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (colors.isDark) Color.White else Color.Black,
                                contentColor = if (colors.isDark) Color.Black else Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            enabled = canSave
                        ) {
                            if (isProcessing) {
                                ExpressiveLoadingIndicator(
                                    color = if (colors.isDark) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = K.saveName.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
