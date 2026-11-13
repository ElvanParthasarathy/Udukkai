package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.navigation.NavTab

/**
 * Placeholder detail view displayed in the right panel of the Landscape Two-Pane layout
 * when no item (invoice, customer, item) is currently selected.
 * Strictly adheres to Pure Tamil policy (zero Sanskrit words).
 */
@Composable
fun LandscapeEmptyDetailPane(
    selectedTab: NavTab,
    colors: ShellColors,
    modifier: Modifier = Modifier
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val icon = when (selectedTab) {
        NavTab.Home -> MaterialSymbols.CustomNav.Home
        NavTab.Create -> MaterialSymbols.CustomNav.Create
        NavTab.Items -> MaterialSymbols.CustomNav.Items
        NavTab.Customers -> MaterialSymbols.CustomNav.Customers
        NavTab.Business -> MaterialSymbols.Rounded.Apartment
        NavTab.Tools -> MaterialSymbols.Rounded.Handyman
    }

    val title = when (selectedTab) {
        NavTab.Home -> "முன்னோட்டம்"
        NavTab.Create -> K.invoices.tr()
        NavTab.Items -> K.items.tr()
        NavTab.Customers -> K.customers.tr()
        NavTab.Business -> K.records.tr()
        NavTab.Tools -> K.tools.tr()
    }

    val hint = "தரவுகளைக் காண இடப்பக்கத்தில் ஒரு உருப்படியைத் தேர்ந்தெடுக்கவும்"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.iconBg)
                    .border(
                        width = 1.dp,
                        color = colors.floatingBorder.copy(alpha = if (isDark) 0.12f else 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                fontFamily = ff,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = hint,
                fontFamily = ff,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textSecondary.copy(alpha = 0.7f)
            )
        }
    }
}
