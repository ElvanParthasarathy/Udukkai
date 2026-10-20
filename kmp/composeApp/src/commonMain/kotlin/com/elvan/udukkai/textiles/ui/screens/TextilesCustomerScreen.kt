package com.elvan.udukkai.textiles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.components.shell.VaangunarCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay

@Composable
fun TextilesCustomerScreen(
    onCustomerClick: (VaangunarTharavuru) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    isRefreshing: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedCustomerIds: Set<Long> = emptySet(),
    onToggleSelect: ((Long) -> Unit)? = null,
    onCustomerLongClick: ((Long) -> Unit)? = null
) {
    val customers = TextilesCustomerRepository.filteredMerchants
    val ff = LocalAppFontFamily.current

    val pageSize = 10
    var visibleCustomerCount by remember { mutableIntStateOf(pageSize) }

    LaunchedEffect(TextilesCustomerRepository.searchQuery) {
        visibleCustomerCount = pageSize
    }

    val displayedCustomers = remember(customers, visibleCustomerCount) {
        customers.take(visibleCustomerCount)
    }
    val hasMoreCustomers = customers.size > visibleCustomerCount

    var isSearchLoading by remember { mutableStateOf(false) }
    LaunchedEffect(TextilesCustomerRepository.searchQuery) {
        if (TextilesCustomerRepository.searchQuery.isNotBlank()) {
            isSearchLoading = true
            delay(300)
            isSearchLoading = false
        } else {
            isSearchLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = Dimens.ContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "customer_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            if (isSearchLoading || isRefreshing) {
                items(6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ContentPadding)
                    ) {
                        VaangunarCardSkeleton()
                    }
                }
            } else if (customers.isEmpty()) {
                item(key = "empty_customers") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.CustomNav.Customers,
                                contentDescription = null,
                                tint = LocalShellColors.current.textQuaternary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (TextilesCustomerRepository.searchQuery.isNotBlank()) {
                                    K.noMatchingCustomers.tr()
                                } else {
                                    K.noCustomersYet.tr()
                                },
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalShellColors.current.textTertiary
                                )
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = displayedCustomers,
                    key = { _, item -> "cust_${item.id}" }
                ) { index, item ->
                    val isSelected = selectedCustomerIds.contains(item.id)
                    val onCardClick: () -> Unit = {
                        if (isSelectionMode) {
                            onToggleSelect?.invoke(item.id)
                        } else {
                            onCustomerClick(item)
                        }
                    }
                    val onCardLongClick: () -> Unit = {
                        onCustomerLongClick?.invoke(item.id)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ContentPadding)
                    ) {
                        TextilesCustomerItemCard(
                            index = index,
                            merchant = item,
                            onClick = onCardClick,
                            colors = colors,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onLongClick = onCardLongClick
                        )
                    }
                }

                if (hasMoreCustomers) {
                    item(key = "cust_bottom_loader") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.customerColor,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            LaunchedEffect(Unit) {
                                delay(400)
                                visibleCustomerCount += pageSize
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TextilesCustomerItemCard(
    index: Int,
    merchant: VaangunarTharavuru,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = TextilesProfileRepository.profile
    val isBilingual = profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primaryName = merchant.peyar[primaryLang]
        ?: merchant.peyar["ta"]
        ?: merchant.peyar.values.firstOrNull()
        ?: "-"
    val secondaryName = if (isBilingual) merchant.peyar[secondaryLang].orEmpty() else ""

    val primaryCity = merchant.oor[primaryLang]
        ?: merchant.oor["ta"]
        ?: merchant.oor.values.firstOrNull()
        ?: ""
    val secondaryCity = if (isBilingual) merchant.oor[secondaryLang].orEmpty() else ""

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

                        if (isBilingual && secondaryName.isNotBlank() && secondaryName != primaryName) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = secondaryName.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textTertiary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = colors.textTertiary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (merchant.gstin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.accent.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GSTIN: ${merchant.gstin}",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent
                            )
                        )
                    }
                }

                val cityDisplay = if (isBilingual && secondaryCity.isNotBlank() && secondaryCity != primaryCity) {
                    "$primaryCity ($secondaryCity)"
                } else {
                    primaryCity
                }

                val details = listOf(cityDisplay, merchant.tholaipaesi).filter { it.isNotBlank() }.joinToString(" • ")
                if (details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = details,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            color = colors.textTertiary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
