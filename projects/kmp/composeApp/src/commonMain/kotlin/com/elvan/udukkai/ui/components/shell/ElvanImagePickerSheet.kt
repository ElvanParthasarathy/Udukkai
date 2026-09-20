package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * ElvanImagePickerSheet — Upward floating action sheet for picking images.
 * Replicates Flutter's `showElvanImagePickerSheet` (`elvan_padam_thaervu_maeladukku.dart`).
 * Offers options for Gallery ("புகைப்படத் தொகுப்பு") and Files ("கோப்புகள்").
 */
@Composable
fun ElvanImagePickerSheet(
    onDismissRequest: () -> Unit,
    onPickGallery: () -> Unit,
    onPickFiles: () -> Unit,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current

    ElvanActionSheet(
        title = K.selectImageSource.tr(),
        cancelText = K.cancelBtn.tr(),
        confirmText = "",
        onDismissRequest = onDismissRequest,
        onConfirm = {},
        colors = colors,
        customContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActionOption(
                    icon = MaterialSymbols.Rounded.PhotoLibrary,
                    label = K.gallery.tr(),
                    onClick = {
                        onDismissRequest()
                        onPickGallery()
                    },
                    colors = colors,
                    fontFamily = ff
                )
                ActionOption(
                    icon = MaterialSymbols.Rounded.FolderOpen,
                    label = K.files.tr(),
                    onClick = {
                        onDismissRequest()
                        onPickFiles()
                    },
                    colors = colors,
                    fontFamily = ff
                )
            }
        }
    )
}

@Composable
private fun ActionOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    colors: ShellColors,
    fontFamily: FontFamily?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colors.textPrimary
        )
    }
}
