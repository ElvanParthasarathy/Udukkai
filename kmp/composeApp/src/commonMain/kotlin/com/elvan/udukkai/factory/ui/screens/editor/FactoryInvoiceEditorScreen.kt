package com.elvan.udukkai.factory.ui.screens.editor

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
import com.elvan.udukkai.ui.screens.editor.invoice.components.*
import com.elvan.udukkai.factory.data.FactoryInvoiceRepository
import com.elvan.udukkai.factory.data.FactoryItemRepository
import com.elvan.udukkai.factory.data.FactoryCustomerRepository
import com.elvan.udukkai.data.settings.LanguageJsonConverter
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.PrintLanguageManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor
import androidx.compose.ui.zIndex
import com.elvan.udukkai.ui.screens.editor.invoice.components.*

/**
 * Form Lock container matching Flutter's `Opacity(0.4) + IgnorePointer`.
 * Dims and locks downstream form sections until a business profile is selected.
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
 * Pixel-Perfect Factory (ஆலை) Invoice Editor — matches Flutter's `CoolieInvoiceEditor` 1:1.
 * Features:
 * - Dynamic Section 0: Business profile selection bottom sheet when multiple profiles exist.
 * - Form Lock: Dims and locks downstream sections until a profile is chosen.
 * - Auto-Selection: Omit Section 0 when only 1 profile exists.
 * - Section 1: Customer picker with saved details card and clear button (Dedicated FactoryCustomerField).
 * - Section 2: Document number pill with dynamic prefix and pencil edit switcher, date picker pill.
 * - Section 3: Weight-based line items with floor(kg * rate) truncation and dynamic other charges.
 * - Section 4: Setharam grams, Ahimsa silk, Courier charges, and breakdown totals card.
 * - Unsaved changes confirmation action sheet guard.
 */
