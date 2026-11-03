package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.GoogleSignInResult
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.core.security.authenticateFingerprint
import com.elvan.udukkai.core.security.fingerprintAvailable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class PinMode { VERIFY, CREATE }

private val bouncy = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

/** One dot of the PIN: it springs up when a digit fills it. */
@Composable
private fun PinDot(filled: Boolean, colors: ShellColors) {
    val scale by animateFloatAsState(if (filled) 1.15f else 0.8f, bouncy, label = "dot")
    val color by animateColorAsState(if (filled) colors.textPrimary else colors.textPrimary.copy(alpha = 0.18f), label = "dotColor")
    Box(Modifier.size(16.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(CircleShape).background(color))
}

/**
 * A key of the number pad in the Material 3 Expressive style: a big rounded square that springs into a circle (and
 * shrinks a little) while it is pressed, instead of a plain ripple.
 */
@Composable
private fun PadKey(label: String, colors: ShellColors, onClick: () -> Unit) {
    val ff = LocalAppFontFamily.current
    var pressed by remember { mutableStateOf(false) }
    val corner by animateDpAsState(if (pressed) 38.dp else 26.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium), label = "corner")
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, bouncy, label = "keyScale")
    val fill by animateColorAsState(if (pressed) colors.textPrimary.copy(alpha = 0.22f) else colors.iconBg, label = "keyFill")
    Box(
        Modifier.size(width = 82.dp, height = 68.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(corner))
            .background(fill)
            // The digit counts when the finger lands, not when it lifts, so a quick tap is never lost
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    pressed = true
                    onClick()
                    waitForUpOrCancellation()
                    pressed = false
                }
            }
            .semantics(mergeDescendants = true) { role = Role.Button; onClick { onClick(); true } },
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = TextStyle(fontFamily = ff, fontSize = 28.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
    }
}

/**
 * Dots and a number pad (Material 3 Expressive): the PIN is typed here, never with the keyboard, so nothing remembers or
 * shows it. [errorKey] goes up with every wrong entry so the dots shake each time, even when the message is the same.
 */
@Composable
fun PinPad(prompt: String, length: Int, error: String?, errorKey: Int, onComplete: (String) -> Unit, colors: ShellColors, modifier: Modifier = Modifier) {
    val ff = LocalAppFontFamily.current
    var digits by remember(length) { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var clearJob by remember { mutableStateOf<Job?>(null) }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(errorKey) {
        if (errorKey > 0) {
            repeat(3) { shake.animateTo(12f, tween(45)); shake.animateTo(-12f, tween(45)) }
            shake.animateTo(0f, tween(45))
        }
    }

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            prompt,
            style = TextStyle(fontFamily = ff, fontSize = 17.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
            color = colors.textPrimary,
            modifier = Modifier.heightIn(min = 28.dp)
        )
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.offset { IntOffset(shake.value.dp.roundToPx(), 0) },
            horizontalArrangement = Arrangement.spacedBy(if (length > 6) 12.dp else 16.dp)
        ) {
            repeat(length) { i -> PinDot(i < digits.length, colors) }
        }
        // Always two lines high, so a message appearing never moves the pad
        Box(Modifier.height(44.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                error.orEmpty(),
                style = TextStyle(fontFamily = ff, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp),
                color = Color(0xFFE53935)
            )
        }

        fun press(d: String) {
            // A digit typed while the last full entry is still showing starts the next entry at once
            if (digits.length >= length) { clearJob?.cancel(); digits = "" }
            digits += d
            if (digits.length == length) {
                onComplete(digits)
                // The dots empty after every full entry (wrong, or the first of two): the next try starts clean
                clearJob = scope.launch { delay(220); digits = "" }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("", "0", "⌫")).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) Spacer(Modifier.size(width = 82.dp, height = 68.dp))
                        else PadKey(key, colors) { if (key == "⌫") digits = digits.dropLast(1) else press(key) }
                    }
                }
            }
        }
    }
}

/**
 * Choosing a new PIN: 4 or 6 digits, typed twice. Everything keeps its place from the first entry to the second: the
 * length choice stays (dimmed) so the pad never jumps. [onCreated] gets the PIN once it is stored.
 */
