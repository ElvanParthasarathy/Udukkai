package com.elvan.udukkai.ui.screens.editor.invoice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ripple
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
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
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
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiAttai
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiKeezhvirivu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu
import kotlin.math.floor
import kotlin.math.roundToLong

private data class InvoiceLineItem(
    val id: String = System.currentTimeMillis().toString() + (0..999).random(),
    var name: String = "",
    var hsn: String = "",
    var quantityOrWeight: String = "1",
    var rate: String = "0",
    var gstRate: String = "5"
)

/**
 * Invoice Editor Screen (பட்டியல் திருத்தி)
 * Handles both Kooli (weight billing) and Silk/Pattu (GST item billing) modes.
 * Uses Flutter's 1:1 boxed card sections, zero bottom-up sheets, and perfectly synced collapsing header.
 */
@Composable
fun InvoiceEditorScreen(
    invoice: PattiyalTharavuru? = null,
    onBack: () -> Unit
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val ff = LocalAppFontFamily.current
    val isEditing = invoice != null && invoice.id > 0L

    val profiles: List<NiruvanaTharavugal> = NiruvanaTharavugalRepository.getAllProfiles(currentMode)
    var selectedNiruvanamId by remember {
        mutableStateOf(invoice?.niruvanamId ?: profiles.firstOrNull()?.id)
    }

    val merchants = VaangunarRepository.merchants
    var selectedVaangunarId by remember { mutableStateOf(invoice?.vaangunarId) }
    var selectedVaangunarPeyarMap by remember {
        mutableStateOf(invoice?.vaangunarPeyar ?: emptyMap())
    }
    var selectedVaangunarMunvariMap by remember {
        mutableStateOf(invoice?.vaangunarMunvari ?: emptyMap())
    }

    var customerSearchQuery by remember { mutableStateOf("") }
    var isCustomerDropdownOpen by remember { mutableStateOf(false) }

    var invoiceNumber by remember {
        mutableStateOf(
            invoice?.patrucheettuEn ?: "INV-${(System.currentTimeMillis() % 10000)}"
        )
    }
    var invoiceDate by remember { mutableStateOf(invoice?.pattiyalNaal ?: System.currentTimeMillis()) }
    var placeOfSupply by remember { mutableStateOf(if (currentMode == AppMode.PATTU) "Tamil Nadu" else "") }

    // Line items list
    var lineItems by remember {
        mutableStateOf(
            listOf(
                InvoiceLineItem(
                    name = if (currentMode == AppMode.KOOLI) "கூலி நெசவு" else "பட்டு புடவை",
                    quantityOrWeight = "1",
                    rate = if (invoice != null && invoice.mothaThogai > 0) invoice.mothaThogai.toString() else "0"
                )
            )
        )
    }

    // Coolie additional charges
    var setharamGrams by remember { mutableStateOf(if (invoice != null && invoice.setharamGrams > 0) invoice.setharamGrams.toString() else "") }
    var courierCharges by remember { mutableStateOf(if (invoice != null && invoice.thabaalThogai > 0) invoice.thabaalThogai.toString() else "") }
    var ahimsaCharges by remember { mutableStateOf(if (invoice != null && invoice.ahimsaPattuThogai > 0) invoice.ahimsaPattuThogai.toString() else "") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.newInvoiceBtn.tr()
    val thogaiRequiredMsg = K.amountMustBeGreaterThanZero.tr()
    val saveSuccessMsg = K.savedSuccessfully.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    // Real-time calculated totals
    val subtotal = remember(lineItems) {
        lineItems.sumOf { item ->
            val qty = item.quantityOrWeight.toDoubleOrNull() ?: 0.0
            val r = item.rate.toDoubleOrNull() ?: 0.0
            if (currentMode == AppMode.KOOLI) {
                floor(qty * r)
            } else {
                qty * r
            }
        }
    }

    val taxOrAdditional = remember(lineItems, setharamGrams, courierCharges, ahimsaCharges, currentMode) {
        if (currentMode == AppMode.KOOLI) {
            val courier = courierCharges.toDoubleOrNull() ?: 0.0
            val ahimsa = ahimsaCharges.toDoubleOrNull() ?: 0.0
            courier + ahimsa
        } else {
            lineItems.sumOf { item ->
                val qty = item.quantityOrWeight.toDoubleOrNull() ?: 0.0
                val r = item.rate.toDoubleOrNull() ?: 0.0
                val gst = item.gstRate.toDoubleOrNull() ?: 0.0
                (qty * r) * (gst / 100.0)
            }
        }
    }

    val grandTotal = subtotal + taxOrAdditional

    fun handleSave() {
        if (grandTotal <= 0.0) {
            validationError = thogaiRequiredMsg
            ElvanSnackbar.show(thogaiRequiredMsg)
            return
        }

        val finalPeyarMap = if (selectedVaangunarPeyarMap.isNotEmpty()) {
            selectedVaangunarPeyarMap
        } else {
            mapOf("ta" to "பொது வாடிக்கையாளர்", "en" to "General Customer")
        }

        val setharam = setharamGrams.toDoubleOrNull() ?: 0.0
        val courier = courierCharges.toDoubleOrNull() ?: 0.0
        val ahimsa = ahimsaCharges.toDoubleOrNull() ?: 0.0

        val invoiceToSave = PattiyalTharavuru(
            id = invoice?.id ?: 0L,
            niruvanamId = selectedNiruvanamId,
            vaangunarId = selectedVaangunarId,
            vaangunarPeyar = finalPeyarMap,
            vaangunarMunvari = selectedVaangunarMunvariMap,
            patrucheettuEn = invoiceNumber.trim(),
            pattiyalNaal = invoiceDate,
            mothaThogai = grandTotal,
            setharamGrams = setharam,
            thabaalThogai = courier,
            ahimsaPattuThogai = ahimsa,
            ullkurippu = "",
            createdAt = invoice?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val savedId = PattiyalRepository.save(invoiceToSave, currentMode)
        if (savedId > 0L) {
            ElvanSnackbar.show(saveSuccessMsg)
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

            // Section 1: Customer Selection (பெறுநர்)
            item(key = "customer_section") {
                ElvanEditorSection(
                    index = 0,
                    title = K.customer.tr()
                ) {
                    val hasCustomer = selectedVaangunarId != null || selectedVaangunarPeyarMap.isNotEmpty()

                    if (hasCustomer) {
                        // Selected Customer Card with Clear button
                        val customerName = selectedVaangunarPeyarMap.values.firstOrNull()?.ifEmpty { null }
                            ?: K.customer.tr()
                        val customerAddress = selectedVaangunarMunvariMap.values.firstOrNull()?.ifEmpty { null }

                        ElvanThiruthiAttai {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = K.savedDetails.tr().preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = customerName.preventBrokenLigatures(),
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                    )
                                    if (!customerAddress.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = customerAddress.preventBrokenLigatures(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 13.sp,
                                                color = colors.textSecondary
                                            )
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.iconBg)
                                        .clickable {
                                            selectedVaangunarId = null
                                            selectedVaangunarPeyarMap = emptyMap()
                                            selectedVaangunarMunvariMap = emptyMap()
                                            customerSearchQuery = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Close,
                                        contentDescription = K.cancelBtn.tr(),
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Inline Customer Search & Picker
                        ElvanThiruthiAttai {
                            ElvanThiruthiUlleedu(
                                label = K.clientNameSearch.tr(),
                                value = customerSearchQuery,
                                onValueChange = {
                                    customerSearchQuery = it
                                    isCustomerDropdownOpen = it.isNotBlank()
                                },
                                placeholder = K.clientNameSearch.tr(),
                                prefixIcon = {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Search,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                            // Matching merchants dropdown / list
                            val filteredMerchants = if (customerSearchQuery.isBlank()) {
                                merchants.take(4)
                            } else {
                                merchants.filter { m ->
                                    m.peyar.values.any { it.contains(customerSearchQuery, ignoreCase = true) } ||
                                    m.oor.values.any { it.contains(customerSearchQuery, ignoreCase = true) }
                                }.take(6)
                            }

                            if (filteredMerchants.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    filteredMerchants.forEach { m ->
                                        val mName = m.peyar.values.firstOrNull().orEmpty()
                                        val mOor = m.oor.values.firstOrNull().orEmpty()

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(colors.iconBg)
                                                .clickable {
                                                    selectedVaangunarId = m.id
                                                    selectedVaangunarPeyarMap = m.peyar
                                                    selectedVaangunarMunvariMap = m.oor
                                                    isCustomerDropdownOpen = false
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = mName.preventBrokenLigatures(),
                                                    style = TextStyle(
                                                        fontFamily = ff,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = colors.textPrimary
                                                    )
                                                )
                                                if (mOor.isNotBlank()) {
                                                    Text(
                                                        text = mOor.preventBrokenLigatures(),
                                                        style = TextStyle(
                                                            fontFamily = ff,
                                                            fontSize = 12.sp,
                                                            color = colors.textSecondary
                                                        )
                                                    )
                                                }
                                            }

                                            Icon(
                                                imageVector = MaterialSymbols.Rounded.ChevronRight,
                                                contentDescription = null,
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Invoice Details (பட்டியல் விவரங்கள்)
            item(key = "metadata_section") {
                ElvanEditorSection(
                    index = 1,
                    title = K.invoiceDetails.tr()
                ) {
                    ElvanThiruthiAttai {
                        // Profile Selector (if multiple profiles exist)
                        if (profiles.size > 1) {
                            val activeProfile = profiles.find { it.id == selectedNiruvanamId }
                            val profileDisplayName = activeProfile?.kurumPeyar?.ifEmpty {
                                activeProfile.niruvanathinPeyar.values.firstOrNull().orEmpty()
                            } ?: K.selectCompany.tr()

                            ElvanThiruthiKeezhvirivu(
                                label = K.company.tr(),
                                selectedText = profileDisplayName,
                                items = profiles.map { p ->
                                    val name = p.kurumPeyar.ifEmpty { p.niruvanathinPeyar.values.firstOrNull().orEmpty() }
                                    p.id.toString() to name
                                },
                                onSelected = { selectedNiruvanamId = it.toLongOrNull() }
                            )
                        }

                        // Invoice Number
                        ElvanThiruthiUlleedu(
                            label = K.english.tr(),
                            value = invoiceNumber,
                            onValueChange = { invoiceNumber = it },
                            placeholder = "INV-001"
                        )

                        // Invoice Date
                        ElvanThiruthiUlleedu(
                            label = K.dateOfBirth.tr(),
                            value = DateUtils.formatEpochMillis(invoiceDate),
                            onValueChange = {},
                            enabled = false,
                            suffixIcon = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        // In Silk mode: Place of Supply
                        if (currentMode == AppMode.PATTU) {
                            ElvanThiruthiUlleedu(
                                label = "வழங்கல் இடம் (Place of Supply)",
                                value = placeOfSupply,
                                onValueChange = { placeOfSupply = it },
                                placeholder = "Tamil Nadu"
                            )
                        }
                    }
                }
            }

            // Section 3: Line Items (உருப்படிகள்)
            item(key = "line_items_section") {
                ElvanEditorSection(
                    index = 2,
                    title = K.products.tr()
                ) {
                    lineItems.forEachIndexed { itemIndex, lineItem ->
                        ElvanThiruthiAttai(
                            borderRadius = 20.dp,
                            backgroundColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF9F9FA)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${K.product.tr()} #${itemIndex + 1}",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary
                                    )
                                )

                                if (lineItems.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                            .clickable {
                                                lineItems = lineItems.toMutableList().also { it.removeAt(itemIndex) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = MaterialSymbols.Rounded.Delete,
                                            contentDescription = K.deleteBtn.tr(),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Item Name Input
                            ElvanThiruthiUlleedu(
                                label = K.product.tr(),
                                value = lineItem.name,
                                onValueChange = { newName ->
                                    lineItems = lineItems.toMutableList().also {
                                        it[itemIndex] = it[itemIndex].copy(name = newName)
                                    }
                                },
                                placeholder = K.product.tr()
                            )

                            // Row: Quantity/Weight + Rate
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ElvanThiruthiUlleedu(
                                        label = if (currentMode == AppMode.KOOLI) "${K.weight.tr()} (kg)" else K.quantity.tr(),
                                        value = lineItem.quantityOrWeight,
                                        onValueChange = { newQty ->
                                            lineItems = lineItems.toMutableList().also {
                                                it[itemIndex] = it[itemIndex].copy(quantityOrWeight = newQty)
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    ElvanThiruthiUlleedu(
                                        label = K.sellingRate.tr(),
                                        value = lineItem.rate,
                                        onValueChange = { newRate ->
                                            lineItems = lineItems.toMutableList().also {
                                                it[itemIndex] = it[itemIndex].copy(rate = newRate)
                                            }
                                        },
                                        prefixText = "₹ ",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }
                            }

                            // In Silk Mode: HSN + GST %
                            if (currentMode == AppMode.PATTU) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ElvanThiruthiUlleedu(
                                            label = K.hsnSacCode.tr(),
                                            value = lineItem.hsn,
                                            onValueChange = { newHsn ->
                                                lineItems = lineItems.toMutableList().also {
                                                    it[itemIndex] = it[itemIndex].copy(hsn = newHsn)
                                                }
                                            },
                                            placeholder = "50020010",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }

                                    Box(modifier = Modifier.weight(1f)) {
                                        ElvanThiruthiUlleedu(
                                            label = K.gstRate.tr(),
                                            value = lineItem.gstRate,
                                            onValueChange = { newGst ->
                                                lineItems = lineItems.toMutableList().also {
                                                    it[itemIndex] = it[itemIndex].copy(gstRate = newGst)
                                                }
                                            },
                                            suffixText = "%",
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add Line Item Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.accent.copy(alpha = 0.12f))
                            .clickable {
                                lineItems = lineItems.toMutableList().also {
                                    it.add(
                                        InvoiceLineItem(
                                            name = if (currentMode == AppMode.KOOLI) "கூலி நெசவு" else "பட்டு புடவை",
                                            quantityOrWeight = "1",
                                            rate = "0"
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.Add,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${K.product.tr()} ${K.add.tr()}".preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accent
                                )
                            )
                        }
                    }
                }
            }

            // Section 4: Additional Charges & Totals (கூடுதல் கட்டணங்கள் / மொத்தங்கள்)
            item(key = "totals_section") {
                ElvanEditorSection(
                    index = 3,
                    title = if (currentMode == AppMode.KOOLI) "கூடுதல் கட்டணங்கள் மற்றும் மொத்தம்" else K.totals.tr()
                ) {
                    ElvanThiruthiAttai {
                        if (currentMode == AppMode.KOOLI) {
                            ElvanThiruthiUlleedu(
                                label = "${K.setharam.tr()} (Grams)",
                                value = setharamGrams,
                                onValueChange = { setharamGrams = it },
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            ElvanThiruthiUlleedu(
                                label = K.courierCharge.tr(),
                                value = courierCharges,
                                onValueChange = { courierCharges = it },
                                placeholder = "0.00",
                                prefixText = "₹ ",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            ElvanThiruthiUlleedu(
                                label = "அகிம்சா பட்டு",
                                value = ahimsaCharges,
                                onValueChange = { ahimsaCharges = it },
                                placeholder = "0.00",
                                prefixText = "₹ ",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        // Divider
                        HorizontalDivider(
                            color = colors.divider,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Subtotal Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "உருப்படிகள் மொத்தம் (Subtotal)",
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    color = colors.textSecondary
                                )
                            )
                            Text(
                                text = CurrencyUtils.formatInr(subtotal),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                        }

                        // Tax / Additional Row
                        if (taxOrAdditional > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentMode == AppMode.KOOLI) "கூடுதல் கட்டணங்கள்" else "வரி (GST)",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 14.sp,
                                        color = colors.textSecondary
                                    )
                                )
                                Text(
                                    text = "+ ${CurrencyUtils.formatInr(taxOrAdditional)}",
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                )
                            }
                        }

                        // Grand Total Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = K.total.tr(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                            Text(
                                text = CurrencyUtils.formatInr(grandTotal),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.accent
                                )
                            )
                        }
                    }
                }
            }

        }
    }
}
}
