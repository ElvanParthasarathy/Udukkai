package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.*

/** The bill colours to pick from: hex (as the bill papers read it) and the name's string key. */
private val presets = listOf(
    "#388e3c" to K.green, "#6a1b9a" to K.violet, "#1565c0" to K.colorBlue, "#1e3a8a" to K.colorNavy,
    "#00796b" to K.colorTeal, "#c62828" to K.colorRed, "#e65100" to K.colorOrange, "#ad1457" to K.colorPink,
    "#5d4037" to K.colorBrown, "#b8860b" to K.colorGold, "#37474f" to K.colorGrey,
)

private const val APP = "app"
private const val CUSTOM = "custom"

fun parseBillColor(hex: String): Color = try {
    Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
} catch (_: Exception) {
    Color(0xFF388E3C)
}

/** "#rrggbb" for three 0..255 channels. */
private fun hexOf(r: Int, g: Int, b: Int): String {
    fun two(v: Int) = v.coerceIn(0, 255).toString(16).padStart(2, '0')
    return "#${two(r)}${two(g)}${two(b)}"
}

/**
 * The PDF colour row of the Create settings (Textiles and Factory): eleven bill colours, the app's own colour
 * ([appThemeHex]) and a custom colour made with three sliders.
 * shortcut: the sliders stop at 200 so a custom colour stays dark enough to read on the white bill; raise it for lighter colours.
 */
@Composable
fun ThemeColorSection(profile: BusinessProfile, colors: ShellColors, appThemeHex: String, onSave: (BusinessProfile) -> Unit) {
    val ff = LocalAppFontFamily.current
    val bottomSheet = LocalElvanBottomSheetController.current
    val current = profile.themeColor.ifEmpty { "#388e3c" }
    val appThemeName = K.appTheme.tr()
    val customName = K.customColor.tr()
    val themeTitle = K.pdfTheme.tr()
    val names = presets.associate { (hex, key) -> hex to key.tr() }
    fun nameOf(hex: String) = names[hex.lowercase()] ?: if (hex.equals(appThemeHex, true)) appThemeName else customName

    var editingCustom by remember { mutableStateOf(false) }
    val start = parseBillColor(current)
    var r by remember(current) { mutableStateOf((start.red * 255).toInt().coerceAtMost(200).toFloat()) }
    var g by remember(current) { mutableStateOf((start.green * 255).toInt().coerceAtMost(200).toFloat()) }
    var b by remember(current) { mutableStateOf((start.blue * 255).toInt().coerceAtMost(200).toFloat()) }
    val customHex = hexOf(r.toInt(), g.toInt(), b.toInt())

    ElvanSettingsSection(colors = colors) {
        ElvanSettingsAnimatedExpand(
            isEditing = editingCustom,
            displayContent = {
                ElvanSettingsDisplayRow(
                    title = K.pdfTheme.tr(),
                    primaryValue = "",
                    primaryWidget = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).clip(CircleShape).background(parseBillColor(current)))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = nameOf(current),
                                style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary
                            )
                        }
                    },
                    onEdit = {
                        val items = presets.map { it.first } + APP + CUSTOM
                        bottomSheet.showSelection(
                            title = themeTitle,
                            items = items,
                            currentValue = items.firstOrNull { it.equals(current, true) } ?: CUSTOM,
                            itemLabelBuilder = { when (it) { APP -> appThemeName; CUSTOM -> customName; else -> names.getValue(it) } },
                            leadingBuilder = { item ->
                                val hex = when (item) { APP -> appThemeHex; CUSTOM -> current; else -> item }
                                Box(Modifier.size(24.dp).clip(CircleShape).background(parseBillColor(hex)))
                            },
                            onSelected = { item ->
                                when (item) {
                                    CUSTOM -> editingCustom = true
                                    APP -> onSave(profile.copy(themeColor = appThemeHex))
                                    else -> onSave(profile.copy(themeColor = item))
                                }
                            }
                        )
                    },
                    colors = colors
                )
            },
            editContent = {
                ElvanSettingsEditContainer(
                    title = customName,
                    onCancel = { editingCustom = false },
                    onSave = {
                        onSave(profile.copy(themeColor = customHex))
                        editingCustom = false
                    },
                    colors = colors
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(parseBillColor(customHex)))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = customHex.uppercase(),
                            style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                            color = colors.textPrimary
                        )
                    }
                    listOf(Triple(r, { v: Float -> r = v }, Color(0xFFE53935)), Triple(g, { v: Float -> g = v }, Color(0xFF43A047)), Triple(b, { v: Float -> b = v }, Color(0xFF1E88E5)))
                        .forEach { (value, set, tint) ->
                            Slider(
                                value = value,
                                onValueChange = set,
                                valueRange = 0f..200f,
                                colors = SliderDefaults.colors(thumbColor = tint, activeTrackColor = tint)
                            )
                        }
                }
            }
        )
    }
}
