package com.android.purebilibili.feature.home.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.isSpecified
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.OpticalContrastPalette
import com.android.purebilibili.core.ui.animation.horizontalDragGesture
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.motion.BottomBarMotionProfile
import com.android.purebilibili.core.ui.motion.BottomBarMotionSpec
import com.android.purebilibili.core.ui.motion.resolveBottomBarMotionSpec
import com.android.purebilibili.core.ui.adaptive.MotionTier
import dev.chrisbanes.haze.HazeState
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign

internal const val BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP = 58
internal const val BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP = 56
private const val SEGMENTED_CONTROL_MIN_INDICATOR_ASPECT_RATIO = 1.6f

internal fun resolveLiquidSegmentedControlUnselectedTextColor(
    onSurface: Color,
    enabled: Boolean
): Color = if (enabled) onSurface else onSurface.copy(alpha = 0.42f)

internal fun resolveSegmentedControlIndicatorWidthDp(
    slotWidthDp: Float,
    indicatorHeightDp: Float,
    itemCount: Int
): Float {
    if (slotWidthDp <= 0f || indicatorHeightDp <= 0f || itemCount <= 0) return 0f
    return slotWidthDp
}

internal fun resolveSegmentedControlIndicatorHeightDp(
    slotWidthDp: Float,
    indicatorHeightDp: Float
): Float {
    if (slotWidthDp <= 0f || indicatorHeightDp <= 0f) return 0f
    return min(
        indicatorHeightDp,
        slotWidthDp / SEGMENTED_CONTROL_MIN_INDICATOR_ASPECT_RATIO
    )
}

internal fun resolveSegmentedControlIndicatorOffsetDp(
    position: Float,
    slotWidthDp: Float,
    contentPaddingDp: Float
): Float {
    return contentPaddingDp + (slotWidthDp * position)
}

internal fun shouldFollowSegmentedControlIndicatorDrag(
    pointerX: Float,
    indicatorPosition: Float,
    itemWidthPx: Float
): Boolean {
    if (itemWidthPx <= 0f) return false
    val startX = indicatorPosition * itemWidthPx
    val endX = startX + itemWidthPx
    return pointerX in startX..endX
}

internal fun resolveSegmentedControlSweepSelectionIndex(
    pointerX: Float,
    itemWidthPx: Float,
    itemCount: Int
): Int {
    if (itemWidthPx <= 0f || itemCount <= 0) return 0
    return (pointerX.coerceAtLeast(0f) / itemWidthPx)
        .toInt()
        .coerceIn(0, itemCount - 1)
}

internal fun resolveSegmentedControlIndicatorPosition(
    internalPosition: Float,
    externalPosition: Float?,
    itemCount: Int
): Float {
    if (itemCount <= 0) return 0f
    return (externalPosition ?: internalPosition)
        .coerceIn(0f, (itemCount - 1).toFloat())
}

internal fun resolveSegmentedControlMotionProgress(
    pressProgress: Float,
    refractionProgress: Float,
    tapPressRefractionEnabled: Boolean
): Float {
    @Suppress("UNUSED_PARAMETER")
    val resolvedPressProgress = if (tapPressRefractionEnabled) pressProgress else 0f
    return maxOf(resolvedPressProgress, refractionProgress)
}

internal fun resolveSegmentedControlExternalPagerVelocityItemsPerSecond(
    currentPosition: Float,
    previousPosition: Float,
    elapsedNanos: Long,
): Float {
    if (elapsedNanos <= 0L) return 0f
    val elapsedSeconds = elapsedNanos / 1_000_000_000f
    if (elapsedSeconds <= 0f) return 0f
    return ((currentPosition - previousPosition) / elapsedSeconds)
        .coerceIn(-12f, 12f)
}

internal fun shouldStretchSegmentedControlExternalPagerIndicator(
    position: Float,
    externalPagerMotionActive: Boolean,
    positionEpsilon: Float = 0.015f,
): Boolean {
    if (!externalPagerMotionActive) return false
    return abs(position - position.roundToInt().toFloat()) > positionEpsilon
}

