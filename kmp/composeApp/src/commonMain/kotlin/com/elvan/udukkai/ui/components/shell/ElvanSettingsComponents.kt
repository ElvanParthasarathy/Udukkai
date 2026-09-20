package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * ElvanSettingsSection — Groups rows inside a single rounded card with slit dividers.
 * Mirrors Flutter / Neram's `ElvanSettingsSection` exactly.
 *
 * Card background: #111111 (Dark) / #FFFFFF (Light)
 * Border Radius: 24.dp
 */
@Composable
fun ElvanSettingsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    borderRadius: Dp = 24.dp,
    cardColor: Color? = null,
    colors: ShellColors = rememberShellColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val finalCardColor = cardColor ?: colors.surface

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        if (title != null) {
            val ff = LocalAppFontFamily.current
            Text(
                text = title.uppercase(),
                style = TextStyle(
                    fontFamily = ff,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = colors.textPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(borderRadius)),
            shape = RoundedCornerShape(borderRadius),
            color = finalCardColor,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * ElvanSettingsRow — A single settings item with circular monochrome icon, title, and description.
 * Mirrors Flutter / Neram's `ElvanSettingsRow` exactly.
 *
 * Padding: 16.dp horizontal, 16.dp vertical
 * Icon: 36.dp circle with monochrome background (#FFFFFF 8% in dark, #000000 6% in light)
 * Title: 15.sp, FontWeight.Medium
 * Description: 12.sp, textSecondary (onSurface 50%)
 */
@Composable
fun ElvanSettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconWidget: (@Composable () -> Unit)? = null,
    description: String? = null,
    customTrailing: (@Composable () -> Unit)? = null,
    iconBgColor: Color? = null,
    iconTint: Color? = null,
    titleColor: Color? = null,
    descColor: Color? = null,
    colors: ShellColors = rememberShellColors()
) {
    val defaultIconBg = iconBgColor ?: colors.iconBg
    val defaultIconTint = iconTint ?: (titleColor ?: colors.textPrimary)
    val ff = LocalAppFontFamily.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ShellDefaults.ripple(colors, bounded = true),
                onClick = onClick
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon container — 36px, monochrome background
            if (icon != null || iconWidget != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBgColor ?: defaultIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconWidget != null) {
                        iconWidget()
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = defaultIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Title + Description
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    color = titleColor ?: colors.textPrimary
                )
                if (!description.isNullOrEmpty()) {
                    Text(
                        text = description,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        ),
                        color = descColor ?: colors.textPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (customTrailing != null) {
                Spacer(modifier = Modifier.width(12.dp))
                customTrailing()
            } else {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = MaterialSymbols.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = colors.textPrimary.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * ElvanSettingsDivider — A slit line divider between settings rows matching Neram.
 * Color: Colors.white 4% (Dark) / Colors.black 4% (Light)
 * Indent: 16.dp start, 20.dp end
 */
@Composable
fun ElvanSettingsDivider(
    modifier: Modifier = Modifier,
    indent: Dp = 16.dp,
    endIndent: Dp = 20.dp,
    colors: ShellColors = rememberShellColors()
) {
    HorizontalDivider(
        modifier = modifier.padding(start = indent, end = endIndent),
        thickness = 1.dp,
        color = colors.divider
    )
}

/**
 * ElvanSettingsSwitch — Crisp monochrome switch matching Apple / One UI monochrome styling.
 */
@Composable
fun ElvanSettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ShellColors = rememberShellColors()
) {
    val isDark = colors.isDark
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = if (isDark) Color.Black else Color.White,
            checkedTrackColor = if (isDark) Color.White else Color.Black,
            uncheckedThumbColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.6f),
            uncheckedTrackColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f),
            disabledCheckedThumbColor = (if (isDark) Color.Black else Color.White).copy(alpha = 0.6f),
            disabledCheckedTrackColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.25f),
            disabledUncheckedThumbColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.3f),
            disabledUncheckedTrackColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.06f),
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedBorderColor = Color.Transparent,
            disabledUncheckedBorderColor = Color.Transparent
        )
    )
}

/**
 * ElvanProfilePillCard — Big Pill Status Card (borderRadius: 999.dp) matching Neram One UI Settings Hub.
 */
@Composable
fun ElvanProfilePillCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconWidget: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) {
    val cardColor = colors.surface
    val avatarBg = colors.iconBg
    val ff = LocalAppFontFamily.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ShellDefaults.ripple(colors, bounded = true),
                onClick = onClick
            ),
        shape = RoundedCornerShape(999.dp),
        color = cardColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Avatar/Icon (52dp)
            Surface(
                shape = CircleShape,
                color = avatarBg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (iconWidget != null) {
                        iconWidget()
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    ),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.textPrimary.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailing()
            }
        }
    }
}

/**
 * ElvanRadioSettingsRow — A row with title and checkmark matching Flutter / Neram's `ElvanRadioSettingsRow`.
 */
@Composable
fun <T> ElvanRadioSettingsRow(
    title: String,
    value: T,
    groupValue: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    fontFamily: FontFamily? = null,
    colors: ShellColors = rememberShellColors()
) {
    val isSelected = value == groupValue
    val ff = fontFamily ?: LocalAppFontFamily.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ShellDefaults.ripple(colors, bounded = true),
                onClick = { onSelected(value) }
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (description != null) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        lineHeight = 21.sp
                    ),
                    color = colors.textPrimary
                )
                if (!description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 17.sp
                        ),
                        color = colors.textSecondary
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Check,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Spacer(modifier = Modifier.size(22.dp))
            }
        }
    }
}

