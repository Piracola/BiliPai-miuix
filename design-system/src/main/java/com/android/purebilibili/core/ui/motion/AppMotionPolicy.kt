package com.android.purebilibili.core.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppMotionMode {
    FOLLOW_SYSTEM,
    OFF,
}

@Immutable
data class AppMotionPolicy(
    val mode: AppMotionMode,
    val systemReduceMotion: Boolean,
    val allowPredictiveBack: Boolean,
    val transientVisibilityDurationMillis: Int,
    val allowDecorativeMotion: Boolean = false,
) {
    companion object {
        fun default(
            mode: AppMotionMode = AppMotionMode.FOLLOW_SYSTEM,
            systemReduceMotion: Boolean = false,
        ): AppMotionPolicy {
            val activeMotion = mode == AppMotionMode.FOLLOW_SYSTEM && !systemReduceMotion
            return AppMotionPolicy(
                mode = mode,
                systemReduceMotion = systemReduceMotion,
                allowPredictiveBack = !systemReduceMotion,
                transientVisibilityDurationMillis = if (activeMotion) 100 else 0,
            )
        }
    }
}

val LocalAppMotionPolicy = staticCompositionLocalOf { AppMotionPolicy.default() }

val AppMotionPolicy.isMotionEnabled: Boolean
    get() = mode == AppMotionMode.FOLLOW_SYSTEM && !systemReduceMotion

fun AppMotionPolicy.resolveAppNavigationTransform(): ContentTransform =
    EnterTransition.None togetherWith ExitTransition.None

fun AppMotionPolicy.resolveTransientVisibilitySpec() =
    tween<Float>(durationMillis = transientVisibilityDurationMillis)

fun AppMotionPolicy.shouldRunDecorativeMotion(): Boolean = false

fun AppMotionPolicy.shouldUsePlatformPredictiveBack(): Boolean = allowPredictiveBack
