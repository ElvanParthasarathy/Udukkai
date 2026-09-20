package com.elvan.udukkai.ui.screens.editor.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.model.PatruPattiyalInaippuTharavuru
import com.elvan.udukkai.data.model.PatrugalTharavuru
import com.elvan.udukkai.data.model.SeluthiVagai
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiKeezhvirivu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu
import com.elvan.udukkai.ui.screens.editor.receipt.components.ReceiptInvoiceSelectionSheet
import com.elvan.udukkai.ui.screens.editor.invoice.components.ElvanAavanaEnnKooru
import com.elvan.udukkai.ui.screens.editor.invoice.components.PattiyalNaalKooru
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer

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
 * Receipt Editor Screen (பற்றுச்சீட்டு திருத்தி)
 * Complete 1:1 port of Flutter's `PatruThiruthi` (patru_thiruthi.dart).
 *
 * Architecture:
 * - Section 0: Business profile selector (45dp capsule pill with clear X, bottom sheet picker, locked if multiple)
 * - Section 1: Linked invoice picker (45dp capsule button, multi-select bottom sheet, stadium chip list)
 * - Section 3: Payment details (amount with auto-calc, SeluthiVagai bottom sheet picker, dynamic reference, internal notes)
 * - Indian Rupee formatting
 * - Unsaved changes guard with 3-action sheet
 * - Continuous form layout matching Flutter (no card container wrappers)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReceiptEditorScreen(
    receipt: PatrugalTharavuru? = null,
    onBack: () -> Unit
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val isEditing = receipt != null && receipt.id > 0L

    val profiles: List<NiruvanaTharavugal> = NiruvanaTharavugalRepository.getAllProfiles(currentMode)
    var selectedNiruvanamId by remember {
        mutableStateOf(receipt?.niruvanamId ?: if (profiles.size == 1) profiles.first().id else null)
    }
    val selectedProfile = remember(selectedNiruvanamId, profiles) {
        profiles.firstOrNull { it.id == selectedNiruvanamId }
    }

    // Auto-select if only 1 profile exists
    LaunchedEffect(profiles) {
        if (profiles.size == 1 && selectedNiruvanamId == null) {
            selectedNiruvanamId = profiles.first().id
        }
    }

    val isFormLocked = profiles.size > 1 && selectedNiruvanamId == null

    // Refresh dependencies on launch
    LaunchedEffect(Unit) {
        PattiyalRepository.loadAll(currentMode)
        PatrugalRepository.loadAll(currentMode)
    }

    // Linked Invoices
    val allInvoices = PattiyalRepository.invoices
    val selectedInvoices = remember { mutableStateListOf<PattiyalTharavuru>() }
    var isInvoicePickerOpen by remember { mutableStateOf(false) }

    // Initial load of linked invoices when editing
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(receipt?.id, allInvoices) {
        if (receipt != null && !isInitialized && allInvoices.isNotEmpty()) {
            isInitialized = true
            val links = PatrugalRepository.getLinksForPatru(receipt.id, currentMode)
            val matching = links.mapNotNull { link ->
                allInvoices.firstOrNull { it.id == link.pattiyalId }
            }
            selectedInvoices.clear()
            selectedInvoices.addAll(matching)
        }
    }

    // Customer
    var selectedVaangunarId by remember { mutableStateOf(receipt?.vaangunarId) }
    var selectedVaangunarPeyarMap by remember {
        mutableStateOf(receipt?.vaangunarPeyar ?: emptyMap())
    }
    var selectedVaangunarMunvariMap by remember {
        mutableStateOf(receipt?.vaangunarMunvari ?: emptyMap())
    }

    // Receipt Data
    var patruNaal by remember { mutableStateOf(receipt?.patruNaal ?: System.currentTimeMillis()) }
    var vanakkam by remember { mutableStateOf(receipt?.vanakkam ?: 1) }
    var patruEn by remember { mutableStateOf(receipt?.patruEn.orEmpty()) }

    // Payment Details
    var thogai by remember {
        mutableStateOf(
            if (receipt != null && receipt.thogai > 0) {
                if (receipt.thogai % 1.0 == 0.0) receipt.thogai.toLong().toString() else receipt.thogai.toString()
            } else ""
        )
    }
    var seluthiVagai by remember {
        mutableStateOf(SeluthiVagai.fromStored(receipt?.seluthumMurai))
    }
    var suttruEn by remember { mutableStateOf(receipt?.parivarthanaiEn.orEmpty()) }
    var ullkurippu by remember { mutableStateOf(receipt?.ullkurippu.orEmpty()) }

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isCompanySheetOpen by remember { mutableStateOf(false) }
    var isSeluthiVagaiSheetOpen by remember { mutableStateOf(false) }

    val amaippugalStr = K.settings.tr()
    val pattiyalaiThaernheduMsg = K.selectInvoice.tr()
    val vaangunarPeyarThaevaiMsg = K.customerNameRequired.tr()
    val thogaiInvalidMsg = K.amountMustBeGreaterThanZero.tr()
    val duplicateMsg = K.receiptNumberAlreadyExists.tr()
    val savedMsg = K.receiptSaved.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    // Helper: auto-calculate receipt number
    fun generatePatruEn(profile: NiruvanaTharavugal?) {
        if (isEditing) return
        val bizShort = profile?.kurumPeyar?.trim()?.ifEmpty { null }
            ?: profile?.niruvanathinPeyar?.get("en")?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercaseChar() }?.joinToString("")?.take(4)?.ifEmpty { null }
            ?: "BIZ"

        val nextSeq = PatrugalRepository.getNextVanakkam(profile?.id, currentMode)
        vanakkam = nextSeq
        patruEn = PatrugalRepository.formatPatruEn(bizShort, nextSeq)
    }

    fun onCompanyChanged(newProfile: NiruvanaTharavugal?) {
        selectedNiruvanamId = newProfile?.id
        hasUnsavedChanges = true
        if (!isEditing) {
            generatePatruEn(newProfile)
        }
    }

    // Auto-generate receipt number on profile selection if new
    LaunchedEffect(selectedNiruvanamId) {
        if (!isEditing && patruEn.isEmpty() && selectedNiruvanamId != null) {
            generatePatruEn(selectedProfile)
        }
    }

    // Auto-fill customer and amount when invoices change
    fun onInvoicesUpdated(newList: List<PattiyalTharavuru>) {
        selectedInvoices.clear()
        selectedInvoices.addAll(newList)
        hasUnsavedChanges = true

        if (newList.isNotEmpty()) {
            val total = newList.sumOf { it.mothaThogai }
            thogai = if (total % 1.0 == 0.0) total.toLong().toString() else ((total * 100).toLong() / 100.0).toString()

            val first = newList.first()
            if (selectedVaangunarId == null || selectedVaangunarPeyarMap.isEmpty()) {
                selectedVaangunarId = first.vaangunarId
                selectedVaangunarPeyarMap = first.vaangunarPeyar
                selectedVaangunarMunvariMap = first.vaangunarMunvari
            }
        } else {
            thogai = ""
            selectedVaangunarId = null
            selectedVaangunarPeyarMap = emptyMap()
            selectedVaangunarMunvariMap = emptyMap()
        }
    }

    // Save logic with FIFO distribution
    fun handleSave() {
        if (selectedProfile == null || selectedProfile.kurumPeyar.trim().isEmpty()) {
            ElvanSnackbar.show("$amaippugalStr - Kurum Peyar is required.")
            return
        }

        if (selectedInvoices.isEmpty()) {
            ElvanSnackbar.show(pattiyalaiThaernheduMsg)
            return
        }

        val peyarTamil = selectedVaangunarPeyarMap["ta"] ?: selectedVaangunarPeyarMap.values.firstOrNull().orEmpty()
        if (peyarTamil.trim().isEmpty()) {
            ElvanSnackbar.show(vaangunarPeyarThaevaiMsg)
            return
        }

        val amount = thogai.trim().toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            ElvanSnackbar.show(thogaiInvalidMsg)
            return
        }

        // Duplicate check
        val isDuplicate = PatrugalRepository.isPatruEnDuplicate(
            selectedNiruvanamId,
            patruEn,
            excludeId = receipt?.id,
            mode = currentMode
        )
        if (isDuplicate) {
            ElvanSnackbar.show("$patruEn - $duplicateMsg")
            return
        }

        // FIFO allocation across selected invoices
        val links = mutableListOf<PatruPattiyalInaippuTharavuru>()
        var remaining = amount
        for (inv in selectedInvoices) {
            if (remaining <= 0.0) break
            val apply = remaining.coerceAtMost(inv.mothaThogai)
            if (apply > 0.0) {
                links.add(
                    PatruPattiyalInaippuTharavuru(
                        patruId = receipt?.id ?: 0L,
                        pattiyalId = inv.id,
                        poruthiyaThogai = apply
                    )
                )
                remaining -= apply
            }
        }

        // Validate links
        val validationErr = PatrugalRepository.validateLinks(links, receipt?.id, currentMode)
        if (validationErr != null) {
            ElvanSnackbar.show(validationErr)
            return
        }

        isSaving = true
        try {
            val now = System.currentTimeMillis()
            val defaultFinYear = DateUtils.formatEpochMillis(now).split("/").last()
            val entryToSave = PatrugalTharavuru(
                id = receipt?.id ?: 0L,
                niruvanamId = selectedNiruvanamId,
                patruEn = patruEn.trim(),
                finYear = receipt?.finYear ?: defaultFinYear,
                vanakkam = vanakkam,
                vaangunarId = selectedVaangunarId,
                vaangunarPeyar = selectedVaangunarPeyarMap,
                vaangunarMunvari = selectedVaangunarMunvariMap,
                patruNaal = patruNaal,
                thogai = amount,
                seluthumMurai = seluthiVagai.storedValue,
                vangiPeyar = receipt?.vangiPeyar,
                parivarthanaiEn = if (seluthiVagai.needsReference) suttruEn.trim() else null,
                ullkurippu = ullkurippu.trim(),
                createdAt = receipt?.createdAt ?: now,
                updatedAt = now,
                isDeleted = false
            )

            val savedId = PatrugalRepository.saveWithLinks(entryToSave, links, currentMode)
            if (savedId > 0L) {
                ElvanSnackbar.show(savedMsg)
                onBack()
            } else {
                ElvanSnackbar.show(saveFailedMsg)
            }
        } catch (e: Exception) {
            ElvanSnackbar.show("$saveFailedMsg ${e.message}")
        } finally {
            isSaving = false
        }
    }

    // Back navigation guard
    fun handleBackAttempt() {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    AppBackHandler(enabled = true) {
        handleBackAttempt()
    }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.createRecord.tr()
    val scrollState = rememberLazyListState()
    val pillBg = colors.iconBg

    ElvanSubShell(
        title = pageTitle,
        onBack = { handleBackAttempt() },
        scrollState = scrollState,
        hasActions = true,
        actions = {
            ElvanActionButton(
                label = K.saveBtn.tr(),
                onClick = { handleSave() },
                enabled = !isSaving && !isFormLocked
            )
        }
    ) {
        CompositionLocalProvider(LocalEditorAccentColor provides colors.receiptColor) {
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
            item(key = "top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
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
                                    .background(pillBg)
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

            // ── Section 1: Linked Invoice (Required) ──
            item(key = "invoice_picker_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex, title = K.forWhichInvoice.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElvanThiruthiThalaippu(label = K.invoice.tr())

                            // Select Invoices 48dp pill button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(pillBg)
                                    .clickable { isInvoicePickerOpen = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Invoice,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedInvoices.isEmpty()) {
                                            K.selectInvoices.tr().preventBrokenLigatures()
                                        } else {
                                            "${selectedInvoices.size} ${K.invoices.tr().preventBrokenLigatures()}"
                                        },
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary
                                        )
                                    )
                                }
                            }

                            // Selected Invoices Stadium Chips
                            if (selectedInvoices.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    selectedInvoices.forEach { inv ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(999.dp))
                                                .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                                                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = inv.patrucheettuEn,
                                                    style = TextStyle(
                                                        fontFamily = ff,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.textPrimary
                                                    )
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = ripple(bounded = true, radius = 10.dp)
                                                        ) {
                                                            val updated = selectedInvoices.filter { it.id != inv.id }
                                                            onInvoicesUpdated(updated)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = MaterialSymbols.Rounded.Cancel,
                                                        contentDescription = "Remove",
                                                        tint = colors.textSecondary,
                                                        modifier = Modifier.size(16.dp)
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

            // ── Section 2: Receipt Data ──
            item(key = "receipt_data_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 1, title = K.receiptDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 1. Receipt Date
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ElvanThiruthiThalaippu(label = K.receiptDate.tr())
                                PattiyalNaalKooru(
                                    selectedDate = patruNaal,
                                    onDateChanged = {
                                        patruNaal = it
                                        hasUnsavedChanges = true
                                    }
                                )
                            }

                            // 2. Customer Pill (Locked with lock icon when loaded from invoice)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ElvanThiruthiThalaippu(label = K.customer.tr())
                                val customerName = selectedVaangunarPeyarMap["ta"]
                                    ?: selectedVaangunarPeyarMap.values.firstOrNull().orEmpty()

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(pillBg)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (customerName.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = customerName.preventBrokenLigatures(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.textPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.Lock,
                                                contentDescription = "Locked",
                                                tint = colors.textSecondary.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.Info,
                                                contentDescription = null,
                                                tint = colors.textSecondary.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = K.customerDetailsAutoFilledAfterInvoice.tr().preventBrokenLigatures(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 14.sp,
                                                    color = colors.textSecondary.copy(alpha = 0.7f)
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Receipt Number (with pencil edit toggle)
                            val bizPrefix = if (selectedProfile != null && selectedProfile.kurumPeyar.isNotEmpty()) {
                                "RCP/${selectedProfile.kurumPeyar}/"
                            } else {
                                "RCP/BIZ/"
                            }
                            ElvanAavanaEnnKooru(
                                label = K.receiptNumber.tr(),
                                prefix = bizPrefix,
                                initialFullNumber = patruEn,
                                onFullNumberChanged = {
                                    patruEn = it
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true }
                            )
                        }
                    }
                }
            }

            // ── Section 3: Payment Details ──
            item(key = "payment_section") {
                FormLockWrapper(isLocked = isFormLocked) {
                    ElvanEditorSection(index = baseIndex + 2, title = K.paymentDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 1. Amount
                            ElvanThiruthiUlleedu(
                                value = thogai,
                                onValueChange = {
                                    thogai = it
                                    hasUnsavedChanges = true
                                },
                                label = K.amountRequired.tr(),
                                prefixText = "₹ ",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            // 2. Payment Mode Selector Pill
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ElvanThiruthiThalaippu(label = K.paymentModeRequired.tr())
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(pillBg)
                                        .clickable { isSeluthiVagaiSheetOpen = true }
                                        .padding(start = 20.dp, end = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = seluthiVagai.icon,
                                                contentDescription = null,
                                                tint = colors.textPrimary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = seluthiVagai.label().preventBrokenLigatures(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.textPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

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

                            // 3. Reference Number (shown when payment mode requires it)
                            if (seluthiVagai.needsReference) {
                                ElvanThiruthiUlleedu(
                                    value = suttruEn,
                                    onValueChange = {
                                        suttruEn = it
                                        hasUnsavedChanges = true
                                    },
                                    label = K.referenceOrTxnNo.tr()
                                )
                            }

                            // 4. Internal Notes
                            ElvanThiruthiUlleedu(
                                value = ullkurippu,
                                onValueChange = {
                                    ullkurippu = it
                                    hasUnsavedChanges = true
                                },
                                label = K.remarks.tr(),
                                singleLine = false,
                                minLines = 2,
                                maxLines = 6
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Modal: Business Profile Bottom Sheet
    if (isCompanySheetOpen) {
        ElvanSelectionBottomSheet<NiruvanaTharavugal>(
            title = K.company.tr(),
            items = profiles,
            currentValue = selectedProfile,
            onSelected = { profile ->
                onCompanyChanged(profile)
                isCompanySheetOpen = false
            },
            onDismissRequest = { isCompanySheetOpen = false },
            itemLabelBuilder = {
                it.kurumPeyar.ifEmpty { it.niruvanathinPeyar.values.firstOrNull().orEmpty() }
            },
            subtitleBuilder = {
                it.niruvanathinPeyar["ta"] ?: it.niruvanathinPeyar["en"]
            }
        )
    }

    // Modal: Invoice Picker Bottom Sheet (Multi-select)
    if (isInvoicePickerOpen) {
        val selectableInvoices = if (selectedNiruvanamId != null) {
            allInvoices.filter { it.niruvanamId == selectedNiruvanamId }
        } else {
            allInvoices
        }

        ReceiptInvoiceSelectionSheet(
            invoices = selectableInvoices,
            initialSelectedIds = selectedInvoices.map { it.id }.toSet(),
            onConfirmed = { pickedList ->
                onInvoicesUpdated(pickedList)
                isInvoicePickerOpen = false
            },
            onDismissRequest = { isInvoicePickerOpen = false }
        )
    }

    // Modal: Payment Mode Bottom Sheet
    if (isSeluthiVagaiSheetOpen) {
        ElvanSelectionBottomSheet<SeluthiVagai>(
            title = K.selectPaymentMode.tr(),
            items = SeluthiVagai.entries,
            currentValue = seluthiVagai,
            onSelected = { mode ->
                seluthiVagai = mode
                if (mode == SeluthiVagai.PANAM) {
                    suttruEn = ""
                }
                hasUnsavedChanges = true
                isSeluthiVagaiSheetOpen = false
            },
            onDismissRequest = { isSeluthiVagaiSheetOpen = false },
            itemLabelBuilder = { it.labelString() },
            leadingBuilder = { mode ->
                Icon(
                    imageVector = mode.icon,
                    contentDescription = null,
                    tint = colors.textPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }

    // Modal: Unsaved Changes Action Sheet
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
}
