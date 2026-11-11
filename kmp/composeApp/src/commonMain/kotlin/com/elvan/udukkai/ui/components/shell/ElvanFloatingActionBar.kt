package com.elvan.udukkai.ui.components.shell

import com.elvan.udukkai.ui.components.ExpressiveLoadingIndicator
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

data class ElvanFloatingAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val isLoading: Boolean = false,
    val isDestructive: Boolean = false,
    val customColor: Color? = null,
    /** A menu this button opens: it sits in the button's slot so it can open from the button. */
    val menu: (@Composable () -> Unit)? = null
)

/** True in phone/tablet landscape: the bar becomes a slim icon column on the trailing edge. */
val LocalElvanSideBar = androidx.compose.runtime.compositionLocalOf { false }

/**
 * Universal Floating Action Bar: the floating pill of the view pages, editors and selection.
 * Laid out like the bottom navigation bar: the pill is as wide as its buttons, and each button is as wide as its
 * label needs (optical spacing, as Samsung's Gallery bar), so it never runs edge to edge. At most five buttons
 * show; any more go into a More menu that opens above the bar.
 */
@Composable
fun ElvanFloatingActionBar(
    actions: List<ElvanFloatingAction>,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors()
) {
    if (actions.isEmpty()) return

    val isDark = colors.isDark
    // Action controls stay out of the keyboard viewport instead of riding its resize animation.
    if (com.elvan.udukkai.core.platform.getImeBottomPadding() > 0.dp) return
    val ff = LocalAppFontFamily.current

    SideEffect {
        ElvanSnackbar.isBottomBarVisible = true
    }

    var moreOpen by remember { mutableStateOf(false) }
    val maxActions = if (LocalElvanSideBar.current) MAX_SIDE_ACTIONS else MAX_BAR_ACTIONS
    val overflow = if (actions.size > maxActions) actions.drop(maxActions - 1) else emptyList()
    val shown = if (overflow.isEmpty()) actions else actions.take(maxActions - 1) + ElvanFloatingAction(
        label = K.more.tr(),
        icon = MaterialSymbols.Rounded.MoreVert,
        onClick = { moreOpen = true }
    )

    if (LocalElvanSideBar.current) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing.only(androidx.compose.foundation.layout.WindowInsetsSides.Horizontal + androidx.compose.foundation.layout.WindowInsetsSides.Bottom))
                .padding(end = 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .cssShadow(color = Color.Black, alpha = if (isDark) 0.25f else 0.06f, blurRadius = 16.dp, offsetY = 4.dp)
                    .frostedGlass(CircleShape, colors)
                    .border(0.5.dp, colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f), CircleShape)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                shown.forEachIndexed { index, action ->
                    val itemColor = action.customColor
                        ?: if (action.isDestructive) colors.danger
                        else if (action.enabled) colors.textPrimary
                        else colors.textSecondary.copy(alpha = 0.35f)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                enabled = action.enabled && !action.isLoading,
                                onClick = action.onClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            if (action.isLoading) {
                                ExpressiveLoadingIndicator(modifier = Modifier.size(20.dp), color = itemColor, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = action.icon, contentDescription = action.label, tint = itemColor, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = action.label.preventBrokenLigatures(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(fontFamily = ff, fontSize = 8.5.sp, lineHeight = 10.sp, fontWeight = FontWeight.Medium, color = itemColor, textAlign = TextAlign.Center),
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                        action.menu?.invoke()
                        if (overflow.isNotEmpty() && index == shown.lastIndex) {
                            ElvanPopupMenu(
                                expanded = moreOpen,
                                onDismissRequest = { moreOpen = false },
                                colors = colors,
                                upward = true,
                                items = overflow.filter { it.enabled }.map { a -> ElvanPopupMenuItem(a.label, a.icon, { moreOpen = false; a.onClick() }) }
                            )
                        }
                    }
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(100f)
            .navigationBarsPaddingIfMobile()
            .padding(bottom = Dimens.FloatingBarBottomGap),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(56.dp)
                // With every button weighted alike, the row takes the widest button's width for each of them
                .width(IntrinsicSize.Max)
                .cssShadow(
                    color = Color.Black,
                    alpha = if (isDark) 0.25f else 0.06f,
                    blurRadius = 16.dp,
                    offsetY = 4.dp
                )
                .frostedGlass(CircleShape, colors)
                .border(
                    width = 0.5.dp,
                    color = colors.floatingBorder.copy(alpha = if (isDark) 0.15f else 0.6f),
                    shape = CircleShape
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            shown.forEachIndexed { index, action ->
                val itemColor = action.customColor
                    ?: if (action.isDestructive) colors.danger
                    else if (action.enabled) colors.textPrimary
                    else colors.textSecondary.copy(alpha = 0.35f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // All buttons the same width, never narrower than a nav-bar tab
                        .widthIn(min = 62.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            enabled = action.enabled && !action.isLoading,
                            onClick = action.onClick
                        )
                        .padding(horizontal = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (action.isLoading) {
                            ExpressiveLoadingIndicator(modifier = Modifier.size(22.dp), color = itemColor, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = itemColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(1.5.dp))
                        Text(
                            text = action.label.preventBrokenLigatures(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = itemColor,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    action.menu?.invoke()
                    if (overflow.isNotEmpty() && index == shown.lastIndex) {
                        ElvanPopupMenu(
                            expanded = moreOpen,
                            onDismissRequest = { moreOpen = false },
                            colors = colors,
                            upward = true,
                            items = overflow.filter { it.enabled }.map { a -> ElvanPopupMenuItem(a.label, a.icon, { moreOpen = false; a.onClick() }) }
                        )
                    }
                }
            }
        }
    }
}

/** The bar shows at most this many buttons; the rest go into More. */
private const val MAX_BAR_ACTIONS = 5

/** The landscape column is shorter, so it shows four. */
private const val MAX_SIDE_ACTIONS = 4
