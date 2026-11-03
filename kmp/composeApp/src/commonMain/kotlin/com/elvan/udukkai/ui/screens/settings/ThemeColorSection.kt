package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.*

/** The few main bill colours: hex (as the bill papers read it) and the name's string key. Every other colour is under More colours. */
private val mainColors = listOf(
    "#388e3c" to K.green, "#6a1b9a" to K.violet, "#1565c0" to K.colorBlue, "#c62828" to K.colorRed, "#ad1457" to K.colorPink,
)

private const val MORE = "more"
private const val RECENT_KEY = "bill_color_recent"

fun parseBillColor(hex: String): Color = try {
    Color(hex.removePrefix("#").toLong(16) or 0xFF000000L)
} catch (_: Exception) {
    Color(0xFF388E3C)
}

/**
 * The PDF colour row of the Create settings (Textiles and Factory): five main colours and More colours, which opens the
 * picker ([BillColorPickerDialog]). The last five colours picked there are kept for next time.
 */
@Composable
fun ThemeColorSection(profile: BusinessProfile, colors: ShellColors, onSave: (BusinessProfile) -> Unit) {
    val ff = LocalAppFontFamily.current
    val bottomSheet = LocalElvanBottomSheetController.current
    val prefs = remember { getPreferencesHelper() }
    val current = profile.themeColor.ifEmpty { "#388e3c" }
    val moreName = K.moreColors.tr()
    val themeTitle = K.pdfTheme.tr()
    val names = mainColors.associate { (hex, key) -> hex to key.tr() }
    fun nameOf(hex: String) = names[hex.lowercase()] ?: hex.uppercase()

    var picking by remember { mutableStateOf(false) }
    var recent by remember { mutableStateOf(prefs.getString(RECENT_KEY)?.split(",")?.filter { it.length == 7 } ?: emptyList()) }

    ElvanSettingsSection(colors = colors) {
        ElvanSettingsDisplayRow(
            title = themeTitle,
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
                val items = mainColors.map { it.first } + MORE
                bottomSheet.showSelection(
                    title = themeTitle,
                    items = items,
                    currentValue = items.firstOrNull { it.equals(current, true) } ?: MORE,
                    itemLabelBuilder = { if (it == MORE) moreName else names.getValue(it) },
                    leadingBuilder = { item ->
                        // More colours shows the colour in use when it is not one of the main ones
                        Box(Modifier.size(24.dp).clip(CircleShape).background(parseBillColor(if (item == MORE) current else item)))
                    },
                    onSelected = { item -> if (item == MORE) picking = true else onSave(profile.copy(themeColor = item)) }
                )
            },
            colors = colors
        )
    }

    if (picking) {
        BillColorPickerDialog(
            initial = parseBillColor(current),
            colors = colors,
            recent = recent,
            onDismiss = { picking = false },
            onDone = { hex ->
                recent = (listOf(hex) + recent).distinct().take(5)
                prefs.setString(RECENT_KEY, recent.joinToString(","))
                onSave(profile.copy(themeColor = hex))
                picking = false
            }
        )
    }
}
