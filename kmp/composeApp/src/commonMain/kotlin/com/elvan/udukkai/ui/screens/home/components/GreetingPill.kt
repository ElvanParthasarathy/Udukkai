package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.shell.ElvanCompanySelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * GreetingPill — Active Company Switcher & Greeting Header.
 * Displays "வணக்கம் ✨" and the active company's full legal business name.
 * Clean, text-led header with compact switcher dropdown chevron.
 * Tapping opens the ElvanCompanySelectionBottomSheet to easily switch between company profiles.
 */
@Composable
fun GreetingPill(
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onManageProfiles: (() -> Unit)? = null
) {
    val mode = LocalAppMode.current
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    val uiLang = LanguageManager.activeLanguageCode
    val defaultModeName = mode.displayName()

    val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)
    val allProfiles = NiruvanaTharavugalRepository.getAllProfiles(mode)

    val fullLegalBusinessName = remember(activeProfile, uiLang, defaultModeName) {
        val direct = activeProfile.niruvanathinPeyar[uiLang]?.trim()?.takeIf { it.isNotEmpty() }
        if (direct != null) return@remember direct

        // Missing translation fallback: transliterate if UI language is Latin/English
        if (uiLang.startsWith("en") || uiLang == "ta-Latn") {
            val ta = activeProfile.niruvanathinPeyar["ta"]?.trim()?.takeIf { it.isNotEmpty() }
            if (ta != null) {
                val latin = NavilMozhimaatri.transliterate(ta).trim()
                if (latin.isNotEmpty()) return@remember latin
            }
        }

        val anyLegal = activeProfile.niruvanathinPeyar.values.firstOrNull { it.isNotBlank() }?.trim()
        if (anyLegal != null) return@remember anyLegal

        if (activeProfile.kurumPeyar.isNotBlank()) return@remember activeProfile.kurumPeyar.trim()

        defaultModeName
    }

    var showCompanySheet by remember { mutableStateOf(false) }

    val pillShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding, vertical = 2.dp)
            .clip(pillShape)
            .background(colors.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ShellDefaults.ripple(colors, bounded = true),
                onClick = { showCompanySheet = true }
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Column: Greeting & Full Legal Business Name
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = K.vanakkam.tr().preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.02).sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = MaterialSymbols.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = fullLegalBusinessName.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Compact switcher dropdown button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                    contentDescription = "Switch Company",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showCompanySheet) {
        ElvanCompanySelectionBottomSheet(
            profiles = allProfiles,
            activeProfileId = activeProfile.id,
            onSelectProfile = { selectedId ->
                NiruvanaTharavugalRepository.setActiveProfile(mode, selectedId)
            },
            onManageProfiles = onManageProfiles,
            onDismissRequest = { showCompanySheet = false },
            colors = colors
        )
    }
}
