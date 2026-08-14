package com.android.purebilibili.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.animation.DampedDragAnimationState
import com.android.purebilibili.core.ui.animation.DampedDragTrackingMode
import com.android.purebilibili.core.ui.animation.rememberDampedDragAnimationState
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.motion.emphasizedEnterTween
import com.android.purebilibili.core.ui.motion.emphasizedExitTween
import com.android.purebilibili.core.ui.motion.softLandingSpring
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import dev.chrisbanes.haze.HazeState
import top.yukonga.miuix.kmp.blur.Backdrop

internal enum class BottomBarLiquidOrientation {
    HORIZONTAL,
    VERTICAL
}

internal enum class BottomBarMatchedDockEdge {
    TOP,
    BOTTOM
}

internal fun Modifier.bottomBarMatchedCaptureOverflow(inset: Dp): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    } else {
        val insetPx = inset.roundToPx().coerceAtLeast(0)
        val expandedWidth = (constraints.maxWidth.toLong() + insetPx.toLong() * 2L)
            .coerceAtMost(Constraints.Infinity.toLong())
            .toInt()
        val expandedHeight = (constraints.maxHeight.toLong() + insetPx.toLong() * 2L)
            .coerceAtMost(Constraints.Infinity.toLong())
            .toInt()
        val placeable = measurable.measure(
            Constraints.fixed(
                width = expandedWidth,
                height = expandedHeight
            )
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(-insetPx, -insetPx)
        }
    }
}

/**
 * UI-only interaction state shared by the home bottom bar and every opted-in chrome.
 * Business selection remains owned by the caller.
 */
@Stable
internal class BottomBarMatchedLiquidChromeState internal constructor(
    internal val dragState: DampedDragAnimationState,
    val orientation: BottomBarLiquidOrientation,
    internal val isScrollInProgressProvider: () -> Boolean
) {
    val position: Float get() = dragState.value
    val targetPosition: Float get() = dragState.targetValue
    val velocityPxPerSecond: Float get() = dragState.velocityPxPerSecond
    val deformationVelocityItemsPerSecond: Float
        get() = dragState.deformationVelocityItemsPerSecond
    val pressProgress: Float get() = dragState.pressProgress
    val dragOffsetPx: Float get() = dragState.dragOffset
    val isDragging: Boolean get() = dragState.isDragging

    fun updateIndex(index: Int) = dragState.updateIndex(index)

    fun setPressed(pressed: Boolean) = dragState.setPressed(pressed)
}

@Composable
internal fun rememberBottomBarMatchedLiquidChromeState(
    initialIndex: Int,
    itemCount: Int,
    onIndexChanged: (Int) -> Unit,
    orientation: BottomBarLiquidOrientation = BottomBarLiquidOrientation.HORIZONTAL,
    isScrollInProgressProvider: () -> Boolean = { false },
    notifyIndexChangedOnReleaseStart: Boolean = false,
    pressedScale: Float = 78f / 56f,
    trackingMode: DampedDragTrackingMode = DampedDragTrackingMode.PROJECTED_SNAP,
): BottomBarMatchedLiquidChromeState {
    val motionSpec = remember { resolveSegmentedControlMotionSpec() }
    val dragState = rememberDampedDragAnimationState(
        initialIndex = initialIndex,
        itemCount = itemCount,
        motionSpec = motionSpec,
        pressedScale = pressedScale,
        trackingMode = trackingMode,
        notifyIndexChangedOnReleaseStart = notifyIndexChangedOnReleaseStart,
        holdPressUntilReleaseTargetSettles = true,
        onIndexChanged = onIndexChanged
    )
    return remember(dragState, orientation, isScrollInProgressProvider) {
        BottomBarMatchedLiquidChromeState(
            dragState = dragState,
            orientation = orientation,
            isScrollInProgressProvider = isScrollInProgressProvider
        )
    }
}

/**
 * Shared floating dock shell: solid capsule surface with optional Haze blur.
 */
@Composable
internal fun BottomBarMatchedLiquidDock(
    containerColor: Color,
    shape: Shape,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    motionTier: MotionTier = MotionTier.Normal,
    isTransitionRunning: Boolean = false,
    forceLowBlurBudget: Boolean = false,
    blurRadius: Dp = AppSpacingTokens.ExtraSmall,
    isScrollInProgressProvider: () -> Boolean = { false },
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .bottomBarMatchedLiquidDockSurface(
                    shape = shape,
                    containerColor = containerColor,
                    blurEnabled = blurEnabled,
                    blurRadius = blurRadius,
                    hazeState = hazeState,
                    motionTier = motionTier,
                    isTransitionRunning = isTransitionRunning,
                    forceLowBlurBudget = forceLowBlurBudget,
                    isScrollInProgressProvider = isScrollInProgressProvider
                )
        )
        content()
    }
}

@Composable
internal fun Modifier.bottomBarMatchedLiquidDockSurface(
    containerColor: Color,
    shape: Shape,
    blurEnabled: Boolean,
    blurRadius: Dp,
    hazeState: HazeState? = null,
    motionTier: MotionTier = MotionTier.Normal,
    isTransitionRunning: Boolean = false,
    forceLowBlurBudget: Boolean = false,
    isScrollInProgressProvider: () -> Boolean = { false },
    materialScrollProgressOverride: Float? = null
): Modifier = composed {
    val isScrolling = isScrollInProgressProvider()
    @Suppress("UNUSED_PARAMETER")
    val animatedScrollProgress = materialScrollProgressOverride
    biliPaiMiuixFloatingDockSurface(
        shape = shape,
        backdrop = null,
        containerColor = containerColor,
        blurEnabled = blurEnabled,
        glassEnabled = false,
        blurRadius = blurRadius,
        hazeState = hazeState,
        motionTier = motionTier,
        isTransitionRunning = isTransitionRunning,
        forceLowBlurBudget = forceLowBlurBudget,
        isScrolling = isScrolling
    )
}

