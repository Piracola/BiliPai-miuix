// 文件路径: feature/home/components/HomeHeader.kt
package com.android.purebilibili.feature.home.components
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppHorizontalDivider

import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppTopChromePolicy
import com.android.purebilibili.core.ui.AppTopTabPresentation
import com.android.purebilibili.core.ui.rememberAppSemanticVisualPolicy
import com.android.purebilibili.core.ui.rememberAppTopChromePolicy
import com.android.purebilibili.core.ui.rememberContentCardSurfaceSpec

import com.android.purebilibili.core.ui.OpticalContrastPalette
import com.android.purebilibili.feature.home.HomeVisualPalette

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance  //  状态栏亮度计算
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.iOSTapEffect
import com.android.purebilibili.core.util.rememberHapticFeedback
import com.android.purebilibili.feature.home.UserState
import com.android.purebilibili.core.theme.iOSSystemGray
import dev.chrisbanes.haze.HazeState
import com.android.purebilibili.core.ui.blur.shouldAllowDirectHazeLiquidGlassFallback
import com.android.purebilibili.core.ui.blur.shouldAllowHomeChromeLiquidGlass
import com.android.purebilibili.core.ui.blur.resolveUnifiedBlurredEdgeTreatment
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.ui.blur.BlurStyles
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import com.android.purebilibili.core.store.HomeHeaderBlurMode
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.HomeTopLayoutOrder
import com.android.purebilibili.core.store.HomeTopRightAction
import com.android.purebilibili.feature.home.resolveHomeTopCategories
import com.android.purebilibili.feature.home.resolveHomeTopCollapsedHandleHeight
import com.android.purebilibili.feature.home.resolveHomeTopTabPresentationHeight
import com.android.purebilibili.feature.home.HomeGlassResolvedColors
import com.android.purebilibili.feature.home.rememberHomeGlassChromeColors
import com.android.purebilibili.feature.home.rememberHomeGlassPillColors
import com.android.purebilibili.feature.home.resolveHomeGlassChromeStyle
import com.android.purebilibili.feature.home.resolveHomeGlassPillStyle
import com.android.purebilibili.core.store.resolveHomeHeaderBlurEnabled
import com.android.purebilibili.navigation.resolveAppNavigationAppearance
import java.io.File
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings

internal data class HomeTopChromeMotionPolicy(
    val isScrolling: Boolean,
    val isTransitionRunning: Boolean
)

internal data class HomeTopLinkedBottomBarAppearance(
    val isFloating: Boolean,
    val blurEnabled: Boolean,
    val liquidGlassEnabled: Boolean
)

internal fun resolveHomeSkinSearchSurfaceColor(
    defaultSurfaceColor: Color,
    skinTint: Color?,
    useUnifiedTopPanel: Boolean
): Color {
    if (skinTint == null || useUnifiedTopPanel) return defaultSurfaceColor
    val targetAlpha = defaultSurfaceColor.alpha.coerceAtLeast(0.72f)
    return androidx.compose.ui.graphics.lerp(
        start = defaultSurfaceColor.copy(alpha = targetAlpha),
        stop = skinTint.copy(alpha = targetAlpha),
        fraction = 0.36f
    )
}

internal fun resolveHomeSkinTopTabContentColor(
    topAtmosphereTint: Color,
    hasTopAtmosphereImage: Boolean = false,
    darkTheme: Boolean = false
): Color {
    if (hasTopAtmosphereImage && darkTheme) {
        return OpticalContrastPalette.Highlight.copy(alpha = 0.98f)
    }
    return if (topAtmosphereTint.luminance() < 0.72f) {
        OpticalContrastPalette.Highlight.copy(alpha = 0.98f)
    } else {
        HomeVisualPalette.IosDarkChrome.copy(alpha = 0.96f)
    }
}

internal fun resolveHomeSkinTopTabUnselectedContentColor(contentColor: Color): Color =
    contentColor.copy(alpha = if (contentColor.luminance() > 0.5f) 0.84f else 0.78f)

internal fun shouldUseHomeSkinPlainTopTabs(uiSkinDecoration: HomeUiSkinDecoration?): Boolean =
    false

internal fun resolveHomeSkinTopTabIndicatorColor(contentColor: Color): Color =
    contentColor.copy(alpha = maxOf(contentColor.alpha, 0.92f))

internal fun resolveHomeSkinTopTabRowHeight(): Dp = AppSpacingTokens.TripleExtraLarge - AppSpacingTokens.Micro

internal enum class HomeTopChromeRenderMode {
    PLAIN,
    BLUR
}

internal enum class HomeTopChromeSurfaceTreatment {
    STRUCTURED_GLASS,
    FLAT_GLASS
}

internal fun resolveHomeTopLinkedBottomBarAppearance(
    homeSettings: HomeSettings?,
    presentation: AppTopTabPresentation,
): HomeTopLinkedBottomBarAppearance {
    val resolvedHomeSettings = homeSettings ?: HomeSettings()
    val navigationAppearance = resolveAppNavigationAppearance(
        homeSettings = resolvedHomeSettings,
    )
    return HomeTopLinkedBottomBarAppearance(
        isFloating = navigationAppearance.bottomBarFloating,
        blurEnabled = navigationAppearance.bottomBarBlurEnabled,
        liquidGlassEnabled = false
    )
}

internal fun formatHomeTopRightUnreadBadge(
    action: HomeTopRightAction,
    unreadCount: Int
): String? {
    if (action != HomeTopRightAction.INBOX || unreadCount <= 0) return null
    return if (unreadCount > 99) "99+" else unreadCount.toString()
}

internal data class HomeTopRightUnreadBadgeLayout(
    val offsetX: Dp,
    val offsetY: Dp,
    val reservedEndWidth: Dp,
    val minWidth: Dp,
    val minHeight: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp
)

internal fun resolveHomeTopRightUnreadBadgeLayout(): HomeTopRightUnreadBadgeLayout {
    return HomeTopRightUnreadBadgeLayout(
        offsetX = AppSpacingTokens.None,
        offsetY = AppSpacingTokens.None,
        reservedEndWidth = AppSpacingTokens.Small + AppSpacingTokens.Micro / 2,
        minWidth = AppSpacingTokens.Large + AppSpacingTokens.Micro,
        minHeight = AppSpacingTokens.Large + AppSpacingTokens.Micro,
        horizontalPadding = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro / 2,
        verticalPadding = AppSpacingTokens.Micro / 2
    )
}

internal fun resolveHomeTopRightActionSlotWidth(
    buttonSize: Dp,
    badgeLayout: HomeTopRightUnreadBadgeLayout,
    hasUnreadBadge: Boolean
): Dp = if (hasUnreadBadge) buttonSize + badgeLayout.reservedEndWidth else buttonSize

internal fun resolveHomeTopRightActionContentDescription(
    action: HomeTopRightAction,
    unreadCount: Int
): String {
    val badgeText = formatHomeTopRightUnreadBadge(action, unreadCount) ?: return action.label
    return "${action.label}，$badgeText 条未读"
}

internal fun resolveHomeTopChromeMaterialMode(
    isHeaderBlurEnabled: Boolean,
    isBottomBarBlurEnabled: Boolean,
): TopTabMaterialMode {
    return when {
        !isHeaderBlurEnabled && !isBottomBarBlurEnabled -> TopTabMaterialMode.PLAIN
        else -> TopTabMaterialMode.BLUR
    }
}

internal fun resolveHomeTopChromeRenderMode(
    materialMode: TopTabMaterialMode,
    hasHazeState: Boolean
): HomeTopChromeRenderMode {
    return when (materialMode) {
        TopTabMaterialMode.PLAIN -> HomeTopChromeRenderMode.PLAIN
        TopTabMaterialMode.BLUR -> if (hasHazeState) {
            HomeTopChromeRenderMode.BLUR
        } else {
            HomeTopChromeRenderMode.PLAIN
        }
    }
}

internal fun shouldDrawHomeTopSearchLegacyHighlight(
    presentation: AppTopTabPresentation,
    useUnifiedTopPanel: Boolean,
    renderMode: HomeTopChromeRenderMode,
    refractionOverlayAlpha: Float
): Boolean {
    if (presentation != AppTopTabPresentation.MOVING_CAPSULE || useUnifiedTopPanel) return false
    if (refractionOverlayAlpha > 0f) return false
    return renderMode != HomeTopChromeRenderMode.PLAIN
}

internal fun resolveHomeTopChromeSurfaceTreatment(
    renderMode: HomeTopChromeRenderMode,
    preferFlatGlass: Boolean
): HomeTopChromeSurfaceTreatment {
    if (!preferFlatGlass) return HomeTopChromeSurfaceTreatment.STRUCTURED_GLASS
    return HomeTopChromeSurfaceTreatment.STRUCTURED_GLASS
}

internal fun resolveHomeHeaderSurfaceAlpha(
    isGlassEnabled: Boolean,
    blurEnabled: Boolean,
    blurIntensity: BlurIntensity
): Float {
    @Suppress("UNUSED_PARAMETER")
    val unusedGlass = isGlassEnabled
    if (!blurEnabled) return 1f
    return BlurStyles.getBackgroundAlpha(blurIntensity)
}

internal fun resolveHomeTopBlurContainerAlpha(
    blurIntensity: BlurIntensity
): Float = BlurStyles.getBackgroundAlpha(blurIntensity)

internal fun resolveHomeTopTabOverlayAlpha(
    materialMode: TopTabMaterialMode,
    isTabFloating: Boolean,
    containerAlpha: Float
): Float {
    return when (materialMode) {
        TopTabMaterialMode.PLAIN -> if (isTabFloating) containerAlpha else 1f
        TopTabMaterialMode.BLUR -> containerAlpha
    }
}

internal fun resolveHomeTopTabVerticalPaddingDp(isTabFloating: Boolean): Float {
    // Keep a hairline inset so floating glass doesn't weld to the reserved track edge.
    return if (isTabFloating) 1f else 0f
}

internal fun resolveNonNegativeHomeTopPadding(padding: Dp): Dp = padding.coerceAtLeast(AppSpacingTokens.None)

internal fun resolveHomeTopTabYOffsetDp(isTabFloating: Boolean): Float {
    // Mild lift toward search; list padding subtracts the same amount so tabs↔cards stays even.
    return if (isTabFloating) (-2f) else 0f
}

internal fun resolveHomeTopSearchBarHeight(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchBarHeight
}

internal data class HomeHeaderScrollLayout(
    val searchBarHeightPx: Float,
    val searchAlpha: Float,
    val tabRowHeightPx: Float,
    val tabAlpha: Float
)

internal data class HomeTopPinnedChromeLayout(
    val tabTop: Dp,
    val searchTop: Dp,
    val blurHeight: Dp
)

internal fun resolveHomeTopPinnedChromeLayout(
    statusBarHeight: Dp,
    visibleSearchHeight: Dp,
    tabRowHeight: Dp,
    searchToTabsSpacing: Dp,
    renderMode: HomeTopChromeRenderMode
): HomeTopPinnedChromeLayout {
    val visibleSearchBlockHeight = if (visibleSearchHeight > AppSpacingTokens.None) {
        searchToTabsSpacing + visibleSearchHeight
    } else {
        AppSpacingTokens.None
    }
    val visibleChromeHeight = statusBarHeight + tabRowHeight + visibleSearchBlockHeight
    return HomeTopPinnedChromeLayout(
        tabTop = statusBarHeight + visibleSearchBlockHeight,
        searchTop = statusBarHeight,
        blurHeight = if (renderMode == HomeTopChromeRenderMode.PLAIN) AppSpacingTokens.None else visibleChromeHeight
    )
}

