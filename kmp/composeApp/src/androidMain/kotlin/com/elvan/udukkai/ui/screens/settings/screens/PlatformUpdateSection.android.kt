package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.runtime.*
import com.elvan.udukkai.BuildConfig
import com.elvan.udukkai.core.update.AppUpdate
import com.elvan.udukkai.core.update.WhatsNewSheet
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.navigation.MaterialSymbols

@Composable
actual fun PlatformUpdateSection(colors: ShellColors) {
    val isTa = LocalAppLanguage.current.startsWith("ta")
    val available = AppUpdate.available
    val checking = AppUpdate.checking
    var showWhatsNew by remember { mutableStateOf(false) }

    val statusText = when {
        available != null -> if (isTa) "புதிய பதிப்பு v${available.version} உள்ளது (தட்டுக)" else "New update v${available.version} available (Tap)"
        checking -> if (isTa) "சரிபார்க்கிறது..." else "Checking for updates..."
        else -> if (isTa) "நிகழ்காலப் பதிப்பில் உள்ளது · v${BuildConfig.VERSION_NAME}" else "App is up to date · v${BuildConfig.VERSION_NAME}"
    }

    ElvanSettingsSection(colors = colors) {
        ElvanSettingsRow(
            icon = MaterialSymbols.Rounded.Download,
            title = if (isTa) "செயலி புதுப்பிப்பு" else "Software Update",
            description = statusText,
            onClick = {
                if (available != null) {
                    showWhatsNew = true
                } else {
                    AppUpdate.checkNow()
                }
            },
            colors = colors
        )
    }

    if (showWhatsNew && available != null) {
        WhatsNewSheet(
            release = available,
            onDismiss = { showWhatsNew = false }
        )
    }
}
