package com.elvan.udukkai.core.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockTest {
    @Test
    fun theSwitchIsKept() {
        AppLock.setEnabled(false)
        assertFalse(AppLock.isEnabled())
        AppLock.setEnabled(true)
        assertTrue(AppLock.isEnabled())
        AppLock.setEnabled(false)
        assertFalse(AppLock.isEnabled())
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
