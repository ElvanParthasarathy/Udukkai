package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.AppFont
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.theme.FontManager
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.NavilSansFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.theme.ThemeMode
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Display Settings Screen matching Neram's `DisplaySettingsScreen.kt` 1:1 pixel-perfect.
 * Features realistic Light & Dark mockup preview cards with Material 3 Expressive ripple,
 * horizontal pill capsule option rows, and Auto-Theme switch with full row ripple.
 */
@Composable
fun DisplaySettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val currentMode = ThemeManager.currentThemeMode
    val ff = LocalAppFontFamily.current

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        // Top spacer driven by One UI collapsible header
        item(key = "spacer_top") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        item(key = "theme_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    // Light & Dark theme options row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Light Mode Option
                        ThemeOptionBox(
                            title = K.lightMode.tr(),
                            isSelected = currentMode == ThemeMode.LIGHT,
                            isDarkModeDesign = false,
                            onClick = { ThemeManager.setThemeMode(ThemeMode.LIGHT) },
                            colors = colors
                        )

                        // Dark Mode Option
                        ThemeOptionBox(
                            title = K.darkMode.tr(),
                            isSelected = currentMode == ThemeMode.DARK,
                            isDarkModeDesign = true,
                            onClick = { ThemeManager.setThemeMode(ThemeMode.DARK) },
                            colors = colors
                        )
                    }

                    ElvanSettingsDivider(colors = colors)

                    // Auto Mode Switch Row
                    val isSystem = currentMode == ThemeMode.SYSTEM
                    val autoRippleColor = if (colors.isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.08f)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = autoRippleColor, bounded = true)
                            ) {
                                if (isSystem) {
                                    ThemeManager.setThemeMode(if (colors.isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                                } else {
                                    ThemeManager.setThemeMode(ThemeMode.SYSTEM)
                                }
                            },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = K.systemAuto.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 20.sp
                                ),
                                color = colors.textPrimary
                            )
                            ElvanSettingsSwitch(
                                checked = isSystem,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        ThemeManager.setThemeMode(ThemeMode.SYSTEM)
                                    } else {
                                        ThemeManager.setThemeMode(if (colors.isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                                    }
                                },
                                colors = colors
                            )
                        }
                    }
                }
            }
        }

        item(key = "font_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(
                    title = K.fontFamily.tr(),
                    colors = colors
                ) {
                    val currentFont = FontManager.currentFont
                    val fontChangedMsg = K.fontUpdatedSuccessfully.tr()

                    AppFont.entries.forEachIndexed { index, fontOption ->
                        val fontOptionFamily = when (fontOption) {
                            AppFont.NAVIL_SANS -> NavilSansFontFamily
                            AppFont.ELVAN_SANS -> ElvanSansFontFamily
                        }

                        ElvanRadioSettingsRow(
                            title = fontOption.displayName,
                            value = fontOption,
                            groupValue = currentFont,
                            fontFamily = fontOptionFamily,
                            onSelected = { selectedFont ->
                                if (currentFont != selectedFont) {
                                    FontManager.setFont(selectedFont)
                                    ElvanSnackbar.show("$fontChangedMsg: ${selectedFont.displayName}")
                                }
                            },
                            colors = colors
                        )

                        if (index < AppFont.entries.size - 1) {
                            ElvanSettingsDivider(colors = colors)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionBox(
    title: String,
    isSelected: Boolean,
    isDarkModeDesign: Boolean,
    onClick: () -> Unit,
    colors: ShellColors
) {
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val boxBgColor = if (isDarkModeDesign) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val borderColor = if (isSelected) {
        if (isDarkModeDesign) Color(0xFF888888) else Color(0xFF555555)
    } else {
        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    }

    val bar1Color = if (isDarkModeDesign) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f)
    val bar2Color = if (isDarkModeDesign) Color(0xFF444444) else Color(0xCFCFCFCF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        // Preview Box (110dp x 85dp, 24dp rounded corners) with Material 3 Expressive Ripple
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            color = boxBgColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .width(110.dp)
                .height(85.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bar 1 (32dp x 8dp)
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bar1Color)
                    )
                    // Bar 2 (Full width x 6dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bar2Color)
                    )
                    // Bar 3 (48dp x 6dp)
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bar2Color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom label & radio button with ripple
        val optionRippleColor = if (isDark) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.08f)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = optionRippleColor, bounded = true),
                    onClick = onClick
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (isSelected) MaterialSymbols.Rounded.CheckCircleFill else MaterialSymbols.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) {
                    LocalShellColors.current.textPrimary
                } else {
                    Color(0xFF888888)
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = if (isSelected) colors.textPrimary else colors.textPrimary.copy(alpha = 0.5f)
            )
        }
    }
}
