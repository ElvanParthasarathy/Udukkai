package com.elvan.udukkai.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily

/**
 * Multiplatform expect declarations for application font families.
 */
expect val ElvanSansFontFamily: FontFamily
expect val NavilSansFontFamily: FontFamily

val LocalAppFontFamily = compositionLocalOf { NavilSansFontFamily }

/**
 * Prevents broken/stylized composite ligatures (such as 'fi', 'fl', 'ff')
 * by inserting a Zero-Width Non-Joiner (ZWNJ \u200C).
 */
fun String.preventBrokenLigatures(): String {
    if (isEmpty()) return this
    return this
        .replace("fi", "f\u200Ci")
        .replace("fI", "f\u200CI")
        .replace("fl", "f\u200Cl")
        .replace("ff", "f\u200Cf")
        .replace("fj", "f\u200Cj")
        .replace("fk", "f\u200Ck")
        .replace("fh", "f\u200Ch")
        .replace("ft", "f\u200Ct")
}
