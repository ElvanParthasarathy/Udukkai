package com.elvan.udukkai.core.data

data class LocationName(
    val en: String,
    val ta: String
)

val indianStates: List<LocationName> = listOf(
    LocationName(en = "Andaman and Nicobar Islands", ta = "அந்தமான் நிக்கோபார் தீவுகள்"),
    LocationName(en = "Andhra Pradesh", ta = "ஆந்திரப் பிரதேசம்"),
    LocationName(en = "Arunachal Pradesh", ta = "அருணாசலப் பிரதேசம்"),
    LocationName(en = "Assam", ta = "அசாம்"),
    LocationName(en = "Bihar", ta = "பீகார்"),
    LocationName(en = "Chandigarh", ta = "சண்டிகர்"),
    LocationName(en = "Chhattisgarh", ta = "சத்தீசுகர்"),
    LocationName(en = "Dadra and Nagar Haveli and Daman and Diu", ta = "தாத்ரா மற்றும் நகர் அவேலி மற்றும் தாமன் மற்றும் தியூ"),
    LocationName(en = "Delhi", ta = "தில்லி"),
    LocationName(en = "Goa", ta = "கோவா"),
    LocationName(en = "Gujarat", ta = "குசராத்து"),
    LocationName(en = "Haryana", ta = "அரியாணா"),
    LocationName(en = "Himachal Pradesh", ta = "இமாசலப் பிரதேசம்"),
    LocationName(en = "Jammu and Kashmir", ta = "சம்மு காசுமீர்"),
    LocationName(en = "Jharkhand", ta = "சார்க்கண்டு"),
    LocationName(en = "Karnataka", ta = "கருநாடகம்"),
    LocationName(en = "Kerala", ta = "கேரளம்"),
    LocationName(en = "Ladakh", ta = "இலடாக்"),
    LocationName(en = "Lakshadweep", ta = "இலட்சத்தீவுகள்"),
    LocationName(en = "Madhya Pradesh", ta = "மத்தியப் பிரதேசம்"),
    LocationName(en = "Maharashtra", ta = "மகாராட்டிரம்"),
    LocationName(en = "Manipur", ta = "மணிப்பூர்"),
    LocationName(en = "Meghalaya", ta = "மேகாலயா"),
    LocationName(en = "Mizoram", ta = "மிசோரம்"),
    LocationName(en = "Nagaland", ta = "நாகாலாந்து"),
    LocationName(en = "Odisha", ta = "ஒடிசா"),
    LocationName(en = "Puducherry", ta = "புதுச்சேரி"),
    LocationName(en = "Punjab", ta = "பஞ்சாபு"),
    LocationName(en = "Rajasthan", ta = "இராசத்தான்"),
    LocationName(en = "Sikkim", ta = "சிக்கிம்"),
    LocationName(en = "Tamil Nadu", ta = "தமிழ்நாடு"),
    LocationName(en = "Telangana", ta = "தெலுங்கானா"),
    LocationName(en = "Tripura", ta = "திரிபுரா"),
    LocationName(en = "Uttar Pradesh", ta = "உத்தரப் பிரதேசம்"),
    LocationName(en = "Uttarakhand", ta = "உத்தராகண்டம்"),
    LocationName(en = "West Bengal", ta = "மேற்கு வங்காளம்"),
    LocationName(en = "Custom", ta = "தனிப்பயன்")
)

