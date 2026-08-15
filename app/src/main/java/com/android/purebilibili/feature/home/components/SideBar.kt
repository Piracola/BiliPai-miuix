package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppPlatformNavigationRail
import com.android.purebilibili.core.ui.components.AppPlatformNavigationRailItem
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.R
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import com.android.purebilibili.core.ui.resolveGlobalWallpaperProtectiveColor
import com.android.purebilibili.core.ui.rememberAppNavigationCapabilities
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.WindowWidthSizeClass
import com.android.purebilibili.core.util.rememberHapticFeedback
import dev.chrisbanes.haze.HazeState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.ViewSidebar
import kotlinx.coroutines.launch

/**
 * 平板端侧边导航栏 - 垂直版本的 FrostedBottomBar
 */
internal fun performHomeSideBarItemTap(
    haptic: (HapticType) -> Unit,
    onClick: () -> Unit
) {
    haptic(HapticType.LIGHT)
    onClick()
}

@Composable
fun FrostedSideBar(
    currentItem: BottomNavItem = BottomNavItem.HOME,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    firstItemModifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onHomeDoubleTap: () -> Unit = {},
    visibleItems: List<BottomNavItem> = listOf(
        BottomNavItem.HOME,
        BottomNavItem.DYNAMIC,
        BottomNavItem.HISTORY,
        BottomNavItem.PROFILE
    ),
    itemColorIndices: Map<String, Int> = emptyMap(),
    uiSkinDecoration: BottomBarUiSkinDecoration? = null,
    onToggleSidebar: (() -> Unit)? = null,
    onAccountSwitchClick: (() -> Unit)? = null,
) {
    if (rememberAppNavigationCapabilities().usePlatformSideRail) {
            MiuixSideBar(
                currentItem = currentItem,
                onItemClick = onItemClick,
                modifier = modifier,
                firstItemModifier = firstItemModifier,
                hazeState = hazeState,
                onHomeDoubleTap = onHomeDoubleTap,
                visibleItems = visibleItems,
                uiSkinDecoration = uiSkinDecoration,
                onToggleSidebar = onToggleSidebar,
                onAccountSwitchClick = onAccountSwitchClick,
            )
    } else {
            FrostedSideBarContent(
                currentItem = currentItem,
                onItemClick = onItemClick,
                modifier = modifier,
                firstItemModifier = firstItemModifier,
                hazeState = hazeState,
                onHomeDoubleTap = onHomeDoubleTap,
                visibleItems = visibleItems,
                uiSkinDecoration = uiSkinDecoration,
                onToggleSidebar = onToggleSidebar,
                onAccountSwitchClick = onAccountSwitchClick,
            )
    }
}

