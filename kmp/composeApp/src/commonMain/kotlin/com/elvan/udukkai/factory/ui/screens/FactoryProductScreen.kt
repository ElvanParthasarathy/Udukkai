package com.elvan.udukkai.factory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.ui.screens.product.FactoryProductCard
import com.elvan.udukkai.factory.data.FactoryProductRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
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
import com.elvan.udukkai.ui.components.shell.PorulCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay

@Composable
fun FactoryProductScreen(
    onItemClick: (PorulTharavuru) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    isRefreshing: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onToggleSelect: ((Long) -> Unit)? = null,
    onItemLongClick: ((Long) -> Unit)? = null
) {
    val items = FactoryProductRepository.filteredItems
    val ff = LocalAppFontFamily.current

    val pageSize = 10
    var visibleItemCount by remember { mutableIntStateOf(pageSize) }

    LaunchedEffect(FactoryProductRepository.searchQuery) {
        visibleItemCount = pageSize
    }

    val displayedItems = remember(items, visibleItemCount) {
        items.take(visibleItemCount)
    }
    val hasMoreItems = items.size > visibleItemCount

    var isSearchLoading by remember { mutableStateOf(false) }
    LaunchedEffect(FactoryProductRepository.searchQuery) {
        if (FactoryProductRepository.searchQuery.isNotBlank()) {
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
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.ContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacing)
        ) {
            item(key = "product_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            if (isSearchLoading || isRefreshing) {
                items(10) {
                    PorulCardSkeleton()
                }
            } else if (items.isEmpty()) {
                item(key = "empty_products") {
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
                                imageVector = MaterialSymbols.CustomNav.Products,
                                contentDescription = null,
                                tint = LocalShellColors.current.textQuaternary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (FactoryProductRepository.searchQuery.isNotBlank()) {
                                    K.noMatchingProducts.tr()
                                } else {
                                    K.noProductsYet.tr()
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
                    items = displayedItems,
                    key = { _, item -> "porul_${item.id}" }
                ) { index, item ->
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

                    FactoryProductCard(
                        index = index,
                        porul = item,
                        onClick = onCardClick,
                        colors = colors,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onLongClick = onCardLongClick
                    )
                }

                if (hasMoreItems) {
                    item(key = "bottom_loader") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.productColor,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            LaunchedEffect(Unit) {
                                delay(400)
                                visibleItemCount += pageSize
                            }
                        }
                    }
                }
            }
        }
    }
}
