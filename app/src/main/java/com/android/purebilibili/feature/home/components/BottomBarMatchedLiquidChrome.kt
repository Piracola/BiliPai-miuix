package com.android.purebilibili.feature.home.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.android.purebilibili.core.store.BottomBarLiquidGlassPreset
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.animation.DampedDragAnimationState
import com.android.purebilibili.core.ui.animation.rememberDampedDragAnimationState
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.motion.BottomBarMotionSpec
import com.android.purebilibili.core.ui.motion.emphasizedEnterTween
import com.android.purebilibili.core.ui.motion.emphasizedExitTween
import com.android.purebilibili.core.ui.motion.softLandingSpring
import com.android.purebilibili.feature.home.components.liquid.rememberCombinedBackdrop
import dev.chrisbanes.haze.HazeState
import com.kyant.backdrop.Backdrop as KyantBackdrop
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity

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
 * UI-only interaction state shared by the home bottom bar and every opted-in liquid Chrome.
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
    notifyIndexChangedOnReleaseStart: Boolean = false
): BottomBarMatchedLiquidChromeState {
    val motionSpec = remember { resolveSegmentedControlMotionSpec() }
    val dragState = rememberDampedDragAnimationState(
        initialIndex = initialIndex,
        itemCount = itemCount,
        motionSpec = motionSpec,
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
 * Exact Miuix/KernelSU material used by the home floating bottom bar.
 */
@Composable
internal fun BottomBarMatchedLiquidDock(
    backdrop: Backdrop?,
    legacyBackdrop: KyantBackdrop? = null,
    containerColor: Color,
    shape: Shape,
    blurEnabled: Boolean,
    glassEnabled: Boolean,
    drawShellLens: Boolean = true,
    blurRadius: Dp,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    motionTier: MotionTier = MotionTier.Normal,
    isTransitionRunning: Boolean = false,
    forceLowBlurBudget: Boolean = false,
    liquidGlassPreset: BottomBarLiquidGlassPreset = BottomBarLiquidGlassPreset.BILIPAI_TUNED,
    isScrollInProgressProvider: () -> Boolean = { false },
    materialScrollProgressOverride: Float? = null,
    materialMotionProgress: Float = 0f,
    materialPressProgress: Float = 0f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .bottomBarMatchedLiquidDockSurface(
                    shape = shape,
                    backdrop = backdrop,
                    legacyBackdrop = legacyBackdrop,
                    containerColor = containerColor,
                    blurEnabled = blurEnabled,
                    glassEnabled = glassEnabled,
                    drawShellLens = drawShellLens,
                    blurRadius = blurRadius,
                    hazeState = hazeState,
                    motionTier = motionTier,
                    isTransitionRunning = isTransitionRunning,
                    forceLowBlurBudget = forceLowBlurBudget,
                    liquidGlassPreset = liquidGlassPreset,
                    isScrollInProgressProvider = isScrollInProgressProvider,
                    materialScrollProgressOverride = materialScrollProgressOverride,
                    materialMotionProgress = materialMotionProgress,
                    materialPressProgress = materialPressProgress
                )
        )
        content()
    }
}

@Composable
internal fun Modifier.bottomBarMatchedLiquidDockSurface(
    backdrop: Backdrop?,
    legacyBackdrop: KyantBackdrop? = null,
    containerColor: Color,
    shape: Shape,
    blurEnabled: Boolean,
    glassEnabled: Boolean,
    blurRadius: Dp,
    hazeState: HazeState? = null,
    motionTier: MotionTier = MotionTier.Normal,
    isTransitionRunning: Boolean = false,
    forceLowBlurBudget: Boolean = false,
    liquidGlassPreset: BottomBarLiquidGlassPreset = BottomBarLiquidGlassPreset.BILIPAI_TUNED,
    isScrollInProgressProvider: () -> Boolean = { false },
    materialScrollProgressOverride: Float? = null,
    materialMotionProgress: Float = 0f,
    materialPressProgress: Float = 0f,
    drawShellLens: Boolean = true
): Modifier = composed {
    val isScrolling = isScrollInProgressProvider()
    val animatedScrollProgress by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0f,
        animationSpec = tween(
            durationMillis = resolveBottomBarMaterialScrollAnimationDurationMillis(isScrolling),
            easing = AppMotionEasing.Continuity
        ),
        label = "bottomBarMatchedMaterialScrollProgress"
    )
    val materialScrollProgress = materialScrollProgressOverride ?: animatedScrollProgress
    if (backdrop != null) {
        kernelSuMiuixFloatingDockSurface(
            shape = shape,
            backdrop = backdrop,
            containerColor = containerColor,
            blurEnabled = blurEnabled,
            glassEnabled = glassEnabled,
            drawShellLens = drawShellLens,
            blurRadius = blurRadius,
            hazeState = hazeState,
            motionTier = motionTier,
            isTransitionRunning = isTransitionRunning,
            forceLowBlurBudget = forceLowBlurBudget,
            liquidGlassPreset = liquidGlassPreset,
            isScrolling = isScrolling,
            materialScrollProgress = materialScrollProgress,
            materialMotionProgress = materialMotionProgress,
            materialPressProgress = materialPressProgress
        )
    } else {
        kernelSuFloatingDockSurface(
            shape = shape,
            backdrop = legacyBackdrop,
            containerColor = containerColor,
            blurEnabled = blurEnabled,
            glassEnabled = glassEnabled,
            drawShellLens = drawShellLens,
            blurRadius = blurRadius,
            hazeState = hazeState,
            motionTier = motionTier,
            isTransitionRunning = isTransitionRunning,
            forceLowBlurBudget = forceLowBlurBudget,
            liquidGlassPreset = liquidGlassPreset,
            isScrolling = isScrolling,
            materialScrollProgress = materialScrollProgress,
            materialMotionProgress = materialMotionProgress,
            materialPressProgress = materialPressProgress
        )
    }
}

