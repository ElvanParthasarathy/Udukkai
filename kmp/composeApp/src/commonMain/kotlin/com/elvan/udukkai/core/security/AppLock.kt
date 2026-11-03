package com.elvan.udukkai.core.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elvan.udukkai.core.platform.getPreferencesHelper
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * The app's own lock for the privacy of the bills, separate from the phone's screen lock (people who know the phone's
 * unlock code must not get into the bills). A PIN of 6 or 8 digits is compulsory: the app cannot be used before one is
 * set. It is kept only as a salted PBKDF2 hash, never as the digits. The app asks for it when it opens, when it comes back
 * after a minute away, and before the actions that can lose or give away data (replacing data by a restore, going back
 * to an earlier copy, deleting a business, sharing a backup, signing out). The phone's fingerprint can open it instead
 * of typing, when the user switches that on; the PIN always works.
 * Wrong tries slow down: five wrong in a row lock it for 30 s, and the wait doubles with each further wrong try (15 min at most).
 */
object AppLock {
    val LENGTHS = listOf(6, 8)

    private const val SALT = "app_pin_salt"
    private const val HASH = "app_pin_hash"
    private const val LEN = "app_pin_len"
    private const val FINGER = "app_pin_fingerprint"
    private const val FAILS = "app_pin_fails"
    private const val UNTIL = "app_pin_until"
    private const val ROUNDS = 100_000
    private const val FREE_TRIES = 5

    /** How long the app may be out of sight before it asks again. */
    private const val AWAY_MS = 60_000L

    /** True while the PIN (or the fingerprint) has been passed and the app was not away for long; the lock page shows when false. */
    var unlockedThisRun by mutableStateOf(false)

    private var leftAt = 0L

    private val prefs get() = getPreferencesHelper()

    fun isSet(): Boolean = !prefs.getString(HASH).isNullOrEmpty()

    /** The number of digits of the PIN that is set (6 when none is). */
    fun length(): Int = prefs.getInt(LEN, 6).takeIf { it in LENGTHS } ?: 6

    fun set(pin: String) {
        require(pin.length in LENGTHS && pin.all { it.isDigit() }) { "The PIN is 6 or 8 digits" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.setString(SALT, hex(salt))
        prefs.setString(HASH, hex(hash(pin, salt)))
        prefs.setInt(LEN, pin.length)
        prefs.setInt(FAILS, 0)
        prefs.setString(UNTIL, "0")
        unlockedThisRun = true
    }

    fun fingerprintOn(): Boolean = prefs.getBoolean(FINGER, false)

    fun setFingerprint(on: Boolean) = prefs.setBoolean(FINGER, on)

    /** Seconds still to wait before the PIN may be tried again (0 when it may be). */
    fun secondsLocked(now: Long = System.currentTimeMillis()): Int {
        val until = prefs.getString(UNTIL)?.toLongOrNull() ?: 0L
        return if (until > now) ((until - now + 999) / 1000).toInt() else 0
    }

    fun verify(pin: String, now: Long = System.currentTimeMillis()): Boolean {
        val salt = prefs.getString(SALT)?.takeIf { it.isNotEmpty() }?.let(::unhex) ?: return false
        val stored = prefs.getString(HASH)?.takeIf { it.isNotEmpty() }?.let(::unhex) ?: return false
        if (secondsLocked(now) > 0) return false
        val ok = MessageDigest.isEqual(hash(pin, salt), stored)
        if (ok) {
            prefs.setInt(FAILS, 0)
            prefs.setString(UNTIL, "0")
            unlockedThisRun = true
        } else {
            val fails = prefs.getInt(FAILS, 0) + 1
            prefs.setInt(FAILS, fails)
            if (fails >= FREE_TRIES) {
                val wait = (30_000L shl (fails - FREE_TRIES).coerceAtMost(5)).coerceAtMost(15 * 60_000L)
                prefs.setString(UNTIL, (now + wait).toString())
            }
        }
        return ok
    }

    /** The app went out of sight. */
    fun left(now: Long = System.currentTimeMillis()) { leftAt = now }

    /** The app is back in sight: it asks again if it was away for more than a minute. */
    fun returned(now: Long = System.currentTimeMillis()) {
        if (leftAt > 0L && now - leftAt > AWAY_MS) unlockedThisRun = false
        leftAt = 0L
    }

    /** Only for tests: a PIN cannot be removed in the app. */
    internal fun clearForTests() {
        prefs.setString(HASH, "")
        prefs.setString(SALT, "")
        prefs.setBoolean(FINGER, false)
        prefs.setInt(FAILS, 0)
        prefs.setString(UNTIL, "0")
        unlockedThisRun = false
    }

    private fun hash(pin: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(pin.toCharArray(), salt, ROUNDS, 256)).encoded

    private fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
    private fun unhex(s: String) = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
