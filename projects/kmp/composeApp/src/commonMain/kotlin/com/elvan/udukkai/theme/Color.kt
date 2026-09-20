package com.elvan.udukkai.theme

import androidx.compose.ui.graphics.Color

// Light Theme (Pure Monochrome)
val PrimaryLight = Color(0xFF000000)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE5E5EA)
val OnPrimaryContainerLight = Color(0xFF000000)
val BackgroundLight = Color(0xFFF7F7F7)
val OnBackgroundLight = Color(0xFF1D1D1F)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1D1D1F)
val ErrorLight = Color(0xFFBA1A1A)

// Dark Theme (Pure Monochrome AMOLED)
val PrimaryDark = Color(0xFFFFFFFF)
val OnPrimaryDark = Color(0xFF000000)
val PrimaryContainerDark = Color(0xFF2C2C2E)
val OnPrimaryContainerDark = Color(0xFFFFFFFF)
val BackgroundDark = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF111111)
val OnSurfaceDark = Color(0xFFFFFFFF)
val ErrorDark = Color(0xFFFFB4AB)

/**
 * Archived Color Palettes for Elvan Billing (Silk Mode & Coolie Mode).
 * Preserved for future reference or reactivation.
 */
object ElvanBillingColorArchive {
    object Coolie {
        val Primary = Color(0xFF087F8C)

        object Dark {
            val Accent = Color(0xFF087F8C)
            val AccentBright = Color(0xFF4DD0DC)
            val AccentDark = Color(0xFF066A75)
            val AccentContainer = Color(0xFF123C40)
            val SecondaryAccent = Color(0xFF6B3F68)
        }

        object Light {
            val Accent = Color(0xFF087F8C)
            val AccentBright = Color(0xFF4DD0DC)
            val AccentDark = Color(0xFF066A75)
            val AccentContainer = Color(0xFFD5F3F5)
            val SecondaryAccent = Color(0xFF6B3F68)
        }
    }

    object Silk {
        val Primary = Color(0xFF6B3F68)

        object Dark {
            val Accent = Color(0xFF6B3F68)
            val AccentBright = Color(0xFFCE85CA)
            val AccentDark = Color(0xFF522B4F)
            val AccentContainer = Color(0xFF3D1B3B)
            val SecondaryAccent = Color(0xFF087F8C)
        }

        object Light {
            val Accent = Color(0xFF6B3F68)
            val AccentBright = Color(0xFFCE85CA)
            val AccentDark = Color(0xFF522B4F)
            val AccentContainer = Color(0xFFF5E8F4)
            val SecondaryAccent = Color(0xFF087F8C)
        }
    }
}
