package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.finance.BillNumbering
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Textiles (சவுளி) Invoice Card for the Invoices list.
 */
@Composable
fun TextilesInvoiceCard(
    index: Int,
    invoice: InvoiceModel,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    isPaid: Boolean = TextilesInvoiceRepository.hasLinkedReceipts(invoice.id),
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val profiles = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profiles
    val profile = (if (invoice.companyId != null) profiles.find { it.id == invoice.companyId } else null)
        ?: com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val primary = invoice.customerName[primaryLang]
        ?: invoice.customerName["ta"]
        ?: invoice.customerName["en"]
        ?: invoice.customerName.values.firstOrNull()
        ?: "-"

    val secondary = if (isBilingual) invoice.customerName[secondaryLang].orEmpty() else ""
    val showSecondary = isBilingual && secondary.isNotBlank() && secondary != primary

    // The place on the card is the customer's ஊர (city) field, not the saved address text: with the town in the
    // address ("Mullipattu") and the city in ஊர ("Arani"), the card shows Arani
    val cityMap = invoice.customerId?.let { id -> com.elvan.udukkai.textiles.data.TextilesCustomerRepository.customers.find { it.id == id }?.city } ?: emptyMap()
    val primaryCity = cityMap[primaryLang] ?: cityMap["ta"] ?: cityMap["en"] ?: cityMap.values.firstOrNull() ?: ""

    // The second line under the name: the second-language name with the place after a dot, the place in the main language ("Sundari Silks • ஆரணி").
    // Without a second language the place stands alone there, so the card adapts.
    val nameSecondLine = if (showSecondary) {
        listOf(secondary, primaryCity).filter { it.isNotBlank() }.joinToString("  •  ")
    } else primaryCity

    val amountStr = CurrencyUtils.formatInr(invoice.totalAmount, showDecimals = invoice.totalAmount % 1.0 != 0.0)
    val dateStr = DateUtils.formatEpochMillis(invoice.invoiceDate)
    val docTypeLabel = if (invoice.invoiceType == "proforma") K.proforma.tr() else K.taxInvoice.tr()
    // The type only shows for an estimate: a tax invoice is the usual bill, and its number is long enough already
    val leftDetail = dateStr

    // A screen reader reads the card as one item; at a larger text size the name and place wrap instead of being cut
    // A proforma is only an estimate: nothing is due on it, and its PF number already says what it is
    val statusText = if (invoice.invoiceType == "proforma") K.proforma.tr() else if (isPaid) K.statusPaid.tr() else K.statusUnpaid.tr()
    val spoken = listOfNotNull(invoice.invoiceNumber, "FY " + BillNumbering.fyShort(invoice.invoiceDate), primary, nameSecondLine.takeIf { it.isNotBlank() }, leftDetail, amountStr, statusText).joinToString(", ")
    val textLines = if (LocalDensity.current.fontScale > 1.15f) 2 else 1

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelected,
        isSelectionMode = isSelectionMode,
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = spoken },
        padding = PaddingValues(16.dp),
        borderRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.InvoiceFill,
                tint = colors.invoiceColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            DocCardText(
                colors = colors,
                primary = primary,
                secondLine = nameSecondLine,
                amount = amountStr,
                date = leftDetail,
                number = invoice.invoiceNumber,
                fy = "FY ${BillNumbering.fyShort(invoice.invoiceDate)}",
                status = statusText,
                textLines = textLines,
                isSelectionMode = isSelectionMode,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
