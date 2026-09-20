package com.elvan.udukkai.ui.components.shell

import androidx.compose.runtime.*
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Configuration for a centralized selection bottom sheet.
 */
data class SelectionSheetConfig<T>(
    val title: String,
    val items: List<T>,
    val currentValue: T?,
    val itemLabelBuilder: (T) -> String,
    val subtitleBuilder: ((T) -> String?)? = null,
    val leadingBuilder: (@Composable (T) -> Unit)? = null,
    val showSearch: Boolean = false,
    val searchFilter: ((T, String) -> Boolean)? = null,
    val onRequestAddNew: (() -> Unit)? = null,
    val addNewLabel: String? = null,
    val onSelected: (T) -> Unit
)

/**
 * Controller to show and dismiss bottom sheets globally without local state boilerplate.
 */
class ElvanBottomSheetController {
    var currentConfig by mutableStateOf<SelectionSheetConfig<*>?>(null)
        private set

    fun <T> showSelection(
        title: String,
        items: List<T>,
        currentValue: T?,
        itemLabelBuilder: (T) -> String,
        subtitleBuilder: ((T) -> String?)? = null,
        leadingBuilder: (@Composable (T) -> Unit)? = null,
        showSearch: Boolean = false,
        searchFilter: ((T, String) -> Boolean)? = null,
        onRequestAddNew: (() -> Unit)? = null,
        addNewLabel: String? = null,
        onSelected: (T) -> Unit
    ) {
        currentConfig = SelectionSheetConfig(
            title = title,
            items = items,
            currentValue = currentValue,
            itemLabelBuilder = itemLabelBuilder,
            subtitleBuilder = subtitleBuilder,
            leadingBuilder = leadingBuilder,
            showSearch = showSearch,
            searchFilter = searchFilter,
            onRequestAddNew = onRequestAddNew,
            addNewLabel = addNewLabel,
            onSelected = onSelected
        )
    }

    fun dismiss() {
        currentConfig = null
    }
}

val LocalElvanBottomSheetController = staticCompositionLocalOf<ElvanBottomSheetController> {
    ElvanBottomSheetController()
}

/**
 * Host composable to place at the top level of the screen hierarchy / NirilChattagam.
 * Automatically renders any active selection bottom sheet from the controller.
 */
@Composable
fun ElvanBottomSheetHost(
    controller: ElvanBottomSheetController = LocalElvanBottomSheetController.current,
    colors: ShellColors = rememberShellColors()
) {
    val config = controller.currentConfig
    if (config != null) {
        @Suppress("UNCHECKED_CAST")
        val typedConfig = config as SelectionSheetConfig<Any?>
        ElvanSelectionBottomSheet(
            title = typedConfig.title,
            items = typedConfig.items,
            currentValue = typedConfig.currentValue,
            onSelected = { item ->
                typedConfig.onSelected(item)
            },
            onDismissRequest = { controller.dismiss() },
            itemLabelBuilder = { typedConfig.itemLabelBuilder(it) },
            subtitleBuilder = typedConfig.subtitleBuilder?.let { builder -> { builder(it) } },
            leadingBuilder = typedConfig.leadingBuilder?.let { builder -> { builder(it) } },
            showSearch = typedConfig.showSearch,
            searchFilter = typedConfig.searchFilter?.let { filter -> { item, q -> filter(item, q) } },
            onRequestAddNew = typedConfig.onRequestAddNew,
            addNewLabel = typedConfig.addNewLabel,
            colors = colors
        )
    }
}
