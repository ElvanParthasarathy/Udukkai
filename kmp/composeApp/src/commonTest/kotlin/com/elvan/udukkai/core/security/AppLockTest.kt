package com.elvan.udukkai.core.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockTest {
    @Test
    fun enableAndDisableAppLock() {
        AppLock.clearForTests()
        try {
            assertFalse(AppLock.isSet())
            AppLock.setEnabled(true)
            assertTrue(AppLock.isSet())
            assertTrue(AppLock.unlockedThisRun)

            AppLock.setEnabled(false)
            assertFalse(AppLock.isSet())
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
