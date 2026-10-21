package com.elvan.udukkai.ui.screens.editor.invoice.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.settings.PaymentAccount
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.editor.ElvanEditorSection

/**
 * Bill-level bank account selector dropdown ported from React Payanpadu.ts.
 * Defaults to company's default account, but allows picking any registered account for this bill.
 */
@Composable
fun InvoiceBankAccountPicker(
    accounts: List<PaymentAccount>,
    selectedAccountId: String?,
    onAccountSelected: (PaymentAccount) -> Unit,
    index: Int = 6,
    modifier: Modifier = Modifier
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    var isSheetOpen by remember { mutableStateOf(false) }

    val activeAccounts = remember(accounts) { accounts.filter { it.isActive } }
    if (activeAccounts.isEmpty()) return

    val currentAccount = remember(activeAccounts, selectedAccountId) {
        activeAccounts.find { it.id == selectedAccountId }
            ?: activeAccounts.find { it.isDefault }
            ?: activeAccounts.firstOrNull()
    }

    val displayTitle = currentAccount?.let { acc ->
        val masked = if (acc.accountNumber.length > 4) " (••••${acc.accountNumber.takeLast(4)})" else ""
        "${acc.label}$masked"
    } ?: K.bankAccount.tr()

    ElvanEditorSection(
        index = index,
        title = K.bankAccount.tr(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(colors.iconBg)
                .clickable {
                    if (activeAccounts.size > 1) {
                        isSheetOpen = true
                    }
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = displayTitle.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    )
                }
                if (activeAccounts.size > 1) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isSheetOpen) {
            ElvanSelectionBottomSheet(
                title = K.bankAccount.tr(),
                items = activeAccounts,
                currentValue = currentAccount,
                onSelected = { acc ->
                    onAccountSelected(acc)
                    isSheetOpen = false
                },
                onDismissRequest = { isSheetOpen = false },
                itemLabelBuilder = { acc ->
                    val masked = if (acc.accountNumber.length > 4) " (••••${acc.accountNumber.takeLast(4)})" else ""
                    "${acc.label}$masked"
                },
                subtitleBuilder = { acc ->
                    val bn = acc.bankName["en"] ?: acc.bankName.values.firstOrNull() ?: ""
                    val br = acc.branch["en"] ?: acc.branch.values.firstOrNull() ?: ""
                    listOf(bn, br).filter { it.isNotEmpty() }.joinToString(" • ")
                }
            )
        }
    }
}
