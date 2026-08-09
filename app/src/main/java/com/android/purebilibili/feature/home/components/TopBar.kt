// 文件路径: feature/home/components/TopBar.kt
package com.android.purebilibili.feature.home.components
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSurface

import com.android.purebilibili.core.ui.OpticalContrastPalette
import com.android.purebilibili.feature.home.HomeVisualPalette

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tv

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.foundation.ExperimentalFoundationApi // [Added]
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.ui.AppIconStyle
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import com.android.purebilibili.core.ui.AppTopTabPresentation
import com.android.purebilibili.core.ui.rememberAppTopChromePolicy
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.feature.home.UserState
import com.android.purebilibili.feature.home.HomeCategory
import com.android.purebilibili.feature.home.resolveHomeTopCategories
import com.android.purebilibili.core.store.BottomBarLiquidGlassPreset
import com.android.purebilibili.core.store.LiquidGlassStyle
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.animation.DampedDragAnimationState
import com.android.purebilibili.core.ui.animation.shouldEngageHorizontalDrag
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.android.purebilibili.feature.home.components.liquid.lens as miuixLens
import com.android.purebilibili.feature.home.components.liquid.rememberCombinedBackdrop as rememberMiuixCombinedBackdrop
import com.android.purebilibili.feature.home.components.liquid.vibrancy as miuixVibrancy
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop
import top.yukonga.miuix.kmp.blur.blur as miuixBlur
import top.yukonga.miuix.kmp.blur.drawBackdrop as miuixDrawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop as miuixLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop as rememberMiuixLayerBackdrop
import dev.chrisbanes.haze.HazeState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlinx.coroutines.delay
import androidx.compose.foundation.combinedClickable // [Added]
import java.io.File

private const val IOS_TOP_TAB_CONTENT_PADDING_DP = 2f

// 指示器拖动释放后允许 spring 飞掷动画 settle 的兜底时长；
// 超过此时长仍未收到 onSettled 回调则强制解除 engaged，避免位置竞争。
private const val TOP_TAB_DRAG_RELEASE_SETTLE_MS = 600L

internal fun resolveFloatingIndicatorStartPaddingPx(
    baseInsetPx: Float,
    leftBiasPx: Float
): Float = (baseInsetPx - leftBiasPx).coerceAtLeast(0f)

internal fun resolveTopTabRowHorizontalPaddingDp(
    isFloatingStyle: Boolean,
    edgeToEdge: Boolean = false,
    labelMode: Int = 0
): Float {
    if (edgeToEdge) return 0f
    if (isFloatingStyle) return 0f
    // Text-only MD3/Miuix: drop the extra 4dp so the first indicator sits closer to the edge.
    return if (normalizeTopTabLabelMode(labelMode) == 2) 0f else 4f
}

// Slightly tighter than before so rest capsule nearly fills the dock (bottom-bar feel),
// while drag scale still overflows the chrome edge.
internal fun resolveTopTabDockIndicatorHorizontalGapDp(hasOuterChromeSurface: Boolean): Float =
    if (hasOuterChromeSurface) 2f else 2f

/**
 * 顶部 Tab 的视觉背景保持 30dp 高；36dp 行高留出上下各 3dp 的呼吸空间。
 */
internal fun resolveTopTabDockIndicatorVerticalGapDp(hasOuterChromeSurface: Boolean): Float = 3f

internal fun resolveTopTabDockIndicatorWidthDp(
    itemWidthDp: Float,
    horizontalGapDp: Float,
    minWidthDp: Float = 0f
): Float {
    if (itemWidthDp <= 0f) return 0f
    val maxWidth = (itemWidthDp - horizontalGapDp.coerceAtLeast(0f) * 2f)
        .coerceAtLeast(0f)
    val minWidth = minWidthDp.coerceIn(0f, itemWidthDp)
    return maxWidth.coerceAtLeast(minWidth)
}

internal fun resolveTopTabDockIndicatorHeightDp(
    rowHeightDp: Float,
    verticalGapDp: Float,
    minHeightDp: Float,
    indicatorWidthDp: Float = Float.POSITIVE_INFINITY
): Float {
    if (rowHeightDp <= 0f) return 0f
    val maxHeight = (rowHeightDp - verticalGapDp.coerceAtLeast(0f) * 2f)
        .coerceAtLeast(0f)
    val minHeight = minHeightDp.coerceIn(0f, rowHeightDp)
    return resolveSegmentedControlIndicatorHeightDp(
        slotWidthDp = indicatorWidthDp,
        indicatorHeightDp = maxHeight
    ).coerceAtLeast(minHeight)
}

/**
 * Preferred per-tab width when the floating dock **wraps content** instead of stretching
 * full width (icon / text density drives dock length).
 */
internal fun resolveTopTabWrapItemWidthDp(
    labelMode: Int,
    isFloatingStyle: Boolean = true
): Float {
    // Keep wrap-dock preferred widths at least the multi-slot floor so iOS / MD3 / Miuix
    // floating docks never pack tighter than the readable minimum.
    val floor = resolveMd3TopTabMinItemWidthDp(labelMode)
    val preferred = when (normalizeTopTabLabelMode(labelMode)) {
        // 图文混合模式至少要容纳 18dp 图标、6dp 间距和两三个汉字，
        // 否则文字会退化为单独的省略号。
        0 -> if (isFloatingStyle) 84f else 80f // icon + text
        1 -> if (isFloatingStyle) 56f else 52f // icon only
        else -> if (isFloatingStyle) 72f else 68f // text only
    }
    return preferred.coerceAtLeast(floor)
}

/**
 * Whether the top dock should shrink to tab content instead of fillMaxWidth.
 * Floating / bottom-bar-matched docks: wrap so right side isn't empty chrome.
 * Embedded / full-bleed rows keep stretch.
 */
internal fun shouldWrapTopTabDockWidth(
    isFloatingStyle: Boolean,
    hasOuterChromeSurface: Boolean,
    edgeToEdge: Boolean
): Boolean {
    if (edgeToEdge) return false
    return isFloatingStyle || hasOuterChromeSurface
}

/**
 * Dock content width = itemWidth × tabCount (+ optional horizontal content padding).
 * Clamped to [maxWidthDp] so small phones still fill when content is wider.
 */
internal fun resolveTopTabDockWrapWidthDp(
    itemWidthDp: Float,
    categoryCount: Int,
    maxWidthDp: Float,
    contentPaddingHorizontalDp: Float = 0f
): Float {
    if (itemWidthDp <= 0f || categoryCount <= 0) return 0f
    val content = itemWidthDp * categoryCount + contentPaddingHorizontalDp.coerceAtLeast(0f) * 2f
    if (maxWidthDp <= 0f) return content
    return content.coerceIn(0f, maxWidthDp)
}

/**
 * Item width for wrap dock: use content-driven preferred width when it fits;
 * otherwise fall back to dividing the available max width (scrollable denser slots).
 */
internal fun resolveTopTabDockItemWidthDp(
    maxWidthDp: Float,
    categoryCount: Int,
    labelMode: Int,
    isFloatingStyle: Boolean,
    wrapContent: Boolean,
    fillItemWidthDp: Float
): Float {
    if (!wrapContent || categoryCount <= 0) return fillItemWidthDp
    val preferred = resolveTopTabWrapItemWidthDp(labelMode, isFloatingStyle)
    val wrapWidth = resolveTopTabDockWrapWidthDp(
        itemWidthDp = preferred,
        categoryCount = categoryCount,
        maxWidthDp = maxWidthDp
    )
    // Preferred pack fits: use content-driven item width.
    if (wrapWidth <= maxWidthDp + 0.01f && preferred * categoryCount <= maxWidthDp + 0.01f) {
        return preferred
    }
    // Overflow: pack into available width.
    return fillItemWidthDp
}

internal fun resolveTopTabDockIndicatorOffsetPx(
    slotTranslationPx: Float,
    horizontalGapPx: Float
): Float = slotTranslationPx + horizontalGapPx.coerceAtLeast(0f)

internal fun resolveTopTabVisibleSlots(
    categoryCount: Int,
    longestLabelLength: Int = 0
): Int {
    val cappedCategoryCount = categoryCount.coerceAtMost(SettingsManager.MAX_TOP_TABS)
    if (cappedCategoryCount in 1..3) return cappedCategoryCount
    if (cappedCategoryCount <= 4) return 4
    return if (longestLabelLength >= 8) 4 else 5
}

internal fun resolveMd3TopTabVisibleSlots(): Int = 3

internal fun resolveMd3TopTabLayoutVisibleSlots(
    categoryCount: Int,
    labelMode: Int,
    showPartitionAction: Boolean,
    fontScale: Float = 1f
): Int {
    val hasSupportedLabelMode = normalizeTopTabLabelMode(labelMode) in 0..2
    val cappedCategoryCount = categoryCount.coerceAtMost(SettingsManager.MAX_TOP_TABS)
    return if (!showPartitionAction && hasSupportedLabelMode && cappedCategoryCount >= 4) {
        if (fontScale > 1.15f) {
            cappedCategoryCount.coerceAtMost(4)
        } else {
            cappedCategoryCount
        }
    } else {
        resolveMd3TopTabVisibleSlots()
    }
}

internal fun resolveIosTopTabLayoutVisibleSlots(
    categoryCount: Int,
    labelMode: Int
): Int = resolveMd3TopTabLayoutVisibleSlots(
    categoryCount = categoryCount,
    labelMode = labelMode,
    showPartitionAction = false
)

internal fun resolveIosTopTabItemWidthDp(
    containerWidthDp: Float,
    categoryCount: Int,
    labelMode: Int
): Float = resolveMd3TopTabItemWidthDp(
    containerWidthDp = (containerWidthDp - IOS_TOP_TAB_CONTENT_PADDING_DP * 2f)
        .coerceAtLeast(0f),
    visibleSlots = resolveIosTopTabLayoutVisibleSlots(categoryCount, labelMode),
    labelMode = labelMode
)

/**
 * Minimum slot width so labels/icons stay readable in the compact dock.
 *
 * Budget for text-only: outer 3dp×2 + content 4dp×2 + ~30dp for two CJK glyphs ≈ 44dp,
 * then add a little for semi-bold / font padding. Icon+text also needs 18+6 for glyph+gap.
 * Prefer scrolling over squeezing every tab into the viewport as pure "...".
 */
internal fun resolveMd3TopTabMinItemWidthDp(labelMode: Int): Float {
    return when (normalizeTopTabLabelMode(labelMode)) {
        0 -> 80f // icon + text
        1 -> 48f // icon only
        else -> 64f // text only — two CJK characters with compact padding
    }
}

internal fun resolveMd3TopTabMaxItemWidthDp(labelMode: Int): Float {
    return when (normalizeTopTabLabelMode(labelMode)) {
        0 -> 96f
        1 -> 64f
        else -> 88f
    }
}

internal fun resolveMd3TopTabItemWidthDp(
    containerWidthDp: Float,
    visibleSlots: Int = resolveMd3TopTabVisibleSlots(),
    labelMode: Int = 2
): Float {
    if (containerWidthDp <= 0f) return 96f
    val minWidth = resolveMd3TopTabMinItemWidthDp(labelMode)
    val maxWidth = resolveMd3TopTabMaxItemWidthDp(labelMode)
    if (visibleSlots >= 5) {
        return (containerWidthDp / visibleSlots).coerceIn(minWidth, maxWidth)
    }
    return (containerWidthDp / visibleSlots.coerceAtLeast(1)).coerceAtLeast(minWidth.coerceAtLeast(88f))
}

internal fun resolveMd3TopTabContentPaddingDp(
    containerWidthDp: Float,
    itemWidthDp: Float,
    categoryCount: Int,
    labelMode: Int = 0
): Float {
    if (containerWidthDp <= 0f || itemWidthDp <= 0f || categoryCount <= 0) return 0f
    val contentWidth = itemWidthDp * categoryCount
    val leftover = (containerWidthDp - contentWidth).coerceAtLeast(0f)
    // Multi-tab rows (MD3 / MIUIX / all label modes): lead-align so the first indicator
    // sits at the leading edge. The 72dp item-width cap on 4–5 tabs creates leftover;
    // centering it pushes "推荐" away from the left of the dock.
    // Sparse rows (1–2 tabs): keep residual centered so a single tab is not glued left.
    @Suppress("UNUSED_PARAMETER")
    val ignoredLabelMode = labelMode
    return if (categoryCount >= 3) {
        0f
    } else {
        leftover / 2f
    }
}

internal fun resolveMd3VisibleTabIndices(
    totalCount: Int,
    selectedIndex: Int,
    visibleSlots: Int = resolveMd3TopTabVisibleSlots()
): List<Int> {
    if (totalCount <= 0) return emptyList()
    return List(totalCount) { it }
}

internal fun resolveMd3SelectedVisibleIndex(
    visibleIndices: List<Int>,
    selectedIndex: Int
): Int {
    val resolved = visibleIndices.indexOf(selectedIndex)
    return if (resolved >= 0) resolved else 0
}

internal fun resolveTopTabMinItemWidthDp(isFloatingStyle: Boolean): Float {
    return if (isFloatingStyle) 72f else 64f
}

