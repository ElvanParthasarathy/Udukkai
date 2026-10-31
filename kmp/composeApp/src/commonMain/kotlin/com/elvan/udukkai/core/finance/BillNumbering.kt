package com.elvan.udukkai.core.finance

import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.InvoiceModel

/** What one financial year holds, for the Books page. */
data class FinancialYearSummary(
    val fyStart: Int,
    val count: Int,
    val total: Double,
    val firstNumber: String,
    val lastNumber: String,
    /** Series (the number without its serial) to the serials missing from it. */
    val gaps: Map<String, List<Int>>,
)

object BillNumbering {
    /** "26-27" for a date between April 2026 and March 2027. */
    fun fyShort(millis: Long): String = DateUtils.financialYearLabel(millis).let { it.substring(2, 4) + it.substring(4) }

    /** "VRM/26-27/" */
    fun seriesPrefix(base: String, millis: Long) = "$base/${fyShort(millis)}/"

    fun format(prefix: String, serial: Int) = prefix + serial.toString().padStart(4, '0')

    private val TRAILING_DIGITS = Regex("(\\d+)$")

    fun serialOf(number: String): Int? = TRAILING_DIGITS.find(number.trim())?.groupValues?.get(1)?.toIntOrNull()

    private fun seriesOf(number: String): String = number.trim().replace(TRAILING_DIGITS, "")

    /** One more than the highest serial among [numbers] that start with [prefix] (the case does not matter). */
    fun nextSerial(numbers: List<String>, prefix: String): Int =
        (numbers.filter { it.trim().startsWith(prefix, ignoreCase = true) }.mapNotNull(::serialOf).maxOrNull() ?: 0) + 1

    /** For each series, the serials missing between its lowest and highest (a year of one running series does not start at 1). */
    fun gaps(numbers: List<String>): Map<String, List<Int>> =
        numbers.mapNotNull { n -> serialOf(n)?.let { seriesOf(n).uppercase() to it } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, serials) -> val have = serials.toSet(); (serials.min()..serials.max()).filter { it !in have } }
            .filterValues { it.isNotEmpty() }

    /**
     * One summary per financial year that has bills, newest first. [kept] are the live bills, [removed] the ones
     * in the recycle bin: their numbers still count as used, so only numbers gone for good show up as gaps.
     * Proformas are estimates and are left out.
     */
    fun summaries(kept: List<InvoiceModel>, removed: List<InvoiceModel>): List<FinancialYearSummary> {
        val live = kept.filter { it.invoiceType != "proforma" }
        val gone = removed.filter { it.invoiceType != "proforma" }
        val years = live.groupBy { DateUtils.financialYearStart(it.invoiceDate) }
        return years.keys.sortedDescending().map { fy ->
            val bills = years.getValue(fy)
            val numbers = (bills + gone.filter { DateUtils.financialYearStart(it.invoiceDate) == fy }).map { it.invoiceNumber }
            val byDate = bills.sortedWith(compareBy({ it.invoiceDate }, { it.id }))
            FinancialYearSummary(fy, bills.size, bills.sumOf { it.totalAmount }, byDate.first().invoiceNumber, byDate.last().invoiceNumber, gaps(numbers))
        }
    }
}
