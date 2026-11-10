package com.elvan.udukkai.ui.components.shell

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsPaneHeaderTest {
    @Test fun fadesAwayBeforeTheFirstCardReachesTheTitle() {
        assertEquals(1f, settingsPaneHeaderAlpha(0, 0, 32f))
        assertEquals(0.5f, settingsPaneHeaderAlpha(0, 16, 32f))
        assertEquals(0f, settingsPaneHeaderAlpha(0, 32, 32f))
        assertEquals(0f, settingsPaneHeaderAlpha(0, 96, 32f))
    }

    @Test fun laterCardsCannotShowTheHeaderEvenWithZeroItemOffset() {
        assertEquals(0f, settingsPaneHeaderAlpha(1, 0, 32f))
        assertEquals(0f, settingsPaneHeaderAlpha(4, 8, 32f))
    }

    @Test fun overscrollDoesNotOverbrightenTheTitle() {
        assertEquals(1f, settingsPaneHeaderAlpha(0, -12, 32f))
    }
}
