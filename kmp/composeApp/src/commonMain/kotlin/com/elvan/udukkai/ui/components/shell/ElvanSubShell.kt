package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors


/**
 * Master Subpage Shell for Jetpack Compose (matching Neram / Flutter's ElvanSubpageShell).
 * Lightweight wrapper around ElvanShell exclusively designed for subpages (Settings, etc.).
 * Disables the bottom navbar and provides full One UI collapsible physics with floating back chevron.
 * Includes a default Android-style passive scrollbar (proportional, auto-fading, non-draggable).
 */
@Composable
fun ElvanSubShell(
    title: String,
    onBack: () -> Unit,
    scrollState: LazyListState = rememberLazyListState(),
    leadingIcon: ImageVector? = null,
    hasActions: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    val colors = rememberShellColors()
    ElvanShell(
        title = title,
        onBack = onBack,
        leadingIcon = leadingIcon,
        showNavbar = false,
        scrollState = scrollState,
        hasActions = hasActions,
        actions = actions,
        navbar = {},
        content = content
    )
}

/**
 * Standard container for section items in ElvanShell pages.
 */
@Composable
fun ElvanSectionContainer(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/**
 * Standard section title with consistent start alignment and bottom spacing.
 */
@Composable
fun ElvanSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors()
) {
    Text(
        text = title.uppercase(),
        style = TextStyle(
            fontFamily = LocalAppFontFamily.current,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        ),
        color = colors.textSecondary.copy(alpha = 0.8f),
        modifier = modifier.padding(
            start = 20.dp,
            bottom = 8.dp
        )
    )
}
