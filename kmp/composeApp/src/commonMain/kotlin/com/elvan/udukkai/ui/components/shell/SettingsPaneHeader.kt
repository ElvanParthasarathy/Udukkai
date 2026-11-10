package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures

/** Detail titles use the search field's top inset and height, without collapsing-header offsets. */
@Composable
fun SettingsPaneHeader(title: String, colors: ShellColors, scrollState: LazyListState, modifier: Modifier = Modifier) {
    val fadeDistancePx = with(LocalDensity.current) { Dimens.HeaderGap.toPx() }
    val titleAlpha by remember(scrollState, fadeDistancePx) {
        derivedStateOf {
            settingsPaneHeaderAlpha(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset, fadeDistancePx)
        }
    }
    if (titleAlpha <= 0f) return
    Box(
        modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(top = Dimens.LandscapeEdgeGap, start = Dimens.SectionTitleStartPadding, end = Dimens.ContentPadding)
            .height(Dimens.SettingsPaneHeaderHeight)
            .graphicsLayer { alpha = titleAlpha },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            title.preventBrokenLigatures(), maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
            style = TextStyle(fontFamily = LocalAppFontFamily.current, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        )
    }
}

/** The title is fully hidden before the first card crosses into the header band. */
internal fun settingsPaneHeaderAlpha(firstVisibleIndex: Int, scrollOffset: Int, fadeDistancePx: Float): Float =
    if (firstVisibleIndex > 0) 0f else (1f - scrollOffset / fadeDistancePx.coerceAtLeast(1f)).coerceIn(0f, 1f)
