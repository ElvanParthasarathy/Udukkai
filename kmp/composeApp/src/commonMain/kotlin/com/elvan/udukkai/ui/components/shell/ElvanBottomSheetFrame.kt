package com.elvan.udukkai.ui.components.shell

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elvan.udukkai.core.platform.ConfigureDialogWindow
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * The bottom sheet of [ElvanSelectionBottomSheet] (the item picker of the invoice editor) as a frame for any content: a
 * Dialog with the dim scrim, the sheet anchored to the bottom with its own colour, rounded top corners and drag handle, the
 * same slide in and out, drag-down or tap outside to close. It is only as tall as its content, never past [maxBodyHeight]
 * (at most 460dp), so a long text scrolls inside it and the sheet stays the same size instead of growing to the full screen.
 */
@Composable
fun ElvanBottomSheetFrame(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ShellColors = rememberShellColors(),
    content: @Composable ColumnScope.(maxBodyHeight: Dp) -> Unit
) {
    val isDark = colors.isDark
    val glass = remember { com.elvan.udukkai.core.platform.dialogBlurAvailable() }
    val sheetBg = LocalShellColors.current.surface.copy(alpha = if (glass) 0.88f else 0.95f)

    val coroutineScope = rememberCoroutineScope()
    val offscreenPx = with(androidx.compose.ui.platform.LocalDensity.current) { 1000.dp.toPx() }
    val sheetOffsetY = remember { Animatable(offscreenPx) }
    var isClosing by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        sheetOffsetY.animateTo(0f, tween(300, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)))
    }

    fun dismissSheet() {
        if (isClosing) return
        isClosing = true
        coroutineScope.launch {
            sheetOffsetY.snapTo(sheetOffsetY.value + dragOffsetY)
            dragOffsetY = 0f
            sheetOffsetY.animateTo(offscreenPx, tween(240, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)))
            onDismissRequest()
        }
    }

    // Downwards only: the sheet never lifts off the bottom edge
    val draggableState = rememberDraggableState { delta -> dragOffsetY = (dragOffsetY + delta).coerceAtLeast(0f) }

    fun snapBack() {
        if (dragOffsetY <= 0f) return
        coroutineScope.launch {
            Animatable(dragOffsetY).animateTo(0f, tween(160, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))) { dragOffsetY = value }
        }
    }

    Dialog(
        onDismissRequest = { dismissSheet() },
        properties = com.elvan.udukkai.core.platform.edgeToEdgeDialogProperties()
    ) {
        ConfigureDialogWindow(isDark = isDark, clearDim = true)

        val scrimAlpha by animateFloatAsState(
            targetValue = if (!isClosing) 0.5f else 0.0f,
            animationSpec = tween(durationMillis = if (!isClosing) 300 else 240),
            label = "sheetFrameScrim"
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { dismissSheet() }
        ) {
            // What is left under the handle and above the navigation bar, within the picker's 460dp
            val maxBodyHeight = (maxHeight - 48.dp - 24.dp).coerceIn(120.dp, 460.dp)

            // Sideways the sheet is the same sheet turned to the right edge: full height, slim, scrolling inside
            val side = maxWidth > maxHeight && com.elvan.udukkai.core.platform.currentPlatform != com.elvan.udukkai.core.platform.PlatformType.DESKTOP
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = if (side) Alignment.CenterEnd else Alignment.BottomCenter) {
                Surface(
                    shape = if (side) RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp) else RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = sheetBg,
                    contentColor = colors.textPrimary,
                    shadowElevation = 16.dp,
                    modifier = (if (side) modifier.width(340.dp).fillMaxHeight().offset { IntOffset((sheetOffsetY.value + dragOffsetY).roundToInt(), 0) }
                        else modifier.widthIn(max = 560.dp).fillMaxWidth().offset { IntOffset(0, (sheetOffsetY.value + dragOffsetY).roundToInt()) })
                        // A tap inside the sheet does not close it
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (side) Modifier.fillMaxHeight().padding(top = 8.dp) else Modifier)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp)
                    ) {
                        // The handle: drag it down to close
                        if (side) Spacer(Modifier.height(16.dp)) else Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggable(
                                    state = draggableState,
                                    orientation = Orientation.Vertical,
                                    onDragStopped = { velocity -> if (dragOffsetY > 100f || velocity > 800f) dismissSheet() else snapBack() }
                                )
                                .padding(top = 16.dp, bottom = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.2f))
                            )
                        }
                        content(maxBodyHeight)
                    }
                }
            }
        }
    }
}
