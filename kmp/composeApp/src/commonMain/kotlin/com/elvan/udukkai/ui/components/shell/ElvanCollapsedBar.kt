package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols

@Composable
fun ElvanTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ElvanCollapsedBar(
    scrollOffset: Float,
    collisionOffsetPx: Float,
    colors: ShellColors,
    expandedHeight: Dp = 280.dp,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    navOpacity: Float = 1.0f,
    hasActions: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    isAllSelected: Boolean = false,
    onSelectAll: () -> Unit = {},
    onCancelSelection: () -> Unit = {},
    isPinnedAlways: Boolean = false,
    leadingActions: (@Composable RowScope.() -> Unit)? = null,
    /** View pages: 0 at the top (plain chevron + title), 1 once scrolled (chevron in its circle, title gone). */
    scrollLift: Float? = null
) {
    val statusBarHeight = com.elvan.udukkai.core.platform.getStatusBarTopPadding()
    val isDesktop = com.elvan.udukkai.core.platform.currentPlatform == com.elvan.udukkai.core.platform.PlatformType.DESKTOP
    val ceiling = if (isDesktop) 16.dp else if (isPinnedAlways && scrollLift == null) statusBarHeight + 8.dp else statusBarHeight + 20.dp
    val density = LocalDensity.current
    val ceilingPx = with(density) { ceiling.toPx() }

    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val currentHeightPx = expandedHeightPx - scrollOffset
    val currentTopPx = currentHeightPx - with(density) { 64.dp.toPx() }

    val isPinned = isPinnedAlways || currentTopPx <= ceilingPx
    val finalTopPx = if (isPinned) ceilingPx else currentTopPx
    val finalTopDp = with(density) { finalTopPx.toDp() }

    val liftStartOffsetPx = collisionOffsetPx - with(density) { 4.dp.toPx() }
    val liftProgress = if (scrollLift != null) scrollLift else if (isPinnedAlways) 1.0f else if (scrollOffset > liftStartOffsetPx) {
        ((scrollOffset - liftStartOffsetPx) / with(density) { 12.dp.toPx() }).coerceIn(0f, 1f)
    } else {
        0f
    }

    if (navOpacity > 0.01f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = finalTopDp, start = 16.dp, end = 16.dp)
                .zIndex(150f)
                .graphicsLayer {
                    this.alpha = navOpacity
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        AnimatedContent(
            targetState = isSelectionMode,
            // Apple-style morph, like the bottom bar: the old controls shrink and fade while the new ones grow in on a spring
            transitionSpec = {
                (fadeIn(animationSpec = tween(260, delayMillis = 50, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))) +
                    androidx.compose.animation.scaleIn(androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 380f), initialScale = 0.88f)) togetherWith
                    (fadeOut(animationSpec = tween(160, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))) +
                        androidx.compose.animation.scaleOut(tween(220), targetScale = 0.94f))
            },
            label = "selectionBarCrossfade",
            modifier = Modifier.fillMaxWidth()
        ) { inSelection ->
            if (inSelection) {
                val selectAllLabel = K.all.tr()
                val cancelLabel = K.cancelBtn.tr()
                val isCollapsed = liftProgress >= 0.5f

                val pillText = if (selectedCount > 0 && !isAllSelected) "$selectedCount" else selectAllLabel

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Side: Select All / Count Pill
                    ElvanPill(
                        liftProgress = liftProgress,
                        colors = colors,
                        onClick = onSelectAll
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                com.elvan.udukkai.ui.components.ElvanCheckMark(
                                    selected = isAllSelected,
                                    selectedTint = colors.modeAccent,
                                    idleTint = colors.textPrimary,
                                    size = 20.dp
                                )
                                AnimatedContent(
                                    targetState = pillText,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(180, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))) togetherWith
                                         fadeOut(animationSpec = tween(120, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))))
                                            .using(SizeTransform(clip = false))
                                    },
                                    label = "selectionPillTextAnim"
                                ) { text ->
                                    Text(
                                        text = text,
                                        maxLines = 1,
                                        style = TextStyle(
                                            fontFamily = LocalAppFontFamily.current,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Right Side: Cancel Pill ("கைவிடு")
                    ElvanPill(
                        liftProgress = liftProgress,
                        colors = colors,
                        onClick = onCancelSelection
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cancelLabel,
                                style = TextStyle(
                                    fontFamily = LocalAppFontFamily.current,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left side (Leading Actions / Back Button / Title)
                    if (leadingActions != null || onBack != null || title != null) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (leadingActions != null) {
                                ElvanPill(liftProgress = liftProgress, colors = colors) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        content = leadingActions
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (onBack != null) {
                                ElvanPill(liftProgress = liftProgress, colors = colors, modifier = Modifier.size(50.dp)) {
                                    ElvanTopBarIconButton(
                                        onClick = onBack,
                                        size = 38.dp
                                    ) {
                                        Icon(
                                            imageVector = leadingIcon ?: MaterialSymbols.Rounded.ArrowBack,
                                            contentDescription = if (leadingIcon != null) K.cancel.tr() else K.back.tr(),
                                            tint = colors.textPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                            if (title != null) {
                                Text(
                                    text = title.preventBrokenLigatures(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontFamily = LocalAppFontFamily.current,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    ),
                                    modifier = Modifier
                                        .padding(start = if (onBack != null || leadingActions != null) 12.dp else 8.dp, end = if (hasActions) 8.dp else 0.dp)
                                        .graphicsLayer {
                                            this.alpha = if (scrollLift != null) 1f - scrollLift else liftProgress
                                        }
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f, fill = false))
                    }

                    // Right side (Action Buttons Pill) - ONLY if hasActions is true!
                    if (hasActions) {
                        val actionsAlpha by animateFloatAsState(
                            targetValue = if (ElvanMenuState.isMenuOpen) 0f else 1f,
                            animationSpec = tween(150, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)),
                            label = "actionsAlpha"
                        )

                        Box(
                            modifier = Modifier.graphicsLayer {
                                alpha = actionsAlpha
                            }
                        ) {
                            ElvanPill(liftProgress = liftProgress, colors = colors) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                                    // The circles sit 6dp from the pill's edge on every side (the pill is 50dp, a circle 38dp)
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    actions()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

