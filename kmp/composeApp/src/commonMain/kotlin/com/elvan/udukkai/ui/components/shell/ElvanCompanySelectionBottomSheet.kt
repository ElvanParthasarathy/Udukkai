package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
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
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Clean, standard Company Profile Selection Bottom Sheet.
 * Built directly on top of [ElvanSelectionBottomSheet] matching Settings selection styling 1:1.
 * - Simple monochrome design without decorative borders or card outlines
 * - Neutral theme tokens for initials avatar (no thoatraNiram color override)
 * - Pure monochrome checkmark indicator for active company
 * - Language-aware business name lookup respecting active UI language (Tamil/English/Latin)
 * - Full legal names wrap naturally
 * - Downward swipe-to-dismiss with native drag handle
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
    val uiLang = LanguageManager.activeLanguageCode

    val currentProfile = remember(profiles, activeProfileId) {
        profiles.find { it.id == activeProfileId }
    }

    fun getLocalizedBusinessName(profile: NiruvanaTharavugal): String {
        // 1. Direct match with active app's UI language
        val direct = profile.niruvanathinPeyar[uiLang]?.takeIf { it.isNotBlank() }
        if (direct != null) return direct

        // 2. Language-aware fallback:
        if (uiLang == "en") {
            val ta = profile.niruvanathinPeyar["ta"]?.takeIf { it.isNotBlank() }
            if (ta != null) {
                val latin = NavilMozhimaatri.transliterate(ta)
                if (latin.isNotBlank()) return latin
                return ta
            }
        } else {
            val ta = profile.niruvanathinPeyar["ta"]?.takeIf { it.isNotBlank() }
            if (ta != null) return ta
            val en = profile.niruvanathinPeyar["en"]?.takeIf { it.isNotBlank() }
            if (en != null) return en
        }

        // 3. Fallback to short name
        if (profile.kurumPeyar.isNotBlank()) return profile.kurumPeyar

        // 4. Any non-blank name
        return profile.niruvanathinPeyar.values.firstOrNull { it.isNotBlank() }.orEmpty()
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
            val shortName = profile.kurumPeyar.trim()
            val legalName = getLocalizedBusinessName(profile).trim()
            if (shortName.isNotEmpty()) shortName else legalName.ifEmpty { "—" }
        },
        subtitleBuilder = { profile ->
            val shortName = profile.kurumPeyar.trim()
            val legalName = getLocalizedBusinessName(profile).trim()
            val city = (profile.oor[uiLang] ?: profile.oor["ta"] ?: profile.oor["en"] ?: profile.oor.values.firstOrNull { it.isNotBlank() }).orEmpty().trim()
            val gstin = profile.gstin.trim()

            val parts = mutableListOf<String>()
            if (legalName.isNotEmpty() && !legalName.equals(shortName, ignoreCase = true)) {
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
            val shortName = profile.kurumPeyar.trim()
            val legalName = getLocalizedBusinessName(profile).trim()
            val initial = (if (shortName.isNotEmpty()) shortName else legalName).take(1).uppercase().preventBrokenLigatures().ifEmpty { "—" }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
            }
        },
        onRequestAddNew = onManageProfiles,
        addNewLabel = K.companySettings.tr()
    )
}
