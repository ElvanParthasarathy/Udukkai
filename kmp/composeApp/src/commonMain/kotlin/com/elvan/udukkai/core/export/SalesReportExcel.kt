package com.elvan.udukkai.core.export

/** One bill of a sales report, already in the form the sheet shows. */
data class SalesRow(
    val number: String,
    val client: String,   // both languages joined with a line break when the business is bilingual
    val date: String,
    val taxable: Double,
    val tax: Double,
    val total: Double
)

/**
 * The "Sales Report" sheet of the React app, made the same way: the business name (both languages) on top, the report
 * title, then Invoice No | Client | Date | Taxable Value | Tax Amount | Total, one bill per row, bordered, rupee format.
 * With [withTax] false (the coolie / factory report, which has no GST) the tax columns are left out.
 */
object SalesReportExcel {
    fun build(businessTitle: String, reportTitle: String, rows: List<SalesRow>, withTax: Boolean): ByteArray {
        val cols = if (withTax) 6 else 4
        fun top(text: String, style: Int): List<Xlsx.Cell> = listOf<Xlsx.Cell>(Xlsx.Cell.Str(text, style)) + List(cols - 1) { Xlsx.Cell.Str("", style) }
        val header = if (withTax) listOf("Invoice No", "Client", "Date", "Taxable Value", "Tax Amount", "Total")
        else listOf("Invoice No", "Client", "Date", "Total")
        val data = ArrayList<List<Xlsx.Cell>>()
        data.add(top(businessTitle, Xlsx.TITLE))
        data.add(top(reportTitle, Xlsx.SUBTITLE))
        data.add(emptyList())
        data.add(header.map { Xlsx.Cell.Str(it, Xlsx.HEADER) })
        for (r in rows) {
            val base = listOf<Xlsx.Cell>(Xlsx.Cell.Str(r.number, Xlsx.TEXT), Xlsx.Cell.Str(r.client, Xlsx.TEXT_WRAP), Xlsx.Cell.Str(r.date, Xlsx.TEXT))
            data.add(
                if (withTax) base + listOf(Xlsx.Cell.Num(r2(r.taxable), Xlsx.MONEY), Xlsx.Cell.Num(r2(r.tax), Xlsx.MONEY), Xlsx.Cell.Num(r2(r.total), Xlsx.MONEY))
                else base + Xlsx.Cell.Num(r2(r.total), Xlsx.MONEY)
            )
        }
        // A totals row under the bills
        val label = listOf<Xlsx.Cell>(Xlsx.Cell.Str("Total (${rows.size})", Xlsx.TEXT_BOLD), Xlsx.Cell.Str("", Xlsx.TEXT_BOLD), Xlsx.Cell.Str("", Xlsx.TEXT_BOLD))
        data.add(
            if (withTax) label + listOf(Xlsx.Cell.Num(r2(rows.sumOf { it.taxable }), Xlsx.MONEY_BOLD), Xlsx.Cell.Num(r2(rows.sumOf { it.tax }), Xlsx.MONEY_BOLD), Xlsx.Cell.Num(r2(rows.sumOf { it.total }), Xlsx.MONEY_BOLD))
            else label + Xlsx.Cell.Num(r2(rows.sumOf { it.total }), Xlsx.MONEY_BOLD)
        )
        val heights = HashMap<Int, Double>().apply {
            put(0, 45.0); put(1, 25.0); put(2, 15.0); put(3, 25.0)
            for (i in 4 until data.size) put(i, 35.0)
        }
        val widths = if (withTax) listOf(18.0, 45.0, 15.0, 15.0, 15.0, 15.0) else listOf(18.0, 45.0, 15.0, 18.0)
        return Xlsx.workbook(Xlsx.Sheet("Sales Report", data, widths, heights, listOf(intArrayOf(0, 0, 0, cols - 1), intArrayOf(1, 0, 1, cols - 1))))
    }

    private fun r2(v: Double) = kotlin.math.round(v * 100.0) / 100.0
}
