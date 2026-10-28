package com.elvan.udukkai.ui.screens.editor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.floor
import kotlin.math.round
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.theme.gradientFor
import com.elvan.udukkai.ui.components.shell.ElvanFloatingAction
import com.elvan.udukkai.ui.components.shell.ElvanFloatingActionBar
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.ui.components.shell.resolveCompanyItemLabel
import com.elvan.udukkai.ui.components.shell.resolveCompanySubtitle

data class CompanyOption(
    val id: Long?,
    val name: String,
    val subtitle: String? = null
)

@Composable
fun rememberCompanyOptions(
    profiles: List<BusinessProfile>,
    isBilingual: Boolean,
    primaryLang: String,
    secondaryLang: String
): List<CompanyOption> {
    val allCompaniesTitle = if (primaryLang == "ta") "எல்லா நிறுவனங்கள்" else "All Companies"
    val allCompaniesSub = if (isBilingual) {
        if (primaryLang == "ta") "All Companies" else "எல்லா நிறுவனங்கள்"
    } else null

    return remember(profiles, isBilingual, primaryLang, secondaryLang) {
        val allOption = CompanyOption(
            id = null,
            name = allCompaniesTitle,
            subtitle = allCompaniesSub
        )
        val list = profiles.map { p ->
            val pName = resolveCompanyItemLabel(p, primaryBillingLang = primaryLang)
                .ifEmpty { p.businessName.values.firstOrNull() ?: p.shortName.ifEmpty { "Company #${p.id}" } }
            val sName = if (isBilingual) {
                resolveCompanySubtitle(
                    profile = p,
                    isBilingual = true,
                    primaryBillingLang = primaryLang,
                    secondaryBillingLang = secondaryLang
                )?.takeIf { it.isNotBlank() && it != pName }
            } else null
            CompanyOption(
                id = p.id,
                name = pName,
                subtitle = sName
            )
        }
        listOf(allOption) + list
    }
}

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
 * Dims and disables downstream form content when a prerequisite (such as customer selection) is not met.
 * Matches React's invoice editor disabled behavior:
 * - Content remains visible on screen so the entire layout is readable.
 * - When [disabled] is true: opacity is dimmed to 0.38f and pointer events are blocked so fields cannot be edited.
 */
@Composable
fun ElvanEditorDisabledOverlay(
    disabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (disabled) {
                    Modifier
                        .alpha(0.38f)
                        .focusProperties { canFocus = false }
                } else Modifier
            )
    ) {
        content()
        if (disabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
            )
        }
    }
}

/**
 * Boxed Card Container for Elvan Editors.
 * Matches Flutter's `ElvanEditorCard`:
 * Default `borderRadius = 24.dp`, no shadow, no border.
 * Light mode: `Color.White`, Dark mode: `Color.White.copy(alpha = 0.03f)`.
 */
