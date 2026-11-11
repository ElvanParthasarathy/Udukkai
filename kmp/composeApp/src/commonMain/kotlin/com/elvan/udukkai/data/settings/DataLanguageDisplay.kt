package com.elvan.udukkai.data.settings

/** Selects visible data without changing the saved translations. UI labels use LanguageManager. */
class DataLanguageDisplay(profile: BusinessProfile) {
    val primary = profile.primaryLanguage
    val secondary = profile.secondaryLanguage
    val languages = (listOf(primary) + if (profile.isBilingual) listOf(secondary) else emptyList()).filter { it.isNotBlank() }.distinct()

    fun first(values: Map<String, String>): String = values[primary].orEmpty()
    fun second(values: Map<String, String>): String =
        if (languages.size > 1) values[secondary].orEmpty().takeUnless { it == first(values) }.orEmpty() else ""

    fun lines(format: (String) -> String): List<Pair<String, Boolean>> =
        languages.mapIndexed { index, language -> format(language) to (index == 0) }
            .filter { it.first.isNotBlank() }.distinctBy { it.first }

    fun lines(values: Map<String, String>): List<Pair<String, Boolean>> =
        languages.mapIndexed { index, language -> values[language].orEmpty() to (index == 0) }
            .filter { it.first.isNotBlank() }.distinctBy { it.first }
}
