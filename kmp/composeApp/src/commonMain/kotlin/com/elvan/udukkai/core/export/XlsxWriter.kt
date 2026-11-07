package com.elvan.udukkai.core.export

/**
 * A small Excel (.xlsx) writer with no libraries: a workbook is a zip of XML files, written here with the "stored"
 * (no compression) method, which every Excel, Sheets and LibreOffice accepts. It covers what the bill reports need:
 * text and number cells, a few fixed styles, merged cells, column widths, row heights and landscape fit-to-width.
 * shortcut: one sheet per workbook is written by [workbook]; add sheets there when a report needs a second tab.
 */
object Xlsx {
    // Style ids (positions in cellXfs of styles.xml below)
    const val TITLE = 1        // bold 16, centred, wrapped
    const val SUBTITLE = 2     // bold 13, centred
    const val HEADER = 3       // bold, grey fill, border, centred
    const val TEXT = 4         // border, centred vertically
    const val TEXT_WRAP = 5    // as TEXT, wrapped (two-language names)
    const val MONEY = 6        // border, rupee format
    const val MONEY_BOLD = 7   // totals row
    const val TEXT_BOLD = 8    // totals label

    sealed class Cell {
        data class Str(val text: String, val style: Int = 0) : Cell()
        data class Num(val value: Double, val style: Int = 0) : Cell()
    }

    class Sheet(
        val name: String,
        val rows: List<List<Cell>>,
        val columnWidths: List<Double>,
        val rowHeights: Map<Int, Double> = emptyMap(),
        /** Merged ranges as (firstRow, firstCol, lastRow, lastCol), 0-based. */
        val merges: List<IntArray> = emptyList()
    )

    private fun esc(s: String): String = buildString {
        for (c in s) when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            else -> if (c.code >= 32 || c == '\n' || c == '\t' || c == '\r') append(c)
        }
    }

    fun columnName(index: Int): String {
        var n = index
        val sb = StringBuilder()
        do {
            sb.append('A' + n % 26)
            n = n / 26 - 1
        } while (n >= 0)
        return sb.reverse().toString()
    }

    private fun num(d: Double): String = if (d % 1.0 == 0.0 && kotlin.math.abs(d) < 1e15) d.toLong().toString() else d.toString()

    private fun sheetXml(sheet: Sheet): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>")
        append("<cols>")
        sheet.columnWidths.forEachIndexed { i, w -> append("<col min=\"${i + 1}\" max=\"${i + 1}\" width=\"$w\" customWidth=\"1\"/>") }
        append("</cols><sheetData>")
        sheet.rows.forEachIndexed { r, row ->
            val h = sheet.rowHeights[r]
            append("<row r=\"${r + 1}\"")
            if (h != null) append(" ht=\"$h\" customHeight=\"1\"")
            append(">")
            row.forEachIndexed { c, cell ->
                val ref = columnName(c) + (r + 1)
                when (cell) {
                    is Cell.Str -> append("<c r=\"$ref\" s=\"${cell.style}\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${esc(cell.text)}</t></is></c>")
                    is Cell.Num -> append("<c r=\"$ref\" s=\"${cell.style}\"><v>${num(cell.value)}</v></c>")
                }
            }
            append("</row>")
        }
        append("</sheetData>")
        if (sheet.merges.isNotEmpty()) {
            append("<mergeCells count=\"${sheet.merges.size}\">")
            sheet.merges.forEach { m -> append("<mergeCell ref=\"${columnName(m[1])}${m[0] + 1}:${columnName(m[3])}${m[2] + 1}\"/>") }
            append("</mergeCells>")
        }
        append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.6\" bottom=\"0.6\" header=\"0.3\" footer=\"0.3\"/>")
        append("<pageSetup orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"0\"/>")
        append("</worksheet>")
    }

    private const val XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"

    private const val CONTENT_TYPES = XML +
        "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
        "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
        "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
        "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
        "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
        "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
        "</Types>"

    private const val ROOT_RELS = XML +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
        "</Relationships>"

    private const val WORKBOOK_RELS = XML +
        "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
        "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
        "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
        "</Relationships>"

    private const val STYLES = XML +
        "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
        "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"&quot;₹&quot;#,##0.00\"/></numFmts>" +
        "<fonts count=\"4\">" +
        "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"16\"/><color rgb=\"FF333333\"/><name val=\"Calibri\"/></font>" +
        "<font><b/><sz val=\"13\"/><color rgb=\"FF555555\"/><name val=\"Calibri\"/></font>" +
        "</fonts>" +
        "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>" +
        "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF1F5F9\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>" +
        "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>" +
        "<border><left style=\"thin\"><color auto=\"1\"/></left><right style=\"thin\"><color auto=\"1\"/></right><top style=\"thin\"><color auto=\"1\"/></top><bottom style=\"thin\"><color auto=\"1\"/></bottom><diagonal/></border></borders>" +
        "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
        "<cellXfs count=\"9\">" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
        "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\" wrapText=\"1\"/></xf>" +
        "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>" +
        "<xf numFmtId=\"164\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>" +
        "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>" +
        "</cellXfs></styleSheet>"

    private fun workbookXml(sheetName: String) = XML +
        "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
        "<sheets><sheet name=\"${esc(sheetName.take(31))}\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>"

    /** The bytes of a one-sheet .xlsx file. */
    fun workbook(sheet: Sheet): ByteArray = Zip.stored(
        listOf(
            "[Content_Types].xml" to CONTENT_TYPES,
            "_rels/.rels" to ROOT_RELS,
            "xl/workbook.xml" to workbookXml(sheet.name),
            "xl/_rels/workbook.xml.rels" to WORKBOOK_RELS,
            "xl/styles.xml" to STYLES,
            "xl/worksheets/sheet1.xml" to sheetXml(sheet)
        ).map { (n, c) -> n to c.encodeToByteArray() }
    )
}

