package com.elvan.udukkai.ui.screens.customer

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
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCardSelector
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.navigation.MaterialSymbols

private fun getDynamicField(
    map: Map<String, String>,
    isPrimary: Boolean,
    isBilingual: Boolean,
    primaryLang: String,
    secondaryLang: String
): String {
    if (!isBilingual && !isPrimary) return ""
    val targetLang = if (isPrimary) primaryLang else secondaryLang
    val exactVal = map[targetLang]?.trim()
    if (!exactVal.isNullOrEmpty()) return exactVal
    if (!isBilingual && isPrimary) {
        val fallbackVal = map[secondaryLang]?.trim()
        if (!fallbackVal.isNullOrEmpty()) return fallbackVal
    }
    if (isPrimary) return map.values.firstOrNull { it.isNotBlank() } ?: ""
    return ""
}

@Composable
fun FactoryCustomerCard(
    index: Int,
    merchant: CustomerModel,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = FactoryProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primaryName = getDynamicField(merchant.name, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { merchant.name.values.firstOrNull() ?: "-" }
    val secondaryName = getDynamicField(merchant.name, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val primaryCity = getDynamicField(merchant.city, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
    val secondaryCity = getDynamicField(merchant.city, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

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
                icon = MaterialSymbols.Rounded.Person,
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

                        if (secondaryName.isNotBlank() && secondaryName != primaryName) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = secondaryName.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSecondary
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

                if (primaryCity.isNotBlank() || secondaryCity.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (primaryCity.isNotBlank()) {
                        Text(
                            text = primaryCity.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.6.sp,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (secondaryCity.isNotBlank() && secondaryCity != primaryCity) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = secondaryCity.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.8.sp,
                                color = colors.textSecondary.copy(alpha = 0.8f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TextilesCustomerCard(
    index: Int,
    merchant: CustomerModel,
    onClick: () -> Unit,
    colors: ShellColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current
    val profile = TextilesProfileRepository.profile
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val primaryName = getDynamicField(merchant.name, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
        .ifEmpty { merchant.name.values.firstOrNull() ?: "-" }
    val secondaryName = getDynamicField(merchant.name, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val primaryCity = getDynamicField(merchant.city, isPrimary = true, isBilingual = isBilingual, primaryLang, secondaryLang)
    val secondaryCity = getDynamicField(merchant.city, isPrimary = false, isBilingual = isBilingual, primaryLang, secondaryLang)

    val gstin = merchant.gstin.trim()

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
                icon = MaterialSymbols.Rounded.Person,
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
                                    color = colors.textSecondary
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

                val cityText = if (isBilingual && secondaryCity.isNotBlank() && secondaryCity != primaryCity) {
                    if (primaryCity.isNotBlank()) "$primaryCity • $secondaryCity" else secondaryCity
                } else {
                    primaryCity
                }

                if (cityText.isNotBlank() || gstin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (cityText.isNotBlank()) {
                        Text(
                            text = cityText.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.6.sp,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (gstin.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GSTIN: $gstin",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.8.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
