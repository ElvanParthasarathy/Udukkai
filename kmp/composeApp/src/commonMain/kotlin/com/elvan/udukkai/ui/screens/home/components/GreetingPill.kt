package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.shell.ElvanCompanySelectionBottomSheet
import com.elvan.udukkai.ui.components.shell.ElvanSectionContainer
import com.elvan.udukkai.ui.components.shell.resolveCompanyItemLabel
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * GreetingPill — Active Company Switcher & Greeting Header.
 * Displays "வணக்கம் ✨" and the active company's full legal business name with company avatar.
 * Styled matching the Settings Hub Mode Pill in dimensions, typography, and monochrome appearance.
 * Tapping opens the ElvanCompanySelectionBottomSheet to easily switch between company profiles.
 */
@Composable
fun GreetingPill(
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onProfileChanging: ((Long) -> Unit)? = null,
    onManageProfiles: (() -> Unit)? = null
) {
    val mode = LocalAppMode.current
    val ff = LocalAppFontFamily.current
    val defaultModeName = mode.displayName()

    val activeProfile = NiruvanaTharavugalRepository.getProfile(mode)
    val allProfiles = NiruvanaTharavugalRepository.getAllProfiles(mode)

    val fullLegalBusinessName = remember(activeProfile, defaultModeName) {
        val primary = resolveCompanyItemLabel(activeProfile).trim()
        if (primary.isNotEmpty()) {
            primary
        } else if (activeProfile.kurumPeyar.isNotBlank()) {
            activeProfile.kurumPeyar.trim()
        } else {
            defaultModeName
        }
    }

    var showCompanySheet by remember { mutableStateOf(false) }

    ElvanSectionContainer(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ShellDefaults.ripple(colors, bounded = true),
                    onClick = { showCompanySheet = true }
                ),
            shape = RoundedCornerShape(999.dp),
            color = colors.surface,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(start = 11.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The circular Company Indicator on the left (diameter 62dp, concentric with 84dp pill, monochrome matching Settings)
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(colors.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullLegalBusinessName.take(1).uppercase(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Greeting & Company Name
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = K.vanakkam.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 22.sp
                            ),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = MaterialSymbols.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fullLegalBusinessName.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.textPrimary.copy(alpha = 0.5f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                            contentDescription = "Switch Company",
                            tint = colors.textPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCompanySheet) {
        ElvanCompanySelectionBottomSheet(
            profiles = allProfiles,
            activeProfileId = activeProfile.id,
            onSelectProfile = { selectedId ->
                if (selectedId != activeProfile.id) {
                    if (onProfileChanging != null) {
                        onProfileChanging(selectedId)
                    } else {
                        NiruvanaTharavugalRepository.setActiveProfile(mode, selectedId)
                    }
                }
            },
            onManageProfiles = onManageProfiles,
            onDismissRequest = { showCompanySheet = false },
            colors = colors
        )
    }
}
