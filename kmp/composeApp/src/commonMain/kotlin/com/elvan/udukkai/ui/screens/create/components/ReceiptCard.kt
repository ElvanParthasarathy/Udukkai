package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

@Composable
fun ReceiptCard(
    index: Int,
    receipt: ReceiptModel,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    profile: com.elvan.udukkai.data.settings.BusinessProfile? = null,
    allProfiles: List<com.elvan.udukkai.data.settings.BusinessProfile> = emptyList(),
    linkedInvoiceNumbers: List<String> = emptyList(),
    /** The customer's ஊர (city) field, which is the place shown under the name (not the saved address text). */
    customerCity: Map<String, String> = emptyMap()
) {
    val ff = LocalAppFontFamily.current

    val resolvedProfile = (if (receipt.companyId != null) allProfiles.find { it.id == receipt.companyId } else null)
        ?: profile
        ?: com.elvan.udukkai.data.settings.BusinessProfile()
    val isBilingual = resolvedProfile.isBilingual
    val primaryLang = resolvedProfile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = resolvedProfile.secondaryLanguage.ifEmpty { "en" }

    val primary = receipt.customerName[primaryLang]
        ?: receipt.customerName["ta"]
        ?: receipt.customerName["en"]
        ?: receipt.customerName.values.firstOrNull()
        ?: receipt.receiptNumber

    val secondary = if (isBilingual) receipt.customerName[secondaryLang].orEmpty() else ""
    val showSecondary = isBilingual && secondary.isNotBlank() && secondary != primary

    // Under the name: the second-language name with the place (in the main language) after a dot; the place alone without one
    val primaryCity = customerCity[primaryLang] ?: customerCity["ta"] ?: customerCity["en"] ?: customerCity.values.firstOrNull() ?: ""
    val nameSecondLine = listOf(secondary.takeIf { showSecondary }.orEmpty(), primaryCity)
        .filter { it.isNotBlank() }.joinToString("  •  ")

    val dateStr = DateUtils.formatEpochMillis(receipt.receiptDate)
    val amountStr = CurrencyUtils.formatInr(receipt.amount, showDecimals = receipt.amount % 1.0 != 0.0)

    val (taLabel, enLabel) = when (receipt.paymentMode.lowercase()) {
        "cash" -> "காசு" to "Cash"
        "upi" -> "UPI" to "UPI"
        "bank_transfer" -> "வங்கி" to "Bank"
        "cheque" -> "காசோலை" to "Cheque"
        "card" -> "அட்டை" to "Card"
        else -> receipt.paymentMode to ""
    }

    val paymentModeText = if (primaryLang.lowercase().startsWith("en") && enLabel.isNotEmpty()) enLabel else taLabel
    val linkedInvoicesText = linkedInvoiceNumbers.joinToString(", ")

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelected,
        modifier = modifier,
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
                icon = MaterialSymbols.Rounded.ReceiptFill,
                tint = colors.receiptColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            DocCardText(
                colors = colors,
                primary = primary,
                secondLine = nameSecondLine,
                amount = amountStr,
                date = dateStr,
                number = receipt.receiptNumber,
                fy = "FY ${com.elvan.udukkai.core.finance.BillNumbering.fyShort(receipt.receiptDate)}",
                status = paymentModeText,
                textLines = 1,
                isSelectionMode = isSelectionMode,
                modifier = Modifier.weight(1f),
                below = {
                    if (linkedInvoicesText.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = linkedInvoicesText.preventBrokenLigatures(),
                            style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = LocalShellColors.current.textTertiary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
        }
    }
}
