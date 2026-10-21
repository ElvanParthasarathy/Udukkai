package com.elvan.udukkai.data.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Type-safe payment mode enum with localized display names.
 * Stored as lowercase string in DB (e.g. 'cash', 'upi', 'bank_transfer', 'cheque', 'card').
 */
enum class PaymentType(
    val storedValue: String,
    val needsReference: Boolean
) {
    BANK_TRANSFER("bank_transfer", true), // Bank Transfer — வங்கி மாற்றம்
    UPI("upi", true),                    // UPI
    CASH("cash", false),                 // Cash — காசு / பணம்
    CHEQUE("cheque", true),              // Cheque — காசோலை
    CARD("card", true);                  // Card — அட்டை

    val icon: ImageVector
        get() = when (this) {
            CASH -> MaterialSymbols.Rounded.Payments
            UPI -> MaterialSymbols.Rounded.QrCode
            BANK_TRANSFER -> MaterialSymbols.Rounded.AccountBalance
            CHEQUE -> MaterialSymbols.Rounded.ReceiptLong
            CARD -> MaterialSymbols.Rounded.CreditCard
        }

    fun labelString(langCode: String = LanguageManager.activeLanguageCode): String {
        return when (this) {
            CASH -> K.cash.trWithLang(langCode)
            UPI -> "UPI"
            BANK_TRANSFER -> K.bankTransfer.trWithLang(langCode)
            CHEQUE -> K.cheque.trWithLang(langCode)
            CARD -> K.card.trWithLang(langCode)
        }
    }

    @Composable
    fun label(): String = labelString()

    fun badgeColor(isDark: Boolean): Color {
        return if (isDark) Color.White else Color.Black
    }

    companion object {
        val VANGI_MAATRAM get() = BANK_TRANSFER
        val PANAM get() = CASH
        val KAASOALAI get() = CHEQUE
        val ATTAI get() = CARD

        fun fromStored(value: String?): PaymentType {
            if (value.isNullOrBlank()) return CASH
            return when (value.trim().lowercase()) {
                "bank_transfer", "vangimaatram", "vangi_maatram" -> BANK_TRANSFER
                "upi" -> UPI
                "cash", "panam", "roakkam" -> CASH
                "cheque", "kaasoalai" -> CHEQUE
                "card", "attai" -> CARD
                else -> CASH
            }
        }

        fun allOptions(langCode: String = LanguageManager.activeLanguageCode): List<Pair<String, String>> = listOf(
            CASH.name to CASH.labelString(langCode),
            UPI.name to UPI.labelString(langCode),
            BANK_TRANSFER.name to BANK_TRANSFER.labelString(langCode),
            CHEQUE.name to CHEQUE.labelString(langCode),
            CARD.name to CARD.labelString(langCode)
        )
    }
}

