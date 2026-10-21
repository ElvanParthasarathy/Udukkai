package com.elvan.udukkai.ui.screens.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.utils.NumberToWords
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.PaymentAccount
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Invoice View Screen (பட்டியல் பார்வை)
 * Ported 1:1 from Flutter's pattu_pattiyal_paarvai.dart & kooli_pattiyal_paarvai.dart.
 * Enhanced with:
 * - Proforma / Estimate badge & 1-click Convert to Tax Invoice flow
 * - Number to Words in Pure Tamil and English Indian numbering
 * - Bill Options (Vehicle No, Due Date, Tax-inclusive badge)
 * - Selected Bank Account display
 */
@Composable
fun InvoiceViewScreen(
    invoice: InvoiceModel,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile> = emptyList(),
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPrint: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onConvertToTaxInvoice: (() -> Unit)? = null
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val activeProfile = (if (invoice.companyId != null) allProfiles.find { it.id == invoice.companyId } else null)
        ?: profile
    val isBilingual = activeProfile.isBilingual
    val primaryLang = activeProfile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = activeProfile.secondaryLanguage.ifEmpty { "en" }

    val customerName = invoice.customerName[primaryLang]
        ?: invoice.customerName[secondaryLang]
        ?: invoice.customerName.values.firstOrNull().orEmpty().ifEmpty { "-" }

    val secondaryCustomerName = if (isBilingual) (invoice.customerName[secondaryLang] ?: "") else ""

    val customerTown = invoice.customerAddress[primaryLang]
        ?: invoice.customerAddress[secondaryLang]
        ?: invoice.customerAddress.values.firstOrNull().orEmpty()

    val isProforma = invoice.invoiceType == "proforma"
    val docTypeTitle = if (isProforma) K.proforma.tr() else K.invoice.tr()

    val displayOptions = remember(invoice.customPreferencesJson) {
        InvoiceDisplayOptions.fromJson(invoice.customPreferencesJson)
    }

    val selectedBank = remember(invoice.bankDetailsJson, activeProfile, displayOptions.selectedAccountId) {
        val parsed = if (invoice.bankDetailsJson.isNotBlank() && invoice.bankDetailsJson != "{}") {
            PaymentAccount.parseList("[${invoice.bankDetailsJson}]").firstOrNull()
        } else null
        parsed ?: activeProfile.getPaymentAccounts().find { it.id == displayOptions.selectedAccountId }
        ?: activeProfile.getDefaultPaymentAccount()
    }

    val tamilWords = remember(invoice.totalAmount) { NumberToWords.toTamil(invoice.totalAmount) }
    val englishWords = remember(invoice.totalAmount) { NumberToWords.toEnglish(invoice.totalAmount) }

    val scrollState = rememberLazyListState()

    ViewTile(
        title = "$docTypeTitle #${invoice.invoiceNumber}",
        onBack = onBack,
        scrollState = scrollState,
        isDocument = true,
        onEdit = onEdit,
        onPrint = onPrint,
        onCopy = onCopy
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = Dimens.SubpageContentPaddingBottom + 64.dp
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
                                    color = colors.textPrimary
                                )
                            )
                            if (isBilingual && secondaryCustomerName.isNotBlank() && secondaryCustomerName != customerName) {
                                Text(
                                    text = secondaryCustomerName.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 14.sp,
                                        color = colors.textSecondary
                                    )
                                )
                            }
                            if (customerTown.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = customerTown.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.5.sp,
                                        color = colors.textSecondary
                                    )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = DateUtils.formatDate(invoice.invoiceDate),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Invoice Type Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isProforma) colors.accent.copy(alpha = 0.15f)
                                        else if (isDark) Color.White.copy(alpha = 0.08f)
                                        else Color.Black.copy(alpha = 0.06f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isProforma) K.proforma.tr() else K.taxInvoice.tr(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isProforma) colors.accent else colors.textPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 1-Click Convert Proforma to Tax Invoice CTA Button
            if (isProforma && onConvertToTaxInvoice != null) {
                item(key = "convert_cta") {
                    Button(
                        onClick = onConvertToTaxInvoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = K.convertToTaxInvoice.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // Bill Options Card (Vehicle No, Due Date, Tax-Inclusive badge)
            val hasActiveOptions = (displayOptions.showVehicle && displayOptions.vehicleNumber.isNotBlank()) ||
                (displayOptions.showDueDate && displayOptions.dueDate != null) ||
                displayOptions.taxInclusive

            if (hasActiveOptions) {
                item(key = "bill_options_card") {
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = K.billOptions.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )

                            if (displayOptions.showVehicle && displayOptions.vehicleNumber.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = K.vehicleNumber.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = displayOptions.vehicleNumber,
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }

                            if (displayOptions.showDueDate && displayOptions.dueDate != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = K.dueDate.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = DateUtils.formatDate(displayOptions.dueDate ?: 0L),
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }

                            if (displayOptions.taxInclusive) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = K.taxInclusive.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = "✓",
                                        style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bank Details Card
            if (selectedBank != null && (selectedBank.accountNumber.isNotBlank() || selectedBank.upiId.isNotBlank())) {
                item(key = "bank_details_card") {
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
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.AccountBalance,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = K.bankAccount.tr().preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${selectedBank.label})",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            val bName = selectedBank.bankName[primaryLang] ?: selectedBank.bankName["ta"] ?: selectedBank.bankName.values.firstOrNull() ?: ""
                            if (bName.isNotBlank()) {
                                Text(
                                    text = "${K.bankName.tr()}: $bName",
                                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary)
                                )
                            }
                            if (selectedBank.accountNumber.isNotBlank()) {
                                Text(
                                    text = "${K.accountNumber.tr()}: ${selectedBank.accountNumber}",
                                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                                )
                            }
                            if (selectedBank.ifsc.isNotBlank()) {
                                Text(
                                    text = "IFSC: ${selectedBank.ifsc}",
                                    style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textSecondary)
                                )
                            }
                            if (selectedBank.upiId.isNotBlank()) {
                                Text(
                                    text = "UPI: ${selectedBank.upiId}",
                                    style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textSecondary)
                                )
                            }
                        }
                    }
                }
            }

            // Factory Specific Additional Charges Card
            if (invoice.wastageGrams > 0 || invoice.postalCharges > 0 || invoice.ahimsaSilkAmount > 0) {
                item(key = "factory_charges_card") {
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
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (invoice.wastageGrams > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "சேதாரம் (கிராம்)",
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = "${invoice.wastageGrams} g",
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }
                            if (invoice.postalCharges > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "அஞ்சல் கட்டணம்",
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatInr(invoice.postalCharges),
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }
                            if (invoice.ahimsaSilkAmount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "அகிம்சா பட்டு",
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, color = colors.textSecondary)
                                    )
                                    Text(
                                        text = CurrencyUtils.formatInr(invoice.ahimsaSilkAmount),
                                        style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Total Amount Card
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
                            text = CurrencyUtils.formatInr(invoice.totalAmount),
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

            // Amount in Words Card (Pure Tamil + English)
            item(key = "amount_in_words_card") {
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = K.amountInWords.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        )
                        Text(
                            text = tamilWords.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.accent
                            )
                        )
                        Text(
                            text = englishWords,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary
                            )
                        )
                    }
                }
            }

            // Internal Notes / Comments
            if (invoice.notes.isNotBlank()) {
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
                                text = invoice.notes.preventBrokenLigatures(),
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