@Composable
fun PinCreator(colors: ShellColors, onCreated: (String) -> Unit) {
    var length by remember { mutableStateOf(AppLock.length().takeIf { it in AppLock.LENGTHS } ?: 6) }
    var first by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var errorKey by remember { mutableStateOf(0) }
    val mismatch = K.pinMismatch.tr()
    val four = K.pinDigits4.tr()
    val six = K.pinDigits6.tr()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.graphicsLayer { alpha = if (first == null) 1f else 0.35f }) {
            ElvanPillShifter(
                items = listOf(PillShifterItem(four), PillShifterItem(six)),
                selectedIndex = AppLock.LENGTHS.indexOf(length).coerceAtLeast(0),
                onIndexSelected = { length = AppLock.LENGTHS[it] },
                colors = colors,
                isFullWidth = true
            )
            if (first != null) {
                Box(Modifier.matchParentSize().pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } })
            }
        }
        PinPad(
            prompt = if (first == null) K.pinChoose.tr() else K.pinAgain.tr(),
            length = length,
            error = error,
            errorKey = errorKey,
            colors = colors,
            onComplete = { pin ->
                when {
                    first == null -> { first = pin; error = null }
                    first == pin -> { AppLock.set(pin); onCreated(pin) }
                    else -> { first = null; error = mismatch; errorKey++ }
                }
            }
        )
    }
}

/**
 * The PIN in an action sheet. [PinMode.VERIFY] asks for the PIN and checks it (with the lock-out of [AppLock]);
 * [PinMode.CREATE] chooses a new one. [onResult] gets the PIN, or null when the sheet is closed.
 */
@Composable
fun PinSheet(mode: PinMode, colors: ShellColors, onResult: (String?) -> Unit) {
    var error by remember { mutableStateOf<String?>(null) }
    var errorKey by remember { mutableStateOf(0) }
    val wrong = K.pinWrong.tr()
    val lockedText = K.pinLocked.tr()

    ElvanActionSheet(
        title = K.appPin.tr(),
        cancelText = K.cancelBtn.tr(),
        confirmText = "",
        onDismissRequest = { onResult(null) },
        onConfirm = {},
        customContent = {
            if (mode == PinMode.CREATE) {
                PinCreator(colors) { onResult(it) }
            } else {
                PinPad(
                    prompt = K.pinEnter.tr(),
                    length = AppLock.length(),
                    error = error,
                    errorKey = errorKey,
                    colors = colors,
                    onComplete = { pin ->
                        val waiting = AppLock.secondsLocked()
                        if (waiting > 0) { error = "$lockedText $waiting"; errorKey++ }
                        else if (AppLock.verify(pin)) onResult(pin)
                        else { error = AppLock.secondsLocked().let { if (it > 0) "$lockedText $it" else wrong }; errorKey++ }
                    }
                )
            }
        },
        colors = colors
    )
}

/**
 * Returns a function that runs an action only after the app PIN (or the fingerprint, when it is on) was passed. Used for
 * the actions that can lose or give away data. The PIN sheet is drawn where this is called.
 */
@Composable
fun rememberPinGuard(colors: ShellColors): (() -> Unit) -> Unit {
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val title = K.appLock.tr()
    val usePin = K.usePin.tr()
    pending?.let { action ->
        PinSheet(PinMode.VERIFY, colors) { pin ->
            pending = null
            if (pin != null) action()
        }
    }
    return { action ->
        when {
            !AppLock.isSet() -> action()
            AppLock.fingerprintOn() && fingerprintAvailable() ->
                authenticateFingerprint(title, usePin) { ok -> if (ok) action() else pending = action }
            else -> pending = action
        }
    }
}

/**
 * The app lock: the app is drawn under it (so nothing being edited is lost), but until a PIN exists only the page that
 * makes one shows (it cannot be skipped), and afterwards only the PIN page shows while the PIN has not been entered:
 * at opening, and when the app comes back after a minute away. Right after the PIN is made, a fingerprint is offered.
 */
@Composable
fun AppLockGate(colors: ShellColors, content: @Composable () -> Unit) {
    val isSet = AppLock.isSet()
    var offerFingerprint by remember { mutableStateOf(false) }
    val title = K.appLock.tr()
    val usePin = K.usePin.tr()
    Box(Modifier.fillMaxSize()) {
        content()
        if (!isSet) CreatePinPage(colors) { if (fingerprintAvailable()) offerFingerprint = true }
        else if (!AppLock.unlockedThisRun) LockPage(colors)
    }
    if (offerFingerprint) {
        ElvanActionSheet(
            title = K.fingerprintOfferTitle.tr(),
            cancelText = K.fingerprintOfferNo.tr(),
            confirmText = K.fingerprintOfferYes.tr(),
            onDismissRequest = { offerFingerprint = false },
            // Switched on only after a fingerprint was read now, so it is known to work
            onConfirm = { authenticateFingerprint(title, usePin) { ok -> if (ok) AppLock.setFingerprint(true) } },
            customContent = {
                Text(K.fingerprintOfferHint.tr(), style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 14.sp, lineHeight = 20.sp), color = colors.textPrimary.copy(alpha = 0.7f))
            },
            colors = colors
        )
    }
}

