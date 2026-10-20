package com.elvan.udukkai.textiles.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.database.getTextilesDatabaseHelper
import com.elvan.udukkai.data.settings.NiruvanaTharavugal

/**
 * Reactive profile repository dedicated exclusively to Udukkai Textiles (சவுளி).
 * Reads and writes directly to `udukkai_textiles.db` without any mode parameterization.
 */
object TextilesProfileRepository {
    val profiles: List<NiruvanaTharavugal> get() = allProfiles
    fun saveProfile(newProfile: NiruvanaTharavugal): Boolean = createProfile(newProfile)


    private val defaultProfile = NiruvanaTharavugal(
        id = 1L,
        mudhanMozhi = "ta",
        thunaiMozhi = "en",
        iruMozhi = true,
        niruvanathinPeyar = mutableMapOf(
            "ta" to "கல்பனா சில்க்ஸ் & டெக்ஸ்டைல்ஸ்",
            "en" to "Kalpana Silks & Textiles"
        ),
        kurumPeyar = "கல்பனா சில்க்ஸ்",
        tholaipaesi1 = "+91 94433 22110",
        tholaipaesi2 = "+91 98840 12345",
        minnanjal = "sales@kalpanasilks.com",
        gstin = "33AABCK1234F1Z5",
        mugavari = mutableMapOf(
            "ta" to "12, காந்தி சாலை, பெரியார் நகர்",
            "en" to "12, Gandhi Road, Periyar Nagar"
        ),
        oor = mutableMapOf(
            "ta" to "ஈரோடு",
            "en" to "Erode"
        ),
        maavattam = mutableMapOf(
            "ta" to "ஈரோடு",
            "en" to "Erode"
        ),
        anjalKuriyeedu = "638001",
        vangiPeyar = mutableMapOf(
            "ta" to "ஸ்டேட் பாங்க் ஆப் இந்தியா",
            "en" to "State Bank of India"
        ),
        kilai = mutableMapOf(
            "ta" to "ஈரோடு முதன்மைக் கிளை",
            "en" to "Erode Main Branch"
        ),
        vangiKanakku = "309845217634",
        ifsc = "SBIN0000837",
        oppamPeyar = "உரிமையாளர் / மேலாளர்",
        oppamMudhalPeyar = "உரிமையாளர்",
        oppamIrudhiPeyar = "மேலாளர்",
        adaimozhi = mutableMapOf(
            "ta" to "உயர்தர பட்டு மற்றும் சவுளி ரகங்கள்",
            "en" to "Premium Silk & Textiles"
        ),
        thoatraNiram = "#6a1b9a"
    )

    const val MAX_PROFILES = 5

    var profile by mutableStateOf(defaultProfile)
        private set

    var allProfiles by mutableStateOf<List<NiruvanaTharavugal>>(listOf(defaultProfile))
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
                    // Seed default profile
                    helper.saveProfile(defaultProfile)
                    profile = defaultProfile
                    allProfiles = listOf(defaultProfile)
                }
            }
        } catch (_: Exception) {}
    }

    fun setActiveProfile(id: Long) {
        val selected = allProfiles.find { it.id == id } ?: return
        profile = selected
        persistActiveId(id)
    }

    fun createProfile(newProfile: NiruvanaTharavugal): Boolean {
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

    fun updateProfile(updated: NiruvanaTharavugal) {
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
