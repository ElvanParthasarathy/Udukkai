package com.elvan.udukkai.core.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The PIN is kept hashed, 4 or 6 digits, wrong tries slow down, and the app asks again after a minute away. */
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
    fun aFourDigitPinWorksAndOtherLengthsDoNot() {
        AppLock.clearForTests()
        try {
            AppLock.set("1234")
            assertEquals(4, AppLock.length())
            assertTrue(AppLock.verify("1234"))
            assertFalse(AppLock.verify("123456"))
            for (bad in listOf("123", "12345", "1234567", "12345678", "12ab", "")) {
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