@Composable
private fun LockLayer(colors: ShellColors, content: @Composable ColumnScope.() -> Unit) {
    // The back gesture and touches must not reach the screens under the lock
    AppBackHandler { }
    Box(
        Modifier.fillMaxSize().background(colors.background)
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } }
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

/** The lock badge: a rounded square (the Expressive shape) with the lock in it. */
@Composable
private fun LockBadge(colors: ShellColors) {
    Box(Modifier.size(76.dp).clip(RoundedCornerShape(28.dp)).background(colors.iconBg), contentAlignment = Alignment.Center) {
        Icon(MaterialSymbols.Rounded.Lock, null, tint = colors.textPrimary, modifier = Modifier.size(32.dp))
    }
}

/** The first thing a new install shows: a PIN has to be chosen before the app can be used. */
@Composable
private fun CreatePinPage(colors: ShellColors, onCreated: () -> Unit) {
    val ff = LocalAppFontFamily.current
    LockLayer(colors) {
        LockBadge(colors)
        Text(
            K.setUpPinTitle.tr(),
            style = TextStyle(fontFamily = ff, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 32.sp),
            color = colors.textPrimary
        )
        Text(
            K.setUpPinHint.tr(),
            style = TextStyle(fontFamily = ff, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center),
            color = colors.textPrimary.copy(alpha = 0.65f)
        )
        Spacer(Modifier.height(4.dp))
        PinCreator(colors) { onCreated() }
    }
}

@Composable
private fun LockPage(colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var errorKey by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var settingNew by remember { mutableStateOf(false) }
    val wrong = K.pinWrong.tr()
    val lockedText = K.pinLocked.tr()
    val title = K.appLock.tr()
    val usePin = K.usePin.tr()
    val recoverFailed = K.recoverFailed.tr()
    val fingerprintReady = AppLock.fingerprintOn() && fingerprintAvailable()
    val accountBefore = remember { AuthManager.signedInEmail }

    fun askFingerprint() = authenticateFingerprint(title, usePin) { ok -> if (ok) AppLock.unlockedThisRun = true }
    LaunchedEffect(Unit) { if (fingerprintReady) askFingerprint() }

    LockLayer(colors) {
        LockBadge(colors)
        PinPad(
            prompt = K.pinEnter.tr(),
            length = AppLock.length(),
            error = error,
            errorKey = errorKey,
            colors = colors,
            onComplete = { pin ->
                val waiting = AppLock.secondsLocked()
                if (waiting > 0) { error = "$lockedText $waiting"; errorKey++ }
                else if (!AppLock.verify(pin)) { error = AppLock.secondsLocked().let { if (it > 0) "$lockedText $it" else wrong }; errorKey++ }
            }
        )
        if (fingerprintReady) {
            Text(
                K.fingerprintUnlock.tr(),
                style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(colors.iconBg).clickable { askFingerprint() }.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
        // A forgotten PIN: the Google account that is signed in on this phone allows choosing a new one
        Text(
            K.forgotPin.tr(),
            style = TextStyle(fontFamily = ff, fontSize = 14.sp),
            color = colors.textPrimary.copy(alpha = 0.7f),
            modifier = Modifier.clip(CircleShape).clickable(enabled = !busy) {
                busy = true
                scope.launch {
                    val result = try { AuthManager.signInWithGoogle() } catch (_: Throwable) { GoogleSignInResult.Failed }
                    busy = false
                    val sameAccount = result is GoogleSignInResult.Success && (accountBefore == null || result.email.equals(accountBefore, ignoreCase = true))
                    if (sameAccount) settingNew = true else if (result !is GoogleSignInResult.Cancelled) ElvanSnackbar.show(recoverFailed)
                }
            }.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }

    if (settingNew) PinSheet(PinMode.CREATE, colors) { pin -> settingNew = false; if (pin != null) AppLock.unlockedThisRun = true }
    if (busy) ElvanLoadingOverlay(text = K.syncing.tr(), colors = colors)
}
