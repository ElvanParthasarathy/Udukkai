package com.elvan.udukkai.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.elvan.udukkai.ui.components.shell.PorulCardSkeleton
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Resolves a field dynamically based on bilingual settings, matching React's `getDynamicField` 1:1.
 */
private fun getDynamicField(
    map: Map<String, String>,
    isPrimary: Boolean,
    isBilingual: Boolean,
    primaryLang: String,
    secondaryLang: String
): String {
    if (!isBilingual && !isPrimary) {
        return ""
    }
    val targetLang = if (isPrimary) primaryLang else secondaryLang
    val exactVal = map[targetLang]?.trim()
    if (!exactVal.isNullOrEmpty()) {
        return exactVal
    }

    // Safety fallback for single-language mode:
    if (!isBilingual && isPrimary) {
        val fallbackVal = map[secondaryLang]?.trim()
        if (!fallbackVal.isNullOrEmpty()) {
            return fallbackVal
        }
    }

    if (isPrimary) {
        return map.values.firstOrNull { it.isNotBlank() } ?: ""
    }
    return ""
}

/**
 * ProductScreen — Displays list of items using PorulRepository.filteredItems.
 * Supports mode-aware item cards: Coolie and Silk matching Flutter 1:1.
 */
@Composable
fun ProductScreen(
    onItemClick: (PorulTharavuru) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    mode: AppMode = LocalAppMode.current,
    colors: ShellColors = rememberShellColors(),
    isRefreshing: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onToggleSelect: ((Long) -> Unit)? = null,
    onItemLongClick: ((Long) -> Unit)? = null
) {
    val items = PorulRepository.filteredItems
    val ff = LocalAppFontFamily.current

    val pageSize = 10
    var visibleItemCount by remember { mutableIntStateOf(pageSize) }

    LaunchedEffect(mode) {
        visibleItemCount = pageSize
    }

    val currentSearchQuery = PorulRepository.searchQuery
    var isSearchLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentSearchQuery) {
        visibleItemCount = pageSize
        if (currentSearchQuery.isNotBlank()) {
            isSearchLoading = true
            delay(300)
            isSearchLoading = false
        } else {
            isSearchLoading = false
        }
    }

    val displayedItems = remember(items, visibleItemCount) {
        items.take(visibleItemCount)
    }
    val hasMoreItems = items.size > visibleItemCount

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.ContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
        ) {
            item(key = "top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            if (isSearchLoading || isRefreshing) {
                items(10) {
                    PorulCardSkeleton()
                }
            } else if (items.isEmpty()) {
                item(key = "empty_state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 56.dp, bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(colors.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = K.noProductsYet.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = K.addFirstProduct.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                        )
                    }
                }
            } else {
                itemsIndexed(displayedItems, key = { _, item -> item.id }) { index, item ->
                    val isSelected = selectedItemIds.contains(item.id)
                    val onCardClick: () -> Unit = {
                        if (isSelectionMode) {
                            onToggleSelect?.invoke(item.id)
                        } else {
                            onItemClick(item)
                        }
                    }
                    val onCardLongClick: () -> Unit = {
                        onItemLongClick?.invoke(item.id)
                    }
                    if (mode == AppMode.KOOLI) {
                        CoolieProductCard(
                            index = index,
                            porul = item,
                            onClick = onCardClick,
                            onLongClick = onCardLongClick,
                            colors = colors,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected
                        )
                    } else {
                        SilkProductCard(
                            index = index,
                            porul = item,
                            onClick = onCardClick,
                            onLongClick = onCardLongClick,
                            colors = colors,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected
                        )
                    }
                }

                if (hasMoreItems) {
                    item(key = "porul_bottom_loader") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = colors.accent
                            )
                        }
                        LaunchedEffect(Unit) {
                            delay(350)
                            visibleItemCount += pageSize
                        }
                    }
                }
            }
        }
    }
}

/**
 * Coolie product card: 28dp index badge, product name (15.2sp bold), secondary name (13sp medium).
 * Exact 1:1 port of Flutter's _CooliePorulCard with unified Pattiyalgal selection badge.
 */
@Composable
internal fun CoolieProductCard(
    index: Int,
    porul: PorulTharavuru,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val profile = NiruvanaTharavugalRepository.getProfile(AppMode.KOOLI)
    val isBilingual = true // Coolie mode is always bilingual
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primary = getDynamicField(porul.porulPeyar, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { porul.porulPeyar.values.firstOrNull() ?: "-" }
    val secondary = getDynamicField(porul.porulPeyar, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

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
                icon = MaterialSymbols.CustomNav.ProductsFill,
                tint = colors.productColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product name column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Row 1: Product Name + Chevron
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

/**
 * Silk product card: 28dp index badge, product name, secondary name,
 * HSN/Tax row, measure type label ("அளவு • Quantity" / "எடை • Weight"), and price.
 * Matches React 1:1 with unified Pattiyalgal circular selection graphics.
 */
@Composable
internal fun SilkProductCard(
    index: Int,
    porul: PorulTharavuru,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val profile = NiruvanaTharavugalRepository.getProfile(AppMode.PATTU)
    val isBilingual = profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primary = getDynamicField(porul.porulPeyar, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { porul.porulPeyar.values.firstOrNull() ?: "-" }
    val secondary = getDynamicField(porul.porulPeyar, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

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
                icon = MaterialSymbols.CustomNav.ProductsFill,
                tint = colors.productColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Product details Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Row 1: Product Name + Chevron
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

                // Bottom Row: HSN/Tax + Measure Type (left) and Rate (right), matching React
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        val hsnText = if (porul.hsnCode.isNotBlank()) "HSN: ${porul.hsnCode}" else ""
                        val gstRate = if (porul.variVeetham % 1.0 == 0.0) porul.variVeetham.toLong().toString() else porul.variVeetham.toString()
                        val taxText = if (porul.variVeetham > 0.0) "Tax: $gstRate%" else ""
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

                        // Measure Type: "எடை • Weight" or "அளவு • Quantity" in accent color matching React
                        val measureLabel = if (porul.alavuVagai == "weight") "எடை • Weight" else "அளவு • Quantity"
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

                    val priceText = if (porul.vilai > 0.0) {
                        val formatted = if (porul.vilai % 1.0 == 0.0) porul.vilai.toLong().toString() else porul.vilai.toString()
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
