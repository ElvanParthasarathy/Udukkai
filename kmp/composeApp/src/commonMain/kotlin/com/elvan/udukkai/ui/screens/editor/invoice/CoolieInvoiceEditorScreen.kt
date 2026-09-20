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
import com.elvan.udukkai.localization.PrintLanguageManager
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
import androidx.compose.ui.zIndex
import com.elvan.udukkai.ui.screens.editor.customer.CustomerEditorScreen
import com.elvan.udukkai.ui.screens.editor.product.ProductEditorScreen
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
 * Pixel-Perfect Coolie Invoice Editor — matches Flutter's `CoolieInvoiceEditor` 1:1.
 * Features:
 * - Dynamic Section 0: Business profile selection bottom sheet when multiple profiles exist.
 * - Form Lock: Dims and locks downstream sections until a profile is chosen.
 * - Auto-Selection: Omit Section 0 when only 1 profile exists.
 * - Section 1: Customer picker with saved details card and clear button.
 * - Section 2: Document number pill with dynamic prefix and pencil edit switcher, date picker pill.
 * - Section 3: Weight-based line items with floor(kg * rate) truncation and dynamic other charges.
 * - Section 4: Setharam grams, Ahimsa silk, Courier charges, and breakdown totals card.
 * - Unsaved changes confirmation action sheet guard.
 */
