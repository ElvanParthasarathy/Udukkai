package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * பெயர் திருத்தும் திரை — Dedicated Name Edit Sub-page matching Business Edit.
 * - Circular avatar preview at the top.
 * - In-place animated expand container (ElvanSettingsAnimatedExpand):
 *   - DisplayRow shows current name with an Edit button on the right.
 *   - Tapping Edit expands the edit box with the text field and Cancel / Save buttons.
 *   - Saving updates the name, displays the snackbar, and collapses smoothly back.
 */
@Composable
fun EditNameScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var tempDisplayName by remember { mutableStateOf(AuthManager.signedInDisplayName ?: "") }
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

            // ── Top Profile Avatar Circle ──
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
                            text = (AuthManager.signedInDisplayName ?: "").ifEmpty { K.userAccount.tr() },
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

            // ── Business Edit Style: Animated Expand Container ──
            item(key = "name_expand_card") {
                ElvanSectionContainer {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsAnimatedExpand(
                            isEditing = isEditing,
                            displayContent = {
                                ElvanSettingsDisplayRow(
                                    title = K.displayName.tr(),
                                    primaryValue = (AuthManager.signedInDisplayName ?: "").ifEmpty { K.userAccount.tr() },
                                    onEdit = {
                                        tempDisplayName = AuthManager.signedInDisplayName ?: ""
                                        isEditing = true
                                    },
                                    colors = colors
                                )
                            },
                            editContent = {
                                ElvanSettingsEditContainer(
                                    title = K.displayName.tr(),
                                    onCancel = { isEditing = false },
                                    onSave = {
                                        if (tempDisplayName.isNotBlank() && !isSaving) {
                                            isSaving = true
                                            scope.launch {
                                                val res = AuthManager.updateDisplayName(tempDisplayName.trim())
                                                isSaving = false
                                                if (res.isSuccess) {
                                                    isEditing = false
                                                    ElvanSnackbar.show(nameSavedMsg)
                                                }
                                            }
                                        }
                                    },
                                    colors = colors
                                ) {
                                    ElvanAuthTextField(
                                        value = tempDisplayName,
                                        onValueChange = { tempDisplayName = it },
                                        placeholder = K.enterDisplayName.tr(),
                                        colors = colors
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
