package com.elvan.udukkai.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DragSelectKeyTest {
    @Test
    fun onlyRecordKeysGiveAnId() {
        assertEquals(12L, recordIdOfKey("item_12"))
        assertEquals(1794063115440000L, recordIdOfKey("invoice_1794063115440000"))
        assertEquals(7L, recordIdOfKey("receipt_7"))
        assertEquals(3L, recordIdOfKey("cust_3"))
        assertNull(recordIdOfKey("date_hdr_inv_20261005"))
        assertNull(recordIdOfKey("item_top_spacer"))
        assertNull(recordIdOfKey("item_pair_12"))
    }
}
