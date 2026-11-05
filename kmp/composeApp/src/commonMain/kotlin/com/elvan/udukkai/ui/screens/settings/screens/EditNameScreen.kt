package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.AccountAvatar
import kotlinx.coroutines.launch

/**
 * பெயர் திருத்தும் திரை — Dedicated Name Edit Sub-page.
 * - Circular avatar preview at the top.
 * - Pill-shaped text field styled like the login screen (NeramField).
 * - Clear Save action with ExpressiveLoadingIndicator and Cancel button.
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
    var nameInput by remember { mutableStateOf(currentName) }
    var isSaving by remember { mutableStateOf(false) }

    val nameSavedMsg = K.nameSaved.tr()

    ElvanSubShell(
        title = K.editName.tr(),
        onBack = onBack,
        scrollState = scrollState
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "shell_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // ── Profile Circle Header ──
            item(key = "avatar_header") {
                ElvanSectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(colors.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            AccountAvatar(fallback = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Person,
                                    contentDescription = null,
                                    tint = colors.textPrimary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                            })
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (nameInput.isNotBlank()) nameInput else K.userAccount.tr(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = AuthManager.signedInEmail ?: "",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colors.textPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // ── Input Card ──
            item(key = "name_input_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = K.displayName.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = colors.textPrimary.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                            )

                            ElvanAuthTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = K.enterDisplayName.tr(),
                                colors = colors
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = onBack,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = K.cancelBtn.tr(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = {
                                        if (nameInput.isNotBlank() && !isSaving) {
                                            isSaving = true
                                            scope.launch {
                                                val res = AuthManager.updateDisplayName(nameInput.trim())
                                                isSaving = false
                                                if (res.isSuccess) {
                                                    ElvanSnackbar.show(nameSavedMsg)
                                                    onBack()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (colors.isDark) Color.White else Color.Black,
                                        contentColor = if (colors.isDark) Color.Black else Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp),
                                    enabled = nameInput.isNotBlank() && !isSaving
                                ) {
                                    if (isSaving) {
                                        ExpressiveLoadingIndicator(
                                            color = if (colors.isDark) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = K.saveName.tr(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
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
    }
}
