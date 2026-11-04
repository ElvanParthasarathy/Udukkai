package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/** The main bill colours: hex code and localized string key. */
private val mainColors = listOf(
    "#388e3c" to K.green,
    "#6a1b9a" to K.violet,
    "#1565c0" to K.colorBlue,
    "#c62828" to K.colorRed,
    "#ad1457" to K.colorPink,
)

private const val MORE = "more"
private const val RECENT_KEY = "bill_color_recent"

fun parseBillColor(hex: String): Color = try {
    Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
} catch (_: Exception) {
    Color(0xFF388E3C)
}

/**
 * PDF Theme Colour Row.
 * Uses an explicit Save & Cancel container to prevent direct unintended colour changes.
 */
@Composable
fun ThemeColorSection(profile: BusinessProfile, colors: ShellColors, onSave: (BusinessProfile) -> Unit) {
    val ff = LocalAppFontFamily.current
    val prefs = remember { getPreferencesHelper() }
    val current = profile.themeColor.ifEmpty { "#388e3c" }
    val moreName = K.moreColors.tr()
    val themeTitle = K.pdfTheme.tr()
    val saveSuccessMsg = K.profileSaved.tr()
    val names = mainColors.associate { (hex, key) -> hex to key.tr() }
    fun nameOf(hex: String) = names[hex.lowercase()] ?: hex.uppercase()

    var isEditing by remember { mutableStateOf(false) }
    var tempColor by remember(profile.themeColor) { mutableStateOf(current) }
    var pickingCustom by remember { mutableStateOf(false) }
    var recent by remember { mutableStateOf(prefs.getString(RECENT_KEY)?.split(",")?.filter { it.length == 7 } ?: emptyList()) }

    ElvanSettingsSection(colors = colors) {
        ElvanSettingsAnimatedExpand(
            isEditing = isEditing,
            displayContent = {
                ElvanSimpleSettingsRow(
                    title = themeTitle,
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(parseBillColor(current))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = nameOf(current),
                                style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = CircleShape,
                                color = colors.iconBg,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ShellDefaults.ripple(colors, bounded = true),
                                        onClick = {
                                            tempColor = current
                                            isEditing = true
                                        }
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Edit,
                                        contentDescription = K.edit.tr(),
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = colors
                )
            },
            editContent = {
                ElvanSettingsEditContainer(
                    title = themeTitle,
                    onCancel = {
                        isEditing = false
                        tempColor = current
                    },
                    onSave = {
                        onSave(profile.copy(themeColor = tempColor))
                        isEditing = false
                        ElvanSnackbar.show(saveSuccessMsg)
                    },
                    colors = colors
                ) {
                    val items = mainColors.map { it.first } + MORE
                    ElvanSettingsDropdown(
                        label = themeTitle,
                        value = tempColor,
                        items = items,
                        itemLabelBuilder = { if (it == MORE) moreName else names.getValue(it) },
                        leadingBuilder = { item ->
                            val fill = if (item == MORE) {
                                Modifier.background(Brush.sweepGradient((0..6).map { Color.hsv(it * 60f, 0.85f, 0.95f) }))
                            } else {
                                Modifier.background(parseBillColor(item))
                            }
                            Box(Modifier.size(20.dp).clip(CircleShape).then(fill))
                        },
                        onChanged = { item ->
                            if (item == MORE) {
                                pickingCustom = true
                            } else {
                                tempColor = item
                            }
                        },
                        colors = colors
                    )
                }
            }
        )
    }

    if (pickingCustom) {
        BillColorPickerDialog(
            initial = parseBillColor(tempColor),
            colors = colors,
            recent = recent,
            onDismiss = { pickingCustom = false },
            onDone = { hex ->
                recent = (listOf(hex) + recent).distinct().take(5)
                prefs.setString(RECENT_KEY, recent.joinToString(","))
                tempColor = hex
                pickingCustom = false
            }
        )
    }
}
