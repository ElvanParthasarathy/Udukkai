package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.PrintLanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiAttai
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu

/**
 * Customer section for Silk Invoice Editor:
 * - Customer search picker with bottom sheet.
 * - Saved details card with bilingual name, address lines, and GSTIN.
 * Matches Flutter's `pattu_vaangunargal_kooru.dart` 1:1.
 */
@Composable
fun PattuVaangunargalKooru(
    selectedVaangunarId: Long?,
    onCustomerSelected: (VaangunarTharavuru) -> Unit,
    onCustomerCleared: () -> Unit,
    onRequestAddNewCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    val config = PrintLanguageManager.getConfig(currentMode)
    val primaryLang = config.primaryLanguage.code
    val secondaryLang = config.secondaryLanguage.code
    val isBilingual = if (currentMode == AppMode.KOOLI) true else config.isBilingual

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    val allMerchants = VaangunarRepository.merchants

    val selectedVaangunar = remember(selectedVaangunarId, allMerchants) {
        allMerchants.firstOrNull { it.id == selectedVaangunarId }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanThiruthiThalaippu(label = K.clientNameSearch.tr())

        val containerBg = colors.iconBg
        val customerName = selectedVaangunar?.peyar?.get(primaryLang)
            ?: selectedVaangunar?.peyar?.get(secondaryLang)
            ?: selectedVaangunar?.peyar?.values?.firstOrNull()
            ?: ""

        // ── Customer Selector Pill ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(containerBg)
                .clickable { isBottomSheetOpen = true }
                .padding(start = 20.dp, end = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (customerName.isNotEmpty()) customerName else K.selectCustomer.tr()).preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.sp,
                        fontWeight = if (customerName.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                        color = if (customerName.isNotEmpty()) colors.textPrimary else colors.textSecondary.copy(alpha = 0.5f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (selectedVaangunar != null) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onCustomerCleared() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = K.cancel.tr(),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Saved Details Card (One of the few cards in the editor) ──
        if (selectedVaangunar != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ElvanThiruthiAttai(
                padding = PaddingValues(16.dp),
                borderRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = K.savedDetails.tr().preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    )

                // Only secondary language name (primary already shown in pill above)
                val secName = if (isBilingual) selectedVaangunar.peyar[secondaryLang].orEmpty() else ""

                if (secName.isNotEmpty()) {
                    Text(
                        text = secName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    )
                }

                // Address & Oor — bilingual
                val primMugavari = selectedVaangunar.mugavari[primaryLang]
                    ?: selectedVaangunar.mugavari.values.firstOrNull().orEmpty()
                val primOor = selectedVaangunar.oor[primaryLang]
                    ?: selectedVaangunar.oor.values.firstOrNull().orEmpty()
                val primMaanilam = selectedVaangunar.maanilam[primaryLang]
                    ?: selectedVaangunar.maanilam.values.firstOrNull().orEmpty()
                val primAddressCombined = listOf(primMugavari, primOor, primMaanilam, selectedVaangunar.anjalKuriyeedu)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                if (primAddressCombined.isNotBlank()) {
                    Text(
                        text = primAddressCombined.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }

                // Secondary language address
                if (isBilingual) {
                    val secMugavari = selectedVaangunar.mugavari[secondaryLang].orEmpty()
                    val secOor = selectedVaangunar.oor[secondaryLang].orEmpty()
                    val secMaanilam = selectedVaangunar.maanilam[secondaryLang].orEmpty()
                    val secAddressCombined = listOf(secMugavari, secOor, secMaanilam)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")

                    if (secAddressCombined.isNotBlank() && secAddressCombined != primAddressCombined.replace(", ${selectedVaangunar.anjalKuriyeedu}", "")) {
                        Text(
                            text = secAddressCombined.preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                color = colors.textSecondary.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                // GSTIN
                if (selectedVaangunar.gstin.isNotBlank()) {
                    Text(
                        text = "GSTIN: ${selectedVaangunar.gstin.trim()}",
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    )
                }
            }
        }
    }
    }

    // ── Customer Selection Bottom Sheet ──
    if (isBottomSheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.selectCustomer.tr(),
            items = allMerchants,
            currentValue = selectedVaangunar,
            showSearch = true,
            onDismissRequest = { isBottomSheetOpen = false },
            onSelected = { customer ->
                onCustomerSelected(customer)
                isBottomSheetOpen = false
            },
            itemLabelBuilder = { c ->
                c.peyar[primaryLang] ?: c.peyar.values.firstOrNull().orEmpty()
            },
            subtitleBuilder = { c ->
                val sec = if (isBilingual) c.peyar[secondaryLang].orEmpty() else ""
                val oor = c.oor[primaryLang] ?: c.oor.values.firstOrNull().orEmpty()
                listOf(sec, oor).filter { it.isNotEmpty() }.joinToString(" - ")
            },
            searchFilter = { c, query ->
                val q = query.lowercase()
                c.peyar.values.any { it.lowercase().contains(q) } || c.oor.values.any { it.lowercase().contains(q) }
            },
            onRequestAddNew = {
                isBottomSheetOpen = false
                onRequestAddNewCustomer()
            },
            addNewLabel = K.addNew.tr()
        )
    }
}
