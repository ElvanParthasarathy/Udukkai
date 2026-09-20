package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu

/**
 * Self-contained component for editing document numbers.
 * Switches between a locked display pill with a pencil icon and an editable text field with a checkmark commit button.
 * Matches Flutter's `elvan_aavana_enn_kooru.dart` 1:1.
 */
@Composable
fun ElvanAavanaEnnKooru(
    label: String,
    prefix: String,
    initialFullNumber: String,
    onFullNumberChanged: (String) -> Unit,
    onDirty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isEditing by remember { mutableStateOf(false) }
    var currentFullNumber by remember(initialFullNumber) { mutableStateOf(initialFullNumber) }

    // Strip prefix for editing
    val numberPart = remember(currentFullNumber, prefix) {
        if (prefix.isNotEmpty() && currentFullNumber.startsWith(prefix)) {
            currentFullNumber.removePrefix(prefix)
        } else {
            currentFullNumber
        }
    }
    var editedNumberPart by remember(numberPart) { mutableStateOf(numberPart) }

    val containerBg = colors.iconBg

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanThiruthiThalaippu(label = label)

        if (isEditing) {
            ElvanThiruthiUlleedu(
                value = editedNumberPart,
                onValueChange = {
                    editedNumberPart = it
                    onDirty()
                },
                prefixText = prefix,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffixIcon = {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f))
                            .clickable {
                                val committed = if (editedNumberPart.isNotBlank()) "$prefix$editedNumberPart" else currentFullNumber
                                currentFullNumber = committed
                                onFullNumberChanged(committed)
                                onDirty()
                                isEditing = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Check,
                            contentDescription = "Save",
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(containerBg)
                    .clickable {
                        editedNumberPart = numberPart
                        isEditing = true
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentFullNumber.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    )

                    Icon(
                        imageVector = MaterialSymbols.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
