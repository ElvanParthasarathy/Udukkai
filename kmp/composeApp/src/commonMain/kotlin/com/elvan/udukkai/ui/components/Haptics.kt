package com.elvan.udukkai.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** The kinds of haptic the app uses. */
enum class HapticKind { Tick, Confirm, Click, ToggleOn, ToggleOff }

/** Plays [kind] on the phone's vibrator, if the phone has one and the user has touch feedback on. Does nothing on desktop. */
internal expect fun platformVibrate(kind: HapticKind)

/**
 * Haptics as Android's guidelines place them: a light tick for each detent of a wheel, grid or shifter, a firmer pulse when a
 * choice is confirmed, a click when a menu opens, and a toggle pulse for switches. They go to the vibrator directly: some
 * phones (Samsung's One UI among them) drop the view-level haptic constants for apps, which would leave the app silent.
 */
class Haptics internal constructor() {
    fun tick() = platformVibrate(HapticKind.Tick)
    fun confirm() = platformVibrate(HapticKind.Confirm)
    fun click() = platformVibrate(HapticKind.Click)
    fun toggle(on: Boolean) = platformVibrate(if (on) HapticKind.ToggleOn else HapticKind.ToggleOff)
}

@Composable
fun rememberHaptics(): Haptics = remember { Haptics() }
