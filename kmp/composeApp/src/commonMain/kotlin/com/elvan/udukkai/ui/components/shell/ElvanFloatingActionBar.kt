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
    val customColor: Color? = null
)

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
    val ff = LocalAppFontFamily.current

    SideEffect {
        ElvanSnackbar.isBottomBarVisible = true
    }

    var moreOpen by remember { mutableStateOf(false) }
    val overflow = if (actions.size > MAX_BAR_ACTIONS) actions.drop(MAX_BAR_ACTIONS - 1) else emptyList()
    val shown = if (overflow.isEmpty()) actions else actions.take(MAX_BAR_ACTIONS - 1) + ElvanFloatingAction(
        label = K.more.tr(),
        icon = MaterialSymbols.Rounded.MoreVert,
        onClick = { moreOpen = true }
    )

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
                .cssShadow(
                    color = Color.Black,
                    alpha = if (isDark) 0.25f else 0.06f,
                    blurRadius = 16.dp,
                    offsetY = 4.dp
                )
                .background(
                    color = colors.floatingBg.copy(alpha = 0.92f),
                    shape = CircleShape
                )
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
                        .fillMaxHeight()
                        // Each button as wide as its label, never narrower than a nav-bar tab
                        .widthIn(min = 58.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            enabled = action.enabled && !action.isLoading,
                            onClick = action.onClick
                        )
                        .padding(horizontal = 12.dp),
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
                    if (overflow.isNotEmpty() && index == shown.lastIndex) {
                        ElvanPopupMenu(
                            expanded = moreOpen,
                            onDismissRequest = { moreOpen = false },
                            colors = colors,
                            upward = true,
                            items = overflow.filter { it.enabled }.map { a -> ElvanPopupMenuItem(a.label, a.icon) { moreOpen = false; a.onClick() } }
                        )
                    }
                }
            }
        }
    }
}

/** The bar shows at most this many buttons; the rest go into More. */
private const val MAX_BAR_ACTIONS = 5
