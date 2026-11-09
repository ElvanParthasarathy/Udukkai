package com.elvan.udukkai.textiles.dev

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Debug only: the dev pill hides itself (menu item "Hide pill") so screenshots do not show it; restart the app to get it back. */
object DevPillState {
    var hidden by mutableStateOf(false)
}
