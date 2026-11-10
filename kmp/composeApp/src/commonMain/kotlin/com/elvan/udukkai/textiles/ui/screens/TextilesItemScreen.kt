package com.elvan.udukkai.textiles.ui.screens

import com.elvan.udukkai.ui.components.EqualHeightCardRow
import com.elvan.udukkai.ui.components.EqualHeightCard

import com.elvan.udukkai.ui.components.shell.sidewaysTight
import com.elvan.udukkai.ui.components.dragSelect
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import androidx.compose.foundation.layout.*
import com.elvan.udukkai.ui.components.shell.elvanTopBandRow
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

    val allTabLabel = K.allTab.tr()
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


    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize().dragSelect(scrollState, { it in selectedItemIds }, { onItemLongClick?.invoke(it) }, { onToggleSelect?.invoke(it) }),
            contentPadding = PaddingValues(
                start = Dimens.ContentPadding,
                end = Dimens.ContentPadding,
                bottom = Dimens.ContentPaddingBottom.sidewaysTight()
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
                        .padding(bottom = 6.dp).elvanTopBandRow(com.elvan.udukkai.ui.components.shell.LocalElvanSideBar.current)
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

            if (isRefreshing) {
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
                if (isLandscape) {
                    val pairs = tabFilteredItems.chunked(2)
                    itemsIndexed(
                        items = pairs,
                        key = { _, pair -> "item_pair_${pair.joinToString("_") { it.id.toString() }}" }
                    ) { rowIndex, pair ->
                        EqualHeightCardRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.ContentPadding)
                        ) {
                            pair.forEachIndexed { colIndex, item ->
                                val cardIndex = (rowIndex * 2) + colIndex
                                val isSelected = selectedItemIds.contains(item.id)
                                val onCardClick: () -> Unit = {
                                    if (isSelectionMode) onToggleSelect?.invoke(item.id)
                                    else onItemClick(item)
                                }
                                val onCardLongClick: () -> Unit = {
                                    onItemLongClick?.invoke(item.id)
                                }
                                EqualHeightCard {
                                    TextilesItemCard(
                                        index = cardIndex,
                                        item = item,
                                        onClick = onCardClick,
                                        colors = colors,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onLongClick = onCardLongClick
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = tabFilteredItems,
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
                }
            }
        }
    }
}