internal fun resolveTopTabItemWidthDp(
    containerWidthDp: Float,
    categoryCount: Int,
    isFloatingStyle: Boolean,
    longestLabelLength: Int = 0
): Float {
    if (containerWidthDp <= 0f) return resolveTopTabMinItemWidthDp(isFloatingStyle)
    val slots = resolveTopTabVisibleSlots(
        categoryCount = categoryCount,
        longestLabelLength = longestLabelLength
    ).coerceAtLeast(1)
    val baseWidth = containerWidthDp / slots
    return baseWidth.coerceAtLeast(resolveTopTabMinItemWidthDp(isFloatingStyle))
}

internal fun resolveTopTabVisibleCategorySlots(
    categoryCount: Int,
    longestLabelLength: Int = 0
): Int {
    return resolveTopTabVisibleSlots(
        categoryCount = categoryCount,
        longestLabelLength = longestLabelLength
    ).coerceAtMost(categoryCount.coerceAtLeast(1)).coerceAtLeast(1)
}

internal fun resolveTopTabActionSlotWidthDp(
    containerWidthDp: Float,
    categoryCount: Int,
    longestLabelLength: Int = 0
): Float {
    if (containerWidthDp <= 0f) return 0f
    val categorySlots = resolveTopTabVisibleCategorySlots(
        categoryCount = categoryCount,
        longestLabelLength = longestLabelLength
    )
    return containerWidthDp / (categorySlots + 1)
}

internal fun normalizeTopTabLabelMode(mode: Int): Int {
    return when (mode) {
        0, 1, 2 -> mode
        else -> 2
    }
}

internal fun shouldShowTopTabIcon(mode: Int): Boolean {
    val normalized = normalizeTopTabLabelMode(mode)
    return normalized == 0 || normalized == 1
}

internal fun shouldShowTopTabText(mode: Int): Boolean {
    val normalized = normalizeTopTabLabelMode(mode)
    return normalized == 0 || normalized == 2
}

internal fun resolveTopTabIconFamily(
    chromeIconFamily: AppSemanticIconFamily,
    useBottomBarMatchedChrome: Boolean,
    iconStyle: AppIconStyle = AppIconStyle.AUTO
): AppSemanticIconFamily {
    return when {
        // MD3 官方推荐样式统一使用 Material 官方字形
        iconStyle == AppIconStyle.MD3_STANDARD -> AppSemanticIconFamily.MATERIAL
        useBottomBarMatchedChrome -> AppSemanticIconFamily.MATERIAL
        else -> chromeIconFamily
    }
}

internal fun resolveMd3TopTabLabelMode(requestedLabelMode: Int): Int =
    normalizeTopTabLabelMode(requestedLabelMode)

private fun resolveTopTabCategoryForIcon(categoryKey: String): HomeCategory? {
    val normalizedKey = categoryKey.trim()
    if (normalizedKey.isEmpty()) return null

    return HomeCategory.entries.firstOrNull { category ->
        category.name.equals(normalizedKey, ignoreCase = true) || category.label == normalizedKey
    }
}

internal fun resolveTopTabCategoryIcon(
    categoryKey: String,
    iconFamily: AppSemanticIconFamily = AppSemanticIconFamily.MATERIAL,
    selected: Boolean = false
): ImageVector {
    val category = resolveTopTabCategoryForIcon(categoryKey)
    return when (iconFamily) {
        AppSemanticIconFamily.MATERIAL -> when (category) {
            HomeCategory.RECOMMEND -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
            HomeCategory.FOLLOW -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            HomeCategory.POPULAR -> if (selected) {
                Icons.AutoMirrored.Filled.TrendingUp
            } else {
                Icons.AutoMirrored.Outlined.TrendingUp
            }
            HomeCategory.LIVE -> if (selected) Icons.Filled.LiveTv else Icons.Outlined.LiveTv
            HomeCategory.ANIME -> if (selected) Icons.Filled.Tv else Icons.Outlined.Tv
            HomeCategory.GAME -> if (selected) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports
            HomeCategory.KNOWLEDGE -> if (selected) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb
            HomeCategory.TECH -> if (selected) Icons.Filled.SmartToy else Icons.Outlined.SmartToy
            else -> Icons.AutoMirrored.Outlined.MenuOpen
        }
    }
}

internal fun resolveTopTabPartitionIcon(iconFamily: AppSemanticIconFamily): ImageVector {
    return if (iconFamily == AppSemanticIconFamily.MATERIAL) {
        Icons.AutoMirrored.Outlined.MenuOpen
    } else {
        Icons.AutoMirrored.Outlined.MenuOpen
    }
}

internal enum class Md3TopTabRowVariant {
    UNDERLINE_FIXED
}

internal fun resolveMd3TopTabRowVariant(): Md3TopTabRowVariant =
    Md3TopTabRowVariant.UNDERLINE_FIXED

internal fun resolveMd3TopTabActionButtonCorner(
    isFloatingStyle: Boolean,
    presentation: AppTopTabPresentation = AppTopTabPresentation.MATERIAL_UNDERLINE
) = if (presentation == AppTopTabPresentation.TONAL_CAPSULE) {
    if (isFloatingStyle) AppSpacingTokens.Large + AppSpacingTokens.Micro else AppSpacingTokens.Medium + AppSpacingTokens.Micro
} else {
    if (isFloatingStyle) AppSpacingTokens.Large else AppSpacingTokens.Medium
}

internal fun resolveMd3TopTabActionButtonSize(
    isFloatingStyle: Boolean,
    presentation: AppTopTabPresentation = AppTopTabPresentation.MATERIAL_UNDERLINE
) = if (presentation == AppTopTabPresentation.TONAL_CAPSULE) {
    if (isFloatingStyle) AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Micro else AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Medium
} else {
    if (isFloatingStyle) AppSpacingTokens.TripleExtraLarge else AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Small + AppSpacingTokens.Micro
}

internal fun resolveMd3TopTabActionIconSize(
    isFloatingStyle: Boolean,
    presentation: AppTopTabPresentation = AppTopTabPresentation.MATERIAL_UNDERLINE
) = if (presentation == AppTopTabPresentation.TONAL_CAPSULE) {
    if (isFloatingStyle) AppSpacingTokens.ExtraLarge else AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro
} else {
    if (isFloatingStyle) AppSpacingTokens.ExtraLarge else AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro
}

internal fun resolveMd3TopTabActionContentBottomPadding(): Dp = AppSpacingTokens.ExtraSmall

internal fun resolveMd3TopTabVerticalLiftDp(): Float = 4f

internal fun resolveMd3TopTabRowVerticalTranslationDp(
    skinPlainStyle: Boolean,
    hasOuterChromeSurface: Boolean
): Float {
    if (skinPlainStyle || hasOuterChromeSurface) return 0f
    return -resolveMd3TopTabVerticalLiftDp()
}

internal fun resolveMd3TopTabIndicatorBottomPadding(): Dp = AppSpacingTokens.Small

internal fun resolveHomeSkinTopTabActionButtonSize(): Dp = AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Medium

internal fun resolveHomeSkinTopTabActionIconSize(): Dp = AppSpacingTokens.ExtraLarge

internal fun resolveHomeSkinTopTabIndicatorBottomPadding(): Dp = AppSpacingTokens.ExtraSmall

internal fun resolveTopTabSkinStickerIconSize(showText: Boolean): Dp =
    if (showText) AppSpacingTokens.DoubleExtraLarge else AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall

internal fun resolveTopTabSkinPartitionIconSize(): Dp = AppSpacingTokens.DoubleExtraLarge

internal fun resolveTopTabSkinStickerIndicatorWidth(): Dp = AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall

internal fun resolveTopTabSkinStickerRowHeight(
    baseRowHeight: Dp,
    hasSkinStickerIcons: Boolean,
    showIcon: Boolean,
    showText: Boolean
): Dp {
    return if (hasSkinStickerIcons && showIcon && showText) {
        baseRowHeight.coerceAtLeast(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large)
    } else {
        baseRowHeight
    }
}

internal fun resolveTopTabSkinStickerItemVerticalPadding(showText: Boolean): Dp =
    if (showText) AppSpacingTokens.Micro else AppSpacingTokens.ExtraSmall

/**
 * iOS top-tab track must match [resolveHomeTopPresetStyle] chrome height (36/40).
 * Taller content rows get clipped by HomeTopTabChrome and collapse labels to "...".
 */
internal fun resolveIosTopTabRowHeight(
    isFloatingStyle: Boolean,
    labelMode: Int = SettingsManager.TopTabLabelMode.TEXT_ONLY
): Dp {
    @Suppress("UNUSED_PARAMETER")
    val ignoredLabelMode = labelMode
    return if (isFloatingStyle) {
        AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Small
    } else {
        AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall
    }
}

internal fun resolveIosTopTabActionButtonSize(isFloatingStyle: Boolean): Dp =
    if (isFloatingStyle) AppSpacingTokens.TripleExtraLarge - AppSpacingTokens.Micro else AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.Medium

internal fun resolveIosTopTabActionButtonCorner(isFloatingStyle: Boolean): Dp =
    if (isFloatingStyle) AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro else AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall

internal fun resolveIosTopTabActionIconSize(isFloatingStyle: Boolean): Dp =
    if (isFloatingStyle) AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro / 2 else AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro

internal fun performHomeTopBarTap(
    haptic: (HapticType) -> Unit,
    onClick: () -> Unit,
    hapticType: HapticType = HapticType.LIGHT
) {
    haptic(hapticType)
    onClick()
}

/**
 * Q弹点击效果
 */
fun Modifier.premiumClickable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "scale"
    )
    this
        .scale(scale)
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

/**
 *  iOS 风格悬浮顶栏
 * - 不贴边，有水平边距
 * - 圆角 + 毛玻璃效果
 */
@Composable
fun FluidHomeTopBar(
    user: UserState,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        //  悬浮式导航栏容器 - 增强视觉层次
        AppSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacingTokens.Large, vertical = AppSpacingTokens.Small),
            shape = AppShapes.borderedContainer(ContainerLevel.Floating),
            color = AppSurfaceTokens.cardContainer(),  //  使用预设感知表面色，适配深色模式
            shadowElevation = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro,  // 添加阴影增加层次感
            tonalElevation = AppSpacingTokens.None,
            border = androidx.compose.foundation.BorderStroke(
                width = AppSpacingTokens.Micro / 2,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.ExtraSmall) // 稍微减小高度
                    .padding(horizontal = AppSpacingTokens.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //  左侧：头像
                Box(
                    modifier = Modifier
                        .size(AppChromeSizeTokens.MinimumTouchTarget)
                        .clip(CircleShape)
                        .premiumClickable { onAvatarClick() }
                        .semantics { contentDescription = "个人中心" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall)
                            .clip(CircleShape)
                            .border(AppSpacingTokens.Micro / 2, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        if (user.isLogin && user.face.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(FormatUtils.fixImageUrl(user.face))
                                    .crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                AppText("未", fontSize = MaterialTheme.typography.labelSmall.fontSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))

                //  中间：搜索框
                val searchClickInteractionSource = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall)
                        .clip(AppShapes.container(ContainerLevel.Pill))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = searchClickInteractionSource,
                            indication = null
                        ) {
                            onSearchClick()
                        }
                        .padding(horizontal = AppSpacingTokens.Medium),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            Icons.Outlined.Search,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                            modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.Micro)
                        )
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                        AppText(
                            text = "搜索视频、UP主...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                
                //  右侧：设置按钮
                AppIconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)
                ) {
                    AppIcon(
                        Icons.Outlined.Settings,
                        contentDescription = "设置",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                    )
                }
            }
        }
    }
}

/**
 * [HIG] iOS 风格可滑动分类标签栏。
 * - 所有分类水平平铺，支持系统惯性滚动。
 * - 使用轻量胶囊和文字强调，不再绘制顶部液态玻璃指示器。
 */
internal fun resolveTopTabUnselectedAlpha(): Float = 0.78f

internal fun resolveTopTabUnselectedColor(isLightMode: Boolean): Color {
    return if (isLightMode) {
        OpticalContrastPalette.Shadow.copy(alpha = 0.72f)
    } else {
        OpticalContrastPalette.Highlight.copy(alpha = 0.72f)
    }
}

internal fun resolveIosTopTabSelectedContentColor(colorScheme: ColorScheme): Color =
    colorScheme.primary

internal fun resolveIosTopTabCapsuleContainerColor(
    isDarkTheme: Boolean,
    selectionFraction: Float
): Color {
    val selectedAlpha = selectionFraction.coerceIn(0f, 1f)
    val baseColor = resolveBottomBarMovingIndicatorSurfaceColor(isDarkTheme = isDarkTheme)
    return baseColor.copy(alpha = 0.28f * selectedAlpha)
}

/**
 * Soft shell lens for short top docks: keeps scroll-time refraction/vibrancy closer to the
 * moving indicator, while staying below full bottom-bar rim strength that causes 虾线.
 */
internal const val TOP_DOCK_SHELL_LENS_INTENSITY = 0.55f

