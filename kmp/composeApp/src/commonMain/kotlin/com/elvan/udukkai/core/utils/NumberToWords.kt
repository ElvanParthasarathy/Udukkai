package com.elvan.udukkai.core.utils

import kotlin.math.roundToLong

/**
 * Number to Words Converter for Udukkai.
 * Provides high-precision Indian currency representation in Pure Tamil (with correct Sandhi joins)
 * and English (with Indian system: Crores, Lakhs, Thousands).
 */
object NumberToWords {

    // ─────────────────────────────────────────────────────────────────────────
    // TAMIL IMPLEMENTATION
    // ─────────────────────────────────────────────────────────────────────────

    private val onesTa = listOf(
        "", "ஒன்று", "இரண்டு", "மூன்று", "நான்கு",
        "ஐந்து", "ஆறு", "ஏழு", "எட்டு", "ஒன்பது",
        "பத்து", "பதினொன்று", "பன்னிரண்டு", "பதிமூன்று", "பதினான்கு",
        "பதினைந்து", "பதினாறு", "பதினேழு", "பதினெட்டு", "பத்தொன்பது"
    )

    private val tensBaseTa = listOf(
        "", "", "இருபது", "முப்பது", "நாற்பது",
        "ஐம்பது", "அறுபது", "எழுபது", "எண்பது", "தொண்ணூறு"
    )

    private val tensObliqueTa = listOf(
        "", "", "இருபத்து", "முப்பத்து", "நாற்பத்து",
        "ஐம்பத்து", "அறுபத்து", "எழுபத்து", "எண்பத்து", "தொண்ணூற்று"
    )

    private val hundredsBaseTa = mapOf(
        1 to "நூறு",
        2 to "இருநூறு",
        3 to "முந்நூறு",
        4 to "நானூறு",
        5 to "ஐந்நூறு",
        6 to "அறுநூறு",
        7 to "எழுநூறு",
        8 to "எண்ணூறு",
        9 to "தொள்ளாயிரம்"
    )

    private val hundredsObliqueTa = mapOf(
        1 to "நூற்று",
        2 to "இருநூற்று",
        3 to "முந்நூற்று",
        4 to "நானூற்று",
        5 to "ஐந்நூற்று",
        6 to "அறுநூற்று",
        7 to "எழுநூற்று",
        8 to "எண்ணூற்று",
        9 to "தொள்ளாயிரத்து"
    )

    private fun joinTamil(w1: String, w2: String): String {
        if (w1.isEmpty()) return w2
        if (w2.isEmpty()) return w1
        return "$w1 $w2"
    }

    private fun convertUnder100Ta(n: Long): String {
        if (n < 20) return onesTa[n.toInt()]
        val tensDigit = (n / 10).toInt()
        val onesDigit = (n % 10).toInt()
        if (onesDigit == 0) return tensBaseTa[tensDigit]
        return joinTamil(tensObliqueTa[tensDigit], onesTa[onesDigit])
    }

    private fun convertUnder1000Ta(n: Long): String {
        if (n < 100) return convertUnder100Ta(n)
        val hundredsDigit = (n / 100).toInt()
        val remainder = n % 100
        if (remainder == 0L) return hundredsBaseTa[hundredsDigit] ?: ""
        val oblique = hundredsObliqueTa[hundredsDigit] ?: ""
        return joinTamil(oblique, convertUnder100Ta(remainder))
    }

    private fun convertUnder100000Ta(n: Long): String {
        if (n < 1000) return convertUnder1000Ta(n)
        val thousands = n / 1000
        val remainder = n % 1000
        val thousandsStr = convertUnder100Ta(thousands)
        val suffix = if (remainder == 0L) "ஆயிரம்" else "ஆயிரத்து"
        val thousandPart = joinTamil(thousandsStr, suffix)
        if (remainder == 0L) return thousandPart
        return "$thousandPart ${convertUnder1000Ta(remainder)}"
    }

    private fun convertUnderCroreTa(n: Long): String {
        if (n < 100000) return convertUnder100000Ta(n)
        val lakhs = n / 100000
        val remainder = n % 100000
        val lakhPart = if (lakhs == 1L) {
            if (remainder == 0L) "ஒரு லட்சம்" else "ஒரு லட்சத்து"
        } else {
            val lakhsStr = convertUnder100Ta(lakhs)
            val suffix = if (remainder == 0L) "லட்சம்" else "லட்சத்து"
            joinTamil(lakhsStr, suffix)
        }
        if (remainder == 0L) return lakhPart
        return "$lakhPart ${convertUnder100000Ta(remainder)}"
    }