/**
 * Shared liquid segmented/top-tab indicator motion must match the home floating bottom bar.
 * Do not soften springs/offsets here — any divergence makes swipe stretch/settle feel wrong.
 */
internal fun resolveSegmentedControlMotionSpec(): BottomBarMotionSpec {
    return resolveBottomBarMotionSpec(profile = BottomBarMotionProfile.ANDROID_NATIVE_FLOATING)
}

/**
 * Same panel-offset formula as [BiliPaiFloatingBottomBar]: fraction of full dock width,
 * capped at AppSpacingTokens.ExtraSmall, EaseOut mapped.
 */
internal fun resolveSharedLiquidIndicatorPanelOffsetPx(
    dragOffsetPx: Float,
    dockWidthPx: Float,
    maxOffsetPx: Float
): Float {
    if (dockWidthPx <= 0f) return 0f
    val fraction = (dragOffsetPx / dockWidthPx).coerceIn(-1f, 1f)
    return maxOffsetPx * fraction.sign * EaseOut.transform(abs(fraction))
}

/**
 * Lens/refraction progress for shared liquid indicators.
 * Bottom bar keeps a drag floor so slow swipes still show glass stretch instead of fading out.
 */
internal fun resolveSharedLiquidIndicatorLensProgress(
    pressProgress: Float,
    motionProgress: Float,
    isDragging: Boolean
): Float {
    val dragFloor = if (isDragging) 0.6f else 0f
    return maxOf(pressProgress, motionProgress, dragFloor).coerceIn(0f, 1f)
}

/**
 * When glass is active and the capsule is moving, visible labels stay neutral and the
 * selected color is carried by the export layer + tint (same as home bottom bar).
 */
internal fun resolveSharedLiquidIndicatorUseGlassColorPath(
    liquidGlassEnabled: Boolean,
    lensProgress: Float
): Boolean = liquidGlassEnabled && lensProgress > 0.001f

/** Capture lens strength: full 24dp while interacting, like BiliPai bottom bar capture. */
internal fun resolveSharedLiquidIndicatorCaptureLensProgress(
    lensProgress: Float,
    isDragging: Boolean
): Float {
    if (isDragging) return 1f
    return lensProgress.coerceIn(0f, 1f)
}

/**
 * Export-layer glyph color before [ColorFilter.tint].
 * Must stay near-white so SrcIn tint resolves to pure theme/primary color.
 */
internal fun resolveSharedLiquidExportMonochromeColor(
    darkTheme: Boolean
): Color = if (darkTheme) {
    OpticalContrastPalette.Highlight.copy(alpha = 0.96f)
} else {
    OpticalContrastPalette.Highlight
}

