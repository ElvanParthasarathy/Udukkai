package com.elvan.udukkai.data.mock

import com.elvan.udukkai.core.utils.MiniJson
import com.elvan.udukkai.data.settings.BusinessProfile
import migrate.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Loads the React app's company images and print settings that
 * kmp/tools/export_react_seed.py exported into composeResources/files/seed/.
 */
@OptIn(ExperimentalResourceApi::class)
object ReactSeedAssets {
    private const val SEED_PREFIX = "seed:"

    private suspend fun readText(name: String): String? = try {
        Res.readBytes("files/seed/$name").decodeToString()
    } catch (_: Exception) {
        null
    }

    /** "seed:<name>" -> the image's data URL; any other value is returned unchanged. */
    suspend fun resolveImage(value: String): String {
        if (!value.startsWith(SEED_PREFIX)) return value
        return readText("${value.removePrefix(SEED_PREFIX)}.txt").orEmpty()
    }

    /** Fills seed image references and React print settings into [profile]. */
    suspend fun apply(profile: BusinessProfile): BusinessProfile {
        val companies = MiniJson.parseObject(readText("react_companies.json"))
        val print = (companies[profile.shortName] as? Map<*, *>)?.get("print")
        return profile.copy(
            logo = resolveImage(profile.logo),
            wideLogo = resolveImage(profile.wideLogo),
            signature = resolveImage(profile.signature),
            printSettingsJson = if (print != null) MiniJson.stringify(print) else profile.printSettingsJson
        )
    }
}
