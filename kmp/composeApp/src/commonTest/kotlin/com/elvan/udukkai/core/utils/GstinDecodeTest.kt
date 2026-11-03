package com.elvan.udukkai.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** A valid GSTIN gives its state and PAN without any lookup; a wrong one gives nothing. */
class GstinDecodeTest {
    @Test
    fun validGstinGivesStateAndPan() {
        assertEquals("Maharashtra", BillRules.gstinState("27AAPFU0939F1ZV"))
        assertEquals("AAPFU0939F", BillRules.gstinPan(" 27aapfu0939f1zv "))
    }

    @Test
    fun wrongGstinGivesNothing() {
        assertNull(BillRules.gstinState("27AAPFU0939F1ZX"))
        assertNull(BillRules.gstinPan("27AAPFU0939F"))
        assertNull(BillRules.gstinState(""))
    }
}
