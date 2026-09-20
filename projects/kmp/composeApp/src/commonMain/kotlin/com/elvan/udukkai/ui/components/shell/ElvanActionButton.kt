package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors

@Composable
fun ElvanActionButton(
    label: String = K.saveBtn.tr(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = rememberShellColors()
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        modifier = modifier.height(44.dp)
    ) {
        Text(
            text = label,
            color = if (enabled) colors.accent else colors.textSecondary.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            fontFamily = LocalAppFontFamily.current
        )
    }
}
