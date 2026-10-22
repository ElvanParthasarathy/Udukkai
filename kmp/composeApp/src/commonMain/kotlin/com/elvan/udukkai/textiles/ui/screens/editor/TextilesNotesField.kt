package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.elvan.udukkai.core.data.LocationName
import com.elvan.udukkai.core.data.indianStates
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel
import com.elvan.udukkai.ui.screens.editor.ElvanIrumozhiPulan

/**
 * Place of Supply (வழங்கல் இடம்) selection widget with bottom sheet for Textiles mode.
 */
@Composable
fun TextilesPlaceOfSupplyField(
    placeOfSupplyEn: String,
    placeOfSupplyTa: String,
    onSelected: (en: String, ta: String) -> Unit,
    onCleared: () -> Unit,
    isBilingual: Boolean = false,
    primaryLang: String = "ta",
    secondaryLang: String = "en",
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isSheetOpen by remember { mutableStateOf(false) }

    val presetState = remember(placeOfSupplyEn, placeOfSupplyTa) {
        indianStates.filter { it.en != "Custom" && it.ta != "தனிப்பயன்" }
            .firstOrNull { it.en.equals(placeOfSupplyEn, ignoreCase = true) || it.ta == placeOfSupplyTa }
    }
    var isCustom by remember(placeOfSupplyEn, placeOfSupplyTa) {
        mutableStateOf(
            (placeOfSupplyEn.isNotBlank() || placeOfSupplyTa.isNotBlank()) &&
            (presetState == null || placeOfSupplyEn == "Custom" || placeOfSupplyTa == "தனிப்பயன்")
        )
    }

    val displayText = when {
        isCustom -> if (LanguageManager.activeLanguageCode == "ta") "தனிப்பயன்" else "Custom"
        placeOfSupplyTa.isNotBlank() -> placeOfSupplyTa
        placeOfSupplyEn.isNotBlank() -> placeOfSupplyEn
        else -> ""
    }

    val containerBg = colors.iconBg

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanEditorLabel(label = K.placeOfSupply.tr())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(containerBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = colors.accent)
                ) { isSheetOpen = true }
                .padding(start = 20.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (displayText.isNotEmpty()) displayText else K.placeOfSupply.tr()).preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = if (displayText.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                        color = if (displayText.isNotEmpty()) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (displayText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 18.dp)
                            ) {
                                isCustom = false
                                onCleared()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = K.cancel.tr(),
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
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (isCustom) {
            Spacer(modifier = Modifier.height(10.dp))
            val currentMap = mapOf(
                primaryLang to (if (primaryLang == "ta") placeOfSupplyTa else placeOfSupplyEn),
                secondaryLang to (if (secondaryLang == "ta") placeOfSupplyTa else placeOfSupplyEn)
            )
            ElvanIrumozhiPulan(
                label = "${K.placeOfSupply.tr()} (${K.customBtn.tr()})",
                value = currentMap,
                onChanged = { map ->
                    val ta = map["ta"] ?: ""
                    val en = map["en"] ?: ""
                    onSelected(en, ta)
                },
                isBilingual = isBilingual,
                primaryLang = primaryLang,
                secondaryLang = secondaryLang,
                placeholder = K.placeOfSupply.tr()
            )
        }
    }

    if (isSheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.placeOfSupply.tr(),
            items = indianStates,
            currentValue = if (isCustom) indianStates.last() else presetState,
            showSearch = true,
            onDismissRequest = { isSheetOpen = false },
            onSelected = { state ->
                if (state.en == "Custom" || state.ta == "தனிப்பயன்") {
                    isCustom = true
                    onSelected("", "")
                } else {
                    isCustom = false
                    onSelected(state.en, state.ta)
                }
                isSheetOpen = false
            },
            itemLabelBuilder = { s -> if (LanguageManager.activeLanguageCode == "ta") s.ta else s.en },
            subtitleBuilder = { s -> if (LanguageManager.activeLanguageCode == "ta") s.en else s.ta },
            searchFilter = { s, query ->
                val q = query.lowercase()
                s.ta.lowercase().contains(q) || s.en.lowercase().contains(q)
            }
        )
    }
}

@Deprecated("Use TextilesPlaceOfSupplyField", ReplaceWith("TextilesPlaceOfSupplyField(placeOfSupplyEn, placeOfSupplyTa, onSelected, onCleared, modifier = modifier)"))
@Composable
fun PattuVilippiIdam(
    placeOfSupplyEn: String,
    placeOfSupplyTa: String,
    onSelected: (en: String, ta: String) -> Unit,
    onCleared: () -> Unit,
    modifier: Modifier = Modifier
) = TextilesPlaceOfSupplyField(placeOfSupplyEn, placeOfSupplyTa, onSelected, onCleared, modifier = modifier)
