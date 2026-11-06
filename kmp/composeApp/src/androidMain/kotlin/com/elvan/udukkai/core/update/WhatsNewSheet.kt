package com.elvan.udukkai.core.update

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(colors.textSecondary.copy(alpha = 0.25f))
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
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

            // Aggregated release notes across skipped versions
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                    note.sections.forEach { section ->
                        NoteSectionBlock(section, isTa)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action button with progress animation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (phase == AppUpdate.Phase.Failed) colors.error else colors.accent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        AppUpdate.install(context)
                    }
                    .drawBehind {
                        if (phase == AppUpdate.Phase.Downloading) {
                            drawRect(
                                Color.White.copy(alpha = 0.28f),
                                size = Size(size.width * AppUpdate.progress, size.height)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (phase) {
                        AppUpdate.Phase.Ready -> if (isTa) "இப்போதே புதுப்பி" else "Update Now"
                        AppUpdate.Phase.Downloading -> {
                            val pct = (AppUpdate.progress * 100).toInt()
                            if (isTa) "பதிவிறக்குகிறது ($pct%)" else "Downloading ($pct%)"
                        }
                        AppUpdate.Phase.Installing -> if (isTa) "நிறுவுகிறது..." else "Installing..."
                        AppUpdate.Phase.Failed -> if (isTa) "மீண்டும் முயல்க" else "Try Again"
                    },
                    color = Color.White,
                    fontFamily = ff,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isTa) "புதுப்பிப்பு உள்ளது" else "Update",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
