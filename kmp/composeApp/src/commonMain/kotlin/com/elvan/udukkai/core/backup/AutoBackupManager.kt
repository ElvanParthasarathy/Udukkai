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
 * 
 * Replaces the Flutter autoBackupProvider with a robust 3-second debouncer.
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
            try {
                getNirilBackupService().createBackup()
            } catch (_: Exception) {}
        }
    }
}
