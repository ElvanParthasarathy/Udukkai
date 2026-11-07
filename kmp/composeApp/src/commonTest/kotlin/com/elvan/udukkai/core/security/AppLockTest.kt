package com.elvan.udukkai.core.security

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockTest {
    @Test
    fun appLockIsAlwaysSet() {
        assertTrue(AppLock.isSet())
    }

    @Test
    fun leavingTheAppDoesNotLockButScreenOffDoes() {
        AppLock.unlockedThisRun = true
        AppLock.left(1_000)
        AppLock.returned(600_000)
        assertTrue(AppLock.unlockedThisRun)
        AppLock.onScreenOff()
        assertFalse(AppLock.unlockedThisRun)
    }
}
