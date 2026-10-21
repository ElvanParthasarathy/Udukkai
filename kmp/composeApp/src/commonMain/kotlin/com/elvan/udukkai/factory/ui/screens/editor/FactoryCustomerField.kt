package com.elvan.udukkai.factory.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.PrintLanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorCard
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel

/**
 * Dedicated Customer section for Factory (ஆலை) Invoice Editor:
 * - Simple customer model (weaving / job-work client).
 * - Name + town/oor + address line (no GSTIN / tax details).
 * - Distinct from Textiles customer handling.
 */
@Composable
fun FactoryCustomerField(
    selectedCustomerId: Long?,
    onCustomerSelected: (CustomerModel) -> Unit,
    onCustomerCleared: () -> Unit,
    onRequestAddNewCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    val config = PrintLanguageManager.getFactoryConfig()
    val primaryLang = config.primaryLanguage.code
    val secondaryLang = config.secondaryLanguage.code
    val isBilingual = config.isBilingual

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    val allMerchants = FactoryCustomerRepository.customers

    val selectedCustomer = remember(selectedCustomerId, allMerchants) {
        allMerchants.firstOrNull { it.id == selectedCustomerId }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanEditorLabel(label = K.clientNameSearch.tr())

        val containerBg = colors.iconBg
        val customerName = selectedCustomer?.name?.get(primaryLang)
            ?: selectedCustomer?.name?.get(secondaryLang)
            ?: selectedCustomer?.name?.values?.firstOrNull()
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

                if (selectedCustomer != null) {
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

        // ── Saved Details Card ──
        if (selectedCustomer != null) {
            Spacer(modifier = Modifier.height(24.dp))
            ElvanEditorCard(
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

                    // Secondary language name if bilingual
                    val secName = if (isBilingual) selectedCustomer.name[secondaryLang].orEmpty() else ""
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

                    // Address & Town (Oor)
                    val primMugavari = selectedCustomer.address[primaryLang]
                        ?: selectedCustomer.address.values.firstOrNull().orEmpty()
                    val primOor = selectedCustomer.city[primaryLang]
                        ?: selectedCustomer.city.values.firstOrNull().orEmpty()
                    val primAddressCombined = listOf(primMugavari, primOor)
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

                    if (isBilingual) {
                        val secMugavari = selectedCustomer.address[secondaryLang].orEmpty()
                        val secOor = selectedCustomer.city[secondaryLang].orEmpty()
                        val secAddressCombined = listOf(secMugavari, secOor)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")

                        if (secAddressCombined.isNotBlank() && secAddressCombined != primAddressCombined) {
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
            }
        }
    }
}

    // ── Customer Selection Bottom Sheet ──
    if (isBottomSheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.selectCustomer.tr(),
            items = allMerchants,
            currentValue = selectedCustomer,
            showSearch = true,
            onDismissRequest = { isBottomSheetOpen = false },
            onSelected = { customer ->
                onCustomerSelected(customer)
                isBottomSheetOpen = false
            },
            itemLabelBuilder = { c ->
                c.name[primaryLang] ?: c.name.values.firstOrNull().orEmpty()
            },
            subtitleBuilder = { c ->
                val sec = if (isBilingual) c.name[secondaryLang].orEmpty() else ""
                val oor = c.city[primaryLang] ?: c.city.values.firstOrNull().orEmpty()
                listOf(sec, oor).filter { it.isNotEmpty() }.joinToString(" - ")
            },
            searchFilter = { c, query ->
                val q = query.lowercase()
                c.name.values.any { it.lowercase().contains(q) } || c.city.values.any { it.lowercase().contains(q) }
            },
            onRequestAddNew = {
                isBottomSheetOpen = false
                onRequestAddNewCustomer()
            },
            addNewLabel = K.addNew.tr()
        )
    }
}
