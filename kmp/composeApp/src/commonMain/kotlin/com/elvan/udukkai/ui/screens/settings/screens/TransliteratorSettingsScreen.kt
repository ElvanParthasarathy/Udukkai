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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.core.utils.DateGroupUtils
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
 * எல்வன் நவில் & மென்பொருள் வடிவாளர் — Elvan Navil & Software Designer Screen.
 * Unified showcase for Elvan Navil software studio and software designer Elvan Parthasarathy.
 * Grounded in native digital craftsmanship, bilingual literature, and Tamil computing.
 */
@Composable
fun TransliteratorSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val uriHandler = LocalUriHandler.current

    val currentLang = LocalAppLanguage.current
    val isTa = currentLang.startsWith("ta")
    val isTanglish = currentLang == "ta-Latn"

    fun t(taText: String, enText: String): String {
        return if (isTanglish) {
            NavilMozhimaatri.transliterate(taText)
        } else if (isTa) {
            taText
        } else {
            enText
        }
    }

    val currentYear = remember {
        DateGroupUtils.getDateComponents(System.currentTimeMillis()).year
    }

    val brandName = if (isTa && !isTanglish) "எல்வன் நவில்" else "Elvan Navil"
    val studioDesc = t("மென்பொருள் படைப்பகம்", "Software Studio")

    LazyColumn(
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

        // ── Brand Hero: Name + Designer & Studio Subtitle + Website Pill ──
        item(key = "brand_hero") {
            ElvanSettingsSection(colors = colors) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = brandName,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = studioDesc,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textSecondary.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Minimalist Pill Button for Studio Website
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
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
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

        // ── Studio Introduction & Grounding Narrative ──
        item(key = "studio_narrative") {
            ElvanSettingsSection(colors = colors) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = t(
                            taText = "தனித்துவமான சொந்த மென்பொருள்கள், எண்மப் படைப்புகள் மற்றும் தமிழ்க் கணினித் தொழில்நுட்பங்களுக்கான தனியாள் மென்பொருள் படைப்பக அடையாளம். நேர்த்தியான கைவினைத்திறன், இருமொழி இலக்கியம் மற்றும் நடைமுறைத் தொழில் முறைமை உருவாக்கத்தில் அர்ப்பணிப்பு கொண்டது.",
                            enText = "An independent software craft label and digital creation imprint. Dedicated to purposeful digital craftsmanship, bilingual literature, artisan business tools, and classical Tamil linguistic computing."
                        ),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // ── Software Designer & Contact Details ──
        item(key = "designer_and_contact") {
            ElvanSettingsSection(colors = colors) {
                // Software Designer / Portfolio
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Person,
                    title = K.softwareDesigner.tr(),
                    description = "Elvan Parthasarathy",
                    onClick = {
                        uriHandler.openUri("https://jaiprakashpartha.vercel.app/")
                    },
                    colors = colors
                )

                ElvanSettingsDivider(colors = colors)

                // Email
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Email,
                    title = K.email.tr(),
                    description = "elvanpartha@gmail.com",
                    onClick = {
                        uriHandler.openUri("mailto:elvanpartha@gmail.com")
                    },
                    colors = colors
                )

                ElvanSettingsDivider(colors = colors)

                // LinkedIn
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Person,
                    title = "LinkedIn",
                    description = "linkedin.com/in/elvanparthasarathy",
                    onClick = {
                        uriHandler.openUri("https://www.linkedin.com/in/elvanparthasarathy")
                    },
                    colors = colors
                )

                ElvanSettingsDivider(colors = colors)

                // GitHub
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Code,
                    title = "GitHub",
                    description = "github.com/elvanparthasarathy",
                    onClick = {
                        uriHandler.openUri("https://github.com/elvanparthasarathy")
                    },
                    colors = colors
                )

                ElvanSettingsDivider(colors = colors)

                // Location
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.LocationOn,
                    title = K.location.tr(),
                    description = K.chennaiTamilNadu.tr(),
                    onClick = {},
                    colors = colors
                )
            }
        }

        // ── Footer: Dynamic Year © All rights reserved ──
        item(key = "footer") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "© $currentYear $brandName",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.textPrimary.copy(alpha = 0.38f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
