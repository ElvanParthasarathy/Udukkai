package com.elvan.udukkai.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.getImeBottomPadding
import com.elvan.udukkai.core.platform.getNavBarBottomPadding

object Dimens {
    val ContentPadding = 12.dp
    val SectionSpacing = 16.dp
    val ItemSpacing = 12.dp
    val CardRadius = 24.dp
    val CardPaddingHorizontal = 20.dp
    val CardPaddingVertical = 16.dp
    val SectionTitleStartPadding = 36.dp // ContentPadding (12.dp) + CardRadius (24.dp) - starts exactly after curve

    val ContentPaddingBottom: Dp
        @Composable
        get() {
            val navBarsPadding = getNavBarBottomPadding()
            val imePadding = getImeBottomPadding()
            return if (imePadding > 0.dp) imePadding + 90.dp else 110.dp + navBarsPadding
        }

    val SubpageContentPaddingBottom: Dp
        @Composable
        get() {
            val navBarsPadding = getNavBarBottomPadding()
            val imePadding = getImeBottomPadding()
            return if (imePadding > 0.dp) 40.dp else 32.dp + navBarsPadding
        }

    val ContentPaddingTop = 85.dp
    val HeaderMarginTop = 20.dp
    val HeaderGap = 8.dp
    val HeaderPillPadding = 12.dp
}
