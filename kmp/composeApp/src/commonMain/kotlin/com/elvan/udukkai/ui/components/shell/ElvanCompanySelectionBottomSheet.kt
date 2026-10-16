package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Clean, standard Company Profile Selection Bottom Sheet.
 * Built directly on top of [ElvanSelectionBottomSheet] matching Settings selection styling 1:1.
 * - Zero upward spring bounce / Zero-detach physics
 * - Downward swipe-to-dismiss with native drag handle
 * - Standardized typography, margins, and dark/light tokens
 * - Pure monochrome checkmark indicator for active company
 * - Direct shortcut button for Company Settings
 */
@Composable
fun ElvanCompanySelectionBottomSheet(
    profiles: List<NiruvanaTharavugal>,
    activeProfileId: Long?,
    onSelectProfile: (Long) -> Unit,
    onManageProfiles: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val currentProfile = remember(profiles, activeProfileId) {
        profiles.find { it.id == activeProfileId }
    }

    ElvanSelectionBottomSheet<NiruvanaTharavugal>(
        title = K.selectCompanyProfile.tr(),
        items = profiles,
        currentValue = currentProfile,
        onSelected = { profile ->
            profile.id?.let { onSelectProfile(it) }
        },
        onDismissRequest = onDismissRequest,
        colors = colors,
        itemLabelBuilder = { profile ->
            profile.kurumPeyar.ifEmpty {
                profile.niruvanathinPeyar.values.firstOrNull().orEmpty()
            }.ifEmpty { "—" }
        },
        subtitleBuilder = { profile ->
            val shortName = profile.kurumPeyar.ifEmpty {
                profile.niruvanathinPeyar.values.firstOrNull().orEmpty()
            }
            val legalName = profile.niruvanathinPeyar.values.firstOrNull().orEmpty()
            val city = profile.oor.values.firstOrNull().orEmpty()
            val gstin = profile.gstin

            val parts = mutableListOf<String>()
            if (legalName.isNotEmpty() && legalName != shortName) {
                parts.add(legalName)
            }
            val locationOrTax = listOfNotNull(
                gstin.takeIf { it.isNotEmpty() },
                city.takeIf { it.isNotEmpty() }
            ).joinToString(" · ")
            if (locationOrTax.isNotEmpty()) {
                parts.add(locationOrTax)
            }
            if (parts.isNotEmpty()) parts.joinToString(" • ") else null
        },
        leadingBuilder = { profile ->
            val shortName = profile.kurumPeyar.ifEmpty {
                profile.niruvanathinPeyar.values.firstOrNull().orEmpty()
            }.ifEmpty { "—" }

            val dotColor = remember(profile.thoatraNiram) {
                try {
                    val cleanHex = profile.thoatraNiram.removePrefix("#")
                    if (cleanHex.length == 6) {
                        Color(cleanHex.toLong(16) or 0x00000000FF000000L)
                    } else colors.accent
                } catch (_: Exception) {
                    colors.accent
                }
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = if (isDark) 0.18f else 0.12f))
                    .border(
                        width = 1.dp,
                        color = dotColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shortName.take(1).uppercase().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = dotColor
                    )
                )
            }
        },
        onRequestAddNew = onManageProfiles,
        addNewLabel = K.companySettings.tr()
    )
}
