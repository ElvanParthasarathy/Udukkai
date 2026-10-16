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
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
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
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)
    val allProfiles = NiruvanaTharavugalRepository.getAllProfiles(mode)

    val companyName = activeProfile.kurumPeyar.ifEmpty {
        activeProfile.niruvanathinPeyar.values.firstOrNull().orEmpty()
    }.ifEmpty { mode.displayName() }

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
                    text = companyName.take(1).uppercase(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = dotColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Greeting & Company Name
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
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = companyName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                        contentDescription = "Switch Company",
                        tint = colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
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
