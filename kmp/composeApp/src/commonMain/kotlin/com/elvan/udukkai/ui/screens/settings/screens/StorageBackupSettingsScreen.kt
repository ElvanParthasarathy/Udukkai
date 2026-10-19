package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.backup.BackupItemMetadata
import com.elvan.udukkai.core.backup.BackupSlot
import com.elvan.udukkai.core.backup.getNirilBackupService
import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.AppSvgs
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Storage & Backup Screen.
 * Features:
 * - Live dynamic storage projection & counts.
 * - Reactive auto-backup state with rotating slots (Latest, Previous, Safety Vault).
 * - In-app safe Restore flow with automatic pre-restore safety snapshot.
 * - Native export/share trigger.
 */
@Composable
fun StorageBackupSettingsScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    val backupService = remember { getNirilBackupService() }

    var showBackupConfirm by remember { mutableStateOf(false) }
    var showRestoreSelectSheet by remember { mutableStateOf(false) }
    var slotToRestore by remember { mutableStateOf<BackupSlot?>(null) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }

    var liveStats by remember { mutableStateOf(backupService.getLiveStats()) }
    var backupStats by remember { mutableStateOf(backupService.getBackupStats()) }
    var totalDbSize by remember { mutableStateOf(backupService.getTotalDatabaseSize().coerceAtLeast(128 * 1024L)) }
    var availableBackups by remember { mutableStateOf(backupService.getAllAvailableBackups()) }

    val backupFailedMsg = K.backupFailed.tr()
    val backupSuccessMsg = K.backupSavedToDocuments.tr()
    val restoreSuccessMsg = K.restoreSuccess.tr()

    fun refreshAll() {
        liveStats = backupService.getLiveStats()
        backupStats = backupService.getBackupStats()
        totalDbSize = backupService.getTotalDatabaseSize().coerceAtLeast(128 * 1024L)
        availableBackups = backupService.getAllAvailableBackups()
    }

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
        // Top spacer driven by One UI collapsible header
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
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
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
            ElvanSettingsSection(colors = colors) {
                // Used Storage
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Folder,
                    title = K.storageUsed.tr(),
                    description = "${formatBytes(totalDbSize)} ${K.databaseStore.tr()}\n${formatBytes(backupSize)} ${K.backup.tr()}",
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // Last Auto Backup
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Schedule,
                    title = K.lastAutoBackup.tr(),
                    description = lastBackupFormatted ?: K.noBackupYet.tr(),
                    onClick = {},
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // Backup Data Button
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.CloudUpload,
                    title = K.backupData.tr(),
                    description = K.manuallyUpdateBackup.tr(),
                    onClick = { showBackupConfirm = true },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // Restore Data Button
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Restore,
                    title = K.restoreDataBtn.tr(),
                    description = K.restorePreviousDataPrompt.tr(),
                    onClick = {
                        refreshAll()
                        showRestoreSelectSheet = true
                    },
                    colors = colors
                )
                ElvanSettingsDivider(colors = colors)

                // Share / Export Backup Button
                ElvanSettingsRow(
                    icon = MaterialSymbols.Rounded.Share,
                    title = K.shareBackupBtn.tr(),
                    description = "${K.backup.tr()} (${latestBackupMeta?.let { formatBytes(it.sizeBytes) } ?: "0 B"})",
                    onClick = {
                        val ok = backupService.shareBackup(BackupSlot.LATEST)
                        if (!ok) {
                            ElvanSnackbar.show(backupFailedMsg)
                        }
                    },
                    colors = colors
                )
            }
        }

        // ── 3. Live Databases Grid Section (Business-by-Business) ──
        item {
            val fixedMode = AppLaunchConfig.fixedMode
            val modes = when (fixedMode) {
                AppMode.KOOLI -> listOf(AppMode.KOOLI)
                AppMode.PATTU -> listOf(AppMode.PATTU)
                null -> listOf(AppMode.KOOLI, AppMode.PATTU)
            }

            val factoryDataLabel = K.factoryData.tr()
            val textilesDataLabel = K.textilesData.tr()
            val companyLabel = K.company.tr()

            val businessItems = remember(liveStats, factoryDataLabel, textilesDataLabel, companyLabel, NiruvanaTharavugalRepository.getProfile(AppMode.KOOLI), NiruvanaTharavugalRepository.getProfile(AppMode.PATTU)) {
                val list = mutableListOf<BusinessDisplayItem>()
                for (m in modes) {
                    val profiles = NiruvanaTharavugalRepository.getAllProfiles(m)
                    val modeLabel = if (m == AppMode.KOOLI) factoryDataLabel else textilesDataLabel

                    if (profiles.isEmpty()) {
                        val fallback = NiruvanaTharavugalRepository.getProfile(m)
                        val name = fallback.kurumPeyar.ifBlank {
                            fallback.getPrimary("niruvanathinPeyar")
                        }.ifBlank { modeLabel }

                        list.add(
                            BusinessDisplayItem(
                                mode = m,
                                name = name,
                                subtitle = modeLabel,
                                invoiceCount = PattiyalRepository.invoices.count { (fallback.id == null || it.niruvanamId == fallback.id) && !it.isDeleted },
                                receiptCount = PatrugalRepository.receipts.count { (fallback.id == null || it.niruvanamId == fallback.id) && !it.isDeleted },
                                customerCount = VaangunarRepository.merchants.count { !it.isDeleted },
                                productCount = PorulRepository.items.count { (fallback.id == null || it.isAvailableForCompany(fallback.id)) && !it.isDeleted }
                            )
                        )
                    } else {
                        profiles.forEachIndexed { index, p ->
                            val bName = p.kurumPeyar.ifBlank {
                                p.getPrimary("niruvanathinPeyar")
                            }.ifBlank {
                                p.getSecondary("niruvanathinPeyar")
                            }.ifBlank {
                                "$companyLabel ${index + 1}"
                            }

                            val sub = p.oor.values.firstOrNull { it.isNotBlank() } ?: modeLabel

                            val invC = PattiyalRepository.invoices.count { (p.id == null || it.niruvanamId == p.id) && !it.isDeleted }
                            val recC = PatrugalRepository.receipts.count { (p.id == null || it.niruvanamId == p.id) && !it.isDeleted }
                            val cusC = VaangunarRepository.merchants.count { !it.isDeleted }
                            val proC = PorulRepository.items.count { (p.id == null || it.isAvailableForCompany(p.id)) && !it.isDeleted }

                            list.add(
                                BusinessDisplayItem(
                                    mode = m,
                                    name = bName,
                                    subtitle = sub,
                                    invoiceCount = invC,
                                    receiptCount = recC,
                                    customerCount = cusC,
                                    productCount = proC
                                )
                            )
                        }
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
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = colors.iconBg,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (item.mode == AppMode.KOOLI) AppSvgs.coolieMode else AppSvgs.silkMode,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.textPrimary
                                )
                                if (item.subtitle.isNotBlank() && item.subtitle != item.name) {
                                    Text(
                                        text = item.subtitle,
                                        style = TextStyle(
                                            fontFamily = ff,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = colors.textPrimary.copy(alpha = 0.5f)
                                    )
                                }
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
                            Text("${item.productCount} ${K.products.tr()}", style = gridStyle, modifier = Modifier.weight(1f))
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

    // Available Backups Selection Sheet
    if (showRestoreSelectSheet) {
        ElvanActionSheet(
            title = K.availableBackups.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = "",
            onDismissRequest = { showRestoreSelectSheet = false },
            onConfirm = {},
            customContent = {
                if (availableBackups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = K.noBackupsAvailable.tr(),
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 14.sp,
                                color = colors.textSecondary
                            )
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableBackups.forEach { backup ->
                            val slotName = when (backup.slot) {
                                BackupSlot.LATEST -> K.backupLatest.tr()
                                BackupSlot.PREVIOUS -> K.backupPrevious.tr()
                                BackupSlot.SAFETY_VAULT -> K.safetyVault.tr()
                            }
                            val dateString = DateUtils.formatDateTime(backup.lastModified)
                            val summary = if (backup.invoiceCount > 0 || backup.customerCount > 0) {
                                "${backup.invoiceCount} ${K.invoices.tr()} • ${backup.customerCount} ${K.customers.tr()}"
                            } else {
                                formatBytes(backup.sizeBytes)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        slotToRestore = backup.slot
                                        showRestoreSelectSheet = false
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (backup.slot == BackupSlot.SAFETY_VAULT) colors.accent.copy(alpha = 0.15f) else colors.iconBg,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (backup.slot == BackupSlot.SAFETY_VAULT) MaterialSymbols.Rounded.Lock else MaterialSymbols.Rounded.Backup,
                                                contentDescription = null,
                                                tint = if (backup.slot == BackupSlot.SAFETY_VAULT) colors.accent else colors.textPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = slotName,
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                        )
                                        Text(
                                            text = "$dateString • $summary",
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        )
                                    }
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            colors = colors
        )
    }

    // Restore Pre-Confirmation Sheet
    if (slotToRestore != null) {
        val targetSlot = slotToRestore!!
        ElvanActionSheet(
            title = K.restoreDataBtn.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.restoreBtn.tr(),
            onDismissRequest = { slotToRestore = null },
            onConfirm = {
                val slot = targetSlot
                slotToRestore = null
                isRestoring = true
                scope.launch {
                    delay(250)
                    val success = backupService.restoreFromSlot(slot)
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
                    text = K.restoreConfirmPrompt.tr().preventBrokenLigatures(),
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    ),
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

private data class BusinessDisplayItem(
    val mode: AppMode,
    val name: String,
    val subtitle: String,
    val invoiceCount: Int,
    val receiptCount: Int,
    val customerCount: Int,
    val productCount: Int
)

