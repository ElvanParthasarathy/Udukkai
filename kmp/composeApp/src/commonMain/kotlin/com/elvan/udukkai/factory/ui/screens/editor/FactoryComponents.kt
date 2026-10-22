package com.elvan.udukkai.factory.ui.screens.editor

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.factory.data.FactoryProductRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.PrintLanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorCard
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel
import com.elvan.udukkai.ui.screens.editor.ElvanEditorTextField
import kotlin.math.floor
import kotlin.math.round

private fun cleanNum(v: Double, isWeight: Boolean = false): String {
    if (isWeight) {
        val formatted = ((round(v * 1000.0)) / 1000.0)
        val s = formatted.toString()
        val dotIdx = s.indexOf('.')
        return if (dotIdx == -1) {
            "$s.000"
        } else {
            val decimals = s.length - dotIdx - 1
            when {
                decimals >= 3 -> s.substring(0, dotIdx + 4)
                else -> s + "0".repeat(3 - decimals)
            }
        }
    }
    return if (v == floor(v)) v.toInt().toString() else v.toString()
}

/**
 * Line item card for Factory Invoice Editor.
 * Weight-based billing with floored row totals.
 */
@Composable
fun FactoryItemCard(
    item: FactoryItem,
    index: Int,
    itemCount: Int,
    onItemUpdated: (FactoryItem) -> Unit,
    onItemDeleted: () -> Unit,
    onItemCleared: () -> Unit,
    onDirty: () -> Unit,
    onRequestAddNewProduct: () -> Unit,
    onAddNewItem: (() -> Unit)? = null,
    onAddNewCharge: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    companyId: Long? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isPickerOpen by remember { mutableStateOf(false) }
    val allProducts = FactoryProductRepository.products
    val availableProducts = remember(allProducts, companyId) {
        allProducts.filter { it.isAvailableForCompany(companyId) }
    }

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
                text = "${K.item.tr()} #${index + 1}".preventBrokenLigatures(),
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
        ElvanEditorCard(
            padding = PaddingValues(16.dp),
            borderRadius = 24.dp
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 600.dp
                val config = PrintLanguageManager.getFactoryConfig()
                val primaryLang = config.primaryLanguage.code
                val secondaryLang = config.secondaryLanguage.code
                val isBilingual = config.isBilingual

                val primaryName = if (item.languageMap.isNotEmpty()) {
                    item.languageMap[primaryLang] ?: item.languageMap.values.firstOrNull().orEmpty()
                } else {
                    if (primaryLang == "ta") item.productName.ifEmpty { item.productNameEn }
                    else item.productNameEn.ifEmpty { item.productName }
                }
                val secondaryName = if (item.languageMap.isNotEmpty()) {
                    item.languageMap[secondaryLang] ?: ""
                } else {
                    if (secondaryLang == "en") item.productNameEn else item.productName
                }
                val displayName = primaryName.ifEmpty { item.productName.ifEmpty { item.productNameEn } }
                val containerBg = colors.iconBg

                val productSearchPill = @Composable {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ElvanEditorLabel(label = K.item.tr())
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
                                    text = (if (displayName.isNotEmpty()) displayName else K.item.tr()).preventBrokenLigatures(),
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


                    }
                }

                val weightInput = @Composable {
                    ElvanEditorTextField(
                        label = "${K.weight.tr()} (kg)",
                        value = if (item.weight == 0.0) "" else cleanNum(item.weight, isWeight = true),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(weight = parsed))
                            onDirty()
                        },
                        suffixText = "kg",
                        placeholder = "0.000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                val rateInput = @Composable {
                    ElvanEditorTextField(
                        label = "${K.rate.tr()} (₹)",
                        value = if (item.price == 0.0) "" else cleanNum(item.price),
                        onValueChange = {
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onItemUpdated(item.copy(price = parsed))
                            onDirty()
                        },
                        prefixText = "₹ ",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                val totalOutput = @Composable {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ElvanEditorLabel(label = "${K.total.tr()} (₹)")
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(colors.accent.copy(alpha = if (isDark) 0.15f else 0.08f))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = CurrencyUtils.formatInr(item.rowTotal.toDouble()),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                if (isWide) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        productSearchPill()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(modifier = Modifier.weight(1f)) { weightInput() }
                            Box(modifier = Modifier.weight(1f)) { rateInput() }
                            Box(modifier = Modifier.weight(1f)) { totalOutput() }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        productSearchPill()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) { weightInput() }
                            Box(modifier = Modifier.weight(1f)) { rateInput() }
                        }
                        totalOutput()
                    }
                }
            }
        }

        // ── Floating Action Buttons (Add Item & Add Other Charges) ──
        if (index == itemCount - 1 && (onAddNewItem != null || onAddNewCharge != null)) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onAddNewItem != null) {
                    val addProductShape = RoundedCornerShape(100.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(addProductShape)
                            .background(colors.iconBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = colors.accent),
                                onClick = onAddNewItem
                            ),
                        contentAlignment = Alignment.Center
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
                                text = K.addItemBtn.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }

                if (onAddNewCharge != null) {
                    val addChargeShape = RoundedCornerShape(100.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(addChargeShape)
                            .background(colors.iconBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = colors.accent),
                                onClick = onAddNewCharge
                            ),
                        contentAlignment = Alignment.Center
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
                                    fontSize = 13.sp,
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
        val config = PrintLanguageManager.getFactoryConfig()
        val primaryLang = config.primaryLanguage.code
        val secondaryLang = config.secondaryLanguage.code
        val isBilingual = config.isBilingual

        ElvanSelectionBottomSheet(
            title = K.products.tr(),
            items = availableProducts,
            currentValue = availableProducts.firstOrNull { it.id.toString() == item.productId },
            showSearch = true,
            onDismissRequest = { isPickerOpen = false },
            onSelected = { p: ProductModel ->
                val primary = if (p.name.isNotEmpty()) {
                    p.name[primaryLang] ?: p.name.values.firstOrNull().orEmpty()
                } else ""
                val secondary = if (p.name.isNotEmpty()) {
                    p.name[secondaryLang] ?: ""
                } else ""

                val priceForCompany = if (companyId != null && p.companyPrices.containsKey(companyId)) {
                    p.companyPrices[companyId] ?: p.price
                } else {
                    p.price
                }

                onItemUpdated(
                    item.copy(
                        productId = p.id.toString(),
                        productName = primary,
                        productNameEn = secondary,
                        price = if (priceForCompany > 0) priceForCompany else item.price,
                        languageMap = p.name
                    )
                )
                onDirty()
                isPickerOpen = false
            },
            itemLabelBuilder = { p ->
                if (p.name.isNotEmpty()) {
                    p.name[primaryLang] ?: p.name.values.firstOrNull().orEmpty()
                } else ""
            },
            subtitleBuilder = { p ->
                val sec = if (isBilingual && p.name.isNotEmpty()) p.name[secondaryLang].orEmpty() else ""
                val priceForComp = if (companyId != null && p.companyPrices.containsKey(companyId)) {
                    p.companyPrices[companyId] ?: p.price
                } else {
                    p.price
                }
                val priceStr = if (priceForComp > 0) CurrencyUtils.formatInr(priceForComp) else ""
                listOf(sec, priceStr).filter { it.isNotEmpty() }.joinToString(" • ")
            },
            searchFilter = { p, query ->
                val q = query.lowercase()
                p.name.values.any { it.lowercase().contains(q) }
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
 * Dynamic "Other Charge" line card for Factory mode.
 */
@Composable
fun FactoryAdditionalChargeCard(
    charge: FactoryAdditionalCharge,
    index: Int,
    onUpdated: (FactoryAdditionalCharge) -> Unit,
    onDeleted: () -> Unit,
    onDirty: () -> Unit,
    modifier: Modifier = Modifier,
    isBilingual: Boolean = false,
    primaryLang: String = "ta",
    secondaryLang: String = "en"
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
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
                    .padding(end = 4.dp, bottom = 6.dp)
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

        ElvanEditorCard(
            padding = PaddingValues(16.dp),
            borderRadius = 24.dp
        ) {
            if (isBilingual) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(1.3f)) {
                            ElvanEditorTextField(
                                label = "${K.chargeName.tr()} (${if (primaryLang == "ta") K.tamil.tr() else K.english.tr()})",
                                value = charge.name,
                                onValueChange = {
                                    onUpdated(charge.copy(name = it))
                                    onDirty()
                                },
                                placeholder = K.chargeName.tr()
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            ElvanEditorTextField(
                                label = "${K.total.tr()} (₹)",
                                value = if (charge.amount == 0.0) "" else cleanNum(charge.amount),
                                onValueChange = {
                                    val parsed = it.toDoubleOrNull() ?: 0.0
                                    onUpdated(charge.copy(amount = parsed))
                                    onDirty()
                                },
                                prefixText = "₹ ",
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }

                    ElvanEditorTextField(
                        label = "${K.chargeName.tr()} (${if (secondaryLang == "en") K.english.tr() else K.tamil.tr()})",
                        value = charge.nameSecondary,
                        onValueChange = {
                            onUpdated(charge.copy(nameSecondary = it))
                            onDirty()
                        },
                        placeholder = K.chargeName.tr()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1.3f)) {
                        ElvanEditorTextField(
                            label = K.chargeName.tr(),
                            value = charge.name,
                            onValueChange = {
                                onUpdated(charge.copy(name = it))
                                onDirty()
                            },
                            placeholder = K.chargeName.tr()
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ElvanEditorTextField(
                            label = "${K.total.tr()} (₹)",
                            value = if (charge.amount == 0.0) "" else cleanNum(charge.amount),
                            onValueChange = {
                                val parsed = it.toDoubleOrNull() ?: 0.0
                                onUpdated(charge.copy(amount = parsed))
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
}

/**
 * Bento grid for Factory Extra Charges (Wastage / Setharam grams, Ahimsa silk, Courier).
 */
@Composable
fun FactoryExtraChargesSection(
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
        ElvanEditorTextField(
            label = "${K.wastage.tr()} (g)",
            value = if (setharamGrams == 0.0) "" else cleanNum(setharamGrams),
            onValueChange = {
                val parsed = it.toDoubleOrNull() ?: 0.0
                onSetharamChanged(parsed)
                onDirty()
            },
            suffixText = "g",
            placeholder = "0",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ElvanEditorTextField(
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
                ElvanEditorTextField(
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
 * Grand totals breakdown card for Factory mode.
 */
@Composable
fun FactoryTotalsSection(
    totals: FactoryTotals,
    setharamGrams: Double,
    ahimsaPattuThogai: Double,
    thabaalThogai: Double,
    charges: List<FactoryAdditionalCharge>,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    ElvanEditorCard(
        padding = PaddingValues(20.dp),
        borderRadius = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subtotal
            FactoryTotalsRow(
                label = K.subtotal.tr(),
                value = CurrencyUtils.formatInr(totals.subtotal),
                labelWeight = FontWeight.SemiBold,
                valueWeight = FontWeight.Bold
            )

            // Ahimsa
            if (ahimsaPattuThogai > 0) {
                FactoryTotalsRow(
                    label = K.ahimsaSilk.tr(),
                    value = CurrencyUtils.formatInr(ahimsaPattuThogai)
                )
            }

            // Courier
            if (thabaalThogai > 0) {
                FactoryTotalsRow(
                    label = K.courier.tr(),
                    value = CurrencyUtils.formatInr(thabaalThogai)
                )
            }

            // Other charges
            for (charge in charges) {
                if (charge.amount > 0) {
                    FactoryTotalsRow(
                        label = charge.name.ifEmpty { K.other.tr() },
                        value = CurrencyUtils.formatInr(charge.amount)
                    )
                }
            }

            // Total Weight
            FactoryTotalsRow(
                label = K.totalWeight.tr(),
                value = "${cleanNum(totals.totalWeight, isWeight = true)} kg",
                labelWeight = FontWeight.SemiBold,
                valueWeight = FontWeight.Bold
            )

            // Setharam grams
            if (setharamGrams > 0) {
                FactoryTotalsRow(
                    label = "+ ${K.wastage.tr()} (g)",
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
                    text = CurrencyUtils.formatInr(totals.grandTotal),
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
private fun FactoryTotalsRow(
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
