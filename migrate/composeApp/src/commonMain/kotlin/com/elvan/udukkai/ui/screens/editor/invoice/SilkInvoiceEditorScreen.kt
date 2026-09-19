package com.elvan.udukkai.ui.screens.editor.invoice

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.MozhiJsonConverter
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.components.shell.ElvanActionButton
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import com.elvan.udukkai.ui.navigation.MaterialSymbols
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
 * Pixel-Perfect Silk (GST) Invoice Editor — matches Flutter's `SilkInvoiceEditor` 1:1.
 * Features:
 * - Bottom-up sheet selectors for Customer, Product, Invoice Type, and Company.
 * - Real-time CGST/SGST (Intra-state) vs IGST (Inter-state) GST calculation engine.
 * - Smooth 300ms animated line items addition and deletion.
 * - Unsaved changes confirmation action sheet guard.
 */
@Composable
fun SilkInvoiceEditorScreen(
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
        NiruvanaTharavugalRepository.refreshFromDatabase()
        PorulRepository.loadAll(AppMode.PATTU)
        VaangunarRepository.loadAll(AppMode.PATTU)
        PattiyalRepository.loadAll(AppMode.PATTU)
    }

    val profiles: List<NiruvanaTharavugal> = NiruvanaTharavugalRepository.getAllProfiles(AppMode.PATTU)
    var selectedNiruvanamId by remember {
        mutableStateOf(
            invoice?.niruvanamId ?: if (profiles.size == 1) profiles.first().id else null
        )
    }
    val selectedProfile = profiles.firstOrNull { it.id == selectedNiruvanamId }

    // Auto-select if only 1 profile exists
    LaunchedEffect(profiles) {
        if (profiles.size == 1 && selectedNiruvanamId == null) {
            selectedNiruvanamId = profiles.first().id
        }
    }

    val isFormLocked = profiles.size > 1 && selectedNiruvanamId == null

    fun computePrefix(p: NiruvanaTharavugal?): String {
        return if (p != null && p.kurumPeyar.isNotEmpty()) p.kurumPeyar else "INV"
    }

    val initialPrefix = computePrefix(selectedProfile)
    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.patrucheettuEn?.ifEmpty {
                PattiyalRepository.getNextInvoiceNumber(selectedNiruvanamId, initialPrefix, AppMode.PATTU)
            } ?: PattiyalRepository.getNextInvoiceNumber(selectedNiruvanamId, initialPrefix, AppMode.PATTU)
        )
    }
    var isInvoiceNumberOverridden by remember { mutableStateOf(isEditing) }

    // Customer
    var selectedVaangunarId by remember { mutableStateOf(invoice?.vaangunarId) }
    var selectedVaangunarPeyarMap by remember { mutableStateOf(invoice?.vaangunarPeyar ?: emptyMap()) }
    var selectedVaangunarMunvariMap by remember { mutableStateOf(invoice?.vaangunarMunvari ?: emptyMap()) }
    var customerState by remember { mutableStateOf("") }

    val initialViruppangal = remember(invoice) {
        PattuKanakku.viruppangalFromJson(invoice?.sonthaViruppangal)
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
                PattuKanakku.pattuListFromJson(invoice.tharavugal).ifEmpty { listOf(PattuUrupadi()) }
            } else {
                listOf(PattuUrupadi())
            }
        )
    }

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
    var isCompanySheetOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Draft persistence
    val draftKey = "udukkai_draft_silk_invoice"
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
        val itemsJson = PattuKanakku.pattuListToJson(items)
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
            val niruvanamId = Regex(""""selectedNiruvanamId"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            if (niruvanamId != null) selectedNiruvanamId = niruvanamId

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
            if (posEn != null) placeOfSupplyEn = posEn

            val posTa = Regex(""""placeOfSupplyTa"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (posTa != null) placeOfSupplyTa = posTa

            val invOverride = Regex(""""invoiceNumberOverride"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!invOverride.isNullOrEmpty()) {
                invoiceNumber = invOverride
                isInvoiceNumberOverridden = true
            }

            val discVal = Regex(""""globalDiscountValue"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (discVal != null) globalDiscountValue = discVal

            val discType = Regex(""""globalDiscountType"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (discType != null) globalDiscountType = discType

            val itemsMatch = Regex(""""items"\s*:\s*(\[.*\])""").find(json)?.groupValues?.get(1)
            val loadedItems = PattuKanakku.pattuListFromJson(itemsMatch)
            if (loadedItems.isNotEmpty()) {
                items = loadedItems
            }
            hasUnsavedChanges = true
        } catch (_: Exception) {}
    }

    LaunchedEffect(hasUnsavedChanges, selectedNiruvanamId, selectedVaangunarId, items.size, globalDiscountValue, placeOfSupplyEn) {
        if (hasUnsavedChanges && !isEditing) {
            prefs.setString(draftKey, serializeDraft())
        }
    }

    fun onCompanyChanged(newProfile: NiruvanaTharavugal?) {
        selectedNiruvanamId = newProfile?.id
        hasUnsavedChanges = true
        errorMessage = null
        if (!isEditing) {
            val prefix = computePrefix(newProfile)
            invoiceNumber = PattiyalRepository.getNextInvoiceNumber(newProfile?.id, prefix, AppMode.PATTU)
            isInvoiceNumberOverridden = false
        }
    }

    // Calculation Engine
    val businessState = selectedProfile?.maanilam?.get("en")
        ?: selectedProfile?.maanilam?.get("ta")
        ?: "Tamil Nadu"

    val totals = remember(items, globalDiscountValue, globalDiscountType, businessState, placeOfSupplyEn) {
        val discountDouble = globalDiscountValue.toDoubleOrNull() ?: 0.0
        PattuKanakku.calculate(
            items = items,
            globalDiscountValue = discountDouble,
            globalDiscountType = globalDiscountType,
            businessState = businessState,
            customerState = placeOfSupplyEn,
            country = "India"
        )
    }

    val handleBack = {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    AppBackHandler(enabled = true) {
        handleBack()
    }

    val niruvanamRequiredMsg = K.selectCompanyProfile.tr()
    val vaangunarRequiredMsg = K.selectCustomer.tr()
    val porulRequiredMsg = K.addAtLeastOneProduct.tr()
    val saveBtnLabel = K.saveBtn.tr()
    val addNewProductLabel = K.addProductBtn.tr()

    val handleSave: () -> Unit = {
        if (profiles.size > 1 && selectedNiruvanamId == null) {
            errorMessage = niruvanamRequiredMsg
        } else if (selectedVaangunarId == null && selectedVaangunarPeyarMap.isEmpty()) {
            errorMessage = vaangunarRequiredMsg
        } else {
            val validItems = items.filter { it.alavu > 0 && it.vilai > 0 }
            if (validItems.isEmpty()) {
                errorMessage = porulRequiredMsg
            } else {
                isSaving = true
                val vanakkam = PattiyalRepository.getNextVanakkam(selectedNiruvanamId, AppMode.PATTU)
                val gVal = globalDiscountValue.toDoubleOrNull() ?: 0.0
                val podhuDiscountThogai = if (globalDiscountType == "%") {
                    (totals.adippadaiMothangal - (totals.thallupadiMothangal - gVal)) * (gVal / 100.0)
                } else {
                    gVal
                }
                val viruppangal = PattuKanakku.PattuViruppangal(
                    globalDiscountValue = gVal,
                    globalDiscountType = globalDiscountType,
                    placeOfSupply = placeOfSupplyEn,
                    placeOfSupplyTa = placeOfSupplyTa
                )

                val newInvoice = PattiyalTharavuru(
                    id = invoice?.id ?: 0L,
                    niruvanamId = selectedNiruvanamId,
                    patrucheettuEn = invoiceNumber,
                    vanakkam = if (invoice != null && invoice.vanakkam > 0) invoice.vanakkam else vanakkam,
                    pattiyalVagai = pattiyalVagai,
                    vaangunarId = selectedVaangunarId,
                    vaangunarPeyar = selectedVaangunarPeyarMap,
                    vaangunarMunvari = selectedVaangunarMunvariMap,
                    pattiyalNaal = invoiceDate,
                    tharavugal = PattuKanakku.pattuListToJson(validItems),
                    mothaThogai = totals.mothaMothangal,
                    thallupadi = totals.thallupadiMothangal,
                    podhuThallupadiMathippu = gVal,
                    podhuThallupadiVagai = globalDiscountType,
                    podhuThallupadiThogai = podhuDiscountThogai,
                    variThogai = totals.variMothangal,
                    variTharavugal = PattuKanakku.variToJson(totals),
                    sonthaViruppangal = PattuKanakku.viruppangalToJson(viruppangal),
                    updatedAt = System.currentTimeMillis()
                )
                PattiyalRepository.save(newInvoice, AppMode.PATTU)
                prefs.setString(draftKey, null)
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
        hasActions = true,
        actions = {
            ElvanActionButton(
                label = saveBtnLabel,
                onClick = handleSave,
                enabled = !isSaving && !isFormLocked
            )
        }
    ) {
        CompositionLocalProvider(LocalEditorAccentColor provides colors.invoiceColor) {
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

            // ── Section 0: Business Profile Selector (if multiple profiles exist) ──
            if (profiles.size > 1) {
                item(key = "profile_section") {
                    val companyName = selectedProfile?.kurumPeyar?.ifEmpty {
                        selectedProfile.niruvanathinPeyar.values.firstOrNull().orEmpty()
                    }

                    ElvanEditorSection(index = 0, title = K.companyDetail.tr()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ElvanThiruthiThalaippu(label = K.company.tr())
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(colors.iconBg)
                                    .clickable { isCompanySheetOpen = true }
                                    .padding(start = 20.dp, end = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = (companyName ?: K.selectCompany.tr()).preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (companyName != null) colors.textPrimary else colors.textSecondary
                                        ),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (selectedNiruvanamId != null) {
                                        // Clear button (X)
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = ripple(bounded = true, radius = 16.dp)
                                                ) {
                                                    onCompanyChanged(null)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.Close,
                                                contentDescription = "Clear",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.size(32.dp),
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
                        }
                    }
                }
            }

            val baseIndex = if (profiles.size > 1) 1 else 0

            // ── Section 1: ① Billed To (பெறுநர்) ──
            item(key = "customer_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex, title = K.billedTo.tr()) {
                        PattuVaangunargalKooru(
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
                            onRequestAddNewCustomer = onRequestAddNewCustomer
                        )
                    }
                }
            }

            // ── Section 2: ② Invoice Details (பட்டியல் தரவுகள்) ──
            item(key = "invoice_details_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 1, title = K.invoiceDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            val profilePrefix = if (selectedProfile != null && selectedProfile.kurumPeyar.isNotEmpty()) {
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
                            PattuVilippiIdam(
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
                        }
                    }
                }
            }

            // ── Section 3: ③ Line Items (பொருட்கள்) ──
            item(key = "line_items_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 2, title = K.products.tr()) {
                        ElvanAsaiPattiyal {
                            items.forEachIndexed { idx, lineItem ->
                                PattuUrupadiAttai(
                                    item = lineItem,
                                    index = idx,
                                    itemCount = items.size,
                                    onItemUpdated = { updated ->
                                        items = items.toMutableList().also { it[idx] = updated }
                                        hasUnsavedChanges = true
                                        errorMessage = null
                                    },
                                    onItemDeleted = {
                                        items = items.toMutableList().also { it.removeAt(idx) }
                                        hasUnsavedChanges = true
                                    },
                                    onItemCleared = {
                                        items = items.toMutableList().also { it[idx] = PattuUrupadi() }
                                        hasUnsavedChanges = true
                                    },
                                    onDirty = { hasUnsavedChanges = true },
                                    onRequestAddNewProduct = onRequestAddNewProduct
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // "+ Add New Item" stadium pill button (matching Flutter + சேர்)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White)
                                    .clickable {
                                        items = items + PattuUrupadi()
                                        hasUnsavedChanges = true
                                    }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Add,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = K.addBtn.tr().preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
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

            // ── Section 4: ④ Totals (மொத்தங்கள்) ──
            item(key = "totals_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 3, title = K.totals.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Global Discount Row (% / ₹)
                            PattuThallupadiKooru(
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

                            // Calculated Totals Breakdown (wrapped in ElvanThiruthiAttai inside PattuMothangalKooru)
                            PattuMothangalKooru(totals = totals)
                        }
                    }
                }
            }

            // ── Section 5: ⑤ Invoice Type (பட்டியல் வகை) ──
            item(key = "invoice_type_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 4, title = K.invoiceType.tr()) {
                        PattuPattiyalVagaiKooru(
                            pattiyalVagai = pattiyalVagai,
                            onChanged = {
                                pattiyalVagai = it
                                hasUnsavedChanges = true
                            }
                        )
                    }
                }
            }
            }
        }
    }

    // ── Company Profile Selection Bottom Sheet ──
    if (isCompanySheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.company.tr(),
            items = profiles,
            currentValue = selectedProfile,
            showSearch = false,
            onDismissRequest = { isCompanySheetOpen = false },
            onSelected = { p ->
                onCompanyChanged(p)
                isCompanySheetOpen = false
            },
            itemLabelBuilder = { p ->
                p.kurumPeyar.ifEmpty { p.niruvanathinPeyar.values.firstOrNull().orEmpty() }
            },
            subtitleBuilder = { p ->
                val primary = p.kurumPeyar.ifEmpty { p.niruvanathinPeyar.values.firstOrNull().orEmpty() }
                val fullTa = p.niruvanathinPeyar["ta"] ?: ""
                val fullEn = p.niruvanathinPeyar["en"] ?: ""
                val place = p.oor["ta"] ?: p.oor["en"] ?: ""
                when {
                    fullTa.isNotEmpty() && fullTa != primary -> fullTa
                    fullEn.isNotEmpty() && fullEn != primary -> fullEn
                    place.isNotEmpty() -> place
                    else -> null
                }
            }
        )
    }

    // ── Unsaved Changes Guard Action Sheet ──
    if (showUnsavedDialog) {
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
                showUnsavedDialog = false
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
}
