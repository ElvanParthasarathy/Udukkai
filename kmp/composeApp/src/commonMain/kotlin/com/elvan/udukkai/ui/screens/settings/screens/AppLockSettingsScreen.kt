package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.security.AppLock
import com.elvan.udukkai.core.security.authenticateFingerprint
import com.elvan.udukkai.core.security.fingerprintAvailable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Everything about the app lock in one place: change the PIN (4 or 6 digits), the fingerprint, and what to do when the
 * PIN is forgotten. The PIN cannot be switched off: the bills are private.
 */
@Composable
fun AppLockSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val guard = rememberPinGuard(colors)
    var tick by remember { mutableStateOf(0) }
    var changePin by remember { mutableStateOf(false) }
    val lockTitle = K.appLock.tr()
    val usePin = K.usePin.tr()
    val pinChangedMsg = K.pinChanged.tr()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") { Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current)) }
        item(key = "lock_rows") {
            ElvanSettingsSection(colors = colors) {
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Lock,
                    title = K.pinChange.tr(),
                    description = "${if (tick >= 0) AppLock.length() else 0} ${K.pinDigitsWord.tr()}\n" + K.appPinHint.tr(),
                    onClick = { guard { changePin = true } },
                    colors = colors
                )
                if (fingerprintAvailable()) {
                    ElvanSettingsDivider(colors = colors)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Lock,
                        title = K.fingerprintUnlock.tr(),
                        description = (if (tick >= 0 && AppLock.fingerprintOn()) K.pinOn else K.pinOff).tr() + "\n" + K.fingerprintHint.tr(),
                        onClick = {
                            if (AppLock.fingerprintOn()) {
                                guard { AppLock.setFingerprint(false); tick++ }
                            } else {
                                // Turning it on needs a fingerprint now, to be sure it works
                                authenticateFingerprint(lockTitle, usePin) { ok -> if (ok) { AppLock.setFingerprint(true); tick++ } }
                            }
                        },
                        colors = colors
                    )
                }
            }
        }
        item(key = "lock_help") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(K.forgotPinHelpTitle.tr(), style = TextStyle(fontFamily = ff, fontSize = 14.sp, lineHeight = 20.sp), color = colors.textPrimary)
                Text(K.forgotPinHelp.tr(), style = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = 19.sp), color = colors.textPrimary.copy(alpha = 0.6f))
            }
        }
    }

    if (changePin) PinSheet(PinMode.CREATE, colors) { pin ->
        changePin = false
        if (pin != null) { tick++; ElvanSnackbar.show(pinChangedMsg) }
    }
}
