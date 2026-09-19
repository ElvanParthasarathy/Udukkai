package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
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
import kotlin.math.floor

private fun cleanNum(v: Double): String {
    return if (v == floor(v)) v.toInt().toString() else v.toString()
}

/**
 * Line item card for Coolie Invoice Editor.
 * Weight-based billing with floored row totals. Matches Flutter's `KooliUrupadiKooru` 1:1.
 */
@Composable
fun KooliUrupadiAttai(
    item: KooliUrupadi,
    index: Int,
    itemCount: Int,
    onItemUpdated: (KooliUrupadi) -> Unit,
    onItemDeleted: () -> Unit,
    onItemCleared: () -> Unit,
    onDirty: () -> Unit,
    onRequestAddNewProduct: () -> Unit,
    onAddNewItem: (() -> Unit)? = null,
    onAddNewCharge: (() -> Unit)? = null,
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
            .padding(bottom = 8.dp)
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
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 600.dp
                val displayName = item.porulPeyar.ifEmpty { item.porulPeyarEn }
                val containerBg = colors.iconBg

                val productSearchPill = @Composable {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ElvanThiruthiThalaippu(label = K.product.tr())
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

                        if (item.porulPeyarEn.isNotEmpty() && item.porulPeyarEn != item.porulPeyar) {
                            Text(
                                text = item.porulPeyarEn,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                ),
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }
                }

                val weightInput = @Composable {
                    ElvanThiruthiUlleedu(
                        label = K.weight.tr(),
                        value = if (item.edai == 0.0) "" else cleanNum(item.edai),
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(edai = parsed))
                            onDirty()
                        },
                        placeholder = "0.000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                val rateInput = @Composable {
                    ElvanThiruthiUlleedu(
                        label = K.sellingRate.tr(),
                        value = if (item.vilai == 0.0) "" else cleanNum(item.vilai),
                        onValueChange = { str ->
                            val parsed = str.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(vilai = parsed))
                            onDirty()
                        },
                        prefixText = "₹ ",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                val totalOutput = @Composable {
                    ElvanThiruthiUlleedu(
                        label = K.total.tr(),
                        value = CurrencyUtils.formatInr(item.varisaiThogai.toDouble()),
                        onValueChange = {},
                        enabled = false
                    )
                }

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(3f)) { productSearchPill() }
                        Box(modifier = Modifier.weight(1f)) { weightInput() }
                        Box(modifier = Modifier.weight(1f)) { rateInput() }
                        Box(modifier = Modifier.weight(1f)) { totalOutput() }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        productSearchPill()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) { weightInput() }
                            Box(modifier = Modifier.weight(1f)) { rateInput() }
                        }
                        totalOutput()
                    }
                }
            }
        }

        // ── Stadium Action Buttons (+ சேர் & + பிற வரவு சேர்) under the last item ──
        if (index == itemCount - 1 && (onAddNewItem != null || onAddNewCharge != null)) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onAddNewItem != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onAddNewItem() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Add,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = K.addBtn.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }

                if (onAddNewCharge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true)
                            ) { onAddNewCharge() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Add,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = K.addOtherCharges.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
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
                        vilai = selected.vilai,
                        mozhiMap = selected.porulPeyar
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
                p.porulPeyar.values.any { it.lowercase().contains(q) }
            },
            onRequestAddNew = {
                isPickerOpen = false
                onRequestAddNewProduct()
            },
            addNewLabel = K.addNew.tr()
        )
    }
}

/**
 * Dynamic "Other Charge" line card. Matches Flutter's `KooliPiraVarivuKooru` 1:1.
 */
@Composable
fun KooliPiraVarivuAttai(
    charge: PiraVarivu,
    index: Int,
    onUpdated: (PiraVarivu) -> Unit,
    onDeleted: () -> Unit,
    onDirty: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        // ── Header: "பிற #N" + trash icon ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${K.other.tr()} #${index + 1}".preventBrokenLigatures(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    .clickable { onDeleted() },
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

        ElvanThiruthiAttai(
            padding = PaddingValues(16.dp),
            borderRadius = 24.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(2f)) {
                    ElvanThiruthiUlleedu(
                        label = K.chargeName.tr(),
                        value = charge.peyar,
                        onValueChange = {
                            onUpdated(charge.copy(peyar = it))
                            onDirty()
                        },
                        placeholder = K.chargeName.tr()
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ElvanThiruthiUlleedu(
                        label = "${K.total.tr()} (₹)",
                        value = if (charge.thogai == 0.0) "" else cleanNum(charge.thogai),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdated(charge.copy(thogai = parsed))
                            onDirty()
                        },
                        prefixText = "₹ ",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }
    }
}

