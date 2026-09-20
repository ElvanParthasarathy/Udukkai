package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.theme.ShellColors

@Composable
fun ElvanPill(
    liftProgress: Float,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
    } else Modifier

    Box(
        modifier = modifier
            .height(50.dp)
            .widthIn(min = 50.dp)
            .cssShadow(
                alpha = 0.05f * liftProgress,
                blurRadius = 16.dp,
                offsetY = 4.dp
            )
            .background(
                color = colors.floatingBg.copy(alpha = 0.88f * liftProgress),
                shape = CircleShape
            )
            .border(
                width = 0.5.dp,
                color = colors.floatingBorder.copy(alpha = 0.15f * liftProgress),
                shape = CircleShape
            )
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
