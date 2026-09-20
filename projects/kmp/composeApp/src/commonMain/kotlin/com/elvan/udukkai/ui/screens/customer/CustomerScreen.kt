package com.elvan.udukkai.ui.screens.customer

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
import com.elvan.udukkai.ui.components.shell.VaangunarCardSkeleton
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
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Resolves a field dynamically based on bilingual settings, matching React's `getDynamicField` 1:1.
 * - In Coolie: isBilingual is always true.
 * - In Silk: isBilingual follows profile.iruMozhi.
 * - If single-language (!isBilingual) and !isPrimary: returns "" (shielded).
 * - If single-language and isPrimary, but primary is empty: falls back to secondary so customer is not blank.
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
 * CustomerScreen — Displays list of customers using VaangunarRepository.filteredMerchants.
 * Supports mode-aware customer cards: Coolie and Silk with 100% React visual and behavioral parity.
 */
@Composable
fun CustomerScreen(
    onMerchantClick: (VaangunarTharavuru) -> Unit,
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
    val merchants = VaangunarRepository.filteredMerchants
    val ff = LocalAppFontFamily.current

    val pageSize = 10
    var visibleMerchantCount by remember { mutableIntStateOf(pageSize) }

    LaunchedEffect(mode) {
        visibleMerchantCount = pageSize
    }

    val currentSearchQuery = VaangunarRepository.searchQuery
    var isSearchLoading by remember { mutableStateOf(false) }

    LaunchedEffect(currentSearchQuery) {
        visibleMerchantCount = pageSize
        if (currentSearchQuery.isNotBlank()) {
            isSearchLoading = true
            delay(300)
            isSearchLoading = false
        } else {
            isSearchLoading = false
        }
    }

    val displayedMerchants = remember(merchants, visibleMerchantCount) {
        merchants.take(visibleMerchantCount)
    }
    val hasMoreMerchants = merchants.size > visibleMerchantCount

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
                    VaangunarCardSkeleton()
                }
            } else if (merchants.isEmpty()) {
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
                                imageVector = MaterialSymbols.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = K.noCustomersYet.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = K.addFirstCustomer.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                        )
                    }
                }
            } else {
                itemsIndexed(displayedMerchants, key = { _, merchant -> merchant.id }) { index, merchant ->
                    val isSelected = selectedItemIds.contains(merchant.id)
                    val onCardClick: () -> Unit = {
                        if (isSelectionMode) {
                            onToggleSelect?.invoke(merchant.id)
                        } else {
                            onMerchantClick(merchant)
                        }
                    }
                    val onCardLongClick: () -> Unit = {
                        onItemLongClick?.invoke(merchant.id)
                    }

                    if (mode == AppMode.KOOLI) {
                        CoolieCustomerCard(
                            index = index,
                            merchant = merchant,
                            onClick = onCardClick,
                            onLongClick = onCardLongClick,
                            colors = colors,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected
                        )
                    } else {
                        SilkCustomerCard(
                            index = index,
                            merchant = merchant,
                            onClick = onCardClick,
                            onLongClick = onCardLongClick,
                            colors = colors,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected
                        )
                    }
                }

                if (hasMoreMerchants) {
                    item(key = "merchant_bottom_loader") {
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
                            visibleMerchantCount += pageSize
                        }
                    }
                }
            }
        }
    }
}


/**
 * Coolie customer card: 28dp index badge, name, secondary name, town/city.
 * Exact 1:1 port of React's CoolieMerchants.tsx with unified Pattiyalgal selection badge.
 */
@Composable
internal fun CoolieCustomerCard(
    index: Int,
    merchant: VaangunarTharavuru,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val profile = NiruvanaTharavugalRepository.getProfile(AppMode.KOOLI)
    val isBilingual = true // Coolie mode is ALWAYS bilingual
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primaryName = getDynamicField(merchant.peyar, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { merchant.peyar.values.firstOrNull() ?: "-" }
    val secondaryName = getDynamicField(merchant.peyar, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val primaryCity = getDynamicField(merchant.oor, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
    val secondaryCity = getDynamicField(merchant.oor, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

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
                icon = MaterialSymbols.CustomNav.CustomersFill,
                tint = colors.customerColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = primaryName.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 15.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (secondaryName.isNotBlank() && secondaryName != primaryName) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = secondaryName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (primaryCity.isNotBlank() || secondaryCity.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (primaryCity.isNotBlank()) {
                        Text(
                            text = primaryCity.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.6.sp,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (secondaryCity.isNotBlank() && secondaryCity != primaryCity) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = secondaryCity.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.8.sp,
                                color = colors.textSecondary.copy(alpha = 0.8f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Silk customer card: 28dp index badge, name, secondary name (if bilingual),
 * inline town/city with bullet separator (`Primary • Secondary`), and GSTIN.
 * Exact 1:1 port of React's Vanigargal.tsx with unified Pattiyalgal selection badge.
 */
@Composable
internal fun SilkCustomerCard(
    index: Int,
    merchant: VaangunarTharavuru,
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

    val primaryName = getDynamicField(merchant.peyar, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { merchant.peyar.values.firstOrNull() ?: "-" }
    val secondaryName = getDynamicField(merchant.peyar, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val primaryCity = getDynamicField(merchant.oor, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
    val secondaryCity = getDynamicField(merchant.oor, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val gstin = merchant.gstin.trim()

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
                icon = MaterialSymbols.CustomNav.CustomersFill,
                tint = colors.customerColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = primaryName.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 15.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Secondary Name - ONLY when bilingual mode is enabled in Silk profile
                if (isBilingual && secondaryName.isNotBlank() && secondaryName != primaryName) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = secondaryName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // City / Oor - Inline bullet format matching React: `Primary • Secondary`
                val cityText = if (isBilingual && secondaryCity.isNotBlank() && secondaryCity != primaryCity) {
                    if (primaryCity.isNotBlank()) "$primaryCity • $secondaryCity" else secondaryCity
                } else {
                    primaryCity
                }

                if (cityText.isNotBlank() || gstin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (cityText.isNotBlank()) {
                        Text(
                            text = cityText.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.6.sp,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // GSTIN label with explicit `GSTIN: ` prefix matching React
                    if (gstin.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GSTIN: $gstin",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.8.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
