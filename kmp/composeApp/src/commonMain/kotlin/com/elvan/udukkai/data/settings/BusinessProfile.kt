package com.elvan.udukkai.data.settings

/**
 * நிறுவனத் தரவுகள் — Business profile data model.
 * Exactly mirrors Flutter's domain model `NiruvanaTharavugal`.
 *
 * All bilingual fields are stored as `Map<String, String>` where key is
 * language code ('ta' / 'en').
 */
data class NiruvanaTharavugal(
    var id: Long? = null,

    // Language Config (மொழி அமைப்பு)
    var mudhanMozhi: String = "ta",
    var thunaiMozhi: String = "en",
    var iruMozhi: Boolean = true,
    var gstPirippugal: Boolean = false,

    // Business Details (நிறுவனத் தரவு)
    var niruvanathinPeyar: MutableMap<String, String> = mutableMapOf(),
    var kurumPeyar: String = "",
    var tholaipaesi1: String = "",
    var tholaipaesi2: String = "",
    var minnanjal: String = "",
    var gstin: String = "",

    // Address (முகவரி)
    var mugavari: MutableMap<String, String> = mutableMapOf(),
    var oor: MutableMap<String, String> = mutableMapOf(),
    var maavattam: MutableMap<String, String> = mutableMapOf(),
    var maanilam: MutableMap<String, String> = mutableMapOf(),
    var naadu: MutableMap<String, String> = mutableMapOf("en" to "India", "ta" to "இந்தியா"),
    var anjalKuriyeedu: String = "",

    // Bank Details (வங்கி)
    var vangiPeyar: MutableMap<String, String> = mutableMapOf(),
    var kilai: MutableMap<String, String> = mutableMapOf(),
    var vangiKanakku: String = "",
    var ifsc: String = "",

    // Branding (அடையாளங்கள்)
    var oavuru: String = "",
    var agalaOavuru: String = "",
    var thalaippuVadivu: String = "small", // "small" or "wide"
    var kaiyoppam: String = "",
    var oppamPeyar: String = "",

    // Additional
    var adaimozhi: MutableMap<String, String> = mutableMapOf(),
    var upiId: String = "",
    var thoatraNiram: String = "#388e3c"
) {
    fun getPrimary(fieldName: String): String {
        if (fieldName == "naadu") return if (mudhanMozhi.lowercase().startsWith("ta")) "இந்தியா" else "India"
        val map = getBilingualMap(fieldName)
        return map[mudhanMozhi] ?: map["ta"] ?: ""
    }

    fun getSecondary(fieldName: String): String {
        if (fieldName == "naadu") return if (thunaiMozhi.lowercase().startsWith("ta")) "இந்தியா" else "India"
        val map = getBilingualMap(fieldName)
        return map[thunaiMozhi] ?: map["en"] ?: ""
    }

    fun setBilingual(fieldName: String, language: String, value: String) {
        if (fieldName == "naadu") return
        val map = getBilingualMap(fieldName)
        map[language] = value
    }

    private fun getBilingualMap(fieldName: String): MutableMap<String, String> {
        return when (fieldName) {
            "niruvanathinPeyar" -> niruvanathinPeyar
            "mugavari" -> mugavari
            "oor" -> oor
            "maavattam" -> maavattam
            "maanilam" -> maanilam
            "naadu" -> naadu
            "vangiPeyar" -> vangiPeyar
            "kilai" -> kilai
            "adaimozhi" -> adaimozhi
            else -> mutableMapOf()
        }
    }

    fun copy(): NiruvanaTharavugal {
        return NiruvanaTharavugal(
            id = id,
            mudhanMozhi = mudhanMozhi,
            thunaiMozhi = thunaiMozhi,
            iruMozhi = iruMozhi,
            gstPirippugal = gstPirippugal,
            niruvanathinPeyar = HashMap(niruvanathinPeyar),
            kurumPeyar = kurumPeyar,
            tholaipaesi1 = tholaipaesi1,
            tholaipaesi2 = tholaipaesi2,
            minnanjal = minnanjal,
            gstin = gstin,
            mugavari = HashMap(mugavari),
            oor = HashMap(oor),
            maavattam = HashMap(maavattam),
            maanilam = HashMap(maanilam),
            naadu = HashMap(naadu),
            anjalKuriyeedu = anjalKuriyeedu,
            vangiPeyar = HashMap(vangiPeyar),
            kilai = HashMap(kilai),
            vangiKanakku = vangiKanakku,
            ifsc = ifsc,
            oavuru = oavuru,
            agalaOavuru = agalaOavuru,
            thalaippuVadivu = thalaippuVadivu,
            kaiyoppam = kaiyoppam,
            oppamPeyar = oppamPeyar,
            adaimozhi = HashMap(adaimozhi),
            upiId = upiId,
            thoatraNiram = thoatraNiram
        )
    }
}
