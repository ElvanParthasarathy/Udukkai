package com.elvan.udukkai.core.utils

/** Rules for Indian GST bills: GSTIN format and check digit, invoice number format, plain numbers. */
object BillRules {
    private const val GSTIN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val GSTIN_FORMAT = Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]")

    /** 15 characters: state code, PAN, entity number, Z and a check character. */
    fun isValidGstin(value: String): Boolean {
        val g = value.trim().uppercase()
        if (!GSTIN_FORMAT.matches(g)) return false
        val state = g.substring(0, 2).toInt()
        if (state !in 1..38 && state != 97 && state != 99) return false
        var total = 0
        for (i in 0 until 14) {
            val v = GSTIN_CHARS.indexOf(g[i]) * (if (i % 2 == 0) 1 else 2)
            total += v / 36 + v % 36
        }
        return GSTIN_CHARS[(36 - total % 36) % 36] == g[14]
    }

    private val PAN_FORMAT = Regex("[A-Z]{5}[0-9]{4}[A-Z]")

    /** 10 characters: five letters, four digits, one letter. */
    fun isValidPan(value: String): Boolean = PAN_FORMAT.matches(value.trim().uppercase())

    /** GST rule: at most 16 characters, only letters, digits, "/" and "-". */
    fun isValidInvoiceNumber(value: String): Boolean {
        val v = value.trim()
        return v.length in 1..16 && v.all { it in '0'..'9' || it in 'A'..'Z' || it in 'a'..'z' || it == '/' || it == '-' }
    }

    /** A number as plain digits: `1.0E7` would read back as `1.0` in the stored JSON. */
    fun plainNumber(d: Double): String {
        if (d.isNaN() || d.isInfinite()) return "0"
        val s = d.toString()
        if (!s.contains('E') && !s.contains('e')) return s
        val parts = s.lowercase().split('e')
        val exp = parts[1].toInt()
        val negative = parts[0].startsWith("-")
        val mantissa = parts[0].removePrefix("-")
        val dot = mantissa.indexOf('.')
        val digits = mantissa.replace(".", "")
        val intLen = (if (dot < 0) mantissa.length else dot) + exp
        val plain = when {
            intLen <= 0 -> "0." + "0".repeat(-intLen) + digits
            intLen >= digits.length -> digits + "0".repeat(intLen - digits.length) + ".0"
            else -> digits.substring(0, intLen) + "." + digits.substring(intLen)
        }
        return (if (negative) "-" else "") + plain
    }
}
