package com.elvan.udukkai.ui.screens.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.print.DocumentPrintController
import com.elvan.udukkai.core.print.PageOrientation
import com.elvan.udukkai.core.print.PaperSize
import com.elvan.udukkai.core.print.PrintFit
import com.elvan.udukkai.core.print.PrintMargin
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors

/**
 * Print options (paper, orientation, margins, colour, fit, file name) shown above the action pill.
 * Lives inside the preview because the preview is its own dialog window.
 */
@Composable
fun PrintOptionsPanel(controller: DocumentPrintController, modifier: Modifier = Modifier) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val o = controller.options
    val isDark = colors.isDark
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(if (isDark) Color(0xFF1C1C1E) else Color.White)
            .border(0.5.dp, colors.floatingBorder.copy(alpha = if (isDark) 0.25f else 0.5f), shape)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = K.printOptions.tr().preventBrokenLigatures(),
            style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
        )

        OptionRow(K.paperSize.tr()) {
            PaperSize.entries.forEach { p ->
                OptionChip(p.label, o.paper == p) { controller.updateOptions(o.copy(paper = p)) }
            }
        }
        OptionRow(K.paperOrientation.tr()) {
            OptionChip(K.orientPortrait.tr(), o.orientation == PageOrientation.PORTRAIT) {
                controller.updateOptions(o.copy(orientation = PageOrientation.PORTRAIT))
            }
            OptionChip(K.orientLandscape.tr(), o.orientation == PageOrientation.LANDSCAPE) {
                controller.updateOptions(o.copy(orientation = PageOrientation.LANDSCAPE))
            }
        }
        OptionRow(K.printMargins.tr()) {
            OptionChip(K.marginNone.tr(), o.margin == PrintMargin.NONE) { controller.updateOptions(o.copy(margin = PrintMargin.NONE)) }
            OptionChip(K.marginNarrow.tr(), o.margin == PrintMargin.NARROW) { controller.updateOptions(o.copy(margin = PrintMargin.NARROW)) }
            OptionChip(K.marginNormal.tr(), o.margin == PrintMargin.NORMAL) { controller.updateOptions(o.copy(margin = PrintMargin.NORMAL)) }
        }
        OptionRow(K.printColor.tr()) {
            OptionChip(K.colorColor.tr(), o.color) { controller.updateOptions(o.copy(color = true)) }
            OptionChip(K.colorBlackWhite.tr(), !o.color) { controller.updateOptions(o.copy(color = false)) }
        }
        OptionRow(K.printFit.tr()) {
            OptionChip(K.fitWidth.tr(), o.fit == PrintFit.WIDTH) { controller.updateOptions(o.copy(fit = PrintFit.WIDTH)) }
            OptionChip(K.fitPage.tr(), o.fit == PrintFit.PAGE) { controller.updateOptions(o.copy(fit = PrintFit.PAGE)) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OptionLabel(K.pdfFileName.tr())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = controller.fileName,
                    onValueChange = { controller.fileName = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = ff, fontSize = 15.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.textPrimary),
                    modifier = Modifier.weight(1f)
                )
                Text(".pdf", style = TextStyle(fontFamily = ff, fontSize = 15.sp, color = colors.textPrimary.copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable
private fun OptionLabel(text: String) {
    val colors = rememberShellColors()
    Text(
        text = text.preventBrokenLigatures(),
        style = TextStyle(
            fontFamily = LocalAppFontFamily.current,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary.copy(alpha = 0.6f)
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OptionLabel(label)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun OptionChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val shape = RoundedCornerShape(50)
    val bg = when {
        selected -> colors.textPrimary
        isDark -> Color.White.copy(alpha = 0.08f)
        else -> Color.Black.copy(alpha = 0.05f)
    }
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text.preventBrokenLigatures(),
            style = TextStyle(
                fontFamily = LocalAppFontFamily.current,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) colors.background else colors.textPrimary
            )
        )
    }
}
