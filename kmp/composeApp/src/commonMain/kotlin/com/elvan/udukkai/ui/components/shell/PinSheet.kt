package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PinMode { VERIFY, CREATE }

/** Dots and a number pad: the PIN is typed here, never with the keyboard, so nothing remembers or shows it. */
@Composable
fun PinPad(prompt: String, error: String?, onComplete: (String) -> Unit, colors: ShellColors, modifier: Modifier = Modifier) {
    val ff = LocalAppFontFamily.current
    var digits by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(prompt, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            repeat(AppLock.LENGTH) { i ->
                Box(Modifier.size(14.dp).clip(CircleShape).background(if (i < digits.length) colors.textPrimary else colors.textPrimary.copy(alpha = 0.18f)))
            }
        }
        Text(error.orEmpty(), style = TextStyle(fontFamily = ff, fontSize = 13.sp), color = Color(0xFFE53935), modifier = Modifier.height(18.dp))

        fun press(d: String) {
            if (digits.length >= AppLock.LENGTH) return
            digits += d
            if (digits.length == AppLock.LENGTH) {
                onComplete(digits)
                // The dots empty after every full entry (wrong, or the first of two): the next try starts clean
                scope.launch { delay(200); digits = "" }
            }
        }
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "⌫")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    Box(
                        Modifier.size(64.dp).clip(CircleShape)
                            .then(if (key.isEmpty()) Modifier else Modifier.background(colors.iconBg).clickable {
                                if (key == "⌫") digits = digits.dropLast(1) else press(key)
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(key, style = TextStyle(fontFamily = ff, fontSize = 22.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

/**
 * The PIN in an action sheet. [PinMode.VERIFY] asks for the PIN and checks it (with the lock-out of [AppLock]);
 * [PinMode.CREATE] asks for a new PIN twice and stores it. [onResult] gets the PIN, or null when the sheet is closed.
 */
@Composable
fun PinSheet(mode: PinMode, colors: ShellColors, onResult: (String?) -> Unit) {
    var first by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val wrong = K.pinWrong.tr()
    val mismatch = K.pinMismatch.tr()
    val lockedText = K.pinLocked.tr()
    val prompt = when {
        mode == PinMode.VERIFY -> K.pinEnter.tr()
        first == null -> K.pinChoose.tr()
        else -> K.pinAgain.tr()
    }

    ElvanActionSheet(
        title = K.appPin.tr(),
        cancelText = K.cancelBtn.tr(),
        confirmText = "",
        onDismissRequest = { onResult(null) },
        onConfirm = {},
        customContent = {
            PinPad(
                prompt = prompt,
                error = error,
                colors = colors,
                onComplete = { pin ->
                    when {
                        mode == PinMode.VERIFY -> {
                            val waiting = AppLock.secondsLocked()
                            if (waiting > 0) error = "$lockedText $waiting"
                            else if (AppLock.verify(pin)) onResult(pin)
                            else error = AppLock.secondsLocked().let { if (it > 0) "$lockedText $it" else wrong }
                        }
                        first == null -> { first = pin; error = null }
                        first == pin -> { AppLock.set(pin); onResult(pin) }
                        else -> { first = null; error = mismatch }
                    }
                }
            )
        },
        colors = colors
    )
}

/**
 * Returns a function that runs an action only after the PIN: with no PIN set it runs the action straight away. Used for
 * the actions that can lose or give away data. The PIN sheet is drawn where this is called.
 */
@Composable
fun rememberPinGuard(colors: ShellColors): (() -> Unit) -> Unit {
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    pending?.let { action ->
        PinSheet(PinMode.VERIFY, colors) { pin ->
            pending = null
            if (pin != null) action()
        }
    }
    return { action -> if (AppLock.isSet()) pending = action else action() }
}

/** The opening page of an app with a PIN: the app shows only after the right PIN, once per run of the app. */
@Composable
fun AppLockGate(colors: ShellColors, content: @Composable () -> Unit) {
    var unlocked by remember { mutableStateOf(!AppLock.isSet() || AppLock.unlockedThisRun) }
    if (unlocked) {
        content()
        return
    }
    var error by remember { mutableStateOf<String?>(null) }
    val wrong = K.pinWrong.tr()
    val lockedText = K.pinLocked.tr()
    Box(Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.Center) {
        PinPad(
            prompt = K.pinEnter.tr(),
            error = error,
            colors = colors,
            modifier = Modifier.padding(24.dp),
            onComplete = { pin ->
                val waiting = AppLock.secondsLocked()
                if (waiting > 0) error = "$lockedText $waiting"
                else if (AppLock.verify(pin)) unlocked = true
                else error = AppLock.secondsLocked().let { if (it > 0) "$lockedText $it" else wrong }
            }
        )
    }
}
