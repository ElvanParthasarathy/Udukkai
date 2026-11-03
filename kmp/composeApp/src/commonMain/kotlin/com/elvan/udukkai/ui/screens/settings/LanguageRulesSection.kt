package com.elvan.udukkai.ui.screens.settings

import androidx.compose.runtime.Composable
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.LanguageRules
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDisplayRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.LocalElvanBottomSheetController

/**
 * Bilingual bills only: the business name (English then Tamil, or Tamil then English), the business address and the
 * customer (name and address together), each in English or Tamil. Stored in the business's print settings and read by
 * the bill papers. Shared by the Textiles and Factory "Create" settings.
 */
@Composable
fun LanguageRulesSection(profile: BusinessProfile, colors: ShellColors, onSave: (BusinessProfile) -> Unit) {
    val bottomSheet = LocalElvanBottomSheetController.current
    val english = K.enCode.tr()
    val tamil = K.taCode.tr()

    fun modeLabel(field: String, mode: String) = when {
        LanguageRules.isBoth(field) -> if (mode == "ta") "$tamil → $english" else "$english → $tamil"
        mode == "ta" -> tamil
        else -> english
    }
    val titles = mapOf(
        "businessName" to K.ruleBusinessName.tr(),
        "businessAddress" to K.ruleBusinessAddress.tr(),
        "customer" to K.ruleCustomer.tr(),
    )

    ElvanSettingsSection(title = K.languageRules.tr(), colors = colors) {
        LanguageRules.fields.forEachIndexed { i, field ->
            if (i > 0) ElvanSettingsDivider(colors = colors)
            val current = LanguageRules.get(profile.printSettingsJson, field)
            ElvanSettingsDisplayRow(
                title = titles.getValue(field),
                primaryValue = modeLabel(field, current),
                onEdit = {
                    bottomSheet.showSelection(
                        title = titles.getValue(field),
                        items = LanguageRules.modes,
                        currentValue = current,
                        itemLabelBuilder = { modeLabel(field, it) },
                        onSelected = { mode ->
                            onSave(profile.copy(printSettingsJson = LanguageRules.set(profile.printSettingsJson, field, mode)))
                        }
                    )
                },
                colors = colors
            )
        }
    }
}
