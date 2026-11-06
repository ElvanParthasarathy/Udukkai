package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Immutable

/**
 * Lightweight banner card data structure for [ElvanExpandedBar] / [ElvanShell].
 * Renders inside the expanded top bar when active, replacing the large header title.
 */
@Immutable
data class ShellBanner(
    val id: String,
    val badge: String = "✦ புதுப்பிப்பு",
    val title: String = "",
    val message: String = "",
    val actionText: String = "",
    val onClick: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)
