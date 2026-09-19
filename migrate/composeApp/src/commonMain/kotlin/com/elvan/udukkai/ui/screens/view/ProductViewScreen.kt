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
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.localization.PrintLanguageManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Product / Item View Screen (பொருள் பார்வை)
 * Ported 1:1 from Flutter's porul_paarvai.dart.
 */
@Composable
fun ProductViewScreen(
    item: PorulTharavuru,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val billingConfig = PrintLanguageManager.getConfig(currentMode)
    val isBilingual = if (currentMode == AppMode.KOOLI) true else billingConfig.isBilingual
    val primaryLang = billingConfig.primaryLanguage.code
    val secondaryLang = billingConfig.secondaryLanguage.code

    val p1 = item.porulPeyar[primaryLang] ?: item.porulPeyar["ta"] ?: ""
    val p2 = item.porulPeyar[secondaryLang] ?: item.porulPeyar["en"] ?: ""
    val primaryName = p1.ifEmpty { p2.ifEmpty { "-" } }
    val secondaryName = if (isBilingual && p1.isNotEmpty() && p2.isNotEmpty() && p1 != p2) p2 else ""

    AppBackHandler(enabled = true) {
        onBack()
    }

    val scrollState = rememberLazyListState()

    ViewTile(
        title = K.productDetails.tr(),
        onBack = onBack,
        scrollState = scrollState,
        onEdit = onEdit
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

            // Product Header Card
            item(key = "header_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF3B1E54).copy(alpha = 0.25f) else Color(0xFFF3E8FF))
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.25f) else Color(0xFFC4B5FD).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Icon Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.3f) else Color(0xFFDDD6FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Inventory2,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9),
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
                    DetailCard(
                        title = K.sellingRate.tr(),
                        value = CurrencyUtils.formatInr(item.vilai),
                        icon = MaterialSymbols.Rounded.CurrencyRupee,
                        colors = colors,
                        ff = ff
                    )

                    DetailCard(
                        title = K.gstRate.tr(),
                        value = "${item.variVeetham}%",
                        icon = MaterialSymbols.Rounded.Percent,
                        colors = colors,
                        ff = ff
                    )

                    if (item.alavuVagai.isNotBlank()) {
                        DetailCard(
                            title = K.measureType.tr(),
                            value = item.alavuVagai,
                            icon = MaterialSymbols.Rounded.Straighten,
                            colors = colors,
                            ff = ff
                        )
                    }

                    if (item.alagu.isNotBlank()) {
                        DetailCard(
                            title = K.unit.tr(),
                            value = item.alagu,
                            icon = MaterialSymbols.Rounded.Inventory2,
                            colors = colors,
                            ff = ff
                        )
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