internal fun resolveHomeTopSearchRevealDeadZone(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchRevealDeadZone
}

internal fun resolveHomeTopVisibleSearchHeightPx(
    rawSearchHeightPx: Float,
    fullSearchHeightPx: Float,
    revealDeadZonePx: Float
): Float {
    if (fullSearchHeightPx <= 0f) return 0f
    val clampedRawHeight = rawSearchHeightPx.coerceIn(0f, fullSearchHeightPx)
    val clampedDeadZone = revealDeadZonePx.coerceIn(0f, fullSearchHeightPx - 0.5f)
    if (clampedDeadZone <= 0f) return clampedRawHeight
    if (clampedRawHeight <= clampedDeadZone) return 0f
    val normalizedFraction = (clampedRawHeight - clampedDeadZone) / (fullSearchHeightPx - clampedDeadZone)
    return (normalizedFraction * fullSearchHeightPx).coerceIn(0f, fullSearchHeightPx)
}

internal fun usesImmediateHomeTopSearchReveal(
    revealDeadZonePx: Float
): Boolean = revealDeadZonePx <= 0.01f

internal fun resolveHomeTopSearchContentRevealFraction(
    searchRevealFraction: Float,
    usesImmediateReveal: Boolean
): Float {
    val clampedFraction = searchRevealFraction.coerceIn(0f, 1f)
    if (!usesImmediateReveal) return clampedFraction
    return (clampedFraction * (0.72f + 0.28f * clampedFraction)).coerceIn(0f, 1f)
}

internal fun resolveHomeTopSearchContentTranslationYPx(
    searchRevealFraction: Float,
    searchBarHeightPx: Float,
    usesImmediateReveal: Boolean
): Float {
    if (!usesImmediateReveal || searchBarHeightPx <= 0f) return 0f
    val clampedFraction = searchRevealFraction.coerceIn(0f, 1f)
    val maxShiftPx = minOf(searchBarHeightPx * 0.18f, 10f)
    return -maxShiftPx * (1f - clampedFraction)
}

internal fun resolveHomeHeaderScrollLayout(
    headerOffsetPx: Float,
    searchBarHeightPx: Float,
    searchCollapseDistancePx: Float,
    tabRowHeightPx: Float,
    isHeaderCollapseEnabled: Boolean,
    searchRevealDeadZonePx: Float = 0f,
    usesImmediateSearchReveal: Boolean = false
): HomeHeaderScrollLayout {
    if (!isHeaderCollapseEnabled) {
        return HomeHeaderScrollLayout(
            searchBarHeightPx = searchBarHeightPx,
            searchAlpha = 1f,
            tabRowHeightPx = tabRowHeightPx,
            tabAlpha = 1f
        )
    }
    val effectiveCollapseDistancePx = searchCollapseDistancePx.coerceAtLeast(searchBarHeightPx)
    val clampedOffsetPx = headerOffsetPx.coerceIn(-effectiveCollapseDistancePx, 0f)
    val currentSearchHeightPx = resolveHomeTopVisibleSearchHeightPx(
        rawSearchHeightPx = searchBarHeightPx + clampedOffsetPx,
        fullSearchHeightPx = searchBarHeightPx,
        revealDeadZonePx = searchRevealDeadZonePx
    )
    val rawSearchRevealFraction = if (searchBarHeightPx > 0f) {
        (currentSearchHeightPx / searchBarHeightPx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val searchAlpha = resolveHomeTopSearchContentRevealFraction(
        searchRevealFraction = rawSearchRevealFraction,
        usesImmediateReveal = usesImmediateSearchReveal
    )
    return HomeHeaderScrollLayout(
        searchBarHeightPx = currentSearchHeightPx,
        searchAlpha = searchAlpha,
        tabRowHeightPx = tabRowHeightPx,
        tabAlpha = 1f
    )
}

internal fun resolveHomeTopTabRowHeight(
    isTabFloating: Boolean,
    chromePolicy: AppTopChromePolicy,
    labelMode: Int = com.android.purebilibili.core.store.SettingsManager.TopTabLabelMode.TEXT_ONLY
): Dp {
    val style = resolveHomeTopPresetStyle(chromePolicy, labelMode)
    return if (isTabFloating) {
        style.tabRowHeightFloating
    } else {
        style.tabRowHeightDocked
    }
}

internal fun resolveHomeTopSearchRowHorizontalPadding(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchRowHorizontalPadding
}

internal fun resolveHomeTopSearchPillHeight(
    @Suppress("UNUSED_PARAMETER") chromePolicy: AppTopChromePolicy,
): Dp {
    // 两主题统一：与头像、设置按钮共用同一控件高度（36dp），不再跟随主题 primaryHeightDp。
    return resolveHomeTopEdgeControlHeight()
}

internal fun resolveHomeTopSearchContentHorizontalPadding(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchContentHorizontalPadding
}

internal fun resolveHomeTopSearchIconTextGap(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchIconTextGap
}

internal fun resolveHomeTopSearchContainerShape(
    chromePolicy: AppTopChromePolicy,
): Shape {
    if (chromePolicy.tabPresentation == AppTopTabPresentation.MOVING_CAPSULE) {
        return resolveSharedBottomBarCapsuleShape()
    }
    return RoundedCornerShape(chromePolicy.compactChromeSpec.primaryCornerRadiusDp.dp)
}

internal fun resolveHomeTopEdgeButtonShape(
    chromePolicy: AppTopChromePolicy,
): Shape {
    return when (chromePolicy.tabPresentation) {
        AppTopTabPresentation.MOVING_CAPSULE -> CircleShape
        // Preserve the former semantic Dialog radii: 14dp scaled by each native profile.
        AppTopTabPresentation.MATERIAL_UNDERLINE,
        AppTopTabPresentation.TONAL_CAPSULE -> RoundedCornerShape(
            chromePolicy.compactChromeSpec.secondaryButtonCornerRadiusDp.dp
        )
    }
}

/**
 * 顶部行统一控件高度：头像、搜索胶囊、设置按钮共用（36dp），两主题一致。
 * 与下方分栏 tab 行（36/40dp）保持同一视觉尺度。
 */
internal fun resolveHomeTopEdgeControlHeight(): Dp =
    AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall

internal fun resolveHomeTopAvatarOuterSize(): Dp = resolveHomeTopEdgeControlHeight()

internal fun resolveHomeTopAvatarInnerSize(): Dp = resolveHomeTopEdgeControlHeight()

internal fun resolveHomeTopSettingsButtonSize(
    @Suppress("UNUSED_PARAMETER") chromePolicy: AppTopChromePolicy,
): Dp {
    // 与头像、搜索胶囊统一控件高度（36dp），两主题一致。
    return resolveHomeTopEdgeControlHeight()
}

internal fun resolveHomeTopSettingsIconSize(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return if (chromePolicy.tabPresentation == AppTopTabPresentation.TONAL_CAPSULE) {
        resolveHomeTopPresetStyle(chromePolicy, labelMode = 2)
            .actionIconSizeDocked
            .coerceAtMost(18.dp)
    } else {
        AppSpacingTokens.Large + AppSpacingTokens.Micro
    }
}

internal fun resolveHomeTopEdgeControlGap(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).edgeControlGap
}

internal fun shouldUseUnifiedHomeTopPanel(chromePolicy: AppTopChromePolicy): Boolean {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).useUnifiedPanel
}

internal fun shouldUseDetachedHomeTopTabDock(
    presentation: AppTopTabPresentation,
): Boolean {
    return presentation != AppTopTabPresentation.MATERIAL_UNDERLINE
}

internal fun resolveHomeTopUnifiedPanelHorizontalPadding(chromePolicy: AppTopChromePolicy): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).unifiedPanelHorizontalPadding
}

internal fun resolveHomeTopUnifiedPanelInnerPadding(
    chromePolicy: AppTopChromePolicy,
    collapsedIntoStatusBar: Boolean = false
): Dp {
    if (collapsedIntoStatusBar) return AppSpacingTokens.Micro
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).unifiedPanelInnerPadding
}

internal fun shouldRenderHomeTopUnifiedPanelChrome(
    searchHeightDp: Float,
    tabHeightDp: Float,
    integratedCollapsedTopBar: Boolean,
    minVisibleHeightDp: Float = 0.5f
): Boolean {
    return integratedCollapsedTopBar ||
        searchHeightDp > minVisibleHeightDp ||
        tabHeightDp > minVisibleHeightDp
}

internal fun resolveHomeTopUnifiedPanelCornerRadius(
    chromePolicy: AppTopChromePolicy,
    collapsedIntoStatusBar: Boolean = false
): Dp {
    if (collapsedIntoStatusBar) return AppSpacingTokens.None
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).unifiedPanelCornerRadius
}

internal fun resolveHomeTopReservedContentBottomGap(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).reservedContentBottomGap
}

internal fun resolveHomeTopEmbeddedTabHorizontalPadding(chromePolicy: AppTopChromePolicy): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).embeddedTabHorizontalPadding
}

internal fun resolveHomeTopTabHorizontalPadding(
    @Suppress("UNUSED_PARAMETER") isTabFloating: Boolean,
    chromePolicy: AppTopChromePolicy,
): Dp {
    // 分栏轨道与搜索行共用同一水平内边距，保证 tab 与顶部三控件左右对齐。
    return resolveHomeTopSearchRowHorizontalPadding(chromePolicy)
}

/**
 * 顶部三控件（头像 + 搜索胶囊 + 设置按钮）在屏幕上的合计宽度。
 * 搜索胶囊 weight 撑满整行，合计宽度即整行内容宽度；复用于下方分栏 tab 的
 * 最大宽度（左右对齐约束）。
 */
internal fun resolveHomeTopControlsContentWidthDp(
    containerWidthDp: Dp,
    chromePolicy: AppTopChromePolicy,
): Dp {
    val rowPadding = resolveHomeTopSearchRowHorizontalPadding(chromePolicy)
    return (containerWidthDp - rowPadding * 2f).coerceAtLeast(AppSpacingTokens.None)
}

internal fun resolveHomeTopSearchToTabsSpacing(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchToTabsSpacing
}

internal fun resolveHomeTopTabsToContentSpacing(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).tabsToContentSpacing
}

internal fun resolveHomeTopSearchCollapseExtraSpacing(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).searchCollapseExtraSpacing
}

internal fun resolveHomeTopSearchCollapseDistance(
    searchBarHeight: Dp,
    chromePolicy: AppTopChromePolicy,
): Dp {
    return searchBarHeight +
        resolveHomeTopSearchToTabsSpacing(chromePolicy) +
        resolveHomeTopSearchCollapseExtraSpacing(chromePolicy)
}

internal fun shouldUseIntegratedCollapsedHomeTopBar(
    searchRevealFraction: Float,
    presentation: AppTopTabPresentation,
): Boolean {
    return presentation == AppTopTabPresentation.MOVING_CAPSULE && searchRevealFraction <= 0.02f
}

internal fun resolveHomeTopContinuousSlabOverlap(
    chromePolicy: AppTopChromePolicy,
): Dp {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).continuousSlabOverlap
}

internal fun resolveHomeTopContinuousSlabShape(): Shape {
    return RoundedCornerShape(bottomStart = AppSpacingTokens.Medium, bottomEnd = AppSpacingTokens.Medium)
}

