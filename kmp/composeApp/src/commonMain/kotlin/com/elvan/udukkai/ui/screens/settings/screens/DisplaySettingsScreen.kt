package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ElvanSansFontFamily
import com.elvan.udukkai.theme.FontManager
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.theme.ThemeMode
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.OneUiBlue

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

    com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
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
                            .toggleable(
                                value = isSystem,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = autoRippleColor, bounded = true),
                                role = Role.Switch
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
                                modifier = Modifier.clearAndSetSemantics { },
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

    // The top of a phone, as in Samsung's own Display page, drawn in greys with blue list icons: a frame with a thin rim, one card with two
    // rows (a dot and a bar), and the next card peeking in at the foot. The pictures look the same in light and dark mode.
    val rimColor = if (isDarkModeDesign) Color(0xFF2D2D2F) else Color(0xFFD4D4D7)
    val frameColor = if (isDarkModeDesign) Color(0xFF38383B) else Color(0xFFE2E2E5)
    val cardColor = if (isDarkModeDesign) Color(0xFF5E5E62) else Color.White
    val dotColor = OneUiBlue
    val barColor = if (isDarkModeDesign) Color(0xFF8E8E93) else Color(0xFFD3D3D3)
    val tileShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = tileShape,
            color = rimColor,
            modifier = Modifier
                .width(116.dp)
                .height(82.dp)
                .clip(tileShape)
                .clearAndSetSemantics { }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val rim = 3.dp.toPx()
                val frameRadius = CornerRadius(13.dp.toPx())
                drawPath(
                    Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = rim, top = rim, right = size.width - rim, bottom = size.height + frameRadius.x,
                                topLeftCornerRadius = frameRadius, topRightCornerRadius = frameRadius
                            )
                        )
                    },
                    frameColor
                )
                val side = 9.dp.toPx()
                val cardWidth = size.width - side * 2
                val cardTop = 19.dp.toPx()
                val cardHeight = 50.dp.toPx()
                val cardRadius = CornerRadius(10.dp.toPx())
                drawRoundRect(cardColor, Offset(side, cardTop), Size(cardWidth, cardHeight), cardRadius)
                drawRoundRect(cardColor, Offset(side, cardTop + cardHeight + 7.dp.toPx()), Size(cardWidth, cardHeight), cardRadius)
                val barLeft = side + 29.dp.toPx()
                val barSize = Size(side + cardWidth - 11.dp.toPx() - barLeft, 6.dp.toPx())
                listOf(13.dp, 37.dp).forEachIndexed { row, rowY ->
                    val y = cardTop + rowY.toPx()
                    drawCircle(dotColor.copy(alpha = if (row == 0) 0.75f else 1f), 5.5.dp.toPx(), Offset(side + 14.dp.toPx(), y))
                    drawRect(barColor, Offset(barLeft, y - barSize.height / 2f), barSize)
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
                .selectable(
                    selected = isSelected,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = optionRippleColor, bounded = true),
                    role = Role.RadioButton,
                    onClick = onClick
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            com.elvan.udukkai.ui.components.ElvanCheckMark(
                selected = isSelected,
                selectedTint = LocalShellColors.current.textPrimary,
                idleTint = Color(0xFF888888),
                size = 20.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            // The label's colour and weight blend with the tick (the bold copy lies over the regular one)
            val on by androidx.compose.animation.core.animateFloatAsState(if (isSelected) 1f else 0f, androidx.compose.animation.core.tween(180), label = "themeLabel")
            Box(contentAlignment = Alignment.CenterStart) {
                listOf(FontWeight.Medium to 1f - on, FontWeight.SemiBold to on).forEach { (weight, weightAlpha) ->
                    Text(
                        text = title,
                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = weight),
                        color = androidx.compose.ui.graphics.lerp(colors.textPrimary.copy(alpha = 0.5f), colors.textPrimary, on),
                        modifier = Modifier.graphicsLayer { alpha = weightAlpha }
                            .then(if (weight == FontWeight.SemiBold) Modifier.clearAndSetSemantics { } else Modifier)
                    )
                }
            }
        }
    }
}
