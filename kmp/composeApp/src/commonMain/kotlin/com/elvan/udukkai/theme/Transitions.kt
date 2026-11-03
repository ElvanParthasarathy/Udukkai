package com.elvan.udukkai.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

/**
 * Material 3 / Android Platform Transitions
 * Implements WhatsApp & native Android style platform slide with lockstep parallax:
 * Both outgoing and incoming screens move synchronously from Frame 0 using StandardDecelerate.
 */
object Transitions {

    /**
     * Standard native platform horizontal screen transition (WhatsApp / Android platform style).
     * Forward: Incoming screen slides in from right (100% to 0) with StandardDecelerate (260ms),
     *          while outgoing screen slides left by 25% parallax and dims slightly in exact lockstep.
     * Backward: Outgoing screen slides out to right (0 to 100%) with StandardDecelerate (220ms),
     *           moving IMMEDIATELY from frame 0 with zero delay, while incoming screen slides
     *           from -25% parallax to 0 and brightens in lockstep.
     *
     * targetContentZIndex is 1f on forward (incoming on top) and -1f on backward (outgoing stays on top).
     */
    fun <S> AnimatedContentTransitionScope<S>.smoothHorizontal(forward: Boolean): ContentTransform {
        return if (forward) {
            (
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(
                        durationMillis = 260,
                        easing = MotionTokens.StandardDecelerate
                    )
                ) togetherWith (
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = MotionTokens.StandardDecelerate
                        ),
                        targetOffset = { fullWidth -> fullWidth / 4 }
                    ) + fadeOut(
                        targetAlpha = 0.85f,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = MotionTokens.StandardDecelerate
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
                            durationMillis = 220,
                            easing = MotionTokens.StandardDecelerate
                        ),
                        initialOffset = { fullWidth -> fullWidth / 4 }
                    ) + fadeIn(
                        initialAlpha = 0.85f,
                        animationSpec = tween(
                            durationMillis = 220,
                            easing = MotionTokens.StandardDecelerate
                        )
                    )
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = MotionTokens.StandardDecelerate
                    )
                )
            ).apply {
                targetContentZIndex = -1f
            }
        }
    }

    /**
     * Standard native platform vertical screen transition (for modal-style subpages like ManageProfiles).
     */
    fun <S> AnimatedContentTransitionScope<S>.smoothVertical(forward: Boolean): ContentTransform {
        return if (forward) {
            (
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(
                        durationMillis = 260,
                        easing = MotionTokens.StandardDecelerate
                    )
                ) togetherWith (
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = MotionTokens.StandardDecelerate
                        ),
                        targetOffset = { fullHeight -> fullHeight / 6 }
                    ) + fadeOut(
                        targetAlpha = 0.9f,
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = MotionTokens.StandardDecelerate
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
                            durationMillis = 220,
                            easing = MotionTokens.StandardDecelerate
                        ),
                        initialOffset = { fullHeight -> fullHeight / 6 }
                    ) + fadeIn(
                        initialAlpha = 0.9f,
                        animationSpec = tween(
                            durationMillis = 220,
                            easing = MotionTokens.StandardDecelerate
                        )
                    )
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(
                        durationMillis = 220,
                        easing = MotionTokens.StandardDecelerate
                    )
                )
            ).apply {
                targetContentZIndex = -1f
            }
        }
    }

    fun <S> AnimatedContentTransitionScope<S>.smoothBackSlideHorizontal(): ContentTransform =
        smoothHorizontal(forward = false)

    fun <S> AnimatedContentTransitionScope<S>.platformSlideHorizontal(forward: Boolean): ContentTransform =
        smoothHorizontal(forward)

    fun <S> AnimatedContentTransitionScope<S>.platformSlideVertical(forward: Boolean): ContentTransform =
        smoothVertical(forward)

    fun sharedAxisX(forward: Boolean): ContentTransform {
        return (slideInHorizontally(
            initialOffsetX = { fullWidth -> if (forward) fullWidth else -fullWidth / 4 },
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.StandardDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                easing = MotionTokens.StandardDecelerate
            )
        )) togetherWith (slideOutHorizontally(
            targetOffsetX = { fullWidth -> if (forward) -fullWidth / 4 else fullWidth },
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.StandardDecelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        ))
    }

    fun sharedAxisY(forward: Boolean): ContentTransform {
        val targetOffsetY = if (forward) 300 else -300
        val initialOffsetY = if (forward) 300 else -300

        return (slideInVertically(
            initialOffsetY = { initialOffsetY },
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.StandardDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                easing = MotionTokens.StandardDecelerate
            )
        )) togetherWith (slideOutVertically(
            targetOffsetY = { -targetOffsetY / 4 },
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.StandardDecelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        ))
    }

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
                easing = MotionTokens.StandardDecelerate
            )
        )) togetherWith (fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        ) + scaleOut(
            targetScale = targetScale,
            animationSpec = tween(
                durationMillis = MotionTokens.DurationLong,
                easing = MotionTokens.StandardDecelerate
            )
        ))
    }

    fun fadeThrough(): ContentTransform {
        return (fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                delayMillis = 90,
                easing = MotionTokens.StandardDecelerate
            )
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(
                durationMillis = MotionTokens.DurationMedium,
                delayMillis = 90,
                easing = MotionTokens.StandardDecelerate
            )
        )) togetherWith (fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        ))
    }
    
    fun menuPopup(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f),
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
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
            transformOrigin = TransformOrigin(1f, 0f),
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = MotionTokens.DurationShort,
                easing = MotionTokens.StandardDecelerate
            )
        )
    }
}
