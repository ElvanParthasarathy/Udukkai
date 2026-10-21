package com.elvan.udukkai.textiles.ui.screens.editor

import androidx.compose.ui.Alignment
import com.elvan.udukkai.ui.screens.editor.ElvanEditorBottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptInvoiceLinkModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.data.model.PaymentType
import com.elvan.udukkai.data.settings.BusinessProfile
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
import com.elvan.udukkai.ui.screens.editor.ElvanEditorDropdown
import com.elvan.udukkai.ui.screens.editor.ElvanEditorLabel
import com.elvan.udukkai.ui.screens.editor.ElvanEditorTextField
import com.elvan.udukkai.ui.screens.editor.receipt.components.ReceiptInvoiceSelectionSheet
import com.elvan.udukkai.ui.screens.editor.invoice.components.ElvanDocumentNumberField
import com.elvan.udukkai.ui.screens.editor.invoice.components.InvoiceDateField
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
fun TextilesReceiptEditorScreen(
    receipt: ReceiptModel? = null,
    onBack: () -> Unit
) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val isEditing = receipt != null && receipt.id > 0L

    val profiles: List<BusinessProfile> = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profiles
    val activeProfile = com.elvan.udukkai.textiles.data.TextilesProfileRepository.profile
    val selectedProfile = remember(profiles, activeProfile, receipt?.companyId) {
        if (isEditing) {
            profiles.firstOrNull { it.id == receipt?.companyId } ?: activeProfile
        } else {
            activeProfile
        }
    }
    val selectedCompanyId = if (isEditing) (receipt?.companyId ?: selectedProfile.id) else (selectedProfile.id ?: profiles.firstOrNull()?.id)
    val isProfileConfigured = profiles.isNotEmpty() && selectedCompanyId != null

    // Refresh dependencies on launch
    LaunchedEffect(Unit) {
        com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.loadAll()
        com.elvan.udukkai.textiles.data.TextilesReceiptRepository.loadAll()
    }

    // Linked Invoices
    val allInvoices = com.elvan.udukkai.textiles.data.TextilesInvoiceRepository.invoices
    val selectedInvoices = remember { mutableStateListOf<InvoiceModel>() }
    var isInvoicePickerOpen by remember { mutableStateOf(false) }

    // Initial load of linked invoices when editing
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(receipt?.id, allInvoices) {
        if (receipt != null && !isInitialized && allInvoices.isNotEmpty()) {
            isInitialized = true
            val links = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.getLinksForReceipt(receipt.id)
            val matching = links.mapNotNull { link ->
                allInvoices.firstOrNull { it.id == link.invoiceId }
            }
            selectedInvoices.clear()
            selectedInvoices.addAll(matching)
        }
    }

    // Customer
    var selectedCustomerId by remember { mutableStateOf(receipt?.customerId) }
    var selectedCustomerNameMap by remember {
        mutableStateOf(receipt?.customerName ?: emptyMap())
    }
    var selectedCustomerAddressMap by remember {
        mutableStateOf(receipt?.customerAddress ?: emptyMap())
    }

    // Receipt Data
    var receiptDate by remember { mutableStateOf(receipt?.receiptDate ?: System.currentTimeMillis()) }
    var vanakkam by remember { mutableStateOf(receipt?.vanakkam ?: 1) }
    var receiptNumber by remember { mutableStateOf(receipt?.receiptNumber.orEmpty()) }

    // Payment Details
    var thogai by remember {
        mutableStateOf(
            if (receipt != null && receipt.amount > 0) {
                if (receipt.amount % 1.0 == 0.0) receipt.amount.toLong().toString() else receipt.amount.toString()
            } else ""
        )
    }
    var paymentType by remember {
        mutableStateOf(PaymentType.fromStored(receipt?.paymentMode))
    }
    var suttruEn by remember { mutableStateOf(receipt?.transactionNumber.orEmpty()) }
    var ullkurippu by remember { mutableStateOf(receipt?.notes.orEmpty()) }

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var isExiting by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isPaymentTypeSheetOpen by remember { mutableStateOf(false) }

    val settingsStr = K.settings.tr()
    val selectInvoiceMsg = K.selectInvoice.tr()
    val customerNameRequiredMsg = K.customerNameRequired.tr()
    val thogaiInvalidMsg = K.amountMustBeGreaterThanZero.tr()
    val duplicateMsg = K.receiptNumberAlreadyExists.tr()
    val savedMsg = K.receiptSaved.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()
    val selectCompanyProfileMsg = K.selectCompanyProfile.tr()

    // Helper: auto-calculate receipt number
    fun generateReceiptNumber(profile: BusinessProfile?) {
        if (isEditing) return
        val bizShort = profile?.shortName?.trim()?.ifEmpty { null }
            ?: profile?.businessName?.get("en")?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercaseChar() }?.joinToString("")?.take(4)?.ifEmpty { null }
            ?: "BIZ"

        val nextSeq = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.getNextVanakkam(profile?.id)
        vanakkam = nextSeq
        receiptNumber = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.formatReceiptNumber(bizShort, nextSeq)
    }

    // Auto-generate receipt number on profile selection if new
    LaunchedEffect(selectedCompanyId) {
        if (!isEditing && receiptNumber.isEmpty() && selectedCompanyId != null) {
            generateReceiptNumber(selectedProfile)
        }
    }

    // Auto-fill customer and amount when invoices change
    fun onInvoicesUpdated(newList: List<InvoiceModel>) {
        selectedInvoices.clear()
        selectedInvoices.addAll(newList)
        hasUnsavedChanges = true

        if (newList.isNotEmpty()) {
            val total = newList.sumOf { it.totalAmount }
            thogai = if (total % 1.0 == 0.0) total.toLong().toString() else ((total * 100).toLong() / 100.0).toString()

            val first = newList.first()
            if (selectedCustomerId == null || selectedCustomerNameMap.isEmpty()) {
                selectedCustomerId = first.customerId
                selectedCustomerNameMap = first.customerName
                selectedCustomerAddressMap = first.customerAddress
            }
        } else {
            thogai = ""
            selectedCustomerId = null
            selectedCustomerNameMap = emptyMap()
            selectedCustomerAddressMap = emptyMap()
        }
    }

    // Save logic with FIFO distribution
    fun handleSave() {
        if (!isProfileConfigured) {
            ElvanSnackbar.show(selectCompanyProfileMsg)
            return
        }

        if (selectedInvoices.isEmpty()) {
            ElvanSnackbar.show(selectInvoiceMsg)
            return
        }

        val customerNameTamil = selectedCustomerNameMap["ta"] ?: selectedCustomerNameMap.values.firstOrNull().orEmpty()
        if (customerNameTamil.trim().isEmpty()) {
            ElvanSnackbar.show(customerNameRequiredMsg)
            return
        }

        val amount = thogai.trim().toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            ElvanSnackbar.show(thogaiInvalidMsg)
            return
        }

        // Duplicate check
        val isDuplicate = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.isReceiptNumberDuplicate(receiptNumber, selectedCompanyId, receipt?.id)
        if (isDuplicate) {
            ElvanSnackbar.show("$receiptNumber - $duplicateMsg")
            return
        }

        // FIFO allocation across selected invoices
        val links = mutableListOf<ReceiptInvoiceLinkModel>()
        var remaining = amount
        for (inv in selectedInvoices) {
            if (remaining <= 0.0) break
            val apply = remaining.coerceAtMost(inv.totalAmount)
            if (apply > 0.0) {
                links.add(
                    ReceiptInvoiceLinkModel(
                        receiptId = receipt?.id ?: 0L,
                        invoiceId = inv.id,
                        appliedAmount = apply
                    )
                )
                remaining -= apply
            }
        }

        // Validate links
        val validationErr = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.validateLinks(links, receipt?.id)
        if (validationErr != null) {
            ElvanSnackbar.show(validationErr)
            return
        }

        isSaving = true
        try {
            val now = System.currentTimeMillis()
            val defaultFinYear = DateUtils.formatEpochMillis(now).split("/").last()
            val entryToSave = ReceiptModel(
                id = receipt?.id ?: 0L,
                companyId = selectedCompanyId,
                receiptNumber = receiptNumber.trim(),
                finYear = receipt?.finYear ?: defaultFinYear,
                vanakkam = vanakkam,
                customerId = selectedCustomerId,
                customerName = selectedCustomerNameMap,
                customerAddress = selectedCustomerAddressMap,
                receiptDate = receiptDate,
                amount = amount,
                paymentMode = paymentType.storedValue,
                bankName = receipt?.bankName,
                transactionNumber = if (paymentType.needsReference) suttruEn.trim() else null,
                notes = ullkurippu.trim(),
                createdAt = receipt?.createdAt ?: now,
                updatedAt = now,
                isDeleted = false
            )

            val savedId = com.elvan.udukkai.textiles.data.TextilesReceiptRepository.saveWithLinks(entryToSave, links)
            if (savedId > 0L) {
                isExiting = true
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
            isExiting = true
            onBack()
        }
    }

    AppBackHandler(enabled = !isExiting && (hasUnsavedChanges || showUnsavedDialog)) {
        handleBackAttempt()
    }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.createRecord.tr()
    val scrollState = rememberLazyListState()
    val pillBg = colors.iconBg

    ElvanSubShell(
        title = pageTitle,
        onBack = { handleBackAttempt() },
        scrollState = scrollState,
        hasActions = false,
        actions = {},
        floatingBar = {
            ElvanEditorBottomBar(
                label = K.saveBtn.tr(),
                onClick = { handleSave() },
                enabled = !isSaving,
                isLoading = isSaving,
                accentColor = colors.receiptColor
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalEditorAccentColor provides colors.receiptColor) {
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
            item(key = "top_spacer") {
                Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
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

            // ── Section 0: Linked Invoice (Required) ──
            item(key = "invoice_picker_section") {
                ElvanEditorSection(index = 0, title = K.forWhichInvoice.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElvanEditorLabel(label = K.invoice.tr())

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
                                                    text = inv.invoiceNumber,
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

            // ── Section 1: Receipt Data ──
            item(key = "receipt_data_section") {
                ElvanEditorSection(index = 1, title = K.receiptDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 1. Receipt Date
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ElvanEditorLabel(label = K.receiptDate.tr())
                                InvoiceDateField(
                                    selectedDate = receiptDate,
                                    onDateChanged = {
                                        receiptDate = it
                                        hasUnsavedChanges = true
                                    }
                                )
                            }

                            // 2. Customer Pill (Locked with lock icon when loaded from invoice)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ElvanEditorLabel(label = K.customer.tr())
                                val customerName = selectedCustomerNameMap["ta"]
                                    ?: selectedCustomerNameMap.values.firstOrNull().orEmpty()

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

                            val bizPrefix = if (selectedProfile.shortName.isNotEmpty()) {
                                "RCP/${selectedProfile.shortName}/"
                            } else {
                                "RCP/BIZ/"
                            }
                            ElvanDocumentNumberField(
                                label = K.receiptNumber.tr(),
                                prefix = bizPrefix,
                                initialFullNumber = receiptNumber,
                                onFullNumberChanged = {
                                    receiptNumber = it
                                    hasUnsavedChanges = true
                                },
                                onDirty = { hasUnsavedChanges = true }
                            )
                        }
                    }
                }

            // ── Section 2: Payment Details ──
            item(key = "payment_section") {
                ElvanEditorSection(index = 2, title = K.paymentDetails.tr()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // 1. Amount
                            ElvanEditorTextField(
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
                                ElvanEditorLabel(label = K.paymentModeRequired.tr())
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(pillBg)
                                        .clickable { isPaymentTypeSheetOpen = true }
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
                                                imageVector = paymentType.icon,
                                                contentDescription = null,
                                                tint = colors.textPrimary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = paymentType.label().preventBrokenLigatures(),
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
                            if (paymentType.needsReference) {
                                ElvanEditorTextField(
                                    value = suttruEn,
                                    onValueChange = {
                                        suttruEn = it
                                        hasUnsavedChanges = true
                                    },
                                    label = K.referenceOrTxnNo.tr()
                                )
                            }

                            // 4. Internal Notes
                            ElvanEditorTextField(
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

    // Modal: Invoice Picker Bottom Sheet (Multi-select)
    if (isInvoicePickerOpen) {
        val selectableInvoices = if (selectedCompanyId != null) {
            allInvoices.filter { it.companyId == selectedCompanyId }
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
            onDismissRequest = { isInvoicePickerOpen = false },
            merchants = com.elvan.udukkai.textiles.data.TextilesCustomerRepository.customers
        )
    }

    // Modal: Payment Mode Bottom Sheet
    if (isPaymentTypeSheetOpen) {
        ElvanSelectionBottomSheet<PaymentType>(
            title = K.selectPaymentMode.tr(),
            items = PaymentType.entries,
            currentValue = paymentType,
            onSelected = { mode ->
                paymentType = mode
                if (mode == PaymentType.CASH) {
                    suttruEn = ""
                }
                hasUnsavedChanges = true
                isPaymentTypeSheetOpen = false
            },
            onDismissRequest = { isPaymentTypeSheetOpen = false },
            itemLabelBuilder = { it.labelString() },
            leadingBuilder = @Composable { mode ->
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
                onBack()
            }
        )
    }
}
