package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Elvan Navil: the name Udukkai is made under, shown plainly (the way an app page just names its maker). The name, the person
 * who designed and developed it, and how to reach them. Settings → About → Elvan Navil, and Settings → About → Designer (the same page).
 */
@Composable
fun TransliteratorSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val uriHandler = LocalUriHandler.current

    val lang = LocalAppLanguage.current
    val brandName = if (lang.startsWith("ta") && lang != "ta-Latn") "எல்வன் நவில்" else "Elvan Navil"

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

        // ── The brand: its name, and what it is in one calm line ──
        item(key = "brand_hero") {
            ElvanSettingsSection(colors = colors) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = brandName,
                        style = TextStyle(fontFamily = ff, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.textPrimary.copy(alpha = 0.08f))
                            .clickable { uriHandler.openUri("https://elvannavil.vercel.app") }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "elvannavil.vercel.app",
                            style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Icon(
                            imageVector = MaterialSymbols.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // ── Credit: the person who designed and developed it, by name in the app's language ──
        item(key = "credits") {
            ElvanSettingsSection(title = K.credits.tr(), colors = colors) {
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Person,
                    title = K.designedBuiltBy.tr(),
                    description = K.makerName.tr(),
                    onClick = { uriHandler.openUri("https://jaiprakashpartha.vercel.app/") },
                    colors = colors
                )
            }
        }

        // ── How to reach the maker ──
        item(key = "contact") {
            ElvanSettingsSection(title = K.contactSection.tr(), colors = colors) {
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Email,
                    title = K.email.tr(),
                    description = "elvanpartha@gmail.com",
                    onClick = { uriHandler.openUri("mailto:elvanpartha@gmail.com") },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Person,
                    title = "LinkedIn",
                    description = "linkedin.com/in/elvanparthasarathy",
                    onClick = { uriHandler.openUri("https://www.linkedin.com/in/elvanparthasarathy") },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Code,
                    title = "GitHub",
                    description = "github.com/elvanparthasarathy",
                    onClick = { uriHandler.openUri("https://github.com/elvanparthasarathy") },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.LocationOn,
                    title = K.location.tr(),
                    description = K.chennaiTamilNadu.tr(),
                    onClick = {},
                    customTrailing = {},
                    colors = colors
                )
            }
        }
    }
}
