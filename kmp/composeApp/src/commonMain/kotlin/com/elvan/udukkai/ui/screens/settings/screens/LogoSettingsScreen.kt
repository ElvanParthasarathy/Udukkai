package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.screens.settings.LogoPlaceSection

/**
 * Dedicated Logo Settings Subpage.
 * Adjusts the invoice and receipt placement and scale of the business logo.
 */
@Composable
fun LogoSettingsScreen(
    profile: BusinessProfile,
    isFactory: Boolean,
    colors: ShellColors = rememberShellColors(),
    scrollState: LazyListState = rememberLazyListState(),
    onSave: (BusinessProfile) -> Unit
) {
    val ff = LocalAppFontFamily.current
    val wide = profile.headerLayout == "wide" && profile.wideLogo.isNotEmpty()
    val path = if (wide) profile.wideLogo else profile.logo

    Box(modifier = Modifier.fillMaxSize()) {
        com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.SubpageContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item(key = "shell_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            if (path.isEmpty()) {
                item(key = "no_logo_notice") {
                    ElvanSettingsSection(colors = colors) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = K.noLogoUploadedNotice.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 20.sp
                                ),
                                color = colors.textPrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                item(key = "logo_place_section") {
                    LogoPlaceSection(
                        profile = profile,
                        isFactory = isFactory,
                        colors = colors,
                        onSave = onSave
                    )
                }
            }
        }
    }
}