@Composable
fun BottomBarLiquidSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemWidth: Dp? = null,
    height: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP.dp,
    indicatorHeight: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP.dp,
    labelFontSize: TextUnit = TextUnit.Unspecified,
    containerHorizontalPadding: Dp = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
    containerVerticalPadding: Dp = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
    @Suppress("UNUSED_PARAMETER") liquidGlassEffectsEnabled: Boolean = true,
    dragSelectionEnabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") preferInlineContentStyle: Boolean = false,
    @Suppress("UNUSED_PARAMETER") forceLiquidChrome: Boolean = false,
    @Suppress("UNUSED_PARAMETER") backdrop: Any? = null,
    @Suppress("UNUSED_PARAMETER") miuixBackdrop: Any? = null,
    @Suppress("UNUSED_PARAMETER") tapPressRefractionEnabled: Boolean = true,
    containerColorOverride: Color? = null,
    selectedTextColorOverride: Color? = null,
    unselectedTextColorOverride: Color? = null,
    indicatorIdleSurfaceColorOverride: Color? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    onIndicatorPositionChanged: ((Float) -> Unit)? = null,
    isScrollInProgressProvider: () -> Boolean = { false },
    @Suppress("UNUSED_PARAMETER") externalPagerMotionEffectsEnabled: Boolean = false,
) {
    if (items.isEmpty()) return

    val effectiveLabelFontSize = if (labelFontSize.isSpecified) {
        labelFontSize
    } else {
        MaterialTheme.typography.labelMedium.fontSize
    }
    val density = LocalDensity.current
    val itemCount = items.size
    val safeSelectedIndex = selectedIndex.coerceIn(0, itemCount - 1)
    val motionSpec = remember { resolveSegmentedControlMotionSpec() }
    val matchedChromeState = rememberBottomBarMatchedLiquidChromeState(
        initialIndex = safeSelectedIndex,
        itemCount = itemCount,
        notifyIndexChangedOnReleaseStart = indicatorPositionProvider != null,
        isScrollInProgressProvider = isScrollInProgressProvider,
        onIndexChanged = { index ->
            if (enabled && index in items.indices) {
                onSelected(index)
            }
        }
    )
    val dragState = matchedChromeState.dragState
    LaunchedEffect(safeSelectedIndex) {
        dragState.updateIndex(safeSelectedIndex)
    }

    val indicatorShape = resolveSharedBottomBarCapsuleShape()
    val indicatorCorner = indicatorHeight / 2
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceColor = AppSurfaceTokens.cardContainer()
    val blurIntensity = currentUnifiedBlurIntensity()
    val containerColor = containerColorOverride ?: resolveBottomBarSurfaceColor(
        surfaceColor = surfaceColor,
        blurEnabled = true,
        blurIntensity = blurIntensity
    )
    val themeColor = MaterialTheme.colorScheme.primary
    val selectedTextColor = selectedTextColorOverride ?: themeColor
    val unselectedTextColor = unselectedTextColorOverride
        ?: resolveLiquidSegmentedControlUnselectedTextColor(
            onSurface = MaterialTheme.colorScheme.onSurface,
            enabled = enabled
        )
    val indicatorIdleSurfaceColor = indicatorIdleSurfaceColorOverride
        ?: resolveAndroidNativeIdleIndicatorSurfaceColor(darkTheme = isDarkTheme)

    fun selectFromTap(index: Int) {
        if (!enabled || index !in items.indices) return
        dragState.updateIndex(index)
        onSelected(index)
    }

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (itemWidth != null) {
                    Modifier.width((itemWidth.value * itemCount).dp + containerHorizontalPadding * 2)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .height(height)
    ) {
        val contentPadding = containerHorizontalPadding
        val contentVerticalInset = containerVerticalPadding
        val slotWidth = (maxWidth - (contentPadding * 2)) / itemCount
        val indicatorWidth = resolveSegmentedControlIndicatorWidthDp(
            slotWidthDp = slotWidth.value,
            indicatorHeightDp = indicatorHeight.value,
            itemCount = itemCount
        ).dp
        val resolvedIndicatorHeight = resolveSegmentedControlIndicatorHeightDp(
            slotWidthDp = slotWidth.value,
            indicatorHeightDp = indicatorHeight.value
        ).dp
        val externalIndicatorPosition = if (dragState.isDragging) {
            null
        } else {
            indicatorPositionProvider?.invoke()
        }
        val indicatorPosition = resolveSegmentedControlIndicatorPosition(
            internalPosition = dragState.value,
            externalPosition = externalIndicatorPosition,
            itemCount = itemCount
        )
        val indicatorOffset = resolveSegmentedControlIndicatorOffsetDp(
            position = indicatorPosition,
            slotWidthDp = slotWidth.value,
            contentPaddingDp = contentPadding.value
        ).dp
        val itemWidthPx = with(density) { slotWidth.toPx() }.coerceAtLeast(1f)
        val dragModifier = if (enabled && itemCount > 1 && dragSelectionEnabled) {
            Modifier.horizontalDragGesture(
                dragState = dragState,
                itemWidthPx = itemWidthPx
            )
        } else {
            Modifier
        }
        val externalPagerMotionActive = externalPagerMotionEffectsEnabled &&
            externalIndicatorPosition != null &&
            isScrollInProgressProvider()
        val externalVelocityPositionTracker = remember {
            FloatArray(1) { indicatorPosition }
        }
        val externalVelocityTimeTracker = remember {
            LongArray(1) { System.nanoTime() }
        }
        val velocitySampleTimeNanos = System.nanoTime()
        val externalPagerVelocityItemsPerSecond = if (externalPagerMotionActive) {
            resolveSegmentedControlExternalPagerVelocityItemsPerSecond(
                currentPosition = indicatorPosition,
                previousPosition = externalVelocityPositionTracker[0],
                elapsedNanos = (velocitySampleTimeNanos - externalVelocityTimeTracker[0])
                    .coerceAtLeast(1L),
            )
        } else {
            0f
        }
        SideEffect {
            externalVelocityPositionTracker[0] = indicatorPosition
            externalVelocityTimeTracker[0] = velocitySampleTimeNanos
        }
        val indicatorIsInteracting = dragState.isDragging || externalPagerMotionActive
        val indicatorShouldStretch = dragState.isDragging ||
            shouldStretchSegmentedControlExternalPagerIndicator(
                position = indicatorPosition,
                externalPagerMotionActive = externalPagerMotionActive,
            )
        val pressMotionProgress by remember {
            derivedStateOf { dragState.pressProgress }
        }
        val motionProgress by animateFloatAsState(
            targetValue = pressMotionProgress.coerceIn(0f, 1f),
            animationSpec = motionSpec.indicator.scaleSpring.toSpringSpec(),
            label = "segmentedControlMotionProgress"
        )
        val indicatorDragScaleProgress = rememberBottomBarIndicatorDragScaleProgress(
            isDragging = indicatorShouldStretch
        )
        val indicatorLayerScaleProgress = maxOf(indicatorDragScaleProgress, pressMotionProgress)
        val panelOffsetPx = 0f

        BottomBarMatchedLiquidDock(
            containerColor = containerColor,
            shape = indicatorShape,
            blurEnabled = true,
            modifier = Modifier.matchParentSize(),
            isScrollInProgressProvider = isScrollInProgressProvider
        ) {}

        // 1) Visible labels.
        BottomBarLiquidSegmentedLabels(
            items = items,
            selectedIndex = safeSelectedIndex,
            indicatorPosition = indicatorPosition,
            motionProgress = motionProgress,
            selectionEmphasis = 1f - motionProgress.coerceIn(0f, 1f) * 0.72f,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor,
            enabled = enabled,
            labelFontSize = effectiveLabelFontSize,
            indicatorCorner = indicatorCorner,
            onSelected = onSelected,
            interactive = false,
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = contentPadding, vertical = contentVerticalInset)
                .graphicsLayer { translationX = panelOffsetPx }
        )

        // 2) Capsule on top.
        BottomBarMatchedLiquidIndicator(
            visible = true,
            indicatorTranslationXPx = with(density) { indicatorOffset.toPx() },
            indicatorPanelOffsetPx = panelOffsetPx,
            indicatorWidth = indicatorWidth,
            indicatorHeight = resolvedIndicatorHeight,
            shellShape = indicatorShape,
            indicatorIdleSurfaceColor = indicatorIdleSurfaceColor,
            indicatorEffectsEnabled = true,
            indicatorLayerScaleProgress = indicatorLayerScaleProgress,
            isDarkTheme = isDarkTheme
        )

        // 3) Invisible hit / drag layer above everything.
        BottomBarLiquidSegmentedLabels(
            items = items,
            selectedIndex = safeSelectedIndex,
            indicatorPosition = indicatorPosition,
            motionProgress = motionProgress,
            selectionEmphasis = 1f - motionProgress.coerceIn(0f, 1f) * 0.72f,
            selectedTextColor = selectedTextColor,
            unselectedTextColor = unselectedTextColor,
            enabled = enabled,
            labelFontSize = effectiveLabelFontSize,
            indicatorCorner = indicatorCorner,
            onSelected = ::selectFromTap,
            interactive = true,
            onPressChanged = dragState::setPressed,
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = contentPadding, vertical = contentVerticalInset)
                .alpha(0f)
                .graphicsLayer { translationX = panelOffsetPx }
                .then(dragModifier)
        )
    }
}

