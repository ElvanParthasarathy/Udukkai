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
enum class SeluthiVagai(
    val storedValue: String,
    val needsReference: Boolean
) {
    VANGI_MAATRAM("bank_transfer", true), // Bank Transfer — வங்கி மாற்றம்
    UPI("upi", true),                    // UPI
    PANAM("cash", false),                // Cash — காசு / பணம்
    KAASOALAI("cheque", true),           // Cheque — காசோலை
    ATTAI("card", true);                 // Card — அட்டை

    val icon: ImageVector
        get() = when (this) {
            PANAM -> MaterialSymbols.Rounded.Payments
            UPI -> MaterialSymbols.Rounded.QrCode
            VANGI_MAATRAM -> MaterialSymbols.Rounded.AccountBalance
            KAASOALAI -> MaterialSymbols.Rounded.ReceiptLong
            ATTAI -> MaterialSymbols.Rounded.CreditCard
        }

    fun labelString(langCode: String = LanguageManager.activeLanguageCode): String {
        return when (this) {
            PANAM -> K.cash.trWithLang(langCode)
            UPI -> "UPI"
            VANGI_MAATRAM -> K.bankTransfer.trWithLang(langCode)
            KAASOALAI -> K.cheque.trWithLang(langCode)
            ATTAI -> K.card.trWithLang(langCode)
        }
    }

    @Composable
    fun label(): String = labelString()

    fun badgeColor(isDark: Boolean): Color {
        return when (this) {
            PANAM -> if (isDark) Color(0xFF81C784) else Color(0xFF388E3C)
            UPI -> if (isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2)
            VANGI_MAATRAM -> if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
            KAASOALAI -> if (isDark) Color(0xFFFFCC80) else Color(0xFFF57C00)
            ATTAI -> if (isDark) Color(0xFF80CBC4) else Color(0xFF00796B)
        }
    }

    companion object {
        fun fromStored(value: String?): SeluthiVagai {
            if (value.isNullOrBlank()) return PANAM
            return when (value.trim().lowercase()) {
                "bank_transfer", "vangimaatram", "vangi_maatram" -> VANGI_MAATRAM
                "upi" -> UPI
                "cash", "panam", "roakkam" -> PANAM
                "cheque", "kaasoalai" -> KAASOALAI
                "card", "attai" -> ATTAI
                else -> PANAM
            }
        }

        fun allOptions(langCode: String = LanguageManager.activeLanguageCode): List<Pair<String, String>> = listOf(
            PANAM.name to PANAM.labelString(langCode),
            UPI.name to UPI.labelString(langCode),
            VANGI_MAATRAM.name to VANGI_MAATRAM.labelString(langCode),
            KAASOALAI.name to KAASOALAI.labelString(langCode),
            ATTAI.name to ATTAI.labelString(langCode)
        )
    }
}