    private fun convertIntegerTa(n: Long): String {
        if (n == 0L) return "சுழியம்"
        if (n < 10000000) return convertUnderCroreTa(n)
        val crores = n / 10000000
        val remainder = n % 10000000
        val crorePart = if (crores == 1L) {
            if (remainder == 0L) "ஒரு கோடி" else "ஒரு கோடியே"
        } else {
            val croreStr = convertUnderCroreTa(crores)
            val suffix = if (remainder == 0L) "கோடி" else "கோடியே"
            joinTamil(croreStr, suffix)
        }
        if (remainder == 0L) return crorePart
        return "$crorePart ${convertUnderCroreTa(remainder)}"
    }

    /**
     * Converts a numeric currency amount into Pure Tamil words.
     * E.g. 15400.0 -> "ரூபாய் பதினைந்தாயிரத்து நானூறு மட்டும்"
     */
    fun toTamil(amount: Double): String {
        if (amount == 0.0) return "சுழியம் ரூபாய்"
        val totalPaise = (amount * 100).roundToLong()
        val rupees = totalPaise / 100
        val paise = totalPaise % 100

        val rupeeWords = convertIntegerTa(rupees)
        val result = StringBuilder("ரூபாய் ").append(rupeeWords)

        if (paise > 0) {
            result.append(" மற்றும் ").append(convertIntegerTa(paise)).append(" காசுகள்")
        }
        result.append(" மட்டும்")
        return result.toString().trim()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENGLISH IMPLEMENTATION (Indian Numbering System)
    // ─────────────────────────────────────────────────────────────────────────

    private val onesEn = listOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tensEn = listOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    private fun convertUnder1000En(n: Long): String {
        var num = n
        val parts = mutableListOf<String>()
        if (num >= 100) {
            parts.add("${onesEn[(num / 100).toInt()]} Hundred")
            num %= 100
            if (num > 0) parts.add("and")
        }
        if (num >= 20) {
            val t = tensEn[(num / 10).toInt()]
            val o = onesEn[(num % 10).toInt()]
            parts.add(if (o.isNotEmpty()) "$t $o" else t)
        } else if (num > 0) {
            parts.add(onesEn[num.toInt()])
        }
        return parts.joinToString(" ")
    }

    private fun convertIntegerEn(n: Long): String {
        if (n == 0L) return "Zero"
        var num = n
        val parts = mutableListOf<String>()

        val crores = num / 10000000
        if (crores > 0) {
            parts.add("${convertIntegerEn(crores)} Crore")
            num %= 10000000
        }

        val lakhs = num / 100000
        if (lakhs > 0) {
            parts.add("${convertUnder1000En(lakhs)} Lakh")
            num %= 100000
        }

        val thousands = num / 1000
        if (thousands > 0) {
            parts.add("${convertUnder1000En(thousands)} Thousand")
            num %= 1000
        }

        if (num > 0) {
            parts.add(convertUnder1000En(num))
        }

        return parts.joinToString(" ").trim()
    }

    /**
     * Converts a numeric currency amount into English words (Indian system).
     * E.g. 15400.0 -> "Rupees Fifteen Thousand Four Hundred Only"
     */
    fun toEnglish(amount: Double): String {
        if (amount == 0.0) return "Zero Rupees Only"
        val totalPaise = (amount * 100).roundToLong()
        val rupees = totalPaise / 100
        val paise = totalPaise % 100

        val rupeeWords = convertIntegerEn(rupees)
        val result = StringBuilder("Rupees ").append(rupeeWords)

        if (paise > 0) {
            result.append(" and ").append(convertUnder1000En(paise)).append(" Paise")
        }
        result.append(" Only")
        return result.toString().trim()
    }

    /**
     * Formats bilingual words tuple based on language profile.
     */
    fun formatBilingual(
        amount: Double,
        primaryLang: String,
        secondaryLang: String,
        isBilingual: Boolean
    ): Pair<String, String?> {
        val primary = if (primaryLang == "ta") toTamil(amount) else toEnglish(amount)
        val secondary = if (isBilingual && primaryLang != secondaryLang) {
            if (secondaryLang == "ta") toTamil(amount) else toEnglish(amount)
        } else null
        return Pair(primary, secondary)
    }
}
