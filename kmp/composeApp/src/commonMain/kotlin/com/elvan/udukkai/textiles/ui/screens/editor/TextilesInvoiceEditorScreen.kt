package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.screens.editor.ElvanEditorBottomBar



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesProductRepository
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import androidx.compose.ui.zIndex
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor
import com.elvan.udukkai.ui.screens.editor.invoice.components.*

/**
 * Form Lock container matching Flutter's `Opacity(0.4) + IgnorePointer`
 * Locks and dims all downstream form sections until a business profile is selected.
 */
@Composable
private fun FormLockWrapper(
    isLocked: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = if (isLocked) 0.4f else 1f }
    ) {
        content()
        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
        }
    }
}

/**
 * Pixel-Perfect Textiles (சவுளி / GST) Invoice Editor — matches Flutter's `SilkInvoiceEditor` 1:1.
 * Features:
 * - Bottom-up sheet selectors for Customer, Product, Invoice Type, and Company.
 * - Real-time CGST/SGST (Intra-state) vs IGST (Inter-state) GST calculation engine.
 * - Smooth 300ms animated line items addition and deletion.
 * - Unsaved changes confirmation action sheet guard.
 */
@Composable
fun TextilesInvoiceEditorScreen(
    invoice: PattiyalTharavuru? = null,
    onBack: () -> Unit,
    onRequestAddNewCustomer: () -> Unit = {},
    onRequestAddNewProduct: () -> Unit = {}
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val isEditing = invoice != null && invoice.id > 0L

    LaunchedEffect(Unit) {
        TextilesProfileRepository.refreshFromDatabase()
        TextilesProductRepository.loadAll()
        TextilesCustomerRepository.loadAll()
        TextilesInvoiceRepository.loadAll()
    }

    val profiles: List<NiruvanaTharavugal> = TextilesProfileRepository.profiles
    val activeProfile = TextilesProfileRepository.profile
    val selectedProfile = remember(profiles, activeProfile, invoice?.niruvanamId) {
        if (isEditing) {
            profiles.firstOrNull { it.id == invoice?.niruvanamId } ?: activeProfile
        } else {
            activeProfile
        }
    }
    val selectedNiruvanamId = if (isEditing) (invoice?.niruvanamId ?: selectedProfile.id) else (selectedProfile.id ?: profiles.firstOrNull()?.id)
    val isProfileConfigured = profiles.isNotEmpty() && selectedNiruvanamId != null

    fun computePrefix(p: NiruvanaTharavugal?): String {
        return if (p != null && p.kurumPeyar.isNotEmpty()) p.kurumPeyar else "INV"
    }

    val initialPrefix = computePrefix(selectedProfile)
    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.patrucheettuEn?.ifEmpty {
                TextilesInvoiceRepository.getNextInvoiceNumber(selectedNiruvanamId, initialPrefix)
            } ?: TextilesInvoiceRepository.getNextInvoiceNumber(selectedNiruvanamId, initialPrefix)
        )
    }
    var isInvoiceNumberOverridden by remember { mutableStateOf(isEditing) }
    var showAddProductEditor by remember { mutableStateOf(false) }
    var showAddCustomerEditor by remember { mutableStateOf(false) }

    // Customer
    var selectedVaangunarId by remember { mutableStateOf(invoice?.vaangunarId) }
    var selectedVaangunarPeyarMap by remember { mutableStateOf(invoice?.vaangunarPeyar ?: emptyMap()) }
    var selectedVaangunarMunvariMap by remember { mutableStateOf(invoice?.vaangunarMunvari ?: emptyMap()) }
    var customerState by remember { mutableStateOf("") }

    val initialViruppangal = remember(invoice) {
        TextilesCalculation.preferencesFromJson(invoice?.sonthaViruppangal)
    }

    // Metadata
    var pattiyalVagai by remember { mutableStateOf(invoice?.pattiyalVagai?.ifEmpty { "tax-invoice" } ?: "tax-invoice") }
    var invoiceDate by remember { mutableStateOf(invoice?.pattiyalNaal ?: System.currentTimeMillis()) }
    var placeOfSupplyEn by remember {
        mutableStateOf(
            if (invoice != null && initialViruppangal.placeOfSupply.isNotEmpty()) initialViruppangal.placeOfSupply else "Tamil Nadu"
        )
    }
    var placeOfSupplyTa by remember {
        mutableStateOf(
            if (invoice != null && initialViruppangal.placeOfSupplyTa.isNotEmpty()) initialViruppangal.placeOfSupplyTa else "தமிழ்நாடு"
        )
    }

    // Line items
    var items by remember {
        mutableStateOf(
            if (invoice != null) {
                TextilesCalculation.itemsFromJson(invoice.tharavugal).ifEmpty { listOf(TextilesItem()) }
            } else {
                listOf(TextilesItem())
            }
        )
    }
    val initialItemIds = remember { items.map { it.id }.toSet() }
    var deletingIds by remember { mutableStateOf(setOf<String>()) }
    val activeItems = remember(items, deletingIds) { items.filter { it.id !in deletingIds } }

    // Global discount
    var globalDiscountValue by remember {
        mutableStateOf(
            if (invoice != null && invoice.podhuThallupadiMathippu > 0) {
                invoice.podhuThallupadiMathippu.toString()
            } else if (invoice != null && initialViruppangal.globalDiscountValue > 0) {
                initialViruppangal.globalDiscountValue.toString()
            } else ""
        )
    }
    var globalDiscountType by remember {
        mutableStateOf(
            invoice?.podhuThallupadiVagai?.ifEmpty { initialViruppangal.globalDiscountType } ?: initialViruppangal.globalDiscountType
        )
    }

    // Guards & states
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isExiting by remember { mutableStateOf(false) }

    // Draft persistence
    val draftKey = "udukkai_draft_textiles_invoice"
    val prefs = remember { getPreferencesHelper() }
    var showDraftRestoreDialog by remember { mutableStateOf(false) }
    var pendingDraftJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!isEditing) {
            val saved = prefs.getString(draftKey)
            if (!saved.isNullOrBlank()) {
                pendingDraftJson = saved
                showDraftRestoreDialog = true
            }
        }
    }

    fun serializeDraft(): String {
        val itemsJson = TextilesCalculation.itemsToJson(items)
        val sb = StringBuilder("{")
        sb.append("\"selectedNiruvanamId\":").append(selectedNiruvanamId ?: "null").append(",")
        sb.append("\"selectedVaangunarId\":").append(selectedVaangunarId ?: "null").append(",")
        sb.append("\"vaangunarPeyar\":").append(MozhiJsonConverter.stringify(selectedVaangunarPeyarMap)).append(",")
        sb.append("\"vaangunarMunvari\":").append(MozhiJsonConverter.stringify(selectedVaangunarMunvariMap)).append(",")
        sb.append("\"pattiyalVagai\":\"").append(pattiyalVagai).append("\",")
        sb.append("\"invoiceDate\":").append(invoiceDate).append(",")
        sb.append("\"placeOfSupplyEn\":\"").append(placeOfSupplyEn).append("\",")
        sb.append("\"placeOfSupplyTa\":\"").append(placeOfSupplyTa).append("\",")
        sb.append("\"invoiceNumberOverride\":\"").append(if (isInvoiceNumberOverridden) invoiceNumber else "").append("\",")
        sb.append("\"globalDiscountValue\":\"").append(globalDiscountValue).append("\",")
        sb.append("\"globalDiscountType\":\"").append(globalDiscountType).append("\",")
        sb.append("\"items\":").append(itemsJson)
        sb.append("}")
        return sb.toString()
    }

    fun restoreDraft(json: String) {
        try {
            val vaangunarId = Regex(""""selectedVaangunarId"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            if (vaangunarId != null) selectedVaangunarId = vaangunarId

            val peyarMatch = Regex(""""vaangunarPeyar"\s*:\s*(\{[^}]*\})""").find(json)?.groupValues?.get(1)
            if (peyarMatch != null) selectedVaangunarPeyarMap = MozhiJsonConverter.parse(peyarMatch)

            val munvariMatch = Regex(""""vaangunarMunvari"\s*:\s*(\{[^}]*\})""").find(json)?.groupValues?.get(1)
            if (munvariMatch != null) selectedVaangunarMunvariMap = MozhiJsonConverter.parse(munvariMatch)

            val vagai = Regex(""""pattiyalVagai"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!vagai.isNullOrEmpty()) pattiyalVagai = vagai

            val date = Regex(""""invoiceDate"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            if (date != null && date > 0) invoiceDate = date

            val posEn = Regex(""""placeOfSupplyEn"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!posEn.isNullOrEmpty()) placeOfSupplyEn = posEn

            val posTa = Regex(""""placeOfSupplyTa"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!posTa.isNullOrEmpty()) placeOfSupplyTa = posTa

            val numOverride = Regex(""""invoiceNumberOverride"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!numOverride.isNullOrEmpty()) {
                invoiceNumber = numOverride
                isInvoiceNumberOverridden = true
            }

            val discVal = Regex(""""globalDiscountValue"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!discVal.isNullOrEmpty()) globalDiscountValue = discVal

            val discType = Regex(""""globalDiscountType"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!discType.isNullOrEmpty()) globalDiscountType = discType

            val itemsMatch = Regex(""""items"\s*:\s*(\[.*?\])(?=\s*,\s*"\w+"|\s*\})""").find(json)?.groupValues?.get(1)
            if (!itemsMatch.isNullOrEmpty()) {
                val parsedItems = TextilesCalculation.itemsFromJson(itemsMatch)
                if (parsedItems.isNotEmpty()) items = parsedItems
            }
            hasUnsavedChanges = true
        } catch (_: Exception) {}
    }

    DisposableEffect(Unit) {
        onDispose {
            if (hasUnsavedChanges && !isEditing && !isExiting) {
                prefs.setString(draftKey, serializeDraft())
            }
        }
    }

    // Automatic calculation engine
    val totals = remember(activeItems, globalDiscountValue, globalDiscountType, selectedProfile.maanilam, customerState) {
        val discountVal = globalDiscountValue.toDoubleOrNull() ?: 0.0
        val bState = selectedProfile.maanilam["en"] ?: selectedProfile.maanilam.values.firstOrNull().orEmpty()
        TextilesCalculation.calculate(
            items = activeItems,
            globalDiscountValue = discountVal,
            globalDiscountType = globalDiscountType,
            businessState = bState,
            customerState = placeOfSupplyEn,
            country = selectedProfile.naadu["en"] ?: "India"
        )
    }

    val handleBack: () -> Unit = {
        if (!isExiting) {
            if (hasUnsavedChanges) {
                showUnsavedDialog = true
            } else {
                isExiting = true
                onBack()
            }
        }
    }

    AppBackHandler(enabled = !isExiting, onBack = handleBack)

    val niruvanamRequiredMsg = K.selectCompanyProfile.tr()
    val vaangunarRequiredMsg = K.selectCustomer.tr()
    val porulRequiredMsg = K.addAtLeastOneProduct.tr()
    val receiptNumberRequiredMsg = K.receiptNumber.tr()
    val saveBtnLabel = K.saveBtn.tr()

    val handleSave: () -> Unit = {
        errorMessage = null
        if (!isProfileConfigured) {
            errorMessage = niruvanamRequiredMsg
        } else if (selectedVaangunarId == null && selectedVaangunarPeyarMap.isEmpty()) {
            errorMessage = vaangunarRequiredMsg
        } else if (invoiceNumber.isBlank()) {
            errorMessage = receiptNumberRequiredMsg
        } else {
            val validItems = activeItems.filter { it.alavu > 0 && it.vilai > 0 }
            if (validItems.isEmpty()) {
                errorMessage = porulRequiredMsg
            } else {
                isSaving = true
                val nextVanakkam = TextilesInvoiceRepository.getNextVanakkam(selectedNiruvanamId)
                val newInvoice = PattiyalTharavuru(
                    id = invoice?.id ?: 0L,
                    niruvanamId = selectedNiruvanamId,
                    patrucheettuEn = invoiceNumber,
                    vanakkam = if (invoice != null && invoice.vanakkam > 0) invoice.vanakkam else nextVanakkam,
                    pattiyalVagai = pattiyalVagai,
                    vaangunarId = selectedVaangunarId,
                    vaangunarPeyar = selectedVaangunarPeyarMap,
                    vaangunarMunvari = selectedVaangunarMunvariMap,
                    pattiyalNaal = invoiceDate,
                    tharavugal = TextilesCalculation.itemsToJson(validItems),
                    mothaThogai = totals.mothaMothangal,
                    thallupadi = totals.thallupadiMothangal,
                    podhuThallupadiMathippu = globalDiscountValue.toDoubleOrNull() ?: 0.0,
                    podhuThallupadiVagai = globalDiscountType,
                    podhuThallupadiThogai = 0.0,
                    variThogai = totals.variMothangal,
                    variTharavugal = TextilesCalculation.taxToJson(totals),
                    sonthaViruppangal = TextilesCalculation.preferencesToJson(
                        TextilesPreferences(
                            globalDiscountValue = globalDiscountValue.toDoubleOrNull() ?: 0.0,
                            globalDiscountType = globalDiscountType,
                            placeOfSupply = placeOfSupplyEn,
                            placeOfSupplyTa = placeOfSupplyTa
                        )
                    ),
                    updatedAt = System.currentTimeMillis()
                )
                TextilesInvoiceRepository.save(newInvoice)
                prefs.setString(draftKey, null)
                isExiting = true
                hasUnsavedChanges = false
                isSaving = false
                onBack()
            }
        }
    }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.createRecord.tr()
    val scrollState = rememberLazyListState()

    ElvanSubShell(
        title = pageTitle,
        onBack = handleBack,
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        floatingBar = {
            ElvanEditorBottomBar(
                label = saveBtnLabel,
                onClick = handleSave,
                enabled = !isSaving,
                isLoading = isSaving,
                accentColor = colors.invoiceColor
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalEditorAccentColor provides colors.invoiceColor) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = Dimens.SubpageContentPaddingBottom + 72.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Top spacer driven by One UI collapsible header
                item(key = "top_spacer") {
                    Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
                }

                // ── Error Message Banner ──
                if (!errorMessage.isNullOrBlank()) {
                    item(key = "error_banner") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!.preventBrokenLigatures(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                if (!isProfileConfigured) {
                    item(key = "unconfigured_warning") {
                        ElvanSettingsSection(colors = colors) {
                            ElvanSimpleSettingsRow(
                                title = K.selectCompanyProfile.tr(),
                                description = K.companyDetail.tr(),
                                colors = colors
                            )
                        }
                    }
                }

                // ── Section 0: ① Billed To (பெறுநர்) ──
                item(key = "customer_section") {
                    ElvanEditorSection(index = 0, title = K.billedTo.tr()) {
                        TextilesCustomerField(
                            selectedVaangunarId = selectedVaangunarId,
                            onCustomerSelected = { customer ->
                                selectedVaangunarId = customer.id
                                selectedVaangunarPeyarMap = customer.peyar
                                selectedVaangunarMunvariMap = customer.oor
                                val sEn = customer.maanilam["en"].orEmpty()
                                val sTa = customer.maanilam["ta"] ?: customer.maanilam.values.firstOrNull().orEmpty()
                                customerState = sEn
                                if (sEn.isNotEmpty()) {
                                    placeOfSupplyEn = sEn
                                    placeOfSupplyTa = sTa
                                }
                                hasUnsavedChanges = true
                                errorMessage = null
                            },
                            onCustomerCleared = {
                                selectedVaangunarId = null
                                selectedVaangunarPeyarMap = emptyMap()
                                selectedVaangunarMunvariMap = emptyMap()
                                customerState = ""
                                hasUnsavedChanges = true
                            },
                            onRequestAddNewCustomer = { showAddCustomerEditor = true }
                        )
                    }
                }

                // ── Section 1: ② Invoice Details (பட்டியல் தரவுகள்) ──
                item(key = "invoice_details_section") {
                    ElvanEditorSection(index = 1, title = K.invoiceDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            val profilePrefix = if (selectedProfile.kurumPeyar.isNotEmpty()) {
                                "${selectedProfile.kurumPeyar}-"
                            } else {
                                "INV-"
                            }

                            // Invoice Number with pencil edit pill
                            ElvanAavanaEnnKooru(
                                label = K.invoiceNumber.tr(),
                                prefix = profilePrefix,
                                initialFullNumber = invoiceNumber,
                                onFullNumberChanged = {
                                    invoiceNumber = it
                                    isInvoiceNumberOverridden = true
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true }
                            )

                            // Date Picker Pill
                            PattiyalNaalKooru(
                                label = K.date.tr(),
                                selectedDate = invoiceDate,
                                onDateChanged = {
                                    invoiceDate = it
                                    hasUnsavedChanges = true
                                }
                            )

                            // Place of Supply with Indian states selection
                            TextilesPlaceOfSupplyField(
                                placeOfSupplyEn = placeOfSupplyEn,
                                placeOfSupplyTa = placeOfSupplyTa,
                                onSelected = { en, ta ->
                                    placeOfSupplyEn = en
                                    placeOfSupplyTa = ta
                                    hasUnsavedChanges = true
                                },
                                onCleared = {
                                    placeOfSupplyEn = ""
                                    placeOfSupplyTa = ""
                                    hasUnsavedChanges = true
                                }
                            )

                            // Invoice Type Selector (Tax Invoice / Proforma)
                            TextilesInvoiceTypeField(
                                pattiyalVagai = pattiyalVagai,
                                onChanged = {
                                    pattiyalVagai = it
                                    hasUnsavedChanges = true
                                }
                            )
                        }
                    }
                }

                // ── Section 2: ③ Items (பொருட்கள்) ──
                item(key = "items_section") {
                    ElvanEditorSection(index = 2, title = K.products.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ElvanAsaiPattiyal {
                                items.forEachIndexed { idx, itm ->
                                    key(itm.id) {
                                        ElvanAsaiCard(
                                            key = itm.id,
                                            isInitial = itm.id in initialItemIds,
                                            onDeleted = {
                                                deletingIds = deletingIds - itm.id
                                                items = items.filter { it.id != itm.id }
                                                hasUnsavedChanges = true
                                            }
                                        ) { requestDelete ->
                                            TextilesItemCard(
                                                item = itm,
                                                index = idx,
                                                itemCount = activeItems.size,
                                                niruvanamId = selectedNiruvanamId,
                                                onItemUpdated = { updated ->
                                                    items = items.map { if (it.id == itm.id) updated else it }
                                                    hasUnsavedChanges = true
                                                },
                                                onItemDeleted = {
                                                    if (activeItems.size > 1) {
                                                        deletingIds = deletingIds + itm.id
                                                        hasUnsavedChanges = true
                                                        requestDelete()
                                                    }
                                                },
                                                onItemCleared = {
                                                    items = items.map { if (it.id == itm.id) TextilesItem(id = itm.id) else it }
                                                    hasUnsavedChanges = true
                                                },
                                                onDirty = { hasUnsavedChanges = true },
                                                onRequestAddNewProduct = { showAddProductEditor = true }
                                            )
                                        }
                                    }
                                }
                            }

                            // Add Item Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(colors.iconBg)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(color = colors.accent),
                                        onClick = {
                                            items = items + TextilesItem()
                                            hasUnsavedChanges = true
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Add,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = K.addProductBtn.tr().preventBrokenLigatures(),
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

                // ── Section 3: ④ Discount (தள்ளுபடி) ──
                item(key = "discount_section") {
                    ElvanEditorSection(index = 3, title = K.discount.tr()) {
                        TextilesDiscountField(
                            discountValue = globalDiscountValue,
                            discountType = globalDiscountType,
                            onValueChanged = {
                                globalDiscountValue = it
                                hasUnsavedChanges = true
                            },
                            onTypeChanged = {
                                globalDiscountType = it
                                hasUnsavedChanges = true
                            }
                        )
                    }
                }

                // ── Section 4: ⑤ Totals (மொத்தங்கள்) ──
                item(key = "totals_section") {
                    ElvanEditorSection(index = 4, title = K.totals.tr()) {
                        TextilesTotalsField(totals = totals)
                    }
                }
            }
        }
    }
    }

    // ── Unsaved Changes Guard Action Sheet ──
    if (showUnsavedDialog && !isExiting) {
        ElvanActionSheet(
            title = K.pendingSave.tr(),
            cancelText = K.continueBtn.tr(),
            confirmText = K.saveBtn.tr(),
            tertiaryText = K.discardBtn.tr(),
            onDismissRequest = { showUnsavedDialog = false },
            onConfirm = {
                showUnsavedDialog = false
                handleSave()
            },
            onTertiary = {
                isExiting = true
                showUnsavedDialog = false
                hasUnsavedChanges = false
                prefs.setString(draftKey, null)
                onBack()
            }
        )
    }

    // ── Restore Unsaved Draft Action Sheet ──
    if (showDraftRestoreDialog && pendingDraftJson != null) {
        ElvanActionSheet(
            title = K.unsavedDraftRestore.tr(),
            cancelText = K.discardBtn.tr(),
            confirmText = K.recover.tr(),
            onDismissRequest = {
                showDraftRestoreDialog = false
                prefs.setString(draftKey, null)
                pendingDraftJson = null
            },
            onConfirm = {
                showDraftRestoreDialog = false
                restoreDraft(pendingDraftJson!!)
                pendingDraftJson = null
            }
        )
    }

    // ── In-Editor Full-Screen Overlays (Add Product / Add Customer) with Smooth Spring Slide ──
    AnimatedVisibility(
        visible = showAddProductEditor,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
        ),
        modifier = Modifier.zIndex(100f)
    ) {
        AppBackHandler { showAddProductEditor = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            TextilesProductEditorScreen(
                item = null,
                onBack = {
                    showAddProductEditor = false
                    TextilesProductRepository.loadAll()
                }
            )
        }
    }

    AnimatedVisibility(
        visible = showAddCustomerEditor,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
        ),
        modifier = Modifier.zIndex(100f)
    ) {
        AppBackHandler { showAddCustomerEditor = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            TextilesCustomerEditorScreen(
                merchant = null,
                onBack = {
                    showAddCustomerEditor = false
                    TextilesCustomerRepository.loadAll()
                }
            )
        }
    }
}

@Deprecated("Use TextilesInvoiceEditorScreen", ReplaceWith("TextilesInvoiceEditorScreen(invoice, onBack, onRequestAddNewCustomer, onRequestAddNewProduct)"))
@Composable
fun SilkInvoiceEditorScreen(
    invoice: PattiyalTharavuru? = null,
    onBack: () -> Unit,
    onRequestAddNewCustomer: () -> Unit = {},
    onRequestAddNewProduct: () -> Unit = {}
) = TextilesInvoiceEditorScreen(invoice, onBack, onRequestAddNewCustomer, onRequestAddNewProduct)
