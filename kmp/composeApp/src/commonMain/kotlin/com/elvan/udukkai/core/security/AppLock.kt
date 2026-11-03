package com.elvan.udukkai.core.security

import com.elvan.udukkai.core.platform.getPreferencesHelper
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * The app PIN: six digits, kept only as a salted PBKDF2 hash (never the digits). With a PIN set, the app asks for it when it
 * opens and before the actions that can lose or give away data (replacing data by a restore, going back to an earlier
 * copy, deleting a business, sharing a backup, signing out). Wrong tries slow down: five wrong in a row lock it for 30 s,
 * and the wait doubles with each further wrong try (at most 15 min).
 * shortcut: no fingerprint; it needs the AndroidX biometric library, add it when the build can fetch it.
 */
object AppLock {
    const val LENGTH = 6

    private const val SALT = "app_pin_salt"
    private const val HASH = "app_pin_hash"
    private const val FAILS = "app_pin_fails"
    private const val UNTIL = "app_pin_until"
    private const val ROUNDS = 100_000
    private const val FREE_TRIES = 5

    /** True once the PIN was entered in this run of the app, so the opening page is asked once. */
    var unlockedThisRun = false

    private val prefs get() = getPreferencesHelper()

    fun isSet(): Boolean = !prefs.getString(HASH).isNullOrEmpty()

    fun set(pin: String) {
        require(pin.length == LENGTH && pin.all { it.isDigit() }) { "The PIN is $LENGTH digits" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.setString(SALT, hex(salt))
        prefs.setString(HASH, hex(hash(pin, salt)))
        prefs.setInt(FAILS, 0)
        prefs.setString(UNTIL, "0")
        unlockedThisRun = true
    }

    /** Seconds still to wait before the PIN may be tried again (0 when it may be). */
    fun secondsLocked(now: Long = System.currentTimeMillis()): Int {
        val until = prefs.getString(UNTIL)?.toLongOrNull() ?: 0L
        return if (until > now) ((until - now + 999) / 1000).toInt() else 0
    }

    fun verify(pin: String, now: Long = System.currentTimeMillis()): Boolean {
        val salt = prefs.getString(SALT)?.let(::unhex) ?: return false
        val stored = prefs.getString(HASH)?.let(::unhex) ?: return false
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

    fun remove() {
        prefs.setString(HASH, "")
        prefs.setString(SALT, "")
        prefs.setInt(FAILS, 0)
        prefs.setString(UNTIL, "0")
    }

    private fun hash(pin: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(pin.toCharArray(), salt, ROUNDS, 256)).encoded

    private fun hex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
    private fun unhex(s: String) = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}
