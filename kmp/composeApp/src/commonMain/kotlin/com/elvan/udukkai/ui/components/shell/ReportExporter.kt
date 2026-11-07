package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.export.SavedReport
import com.elvan.udukkai.core.export.openSavedReport
import com.elvan.udukkai.core.export.saveReport
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors

/** Saves reports into the app's Documents folder and then offers to open the file. */
class ReportExporter(private val businessFolder: String, private val onFailed: () -> Unit) {
    internal var saved by mutableStateOf<SavedReport?>(null)

    fun save(baseName: String, extension: String, mimeType: String, bytes: ByteArray) {
        val report = saveReport(baseName, extension, mimeType, bytes, businessFolder)
        if (report == null) onFailed() else saved = report
    }
}

/** The exporter, with the "Saved. Open it?" sheet shown after each save. */
@Composable
fun rememberReportExporter(businessFolder: String): ReportExporter {
    val failedMsg = K.excelFailed.tr()
    val noAppMsg = K.noAppToOpenFile.tr()
    val exporter = remember(businessFolder) { ReportExporter(businessFolder) { ElvanSnackbar.show(failedMsg) } }
    val report = exporter.saved
    if (report != null) {
        val colors = rememberShellColors()
        val ff = LocalAppFontFamily.current
        ElvanActionSheet(
            title = K.excelSaved.tr(),
            cancelText = K.close.tr(),
            confirmText = K.openBtn.tr(),
            onDismissRequest = { exporter.saved = null },
            onConfirm = { if (!openSavedReport(report)) ElvanSnackbar.show(noAppMsg) },
            colors = colors,
            customContent = {
                Text(
                    text = report.fileName + "\n" + report.folder.replace("Documents/", ""),
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, lineHeight = 18.sp),
                    color = colors.textPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        )
    }
    return exporter
}