/**
 * Bento grid for Coolie Extra Charges (Setharam grams, Ahimsa silk, Courier).
 * Matches Flutter's `KooliMelthogaiKooru` 1:1.
 */
@Composable
fun KooliMelthogaiKooru(
    setharamGrams: Double,
    ahimsaPattuThogai: Double,
    thabaalThogai: Double,
    onSetharamChanged: (Double) -> Unit,
    onAhimsaChanged: (Double) -> Unit,
    onThabaalChanged: (Double) -> Unit,
    onDirty: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ElvanThiruthiUlleedu(
            label = K.wastage.tr(),
            value = if (setharamGrams == 0.0) "" else cleanNum(setharamGrams),
            onValueChange = {
                val parsed = it.toDoubleOrNull() ?: 0.0
                onSetharamChanged(parsed)
                onDirty()
            },
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ElvanThiruthiUlleedu(
                    label = K.ahimsaSilkAmount.tr(),
                    value = if (ahimsaPattuThogai == 0.0) "" else cleanNum(ahimsaPattuThogai),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onAhimsaChanged(parsed)
                        onDirty()
                    },
                    prefixText = "₹ ",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                ElvanThiruthiUlleedu(
                    label = K.courierCharge.tr(),
                    value = if (thabaalThogai == 0.0) "" else cleanNum(thabaalThogai),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        onThabaalChanged(parsed)
                        onDirty()
                    },
                    prefixText = "₹ ",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

/**
 * Grand totals breakdown card for Coolie mode. Matches Flutter's `KooliMothangalKooru` 1:1.
 */
@Composable
fun KooliMothangalKooru(
    totals: KooliMothangal,
    setharamGrams: Double,
    ahimsaPattuThogai: Double,
    thabaalThogai: Double,
    piraVarivugal: List<PiraVarivu>,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    ElvanThiruthiAttai(
        padding = PaddingValues(20.dp),
        borderRadius = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subtotal
            KooliTotalsRow(
                label = K.subtotal.tr(),
                value = CurrencyUtils.formatInr(totals.adippadaiMothangal),
                labelWeight = FontWeight.SemiBold,
                valueWeight = FontWeight.Bold
            )

            // Ahimsa
            if (ahimsaPattuThogai > 0) {
                KooliTotalsRow(
                    label = K.ahimsaSilk.tr(),
                    value = CurrencyUtils.formatInr(ahimsaPattuThogai)
                )
            }

            // Courier
            if (thabaalThogai > 0) {
                KooliTotalsRow(
                    label = K.courier.tr(),
                    value = CurrencyUtils.formatInr(thabaalThogai)
                )
            }

            // Other charges
            for (charge in piraVarivugal) {
                if (charge.thogai > 0) {
                    KooliTotalsRow(
                        label = charge.peyar.ifEmpty { K.other.tr() },
                        value = CurrencyUtils.formatInr(charge.thogai)
                    )
                }
            }

            // Total Weight
            KooliTotalsRow(
                label = K.totalWeight.tr(),
                value = "${totals.mothaEdai} Kg",
                labelWeight = FontWeight.SemiBold,
                valueWeight = FontWeight.Bold
            )

            // Setharam grams
            if (setharamGrams > 0) {
                KooliTotalsRow(
                    label = "+ ${K.setharam.tr()}",
                    value = "${cleanNum(setharamGrams)} g"
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
            )

            // Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = K.total.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                )

                Text(
                    text = CurrencyUtils.formatInr(totals.perumMothangal),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun KooliTotalsRow(
    label: String,
    value: String,
    labelWeight: FontWeight = FontWeight.Medium,
    valueWeight: FontWeight = FontWeight.SemiBold
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.preventBrokenLigatures(),
            style = TextStyle(
                fontFamily = ff,
                fontSize = 14.sp,
                fontWeight = labelWeight,
                color = colors.textSecondary
            )
        )

        Text(
            text = value,
            style = TextStyle(
                fontFamily = ff,
                fontSize = 14.sp,
                fontWeight = valueWeight,
                color = colors.textPrimary
            )
        )
    }
}
