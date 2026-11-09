package com.elvan.udukkai.core.update

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetFrame
import com.elvan.udukkai.ui.navigation.MaterialSymbols

/**
 * Modal bottom sheet showing release notes and the update action button.
 * Adheres to Senthamizh language rules for Tamil labels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(
    release: AppUpdate.Release,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ff = LocalAppFontFamily.current
    val colors = rememberShellColors()
    val isTa = LocalAppLanguage.current.startsWith("ta")
    val phase = AppUpdate.phase
    // The picker's own bottom sheet: it stays the same size and the notes scroll inside it, never a full-screen sheet
    ElvanBottomSheetFrame(onDismissRequest = onDismiss, colors = colors) { maxBody ->
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = maxBody)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isTa) "புதிய மாற்றங்கள்" else "What's New",
                fontFamily = ff,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = colors.textPrimary
            )

            Spacer(Modifier.height(4.dp))

            Surface(
                shape = CircleShape,
                color = colors.textSecondary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "v${release.version}",
                    fontFamily = ff,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Aggregated release notes across skipped versions: the only part that scrolls
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (release.notes.isEmpty()) {
                    Text(
                        text = if (isTa) "செயலி மேம்பாடுகள் மற்றும் பிழைத் திருத்தங்கள் சேர்க்கப்பட்டுள்ளன." else "Performance improvements and bug fixes included.",
                        fontFamily = ff,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        color = colors.textPrimary
                    )
                } else {
                    release.notes.forEach { note ->
                        if (release.notes.size > 1) {
                            Text(
                                text = "v${note.version}",
                                fontFamily = ff,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = colors.accent
                            )
                        }
                        if (note.sections.isNotEmpty()) {
                            note.sections.forEach { section ->
                                NoteSectionBlock(section, isTa)
                            }
                        } else if (note.body.isNotBlank()) {
                            Text(
                                text = note.body,
                                fontFamily = ff,
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Action area with solid indication & matching capsule button
            if (phase == AppUpdate.Phase.Downloading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Solid Progress Track & Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colors.textSecondary.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = AppUpdate.progress.coerceIn(0.02f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(colors.accent)
                        )
                    }

                    // Progress info
                    val pct = (AppUpdate.progress * 100).toInt()
                    val mbRead = (AppUpdate.progress * (release.size.toFloat() / (1024 * 1024))).let { (it * 10).toInt() / 10f }
                    val mbTotal = (release.size.toFloat() / (1024 * 1024)).let { (it * 10).toInt() / 10f }
                    val sizeText = if (mbTotal > 0f) " ($mbRead MB / $mbTotal MB)" else ""

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTa) "பதிவிறக்குகிறது$sizeText..." else "Downloading$sizeText...",
                            fontFamily = ff,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "$pct%",
                            fontFamily = ff,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            } else if (phase == AppUpdate.Phase.Installing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = colors.accent,
                        trackColor = colors.textSecondary.copy(alpha = 0.15f)
                    )
                    Text(
                        text = if (isTa) "பின்னணியில் அமைதியாக நிறுவுகிறது..." else "Installing silently in background...",
                        fontFamily = ff,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                }
            } else {
                // Matching One UI Capsule Button
                val isDark = colors.isDark
                val isFailed = phase == AppUpdate.Phase.Failed
                val btnBg = if (isFailed) colors.error else if (isDark) Color.White else Color(0xFF1D1D1F)
                val btnText = if (isFailed) Color.White else if (isDark) Color.Black else Color.White

                val isPlay = AppUpdate.isPlayStoreChannel()
                Button(
                    onClick = { AppUpdate.install(context) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBg,
                        contentColor = btnText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = when {
                            isFailed -> if (isTa) "மீண்டும் முயல்க" else "Try Again"
                            isPlay -> if (isTa) "Google Play வழியே புதுப்பிக்குக" else "Update on Google Play"
                            else -> if (isTa) "இப்போதே புதுப்பி" else "Update Now"
                        },
                        fontFamily = ff,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = btnText
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NoteSectionBlock(section: AppUpdate.NoteSection, isTa: Boolean) {
    val ff = LocalAppFontFamily.current
    val colors = rememberShellColors()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        section.title?.let {
            Text(
                text = it,
                fontFamily = ff,
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        section.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    Modifier
                        .padding(top = 8.dp, end = 12.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                )
                Text(
                    text = item,
                    fontFamily = ff,
                    fontSize = 14.5.sp,
                    lineHeight = 21.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Compact pill showing update status; taps open the WhatsNewSheet.
 */
@Composable
fun UpdateActionPill(
    onOpenSheet: () -> Unit
) {
    val release = AppUpdate.available ?: return
    val colors = rememberShellColors()
    val isTa = LocalAppLanguage.current.startsWith("ta")
    val appear = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(350))
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = appear.value
                scaleX = 0.8f + 0.2f * appear.value
                scaleY = 0.8f + 0.2f * appear.value
            }
            .clip(CircleShape)
            .background(colors.accent)
            .clickable(onClick = onOpenSheet)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Download,
                contentDescription = if (isTa) "புதுப்பிப்பு" else "Update",
                tint = if (colors.isDark) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isTa) "புதுப்பிப்பு உள்ளது" else "Update",
                color = if (colors.isDark) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
