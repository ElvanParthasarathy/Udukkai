package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.elvan.udukkai.core.platform.ScreenLayout
import com.elvan.udukkai.core.platform.platformLayoutPolicy

internal val LocalAuthLandscape = compositionLocalOf { false }
internal val LocalAuthKeyboardVisible = compositionLocalOf { false }

/** Explicit slots keep branding, fields, and actions together at every Android window height. */
@Composable
internal fun NeramPage(
    bottom: (@Composable () -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    pinActions: Boolean = true,
    boundedContent: Boolean = false,
    landscapeEndPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Resolve against the window before IME insets; opening a keyboard must not switch layouts.
        val landscape = platformLayoutPolicy.screenLayout(maxWidth, maxHeight) == ScreenLayout.AndroidLandscape
        CompositionLocalProvider(LocalAuthLandscape provides landscape, LocalAuthKeyboardVisible provides (com.elvan.udukkai.core.platform.getImeBottomPadding() > 0.dp)) {
            when {
                landscape -> AndroidAuthPage(header, bottom, boundedContent, landscapeEndPadding, content)
                pinActions -> LegacyNeramPage(bottom = { bottom?.invoke() }) {
                    header?.invoke()
                    content()
                }
                else -> BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding()) {
                    Column(
                        Modifier.fillMaxWidth().heightIn(min = maxHeight).verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        header?.invoke()
                        content()
                        bottom?.invoke()
                    }
                }
            }
        }
    }
}

/** The form owns scrolling; actions stay outside its viewport and branding keeps its position. */
@Composable
internal fun AndroidAuthPage(
    header: (@Composable () -> Unit)?,
    bottom: (@Composable () -> Unit)?,
    boundedContent: Boolean = false,
    landscapeEndPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
            .padding(horizontal = 24.dp).padding(top = 16.dp, bottom = if (LocalAuthKeyboardVisible.current) 0.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (header != null) Box(
            Modifier.weight(0.4f).fillMaxHeight().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier.widthIn(max = 320.dp).fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { header() }
        }
        Column(
            Modifier.weight(0.6f).fillMaxHeight().padding(end = landscapeEndPadding).imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier.weight(1f).fillMaxWidth().then(
                    if (boundedContent) Modifier else Modifier.verticalScroll(rememberScrollState())
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = content
            )
            if (bottom != null && !LocalAuthKeyboardVisible.current) {
                Spacer(Modifier.height(12.dp))
                bottom()
            }
        }
    }
}

/** Welcome/permission/restore pages retain their portrait layout and share the Android landscape slots. */
@Composable
internal fun NeramIntroPage(
    horizontalPadding: Dp = 16.dp,
    centered: Boolean = false,
    header: @Composable () -> Unit,
    bottom: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        when (platformLayoutPolicy.screenLayout(maxWidth, maxHeight)) {
            ScreenLayout.AndroidLandscape -> if (centered) CompositionLocalProvider(LocalAuthLandscape provides true) {
                Column(
                    Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { header() }
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { bottom() }
                }
            } else NeramPage(
                header = { Column(horizontalAlignment = Alignment.CenterHorizontally) { header() } },
                bottom = bottom
            ) {}
            else -> Column(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = horizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))
                header()
                Spacer(Modifier.weight(1f))
                bottom()
            }
        }
    }
}