@Composable
fun ElvanEditorCard(
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
 * Matches Flutter's `ElvanEditorLabel`.
 */
@Composable
fun ElvanEditorLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    // Ensure compulsory asterisk appears after language bracket: e.g. "பெயர் (தமிழ்)*"
    val formattedLabel = if (label.contains("*") && label.contains("(") && label.contains(")")) {
        val clean = label.replace("*", "").trim()
        "$clean*"
    } else {
        label
    }

    Text(
        text = formattedLabel.preventBrokenLigatures(),
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
 * Matches Flutter's `ElvanEditorTextField` and `ElvanSettingsTextField`:
 * - Height: 48dp (for single-line, min 48dp for multiline)
 * - Corner radius: 100dp (pill) or 16dp (multiline)
 * - Padding: horizontal 20dp, vertical 12dp (multiline)
 * - Background: colors.iconBg (dynamic light/dark fill)
 */
@Composable
fun ElvanEditorTextField(
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
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = if (singleLine) 1 else 2,
    maxLines: Int = if (singleLine) 1 else 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: String? = null,
    backgroundColor: Color? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null
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
            ElvanEditorLabel(label = label)
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
                .padding(start = 20.dp, end = if (suffixIcon != null) 8.dp else 20.dp, top = if (singleLine) 0.dp else 12.dp, bottom = if (singleLine) 0.dp else 12.dp),
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
                        readOnly = readOnly,
                        singleLine = singleLine,
                        minLines = minLines,
                        maxLines = maxLines,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        cursorBrush = if (readOnly) SolidColor(Color.Transparent) else SolidColor(colors.textPrimary),
                        textStyle = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = if (readOnly) FontWeight.Medium else FontWeight.Normal,
                            color = if (enabled) colors.textPrimary else colors.textSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                onFocusChanged?.invoke(focusState.isFocused)
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

private fun formatNumericDisplay(v: Double, isWeight: Boolean): String {
    if (v == 0.0) return ""
    return if (isWeight) {
        val r = round(v * 1000.0) / 1000.0
        val s = r.toString()
        val dot = s.indexOf('.')
        if (dot == -1) {
            "$s.000"
        } else {
            val dec = s.length - dot - 1
            when {
                dec >= 3 -> s.substring(0, dot + 4)
                else -> s + "0".repeat(3 - dec)
            }
        }
    } else {
        if (v == floor(v)) v.toLong().toString() else v.toString()
    }
}

/**
 * Stateful numeric input field for Elvan Editors.
 * 1:1 matches Flutter's ItemFieldWidget:
 * - Maintains local text state during active editing so keystrokes like "32."
 *   do NOT collapse calculations to zero or erase the typed decimal point.
 * - Prevents multiple decimal points (".." etc.).
 * - Parses intermediate input safely ("32." -> 32.0, so totals do not collapse to zero).
 * - Formats on blur (e.g. 3 decimals for weight matching weighing machines, clean integer for rates).
 * - Syncs with external value updates when not focused (e.g. item picker selection, profile changes).
 */
@Composable
fun ElvanEditorNumericField(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    suffixIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    isWeight: Boolean = false,
    maxDecimals: Int = if (isWeight) 3 else 2,
    key: Any? = null,
    onDirty: (() -> Unit)? = null,
    backgroundColor: Color? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    var localText by remember(key) { mutableStateOf(formatNumericDisplay(value, isWeight)) }

    // Sync from external value changes (e.g. item picker, database load, external reset)
    // ONLY when the field does NOT have active focus.
    LaunchedEffect(key, value, isFocused) {
        if (!isFocused) {
            val parsed = localText.toDoubleOrNull() ?: 0.0
            if (parsed != value || (value == 0.0 && localText.isNotEmpty()) || (value > 0.0 && localText.isEmpty())) {
                localText = formatNumericDisplay(value, isWeight)
            }
        }
    }

    ElvanEditorTextField(
        value = localText,
        onValueChange = { input ->
            val normalized = input.replace(',', '.')
            if (normalized.isEmpty()) {
                localText = ""
                onDirty?.invoke()
                onValueChange(0.0)
                return@ElvanEditorTextField
            }

            val dotCount = normalized.count { it == '.' }
            val validChars = normalized.all { it.isDigit() || it == '.' }
            if (dotCount <= 1 && validChars) {
                val dotIdx = normalized.indexOf('.')
                val decimalsValid = if (dotIdx != -1) {
                    normalized.length - dotIdx - 1 <= maxDecimals
                } else true

                if (decimalsValid) {
                    localText = normalized
                    onDirty?.invoke()

                    // Safe parse: if input ends with '.', parse without the dot so
                    // intermediate typing like "32." parses as 32.0 and does NOT collapse totals to zero!
                    val clean = normalized.trimEnd('.')
                    val parsed = if (clean.isEmpty() || clean == ".") 0.0 else clean.toDoubleOrNull() ?: 0.0
                    onValueChange(parsed)
                }
            }
        },
        onFocusChanged = { focused ->
            isFocused = focused
            if (!focused) {
                // On blur: format cleanly matching weighing machine (3 decimals for weight) or clean integer
                val clean = localText.trimEnd('.')
                val parsed = if (clean.isEmpty() || clean == ".") 0.0 else clean.toDoubleOrNull() ?: 0.0
                if (parsed > 0.0) {
                    localText = formatNumericDisplay(parsed, isWeight)
                    onValueChange(parsed)
                } else {
                    localText = ""
                    onValueChange(0.0)
                }
            }
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        prefixText = prefixText,
        suffixText = suffixText,
        suffixIcon = suffixIcon,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        backgroundColor = backgroundColor
    )
}

/**
 * Reusable Bilingual Input Field.
 * Matches Flutter's `ElvanIrumozhiPulan`:
 * Checks `profile.isBilingual` from settings:
 * - If false: Renders 1 single input for primary language (`profile.primaryLanguage`).
 * - If true: Renders 2 inputs: Primary language and Secondary language (`profile.secondaryLanguage`).
 */
@Composable
fun ElvanIrumozhiPulan(
    label: String,
    value: Map<String, String>,
    onChanged: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isBilingual: Boolean = true,
    primaryLang: String = "ta",
    secondaryLang: String = "en",
    maxLines: Int = 1,
    minLines: Int = if (maxLines > 1) 2 else 1,
    placeholder: String? = null
) {
    val hasAsterisk = label.contains("*")
    val cleanBaseLabel = label.replace("*", "").trim()
    val starSuffix = if (hasAsterisk) "*" else ""

    val primaryLangLabel = when (primaryLang.lowercase()) {
        "en" -> K.enCode.tr()
        "ta" -> K.taCode.tr()
        else -> primaryLang
    }
    val secondaryLangLabel = when (secondaryLang.lowercase()) {
        "en" -> K.enCode.tr()
        "ta" -> K.taCode.tr()
        else -> secondaryLang
    }

    val primaryValue = value[primaryLang] ?: ""
    val secondaryValue = value[secondaryLang] ?: ""

    val isMultiline = maxLines > 1 || minLines > 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Primary Field
        key(primaryLang) {
            ElvanEditorTextField(
                value = primaryValue,
                onValueChange = { newText ->
                    val updated = value.toMutableMap()
                    updated[primaryLang] = newText
                    onChanged(updated)
                },
                label = "$cleanBaseLabel ($primaryLangLabel)$starSuffix",
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
                ElvanEditorTextField(
                    value = secondaryValue,
                    onValueChange = { newText ->
                        val updated = value.toMutableMap()
                        updated[secondaryLang] = newText
                        onChanged(updated)
                    },
                    label = "$cleanBaseLabel ($secondaryLangLabel)$starSuffix",
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
fun <T> ElvanEditorDropdown(
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
            ElvanEditorLabel(label = label)
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 18.dp)
                            ) { onClear() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.size(36.dp),
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
fun ElvanEditorDropdown(
    label: String? = null,
    selectedText: String,
    items: List<Pair<String, String>>, // value to display label
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null
) {
    ElvanEditorDropdown(
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
 * Pill button component for Elvan Editors matching Flutter's `ElvanEditorButton`.
 * Ensures pixel-perfect consistency (48dp height, 20dp padding, 100dp pill shape).
 */
@Composable
fun ElvanEditorButton(
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



/**
 * Bottom Floating Action Bar for Elvan Editors (One UI pattern).
 * Replaces the cramped top-right action button with a primary thumb-accessible floating pill at the bottom.
 * Powered by the shared ElvanFloatingActionBar component.
 */
@Composable
fun ElvanEditorBottomBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = K.saveBtn.tr(),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    accentColor: Color? = null,
    onPreview: (() -> Unit)? = null,
    showSave: Boolean = true
) {
    val previewLabel = K.previewToolbarBtn.tr()
    val actions = remember(label, enabled, isLoading, onClick, onPreview, previewLabel, showSave) {
        listOfNotNull(
            onPreview?.let {
                ElvanFloatingAction(
                    label = previewLabel,
                    icon = MaterialSymbols.Rounded.Visibility,
                    onClick = it
                )
            },
            if (showSave) {
                ElvanFloatingAction(
                    label = label,
                    icon = MaterialSymbols.Rounded.Save,
                    onClick = onClick,
                    enabled = enabled,
                    isLoading = isLoading
                )
            } else null
        )
    }
    if (actions.isEmpty()) return

    ElvanFloatingActionBar(
        actions = actions,
        modifier = modifier
    )
}
