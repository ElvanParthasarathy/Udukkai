package com.elvan.udukkai.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyUtilsTest {
    @Test
    fun amounts_show_the_paise_they_hold() {
        // 8500.29 and 0.29 are stored a hair below the value, and cutting that off used to show .28
        assertEquals("₹8,500.29", CurrencyUtils.formatInr(8500.29))
        assertEquals("₹0.29", CurrencyUtils.formatInr(0.29))
        assertEquals("₹1,14,765.58", CurrencyUtils.formatInr(114765.58))
        assertEquals("₹97,545.00", CurrencyUtils.formatInr(97545.0))
    }

    @Test
    fun amounts_use_indian_grouping_with_and_without_paise() {
        assertEquals("₹5,25,43,303.00", CurrencyUtils.formatInr(52543303.0))
        assertEquals("₹8,500", CurrencyUtils.formatInr(8500.0, showDecimals = false))
        assertEquals("-₹1,200.50", CurrencyUtils.formatInr(-1200.5))
        assertEquals("₹0.00", CurrencyUtils.formatInr(0.0))
    }
}
