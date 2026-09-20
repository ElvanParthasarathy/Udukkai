package com.elvan.udukkai.ui.components.shell.sheets

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet

/**
 * ElvanDeleteConfirmSheet — Delete confirmation action sheet matching Flutter's `elvan_azhippu_urudhi_maeladukku.dart`.
 */
@Composable
fun ElvanDeleteConfirmSheet(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    title: String = K.deleteCompanyProfilePrompt.tr(),
    cancelText: String = K.cancel.tr(),
    confirmText: String = K.delete.tr(),
    colors: ShellColors = rememberShellColors()
) {
    ElvanActionSheet(
        title = title,
        cancelText = cancelText,
        confirmText = confirmText,
        confirmColor = MaterialTheme.colorScheme.error,
        onDismissRequest = onDismissRequest,
        onConfirm = onConfirm,
        colors = colors
    )
}