@Composable
fun FactoryInvoiceEditorScreen(
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
        invoice != null && invoice.id > 0L && FactoryInvoiceRepository.hasLinkedReceipts(invoice.id)
    }
    val invoiceLockedMsg = K.invoiceLockedDueToReceipt.tr()

    LaunchedEffect(isPaidLocked) {
        if (isPaidLocked) {
            ElvanSnackbar.show(invoiceLockedMsg)
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        FactoryProfileRepository.refreshFromDatabase()
        FactoryItemRepository.loadAll()
        FactoryCustomerRepository.loadAll()
        FactoryInvoiceRepository.loadAll()
    }

    val profiles: List<BusinessProfile> = FactoryProfileRepository.profiles
    val activeProfile = FactoryProfileRepository.profile
    val selectedProfile = remember(profiles, activeProfile, invoice?.companyId) {
        if (isEditing) {
            profiles.firstOrNull { it.id == invoice?.companyId } ?: activeProfile
        } else {
            activeProfile
        }
    }
    val selectedCompanyId = if (isEditing) (invoice?.companyId ?: selectedProfile.id) else (selectedProfile.id ?: profiles.firstOrNull()?.id)
    val isProfileConfigured = profiles.isNotEmpty() && selectedCompanyId != null
    val isBilingual = selectedProfile.isBilingual
    val primaryLang = selectedProfile.primaryLanguage.ifEmpty { "ta" }
    val secondaryLang = selectedProfile.secondaryLanguage.ifEmpty { "en" }

    fun computePrefix(p: BusinessProfile?): String {
        return if (p != null && p.shortName.isNotEmpty()) p.shortName else "CB"
    }

    val initialPrefix = computePrefix(selectedProfile)
    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.invoiceNumber?.ifEmpty {
                FactoryInvoiceRepository.getNextInvoiceNumber(selectedCompanyId, initialPrefix)
            } ?: FactoryInvoiceRepository.getNextInvoiceNumber(selectedCompanyId, initialPrefix)
        )
    }
    var isInvoiceNumberOverridden by remember { mutableStateOf(isEditing) }
    var showAddItemEditor by remember { mutableStateOf(false) }
    var showAddCustomerEditor by remember { mutableStateOf(false) }

    // Customer
    var selectedCustomerId by remember { mutableStateOf(invoice?.customerId) }
    var selectedCustomerNameMap by remember { mutableStateOf(invoice?.customerName ?: emptyMap()) }
    var selectedCustomerAddressMap by remember { mutableStateOf(invoice?.customerAddress ?: emptyMap()) }

    // Invoice Type & Options
    var invoiceType by remember { mutableStateOf(invoice?.invoiceType?.ifEmpty { "tax-invoice" } ?: "tax-invoice") }
    val initialDisplayOptions = remember(invoice) {
        InvoiceDisplayOptions.fromJson(invoice?.customPreferencesJson)
    }
    var displayOptions by remember { mutableStateOf(initialDisplayOptions) }
    var selectedAccountId by remember {
        mutableStateOf(displayOptions.selectedAccountId ?: selectedProfile.getDefaultPaymentAccount()?.id)
    }

    // Date
    var invoiceDate by remember { mutableStateOf(invoice?.invoiceDate ?: System.currentTimeMillis()) }

    // Line Items
    var items by remember {
        mutableStateOf(
            if (invoice != null) {
                FactoryCalculation.itemsFromJson(invoice.itemsJson).ifEmpty { listOf(FactoryItem()) }
            } else {
                listOf(FactoryItem())
            }
        )
    }

    LaunchedEffect(FactoryItemRepository.items) {
        if (FactoryItemRepository.items.isNotEmpty()) {
            items = items.map { item ->
                if (item.languageMap.isEmpty()) {
                    val matched = FactoryItemRepository.items.firstOrNull { it.id.toString() == item.itemId }
                        ?: FactoryItemRepository.items.firstOrNull { it.name.values.any { v -> v.equals(item.itemName, ignoreCase = true) || v.equals(item.itemNameEn, ignoreCase = true) } }
                    if (matched != null && matched.name.isNotEmpty()) {
                        item.copy(
                            itemId = matched.id.toString(),
                            itemName = matched.name["ta"] ?: item.itemName,
                            itemNameEn = matched.name["en"] ?: item.itemNameEn,
                            languageMap = matched.name
                        )
                    } else if (item.itemName.isNotEmpty() || item.itemNameEn.isNotEmpty()) {
                        item.copy(
                            languageMap = mapOf("ta" to item.itemName, "en" to item.itemNameEn).filterValues { it.isNotEmpty() }
                        )
                    } else {
                        item
                    }
                } else {
                    item
                }
            }
        }
    }

    LaunchedEffect(FactoryCustomerRepository.merchants) {
        if (selectedCustomerId == null && selectedCustomerNameMap.isNotEmpty() && FactoryCustomerRepository.merchants.isNotEmpty()) {
            val matched = FactoryCustomerRepository.merchants.firstOrNull { m ->
                m.name.values.any { v -> selectedCustomerNameMap.values.contains(v) }
            }
            if (matched != null) {
                selectedCustomerId = matched.id
                selectedCustomerNameMap = matched.name
                selectedCustomerAddressMap = matched.address
            }
        }
    }

    val initialItemIds = remember { items.map { it.id }.toSet() }
    var deletingItemIds by remember { mutableStateOf(setOf<String>()) }
    val activeItems = remember(items, deletingItemIds) { items.filter { it.id !in deletingItemIds } }

    // Additional Charges
    var setharamGrams by remember { mutableStateOf(invoice?.wastageGrams ?: 0.0) }
    var thabaalThogai by remember { mutableStateOf(invoice?.postalCharges ?: 0.0) }
    var ahimsaPattuThogai by remember { mutableStateOf(invoice?.ahimsaSilkAmount ?: 0.0) }
    var additionalCharges by remember {
        mutableStateOf(
            if (invoice != null) {
                FactoryCalculation.chargesFromJson(invoice.otherTaxesJson)
            } else {
                emptyList()
            }
        )
    }
    val initialChargeIds = remember { additionalCharges.map { it.id }.toSet() }
    var deletingChargeIds by remember { mutableStateOf(setOf<String>()) }
    val activeCharges = remember(additionalCharges, deletingChargeIds) { additionalCharges.filter { it.id !in deletingChargeIds } }

    // Real-time calculated totals
    val totals = remember(activeItems, setharamGrams, thabaalThogai, ahimsaPattuThogai, activeCharges) {
        FactoryCalculation.calculate(
            items = activeItems,
            setharamGrams = setharamGrams,
            thabaalThogai = thabaalThogai,
            ahimsaPattuThogai = ahimsaPattuThogai,
            charges = activeCharges
        )
    }

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showDraftRestoreDialog by remember { mutableStateOf(false) }
    var pendingDraftJson by remember { mutableStateOf<String?>(null) }
    var isExiting by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Local draft management
    val prefs = remember { getPreferencesHelper() }
    val draftKey = "draft_factory_invoice_${invoice?.id ?: 0L}"

    fun saveDraftLocally() {
        if (!hasUnsavedChanges || isEditing) return
        val draftJson = StringBuilder("{")
            .append("\"invoiceNumber\":\"$invoiceNumber\",")
            .append("\"invoiceDate\":$invoiceDate,")
            .append("\"customerId\":${selectedCustomerId ?: "null"},")
            .append("\"items\":${FactoryCalculation.itemsToJson(items)},")
            .append("\"setharam\":$setharamGrams,")
            .append("\"courier\":$thabaalThogai,")
            .append("\"ahimsa\":$ahimsaPattuThogai,")
            .append("\"other\":${FactoryCalculation.chargesToJson(additionalCharges)}")
            .append("}")
            .toString()
        prefs.setString(draftKey, draftJson)
    }

    fun restoreDraft(json: String) {
        try {
            val num = Regex(""""invoiceNumber"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            val date = Regex(""""invoiceDate"\s*:\s*([0-9]+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            val cId = Regex(""""customerId"\s*:\s*([0-9]+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            val itemsJson = Regex(""""items"\s*:\s*(\[.*?\])""").find(json)?.groupValues?.get(1)
            val setharam = Regex(""""setharam"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull()
            val courier = Regex(""""courier"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull()
            val ahimsa = Regex(""""ahimsa"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull()
            val otherJson = Regex(""""other"\s*:\s*(\[.*?\])""").find(json)?.groupValues?.get(1)

            if (!num.isNullOrEmpty()) invoiceNumber = num
            if (date != null && date > 0) invoiceDate = date
            if (cId != null) {
                selectedCustomerId = cId
                val c = FactoryCustomerRepository.getById(cId)
                if (c != null) {
                    selectedCustomerNameMap = c.name
                    selectedCustomerAddressMap = c.address
                }
            }
            if (!itemsJson.isNullOrEmpty()) {
                val parsed = FactoryCalculation.itemsFromJson(itemsJson)
                if (parsed.isNotEmpty()) items = parsed
            }
            if (setharam != null) setharamGrams = setharam
            if (courier != null) thabaalThogai = courier
            if (ahimsa != null) ahimsaPattuThogai = ahimsa
            if (!otherJson.isNullOrEmpty()) {
                val parsedCharges = FactoryCalculation.chargesFromJson(otherJson)
                if (parsedCharges.isNotEmpty()) additionalCharges = parsedCharges
            }
            hasUnsavedChanges = true
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) {
        if (!isEditing) {
            val savedDraft = prefs.getString(draftKey, null)
            if (!savedDraft.isNullOrBlank()) {
                pendingDraftJson = savedDraft
                showDraftRestoreDialog = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (hasUnsavedChanges && !isEditing && !isExiting) {
                saveDraftLocally()
            }
        }
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

    val companyRequiredMsg = K.selectCompanyProfile.tr()
    val customerRequiredMsg = K.selectCustomer.tr()
    val itemRequiredMsg = K.addAtLeastOneItem.tr()
    val invoiceNumberRequiredMsg = K.invoiceNumber.tr()
    val invoiceSavedMsg = K.invoiceSaved.tr()
    val invoiceNumberDuplicateMsg = K.invoiceNumberAlreadyExists.tr()
    val saveBtnLabel = K.draft.tr()

    val handleSave: () -> Unit = {
        if (!isProfileConfigured) {
            ElvanSnackbar.show(companyRequiredMsg)
        } else if (selectedCustomerId == null && selectedCustomerNameMap.isEmpty()) {
            ElvanSnackbar.show(customerRequiredMsg)
        } else if (invoiceNumber.isBlank()) {
            ElvanSnackbar.show(invoiceNumberRequiredMsg)
        } else if (FactoryInvoiceRepository.isInvoiceNumberDuplicate(invoiceNumber, selectedCompanyId, invoice?.id)) {
            val dupMsg = "$invoiceNumber - $invoiceNumberDuplicateMsg"
            ElvanSnackbar.show(dupMsg)
        } else if (isPaidLocked) {
            ElvanSnackbar.show(invoiceLockedMsg)
        } else {
            val validItems = activeItems.filter { it.weight > 0 && it.price > 0 }
            if (validItems.isEmpty()) {
                ElvanSnackbar.show(itemRequiredMsg)
            } else {
                isSaving = true
                val vanakkam = FactoryInvoiceRepository.getNextVanakkam(selectedCompanyId)
                val selectedAcc = selectedProfile.getPaymentAccounts().find { it.id == selectedAccountId }
                    ?: selectedProfile.getDefaultPaymentAccount()
                val newInvoice = InvoiceModel(
                    id = invoice?.id ?: 0L,
                    companyId = selectedCompanyId,
                    invoiceNumber = invoiceNumber,
                    vanakkam = if (invoice != null && invoice.vanakkam > 0) invoice.vanakkam else vanakkam,
                    invoiceType = invoiceType,
                    customerId = selectedCustomerId,
                    customerName = selectedCustomerNameMap,
                    customerAddress = selectedCustomerAddressMap,
                    invoiceDate = invoiceDate,
                    itemsJson = FactoryCalculation.itemsToJson(validItems),
                    totalAmount = totals.grandTotal,
                    totalWeight = totals.totalWeight,
                    wastageGrams = setharamGrams,
                    postalCharges = thabaalThogai,
                    ahimsaSilkAmount = ahimsaPattuThogai,
                    otherTaxesJson = FactoryCalculation.chargesToJson(additionalCharges),
                    customPreferencesJson = displayOptions.copy(selectedAccountId = selectedAccountId).toJson(),
                    bankDetailsJson = selectedAcc?.toJson() ?: "{}",
                    updatedAt = System.currentTimeMillis()
                )
                val savedId = FactoryInvoiceRepository.save(newInvoice)
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
        onBack = handleBack,
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        floatingBar = {
            ElvanEditorBottomBar(
                label = saveBtnLabel,
                onClick = handleSave,
                enabled = !isSaving && !isPaidLocked,
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
                                    imageVector = MaterialSymbols.Rounded.Lock,
                                    contentDescription = "Locked",
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

                // ── Section 0: Customer (பெறுநர்) ──
                item(key = "customer_section") {
                    ElvanEditorSection(index = 0, title = K.billedTo.tr()) {
                        FactoryCustomerField(
                            selectedCustomerId = selectedCustomerId,
                            onCustomerSelected = { customer ->
                                selectedCustomerId = customer.id
                                selectedCustomerNameMap = customer.name
                                selectedCustomerAddressMap = customer.address
                                hasUnsavedChanges = true
                            },
                            onCustomerCleared = {
                                selectedCustomerId = null
                                selectedCustomerNameMap = emptyMap()
                                selectedCustomerAddressMap = emptyMap()
                                hasUnsavedChanges = true
                            },
                            onRequestAddNewCustomer = { showAddCustomerEditor = true }
                        )
                    }
                }

                // ── Section 1: Invoice Details (பட்டியல் தரவுகள்) ──
                item(key = "invoice_details_section") {
                    ElvanEditorSection(index = 1, title = K.invoiceDetails.tr()) {
                        val currentPrefix = computePrefix(selectedProfile)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ElvanDocumentNumberField(
                                label = K.invoiceNumber.tr(),
                                prefix = "$currentPrefix-",
                                initialFullNumber = invoiceNumber,
                                onFullNumberChanged = { newNum ->
                                    invoiceNumber = newNum
                                    isInvoiceNumberOverridden = true
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true },
                                isReadOnly = isPaidLocked,
                                readOnlyMessage = invoiceLockedMsg
                            )

                            InvoiceDateField(
                                label = K.date.tr(),
                                selectedDate = invoiceDate,
                                onDateChanged = { newDate ->
                                    invoiceDate = newDate
                                    hasUnsavedChanges = true
                                },
                                isReadOnly = isPaidLocked,
                                readOnlyMessage = invoiceLockedMsg
                            )
                        }
                    }
                }

                // ── Section 2: Items (பொருட்கள்) ──
                item(key = "items_section") {
                    ElvanEditorSection(index = 2, title = K.items.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // ── Item Line Items ──
                            ElvanAnimatedList {
                                items.forEachIndexed { idx, itm ->
                                    key(itm.id) {
                                        ElvanAnimatedCard(
                                            key = itm.id,
                                            isInitial = itm.id in initialItemIds,
                                            onDeleted = {
                                                deletingItemIds = deletingItemIds - itm.id
                                                items = items.filter { it.id != itm.id }
                                                hasUnsavedChanges = true
                                            }
                                        ) { requestDelete ->
                                            FactoryItemCard(
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
                                                        deletingItemIds = deletingItemIds + itm.id
                                                        hasUnsavedChanges = true
                                                        requestDelete()
                                                    }
                                                },
                                                onItemCleared = {
                                                    items = items.map { if (it.id == itm.id) FactoryItem(id = itm.id) else it }
                                                    hasUnsavedChanges = true
                                                },
                                                onDirty = { hasUnsavedChanges = true },
                                                onRequestAddNewItem = { showAddItemEditor = true },
                                                onAddNewItem = null,
                                                onAddNewCharge = null
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Add Item & Add Charge Buttons ──
                            if (!isPaidLocked) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(colors.iconBg)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = colors.accent),
                                            onClick = {
                                                items = items + FactoryItem()
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

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(colors.iconBg)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(color = colors.accent),
                                            onClick = {
                                                additionalCharges = additionalCharges + FactoryAdditionalCharge()
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
                                            text = K.addOtherCharges.tr().preventBrokenLigatures(),
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

                            // ── Other Charges Animated Cards (BELOW BUTTONS) ──
                            ElvanAnimatedList {
                                    additionalCharges.forEachIndexed { cIdx, charge ->
                                        key(charge.id) {
                                            ElvanAnimatedCard(
                                                key = charge.id,
                                                isInitial = charge.id in initialChargeIds,
                                                onDeleted = {
                                                    deletingChargeIds = deletingChargeIds - charge.id
                                                    additionalCharges = additionalCharges.filter { it.id != charge.id }
                                                    hasUnsavedChanges = true
                                                }
                                            ) { requestDelete ->
                                                FactoryAdditionalChargeCard(
                                                    charge = charge,
                                                    index = cIdx,
                                                    isBilingual = isBilingual,
                                                    primaryLang = primaryLang,
                                                    secondaryLang = secondaryLang,
                                                    onUpdated = { updated ->
                                                        additionalCharges = additionalCharges.map { if (it.id == charge.id) updated else it }
                                                        hasUnsavedChanges = true
                                                    },
                                                    onDeleted = {
                                                        deletingChargeIds = deletingChargeIds + charge.id
                                                        hasUnsavedChanges = true
                                                        requestDelete()
                                                    },
                                                    onDirty = { hasUnsavedChanges = true }
                                                )
                                            }
                                        }
                                    }
                                }

                        }
                    }
                }

                // ── Section 3: Totals & Extra Charges (மொத்தங்கள்) ──
                item(key = "totals_section") {
                    ElvanEditorSection(index = 3, title = K.totals.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FactoryExtraChargesSection(
                                setharamGrams = setharamGrams,
                                ahimsaPattuThogai = ahimsaPattuThogai,
                                thabaalThogai = thabaalThogai,
                                onSetharamChanged = {
                                    setharamGrams = it
                                    hasUnsavedChanges = true
                                },
                                onAhimsaChanged = {
                                    ahimsaPattuThogai = it
                                    hasUnsavedChanges = true
                                },
                                onThabaalChanged = {
                                    thabaalThogai = it
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true }
                            )

                            FactoryTotalsSection(
                                totals = totals,
                                setharamGrams = setharamGrams,
                                ahimsaPattuThogai = ahimsaPattuThogai,
                                thabaalThogai = thabaalThogai,
                                charges = activeCharges
                            )
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
            FactoryItemEditorScreen(
                item = null,
                onBack = {
                    showAddItemEditor = false
                    FactoryItemRepository.loadAll()
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
            FactoryCustomerEditorScreen(
                merchant = null,
                onBack = {
                    showAddCustomerEditor = false
                    FactoryCustomerRepository.loadAll()
                }
            )
        }
    }
}

@Deprecated("Use FactoryInvoiceEditorScreen", ReplaceWith("FactoryInvoiceEditorScreen(invoice, onBack, onRequestAddNewCustomer, onRequestAddNewItem)"))
@Composable
fun CoolieInvoiceEditorScreen(
    invoice: InvoiceModel? = null,
    onBack: () -> Unit,
    onRequestAddNewCustomer: () -> Unit = {},
    onRequestAddNewItem: () -> Unit = {}
) = FactoryInvoiceEditorScreen(invoice, onBack, onRequestAddNewCustomer, onRequestAddNewItem)