val worldCountries: List<LocationName> = listOf(
    LocationName(en = "India", ta = "இந்தியா"),
    LocationName(en = "United States", ta = "அமெரிக்க ஒன்றிணைந்த நாடுகள்"),
    LocationName(en = "United Kingdom", ta = "ஒன்றிணைந்த பேரரசு"),
    LocationName(en = "Canada", ta = "கனடா"),
    LocationName(en = "Australia", ta = "ஆஸ்திரேலியா"),
    LocationName(en = "New Zealand", ta = "நியூசிலாந்து"),
    LocationName(en = "Japan", ta = "நிஹொன்"),
    LocationName(en = "South Korea", ta = "ஹங்குக்கு"),
    LocationName(en = "China", ta = "சீனா"),
    LocationName(en = "France", ta = "ஃபிரான்சு"),
    LocationName(en = "Germany", ta = "டொயிச்சுலாந்து"),
    LocationName(en = "Italy", ta = "இத்தாலியா"),
    LocationName(en = "Spain", ta = "எசுப்பானியா"),
    LocationName(en = "Netherlands", ta = "நெதர்லாந்து"),
    LocationName(en = "Singapore", ta = "சிங்கப்பூர்"),
    LocationName(en = "Malaysia", ta = "மலேசியா"),
    LocationName(en = "Indonesia", ta = "இந்தோனேசியா"),
    LocationName(en = "Thailand", ta = "தாய்லாந்து"),
    LocationName(en = "United Arab Emirates", ta = "ஒன்றிணைந்த அரபு அமீரகம்"),
    LocationName(en = "Saudi Arabia", ta = "சவுதி அரேபியா"),
    LocationName(en = "South Africa", ta = "தென் ஆப்பிரிக்கா"),
    LocationName(en = "Sri Lanka", ta = "இலங்கை"),
    LocationName(en = "Bangladesh", ta = "வங்காளதேசம்"),
    LocationName(en = "Nepal", ta = "நேபாளம்"),
    LocationName(en = "Pakistan", ta = "பாகிஸ்தான்"),
    LocationName(en = "Philippines", ta = "பிலிப்பினாசு"),
    LocationName(en = "Custom", ta = "தனிப்பயன்")
)

val worldCallingCodes: List<String> = listOf(
    "+971", "+880", "+977", "+966", "+852",
    "+91", "+44", "+61", "+64", "+81", "+82", "+86", "+33", "+49",
    "+39", "+34", "+31", "+65", "+60", "+62", "+66", "+27", "+94",
    "+92", "+63", "+1"
)

fun getCountryCallingCode(countryName: String?): String {
    val c = countryName?.trim()?.lowercase() ?: ""
    return when {
        c == "india" || c == "இந்தியா" -> "+91"
        c == "united states" || c == "அமெரிக்க ஒன்றிணைந்த நாடுகள்" -> "+1"
        c == "united kingdom" || c == "ஒன்றிணைந்த பேரரசு" -> "+44"
        c == "canada" || c == "கனடா" -> "+1"
        c == "australia" || c == "ஆஸ்திரேலியா" -> "+61"
        c == "new zealand" || c == "நியூசிலாந்து" -> "+64"
        c == "japan" || c == "நிஹொன்" -> "+81"
        c == "south korea" || c == "ஹங்குக்கு" -> "+82"
        c == "china" || c == "சீனா" -> "+86"
        c == "france" || c == "ஃபிரான்சு" -> "+33"
        c == "germany" || c == "டொயிச்சுலாந்து" -> "+49"
        c == "italy" || c == "இத்தாலியா" -> "+39"
        c == "spain" || c == "எசுப்பானியா" -> "+34"
        c == "netherlands" || c == "நெதர்லாந்து" -> "+31"
        c == "singapore" || c == "சிங்கப்பூர்" -> "+65"
        c == "malaysia" || c == "மலேசியா" -> "+60"
        c == "indonesia" || c == "இந்தோனேசியா" -> "+62"
        c == "thailand" || c == "தாய்லாந்து" -> "+66"
        c == "united arab emirates" || c == "ஒன்றிணைந்த அரபு அமீரகம்" -> "+971"
        c == "saudi arabia" || c == "சவுதி அரேபியா" -> "+966"
        c == "south africa" || c == "தென் ஆப்பிரிக்கா" -> "+27"
        c == "sri lanka" || c == "இலங்கை" -> "+94"
        c == "bangladesh" || c == "வங்காளதேசம்" -> "+880"
        c == "nepal" || c == "நேபாளம்" -> "+977"
        c == "pakistan" || c == "பாகிஸ்தான்" -> "+92"
        c == "philippines" || c == "பிலிப்பினாசு" -> "+63"
        else -> "+91"
    }
}
