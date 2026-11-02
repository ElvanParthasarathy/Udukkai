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
 * Bilingual bills only: the three fields that print in one language (business address, customer name, customer address)
 * and which language each prints in, English or Tamil. Stored in the business's print settings and read by the bill
 * papers. Shared by the Textiles and Factory "Create" settings.
 */
@Composable
fun LanguageRulesSection(profile: BusinessProfile, colors: ShellColors, onSave: (BusinessProfile) -> Unit) {
    val bottomSheet = LocalElvanBottomSheetController.current
    val english = K.enCode.tr()
    val tamil = K.taCode.tr()

    fun modeLabel(mode: String) = if (mode == "ta") tamil else english
    val titles = mapOf(
        "businessAddress" to K.ruleBusinessAddress.tr(),
        "customerName" to K.ruleCustomerName.tr(),
        "customerAddress" to K.ruleCustomerAddress.tr(),
    )

    ElvanSettingsSection(title = K.languageRules.tr(), colors = colors) {
        LanguageRules.fields.forEachIndexed { i, field ->
            if (i > 0) ElvanSettingsDivider(colors = colors)
            val current = LanguageRules.get(profile.printSettingsJson, field)
            ElvanSettingsDisplayRow(
                title = titles.getValue(field),
                primaryValue = modeLabel(current),
                onEdit = {
                    bottomSheet.showSelection(
                        title = titles.getValue(field),
                        items = LanguageRules.modes,
                        currentValue = current,
                        itemLabelBuilder = ::modeLabel,
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
