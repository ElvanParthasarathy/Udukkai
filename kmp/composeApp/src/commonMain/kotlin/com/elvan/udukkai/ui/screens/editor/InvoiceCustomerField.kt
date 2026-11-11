package com.elvan.udukkai.ui.screens.editor

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.fullAddress
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.DataLanguageDisplay
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorCard
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel

@Composable
fun InvoiceCustomerField(
    customers: List<CustomerModel>,
    profile: BusinessProfile,
    savedCustomer: CustomerModel? = null,
    selectedCustomerId: Long?,
    onCustomerSelected: (CustomerModel) -> Unit,
    onCustomerCleared: () -> Unit,
    onRequestAddNewCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    val language = DataLanguageDisplay(profile)
    var isBottomSheetOpen by remember { mutableStateOf(false) }
    val selectedCustomer = savedCustomer ?: customers.firstOrNull { it.id == selectedCustomerId }
    var lastCustomer by remember { mutableStateOf<CustomerModel?>(null) }
    if (selectedCustomer != null) {
        lastCustomer = selectedCustomer
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ElvanEditorLabel(label = K.clientNameSearch.tr())

        val containerBg = colors.iconBg
        val customerName = selectedCustomer?.let { language.first(it.billName()) }.orEmpty()

        // ── Customer Selector Pill ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(containerBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = colors.accent)
                ) { isBottomSheetOpen = true }
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 18.dp)
                            ) { onCustomerCleared() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Close,
                            contentDescription = K.cancel.tr(),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Saved Details Card (Smooth Fluid Entrance & Reverse Exit Animation) ──
        AnimatedVisibility(
            visible = selectedCustomer != null,
            enter = expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeOut()
        ) {
            val displayCustomer = selectedCustomer ?: lastCustomer
            if (displayCustomer != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))
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

                            val secondaryName = language.second(displayCustomer.billName())
                            if (secondaryName.isNotBlank()) {
                                Text(secondaryName.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary))
                            }
                            language.lines(displayCustomer::fullAddress).forEach { (address, primary) ->
                                Text(address.preventBrokenLigatures(), style = TextStyle(fontFamily = ff, fontSize = 13.sp,
                                    lineHeight = 18.sp, color = if (primary) colors.textSecondary else colors.textTertiary))
                            }

                            // GSTIN
                            if (displayCustomer.gstin.isNotBlank()) {
                                Text(
                                    text = "GSTIN: ${displayCustomer.gstin.trim()}",
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
        }
    }

    // ── Customer Selection Bottom Sheet ──
    if (isBottomSheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.selectCustomer.tr(),
            items = customers,
            currentValue = customers.firstOrNull { it.id == selectedCustomerId },
            showSearch = true,
            onDismissRequest = { isBottomSheetOpen = false },
            onSelected = { customer ->
                onCustomerSelected(customer)
                isBottomSheetOpen = false
            },
            itemLabelBuilder = { language.first(it.billName()) },
            subtitleBuilder = { customer ->
                listOf(language.second(customer.billName()), language.first(customer.city))
                    .filter { it.isNotBlank() }.joinToString(" • ")
            },
            searchFilter = { c, query ->
                val q = query.lowercase()
                c.name.values.any { it.lowercase().contains(q) } ||
                c.city.values.any { it.lowercase().contains(q) } ||
                c.gstin.lowercase().contains(q)
            },
            onRequestAddNew = {
                isBottomSheetOpen = false
                onRequestAddNewCustomer()
            },
            addNewLabel = K.addNew.tr()
        )
    }
}
