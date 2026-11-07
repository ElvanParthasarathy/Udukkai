package com.elvan.udukkai.ui.screens.editor

import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * True once the form's values differ from what they were when the form had settled after opening. Forms fill in some
 * defaults by themselves in the first moments (country, company, units), so comparing with the stored record would
 * ask "discard changes?" for a form nobody touched. [state] must list every value the user can change.
 */
@Composable
fun rememberSettledChange(state: List<Any?>): Boolean {
    val latest by rememberUpdatedState(state)
    var settled by remember { mutableStateOf<List<Any?>?>(null) }
    LaunchedEffect(Unit) {
        delay(400)
        settled = latest
    }
    return settled?.let { state != it } ?: false
}
