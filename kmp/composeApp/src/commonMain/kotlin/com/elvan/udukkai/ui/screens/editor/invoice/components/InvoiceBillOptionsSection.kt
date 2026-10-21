package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsTextField
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection

/**
 * Per-bill configuration options section ported from React InvoiceTypes.ts.
 * Supports taxInclusive, showHSN, showDiscountColumn, showVehicle, showDueDate.
 */
@Composable
fun InvoiceBillOptionsSection(
    options: InvoiceDisplayOptions,
    onOptionsChanged: (InvoiceDisplayOptions) -> Unit,
    showTaxInclusiveOption: Boolean = true,
    index: Int = 5,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    var isExpanded by remember { mutableStateOf(false) }

    ElvanEditorSection(
        index = index,
        title = K.billOptions.tr(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header row to expand / collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = K.billOptions.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
                Icon(
                    imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isExpanded) 180f else 0f)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = colors.divider, thickness = 0.5.dp)

                    // 1. Tax Inclusive Pricing (Textiles only)
                    if (showTaxInclusiveOption) {
                        OptionToggleRow(
                            label = K.taxInclusive.tr(),
                            checked = options.taxInclusive,
                            onCheckedChange = {
                                onOptionsChanged(options.copy(taxInclusive = it))
                            }
                        )
                    }

                    // 2. Show HSN
                    OptionToggleRow(
                        label = K.showHsn.tr(),
                        checked = options.showHSN,
                        onCheckedChange = {
                            onOptionsChanged(options.copy(showHSN = it))
                        }
                    )

                    // 3. Show Discount Column
                    OptionToggleRow(
                        label = K.showDiscountColumn.tr(),
                        checked = options.showDiscountColumn,
                        onCheckedChange = {
                            onOptionsChanged(options.copy(showDiscountColumn = it))
                        }
                    )

                    // 4. Vehicle Number Toggle
                    OptionToggleRow(
                        label = K.showVehicle.tr(),
                        checked = options.showVehicle,
                        onCheckedChange = {
                            onOptionsChanged(options.copy(showVehicle = it))
                        }
                    )
                    if (options.showVehicle) {
                        ElvanSettingsTextField(
                            label = K.vehicleNumber.tr(),
                            value = options.vehicleNumber,
                            onValueChange = {
                                onOptionsChanged(options.copy(vehicleNumber = it.uppercase()))
                            },
                            colors = colors
                        )
                    }

                    // 5. Due Date Toggle
                    OptionToggleRow(
                        label = K.showDueDate.tr(),
                        checked = options.showDueDate,
                        onCheckedChange = {
                            onOptionsChanged(options.copy(showDueDate = it, dueDate = if (it) (options.dueDate ?: (System.currentTimeMillis() + 7 * 86400000L)) else null))
                        }
                    )
                    if (options.showDueDate) {
                        InvoiceDateField(
                            label = K.dueDate.tr(),
                            selectedDate = options.dueDate ?: (System.currentTimeMillis() + 7 * 86400000L),
                            onDateChanged = {
                                onOptionsChanged(options.copy(dueDate = it))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.preventBrokenLigatures(),
            style = TextStyle(
                fontFamily = ff,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textPrimary
            )
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent
            )
        )
    }
}
