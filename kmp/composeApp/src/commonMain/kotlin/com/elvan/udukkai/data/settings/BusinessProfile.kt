package com.elvan.udukkai.data.settings

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
    var country: MutableMap<String, String> = mutableMapOf("en" to "India", "ta" to "இந்தியா"),
    var pinCode: String = "",

    // Bank Details
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
    var themeColor: String = "#388e3c"
) {
    val fullSignatoryName: String
        get() {
            val combined = listOf(signatoryFirstName.trim(), signatoryLastName.trim())
                .filter { it.isNotEmpty() }
                .joinToString(" ")
            return combined.ifEmpty { signatoryName.trim() }
        }

    fun getPrimary(fieldName: String): String {
        if (fieldName == "naadu" || fieldName == "country") return if (primaryLanguage.lowercase().startsWith("ta")) "இந்தியா" else "India"
        val map = getBilingualMap(fieldName)
        return map[primaryLanguage] ?: map["ta"] ?: ""
    }

    fun getSecondary(fieldName: String): String {
        if (fieldName == "naadu" || fieldName == "country") return if (secondaryLanguage.lowercase().startsWith("ta")) "இந்தியா" else "India"
        val map = getBilingualMap(fieldName)
        return map[secondaryLanguage] ?: map["en"] ?: ""
    }

    fun setBilingual(fieldName: String, language: String, value: String) {
        if (fieldName == "naadu" || fieldName == "country") return
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
            themeColor = themeColor
        )
    }
}

typealias CompanyProfile = BusinessProfile
