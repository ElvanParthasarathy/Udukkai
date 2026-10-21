package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.runtime.Composable
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.ui.screens.editor.CustomerEditorScreen

/**
 * Dedicated Customer Editor for Udukkai Textiles (சவுளி).
 * Thin wrapper delegating to shared [CustomerEditorScreen] with GST enabled.
 */
@Composable
fun TextilesCustomerEditorScreen(
    merchant: CustomerModel? = null,
    onBack: () -> Unit
) {
    val profile = TextilesProfileRepository.profile
    CustomerEditorScreen(
        merchant = merchant,
        showGst = true,
        isBilingual = profile.isBilingual,
        primaryLang = profile.primaryLanguage.ifEmpty { "ta" },
        secondaryLang = profile.secondaryLanguage.ifEmpty { "en" },
        onSave = { TextilesCustomerRepository.save(it) },
        onBack = onBack
    )
}
