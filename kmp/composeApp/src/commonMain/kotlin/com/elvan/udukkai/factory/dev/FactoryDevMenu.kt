package com.elvan.udukkai.factory.dev

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.FontManager
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Floating developer menu for Udukkai Factory (ஆலை).
 * Strictly for debug builds only (`isDebugBuild`).
 */
@Composable
fun FactoryDevMenu() {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(with(density) { 16.dp.toPx() }) }
    var offsetY by remember { mutableStateOf(with(density) { 90.dp.toPx() }) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        ) {
            // Circular draggable & clickable toggle button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(if (isExpanded) Color(0xFFFF5252) else if (isDark) Color(0xFF2C2C2E) else Color(0xFF222222))
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var isDrag = false
                            var lastPos = down.position
                            val pointerId = down.id
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                if (!change.pressed) break
                                val dx = change.position.x - lastPos.x
                                val dy = change.position.y - lastPos.y
                                val totalDist = (change.position - down.position).getDistance()
                                if (totalDist > 8f) {
                                    isDrag = true
                                    offsetX += dx
                                    offsetY += dy
                                    lastPos = change.position
                                    change.consume()
                                }
                            }
                            if (!isDrag) {
                                isExpanded = !isExpanded
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) MaterialSymbols.Rounded.Close else MaterialSymbols.Rounded.Code,
                    contentDescription = "Factory Dev Menu",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(220.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1E1E1E) else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isDark) Color(0xFF333333) else Color(0x1F000000),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    // 1. Seed Factory Data
                    DevMenuItem(
                        label = "Seed Factory Data",
                        icon = MaterialSymbols.Rounded.AutoAwesome,
                        color = Color(0xFF4CAF50),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            FactoryMockDataSeeder.seedAllData()
                            ElvanSnackbar.show("Factory Data Seeded Successfully ✓")
                        }
                    }

                    DevMenuDivider(isDark)

                    // 2. Erase Data
                    DevMenuItem(
                        label = "Erase Data",
                        icon = MaterialSymbols.Rounded.DeleteForever,
                        color = Color(0xFFF44336),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            FactoryMockDataSeeder.eraseAllData()
                            ElvanSnackbar.show("Factory Data Purged ✗")
                        }
                    }

                    DevMenuDivider(isDark)

                    // 3. Toggle Factory ±PVS
                    DevMenuItem(
                        label = "Toggle Factory ±PVS",
                        icon = MaterialSymbols.Rounded.SwapHoriz,
                        color = Color(0xFF009688),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = FactoryMockDataSeeder.toggleExtraProfile()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 4. Toggle UI Lang
                    DevMenuItem(
                        label = "Toggle UI Lang",
                        icon = MaterialSymbols.Rounded.Translate,
                        color = Color(0xFF2196F3),
                        isDark = isDark
                    ) {
                        val msg = FactoryMockDataSeeder.toggleLanguage()
                        ElvanSnackbar.show(msg)
                    }

                    DevMenuDivider(isDark)

                    // 5. Toggle Bilingual
                    DevMenuItem(
                        label = "Toggle Bilingual",
                        icon = MaterialSymbols.Rounded.Translate,
                        color = Color(0xFFFF9800),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = FactoryMockDataSeeder.toggleBilingual()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 6. Swap Data Langs
                    DevMenuItem(
                        label = "Swap Data Langs",
                        icon = MaterialSymbols.Rounded.Sync,
                        color = Color(0xFFE91E63),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = FactoryMockDataSeeder.swapDataLanguages()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 7. Color Analyzer
                    DevMenuItem(
                        label = "Color Analyzer",
                        icon = MaterialSymbols.Rounded.Palette,
                        color = Color(0xFFFFB300),
                        isDark = isDark
                    ) {
                        isExpanded = false
                        ElvanSnackbar.show("Theme: ${if (isDark) "Dark Mode (கரிய பயன்முறை)" else "Light Mode (வெளிர் பயன்முறை)"}")
                    }

                    DevMenuDivider(isDark)

                    // 8. Font Switcher
                    val currentAppFont = FontManager.currentFont
                    DevMenuItem(
                        label = "Font: ${currentAppFont.displayName}",
                        icon = MaterialSymbols.Rounded.Translate,
                        color = Color(0xFF00BCD4),
                        isDark = isDark
                    ) {
                        val nextFont = FontManager.toggleFont()
                        ElvanSnackbar.show("Font: ${nextFont.displayName}")
                    }
                }
            }
        }
    }
}

@Composable
private fun DevMenuItem(
    label: String,
    icon: ImageVector,
    color: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDark) Color(0xFFEEEEEE) else Color(0xFF222222)
        )
    }
}

@Composable
private fun DevMenuDivider(isDark: Boolean) {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = if (isDark) Color(0x1AFFFFFF) else Color(0x14000000)
    )
}
