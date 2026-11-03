package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.PrintNumbers
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.*

/**
 * The size of the logo on the bill, for the small logo and the wide logo: 50% to 130% of the size the bill draws it at
 * (100% fits the way it always did). Shown only for a logo that is set. Stored in the print settings as "logoScale" and
 * "wideLogoScale"; both bills read them.
 * shortcut: the top is 130% so a bigger logo does not run into the bill number bar; raise it if the header gets room.
 */
@Composable
fun LogoSizeSection(profile: BusinessProfile, colors: ShellColors, onSave: (BusinessProfile) -> Unit) {
    val rows = listOfNotNull(
        ("logoScale" to K.logoSize).takeIf { profile.logo.isNotEmpty() },
        ("wideLogoScale" to K.wideLogoSize).takeIf { profile.wideLogo.isNotEmpty() },
    )
    if (rows.isEmpty()) return
    var editing by remember { mutableStateOf<String?>(null) }

    ElvanSettingsSection(colors = colors) {
        rows.forEachIndexed { i, (key, title) ->
            if (i > 0) ElvanSettingsDivider(colors = colors)
            val saved = PrintNumbers.get(profile.printSettingsJson, key, 1.0).toFloat()
            var temp by remember(saved) { mutableStateOf(saved) }
            ElvanSettingsAnimatedExpand(
                isEditing = editing == key,
                displayContent = {
                    ElvanSettingsDisplayRow(
                        title = title.tr(),
                        primaryValue = "${(saved * 100f + 0.5f).toInt()}%",
                        onEdit = { temp = saved; editing = key },
                        colors = colors
                    )
                },
                editContent = {
                    ElvanSettingsEditContainer(
                        title = title.tr(),
                        onCancel = { editing = null },
                        onSave = {
                            onSave(profile.copy(printSettingsJson = PrintNumbers.set(profile.printSettingsJson, key, temp.toDouble())))
                            editing = null
                        },
                        colors = colors
                    ) {
                        androidx.compose.material3.Text(
                            text = "${(temp * 100f + 0.5f).toInt()}%",
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Slider(value = temp, onValueChange = { temp = it }, valueRange = 0.5f..1.3f)
                    }
                }
            )
        }
    }
}
