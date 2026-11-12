package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.runtime.Composable
import com.elvan.udukkai.core.finance.InvoicePaymentFilter
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.components.shell.sheets.ElvanSheetItem

@Composable
internal fun InvoicePaymentFilter.label(): String = when (this) {
    InvoicePaymentFilter.All -> K.allBills.tr()
    InvoicePaymentFilter.Paid -> K.statusPaid.tr()
    InvoicePaymentFilter.Unpaid -> K.statusUnpaid.tr()
}

/**
 * DocumentFilterSheet — unified filter sheet.
 *
 * Layout:
 *  1. Date Filter row  (navigation item — no tick, subtitle shows active date range)
 *  2. Payment rows     (selectable — tick on the active InvoicePaymentFilter value)
 *
 * Fixes vs. the old FilterOption data-class approach:
 *  - Lambda fields in a data class break equality (lambdas use reference identity, not structural equality),
 *    so the old `item == currentValue` check in ElvanSelectionBottomSheet ALWAYS returned false.
 *  - Now: Date row is explicitly rendered with isSelected = false (never ticked).
 *  - Payment rows compare FilterRow.PaymentItem data classes — no lambda fields — equality is correct.
 */
@Composable
internal fun DocumentFilterSheet(
    payment: InvoicePaymentFilter?,
    onPayment: (InvoicePaymentFilter) -> Unit,
    start: Long?,
    end: Long?,
    onDate: () -> Unit,
    onDismiss: () -> Unit,
    colors: ShellColors
) {
    sealed interface FilterRow {
        data object DateNav : FilterRow
        data class PaymentItem(val filter: InvoicePaymentFilter) : FilterRow
    }

    val rows: List<FilterRow> = buildList {
        add(FilterRow.DateNav)
        if (payment != null) InvoicePaymentFilter.entries.forEach { add(FilterRow.PaymentItem(it)) }
    }

    // Active tick target: the PaymentItem matching the current payment filter
    val currentValue: FilterRow? = payment?.let { FilterRow.PaymentItem(it) }

    ElvanSelectionBottomSheet(
        title = K.filterBy.tr(),
        items = rows,
        currentValue = currentValue,
        colors = colors,
        itemRowContent = { row, isSelected, _ ->
            when (row) {
                FilterRow.DateNav -> {
                    // Date row is a navigation shortcut — never ticked, subtitle shows the active range
                    ElvanSheetItem(
                        title = K.dateFilter.tr(),
                        subtitle = dateFilterLabel(start, end).ifBlank { null },
                        isSelected = false,
                        onTap = onDate,
                        colors = colors
                    )
                }
                is FilterRow.PaymentItem -> {
                    // Payment rows: tick reflects which filter is active
                    ElvanSheetItem(
                        title = row.filter.label(),
                        isSelected = isSelected,
                        onTap = { onPayment(row.filter) },
                        colors = colors
                    )
                }
            }
        },
        onDismissRequest = onDismiss
    )
}