/**
 * ElvanSettingsAnimatedExpand — Smoothly expands and collapses between read-only display and edit modes
 * using vertical expand and fade transitions.
 */
@Composable
fun ElvanSettingsAnimatedExpand(
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    displayContent: @Composable () -> Unit,
    editContent: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = !isEditing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            displayContent()
        }
        AnimatedVisibility(
            visible = isEditing,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            editContent()
        }
    }
}

/**
 * ElvanSettingsDisplayRow — Read-only state showing title label, primary value, optional subtitle, and circular edit button.
 * Mirrors Flutter / Neram's `ElvanSettingsDisplayRow` exactly.
 */
@Composable
fun ElvanSettingsDisplayRow(
    title: String,
    primaryValue: String = "",
    value: String = primaryValue,
    modifier: Modifier = Modifier,
    secondaryValue: String? = null,
    onEdit: (() -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    icon: ImageVector = MaterialSymbols.Rounded.Edit,
    iconColor: Color? = null,
    primaryWidget: (@Composable () -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val defaultIconBg = colors.iconBg
    val effectiveValue = if (value.isNotEmpty()) value else primaryValue

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onTap != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ShellDefaults.ripple(colors, bounded = true),
                    onClick = onTap
                ) else Modifier
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 18.sp
                    ),
                    color = colors.textPrimary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (primaryWidget != null) {
                    primaryWidget()
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (primaryWidget == null || effectiveValue.isNotEmpty()) {
                    Text(
                        text = if (effectiveValue.isEmpty()) "-" else effectiveValue,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!secondaryValue.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = secondaryValue,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (onEdit != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = CircleShape,
                    color = iconColor?.copy(alpha = 0.1f) ?: defaultIconBg,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ShellDefaults.ripple(colors, bounded = true),
                            onClick = onEdit
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = K.edit.tr(),
                            tint = iconColor ?: colors.textPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ElvanSettingsEditContainer — Tinted container for edit mode forms with Cancel and Save buttons.
 * Mirrors Flutter / Neram's `ElvanSettingsEditContainer` exactly.
 */
@Composable
fun ElvanSettingsEditContainer(
    title: String? = null,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String? = null,
    saveText: String? = null,
    extraAction: (@Composable () -> Unit)? = null,
    colors: ShellColors = rememberShellColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val ff = LocalAppFontFamily.current
    val effectiveCancel = cancelText ?: K.cancel.tr()
    val effectiveSave = saveText ?: K.save.tr()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        content()
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (extraAction != null) {
                extraAction()
                Spacer(modifier = Modifier.width(8.dp))
            }
            TextButton(
                onClick = onCancel,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = effectiveCancel,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.textPrimary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) Color.White else Color.Black,
                    contentColor = if (colors.isDark) Color.Black else Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = effectiveSave,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * ElvanSettingsTextField — Pill-shaped text field with dynamic fill color matching Flutter / Neram's `ElvanSettingsTextField`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ElvanSettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    prefixText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = if (singleLine) 1 else 2,
    maxLines: Int = if (singleLine) 1 else 6,
    maxLength: Int? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val fieldBg = colors.iconBg
    val shapeRadius = if (singleLine) 100.dp else 16.dp
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var isFocused by remember { mutableStateOf(false) }
    val imeBottom = com.elvan.udukkai.core.platform.getImeBottomPadding()

    LaunchedEffect(isFocused, imeBottom) {
        if (isFocused && imeBottom > 0.dp) {
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = ff,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.3.sp
            ),
            color = colors.textPrimary.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Surface(
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
                .clip(RoundedCornerShape(shapeRadius))
                .then(
                    if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
                ),
            shape = RoundedCornerShape(shapeRadius),
            color = fieldBg,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = if (singleLine) 0.dp else 12.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                if (prefixText != null) {
                    Text(
                        text = prefixText,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = colors.textPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 4.dp, top = if (singleLine) 0.dp else 2.dp)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colors.textPrimary.copy(alpha = 0.35f)
                        )
                    }

                    if (readOnly) {
                        Text(
                            text = value,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = colors.textPrimary,
                            maxLines = if (singleLine) 1 else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = { newVal ->
                                if (maxLength == null || newVal.length <= maxLength) {
                                    onValueChange(newVal)
                                }
                            },
                            singleLine = singleLine,
                            minLines = minLines,
                            maxLines = maxLines,
                            keyboardOptions = keyboardOptions,
                            textStyle = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textPrimary
                            ),
                            cursorBrush = SolidColor(colors.textPrimary),
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
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    }
}

/**
 * ElvanSimpleSettingsRow — A generic row with title, description, and trailing widget matching Flutter's `ElvanSimpleSettingsRow`.
 */
@Composable
fun ElvanSimpleSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ShellDefaults.ripple(colors, bounded = true),
                    onClick = onClick
                ) else Modifier
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                    color = colors.textPrimary
                )
                if (!description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.5f)
                    )
                }
            }

            if (trailing != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailing()
            }
        }
    }
}

