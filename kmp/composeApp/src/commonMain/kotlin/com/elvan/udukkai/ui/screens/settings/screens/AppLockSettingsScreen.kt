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
 * App lock settings using the device's system lock (Fingerprint, Face, or Phone Screen Lock PIN/Pattern).
 */
@Composable
fun AppLockSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    var isEnabled by remember { mutableStateOf(AppLock.isSet()) }
    val lockTitle = K.appLock.tr()

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
                    title = K.appLock.tr(),
                    description = (if (isEnabled) K.pinOn else K.pinOff).tr() + "\n" + K.appLockRowHint.tr(),
                    onClick = {
                        if (isEnabled) {
                            // Verify identity before disabling
                            authenticateFingerprint(lockTitle, "") { ok ->
                                if (ok) {
                                    AppLock.setEnabled(false)
                                    isEnabled = false
                                }
                            }
                        } else {
                            // Verify system lock works before enabling
                            authenticateFingerprint(lockTitle, "") { ok ->
                                if (ok) {
                                    AppLock.setEnabled(true)
                                    isEnabled = true
                                }
                            }
                        }
                    },
                    colors = colors
                )
            }
        }
        item(key = "lock_help") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = K.appLockHelpTitle.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, lineHeight = 20.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = K.appLockHelpDesc.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = 19.sp),
                    color = colors.textPrimary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
