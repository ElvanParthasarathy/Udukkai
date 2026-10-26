package com.elvan.udukkai.data.settings

/**
 * Individual Bank / UPI Payment Account model.
 */
data class PaymentAccount(
    val id: String = "",
    val label: String = "",
    val bankName: Map<String, String> = emptyMap(),
    val branch: Map<String, String> = emptyMap(),
    val accountNumber: String = "",
    val ifsc: String = "",
    val upiId: String = "",
    val isDefault: Boolean = false,
    val isActive: Boolean = true
) {
    fun toJson(): String {
        val sb = StringBuilder("{")
        sb.append("\"id\":\"").append(id.replace("\"", "\\\"")).append("\",")
        sb.append("\"label\":\"").append(label.replace("\"", "\\\"")).append("\",")
        sb.append("\"accountNumber\":\"").append(accountNumber.replace("\"", "\\\"")).append("\",")
        sb.append("\"ifsc\":\"").append(ifsc.replace("\"", "\\\"")).append("\",")
        sb.append("\"upiId\":\"").append(upiId.replace("\"", "\\\"")).append("\",")
        sb.append("\"isDefault\":").append(isDefault).append(",")
        sb.append("\"isActive\":").append(isActive).append(",")
        sb.append("\"bankName\":").append(LanguageJsonConverter.stringify(bankName)).append(",")
        sb.append("\"branch\":").append(LanguageJsonConverter.stringify(branch))
        sb.append("}")
        return sb.toString()
    }

    companion object {
        fun parseList(json: String?): List<PaymentAccount> {
            if (json.isNullOrBlank() || json.trim() == "[]" || json.trim() == "null") return emptyList()
            val list = mutableListOf<PaymentAccount>()
            try {
                var depth = 0
                var start = -1
                var inStr = false
                var escape = false
                for (i in json.indices) {
                    val c = json[i]
                    if (escape) { escape = false; continue }
                    if (c == '\\') { escape = true; continue }
                    if (c == '"') { inStr = !inStr; continue }
                    if (!inStr) {
                        if (c == '{') {
                            if (depth == 0) start = i
                            depth++
                        } else if (c == '}') {
                            depth--
                            if (depth == 0 && start != -1) {
                                val itemStr = json.substring(start, i + 1)
                                parseSingle(itemStr)?.let { list.add(it) }
                                start = -1
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
            return list
        }

        private fun parseSingle(itemStr: String): PaymentAccount? {
            return try {
                val id = Regex(""""id"\s*:\s*"([^"]*)"""").find(itemStr)?.groupValues?.get(1) ?: ""
                val label = Regex(""""label"\s*:\s*"([^"]*)"""").find(itemStr)?.groupValues?.get(1) ?: ""
                val accNum = Regex(""""accountNumber"\s*:\s*"([^"]*)"""").find(itemStr)?.groupValues?.get(1) ?: ""
                val ifsc = Regex(""""ifsc"\s*:\s*"([^"]*)"""").find(itemStr)?.groupValues?.get(1) ?: ""
                val upi = Regex(""""upiId"\s*:\s*"([^"]*)"""").find(itemStr)?.groupValues?.get(1) ?: ""
                val isDef = Regex(""""isDefault"\s*:\s*(true|false)""").find(itemStr)?.groupValues?.get(1)?.toBoolean() ?: false
                val isAct = Regex(""""isActive"\s*:\s*(true|false)""").find(itemStr)?.groupValues?.get(1)?.toBoolean() ?: true

                val bnStr = Regex(""""bankName"\s*:\s*(\{[^}]*\})""").find(itemStr)?.groupValues?.get(1) ?: "{}"
                val brStr = Regex(""""branch"\s*:\s*(\{[^}]*\})""").find(itemStr)?.groupValues?.get(1) ?: "{}"

                PaymentAccount(
                    id = id,
                    label = label,
                    bankName = LanguageJsonConverter.parse(bnStr),
                    branch = LanguageJsonConverter.parse(brStr),
                    accountNumber = accNum,
                    ifsc = ifsc,
                    upiId = upi,
                    isDefault = isDef,
                    isActive = isAct
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Business profile data model.
 * Standardized English property names with backward-compatible Tanglish accessors.
 */
data class BusinessProfile(
    var id: Long? = null,

    // Language Config
    var primaryLanguage: String = "ta",
    var secondaryLanguage: String = "en",
    var isBilingual: Boolean = true,
    var splitGst: Boolean = false,

    // Business Details
    var businessName: MutableMap<String, String> = mutableMapOf(),
    var shortName: String = "",
    var phone1: String = "",
    var phone2: String = "",
    var email: String = "",
    var gstin: String = "",

    // Address
    var address: MutableMap<String, String> = mutableMapOf(),
    var city: MutableMap<String, String> = mutableMapOf(),
    var district: MutableMap<String, String> = mutableMapOf(),
    var state: MutableMap<String, String> = mutableMapOf(),
    var country: MutableMap<String, String> = mutableMapOf(),
    var pinCode: String = "",

    // Legacy Bank Details (mirrored from default PaymentAccount)
    var bankName: MutableMap<String, String> = mutableMapOf(),
    var branch: MutableMap<String, String> = mutableMapOf(),
    var accountNumber: String = "",
    var ifsc: String = "",

    // Branding
    var logo: String = "",
    var wideLogo: String = "",
    var headerLayout: String = "small", // "small" or "wide"
    var signature: String = "",
    var signatoryName: String = "",
    var signatoryFirstName: String = "",
    var signatoryLastName: String = "",

    // Additional
    var tagline: MutableMap<String, String> = mutableMapOf(),
    var upiId: String = "",
    var themeColor: String = "#388e3c",
    var paymentAccountsJson: String = "[]",

    // Print-only settings (React template names): logoHeight, wideLogoX/Y/Scale,
    // invoiceDisplayOptions, receiptLanguage …
    var printSettingsJson: String = "{}"
) {
    val fullSignatoryName: String
        get() {
            val combined = listOf(signatoryFirstName.trim(), signatoryLastName.trim())
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            return combined.ifEmpty { signatoryName.trim() }
        }

    fun getPrimary(fieldName: String): String {
        val map = getBilingualMap(fieldName)
        return map[primaryLanguage] ?: map["ta"] ?: ""
    }

    fun getSecondary(fieldName: String): String {
        val map = getBilingualMap(fieldName)
        return map[secondaryLanguage] ?: map["en"] ?: ""
    }

    fun setBilingual(fieldName: String, language: String, value: String) {
        val map = getBilingualMap(fieldName)
        map[language] = value
    }

    private fun getBilingualMap(fieldName: String): MutableMap<String, String> {
        return when (fieldName) {
            "businessName", "niruvanathinPeyar" -> businessName
            "address", "mugavari" -> address
            "city", "oor" -> city
            "district", "maavattam" -> district
            "state", "maanilam" -> state
            "country", "naadu" -> country
            "bankName", "vangiPeyar" -> bankName
            "branch", "kilai" -> branch
            "tagline", "adaimozhi" -> tagline
            else -> mutableMapOf()
        }
    }

    /**
     * Resolves all configured payment accounts.
     * Backwards compatibility: If [paymentAccountsJson] is empty, synthesizes
     * a single default account from the legacy flat fields.
     */
    fun getPaymentAccounts(): List<PaymentAccount> {
        val accounts = PaymentAccount.parseList(paymentAccountsJson)
        if (accounts.isNotEmpty()) return accounts

        // Synthesize legacy entry if any flat bank/UPI field is set
        val hasLegacy = bankName.isNotEmpty() || accountNumber.isNotEmpty() || ifsc.isNotEmpty() || upiId.isNotEmpty()
        if (hasLegacy) {
            val primaryBank = getPrimary("bankName").ifEmpty { "Default Account" }
            return listOf(
                PaymentAccount(
                    id = "legacy_default",
                    label = primaryBank,
                    bankName = HashMap(bankName),
                    branch = HashMap(branch),
                    accountNumber = accountNumber,
                    ifsc = ifsc,
                    upiId = upiId,
                    isDefault = true,
                    isActive = true
                )
            )
        }
        return emptyList()
    }

    /**
     * Returns the active default payment account, or the first active account.
     */
    fun getDefaultPaymentAccount(): PaymentAccount? {
        val accounts = getPaymentAccounts().filter { it.isActive }
        return accounts.find { it.isDefault } ?: accounts.firstOrNull()
    }

    /**
     * Updates payment accounts and synchronizes the default account's values
     * back to the legacy flat fields.
     */
    fun setPaymentAccounts(accounts: List<PaymentAccount>) {
        val sb = StringBuilder("[")
        accounts.forEachIndexed { i, acc ->
            sb.append(acc.toJson())
            if (i < accounts.size - 1) sb.append(",")
        }
        sb.append("]")
        paymentAccountsJson = sb.toString()

        val def = accounts.find { it.isDefault } ?: accounts.firstOrNull()
        if (def != null) {
            bankName = HashMap(def.bankName)
            branch = HashMap(def.branch)
            accountNumber = def.accountNumber
            ifsc = def.ifsc
            upiId = def.upiId
        }
    }

    fun copy(): BusinessProfile {
        return BusinessProfile(
            id = id,
            primaryLanguage = primaryLanguage,
            secondaryLanguage = secondaryLanguage,
            isBilingual = isBilingual,
            splitGst = splitGst,
            businessName = HashMap(businessName),
            shortName = shortName,
            phone1 = phone1,
            phone2 = phone2,
            email = email,
            gstin = gstin,
            address = HashMap(address),
            city = HashMap(city),
            district = HashMap(district),
            state = HashMap(state),
            country = HashMap(country),
            pinCode = pinCode,
            bankName = HashMap(bankName),
            branch = HashMap(branch),
            accountNumber = accountNumber,
            ifsc = ifsc,
            logo = logo,
            wideLogo = wideLogo,
            headerLayout = headerLayout,
            signature = signature,
            signatoryName = signatoryName,
            signatoryFirstName = signatoryFirstName,
            signatoryLastName = signatoryLastName,
            tagline = HashMap(tagline),
            upiId = upiId,
            themeColor = themeColor,
            paymentAccountsJson = paymentAccountsJson,
            printSettingsJson = printSettingsJson
        )
    }
}

typealias CompanyProfile = BusinessProfile
