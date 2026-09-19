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
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.localization.K
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
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    val allMerchants = VaangunarRepository.merchants

    val selectedVaangunar = remember(selectedVaangunarId, allMerchants) {
        allMerchants.firstOrNull { it.id == selectedVaangunarId }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanThiruthiThalaippu(label = K.clientNameSearch.tr())

        val containerBg = colors.iconBg
        val customerName = selectedVaangunar?.peyar?.get("ta")
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

                // Bilingual name
                val enName = selectedVaangunar.peyar["en"].orEmpty()
                val taName = selectedVaangunar.peyar["ta"] ?: selectedVaangunar.peyar.values.firstOrNull().orEmpty()

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = taName.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    )
                    if (enName.isNotEmpty() && enName != taName) {
                        Text(
                            text = enName,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary
                            )
                        )
                    }
                }

                // Address & Oor
                val taMugavari = selectedVaangunar.mugavari["ta"] ?: selectedVaangunar.mugavari.values.firstOrNull().orEmpty()
                val taOor = selectedVaangunar.oor["ta"] ?: selectedVaangunar.oor.values.firstOrNull().orEmpty()
                val taMaanilam = selectedVaangunar.maanilam["ta"] ?: selectedVaangunar.maanilam.values.firstOrNull().orEmpty()
                val addressCombined = listOf(taMugavari, taOor, taMaanilam, selectedVaangunar.anjalKuriyeedu)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                if (addressCombined.isNotBlank()) {
                    Text(
                        text = addressCombined.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )
                    )
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
                c.peyar["ta"] ?: c.peyar.values.firstOrNull().orEmpty()
            },
            subtitleBuilder = { c ->
                val en = c.peyar["en"].orEmpty()
                val oor = c.oor["ta"] ?: c.oor.values.firstOrNull().orEmpty()
                listOf(en, oor).filter { it.isNotEmpty() }.joinToString(" - ")
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
