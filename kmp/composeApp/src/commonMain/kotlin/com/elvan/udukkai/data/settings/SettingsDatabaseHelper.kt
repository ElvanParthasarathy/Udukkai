package com.elvan.udukkai.data.settings

import com.elvan.udukkai.core.mode.AppMode

data class DatabaseScanReport(
    val isCoolieFound: Boolean = false,
    val cooliePath: String? = null,
    val isSilkFound: Boolean = false,
    val silkPath: String? = null,
    val message: String = ""
)

object MozhiJsonConverter {
    fun parse(json: String?): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()
        if (json.isNullOrBlank() || json.trim() == "{}" || json.trim() == "null") return result
        val trimmed = json.trim()
        val body = if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }
        val pattern = Regex(""""([^"]+)"\s*:\s*"((?:\\.|[^"\\])*)"""")
        for (match in pattern.findAll(body)) {
            val key = match.groupValues[1]
            val rawValue = match.groupValues[2]
            val value = rawValue
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
            result[key] = value
        }
        return result
    }

    fun stringify(map: Map<String, String>?): String {
        if (map.isNullOrEmpty()) return "{}"
        val entries = map.entries.joinToString(",") { (k, v) ->
            val escapedV = v
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            "\"$k\":\"$escapedV\""
        }
        return "{$entries}"
    }
}

interface SettingsDatabaseHelper {
    fun scanAndSync(): DatabaseScanReport
    fun loadProfile(mode: AppMode, profileId: Long? = null): NiruvanaTharavugal?
    fun loadAllProfiles(mode: AppMode): List<NiruvanaTharavugal>
    fun saveProfile(mode: AppMode, profile: NiruvanaTharavugal): Boolean
    fun deleteProfile(mode: AppMode, profileId: Long): Boolean
    fun clearProfiles(mode: AppMode): Boolean
}

expect fun getSettingsDatabaseHelper(): SettingsDatabaseHelper