/**
 * Content-slot entry point for search fields, comment/action bars, and other inline Chrome.
 * When global reuse is disabled, [content] is emitted unchanged.
 */
@Composable
internal fun BottomBarMatchedReusableLiquidDock(
    shape: Shape,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    liquidGlassEffectsEnabled: Boolean = true,
    isScrollInProgressProvider: () -> Boolean = { false },
    drawShellLens: Boolean = true,
    content: @Composable BoxScope.(liquidChromeActive: Boolean) -> Unit
) {
    val context = LocalContext.current
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(
            initialValue = HomeSettings(),
            context = kotlin.coroutines.EmptyCoroutineContext
        )
    val glassEnabled = resolveAndroidNativeBottomBarGlassEnabled(
        liquidGlassEnabled = homeSettings.androidNativeLiquidGlassEnabled,
        blurEnabled = true
    )
    if (!homeSettings.androidNativeLiquidGlassEnabled || !liquidGlassEffectsEnabled) {
        Box(modifier = modifier) {
            content(false)
        }
        return
    }

    val localBackdrop = rememberLayerBackdrop()
    val effectiveBackdrop = if (backdrop != null) {
        rememberCombinedBackdrop(localBackdrop, backdrop)
    } else {
        localBackdrop
    }
    val isDarkTheme = isSystemInDarkTheme()
    val blurIntensity = currentUnifiedBlurIntensity()
    val tuning = resolveAndroidNativeBottomBarTuning(
        blurEnabled = true,
        darkTheme = isDarkTheme
    )
    val containerColor = resolveAndroidNativeFloatingBottomBarContainerColor(
        surfaceColor = AppSurfaceTokens.cardContainer(),
        tuning = tuning,
        glassEnabled = glassEnabled,
        blurEnabled = true,
        blurIntensity = blurIntensity,
        liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset
    )
    val fullCaptureLensSpec = resolveBottomBarBackdropPresetCaptureLens(progress = 1f)
    val captureSafeInset = if (drawShellLens) {
        resolveBottomBarCaptureSafeInsetDp(
            indicatorWidthDp = 0f,
            refractionHeightDp = fullCaptureLensSpec.refractionHeightDp,
            refractionAmountDp = fullCaptureLensSpec.refractionAmountDp,
            panelOffsetDp = 0f
        ).dp
    } else {
        AppSpacingTokens.None
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .bottomBarMatchedCaptureOverflow(captureSafeInset)
                .alpha(0f)
                .layerBackdrop(localBackdrop)
                .background(AppSurfaceTokens.background())
        )
        BottomBarMatchedLiquidDock(
            backdrop = effectiveBackdrop,
            containerColor = containerColor,
            shape = shape,
            blurEnabled = true,
            glassEnabled = glassEnabled,
            blurRadius = tuning.shellBlurRadiusDp.dp,
            modifier = Modifier.matchParentSize(),
            liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset,
            isScrollInProgressProvider = isScrollInProgressProvider
        ) {}
        content(true)
    }
}

/**
 * Exact moving indicator used by the home floating bottom bar. Orientation only swaps axes.
 */
