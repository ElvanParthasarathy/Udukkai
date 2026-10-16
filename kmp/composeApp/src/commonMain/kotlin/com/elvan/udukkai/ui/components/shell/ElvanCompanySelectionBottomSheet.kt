package com.elvan.udukkai.ui.components.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.core.platform.getNavBarBottomPadding
import com.elvan.udukkai.data.settings.NiruvanaTharavugal
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ElvanCompanySelectionBottomSheet(
    profiles: List<NiruvanaTharavugal>,
    activeProfileId: Long?,
    onSelectProfile: (Long) -> Unit,
    onManageProfiles: (() -> Unit)? = null,
    onDismissRequest: () -> Unit,
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var isClosing by remember { mutableStateOf(false) }
    val offsetY = remember { Animatable(300f) }

    fun dismiss() {
        if (isClosing) return
        isClosing = true
        coroutineScope.launch {
            offsetY.animateTo(
                targetValue = 600f,
                animationSpec = tween(durationMillis = 200, easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
            )
            onDismissRequest()
        }
    }

    LaunchedEffect(Unit) {
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 280, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))
        )
    }

    Dialog(
        onDismissRequest = { dismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ConfigureDialogWindow(isDark = isDark, clearDim = true)

        val scrimAlpha by animateFloatAsState(
            targetValue = if (!isClosing) 0.45f else 0.0f,
            animationSpec = tween(durationMillis = if (!isClosing) 280 else 200),
            label = "scrimAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { dismiss() }
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            val navBottomPadding = getNavBarBottomPadding()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            if (delta > 0 || offsetY.value > 0f) {
                                coroutineScope.launch {
                                    offsetY.snapTo((offsetY.value + delta).coerceAtLeast(0f))
                                }
                            }
                        },
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity ->
                            if (offsetY.value > 120f || velocity > 800f) {
                                dismiss()
                            } else {
                                coroutineScope.launch {
                                    offsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(180, easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f))
                                    )
                                }
                            }
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(colors.surface)
                    .padding(bottom = navBottomPadding + 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Pull Handle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.textSecondary.copy(alpha = 0.25f))
                        )
                    }

                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = K.selectCompanyProfile.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                            Text(
                                text = "${profiles.size} ${K.companies.tr()}".preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSecondary
                                )
                            )
                        }

                        if (onManageProfiles != null) {
                            TextButton(
                                onClick = {
                                    dismiss()
                                    onManageProfiles()
                                }
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colors.accent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = K.companySettings.tr().preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.accent
                                    )
                                )
                            }
                        }
                    }

                    // Company Profiles List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(profiles, key = { it.id ?: 0L }) { profile ->
                            val isSelected = profile.id == activeProfileId
                            val shortName = profile.kurumPeyar.ifEmpty {
                                profile.niruvanathinPeyar.values.firstOrNull().orEmpty()
                            }.ifEmpty { "—" }
                            val legalName = profile.niruvanathinPeyar.values.firstOrNull().orEmpty()
                            val city = profile.oor.values.firstOrNull().orEmpty()
                            val gstin = profile.gstin

                            val dotColor = try {
                                val cleanHex = profile.thoatraNiram.removePrefix("#")
                                if (cleanHex.length == 6) {
                                    Color(cleanHex.toLong(16) or 0x00000000FF000000L)
                                } else colors.accent
                            } catch (_: Exception) {
                                colors.accent
                            }

                            val cardBg = if (isSelected) {
                                colors.accent.copy(alpha = if (isDark) 0.16f else 0.08f)
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.04f) else Color.Black.copy(alpha = 0.03f)
                            }

                            val borderColor = if (isSelected) {
                                colors.accent.copy(alpha = 0.5f)
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(cardBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ShellDefaults.ripple(colors, bounded = true),
                                        onClick = {
                                            profile.id?.let { onSelectProfile(it) }
                                            dismiss()
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Company Initial Circle Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(dotColor.copy(alpha = 0.15f))
                                            .border(1.dp, dotColor.copy(alpha = 0.35f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shortName.take(1).uppercase(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = dotColor
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Details Column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = shortName.preventBrokenLigatures(),
                                            style = TextStyle(
                                                fontFamily = ff,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textPrimary
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        if (legalName.isNotEmpty() && legalName != shortName) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = legalName.preventBrokenLigatures(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = colors.textSecondary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        val subLine = listOfNotNull(
                                            gstin.takeIf { it.isNotEmpty() },
                                            city.takeIf { it.isNotEmpty() }
                                        ).joinToString(" · ")

                                        if (subLine.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = subLine.preventBrokenLigatures(),
                                                style = TextStyle(
                                                    fontFamily = ff,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = colors.textSecondary.copy(alpha = 0.8f)
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Selection Radio / Check Indicator
                                    Icon(
                                        imageVector = if (isSelected) {
                                            MaterialSymbols.Rounded.CheckCircleFill
                                        } else {
                                            MaterialSymbols.Rounded.RadioButtonUnchecked
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) colors.accent else colors.textSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
