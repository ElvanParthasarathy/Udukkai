package com.elvan.udukkai.textiles.ui.screens.editor

import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.core.utils.BillRules
import com.elvan.udukkai.core.print.PrintPayload
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.ui.screens.preview.DraftBillPreview
import com.elvan.udukkai.core.print.BillPdfAutoSave
import com.elvan.udukkai.core.print.invoicePdfName
import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.screens.editor.ElvanEditorBottomBar
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar



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
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.InvoiceDisplayOptions
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.data.settings.LanguageJsonConverter
import com.elvan.udukkai.data.settings.BusinessProfile
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
import com.elvan.udukkai.ui.screens.editor.ElvanEditorDisabledOverlay
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel
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
 * - Bottom-up sheet selectors for Customer, Item, Invoice Type, and Company.
 * - Real-time CGST/SGST (Intra-state) vs IGST (Inter-state) GST calculation engine.
 * - Smooth 300ms animated line items addition and deletion.
 * - Unsaved changes confirmation action sheet guard.
 */
@Composable
fun TextilesInvoiceEditorScreen(
    invoice: InvoiceModel? = null,
    onBack: () -> Unit,
    onRequestAddNewCustomer: () -> Unit = {},
    onRequestAddNewItem: () -> Unit = {}
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val isEditing = invoice != null && invoice.id > 0L
    val isPaidLocked = remember(invoice) {
        invoice != null && invoice.id > 0L && TextilesInvoiceRepository.hasLinkedReceipts(invoice.id)
    }
    val invoiceLockedMsg = K.invoiceLockedDueToReceipt.tr()

    LaunchedEffect(isPaidLocked) {
        if (isPaidLocked) {
            ElvanSnackbar.show(invoiceLockedMsg)
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        TextilesProfileRepository.refreshFromDatabase()
        TextilesItemRepository.loadAll()
        TextilesCustomerRepository.loadAll()
        TextilesInvoiceRepository.loadAll()
    }

    val profiles: List<BusinessProfile> = TextilesProfileRepository.profiles
    val activeProfile = TextilesProfileRepository.profile
    val selectedProfile = remember(profiles, activeProfile, invoice?.companyId) {
        if (isEditing) {
            profiles.firstOrNull { it.id == invoice?.companyId } ?: activeProfile
        } else {
            activeProfile
        }
    }
    val selectedCompanyId = if (isEditing) (invoice?.companyId ?: selectedProfile.id) else (selectedProfile.id ?: profiles.firstOrNull()?.id)
    val isProfileConfigured = profiles.isNotEmpty() && selectedCompanyId != null

    fun computePrefix(p: BusinessProfile?): String {
        return if (p != null && p.shortName.isNotEmpty()) p.shortName else "INV"
    }

    val initialPrefix = computePrefix(selectedProfile)
    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.invoiceNumber?.ifEmpty {
                TextilesInvoiceRepository.getNextInvoiceNumber(selectedCompanyId, initialPrefix)
            } ?: TextilesInvoiceRepository.getNextInvoiceNumber(selectedCompanyId, initialPrefix)
        )
    }
    var isInvoiceNumberOverridden by remember { mutableStateOf(isEditing) }
    var showAddItemEditor by remember { mutableStateOf(false) }
    var showAddCustomerEditor by remember { mutableStateOf(false) }

    // Customer
    var selectedCustomerId by remember { mutableStateOf(invoice?.customerId) }
    var selectedCustomerNameMap by remember { mutableStateOf(invoice?.customerName ?: emptyMap()) }
    var selectedCustomerAddressMap by remember { mutableStateOf(invoice?.customerAddress ?: emptyMap()) }
    var customerState by remember { mutableStateOf("") }
    val isCustomerSelected = (selectedCustomerId != null && selectedCustomerId!! > 0L) || selectedCustomerNameMap.isNotEmpty()

    val initialDisplayOptions = remember(invoice) {
        InvoiceDisplayOptions.fromJson(invoice?.customPreferencesJson)
    }
    var displayOptions by remember { mutableStateOf(initialDisplayOptions) }

    // Metadata
    var invoiceType by remember { mutableStateOf(invoice?.invoiceType?.ifEmpty { "tax-invoice" } ?: "tax-invoice") }
    var invoiceDate by remember { mutableStateOf(invoice?.invoiceDate ?: System.currentTimeMillis()) }
    var placeOfSupplyEn by remember {
        mutableStateOf(
            if (invoice != null && initialDisplayOptions.placeOfSupply.isNotEmpty()) initialDisplayOptions.placeOfSupply else "Tamil Nadu"
        )
    }
    var placeOfSupplyTa by remember {
        mutableStateOf(
            if (invoice != null && initialDisplayOptions.placeOfSupplyTa.isNotEmpty()) initialDisplayOptions.placeOfSupplyTa else "தமிழ்நாடு"
        )
    }

    // Line items
    var items by remember {
        mutableStateOf(
            if (invoice != null) {
                TextilesCalculation.itemsFromJson(invoice.itemsJson).ifEmpty { listOf(TextilesItem()) }
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
            if (invoice != null && invoice.globalDiscountValue > 0) {
                invoice.globalDiscountValue.toString()
            } else if (invoice != null && initialDisplayOptions.globalDiscountValue > 0) {
                initialDisplayOptions.globalDiscountValue.toString()
            } else ""
        )
    }
    var globalDiscountType by remember {
        mutableStateOf(
            invoice?.globalDiscountType?.ifEmpty { initialDisplayOptions.globalDiscountType } ?: initialDisplayOptions.globalDiscountType
        )
    }

    val initialItemsList = remember(invoice) {
        if (invoice != null) TextilesCalculation.itemsFromJson(invoice.itemsJson) else emptyList()
    }

    // Guards & states
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }

    val isFormActuallyDirty: Boolean = if (isEditing) {
        val itemsChanged = activeItems.size != initialItemsList.size ||
            activeItems.zip(initialItemsList).any { (a, b) ->
                a.itemName != b.itemName || a.quantity != b.quantity || a.rate != b.rate || a.unit != b.unit
            }
        val initialDiscount = if (invoice != null && invoice.globalDiscountValue > 0) invoice.globalDiscountValue.toString() else ""
        val initialPlaceOfSupply = if (invoice != null && initialDisplayOptions.placeOfSupply.isNotEmpty()) initialDisplayOptions.placeOfSupply else "Tamil Nadu"

        hasUnsavedChanges && (
            selectedCustomerId != invoice?.customerId ||
            invoiceNumber != (invoice?.invoiceNumber ?: "") ||
            invoiceDate != (invoice?.invoiceDate ?: 0L) ||
            invoiceType != (invoice?.invoiceType ?: "tax-invoice") ||
            globalDiscountValue != initialDiscount ||
            globalDiscountType != (invoice?.globalDiscountType ?: initialDisplayOptions.globalDiscountType) ||
            placeOfSupplyEn != initialPlaceOfSupply ||
            itemsChanged
        )
    } else {
        hasUnsavedChanges && (
            isCustomerSelected ||
            activeItems.any { it.quantity > 0.0 || it.rate > 0.0 || it.itemName.isNotBlank() } ||
            isInvoiceNumberOverridden ||
            globalDiscountValue.isNotBlank()
        )
    }

    // Draft persistence
    val draftKey = "udukkai_draft_textiles_invoice"
    val prefs = remember { getPreferencesHelper() }
    var showDraftRestoreDialog by remember { mutableStateOf(false) }
    var pendingDraftJson by remember { mutableStateOf<String?>(null) }

    fun isDraftMeaningful(json: String): Boolean {
        try {
            val custId = Regex(""""(?:selectedCustomerId|vaangunarId)"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            if (custId != null && custId > 0L) return true

            val discVal = Regex(""""globalDiscountValue"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!discVal.isNullOrBlank()) return true

            val numOverride = Regex(""""invoiceNumberOverride"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!numOverride.isNullOrBlank()) return true

            val itemsMatch = Regex(""""items"\s*:\s*(\[.*?\])(?=\s*,\s*"\w+"|\s*\})""").find(json)?.groupValues?.get(1)
            if (!itemsMatch.isNullOrEmpty()) {
                val parsedItems = TextilesCalculation.itemsFromJson(itemsMatch)
                if (parsedItems.any { it.itemName.isNotBlank() || it.quantity > 0.0 || it.rate > 0.0 }) return true
            }
        } catch (_: Exception) {}
        return false
    }

    LaunchedEffect(Unit) {
        if (!isEditing) {
            val saved = prefs.getString(draftKey)
            if (!saved.isNullOrBlank()) {
                if (isDraftMeaningful(saved)) {
                    pendingDraftJson = saved
                    showDraftRestoreDialog = true
                } else {
                    prefs.setString(draftKey, null)
                }
            }
        }
    }

    fun serializeDraft(): String {
        val itemsJson = TextilesCalculation.itemsToJson(items)
        val sb = StringBuilder("{")
        sb.append("\"selectedCompanyId\":").append(selectedCompanyId ?: "null").append(",")
        sb.append("\"selectedCustomerId\":").append(selectedCustomerId ?: "null").append(",")
        sb.append("\"customerName\":").append(LanguageJsonConverter.stringify(selectedCustomerNameMap)).append(",")
        sb.append("\"customerAddress\":").append(LanguageJsonConverter.stringify(selectedCustomerAddressMap)).append(",")
        sb.append("\"invoiceType\":\"").append(invoiceType).append("\",")
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
            val custId = Regex(""""(?:selectedCustomerId|vaangunarId)"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            if (custId != null) selectedCustomerId = custId

            val nameMatch = Regex(""""(?:customerName|vaangunarPeyar)"\s*:\s*(\{[^}]*\})""").find(json)?.groupValues?.get(1)
            if (nameMatch != null) selectedCustomerNameMap = LanguageJsonConverter.parse(nameMatch)

            val addressMatch = Regex(""""(?:customerAddress|vaangunarMunvari)"\s*:\s*(\{[^}]*\})""").find(json)?.groupValues?.get(1)
            if (addressMatch != null) selectedCustomerAddressMap = LanguageJsonConverter.parse(addressMatch)

            val typeMatch = Regex(""""(?:invoiceType|invoiceType)"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!typeMatch.isNullOrEmpty()) invoiceType = typeMatch

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
            if (isFormActuallyDirty && !isEditing && !isExiting) {
                prefs.setString(draftKey, serializeDraft())
            } else if (!isFormActuallyDirty && !isEditing) {
                prefs.setString(draftKey, null)
            }
        }
    }

    // Automatic calculation engine
    val totals = remember(activeItems, globalDiscountValue, globalDiscountType, selectedProfile.state, customerState, displayOptions.taxInclusive) {
        val discountVal = globalDiscountValue.toDoubleOrNull() ?: 0.0
        val bState = selectedProfile.state["en"] ?: selectedProfile.state.values.firstOrNull().orEmpty()
        TextilesCalculation.calculate(
            items = activeItems,
            globalDiscountValue = discountVal,
            globalDiscountType = globalDiscountType,
            businessState = bState,
            customerState = placeOfSupplyEn,
            country = selectedProfile.country["en"] ?: "India",
            taxInclusive = displayOptions.taxInclusive
        )
    }

    val handleBack: () -> Unit = {
        if (!isExiting) {
            if (isFormActuallyDirty) {
                showUnsavedDialog = true
            } else {
                isExiting = true
                if (!isEditing) prefs.setString(draftKey, null)
                onBack()
            }
        }
    }

    AppBackHandler(enabled = !isExiting && isFormActuallyDirty, onBack = handleBack)

    val companyRequiredMsg = K.selectCompanyProfile.tr()
    val customerRequiredMsg = K.selectCustomer.tr()
    val itemRequiredMsg = K.addAtLeastOneItem.tr()
    val invoiceNumberRequiredMsg = K.invoiceNumber.tr()
    val invoiceNumberFormatMsg = K.invoiceNumberFormat.tr()
    val invoiceSavedMsg = K.invoiceSaved.tr()
    val invoiceNumberDuplicateMsg = K.invoiceNumberAlreadyExists.tr()
    val saveBtnLabel = K.draft.tr()

    // The bill as the form stands now: used by Save and by the draft preview
    val buildInvoice: (Int) -> InvoiceModel = { nextVanakkam ->
        val validItems = activeItems.filter { it.quantity > 0 && it.price > 0 }
        InvoiceModel(
            id = invoice?.id ?: 0L,
            companyId = selectedCompanyId,
            finYear = DateUtils.financialYearStart(invoiceDate),
            invoiceNumber = invoiceNumber,
            vanakkam = if (invoice != null && invoice.vanakkam > 0) invoice.vanakkam else nextVanakkam,
            invoiceType = invoiceType,
            customerId = selectedCustomerId,
            customerName = selectedCustomerNameMap,
            customerAddress = selectedCustomerAddressMap,
            invoiceDate = invoiceDate,
            itemsJson = TextilesCalculation.itemsToJson(validItems),
            totalAmount = totals.grandTotal,
            discount = totals.totalDiscount,
            globalDiscountValue = globalDiscountValue.toDoubleOrNull() ?: 0.0,
            globalDiscountType = globalDiscountType,
            globalDiscountAmount = 0.0,
            taxAmount = totals.totalTax,
            taxDataJson = TextilesCalculation.taxToJson(totals),
            customPreferencesJson = displayOptions.copy(
                placeOfSupply = placeOfSupplyEn,
                placeOfSupplyTa = placeOfSupplyTa
            ).toJson(),
            updatedAt = System.currentTimeMillis()
        )
    }

    var previewPayload by remember { mutableStateOf<String?>(null) }
    val openPreview: () -> Unit = preview@{
        if (!isProfileConfigured) { ElvanSnackbar.show(companyRequiredMsg); return@preview }
        if (selectedCustomerId == null && selectedCustomerNameMap.isEmpty()) { ElvanSnackbar.show(customerRequiredMsg); return@preview }
        if (activeItems.none { it.quantity > 0 && it.price > 0 }) { ElvanSnackbar.show(itemRequiredMsg); return@preview }
        previewPayload = PrintPayload.forTextilesInvoice(
            invoice = buildInvoice(0),
            profile = selectedProfile,
            customer = selectedCustomerId?.let { TextilesCustomerRepository.getById(it) },
            appLanguage = if (LanguageManager.activeLanguageCode.startsWith("ta")) "ta" else "en"
        )
    }

    val handleSave: () -> Unit = {
        if (!isProfileConfigured) {
            ElvanSnackbar.show(companyRequiredMsg)
        } else if (selectedCustomerId == null && selectedCustomerNameMap.isEmpty()) {
            ElvanSnackbar.show(customerRequiredMsg)
        } else if (invoiceNumber.isBlank()) {
            ElvanSnackbar.show(invoiceNumberRequiredMsg)
        } else if (!BillRules.isValidInvoiceNumber(invoiceNumber)) {
            ElvanSnackbar.show(invoiceNumberFormatMsg)
        } else if (TextilesInvoiceRepository.isInvoiceNumberDuplicate(invoiceNumber, selectedCompanyId, invoice?.id)) {
            val dupMsg = "$invoiceNumber - $invoiceNumberDuplicateMsg"
            ElvanSnackbar.show(dupMsg)
        } else if (isPaidLocked) {
            ElvanSnackbar.show(invoiceLockedMsg)
        } else {
            val validItems = activeItems.filter { it.quantity > 0 && it.price > 0 }
            if (validItems.isEmpty()) {
                ElvanSnackbar.show(itemRequiredMsg)
            } else {
                isSaving = true
                val nextVanakkam = TextilesInvoiceRepository.getNextVanakkam(selectedCompanyId)
                val newInvoice = buildInvoice(nextVanakkam)
                val savedId = TextilesInvoiceRepository.save(newInvoice)
                BillPdfAutoSave.request(BillPdfAutoSave.Kind.Invoice, savedId, invoice?.takeIf { it.id > 0L }?.let(::invoicePdfName), invoice?.takeIf { it.id > 0L }?.companyId)
                prefs.setString(draftKey, null)
                isExiting = true
                hasUnsavedChanges = false
                isSaving = false
                if (savedId > 0) {
                    ElvanSnackbar.show(invoiceSavedMsg)
                }
                onBack()
            }
        }
    }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.createRecord.tr()
    val scrollState = rememberLazyListState()

    ElvanSubShell(
        title = pageTitle,
        overlay = {
            DraftBillPreview(previewPayload, K.previewBtn.tr(), (if (invoiceType == "proforma") "EST_" else "INV_") + invoiceNumber.replace('/', '-')) { previewPayload = null }
        },
        onBack = handleBack,
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        floatingBar = {
            ElvanEditorBottomBar(
                onPreview = openPreview,
                label = saveBtnLabel,
                onClick = handleSave,
                enabled = isCustomerSelected && !isSaving && !isPaidLocked,
                isLoading = isSaving,
                accentColor = colors.invoiceColor,
                showSave = !isPaidLocked
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

                // ── Paid Locked Banner ──
                if (isPaidLocked) {
                    item(key = "paid_locked_banner") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.accent.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Info,
                                    contentDescription = "Info",
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = K.invoiceLockedDueToReceipt.tr().preventBrokenLigatures(),
                                    color = colors.textPrimary,
                                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                )
                            }
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
                            selectedCustomerId = selectedCustomerId,
                            onCustomerSelected = { customer ->
                                selectedCustomerId = customer.id
                                selectedCustomerNameMap = customer.name
                                selectedCustomerAddressMap = customer.city
                                val sEn = customer.state["en"].orEmpty()
                                val sTa = customer.state["ta"] ?: customer.state.values.firstOrNull().orEmpty()
                                customerState = sEn
                                if (sEn.isNotEmpty()) {
                                    placeOfSupplyEn = sEn
                                    placeOfSupplyTa = sTa
                                }
                                hasUnsavedChanges = true
                            },
                            onCustomerCleared = {
                                selectedCustomerId = null
                                selectedCustomerNameMap = emptyMap()
                                selectedCustomerAddressMap = emptyMap()
                                customerState = ""
                                hasUnsavedChanges = true
                            },
                            onRequestAddNewCustomer = { showAddCustomerEditor = true }
                        )
                    }
                }

                // ── Section 1: ② Invoice Details (பட்டியல் தரவுகள்) ──
                item(key = "invoice_details_section") {
                    ElvanEditorDisabledOverlay(disabled = !isCustomerSelected) {
                        ElvanEditorSection(index = 1, title = K.invoiceDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            val profilePrefix = if (selectedProfile.shortName.isNotEmpty()) {
                                "${selectedProfile.shortName}-"
                            } else {
                                "INV-"
                            }
                            val currentDocPrefix = if (invoiceType == "proforma") "EST-" else profilePrefix

                            // Invoice Number with pencil edit pill
                            ElvanDocumentNumberField(
                                label = if (invoiceType == "proforma") K.proforma.tr() else K.invoiceNumber.tr(),
                                prefix = currentDocPrefix,
                                initialFullNumber = invoiceNumber,
                                onFullNumberChanged = {
                                    invoiceNumber = it
                                    isInvoiceNumberOverridden = true
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true },
                                isReadOnly = isPaidLocked,
                                readOnlyMessage = invoiceLockedMsg
                            )

                            // Date Picker Pill
                            InvoiceDateField(
                                label = K.date.tr(),
                                selectedDate = invoiceDate,
                                onDateChanged = {
                                    invoiceDate = it
                                    hasUnsavedChanges = true
                                },
                                isReadOnly = isPaidLocked,
                                readOnlyMessage = invoiceLockedMsg
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
                                },
                                isBilingual = selectedProfile.isBilingual,
                                primaryLang = selectedProfile.primaryLanguage.ifEmpty { "ta" },
                                secondaryLang = selectedProfile.secondaryLanguage.ifEmpty { "en" }
                            )

                            // Invoice Type Selector (Tax Invoice / Proforma)
                            InvoiceTypeField(
                                invoiceType = invoiceType,
                                onChanged = { newType ->
                                    invoiceType = newType
                                    val pfx = if (newType == "proforma") "EST" else (selectedProfile.shortName.ifEmpty { "INV" })
                                    if (!isInvoiceNumberOverridden) {
                                        invoiceNumber = TextilesInvoiceRepository.getNextInvoiceNumber(selectedCompanyId, pfx)
                                    }
                                    hasUnsavedChanges = true
                                }
                            )
                        }
                    }
                    }
                }

                // ── Section 2: ③ Items (பொருட்கள்) ──
                item(key = "items_section") {
                    ElvanEditorDisabledOverlay(disabled = !isCustomerSelected) {
                        ElvanEditorSection(index = 2, title = K.items.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ElvanAnimatedList {
                                items.forEachIndexed { idx, itm ->
                                    key(itm.id) {
                                        ElvanAnimatedCard(
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
                                                companyId = selectedCompanyId,
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
                                                onRequestAddNewItem = { showAddItemEditor = true }
                                            )
                                        }
                                    }
                                }
                            }

                            // Add Item Button
                            if (!isPaidLocked) {
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
                                        text = K.addItemBtn.tr().preventBrokenLigatures(),
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

                // ── Section 3: ④ Discount (தள்ளுபடி) ──
                item(key = "discount_section") {
                    ElvanEditorDisabledOverlay(disabled = !isCustomerSelected) {
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
                }

                // ── Section 4: ⑤ Totals (மொத்தங்கள்) ──
                item(key = "totals_section") {
                    ElvanEditorDisabledOverlay(disabled = !isCustomerSelected) {
                        ElvanEditorSection(index = 4, title = K.totals.tr()) {
                            TextilesTotalsField(totals = totals)
                        }
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

    // ── In-Editor Full-Screen Overlays (Add Item / Add Customer) with Smooth Spring Slide ──
    AnimatedVisibility(
        visible = showAddItemEditor,
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
        AppBackHandler { showAddItemEditor = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            TextilesItemEditorScreen(
                item = null,
                onBack = {
                    showAddItemEditor = false
                    TextilesItemRepository.loadAll()
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

