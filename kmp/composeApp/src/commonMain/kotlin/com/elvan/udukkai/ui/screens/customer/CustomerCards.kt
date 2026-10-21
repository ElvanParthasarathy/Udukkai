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
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr

@Composable
private fun resolveCustomerCompanyTag(
    merchant: CustomerModel,
    profiles: List<BusinessProfile>,
    primaryLang: String
): String {
    if (merchant.companyIds.isEmpty()) {
        return K.allCompanies.tr()
    }
    return merchant.companyIds.mapNotNull { cId ->
        val p = profiles.find { it.id == cId }
        p?.shortName?.trim()?.takeIf { it.isNotEmpty() }
            ?: p?.businessName?.get(primaryLang)?.trim()?.takeIf { it.isNotEmpty() }
            ?: p?.businessName?.values?.firstOrNull { it.isNotBlank() }?.trim()
            ?: "Company #$cId"
    }.joinToString(", ").ifEmpty { K.allCompanies.tr() }
}

private fun getMainLanguageField(
    map: Map<String, String>,
    primaryLang: String,
    secondaryLang: String
): String {
    val exactVal = map[primaryLang]?.trim()
    if (!exactVal.isNullOrEmpty()) return exactVal
    val fallbackVal = map[secondaryLang]?.trim()
    if (!fallbackVal.isNullOrEmpty()) return fallbackVal
    return map.values.firstOrNull { it.isNotBlank() }?.trim() ?: ""
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
    val profiles = FactoryProfileRepository.allProfiles
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val primaryName = getMainLanguageField(merchant.name, primaryLang, secondaryLang)
        .ifEmpty { "-" }
    val primaryCity = getMainLanguageField(merchant.city, primaryLang, secondaryLang)

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = primaryName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp)
                    )
                }

                val companyTag = resolveCustomerCompanyTag(merchant, profiles, primaryLang)
                val cityLine = primaryCity

                if (cityLine.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cityLine.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.5.sp,
                            color = colors.textSecondary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (companyTag.isNotBlank()) {
                    Spacer(modifier = Modifier.height(if (cityLine.isNotBlank()) 2.dp else 4.dp))
                    Text(
                        text = companyTag.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.5.sp,
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
    val profiles = TextilesProfileRepository.allProfiles
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }

    val primaryName = getMainLanguageField(merchant.name, primaryLang, secondaryLang)
        .ifEmpty { "-" }
    val primaryCity = getMainLanguageField(merchant.city, primaryLang, secondaryLang)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = primaryName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp)
                    )
                }

                val cityText = primaryCity
                val companyTag = resolveCustomerCompanyTag(merchant, profiles, primaryLang)
                val hasCity = cityText.isNotBlank()
                val hasGstin = gstin.isNotBlank()
                val hasCompany = companyTag.isNotBlank()

                if (hasCity || hasGstin || hasCompany) {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (hasCity) {
                        Text(
                            text = cityText.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (hasGstin) {
                        if (hasCity) Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GSTIN: $gstin",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                color = colors.textSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (hasCompany) {
                        if (hasCity || hasGstin) Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = companyTag.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
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
