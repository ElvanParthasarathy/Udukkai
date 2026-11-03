package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.backup.*
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.components.shell.ElvanLoadingOverlay
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.components.shell.rememberBackupFilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Restoring from one backup file, safely (Textiles and Factory):
 * 1. the user picks a file; it is checked (whole, readable, the right app) and its contents are counted, nothing is touched;
 * 2. the user sees what is in it and chooses: add what is missing (the default: nothing that is in the app changes, and
 *    each kind of data can be left out) or replace everything (asks again, and says how many bills would go);
 * 3. a copy of the current data is kept first, the change is made in one step that is undone as a whole if anything
 *    fails, and the result is told in numbers.
 * Returns the function that starts it; the sheets are drawn where this is called. [onRestored] runs after a restore that worked.
 */
@Composable
fun restoreFlow(colors: ShellColors, onRestored: () -> Unit): () -> Unit {
    val ff = LocalAppFontFamily.current
    val service = remember { getUdukkaiBackupService() }
    val scope = rememberCoroutineScope()

    var checking by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf<Pair<String, BackupPreview>?>(null) }
    var mode by remember { mutableStateOf(RestoreMode.MERGE) }
    var chosen by remember { mutableStateOf(RestoreCategory.entries.toSet()) }
    var confirmReplace by remember { mutableStateOf<Pair<String, BackupPreview>?>(null) }
    var result by remember { mutableStateOf<RestoreReport?>(null) }

    val problems = mapOf(
        BackupProblem.NOT_A_BACKUP to K.restoreNotABackup.tr(),
        BackupProblem.DAMAGED to K.restoreDamaged.tr(),
        BackupProblem.OTHER_APP to K.restoreOtherApp.tr(),
        BackupProblem.NO_SAFETY_COPY to K.restoreNoSafetyCopy.tr(),
        BackupProblem.NOTHING_CHOSEN to K.restoreNothingChosen.tr(),
        BackupProblem.FAILED to K.restoreFailedNoChange.tr(),
        BackupProblem.UNSUPPORTED to K.restoreUnsupported.tr()
    )
    val categoryNames = mapOf(
        RestoreCategory.BUSINESSES to K.restoreBusinesses.tr(),
        RestoreCategory.CUSTOMERS to K.customers.tr(),
        RestoreCategory.ITEMS to K.items.tr(),
        RestoreCategory.BILLS to K.restoreBills.tr()
    )
    val checkingText = K.restoreChecking.tr()
    val restoringText = K.restoringData.tr()

    fun counts(p: BackupPreview) = mapOf(
        RestoreCategory.BUSINESSES to p.businesses, RestoreCategory.CUSTOMERS to p.customers,
        RestoreCategory.ITEMS to p.items, RestoreCategory.BILLS to p.invoices + p.receipts
    )

    fun run(path: String) {
        working = true
        scope.launch {
            val report = withContext(Dispatchers.Default) {
                try { service.restoreFromFile(path, mode, chosen) } catch (_: Throwable) { RestoreReport(ok = false, problem = BackupProblem.FAILED) }
            }
            working = false
            if (report.ok) onRestored()
            result = report
        }
    }

    val picker = rememberBackupFilePicker { path ->
        checking = true
        scope.launch {
            val preview = withContext(Dispatchers.Default) {
                try { service.inspectBackupFile(path) } catch (_: Throwable) { BackupPreview(problem = BackupProblem.DAMAGED) }
            }
            checking = false
            if (preview.ok) {
                mode = RestoreMode.MERGE
                chosen = RestoreCategory.entries.toSet()
                picked = path to preview
            } else {
                ElvanSnackbar.show(problems[preview.problem] ?: problems.getValue(BackupProblem.FAILED))
            }
        }
    }

    val muted = TextStyle(fontFamily = ff, fontSize = 12.sp)
    val body = TextStyle(fontFamily = ff, fontSize = 14.sp)

    // 1. What is in the file, and what to do with it
    picked?.let { (path, preview) ->
        ElvanActionSheet(
            title = K.restoreFromFile.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.restoreBtn.tr(),
            onDismissRequest = { picked = null },
            onConfirm = {
                if (mode == RestoreMode.REPLACE) confirmReplace = path to preview else run(path)
            },
            customContent = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${K.restoreFileHas.tr()} ${DateUtils.formatDateTime(preview.createdAt)}",
                        style = muted, color = colors.textPrimary.copy(alpha = 0.6f)
                    )
                    Text(
                        counts(preview).entries.joinToString("  ·  ") { "${it.value} ${categoryNames.getValue(it.key)}" },
                        style = body, color = colors.textPrimary
                    )

                    Spacer(Modifier.height(2.dp))
                    ChoiceRow(mode == RestoreMode.MERGE, K.restoreMerge.tr(), K.restoreMergeHint.tr(), colors, radio = true) { mode = RestoreMode.MERGE }
                    ChoiceRow(mode == RestoreMode.REPLACE, K.restoreReplace.tr(), K.restoreReplaceHint.tr(), colors, radio = true) { mode = RestoreMode.REPLACE }

                    if (mode == RestoreMode.MERGE) {
                        Spacer(Modifier.height(2.dp))
                        Text(K.restoreWhat.tr(), style = muted, color = colors.textPrimary.copy(alpha = 0.6f))
                        RestoreCategory.entries.forEach { category ->
                            val n = counts(preview).getValue(category)
                            ChoiceRow(category in chosen, "${categoryNames.getValue(category)} ($n)", null, colors, radio = false) {
                                // At least one kind stays chosen
                                chosen = if (category in chosen) (if (chosen.size > 1) chosen - category else chosen) else chosen + category
                            }
                        }
                    }
                }
            },
            colors = colors
        )
    }

    // 2. Replacing everything asks once more, and says what would be lost
    confirmReplace?.let { (path, preview) ->
        val live = remember { service.getLiveStats() }
        val fewer = (live.invoiceCount - preview.invoices).coerceAtLeast(0)
        ElvanActionSheet(
            title = K.restoreReplaceTitle.tr(),
            cancelText = K.cancelBtn.tr(),
            confirmText = K.restoreReplace.tr(),
            onDismissRequest = { confirmReplace = null },
            onConfirm = { run(path) },
            confirmColor = Color(0xFFE53935),
            isConfirmFilled = true,
            customContent = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(K.restoreReplaceWarn.tr(), style = body, color = colors.textPrimary)
                    if (fewer > 0) {
                        Text(
                            "${K.restoreYouHave.tr()} ${live.invoiceCount} ${K.invoices.tr()}, ${K.restoreFileHas2.tr()} ${preview.invoices}: $fewer ${K.restoreWouldGo.tr()}",
                            style = body.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFFE53935)
                        )
                    }
                    Text(K.restoreUndoNote.tr(), style = muted, color = colors.textPrimary.copy(alpha = 0.6f))
                }
            },
            colors = colors
        )
    }

    // 3. What happened, in numbers
    result?.let { report ->
        ElvanActionSheet(
            title = if (report.ok) K.restoreDone.tr() else K.restoreNotDone.tr(),
            cancelText = K.confirm.tr(),
            confirmText = "",
            onDismissRequest = { result = null },
            onConfirm = {},
            customContent = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (report.ok) {
                        report.added.forEach { (category, added) ->
                            val kept = report.kept[category] ?: 0
                            Text(
                                "${categoryNames.getValue(category)}: $added ${K.restoreAdded.tr()}" + if (kept > 0) ", $kept ${K.restoreAlreadyThere.tr()}" else "",
                                style = body, color = colors.textPrimary
                            )
                        }
                    } else {
                        Text(problems[report.problem] ?: problems.getValue(BackupProblem.FAILED), style = body, color = colors.textPrimary)
                    }
                }
            },
            colors = colors
        )
    }

    if (checking) ElvanLoadingOverlay(text = checkingText, colors = colors)
    if (working) ElvanLoadingOverlay(text = restoringText, colors = colors)

    return { picker.launch() }
}

@Composable
private fun ChoiceRow(selected: Boolean, title: String, hint: String?, colors: ShellColors, radio: Boolean, onClick: () -> Unit) {
    val ff = LocalAppFontFamily.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (radio) RadioButton(selected = selected, onClick = onClick) else Checkbox(checked = selected, onCheckedChange = { onClick() })
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary)
            if (hint != null) Text(hint, style = TextStyle(fontFamily = ff, fontSize = 12.sp), color = colors.textPrimary.copy(alpha = 0.6f))
        }
    }
}
