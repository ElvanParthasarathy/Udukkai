package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Item View Screen (உருப்படி பார்வை)
 */
@Composable
fun ItemViewScreen(
    item: ItemModel,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile>,
    isCoolie: Boolean = false,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCopy: (() -> Unit)? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = profile.secondaryLanguage.ifEmpty { "en" }
            
    val p1 = item.name[primaryLang] ?: item.name["ta"] ?: ""
    val p2 = item.name[secondaryLang] ?: item.name["en"] ?: ""
    val primaryName = p1.ifEmpty { p2.ifEmpty { "-" } }
    val secondaryName = if (isBilingual && p1.isNotEmpty() && p2.isNotEmpty() && p1 != p2) p2 else ""

    val scrollState = rememberLazyListState()

    ViewTile(
        title = K.itemDetails.tr(),
        onBack = onBack,
        scrollState = scrollState,
        onEdit = onEdit,
        onCopy = onCopy
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = Dimens.SubpageContentPaddingBottom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // One UI Collapsible Header top spacer
            item(key = "top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // Item Header Card
            item(key = "header_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.025f))
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Item Icon Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Inventory2,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = primaryName.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    letterSpacing = (-0.2).sp
                                )
                            )
                            if (secondaryName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = secondaryName.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        color = colors.textSecondary
                                    )
                                )
                            }
                        }

                        if (item.hsnCode.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "HSN: ${item.hsnCode}",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = colors.textPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Details Cards
            item(key = "details_cards") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isCoolie) {
                        DetailCard(
                            title = K.sellingRate.tr(),
                            value = CurrencyUtils.formatInr(item.price),
                            icon = MaterialSymbols.Rounded.CurrencyRupee,
                            colors = colors,
                            ff = ff
                        )

                        DetailCard(
                            title = K.gstRate.tr(),
                            value = "${item.taxRate}%",
                            icon = MaterialSymbols.Rounded.Percent,
                            colors = colors,
                            ff = ff
                        )

                        if (item.quantityType.isNotBlank()) {
                            DetailCard(
                                title = K.measureType.tr(),
                                value = item.quantityType,
                                icon = MaterialSymbols.Rounded.Straighten,
                                colors = colors,
                                ff = ff
                            )
                        }

                        if (item.unit.isNotBlank()) {
                            DetailCard(
                                title = K.unit.tr(),
                                value = item.unit,
                                icon = MaterialSymbols.Rounded.Inventory2,
                                colors = colors,
                                ff = ff
                            )
                        }
                    }

                    // Company Availability
                    val availabilityText = if (item.companyIds.isEmpty()) {
                        "${K.all.tr()} ${K.companies.tr()}"
                    } else {
                        item.companyIds.mapNotNull { cId ->
                            val p = allProfiles.find { it.id == cId }
                            p?.shortName?.ifEmpty { p.businessName.values.firstOrNull() }
                        }.joinToString(", ").ifEmpty { "${K.select.tr()} ${K.companies.tr()}" }
                    }
                    DetailCard(
                        title = K.companies.tr(),
                        value = availabilityText,
                        icon = MaterialSymbols.Rounded.Apartment,
                        colors = colors,
                        ff = ff
                    )

                    // Company Price Overrides
                    if (!isCoolie && item.companyPrices.isNotEmpty()) {
                        item.companyPrices.forEach { (cId, overridePrice) ->
                            val p = allProfiles.find { it.id == cId }
                            val cName = p?.shortName?.ifEmpty { p.businessName.values.firstOrNull() } ?: "Company #$cId"
                            DetailCard(
                                title = "$cName ${K.sellingRate.tr()}",
                                value = CurrencyUtils.formatInr(overridePrice),
                                icon = MaterialSymbols.Rounded.CurrencyRupee,
                                colors = colors,
                                ff = ff
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    icon: ImageVector,
    colors: com.elvan.udukkai.theme.ShellColors,
    ff: androidx.compose.ui.text.font.FontFamily?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (colors.isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.025f))
            .border(
                width = 1.dp,
                color = if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value.preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                )
            }
        }
    }
}
