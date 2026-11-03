package com.elvan.udukkai.core.print

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The top of a bill drawn by the real React template (see UdukkaiTemplates), shrunk to the width of the view and cut off
 * after the header; the view is 794 : 300 (a sheet's width to the part shown). [payloadJson] is a bill payload and is
 * read once; [settingsJson] is a JSON object of print settings (the logo size and place) that redraws the header at
 * once whenever it changes. Draws nothing where [isDocumentPrintSupported] is false.
 */
@Composable
expect fun BillHeaderPreview(payloadJson: String, settingsJson: String, modifier: Modifier = Modifier)
