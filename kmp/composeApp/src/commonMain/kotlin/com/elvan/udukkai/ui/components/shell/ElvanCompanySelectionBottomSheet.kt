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
 *     - Each company's saved iruMozhi setting controls its own display.
 *     - When a company's bilingual billing is ON: First line = primary billing language business name;
 *       Subtitle = secondary billing language business name (no address, GSTIN or extra details).
 *     - When a company's bilingual billing is OFF: First line = primary billing language business name;
 *       No subtitle (returns null; no address, GSTIN or secondary details).
 *     - Selecting a company never shifts or resizes other rows.
 * - Full legal names wrap naturally
 * - Downward swipe-to-dismiss with native drag handle
 * - Pure company profile selection without bottom action button
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
            resolveCompanyItemLabel(profile).ifEmpty { "—" }
        },
        subtitleBuilder = { profile ->
            resolveCompanySubtitle(profile)
        },
        onRequestAddNew = null,
        addNewLabel = null
    )
}

/**
 * Resolves the primary business name for display in the bottom sheet.
 * Uses the profile's own mudhanMozhi setting by default.
 */
internal fun resolveCompanyItemLabel(
    profile: NiruvanaTharavugal,
    primaryBillingLang: String = profile.mudhanMozhi.ifEmpty { "ta" }
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
 * - Each company's saved iruMozhi setting controls its own subtitle display.
 * - Returns the secondary business name when [isBilingual] is true.
 * - Returns null when [isBilingual] is false (showing no subtitle space or address).
 */
internal fun resolveCompanySubtitle(
    profile: NiruvanaTharavugal,
    isBilingual: Boolean = profile.iruMozhi,
    primaryBillingLang: String = profile.mudhanMozhi.ifEmpty { "ta" },
    secondaryBillingLang: String = profile.thunaiMozhi.ifEmpty { "en" }
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
