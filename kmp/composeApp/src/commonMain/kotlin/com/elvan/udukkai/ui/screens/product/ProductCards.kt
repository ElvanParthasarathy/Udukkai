package com.elvan.udukkai.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.ProductModel
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.navigation.MaterialSymbols

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

@Composable
fun FactoryProductCard(
    index: Int,
    porul: ProductModel,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = FactoryProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primary = getDynamicField(porul.name, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { porul.name.values.firstOrNull() ?: "-" }
    val secondary = getDynamicField(porul.name, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        padding = PaddingValues(
            horizontal = Dimens.CardPaddingHorizontal,
            vertical = Dimens.CardPaddingVertical
        ),
        borderRadius = Dimens.CardRadius
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.Inventory2,
                tint = colors.productColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primary.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 15.2.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isBilingual && secondary.isNotBlank() && secondary != primary) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = secondary.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalShellColors.current.textSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
fun TextilesProductCard(
    index: Int,
    porul: ProductModel,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = TextilesProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primary = getDynamicField(porul.name, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { porul.name.values.firstOrNull() ?: "-" }
    val secondary = getDynamicField(porul.name, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
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
                tint = colors.productColor
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
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primary.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 15.2.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isBilingual && secondary.isNotBlank() && secondary != primary) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = secondary.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalShellColors.current.textSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        val hsnText = if (porul.hsnCode.isNotBlank()) "HSN: ${porul.hsnCode}" else ""
                        val gstRate = if (porul.taxRate % 1.0 == 0.0) porul.taxRate.toLong().toString() else porul.taxRate.toString()
                        val taxText = if (porul.taxRate > 0.0) "Tax: $gstRate%" else ""
                        val hsnTaxLine = if (hsnText.isNotBlank() && taxText.isNotBlank()) "$hsnText • $taxText" else "$hsnText$taxText"

                        if (hsnTaxLine.isNotBlank()) {
                            Text(
                                text = hsnTaxLine,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.6.sp,
                                    color = colors.textSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val measureLabel = if (porul.quantityType == "weight") "எடை • Weight" else "அளவு • Quantity"
                        Text(
                            text = measureLabel,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.8.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.accent
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val priceText = if (porul.price > 0.0) {
                        val formatted = if (porul.price % 1.0 == 0.0) porul.price.toLong().toString() else porul.price.toString()
                        "₹$formatted"
                    } else {
                        "-"
                    }
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
