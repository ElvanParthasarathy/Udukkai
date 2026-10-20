package com.elvan.udukkai.core.backup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reactive Auto-Backup Manager.
 * Listens to database writes across repositories and performs a debounced atomic backup.
 * Includes automatic retry on failure and immediate flush capabilities.
 */
object AutoBackupManager {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var debounceJob: Job? = null

    /**
     * Triggered on any repository insert, update, or deletion.
     * Debounces by 3 seconds so rapid inputs don't perform excessive disk I/O.
     */
    fun onDataChanged() {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(3000)
            executeBackupWithRetry(maxRetries = 2)
        }
    }

    /**
     * Immediately triggers an atomic backup without waiting for the debounce window.
     * Useful during critical actions or when saving vital company details.
     */
    fun flushImmediate() {
        debounceJob?.cancel()
        scope.launch {
            executeBackupWithRetry(maxRetries = 2)
        }
    }

    private suspend fun executeBackupWithRetry(maxRetries: Int) {
        var attempts = 0
        var success = false
        while (!success && attempts <= maxRetries) {
            attempts++
            try {
                success = getUdukkaiBackupService().createBackup()
            } catch (_: Exception) {
                success = false
            }
            if (!success && attempts <= maxRetries) {
                delay(2000) // Brief backoff before retrying
            }
        }
    }
}
