package com.elvan.udukkai.data.mock

import com.elvan.udukkai.core.utils.MiniJson
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.ItemModel
import com.elvan.udukkai.data.settings.BusinessProfile
import migrate.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Turns the generated seed data ([MockData.kt]) into app models, loading the company images that
 * kmp/tools/import_rtdb_export.py wrote into composeResources/files/seed/.
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

    suspend fun profileOf(s: SeedProfile): BusinessProfile = BusinessProfile(
        businessName = s.businessName.toMutableMap(),
        shortName = s.shortName,
        phone1 = s.phone1, phone2 = s.phone2, email = s.email, gstin = s.gstin, pan = s.pan,
        address = s.address.toMutableMap(), city = s.city.toMutableMap(), district = s.district.toMutableMap(),
        state = s.state.toMutableMap(), country = s.country.toMutableMap(), pinCode = s.pinCode,
        bankName = s.bankName.toMutableMap(), branch = s.branch.toMutableMap(),
        accountNumber = s.accountNumber, ifsc = s.ifsc,
        logo = resolveImage(s.logo), wideLogo = resolveImage(s.wideLogo), signature = resolveImage(s.signature),
        headerLayout = s.headerLayout, signatoryName = s.signatoryName,
        tagline = s.tagline.toMutableMap(), themeColor = s.themeColor,
        printSettingsJson = MiniJson.stringify(s.printSettings)
    )

    fun itemOf(s: SeedItem) = ItemModel(
        name = s.name, hsnCode = s.hsnCode, price = s.price, taxRate = s.taxRate, quantityType = s.quantityType, unit = s.unit
    )

    fun customerOf(s: SeedCustomer) = CustomerModel(
        name = s.name, address = s.address, city = s.city, district = s.district, state = s.state, country = s.country,
        pinCode = s.pinCode, gstin = s.gstin, email = s.email, phone = s.phone
    )
}
