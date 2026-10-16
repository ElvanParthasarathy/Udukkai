package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Clean, standard Company Profile Selection Bottom Sheet.
 * Built directly on top of [ElvanSelectionBottomSheet] matching Settings selection styling 1:1.
 * - Simple monochrome design without decorative borders, card outlines, or avatar initials
 * - Pure monochrome checkmark indicator for active company
 * - Bilingual billing display rules:
 *     - When bilingual billing is ON: First line = primary billing language business name;
 *       Subtitle = secondary billing language business name (no address, GSTIN or extra details).
 *     - When bilingual billing is OFF: First line = primary billing language business name;
 *       Subtitle = business address (no GSTIN or extra details).
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
    val uiLang = LanguageManager.activeLanguageCode

    val currentProfile = remember(profiles, activeProfileId) {
        profiles.find { it.id == activeProfileId }
    }

    // Active profile's saved billing-language settings
    val activeBilling = currentProfile ?: profiles.firstOrNull()
    val isBilingual = activeBilling?.iruMozhi ?: true
    val primaryBillingLang = activeBilling?.mudhanMozhi?.ifEmpty { "ta" } ?: "ta"
    val secondaryBillingLang = activeBilling?.thunaiMozhi?.ifEmpty { "en" } ?: "en"

    fun getPrimaryBusinessName(profile: NiruvanaTharavugal): String {
        val primary = profile.niruvanathinPeyar[primaryBillingLang]?.trim()?.takeIf { it.isNotEmpty() }
        if (primary != null) return primary

        // Missing translation fallback
        val otherLang = if (primaryBillingLang == "ta") "en" else "ta"
        val other = profile.niruvanathinPeyar[otherLang]?.trim()?.takeIf { it.isNotEmpty() }
        if (other != null) {
            if (primaryBillingLang.startsWith("en") || primaryBillingLang == "ta-Latn") {
                val latin = NavilMozhimaatri.transliterate(other).trim()
                if (latin.isNotEmpty()) return latin
            }
            return other
        }

        val anyLegal = profile.niruvanathinPeyar.values.firstOrNull { it.isNotBlank() }?.trim()
        if (anyLegal != null) return anyLegal
        if (profile.kurumPeyar.isNotBlank()) return profile.kurumPeyar.trim()
        return ""
    }

    fun getSecondaryBusinessName(profile: NiruvanaTharavugal, primaryName: String): String? {
        val direct = profile.niruvanathinPeyar[secondaryBillingLang]?.trim()?.takeIf { it.isNotEmpty() }
        if (direct != null && !direct.equals(primaryName, ignoreCase = true)) {
            return direct
        }

        // Transliteration fallback if secondary is Latin/English and direct translation is missing
        if (secondaryBillingLang.startsWith("en") || secondaryBillingLang == "ta-Latn") {
            val ta = profile.niruvanathinPeyar["ta"]?.trim()?.takeIf { it.isNotEmpty() }
                ?: primaryName.takeIf { it.isNotEmpty() }
            if (ta != null) {
                val latin = NavilMozhimaatri.transliterate(ta).trim()
                if (latin.isNotEmpty() && !latin.equals(primaryName, ignoreCase = true)) {
                    return latin
                }
            }
        } else if (secondaryBillingLang == "ta") {
            val ta = profile.niruvanathinPeyar["ta"]?.trim()?.takeIf { it.isNotEmpty() }
            if (ta != null && !ta.equals(primaryName, ignoreCase = true)) {
                return ta
            }
        }

        return null
    }

    fun getAddressSubtitle(profile: NiruvanaTharavugal): String? {
        val street = (profile.mugavari[uiLang] ?: profile.mugavari["ta"] ?: profile.mugavari["en"] ?: profile.mugavari.values.firstOrNull { it.isNotBlank() }).orEmpty().trim()
        val town = (profile.oor[uiLang] ?: profile.oor["ta"] ?: profile.oor["en"] ?: profile.oor.values.firstOrNull { it.isNotBlank() }).orEmpty().trim()
        val district = (profile.maavattam[uiLang] ?: profile.maavattam["ta"] ?: profile.maavattam["en"] ?: profile.maavattam.values.firstOrNull { it.isNotBlank() }).orEmpty().trim()
        val pin = profile.anjalKuriyeedu.trim()

        val parts = listOfNotNull(
            street.takeIf { it.isNotEmpty() },
            town.takeIf { it.isNotEmpty() },
            district.takeIf { it.isNotEmpty() },
            pin.takeIf { it.isNotEmpty() }
        )
        return parts.joinToString(", ").takeIf { it.isNotEmpty() }
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
            getPrimaryBusinessName(profile).ifEmpty { "—" }
        },
        subtitleBuilder = { profile ->
            val primaryName = getPrimaryBusinessName(profile)
            if (isBilingual) {
                getSecondaryBusinessName(profile, primaryName)
            } else {
                getAddressSubtitle(profile)
            }
        },
        onRequestAddNew = onManageProfiles,
        addNewLabel = K.companySettings.tr()
    )
}
