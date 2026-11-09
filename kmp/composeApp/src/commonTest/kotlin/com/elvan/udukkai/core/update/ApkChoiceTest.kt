package com.elvan.udukkai.core.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApkChoiceTest {
    // The order of the real v1.0.6 release: Factory first
    private val release = listOf("Udukkai-Factory.apk", "Udukkai-Textiles.apk")

    @Test
    fun textilesTakesItsOwnApkEvenWhenFactoryComesFirst() {
        assertEquals(1, pickApkIndex(release, "textiles"))
        assertEquals(1, pickApkIndex(release, "Textiles"))
    }

    @Test
    fun factoryTakesItsOwnApk() {
        assertEquals(0, pickApkIndex(release, "factory"))
        assertEquals(1, pickApkIndex(release.reversed(), "factory"))
    }

    @Test
    fun theOtherAppsApkIsNeverTaken() {
        assertNull(pickApkIndex(listOf("Udukkai-Factory.apk"), "textiles"))
        assertNull(pickApkIndex(listOf("Udukkai-Textiles.apk"), "factory"))
    }

    @Test
    fun aLoneApkThatNamesNoAppIsTaken() {
        assertEquals(0, pickApkIndex(listOf("Udukkai.apk"), "textiles"))
        assertEquals(1, pickApkIndex(listOf("notes.txt", "Udukkai.apk"), "factory"))
    }

    @Test
    fun twoApksThatNameNoAppAreNotGuessed() {
        assertNull(pickApkIndex(listOf("Udukkai-a.apk", "Udukkai-b.apk"), "textiles"))
    }

    @Test
    fun nonApkFilesAreIgnored() {
        assertNull(pickApkIndex(listOf("Udukkai-Textiles.apk.sha256", "source.zip"), "textiles"))
    }
}
