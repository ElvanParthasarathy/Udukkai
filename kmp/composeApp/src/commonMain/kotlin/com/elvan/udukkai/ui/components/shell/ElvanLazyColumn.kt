package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.rememberShellColors

val LocalLandscapeContent = compositionLocalOf { false }
val LocalLandscapeHeaderClearance = compositionLocalOf { 0.dp }
val LocalLandscapeSettingsHeader = compositionLocalOf { false }

/** Screen lists share the floating controls' safe-area margins in landscape. Nested lists keep their own layout. */
@Composable
fun ElvanLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    landscapeRoot: Boolean = false,
    content: LazyListScope.() -> Unit
) {
    val landscape = com.elvan.udukkai.core.platform.platformLayoutPolicy.supportsLandscapeControls && (LocalLandscapeContent.current || landscapeRoot)
    val screenList = landscape && (landscapeRoot || state === LocalElvanScrollState.current)
    if (!screenList) {
        LazyColumn(modifier, state, contentPadding, reverseLayout, verticalArrangement, horizontalAlignment, flingBehavior, userScrollEnabled, content = content)
        return
    }
    val direction = LocalLayoutDirection.current
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val padding = if (screenList) PaddingValues(
        start = contentPadding.calculateStartPadding(direction),
        end = contentPadding.calculateEndPadding(direction),
        top = safe.calculateTopPadding() + Dimens.LandscapeEdgeGap + LocalLandscapeHeaderClearance.current,
        bottom = safe.calculateBottomPadding() + Dimens.LandscapeEdgeGap
    ) else contentPadding
    val colors = rememberShellColors()
    Box(modifier) {
        LazyColumn(Modifier.fillMaxSize(), state, padding, reverseLayout, verticalArrangement, horizontalAlignment, flingBehavior, userScrollEnabled) {
            LandscapeListScope(this).content()
        }
        // Full-bleed scrolling, with only a faint veil within the two system-bar regions.
        if (safe.calculateTopPadding() > 0.dp) Box(
            Modifier.align(Alignment.TopCenter).fillMaxWidth().height(safe.calculateTopPadding() + 4.dp)
                .background(Brush.verticalGradient(listOf(colors.background.copy(alpha = 0.35f), Color.Transparent)))
        )
        if (safe.calculateBottomPadding() > 0.dp) Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(safe.calculateBottomPadding() + 4.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, colors.background.copy(alpha = 0.35f))))
        )
    }
}

private class LandscapeListScope(private val target: LazyListScope) : LazyListScope by target {
    override fun item(key: Any?, contentType: Any?, content: @Composable LazyItemScope.() -> Unit) {
        // These portrait header placeholders must not add a second landscape inset or an extra list gap.
        if (key != "spacer_top" && (key as? String)?.endsWith("top_spacer") != true) {
            target.item(key, contentType, content)
        }
    }
}
