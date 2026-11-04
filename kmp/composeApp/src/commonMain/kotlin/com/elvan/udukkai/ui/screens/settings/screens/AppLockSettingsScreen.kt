package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * App lock settings: displays the permanent device-level banking-grade security status.
 */
@Composable
fun AppLockSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

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
                    description = K.appLockAlwaysActive.tr() + " • " + K.appLockRowHint.tr(),
                    onClick = {},
                    customTrailing = {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.CheckCircleFill,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = colors
                )
            }
        }
        item(key = "lock_help") {
            Column(Modifier.fillMaxWidth().padding(horizontal = 26.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = K.appLockHelpTitle.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = K.appLockMandatoryDesc.tr(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = 19.sp),
                    color = colors.textPrimary.copy(alpha = 0.65f)
                )
            }
        }
    }
}