@Composable
internal fun AndroidNativeUnderlinedSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemWidth: Dp? = null,
    height: Dp,
    labelFontSize: TextUnit,
    selectedTextColorOverride: Color? = null,
    unselectedTextColorOverride: Color? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    onIndicatorPositionChanged: ((Float) -> Unit)? = null
) {
    val itemCount = items.size
    val safeSelectedIndex = selectedIndex.coerceIn(0, itemCount - 1)
    val selectedTextColor = selectedTextColorOverride ?: MaterialTheme.colorScheme.primary
    val unselectedTextColor = unselectedTextColorOverride
        ?: MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.78f else 0.42f)
    val underlineShape = CircleShape
    val indicatorPosition = resolveSegmentedControlIndicatorPosition(
        internalPosition = safeSelectedIndex.toFloat(),
        externalPosition = indicatorPositionProvider?.invoke(),
        itemCount = itemCount
    )

    SideEffect {
        onIndicatorPositionChanged?.invoke(indicatorPosition)
    }

    BoxWithConstraints(
        modifier = modifier
            .then(
                if (itemWidth != null) {
                    Modifier.width(itemWidth * itemCount)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
            .height(height)
    ) {
        val segmentWidth = maxWidth / itemCount
        val underlineWidth = (segmentWidth * 0.42f)
            .coerceAtLeast(AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall)
            .coerceAtMost(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Small)
        val underlineOffsetX = (segmentWidth * indicatorPosition) + ((segmentWidth - underlineWidth) / 2)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, label ->
                val selected = index == safeSelectedIndex
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clickable(enabled = enabled) { onSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        text = label,
                        color = if (selected) selectedTextColor else unselectedTextColor,
                        fontSize = labelFontSize,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = underlineOffsetX)
                .width(underlineWidth)
                .height(AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2)
                .clip(underlineShape)
                .background(selectedTextColor)
        )
    }
}