internal fun resolveHomeTopReservedListPadding(
    statusBarHeight: Dp,
    searchBarHeight: Dp,
    tabRowHeight: Dp,
    chromePolicy: AppTopChromePolicy,
    isTabFloating: Boolean = false
): Dp {
    val useUnifiedPanel = shouldUseUnifiedHomeTopPanel(chromePolicy)
    val searchToTabs = resolveHomeTopSearchToTabsSpacing(chromePolicy)
    val tabsToContent = resolveHomeTopTabsToContentSpacing(chromePolicy)
    // Floating dock is shifted up via [resolveHomeTopTabYOffsetDp]; fold the same delta into
    // list padding so the visual gap under the dock matches [tabsToContent], not tabsToContent+|offset|.
    val floatingDockLift = resolveHomeTopTabYOffsetDp(isTabFloating).dp
    val chromeHeight = if (useUnifiedPanel) {
        searchBarHeight +
            tabRowHeight +
            (resolveHomeTopUnifiedPanelInnerPadding(chromePolicy) * 2) +
            searchToTabs
    } else {
        searchBarHeight + searchToTabs + tabRowHeight
    }
    return statusBarHeight + chromeHeight + tabsToContent + floatingDockLift
}

internal fun resolveHomeTopBlurContainerColors(
    colors: HomeGlassResolvedColors,
    surfaceColor: Color,
    blurIntensity: BlurIntensity
): HomeGlassResolvedColors {
    return colors.copy(
        containerColor = resolveBottomBarSurfaceColor(
            surfaceColor = surfaceColor,
            blurEnabled = true,
            blurIntensity = blurIntensity
        )
    )
}

internal fun resolveHomeTopBlurSurfaceType(
    renderMode: HomeTopChromeRenderMode
): BlurSurfaceType {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> BlurSurfaceType.HEADER
        else -> BlurSurfaceType.HEADER
    }
}

internal fun resolveHomeTopContinuousSlabRenderMode(
    renderMode: HomeTopChromeRenderMode,
): HomeTopChromeRenderMode {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> HomeTopChromeRenderMode.BLUR
        else -> HomeTopChromeRenderMode.PLAIN
    }
}

internal fun resolveHomeTopContinuousSlabHeight(
    statusBarHeight: Dp,
    searchBarHeight: Dp,
    tabRowHeight: Dp,
    renderMode: HomeTopChromeRenderMode,
    hasVisibleTopContent: Boolean = true
): Dp {
    return resolveHomeTopPinnedChromeLayout(
        statusBarHeight = statusBarHeight,
        visibleSearchHeight = if (hasVisibleTopContent) searchBarHeight else AppSpacingTokens.None,
        tabRowHeight = if (hasVisibleTopContent) tabRowHeight else AppSpacingTokens.None,
        searchToTabsSpacing = AppSpacingTokens.None,
        renderMode = renderMode
    ).blurHeight
}

internal fun resolveHomeTopContinuousSlabSurfaceColor(
    baseColor: Color,
    blurAlpha: Float,
    usesNativeContainerTreatment: Boolean,
    renderMode: HomeTopChromeRenderMode
): Color {
    if (renderMode == HomeTopChromeRenderMode.PLAIN) return Color.Transparent
    if (renderMode != HomeTopChromeRenderMode.BLUR) {
        return baseColor.copy(alpha = maxOf(baseColor.alpha, blurAlpha))
    }
    return if (usesNativeContainerTreatment) {
        baseColor.copy(alpha = maxOf(baseColor.alpha, blurAlpha))
    } else {
        Color.Transparent
    }
}

internal fun resolveHomeTopPanelChromeRenderMode(
    renderMode: HomeTopChromeRenderMode,
    usesNativeContainerTreatment: Boolean,
    useUnifiedPanel: Boolean = false
): HomeTopChromeRenderMode {
    if (useUnifiedPanel) return HomeTopChromeRenderMode.PLAIN
    return resolveHomeTopLocalChromeRenderMode(
        renderMode = renderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
    )
}

internal fun resolveHomeTopSearchChromeRenderMode(
    renderMode: HomeTopChromeRenderMode,
    useUnifiedPanel: Boolean = false,
    usesNativeContainerTreatment: Boolean,
): HomeTopChromeRenderMode {
    if (useUnifiedPanel) {
        return when (renderMode) {
            HomeTopChromeRenderMode.BLUR -> renderMode
            HomeTopChromeRenderMode.PLAIN -> HomeTopChromeRenderMode.PLAIN
        }
    }
    return resolveHomeTopLocalChromeRenderMode(
        renderMode = renderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
    )
}

internal fun resolveHomeTopUnifiedTabChromeRenderMode(
    localTabChromeRenderMode: HomeTopChromeRenderMode,
    usesTonalContainerTreatment: Boolean,
    useUnifiedLiquidChrome: Boolean
): HomeTopChromeRenderMode {
    if (usesTonalContainerTreatment) {
        return localTabChromeRenderMode
    }
    return if (useUnifiedLiquidChrome) {
        localTabChromeRenderMode
    } else if (localTabChromeRenderMode == HomeTopChromeRenderMode.BLUR) {
        HomeTopChromeRenderMode.BLUR
    } else {
        HomeTopChromeRenderMode.PLAIN
    }
}

internal fun resolveHomeTopUnifiedLocalTabChromeRenderMode(
    renderMode: HomeTopChromeRenderMode,
    usesNativeContainerTreatment: Boolean,
    usesTonalContainerTreatment: Boolean,
): HomeTopChromeRenderMode {
    if (usesTonalContainerTreatment) {
        return resolveHomeTopLocalChromeRenderMode(
            renderMode = renderMode,
            usesNativeContainerTreatment = usesNativeContainerTreatment,
        )
    }
    // 统一面板关闭外层 slab 后，标签行需要保留自己的模糊承托区域。
    if (renderMode == HomeTopChromeRenderMode.BLUR) {
        return HomeTopChromeRenderMode.BLUR
    }
    return resolveHomeTopLocalChromeRenderMode(
        renderMode = renderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
    )
}

internal fun resolveHomeTopUnifiedTabSurfaceColor(
    tabContainerColor: Color,
    tabOverlayAlpha: Float,
    usesTonalContainerTreatment: Boolean,
    useUnifiedLiquidChrome: Boolean,
    tabChromeRenderMode: HomeTopChromeRenderMode = HomeTopChromeRenderMode.PLAIN
): Color {
    if (usesTonalContainerTreatment) {
        return tabContainerColor.copy(alpha = tabOverlayAlpha)
    }
    return if (useUnifiedLiquidChrome || tabChromeRenderMode == HomeTopChromeRenderMode.BLUR) {
        tabContainerColor.copy(alpha = tabOverlayAlpha)
    } else {
        Color.Transparent
    }
}

internal fun resolveHomeTopDetachedTabDockSurfaceColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode
): Color {
    val alpha = when (renderMode) {
        HomeTopChromeRenderMode.PLAIN -> if (isLightMode) 0.58f else 0.64f
        HomeTopChromeRenderMode.BLUR -> if (isLightMode) 0.46f else 0.58f
    }
    return if (isLightMode) {
        OpticalContrastPalette.Highlight.copy(alpha = alpha)
    } else {
        OpticalContrastPalette.Shadow.copy(alpha = alpha)
    }
}

internal fun resolveHomeTopUnifiedSearchContainerColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode = HomeTopChromeRenderMode.BLUR
): Color {
    val alpha = when (renderMode) {
        HomeTopChromeRenderMode.PLAIN -> if (isLightMode) 0.62f else 0.42f
        HomeTopChromeRenderMode.BLUR -> if (isLightMode) 0.38f else 0.32f
    }
    return if (isLightMode) {
        OpticalContrastPalette.Highlight.copy(alpha = alpha)
    } else {
        OpticalContrastPalette.Shadow.copy(alpha = alpha)
    }
}

internal fun resolveHomeTopSearchDarkWhiteOverlayMultiplier(
    isLightMode: Boolean
): Float {
    return if (isLightMode) 0.86f else 0.30f
}

internal fun resolveHomeTopUnifiedSearchBorderColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode = HomeTopChromeRenderMode.BLUR
): Color {
    if (renderMode == HomeTopChromeRenderMode.PLAIN) {
        return if (isLightMode) {
            OpticalContrastPalette.Shadow.copy(alpha = 0.14f)
        } else {
            OpticalContrastPalette.Highlight.copy(alpha = 0.22f)
        }
    }
    if (renderMode == HomeTopChromeRenderMode.BLUR) {
        return if (isLightMode) {
            OpticalContrastPalette.Highlight.copy(alpha = 0.22f)
        } else {
            OpticalContrastPalette.Highlight.copy(alpha = 0.18f)
        }
    }
    return if (isLightMode) {
        OpticalContrastPalette.Highlight.copy(alpha = 0.20f)
    } else {
        OpticalContrastPalette.Highlight.copy(alpha = 0.12f)
    }
}

internal fun resolveHomeTopEdgeControlContainerColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode
): Color {
    val alpha = when (renderMode) {
        HomeTopChromeRenderMode.PLAIN -> if (isLightMode) 0.58f else 0.40f
        HomeTopChromeRenderMode.BLUR -> if (isLightMode) 0.38f else 0.32f
    }
    return if (isLightMode) {
        OpticalContrastPalette.Highlight.copy(alpha = alpha)
    } else {
        OpticalContrastPalette.Shadow.copy(alpha = alpha)
    }
}

internal fun resolveHomeTopEdgeControlBorderColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode
): Color {
    return when (renderMode) {
        HomeTopChromeRenderMode.PLAIN -> if (isLightMode) {
            OpticalContrastPalette.Shadow.copy(alpha = 0.12f)
        } else {
            OpticalContrastPalette.Highlight.copy(alpha = 0.20f)
        }
        HomeTopChromeRenderMode.BLUR -> if (isLightMode) {
            OpticalContrastPalette.Highlight.copy(alpha = 0.16f)
        } else {
            OpticalContrastPalette.Highlight.copy(alpha = 0.16f)
        }
    }
}

internal fun resolveHomeTopUnifiedPanelReadabilityColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode
): Color {
    val alpha = when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.16f
        HomeTopChromeRenderMode.PLAIN -> 0f
    }
    return if (isLightMode) {
        OpticalContrastPalette.Highlight.copy(alpha = alpha)
    } else {
        OpticalContrastPalette.Shadow.copy(alpha = alpha)
    }
}

internal fun resolveHomeTopWideChromePreferFlatGlass(
    renderMode: HomeTopChromeRenderMode
): Boolean {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR,
        HomeTopChromeRenderMode.PLAIN -> true
    }
}

internal fun resolveHomeTopLocalChromeRenderMode(
    renderMode: HomeTopChromeRenderMode,
    usesNativeContainerTreatment: Boolean,
): HomeTopChromeRenderMode {
    if (usesNativeContainerTreatment && renderMode == HomeTopChromeRenderMode.BLUR) {
        return HomeTopChromeRenderMode.BLUR
    }
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> HomeTopChromeRenderMode.PLAIN
        else -> renderMode
    }
}

internal fun resolveHomeTopChromeMotionPolicy(
    renderMode: HomeTopChromeRenderMode,
    isScrolling: Boolean,
    isTransitionRunning: Boolean
): HomeTopChromeMotionPolicy {
    return if (renderMode == HomeTopChromeRenderMode.BLUR) {
        HomeTopChromeMotionPolicy(
            isScrolling = false,
            isTransitionRunning = false
        )
    } else {
        HomeTopChromeMotionPolicy(
            isScrolling = isScrolling,
            isTransitionRunning = isTransitionRunning
        )
    }
}

