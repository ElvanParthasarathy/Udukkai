package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

data class VanigaChipItem(
    val label: String,
    val icon: Any? = null
)

/**
 * Compact Borderless Monogram Avatars (iOS Contacts / WhatsApp Style):
 * - Small circular avatar discs (size = 36.dp, CircleShape).
 * - Borderless with zero outline.
 * - Active: Solid high-contrast fill (White in dark / Black in light) with drop shadow and bold monogram.
 * - Inactive: Subtle translucent disc with dimmed monogram.
 * - Adaptive layout: Centered when few items, horizontally swipeable with LazyRow when many items.
 */
@Composable
fun BusinessChips(
    items: List<VanigaChipItem>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    colors: ShellColors = rememberShellColors(),
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    val lazyListState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        if (selectedIndex in items.indices) {
            lazyListState.animateScrollToItem(selectedIndex)
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            val monogram = remember(item.label) { formatMonogram(item.label) }

            VanigaMonogramAvatar(
                monogram = monogram,
                isSelected = isSelected,
                isDark = isDark,
                colors = colors,
                ff = ff,
                onClick = { onIndexSelected(index) }
            )
        }
    }
}

private fun formatMonogram(label: String): String {
    val trimmed = label.trim()
    return when {
        trimmed.equals("யாவும்", ignoreCase = true) -> "யா"
        trimmed.equals("ALL", ignoreCase = true) -> "ALL"
        trimmed.length <= 3 -> trimmed
        else -> trimmed.take(2)
    }
}

@Composable
private fun VanigaMonogramAvatar(
    monogram: String,
    isSelected: Boolean,
    isDark: Boolean,
    colors: ShellColors,
    ff: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color.White else Color(0xFF1D1D1F)
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "avatarBgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (isDark) Color.Black else Color.White
        } else {
            if (isDark) Color.White.copy(alpha = 0.65f) else Color.Black.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "avatarContentColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "avatarScale"
    )

    val shadowModifier = if (isSelected) {
        Modifier.cssShadow(
            color = Color.Black,
            alpha = if (isDark) 0.35f else 0.20f,
            borderRadius = 50.dp,
            blurRadius = 8.dp,
            offsetY = 2.dp
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(shadowModifier)
            .clip(CircleShape)
            .background(animBgColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.ripple, bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = monogram.preventBrokenLigatures(),
            style = TextStyle(
                fontFamily = ff,
                fontSize = if (monogram.length >= 3) 11.sp else 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                letterSpacing = if (monogram.length >= 3) (-0.2).sp else 0.sp
            ),
            color = contentColor,
            maxLines = 1
        )
    }
}
