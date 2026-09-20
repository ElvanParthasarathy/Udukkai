package com.elvan.udukkai.data.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    fun refreshFromDatabase() {
        try {
            val helper = getSettingsDatabaseHelper()
            helper.scanAndSync()

            val kooliProfiles = helper.loadAllProfiles(AppMode.KOOLI).map { it.copy(iruMozhi = true) }
            if (kooliProfiles.isNotEmpty()) {
                kooliProfilesState = kooliProfiles
                val currentId = kooliProfileState.id
                val matching = kooliProfiles.find { it.id == currentId }
                kooliProfileState = (matching ?: kooliProfiles.first()).copy(iruMozhi = true)
            } else {
                helper.loadProfile(AppMode.KOOLI)?.let { kooliProfile ->
                    val fixed = kooliProfile.copy(iruMozhi = true)
                    kooliProfileState = fixed
                    kooliProfilesState = listOf(fixed)
                }
            }

            val pattuProfiles = helper.loadAllProfiles(AppMode.PATTU)
            if (pattuProfiles.isNotEmpty()) {
                pattuProfilesState = pattuProfiles
                val currentId = pattuProfileState.id
                val matching = pattuProfiles.find { it.id == currentId }
                pattuProfileState = matching ?: pattuProfiles.first()
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
            AppMode.KOOLI -> if (kooliProfileState.iruMozhi) kooliProfileState else kooliProfileState.copy(iruMozhi = true)
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
                when (mode) {
                    AppMode.KOOLI -> kooliProfileState = newActive
                    AppMode.PATTU -> pattuProfileState = newActive
                }
            }
        }
        return success
    }

    fun updateProfile(mode: AppMode, profile: NiruvanaTharavugal) {
        val updated = if (mode == AppMode.KOOLI) profile.copy(iruMozhi = true) else profile.copy()
        when (mode) {
            AppMode.KOOLI -> kooliProfileState = updated
            AppMode.PATTU -> pattuProfileState = updated
        }
        try {
            val saved = getSettingsDatabaseHelper().saveProfile(mode, updated)
            if (saved) {
                refreshFromDatabase()
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
