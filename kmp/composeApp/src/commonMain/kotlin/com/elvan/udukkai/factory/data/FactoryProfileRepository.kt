package com.elvan.udukkai.factory.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.data.database.getFactoryDatabaseHelper
import com.elvan.udukkai.data.settings.NiruvanaTharavugal

/**
 * Reactive profile repository dedicated exclusively to Udukkai Factory (ஆலை).
 * Reads and writes directly to `udukkai_factory.db` without any mode parameterization.
 */
object FactoryProfileRepository {
    val profiles: List<NiruvanaTharavugal> get() = allProfiles
    fun saveProfile(newProfile: NiruvanaTharavugal): Boolean = createProfile(newProfile)


    private val defaultProfile = NiruvanaTharavugal(
        id = 1L,
        mudhanMozhi = "ta",
        thunaiMozhi = "en",
        iruMozhi = true,
        niruvanathinPeyar = mutableMapOf(
            "ta" to "முருகன் கைத்தறி நெசவகம்",
            "en" to "Murugan Handloom Weaving"
        ),
        kurumPeyar = "முருகன் நெசவு",
        tholaipaesi1 = "+91 98765 43210",
        tholaipaesi2 = "+91 94432 10987",
        minnanjal = "murugan.loom@udukkai.in",
        mugavari = mutableMapOf(
            "ta" to "45, நெசவாளர் தெரு, சின்னாளபட்டி",
            "en" to "45, Weaver Street, Chinnalapatti"
        ),
        oor = mutableMapOf(
            "ta" to "திண்டுக்கல்",
            "en" to "Dindigul"
        ),
        maavattam = mutableMapOf(
            "ta" to "திண்டுக்கல்",
            "en" to "Dindigul"
        ),
        anjalKuriyeedu = "624301",
        vangiPeyar = mutableMapOf(
            "ta" to "இந்தியன் வங்கி",
            "en" to "Indian Bank"
        ),
        kilai = mutableMapOf(
            "ta" to "சின்னாளபட்டி கிளை",
            "en" to "Chinnalapatti Branch"
        ),
        vangiKanakku = "502394857219",
        ifsc = "IDIB000C024",
        oppamPeyar = "உரிமையாளர் / மேலாளர்",
        oppamMudhalPeyar = "உரிமையாளர்",
        oppamIrudhiPeyar = "மேலாளர்",
        adaimozhi = mutableMapOf(
            "ta" to "பாரம்பரிய கைத்தறி நெசவு",
            "en" to "Traditional Handloom Weaving"
        ),
        thoatraNiram = "#2e7d32"
    )

    const val MAX_PROFILES = 5

    var profile by mutableStateOf(defaultProfile)
        private set

    var allProfiles by mutableStateOf<List<NiruvanaTharavugal>>(listOf(defaultProfile))
        private set

    init {
        refreshFromDatabase()
    }

    private const val PREF_KEY_ACTIVE_ID = "active_factory_niruvanam_id"

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
            val helper = getFactoryDatabaseHelper()
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
        val helper = getFactoryDatabaseHelper()
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
            val saved = getFactoryDatabaseHelper().saveProfile(copy)
            if (saved) {
                copy.id?.let { persistActiveId(it) }
                refreshFromDatabase()
                AutoBackupManager.onDataChanged()
            }
        } catch (_: Exception) {}
    }

    fun deleteProfile(id: Long): Boolean {
        val helper = getFactoryDatabaseHelper()
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
        val success = getFactoryDatabaseHelper().clearProfiles()
        if (success) {
            allProfiles = emptyList()
            refreshFromDatabase()
        }
        return success
    }
}
