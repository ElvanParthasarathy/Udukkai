package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.PaymentAccount
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Bank Settings Screen supporting Multiple Bank Accounts per Company.
 * Ported from React Payanpadu.ts payment account architecture.
 */
@Composable
fun BankSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    profile: BusinessProfile,
    onUpdateProfile: (BusinessProfile) -> Unit
) {
    val ff = LocalAppFontFamily.current
    val isBilingual = profile.isBilingual
    val primaryLang = profile.primaryLanguage
    val secondaryLang = profile.secondaryLanguage
    val profileSavedMsg = K.profileSaved.tr()
    val minAccountMsg = "At least one account is required."

    val primaryLangLabel = if (primaryLang.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()
    val secondaryLangLabel = if (secondaryLang.lowercase().startsWith("ta")) K.taCode.tr() else K.enCode.tr()

    val accounts = remember(profile.paymentAccountsJson, profile.accountNumber, profile.ifsc) {
        profile.getPaymentAccounts()
    }

    var editingAccountId by remember { mutableStateOf<String?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    // Temp form states
    var tempLabel by remember { mutableStateOf("") }
    var tempBankPrimary by remember { mutableStateOf("") }
    var tempBankSecondary by remember { mutableStateOf("") }
    var tempBranchPrimary by remember { mutableStateOf("") }
    var tempBranchSecondary by remember { mutableStateOf("") }
    var tempAccNum by remember { mutableStateOf("") }
    var tempIfsc by remember { mutableStateOf("") }
    var tempUpi by remember { mutableStateOf("") }
    var tempIsDefault by remember { mutableStateOf(false) }

    fun openEditor(acc: PaymentAccount?) {
        if (acc != null) {
            editingAccountId = acc.id
            isAddingNew = false
            tempLabel = acc.label
            tempBankPrimary = acc.bankName[primaryLang] ?: acc.bankName["ta"] ?: ""
            tempBankSecondary = acc.bankName[secondaryLang] ?: acc.bankName["en"] ?: ""
            tempBranchPrimary = acc.branch[primaryLang] ?: acc.branch["ta"] ?: ""
            tempBranchSecondary = acc.branch[secondaryLang] ?: acc.branch["en"] ?: ""
            tempAccNum = acc.accountNumber
            tempIfsc = acc.ifsc
            tempUpi = acc.upiId
            tempIsDefault = acc.isDefault
        } else {
            editingAccountId = "new_${System.currentTimeMillis()}"
            isAddingNew = true
            tempLabel = ""
            tempBankPrimary = ""
            tempBankSecondary = ""
            tempBranchPrimary = ""
            tempBranchSecondary = ""
            tempAccNum = ""
            tempIfsc = ""
            tempUpi = ""
            tempIsDefault = accounts.isEmpty()
        }
    }

    fun saveAccount() {
        val id = editingAccountId ?: return
        val currentAccounts = accounts.toMutableList()

        val bnMap = mutableMapOf<String, String>()
        if (tempBankPrimary.isNotBlank()) bnMap[primaryLang] = tempBankPrimary.trim()
        if (isBilingual && tempBankSecondary.isNotBlank()) bnMap[secondaryLang] = tempBankSecondary.trim()

        val brMap = mutableMapOf<String, String>()
        if (tempBranchPrimary.isNotBlank()) brMap[primaryLang] = tempBranchPrimary.trim()
        if (isBilingual && tempBranchSecondary.isNotBlank()) brMap[secondaryLang] = tempBranchSecondary.trim()

        val newAcc = PaymentAccount(
            id = id,
            label = tempLabel.trim().ifEmpty { tempBankPrimary.trim().ifEmpty { "Bank Account" } },
            bankName = bnMap,
            branch = brMap,
            accountNumber = tempAccNum.trim(),
            ifsc = tempIfsc.trim().uppercase(),
            upiId = tempUpi.trim(),
            isDefault = tempIsDefault || currentAccounts.isEmpty(),
            isActive = true
        )

        val existingIdx = currentAccounts.indexOfFirst { it.id == id }
        if (existingIdx >= 0) {
            currentAccounts[existingIdx] = newAcc
        } else {
            currentAccounts.add(newAcc)
        }

        // If this one is set as default, unset default for others
        if (newAcc.isDefault) {
            for (i in currentAccounts.indices) {
                if (currentAccounts[i].id != id) {
                    currentAccounts[i] = currentAccounts[i].copy(isDefault = false)
                }
            }
        } else {
            // Ensure at least one default exists
            if (currentAccounts.none { it.isDefault }) {
                currentAccounts[0] = currentAccounts[0].copy(isDefault = true)
            }
        }

        val updatedProfile = profile.copy()
        updatedProfile.setPaymentAccounts(currentAccounts)
        onUpdateProfile(updatedProfile)

        editingAccountId = null
        isAddingNew = false
        ElvanSnackbar.show(profileSavedMsg)
    }

    fun setDefault(accountId: String) {
        val updated = accounts.map { it.copy(isDefault = it.id == accountId) }
        val updatedProfile = profile.copy()
        updatedProfile.setPaymentAccounts(updated)
        onUpdateProfile(updatedProfile)
        ElvanSnackbar.show(profileSavedMsg)
    }

    fun deleteAccount(accountId: String) {
        if (accounts.size <= 1) {
            ElvanSnackbar.show(minAccountMsg)
            return
        }
        val remaining = accounts.filter { it.id != accountId }.toMutableList()
        if (remaining.none { it.isDefault }) {
            remaining[0] = remaining[0].copy(isDefault = true)
        }
        val updatedProfile = profile.copy()
        updatedProfile.setPaymentAccounts(remaining)
        onUpdateProfile(updatedProfile)
        ElvanSnackbar.show(profileSavedMsg)
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // Editor Container if editing or adding
        if (editingAccountId != null) {
            item(key = "account_editor") {
                ElvanSettingsEditContainer(
                    title = if (isAddingNew) K.addBankAccount.tr() else K.editBankAccount.tr(),
                    onCancel = {
                        editingAccountId = null
                        isAddingNew = false
                    },
                    onSave = { saveAccount() },
                    colors = colors
                ) {
                    // Account Label
                    ElvanSettingsTextField(
                        label = K.accountLabel.tr(),
                        value = tempLabel,
                        onValueChange = { tempLabel = it },
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bank Name
                    ElvanSettingsTextField(
                        label = "${K.bankName.tr()} ($primaryLangLabel)",
                        value = tempBankPrimary,
                        onValueChange = { tempBankPrimary = it },
                        colors = colors
                    )
                    if (isBilingual) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ElvanSettingsTextField(
                            label = "${K.bankName.tr()} ($secondaryLangLabel)",
                            value = tempBankSecondary,
                            onValueChange = { tempBankSecondary = it },
                            colors = colors
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Account Number
                    ElvanSettingsTextField(
                        label = K.accountNumber.tr(),
                        value = tempAccNum,
                        onValueChange = { tempAccNum = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // IFSC
                    ElvanSettingsTextField(
                        label = K.ifscCode.tr(),
                        value = tempIfsc,
                        onValueChange = { tempIfsc = it.uppercase() },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Branch Name
                    ElvanSettingsTextField(
                        label = "${K.branchName.tr()} ($primaryLangLabel)",
                        value = tempBranchPrimary,
                        onValueChange = { tempBranchPrimary = it },
                        colors = colors
                    )
                    if (isBilingual) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ElvanSettingsTextField(
                            label = "${K.branchName.tr()} ($secondaryLangLabel)",
                            value = tempBranchSecondary,
                            onValueChange = { tempBranchSecondary = it },
                            colors = colors
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // UPI ID
                    ElvanSettingsTextField(
                        label = K.upiId.tr(),
                        value = tempUpi,
                        onValueChange = { tempUpi = it },
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Set as default
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tempIsDefault = !tempIsDefault },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = tempIsDefault,
                            onCheckedChange = { tempIsDefault = it },
                            colors = CheckboxDefaults.colors(checkedColor = colors.accent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = K.defaultAccount.tr().preventBrokenLigatures(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        )
                    }
                }
            }
        }

        // List of configured accounts
        items(
            count = accounts.size,
            key = { accounts[it].id.ifEmpty { "acc_$it" } }
        ) { index ->
            val acc = accounts[index]
            val isBeingEdited = editingAccountId == acc.id

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(
                        width = if (acc.isDefault) 1.5.dp else 0.5.dp,
                        color = if (acc.isDefault) colors.accent else colors.floatingBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = acc.label.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                            if (acc.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.accent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "★ ${K.defaultAccount.tr()}",
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.accent
                                        )
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!acc.isDefault) {
                                TextButton(
                                    onClick = { setDefault(acc.id) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = K.setAsDefault.tr(),
                                        style = TextStyle(fontFamily = ff, fontSize = 12.sp, color = colors.accent)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { openEditor(acc) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Edit,
                                    contentDescription = K.editToolbarBtn.tr(),
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (accounts.size > 1) {
                                IconButton(
                                    onClick = { deleteAccount(acc.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Delete,
                                        contentDescription = K.deleteBtn.tr(),
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val bankName = acc.bankName[primaryLang] ?: acc.bankName["ta"] ?: acc.bankName.values.firstOrNull() ?: ""
                    if (bankName.isNotBlank()) {
                        Text(
                            text = bankName.preventBrokenLigatures(),
                            style = TextStyle(fontFamily = ff, fontSize = 13.5.sp, color = colors.textSecondary)
                        )
                    }

                    if (acc.accountNumber.isNotBlank()) {
                        val masked = if (acc.accountNumber.length > 4) "•••• " + acc.accountNumber.takeLast(4) else acc.accountNumber
                        Text(
                            text = "${K.accountNumber.tr()}: $masked",
                            style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary)
                        )
                    }

                    if (acc.ifsc.isNotBlank()) {
                        Text(
                            text = "IFSC: ${acc.ifsc}",
                            style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textSecondary)
                        )
                    }

                    if (acc.upiId.isNotBlank()) {
                        Text(
                            text = "UPI: ${acc.upiId}",
                            style = TextStyle(fontFamily = ff, fontSize = 12.5.sp, color = colors.textSecondary)
                        )
                    }
                }
            }
        }

        // Add New Account Button
        item(key = "add_account_btn") {
            Button(
                onClick = { openEditor(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = K.addBankAccount.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
