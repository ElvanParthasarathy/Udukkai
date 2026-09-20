package com.elvan.udukkai.ui.screens.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.theme.gradientFor
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

val LocalEditorAccentColor = compositionLocalOf<Color?> { null }

/**
 * Numbered Section Header for Elvan Editors (Thiruthi).
 * Matches Flutter's `ElvanEditorSection`:
 * 24x24dp circle with 1-based index (1, 2, 3...) followed by a semi-bold title.
 */
@Composable
fun ElvanEditorSection(
    index: Int,
    title: String,
    modifier: Modifier = Modifier,
    badgeColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val badgeBg = badgeColor ?: if (isDark) Color.White else Color.Black
    val badgeTextColor = if (badgeColor != null) Color.White else if (isDark) Color.Black else Color.White

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
        ) {
            // Numbered Circle Badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = badgeTextColor
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Section Title
            Text(
                text = title.preventBrokenLigatures(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            )
        }

        // Section Content
        content()
    }
}

/**
 * Boxed Card Container for Elvan Editors.
 * Matches Flutter's `ElvanThiruthiAttai`:
 * Default `borderRadius = 24.dp`, no shadow, no border.
 * Light mode: `Color.White`, Dark mode: `Color.White.copy(alpha = 0.03f)`.
 */
@Composable
fun ElvanThiruthiAttai(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 24.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark

    val bg = backgroundColor ?: if (isDark) Color.White.copy(alpha = 0.08f) else Color.White

    val shape = RoundedCornerShape(borderRadius)

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .then(clickableModifier)
            .padding(padding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

/**
 * Small label displayed directly above editor text fields.
 * Matches Flutter's `ElvanThiruthiThalaippu`.
 */
@Composable
fun ElvanThiruthiThalaippu(
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    Text(
        text = label.preventBrokenLigatures(),
        style = TextStyle(
            fontFamily = ff,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        ),
        modifier = modifier.padding(start = 16.dp, bottom = 4.dp)
    )
}

/**
 * Pill-shaped input designed specifically for Elvan Editors (Thiruthi).
 * Matches Flutter's `ElvanThiruthiUlleedu` and `ElvanSettingsTextField`:
 * - Height: 48dp (for single-line, min 48dp for multiline)
 * - Corner radius: 100dp (pill) or 16dp (multiline)
 * - Padding: horizontal 20dp, vertical 12dp (multiline)
 * - Background: colors.iconBg (dynamic light/dark fill)
 */
@Composable
fun ElvanThiruthiUlleedu(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    prefixIcon: @Composable (() -> Unit)? = null,
    suffixIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = if (singleLine) 1 else 2,
    maxLines: Int = if (singleLine) 1 else 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: String? = null,
    backgroundColor: Color? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    val imeBottom = com.elvan.udukkai.core.platform.getImeBottomPadding()

    // As keyboard opens or expands while this field has focus, smoothly bring it into view
    LaunchedEffect(isFocused, imeBottom) {
        if (isFocused && imeBottom > 0.dp) {
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {}
        }
    }

    val containerBg = backgroundColor ?: colors.iconBg
    val shape = if (singleLine) RoundedCornerShape(100.dp) else RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        if (!label.isNullOrBlank()) {
            ElvanThiruthiThalaippu(label = label)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (singleLine) {
                        Modifier.height(48.dp)
                    } else {
                        Modifier.heightIn(
                            min = (24 + (minLines * 22)).dp,
                            max = (24 + (maxLines.coerceAtLeast(minLines) * 22)).dp
                        )
                    }
                )
                .clip(shape)
                .background(containerBg)
                .padding(horizontal = 20.dp, vertical = if (singleLine) 0.dp else 12.dp),
            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                if (prefixIcon != null) {
                    prefixIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (!prefixText.isNullOrEmpty()) {
                    Text(
                        text = prefixText,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textPrimary.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.padding(end = 4.dp, top = if (singleLine) 0.dp else 2.dp)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                ) {
                    if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textPrimary.copy(alpha = 0.35f)
                            ),
                            maxLines = if (singleLine) 1 else Int.MAX_VALUE
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = singleLine,
                        minLines = minLines,
                        maxLines = maxLines,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        cursorBrush = SolidColor(colors.textPrimary),
                        textStyle = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (enabled) colors.textPrimary else colors.textSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    coroutineScope.launch {
                                        try { bringIntoViewRequester.bringIntoView() } catch (_: Exception) {}
                                        delay(100)
                                        try { bringIntoViewRequester.bringIntoView() } catch (_: Exception) {}
                                        delay(150)
                                        try { bringIntoViewRequester.bringIntoView() } catch (_: Exception) {}
                                        delay(150)
                                        try { bringIntoViewRequester.bringIntoView() } catch (_: Exception) {}
                                    }
                                }
                            }
                    )
                }

                if (!suffixText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = suffixText,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textPrimary.copy(alpha = 0.6f)
                        )
                    )
                }

                if (suffixIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    suffixIcon()
                }
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Reusable Bilingual Input Field.
 * Matches Flutter's `ElvanIrumozhiPulan`:
 * Checks `profile.iruMozhi` from settings:
 * - If false: Renders 1 single input for primary language (`profile.mudhanMozhi`).
 * - If true: Renders 2 inputs: Primary language and Secondary language (`profile.thunaiMozhi`).
 */
