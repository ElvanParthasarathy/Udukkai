package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel

/** What one financial year holds, for the Books page. */
data class FinancialYearSummary(val fyStart: Int, val count: Int, val total: Double)

object BillNumbering {
    /** "26-27" for a date between April 2026 and March 2027. */
    fun fyShort(millis: Long): String = DateUtils.financialYearLabel(millis).let { it.substring(2, 4) + it.substring(4) }

    /** "VRM-007": the short number every bill shows. The year is kept apart, the pair is what must be unique. */
    fun format(base: String, serial: Int) = "$base-" + serial.toString().padStart(3, '0')

    private val TRAILING_DIGITS = Regex("(\\d+)$")

    fun serialOf(number: String): Int? = TRAILING_DIGITS.find(number.trim())?.groupValues?.get(1)?.toIntOrNull()

    /** One more than the highest serial among [numbers] that start with "[base]-" (the case does not matter). */
    fun nextSerial(numbers: List<String>, base: String): Int =
        (numbers.filter { it.trim().startsWith("$base-", ignoreCase = true) }.mapNotNull(::serialOf).maxOrNull() ?: 0) + 1

    /** One summary per financial year that has bills, newest first. Proformas are estimates and are left out. */
    fun summaries(bills: List<InvoiceModel>): List<FinancialYearSummary> =
        bills.filter { it.invoiceType != "proforma" }
            .groupBy { DateUtils.financialYearStart(it.invoiceDate) }
            .map { (fy, list) -> FinancialYearSummary(fy, list.size, list.sumOf { it.totalAmount }) }
            .sortedByDescending { it.fyStart }
}
