package com.elvan.udukkai.ui.screens.product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

private fun getDynamicField(
    map: Map<String, String>,
    isPrimary: Boolean,
    isBilingual: Boolean,
    primaryLang: String,
    secondaryLang: String
): String {
    return if (isPrimary) {
        map[primaryLang]?.takeIf { it.isNotBlank() }
            ?: map[secondaryLang]?.takeIf { it.isNotBlank() }
            ?: map.values.firstOrNull { it.isNotBlank() }.orEmpty()
    } else {
        if (!isBilingual) ""
        else map[secondaryLang]?.takeIf { it.isNotBlank() }.orEmpty()
    }
}

@Composable
fun FactoryProductCard(
    index: Int,
    porul: PorulTharavuru,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = FactoryProfileRepository.profile
    val isBilingual = profile.iruMozhi
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primary = getDynamicField(porul.porulPeyar, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { porul.porulPeyar.values.firstOrNull() ?: "-" }
    val secondary = getDynamicField(porul.porulPeyar, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val rateStr = if (porul.vilai > 0.0) CurrencyUtils.formatInr(porul.vilai) else ""

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        padding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        ),
        borderRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.Inventory2,
                tint = colors.productColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primary.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (secondary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = secondary.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            color = LocalShellColors.current.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (rateStr.isNotEmpty() || porul.alagu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val subtitle = listOfNotNull(
                        rateStr.takeIf { it.isNotEmpty() },
                        porul.alagu.takeIf { it.isNotEmpty() }
                    ).joinToString("  •  ")

                    Text(
                        text = subtitle.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            color = LocalShellColors.current.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = MaterialSymbols.Rounded.ChevronRight,
                contentDescription = null,
                tint = LocalShellColors.current.border,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TextilesProductCard(
    index: Int,
    porul: PorulTharavuru,
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

    val primary = getDynamicField(porul.porulPeyar, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { porul.porulPeyar.values.firstOrNull() ?: "-" }
    val secondary = getDynamicField(porul.porulPeyar, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val rateStr = if (porul.vilai > 0.0) CurrencyUtils.formatInr(porul.vilai) else ""

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        padding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp
        ),
        borderRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElvanCardSelector(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected
            )

            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.Inventory2,
                tint = colors.productColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primary.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (secondary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = secondary.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            color = LocalShellColors.current.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (rateStr.isNotEmpty() || porul.alagu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val subtitle = listOfNotNull(
                        rateStr.takeIf { it.isNotEmpty() },
                        porul.alagu.takeIf { it.isNotEmpty() }
                    ).joinToString("  •  ")

                    Text(
                        text = subtitle.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            color = LocalShellColors.current.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = MaterialSymbols.Rounded.ChevronRight,
                contentDescription = null,
                tint = LocalShellColors.current.border,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
