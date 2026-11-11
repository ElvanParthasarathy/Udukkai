package com.elvan.udukkai.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class DragSelectKeyTest {
    @Test
    fun onlyRecordKeysGiveIds() {
        assertEquals(listOf(12L), recordIdsOfKey("item_12"))
        assertEquals(listOf(1794063115440000L), recordIdsOfKey("invoice_1794063115440000"))
        assertEquals(listOf(7L), recordIdsOfKey("receipt_7"))
        assertEquals(listOf(3L), recordIdsOfKey("cust_3"))
        assertEquals(emptyList(), recordIdsOfKey("date_hdr_inv_20261005"))
        assertEquals(emptyList(), recordIdsOfKey("item_top_spacer"))
        assertEquals(emptyList(), recordIdsOfKey("item_measure_type_pill_shifter"))
    }

    @Test
    fun landscapeRowKeysGiveBothIdsLeftToRight() {
        assertEquals(listOf(12L, 13L), recordIdsOfKey("item_pair_12_13"))
        assertEquals(listOf(5L), recordIdsOfKey("cust_pair_5"))
        assertEquals(listOf(9L, 4L), recordIdsOfKey("inv_pair_9_4"))
        assertEquals(listOf(8L, 2L), recordIdsOfKey("rec_pair_8_2"))
    }

    @Test
    fun columnFollowsTheFingerAcrossTheRow() {
        assertEquals(0, columnAt(1, 900f, 1000f))
        assertEquals(0, columnAt(2, 499f, 1000f))
        assertEquals(1, columnAt(2, 501f, 1000f))
        assertEquals(1, columnAt(2, 5000f, 1000f)) // finger dragged past the edge
        assertEquals(0, columnAt(2, -20f, 1000f))
    }

    private val portrait = mapOf(0 to emptyList(), 1 to listOf(10L), 2 to listOf(20L), 3 to listOf(30L), 4 to listOf(40L))
    private val landscape = mapOf(1 to listOf(10L, 11L), 2 to listOf(20L, 21L), 3 to listOf(30L))

    @Test
    fun rangeRunsFromAnchorToFingerEitherWay() {
        assertEquals(setOf(10L, 20L, 30L), idsBetween(portrait, 1, 0, 3, 0))
        assertEquals(setOf(10L, 20L, 30L), idsBetween(portrait, 3, 0, 1, 0))
        assertEquals(setOf(20L), idsBetween(portrait, 2, 0, 2, 0))
        assertEquals(setOf(10L), idsBetween(portrait, 0, 0, 1, 0)) // header row adds nothing
    }

    @Test
    fun reverseDragShrinksTheRange() {
        val down = idsBetween(portrait, 2, 0, 4, 0)
        val back = idsBetween(portrait, 2, 0, 3, 0)
        assertEquals(setOf(40L), down - back)
    }

    @Test
    fun landscapeRangeIsReadingOrder() {
        assertEquals(setOf(11L, 20L), idsBetween(landscape, 1, 1, 2, 0))
        assertEquals(setOf(11L, 20L, 21L, 30L), idsBetween(landscape, 1, 1, 3, 0))
        assertEquals(setOf(10L, 11L, 20L), idsBetween(landscape, 2, 0, 1, 0))
    }
}
