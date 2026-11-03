package com.elvan.udukkai.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

/**
 * Material 3 Transitions
 * Implements standard patterns: Normal platform slide with parallax, Shared Axis, Fade Through,
 * Elevation Scale, and menu popups.
 */
object Transitions {

    /**
     * Standard native platform horizontal screen transition.
     * Forward: Incoming screen slides in from right (100% to 0) with EmphasizedDecelerate,
     *          while outgoing screen slides left by 25% parallax and dims slightly.
     * Backward: Outgoing screen slides out to right (0 to 100%) with EmphasizedAccelerate,
     *           while incoming screen slides from -25% parallax to 0 and brightens.
     *
     * targetContentZIndex is set to 1f on forward (incoming on top) and -1f on backward
     * (outgoing stays on top while sliding away), completely preventing card-shuffle artifacts
     * even during rapid navigation.
     */
    fun <S> AnimatedContentTransitionScope<S>.smoothHorizontal(forward: Boolean): ContentTransform {
        return if (forward) {
            (
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DurationMedium,
                        easing = MotionTokens.EmphasizedDecelerate
                    )
                ) togetherWith (
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationMedium,
                            easing = MotionTokens.EmphasizedDecelerate
                        ),
                        targetOffset = { fullWidth -> fullWidth / 4 }
                    ) + fadeOut(
                        targetAlpha = 0.85f,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationMedium,
                            easing = MotionTokens.StandardAccelerate
                        )
                    )
                )
            ).apply {
                targetContentZIndex = 1f
            }
        } else {
            (
                (
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationShort,
                            easing = MotionTokens.EmphasizedDecelerate
                        ),
                        initialOffset = { fullWidth -> fullWidth / 4 }
                    ) + fadeIn(
                        initialAlpha = 0.85f,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationShort,
                            easing = MotionTokens.StandardDecelerate
                        )
                    )
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DurationShort,
                        easing = MotionTokens.EmphasizedAccelerate
                    )
                )
            ).apply {
                targetContentZIndex = -1f
            }
        }
    }

    /**
     * Standard native platform vertical screen transition (for modal-style subpages like ManageProfiles).
     * Forward: Incoming screen slides up smoothly from bottom on top.
     * Backward: Exiting screen remains on top and slides down smoothly, revealing the parent screen underneath.
     */
    fun <S> AnimatedContentTransitionScope<S>.smoothVertical(forward: Boolean): ContentTransform {
        return if (forward) {
            (
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DurationMedium,
                        easing = MotionTokens.EmphasizedDecelerate
                    )
                ) togetherWith (
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationMedium,
                            easing = MotionTokens.EmphasizedDecelerate
                        ),
                        targetOffset = { fullHeight -> fullHeight / 6 }
                    ) + fadeOut(
                        targetAlpha = 0.9f,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationShort,
                            easing = MotionTokens.StandardAccelerate
                        )
                    )
                )
            ).apply {
                targetContentZIndex = 1f
            }
        } else {
            (
                (
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationShort,
                            easing = MotionTokens.EmphasizedDecelerate
                        ),
                        initialOffset = { fullHeight -> fullHeight / 6 }
                    ) + fadeIn(
                        initialAlpha = 0.9f,
                        animationSpec = tween(
                            durationMillis = MotionTokens.DurationShort,
                            easing = MotionTokens.StandardDecelerate
                        )
                    )
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DurationShort,
                        easing = MotionTokens.EmphasizedAccelerate
                    )
                )
            ).apply {
                targetContentZIndex = -1f
            }
        }
    }

    /**
     * Slide-only transition for returning to a parent screen.
     * Kept for backward compatibility, routes directly to smoothHorizontal(forward = false).
     */
    fun <S> AnimatedContentTransitionScope<S>.smoothBackSlideHorizontal(): ContentTransform {
        return smoothHorizontal(forward = false)
    }

    /**
     * Standard Android platform / WhatsApp style horizontal screen transition.
     * Alias to smoothHorizontal.
     */
    fun <S> AnimatedContentTransitionScope<S>.platformSlideHorizontal(forward: Boolean): ContentTransform {
        return smoothHorizontal(forward)
    }

    /**
     * Standard Android platform style vertical screen transition (for modal-style subpages).
     * Alias to smoothVertical.
     */
    fun <S> AnimatedContentTransitionScope<S>.platformSlideVertical(forward: Boolean): ContentTransform {
        return smoothVertical(forward)
    }

    /**
     * Shared Axis X (Horizontal)
     * Used for hierarchical navigation (Forward/Backward)
     */
    fun sharedAxisX(forward: Boolean): ContentTransform {
        return (slideInHorizontally(
            initialOffsetX = { fullWidth -> if (forward) fullWidth else -fullWidth / 4 },
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.EmphasizedDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                easing = MotionTokens.StandardDecelerate
            )
        )) togetherWith (slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth }, // Parallax effect
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.EmphasizedAccelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardAccelerate
            )
        ))
    }

    /**
     * Shared Axis Y (Vertical)
     * Used for entering/exiting modal-like screens or lists
     */
    fun sharedAxisY(forward: Boolean): ContentTransform {
        val targetOffsetY = if (forward) 300 else -300
        val initialOffsetY = if (forward) 300 else -300

        return (slideInVertically(
            initialOffsetY = { initialOffsetY },
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.EmphasizedDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                easing = MotionTokens.StandardDecelerate
            )
        )) togetherWith (slideOutVertically(
            targetOffsetY = { -targetOffsetY / 4 }, // Parallax effect
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.EmphasizedAccelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardAccelerate
            )
        ))
    }

    /**
     * Shared Axis Z (Scale)
     * Used for parent-child transitions where user stays on same "plane" but zooms in
     */
    fun sharedAxisZ(forward: Boolean): ContentTransform {
        val initialScale = if (forward) 0.8f else 1.1f
        val targetScale = if (forward) 1.1f else 0.8f

        return (fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                easing = MotionTokens.StandardDecelerate
            )
        ) + scaleIn(
            initialScale = initialScale,
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.EmphasizedDecelerate
            )
        )) togetherWith (fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardAccelerate
            )
        ) + scaleOut(
            targetScale = targetScale,
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.EmphasizedAccelerate
            )
        ))
    }

    /**
     * Fade Through
     * Used for switching between peer UI elements (Tabs, Bottom Nav)
     */
    fun fadeThrough(): ContentTransform {
        return (fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                delayMillis = 90, // Wait for outgoing to start fading
                easing = MotionTokens.EmphasizedDecelerate
            )
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                delayMillis = 90,
                easing = MotionTokens.EmphasizedDecelerate
            )
        )) togetherWith (fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardAccelerate
            )
        ))
    }
    
    /**
     * Menu Popup Enter/Exit
     * Expands from top right corner (Scale/Zoom)
     */
    fun menuPopup(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f), // Top Right
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.EmphasizedDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        )
    }

    fun menuPopupExit(): ExitTransition {
        return scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f), // Top Right
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.EmphasizedAccelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardAccelerate
            )
        )
    }
}
