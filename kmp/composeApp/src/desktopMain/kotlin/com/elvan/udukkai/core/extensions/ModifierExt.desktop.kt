package com.elvan.udukkai.core.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

actual fun Modifier.cssShadow(
    color: Color,
    alpha: Float,
    borderRadius: Dp,
    blurRadius: Dp,
    offsetY: Dp,
    offsetX: Dp
): Modifier = this