internal fun resolveHomeTopTabChromeMotionPolicy(
    renderMode: HomeTopChromeRenderMode,
    isScrolling: Boolean,
    isTransitionRunning: Boolean
): HomeTopChromeMotionPolicy {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> HomeTopChromeMotionPolicy(
            isScrolling = false,
            isTransitionRunning = false
        )
        HomeTopChromeRenderMode.PLAIN -> resolveHomeTopChromeMotionPolicy(
            renderMode = renderMode,
            isScrolling = isScrolling,
            isTransitionRunning = isTransitionRunning
        )
    }
}

internal fun shouldEnableTopTabSecondaryBlur(
    hasHeaderBlur: Boolean,
    topTabMaterialMode: TopTabMaterialMode,
    isScrolling: Boolean,
    isTransitionRunning: Boolean
): Boolean {
    @Suppress("UNUSED_PARAMETER")
    val unused = listOf(isScrolling, isTransitionRunning)
    if (!hasHeaderBlur) return false
    if (topTabMaterialMode == TopTabMaterialMode.PLAIN) return false
    return true
}

internal fun resolveHomeHeaderTabBorderAlpha(
    isTabFloating: Boolean,
    isTabGlassEnabled: Boolean
): Float {
    @Suppress("UNUSED_PARAMETER")
    val unused = listOf(isTabFloating, isTabGlassEnabled)
    return 0f
}

internal fun resolveHomeTopChromeReadabilityAlpha(
    renderMode: HomeTopChromeRenderMode
): Float {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.30f
        HomeTopChromeRenderMode.PLAIN -> 0.16f
    }
}

internal fun resolveHomeTopSearchContentAlpha(
    renderMode: HomeTopChromeRenderMode
): Float {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.92f
        HomeTopChromeRenderMode.PLAIN -> 0.78f
    }
}

internal fun resolveHomeTopForegroundColor(
    isLightMode: Boolean
): Color {
    return if (isLightMode) {
        OpticalContrastPalette.Shadow
    } else {
        OpticalContrastPalette.Highlight.copy(alpha = 0.92f)
    }
}

internal fun resolveHomeTopInnerUnderlayColor(
    isLightMode: Boolean,
    renderMode: HomeTopChromeRenderMode,
    softenWideChrome: Boolean = false
): Color {
    val alpha = resolveHomeTopTabContentUnderlayAlpha(
        renderMode = renderMode,
        softenWideChrome = softenWideChrome
    )
    return if (isLightMode) {
        OpticalContrastPalette.Highlight.copy(alpha = alpha)
    } else {
        OpticalContrastPalette.Shadow.copy(alpha = (alpha * 0.72f).coerceAtLeast(0.05f))
    }
}

internal fun resolveHomeTopChromeHighlightOverlayColor(
    baseColor: Color,
    renderMode: HomeTopChromeRenderMode,
    softenWideChrome: Boolean
): Color {
    if (!softenWideChrome) return baseColor
    val alphaMultiplier = when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.42f
        else -> 1f
    }
    return baseColor.copy(alpha = baseColor.alpha * alphaMultiplier)
}

internal fun tuneHomeTopGlassColors(
    colors: HomeGlassResolvedColors,
    isLightMode: Boolean,
    emphasized: Boolean
): HomeGlassResolvedColors {
    if (isLightMode) return colors
    return colors.copy(
        containerColor = colors.containerColor.copy(alpha = colors.containerColor.alpha * if (emphasized) 0.74f else 0.68f),
        borderColor = OpticalContrastPalette.Highlight.copy(alpha = colors.borderColor.alpha * 0.48f),
        highlightColor = OpticalContrastPalette.Highlight.copy(alpha = colors.highlightColor.alpha * 0.28f)
    )
}

internal fun resolveHomeTopContainerColors(
    usesNativeContainerTreatment: Boolean,
    usesTonalContainerTreatment: Boolean,
    emphasized: Boolean,
    blurEnabled: Boolean,
    fallbackColors: HomeGlassResolvedColors,
    surfaceContainerColor: Color,
    surfaceContainerHighColor: Color,
    outlineVariantColor: Color
): HomeGlassResolvedColors {
    if (!usesNativeContainerTreatment) return fallbackColors
    if (blurEnabled) {
        val baseColor = if (usesTonalContainerTreatment) {
            surfaceContainerColor
        } else if (emphasized) {
            surfaceContainerHighColor
        } else {
            surfaceContainerColor
        }
        return HomeGlassResolvedColors(
            containerColor = baseColor.copy(alpha = fallbackColors.containerColor.alpha),
            borderColor = outlineVariantColor.copy(
                alpha = fallbackColors.borderColor.alpha.coerceAtLeast(
                    if (usesTonalContainerTreatment) 0.16f else if (emphasized) 0.18f else 0.14f
                )
            ),
            highlightColor = Color.Transparent
        )
    }
    return HomeGlassResolvedColors(
        containerColor = if (usesTonalContainerTreatment) {
            surfaceContainerColor
        } else if (emphasized) {
            surfaceContainerHighColor
        } else {
            surfaceContainerColor
        },
        borderColor = outlineVariantColor.copy(
            alpha = if (usesTonalContainerTreatment) {
                if (emphasized) 0.44f else 0.34f
            } else if (emphasized) {
                0.55f
            } else {
                0.42f
            }
        ),
        highlightColor = Color.Transparent
    )
}

internal fun resolveHomeTopActionIconAlpha(
    renderMode: HomeTopChromeRenderMode
): Float {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.90f
        HomeTopChromeRenderMode.PLAIN -> 0.78f
    }
}

internal fun resolveHomeTopUnifiedPanelDividerAlpha(
    renderMode: HomeTopChromeRenderMode
): Float {
    return when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.18f
        HomeTopChromeRenderMode.PLAIN -> 0.12f
    }
}

internal fun shouldShowUnifiedHomeTopPanelDivider(
    chromePolicy: AppTopChromePolicy,
): Boolean {
    return resolveHomeTopPresetStyle(chromePolicy, labelMode = 2).showUnifiedPanelDivider
}

internal fun resolveHomeTopTabContentUnderlayAlpha(
    renderMode: HomeTopChromeRenderMode,
    softenWideChrome: Boolean = false
): Float {
    val base = when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> 0.14f
        HomeTopChromeRenderMode.PLAIN -> 0.08f
    }
    return if (softenWideChrome && renderMode == HomeTopChromeRenderMode.BLUR) {
        (base * 0.42f).coerceAtLeast(0.04f)
    } else {
        base
    }
}

internal fun resolveHomeTopChromeLensShape(shape: Shape): Shape? {
    return when {
        shape is CornerBasedShape -> shape
        shape === CircleShape -> RoundedCornerShape(percent = 50)
        shape === androidx.compose.ui.graphics.RectangleShape -> RoundedCornerShape(size = AppSpacingTokens.None)
        else -> null
    }
}

private data class HomeTopChromeSurfaceStyle(
    val blurSurfaceType: BlurSurfaceType,
    val preferFlatGlass: Boolean,
    val depthEffect: Boolean,
    val refractionAmountScrollMultiplier: Float,
    val refractionAmountScrollCap: Float,
    val surfaceAlphaScrollMultiplier: Float,
    val surfaceAlphaScrollCap: Float,
    val darkThemeWhiteOverlayMultiplier: Float,
    val useTuningSurfaceAlpha: Boolean,
    val hazeBackgroundAlphaMultiplier: Float
)

internal fun Modifier.homeTopChromeSurface(
    renderMode: HomeTopChromeRenderMode,
    shape: Shape,
    surfaceColor: Color,
    hazeState: HazeState?,
    miuixBackdrop: top.yukonga.miuix.kmp.blur.Backdrop? = null,
    motionTier: MotionTier,
    isScrolling: Boolean,
    isTransitionRunning: Boolean,
    forceLowBlurBudget: Boolean,
    preferFlatGlass: Boolean = false,
    darkThemeWhiteOverlayMultiplier: Float = 0.86f
): Modifier = composed {
    @Suppress("UNUSED_PARAMETER")
    val unused = listOf(miuixBackdrop, preferFlatGlass, darkThemeWhiteOverlayMultiplier)
    when (renderMode) {
        HomeTopChromeRenderMode.BLUR -> {
            this
                .then(
                    if (hazeState != null) {
                        Modifier.unifiedBlur(
                            hazeState = hazeState,
                            shape = shape,
                            surfaceType = resolveHomeTopBlurSurfaceType(renderMode),
                            motionTier = motionTier,
                            isScrolling = isScrolling,
                            isTransitionRunning = isTransitionRunning,
                            forceLowBudget = forceLowBlurBudget
                        )
                    } else {
                        Modifier
                    }
                )
                .background(surfaceColor, shape)
        }

        HomeTopChromeRenderMode.PLAIN -> {
            this.background(surfaceColor, shape)
        }
    }
}

/**
 *  简洁版首页头部 (带滚动隐藏/显示动画)
 * 
 *  [Refactor] 现在改为由外部通过 NestedScrollConnection 直接控制高度和透明度，
 *  实现了 1:1 的物理跟手效果，消除了漂浮感。
 */
