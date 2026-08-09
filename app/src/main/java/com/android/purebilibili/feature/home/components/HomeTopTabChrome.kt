package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.OpticalContrastPalette

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.purebilibili.feature.home.HomeTopTabGestureAction
import com.android.purebilibili.feature.home.resolveHomeTopTabGestureAction
import com.android.purebilibili.core.store.BottomBarLiquidGlassPreset
import com.android.purebilibili.core.store.LiquidGlassStyle
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.kyant.backdrop.backdrops.LayerBackdrop
import dev.chrisbanes.haze.HazeState
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop

@Composable
internal fun HomeTopTabChrome(
    currentTabHeight: Dp,
    tabAlpha: Float,
    tabContentAlpha: Float,
    containerZIndex: Float = -1f,
    tabHorizontalPadding: Dp,
    tabVerticalPadding: Dp,
    tabVerticalOffset: Dp,
    isTabFloating: Boolean,
    effectiveTabShadowElevation: Dp,
    tabShape: Shape,
    tabChromeRenderMode: HomeTopChromeRenderMode,
    tabSurfaceColor: Color,
    hazeState: HazeState?,
    backdrop: LayerBackdrop?,
    miuixBackdrop: MiuixBackdrop? = null,
    liquidStyle: LiquidGlassStyle,
    liquidGlassTuning: LiquidGlassTuning? = null,
    liquidGlassPreset: BottomBarLiquidGlassPreset = BottomBarLiquidGlassPreset.BILIPAI_TUNED,
    motionTier: MotionTier,
    isScrolling: Boolean,
    isTransitionRunning: Boolean,
    forceLowBlurBudget: Boolean,
    preferFlatGlass: Boolean,
    tabBorderAlpha: Float,
    tabHighlightColor: Color,
    tabContentUnderlayColor: Color,
    gestureEnabled: Boolean = false,
    isTabsCollapsed: Boolean = false,
    onTabsCollapsedChange: ((Boolean) -> Unit)? = null,
    drawChromeSurface: Boolean = true,
    useBottomBarMatchedSurface: Boolean = false,
    /**
     * 顶栏分类 dock 默认开 soft shell lens（[TOP_DOCK_SHELL_LENS_INTENSITY]）：
     * 保留上下滑动液态折射以贴近指示器，强度低于底栏整壳以避免虾线。
     * 搜索等小胶囊仍应显式传 false。
     */
    drawMatchedShellLens: Boolean = true,
    matchedShellLensIntensity: Float = TOP_DOCK_SHELL_LENS_INTENSITY,
    /**
     * When true, the floating dock shell shrinks to tab content width (icon/text density ×
     * count) and centers in the padded track — no full-bleed empty glass on the right.
     */
    wrapDockWidth: Boolean = false,
    dockCategoryCount: Int = 0,
    dockLabelMode: Int = 2,
    /**
     * Cap on the dock width so the tab strip never exceeds the top controls'
     * combined width (avatar + search pill + settings) — keeps left/right edges
     * aligned with the search row. [Dp.Infinity] keeps legacy full-bleed docks.
     */
    maxDockWidth: Dp = Dp.Infinity,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val gestureThresholdPx = with(density) {
        (AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Small).toPx()
    }
    val showCollapsedHandle = gestureEnabled && isTabsCollapsed
    val safeTabHorizontalPadding = tabHorizontalPadding.coerceAtLeast(AppSpacingTokens.None)
    val safeTabVerticalPadding = tabVerticalPadding.coerceAtLeast(AppSpacingTokens.None)
    val containerAlpha = if (showCollapsedHandle) {
        tabAlpha
    } else {
        tabAlpha * tabContentAlpha
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(containerZIndex)
            .height(currentTabHeight)
            .graphicsLayer { alpha = containerAlpha }
            .offset { IntOffset(x = 0, y = tabVerticalOffset.roundToPx()) }
            .then(
                if (gestureEnabled && onTabsCollapsedChange != null) {
                    Modifier.pointerInput(isTabsCollapsed, gestureThresholdPx) {
                        var accumulatedDragY = 0f
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                accumulatedDragY += dragAmount
                            },
                            onDragCancel = {
                                accumulatedDragY = 0f
                            },
                            onDragEnd = {
                                when (
                                    resolveHomeTopTabGestureAction(
                                        dragDeltaPx = accumulatedDragY,
                                        isCollapsed = isTabsCollapsed,
                                        thresholdPx = gestureThresholdPx
                                    )
                                ) {
                                    HomeTopTabGestureAction.COLLAPSE -> onTabsCollapsedChange(true)
                                    HomeTopTabGestureAction.EXPAND -> onTabsCollapsedChange(false)
                                    HomeTopTabGestureAction.NONE -> Unit
                                }
                                accumulatedDragY = 0f
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = safeTabHorizontalPadding, vertical = safeTabVerticalPadding)
        ) {
            val shouldWrap = wrapDockWidth &&
                dockCategoryCount > 0 &&
                maxWidth > AppSpacingTokens.None
            // 分栏 dock 宽度封顶于顶部三控件合计宽度。
            val cappedMaxWidth = minOf(maxWidth.value, maxDockWidth.value)
            val dockWidth = if (shouldWrap) {
                val preferredItem = resolveTopTabWrapItemWidthDp(
                    labelMode = dockLabelMode,
                    isFloatingStyle = isTabFloating
                )
                resolveTopTabDockWrapWidthDp(
                    itemWidthDp = preferredItem,
                    categoryCount = dockCategoryCount,
                    maxWidthDp = cappedMaxWidth
                ).dp
            } else {
                maxWidth
            }
            // 包裹 dock 始终在可用顶部区域内居中，不受标签样式和数量影响。
            val dockAlignment = Alignment.Center
            // clipToBounds：文字、选中态与阴影一律裁剪在胶囊容器边界内，
            // 不再溢出到屏幕边缘（首尾 tab 的选中态阴影此前会撞屏）。
            val dockModifier = Modifier
                .align(dockAlignment)
                .width(dockWidth)
                .widthIn(max = maxDockWidth)
                .fillMaxHeight()
                .clipToBounds()

            Box(
                modifier = dockModifier
                    .then(
                        if (drawChromeSurface && effectiveTabShadowElevation > AppSpacingTokens.None) {
                            Modifier.shadow(
                                elevation = effectiveTabShadowElevation,
                                shape = tabShape,
                                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (drawChromeSurface) {
                            if (useBottomBarMatchedSurface) {
                                Modifier.homeTopBottomBarMatchedSurface(
                                    renderMode = tabChromeRenderMode,
                                    shape = tabShape,
                                    hazeState = hazeState,
                                    backdrop = backdrop,
                                    miuixBackdrop = miuixBackdrop,
                                    liquidGlassStyle = liquidStyle,
                                    liquidGlassTuning = liquidGlassTuning,
                                    liquidGlassPreset = liquidGlassPreset,
                                    motionTier = motionTier,
                                    isTransitionRunning = isTransitionRunning,
                                    forceLowBlurBudget = forceLowBlurBudget,
                                    drawShellLens = drawMatchedShellLens,
                                    shellLensIntensity = matchedShellLensIntensity,
                                    isScrolling = isScrolling
                                )
                            } else {
                                Modifier.homeTopChromeSurface(
                                    renderMode = tabChromeRenderMode,
                                    shape = tabShape,
                                    surfaceColor = tabSurfaceColor,
                                    hazeState = hazeState,
                                    backdrop = backdrop,
                                    liquidStyle = liquidStyle,
                                    liquidGlassTuning = liquidGlassTuning,
                                    liquidGlassPreset = liquidGlassPreset,
                                    motionTier = motionTier,
                                    isScrolling = isScrolling,
                                    isTransitionRunning = isTransitionRunning,
                                    forceLowBlurBudget = forceLowBlurBudget,
                                    preferFlatGlass = preferFlatGlass
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (drawChromeSurface && isTabFloating) {
                            Modifier.border(
                                width = AppSpacingTokens.Micro * 0.4f,
                                color = OpticalContrastPalette.Highlight.copy(alpha = tabBorderAlpha),
                                shape = tabShape
                            )
                        } else {
                            Modifier
                        }
                    )
                    .graphicsLayer { alpha = tabContentAlpha }
            ) {
                if (drawChromeSurface) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tabContentUnderlayColor, tabShape)
                    )
                    if (isTabFloating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppSpacingTokens.Large)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            tabHighlightColor,
                                            Color.Transparent
                                        )
                                    ),
                                    shape = tabShape
                                )
                        )
                    }
                }
            }

            // Do not clip: liquid capsule drag-scale should slightly overflow the dock like bottom bar.
            Box(
                modifier = dockModifier
                    .graphicsLayer {
                        alpha = tabContentAlpha
                        clip = false
                    },
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }

        if (showCollapsedHandle) {
            CollapsedTopTabHandle()
        }
    }
}

@Composable
private fun BoxScope.CollapsedTopTabHandle() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(width = AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Micro, height = AppSpacingTokens.ExtraSmall)
            .clip(AppShapes.container(ContainerLevel.Pill))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f))
    )
}
