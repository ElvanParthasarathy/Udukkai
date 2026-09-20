package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.localization.PrintLanguageManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Receipt View Screen (பற்றுச்சீட்டு பார்வை)
 * Ported 1:1 from Flutter's patrucheettu_paarvai.dart.
 */
@Composable
fun ReceiptViewScreen(
    receipt: PatrugalTharavuru,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPrint: (() -> Unit)? = null
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val billingConfig = PrintLanguageManager.getConfig(currentMode)
    val isBilingual = if (currentMode == AppMode.KOOLI) true else billingConfig.isBilingual
    val primaryLang = billingConfig.primaryLanguage.code
    val secondaryLang = billingConfig.secondaryLanguage.code

    val customerName = receipt.vaangunarPeyar[primaryLang]
        ?: receipt.vaangunarPeyar[secondaryLang]
        ?: receipt.vaangunarPeyar.values.firstOrNull().orEmpty().ifEmpty { "-" }

    val secondaryCustomerName = if (isBilingual) (receipt.vaangunarPeyar[secondaryLang] ?: "") else ""

    AppBackHandler(enabled = true) {
        onBack()
    }

    val scrollState = rememberLazyListState()

    ViewTile(
        title = "${K.receipt.tr()} #${receipt.patruEn}",
        onBack = onBack,
        scrollState = scrollState,
        onEdit = onEdit,
        onPrint = onPrint
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

            // Customer & Date Header Card
            item(key = "header_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customerName.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    letterSpacing = (-0.2).sp
                                )
                            )
                            if (isBilingual && secondaryCustomerName.isNotBlank() && secondaryCustomerName != customerName) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = secondaryCustomerName.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = LocalShellColors.current.textSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = DateUtils.formatEpochMillis(receipt.patruNaal),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                            if (receipt.seluthumMurai.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = receipt.seluthumMurai.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Total Received Amount Card
            item(key = "total_card") {
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
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = K.grandTotal.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyUtils.formatInr(receipt.thogai),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                        )
                    }
                }
            }

            // Payment Transaction / Reference Details
            if (!receipt.parivarthanaiEn.isNullOrBlank() || !receipt.vangiPeyar.isNullOrBlank()) {
                item(key = "reference_details") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.025f))
                            .border(
                                width = 1.dp,
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!receipt.parivarthanaiEn.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "பரிமாற்ற எண்",
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = receipt.parivarthanaiEn,
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }
                            if (!receipt.vangiPeyar.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = K.bank.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = receipt.vangiPeyar,
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes / Comments
            if (receipt.ullkurippu.isNotBlank()) {
                item(key = "notes_card") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = K.remarks.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.025f))
                                .border(
                                    width = 1.dp,
                                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = receipt.ullkurippu.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    color = colors.textPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
