package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

import androidx.compose.material3.ripple

/**
 * RecentActivityHeader for DashboardScreen.
 * Aligns cleanly after the card curve with the cards below.
 * Displays "நடப்பு வினைகள்" and a compact "யாவும் >" button with a bounded pill ripple.
 */
@Composable
fun RecentActivityHeader(
    onSeeAll: () -> Unit,
    colors: ShellColors,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.SectionTitleStartPadding, end = 20.dp, top = 0.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = K.recentActivity.tr().preventBrokenLigatures(),
            style = TextStyle(
                fontFamily = ff,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                letterSpacing = (-0.2).sp
            )
        )

        // Compact "See All" action with no background shape but bounded pill ripple
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = colors.ripple),
                    onClick = onSeeAll
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = K.seeAllBtn.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LocalShellColors.current.textSecondary,
                        letterSpacing = (-0.2).sp
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = MaterialSymbols.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = LocalShellColors.current.textSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