internal fun Modifier.homeTopBottomBarMatchedSurface(
    renderMode: HomeTopChromeRenderMode,
    shape: Shape,
    hazeState: HazeState?,
    backdrop: LayerBackdrop?,
    miuixBackdrop: MiuixBackdrop? = null,
    liquidGlassStyle: LiquidGlassStyle,
    liquidGlassTuning: LiquidGlassTuning?,
    liquidGlassPreset: BottomBarLiquidGlassPreset = BottomBarLiquidGlassPreset.BILIPAI_TUNED,
    motionTier: MotionTier,
    isTransitionRunning: Boolean,
    forceLowBlurBudget: Boolean,
    drawShellLens: Boolean = true,
    shellLensIntensity: Float = 1f,
    isScrolling: Boolean = false,
    materialScrollProgress: Float = if (isScrolling) 1f else 0f
): Modifier = composed {
    val isGlassEnabled = renderMode == HomeTopChromeRenderMode.LIQUID_GLASS_BACKDROP ||
        renderMode == HomeTopChromeRenderMode.LIQUID_GLASS_HAZE
    val isBlurEnabled = renderMode != HomeTopChromeRenderMode.PLAIN
    val blurIntensity = currentUnifiedBlurIntensity()
    val isDarkTheme = resolveBottomBarDarkTheme(AppSurfaceTokens.chromeBackground())
    val tuning = resolveAndroidNativeBottomBarTuning(
        blurEnabled = isBlurEnabled || isGlassEnabled,
        darkTheme = isDarkTheme
    )
    // Match floating bottom-bar shell tint exactly (same preset + glass material policy).
    val containerColor = resolveAndroidNativeFloatingBottomBarContainerColor(
        surfaceColor = MaterialTheme.colorScheme.surfaceContainer,
        tuning = tuning,
        glassEnabled = isGlassEnabled,
        blurEnabled = isBlurEnabled,
        blurIntensity = blurIntensity,
        liquidGlassPreset = liquidGlassPreset
    )
    this.bottomBarMatchedLiquidDockSurface(
        shape = shape,
        backdrop = miuixBackdrop,
        legacyBackdrop = backdrop,
        containerColor = containerColor,
        blurEnabled = isBlurEnabled,
        glassEnabled = isGlassEnabled,
        drawShellLens = drawShellLens,
        shellLensIntensity = shellLensIntensity,
        blurRadius = tuning.shellBlurRadiusDp.dp,
        hazeState = hazeState,
        motionTier = motionTier,
        isTransitionRunning = isTransitionRunning,
        forceLowBlurBudget = forceLowBlurBudget,
        liquidGlassPreset = liquidGlassPreset,
        isScrollInProgressProvider = { isScrolling },
        materialScrollProgressOverride = materialScrollProgress
    )
}

