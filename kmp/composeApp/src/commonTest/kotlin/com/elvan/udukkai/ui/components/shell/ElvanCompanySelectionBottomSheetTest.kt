package com.elvan.udukkai.ui.components.shell

import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ElvanCompanySelectionBottomSheetTest {

    private fun createSampleProfile(
        id: Long = 1L,
        iruMozhi: Boolean = true,
        primaryLang: String = "ta",
        secondaryLang: String = "en",
        taName: String = "முருகன் கைத்தறி நெசவகம்",
        enName: String = "Murugan Handloom Weaving",
        kurumPeyar: String = "முருகன் நெசவு",
        addressTa: String = "45, நெசவாளர் தெரு, சின்னாளபட்டி",
        addressEn: String = "45, Weaver Street, Chinnalapatti"
    ): NiruvanaTharavugal {
        return NiruvanaTharavugal(
            id = id,
            mudhanMozhi = primaryLang,
            thunaiMozhi = secondaryLang,
            iruMozhi = iruMozhi,
            niruvanathinPeyar = mutableMapOf(
                "ta" to taName,
                "en" to enName
            ),
            kurumPeyar = kurumPeyar,
            mugavari = mutableMapOf(
                "ta" to addressTa,
                "en" to addressEn
            ),
            oor = mutableMapOf("ta" to "திண்டுக்கல்", "en" to "Dindigul"),
            maavattam = mutableMapOf("ta" to "திண்டுக்கல்", "en" to "Dindigul"),
            anjalKuriyeedu = "624301"
        )
    }

    @Test
    fun testBilingualBillingOn_showsSecondaryBusinessNameAsSubtitle() {
        val profile = createSampleProfile(iruMozhi = true)

        val primary = resolveCompanyItemLabel(profile, primaryBillingLang = "ta")
        val subtitle = resolveCompanySubtitle(
            profile = profile,
            isBilingual = true,
            primaryBillingLang = "ta",
            secondaryBillingLang = "en"
        )

        assertEquals("முருகன் கைத்தறி நெசவகம்", primary)
        assertEquals("Murugan Handloom Weaving", subtitle)
    }

    @Test
    fun testBilingualBillingOff_returnsNullSubtitle_neverShowsAddress() {
        val profile = createSampleProfile(iruMozhi = false)

        val primary = resolveCompanyItemLabel(profile, primaryBillingLang = "ta")
        val subtitle = resolveCompanySubtitle(
            profile = profile,
            isBilingual = false,
            primaryBillingLang = "ta",
            secondaryBillingLang = "en"
        )

        assertEquals("முருகன் கைத்தறி நெசவகம்", primary)
        assertNull(subtitle, "Subtitle must be null when bilingual billing is OFF")
    }

    @Test
    fun testBilingualBillingOn_transliterationFallbackWhenSecondaryMissing() {
        val profile = createSampleProfile(
            iruMozhi = true,
            taName = "காவிரி பட்டு மையம்",
            enName = "" // Missing English name
        )

        val subtitle = resolveCompanySubtitle(
            profile = profile,
            isBilingual = true,
            primaryBillingLang = "ta",
            secondaryBillingLang = "en"
        )

        // Transliterated from Tamil
        assertEquals("Kaaviri Pattu Maiyam", subtitle)
    }

    @Test
    fun testBilingualBillingOn_returnsNullWhenSecondaryEqualsPrimary() {
        val profile = createSampleProfile(
            iruMozhi = true,
            taName = "Apex Textiles",
            enName = "Apex Textiles"
        )

        val subtitle = resolveCompanySubtitle(
            profile = profile,
            isBilingual = true,
            primaryBillingLang = "ta",
            secondaryBillingLang = "en"
        )

        assertNull(subtitle, "Subtitle should be null when secondary name matches primary name")
    }

    @Test
    fun testActiveCompanyBillingSettingGovernsAllRows() {
        // Active company has bilingual billing turned OFF
        val activeProfile = createSampleProfile(id = 1L, iruMozhi = false)
        // Another company in the list has iruMozhi = true in its own record
        val otherProfile = createSampleProfile(id = 2L, iruMozhi = true, taName = "சேலம் பட்டு", enName = "Salem Silk")

        val profiles = listOf(activeProfile, otherProfile)
        val activeBilling = profiles.find { it.id == activeProfile.id } ?: profiles.first()

        // Setting is determined from active company's saved configuration
        val isBilingual = activeBilling.iruMozhi
        assertEquals(false, isBilingual)

        // Both rows must render with NO subtitle because active company's billing setting has bilingual OFF
        val activeSubtitle = resolveCompanySubtitle(
            profile = activeProfile,
            isBilingual = isBilingual,
            primaryBillingLang = activeBilling.mudhanMozhi,
            secondaryBillingLang = activeBilling.thunaiMozhi
        )
        val otherSubtitle = resolveCompanySubtitle(
            profile = otherProfile,
            isBilingual = isBilingual,
            primaryBillingLang = activeBilling.mudhanMozhi,
            secondaryBillingLang = activeBilling.thunaiMozhi
        )

        assertNull(activeSubtitle)
        assertNull(otherSubtitle)
    }
}
