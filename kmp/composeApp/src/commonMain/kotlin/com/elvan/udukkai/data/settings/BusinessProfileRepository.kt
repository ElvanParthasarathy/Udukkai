package com.elvan.udukkai.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.backup.AutoBackupManager
import com.elvan.udukkai.core.mode.AppMode

/**
 * Reactive repository managing active and stored business profiles for both Kooli and Pattu modes.
 * Strictly maintains Brick Wall decoupling:
 * - Kooli profile is isolated and backed by Kooli database space.
 * - Pattu profile is isolated and backed by Pattu database space.
 */
object NiruvanaTharavugalRepository {

    private var kooliProfileState by mutableStateOf(
        NiruvanaTharavugal(
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
            thoatraNiram = "#388e3c"
        )
    )

    private var pattuProfileState by mutableStateOf(
        NiruvanaTharavugal(
            id = 2L,
            mudhanMozhi = "ta",
            thunaiMozhi = "en",
            iruMozhi = true,
            gstPirippugal = true,
            niruvanathinPeyar = mutableMapOf(
                "ta" to "ஸ்ரீ மஹாலக்ஷ்மி பட்டு மையம்",
                "en" to "Sri Mahalakshmi Silk Centre"
            ),
            kurumPeyar = "மஹாலக்ஷ்மி சில்க்ஸ்",
            tholaipaesi1 = "+91 98421 56789",
            tholaipaesi2 = "+91 97890 12345",
            minnanjal = "sales@mahalakshmisilks.in",
            gstin = "33ABCDE1234F1Z5",
            mugavari = mutableMapOf(
                "ta" to "128, காந்தி சாலை, ஆரணி",
                "en" to "128, Gandhi Road, Arani"
            ),
            oor = mutableMapOf(
                "ta" to "ஆரணி",
                "en" to "Arani"
            ),
            maavattam = mutableMapOf(
                "ta" to "திருவண்ணாமலை",
                "en" to "Tiruvannamalai"
            ),
            maanilam = mutableMapOf(
                "ta" to "தமிழ்நாடு",
                "en" to "Tamil Nadu"
            ),
            naadu = mutableMapOf(
                "ta" to "இந்தியா",
                "en" to "India"
            ),
            anjalKuriyeedu = "632301",
            vangiPeyar = mutableMapOf(
                "ta" to "பாரத ஸ்டேட் வங்கி",
                "en" to "State Bank of India"
            ),
            kilai = mutableMapOf(
                "ta" to "ஆரணி கிளை",
                "en" to "Arani Branch"
            ),
            vangiKanakku = "309485729103",
            ifsc = "SBIN0000806",
            thalaippuVadivu = "small",
            oppamPeyar = "ஆட்சியர் / முகாமையாளர்",
            oppamMudhalPeyar = "ஆட்சியர்",
            oppamIrudhiPeyar = "முகாமையாளர்",
            adaimozhi = mutableMapOf(
                "ta" to "தூய பட்டுச் சேலைகளின் சங்கமம்",
                "en" to "The Realm of Pure Silk Sarees"
            ),
            thoatraNiram = "#6a1b9a"
        )
    )

    const val MAX_PROFILES = 5

    private var kooliProfilesState by mutableStateOf<List<NiruvanaTharavugal>>(emptyList())
    private var pattuProfilesState by mutableStateOf<List<NiruvanaTharavugal>>(emptyList())

    init {
        refreshFromDatabase()
    }