@Composable
internal fun BoxScope.BottomBarMatchedLiquidIndicator(
    visible: Boolean,
    dockContentAlpha: Float,
    indicatorTranslationXPx: Float,
    indicatorTranslationYPx: Float = 0f,
    indicatorPanelOffsetPx: Float,
    indicatorPanelOffsetYPx: Float = 0f,
    indicatorWidth: Dp,
    indicatorHeight: Dp,
    shellShape: Shape,
    liquidGlassPreset: BottomBarLiquidGlassPreset,
    contentBackdrop: Backdrop?,
    backdrop: Backdrop?,
    legacyContentBackdrop: KyantBackdrop? = null,
    legacyBackdrop: KyantBackdrop? = null,
    indicatorLensSpec: BottomBarBackdropPresetLensSpec,
    effectivePressProgress: Float,
    indicatorIdleSurfaceColor: Color,
    glassEnabled: Boolean,
    indicatorEffectsEnabled: Boolean = glassEnabled,
    motionProgress: Float,
    velocityItemsPerSecond: Float,
    isDragging: Boolean,
    indicatorLayerScaleProgress: Float,
    bottomBarMotionSpec: BottomBarMotionSpec,
    isDarkTheme: Boolean,
    indicatorSettleReboundTransform: BottomBarClickPulseTransform =
        BottomBarClickPulseTransform(scaleX = 1f),
    orientation: BottomBarLiquidOrientation = BottomBarLiquidOrientation.HORIZONTAL,
    indicatorAlignment: Alignment = Alignment.CenterStart
) {
    if (backdrop != null) {
        KernelSuMiuixBottomBarIndicatorLayer(
            visible = visible,
            dockContentAlpha = dockContentAlpha,
            indicatorTranslationXPx = indicatorTranslationXPx,
            indicatorTranslationYPx = indicatorTranslationYPx,
            indicatorPanelOffsetPx = indicatorPanelOffsetPx,
            indicatorPanelOffsetYPx = indicatorPanelOffsetYPx,
            indicatorWidth = indicatorWidth,
            indicatorHeight = indicatorHeight,
            shellShape = shellShape,
            liquidGlassPreset = liquidGlassPreset,
            contentBackdrop = contentBackdrop,
            backdrop = backdrop,
            indicatorLensSpec = indicatorLensSpec,
            effectivePressProgress = effectivePressProgress,
            indicatorIdleSurfaceColor = indicatorIdleSurfaceColor,
            glassEnabled = glassEnabled,
            indicatorEffectsEnabled = indicatorEffectsEnabled,
            motionProgress = motionProgress,
            velocityItemsPerSecond = velocityItemsPerSecond,
            isDragging = isDragging,
            indicatorLayerScaleProgress = indicatorLayerScaleProgress,
            indicatorLayerScaleTransform = null,
            bottomBarMotionSpec = bottomBarMotionSpec,
            isDarkTheme = isDarkTheme,
            swapMotionAxes = orientation == BottomBarLiquidOrientation.VERTICAL,
            indicatorAlignment = indicatorAlignment
        )
        return
    }
    KernelSuBottomBarIndicatorLayer(
        visible = visible,
        dockContentAlpha = dockContentAlpha,
        indicatorTranslationXPx = indicatorTranslationXPx,
        indicatorTranslationYPx = indicatorTranslationYPx,
        indicatorPanelOffsetPx = indicatorPanelOffsetPx,
        indicatorPanelOffsetYPx = indicatorPanelOffsetYPx,
        indicatorWidth = indicatorWidth,
        indicatorHeight = indicatorHeight,
        shellShape = shellShape,
        liquidGlassPreset = liquidGlassPreset,
        contentBackdrop = legacyContentBackdrop,
        backdrop = legacyBackdrop,
        indicatorLensSpec = indicatorLensSpec,
        indicatorSettleReboundTransform = indicatorSettleReboundTransform,
        effectivePressProgress = effectivePressProgress,
        indicatorIdleSurfaceColor = indicatorIdleSurfaceColor,
        glassEnabled = glassEnabled,
        indicatorEffectsEnabled = indicatorEffectsEnabled,
        motionProgress = motionProgress,
        velocityItemsPerSecond = velocityItemsPerSecond,
        isDragging = isDragging,
        indicatorLayerScaleProgress = indicatorLayerScaleProgress,
        indicatorLayerScaleTransform = null,
        bottomBarMotionSpec = bottomBarMotionSpec,
        isDarkTheme = isDarkTheme,
        swapMotionAxes = orientation == BottomBarLiquidOrientation.VERTICAL,
        indicatorAlignment = indicatorAlignment
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
