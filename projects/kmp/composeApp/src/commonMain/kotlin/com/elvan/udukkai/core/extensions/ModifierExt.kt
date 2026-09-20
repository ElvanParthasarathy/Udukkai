package com.elvan.udukkai.core.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect fun Modifier.cssShadow(
    color: Color = Color.Black,
    alpha: Float = 0.05f,
    borderRadius: Dp = 50.dp,
    blurRadius: Dp = 16.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp
): Modifier