@Composable
fun HomeHeader(
    headerOffsetProvider: () -> Float, // [Optimization] Defer state read to prevent parent recomposition
    isHeaderCollapseEnabled: Boolean = true,
    isTopTabsAutoCollapseEnabled: Boolean = false,
    isTopTabsManualCollapseEnabled: Boolean = true,
    user: UserState,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onInboxClick: () -> Unit = {},
    topRightUnreadCount: Int = 0,
    onSearchClick: () -> Unit,
    topCategories: List<String> = resolveHomeTopCategories().map { it.label },
    topCategoryKeys: List<String> = resolveHomeTopCategories().map { it.name },
    categoryIndex: Int,
    onCategorySelected: (Int) -> Unit,
    onPartitionClick: () -> Unit = {},  //  新增：分区按钮回调
    hazeState: HazeState? = null,  // 保留参数兼容性，但不用于模糊
    onStatusBarDoubleTap: () -> Unit = {},
    //  [新增] 下拉刷新状态
    isRefreshing: Boolean = false,
    pullProgress: Float = 0f,  // 0.0 ~ 1.0+ 下拉进度
    pagerState: androidx.compose.foundation.pager.PagerState? = null, // [New] PagerState for sync
    // Miuix is the single liquid-glass renderer for the home chrome.
    miuixBackdrop: top.yukonga.miuix.kmp.blur.Backdrop? = null,
    homeSettings: com.android.purebilibili.core.store.HomeSettings? = null,
    topTabsVisible: Boolean = true,
    topTabsCollapsed: Boolean = false,
    onTopTabsCollapsedChange: (Boolean) -> Unit = {},
    motionTier: MotionTier = MotionTier.Normal,
    isScrolling: Boolean = false,
    isTransitionRunning: Boolean = false,
    forceLowBlurBudget: Boolean = false,
    interactionBudget: HomeInteractionMotionBudget = HomeInteractionMotionBudget.FULL,
    uiSkinDecoration: HomeUiSkinDecoration? = null
) {
    val topChromePolicy = rememberAppTopChromePolicy()
    val semanticVisualPolicy = rememberAppSemanticVisualPolicy()
    val contentCardSurfaceSpec = rememberContentCardSurfaceSpec()
    val usesNativeContainerTreatment = semanticVisualPolicy.prefersNativeChrome
    val usesTonalContainerTreatment = contentCardSurfaceSpec.usesTonalContainerTreatment
    val shouldUseSkinPlainTopTabs = shouldUseHomeSkinPlainTopTabs(uiSkinDecoration)
    val haptic = rememberHapticFeedback()
    val density = LocalDensity.current
    val resolvedHeaderBlurMode = homeSettings?.headerBlurMode ?: HomeHeaderBlurMode.FOLLOW_PRESET
    val isHeaderBlurEnabled = remember(resolvedHeaderBlurMode) {
        resolveHomeHeaderBlurEnabled(
            mode = resolvedHeaderBlurMode,
        )
    }
    val linkedBottomBarAppearance = remember(
        homeSettings,
        topChromePolicy.tabPresentation,
    ) {
        resolveHomeTopLinkedBottomBarAppearance(
            homeSettings = homeSettings,
            presentation = topChromePolicy.tabPresentation,
        )
    }
    val edgeButtonShape = resolveHomeTopEdgeButtonShape(topChromePolicy)
    val searchContainerShape = resolveHomeTopSearchContainerShape(topChromePolicy)
    val searchIcon = MiuixIcons.Search
    val topRightAction = homeSettings?.homeTopRightAction ?: HomeTopRightAction.SETTINGS
    val settingsIcon = MiuixIcons.Settings
    val inboxIcon = MiuixIcons.Messages
    val topRightActionIcon = if (topRightAction == HomeTopRightAction.INBOX) inboxIcon else settingsIcon
    val topRightActionContentDescription = resolveHomeTopRightActionContentDescription(
        action = topRightAction,
        unreadCount = topRightUnreadCount
    )
    val topRightUnreadBadge = formatHomeTopRightUnreadBadge(
        action = topRightAction,
        unreadCount = topRightUnreadCount
    )
    val topRightUnreadBadgeLayout = resolveHomeTopRightUnreadBadgeLayout()
    val onTopRightActionClick = if (topRightAction == HomeTopRightAction.INBOX) {
        onInboxClick
    } else {
        onSettingsClick
    }
    val topChromeLiquidGlassEnabled = false

    // 状态栏高度
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    
    // [Feature] Liquid Glass Logic (retired: Haze blur only)
    val topChromeMaterialMode = resolveHomeTopChromeMaterialMode(
        isHeaderBlurEnabled = isHeaderBlurEnabled,
        isBottomBarBlurEnabled = linkedBottomBarAppearance.blurEnabled,
    )
    val isGlassEnabled = false
    val isTopChromeBlurEnabled = topChromeMaterialMode != TopTabMaterialMode.PLAIN
    val searchChromeMaterialMode = resolveHomeTopChromeMaterialMode(
        isHeaderBlurEnabled = isHeaderBlurEnabled,
        isBottomBarBlurEnabled = linkedBottomBarAppearance.blurEnabled,
    )
    val isSearchGlassEnabled = false
    val isSearchBlurEnabled = searchChromeMaterialMode != TopTabMaterialMode.PLAIN

    //  读取当前模糊强度以确定背景透明度
    val blurIntensity = currentUnifiedBlurIntensity()
    val backgroundAlpha = resolveHomeHeaderSurfaceAlpha(
        isGlassEnabled = isGlassEnabled,
        blurEnabled = isTopChromeBlurEnabled,
        blurIntensity = blurIntensity
    )

    val topTabStyle = resolveTopTabStyle(
        isBottomBarFloating = linkedBottomBarAppearance.isFloating,
        isBottomBarBlurEnabled = isHeaderBlurEnabled,
    )
    val isTabFloating = topTabStyle.floating
    val isTabGlassEnabled = false
    val isTabBlurEnabled = topChromeMaterialMode == TopTabMaterialMode.BLUR
    val enableTopTabSecondaryBlur = shouldEnableTopTabSecondaryBlur(
        hasHeaderBlur = hazeState != null,
        topTabMaterialMode = topChromeMaterialMode,
        isScrolling = isScrolling,
        isTransitionRunning = isTransitionRunning
    )
    val topChromeRenderMode = resolveHomeTopChromeRenderMode(
        materialMode = topChromeMaterialMode,
        hasHazeState = hazeState != null
    )
    val surfaceColor = AppSurfaceTokens.cardContainer()
    val surfaceContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val tabShape = RoundedCornerShape(if (isTabFloating) AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro else AppSpacingTokens.None)
    val tabSurfaceColor = surfaceColor
    val isLightMode = surfaceColor.luminance() > 0.5f
    val effectiveTabMaterialMode = resolveEffectiveHomeHeaderTabMaterialMode(
        materialMode = topChromeMaterialMode,
        interactionBudget = interactionBudget
    )
    // 分栏 presentation 由主题决定（两主题均走移动胶囊），不再随液态玻璃开关切换。
    val drawTopTabOuterChromeSurface = shouldDrawHomeTopTabOuterChromeSurface(
        presentation = topChromePolicy.tabPresentation,
        materialMode = effectiveTabMaterialMode
    )
    val rawHeaderChromeColors = tuneHomeTopGlassColors(
        colors = rememberHomeGlassChromeColors(
            glassEnabled = isGlassEnabled,
            blurEnabled = isTopChromeBlurEnabled
        ),
        isLightMode = isLightMode,
        emphasized = false
    )
    val headerChromeColors = remember(
        rawHeaderChromeColors,
        isGlassEnabled,
        isTopChromeBlurEnabled,
        blurIntensity,
        usesNativeContainerTreatment,
        usesTonalContainerTreatment,
    ) {
        val resolved = if (!isGlassEnabled && isTopChromeBlurEnabled) {
            resolveHomeTopBlurContainerColors(
                colors = rawHeaderChromeColors,
                surfaceColor = surfaceColor,
                blurIntensity = blurIntensity
            )
        } else {
            rawHeaderChromeColors
        }
        resolveHomeTopContainerColors(
            usesNativeContainerTreatment = usesNativeContainerTreatment,
            usesTonalContainerTreatment = usesTonalContainerTreatment,
            emphasized = false,
            blurEnabled = !isGlassEnabled && isTopChromeBlurEnabled,
            fallbackColors = resolved,
            surfaceContainerColor = surfaceContainerColor,
            surfaceContainerHighColor = surfaceContainerHighColor,
            outlineVariantColor = outlineVariantColor
        )
    }
    val rawSearchPillColors = tuneHomeTopGlassColors(
        colors = rememberHomeGlassPillColors(
            glassEnabled = isSearchGlassEnabled,
            blurEnabled = isSearchBlurEnabled,
            emphasized = true,
            baseColor = AppSurfaceTokens.cardContainer()
        ),
        isLightMode = isLightMode,
        emphasized = true
    )
    val searchPillColors = remember(
        rawSearchPillColors,
        isSearchGlassEnabled,
        isSearchBlurEnabled,
        blurIntensity,
        usesNativeContainerTreatment,
        usesTonalContainerTreatment,
    ) {
        val resolved = if (!isSearchGlassEnabled && isSearchBlurEnabled) {
            resolveHomeTopBlurContainerColors(
                colors = rawSearchPillColors,
                surfaceColor = surfaceColor,
                blurIntensity = blurIntensity
            )
        } else {
            rawSearchPillColors
        }
        resolveHomeTopContainerColors(
            usesNativeContainerTreatment = usesNativeContainerTreatment,
            usesTonalContainerTreatment = usesTonalContainerTreatment,
            emphasized = true,
            blurEnabled = !isSearchGlassEnabled && isSearchBlurEnabled,
            fallbackColors = resolved,
            surfaceContainerColor = surfaceContainerColor,
            surfaceContainerHighColor = surfaceContainerHighColor,
            outlineVariantColor = outlineVariantColor
        )
    }
    val rawTabChromeColors = tuneHomeTopGlassColors(
        colors = rememberHomeGlassChromeColors(
            glassEnabled = false,
            blurEnabled = enableTopTabSecondaryBlur || effectiveTabMaterialMode != TopTabMaterialMode.PLAIN
        ),
        isLightMode = isLightMode,
        emphasized = false
    )
    val tabChromeColors = remember(rawTabChromeColors, effectiveTabMaterialMode, blurIntensity) {
        if (effectiveTabMaterialMode == TopTabMaterialMode.BLUR) {
            resolveHomeTopBlurContainerColors(
                colors = rawTabChromeColors,
                surfaceColor = tabSurfaceColor,
                blurIntensity = blurIntensity
            )
        } else {
            rawTabChromeColors
        }
    }
    val searchPillStyle = remember(isSearchGlassEnabled, isSearchBlurEnabled) {
        resolveHomeGlassPillStyle(
            glassEnabled = isSearchGlassEnabled,
            blurEnabled = isSearchBlurEnabled,
            emphasized = true
        )
    }
    val tabChromeStyle = remember(effectiveTabMaterialMode, enableTopTabSecondaryBlur) {
        resolveHomeGlassChromeStyle(
            glassEnabled = false,
            blurEnabled = enableTopTabSecondaryBlur || effectiveTabMaterialMode != TopTabMaterialMode.PLAIN
        )
    }
    val topForegroundColor = resolveHomeTopForegroundColor(isLightMode = isLightMode)
    val topSearchContentAlpha = resolveHomeTopSearchContentAlpha(topChromeRenderMode)
    val topActionIconAlpha = resolveHomeTopActionIconAlpha(topChromeRenderMode)
    val topChromeMotionPolicy = resolveHomeTopChromeMotionPolicy(
        renderMode = topChromeRenderMode,
        isScrolling = isScrolling,
        isTransitionRunning = isTransitionRunning
    )
    val tabChromeRenderMode = when (effectiveTabMaterialMode) {
        TopTabMaterialMode.BLUR -> if (enableTopTabSecondaryBlur) {
            HomeTopChromeRenderMode.BLUR
        } else {
            HomeTopChromeRenderMode.PLAIN
        }
        TopTabMaterialMode.PLAIN -> HomeTopChromeRenderMode.PLAIN
    }
    val tabChromeMotionPolicy = resolveHomeTopTabChromeMotionPolicy(
        renderMode = tabChromeRenderMode,
        isScrolling = isScrolling,
        isTransitionRunning = isTransitionRunning
    )
    val useUnifiedTopPanel = shouldUseUnifiedHomeTopPanel(topChromePolicy)
    val useDetachedTopTabDock = shouldUseDetachedHomeTopTabDock(topChromePolicy.tabPresentation)
    val embedTopTabsInUnifiedPanel = useUnifiedTopPanel && !useDetachedTopTabDock
    val topPanelChromeRenderMode = resolveHomeTopPanelChromeRenderMode(
        renderMode = topChromeRenderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
        useUnifiedPanel = useUnifiedTopPanel
    )
    val searchChromeBaseRenderMode = resolveHomeTopChromeRenderMode(
        materialMode = searchChromeMaterialMode,
        hasHazeState = hazeState != null
    )
    val searchChromeRenderMode = resolveHomeTopSearchChromeRenderMode(
        renderMode = searchChromeBaseRenderMode,
        useUnifiedPanel = useUnifiedTopPanel,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
    )
    val useBottomBarMatchedTopControls = false
    val localTopChromeRenderMode = resolveHomeTopLocalChromeRenderMode(
        renderMode = topChromeRenderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
    )
    val localTabChromeRenderMode = resolveHomeTopLocalChromeRenderMode(
        renderMode = tabChromeRenderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
    )
    val continuousSlabRenderMode = resolveHomeTopContinuousSlabRenderMode(
        renderMode = topChromeRenderMode,
    )

    val headerOffsetQuantizationPx = with(density) { AppSpacingTokens.ExtraSmall.toPx() }
    val currentHeaderOffsetProvider by rememberUpdatedState(headerOffsetProvider)
    val headerOffset by remember(headerOffsetQuantizationPx) {
        derivedStateOf {
            com.android.purebilibili.feature.home.policy.quantizeHomeHeaderOffset(
                offsetPx = currentHeaderOffsetProvider(),
                stepPx = headerOffsetQuantizationPx
            )
        }
    }
    
    val searchBarHeightDp = resolveHomeTopSearchBarHeight(topChromePolicy)
    val tabRowHeightDp = resolveHomeTopTabRowHeight(
        isTabFloating = isTabFloating,
        chromePolicy = topChromePolicy,
        labelMode = homeSettings?.topTabLabelMode
            ?: com.android.purebilibili.core.store.SettingsManager.TopTabLabelMode.TEXT_ONLY
    )
    val searchCollapseDistanceDp = resolveHomeTopSearchCollapseDistance(
        searchBarHeight = searchBarHeightDp,
        chromePolicy = topChromePolicy,
    )
    val searchRevealDeadZoneDp = resolveHomeTopSearchRevealDeadZone(topChromePolicy)
    val searchBarHeightPx = with(density) { searchBarHeightDp.toPx() }
    val searchCollapseDistancePx = with(density) { searchCollapseDistanceDp.toPx() }
    val searchRevealDeadZonePx = with(density) { searchRevealDeadZoneDp.toPx() }
    val tabRowHeightPx = with(density) { tabRowHeightDp.toPx() }

    val scrollLayout = remember(
        headerOffset,
        searchBarHeightPx,
        searchCollapseDistancePx,
        searchRevealDeadZonePx,
        tabRowHeightPx,
        isHeaderCollapseEnabled
    ) {
        resolveHomeHeaderScrollLayout(
            headerOffsetPx = headerOffset,
            searchBarHeightPx = searchBarHeightPx,
            searchCollapseDistancePx = searchCollapseDistancePx,
            tabRowHeightPx = tabRowHeightPx,
            isHeaderCollapseEnabled = isHeaderCollapseEnabled,
            searchRevealDeadZonePx = searchRevealDeadZonePx,
            usesImmediateSearchReveal = usesImmediateHomeTopSearchReveal(searchRevealDeadZonePx)
        )
    }
    val currentSearchHeight = with(density) { scrollLayout.searchBarHeightPx.toDp() }
    val searchAlpha = scrollLayout.searchAlpha
    val expandedTabHeight = with(density) { scrollLayout.tabRowHeightPx.toDp() }
    val currentTabHeight by animateDpAsState(
        targetValue = resolveHomeTopTabPresentationHeight(
            expandedHeight = expandedTabHeight,
            isCollapsed = topTabsVisible && topTabsCollapsed,
            collapsedHandleHeight = if (isHeaderCollapseEnabled || isTopTabsAutoCollapseEnabled) {
                AppSpacingTokens.None
            } else {
                resolveHomeTopCollapsedHandleHeight()
            }
        ),
        animationSpec = AppMotionTokens.standardSpec(),
        label = "currentTabHeight"
    )
    val tabAlpha = scrollLayout.tabAlpha
    val searchRevealFraction = if (searchBarHeightPx > 0f) {
        (scrollLayout.searchBarHeightPx / searchBarHeightPx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val usesImmediateSearchReveal = remember(searchRevealDeadZonePx) {
        usesImmediateHomeTopSearchReveal(searchRevealDeadZonePx)
    }
    val searchContentRevealFraction = remember(searchRevealFraction, usesImmediateSearchReveal) {
        resolveHomeTopSearchContentRevealFraction(
            searchRevealFraction = searchRevealFraction,
            usesImmediateReveal = usesImmediateSearchReveal
        )
    }
    val searchContentTranslationYPx = remember(
        searchRevealFraction,
        searchBarHeightPx,
        usesImmediateSearchReveal
    ) {
        resolveHomeTopSearchContentTranslationYPx(
            searchRevealFraction = searchRevealFraction,
            searchBarHeightPx = searchBarHeightPx,
            usesImmediateReveal = usesImmediateSearchReveal
        )
    }
    val integratedCollapsedTopBar = shouldUseIntegratedCollapsedHomeTopBar(
        searchRevealFraction = searchRevealFraction,
        presentation = topChromePolicy.tabPresentation,
    )
    val unifiedPanelCornerRadius = resolveHomeTopUnifiedPanelCornerRadius(
        chromePolicy = topChromePolicy,
        collapsedIntoStatusBar = integratedCollapsedTopBar
    )
    val unifiedPanelShape = if (unifiedPanelCornerRadius == AppSpacingTokens.None) {
        androidx.compose.ui.graphics.RectangleShape
    } else {
        RoundedCornerShape(unifiedPanelCornerRadius)
    }
    val unifiedPanelHorizontalPadding = resolveHomeTopUnifiedPanelHorizontalPadding(topChromePolicy)
    val unifiedPanelInnerPadding = resolveHomeTopUnifiedPanelInnerPadding(
        chromePolicy = topChromePolicy,
        collapsedIntoStatusBar = integratedCollapsedTopBar
    )
    val searchToTabsSpacing = resolveHomeTopSearchToTabsSpacing(topChromePolicy)
    val currentSearchToTabsSpacing = searchToTabsSpacing * searchContentRevealFraction
    val currentUnifiedDividerBottomSpacing = AppSpacingTokens.ExtraSmall * searchContentRevealFraction

    val tabHorizontalPadding by animateDpAsState(
        targetValue = resolveHomeTopTabHorizontalPadding(
            isTabFloating = isTabFloating,
            chromePolicy = topChromePolicy,
        ),
        animationSpec = AppMotionTokens.standardSpec(),
        label = "tabHorizontalPadding"
    )
    val tabVerticalPadding by animateDpAsState(
        targetValue = resolveHomeTopTabVerticalPaddingDp(isTabFloating).dp,
        animationSpec = AppMotionTokens.standardSpec(),
        label = "tabVerticalPadding"
    )
    val tabVerticalOffset by animateDpAsState(
        targetValue = resolveHomeTopTabYOffsetDp(isTabFloating).dp,
        animationSpec = AppMotionTokens.standardSpec(),
        label = "tabVerticalOffset"
    )
    val tabShadowElevation by animateDpAsState(
        targetValue = if (useDetachedTopTabDock && usesNativeContainerTreatment) {
            // Native (MIUIX) detached capsule dock always lifts a hair so the long pill
            // reads as a floating segment over the feed below.
            AppSpacingTokens.Small
        } else if (usesNativeContainerTreatment) {
            AppSpacingTokens.None
        } else if (isTabFloating) {
            AppSpacingTokens.Small
        } else {
            AppSpacingTokens.None
        },
        animationSpec = AppMotionTokens.standardSpec(),
        label = "tabShadowElevation"
    )
    val effectiveTabShadowElevation = if (interactionBudget == HomeInteractionMotionBudget.REDUCED) AppSpacingTokens.None else tabShadowElevation
    val tabOverlayAlpha = resolveHomeTopTabOverlayAlpha(
        materialMode = effectiveTabMaterialMode,
        isTabFloating = isTabFloating,
        containerAlpha = tabChromeColors.containerColor.alpha
    )
    val tabContentAlpha by animateFloatAsState(
        targetValue = if (topTabsVisible && !topTabsCollapsed) 1f else 0f,
        animationSpec = AppMotionTokens.standardSpec(),
        label = "tabContentAlpha"
    )
    val effectiveContinuousSlabRenderMode = HomeTopChromeRenderMode.PLAIN
    val effectiveTopPanelChromeRenderMode = if (integratedCollapsedTopBar) {
        HomeTopChromeRenderMode.PLAIN
    } else {
        topPanelChromeRenderMode
    }
    val useUnifiedLiquidChrome = false
    val unifiedLocalTabChromeRenderMode = resolveHomeTopUnifiedLocalTabChromeRenderMode(
        renderMode = tabChromeRenderMode,
        usesNativeContainerTreatment = usesNativeContainerTreatment,
        usesTonalContainerTreatment = usesTonalContainerTreatment,
    )
    val effectiveTabChromeRenderMode = if (useUnifiedTopPanel) {
        resolveHomeTopUnifiedTabChromeRenderMode(
            localTabChromeRenderMode = unifiedLocalTabChromeRenderMode,
            usesTonalContainerTreatment = usesTonalContainerTreatment,
            useUnifiedLiquidChrome = useUnifiedLiquidChrome
        )
    } else {
        localTabChromeRenderMode
    }
    val topTabDockChromeRenderMode = if (
        useDetachedTopTabDock &&
        unifiedLocalTabChromeRenderMode == HomeTopChromeRenderMode.PLAIN &&
        hazeState != null
    ) {
        HomeTopChromeRenderMode.BLUR
    } else {
        unifiedLocalTabChromeRenderMode
    }
    val effectiveTabSurfaceColor = if (useDetachedTopTabDock) {
        resolveHomeTopDetachedTabDockSurfaceColor(
            isLightMode = isLightMode,
            renderMode = topTabDockChromeRenderMode
        )
    } else if (useUnifiedTopPanel) {
        resolveHomeTopUnifiedTabSurfaceColor(
            tabContainerColor = tabChromeColors.containerColor,
            tabOverlayAlpha = tabOverlayAlpha,
            usesTonalContainerTreatment = usesTonalContainerTreatment,
            useUnifiedLiquidChrome = useUnifiedLiquidChrome,
            tabChromeRenderMode = effectiveTabChromeRenderMode
        )
    } else {
        tabSurfaceColor.copy(alpha = tabOverlayAlpha)
    }
    val skinTintedTabSurfaceColor = uiSkinDecoration?.topAtmosphereTint?.copy(
        alpha = effectiveTabSurfaceColor.alpha.coerceAtLeast(0.36f)
    ) ?: effectiveTabSurfaceColor
    val renderUnifiedTopPanelChrome = embedTopTabsInUnifiedPanel && shouldRenderHomeTopUnifiedPanelChrome(
        searchHeightDp = currentSearchHeight.value,
        tabHeightDp = currentTabHeight.value,
        integratedCollapsedTopBar = integratedCollapsedTopBar
    )
    val drawUnifiedTopPanelChrome =
        renderUnifiedTopPanelChrome && effectiveTopPanelChromeRenderMode != HomeTopChromeRenderMode.PLAIN
    val drawTopSearchDivider =
        useUnifiedTopPanel &&
            shouldShowUnifiedHomeTopPanelDivider(topChromePolicy) &&
            drawUnifiedTopPanelChrome &&
            currentSearchHeight > AppSpacingTokens.None &&
            searchRevealFraction > 0f
    val useTopTabBottomBarMatchedDock = false
    val drawTopTabDockChrome = drawTopTabOuterChromeSurface || useTopTabBottomBarMatchedDock || useDetachedTopTabDock
    val topTabLabelMode = homeSettings?.topTabLabelMode
        ?: com.android.purebilibili.core.store.SettingsManager.TopTabLabelMode.TEXT_ONLY
    // Floating dock shell + tabs share one wrap decision so glass length matches content.
    val wrapTopTabDockFloatingStyle = if (embedTopTabsInUnifiedPanel) false else isTabFloating
    val wrapTopTabDockHasOuterChrome = drawTopTabDockChrome && !embedTopTabsInUnifiedPanel
    val wrapTopTabDockWidth = shouldWrapTopTabDockWidth(
        isFloatingStyle = wrapTopTabDockFloatingStyle,
        hasOuterChromeSurface = wrapTopTabDockHasOuterChrome,
        edgeToEdge = integratedCollapsedTopBar
    )
    val currentTabToSearchSpacing = currentSearchToTabsSpacing + if (drawTopSearchDivider) {
        AppSpacingTokens.Micro / 2 + currentUnifiedDividerBottomSpacing
    } else {
        AppSpacingTokens.None
    }
    val pinnedChromeLayout = resolveHomeTopPinnedChromeLayout(
        statusBarHeight = statusBarHeight,
        visibleSearchHeight = currentSearchHeight,
        tabRowHeight = currentTabHeight,
        searchToTabsSpacing = currentTabToSearchSpacing,
        renderMode = effectiveContinuousSlabRenderMode
    )
    val continuousSlabHeight = pinnedChromeLayout.blurHeight
    val isTopTabViewportSyncEnabled = resolveHomeTopTabViewportSyncEnabled(
        currentTabHeightDp = currentTabHeight.value,
        tabAlpha = tabAlpha,
        tabContentAlpha = tabContentAlpha
    )
    val tabBorderAlpha = if (isTabFloating) tabChromeStyle.borderAlpha else 0f
    val topAtmosphereImagePath = uiSkinDecoration?.topAtmosphereImagePath
    val topLayoutOrder = homeSettings?.homeTopLayoutOrder ?: HomeTopLayoutOrder.SEARCH_THEN_TABS
    val topTabsContent: @Composable (Dp) -> Unit = { maxDockWidth ->
        HomeTopTabChrome(
            currentTabHeight = currentTabHeight,
            tabAlpha = tabAlpha,
            tabContentAlpha = tabContentAlpha,
            containerZIndex = if (useUnifiedTopPanel) 0f else -1f,
            // 分栏 dock 最大宽度 = 顶部三控件合计宽度，保证左右对齐。
            maxDockWidth = maxDockWidth,
            tabHorizontalPadding = if (embedTopTabsInUnifiedPanel) {
                resolveNonNegativeHomeTopPadding(resolveHomeTopEmbeddedTabHorizontalPadding(topChromePolicy))
            } else {
                resolveNonNegativeHomeTopPadding(tabHorizontalPadding)
            },
            tabVerticalPadding = if (embedTopTabsInUnifiedPanel) {
                AppSpacingTokens.None
            } else {
                resolveNonNegativeHomeTopPadding(tabVerticalPadding)
            },
            tabVerticalOffset = if (embedTopTabsInUnifiedPanel) AppSpacingTokens.None else tabVerticalOffset,
            isTabFloating = if (embedTopTabsInUnifiedPanel) false else isTabFloating,
            effectiveTabShadowElevation = if (embedTopTabsInUnifiedPanel) AppSpacingTokens.None else effectiveTabShadowElevation,
            tabShape = if (useUnifiedTopPanel) {
                resolveSharedBottomBarCapsuleShape()
            } else {
                tabShape
            },
            tabChromeRenderMode = if (useTopTabBottomBarMatchedDock) {
                topTabDockChromeRenderMode
            } else {
                effectiveTabChromeRenderMode
            },
            tabSurfaceColor = skinTintedTabSurfaceColor,
            hazeState = hazeState,
            miuixBackdrop = miuixBackdrop,
            motionTier = motionTier,
            isScrolling = tabChromeMotionPolicy.isScrolling,
            isTransitionRunning = tabChromeMotionPolicy.isTransitionRunning,
            forceLowBlurBudget = forceLowBlurBudget,
            preferFlatGlass = !embedTopTabsInUnifiedPanel,
            tabBorderAlpha = if (embedTopTabsInUnifiedPanel) {
                0f
            } else {
                tabBorderAlpha
            },
            tabHighlightColor = if (embedTopTabsInUnifiedPanel) {
                Color.Transparent
            } else {
                resolveHomeTopChromeHighlightOverlayColor(
                    baseColor = tabChromeColors.highlightColor,
                    renderMode = tabChromeRenderMode,
                    softenWideChrome = true
                )
            },
            tabContentUnderlayColor = if (embedTopTabsInUnifiedPanel) {
                Color.Transparent
            } else {
                resolveHomeTopInnerUnderlayColor(
                    isLightMode = isLightMode,
                    renderMode = tabChromeRenderMode,
                    softenWideChrome = true
                )
            },
            gestureEnabled = topTabsVisible &&
                isTopTabsManualCollapseEnabled &&
                !isHeaderCollapseEnabled &&
                !isTopTabsAutoCollapseEnabled,
            isTabsCollapsed = topTabsCollapsed,
            onTabsCollapsedChange = onTopTabsCollapsedChange,
            drawChromeSurface = drawTopTabDockChrome,
            // Floating / matched dock: length follows icon+text × tab count (no full-bleed empty glass).
            wrapDockWidth = wrapTopTabDockWidth,
            dockCategoryCount = topCategories.size,
            dockLabelMode = topTabLabelMode
        ) {
            CategoryTabRow(
                categories = topCategories,
                categoryKeys = topCategoryKeys,
                selectedIndex = categoryIndex,
                onCategorySelected = { index ->
                    if (topTabsVisible) onCategorySelected(index)
                },
                onPartitionClick = {
                    if (topTabsVisible) onPartitionClick()
                },
                pagerState = pagerState,
                labelMode = topTabLabelMode,
                hazeState = hazeState,
                miuixBackdrop = miuixBackdrop,
                isFloatingStyle = isTabFloating,
                edgeToEdge = integratedCollapsedTopBar,
                hasOuterChromeSurface = drawTopTabDockChrome,
                // Same wrap decision as HomeTopTabChrome so shell length matches tab content.
                wrapDockWidth = wrapTopTabDockWidth,
                interactionBudget = interactionBudget,
                motionTier = motionTier,
                isTransitionRunning = isTransitionRunning,
                forceLowBlurBudget = forceLowBlurBudget,
                isViewportSyncEnabled = isTopTabViewportSyncEnabled,
                skinPlainStyle = shouldUseSkinPlainTopTabs,
                skinPlainContentColor = null,
                topTabSkinIconPaths = uiSkinDecoration?.topTabSkinIconPaths.orEmpty(),
                partitionSkinIconPath = uiSkinDecoration?.topTabPartitionIconPath(),
                maxDockWidthDp = maxDockWidth.value,
                forceMaterialUnderline = false
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
    ) {
        // 分栏 tab 最大宽度 = 顶部三控件合计宽度（头像左缘 ~ 设置按钮右缘）。
        val topControlsContentWidth = resolveHomeTopControlsContentWidthDp(
            containerWidthDp = maxWidth,
            chromePolicy = topChromePolicy
        )
        if (effectiveContinuousSlabRenderMode != HomeTopChromeRenderMode.PLAIN) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(continuousSlabHeight)
                    .homeTopChromeSurface(
                        renderMode = effectiveContinuousSlabRenderMode,
                        shape = resolveHomeTopContinuousSlabShape(),
                        surfaceColor = resolveHomeTopContinuousSlabSurfaceColor(
                            baseColor = headerChromeColors.containerColor,
                            blurAlpha = backgroundAlpha,
                            usesNativeContainerTreatment = usesNativeContainerTreatment,
                            renderMode = effectiveContinuousSlabRenderMode
                        ),
                        hazeState = hazeState,
                        miuixBackdrop = miuixBackdrop,
                        motionTier = motionTier,
                        isScrolling = topChromeMotionPolicy.isScrolling,
                        isTransitionRunning = topChromeMotionPolicy.isTransitionRunning,
                        forceLowBlurBudget = forceLowBlurBudget
                    )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacingTokens.None) // Reset padding, controlled by spacer
        ) {
            // 1. Status Bar Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                haptic(HapticType.LIGHT)
                                onStatusBarDoubleTap()
                            }
                        )
                    }
            )

            // 2. Search Bar + Avatar + right action
            // 高度和透明度由外部直接控制，实现物理跟手
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (embedTopTabsInUnifiedPanel) {
                            Modifier
                                .padding(horizontal = unifiedPanelHorizontalPadding)
                                .clip(unifiedPanelShape)
                                .then(
                                    if (drawUnifiedTopPanelChrome) {
                                        Modifier.homeTopChromeSurface(
                                            renderMode = effectiveTopPanelChromeRenderMode,
                                            shape = unifiedPanelShape,
                                            surfaceColor = headerChromeColors.containerColor,
                                            hazeState = hazeState,
                                            miuixBackdrop = miuixBackdrop,
                                            motionTier = motionTier,
                                            isScrolling = topChromeMotionPolicy.isScrolling,
                                            isTransitionRunning = topChromeMotionPolicy.isTransitionRunning,
                                            forceLowBlurBudget = forceLowBlurBudget,
                                            preferFlatGlass = resolveHomeTopWideChromePreferFlatGlass(
                                                effectiveTopPanelChromeRenderMode
                                            )
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .then(
                                    if (
                                        drawUnifiedTopPanelChrome &&
                                        !integratedCollapsedTopBar &&
                                        !useUnifiedLiquidChrome
                                    ) {
                                        Modifier.border(AppSpacingTokens.Micro * 0.4f, headerChromeColors.borderColor, unifiedPanelShape)
                                    } else {
                                        Modifier
                                    }
                                )
                        } else {
                            Modifier
                        }
                    )
            ) {
                if (!topAtmosphereImagePath.isNullOrBlank()) {
                    AsyncImage(
                        model = File(topAtmosphereImagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.30f)
                            .clearAndSetSemantics {}
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        headerChromeColors.containerColor.copy(alpha = 0.52f)
                                    )
                                )
                            )
                            .clearAndSetSemantics {}
                    )
                }
                if (
                    drawUnifiedTopPanelChrome &&
                    useUnifiedTopPanel &&
                    !integratedCollapsedTopBar &&
                    !useUnifiedLiquidChrome
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                resolveHomeTopUnifiedPanelReadabilityColor(
                                    isLightMode = isLightMode,
                                    renderMode = effectiveTopPanelChromeRenderMode
                                )
                            )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (embedTopTabsInUnifiedPanel) {
                                Modifier.padding(
                                    horizontal = if (integratedCollapsedTopBar) AppSpacingTokens.None else unifiedPanelInnerPadding,
                                    vertical = if (renderUnifiedTopPanelChrome) {
                                        unifiedPanelInnerPadding
                                    } else {
                                        AppSpacingTokens.None
                                    }
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    if (topLayoutOrder == HomeTopLayoutOrder.TABS_THEN_SEARCH) {
                        topTabsContent(topControlsContentWidth)
                        if (drawTopSearchDivider) {
                            Spacer(modifier = Modifier.height(currentSearchToTabsSpacing))
                            AppHorizontalDivider(
                                thickness = AppSpacingTokens.Micro / 2,
                                color = headerChromeColors.borderColor.copy(
                                    alpha = resolveHomeTopUnifiedPanelDividerAlpha(topChromeRenderMode) *
                                        searchRevealFraction
                                )
                            )
                            Spacer(modifier = Modifier.height(currentUnifiedDividerBottomSpacing))
                        } else {
                            Spacer(modifier = Modifier.height(currentSearchToTabsSpacing))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(currentSearchHeight)
                            .graphicsLayer {
                                alpha = searchAlpha
                                translationY = searchContentTranslationYPx
                            }
                            .clip(androidx.compose.ui.graphics.RectangleShape)
                    ) {
                        Row(
	                            modifier = Modifier
	                                .fillMaxWidth()
	                                .height(searchBarHeightDp)
	                                .padding(
	                                    horizontal = if (embedTopTabsInUnifiedPanel) {
	                                        AppSpacingTokens.None
	                                    } else {
	                                        resolveHomeTopSearchRowHorizontalPadding(topChromePolicy)
	                                    }
	                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(resolveHomeTopAvatarOuterSize())
                                    .then(
                                        if (usesNativeContainerTreatment) {
                                            Modifier.clickable {
                                                performHomeTopBarTap(haptic = haptic, onClick = onAvatarClick)
                                            }
                                        } else {
                                            Modifier.iOSTapEffect { onAvatarClick() }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(resolveHomeTopAvatarInnerSize())
                                        .clip(edgeButtonShape)
                                        .then(
                                            if (useUnifiedTopPanel) {
                                                if (useUnifiedLiquidChrome) {
                                                    Modifier
                                                        .homeTopChromeSurface(
                                                            renderMode = localTopChromeRenderMode,
                                                            shape = edgeButtonShape,
                                                            surfaceColor = headerChromeColors.containerColor,
                                                            hazeState = hazeState,
                                                            miuixBackdrop = miuixBackdrop,
                                                            motionTier = motionTier,
                                                            isScrolling = topChromeMotionPolicy.isScrolling,
                                                            isTransitionRunning = topChromeMotionPolicy.isTransitionRunning,
                                                            forceLowBlurBudget = forceLowBlurBudget
                                                        )
                                                } else {
                                                    Modifier.border(
                                                        width = AppSpacingTokens.Micro * 0.4f,
                                                        color = headerChromeColors.borderColor.copy(alpha = 0.7f),
                                                        shape = edgeButtonShape
                                                    )
                                                }
                                            } else {
                                                Modifier
                                                    .homeTopChromeSurface(
                                                        renderMode = localTopChromeRenderMode,
                                                        shape = edgeButtonShape,
                                                        surfaceColor = headerChromeColors.containerColor,
                                                        hazeState = hazeState,
                                                        miuixBackdrop = miuixBackdrop,
                                                        motionTier = motionTier,
                                                        isScrolling = topChromeMotionPolicy.isScrolling,
                                                        isTransitionRunning = topChromeMotionPolicy.isTransitionRunning,
                                                        forceLowBlurBudget = forceLowBlurBudget
                                                    )
                                                    .border(AppSpacingTokens.Micro / 2, headerChromeColors.borderColor, edgeButtonShape)
                                            }
                                        )
                                ) {
                                    HomeTopAvatarContent(
                                        user = user,
                                        shape = edgeButtonShape,
                                        fallbackBackgroundColor = if (useUnifiedTopPanel) {
                                            if (useUnifiedLiquidChrome) {
                                                Color.Transparent
                                            } else {
                                                topForegroundColor.copy(alpha = 0.10f)
                                            }
                                        } else {
                                            headerChromeColors.containerColor
                                        },
                                        fallbackTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(resolveHomeTopEdgeControlGap(topChromePolicy)))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(resolveHomeTopSearchPillHeight(topChromePolicy)),
                                contentAlignment = Alignment.Center
                            ) {
                                val isTablet = com.android.purebilibili.core.util.LocalWindowSizeClass.current.isTablet
                                val stableSearchContentColor = if (usesNativeContainerTreatment) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else if (isLightMode) {
                                    topForegroundColor
                                } else {
                                    OpticalContrastPalette.Highlight.copy(alpha = 0.96f)
                                }
                                val searchPillContent: @Composable () -> Unit = {
                                    HomeTopSearchPillContent(
                                        searchIcon = searchIcon,
                                        contentColor = stableSearchContentColor,
                                        textFontSize = if (usesNativeContainerTreatment) {
                                            if (isTablet) {
                                                MaterialTheme.typography.bodyMedium.fontSize
                                            } else {
                                                MaterialTheme.typography.labelMedium.fontSize
                                            }
                                        } else {
                                            if (isTablet) {
                                                MaterialTheme.typography.bodyLarge.fontSize
                                            } else {
                                                MaterialTheme.typography.bodyMedium.fontSize
                                            }
                                        },
                                        iconTextGap = resolveHomeTopSearchIconTextGap(topChromePolicy)
                                    )
                                }
                                val searchClickInteractionSource = remember { MutableInteractionSource() }
                                val defaultSearchSurfaceColor = if (useUnifiedTopPanel) {
                                    resolveHomeTopUnifiedSearchContainerColor(
                                        isLightMode = isLightMode,
                                        renderMode = searchChromeRenderMode
                                    )
                                } else {
                                    searchPillColors.containerColor
                                }
                                val skinSearchSurfaceColor = resolveHomeSkinSearchSurfaceColor(
                                    defaultSurfaceColor = defaultSearchSurfaceColor,
                                    skinTint = uiSkinDecoration?.searchCapsuleTint,
                                    useUnifiedTopPanel = useUnifiedTopPanel
                                )
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = AppSpacingTokens.TripleExtraLarge * 13 + AppSpacingTokens.Large)
                                        .fillMaxWidth()
                                        .height(resolveHomeTopSearchPillHeight(topChromePolicy))
                                        .then(
                                            // drawBackdrop owns the glass shape. Pre-clipping the
                                            // BiliPai lens cuts its sampled rim into a bright line.
                                            if (useBottomBarMatchedTopControls) {
                                                Modifier
                                            } else {
                                                Modifier.clip(searchContainerShape)
                                            }
                                        )
                                        .then(
                                            Modifier.homeTopChromeSurface(
                                                renderMode = searchChromeRenderMode,
                                                shape = searchContainerShape,
                                                surfaceColor = skinSearchSurfaceColor,
                                                hazeState = hazeState,
                                                miuixBackdrop = miuixBackdrop,
                                                motionTier = motionTier,
                                                isScrolling = topChromeMotionPolicy.isScrolling,
                                                isTransitionRunning = topChromeMotionPolicy.isTransitionRunning,
                                                forceLowBlurBudget = forceLowBlurBudget,
                                                preferFlatGlass = resolveHomeTopWideChromePreferFlatGlass(
                                                    searchChromeRenderMode
                                                ),
                                                darkThemeWhiteOverlayMultiplier = resolveHomeTopSearchDarkWhiteOverlayMultiplier(
                                                    isLightMode = isLightMode
                                                )
                                            )
                                        )
                                        .border(
                                            width = AppSpacingTokens.Micro * 0.4f,
                                            color = if (useBottomBarMatchedTopControls) {
                                                Color.Transparent
                                            } else if (useUnifiedTopPanel) {
                                                resolveHomeTopUnifiedSearchBorderColor(
                                                    isLightMode = isLightMode,
                                                    renderMode = searchChromeRenderMode
                                                )
                                            } else {
                                                searchPillColors.borderColor
                                            },
                                            shape = searchContainerShape
                                        )
                                        .clickable(
                                            interactionSource = searchClickInteractionSource,
                                            indication = null
                                        ) {
                                            haptic(HapticType.LIGHT)
                                            onSearchClick()
                                        }
                                        .padding(horizontal = resolveHomeTopSearchContentHorizontalPadding(topChromePolicy)),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (
                                        shouldDrawHomeTopSearchLegacyHighlight(
                                            presentation = topChromePolicy.tabPresentation,
                                            useUnifiedTopPanel = useUnifiedTopPanel,
                                            renderMode = searchChromeRenderMode,
                                            refractionOverlayAlpha = 0f
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(AppSpacingTokens.Medium + AppSpacingTokens.Micro)
                                                .align(Alignment.TopCenter)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            resolveHomeTopChromeHighlightOverlayColor(
                                                                baseColor = searchPillColors.highlightColor,
                                                                renderMode = topChromeRenderMode,
                                                                softenWideChrome = true
                                                            ),
                                                            Color.Transparent
                                                        )
                                                    )
                                            )
                                        )
                                    }
                                    searchPillContent()
                                }
                            }

                            Spacer(modifier = Modifier.width(resolveHomeTopEdgeControlGap(topChromePolicy)))

                            val topRightActionButtonSize = resolveHomeTopSettingsButtonSize(topChromePolicy)
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(
                                        resolveHomeTopRightActionSlotWidth(
                                            buttonSize = topRightActionButtonSize,
                                            badgeLayout = topRightUnreadBadgeLayout,
                                            hasUnreadBadge = topRightUnreadBadge != null
                                        )
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(topRightActionButtonSize)
                                        .clip(edgeButtonShape)
                                        .then(
                                            if (useUnifiedTopPanel) {
                                                Modifier
                                                    .background(
                                                        resolveHomeTopEdgeControlContainerColor(
                                                            isLightMode = isLightMode,
                                                            renderMode = localTopChromeRenderMode
                                                        )
                                                    )
                                                    .border(
                                                        width = AppSpacingTokens.Micro * 0.4f,
                                                        color = resolveHomeTopEdgeControlBorderColor(
                                                            isLightMode = isLightMode,
                                                            renderMode = localTopChromeRenderMode
                                                        ),
                                                        shape = edgeButtonShape
                                                    )
                                            } else {
                                                Modifier
                                                    .homeTopChromeSurface(
                                                        renderMode = localTopChromeRenderMode,
                                                        shape = edgeButtonShape,
                                                        surfaceColor = headerChromeColors.containerColor,
                                                        hazeState = hazeState,
                                                        miuixBackdrop = miuixBackdrop,
                                                        motionTier = motionTier,
                                                        isScrolling = topChromeMotionPolicy.isScrolling,
                                                        isTransitionRunning = topChromeMotionPolicy.isTransitionRunning,
                                                        forceLowBlurBudget = forceLowBlurBudget
                                                    )
                                                    .border(AppSpacingTokens.Micro * 0.4f, headerChromeColors.borderColor, edgeButtonShape)
                                            }
                                        )
                                        .then(
                                            if (usesNativeContainerTreatment) {
                                                Modifier.clickable {
                                                    performHomeTopBarTap(haptic = haptic, onClick = onTopRightActionClick)
                                                }
                                            } else {
                                                Modifier.iOSTapEffect {
                                                    haptic(HapticType.LIGHT)
                                                    onTopRightActionClick()
                                                }
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AppIcon(
                                        topRightActionIcon,
                                        contentDescription = topRightActionContentDescription,
                                        tint = if (isLightMode) {
                                            topForegroundColor
                                        } else {
                                            topForegroundColor.copy(alpha = topActionIconAlpha)
                                        },
                                        modifier = Modifier.size(resolveHomeTopSettingsIconSize(topChromePolicy))
                                    )
                                }
                                if (topRightUnreadBadge != null) {
                                    HomeTopUnreadBadge(
                                        text = topRightUnreadBadge,
                                        layout = topRightUnreadBadgeLayout,
                                        borderColor = AppSurfaceTokens.cardContainer(),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(
                                                x = topRightUnreadBadgeLayout.offsetX,
                                                y = topRightUnreadBadgeLayout.offsetY
                                            )
                                    )
                                }
                            }
                        }
                    }

                    if (topLayoutOrder == HomeTopLayoutOrder.SEARCH_THEN_TABS) {
                        if (drawTopSearchDivider) {
                            Spacer(modifier = Modifier.height(currentSearchToTabsSpacing))
                            AppHorizontalDivider(
                                thickness = AppSpacingTokens.Micro / 2,
                                color = headerChromeColors.borderColor.copy(
                                    alpha = resolveHomeTopUnifiedPanelDividerAlpha(topChromeRenderMode) *
                                        searchRevealFraction
                                )
                            )
                            Spacer(modifier = Modifier.height(currentUnifiedDividerBottomSpacing))
                        } else {
                            Spacer(modifier = Modifier.height(currentSearchToTabsSpacing))
                        }

                        topTabsContent(topControlsContentWidth)
                    }
                }
            }
        }
    }
}