@Composable
fun CoolieInvoiceEditorScreen(
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
        PorulRepository.loadAll(AppMode.KOOLI)
        VaangunarRepository.loadAll(AppMode.KOOLI)
        PattiyalRepository.loadAll(AppMode.KOOLI)
    }

    val profiles: List<NiruvanaTharavugal> = NiruvanaTharavugalRepository.getAllProfiles(AppMode.KOOLI)
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
        return if (p != null && p.kurumPeyar.isNotEmpty()) p.kurumPeyar else "CB"
    }

    val initialPrefix = computePrefix(selectedProfile)
    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.patrucheettuEn?.ifEmpty {
                PattiyalRepository.getNextInvoiceNumber(selectedNiruvanamId, initialPrefix, AppMode.KOOLI)
            } ?: PattiyalRepository.getNextInvoiceNumber(selectedNiruvanamId, initialPrefix, AppMode.KOOLI)
        )
    }
    var isInvoiceNumberOverridden by remember { mutableStateOf(isEditing) }
    var showAddProductEditor by remember { mutableStateOf(false) }
    var showAddCustomerEditor by remember { mutableStateOf(false) }

    // Customer
    var selectedVaangunarId by remember { mutableStateOf(invoice?.vaangunarId) }
    var selectedVaangunarPeyarMap by remember { mutableStateOf(invoice?.vaangunarPeyar ?: emptyMap()) }
    var selectedVaangunarMunvariMap by remember { mutableStateOf(invoice?.vaangunarMunvari ?: emptyMap()) }

    // Date
    var invoiceDate by remember { mutableStateOf(invoice?.pattiyalNaal ?: System.currentTimeMillis()) }

    // Line Items
    var items by remember {
        mutableStateOf(
            if (invoice != null) {
                KooliKanakku.kooliListFromJson(invoice.tharavugal).ifEmpty { listOf(KooliUrupadi()) }
            } else {
                listOf(KooliUrupadi())
            }
        )
    }
    val billingConfig = PrintLanguageManager.getConfig(AppMode.KOOLI)
    val primaryLang = billingConfig.primaryLanguage.code
    val secondaryLang = billingConfig.secondaryLanguage.code

    LaunchedEffect(PorulRepository.items) {
        if (PorulRepository.items.isNotEmpty()) {
            items = items.map { item ->
                if (item.mozhiMap.isEmpty()) {
                    val matched = PorulRepository.items.firstOrNull { it.id.toString() == item.porulId }
                        ?: PorulRepository.items.firstOrNull { it.porulPeyar.values.any { v -> v.equals(item.porulPeyar, ignoreCase = true) || v.equals(item.porulPeyarEn, ignoreCase = true) } }
                    if (matched != null && matched.porulPeyar.isNotEmpty()) {
                        item.copy(
                            porulId = matched.id.toString(),
                            porulPeyar = matched.porulPeyar["ta"] ?: item.porulPeyar,
                            porulPeyarEn = matched.porulPeyar["en"] ?: item.porulPeyarEn,
                            mozhiMap = matched.porulPeyar
                        )
                    } else if (item.porulPeyar.isNotEmpty() || item.porulPeyarEn.isNotEmpty()) {
                        item.copy(
                            mozhiMap = mapOf("ta" to item.porulPeyar, "en" to item.porulPeyarEn).filterValues { it.isNotEmpty() }
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

    LaunchedEffect(VaangunarRepository.merchants) {
        if (selectedVaangunarId == null && selectedVaangunarPeyarMap.isNotEmpty() && VaangunarRepository.merchants.isNotEmpty()) {
            val matched = VaangunarRepository.merchants.firstOrNull { m ->
                m.peyar.values.any { v -> selectedVaangunarPeyarMap.values.contains(v) }
            }
            if (matched != null) {
                selectedVaangunarId = matched.id
                selectedVaangunarPeyarMap = matched.peyar
                selectedVaangunarMunvariMap = matched.mugavari
            }
        }
    }

    val initialItemIds = remember { items.map { it.id }.toSet() }
    var deletingItemIds by remember { mutableStateOf(setOf<String>()) }
    val activeItems = remember(items, deletingItemIds) { items.filter { it.id !in deletingItemIds } }

    // Additional Charges
    var setharamGrams by remember { mutableStateOf(invoice?.setharamGrams ?: 0.0) }
    var thabaalThogai by remember { mutableStateOf(invoice?.thabaalThogai ?: 0.0) }
    var ahimsaPattuThogai by remember { mutableStateOf(invoice?.ahimsaPattuThogai ?: 0.0) }
    var piraVarivugal by remember {
        mutableStateOf(
            if (invoice != null) {
                KooliKanakku.piraVarivuListFromJson(invoice.piravariVugal)
            } else {
                emptyList()
            }
        )
    }
    val initialChargeIds = remember { piraVarivugal.map { it.id }.toSet() }
    var deletingChargeIds by remember { mutableStateOf(setOf<String>()) }
    val activeCharges = remember(piraVarivugal, deletingChargeIds) { piraVarivugal.filter { it.id !in deletingChargeIds } }

    // Real-time calculated totals
    val totals = remember(activeItems, setharamGrams, thabaalThogai, ahimsaPattuThogai, activeCharges) {
        KooliKanakku.calculate(
            items = activeItems,
            setharamGrams = setharamGrams,
            thabaalThogai = thabaalThogai,
            ahimsaPattuThogai = ahimsaPattuThogai,
            piraVarivugal = activeCharges
        )
    }

    // UI state
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isCompanySheetOpen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Draft persistence
    val draftKey = "udukkai_draft_coolie_invoice"
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
        val itemsJson = KooliKanakku.kooliListToJson(items)
        val piraJson = KooliKanakku.piraVarivuListToJson(piraVarivugal)
        val sb = StringBuilder("{")
        sb.append("\"selectedNiruvanamId\":").append(selectedNiruvanamId ?: "null").append(",")
        sb.append("\"selectedVaangunarId\":").append(selectedVaangunarId ?: "null").append(",")
        sb.append("\"vaangunarPeyar\":").append(MozhiJsonConverter.stringify(selectedVaangunarPeyarMap)).append(",")
        sb.append("\"vaangunarMunvari\":").append(MozhiJsonConverter.stringify(selectedVaangunarMunvariMap)).append(",")
        sb.append("\"invoiceDate\":").append(invoiceDate).append(",")
        sb.append("\"invoiceNumberOverride\":\"").append(if (isInvoiceNumberOverridden) invoiceNumber else "").append("\",")
        sb.append("\"setharamGrams\":").append(setharamGrams).append(",")
        sb.append("\"thabaalThogai\":").append(thabaalThogai).append(",")
        sb.append("\"ahimsaPattuThogai\":").append(ahimsaPattuThogai).append(",")
        sb.append("\"items\":").append(itemsJson).append(",")
        sb.append("\"piraVarivugal\":").append(piraJson)
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

            val date = Regex(""""invoiceDate"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull()
            if (date != null && date > 0) invoiceDate = date

            val invOverride = Regex(""""invoiceNumberOverride"\s*:\s*"([^"]*)"""").find(json)?.groupValues?.get(1)
            if (!invOverride.isNullOrEmpty()) {
                invoiceNumber = invOverride
                isInvoiceNumberOverridden = true
            }

            val setharam = Regex(""""setharamGrams"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull()
            if (setharam != null) setharamGrams = setharam

            val thabaal = Regex(""""thabaalThogai"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull()
            if (thabaal != null) thabaalThogai = thabaal

            val ahimsa = Regex(""""ahimsaPattuThogai"\s*:\s*([0-9.]+)""").find(json)?.groupValues?.get(1)?.toDoubleOrNull()
            if (ahimsa != null) ahimsaPattuThogai = ahimsa

            val itemsMatch = Regex(""""items"\s*:\s*(\[.*\])""").find(json)?.groupValues?.get(1)
            val loadedItems = KooliKanakku.kooliListFromJson(itemsMatch)
            if (loadedItems.isNotEmpty()) {
                items = loadedItems
            }

            val piraMatch = Regex(""""piraVarivugal"\s*:\s*(\[.*\])""").find(json)?.groupValues?.get(1)
            val loadedPira = KooliKanakku.piraVarivuListFromJson(piraMatch)
            if (loadedPira.isNotEmpty()) {
                piraVarivugal = loadedPira
            }
            hasUnsavedChanges = true
        } catch (_: Exception) {}
    }

    LaunchedEffect(hasUnsavedChanges, selectedNiruvanamId, selectedVaangunarId, items.size, setharamGrams, thabaalThogai, ahimsaPattuThogai, piraVarivugal.size) {
        if (hasUnsavedChanges && !isEditing) {
            prefs.setString(draftKey, serializeDraft())
        }
    }

    val onCompanyChanged: (NiruvanaTharavugal?) -> Unit = { p ->
        selectedNiruvanamId = p?.id
        if (!isEditing) {
            val newPrefix = computePrefix(p)
            invoiceNumber = PattiyalRepository.getNextInvoiceNumber(p?.id, newPrefix, AppMode.KOOLI)
            isInvoiceNumberOverridden = false
        }
        hasUnsavedChanges = true
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

    val handleSave: () -> Unit = {
        if (profiles.size > 1 && selectedNiruvanamId == null) {
            errorMessage = niruvanamRequiredMsg
        } else if (selectedVaangunarId == null && selectedVaangunarPeyarMap.isEmpty()) {
            errorMessage = vaangunarRequiredMsg
        } else {
            val validItems = items.filter { it.edai > 0 && it.vilai > 0 }
            if (validItems.isEmpty()) {
                errorMessage = porulRequiredMsg
            } else {
                isSaving = true
                val vanakkam = PattiyalRepository.getNextVanakkam(selectedNiruvanamId, AppMode.KOOLI)
                val newInvoice = PattiyalTharavuru(
                    id = invoice?.id ?: 0L,
                    niruvanamId = selectedNiruvanamId,
                    patrucheettuEn = invoiceNumber,
                    vanakkam = if (invoice != null && invoice.vanakkam > 0) invoice.vanakkam else vanakkam,
                    pattiyalVagai = "",
                    vaangunarId = selectedVaangunarId,
                    vaangunarPeyar = selectedVaangunarPeyarMap,
                    vaangunarMunvari = selectedVaangunarMunvariMap,
                    pattiyalNaal = invoiceDate,
                    tharavugal = KooliKanakku.kooliListToJson(validItems),
                    mothaThogai = totals.perumMothangal,
                    mothaEdai = totals.mothaEdai,
                    setharamGrams = setharamGrams,
                    thabaalThogai = thabaalThogai,
                    ahimsaPattuThogai = ahimsaPattuThogai,
                    piravariVugal = KooliKanakku.piraVarivuListToJson(piraVarivugal),
                    updatedAt = System.currentTimeMillis()
                )
                PattiyalRepository.save(newInvoice, AppMode.KOOLI)
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
                    val companyName = selectedProfile?.niruvanathinPeyar?.get(primaryLang)?.ifEmpty { null }
                        ?: selectedProfile?.kurumPeyar?.ifEmpty { null }
                        ?: selectedProfile?.niruvanathinPeyar?.values?.firstOrNull().orEmpty()

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

            // ── Section 1: Customer (பெறுநர்) ──
            item(key = "customer_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex, title = K.billedTo.tr()) {
                        PattuVaangunargalKooru(
                            selectedVaangunarId = selectedVaangunarId,
                            onCustomerSelected = { customer ->
                                selectedVaangunarId = customer.id
                                selectedVaangunarPeyarMap = customer.peyar
                                selectedVaangunarMunvariMap = customer.mugavari
                                hasUnsavedChanges = true
                            },
                            onCustomerCleared = {
                                selectedVaangunarId = null
                                selectedVaangunarPeyarMap = emptyMap()
                                selectedVaangunarMunvariMap = emptyMap()
                                hasUnsavedChanges = true
                            },
                            onRequestAddNewCustomer = { showAddCustomerEditor = true }
                        )
                    }
                }
            }

            // ── Section 2: Invoice Details (பட்டியல் தரவுகள்) ──
            item(key = "invoice_details_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 1, title = K.invoiceDetails.tr()) {
                        val currentPrefix = computePrefix(selectedProfile)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ElvanAavanaEnnKooru(
                                label = K.receiptNumber.tr(),
                                prefix = "$currentPrefix-",
                                initialFullNumber = invoiceNumber,
                                onFullNumberChanged = { newNum ->
                                    invoiceNumber = newNum
                                    isInvoiceNumberOverridden = true
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true }
                            )

                            PattiyalNaalKooru(
                                label = K.date.tr(),
                                selectedDate = invoiceDate,
                                onDateChanged = { newDate ->
                                    invoiceDate = newDate
                                    hasUnsavedChanges = true
                                }
                            )
                        }
                    }
                }
            }

            // ── Section 3: Items (பொருட்கள்) ──
            item(key = "items_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 2, title = K.products.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // ── Product Line Items ──
                            ElvanAsaiPattiyal {
                                items.forEachIndexed { idx, itm ->
                                    key(itm.id) {
                                        ElvanAsaiCard(
                                            key = itm.id,
                                            isInitial = itm.id in initialItemIds,
                                            onDeleted = {
                                                deletingItemIds = deletingItemIds - itm.id
                                                items = items.filter { it.id != itm.id }
                                                hasUnsavedChanges = true
                                            }
                                        ) { requestDelete ->
                                            KooliUrupadiAttai(
                                                item = itm,
                                                index = idx,
                                                itemCount = activeItems.size,
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
                                                    items = items.map { if (it.id == itm.id) KooliUrupadi(id = itm.id) else it }
                                                    hasUnsavedChanges = true
                                                },
                                                onDirty = { hasUnsavedChanges = true },
                                                onRequestAddNewProduct = { showAddProductEditor = true },
                                                onAddNewItem = null,
                                                onAddNewCharge = null
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Stadium Action Buttons (+ சேர் & + பிற கட்டணம் சேர்) ──
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true)
                                        ) {
                                            items = items + KooliUrupadi()
                                            hasUnsavedChanges = true
                                        }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
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

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true)
                                        ) {
                                            piraVarivugal = piraVarivugal + PiraVarivu()
                                            hasUnsavedChanges = true
                                        }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
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
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // ── Dynamic Other Charges ──
                        ElvanAsaiPattiyal {
                            piraVarivugal.forEachIndexed { pIdx, charge ->
                                key(charge.id) {
                                    ElvanAsaiCard(
                                        key = charge.id,
                                        isInitial = charge.id in initialChargeIds,
                                        modifier = Modifier.padding(top = if (pIdx == 0) 16.dp else 0.dp),
                                        onDeleted = {
                                            deletingChargeIds = deletingChargeIds - charge.id
                                            piraVarivugal = piraVarivugal.filter { it.id != charge.id }
                                            hasUnsavedChanges = true
                                        }
                                    ) { requestDelete ->
                                            KooliPiraVarivuAttai(
                                                charge = charge,
                                                index = pIdx,
                                                onUpdated = { updatedCharge ->
                                                    piraVarivugal = piraVarivugal.map { if (it.id == charge.id) updatedCharge else it }
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

            // ── Section 4: Totals & Extra Charges (மொத்தங்கள்) ──
            item(key = "totals_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 3, title = K.totals.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            KooliMelthogaiKooru(
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

                            KooliMothangalKooru(
                                totals = totals,
                                setharamGrams = setharamGrams,
                                ahimsaPattuThogai = ahimsaPattuThogai,
                                thabaalThogai = thabaalThogai,
                                piraVarivugal = activeCharges
                            )
                        }
                    }
                }
            }


            }
        }
    }

    // ── Company Selection Bottom Sheet ──
    if (isCompanySheetOpen) {
        ElvanSelectionBottomSheet(
            title = K.selectCompany.tr(),
            items = profiles,
            currentValue = selectedProfile,
            onDismissRequest = { isCompanySheetOpen = false },
            onSelected = { profile ->
                onCompanyChanged(profile)
                isCompanySheetOpen = false
            },
            itemLabelBuilder = { p ->
                p.niruvanathinPeyar[primaryLang]?.ifEmpty { null }
                    ?: p.kurumPeyar.ifEmpty {
                        p.niruvanathinPeyar.values.firstOrNull().orEmpty()
                    }
            },
            subtitleBuilder = { p ->
                val sec = p.niruvanathinPeyar[secondaryLang].orEmpty()
                val oor = p.oor[primaryLang] ?: p.oor.values.firstOrNull().orEmpty()
                listOf(sec, oor).filter { it.isNotEmpty() }.joinToString(" • ")
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

    // ── In-Editor Full-Screen Overlays (Add Product / Add Customer) ──
    if (showAddProductEditor) {
        AppBackHandler { showAddProductEditor = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .zIndex(100f)
        ) {
            ProductEditorScreen(
                item = null,
                onBack = {
                    showAddProductEditor = false
                    PorulRepository.loadAll(AppMode.KOOLI)
                }
            )
        }
    }

    if (showAddCustomerEditor) {
        AppBackHandler { showAddCustomerEditor = false }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .zIndex(100f)
        ) {
            CustomerEditorScreen(
                merchant = null,
                onBack = {
                    showAddCustomerEditor = false
                    VaangunarRepository.loadAll(AppMode.KOOLI)
                }
            )
        }
    }
}