/**
 * Content-slot entry point for search fields, comment/action bars, and other inline chrome.
 * Liquid-glass is retired: [content] is always emitted with a plain (non-glass) flag.
 */
@Composable
internal fun BottomBarMatchedReusableLiquidDock(
    shape: Shape,
    modifier: Modifier = Modifier,
    drawShellLens: Boolean = true,
    shellLensIntensity: Float = 1f,
    isScrollInProgressProvider: () -> Boolean = { false },
    content: @Composable BoxScope.(liquidChromeActive: Boolean) -> Unit
) {
    @Suppress("UNUSED_PARAMETER")
    val unused = listOf(shape, drawShellLens, shellLensIntensity, isScrollInProgressProvider)
    Box(modifier = modifier) {
        content(false)
    }
}

/**
 * Solid moving indicator used by top docks and segmented controls.
 */
@Composable
internal fun BoxScope.BottomBarMatchedLiquidIndicator(
    visible: Boolean,
    indicatorTranslationXPx: Float,
    indicatorTranslationYPx: Float = 0f,
    indicatorPanelOffsetPx: Float,
    indicatorWidth: Dp,
    indicatorHeight: Dp,
    shellShape: Shape,
    indicatorIdleSurfaceColor: Color,
    indicatorEffectsEnabled: Boolean = true,
    indicatorLayerScaleProgress: Float,
    indicatorLayerScaleTransform: BottomBarIndicatorLayerTransform? = null,
    isDarkTheme: Boolean,
    orientation: BottomBarLiquidOrientation = BottomBarLiquidOrientation.HORIZONTAL,
    indicatorAlignment: Alignment = Alignment.CenterStart
) {
    if (!visible) return
    @Suppress("UNUSED_PARAMETER")
    val unused = listOf(indicatorEffectsEnabled, indicatorLayerScaleTransform, isDarkTheme, orientation)
    Box(
        modifier = Modifier
            .align(indicatorAlignment)
            .graphicsLayer {
                translationX = indicatorTranslationXPx + indicatorPanelOffsetPx
                translationY = indicatorTranslationYPx
                scaleX = indicatorLayerScaleProgress
                scaleY = indicatorLayerScaleProgress
            }
            .width(indicatorWidth)
            .height(indicatorHeight)
            .background(indicatorIdleSurfaceColor, shellShape)
    )
}

@Composable
internal fun BottomBarMatchedDockVisibility(
    visible: Boolean,
    edge: BottomBarMatchedDockEdge,
    modifier: Modifier = Modifier,
    enterFadeDurationMillis: Int = 255,
    exitFadeDurationMillis: Int = 160,
    content: @Composable () -> Unit
) {
    val direction = if (edge == BottomBarMatchedDockEdge.BOTTOM) 1 else -1
    val transformOrigin = if (edge == BottomBarMatchedDockEdge.BOTTOM) {
        TransformOrigin(0.5f, 1f)
    } else {
        TransformOrigin(0.5f, 0f)
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = softLandingSpring(),
            initialOffsetY = { height -> direction * height }
        ) + fadeIn(animationSpec = emphasizedEnterTween(enterFadeDurationMillis)) +
            scaleIn(
                animationSpec = softLandingSpring(),
                initialScale = 0.96f,
                transformOrigin = transformOrigin
            ),
        exit = slideOutVertically(
            animationSpec = emphasizedExitTween(exitFadeDurationMillis),
            targetOffsetY = { height -> direction * height }
        ) + fadeOut(animationSpec = emphasizedExitTween(exitFadeDurationMillis)) +
            scaleOut(
                animationSpec = emphasizedExitTween(exitFadeDurationMillis),
                targetScale = 0.92f,
                transformOrigin = transformOrigin
            ),
        content = { content() }
    )
}

@Composable
internal fun BottomBarMatchedDockVisibility(
    visibleState: MutableTransitionState<Boolean>,
    edge: BottomBarMatchedDockEdge,
    modifier: Modifier = Modifier,
    enterFadeDurationMillis: Int = 255,
    exitFadeDurationMillis: Int = 160,
    content: @Composable () -> Unit
) {
    val direction = if (edge == BottomBarMatchedDockEdge.BOTTOM) 1 else -1
    val transformOrigin = if (edge == BottomBarMatchedDockEdge.BOTTOM) {
        TransformOrigin(0.5f, 1f)
    } else {
        TransformOrigin(0.5f, 0f)
    }
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = softLandingSpring(),
            initialOffsetY = { height -> direction * height }
        ) + fadeIn(animationSpec = emphasizedEnterTween(enterFadeDurationMillis)) +
            scaleIn(
                animationSpec = softLandingSpring(),
                initialScale = 0.96f,
                transformOrigin = transformOrigin
            ),
        exit = slideOutVertically(
            animationSpec = emphasizedExitTween(exitFadeDurationMillis),
            targetOffsetY = { height -> direction * height }
        ) + fadeOut(animationSpec = emphasizedExitTween(exitFadeDurationMillis)) +
            scaleOut(
                animationSpec = emphasizedExitTween(exitFadeDurationMillis),
                targetScale = 0.92f,
                transformOrigin = transformOrigin
            ),
        content = { content() }
    )
}
