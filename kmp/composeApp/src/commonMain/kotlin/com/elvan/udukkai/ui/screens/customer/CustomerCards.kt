package com.elvan.udukkai.ui.screens.customer

import androidx.compose.foundation.layout.*
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
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.components.ElvanCardSecondLine
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
    // With one company only, its own name shows instead of the generic "All Companies"
    val ids = if (merchant.companyIds.isEmpty() && profiles.size == 1) listOfNotNull(profiles[0].id) else merchant.companyIds
    if (ids.isEmpty()) {
        return K.allCompanies.tr()
    }
    return ids.mapNotNull { cId ->
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

private fun getBilingualSecondaryName(
    map: Map<String, String>,
    primaryLang: String,
    secondaryLang: String
): String {
    val exactVal = map[secondaryLang]?.trim()
    if (!exactVal.isNullOrEmpty()) return exactVal
    return map.entries.firstOrNull { it.key != primaryLang && it.value.isNotBlank() }?.value?.trim().orEmpty()
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
    val secondaryName = if (profile.isBilingual) {
        getBilingualSecondaryName(merchant.name, primaryLang, secondaryLang)
    } else ""
    val primaryCity = getMainLanguageField(merchant.city, primaryLang, secondaryLang)
    // Under the name: the second-language name with the place (in the main language) after a dot; the place alone without one
    val nameSecondLine = listOf(secondaryName.takeIf { it.isNotBlank() && it != primaryName }.orEmpty(), primaryCity)
        .filter { it.isNotBlank() }.joinToString("  •  ")

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        isSelectionMode = isSelectionMode,
        padding = PaddingValues(
            horizontal = Dimens.CardPaddingHorizontal,
            vertical = if (com.elvan.udukkai.ui.components.shell.LocalLandscapeContent.current) 12.dp else Dimens.CardPaddingVertical
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
                    var primaryLineCount by remember(primaryName, com.elvan.udukkai.ui.components.shell.LocalLandscapeContent.current) { mutableStateOf<Int?>(null) }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primaryName.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            maxLines = primaryLineCount ?: 2,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layoutResult ->
                                if (!isSelectionMode) {
                                    primaryLineCount = layoutResult.lineCount
                                }
                            }
                        )

                        ElvanCardSecondLine(
                            text = nameSecondLine,
                            label = if (merchant.companyIds.isNotEmpty()) resolveCustomerCompanyTag(merchant, profiles, primaryLang) else "",
                            color = colors.textSecondary,
                            labelColor = colors.textTertiary,
                            fontFamily = ff,
                            isSelectionMode = isSelectionMode
                        )
                    }

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
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
    val secondaryName = if (profile.isBilingual) {
        getBilingualSecondaryName(merchant.name, primaryLang, secondaryLang)
    } else ""
    val primaryCity = getMainLanguageField(merchant.city, primaryLang, secondaryLang)
    // Under the name: the second-language name with the place (in the main language) after a dot; the place alone without one
    val nameSecondLine = listOf(secondaryName.takeIf { it.isNotBlank() && it != primaryName }.orEmpty(), primaryCity)
        .filter { it.isNotBlank() }.joinToString("  •  ")
    val gstin = merchant.gstin.trim()

    ElvanCommonCard(
        onClick = onClick,
        onLongClick = onLongClick,
        isSelected = isSelectionMode && isSelected,
        isSelectionMode = isSelectionMode,
        padding = PaddingValues(
            horizontal = Dimens.CardPaddingHorizontal,
            vertical = if (com.elvan.udukkai.ui.components.shell.LocalLandscapeContent.current) 12.dp else Dimens.CardPaddingVertical
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
                    var primaryLineCount by remember(primaryName, com.elvan.udukkai.ui.components.shell.LocalLandscapeContent.current) { mutableStateOf<Int?>(null) }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = primaryName.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            ),
                            maxLines = primaryLineCount ?: 2,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { layoutResult ->
                                if (!isSelectionMode) {
                                    primaryLineCount = layoutResult.lineCount
                                }
                            }
                        )

                        ElvanCardSecondLine(
                            text = nameSecondLine,
                            label = if (merchant.companyIds.isNotEmpty()) resolveCustomerCompanyTag(merchant, profiles, primaryLang) else "",
                            color = colors.textSecondary,
                            labelColor = colors.textTertiary,
                            fontFamily = ff,
                            isSelectionMode = isSelectionMode
                        )
                    }

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                }

                if (gstin.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
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
            }
        }
    }
}
