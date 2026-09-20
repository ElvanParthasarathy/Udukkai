package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiAttai
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu

/**
 * Single line item card for the Silk Invoice Editor.
 * Matches Flutter's `pattu_urupadi_attai.dart` 1:1.
 */
@Composable
fun PattuUrupadiAttai(
    item: PattuUrupadi,
    index: Int,
    itemCount: Int,
    onItemUpdated: (PattuUrupadi) -> Unit,
    onItemDeleted: () -> Unit,
    onItemCleared: () -> Unit,
    onDirty: () -> Unit,
    onRequestAddNewProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isPickerOpen by remember { mutableStateOf(false) }
    val allProducts = PorulRepository.items

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // ── Header: "பொருள் #N" + trash icon ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${K.product.tr()} #${index + 1}".preventBrokenLigatures(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )

            if (itemCount > 1) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                        .clickable { onItemDeleted() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Delete,
                        contentDescription = K.delete.tr(),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ── Item Card ──
        ElvanThiruthiAttai(
            padding = PaddingValues(16.dp),
            borderRadius = 24.dp
        ) {
            // ── Product Picker Pill ──
            Column(modifier = Modifier.fillMaxWidth()) {
                ElvanThiruthiThalaippu(label = K.product.tr())

                val displayName = item.porulPeyar.ifEmpty { item.porulPeyarEn }
                val containerBg = colors.iconBg

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(containerBg)
                        .clickable { isPickerOpen = true }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (if (displayName.isNotEmpty()) displayName else K.products.tr()).preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = if (displayName.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                                color = if (displayName.isNotEmpty()) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (displayName.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onItemCleared()
                                        onDirty()
                                    },
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

                // Subtitle Info (English name • GST%)
                if (item.porulPeyarEn.isNotEmpty() && item.porulPeyarEn != item.porulPeyar) {
                    Text(
                        text = "${item.porulPeyarEn}  •  GST ${item.variVizhukkaadu.toInt()}%",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        ),
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            // ── Quantity & Rate Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ElvanThiruthiUlleedu(
                        label = K.quantity.tr(),
                        value = if (item.alavu == 0.0) "" else if (item.alavu % 1.0 == 0.0) item.alavu.toInt().toString() else item.alavu.toString(),
                        onValueChange = { str ->
                            val qty = str.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(alavu = qty))
                            onDirty()
                        },
                        placeholder = "1",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ElvanThiruthiUlleedu(
                        label = K.sellingRate.tr(),
                        value = if (item.vilai == 0.0) "" else if (item.vilai % 1.0 == 0.0) item.vilai.toInt().toString() else item.vilai.toString(),
                        onValueChange = { str ->
                            val rate = str.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(vilai = rate))
                            onDirty()
                        },
                        prefixText = "₹ ",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            // ── Discount & Row Total Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ElvanThiruthiUlleedu(
                        label = K.discount.tr(),
                        value = if (item.thallupadi == 0.0) "" else if (item.thallupadi % 1.0 == 0.0) item.thallupadi.toInt().toString() else item.thallupadi.toString(),
                        onValueChange = { str ->
                            val disc = str.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(thallupadi = disc))
                            onDirty()
                        },
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        suffixIcon = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val newType = if (item.thallupadiVagai == "%") "₹" else "%"
                                        onItemUpdated(item.copy(thallupadiVagai = newType))
                                        onDirty()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (item.thallupadiVagai == "%") "%" else "₹",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                )
                            }
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ElvanThiruthiUlleedu(
                        label = K.total.tr(),
                        value = CurrencyUtils.formatInr(item.rowTotal),
                        onValueChange = {},
                        enabled = false
                    )
                }
            }
        }
    }

    // ── Product Selection Bottom Sheet ──
    if (isPickerOpen) {
        ElvanSelectionBottomSheet(
            title = K.products.tr(),
            items = allProducts,
            currentValue = allProducts.firstOrNull { it.id.toString() == item.porulId },
            showSearch = true,
            onDismissRequest = { isPickerOpen = false },
            onSelected = { selected ->
                val primaryName = selected.porulPeyar["ta"] ?: selected.porulPeyar.values.firstOrNull().orEmpty()
                val secondaryName = selected.porulPeyar["en"] ?: ""
                onItemUpdated(
                    item.copy(
                        porulId = selected.id.toString(),
                        porulPeyar = primaryName,
                        porulPeyarEn = secondaryName,
                        hsnKuriyeedu = selected.hsnCode,
                        vilai = selected.vilai,
                        variVizhukkaadu = if (selected.variVeetham > 0) selected.variVeetham else 5.0,
                        alagu = selected.alagu
                    )
                )
                onDirty()
                isPickerOpen = false
            },
            itemLabelBuilder = { p ->
                p.porulPeyar["ta"] ?: p.porulPeyar.values.firstOrNull().orEmpty()
            },
            subtitleBuilder = { p ->
                val enName = p.porulPeyar["en"].orEmpty()
                val priceStr = if (p.vilai > 0) CurrencyUtils.formatInr(p.vilai) else ""
                listOf(enName, priceStr).filter { it.isNotEmpty() }.joinToString("  •  ")
            },
            searchFilter = { p, query ->
                val q = query.lowercase()
                p.porulPeyar.values.any { it.lowercase().contains(q) } || p.hsnCode.lowercase().contains(q)
            },
            onRequestAddNew = {
                isPickerOpen = false
                onRequestAddNewProduct()
            },
            addNewLabel = K.addNew.tr()
        )
    }
}
