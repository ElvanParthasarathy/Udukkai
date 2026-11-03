package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.elvan.udukkai.ui.screens.onboarding.screens.NeramField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PinMode { VERIFY, CREATE }

/** Dots and a number pad: the PIN is typed here, never with the keyboard, so nothing remembers or shows it. */
@Composable
fun PinPad(prompt: String, length: Int, error: String?, onComplete: (String) -> Unit, colors: ShellColors, modifier: Modifier = Modifier) {
    val ff = LocalAppFontFamily.current
    var digits by remember(length) { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(prompt, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(if (length > 6) 10.dp else 14.dp)) {
            repeat(length) { i ->
                Box(Modifier.size(14.dp).clip(CircleShape).background(if (i < digits.length) colors.textPrimary else colors.textPrimary.copy(alpha = 0.18f)))
            }
        }
        Text(error.orEmpty(), style = TextStyle(fontFamily = ff, fontSize = 13.sp), color = Color(0xFFE53935), modifier = Modifier.height(18.dp))

        fun press(d: String) {
            if (digits.length >= length) return
            digits += d
            if (digits.length == length) {
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

/** Choosing a new PIN: 6 or 8 digits, typed twice. [onCreated] gets the PIN once it is stored. */
@Composable
fun PinCreator(colors: ShellColors, onCreated: (String) -> Unit) {
    var length by remember { mutableStateOf(AppLock.length()) }
    var first by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val mismatch = K.pinMismatch.tr()
    val six = K.pinDigits6.tr()
    val eight = K.pinDigits8.tr()
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (first == null) {
            ElvanPillShifter(
                items = listOf(PillShifterItem(six), PillShifterItem(eight)),
                selectedIndex = AppLock.LENGTHS.indexOf(length),
                onIndexSelected = { length = AppLock.LENGTHS[it] },
                colors = colors,
                isFullWidth = true
            )
        }
        PinPad(
            prompt = if (first == null) K.pinChoose.tr() else K.pinAgain.tr(),
            length = length,
            error = error,
            colors = colors,
            onComplete = { pin ->
                when {
                    first == null -> { first = pin; error = null }
                    first == pin -> { AppLock.set(pin); onCreated(pin) }
                    else -> { first = null; error = mismatch }
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
                    colors = colors,
                    onComplete = { pin ->
                        val waiting = AppLock.secondsLocked()
                        if (waiting > 0) error = "$lockedText $waiting"
                        else if (AppLock.verify(pin)) onResult(pin)
                        else error = AppLock.secondsLocked().let { if (it > 0) "$lockedText $it" else wrong }
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
 * at opening, and when the app comes back after a minute away.
 */
@Composable
fun AppLockGate(colors: ShellColors, content: @Composable () -> Unit) {
    val isSet = AppLock.isSet()
    Box(Modifier.fillMaxSize()) {
        content()
        if (!isSet) CreatePinPage(colors) else if (!AppLock.unlockedThisRun) LockPage(colors)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun LockBadge(colors: ShellColors) {
    Box(Modifier.size(64.dp).clip(CircleShape).background(colors.iconBg), contentAlignment = Alignment.Center) {
        Icon(MaterialSymbols.Rounded.Lock, null, tint = colors.textPrimary, modifier = Modifier.size(28.dp))
    }
}

/** The first thing a new install shows: a PIN has to be chosen before the app can be used. */
@Composable
private fun CreatePinPage(colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    LockLayer(colors) {
        LockBadge(colors)
        Text(K.setUpPinTitle.tr(), style = TextStyle(fontFamily = ff, fontSize = 20.sp, fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
        Text(K.setUpPinHint.tr(), style = TextStyle(fontFamily = ff, fontSize = 14.sp, lineHeight = 20.sp), color = colors.textPrimary.copy(alpha = 0.65f))
        PinCreator(colors) { }
    }
}

@Composable
private fun LockPage(colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var recovering by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var settingNew by remember { mutableStateOf(false) }
    val wrong = K.pinWrong.tr()
    val lockedText = K.pinLocked.tr()
    val title = K.appLock.tr()
    val usePin = K.usePin.tr()
    val recoverFailed = K.recoverFailed.tr()
    val fingerprintReady = AppLock.fingerprintOn() && fingerprintAvailable()

    fun askFingerprint() = authenticateFingerprint(title, usePin) { ok -> if (ok) AppLock.unlockedThisRun = true }
    LaunchedEffect(Unit) { if (fingerprintReady) askFingerprint() }

    LockLayer(colors) {
        LockBadge(colors)
        PinPad(
            prompt = K.pinEnter.tr(),
            length = AppLock.length(),
            error = error,
            colors = colors,
            onComplete = { pin ->
                val waiting = AppLock.secondsLocked()
                if (waiting > 0) error = "$lockedText $waiting"
                else if (!AppLock.verify(pin)) error = AppLock.secondsLocked().let { if (it > 0) "$lockedText $it" else wrong }
            }
        )
        if (fingerprintReady) {
            Text(
                K.fingerprintUnlock.tr(),
                style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                modifier = Modifier.clip(CircleShape).clickable { askFingerprint() }.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
        Text(
            K.forgotPin.tr(),
            style = TextStyle(fontFamily = ff, fontSize = 13.sp),
            color = colors.textPrimary.copy(alpha = 0.6f),
            modifier = Modifier.clip(CircleShape).clickable { recovering = true }.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    // Forgotten PIN: the account's own email and password (needs internet) allow choosing a new one
    if (recovering) {
        ElvanActionSheet(
            title = K.recoverTitle.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.recoverConfirm.tr(),
            onDismissRequest = { recovering = false },
            onConfirm = {
                busy = true
                scope.launch {
                    val result = try { AuthManager.signInWithEmail(email.trim(), password) } catch (_: Throwable) { GoogleSignInResult.Failed }
                    busy = false
                    password = ""
                    if (result is GoogleSignInResult.Success) settingNew = true else ElvanSnackbar.show(recoverFailed)
                }
            },
            customContent = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(K.recoverHint.tr(), style = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = 18.sp), color = colors.textPrimary.copy(alpha = 0.65f))
                    NeramField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = K.emailAddress.tr(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    NeramField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = K.password.tr(),
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        keyboardActions = KeyboardActions.Default
                    )
                }
            },
            colors = colors
        )
    }
    if (settingNew) PinSheet(PinMode.CREATE, colors) { pin -> settingNew = false; if (pin != null) AppLock.unlockedThisRun = true }
    if (busy) ElvanLoadingOverlay(text = K.syncing.tr(), colors = colors)
}