@Composable
private fun MiuixSideBar(
    currentItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier,
    firstItemModifier: Modifier,
    hazeState: HazeState?,
    onHomeDoubleTap: () -> Unit,
    visibleItems: List<BottomNavItem>,
    uiSkinDecoration: BottomBarUiSkinDecoration?,
    onToggleSidebar: (() -> Unit)?,
    onAccountSwitchClick: (() -> Unit)?,
) {
    val haptic = rememberHapticFeedback()
    val isExpandedWidthClass =
        LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded
    val expandable = shouldUseExpandableMiuixSideBar(isExpandedWidthClass)
    val chromeBackground = AppSurfaceTokens.surface()
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val blurIntensity = com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity()
    val backgroundAlpha = com.android.purebilibili.core.ui.blur.BlurStyles.getBackgroundAlpha(blurIntensity)
    val railColor = if (hazeState != null) {
        val rawColor = chromeBackground.copy(alpha = backgroundAlpha)
        if (globalWallpaperVisible) {
            val protectiveColor = resolveGlobalWallpaperProtectiveColor(
                baseColor = chromeBackground,
                lightAlpha = 0.70f,
                darkAlpha = 0.76f
            )
            rawColor.copy(alpha = maxOf(rawColor.alpha, protectiveColor.alpha))
        } else {
            rawColor
        }
    } else {
        chromeBackground
    }
    var lastHomeClickMs by remember { mutableLongStateOf(0L) }

    AppPlatformNavigationRail(
        expanded = expandable,
        modifier = modifier
            .fillMaxHeight()
            .then(
                if (hazeState != null) {
                    Modifier.unifiedBlur(hazeState, shape = androidx.compose.ui.graphics.RectangleShape)
                } else {
                    Modifier
                }
            ),
        color = railColor,
        showDivider = true,
    ) {
        visibleItems.forEachIndexed { itemIndex, item ->
            val isSelected = item == currentItem
            val itemLabel = resolveBottomNavItemLabel(item)
            val skinIconPath = uiSkinDecoration?.iconPathFor(item, selected = isSelected)
            val itemModifier = if (itemIndex == 0) firstItemModifier else Modifier
            val onItemTap = {
                val nowMs = SystemClock.elapsedRealtime()
                when (
                    resolveHomeSideBarClickAction(
                        item = item,
                        nowMs = nowMs,
                        lastHomeClickMs = lastHomeClickMs
                    )
                ) {
                    HomeSideBarClickAction.HOME_DOUBLE_TAP -> {
                        haptic(HapticType.MEDIUM)
                        onHomeDoubleTap()
                    }
                    HomeSideBarClickAction.NAVIGATE -> {
                        performHomeSideBarItemTap(
                            haptic = haptic,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
                if (item == BottomNavItem.HOME) {
                    lastHomeClickMs = nowMs
                }
            }

            if (shouldUseMiuixOfficialSideBarItem(skinIconPath)) {
                AppPlatformNavigationRailItem(
                    selected = isSelected,
                    onClick = onItemTap,
                    icon = resolveHomeNavigationBarIcon(item),
                    label = itemLabel,
                    modifier = itemModifier
                )
            } else {
                MiuixSideBarSkinItem(
                    selected = isSelected,
                    label = itemLabel,
                    skinIconPath = skinIconPath,
                    onClick = onItemTap,
                    modifier = itemModifier
                )
            }
        }

        if (onAccountSwitchClick != null) {
            Spacer(modifier = Modifier.weight(1f))
            SideBarAccountSwitchButton(
                onClick = onAccountSwitchClick,
                tint = AppSurfaceTokens.onSurfaceVariantSummary(),
            )
        }

        if (onToggleSidebar != null) {
            Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
            val sidebarLabel = stringResource(R.string.sidebar_toggle)
            Box(
                modifier = Modifier
                    .size(AppChromeSizeTokens.MinimumTouchTarget)
                    .clip(AppShapes.container(ContainerLevel.Card))
                    .clickable {
                        haptic(HapticType.LIGHT)
                        onToggleSidebar()
                    },
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    Icons.Outlined.ViewSidebar,
                    contentDescription = sidebarLabel,
                    tint = AppSurfaceTokens.onSurfaceVariantSummary(),
                    modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.MiuixSideBarSkinItem(
    selected: Boolean,
    label: String,
    skinIconPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val unselectedColor = AppSurfaceTokens.onSurface().copy(alpha = 0.6f)
    val iconColor = if (selected) primaryColor else unselectedColor
    Column(
        modifier = modifier
            .padding(vertical = AppSpacingTokens.Medium)
            .size(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides iconColor) {
            if (skinIconPath != null) {
                BottomBarSkinIcon(
                    iconPath = skinIconPath,
                    contentDescription = label,
                    size = resolveBottomBarMiuixSkinDockIconSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))
        AppText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = iconColor
        )
    }
}

@Composable
private fun FrostedSideBarContent(
    currentItem: BottomNavItem,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier,
    firstItemModifier: Modifier,
    hazeState: HazeState?,
    onHomeDoubleTap: () -> Unit,
    visibleItems: List<BottomNavItem>,
    uiSkinDecoration: BottomBarUiSkinDecoration?,
    onToggleSidebar: (() -> Unit)?,
    onAccountSwitchClick: (() -> Unit)?,
) {
    val haptic = rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val blurIntensity = com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity()
    val backgroundAlpha = com.android.purebilibili.core.ui.blur.BlurStyles.getBackgroundAlpha(blurIntensity)
    val chromeBackground = AppSurfaceTokens.chromeBackground()
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val sideBarContainerColor = if (hazeState != null) {
        val rawColor = chromeBackground.copy(alpha = backgroundAlpha)
        if (globalWallpaperVisible) {
            val protectiveColor = resolveGlobalWallpaperProtectiveColor(
                baseColor = chromeBackground,
                lightAlpha = 0.70f,
                darkAlpha = 0.76f
            )
            rawColor.copy(alpha = maxOf(rawColor.alpha, protectiveColor.alpha))
        } else {
            rawColor
        }
    } else {
        chromeBackground.copy(alpha = 0.95f)
    }

    val sideBarWidth = AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.DoubleExtraLarge

    AppSurface(
        modifier = modifier
            .width(sideBarWidth)
            .fillMaxHeight()
            .then(
                if (hazeState != null) {
                    Modifier.unifiedBlur(hazeState, shape = androidx.compose.ui.graphics.RectangleShape)
                } else {
                    Modifier.background(chromeBackground)
                }
            ),
        shape = androidx.compose.ui.graphics.RectangleShape,
        color = sideBarContainerColor,
        border = if (hazeState != null) {
            androidx.compose.foundation.BorderStroke(
                width = AppSpacingTokens.Micro / 4,
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                )
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                width = AppSpacingTokens.Micro / 4,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .padding(vertical = AppSpacingTokens.ExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            visibleItems.forEachIndexed { itemIndex, item ->
                val isSelected = item == currentItem
                val itemLabel = resolveBottomNavItemLabel(item)

                var isPending by remember { mutableStateOf(false) }
                var wobbleAngle by remember { mutableFloatStateOf(0f) }

                val primaryColor = MaterialTheme.colorScheme.primary
                val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                val skinIconPath = uiSkinDecoration?.iconPathFor(item, selected = isSelected)

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected || isPending) primaryColor else unselectedColor,
                    animationSpec = AppMotionTokens.standardSpec(),
                    label = "iconColor"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = sideBarSelectionScaleMotionSpec(),
                    label = "scale"
                )

                val animatedWobble by animateFloatAsState(
                    targetValue = wobbleAngle,
                    animationSpec = sideBarWobbleMotionSpec(),
                    label = "wobble"
                )

                LaunchedEffect(wobbleAngle) {
                    if (wobbleAngle != 0f) {
                        kotlinx.coroutines.delay(50)
                        wobbleAngle = 0f
                    }
                }
                val triggerItemClick = {
                    isPending = true
                    performHomeSideBarItemTap(
                        haptic = haptic,
                        onClick = { onItemClick(item) }
                    )
                    wobbleAngle = 8f
                    scope.launch {
                        kotlinx.coroutines.delay(90)
                        isPending = false
                    }
                }

                Column(
                    modifier = Modifier
                        .size(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large)
                        .then(if (itemIndex == 0) firstItemModifier else Modifier)
                        .then(
                            if (item == BottomNavItem.HOME) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            triggerItemClick()
                                        },
                                        onDoubleTap = {
                                            haptic(HapticType.MEDIUM)
                                            onHomeDoubleTap()
                                        }
                                    )
                                }
                            } else {
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    triggerItemClick()
                                }
                            }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                rotationZ = animatedWobble
                            }
                    ) {
                        CompositionLocalProvider(LocalContentColor provides iconColor) {
                            if (skinIconPath != null) {
                                BottomBarSkinIcon(
                                    iconPath = skinIconPath,
                                    contentDescription = itemLabel,
                                    size = resolveBottomBarMiuixSkinDockIconSize()
                                )
                            } else {
                                AppIcon(
                                    imageVector = resolveHomeNavigationBarIcon(item = item),
                                    contentDescription = itemLabel
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))

                    AppText(
                        text = itemLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = iconColor
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
            }

            Spacer(modifier = Modifier.weight(1f))

            if (onAccountSwitchClick != null) {
                SideBarAccountSwitchButton(
                    onClick = onAccountSwitchClick,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
            }

            if (onToggleSidebar != null) {
                val sidebarLabel = stringResource(R.string.sidebar_toggle)
                Box(
                    modifier = Modifier
                        .size(AppChromeSizeTokens.MinimumTouchTarget)
                        .clip(AppShapes.container(ContainerLevel.Card))
                        .clickable {
                            haptic(HapticType.LIGHT)
                            onToggleSidebar()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        Icons.Outlined.ViewSidebar,
                        contentDescription = sidebarLabel,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraLarge))
            }
        }
    }
}

@Composable
private fun SideBarAccountSwitchButton(
    onClick: () -> Unit,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val haptic = rememberHapticFeedback()
    Box(
        modifier = modifier
            .size(AppChromeSizeTokens.MinimumTouchTarget)
            .clip(AppShapes.container(ContainerLevel.Card))
            .clickable {
                haptic(HapticType.LIGHT)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            Icons.Outlined.SwapHoriz,
            contentDescription = "切换账号",
            tint = tint,
            modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro),
        )
    }
}
