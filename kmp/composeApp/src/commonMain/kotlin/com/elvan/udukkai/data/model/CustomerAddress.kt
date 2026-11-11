package com.elvan.udukkai.data.model

import com.elvan.udukkai.core.data.indianStates
import com.elvan.udukkai.core.data.locationDataName
import com.elvan.udukkai.core.data.tamilNaduDistricts
import com.elvan.udukkai.core.data.worldCountries

fun CustomerModel.fullAddress(language: String): String {
    val countryName = locationDataName(country, worldCountries, language)
    val overseas = internationalAddress[language].orEmpty()
    val parts = if (overseas.isNotBlank()) listOf(overseas, countryName) else listOf(
        address[language].orEmpty(), city[language].orEmpty(),
        locationDataName(district, tamilNaduDistricts, language),
        locationDataName(state, indianStates, language), pinCode, countryName
    )
    return parts.map(String::trim).filter(String::isNotEmpty).distinctBy { it.lowercase() }.joinToString(", ")
}
