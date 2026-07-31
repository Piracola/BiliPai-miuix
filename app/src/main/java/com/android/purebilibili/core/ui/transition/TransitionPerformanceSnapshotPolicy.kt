package com.android.purebilibili.core.ui.transition

import com.android.purebilibili.core.ui.adaptive.MotionTier
import androidx.compose.runtime.compositionLocalOf

/**
 * A transition receives one immutable decision before its first visual frame.
 * Runtime signals may affect the next transition, never change the current one.
 */
internal enum class TransitionPerformanceTier {
    FULL,
    BALANCED,
    CRITICAL,
}

internal enum class TransitionThermalLevel {
    NONE,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
}

internal enum class TransitionVisualBudget {
    DYNAMIC,
    FROZEN,
    SOLID_TINT,
}

internal enum class TransitionReturnOutputMode {
    LIVE_SURFACE,
    COVER_KEEP_BOUND,
    DETACH_AFTER_COVER_READY,
}

internal data class TransitionPerformanceInputs(
    val motionTier: MotionTier,
    val systemAnimationsEnabled: Boolean,
    val powerSaveMode: Boolean,
    val thermalLevel: TransitionThermalLevel,
    val runtimeGuardDowngraded: Boolean,
)

internal data class TransitionPerformanceSnapshot(
    val tier: TransitionPerformanceTier,
    val visualBudget: TransitionVisualBudget,
    val returnOutputMode: TransitionReturnOutputMode,
    val allowNavigationTextureSurface: Boolean,
    val bypassAnime4k: Boolean,
    val suppressPlayerOverlays: Boolean,
    val lockOutputRoute: Boolean,
    val force60Hz: Boolean,
)

/** Null outside a card transition; an active transition always exposes one fixed snapshot. */
internal val LocalTransitionPerformanceSnapshot = compositionLocalOf<TransitionPerformanceSnapshot?> { null }

internal fun resolveTransitionPerformanceSnapshot(
    inputs: TransitionPerformanceInputs,
): TransitionPerformanceSnapshot {
    val tier = when {
        !inputs.systemAnimationsEnabled ||
            inputs.motionTier == MotionTier.Reduced ||
            inputs.powerSaveMode ||
            inputs.thermalLevel >= TransitionThermalLevel.MODERATE ||
            inputs.runtimeGuardDowngraded -> TransitionPerformanceTier.CRITICAL

        inputs.motionTier == MotionTier.Enhanced &&
            inputs.thermalLevel <= TransitionThermalLevel.LIGHT -> TransitionPerformanceTier.FULL

        else -> TransitionPerformanceTier.BALANCED
    }
    return when (tier) {
        TransitionPerformanceTier.FULL -> TransitionPerformanceSnapshot(
            tier = tier,
            visualBudget = TransitionVisualBudget.DYNAMIC,
            returnOutputMode = TransitionReturnOutputMode.LIVE_SURFACE,
            allowNavigationTextureSurface = true,
            bypassAnime4k = true,
            suppressPlayerOverlays = false,
            lockOutputRoute = true,
            force60Hz = false,
        )

        TransitionPerformanceTier.BALANCED -> TransitionPerformanceSnapshot(
            tier = tier,
            visualBudget = TransitionVisualBudget.FROZEN,
            returnOutputMode = TransitionReturnOutputMode.DETACH_AFTER_COVER_READY,
            allowNavigationTextureSurface = false,
            bypassAnime4k = true,
            suppressPlayerOverlays = true,
            lockOutputRoute = true,
            force60Hz = false,
        )

        TransitionPerformanceTier.CRITICAL -> TransitionPerformanceSnapshot(
            tier = tier,
            visualBudget = TransitionVisualBudget.SOLID_TINT,
            returnOutputMode = TransitionReturnOutputMode.DETACH_AFTER_COVER_READY,
            allowNavigationTextureSurface = false,
            bypassAnime4k = true,
            suppressPlayerOverlays = true,
            lockOutputRoute = true,
            force60Hz = true,
        )
    }
}

internal fun shouldDetachVideoOutputAfterCoverReady(
    mode: TransitionReturnOutputMode,
    coverReady: Boolean,
): Boolean = mode == TransitionReturnOutputMode.DETACH_AFTER_COVER_READY && coverReady

/**
 * BALANCED/CRITICAL transitions keep the cover in charge from the first detail composition.
 * IDLE is intentional here: navigation composition happens before the host effect advances the
 * clock to OPENING.
 */
internal fun shouldUsePerformanceCoverTransition(
    snapshot: TransitionPerformanceSnapshot?,
    phase: VideoCardTransitionBackgroundPhase,
    isCommittedCardReturn: Boolean,
): Boolean {
    if (snapshot?.returnOutputMode == null ||
        snapshot.returnOutputMode == TransitionReturnOutputMode.LIVE_SURFACE
    ) {
        return false
    }
    return isCommittedCardReturn ||
        phase == VideoCardTransitionBackgroundPhase.IDLE ||
        phase == VideoCardTransitionBackgroundPhase.OPENING ||
        phase == VideoCardTransitionBackgroundPhase.RETURNING
}
