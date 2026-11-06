package com.elvan.udukkai.textiles.ui.screens.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.backup.BackupSlot
import com.elvan.udukkai.core.backup.getUdukkaiBackupService
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.textiles.data.TextilesCustomerRepository
import com.elvan.udukkai.textiles.data.TextilesInvoiceRepository
import com.elvan.udukkai.textiles.data.TextilesItemRepository
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.data.TextilesReceiptRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.screens.settings.restoreFlow
import com.elvan.udukkai.ui.screens.settings.undoRestoreDetails
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Textiles Storage & Backup Screen (சவுளி சேமிப்பகமும் காப்பும்).
 * Dedicated to Udukkai Textiles with zero cross-app branching or flags.
 */
@Composable
fun TextilesStorageBackupScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val backupService = remember { getUdukkaiBackupService() }

    var showBackupConfirm by remember { mutableStateOf(false) }
    var showUndoConfirm by remember { mutableStateOf(false) }
    val guard = rememberPinGuard(colors)
    // Bumped after a restore so the "go back" row appears at once
    var restoreTick by remember { mutableStateOf(0) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    var backupStats by remember { mutableStateOf(backupService.getBackupStats()) }
    var totalDbSize by remember { mutableStateOf(backupService.getTotalDatabaseSize().coerceAtLeast(128 * 1024L)) }
    var availableBackups by remember { mutableStateOf(backupService.getAllAvailableBackups()) }

    val backupFailedMsg = K.backupFailed.tr()
    val backupSuccessMsg = K.backupSavedToDocuments.tr()
    val restoreSuccessMsg = K.restoreSuccess.tr()

    fun refreshAll() {
        restoreTick++
        backupStats = backupService.getBackupStats()
        totalDbSize = backupService.getTotalDatabaseSize().coerceAtLeast(128 * 1024L)
        availableBackups = backupService.getAllAvailableBackups()
    }

    val startRestore = restoreFlow(colors) { refreshAll() }

    val latestBackupMeta = availableBackups.firstOrNull { it.slot == BackupSlot.LATEST }
    val lastBackupFormatted = remember(latestBackupMeta) {
        if (latestBackupMeta != null) {
            DateUtils.formatDateTime(latestBackupMeta.lastModified)
        } else {
            null
        }
    }
    val backupSize = backupStats?.sizeBytes ?: 0L

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> "${(mb * 100).toInt() / 100.0} MB"
            kb >= 1.0 -> "${(kb * 100).toInt() / 100.0} KB"
            else -> "$bytes B"
        }
    }

    val maxSpace = 1024.0 * 1024.0 * 1024.0 // 1 GB
    val progress = (totalDbSize / maxSpace).toFloat().coerceIn(0.02f, 1f)

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── 1. Storage Projection Pill ──
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = K.databaseStore.tr(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${formatBytes(totalDbSize)} / 1 GB",
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.textPrimary.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(durationMillis = 1000)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(colors.textPrimary.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(100.dp))
                                .background(colors.textPrimary)
                        )
                    }
                }
            }
        }

        // ── 2. Storage Actions Section ──
        item {
            val sync = remember { com.elvan.udukkai.core.sync.getFirebaseSyncManager() }
            val syncStatus by sync.syncStatus
            val lastSync by sync.lastSyncTime
            val syncStatusText = when (syncStatus) {
                com.elvan.udukkai.core.sync.SyncStatus.SYNCED -> K.syncStatusSynced.tr()
                com.elvan.udukkai.core.sync.SyncStatus.SYNCING -> K.syncStatusSyncing.tr()
                com.elvan.udukkai.core.sync.SyncStatus.OFFLINE -> K.syncStatusOffline.tr()
                com.elvan.udukkai.core.sync.SyncStatus.ERROR, com.elvan.udukkai.core.sync.SyncStatus.GUARD_TRIGGERED -> K.syncStatusError.tr()
                com.elvan.udukkai.core.sync.SyncStatus.IDLE -> K.syncStatusIdle.tr()
            }

            ElvanSettingsSection(colors = colors) {
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Sync,
                    title = syncStatusText,
                    description = if (lastSync > 0L) "${K.lastSyncedAt.tr()}: ${DateUtils.formatDateTime(lastSync)}" else null,
                    onClick = { sync.triggerManualSync() },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Folder,
                    title = K.storageUsed.tr(),
                    description = "${formatBytes(totalDbSize)} ${K.databaseStore.tr()}\n${formatBytes(backupSize)} ${K.backup.tr()}",
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Schedule,
                    title = K.lastAutoBackup.tr(),
                    description = lastBackupFormatted ?: K.noBackupYet.tr(),
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.CloudUpload,
                    title = K.backupData.tr(),
                    description = K.manuallyUpdateBackup.tr(),
                    onClick = { showBackupConfirm = true },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Restore,
                    title = K.restoreFromFile.tr(),
                    description = K.restoreFromFileHint.tr(),
                    onClick = startRestore,
                    colors = colors
                )
                if (restoreTick >= 0 && backupService.lastRestoreBackup() != null) {
                    ElvanSettingsDivider(colors = colors)
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Restore,
                        title = K.undoRestore.tr(),
                        description = undoRestoreDetails(backupService),
                        onClick = { guard { showUndoConfirm = true } },
                        colors = colors
                    )
                }
                ElvanSettingsDivider(colors = colors)

                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Share,
                    title = K.shareBackupBtn.tr(),
                    description = "${K.backup.tr()} (${latestBackupMeta?.let { formatBytes(it.sizeBytes) } ?: "0 B"})",
                    onClick = {
                        guard {
                            val ok = backupService.shareBackup(BackupSlot.LATEST)
                            if (!ok) {
                                ElvanSnackbar.show(backupFailedMsg)
                            }
                        }
                    },
                    colors = colors
                )
            }
        }

        // ── 3. Live Textiles Databases Grid Section ──
        item {
            val appBadge = K.udukkaiTextiles.tr()
            val companyLabel = K.company.tr()

            val profiles = TextilesProfileRepository.profiles
            val fallback = TextilesProfileRepository.profile

            val businessItems = remember(profiles, fallback) {
                val list = mutableListOf<TextilesBusinessDisplayItem>()
                if (profiles.isEmpty()) {
                    val name = fallback.shortName.ifBlank {
                        fallback.getPrimary("businessName")
                    }.ifBlank {
                        fallback.getSecondary("niruvanathinPeyar")
                    }.ifBlank { appBadge }

                    val town = fallback.city.values.firstOrNull { it.isNotBlank() }
                    val subtitleText = if (!town.isNullOrBlank()) "$appBadge • $town" else appBadge

                    val invCount = TextilesInvoiceRepository.invoices.count { (fallback.id == null || it.companyId == fallback.id) && !it.isDeleted }
                    val recCount = TextilesReceiptRepository.receipts.count { (fallback.id == null || it.companyId == fallback.id) && !it.isDeleted }
                    val cusCount = TextilesCustomerRepository.customers.count { !it.isDeleted }
                    val proCount = TextilesItemRepository.items.count { (fallback.id == null || it.isAvailableForCompany(fallback.id)) && !it.isDeleted }

                    list.add(
                        TextilesBusinessDisplayItem(
                            name = name,
                            subtitle = subtitleText,
                            invoiceCount = invCount,
                            receiptCount = recCount,
                            customerCount = cusCount,
                            itemCount = proCount
                        )
                    )
                } else {
                    profiles.forEachIndexed { index, p ->
                        val bName = p.shortName.ifBlank {
                            p.getPrimary("businessName")
                        }.ifBlank {
                            p.getSecondary("niruvanathinPeyar")
                        }.ifBlank {
                            "$companyLabel ${index + 1}"
                        }

                        val town = p.city.values.firstOrNull { it.isNotBlank() }
                        val sub = if (!town.isNullOrBlank()) "$appBadge • $town" else appBadge

                        val invC = TextilesInvoiceRepository.invoices.count { (p.id == null || it.companyId == p.id) && !it.isDeleted }
                        val recC = TextilesReceiptRepository.receipts.count { (p.id == null || it.companyId == p.id) && !it.isDeleted }
                        val cusC = TextilesCustomerRepository.customers.count { !it.isDeleted }
                        val proC = TextilesItemRepository.items.count { (p.id == null || it.isAvailableForCompany(p.id)) && !it.isDeleted }

                        list.add(
                            TextilesBusinessDisplayItem(
                                name = bName,
                                subtitle = sub,
                                invoiceCount = invC,
                                receiptCount = recC,
                                customerCount = cusC,
                                itemCount = proC
                            )
                        )
                    }
                }
                list
            }

            ElvanSettingsSection(colors = colors) {
                businessItems.forEachIndexed { index, item ->
                    if (index > 0) {
                        ElvanSettingsDivider(colors = colors)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = colors.iconBg,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.BusinessCenter,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                )
                                Text(
                                    text = item.subtitle.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val gridStyle = TextStyle(fontFamily = ff, fontSize = 12.sp, color = colors.textPrimary.copy(alpha = 0.5f))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 50.dp)) {
                            Text("${item.invoiceCount} ${K.invoices.tr()}", style = gridStyle, modifier = Modifier.weight(1f))
                            Text("${item.receiptCount} ${K.receipts.tr()}", style = gridStyle, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 50.dp)) {
                            Text("${item.customerCount} ${K.customers.tr()}", style = gridStyle, modifier = Modifier.weight(1f))
                            Text("${item.itemCount} ${K.items.tr()}", style = gridStyle, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Backup Confirmation Action Sheet
    if (showBackupConfirm) {
        ElvanActionSheet(
            title = K.backupDatabase.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.backupNowBtn.tr(),
            onDismissRequest = { showBackupConfirm = false },
            onConfirm = {
                showBackupConfirm = false
                isBackingUp = true
                scope.launch {
                    delay(250)
                    val success = backupService.createBackup()
                    isBackingUp = false
                    if (success) {
                        refreshAll()
                        ElvanSnackbar.show(backupSuccessMsg)
                    } else {
                        ElvanSnackbar.show(backupFailedMsg)
                    }
                }
            },
            colors = colors
        )
    }

    // Going back to the copy kept just before the last restore
    if (showUndoConfirm) {
        ElvanActionSheet(
            title = K.undoRestore.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.restoreBtn.tr(),
            onDismissRequest = { showUndoConfirm = false },
            onConfirm = {
                showUndoConfirm = false
                isRestoring = true
                scope.launch {
                    delay(250)
                    val success = backupService.undoLastRestore()
                    isRestoring = false
                    if (success) {
                        refreshAll()
                        ElvanSnackbar.show(restoreSuccessMsg)
                    } else {
                        ElvanSnackbar.show(backupFailedMsg)
                    }
                }
            },
            customContent = {
                Text(
                    text = (K.undoRestoreHint.tr() + "\n\n" + undoRestoreDetails(backupService)).preventBrokenLigatures(),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            },
            colors = colors
        )
    }

    if (isBackingUp) {
        ElvanLoadingOverlay(
            text = K.backingUp.tr(),
            colors = colors
        )
    }

    if (isRestoring) {
        ElvanLoadingOverlay(
            text = K.restoringData.tr(),
            colors = colors
        )
    }
}

private data class TextilesBusinessDisplayItem(
    val name: String,
    val subtitle: String,
    val invoiceCount: Int,
    val receiptCount: Int,
    val customerCount: Int,
    val itemCount: Int
)
