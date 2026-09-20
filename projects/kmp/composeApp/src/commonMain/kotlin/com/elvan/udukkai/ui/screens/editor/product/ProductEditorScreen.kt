package com.elvan.udukkai.ui.screens.editor.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.PorulTharavuru
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection
import com.elvan.udukkai.ui.screens.editor.ElvanIrumozhiPulan
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiKeezhvirivu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor

/**
 * ProductEditorScreen — Full subpage to create or edit a product/service item.
 * Supports Coolie & Silk modes using Flutter's boxed card editor layout.
 */
@Composable
fun ProductEditorScreen(
    item: PorulTharavuru? = null,
    onBack: () -> Unit
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = item != null && item.id > 0L

    var porulPeyarMap by remember {
        mutableStateOf(item?.porulPeyar ?: emptyMap())
    }
    var hsnCode by remember { mutableStateOf(item?.hsnCode ?: "") }
    var vilai by remember {
        mutableStateOf(
            if (item != null && item.vilai > 0) {
                if (item.vilai % 1.0 == 0.0) item.vilai.toLong().toString() else item.vilai.toString()
            } else ""
        )
    }
    var variVeetham by remember {
        mutableStateOf(
            if (item != null) {
                if (item.variVeetham % 1.0 == 0.0) item.variVeetham.toLong().toString() else item.variVeetham.toString()
            } else "5"
        )
    }
    var alavuVagai by remember { mutableStateOf(item?.alavuVagai?.ifEmpty { "quantity" } ?: "quantity") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.createRecord.tr()
    val nameRequiredMsg = K.productNameRequired.tr()
    val savedMsg = K.productSavedSuccessfully.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    fun handleSave() {
        if (porulPeyarMap.values.none { it.isNotBlank() }) {
            validationError = nameRequiredMsg
            ElvanSnackbar.show(nameRequiredMsg)
            return
        }

        val unit = if (alavuVagai == "weight") "kg" else "Nos"

        val itemToSave = PorulTharavuru(
            id = item?.id ?: 0L,
            porulPeyar = porulPeyarMap.filterValues { it.isNotBlank() },
            hsnCode = if (currentMode == AppMode.PATTU) hsnCode.trim() else "",
            vilai = if (currentMode == AppMode.PATTU) (vilai.trim().toDoubleOrNull() ?: 0.0) else 0.0,
            variVeetham = if (currentMode == AppMode.PATTU) (variVeetham.trim().toDoubleOrNull() ?: 0.0) else 0.0,
            alavuVagai = if (currentMode == AppMode.PATTU) alavuVagai else "quantity",
            alagu = if (currentMode == AppMode.PATTU) unit else "Nos",
            createdAt = item?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val savedId = PorulRepository.save(itemToSave, currentMode)
        if (savedId > 0L) {
            ElvanSnackbar.show(savedMsg)
            onBack()
        } else {
            ElvanSnackbar.show(saveFailedMsg)
        }
    }

    AppBackHandler(enabled = true) {
        onBack()
    }

    val scrollState = rememberLazyListState()

    ElvanSubShell(
        title = pageTitle,
        onBack = onBack,
        scrollState = scrollState,
        hasActions = true,
        actions = {
            ElvanActionButton(
                label = K.saveBtn.tr(),
                onClick = { handleSave() }
            )
        }
    ) {
        CompositionLocalProvider(LocalEditorAccentColor provides colors.productColor) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = Dimens.SubpageContentPaddingBottom
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // Top spacer driven by One UI collapsible header
            item(key = "top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
            }

            // Section 1: Product Details (பொருள் தரவுகள்)
            item(key = "product_name_section") {
                ElvanEditorSection(
                    index = 0,
                    title = K.productDetails.tr()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ElvanIrumozhiPulan(
                            label = K.product.tr(),
                            value = porulPeyarMap,
                            onChanged = {
                                porulPeyarMap = it
                                validationError = null
                            },
                            placeholder = K.product.tr()
                        )

                        if (validationError != null) {
                            Text(
                                text = validationError!!,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        // In Silk mode: Measurement method selector
                        if (currentMode == AppMode.PATTU) {
                            ElvanThiruthiKeezhvirivu<String>(
                                label = K.measurementMethod.tr(),
                                value = if (alavuVagai == "weight") K.weight else K.quantity,
                                items = listOf(K.quantity, K.weight),
                                onSelected = { key ->
                                    alavuVagai = if (key == K.weight) "weight" else "quantity"
                                },
                                itemLabelBuilder = { key ->
                                    val lang = LanguageManager.activeLanguageCode
                                    if (key == K.weight) "${K.weight.trWithLang(lang)} (kg)" else "${K.quantity.trWithLang(lang)} (Nos)"
                                }
                            )
                        }
                    }
                }
            }

            // Section 2: Price & Tax (விலை மற்றும் வரி) - Pattu only
            if (currentMode == AppMode.PATTU) {
                item(key = "price_tax_section") {
                    ElvanEditorSection(
                        index = 1,
                        title = K.pricingAndTax.tr()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // HSN Code
                            ElvanThiruthiUlleedu(
                                label = K.hsnSacCode.tr(),
                                value = hsnCode,
                                onValueChange = { hsnCode = it },
                                placeholder = "50020010",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            // Quick HSN Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("50020010", "50040010", "50072010").forEach { code ->
                                    val isSelected = hsnCode.trim() == code
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { hsnCode = code },
                                        label = { Text(code) },
                                        shape = RoundedCornerShape(100)
                                    )
                                }
                            }

                            // Price
                            ElvanThiruthiUlleedu(
                                label = K.sellingRate.tr(),
                                value = vilai,
                                onValueChange = { vilai = it },
                                placeholder = "0.00",
                                prefixText = "₹ ",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            // GST %
                            ElvanThiruthiUlleedu(
                                label = K.gstRate.tr(),
                                value = variVeetham,
                                onValueChange = { variVeetham = it },
                                placeholder = "5",
                                suffixText = "%",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            // Quick GST rate pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("0", "5", "12", "18").forEach { rate ->
                                    val isSelected = variVeetham.trim() == rate
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { variVeetham = rate },
                                        label = { Text("$rate%") },
                                        shape = RoundedCornerShape(100)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
}
