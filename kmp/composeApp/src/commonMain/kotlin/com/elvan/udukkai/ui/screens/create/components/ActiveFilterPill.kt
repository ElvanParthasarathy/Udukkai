package com.elvan.udukkai.ui.screens.create.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.navigation.MaterialSymbols

@Composable
fun ActiveFilterPill(label: String, colors: ShellColors, onClear: () -> Unit) {
    Surface(onClick = onClear, shape = CircleShape, color = colors.accent.copy(alpha = 0.12f)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(MaterialSymbols.Rounded.FilterList, null, tint = colors.accent, modifier = Modifier.size(14.dp))
            Text(label.preventBrokenLigatures(), fontFamily = LocalAppFontFamily.current, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, color = colors.accent)
            Icon(MaterialSymbols.Rounded.Close, K.clear.tr(), tint = colors.accent, modifier = Modifier.size(14.dp))
        }
    }
}
