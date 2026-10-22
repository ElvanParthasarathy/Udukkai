package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.textiles.data.TextilesItemRepository
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
import com.elvan.udukkai.ui.screens.editor.ElvanEditorNumericField
import com.elvan.udukkai.ui.screens.editor.ElvanEditorTextField

/**
 * Single line item card for the Textiles (சவுளி) Invoice Editor.
 */
@Composable
fun TextilesItemCard(
    item: TextilesItem,
    index: Int,
    itemCount: Int,
    onItemUpdated: (TextilesItem) -> Unit,
    onItemDeleted: () -> Unit,
    onItemCleared: () -> Unit,
    onDirty: () -> Unit,
    onRequestAddNewItem: () -> Unit,
    modifier: Modifier = Modifier,
    companyId: Long? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isPickerOpen by remember { mutableStateOf(false) }
    val allItems = TextilesItemRepository.items
    val availableItems = remember(allItems, companyId) {
        allItems.filter { it.isAvailableForCompany(companyId) }
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
            // ── Item Picker Pill ──
            Column(modifier = Modifier.fillMaxWidth()) {
                ElvanEditorLabel(label = K.item.tr())

                val config = PrintLanguageManager.getTextilesConfig()
                val primaryName = if (item.languageMap.isNotEmpty()) {
                    item.languageMap[config.primaryLanguage.code] ?: item.languageMap.values.firstOrNull().orEmpty()
                } else {
                    item.name
                }
                val secondaryName = if (config.isBilingual) {
                    if (item.languageMap.isNotEmpty()) {
                        item.languageMap[config.secondaryLanguage.code].orEmpty()
                    } else {
                        item.itemNameEn
                    }
                } else ""
                val displayName = primaryName.ifEmpty { secondaryName }
                val containerBg = colors.iconBg

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
                            text = (if (displayName.isNotEmpty()) displayName else K.items.tr()).preventBrokenLigatures(),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, radius = 18.dp)
                                    ) {
                                        onItemCleared()
                                        onDirty()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Close,
                                    contentDescription = K.cancel.tr(),
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
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

                // Subtitle Info (Secondary/English name • GST%)
                val subtitleParts = mutableListOf<String>()

                if (item.taxRate > 0) {
                    subtitleParts.add("GST ${item.taxRate.toInt()}%")
                }
                if (subtitleParts.isNotEmpty()) {
                    Text(
                        text = subtitleParts.joinToString("  •  "),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        ),
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp)
                    )
                }
            }

            // ── Quantity & Rate Row ──
            val isWeightItem = item.unit.equals("kg", ignoreCase = true)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ElvanEditorNumericField(
                        label = if (isWeightItem) K.weight.tr() else K.quantity.tr(),
                        value = item.quantity,
                        onValueChange = { qty ->
                            onItemUpdated(item.copy(quantity = qty))
                        },
                        placeholder = if (isWeightItem) "0.000" else "1",
                        suffixText = if (isWeightItem) "kg" else null,
                        isWeight = isWeightItem,
                        key = item.id,
                        onDirty = onDirty
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    ElvanEditorNumericField(
                        label = K.sellingRate.tr(),
                        value = item.price,
                        onValueChange = { rate ->
                            onItemUpdated(item.copy(price = rate))
                        },
                        prefixText = "₹ ",
                        placeholder = "0",
                        key = item.id,
                        onDirty = onDirty
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
                    ElvanEditorNumericField(
                        label = K.discount.tr(),
                        value = item.discount,
                        onValueChange = { disc ->
                            onItemUpdated(item.copy(discount = disc))
                        },
                        placeholder = "0",
                        key = item.id,
                        onDirty = onDirty,
                        suffixIcon = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val newType = if (item.discountType == "%") "₹" else "%"
                                        onItemUpdated(item.copy(discountType = newType))
                                        onDirty()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (item.discountType == "%") "%" else "₹",
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
                    ElvanEditorTextField(
                        label = K.total.tr(),
                        value = CurrencyUtils.formatInr(item.rowTotal),
                        onValueChange = {},
                        enabled = false
                    )
                }
            }
        }
    }

    // ── Item Selection Bottom Sheet ──
    if (isPickerOpen) {
        ElvanSelectionBottomSheet(
            title = K.items.tr(),
            items = availableItems,
            currentValue = availableItems.firstOrNull { it.id.toString() == item.itemId },
            showSearch = true,
            onDismissRequest = { isPickerOpen = false },
            onSelected = { selected ->
                val config = PrintLanguageManager.getTextilesConfig()
                val primaryName = selected.name[config.primaryLanguage.code] ?: selected.name.values.firstOrNull().orEmpty()
                val secondaryName = if (config.isBilingual) selected.name[config.secondaryLanguage.code].orEmpty() else ""
                val effectivePrice = selected.getEffectivePrice(companyId)
                val priceToUse = if (effectivePrice > 0.0) effectivePrice else selected.price
                onItemUpdated(
                    item.copy(
                        itemId = selected.id.toString(),
                        itemName = primaryName,
                        itemNameEn = secondaryName,
                        languageMap = selected.name,
                        hsnCode = selected.hsnCode,
                        price = priceToUse,
                        taxRate = if (selected.taxRate > 0) selected.taxRate else 5.0,
                        unit = selected.unit
                    )
                )
                onDirty()
                isPickerOpen = false
            },
            itemLabelBuilder = { p ->
                val config = PrintLanguageManager.getTextilesConfig()
                p.name[config.primaryLanguage.code] ?: p.name.values.firstOrNull().orEmpty()
            },
            subtitleBuilder = { p ->
                val config = PrintLanguageManager.getTextilesConfig()
                val subName = if (config.isBilingual) p.name[config.secondaryLanguage.code].orEmpty() else ""
                val price = p.getEffectivePrice(companyId)
                val priceStr = if (price > 0) CurrencyUtils.formatInr(price) else ""
                listOf(subName, priceStr).filter { it.isNotEmpty() }.joinToString("  •  ")
            },
            searchFilter = { p, query ->
                val q = query.lowercase()
                p.name.values.any { it.lowercase().contains(q) } || p.hsnCode.lowercase().contains(q)
            },
            onRequestAddNew = {
                isPickerOpen = false
                onRequestAddNewItem()
            },
            addNewLabel = K.addNew.tr()
        )
    }
}
