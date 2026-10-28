package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.settings.BusinessProfile

/**
 * Reactive profile repository dedicated exclusively to Udukkai Textiles (சவுளி).
 * Reads and writes directly to `udukkai_textiles.db` without any mode parameterization.
 */
object TextilesProfileRepository {
    val profiles: List<BusinessProfile> get() = allProfiles
    fun saveProfile(newProfile: BusinessProfile): Boolean = createProfile(newProfile)


    private val defaultProfile = BusinessProfile(
        id = 1L,
        primaryLanguage = "ta",
        secondaryLanguage = "en",
        isBilingual = true,
        businessName = mutableMapOf(
            "ta" to "கல்பனா சில்க்ஸ் & டெக்ஸ்டைல்ஸ்",
            "en" to "Kalpana Silks & Textiles"
        ),
        shortName = "கல்பனா சில்க்ஸ்",
        phone1 = "+91 94433 22110",
        phone2 = "+91 98840 12345",
        email = "sales@kalpanasilks.com",
        gstin = "33AABCK1234F1Z5",
        address = mutableMapOf(
            "ta" to "12, காந்தி சாலை, பெரியார் நகர்",
            "en" to "12, Gandhi Road, Periyar Nagar"
        ),
        city = mutableMapOf(
            "ta" to "ஈரோடு",
            "en" to "Erode"
        ),
        district = mutableMapOf(
            "ta" to "ஈரோடு",
            "en" to "Erode"
        ),
        pinCode = "638001",
        bankName = mutableMapOf(
            "ta" to "ஸ்டேட் பாங்க் ஆப் இந்தியா",
            "en" to "State Bank of India"
        ),
        branch = mutableMapOf(
            "ta" to "ஈரோடு முதன்மைக் கிளை",
            "en" to "Erode Main Branch"
        ),
        accountNumber = "309845217634",
        ifsc = "SBIN0000837",
        signatoryName = "உரிமையாளர் / மேலாளர்",
        signatoryFirstName = "உரிமையாளர்",
        signatoryLastName = "மேலாளர்",
        tagline = mutableMapOf(
            "ta" to "உயர்தர பட்டு மற்றும் சவுளி ரகங்கள்",
            "en" to "Premium Silk & Textiles"
        ),
        themeColor = "#6a1b9a"
    )

    const val MAX_PROFILES = 5

    var profile by mutableStateOf(defaultProfile)
        private set

    // Empty until the user creates their business in the setup flow (Flutter: isSetupComplete).
    var allProfiles by mutableStateOf<List<BusinessProfile>>(emptyList())
        private set

    init {
        refreshFromDatabase()
    }

    private const val PREF_KEY_ACTIVE_ID = "active_textiles_niruvanam_id"

    private fun getPersistedActiveId(): Long? = try {
        getPreferencesHelper().getString(PREF_KEY_ACTIVE_ID)?.toLongOrNull()
    } catch (_: Exception) { null }

    private fun persistActiveId(id: Long) {
        try {
            getPreferencesHelper().setString(PREF_KEY_ACTIVE_ID, id.toString())
        } catch (_: Exception) {}
    }

    fun refreshFromDatabase() {
        try {
            val helper = getTextilesDatabaseHelper()
            val list = helper.loadAllProfiles()
            if (list.isNotEmpty()) {
                allProfiles = list
                val savedId = getPersistedActiveId()
                val target = if (savedId != null) list.find { it.id == savedId } ?: list.first() else list.first()
                profile = target
                target.id?.let { persistActiveId(it) }
            } else {
                helper.loadProfile()?.let {
                    profile = it
                    allProfiles = listOf(it)
                } ?: run {
                    // No business yet: keep the placeholder in memory only so the
                    // onboarding setup flow runs (nothing is written to the database).
                    profile = defaultProfile
                    allProfiles = emptyList()
                }
            }
        } catch (_: Exception) {}
    }

    fun setActiveProfile(id: Long) {
        val selected = allProfiles.find { it.id == id } ?: return
        profile = selected
        persistActiveId(id)
    }

    fun createProfile(newProfile: BusinessProfile): Boolean {
        if (allProfiles.size >= MAX_PROFILES) return false
        val helper = getTextilesDatabaseHelper()
        val success = helper.saveProfile(newProfile)
        if (success) {
            refreshFromDatabase()
            if (newProfile.id != null) {
                setActiveProfile(newProfile.id!!)
            } else {
                allProfiles.lastOrNull()?.id?.let { setActiveProfile(it) }
            }
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun updateProfile(updated: BusinessProfile) {
        val copy = updated.copy()
        profile = copy
        allProfiles = allProfiles.map { if (it.id == copy.id) copy else it }
        try {
            val saved = getTextilesDatabaseHelper().saveProfile(copy)
            if (saved) {
                copy.id?.let { persistActiveId(it) }
                refreshFromDatabase()
                AutoBackupManager.onDataChanged()
            }
        } catch (_: Exception) {}
    }

    fun deleteProfile(id: Long): Boolean {
        // The only business is never deleted
        if (allProfiles.size <= 1) return false
        val helper = getTextilesDatabaseHelper()
        val success = helper.deleteProfile(id)
        if (success) {
            refreshFromDatabase()
            if (profile.id == id && allProfiles.isNotEmpty()) {
                allProfiles.first().id?.let { setActiveProfile(it) }
            }
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun clearProfiles(): Boolean {
        val success = getTextilesDatabaseHelper().clearProfiles()
        if (success) {
            allProfiles = emptyList()
            refreshFromDatabase()
        }
        return success
    }
}
