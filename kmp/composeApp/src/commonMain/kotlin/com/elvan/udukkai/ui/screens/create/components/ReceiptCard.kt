package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.data.settings.DataLanguageDisplay
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.PaymentType
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols

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
    /** The customer's ஊர (city) field, which is the place shown under the name (not the saved address text). */
    customerCity: Map<String, String> = emptyMap()
) {
    val resolvedProfile = (if (receipt.companyId != null) allProfiles.find { it.id == receipt.companyId } else null)
        ?: profile
        ?: com.elvan.udukkai.data.settings.BusinessProfile()
    val language = DataLanguageDisplay(resolvedProfile)
    val primary = language.first(receipt.customerName).ifBlank { receipt.receiptNumber }
    val secondary = language.second(receipt.customerName)
    val showSecondary = secondary.isNotBlank()

    // Under the name: the second-language name with the place (in the main language) after a dot; the place alone without one
    val primaryCity = language.first(customerCity)
    val nameSecondLine = listOf(secondary.takeIf { showSecondary }.orEmpty(), primaryCity)
        .filter { it.isNotBlank() }.joinToString("  •  ")

    val dateStr = DateUtils.formatEpochMillis(receipt.receiptDate)
    val amountStr = CurrencyUtils.formatInr(receipt.amount, showDecimals = receipt.amount % 1.0 != 0.0)

    val paymentType = PaymentType.fromStored(receipt.paymentMode)

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelected,
        isSelectionMode = isSelectionMode,
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
                status = paymentType.labelString(LocalAppLanguage.current),
                textLines = 1,
                isSelectionMode = isSelectionMode,
                paymentIcon = paymentType.icon,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
