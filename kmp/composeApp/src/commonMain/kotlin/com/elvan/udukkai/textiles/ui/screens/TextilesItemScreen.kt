package com.elvan.udukkai.textiles.ui.screens

import com.elvan.udukkai.ui.components.ExpressiveContainedLoader
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.ui.screens.item.TextilesItemCard
import com.elvan.udukkai.textiles.data.TextilesItemRepository
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
import com.elvan.udukkai.ui.components.shell.ItemCardSkeleton
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.create.components.ElvanPillShifter
import com.elvan.udukkai.ui.screens.create.components.PillShifterItem
import kotlinx.coroutines.delay

@Composable
fun TextilesItemScreen(
    onItemClick: (ItemModel) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    isRefreshing: Boolean = false,
    isSelectionMode: Boolean = false,
    selectedItemIds: Set<Long> = emptySet(),
    onToggleSelect: ((Long) -> Unit)? = null,
    onItemLongClick: ((Long) -> Unit)? = null
) {
    val items = TextilesItemRepository.filteredItems
    val ff = LocalAppFontFamily.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val allTabLabel = K.all.tr()
    val quantityTabLabel = K.quantity.tr()
    val weightTabLabel = K.weight.tr()

    val shifterItems = remember(allTabLabel, quantityTabLabel, weightTabLabel) {
        listOf(
            PillShifterItem(
                label = allTabLabel,
                icon = MaterialSymbols.Rounded.Apps,
                activeIcon = MaterialSymbols.Rounded.AppsFill
            ),
            PillShifterItem(
                label = quantityTabLabel,
                icon = MaterialSymbols.Rounded.Numbers,
                activeIcon = MaterialSymbols.Rounded.NumbersFill
            ),
            PillShifterItem(
                label = weightTabLabel,
                icon = MaterialSymbols.Rounded.Balance,
                activeIcon = MaterialSymbols.Rounded.BalanceFill
            )
        )
    }

    val tabFilteredItems = remember(items, selectedTabIndex) {
        when (selectedTabIndex) {
            1 -> items.filter { it.quantityType.lowercase() != "weight" }
            2 -> items.filter { it.quantityType.lowercase() == "weight" }
            else -> items
        }
    }

    val pageSize = 30
    var visibleItemCount by remember { mutableIntStateOf(pageSize) }

    LaunchedEffect(TextilesItemRepository.searchQuery, selectedTabIndex) {
        visibleItemCount = pageSize
    }

    val displayedItems = remember(tabFilteredItems, visibleItemCount) {
        tabFilteredItems.take(visibleItemCount)
    }
    val hasMoreItems = tabFilteredItems.size > visibleItemCount

    var isSearchLoading by remember { mutableStateOf(false) }
    LaunchedEffect(TextilesItemRepository.searchQuery) {
        if (TextilesItemRepository.searchQuery.isNotBlank()) {
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
            item(key = "item_top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            item(key = "item_measure_type_pill_shifter") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    ElvanPillShifter(
                        items = shifterItems,
                        selectedIndex = selectedTabIndex,
                        onIndexSelected = {
                            selectedTabIndex = it
                        },
                        colors = colors,
                        isSmall = true,
                        isFullWidth = false
                    )
                }
            }

            if (isSearchLoading || isRefreshing) {
                items(10) {
                    ItemCardSkeleton()
                }
            } else if (tabFilteredItems.isEmpty()) {
                item(key = "empty_items") {
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
                                imageVector = MaterialSymbols.CustomNav.Items,
                                contentDescription = null,
                                tint = LocalShellColors.current.textQuaternary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (TextilesItemRepository.searchQuery.isNotBlank()) {
                                    K.noMatchingItems.tr()
                                } else {
                                    K.noItemsYet.tr()
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
                    key = { _, item -> "item_${item.id}" }
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

                    TextilesItemCard(
                        index = index,
                        item = item,
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
                            ExpressiveContainedLoader(modifier = Modifier.size(34.dp), containerColor = colors.itemColor.copy(alpha = 0.12f), indicatorColor = colors.itemColor)
                            LaunchedEffect(Unit) {
                                visibleItemCount += pageSize
                            }
                        }
                    }
                }
            }
        }
    }
}
