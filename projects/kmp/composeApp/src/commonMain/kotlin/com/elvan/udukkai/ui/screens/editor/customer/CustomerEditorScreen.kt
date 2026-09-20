package com.elvan.udukkai.ui.screens.editor.customer

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.data.IdangalinPeyar
import com.elvan.udukkai.core.data.indhiyaMaanilangal
import com.elvan.udukkai.core.data.tamizhnaattuMaavattangal
import com.elvan.udukkai.core.data.ulagaNaadugal
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.data.model.VaangunarTharavuru
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection
import com.elvan.udukkai.ui.screens.editor.ElvanIrumozhiPulan
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiAttai
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiKeezhvirivu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiThalaippu
import com.elvan.udukkai.ui.screens.editor.ElvanThiruthiUlleedu
import com.elvan.udukkai.ui.screens.editor.LocalEditorAccentColor

/**
 * CustomerEditorScreen — Customer / Merchant Editor.
 * 100% feature parity with Flutter's `SilkMerchantEditor` and `CoolieMerchantEditor`.
 *
 * Features:
 * - Bilingual Name input (Tamil + English)
 * - Country picker bottom sheet (`ulagaNaadugal`)
 * - India vs Foreign address branching:
 *   - If Country != India: Single multiline foreign address (`velinaadMugavari`)
 *   - If Country == India:
 *     - State picker bottom sheet (`indhiyaMaanilangal`)
 *     - District picker:
 *       - If State is Tamil Nadu: District picker bottom sheet (`tamizhnaattuMaavattangal`)
 *       - Otherwise: Free-text bilingual input
 *     - Town / City (`oor`)
 *     - Street address (`mugavari`)
 *     - PIN code (`anjalKuriyeedu`, 6 digits)
 * - Silk mode: Contact & Tax (Phone, Email, GSTIN with 15-char regex validation)
 * - Coolie mode: Simplified to Name + Town + Street only
 * - Soft delete confirmation dialog
 */
