package com.elvan.udukkai.core.utils

import kotlin.math.abs

/**
 * Currency utilities for Indian Rupees (₹) formatting.
 * Formats numbers in standard Indian grouping: 1,00,000.00
 */
object CurrencyUtils {

    fun formatInr(amount: Double, showDecimals: Boolean = true): String {
        val isNegative = amount < 0
        // Whole paise by rounding: 8500.29 is stored as 8500.2899999..., and cutting that off would show .28
        val paise = kotlin.math.round(abs(amount) * 100).toLong()
        val integerPart = paise / 100
        val decimalPart = paise % 100

        val intStr = integerPart.toString()
        val result = StringBuilder()

        if (intStr.length <= 3) {
            result.append(intStr)
        } else {
            val last3 = intStr.substring(intStr.length - 3)
            var remaining = intStr.substring(0, intStr.length - 3)
            val chunks = mutableListOf<String>()
            while (remaining.length > 2) {
                chunks.add(remaining.substring(remaining.length - 2))
                remaining = remaining.substring(0, remaining.length - 2)
            }
            if (remaining.isNotEmpty()) {
                chunks.add(remaining)
            }
            chunks.reverse()
            result.append(chunks.joinToString(","))
            result.append(",")
            result.append(last3)
        }

        val formatted = if (showDecimals) {
            val decStr = decimalPart.toString().padStart(2, '0')
            "$result.$decStr"
        } else {
            result.toString()
        }

        return if (isNegative) "-₹$formatted" else "₹$formatted"
    }
}
