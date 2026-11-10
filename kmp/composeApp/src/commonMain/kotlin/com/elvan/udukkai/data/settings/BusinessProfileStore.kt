package com.elvan.udukkai.data.settings

data class NewProfileDefaults(val splitGst: Boolean, val themeColor: String)

/** Shared Settings UI depends on this contract; each app retains its own database and profile state. */
interface BusinessProfileStore {
    val profile: BusinessProfile
    val profiles: List<BusinessProfile>
    val newProfileDefaults: NewProfileDefaults
    fun setActiveProfile(id: Long)
    fun updateProfile(updated: BusinessProfile)
    fun createProfile(newProfile: BusinessProfile): Boolean
    fun deleteProfile(id: Long): Boolean
}
