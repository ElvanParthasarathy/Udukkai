package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import kotlinx.coroutines.delay

/**
 * Runs [process] for each id one by one while showing the percentage done, with Pause/Resume and Cancel.
 * Cancel stops the rest (items already done stay done). [onFinished] gets how many ids [process] accepted.
 */
@Composable
fun ElvanBulkProgressSheet(
    title: String,
    ids: List<Long>,
    colors: ShellColors,
    process: (Long) -> Boolean,
    onFinished: (accepted: Int) -> Unit
) {
    val ff = LocalAppFontFamily.current
    var done by remember { mutableStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var cancelled by remember { mutableStateOf(false) }
    val latestProcess by rememberUpdatedState(process)
    val latestFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(ids) {
        var accepted = 0
        for (id in ids) {
            while (paused && !cancelled) delay(100)
            if (cancelled) break
            if (latestProcess(id)) accepted++
            done++
            delay(25) // lets the bar and number repaint between items
        }
        latestFinished(accepted)
    }

    val percent = if (ids.isEmpty()) 100 else done * 100 / ids.size
    ElvanActionSheet(
        title = title,
        cancelText = K.cancelBtn.tr(),
        confirmText = if (paused) K.resumeBtn.tr() else K.pauseBtn.tr(),
        keepOpenOnConfirm = true,
        dismissible = false,
        onConfirm = { paused = !paused },
        onDismissRequest = { cancelled = true },
        colors = colors,
        customContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$percent%",
                    style = TextStyle(fontFamily = ff, fontSize = 28.sp, fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$done / ${ids.size}",
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp),
                    color = colors.textPrimary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { if (ids.isEmpty()) 1f else done.toFloat() / ids.size },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = colors.textPrimary,
                    trackColor = colors.iconBg
                )
            }
        }
    )
}
