package com.elvan.udukkai.core.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The PIN is kept hashed, wrong tries slow down, and removing it removes the lock. */
class AppLockTest {
    @Test
    fun setVerifyWrongLockoutAndRemove() {
        AppLock.remove()
        try {
            assertFalse(AppLock.isSet())
            AppLock.set("123456")
            assertTrue(AppLock.isSet())
            assertTrue(AppLock.verify("123456"))
            assertFalse(AppLock.verify("000000"))

            // Five wrong tries in a row lock it, even for the right PIN, until the wait is over
            val t0 = 1_000_000L
            repeat(3) { assertFalse(AppLock.verify("111111", t0)) }
            assertEquals(0, AppLock.secondsLocked(t0))
            assertFalse(AppLock.verify("111111", t0))
            assertEquals(30, AppLock.secondsLocked(t0))
            assertFalse(AppLock.verify("123456", t0 + 1_000))
            assertTrue(AppLock.verify("123456", t0 + 31_000))
            assertEquals(0, AppLock.secondsLocked(t0 + 31_000))
        } finally {
            AppLock.remove()
        }
        assertFalse(AppLock.isSet())
    }

    @Test
    fun onlySixDigitsAreAccepted() {
        try {
            AppLock.set("12ab56")
            error("accepted")
        } catch (_: IllegalArgumentException) {
        }
        assertFalse(AppLock.isSet())
    }
}
