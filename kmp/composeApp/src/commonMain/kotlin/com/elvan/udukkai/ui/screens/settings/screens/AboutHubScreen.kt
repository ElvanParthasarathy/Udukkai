package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsRoute

/** About the app, who made it, and the legal pages: the second level of the settings (Settings → About → a page). */
@Composable
fun AboutHubScreen(
    versionText: String,
    onNavigate: (SettingsRoute) -> Unit,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") { Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current)) }

        // ── App Info, Developer & Studio (Joined in One Card) ──
        item(key = "about_app_and_creators") {
            ElvanSettingsSection(colors = colors) {
                // About App & Version
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Info,
                    title = K.aboutApp.tr(),
                    description = versionText,
                    onClick = { onNavigate(SettingsRoute.AboutApp) },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                // Studio & Designer — Elvan Navil
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.BusinessCenter,
                    title = K.elvanNavil.tr(),
                    description = K.elvanNavilStudioDesc.tr(),
                    onClick = { onNavigate(SettingsRoute.ElvanNavil) },
                    colors = colors
                )
            }
        }

        // ── Legal ──
        item(key = "about_legal") {
            ElvanSettingsSection(colors = colors) {
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Lock,
                    title = K.privacyPolicy.tr(),
                    description = K.privacyPolicyHint.tr(),
                    onClick = { onNavigate(SettingsRoute.Privacy) },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Info,
                    title = K.termsOfUse.tr(),
                    description = K.termsOfUseHint.tr(),
                    onClick = { onNavigate(SettingsRoute.Terms) },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Code,
                    title = K.openSourceLicences.tr(),
                    description = K.openSourceLicencesHint.tr(),
                    onClick = { onNavigate(SettingsRoute.Licences) },
                    colors = colors
                )
            }
        }
    }
}