@Composable
private fun LightweightHomeTopTabs(
    presentation: AppTopTabPresentation,
    categories: List<String>,
    categoryKeys: List<String>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit,
    onPartitionClick: () -> Unit,
    pagerState: androidx.compose.foundation.pager.PagerState?,
    labelMode: Int,
    isFloatingStyle: Boolean,
    edgeToEdge: Boolean,
    skinPlainStyle: Boolean = false,
    skinPlainContentColor: Color? = null,
    isLiquidGlassEnabled: Boolean = false,
    liquidGlassStyle: LiquidGlassStyle = LiquidGlassStyle.CLASSIC,
    liquidGlassTuning: LiquidGlassTuning? = null,
    liquidGlassPreset: BottomBarLiquidGlassPreset = BottomBarLiquidGlassPreset.BILIPAI_TUNED,
    backdrop: LayerBackdrop? = null,
    miuixBackdrop: MiuixBackdrop? = null,
    topTabSkinIconPaths: Map<String, TopTabSkinIconPaths> = emptyMap(),
    partitionSkinIconPath: String? = null,
    hasOuterChromeSurface: Boolean = false,
    /** When non-null, overrides [shouldWrapTopTabDockWidth] so shell and tabs share one decision. */
    wrapDockWidth: Boolean? = null,
    /**
     * Cap on the dock width (top controls' combined width) so the tab strip never
     * extends beyond the avatar / settings alignment. [Float.POSITIVE_INFINITY] keeps
     * legacy full-bleed docks.
     */
    maxDockWidthDp: Float = Float.POSITIVE_INFINITY,
    isTransitionRunning: Boolean = false,
    showPartitionAction: Boolean = true,
    isViewportSyncEnabled: Boolean = true,
    forceMaterialUnderline: Boolean = false
) {
    val chromePolicy = rememberAppTopChromePolicy()
    val haptic = com.android.purebilibili.core.util.rememberHapticFeedback()
    val scrollChannel = com.android.purebilibili.feature.home.LocalHomeScrollChannel.current
    val normalizedLabelMode = normalizeTopTabLabelMode(labelMode)
    val topTabIconFamily = resolveTopTabIconFamily(
        chromeIconFamily = chromePolicy.effectiveIconFamily,
        useBottomBarMatchedChrome = isFloatingStyle || hasOuterChromeSurface,
        iconStyle = chromePolicy.iconStyle
    )
    val showIcon = shouldShowTopTabIcon(normalizedLabelMode)
    val showText = shouldShowTopTabText(normalizedLabelMode)
    val effectivePresentation = if (skinPlainStyle || forceMaterialUnderline) {
        AppTopTabPresentation.MATERIAL_UNDERLINE
    } else {
        presentation
    }
    val safeSelectedIndex = selectedIndex.coerceIn(0, (categories.size - 1).coerceAtLeast(0))
    val topTabDragMotionSpec = remember { resolveSegmentedControlMotionSpec() }
    var topTabIndicatorDragEngaged by remember { mutableStateOf(false) }
    val matchedChromeState = rememberBottomBarMatchedLiquidChromeState(
        initialIndex = safeSelectedIndex,
        itemCount = categories.size.coerceAtLeast(1),
        onIndexChanged = { index ->
            if (index in categories.indices) {
                onCategorySelected(index)
            }
        }
    )
    val topTabDragState = matchedChromeState.dragState
    LaunchedEffect(topTabDragState.settledReleaseCount) {
        if (topTabDragState.settledReleaseCount > 0) {
            topTabIndicatorDragEngaged = false
        }
    }
    // 兜底：拖动释放（isDragging 下降沿）后延时无条件解除 engaged。
    // onDragEnd 的 onSettled 若被并发 updateIndex 的 valueJob.cancel 抢占，
    // settledReleaseCount 不会递增，原 LaunchedEffect 失效会永久卡 engaged，
    // 导致后续每次 pager 滑动都触发 dragState 动画与跟手位置的竞争抖动。
    LaunchedEffect(topTabDragState.isDragging) {
        if (topTabDragState.isDragging) return@LaunchedEffect
        delay(TOP_TAB_DRAG_RELEASE_SETTLE_MS)
        if (!topTabDragState.isDragging) {
            topTabIndicatorDragEngaged = false
        }
    }
    val baseRowHeight = if (skinPlainStyle) {
        resolveHomeSkinTopTabRowHeight()
    } else when (effectivePresentation) {
        AppTopTabPresentation.MOVING_CAPSULE -> resolveIosTopTabRowHeight(isFloatingStyle, normalizedLabelMode)
        AppTopTabPresentation.MATERIAL_UNDERLINE -> resolveMd3TopTabVisualSpec(
            isFloatingStyle = isFloatingStyle,
            labelMode = normalizedLabelMode
        ).rowHeight
        AppTopTabPresentation.TONAL_CAPSULE -> resolveMd3TopTabVisualSpec(
            isFloatingStyle = false,
            presentation = AppTopTabPresentation.TONAL_CAPSULE,
            labelMode = normalizedLabelMode
        ).rowHeight
    }
    val hasSkinStickerIcons = topTabSkinIconPaths.isNotEmpty() || !partitionSkinIconPath.isNullOrBlank()
    val rowHeight = resolveTopTabSkinStickerRowHeight(
        baseRowHeight = baseRowHeight,
        hasSkinStickerIcons = hasSkinStickerIcons,
        showIcon = showIcon,
        showText = showText
    )
    val actionButtonSize = if (skinPlainStyle) {
        resolveHomeSkinTopTabActionButtonSize()
    } else when (effectivePresentation) {
        AppTopTabPresentation.MOVING_CAPSULE -> resolveIosTopTabActionButtonSize(isFloatingStyle)
        AppTopTabPresentation.MATERIAL_UNDERLINE -> resolveMd3TopTabActionButtonSize(isFloatingStyle)
        AppTopTabPresentation.TONAL_CAPSULE -> resolveMd3TopTabActionButtonSize(
            isFloatingStyle = false,
            presentation = AppTopTabPresentation.TONAL_CAPSULE
        )
    }
    val actionButtonCorner = if (skinPlainStyle) {
        AppSpacingTokens.None
    } else when (effectivePresentation) {
        AppTopTabPresentation.MOVING_CAPSULE -> resolveIosTopTabActionButtonCorner(isFloatingStyle)
        AppTopTabPresentation.MATERIAL_UNDERLINE -> resolveMd3TopTabActionButtonCorner(isFloatingStyle)
        AppTopTabPresentation.TONAL_CAPSULE -> resolveMd3TopTabActionButtonCorner(
            isFloatingStyle = false,
            presentation = AppTopTabPresentation.TONAL_CAPSULE
        )
    }
    val actionIconSize = if (skinPlainStyle) {
        resolveHomeSkinTopTabActionIconSize()
    } else when (effectivePresentation) {
        AppTopTabPresentation.MOVING_CAPSULE -> resolveIosTopTabActionIconSize(isFloatingStyle)
        AppTopTabPresentation.MATERIAL_UNDERLINE -> resolveMd3TopTabActionIconSize(isFloatingStyle)
        AppTopTabPresentation.TONAL_CAPSULE -> resolveMd3TopTabActionIconSize(
            isFloatingStyle = false,
            presentation = AppTopTabPresentation.TONAL_CAPSULE
        )
    }
    val listState = rememberLazyListState()
    var tabViewportLeftInWindowPx by remember { mutableFloatStateOf(Float.NaN) }
    var selectedItemLeftInWindowPx by remember { mutableFloatStateOf(Float.NaN) }
    val pagerIsDragging = rememberTopTabPagerDragHeld(pagerState)
    val currentPositionProvider = remember(pagerState, selectedIndex) {
        {
            resolveTopTabIndicatorRenderPosition(
                selectedIndex = selectedIndex,
                pagerCurrentPage = pagerState?.currentPage,
                pagerTargetPage = pagerState?.targetPage,
                pagerCurrentPageOffsetFraction = pagerState?.currentPageOffsetFraction,
                pagerIsScrolling = pagerState?.isScrollInProgress == true
            )
        }
    }
    val selectedContentPositionProvider = remember(pagerState, selectedIndex) {
        {
            resolveTopTabSelectedContentPosition(
                selectedIndex = selectedIndex,
                pagerCurrentPage = pagerState?.currentPage,
                pagerTargetPage = pagerState?.targetPage,
                pagerCurrentPageOffsetFraction = pagerState?.currentPageOffsetFraction,
                pagerIsScrolling = pagerState?.isScrollInProgress == true
            )
        }
    }
    val pagerScrollingProvider = remember(pagerState) {
        { pagerState?.isScrollInProgress == true }
    }
    val density = LocalDensity.current

    LaunchedEffect(selectedIndex, categories.size) {
        selectedItemLeftInWindowPx = Float.NaN
        if (categories.isNotEmpty()) {
            val targetIndex = selectedIndex.coerceIn(0, categories.lastIndex)
            if (pagerState?.isScrollInProgress == true) {
                // pager 滚动中指示器由 pager 跟手位置（currentPosition）驱动；
                // 跳过带 press/spring 动画的 updateIndex，避免 dragState 的
                // isRunning/pressProgress 与 pager 位置双源竞争造成指示器抖动。
                listState.animateScrollToItem(targetIndex)
                return@LaunchedEffect
            }
            topTabDragState.updateIndex(targetIndex)
            listState.animateScrollToItem(targetIndex)
        }
    }

    // pager 滚动结束后将 dragState 静默同步到当前选中页，保证之后拖动指示器
    // 的起点正确（滚动中跳过的 updateIndex 在此补齐，此时无位置竞争）。
    LaunchedEffect(pagerState?.isScrollInProgress, categories.size) {
        if (pagerState?.isScrollInProgress == false && categories.isNotEmpty()) {
            val targetIndex = selectedIndex.coerceIn(0, categories.lastIndex)
            topTabDragState.updateIndex(targetIndex)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowHeight)
            .padding(
                horizontal = resolveTopTabRowHorizontalPaddingDp(
                    isFloatingStyle = isFloatingStyle,
                    edgeToEdge = edgeToEdge,
                    labelMode = normalizedLabelMode
                ).dp
            )
    ) {
        val wrapDock = wrapDockWidth ?: shouldWrapTopTabDockWidth(
            isFloatingStyle = isFloatingStyle,
            hasOuterChromeSurface = hasOuterChromeSurface,
            edgeToEdge = edgeToEdge
        )
        // 分栏 dock 最大宽度 = 顶部三控件合计宽度，与外壳共享同一上限。
        val effectiveMaxDockWidth = minOf(maxWidth.value, maxDockWidthDp)
        val fillItemWidthDp = when (effectivePresentation) {
            AppTopTabPresentation.MOVING_CAPSULE -> resolveIosTopTabItemWidthDp(
                containerWidthDp = effectiveMaxDockWidth,
                categoryCount = categories.size,
                labelMode = normalizedLabelMode
            )
            AppTopTabPresentation.MATERIAL_UNDERLINE,
            AppTopTabPresentation.TONAL_CAPSULE -> resolveMd3TopTabItemWidthDp(
                containerWidthDp = effectiveMaxDockWidth,
                visibleSlots = resolveMd3TopTabLayoutVisibleSlots(
                    categoryCount = categories.size,
                    labelMode = normalizedLabelMode,
                    showPartitionAction = showPartitionAction,
                    fontScale = density.fontScale
                ),
                labelMode = normalizedLabelMode
            )
        }
        val itemWidthDp = resolveTopTabDockItemWidthDp(
            maxWidthDp = effectiveMaxDockWidth,
            categoryCount = categories.size,
            labelMode = normalizedLabelMode,
            isFloatingStyle = isFloatingStyle,
            wrapContent = wrapDock,
            fillItemWidthDp = fillItemWidthDp
        )
        val itemWidth = itemWidthDp.dp
        // Prefer content-driven dock length; parent chrome also uses this policy so shell + tabs match.
        val dockContentWidthDp = if (wrapDock) {
            resolveTopTabDockWrapWidthDp(
                itemWidthDp = itemWidthDp,
                categoryCount = categories.size,
                maxWidthDp = effectiveMaxDockWidth
            )
        } else {
            effectiveMaxDockWidth
        }
        // Match the bottom bar's actual app-surface luminance. The system theme can differ
        // from the active app theme and previously produced a dark gray capture on light pages.
        val isDarkTheme = resolveBottomBarDarkTheme(AppSurfaceTokens.background())
        // When dock wraps content, no leftover to center — lead padding is always 0.
        val md3ContentPadding = if (
            effectivePresentation != AppTopTabPresentation.MOVING_CAPSULE &&
            !wrapDock
        ) {
            resolveMd3TopTabContentPaddingDp(
                containerWidthDp = maxWidth.value,
                itemWidthDp = itemWidth.value,
                categoryCount = categories.size,
                labelMode = normalizedLabelMode
            ).dp
        } else {
            AppSpacingTokens.None
        }
        val md3IndicatorWidth = if (skinPlainStyle) AppSpacingTokens.DoubleExtraLarge - AppSpacingTokens.Micro else AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall
        val dockIndicatorHorizontalGap = resolveTopTabDockIndicatorHorizontalGapDp(
            hasOuterChromeSurface = hasOuterChromeSurface
        ).dp
        val dockIndicatorVerticalGap = resolveTopTabDockIndicatorVerticalGapDp(
            hasOuterChromeSurface = hasOuterChromeSurface
        ).dp
        val md3TopTabRowVerticalTranslationPx = with(density) {
            resolveMd3TopTabRowVerticalTranslationDp(
                skinPlainStyle = skinPlainStyle,
                hasOuterChromeSurface = hasOuterChromeSurface
            ).dp.toPx()
        }
        val rowScrollOffsetPx by remember(itemWidth, density, listState) {
            derivedStateOf {
                with(density) {
                    listState.firstVisibleItemIndex * itemWidth.toPx() +
                        listState.firstVisibleItemScrollOffset
                }
            }
        }
        val rowScrollStartPadding = with(density) { (-rowScrollOffsetPx).toDp() }
        HomeTopTabMotionLayer {
        val pagerIsScrolling = pagerScrollingProvider()
        val currentPosition = currentPositionProvider()
        val selectedContentPosition = selectedContentPositionProvider()
        val topTabDragPosition by remember(topTabDragState, categories.size) {
            derivedStateOf {
                topTabDragState.value.coerceIn(0f, (categories.size - 1).coerceAtLeast(0).toFloat())
            }
        }
        val topTabDragTargetIndex = topTabDragPosition.roundToInt()
            .coerceIn(0, (categories.size - 1).coerceAtLeast(0))
        val topTabDragActive by remember(topTabDragState, topTabIndicatorDragEngaged) {
            derivedStateOf {
                topTabIndicatorDragEngaged &&
                    (topTabDragState.isDragging || topTabDragState.isRunning || topTabDragState.pressProgress > 0.001f)
            }
        }
        val topTabIndicatorPosition = if (topTabDragActive) topTabDragPosition else currentPosition
        val topTabContentPosition = if (topTabDragActive) {
            topTabDragPosition
        } else if (effectivePresentation == AppTopTabPresentation.MOVING_CAPSULE) {
            selectedContentPosition
        } else {
            currentPosition
        }
        LaunchedEffect(topTabDragActive, topTabDragTargetIndex, isViewportSyncEnabled) {
            if (!isViewportSyncEnabled || !topTabDragActive) return@LaunchedEffect
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val firstVisibleIndex = visibleItems.firstOrNull()?.index ?: return@LaunchedEffect
            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: return@LaunchedEffect
            if (topTabDragTargetIndex !in firstVisibleIndex..lastVisibleIndex) {
                listState.scrollToItem(topTabDragTargetIndex)
            }
        }
        val iosCapsulePosition = if (topTabDragActive) topTabDragPosition else selectedContentPosition
        val indicatorIsInteracting = pagerIsDragging || pagerIsScrolling || topTabDragActive
        val topTabShouldStretchIndicator = (topTabDragActive && topTabDragState.isDragging) ||
            shouldDeformTopTabIndicator(
                position = topTabIndicatorPosition,
                isInMotion = indicatorIsInteracting
            )
        val topTabVelocityPositionTracker = remember { FloatArray(1) { topTabIndicatorPosition } }
        val topTabVelocityTimeTracker = remember { LongArray(1) { System.nanoTime() } }
        val topTabPagerVelocityItemsPerSecond = if (topTabDragActive) {
            0f
        } else {
            resolveTopTabPagerVelocityItemsPerSecond(
                currentPosition = topTabIndicatorPosition,
                previousPosition = topTabVelocityPositionTracker[0],
                elapsedNanos = (System.nanoTime() - topTabVelocityTimeTracker[0]).coerceAtLeast(1L)
            )
        }
        SideEffect {
            topTabVelocityPositionTracker[0] = topTabIndicatorPosition
            topTabVelocityTimeTracker[0] = System.nanoTime()
        }
        val topTabMotionVelocityItemsPerSecond = if (topTabDragActive) {
            topTabDragState.deformationVelocityItemsPerSecond
        } else {
            topTabPagerVelocityItemsPerSecond
        }
        val topTabIndicatorLayerVelocityItemsPerSecond =
            resolveTopTabIndicatorLayerVelocityItemsPerSecond(
                motionVelocityItemsPerSecond = topTabMotionVelocityItemsPerSecond
            )
        val topTabMotionVelocityPxPerSecond = with(density) {
            if (topTabDragActive) {
                topTabDragState.velocityPxPerSecond
            } else {
                topTabMotionVelocityItemsPerSecond * itemWidth.toPx()
            }
        }
        val topTabIndicatorDragScaleProgress = rememberBottomBarIndicatorDragScaleProgress(
            isDragging = topTabShouldStretchIndicator
        )
        val topTabPressProgress = if (topTabDragActive) {
            topTabDragState.pressProgress
        } else {
            0f
        }
        val topTabIndicatorLayerScaleProgress = resolveTopTabIndicatorScaleProgress(
            dragScaleProgress = topTabIndicatorDragScaleProgress,
            pressProgress = topTabPressProgress
        )
        val topTabRefractionMotionProfile = resolveBottomBarRefractionMotionProfile(
            position = topTabIndicatorPosition,
            velocity = topTabMotionVelocityPxPerSecond,
            isDragging = indicatorIsInteracting,
            motionSpec = topTabDragMotionSpec
        )
        val topTabMotionProgress = resolveSegmentedControlMotionProgress(
            pressProgress = topTabPressProgress,
            refractionProgress = topTabRefractionMotionProfile.progress,
            tapPressRefractionEnabled = true
        )
        val topTabDragPanelOffsetPx by remember(density, itemWidth, categories.size, topTabDragState) {
            derivedStateOf {
                val dockWidthPx = with(density) {
                    (itemWidth * categories.size.coerceAtLeast(1)).toPx()
                }.coerceAtLeast(1f)
                val maxOffsetPx = with(density) { AppSpacingTokens.ExtraSmall.toPx() }
                resolveSharedLiquidIndicatorPanelOffsetPx(
                    dragOffsetPx = topTabDragState.dragOffset,
                    dockWidthPx = dockWidthPx,
                    maxOffsetPx = maxOffsetPx
                )
            }
        }
        val topTabPanelOffsetPx = resolveTopTabMatchedPanelOffsetPx(
            dragPanelOffsetPx = topTabDragPanelOffsetPx,
            pagerPanelOffsetFraction = topTabRefractionMotionProfile.indicatorPanelOffsetFraction,
            maxOffsetPx = with(density) { AppSpacingTokens.ExtraSmall.toPx() },
            dragActive = topTabDragActive
        )
        // Pager swipes have no direct press event. Reuse the bottom-bar drag-scale animation
        // as their effective press so the indicator surface fades and lens ramps identically.
        val topTabLensProgress = topTabIndicatorLayerScaleProgress
        // Swipe/press lens progress so theme-tinted glass follows the capsule.
        val topTabIndicatorLensSpec = resolveBottomBarBackdropPresetIndicatorLens(
            progress = topTabLensProgress
        )
        val md3LiquidCapsuleWidth = resolveTopTabDockIndicatorWidthDp(
            itemWidthDp = itemWidth.value,
            horizontalGapDp = dockIndicatorHorizontalGap.value,
            minWidthDp = md3IndicatorWidth.value
        ).dp
        val dockIndicatorHeight = resolveTopTabDockIndicatorHeightDp(
            rowHeightDp = rowHeight.value,
            verticalGapDp = dockIndicatorVerticalGap.value,
            // Prefer near-full dock fill at rest; the selected-tab pill keeps the same
            // breathing gap above and below so it never bleeds past the tab row.
            minHeightDp = resolveTopTabVisualTuning().floatingIndicatorHeightDp,
            indicatorWidthDp = md3LiquidCapsuleWidth.value
        ).dp
        // Selected-tab pill position: item slot center minus half the pill width, so the
        // capsule follows the pager offset and the row scroll while staying inside the dock.
        val md3IndicatorTranslationXPx by remember(
            topTabIndicatorPosition,
            itemWidth,
            md3LiquidCapsuleWidth,
            density,
            listState
        ) {
            derivedStateOf {
                with(density) {
                    resolveMd3TopTabIndicatorTranslationPx(
                        absolutePagerPosition = topTabIndicatorPosition,
                        itemWidthPx = itemWidth.toPx(),
                        rowScrollOffsetPx = rowScrollOffsetPx,
                        indicatorWidthPx = md3LiquidCapsuleWidth.toPx(),
                        contentPaddingPx = md3ContentPadding.toPx()
                    )
                }
            }
        }
        val shouldUseMovingIosCapsule = effectivePresentation == AppTopTabPresentation.MOVING_CAPSULE &&
            !skinPlainStyle &&
            !hasSkinStickerIcons
        val shouldUseLiquidGlassIndicator = isLiquidGlassEnabled &&
            !skinPlainStyle &&
            !hasSkinStickerIcons
        // 移动胶囊本体与玻璃状态解耦：MATERIAL_UNDERLINE 始终渲染底栏同款胶囊，
        // 液态玻璃只切换胶囊材质（glassEnabled），玻璃关闭时回退纯色胶囊，两主题渲染恒定。
        val shouldUseMd3LiquidCapsule = effectivePresentation == AppTopTabPresentation.MATERIAL_UNDERLINE &&
            !skinPlainStyle &&
            !hasSkinStickerIcons &&
            !hasOuterChromeSurface
        val shouldUseMd3DockBackedCapsule = effectivePresentation == AppTopTabPresentation.MATERIAL_UNDERLINE &&
            !skinPlainStyle &&
            !hasSkinStickerIcons &&
            hasOuterChromeSurface
        val shouldPrimeTopTabLiquidGlassCapture =
            isLiquidGlassEnabled &&
                !skinPlainStyle &&
                !hasSkinStickerIcons
        val topTabContentBackdrop = rememberLayerBackdrop()
        val topTabMiuixContentBackdrop = rememberMiuixLayerBackdrop()
        val topTabIndicatorVisualPolicy = resolveTopTabIndicatorVisualPolicy(
            position = topTabIndicatorPosition,
            interacting = indicatorIsInteracting,
            velocityPxPerSecond = topTabMotionVelocityPxPerSecond,
            useNeutralIndicatorTint = shouldUseLiquidGlassIndicator
        )
        val topTabIndicatorBackdropPolicy = resolveTopTabIndicatorBackdropPolicy(
            effectiveLiquidGlassEnabled = shouldUseLiquidGlassIndicator,
            hasBackdrop = backdrop != null || miuixBackdrop != null,
            indicatorVisualPolicy = topTabIndicatorVisualPolicy
        )
        // 跟手/滑动/settle 动画期间禁用导出捕获：捕获帧是定格快照，胶囊平移时
        // 快照内容不随胶囊移动，会导致「图标+文字」被错误地多渲染一份
        // （主层原位一份 + 胶囊内旧快照一份）。过渡中回退纯色胶囊，settle 后恢复。
        val topTabIndicatorTransitioning = indicatorIsInteracting ||
            topTabShouldStretchIndicator ||
            topTabMotionProgress < 0.999f
        // Match the bottom bar's two-source topology. The local source first records the
        // already-frosted dock material and tinted labels, so the indicator never falls
        // back to a raw-page-only frame during idle/gesture transitions.
        // 过渡中 contentBackdrop 也置 null：KernelSu 层在 combined preset 下优先取
        // contentBackdrop 作为捕获源，仅置空 backdrop 无法真正降级。
        val effectiveTopTabMiuixContentBackdrop = when {
            topTabIndicatorTransitioning -> null
            topTabIndicatorBackdropPolicy.useCombinedBackdrop && miuixBackdrop != null ->
                rememberMiuixCombinedBackdrop(miuixBackdrop, topTabMiuixContentBackdrop)
            else -> topTabMiuixContentBackdrop
        }
        val topTabIndicatorMiuixBackdrop =
            if (!topTabIndicatorTransitioning &&
                topTabIndicatorBackdropPolicy.useIndicatorBackdrop &&
                miuixBackdrop != null
            ) {
                miuixBackdrop
            } else {
                null
            }
        val topTabIndicatorLegacyBackdrop =
            if (!topTabIndicatorTransitioning &&
                topTabIndicatorBackdropPolicy.useIndicatorBackdrop &&
                backdrop != null
            ) {
                topTabContentBackdrop
            } else {
                null
            }
        val topTabIndicatorCaptureSurfaceColor =
            resolveKernelSuBottomBarContainerColor(darkTheme = isDarkTheme)
        // The bottom bar keeps its export-capture lens fully active while glass is enabled.
        val topTabCaptureLensProgress = if (shouldUseLiquidGlassIndicator) 1f else 0f
        val topTabCaptureLensSpec = resolveBottomBarBackdropPresetCaptureLens(
            progress = topTabCaptureLensProgress
        )
        val topTabCaptureHighlightAlpha =
            resolveBottomBarLiquidGlassHighlightAlpha(topTabCaptureLensProgress)
        val topTabCaptureBlurRadius = resolveAndroidNativeBottomBarTuning(
            blurEnabled = true,
            darkTheme = isDarkTheme
        ).shellBlurRadiusDp.dp
        val topTabIndicatorIdleSurfaceColor = resolveBottomBarIdleIndicatorSurfaceColor(
            preset = liquidGlassPreset,
            darkTheme = isDarkTheme
        )
        // 过渡中退出玻璃颜色路径：内容层回到胶囊上方（zIndex 2f），
        // 降级为纯色胶囊 + 主层文字，避免纯色胶囊盖住选中项。
        val useTopTabGlassColorPath = resolveSharedLiquidIndicatorUseGlassColorPath(
            liquidGlassEnabled = shouldUseLiquidGlassIndicator && !topTabIndicatorTransitioning,
            lensProgress = topTabLensProgress
        )
        val topTabVisibleContentZIndex = if (useTopTabGlassColorPath) 0f else 2f
        val topTabThemeColor = MaterialTheme.colorScheme.primary
        val topTabExportTintColor = resolveAndroidNativeExportTintColor(
            themeColor = topTabThemeColor,
            darkTheme = isDarkTheme
        )
        val topTabExportMonochromeColor = resolveSharedLiquidExportMonochromeColor(
            darkTheme = isDarkTheme
        )
        val measuredSelectedItemLeftPx by remember(shouldUseMovingIosCapsule) {
            derivedStateOf {
                if (!shouldUseMovingIosCapsule ||
                    tabViewportLeftInWindowPx.isNaN() ||
                    selectedItemLeftInWindowPx.isNaN()
                ) {
                    null
                } else {
                    selectedItemLeftInWindowPx - tabViewportLeftInWindowPx
                }
            }
        }
        val iosCapsuleTargetTranslationXPx by remember(
            iosCapsulePosition,
            measuredSelectedItemLeftPx,
            itemWidth,
            density,
            rowScrollOffsetPx,
            pagerState,
            pagerIsDragging,
            topTabDragActive
        ) {
            derivedStateOf {
                with(density) {
                    resolveIosTopTabCapsuleTargetTranslationPx(
                        measuredSelectedItemLeftPx = measuredSelectedItemLeftPx,
                        absolutePagerPosition = iosCapsulePosition,
                        itemWidthPx = itemWidth.toPx(),
                        rowScrollOffsetPx = rowScrollOffsetPx,
                        contentPaddingPx = IOS_TOP_TAB_CONTENT_PADDING_DP.dp.toPx(),
                        followPagerPosition = pagerIsDragging || pagerIsScrolling || topTabDragActive
                    )
                }
            }
        }
        val shouldAnimateIosCapsule = shouldAnimateIosTopTabCapsule(
            pagerIsDragging = pagerIsDragging,
            pagerIsScrolling = pagerIsScrolling || topTabDragActive
        )
        val animatedIosCapsuleTranslationXPx by animateFloatAsState(
            targetValue = iosCapsuleTargetTranslationXPx,
            animationSpec = iosTopTabCapsuleMotionSpec(),
            label = "iosTopTabCapsuleTranslation"
        )
        val iosCapsuleTranslationXPx = if (shouldAnimateIosCapsule) {
            animatedIosCapsuleTranslationXPx
        } else {
            iosCapsuleTargetTranslationXPx
        }
        Row(
            modifier = Modifier
                .then(
                    if (wrapDock) {
                        // 与搜索行左对齐（头像左缘），宽度封顶于设置按钮右缘。
                        Modifier
                            .width(dockContentWidthDp.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                    } else {
                        Modifier.fillMaxSize()
                    }
                )
                .graphicsLayer {
                    translationY = if (effectivePresentation == AppTopTabPresentation.MATERIAL_UNDERLINE) {
                        md3TopTabRowVerticalTranslationPx
                    } else {
                        0f
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onGloballyPositioned { coordinates ->
                        tabViewportLeftInWindowPx = coordinates.boundsInWindow().left
                    }
            ) {
                val topTabHorizontalPadding = if (effectivePresentation == AppTopTabPresentation.MOVING_CAPSULE) {
                    IOS_TOP_TAB_CONTENT_PADDING_DP.dp
                } else {
                    md3ContentPadding
                }
                val topTabContentPadding = PaddingValues(horizontal = topTabHorizontalPadding)
                // Frame-synced export scroll (no second LazyList / LaunchedEffect lag → no ghost).
                val topTabListScrollOffsetPx by remember(density, itemWidth, listState) {
                    derivedStateOf {
                        with(density) {
                            listState.firstVisibleItemIndex * itemWidth.toPx() +
                                listState.firstVisibleItemScrollOffset.toFloat()
                        }
                    }
                }
                val topTabContentPanelOffsetPx =
                    if (shouldUseLiquidGlassIndicator) topTabPanelOffsetPx else 0f
                val topTabHorizontalPaddingPx = with(density) { topTabHorizontalPadding.toPx() }
                // One shared shift for export + visible + capsule avoids double panel offset ghosts.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationX = topTabContentPanelOffsetPx }
                ) {
                // Match the bottom bar: keep the export capture inside the dock band.
                // When the indicator scales beyond it, the combined backdrop exposes the
                // page source above and below instead of stretching dock material outward.
                if (shouldPrimeTopTabLiquidGlassCapture) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clearAndSetSemantics {}
                            .alpha(0f)
                            .zIndex(0f)
                            .run {
                                when {
                                    miuixBackdrop != null -> {
                                        miuixLayerBackdrop(topTabMiuixContentBackdrop)
                                            .graphicsLayer {
                                                // Only mirror LazyRow content origin (padding - scroll).
                                                translationX =
                                                    topTabHorizontalPaddingPx - topTabListScrollOffsetPx
                                            }
                                            .miuixDrawBackdrop(
                                                backdrop = miuixBackdrop,
                                                shape = { resolveSharedBottomBarCapsuleShape() },
                                                effects = {
                                                    miuixVibrancy()
                                                    miuixBlur(
                                                        AppSpacingTokens.ExtraSmall.toPx(),
                                                        AppSpacingTokens.ExtraSmall.toPx()
                                                    )
                                                    if (
                                                        shouldUseBottomBarCaptureLens(
                                                            shouldUseLiquidGlassIndicator
                                                        )
                                                    ) {
                                                        miuixLens(
                                                            refractionHeight =
                                                                topTabCaptureLensSpec.refractionHeightDp.dp.toPx(),
                                                            refractionAmount =
                                                                topTabCaptureLensSpec.refractionAmountDp.dp.toPx(),
                                                            depthEffect = true,
                                                            chromaticAberration = 0.5f
                                                        )
                                                    }
                                                },
                                                onDrawSurface = {
                                                    drawRect(topTabIndicatorCaptureSurfaceColor)
                                                }
                                            )
                                    }

                                    backdrop != null -> {
                                        layerBackdrop(topTabContentBackdrop)
                                            .graphicsLayer {
                                                translationX =
                                                    topTabHorizontalPaddingPx - topTabListScrollOffsetPx
                                            }
                                            .drawBackdrop(
                                                backdrop = backdrop,
                                                shape = { resolveSharedBottomBarCapsuleShape() },
                                                effects = {
                                                    vibrancy()
                                                    blur(topTabCaptureBlurRadius.toPx())
                                                    if (
                                                        shouldUseBottomBarCaptureLens(
                                                            shouldUseLiquidGlassIndicator
                                                        )
                                                    ) {
                                                        lens(
                                                            refractionHeight =
                                                                topTabCaptureLensSpec.refractionHeightDp.dp.toPx(),
                                                            refractionAmount =
                                                                topTabCaptureLensSpec.refractionAmountDp.dp.toPx(),
                                                            depthEffect = true,
                                                            chromaticAberration = true
                                                        )
                                                    }
                                                },
                                                highlight = {
                                                    Highlight.Default.copy(
                                                        alpha = topTabCaptureHighlightAlpha
                                                    )
                                                },
                                                onDrawSurface = {
                                                    drawRect(topTabIndicatorCaptureSurfaceColor)
                                                }
                                            )
                                    }

                                    else -> {
                                        layerBackdrop(topTabContentBackdrop)
                                            .graphicsLayer {
                                                translationX =
                                                    topTabHorizontalPaddingPx - topTabListScrollOffsetPx
                                            }
                                            .background(topTabIndicatorCaptureSurfaceColor)
                                    }
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            categories.forEachIndexed { index, category ->
                                val categoryKey = categoryKeys.getOrNull(index) ?: category
                                LightweightTopTabItem(
                                    presentation = effectivePresentation,
                                    iconFamily = topTabIconFamily,
                                    category = category,
                                    categoryKey = categoryKey,
                                    index = index,
                                    selectionFraction = 1f,
                                    selectedIndex = selectedIndex,
                                    showIcon = showIcon,
                                    showText = showText,
                                    itemWidth = itemWidth,
                                    skinPlainStyle = false,
                                    drawContainer = false,
                                    skinIconPaths = null,
                                    hasSkinStickerIcon = false,
                                    useClickIndication = false,
                                    colorMode = TopTabLiquidColorMode.GLASS_EXPORT,
                                    exportMonochromeColor = topTabExportMonochromeColor,
                                    modifier = Modifier.graphicsLayer(
                                        colorFilter = ColorFilter.tint(topTabExportTintColor)
                                    ),
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        // At rest, keep glyphs visible above the idle capsule. During the
                        // glass motion path they must sit below it, so exported tint moves
                        // with the indicator instead of leaving the old glyph color on top.
                        .zIndex(topTabVisibleContentZIndex),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    contentPadding = topTabContentPadding
                ) {
                    itemsIndexed(
                        items = categories,
                        key = { index, category -> categoryKeys.getOrNull(index) ?: category }
                    ) { index, category ->
                        val categoryKey = categoryKeys.getOrNull(index) ?: category
                        val selectionFraction = (1f - abs(topTabContentPosition - index.toFloat())).coerceIn(0f, 1f)
                        val drawItemContainer = shouldDrawLightweightTopTabItemContainer(
                            presentation = effectivePresentation,
                            skinPlainStyle = skinPlainStyle,
                            hasSkinStickerIcon = hasSkinStickerIcons
                        )
                        val measuredItemModifier = if (shouldUseMovingIosCapsule && index == selectedIndex) {
                            Modifier.onGloballyPositioned { coordinates ->
                                selectedItemLeftInWindowPx = coordinates.boundsInWindow().left
                            }
                        } else {
                            Modifier
                        }
                        val gestureItemModifier = if (index == safeSelectedIndex) {
                            measuredItemModifier.topTabSelectedItemDrag(
                                dragState = topTabDragState,
                                itemWidthPx = with(density) { itemWidth.toPx() },
                                itemCount = categories.size,
                                onDragEngaged = {
                                    topTabIndicatorDragEngaged = true
                                }
                            )
                        } else {
                            measuredItemModifier
                        }
                        LightweightTopTabItem(
                            presentation = effectivePresentation,
                            iconFamily = topTabIconFamily,
                            category = category,
                            categoryKey = categoryKey,
                            index = index,
                            selectionFraction = selectionFraction,
                            selectedIndex = selectedIndex,
                            showIcon = showIcon,
                            showText = showText,
                            itemWidth = itemWidth,
                            skinPlainStyle = skinPlainStyle,
                            skinPlainContentColor = skinPlainContentColor,
                            drawContainer = drawItemContainer,
                            skinIconPaths = topTabSkinIconPaths[categoryKey.trim().uppercase()],
                            hasSkinStickerIcon = hasSkinStickerIcons,
                            useClickIndication = shouldUseLightweightTopTabItemClickIndication(
                                presentation = effectivePresentation,
                                skinPlainStyle = skinPlainStyle,
                                usesCapsuleIndicator = shouldUseMovingIosCapsule ||
                                    shouldUseMd3LiquidCapsule ||
                                    shouldUseMd3DockBackedCapsule
                            ),
                            // Tonal capsule 自己渲染选中背景；不能走仅供独立玻璃指示器使用的
                            // GLASS_VISIBLE 路径，否则会同时失去背景和选中色。
                            colorMode = if (
                                useTopTabGlassColorPath &&
                                    effectivePresentation != AppTopTabPresentation.TONAL_CAPSULE
                            ) {
                                TopTabLiquidColorMode.GLASS_VISIBLE
                            } else {
                                TopTabLiquidColorMode.NORMAL
                            },
                            modifier = gestureItemModifier,
                            onClick = {
                                performHomeTopBarTap(haptic = haptic, onClick = {
                                    when (resolveTopTabClickAction(index, selectedIndex)) {
                                        TopTabClickAction.SELECT_TAB -> onCategorySelected(index)
                                        TopTabClickAction.SCROLL_TO_TOP -> scrollChannel?.trySend(Unit)
                                    }
                                })
                            }
                        )
                    }
                }
                // Keep the indicator between its capture layer and the visible tab content.
                // The parent owns the panel offset; clip=false lets the bottom-bar motion
                // transform exceed the dock chrome.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .graphicsLayer { clip = false }
                ) {
                    if (shouldUseMovingIosCapsule) {
                        val capsuleShape = resolveSharedBottomBarCapsuleShape()
                        val indicatorWidth = resolveTopTabDockIndicatorWidthDp(
                            itemWidthDp = itemWidth.value,
                            horizontalGapDp = dockIndicatorHorizontalGap.value
                        ).dp
                        BottomBarMatchedLiquidIndicator(
                            visible = true,
                            dockContentAlpha = 1f,
                            indicatorTranslationXPx = resolveTopTabDockIndicatorOffsetPx(
                                slotTranslationPx = iosCapsuleTranslationXPx,
                                horizontalGapPx = with(density) {
                                    dockIndicatorHorizontalGap.toPx()
                                }
                            ),
                            indicatorPanelOffsetPx = 0f,
                            indicatorWidth = indicatorWidth,
                            indicatorHeight = dockIndicatorHeight,
                            shellShape = capsuleShape,
                            liquidGlassPreset = liquidGlassPreset,
                            // Prefer export capture so capsule shows theme-tinted glyphs.
                            contentBackdrop = effectiveTopTabMiuixContentBackdrop,
                            backdrop = topTabIndicatorMiuixBackdrop,
                            legacyContentBackdrop = topTabContentBackdrop,
                            legacyBackdrop = topTabIndicatorLegacyBackdrop,
                            indicatorLensSpec = topTabIndicatorLensSpec,
                            effectivePressProgress = topTabLensProgress,
                            indicatorIdleSurfaceColor = topTabIndicatorIdleSurfaceColor,
                            glassEnabled = shouldUseLiquidGlassIndicator,
                            indicatorEffectsEnabled = shouldUseLiquidGlassIndicator,
                            motionProgress = topTabMotionProgress,
                            velocityItemsPerSecond = topTabIndicatorLayerVelocityItemsPerSecond,
                            isDragging = topTabShouldStretchIndicator,
                            indicatorLayerScaleProgress = topTabIndicatorLayerScaleProgress,
                            bottomBarMotionSpec = topTabDragMotionSpec,
                            isDarkTheme = isDarkTheme
                        )
                    }
                    if (shouldUseMd3DockBackedCapsule) {
                        BottomBarMatchedLiquidIndicator(
                            visible = true,
                            dockContentAlpha = 1f,
                            indicatorTranslationXPx = md3IndicatorTranslationXPx,
                            indicatorPanelOffsetPx = 0f,
                            indicatorWidth = md3LiquidCapsuleWidth,
                            indicatorHeight = dockIndicatorHeight,
                            shellShape = resolveSharedBottomBarCapsuleShape(),
                            liquidGlassPreset = liquidGlassPreset,
                            // Prefer export capture so capsule shows theme-tinted glyphs.
                            contentBackdrop = effectiveTopTabMiuixContentBackdrop,
                            backdrop = topTabIndicatorMiuixBackdrop,
                            legacyContentBackdrop = topTabContentBackdrop,
                            legacyBackdrop = topTabIndicatorLegacyBackdrop,
                            indicatorLensSpec = topTabIndicatorLensSpec,
                            effectivePressProgress = topTabLensProgress,
                            indicatorIdleSurfaceColor = topTabIndicatorIdleSurfaceColor,
                            glassEnabled = shouldUseLiquidGlassIndicator,
                            motionProgress = topTabMotionProgress,
                            velocityItemsPerSecond = topTabIndicatorLayerVelocityItemsPerSecond,
                            isDragging = topTabShouldStretchIndicator,
                            indicatorLayerScaleProgress = topTabIndicatorLayerScaleProgress,
                            bottomBarMotionSpec = topTabDragMotionSpec,
                            isDarkTheme = isDarkTheme
                        )
                    }
                    if (shouldUseMd3LiquidCapsule) {
                        val capsuleShape = resolveSharedBottomBarCapsuleShape()
                        BottomBarMatchedLiquidIndicator(
                            visible = true,
                            dockContentAlpha = 1f,
                            indicatorTranslationXPx = md3IndicatorTranslationXPx,
                            indicatorPanelOffsetPx = 0f,
                            indicatorWidth = md3LiquidCapsuleWidth,
                            indicatorHeight = dockIndicatorHeight,
                            shellShape = capsuleShape,
                            liquidGlassPreset = liquidGlassPreset,
                            // Prefer export capture so capsule shows theme-tinted glyphs.
                            contentBackdrop = effectiveTopTabMiuixContentBackdrop,
                            backdrop = topTabIndicatorMiuixBackdrop,
                            legacyContentBackdrop = topTabContentBackdrop,
                            legacyBackdrop = topTabIndicatorLegacyBackdrop,
                            indicatorLensSpec = topTabIndicatorLensSpec,
                            effectivePressProgress = topTabLensProgress,
                            indicatorIdleSurfaceColor = topTabIndicatorIdleSurfaceColor,
                            glassEnabled = shouldUseLiquidGlassIndicator,
                            motionProgress = topTabMotionProgress,
                            velocityItemsPerSecond = topTabIndicatorLayerVelocityItemsPerSecond,
                            isDragging = topTabShouldStretchIndicator,
                            indicatorLayerScaleProgress = topTabIndicatorLayerScaleProgress,
                            bottomBarMotionSpec = topTabDragMotionSpec,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
                } // shared panel-offset group (export + visible + capsule)

                // 纯色 wash 胶囊仅在 skin 主题下兜底（skin 不走移动胶囊路径）；
                // 常规主题始终由移动胶囊负责，玻璃只切换胶囊材质。
                if (effectivePresentation == AppTopTabPresentation.MATERIAL_UNDERLINE && !hasSkinStickerIcons && skinPlainStyle) {
                    val indicatorColor = if (skinPlainContentColor != null) {
                        resolveHomeSkinTopTabIndicatorColor(skinPlainContentColor)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    if (!shouldUseMd3DockBackedCapsule && !shouldUseMd3LiquidCapsule) {
                        // Selected-tab capsule: fully rounded (max corner radius) and sized to
                        // the dock track minus breathing gap, so it never bleeds above or below
                        // the tab row. No drag scale is applied here — only the liquid-glass
                        // capsule paths may overflow the dock chrome.
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .graphicsLayer {
                                    translationX = md3IndicatorTranslationXPx
                                }
                                .width(md3LiquidCapsuleWidth)
                                .height(dockIndicatorHeight)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(indicatorColor.copy(alpha = 0.12f))
                        )
                    }
                }
            }

            if (showPartitionAction) {
                Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall))

                Box(
                    modifier = Modifier
                        .size(actionButtonSize)
                        .then(
                            if (skinPlainStyle) {
                                Modifier
                            } else {
                                Modifier.clip(RoundedCornerShape(actionButtonCorner))
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current
                        ) {
                            performHomeTopBarTap(haptic = haptic, onClick = onPartitionClick)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!partitionSkinIconPath.isNullOrBlank()) {
                        AsyncImage(
                            model = File(partitionSkinIconPath),
                            contentDescription = "浏览全部分区",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(resolveTopTabSkinPartitionIconSize())
                        )
                    } else {
                        AppIcon(
                            resolveTopTabPartitionIcon(topTabIconFamily),
                            contentDescription = "浏览全部分区",
                            tint = skinPlainContentColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(actionIconSize)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro))
            }
        }
        }
    }
}

@Composable
private fun HomeTopTabMotionLayer(
    content: @Composable () -> Unit
) {
    content()
}

internal enum class TopTabLiquidColorMode {
    /** Normal selected/unselected lerp. */
    NORMAL,
    /** Visible layer while glass is sliding — neutral so theme color lives under glass. */
    GLASS_VISIBLE,
    /** Hidden export layer monochrome glyphs before theme ColorFilter.tint. */
    GLASS_EXPORT
}

@Composable
private fun LightweightTopTabItem(
    presentation: AppTopTabPresentation,
    iconFamily: AppSemanticIconFamily,
    category: String,
    categoryKey: String,
    index: Int,
    selectionFraction: Float,
    selectedIndex: Int,
    showIcon: Boolean,
    showText: Boolean,
    itemWidth: Dp,
    skinPlainStyle: Boolean = false,
    skinPlainContentColor: Color? = null,
    drawContainer: Boolean = true,
    skinIconPaths: TopTabSkinIconPaths? = null,
    hasSkinStickerIcon: Boolean = false,
    useClickIndication: Boolean = true,
    colorMode: TopTabLiquidColorMode = TopTabLiquidColorMode.NORMAL,
    exportMonochromeColor: Color = OpticalContrastPalette.Highlight,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkTheme = isSystemInDarkTheme()
    val selected = selectionFraction > 0.5f || index == selectedIndex
    val skinIconPath = skinIconPaths?.pathFor(selected)
    val unselectedIcon = resolveTopTabCategoryIcon(
        categoryKey = categoryKey,
        iconFamily = iconFamily,
        selected = false
    )
    val selectedIcon = resolveTopTabCategoryIcon(
        categoryKey = categoryKey,
        iconFamily = iconFamily,
        selected = true
    )
    val selectedColor = when (presentation) {
        AppTopTabPresentation.MOVING_CAPSULE -> if (skinPlainStyle) {
            skinPlainContentColor ?: colorScheme.onSurface
        } else {
            resolveIosTopTabSelectedContentColor(colorScheme)
        }
        AppTopTabPresentation.MATERIAL_UNDERLINE -> if (skinPlainStyle) {
            skinPlainContentColor ?: colorScheme.onSurface
        } else {
            colorScheme.primary
        }
        AppTopTabPresentation.TONAL_CAPSULE -> if (skinPlainStyle) {
            skinPlainContentColor ?: colorScheme.onSurface
        } else {
            colorScheme.onSecondaryContainer
        }
    }
    val unselectedColor = if (skinPlainStyle) {
        resolveHomeSkinTopTabUnselectedContentColor(skinPlainContentColor ?: colorScheme.onSurface)
    } else {
        colorScheme.onSurfaceVariant
    }
    val contentColor = when (colorMode) {
        TopTabLiquidColorMode.GLASS_EXPORT -> exportMonochromeColor
        TopTabLiquidColorMode.GLASS_VISIBLE -> unselectedColor
        TopTabLiquidColorMode.NORMAL -> androidx.compose.ui.graphics.lerp(
            unselectedColor,
            selectedColor,
            selectionFraction
        )
    }
    val containerColor = when {
        !drawContainer || colorMode == TopTabLiquidColorMode.GLASS_EXPORT -> Color.Transparent
        skinPlainStyle -> Color.Transparent
        presentation == AppTopTabPresentation.MOVING_CAPSULE && colorMode == TopTabLiquidColorMode.NORMAL ->
            resolveIosTopTabCapsuleContainerColor(
                isDarkTheme = isDarkTheme,
                selectionFraction = selectionFraction
            )
        presentation == AppTopTabPresentation.MATERIAL_UNDERLINE -> Color.Transparent
        // 即使启用了液态玻璃，Tonal 栏目也由自身提供稳定的圆角选中态。
        presentation == AppTopTabPresentation.TONAL_CAPSULE ->
            colorScheme.secondaryContainer.copy(alpha = 0.70f * selectionFraction)
        colorMode == TopTabLiquidColorMode.GLASS_VISIBLE -> Color.Transparent
        else -> Color.Transparent
    }
    val itemShape = when {
        skinPlainStyle -> androidx.compose.ui.graphics.RectangleShape
        presentation == AppTopTabPresentation.MOVING_CAPSULE -> resolveSharedBottomBarCapsuleShape()
        presentation == AppTopTabPresentation.MATERIAL_UNDERLINE -> androidx.compose.ui.graphics.RectangleShape
        // Tonal capsule uses the same fully rounded pill as the plain selected-tab indicator.
        else -> RoundedCornerShape(percent = 50)
    }
    // Compact dock: keep side padding small so five tabs do not collapse to "...".
    val itemContentHorizontalPadding = AppSpacingTokens.ExtraSmall

    Box(
        modifier = modifier
            .width(itemWidth)
            .fillMaxHeight()
            .padding(
                horizontal = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
                vertical = if (hasSkinStickerIcon) {
                    resolveTopTabSkinStickerItemVerticalPadding(showText = showText)
                } else {
                    resolveTopTabDockIndicatorVerticalGapDp(hasOuterChromeSurface = false).dp
                }
            )
            .clip(itemShape)
            .background(containerColor, itemShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = if (useClickIndication) LocalIndication.current else null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = itemContentHorizontalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showIcon) {
                if (!skinIconPath.isNullOrBlank()) {
                    AsyncImage(
                        model = File(skinIconPath),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(resolveTopTabSkinStickerIconSize(showText = showText))
                    )
                } else {
                    TopTabBlendedIcon(
                        unselectedIcon = unselectedIcon,
                        selectedIcon = selectedIcon,
                        selectedAlpha = selectionFraction,
                        tint = contentColor,
                        modifier = Modifier.size(
                            resolveTopTabIconSizeDp(if (showText) 0 else 1).dp
                        )
                    )
                }
            }
            if (showIcon && showText) {
                Spacer(modifier = Modifier.width(resolveTopTabIconTextSpacingDp(0).dp))
            }
            if (showText) {
                val labelMode = when {
                    showIcon && showText -> 0
                    showIcon -> 1
                    else -> 2
                }
                AppText(
                    text = category,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = resolveTopTabLabelTextSizeSp(labelMode).sp,
                    lineHeight = resolveTopTabLabelLineHeightSp(labelMode).sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor
                )
            }
            if (hasSkinStickerIcon && showText) {
                Spacer(modifier = Modifier.height(AppSpacingTokens.Micro))
                Box(
                    modifier = Modifier
                        .width(resolveTopTabSkinStickerIndicatorWidth())
                        .height(AppSpacingTokens.Micro)
                        .clip(AppShapes.container(ContainerLevel.Pill))
                        .background(selectedColor)
                        .alpha(selectionFraction)
                )
            }
        }

    }
}

@Composable
private fun TopTabBlendedIcon(
    unselectedIcon: ImageVector,
    selectedIcon: ImageVector,
    selectedAlpha: Float,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val progress = selectedAlpha.coerceIn(0f, 1f)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (unselectedIcon == selectedIcon) {
            AppIcon(
                imageVector = unselectedIcon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.matchParentSize()
            )
            return
        }
        AppIcon(
            imageVector = unselectedIcon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .matchParentSize()
                .alpha(1f - progress)
        )
        AppIcon(
            imageVector = selectedIcon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .matchParentSize()
                .alpha(progress)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTabRow(
    categories: List<String> = resolveHomeTopCategories().map { it.label },
    categoryKeys: List<String> = resolveHomeTopCategories().map { it.name },
    selectedIndex: Int = 0,
    onCategorySelected: (Int) -> Unit = {},
    onPartitionClick: () -> Unit = {},
    pagerState: androidx.compose.foundation.pager.PagerState? = null, // [New] PagerState for sync
    labelMode: Int = 2,
    isLiquidGlassEnabled: Boolean = false,
    liquidGlassStyle: LiquidGlassStyle = LiquidGlassStyle.CLASSIC,
    liquidGlassTuning: LiquidGlassTuning? = null,
    liquidGlassPreset: BottomBarLiquidGlassPreset = BottomBarLiquidGlassPreset.BILIPAI_TUNED,
    hazeState: HazeState? = null,
    backdrop: LayerBackdrop? = null,
    miuixBackdrop: MiuixBackdrop? = null,
    isFloatingStyle: Boolean = false,
    edgeToEdge: Boolean = false,
    hasOuterChromeSurface: Boolean = false,
    /** Shared with [HomeTopTabChrome.wrapDockWidth] so glass shell and tabs stay the same length. */
    wrapDockWidth: Boolean? = null,
    /** Cap on the dock width (top controls' combined width) so tabs stay left-right aligned. */
    maxDockWidthDp: Float = Float.POSITIVE_INFINITY,
    interactionBudget: HomeInteractionMotionBudget = HomeInteractionMotionBudget.FULL,
    motionTier: MotionTier = MotionTier.Normal,
    isTransitionRunning: Boolean = false,
    forceLowBlurBudget: Boolean = false,
    isViewportSyncEnabled: Boolean = true,
    skinPlainStyle: Boolean = false,
    skinPlainContentColor: Color? = null,
    topTabSkinIconPaths: Map<String, TopTabSkinIconPaths> = emptyMap(),
    partitionSkinIconPath: String? = null,
    forceMaterialUnderline: Boolean = false
) {
    val chromePolicy = rememberAppTopChromePolicy()
    val presetStyle = resolveHomeTopPresetStyle(
        chromePolicy = chromePolicy,
        labelMode = labelMode
    )
    val showPartitionAction = false
    val hasSkinStickerIcons = topTabSkinIconPaths.isNotEmpty() || !partitionSkinIconPath.isNullOrBlank()
    LightweightHomeTopTabs(
        presentation = presetStyle.presentation,
        categories = categories,
        categoryKeys = categoryKeys,
        selectedIndex = selectedIndex,
        onCategorySelected = onCategorySelected,
        onPartitionClick = onPartitionClick,
        pagerState = pagerState,
        labelMode = labelMode,
        isFloatingStyle = isFloatingStyle,
        edgeToEdge = edgeToEdge,
        skinPlainStyle = skinPlainStyle,
        skinPlainContentColor = skinPlainContentColor,
        isLiquidGlassEnabled = isLiquidGlassEnabled,
        liquidGlassStyle = liquidGlassStyle,
        liquidGlassTuning = liquidGlassTuning,
        liquidGlassPreset = liquidGlassPreset,
        backdrop = backdrop,
        miuixBackdrop = miuixBackdrop,
        topTabSkinIconPaths = topTabSkinIconPaths,
        partitionSkinIconPath = partitionSkinIconPath,
        hasOuterChromeSurface = hasOuterChromeSurface,
        wrapDockWidth = wrapDockWidth,
        maxDockWidthDp = maxDockWidthDp,
        isTransitionRunning = isTransitionRunning,
        showPartitionAction = showPartitionAction,
        isViewportSyncEnabled = isViewportSyncEnabled,
        forceMaterialUnderline = forceMaterialUnderline
    )
}

@Composable
private fun rememberTopTabPagerDragHeld(
    pagerState: androidx.compose.foundation.pager.PagerState?
): Boolean {
    if (pagerState == null) return false
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    return isDragged
}

internal fun resolveTopTabIndicatorHitLeftPx(
    indicatorPosition: Float,
    itemWidthPx: Float,
    rowScrollOffsetPx: Float,
    contentPaddingPx: Float,
    indicatorWidthPx: Float
): Float {
    if (itemWidthPx <= 0f || indicatorWidthPx <= 0f) return contentPaddingPx
    val centeredIndicatorInsetPx = (itemWidthPx - indicatorWidthPx) / 2f
    return contentPaddingPx +
        indicatorPosition.coerceAtLeast(0f) * itemWidthPx -
        rowScrollOffsetPx +
        centeredIndicatorInsetPx
}

internal fun shouldStartTopTabIndicatorLongPressDrag(
    pointerX: Float,
    indicatorPosition: Float,
    itemWidthPx: Float,
    rowScrollOffsetPx: Float,
    contentPaddingPx: Float,
    indicatorWidthPx: Float
): Boolean {
    if (itemWidthPx <= 0f || indicatorWidthPx <= 0f) return false
    val indicatorLeftPx = resolveTopTabIndicatorHitLeftPx(
        indicatorPosition = indicatorPosition,
        itemWidthPx = itemWidthPx,
        rowScrollOffsetPx = rowScrollOffsetPx,
        contentPaddingPx = contentPaddingPx,
        indicatorWidthPx = indicatorWidthPx
    )
    return pointerX in indicatorLeftPx..(indicatorLeftPx + indicatorWidthPx)
}

private fun Modifier.topTabSelectedItemDrag(
    dragState: DampedDragAnimationState,
    itemWidthPx: Float,
    itemCount: Int,
    onDragEngaged: () -> Unit
): Modifier = pointerInput(
    dragState,
    itemWidthPx,
    itemCount
) {
    val velocityTracker = VelocityTracker()
    awaitPointerEventScope {
        while (true) {
            val down = awaitFirstDown(requireUnconsumed = false)
            velocityTracker.resetTracking()
            velocityTracker.addPosition(down.uptimeMillis, down.position)
            var horizontalDragEngaged = false
            val dragStart = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                val totalDelta = change.position - down.position
                if (!shouldEngageHorizontalDrag(totalDelta.x, totalDelta.y)) {
                    return@awaitHorizontalTouchSlopOrCancellation
                }
                horizontalDragEngaged = true
                change.consume()
                onDragEngaged()
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                dragState.onDrag(over, itemWidthPx)
            }
            if (dragStart == null || !horizontalDragEngaged) continue

            var isCancelled = false
            try {
                horizontalDrag(dragStart.id) { change ->
                    change.consume()
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    val dragAmount = change.position.x - change.previousPosition.x
                    val velocityX = velocityTracker.calculateVelocity().x
                    dragState.onDrag(dragAmount, itemWidthPx, velocityX)
                }
            } catch (e: Exception) {
                isCancelled = true
            }

            val velocityX = if (isCancelled) 0f else velocityTracker.calculateVelocity().x
            dragState.onDragEnd(
                velocityX = velocityX,
                itemWidthPx = itemWidthPx,
                notifyIndexChanged = true
            )
        }
    }
}

internal fun resolveTopTabIndicatorVelocity(
    horizontalVelocityPxPerSecond: Float
): Float {
    // 顶部指示器仅响应横向分页滑动，避免页面纵向滚动触发胶囊形变。
    return horizontalVelocityPxPerSecond.coerceIn(-4200f, 4200f)
}

internal fun resolveTopTabPagerVelocityItemsPerSecond(
    currentPosition: Float,
    previousPosition: Float,
    elapsedNanos: Long
): Float {
    if (elapsedNanos <= 0L) return 0f
    val elapsedSeconds = elapsedNanos / 1_000_000_000f
    if (elapsedSeconds <= 0f) return 0f
    return ((currentPosition - previousPosition) / elapsedSeconds).coerceIn(-12f, 12f)
}

internal fun resolveTopTabIndicatorLayerVelocityItemsPerSecond(
    motionVelocityItemsPerSecond: Float
): Float = motionVelocityItemsPerSecond

internal fun shouldTopTabIndicatorBeInteracting(
    pagerIsDragging: Boolean = false,
    pagerIsScrolling: Boolean,
    combinedVelocityPxPerSecond: Float,
    liquidGlassEnabled: Boolean
): Boolean {
    if (pagerIsDragging) return true
    if (pagerIsScrolling) return true
    val combinedThreshold = if (liquidGlassEnabled) 20f else 60f
    return abs(combinedVelocityPxPerSecond) > combinedThreshold
}

internal fun resolveTopTabIndicatorInteractionReleaseDelayMillis(
    liquidGlassEnabled: Boolean
): Long {
    return if (liquidGlassEnabled) 140L else 0L
}

internal fun shouldTopTabIndicatorUseRefraction(
    position: Float,
    interacting: Boolean,
    velocityPxPerSecond: Float,
    positionEpsilon: Float = 0.015f,
    velocityEpsilon: Float = 45f
): Boolean {
    val fractional = abs(position - position.roundToInt().toFloat()) > positionEpsilon
    if (fractional) return true
    return abs(velocityPxPerSecond) > velocityEpsilon
}

internal fun shouldDeformTopTabIndicator(
    position: Float,
    isInMotion: Boolean,
    positionEpsilon: Float = 0.015f
): Boolean {
    if (!isInMotion) return false
    return abs(position - position.roundToInt().toFloat()) > positionEpsilon
}

internal fun resolveTopTabIndicatorVisualPolicy(
    position: Float,
    interacting: Boolean,
    velocityPxPerSecond: Float,
    useNeutralIndicatorTint: Boolean
): BottomBarIndicatorVisualPolicy {
    val shouldRefract = shouldTopTabIndicatorUseRefraction(
        position = position,
        interacting = interacting,
        velocityPxPerSecond = velocityPxPerSecond
    )
    return BottomBarIndicatorVisualPolicy(
        isInMotion = shouldRefract,
        shouldRefract = shouldRefract,
        useNeutralTint = shouldRefract && useNeutralIndicatorTint
    )
}

internal fun resolveTopTabStaticIndicatorVisualPolicy(
    useNeutralIndicatorTint: Boolean
): BottomBarIndicatorVisualPolicy {
    return BottomBarIndicatorVisualPolicy(
        isInMotion = false,
        shouldRefract = false,
        useNeutralTint = useNeutralIndicatorTint
    )
}

internal fun resolveTopTabIndicatorLayerTransform(
    motionProgress: Float,
    velocityItemsPerSecond: Float,
    motionSpec: com.android.purebilibili.core.ui.motion.BottomBarMotionSpec =
        resolveSegmentedControlMotionSpec()
): BottomBarIndicatorLayerTransform {
    val bottomBarTransform = resolveBottomBarIndicatorLayerTransform(
        motionProgress = motionProgress,
        velocityItemsPerSecond = velocityItemsPerSecond,
        isDragging = true,
        dragScaleProgress = motionProgress,
        motionSpec = motionSpec
    )
    return bottomBarTransform
}

internal fun resolveTopTabIndicatorScaleProgress(
    dragScaleProgress: Float,
    pressProgress: Float
): Float {
    return maxOf(dragScaleProgress, pressProgress).coerceIn(0f, 1f)
}

internal fun resolveTopTabMatchedPanelOffsetPx(
    dragPanelOffsetPx: Float,
    pagerPanelOffsetFraction: Float,
    maxOffsetPx: Float,
    dragActive: Boolean
): Float {
    if (dragActive) return dragPanelOffsetPx
    return pagerPanelOffsetFraction.coerceIn(-1f, 1f) * maxOffsetPx.coerceAtLeast(0f)
}

internal fun resolveTopTabNeutralIndicatorColor(
    isDarkTheme: Boolean,
    alpha: Float
): Color {
    val baseColor = if (isDarkTheme) {
        HomeVisualPalette.SearchFieldDark
    } else {
        HomeVisualPalette.SearchFieldLight
    }
    return baseColor.copy(alpha = alpha)
}

internal fun resolveTopTabNeutralIndicatorTintAlpha(
    isDarkTheme: Boolean,
    configuredAlpha: Float
): Float {
    val floor = if (isDarkTheme) 0.38f else 0.42f
    return configuredAlpha.coerceAtLeast(floor)
}

internal data class TopTabIndicatorBackdropPolicy(
    val useIndicatorBackdrop: Boolean,
    val useCombinedBackdrop: Boolean
)

internal fun resolveTopTabIndicatorBackdropPolicy(
    effectiveLiquidGlassEnabled: Boolean,
    hasBackdrop: Boolean,
    indicatorVisualPolicy: BottomBarIndicatorVisualPolicy
): TopTabIndicatorBackdropPolicy {
    if (!effectiveLiquidGlassEnabled) {
        return TopTabIndicatorBackdropPolicy(
            useIndicatorBackdrop = indicatorVisualPolicy.shouldRefract && hasBackdrop,
            useCombinedBackdrop = false
        )
    }

    return TopTabIndicatorBackdropPolicy(
        useIndicatorBackdrop = true,
        // Same as the bottom bar: raw page + an export layer that already contains the
        // frosted dock material and selected-content tint.
        useCombinedBackdrop = hasBackdrop
    )
}

internal data class TopTabRefractionMotionProfile(
    val lensAmountScale: Float,
    val lensHeightScale: Float,
    val chromaticBoostScale: Float,
    val forceChromaticAberration: Boolean,
    val visibleSelectionEmphasis: Float,
    val exportSelectionEmphasis: Float,
    val indicatorPanelOffsetFraction: Float,
    val visiblePanelOffsetFraction: Float,
    val exportPanelOffsetFraction: Float
)

internal fun resolveTopTabRefractionMotionProfile(
    position: Float,
    shouldRefract: Boolean,
    velocityPxPerSecond: Float,
    liquidGlassEnabled: Boolean
): TopTabRefractionMotionProfile {
    if (!shouldRefract || !liquidGlassEnabled) {
        return TopTabRefractionMotionProfile(
            lensAmountScale = 1f,
            lensHeightScale = 1f,
            chromaticBoostScale = 1f,
            forceChromaticAberration = false,
            visibleSelectionEmphasis = 1f,
            exportSelectionEmphasis = 1f,
            indicatorPanelOffsetFraction = 0f,
            visiblePanelOffsetFraction = 0f,
            exportPanelOffsetFraction = 0f
        )
    }
    val bottomMotionSpec = resolveSegmentedControlMotionSpec()
    val bottomProfile = resolveBottomBarRefractionMotionProfile(
        position = position,
        velocity = velocityPxPerSecond,
        isDragging = true,
        motionSpec = bottomMotionSpec
    )
    return TopTabRefractionMotionProfile(
        lensAmountScale = 1f,
        lensHeightScale = 1f,
        chromaticBoostScale = 1f,
        forceChromaticAberration = bottomProfile.progress > 0.02f,
        visibleSelectionEmphasis = bottomProfile.visibleSelectionEmphasis,
        exportSelectionEmphasis = bottomProfile.exportSelectionEmphasis,
        indicatorPanelOffsetFraction = bottomProfile.indicatorPanelOffsetFraction,
        visiblePanelOffsetFraction = bottomProfile.visiblePanelOffsetFraction,
        exportPanelOffsetFraction = bottomProfile.exportPanelOffsetFraction
    )
}

internal fun resolveTopTabRefractionMotionProfile(
    shouldRefract: Boolean,
    velocityPxPerSecond: Float,
    liquidGlassEnabled: Boolean
): TopTabRefractionMotionProfile {
    return resolveTopTabRefractionMotionProfile(
        position = 0f,
        shouldRefract = shouldRefract,
        velocityPxPerSecond = velocityPxPerSecond,
        liquidGlassEnabled = liquidGlassEnabled
    )
}

internal fun resolveTopTabItemMotionVisual(
    itemIndex: Int,
    indicatorPosition: Float,
    currentSelectedIndex: Int,
    isInMotion: Boolean,
    selectionEmphasis: Float
): BottomBarItemMotionVisual {
    return resolveBottomBarItemMotionVisual(
        itemIndex = itemIndex,
        indicatorPosition = indicatorPosition,
        currentSelectedIndex = currentSelectedIndex,
        motionProgress = if (isInMotion) 1f else 0f,
        selectionEmphasis = selectionEmphasis
    )
}

internal fun resolveTopTabHorizontalDeltaPx(
    positionDeltaPages: Float,
    tabWidthPx: Float,
    deadZonePages: Float = 0.0012f
): Float {
    if (tabWidthPx <= 0f) return 0f
    if (abs(positionDeltaPages) < deadZonePages) return 0f
    return positionDeltaPages * tabWidthPx
}

internal fun resolveTopTabIndicatorViewportShiftPx(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffsetPx: Int,
    tabWidthPx: Float
): Float {
    if (tabWidthPx <= 0f) return 0f
    if (firstVisibleItemIndex < 0) return 0f
    val clampedScrollOffsetPx = firstVisibleItemScrollOffsetPx.coerceAtLeast(0)
    return firstVisibleItemIndex * tabWidthPx + clampedScrollOffsetPx.toFloat()
}

internal fun resolveTopTabIndicatorViewportClampShiftPx(
    rowScrollOffsetPx: Float,
    indicatorPanelOffsetPx: Float
): Float {
    // 手动横向滚动顶栏只改变标签列表视口，不应把选中指示器夹到当前视口里。
    return 0f
}

@Composable
fun CategoryTabItem(
    category: String,
    categoryKey: String = category,
    index: Int,
    selectedIndex: Int,
    currentPosition: Float,
    primaryColor: Color,
    unselectedColor: Color,
    labelMode: Int,
    isInMotion: Boolean = false,
    selectionEmphasis: Float = 1f,
    isInteractive: Boolean = true,
    onClick: () -> Unit,
    onDoubleTap: () -> Unit = {}
) {
     val chromePolicy = rememberAppTopChromePolicy()
     val motionVisual = remember(
         index,
         currentPosition,
         selectedIndex,
         isInMotion,
         selectionEmphasis
     ) {
         resolveTopTabItemMotionVisual(
             itemIndex = index,
             indicatorPosition = currentPosition,
             currentSelectedIndex = selectedIndex,
             isInMotion = isInMotion,
             selectionEmphasis = selectionEmphasis
         )
     }
     val selectionFraction = motionVisual.themeWeight

     // 单层文本渲染，避免双层交叉透明带来的发虚/重影。
     val contentColor = androidx.compose.ui.graphics.lerp(
         unselectedColor,
         primaryColor,
         selectionFraction
     )
     val normalizedLabelMode = normalizeTopTabLabelMode(labelMode)
     val showIcon = shouldShowTopTabIcon(normalizedLabelMode)
     val showText = shouldShowTopTabText(normalizedLabelMode)
     val unselectedIcon = resolveTopTabCategoryIcon(
         categoryKey = categoryKey,
         iconFamily = chromePolicy.effectiveIconFamily,
         selected = false
     )
     val selectedIcon = resolveTopTabCategoryIcon(
         categoryKey = categoryKey,
         iconFamily = chromePolicy.effectiveIconFamily,
         selected = true
     )
     val iconSize = resolveTopTabIconSizeDp(normalizedLabelMode).dp
     val textSize = resolveTopTabLabelTextSizeSp(normalizedLabelMode).sp
     val textLineHeight = resolveTopTabLabelLineHeightSp(normalizedLabelMode).sp
     val contentMinHeight = resolveTopTabContentMinHeightDp(normalizedLabelMode).dp
     val contentVerticalPadding = resolveTopTabContentVerticalPaddingDp(normalizedLabelMode).dp
     val iconTextSpacing = resolveTopTabIconTextSpacingDp(normalizedLabelMode).dp
     
     val targetScale = resolveTopTabContentScale(
         selectionFraction = selectionFraction,
         showIcon = showIcon,
         showText = showText,
         presentation = chromePolicy.tabPresentation
     )
     
     // Font weight change still triggers relayout, but it's discrete (only happens at 0.6 threshold)
     // This is acceptable as it doesn't happen every frame.
     val fontWeight = if (selectionFraction > 0.6f) FontWeight.SemiBold else FontWeight.Medium

     val haptic = com.android.purebilibili.core.util.rememberHapticFeedback()

     Box(
         modifier = Modifier
             .clip(AppShapes.container(ContainerLevel.Pill))
             .then(
                 if (isInteractive) {
                     Modifier.combinedClickable(
                         interactionSource = remember { MutableInteractionSource() },
                         indication = null,
                         onClick = { onClick() },
                         onDoubleClick = onDoubleTap
                     )
                 } else {
                     Modifier
                 }
             )
             .padding(horizontal = AppSpacingTokens.Small, vertical = contentVerticalPadding)
             .heightIn(min = contentMinHeight),
         contentAlignment = Alignment.Center
     ) {
         if (showIcon && showText) {
             Row(
                 horizontalArrangement = Arrangement.Center,
                 verticalAlignment = Alignment.CenterVertically,
                 modifier = Modifier.graphicsLayer {
                     scaleX = targetScale
                     scaleY = targetScale
                     transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                 }
             ) {
                TopTabBlendedIcon(
                     unselectedIcon = unselectedIcon,
                     selectedIcon = selectedIcon,
                     selectedAlpha = selectionFraction,
                     tint = contentColor,
                     modifier = Modifier.size(iconSize)
                 )
                 Spacer(modifier = Modifier.width(iconTextSpacing))
                 AppText(
                     text = category,
                     color = contentColor,
                     fontSize = textSize,
                     fontWeight = fontWeight,
                     lineHeight = textLineHeight,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                 )
             }
         } else if (showIcon) {
            TopTabBlendedIcon(
                unselectedIcon = unselectedIcon,
                selectedIcon = selectedIcon,
                 selectedAlpha = selectionFraction,
                 tint = contentColor,
                 modifier = Modifier
                     .size(iconSize)
                     .graphicsLayer {
                         scaleX = targetScale
                         scaleY = targetScale
                         transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                     }
             )
         } else {
             AppText(
                 text = category,
                 color = contentColor,
                 fontSize = textSize,
                 fontWeight = fontWeight,
                 lineHeight = textLineHeight,
                 modifier = Modifier.graphicsLayer {
                     scaleX = targetScale
                     scaleY = targetScale
                     transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                 },
                 maxLines = 1,
                 overflow = TextOverflow.Ellipsis
             )
         }
     }
}
