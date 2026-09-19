package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.data.IdangalinPeyar
import com.elvan.udukkai.core.data.indhiyaMaanilangal
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu

/**
 * Place of Supply (வழங்கல் இடம்) selection widget with bottom sheet.
 * Matches Flutter's `PattuVilippiIdam` 1:1.
 */
@Composable
fun PattuVilippiIdam(
    placeOfSupplyEn: String,
    placeOfSupplyTa: String,
    onSelected: (en: String, ta: String) -> Unit,
    onCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isSheetOpen by remember { mutableStateOf(false) }

    val displayText = when {
        placeOfSupplyTa.isNotBlank() -> placeOfSupplyTa
        placeOfSupplyEn.isNotBlank() -> placeOfSupplyEn
        else -> ""
    }

    val containerBg = colors.iconBg

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanThiruthiThalaippu(label = K.placeOfSupply.tr())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(containerBg)
                .clickable { isSheetOpen = true }
                .padding(start = 20.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (displayText.isNotEmpty()) displayText else K.placeOfSupply.tr()).preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = if (displayText.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                        color = if (displayText.isNotEmpty()) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (displayText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onCleared() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = K.cancel.tr(),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (isSheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.placeOfSupply.tr(),
            items = indhiyaMaanilangal,
            currentValue = indhiyaMaanilangal.firstOrNull { it.en.equals(placeOfSupplyEn, ignoreCase = true) },
            showSearch = true,
            onDismissRequest = { isSheetOpen = false },
            onSelected = { state ->
                onSelected(state.en, state.ta)
                isSheetOpen = false
            },
            itemLabelBuilder = { s -> s.ta },
            subtitleBuilder = { s -> s.en },
            searchFilter = { s, query ->
                val q = query.lowercase()
                s.ta.lowercase().contains(q) || s.en.lowercase().contains(q)
            }
        )
    }
}
