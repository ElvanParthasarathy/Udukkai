package com.elvan.udukkai.factory.ui.screens.editor

import androidx.compose.runtime.Composable
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.ui.screens.editor.CustomerEditorScreen

/**
 * Dedicated Customer Editor for Udukkai Factory (ஆலை).
 * Thin wrapper delegating to shared [CustomerEditorScreen] with GST disabled by default.
 */
@Composable
fun FactoryCustomerEditorScreen(
    merchant: CustomerModel? = null,
    onBack: () -> Unit
) {
    val profile = FactoryProfileRepository.profile
    CustomerEditorScreen(
        merchant = merchant,
        showGst = false,
        isBilingual = profile.isBilingual,
        primaryLang = profile.primaryLanguage.ifEmpty { "ta" },
        secondaryLang = profile.secondaryLanguage.ifEmpty { "en" },
        allProfiles = FactoryProfileRepository.profiles,
        activeCompanyId = profile.id,
        onSave = { FactoryCustomerRepository.save(it) },
        onBack = onBack
    )
}
