package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * Displays "வணக்கம் ✨" and the active company's short name with company avatar.
 * Tapping opens the ElvanCompanySelectionBottomSheet to easily switch between company profiles.
 */
@Composable
fun GreetingPill(
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onManageProfiles: (() -> Unit)? = null
) {
    val mode = LocalAppMode.current
    val modeName = mode.displayName()
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)
    val allProfiles = NiruvanaTharavugalRepository.getAllProfiles(mode)

    val isBilingual = activeProfile.iruMozhi
    val primaryBillingLang = activeProfile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryBillingLang = activeProfile.thunaiMozhi.ifEmpty { "en" }
    val uiLang = LanguageManager.activeLanguageCode

    // First line logic
    val firstLineText = remember(activeProfile, isBilingual, primaryBillingLang, modeName) {
        if (isBilingual) {
            // Short business name where available, otherwise full business name in primary billing language
            activeProfile.kurumPeyar.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar[primaryBillingLang]?.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar["ta"]?.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar["en"]?.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar.values.firstOrNull { it.isNotBlank() }
                ?: modeName
        } else {
            // Full business name in primary billing language
            activeProfile.niruvanathinPeyar[primaryBillingLang]?.takeIf { it.isNotBlank() }
                ?: activeProfile.kurumPeyar.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar["ta"]?.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar["en"]?.takeIf { it.isNotBlank() }
                ?: activeProfile.niruvanathinPeyar.values.firstOrNull { it.isNotBlank() }
                ?: modeName
        }
    }

    // Subtitle logic
    val subtitleText = remember(activeProfile, isBilingual, secondaryBillingLang, uiLang, firstLineText) {
        if (isBilingual) {
            // Subtitle: business name in secondary billing language
            val secondary = activeProfile.niruvanathinPeyar[secondaryBillingLang]?.takeIf { it.isNotBlank() }
            if (secondary != null && !secondary.equals(firstLineText, ignoreCase = true)) {
                secondary
            } else {
                // Appropriate fallback: try the other language, or legal name if short name was used
                val legalPrimary = activeProfile.niruvanathinPeyar[primaryBillingLang]?.takeIf { it.isNotBlank() }
                if (legalPrimary != null && !legalPrimary.equals(firstLineText, ignoreCase = true)) {
                    legalPrimary
                } else {
                    val otherLang = if (primaryBillingLang == "ta") "en" else "ta"
                    val other = activeProfile.niruvanathinPeyar[otherLang]?.takeIf { it.isNotBlank() }
                    if (other != null && !other.equals(firstLineText, ignoreCase = true)) {
                        other
                    } else {
                        // Transliterate if Latin representation needed
                        val transliterated = NavilMozhimaatri.transliterate(firstLineText)
                        if (transliterated.isNotBlank() && !transliterated.equals(firstLineText, ignoreCase = true)) {
                            transliterated
                        } else ""
                    }
                }
            }
        } else {
            // Subtitle: business address in configured UI language (no GSTIN)
            val street = activeProfile.mugavari[uiLang] ?: activeProfile.mugavari["ta"] ?: activeProfile.mugavari["en"] ?: ""
            val town = activeProfile.oor[uiLang] ?: activeProfile.oor["ta"] ?: activeProfile.oor["en"] ?: ""
            val district = activeProfile.maavattam[uiLang] ?: activeProfile.maavattam["ta"] ?: activeProfile.maavattam["en"] ?: ""
            val pin = activeProfile.anjalKuriyeedu.trim()

            val parts = listOfNotNull(
                street.takeIf { it.isNotBlank() },
                town.takeIf { it.isNotBlank() },
                district.takeIf { it.isNotBlank() },
                pin.takeIf { it.isNotBlank() }
            )
            parts.joinToString(", ").ifEmpty {
                activeProfile.oor.values.firstOrNull { it.isNotBlank() }.orEmpty()
            }
        }
    }

    var showCompanySheet by remember { mutableStateOf(false) }

    val dotColor = remember(activeProfile.thoatraNiram) {
        try {
            val cleanHex = activeProfile.thoatraNiram.removePrefix("#")
            if (cleanHex.length == 6) {
                Color(cleanHex.toLong(16) or 0x00000000FF000000L)
            } else colors.accent
        } catch (_: Exception) {
            colors.accent
        }
    }

    val pillShape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding, vertical = 2.dp)
            .height(84.dp)
            .clip(pillShape)
            .background(colors.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ShellDefaults.ripple(colors, bounded = true),
                onClick = { showCompanySheet = true }
            )
            .padding(start = 11.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Circle Avatar (62dp concentric with 84dp pill)
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = if (isDark) 0.18f else 0.12f))
                    .border(
                        width = 1.5.dp,
                        color = dotColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLineText.take(1).uppercase(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = dotColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Company Name & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = firstLineText.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.01).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                        contentDescription = "Switch Company",
                        tint = colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (subtitleText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitleText.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
