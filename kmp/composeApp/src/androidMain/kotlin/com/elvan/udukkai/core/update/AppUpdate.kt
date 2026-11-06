package com.elvan.udukkai.core.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInstaller
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.elvan.udukkai.BuildConfig
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Self-update system powered by GitHub releases and Android 12+ PackageInstaller.
 * Periodically checks for newer versions. On Wi-Fi, pre-fetches the APK.
 * When triggered, streams the APK to [PackageInstaller] with [PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED],
 * updating the application silently in the background without standard installation prompts.
 */
object AppUpdate {
    private const val TAG = "UdukkaiUpdate"
    private const val RELEASES_URL = "https://api.github.com/repos/ElvanParthasarathy/Udukkai/releases?per_page=30"
    private const val CHECK_EVERY_MS = 24 * 60 * 60 * 1000L
    private const val RETRY_AFTER_MS = 60 * 60 * 1000L
    private const val KEY_NEXT_CHECK = "next_check"
    private const val KEY_RELEASE = "release"
    private const val KEY_CHECKED_BY = "checked_by"
    private const val KEY_INSTALLING = "installing"
    private const val KEY_PROMPTED = "prompted"
    private const val CHANNEL = "app_updates"
    internal const val ACTION_RESULT = "com.elvan.udukkai.UPDATE_RESULT"

    data class Release(
        val version: String,
        val apkUrl: String,
        val size: Long,
        val sha256: String?,
        val notes: List<ReleaseNotes> = emptyList(),
    )

    data class ReleaseNotes(val version: String, val body: String) {
        val sections by lazy { parseNotes(body) }
    }

    data class NoteSection(val title: String?, val items: List<String>)

    enum class Phase { Ready, Downloading, Installing, Failed }

    var available by mutableStateOf<Release?>(null)
        private set
    var phase by mutableStateOf(Phase.Ready)
        private set
    var progress by mutableFloatStateOf(0f)
        private set
    var checking by mutableStateOf(false)
        private set

    private lateinit var app: Context
    private lateinit var prefs: SharedPreferences
    private val scope = MainScope()
    private var download: Deferred<File?>? = null
    private var downloadVersion: String? = null
    private var awaitingPermission = false

    fun init(context: Context) {
        app = context.applicationContext
        prefs = app.getSharedPreferences("udukkai_update", Context.MODE_PRIVATE)
        available = savedRelease()?.takeIf { isNewer(it.version) }
        val keep = available?.let { apkFile(it).name }
        dir().listFiles()?.forEach { if (it.name != keep) it.delete() }
    }

    fun checkIfDue() = check(force = false)
    fun checkNow() = check(force = true)

