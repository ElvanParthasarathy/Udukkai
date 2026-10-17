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

        val primary = resolveCompanyItemLabel(profile)
        val subtitle = resolveCompanySubtitle(profile)

        assertEquals("முருகன் கைத்தறி நெசவகம்", primary)
        assertEquals("Murugan Handloom Weaving", subtitle)
    }

    @Test
    fun testBilingualBillingOff_returnsNullSubtitle_neverShowsAddress() {
        val profile = createSampleProfile(iruMozhi = false)

        val primary = resolveCompanyItemLabel(profile)
        val subtitle = resolveCompanySubtitle(profile)

        assertEquals("முருகன் கைத்தறி நெசவகம்", primary)
        assertNull(subtitle, "Subtitle must be null when bilingual billing is OFF")
    }

    @Test
    fun testScenario1_bilingualToAnotherBilingualCompany() {
        val companyA = createSampleProfile(id = 1L, iruMozhi = true, taName = "நிறுவனம் A", enName = "Company A")
        val companyB = createSampleProfile(id = 2L, iruMozhi = true, taName = "நிறுவனம் B", enName = "Company B")

        // Both companies retain their own independent bilingual display
        assertEquals("நிறுவனம் A", resolveCompanyItemLabel(companyA))
        assertEquals("Company A", resolveCompanySubtitle(companyA))

        assertEquals("நிறுவனம் B", resolveCompanyItemLabel(companyB))
        assertEquals("Company B", resolveCompanySubtitle(companyB))
    }

    @Test
    fun testScenario2_bilingualToNonBilingualCompany() {
        val bilingualCompany = createSampleProfile(id = 1L, iruMozhi = true, taName = "பட்டு நெசவகம்", enName = "Silk Weaving")
        val nonBilingualCompany = createSampleProfile(id = 2L, iruMozhi = false, taName = "கைத்தறி கூடம்", enName = "Handloom Shed")

        // Bilingual company has subtitle
        assertEquals("பட்டு நெசவகம்", resolveCompanyItemLabel(bilingualCompany))
        assertEquals("Silk Weaving", resolveCompanySubtitle(bilingualCompany))

        // Non-bilingual company has NO subtitle and no address
        assertEquals("கைத்தறி கூடம்", resolveCompanyItemLabel(nonBilingualCompany))
        assertNull(resolveCompanySubtitle(nonBilingualCompany))
    }

    @Test
    fun testScenario3_nonBilingualToBilingualCompany() {
        val nonBilingualCompany = createSampleProfile(id = 1L, iruMozhi = false, taName = "அமுதம் டெக்ஸ்டைல்ஸ்", enName = "Amudham Textiles")
        val bilingualCompany = createSampleProfile(id = 2L, iruMozhi = true, taName = "வள்ளுவர் நெசவு", enName = "Valluvar Weaving")

        // Non-bilingual company has NO subtitle
        assertNull(resolveCompanySubtitle(nonBilingualCompany))
        // Bilingual company has subtitle
        assertEquals("Valluvar Weaving", resolveCompanySubtitle(bilingualCompany))
    }

    @Test
    fun testScenario4_switchingRepeatedlyBetweenCompaniesWithDifferentSettings() {
        val company1 = createSampleProfile(id = 1L, iruMozhi = true, taName = "நிறுவனம் 1", enName = "Company 1")
        val company2 = createSampleProfile(id = 2L, iruMozhi = false, taName = "நிறுவனம் 2", enName = "Company 2")
        val company3 = createSampleProfile(id = 3L, iruMozhi = true, taName = "நிறுவனம் 3", enName = "Company 3")

        val profiles = listOf(company1, company2, company3)

        // Simulate switching active company to 1, then 2, then 3, then back to 1
        val simulatedSelections = listOf(1L, 2L, 3L, 1L, 2L)

        for (activeId in simulatedSelections) {
            // When any company is active, every row's display remains strictly invariant to active selection
            for (p in profiles) {
                val label = resolveCompanyItemLabel(p)
                val subtitle = resolveCompanySubtitle(p)

                if (p.iruMozhi) {
                    assertEquals(p.niruvanathinPeyar["en"], subtitle)
                } else {
                    assertNull(subtitle)
                }
                assertEquals(p.niruvanathinPeyar["ta"], label)
            }
        }
    }

    @Test
    fun testScenario5_longBusinessNameThatWrapsOntoMultipleLines() {
        val longTaName = "ஸ்ரீ மஹாலக்ஷ்மி பட்டு மையம் மற்றும் பாரம்பரிய கைத்தறி ஆடைகள் உற்பத்தி நிறுவனம்"
        val longEnName = "Sri Mahalakshmi Silk Centre and Traditional Handloom Garments Manufacturing Company"
        val profile = createSampleProfile(
            iruMozhi = true,
            taName = longTaName,
            enName = longEnName
        )

        val primary = resolveCompanyItemLabel(profile)
        val subtitle = resolveCompanySubtitle(profile)

        assertEquals(longTaName, primary)
        assertEquals(longEnName, subtitle)
    }

    @Test
    fun testScenario6_companyWithMissingSecondaryLanguageName() {
        val profile = createSampleProfile(
            iruMozhi = true,
            taName = "காவிரி பட்டு மையம்",
            enName = "" // Missing English name
        )

        val subtitle = resolveCompanySubtitle(profile)

        // Falls back to transliteration of Tamil name
        assertEquals("Kaaviri Pattu Maiyam", subtitle)
    }

    @Test
    fun testBilingualBillingOn_returnsNullWhenSecondaryEqualsPrimary() {
        val profile = createSampleProfile(
            iruMozhi = true,
            taName = "Apex Textiles",
            enName = "Apex Textiles"
        )

        val subtitle = resolveCompanySubtitle(profile)

        assertNull(subtitle, "Subtitle should be null when secondary name matches primary name")
    }
}
