package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import androidx.compose.material3.*
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
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*

/**
 * எல்வன் நவில் பற்றி திரை — Elvan Navil About Screen.
 * Exact 1:1 match with Neram's ElvanNavilScreen.kt.
 */
@Composable
fun TransliteratorSettingsScreen(
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

        // Clean Brand Layout: Text alone at top and website pill button
        item(key = "brand_content") {
            ElvanSectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = K.elvanNavil.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Monochrome Pill Button for Website
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.textPrimary.copy(alpha = 0.1f))
                            .clickable { uriHandler.openUri("https://elvannavil.vercel.app") }
                            .padding(horizontal = 22.dp, vertical = 11.dp)
                    ) {
                        Text(
                            text = "elvannavil.vercel.app",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textPrimary
                        )
                        Icon(
                            imageVector = MaterialSymbols.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Footer: © All rights reserved and App Version
        item(key = "footer") {
            ElvanSectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${K.appName.tr()} v1.0.0",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textPrimary.copy(alpha = 0.45f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = K.allRightsReserved.tr(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colors.textPrimary.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
