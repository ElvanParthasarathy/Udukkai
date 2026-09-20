package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
 * மென்பொருள் வடிவாளர் பற்றி திரை — About Developer Screen.
 * Adapted from Neram's DeveloperInfoScreen.kt.
 */
@Composable
fun AboutDeveloperScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── Developer Profile Card ──
        item(key = "developer_hero") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile avatar placeholder (initials circle)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.textPrimary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EP",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Elvan Parthasarathy",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Monochrome pill button for portfolio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.textPrimary.copy(alpha = 0.1f))
                            .clickable { uriHandler.openUri("https://jaiprakashpartha.vercel.app/") }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "jaiprakashpartha.vercel.app",
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
        }

        // ── Contact Links & Social Section ──
        item(key = "contact_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Email,
                        title = K.email.tr(),
                        description = "jaiprakashpartha@gmail.com",
                        onClick = {
                            uriHandler.openUri("mailto:jaiprakashpartha@gmail.com")
                        },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Person,
                        title = "LinkedIn",
                        description = "linkedin.com/in/jaiprakashpartha",
                        onClick = {
                            uriHandler.openUri("https://www.linkedin.com/in/jaiprakashpartha")
                        },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

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

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.LocationOn,
                        title = K.location.tr(),
                        description = K.chennaiTamilNadu.tr(),
                        onClick = {},
                        colors = colors
                    )
                }
            }
        }
    }
}