@Composable
fun CustomerEditorScreen(
    merchant: VaangunarTharavuru? = null,
    onBack: () -> Unit
) {
    val currentMode = LocalAppMode.current
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val isEditing = merchant != null && merchant.id > 0L

    var peyarMap by remember {
        mutableStateOf(merchant?.peyar ?: emptyMap())
    }
    var oorMap by remember {
        mutableStateOf(merchant?.oor ?: emptyMap())
    }
    var mugavariMap by remember {
        mutableStateOf(merchant?.mugavari ?: emptyMap())
    }
    var maavattamMap by remember {
        mutableStateOf(merchant?.maavattam ?: emptyMap())
    }
    var maanilamMap by remember {
        mutableStateOf(merchant?.maanilam ?: mapOf("en" to "Tamil Nadu", "ta" to "தமிழ்நாடு"))
    }
    var naaduMap by remember {
        mutableStateOf(merchant?.naadu ?: mapOf("en" to "India", "ta" to "இந்தியா"))
    }
    var velinaadMugavariMap by remember {
        mutableStateOf(merchant?.velinaadMugavari ?: emptyMap())
    }

    var anjalKuriyeedu by remember { mutableStateOf(merchant?.anjalKuriyeedu ?: "") }
    var gstin by remember { mutableStateOf(merchant?.gstin ?: "") }
    var tholaipaesi by remember { mutableStateOf(merchant?.tholaipaesi ?: "") }
    var minnanjal by remember { mutableStateOf(merchant?.minnanjal ?: "") }

    var nameValidationError by remember { mutableStateOf<String?>(null) }
    var gstinValidationError by remember { mutableStateOf<String?>(null) }

    val pageTitle = if (isEditing) K.editRecord.tr() else K.createRecord.tr()
    val nameRequiredMsg = K.customerNameRequired.tr()
    val gstinErrorMsg = K.invalidGstinFormat.tr()
    val savedMsg = K.customerSavedSuccessfully.tr()
    val saveFailedMsg = K.couldNotSavePrefix.tr()

    // India vs Overseas address check
    val isIndia = remember(naaduMap) {
        val en = naaduMap["en"]?.trim()?.lowercase() ?: ""
        val ta = naaduMap["ta"]?.trim() ?: ""
        naaduMap.isEmpty() || en == "india" || ta == "இந்தியா"
    }

    // Tamil Nadu check for district picker
    val isTamilNadu = remember(maanilamMap) {
        val en = maanilamMap["en"]?.trim()?.lowercase() ?: ""
        val ta = maanilamMap["ta"]?.trim() ?: ""
        en == "tamil nadu" || ta == "தமிழ்நாடு"
    }

    fun validateGstin(value: String): Boolean {
        if (value.isBlank()) return true
        val upper = value.trim().uppercase()
        if (upper.length != 15) return false
        val gstinRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
        return gstinRegex.matches(upper)
    }

    fun handleSave() {
        if (peyarMap.values.none { it.isNotBlank() }) {
            nameValidationError = nameRequiredMsg
            ElvanSnackbar.show(nameRequiredMsg)
            return
        }

        if (currentMode == AppMode.PATTU && gstin.isNotBlank()) {
            if (!validateGstin(gstin)) {
                gstinValidationError = gstinErrorMsg
                ElvanSnackbar.show(gstinErrorMsg)
                return
            }
        }

        val merchantToSave = if (currentMode == AppMode.KOOLI) {
            // Coolie mode: simplified to Name, Town, Street only
            VaangunarTharavuru(
                id = merchant?.id ?: 0L,
                peyar = peyarMap.filterValues { it.isNotBlank() },
                oor = oorMap.filterValues { it.isNotBlank() },
                mugavari = mugavariMap.filterValues { it.isNotBlank() },
                maavattam = emptyMap(),
                maanilam = emptyMap(),
                naadu = emptyMap(),
                velinaadMugavari = emptyMap(),
                anjalKuriyeedu = "",
                gstin = "",
                minnanjal = "",
                tholaipaesi = "",
                isDeleted = false,
                createdAt = merchant?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            // Silk mode: full domestic or foreign address + contact & tax
            VaangunarTharavuru(
                id = merchant?.id ?: 0L,
                peyar = peyarMap.filterValues { it.isNotBlank() },
                oor = if (isIndia) oorMap.filterValues { it.isNotBlank() } else emptyMap(),
                mugavari = if (isIndia) mugavariMap.filterValues { it.isNotBlank() } else emptyMap(),
                maavattam = if (isIndia) maavattamMap.filterValues { it.isNotBlank() } else emptyMap(),
                maanilam = if (isIndia) maanilamMap.filterValues { it.isNotBlank() } else emptyMap(),
                naadu = naaduMap.filterValues { it.isNotBlank() },
                velinaadMugavari = if (!isIndia) velinaadMugavariMap.filterValues { it.isNotBlank() } else emptyMap(),
                anjalKuriyeedu = if (isIndia) anjalKuriyeedu.trim() else "",
                gstin = gstin.trim().uppercase(),
                minnanjal = minnanjal.trim(),
                tholaipaesi = tholaipaesi.trim(),
                isDeleted = false,
                createdAt = merchant?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        val savedId = VaangunarRepository.save(merchantToSave, currentMode)
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
    val pillBg = if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.White

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
        CompositionLocalProvider(LocalEditorAccentColor provides colors.customerColor) {
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

            // Section 1: Customer Details (வணிகர் தரவுகள்)
            item(key = "merchant_details_section") {
                ElvanEditorSection(
                    index = 0,
                    title = K.businessDetails.tr()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ElvanIrumozhiPulan(
                            label = K.customerName.tr(),
                            value = peyarMap,
                            onChanged = {
                                peyarMap = it
                                nameValidationError = null
                            },
                            placeholder = K.customerName.tr()
                        )

                        if (nameValidationError != null) {
                            Text(
                                text = nameValidationError!!,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            // Section 2: Address (முகவரி)
            item(key = "address_section") {
                ElvanEditorSection(
                    index = 1,
                    title = K.address.tr()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentMode == AppMode.PATTU) {
                            // Country Selector Pill
                            ElvanThiruthiKeezhvirivu<IdangalinPeyar>(
                                label = K.country.tr(),
                                value = ulagaNaadugal.firstOrNull { it.en.equals(naaduMap["en"], ignoreCase = true) || it.ta == naaduMap["ta"] } ?: ulagaNaadugal.first(),
                                items = ulagaNaadugal,
                                onSelected = { picked ->
                                    naaduMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                },
                                itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                showSearch = true,
                                searchFilter = { item, query ->
                                    item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                }
                            )

                            if (isIndia) {
                                // State Selector Pill
                                ElvanThiruthiKeezhvirivu<IdangalinPeyar>(
                                    label = K.state.tr(),
                                    value = indhiyaMaanilangal.firstOrNull { it.en.equals(maanilamMap["en"], ignoreCase = true) || it.ta == maanilamMap["ta"] },
                                    items = indhiyaMaanilangal,
                                    onSelected = { picked ->
                                        maanilamMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                        if (picked.en != "Tamil Nadu" && picked.ta != "தமிழ்நாடு") {
                                            maavattamMap = emptyMap()
                                        }
                                    },
                                    itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                    subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                    showSearch = true,
                                    searchFilter = { item, query ->
                                        item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                    }
                                )

                                // District: Autocomplete if TN, freeform otherwise
                                if (isTamilNadu) {
                                    ElvanThiruthiKeezhvirivu<IdangalinPeyar>(
                                        label = K.district.tr(),
                                        value = tamizhnaattuMaavattangal.firstOrNull { it.en.equals(maavattamMap["en"], ignoreCase = true) || it.ta == maavattamMap["ta"] },
                                        items = tamizhnaattuMaavattangal,
                                        onSelected = { picked ->
                                            maavattamMap = mapOf("en" to picked.en, "ta" to picked.ta)
                                        },
                                        onClear = { maavattamMap = emptyMap() },
                                        itemLabelBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.ta else it.en },
                                        subtitleBuilder = { if (LanguageManager.activeLanguageCode == "ta") it.en else it.ta },
                                        showSearch = true,
                                        searchFilter = { item, query ->
                                            item.en.contains(query, ignoreCase = true) || item.ta.contains(query, ignoreCase = true)
                                        }
                                    )
                                } else {
                                    ElvanIrumozhiPulan(
                                        label = K.district.tr(),
                                        value = maavattamMap,
                                        onChanged = { maavattamMap = it },
                                        placeholder = K.district.tr()
                                    )
                                }

                                // Town / City
                                ElvanIrumozhiPulan(
                                    label = K.city.tr(),
                                    value = oorMap,
                                    onChanged = { oorMap = it },
                                    placeholder = K.city.tr()
                                )

                                // Street Address
                                ElvanIrumozhiPulan(
                                    label = K.address.tr(),
                                    value = mugavariMap,
                                    onChanged = { mugavariMap = it },
                                    placeholder = K.address.tr(),
                                    maxLines = 2
                                )

                                // PIN Code
                                ElvanThiruthiUlleedu(
                                    label = K.pincode.tr(),
                                    value = anjalKuriyeedu,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                            anjalKuriyeedu = it
                                        }
                                    },
                                    placeholder = "631501",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            } else {
                                // Overseas Address: Single multiline bilingual text field
                                ElvanIrumozhiPulan(
                                    label = K.fullAddress.tr(),
                                    value = velinaadMugavariMap,
                                    onChanged = { velinaadMugavariMap = it },
                                    placeholder = K.fullAddress.tr(),
                                    maxLines = 4
                                )
                            }
                        } else {
                            // Coolie Mode: only Town and Street Address
                            ElvanIrumozhiPulan(
                                label = K.city.tr(),
                                value = oorMap,
                                onChanged = { oorMap = it },
                                placeholder = K.city.tr()
                            )

                            ElvanIrumozhiPulan(
                                label = K.address.tr(),
                                value = mugavariMap,
                                onChanged = { mugavariMap = it },
                                placeholder = K.address.tr(),
                                maxLines = 4
                            )
                        }
                    }
                }
            }

            // Section 3: Contact & Tax (Silk mode only)
            if (currentMode == AppMode.PATTU) {
                item(key = "contact_tax_section") {
                    ElvanEditorSection(
                        index = 2,
                        title = K.contactAndTax.tr()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ElvanThiruthiUlleedu(
                                label = "GSTIN",
                                value = gstin,
                                onValueChange = {
                                    gstin = it.uppercase()
                                    gstinValidationError = null
                                },
                                placeholder = "33AAAAA0000A1Z5",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                errorMessage = gstinValidationError
                            )

                            ElvanThiruthiUlleedu(
                                label = K.email.tr(),
                                value = minnanjal,
                                onValueChange = { minnanjal = it },
                                placeholder = "merchant@example.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            ElvanThiruthiUlleedu(
                                label = K.telephone.tr(),
                                value = tholaipaesi,
                                onValueChange = { tholaipaesi = it },
                                placeholder = "+91 98765 43210",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                    }
                }
            }

            }
        }
    }
}
