package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel

private data class InvoiceTypeOption(val id: String, val label: String)

/**
 * Universal Invoice Type Selector (Tax Invoice / Proforma) for both Textiles and Factory modes.
 */
@Composable
fun InvoiceTypeField(
    invoiceType: String,
    onChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    var isSheetOpen by remember { mutableStateOf(false) }

    val options = listOf(
        InvoiceTypeOption("tax-invoice", K.taxInvoice.tr()),
        InvoiceTypeOption("proforma", K.proforma.tr())
    )

    val currentOption = options.find { it.id == invoiceType } ?: options.first()
    val containerBg = colors.iconBg

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ElvanEditorLabel(label = K.invoiceType.tr())

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(containerBg)
                    .clickable { isSheetOpen = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentOption.label.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    )
                    Icon(
                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isSheetOpen) {
            ElvanSelectionBottomSheet(
                title = K.invoiceType.tr(),
                items = options,
                currentValue = currentOption,
                onSelected = { opt ->
                    onChanged(opt.id)
                    isSheetOpen = false
                },
                onDismissRequest = { isSheetOpen = false },
                itemLabelBuilder = { it.label }
            )
        }
    }
}
