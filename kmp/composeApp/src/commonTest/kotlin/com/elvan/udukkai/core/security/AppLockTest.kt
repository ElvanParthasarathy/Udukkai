package com.elvan.udukkai.core.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The PIN is kept hashed, 6 or 8 digits, wrong tries slow down, and the app asks again after a minute away. */
class AppLockTest {
    @Test
    fun setVerifyWrongAndLockout() {
        AppLock.clearForTests()
        try {
            assertFalse(AppLock.isSet())
            AppLock.set("123456")
            assertTrue(AppLock.isSet())
            assertEquals(6, AppLock.length())
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
            AppLock.clearForTests()
        }
    }

    @Test
    fun anEightDigitPinWorksAndOtherLengthsDoNot() {
        AppLock.clearForTests()
        try {
            AppLock.set("12345678")
            assertEquals(8, AppLock.length())
            assertTrue(AppLock.verify("12345678"))
            assertFalse(AppLock.verify("123456"))
            for (bad in listOf("1234", "1234567", "123456789", "12ab56", "")) {
                try {
                    AppLock.set(bad)
                    error("accepted $bad")
                } catch (_: IllegalArgumentException) {
                }
            }
        } finally {
            AppLock.clearForTests()
        }
    }

    @Test
    fun locksAgainOnlyAfterAMinuteAway() {
        AppLock.unlockedThisRun = true
        AppLock.left(1_000)
        AppLock.returned(31_000)
        assertTrue(AppLock.unlockedThisRun)
        AppLock.left(1_000)
        AppLock.returned(62_000)
        assertFalse(AppLock.unlockedThisRun)
    }
}
