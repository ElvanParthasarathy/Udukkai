package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors

/**
 * The top Save pill. Clip comes before clickable so the ripple fills the whole pill, and the label sits in a centred Box
 * with trimmed line height so Tamil/English glyphs centre vertically.
 */
@Composable
fun ElvanActionButton(
    label: String = K.saveBtn.tr(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    /** In the top pill, the ripple runs to the pill's edges; off for a button that stands on its own. */
    reachPillEdge: Boolean = true
) {
    val colors = rememberShellColors()
    // The ripple fills the top pill only while the pill is showing; with no shape behind it, it stays the button's own size
    val lift = if (reachPillEdge) LocalElvanPillLift.current else 0f
    Box(
        modifier = modifier
            .heightIn(min = 38.dp)
            .then(
                if (!reachPillEdge) Modifier else Modifier
                    .layout { measurable, constraints ->
                        val extra = (6.dp * lift).roundToPx()
                        val placeable = measurable.measure(
                            constraints.copy(minWidth = 0, maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth + 2 * extra else androidx.compose.ui.unit.Constraints.Infinity)
                        )
                        layout((placeable.width - 2 * extra).coerceAtLeast(0), placeable.height) { placeable.place(-extra, 0) }
                    }
                    .requiredHeight(38.dp + 12.dp * lift)
            )
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ShellDefaults.ripple(colors, bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp + 6.dp * lift),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (enabled) colors.accent else colors.textSecondary.copy(alpha = 0.4f),
            style = TextStyle(
                fontFamily = LocalAppFontFamily.current,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both)
            )
        )
    }
}
