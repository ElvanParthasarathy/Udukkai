package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.data.model.VaangunarTharavuru

/**
 * Backward-compatibility wrapper for SilkCustomerField.
 * Use [TextilesCustomerField] for Textiles mode or [FactoryCustomerField] for Factory mode.
 */
@Deprecated("Use TextilesCustomerField or FactoryCustomerField instead", ReplaceWith("TextilesCustomerField(selectedVaangunarId, onCustomerSelected, onCustomerCleared, onRequestAddNewCustomer, modifier)"))
@Composable
fun LegacySilkCustomerField(
    selectedVaangunarId: Long?,
    onCustomerSelected: (VaangunarTharavuru) -> Unit,
    onCustomerCleared: () -> Unit,
    onRequestAddNewCustomer: () -> Unit,
    modifier: Modifier = Modifier
) = TextilesCustomerField(selectedVaangunarId, onCustomerSelected, onCustomerCleared, onRequestAddNewCustomer, modifier)
