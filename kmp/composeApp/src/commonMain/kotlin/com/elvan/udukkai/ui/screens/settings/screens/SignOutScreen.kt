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
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * வெளியேறும் திரை — Dedicated Sign Out Sub-page.
 * Calm, minimal layout:
 * - Shows signed-in account details.
 * - Quiet explanation.
 * - Standard monochrome sign out button (no loud red).
 */
@Composable
fun SignOutScreen(
    onBack: () -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val email = AuthManager.signedInEmail ?: ""

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── Signed-in Account Info ──
        item(key = "account_info") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Person,
                        title = email.ifEmpty { K.userAccount.tr() },
                        description = K.signedInAs.tr(),
                        onClick = {},
                        customTrailing = {},
                        colors = colors
                    )
                }
            }
        }

        // ── Sign Out Action Button (Monochrome, Restrained) ──
        item(key = "signout_btn") {
            ElvanSectionContainer {
                Button(
                    onClick = { AuthManager.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (colors.isDark) Color.White else Color.Black,
                        contentColor = if (colors.isDark) Color.Black else Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = K.signOutBtn.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        // ── Quiet Explanation Description ──
        item(key = "desc") {
            ElvanSectionContainer {
                Text(
                    text = K.signOutPageDesc.tr(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = colors.textPrimary.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}
