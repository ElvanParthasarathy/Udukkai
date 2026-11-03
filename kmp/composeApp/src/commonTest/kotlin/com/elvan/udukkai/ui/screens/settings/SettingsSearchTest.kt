package com.elvan.udukkai.ui.screens.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The settings search finds a page by words in either language, by keywords, and by several words together. */
class SettingsSearchTest {
    private val entries = settingsEntries(SettingsRoute.TextilesIdentity)

    private fun first(q: String) = searchSettings(entries, q).firstOrNull()?.route

    @Test
    fun findsByEnglishWordsAndKeywords() {
        assertEquals(SettingsRoute.StorageBackup, first("backup"))
        assertEquals(SettingsRoute.AppLock, first("fingerprint"))
        assertEquals(SettingsRoute.TextilesIdentity, first("logo"))
        assertEquals(SettingsRoute.Address, first("pincode"))
        assertEquals(SettingsRoute.Bank, first("ifsc"))
    }

    @Test
    fun findsByTamilTitleAndIgnoresCase() {
        assertEquals(SettingsRoute.Address, first("முகவரி"))
        assertEquals(SettingsRoute.StorageBackup, first("BACKUP"))
    }

    @Test
    fun everyWordMustMatchAndNothingMatchesNonsense() {
        assertTrue(searchSettings(entries, "backup restore").any { it.route == SettingsRoute.StorageBackup })
        assertTrue(searchSettings(entries, "backup zzzz").isEmpty())
        assertTrue(searchSettings(entries, "   ").isEmpty())
    }
}
