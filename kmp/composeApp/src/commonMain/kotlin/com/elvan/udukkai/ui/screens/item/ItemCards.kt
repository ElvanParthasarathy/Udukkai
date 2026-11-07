package com.elvan.udukkai.ui.screens.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.data.settings.BusinessProfile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCardSecondLine
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr

@Composable
private fun resolveItemCompanyTag(
    item: ItemModel,
    profiles: List<BusinessProfile>,
    primaryLang: String
): String {
    if (item.companyIds.isEmpty()) {
        return K.allCompanies.tr()
    }
    return item.companyIds.mapNotNull { cId ->
        val p = profiles.find { it.id == cId }
        p?.shortName?.trim()?.takeIf { it.isNotEmpty() }
            ?: p?.businessName?.get(primaryLang)?.trim()?.takeIf { it.isNotEmpty() }
            ?: p?.businessName?.values?.firstOrNull { it.isNotBlank() }?.trim()
            ?: "Company #$cId"
    }.joinToString(", ").ifEmpty { K.allCompanies.tr() }
}

private fun getDynamicField(
    map: Map<String, String>,
    isPrimary: Boolean,
    isBilingual: Boolean,
    primaryLang: String,
    secondaryLang: String
): String {
    if (!isBilingual && !isPrimary) return ""
    val targetLang = if (isPrimary) primaryLang else secondaryLang
    val exactVal = map[targetLang]?.trim()
    if (!exactVal.isNullOrEmpty()) return exactVal
    if (!isBilingual && isPrimary) {
        val fallbackVal = map[secondaryLang]?.trim()
        if (!fallbackVal.isNullOrEmpty()) return fallbackVal
    }
    if (isPrimary) return map.values.firstOrNull { it.isNotBlank() } ?: ""
    return ""
}

/**
 * The item's name in the second language, in grey under the main name, with the short label of its businesses at the
 * right end when it is not offered by all of them.
 */
@Composable
private fun ItemSecondaryName(
    item: ItemModel,
    primary: String,
    isBilingual: Boolean,
    primaryLang: String,
    secondaryLang: String,
    profiles: List<BusinessProfile>,
    colors: ShellColors
) {
    val secondary = getDynamicField(item.name, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)
        .takeIf { it != primary }.orEmpty()
    val label = if (item.companyIds.isNotEmpty()) resolveItemCompanyTag(item, profiles, primaryLang) else ""
    ElvanCardSecondLine(secondary, label, colors.textSecondary, colors.textTertiary, LocalAppFontFamily.current)
}

@Composable
fun FactoryItemCard(
    index: Int,
    item: ItemModel,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = FactoryProfileRepository.profile
    val profiles = FactoryProfileRepository.allProfiles
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val primary = getDynamicField(item.name, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { item.name.values.firstOrNull() ?: "-" }

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        isSelectionMode = isSelectionMode,
        padding = PaddingValues(
            horizontal = Dimens.CardPaddingHorizontal,
            vertical = Dimens.CardPaddingVertical
        ),
        borderRadius = Dimens.CardRadius
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.Inventory2,
                tint = colors.itemColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    var primaryLineCount by remember { mutableStateOf<Int?>(null) }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primary.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            maxLines = primaryLineCount ?: 2,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layoutResult ->
                                if (!isSelectionMode) {
                                    primaryLineCount = layoutResult.lineCount
                                }
                            }
                        )
                        ItemSecondaryName(item, primary, isBilingual, primaryLang, secondaryLang, profiles, colors)

                    }

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TextilesItemCard(
    index: Int,
    item: ItemModel,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = TextilesProfileRepository.profile
    val profiles = TextilesProfileRepository.allProfiles
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val primary = getDynamicField(item.name, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { item.name.values.firstOrNull() ?: "-" }

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        isSelectionMode = isSelectionMode,
        padding = PaddingValues(
            horizontal = Dimens.CardPaddingHorizontal,
            vertical = Dimens.CardPaddingVertical
        ),
        borderRadius = Dimens.CardRadius
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.Inventory2,
                tint = colors.itemColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    var primaryLineCount by remember { mutableStateOf<Int?>(null) }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primary.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            maxLines = primaryLineCount ?: 2,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layoutResult ->
                                if (!isSelectionMode) {
                                    primaryLineCount = layoutResult.lineCount
                                }
                            }
                        )
                        ItemSecondaryName(item, primary, isBilingual, primaryLang, secondaryLang, profiles, colors)
                    }

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        val hsnText = if (item.hsnCode.isNotBlank()) "HSN: ${item.hsnCode}" else ""
                        val gstRate = if (item.taxRate % 1.0 == 0.0) item.taxRate.toLong().toString() else item.taxRate.toString()
                        val taxText = if (item.taxRate > 0.0) "Tax: $gstRate%" else ""
                        val hsnTaxLine = if (hsnText.isNotBlank() && taxText.isNotBlank()) "$hsnText • $taxText" else "$hsnText$taxText"

                        if (hsnTaxLine.isNotBlank()) {
                            Text(
                                text = hsnTaxLine,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.5.sp,
                                    color = colors.textSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val priceText = if (item.price > 0.0) {
                        com.elvan.udukkai.core.utils.CurrencyUtils.formatInr(item.price, showDecimals = item.price % 1.0 != 0.0)
                    } else {
                        "-"
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        val measureLabel = if (item.quantityType == "weight") K.weight.tr() else K.quantity.tr()
                        Text(
                            text = measureLabel.preventBrokenLigatures(),
                            style = TextStyle(fontFamily = ff, fontSize = 12.sp, color = colors.textSecondary),
                            maxLines = 1
                        )
                        Text(
                            text = priceText,
                            style = TextStyle(
                                fontFamily = ff,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (priceText.length > 11) 12.8.sp else 15.2.sp,
                                color = colors.accent
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