    private fun check(force: Boolean) {
        val newInstall = prefs.getString(KEY_CHECKED_BY, null) != BuildConfig.VERSION_NAME
        if (checking || (!force && !newInstall && System.currentTimeMillis() < prefs.getLong(KEY_NEXT_CHECK, 0L))) return
        checking = true
        scope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { fetchLatest() } }
            checking = false
            val now = System.currentTimeMillis()
            result.onFailure {
                Log.w(TAG, "Update check failed (${it.message}); retry scheduled in 1 hr")
                prefs.edit().putLong(KEY_NEXT_CHECK, now + RETRY_AFTER_MS).putString(KEY_CHECKED_BY, BuildConfig.VERSION_NAME).apply()
                return@launch
            }
            val latest = result.getOrNull()
            prefs.edit().putLong(KEY_NEXT_CHECK, now + CHECK_EVERY_MS).putString(KEY_CHECKED_BY, BuildConfig.VERSION_NAME)
                .apply { if (latest != null) putString(KEY_RELEASE, latest.toJson()) else remove(KEY_RELEASE) }
                .apply()
            val newer = latest?.takeIf { isNewer(it.version) }
            Log.i(TAG, "Update check: latest ${latest?.version ?: "none"}, current ${BuildConfig.VERSION_NAME}")
            if (phase == Phase.Downloading || phase == Phase.Installing) return@launch
            if (newer != available) { available = newer; phase = Phase.Ready }
            if (newer != null && !isMetered()) apkFor(newer)
        }
    }

    fun install(context: Context) {
        val release = available ?: return
        if (phase == Phase.Downloading || phase == Phase.Installing) return
        if (!app.packageManager.canRequestPackageInstalls()) {
            awaitingPermission = true
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${app.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }
        phase = Phase.Downloading
        scope.launch {
            val apk = apkFor(release).await()
            if (apk == null) { phase = Phase.Failed; return@launch }
            phase = Phase.Installing
            runCatching { withContext(Dispatchers.IO) { commit(apk, release) } }.onFailure {
                Log.w(TAG, "Install failed to start: ${it.message}", it)
                phase = Phase.Failed
            }
        }
    }

    fun onWindowResumed(context: Context) {
        if (!awaitingPermission) return
        awaitingPermission = false
        if (app.packageManager.canRequestPackageInstalls()) install(context)
    }

    internal fun onInstallResult(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = if (Build.VERSION.SDK_INT >= 33) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                if (confirm == null) { phase = Phase.Failed; return }
                prefs.edit().putBoolean(KEY_PROMPTED, true).apply()
                context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            PackageInstaller.STATUS_SUCCESS -> Unit
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                prefs.edit().remove(KEY_INSTALLING).remove(KEY_PROMPTED).apply()
                phase = Phase.Ready
            }
            else -> {
                Log.w(TAG, "Install failed with status $status: ${intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)}")
                prefs.edit().remove(KEY_INSTALLING).remove(KEY_PROMPTED).apply()
                phase = Phase.Failed
            }
        }
    }

    internal fun onUpdated(context: Context) {
        val version = prefs.getString(KEY_INSTALLING, null) ?: return
        val prompted = prefs.getBoolean(KEY_PROMPTED, false)
        prefs.edit().remove(KEY_INSTALLING).remove(KEY_PROMPTED).apply()
        Log.i(TAG, "Updated successfully to ${BuildConfig.VERSION_NAME} (from $version)")
        if (prompted || !NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, "செயலி புதுப்பிப்பு (Updates)", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("உடுக்கை v${BuildConfig.VERSION_NAME}")
            .setContentText("செயலி வெற்றிகரமாகப் புதுப்பிக்கப்பட்டது · App updated")
            .setContentIntent(PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_IMMUTABLE))
            .setAutoCancel(true)
            .setSilent(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(1, notification) }
    }

    private fun fetchLatest(): Release? {
        val url = URL(RELEASES_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "Udukkai/${BuildConfig.VERSION_NAME}")
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.connect()
        if (conn.responseCode !in 200..299) return null
        val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        return parseReleases(JSONArray(jsonStr), BuildConfig.VERSION_NAME)
    }

    internal fun parseReleases(json: JSONArray, current: String): Release? {
        val published = (0 until json.length()).map { json.getJSONObject(it) }
            .filter { !it.optBoolean("draft") && !it.optBoolean("prerelease") }
        val latest = published.mapNotNull { parseRelease(it) }
            .maxWithOrNull { a, b -> compareVersions(a.version, b.version) } ?: return null
        val notes = published
            .map { ReleaseNotes(it.optString("tag_name").removePrefix("v"), it.optString("body")) }
            .filter { compareVersions(it.version, current) > 0 && compareVersions(it.version, latest.version) <= 0 }
            .filter { it.sections.isNotEmpty() }
            .sortedWith { a, b -> compareVersions(b.version, a.version) }
        return latest.copy(notes = notes)
    }

    internal fun parseRelease(json: JSONObject): Release? {
        val version = json.optString("tag_name").removePrefix("v").ifBlank { return null }
        val assets = json.optJSONArray("assets") ?: return null
        val flavor = BuildConfig.FLAVOR.lowercase()
        // Match flavor-specific APK (e.g. Udukkai-Factory.apk) or generic Udukkai.apk
        val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
            .firstOrNull {
                val name = it.optString("name").lowercase()
                name.endsWith(".apk") && (name.contains(flavor) || name.contains("udukkai") || assets.length() == 1)
            } ?: return null
        return Release(
            version = version,
            apkUrl = apk.optString("browser_download_url").ifBlank { return null },
            size = apk.optLong("size"),
            sha256 = apk.optString("digest").takeIf { it.startsWith("sha256:") }?.removePrefix("sha256:"),
            notes = listOf(ReleaseNotes(version, json.optString("body"))).filter { it.sections.isNotEmpty() }
        )
    }

    internal fun parseNotes(body: String): List<NoteSection> {
        val sections = mutableListOf<NoteSection>()
        var title: String? = null
        val items = mutableListOf<String>()
        fun flush() {
            if (items.isNotEmpty()) sections += NoteSection(title, items.toList())
            items.clear()
        }
        for (raw in body.lineSequence()) {
            val line = raw.trim()
            if (line == "---") break
            when {
                line.startsWith("#") -> { flush(); title = plain(line.trimStart('#').trim()).ifBlank { null } }
                line.startsWith("- ") || line.startsWith("* ") -> items += plain(line.substring(2).trim())
                line.isNotEmpty() && items.isNotEmpty() && raw.startsWith("  ") -> items[items.lastIndex] += " " + plain(line)
            }
        }
        flush()
        return sections
    }

    private fun plain(markdown: String) = markdown
        .replace(Regex("""\[([^\]]+)]\([^)]*\)"""), "$1")
        .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
        .replace(Regex("""`([^`]+)`"""), "$1")

    internal fun compareVersions(a: String, b: String): Int {
        val x = a.split('.').map { it.toIntOrNull() ?: 0 }
        val y = b.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(x.size, y.size)) {
            val d = x.getOrElse(i) { 0 }.compareTo(y.getOrElse(i) { 0 })
            if (d != 0) return d
        }
        return 0
    }

    private fun isNewer(version: String) = compareVersions(version, BuildConfig.VERSION_NAME) > 0
    private fun isMetered() = app.getSystemService(ConnectivityManager::class.java)?.isActiveNetworkMetered != false

    private fun apkFor(release: Release): Deferred<File?> {
        download?.takeIf { it.isActive && downloadVersion == release.version }?.let { return it }
        downloadVersion = release.version
        return scope.async(Dispatchers.IO) {
            runCatching { downloadApk(release) }
                .onFailure { Log.w(TAG, "Download of ${release.version} failed: ${it.message}") }
                .getOrNull()
        }.also { download = it }
    }

    private fun downloadApk(release: Release): File {
        val file = apkFile(release)
        if (file.length() == release.size && (release.sha256 == null || file.sha256() == release.sha256)) {
            progress = 1f
            return file
        }
        progress = 0f
        val part = File(dir(), file.name + ".part")
        val url = URL(release.apkUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("User-Agent", "Udukkai/${BuildConfig.VERSION_NAME}")
        conn.connect()
        check(conn.responseCode in 200..299) { "HTTP ${conn.responseCode}" }
        val total = release.size.takeIf { it > 0 } ?: conn.contentLength.toLong()
        val digest = MessageDigest.getInstance("SHA-256")
        var read = 0L
        part.outputStream().use { out ->
            conn.inputStream.use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    out.write(buffer, 0, n)
                    digest.update(buffer, 0, n)
                    read += n
                    if (total > 0) progress = (read.toFloat() / total).coerceIn(0f, 1f)
                }
            }
        }
        conn.disconnect()
        check(release.size <= 0 || read == release.size) { "Received $read bytes, expected ${release.size}" }
        release.sha256?.let { check(digest.digest().hex() == it) { "SHA-256 mismatch" } }
        check(part.renameTo(file)) { "Failed to rename ${part.name}" }
        Log.i(TAG, "Downloaded ${release.version} (${file.length()} bytes)")
        return file
    }

    private fun commit(apk: File, release: Release) {
        val installer = app.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(app.packageName)
            setSize(apk.length())
            // Android 12+ silent update without user prompt
            if (Build.VERSION.SDK_INT >= 31) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
            }
        }
        val id = installer.createSession(params)
        val session = installer.openSession(id)
        try {
            session.openWrite("udukkai_update.apk", 0, apk.length()).use { out ->
                apk.inputStream().use { it.copyTo(out) }
                session.fsync(out)
            }
            prefs.edit().putString(KEY_INSTALLING, release.version).putBoolean(KEY_PROMPTED, false).commit()
            val result = Intent(app, UpdateReceiver::class.java).setAction(ACTION_RESULT)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0)
            Log.i(TAG, "Installing ${release.version} via PackageInstaller session $id")
            session.commit(PendingIntent.getBroadcast(app, id, result, flags).intentSender)
            session.close()
        } catch (e: Exception) {
            session.abandon()
            throw e
        }
    }

    private fun dir() = File(app.cacheDir, "update").apply { mkdirs() }
    private fun apkFile(release: Release) = File(dir(), "Udukkai-${release.version}.apk")

    private fun savedRelease(): Release? = prefs.getString(KEY_RELEASE, null)?.let {
        runCatching {
            val o = JSONObject(it)
            val notes = o.optJSONArray("notes") ?: JSONArray()
            Release(
                o.getString("version"), o.getString("url"), o.getLong("size"), o.optString("sha256").ifBlank { null },
                (0 until notes.length()).map { i -> notes.getJSONObject(i).let { n -> ReleaseNotes(n.getString("version"), n.getString("body")) } }
            )
        }.getOrNull()
    }

    private fun Release.toJson(): String = JSONObject().apply {
        put("version", version)
        put("url", apkUrl)
        put("size", size)
        put("sha256", sha256 ?: "")
        put("notes", JSONArray().apply { notes.forEach { put(JSONObject().apply { put("version", it.version); put("body", it.body) }) } })
    }.toString()

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().use { input ->
            val buf = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().hex()
    }

    private fun ByteArray.hex() = joinToString("") { "%02x".format(it) }
}

/** Receives PackageInstaller commit results and Android package replacement broadcast. */
class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AppUpdate.ACTION_RESULT -> AppUpdate.onInstallResult(context, intent)
            Intent.ACTION_MY_PACKAGE_REPLACED -> AppUpdate.onUpdated(context)
        }
    }
}
