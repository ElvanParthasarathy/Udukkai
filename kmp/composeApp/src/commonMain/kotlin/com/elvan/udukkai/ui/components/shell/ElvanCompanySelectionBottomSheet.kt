package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.localization.K
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
 *       No subtitle (returns null; no address, GSTIN or secondary details).
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
    val currentProfile = remember(profiles, activeProfileId) {
        profiles.find { it.id == activeProfileId }
    }

    // Active profile's saved billing-language settings
    val activeBilling = currentProfile ?: profiles.firstOrNull()
    val isBilingual = activeBilling?.iruMozhi ?: true
    val primaryBillingLang = activeBilling?.mudhanMozhi?.ifEmpty { "ta" } ?: "ta"
    val secondaryBillingLang = activeBilling?.thunaiMozhi?.ifEmpty { "en" } ?: "en"

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
            resolveCompanyItemLabel(profile, primaryBillingLang).ifEmpty { "—" }
        },
        subtitleBuilder = { profile ->
            resolveCompanySubtitle(
                profile = profile,
                isBilingual = isBilingual,
                primaryBillingLang = primaryBillingLang,
                secondaryBillingLang = secondaryBillingLang
            )
        },
        onRequestAddNew = onManageProfiles,
        addNewLabel = K.companySettings.tr()
    )
}

/**
 * Resolves the primary business name for display in the bottom sheet.
 */
internal fun resolveCompanyItemLabel(
    profile: NiruvanaTharavugal,
    primaryBillingLang: String = "ta"
): String {
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

/**
 * Resolves the subtitle for a company row in the bottom sheet:
 * - Returns the secondary business name when [isBilingual] is true.
 * - Returns null when [isBilingual] is false (showing no subtitle space or address).
 */
internal fun resolveCompanySubtitle(
    profile: NiruvanaTharavugal,
    isBilingual: Boolean,
    primaryBillingLang: String = "ta",
    secondaryBillingLang: String = "en"
): String? {
    if (!isBilingual) return null

    val primaryName = resolveCompanyItemLabel(profile, primaryBillingLang)
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
