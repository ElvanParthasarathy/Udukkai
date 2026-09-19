package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * செயலி பற்றி திரை — About App Screen.
 * Adapted from Neram's AboutAppScreen.kt to use the CMP shell system.
 */
@Composable
fun AboutAppScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── App Header: Logo + Name + Tagline ──
        item(key = "app_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val logoBg = if (colors.isDark) colors.textPrimary.copy(alpha = 0.08f)
                else colors.textPrimary.copy(alpha = 0.06f)

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(logoBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Notes,
                        contentDescription = K.aboutApp.tr(),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "நூலாசு",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = K.elvanNavilStudioDesc.tr(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.textSecondary.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "v1.0.0",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.textSecondary.copy(alpha = 0.6f)
                )
            }
        }

        // ── Features Section ──
        item(key = "features_section") {
            ElvanSettingsSection(colors = colors) {
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Description,
                    title = "பட்டியல் / பற்றுச்சீட்டு",
                    description = "இலகுவான பட்டியல் & பற்றுச்சீட்டு உருவாக்கம்",
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.BusinessCenter,
                    title = "வாங்குனர்கள்",
                    description = "வாங்குனர் தரவுகள் நிர்வாகம்",
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Inventory2,
                    title = "பொருட்கள்",
                    description = "பொருட்கள் சேர்க்கை & நிர்வாகம்",
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Storage,
                    title = "ஒருங்கிணைந்த தரவு",
                    description = "ஒரே தரவுத்தளத்தில் முழு வணிகத் தரவு",
                    onClick = {},
                    colors = colors
                )
            }
        }
    }
}
