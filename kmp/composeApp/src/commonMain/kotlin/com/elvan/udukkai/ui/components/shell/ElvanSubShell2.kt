package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.getNavBarHeightIgnoringVisibility
import com.elvan.udukkai.core.platform.getStatusBarHeightIgnoringVisibility
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Subpage shell with a fixed header: no expanding or collapsing title, for pages whose content
 * scrolls or zooms on its own (the document preview). Same top fade, back pill and title, bottom
 * fade and floating bar slot as the other subpages; the content fills the whole screen under them.
 * [hideChrome] fades all of it out for full-screen viewing. The insets ignore whether the system
 * bars are showing, so nothing moves when they hide or come back.
 */
@Composable
fun ElvanSubShell2(
    title: String,
    onBack: () -> Unit,
    hideChrome: Boolean = false,
    showBottomFade: Boolean = true,
    floatingBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    // Sizes of the system bars as if they were always visible: hiding or showing them moves nothing
    val statusTop = getStatusBarHeightIgnoringVisibility()
    val navBottom = getNavBarHeightIgnoringVisibility()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            // Swallows touches that the page itself does not use, so none reach the screen below
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    ) {
        content()

        AnimatedVisibility(
            visible = !hideChrome,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (showBottomFade) {
                    // Bottom shadow fade above the system navigation bar, like the other subpages
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(72.dp + navBottom)
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.3f to colors.background.copy(alpha = 0.16f),
                                    0.65f to colors.background.copy(alpha = 0.55f),
                                    1.0f to colors.background
                                )
                            )
                    )
                }

                ElvanStandardTopBar(
                    title = title,
                    onBack = onBack,
                    scrolled = false,
                    statusTop = statusTop,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // The bar's own navigation bar padding is replaced by the held value
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = navBottom)
                        .consumeWindowInsets(WindowInsets.navigationBars),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    floatingBar()
                }
            }
        }
    }
}
