package com.elvan.udukkai.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.elvan.udukkai.R

actual val ElvanSansFontFamily: FontFamily = FontFamily(
    Font(R.font.elvan_sans_regular, FontWeight.Light),
    Font(R.font.elvan_sans_regular, FontWeight.Normal),
    Font(R.font.elvan_sans_medium, FontWeight.Medium),
    Font(R.font.elvan_sans_semibold, FontWeight.SemiBold),
    Font(R.font.elvan_sans_bold, FontWeight.Bold)
)

actual val NavilSansFontFamily: FontFamily = FontFamily(
    Font(R.font.navil_sans_light, FontWeight.Light),
    Font(R.font.navil_sans_regular, FontWeight.Normal),
    Font(R.font.navil_sans_medium, FontWeight.Medium),
    Font(R.font.navil_sans_semibold, FontWeight.SemiBold),
    Font(R.font.navil_sans_bold, FontWeight.Bold)
)
