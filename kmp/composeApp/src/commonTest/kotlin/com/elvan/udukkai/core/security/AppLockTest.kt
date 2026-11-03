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
}
