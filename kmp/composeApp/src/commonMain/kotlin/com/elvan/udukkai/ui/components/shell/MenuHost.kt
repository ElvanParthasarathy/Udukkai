package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

/**
 * A layer over the page where menus open, in the page's own window, so they can be frosted glass over it
 * (a separate popup window cannot see the page to blur it). One menu at a time.
 */
class MenuHostState {
    var content by mutableStateOf<(@Composable () -> Unit)?>(null)
    var onDismiss: () -> Unit = {}
}

val LocalMenuHost = compositionLocalOf<MenuHostState?> { null }

/** Drawn last in the page shell: a clear layer that closes the menu when tapped outside it, then the menu. */
@Composable
fun MenuHostLayer(host: MenuHostState) {
    val content = host.content ?: return
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(300f)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { host.onDismiss() }
    ) {
        content()
    }
    com.elvan.udukkai.core.platform.AppBackHandler { host.onDismiss() }
}