    private fun getPersistedActiveId(mode: AppMode): Long? {
        return try {
            com.elvan.udukkai.core.platform.getPreferencesHelper().getString("active_niruvanam_id_${mode.key}")?.toLongOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun persistActiveId(mode: AppMode, id: Long) {
        try {
            com.elvan.udukkai.core.platform.getPreferencesHelper().setString("active_niruvanam_id_${mode.key}", id.toString())
        } catch (_: Exception) {}
    }

    fun refreshFromDatabase() {
        try {
            val helper = getSettingsDatabaseHelper()
            helper.scanAndSync()

            val kooliProfiles = helper.loadAllProfiles(AppMode.KOOLI)
            if (kooliProfiles.isNotEmpty()) {
                kooliProfilesState = kooliProfiles
                val savedId = getPersistedActiveId(AppMode.KOOLI)
                val matching = if (savedId != null) kooliProfiles.find { it.id == savedId } else null
                val target = matching ?: kooliProfiles.first()
                kooliProfileState = target
                if (savedId == null || matching == null) {
                    target.id?.let { persistActiveId(AppMode.KOOLI, it) }
                }
            } else {
                helper.loadProfile(AppMode.KOOLI)?.let { kooliProfile ->
                    kooliProfileState = kooliProfile
                    kooliProfilesState = listOf(kooliProfile)
                }
            }

            val pattuProfiles = helper.loadAllProfiles(AppMode.PATTU)
            if (pattuProfiles.isNotEmpty()) {
                pattuProfilesState = pattuProfiles
                val savedId = getPersistedActiveId(AppMode.PATTU)
                val matching = if (savedId != null) pattuProfiles.find { it.id == savedId } else null
                val target = matching ?: pattuProfiles.first()
                pattuProfileState = target
                if (savedId == null || matching == null) {
                    target.id?.let { persistActiveId(AppMode.PATTU, it) }
                }
            } else {
                helper.loadProfile(AppMode.PATTU)?.let { pattuProfile ->
                    pattuProfileState = pattuProfile
                    pattuProfilesState = listOf(pattuProfile)
                }
            }
        } catch (_: Exception) {}
    }

    fun getAllProfiles(mode: AppMode): List<NiruvanaTharavugal> {
        return when (mode) {
            AppMode.KOOLI -> if (kooliProfilesState.isNotEmpty()) kooliProfilesState else listOf(kooliProfileState)
            AppMode.PATTU -> if (pattuProfilesState.isNotEmpty()) pattuProfilesState else listOf(pattuProfileState)
        }
    }

    fun getProfile(mode: AppMode): NiruvanaTharavugal {
        return when (mode) {
            AppMode.KOOLI -> kooliProfileState
            AppMode.PATTU -> pattuProfileState
        }
    }

    fun setActiveProfile(mode: AppMode, id: Long) {
        val profiles = getAllProfiles(mode)
        val selected = profiles.find { it.id == id } ?: return
        when (mode) {
            AppMode.KOOLI -> kooliProfileState = selected
            AppMode.PATTU -> pattuProfileState = selected
        }
        persistActiveId(mode, id)
    }

    fun createProfile(mode: AppMode, profile: NiruvanaTharavugal): Boolean {
        val currentProfiles = getAllProfiles(mode)
        if (currentProfiles.size >= MAX_PROFILES) {
            return false
        }
        val helper = getSettingsDatabaseHelper()
        val success = helper.saveProfile(mode, profile)
        if (success) {
            refreshFromDatabase()
            if (profile.id != null) {
                setActiveProfile(mode, profile.id!!)
            } else {
                val updatedProfiles = getAllProfiles(mode)
                updatedProfiles.lastOrNull()?.let {
                    setActiveProfile(mode, it.id ?: 1L)
                }
            }
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun deleteProfile(mode: AppMode, id: Long): Boolean {
        val helper = getSettingsDatabaseHelper()
        val success = helper.deleteProfile(mode, id)
        if (success) {
            refreshFromDatabase()
            val remaining = getAllProfiles(mode)
            val currentActiveId = when (mode) {
                AppMode.KOOLI -> kooliProfileState.id
                AppMode.PATTU -> pattuProfileState.id
            }
            if (currentActiveId == id && remaining.isNotEmpty()) {
                val newActive = remaining.first()
                newActive.id?.let { setActiveProfile(mode, it) }
            }
            AutoBackupManager.onDataChanged()
        }
        return success
    }

    fun updateProfile(mode: AppMode, profile: NiruvanaTharavugal) {
        val updated = profile.copy()
        when (mode) {
            AppMode.KOOLI -> {
                kooliProfileState = updated
                kooliProfilesState = if (kooliProfilesState.isNotEmpty()) {
                    kooliProfilesState.map { if (it.id == updated.id) updated else it }
                } else listOf(updated)
            }
            AppMode.PATTU -> {
                pattuProfileState = updated
                pattuProfilesState = if (pattuProfilesState.isNotEmpty()) {
                    pattuProfilesState.map { if (it.id == updated.id) updated else it }
                } else listOf(updated)
            }
        }
        try {
            val saved = getSettingsDatabaseHelper().saveProfile(mode, updated)
            if (saved) {
                refreshFromDatabase()
                AutoBackupManager.onDataChanged()
            }
        } catch (_: Exception) {}
    }

    fun clearProfiles(mode: AppMode): Boolean {
        val helper = getSettingsDatabaseHelper()
        val success = helper.clearProfiles(mode)
        if (success) {
            when (mode) {
                AppMode.KOOLI -> kooliProfilesState = emptyList()
                AppMode.PATTU -> pattuProfilesState = emptyList()
            }
            refreshFromDatabase()
        }
        return success
    }
}