/** A zip file made of uncompressed ("stored") entries. */
internal object Zip {
    private val crcTable = IntArray(256) { n ->
        var c = n
        repeat(8) { c = if (c and 1 != 0) (c ushr 1) xor 0xEDB88320.toInt() else c ushr 1 }
        c
    }

    fun crc32(data: ByteArray): Int {
        var c = -1
        for (b in data) c = crcTable[(c xor b.toInt()) and 0xFF] xor (c ushr 8)
        return c.inv()
    }

    private class Out {
        private var buf = ByteArray(8192)
        var size = 0
        private fun ensure(n: Int) { if (size + n > buf.size) buf = buf.copyOf(maxOf(buf.size * 2, size + n)) }
        fun u16(v: Int) { ensure(2); buf[size++] = v.toByte(); buf[size++] = (v shr 8).toByte() }
        fun u32(v: Int) { u16(v and 0xFFFF); u16((v ushr 16) and 0xFFFF) }
        fun bytes(b: ByteArray) { ensure(b.size); b.copyInto(buf, size); size += b.size }
        fun result() = buf.copyOf(size)
    }

    fun stored(entries: List<Pair<String, ByteArray>>): ByteArray {
        val out = Out()
        val offsets = ArrayList<Int>()
        val crcs = ArrayList<Int>()
        for ((name, data) in entries) {
            offsets.add(out.size)
            val crc = crc32(data)
            crcs.add(crc)
            val nameBytes = name.encodeToByteArray()
            out.u32(0x04034b50); out.u16(20); out.u16(0x0800); out.u16(0); out.u16(0); out.u16(0x0021)
            out.u32(crc); out.u32(data.size); out.u32(data.size); out.u16(nameBytes.size); out.u16(0)
            out.bytes(nameBytes); out.bytes(data)
        }
        val centralStart = out.size
        entries.forEachIndexed { i, (name, data) ->
            val nameBytes = name.encodeToByteArray()
            out.u32(0x02014b50); out.u16(20); out.u16(20); out.u16(0x0800); out.u16(0); out.u16(0); out.u16(0x0021)
            out.u32(crcs[i]); out.u32(data.size); out.u32(data.size); out.u16(nameBytes.size)
            out.u16(0); out.u16(0); out.u16(0); out.u16(0); out.u32(0); out.u32(offsets[i]); out.bytes(nameBytes)
        }
        val centralSize = out.size - centralStart
        out.u32(0x06054b50); out.u16(0); out.u16(0); out.u16(entries.size); out.u16(entries.size)
        out.u32(centralSize); out.u32(centralStart); out.u16(0)
        return out.result()
    }
}
