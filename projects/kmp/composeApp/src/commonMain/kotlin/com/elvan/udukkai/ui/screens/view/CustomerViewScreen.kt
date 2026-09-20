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
import com.elvan.udukkai.data.model.VaangunarTharavuru
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
 * Customer / Merchant View Screen (வாங்குநர் பார்வை)
 * Ported 1:1 from Flutter's vaangunar_paarvai.dart.
 */
@Composable
fun CustomerViewScreen(
    merchant: VaangunarTharavuru,
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

    val p1 = merchant.peyar[primaryLang] ?: merchant.peyar["ta"] ?: ""
    val p2 = merchant.peyar[secondaryLang] ?: merchant.peyar["en"] ?: ""
    val primaryName = p1.ifEmpty { p2.ifEmpty { "-" } }
    val secondaryName = if (isBilingual && p1.isNotEmpty() && p2.isNotEmpty() && p1 != p2) p2 else ""

    val oorVal = merchant.oor[primaryLang] ?: merchant.oor[secondaryLang] ?: merchant.oor.values.firstOrNull().orEmpty()
    val mugavariVal = merchant.mugavari[primaryLang] ?: merchant.mugavari[secondaryLang] ?: merchant.mugavari.values.firstOrNull().orEmpty()

    AppBackHandler(enabled = true) {
        onBack()
    }

    val scrollState = rememberLazyListState()

    ViewTile(
        title = K.businessDetails.tr(),
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

            // Customer Header Card
            item(key = "header_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color(0xFF1E3A5F).copy(alpha = 0.25f) else Color(0xFFE8F1FC))
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color(0xFF3B82F6).copy(alpha = 0.25f) else Color(0xFF93C5FD).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF3B82F6).copy(alpha = 0.3f) else Color(0xFFBFDBFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (primaryName.isNotEmpty()) primaryName.take(1).uppercase() else "?",
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                                )
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

                        if (merchant.anjalKuriyeedu.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = merchant.anjalKuriyeedu,
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
                        title = K.city.tr(),
                        value = oorVal.ifEmpty { "-" },
                        icon = MaterialSymbols.Rounded.LocationOn,
                        colors = colors,
                        ff = ff
                    )

                    DetailCard(
                        title = K.address.tr(),
                        value = mugavariVal.ifEmpty { "-" },
                        icon = MaterialSymbols.Rounded.Apartment,
                        colors = colors,
                        ff = ff
                    )

                    if (merchant.tholaipaesi.isNotBlank()) {
                        DetailCard(
                            title = K.telephone.tr(),
                            value = merchant.tholaipaesi,
                            icon = MaterialSymbols.Rounded.Call,
                            colors = colors,
                            ff = ff
                        )
                    }

                    if (!merchant.gstin.isNullOrBlank()) {
                        DetailCard(
                            title = "GSTIN",
                            value = merchant.gstin,
                            icon = MaterialSymbols.Rounded.AccountBalance,
                            colors = colors,
                            ff = ff
                        )
                    }

                    if (!merchant.minnanjal.isNullOrBlank()) {
                        DetailCard(
                            title = K.email.tr(),
                            value = merchant.minnanjal,
                            icon = MaterialSymbols.Rounded.Email,
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
