package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel
import com.elvan.udukkai.ui.screens.editor.ElvanEditorTextField

@Composable
fun ElvanDocumentNumberField(
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

    val numberPart = remember(currentFullNumber, prefix) {
        if (prefix.isNotEmpty() && currentFullNumber.startsWith(prefix)) {
            currentFullNumber.removePrefix(prefix)
        } else {
            currentFullNumber
        }
    }
    var editedNumberPart by remember(numberPart) { mutableStateOf(numberPart) }

    val commitNumber = {
        val committed = if (editedNumberPart.isNotBlank()) "$prefix$editedNumberPart" else currentFullNumber
        currentFullNumber = committed
        onFullNumberChanged(committed)
        onDirty()
        isEditing = false
    }

    val containerBg = colors.iconBg

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanEditorLabel(label = label)

        if (isEditing) {
            ElvanEditorTextField(
                value = editedNumberPart,
                onValueChange = {
                    editedNumberPart = it
                    onDirty()
                },
                prefixText = prefix,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commitNumber() }),
                suffixIcon = {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 18.dp)
                            ) { commitNumber() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Check,
                            contentDescription = "Save",
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.accent)
                    ) {
                        editedNumberPart = numberPart
                        isEditing = true
                    }
                    .padding(start = 20.dp, end = 8.dp),
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

                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
}
