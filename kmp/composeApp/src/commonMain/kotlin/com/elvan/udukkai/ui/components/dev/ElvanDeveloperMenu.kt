package com.elvan.udukkai.ui.components.dev

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.mock.SodhanaiTharavuUruvakki
import com.elvan.udukkai.data.repository.PatrugalRepository
import com.elvan.udukkai.data.repository.PattiyalRepository
import com.elvan.udukkai.data.repository.PorulRepository
import com.elvan.udukkai.data.repository.VaangunarRepository
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSnackbar
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Global floating developer pill menu that allows rapid testing, database seeding,
 * data purging, and live profile/language switches in Compose Multiplatform.
 * Matches Flutter's `ElvanUruvakkunarMenu` 1:1.
 */
@Composable
fun ElvanUruvakkunarMenu() {
    val colors = rememberShellColors()
    val isDark = colors.isDark
    val coroutineScope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(20f) }
    var offsetY by remember { mutableStateOf(100f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            // Circular toggle button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(if (isExpanded) Color(0xFFFF5252) else if (isDark) Color(0xFF2C2C2E) else Color(0xFF222222))
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) MaterialSymbols.Rounded.Close else MaterialSymbols.Rounded.Code,
                    contentDescription = "Developer Menu",
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
                    // 1. Seed All
                    DevMenuItem(
                        label = "Seed All",
                        icon = MaterialSymbols.Rounded.AutoAwesome,
                        color = Color(0xFF4CAF50),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            SodhanaiTharavuUruvakki.seedAllData()
                            val mode = ModeManager.currentMode
                            NiruvanaTharavugalRepository.refreshFromDatabase()
                            VaangunarRepository.loadAll(mode)
                            PorulRepository.loadAll(mode)
                            PattiyalRepository.loadAll(mode)
                            PatrugalRepository.loadAll(mode)
                            ElvanSnackbar.show("All Data Seeded Successfully ✓")
                            isExpanded = false
                        }
                    }

                    DevMenuDivider(isDark)

                    // 2. Erase Data
                    DevMenuItem(
                        label = "Erase Data",
                        icon = MaterialSymbols.Rounded.DeleteForever,
                        color = Color(0xFFE53935),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            SodhanaiTharavuUruvakki.eraseAllData()
                            val mode = ModeManager.currentMode
                            NiruvanaTharavugalRepository.refreshFromDatabase()
                            VaangunarRepository.loadAll(mode)
                            PorulRepository.loadAll(mode)
                            PattiyalRepository.loadAll(mode)
                            PatrugalRepository.loadAll(mode)
                            ElvanSnackbar.show("All Data Erased ✗")
                            isExpanded = false
                        }
                    }

                    DevMenuDivider(isDark)

                    // 3. Toggle Silk ±EPS
                    DevMenuItem(
                        label = "Toggle Silk ±EPS",
                        icon = MaterialSymbols.Rounded.SwapHoriz,
                        color = Color(0xFF673AB7),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = SodhanaiTharavuUruvakki.toggleExtraSilk()
                            NiruvanaTharavugalRepository.refreshFromDatabase()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 4. Toggle Coolie ±PVS
                    DevMenuItem(
                        label = "Toggle Coolie ±PVS",
                        icon = MaterialSymbols.Rounded.SwapHoriz,
                        color = Color(0xFF009688),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = SodhanaiTharavuUruvakki.toggleExtraCoolie()
                            NiruvanaTharavugalRepository.refreshFromDatabase()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 5. Toggle UI Lang
                    DevMenuItem(
                        label = "Toggle UI Lang",
                        icon = MaterialSymbols.Rounded.Translate,
                        color = Color(0xFF2196F3),
                        isDark = isDark
                    ) {
                        val msg = SodhanaiTharavuUruvakki.toggleLanguage()
                        ElvanSnackbar.show(msg)
                    }

                    DevMenuDivider(isDark)

                    // 6. Toggle Bilingual
                    DevMenuItem(
                        label = "Toggle Bilingual",
                        icon = MaterialSymbols.Rounded.Translate,
                        color = Color(0xFFFF9800),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = SodhanaiTharavuUruvakki.toggleBilingual()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 7. Swap Data Langs
                    DevMenuItem(
                        label = "Swap Data Langs",
                        icon = MaterialSymbols.Rounded.Sync,
                        color = Color(0xFFE91E63),
                        isDark = isDark
                    ) {
                        coroutineScope.launch {
                            val msg = SodhanaiTharavuUruvakki.swapDataLanguages()
                            ElvanSnackbar.show(msg)
                        }
                    }

                    DevMenuDivider(isDark)

                    // 8. Color Analyzer
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

                    // 9. Font Switcher
                    val currentAppFont = com.elvan.udukkai.theme.FontManager.currentFont
                    DevMenuItem(
                        label = "Font: ${currentAppFont.displayName}",
                        icon = MaterialSymbols.Rounded.Translate,
                        color = Color(0xFF00BCD4),
                        isDark = isDark
                    ) {
                        val nextFont = com.elvan.udukkai.theme.FontManager.toggleFont()
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
