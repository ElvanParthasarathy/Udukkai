package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDateField(
    selectedDate: Long,
    onDateChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isPickerOpen by remember { mutableStateOf(false) }
    val containerBg = colors.iconBg

    Column(modifier = modifier.fillMaxWidth()) {
        if (!label.isNullOrBlank()) {
            ElvanEditorLabel(label = label)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(containerBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = colors.accent)
                ) { isPickerOpen = true }
                .padding(start = 20.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatEpochMillis(selectedDate).preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                )

                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (isPickerOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate,
            yearRange = DatePickerDefaults.YearRange,
            selectableDates = DatePickerDefaults.AllDates
        )
        val dialogBg = if (isDark) Color(0xFF1E1E1E) else Color.White

        DatePickerDialog(
            onDismissRequest = { isPickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateChanged(it) }
                        isPickerOpen = false
                    }
                ) {
                    Text(
                        text = K.confirm.tr(),
                        style = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, color = colors.accent)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isPickerOpen = false }) {
                    Text(
                        text = K.cancel.tr(),
                        style = TextStyle(fontFamily = ff, color = colors.textSecondary)
                    )
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = dialogBg
            )
        ) {
            DatePicker(
                state = datePickerState,
                title = null,
                headline = {
                    DatePickerDefaults.DatePickerHeadline(
                        selectedDateMillis = datePickerState.selectedDateMillis,
                        displayMode = datePickerState.displayMode,
                        dateFormatter = remember { DatePickerDefaults.dateFormatter() },
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 8.dp)
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = dialogBg,
                    selectedDayContainerColor = colors.accent,
                    todayDateBorderColor = colors.accent
                )
            )
        }
    }
}