@Composable
fun ElvanIrumozhiPulan(
    label: String,
    value: Map<String, String>,
    onChanged: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = if (maxLines > 1) 2 else 1,
    placeholder: String? = null
) {
    val currentMode = LocalAppMode.current
    val profile = NiruvanaTharavugalRepository.getProfile(currentMode)

    val isBilingual = if (currentMode == AppMode.KOOLI) true else profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primaryLangLabel = if (primaryLang == "ta") K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (secondaryLang == "en") K.enCode.tr() else K.taCode.tr()

    val primaryValue = value[primaryLang] ?: ""
    val secondaryValue = value[secondaryLang] ?: ""

    val isMultiline = maxLines > 1 || minLines > 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Primary Field
        key(primaryLang) {
            ElvanThiruthiUlleedu(
                value = primaryValue,
                onValueChange = { newText ->
                    val updated = value.toMutableMap()
                    updated[primaryLang] = newText
                    onChanged(updated)
                },
                label = "$label ($primaryLangLabel)",
                placeholder = placeholder,
                enabled = enabled,
                singleLine = !isMultiline,
                minLines = minLines,
                maxLines = if (isMultiline) maxLines.coerceAtLeast(6) else 1
            )
        }

        // Secondary Field (shown only when bilingual mode is enabled in Settings)
        AnimatedVisibility(
            visible = isBilingual,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            key(secondaryLang) {
                ElvanThiruthiUlleedu(
                    value = secondaryValue,
                    onValueChange = { newText ->
                        val updated = value.toMutableMap()
                        updated[secondaryLang] = newText
                        onChanged(updated)
                    },
                    label = "$label ($secondaryLangLabel)",
                    placeholder = placeholder,
                    enabled = enabled,
                    singleLine = !isMultiline,
                    minLines = minLines,
                    maxLines = if (isMultiline) maxLines.coerceAtLeast(6) else 1
                )
            }
        }
    }
}

/**
 * Generic Dropdown Pill Selector for Elvan Editors.
 * Matches Flutter's `ElvanThiruthiKeezhvirivu<T>`:
 * Height: 48dp capsule pill with padding(start = 20.dp, end = 8.dp).
 * Opens `ElvanSelectionBottomSheet` on tap.
 */
@Composable
fun <T> ElvanThiruthiKeezhvirivu(
    label: String? = null,
    hideLabel: Boolean = false,
    value: T?,
    items: List<T>,
    onSelected: (T) -> Unit,
    itemLabelBuilder: (T) -> String,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    leadingBuilder: (@Composable (T) -> Unit)? = null,
    subtitleBuilder: ((T) -> String?)? = null,
    showSearch: Boolean = false,
    searchFilter: ((T, String) -> Boolean)? = null,
    onRequestAddNew: (() -> Unit)? = null,
    backgroundColor: Color? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isSheetOpen by remember { mutableStateOf(false) }
    val containerBg = backgroundColor ?: colors.iconBg
    val displayText = if (value != null) itemLabelBuilder(value) else ""

    Column(modifier = modifier.fillMaxWidth()) {
        if (!label.isNullOrBlank() && !hideLabel) {
            ElvanThiruthiThalaippu(label = label)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(containerBg)
                .clickable { isSheetOpen = true }
                .padding(start = 20.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value != null && leadingBuilder != null) {
                    leadingBuilder(value)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = displayText.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (displayText.isNotEmpty()) colors.textPrimary else colors.textPrimary.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (value != null && onClear != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 16.dp)
                            ) { onClear() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textPrimary.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (isSheetOpen) {
        ElvanSelectionBottomSheet(
            title = label ?: "",
            items = items,
            currentValue = value,
            onSelected = {
                onSelected(it)
                isSheetOpen = false
            },
            onDismissRequest = { isSheetOpen = false },
            itemLabelBuilder = itemLabelBuilder,
            subtitleBuilder = subtitleBuilder,
            leadingBuilder = leadingBuilder,
            showSearch = showSearch,
            searchFilter = searchFilter,
            onRequestAddNew = onRequestAddNew
        )
    }
}

/**
 * Dropdown Pill Selector for Elvan Editors (Pair variant for simple string lists).
 * Matches Flutter's `ElvanThiruthiKeezhvirivu` and opens `ElvanSelectionBottomSheet`.
 */
@Composable
fun ElvanThiruthiKeezhvirivu(
    label: String? = null,
    selectedText: String,
    items: List<Pair<String, String>>, // value to display label
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null
) {
    ElvanThiruthiKeezhvirivu(
        label = label,
        value = items.firstOrNull { it.first == selectedText || it.second == selectedText },
        items = items,
        onSelected = { onSelected(it.first) },
        itemLabelBuilder = { it.second },
        modifier = modifier,
        backgroundColor = backgroundColor
    )
}

/**
 * Pill button component for Elvan Editors matching Flutter's `ElvanThiruthiPothan`.
 * Ensures pixel-perfect consistency (48dp height, 20dp padding, 100dp pill shape).
 */
@Composable
fun ElvanThiruthiPothan(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    padding: PaddingValues = PaddingValues(horizontal = 20.dp),
    backgroundColor: Color? = null,
    content: @Composable RowScope.() -> Unit
) {
    val colors = rememberShellColors()
    val containerBg = backgroundColor ?: colors.iconBg

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(100.dp))
            .background(containerBg)
            .clickable(onClick = onClick)
            .padding(padding),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

