package com.elvan.udukkai.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ElvanColorPickerTest {
    @Test
    fun hexRoundTrips() {
        for (hex in listOf("#388e3c", "#000000", "#ffffff", "#ff0080", "#0a0b0c")) {
            assertEquals(hex, colorToHex(parseHexColor(hex)!!))
        }
    }

    @Test
    fun hexAcceptsShortFormsAndTidiesInput() {
        assertEquals("#aabbcc", colorToHex(parseHexColor("abc")!!))
        assertEquals("#ff0000", colorToHex(parseHexColor(" #F00 ")!!))
        assertEquals("#336699", colorToHex(parseHexColor("336699")!!))
    }

    @Test
    fun hexRejectsAnythingElse() {
        for (bad in listOf("", "#", "#12", "#1234", "#12345", "#1234567", "#gggggg", "#12 456", "red")) {
            assertNull(parseHexColor(bad), "should reject '$bad'")
        }
    }

    @Test
    fun hsvRoundTripsThroughTheColour() {
        for (c in listOf(Color(0xFFFF0000), Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF388E3C), Color(0xFF808080), Color(0xFFFFD54F))) {
            val (h, s, v) = rgbToHsv(c)
            assertEquals(colorToHex(c), colorToHex(Color.hsv(h, s, v)))
        }
    }

    @Test
    fun primariesHaveTheRightHue() {
        assertEquals(0f, rgbToHsv(Color(0xFFFF0000)).first)
        assertEquals(120f, rgbToHsv(Color(0xFF00FF00)).first)
        assertEquals(240f, rgbToHsv(Color(0xFF0000FF)).first)
        assertEquals(0f, rgbToHsv(Color(0xFF000000)).second) // black has no saturation
        assertEquals(0f, rgbToHsv(Color(0xFF808080)).second) // nor grey
    }

    @Test
    fun lightnessSwatchesRunDarkToLight() {
        val dark = hslColor(200f, 1f, 0.12f)
        val light = hslColor(200f, 1f, 0.62f)
        assertTrue(light.red + light.green + light.blue > dark.red + dark.green + dark.blue)
        assertTrue(abs(hslColor(0f, 0f, 0.5f).red - 0.5f) < 0.01f) // zero saturation is a pure grey
    }

    @Test
    fun recentColorsKeepNewestFirstWithoutDuplicates() {
        var r = emptyList<String>()
        for (hex in listOf("#111111", "#222222", "#333333", "#444444", "#555555", "#666666")) r = pushRecentColor(r, hex)
        assertEquals(listOf("#666666", "#555555", "#444444", "#333333", "#222222"), r) // capped at five
        assertEquals(listOf("#333333", "#666666", "#555555", "#444444", "#222222"), pushRecentColor(r, "#333333")) // reused moves to the front
        assertEquals(listOf("#aabbcc", "#666666"), pushRecentColor(listOf("#666666"), "#AABBCC")) // stored lower case
        assertEquals(listOf("#666666"), pushRecentColor(listOf("#666666"), "#666666".uppercase()))
    }

    @Test
    fun savedRecentListIgnoresDamage() {
        assertEquals(listOf("#aabbcc", "#112233"), parseRecentColors("#AABBCC, #112233"))
        assertEquals(emptyList(), parseRecentColors(null))
        assertEquals(emptyList(), parseRecentColors(""))
        assertEquals(listOf("#112233"), parseRecentColors("#zzzzzz,#12,,#112233,112233"))
        assertNotNull(parseHexColor(parseRecentColors("#388E3C").single()))
    }
}
