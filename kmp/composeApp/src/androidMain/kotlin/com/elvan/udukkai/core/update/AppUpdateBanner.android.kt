package com.elvan.udukkai.core.update

import androidx.compose.runtime.*
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.ui.components.shell.ShellBanner

private var dismissedSessionUpdateId by mutableStateOf<String?>(null)

@Composable
actual fun rememberAppUpdateBanner(): ShellBanner? {
    val available = AppUpdate.available ?: return null
    if (dismissedSessionUpdateId == available.version) return null

    val isTa = LocalAppLanguage.current.startsWith("ta")
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        WhatsNewSheet(
            release = available,
            onDismiss = { showSheet = false }
        )
    }

    return ShellBanner(
        id = available.version,
        badge = if (isTa) "✦ புதுப்பிப்பு" else "✦ UPDATE",
        title = if (isTa) "புதிய பதிப்பு v${available.version} கிடைத்துள்ளது" else "New update v${available.version} available",
        message = if (isTa) "ஒரு தொடுதலில் பின்னணியில் நிறுவ தட்டவும்" else "Tap to install seamlessly in background",
        actionText = if (isTa) "புதுப்பி" else "Update",
        onClick = { showSheet = true },
        onDismiss = { dismissedSessionUpdateId = available.version }
    )
}
