package com.elvan.udukkai.core.export

/** One line of a bill, already split into tax parts. */
data class GstLine(
    val hsn: String,
    val description: String,
    val unit: String,
    val quantity: Double,
    val rate: Double,
    val taxable: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double
)

/** One tax invoice as the GST summaries need it. */
data class GstBill(
    val number: String,
    val date: String,
    val gstin: String,
    val customer: String,
    val total: Double,
    val placeOfSupply: String,
    val interState: Boolean,
    val taxable: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val lines: List<GstLine>
)

/**
 * The GSTR-1 B2B table (4A) and the HSN summary (table 12), with the same columns as the React app's exports, as
 * Excel sheets in the same look as the sales report.
 */
object GstrExcel {
    private fun r2(v: Double) = kotlin.math.round(v * 100.0) / 100.0
    private fun s(text: String, style: Int = Xlsx.TEXT) = Xlsx.Cell.Str(text, style)
    private fun m(v: Double, style: Int = Xlsx.MONEY) = Xlsx.Cell.Num(r2(v), style)

    /** The unit codes GSTR-1 uses; anything else is "OTH". */
    fun uqc(unit: String): String = when (unit.trim().lowercase()) {
        "nos", "no", "pcs", "piece", "pieces", "number", "" -> "NOS"
        "kg", "kgs", "kilogram" -> "KGS"
        "g", "gm", "gms", "gram" -> "GMS"
        "m", "mtr", "mtrs", "meter", "metre" -> "MTR"
        "l", "ltr", "litre", "liter" -> "LTR"
        "box", "boxes" -> "BOX"
        "set", "sets" -> "SET"
        "pair", "pairs" -> "PRS"
        else -> "OTH"
    }

    private fun sheet(name: String, business: String, title: String, header: List<String>, rows: List<List<Xlsx.Cell>>, widths: List<Double>): ByteArray {
        val cols = header.size
        fun top(text: String, style: Int): List<Xlsx.Cell> = listOf<Xlsx.Cell>(Xlsx.Cell.Str(text, style)) + List(cols - 1) { Xlsx.Cell.Str("", style) }
        val data = ArrayList<List<Xlsx.Cell>>()
        data.add(top(business, Xlsx.TITLE))
        data.add(top(title, Xlsx.SUBTITLE))
        data.add(emptyList())
        data.add(header.map { Xlsx.Cell.Str(it, Xlsx.HEADER) })
        data.addAll(rows)
        val heights = HashMap<Int, Double>().apply { put(0, 45.0); put(1, 25.0); put(2, 15.0); put(3, 25.0); for (i in 4 until data.size) put(i, 28.0) }
        return Xlsx.workbook(Xlsx.Sheet(name, data, widths, heights, listOf(intArrayOf(0, 0, 0, cols - 1), intArrayOf(1, 0, 1, cols - 1))))
    }

    fun b2b(business: String, title: String, bills: List<GstBill>): ByteArray {
        val header = listOf("GSTIN/UIN", "Receiver Name", "Invoice Number", "Invoice Date", "Invoice Value", "Place of Supply", "Reverse Charge", "Invoice Type", "Supply Type", "Taxable Value", "CGST Amount", "SGST Amount", "IGST Amount")
        val rows = ArrayList<List<Xlsx.Cell>>()
        for (b in bills) rows.add(listOf(
            s(b.gstin), s(b.customer, Xlsx.TEXT_WRAP), s(b.number), s(b.date), m(b.total), s(b.placeOfSupply), s("N"), s("Regular"),
            s(if (b.interState) "Inter State" else "Intra State"), m(b.taxable), m(if (b.interState) 0.0 else b.cgst), m(if (b.interState) 0.0 else b.sgst), m(if (b.interState) b.igst else 0.0)
        ))
        rows.add(listOf(s("Total (${bills.size})", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), m(bills.sumOf { it.total }, Xlsx.MONEY_BOLD),
            s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), m(bills.sumOf { it.taxable }, Xlsx.MONEY_BOLD),
            m(bills.sumOf { if (it.interState) 0.0 else it.cgst }, Xlsx.MONEY_BOLD), m(bills.sumOf { if (it.interState) 0.0 else it.sgst }, Xlsx.MONEY_BOLD), m(bills.sumOf { if (it.interState) it.igst else 0.0 }, Xlsx.MONEY_BOLD)))
        return sheet("GSTR1 B2B", business, title, header, rows, listOf(20.0, 34.0, 16.0, 13.0, 15.0, 18.0, 10.0, 11.0, 12.0, 15.0, 14.0, 14.0, 14.0))
    }

    private data class HsnKey(val hsn: String, val uqc: String, val rate: Double)
    private class HsnSum(val description: String) { var qty = 0.0; var taxable = 0.0; var cgst = 0.0; var sgst = 0.0; var igst = 0.0 }

    fun hsn(business: String, title: String, bills: List<GstBill>): ByteArray {
        val header = listOf("HSN", "Description", "UQC", "Total Quantity", "Rate %", "Taxable Value", "IGST Amount", "CGST Amount", "SGST Amount", "Cess Amount", "Total Value")
        val sums = sortedMapOf<String, Pair<HsnKey, HsnSum>>()
        for (b in bills) for (l in b.lines) {
            val key = HsnKey(l.hsn.ifBlank { "N/A" }, uqc(l.unit), l.rate)
            val sortKey = key.hsn + "|" + key.uqc + "|" + key.rate
            val sum = sums.getOrPut(sortKey) { key to HsnSum(l.description) }.second
            sum.qty += l.quantity; sum.taxable += l.taxable; sum.cgst += l.cgst; sum.sgst += l.sgst; sum.igst += l.igst
        }
        val rows = ArrayList<List<Xlsx.Cell>>()
        for ((key, sum) in sums.values) rows.add(listOf(
            s(key.hsn), s(sum.description, Xlsx.TEXT_WRAP), s(key.uqc), Xlsx.Cell.Num(r2(sum.qty), Xlsx.TEXT), Xlsx.Cell.Num(key.rate, Xlsx.TEXT),
            m(sum.taxable), m(sum.igst), m(sum.cgst), m(sum.sgst), m(0.0), m(sum.taxable + sum.igst + sum.cgst + sum.sgst)
        ))
        val all = sums.values.map { it.second }
        rows.add(listOf(s("Total", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD), s("", Xlsx.TEXT_BOLD),
            m(all.sumOf { it.taxable }, Xlsx.MONEY_BOLD), m(all.sumOf { it.igst }, Xlsx.MONEY_BOLD), m(all.sumOf { it.cgst }, Xlsx.MONEY_BOLD), m(all.sumOf { it.sgst }, Xlsx.MONEY_BOLD),
            m(0.0, Xlsx.MONEY_BOLD), m(all.sumOf { it.taxable + it.igst + it.cgst + it.sgst }, Xlsx.MONEY_BOLD)))
        return sheet("HSN Summary", business, title, header, rows, listOf(14.0, 34.0, 8.0, 14.0, 9.0, 15.0, 14.0, 14.0, 14.0, 12.0, 16.0))
    }
}