@Composable
private fun BottomBarLiquidSegmentedLabels(
    items: List<String>,
    selectedIndex: Int,
    indicatorPosition: Float,
    motionProgress: Float,
    selectionEmphasis: Float,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    enabled: Boolean,
    labelFontSize: TextUnit,
    indicatorCorner: Dp,
    onSelected: (Int) -> Unit,
    interactive: Boolean,
    onPressChanged: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, label ->
            val interactionSource = remember { MutableInteractionSource() }
            if (interactive && onPressChanged != null) {
                val pressed by interactionSource.collectIsPressedAsState()
                LaunchedEffect(pressed) {
                    onPressChanged(pressed)
                }
            }
            val visual = resolveBottomBarItemMotionVisual(
                itemIndex = index,
                indicatorPosition = indicatorPosition,
                currentSelectedIndex = selectedIndex,
                motionProgress = motionProgress,
                selectionEmphasis = selectionEmphasis
            )
            val textColor = if (!enabled) {
                unselectedTextColor.copy(alpha = 0.44f)
            } else {
                lerpColor(start = unselectedTextColor, stop = selectedTextColor, fraction = visual.themeWeight)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(indicatorCorner))
                    .then(
                        if (interactive) {
                            Modifier.clickable(
                                enabled = enabled,
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onSelected(index)
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = label,
                    color = textColor,
                    fontSize = labelFontSize,
                    fontWeight = if (visual.themeWeight > 0.5f) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer {
                        scaleX = visual.scale
                        scaleY = visual.scale
                    }
                )
            }
        }
    }
}

private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (stop.red - start.red) * t,
        green = start.green + (stop.green - start.green) * t,
        blue = start.blue + (stop.blue - start.blue) * t,
        alpha = start.alpha + (stop.alpha - start.alpha) * t
    )
}
