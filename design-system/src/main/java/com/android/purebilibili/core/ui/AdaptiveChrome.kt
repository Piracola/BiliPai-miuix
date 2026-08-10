package com.android.purebilibili.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar as MiuixSmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils

fun isNativeMiuixEnabled(
    uiStyle: AppUiStyle
): Boolean = uiStyle == AppUiStyle.MIUIX

@Composable
fun rememberIsNativeMiuixEnabled(): Boolean {
    return isNativeMiuixEnabled(
        uiStyle = LocalAppUiStyle.current
    )
}

enum class AdaptiveTopAppBarStyle {
    SMALL,
    CENTERED,
    LARGE
}

data class AdaptiveTopAppBarChromeSpec(
    val containerCornerRadiusDp: Int,
    val scrolledContainerAlpha: Float,
    val scrolledTonalElevationDp: Int,
    val motionScale: Float
)

val LocalGlobalWallpaperBackdropVisible = compositionLocalOf { false }

fun resolveGlobalWallpaperProtectiveColor(
    baseColor: Color,
    lightAlpha: Float = 0.74f,
    darkAlpha: Float = 0.80f
): Color {
    val alpha = if (baseColor.luminance() > 0.5f) lightAlpha else darkAlpha
    return baseColor.copy(alpha = alpha.coerceIn(0f, 1f))
}

fun resolveAdaptiveTopAppBarChromeSpec(
    uiStyle: AppUiStyle
): AdaptiveTopAppBarChromeSpec {
    val chromeTokens = resolveAndroidNativeChromeTokens(uiStyle)
    return AdaptiveTopAppBarChromeSpec(
        containerCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
        scrolledContainerAlpha = 1f,
        scrolledTonalElevationDp = 0,
        motionScale = chromeTokens.motionScale
    )
}

fun resolveAdaptiveScaffoldContainerColor(
    requestedContainerColor: Color,
    defaultBackgroundColor: Color,
    globalWallpaperVisible: Boolean
): Color {
    return if (globalWallpaperVisible && requestedContainerColor == defaultBackgroundColor) {
        resolveGlobalWallpaperProtectiveColor(
            baseColor = requestedContainerColor,
            lightAlpha = 0.66f,
            darkAlpha = 0.72f
        )
    } else {
        requestedContainerColor
    }
}

fun resolveGlobalWallpaperChromeColor(
    requestedColor: Color,
    defaultBackgroundColor: Color,
    defaultSurfaceColor: Color,
    globalWallpaperVisible: Boolean
): Color {
    if (!globalWallpaperVisible || requestedColor.alpha == 0f) return requestedColor
    val requestedOpaque = requestedColor.copy(alpha = 1f)
    return if (
        requestedOpaque == defaultBackgroundColor.copy(alpha = 1f) ||
        requestedOpaque == defaultSurfaceColor.copy(alpha = 1f)
    ) {
        resolveGlobalWallpaperProtectiveColor(
            baseColor = requestedOpaque,
            lightAlpha = 0.74f,
            darkAlpha = 0.80f
        )
    } else {
        requestedColor
    }
}

@Composable
fun globalWallpaperAwareChromeColor(color: Color): Color {
    return resolveGlobalWallpaperChromeColor(
        requestedColor = color,
        defaultBackgroundColor = MaterialTheme.colorScheme.background,
        defaultSurfaceColor = MaterialTheme.colorScheme.surface,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    )
}

@Composable
fun Modifier.globalWallpaperAwareBackground(
    color: Color = MaterialTheme.colorScheme.background
): Modifier {
    return if (LocalGlobalWallpaperBackdropVisible.current) {
        background(
            resolveAdaptiveScaffoldContainerColor(
                requestedContainerColor = color,
                defaultBackgroundColor = MaterialTheme.colorScheme.background,
                globalWallpaperVisible = true
            )
        )
    } else {
        background(color)
    }
}

@Composable
fun AdaptiveScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars,
    content: @Composable (PaddingValues) -> Unit
) {
    val effectiveContainerColor = resolveAdaptiveScaffoldContainerColor(
        requestedContainerColor = containerColor,
        defaultBackgroundColor = MaterialTheme.colorScheme.background,
        globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    )
    val scaffoldRenderer = resolveAdaptiveScaffoldRenderer(
        uiStyle = LocalAppUiStyle.current
    )
    when (scaffoldRenderer) {
        AdaptiveScaffoldRenderer.MIUIX_SCAFFOLD_WITH_POPUP_HOST -> {
        MiuixScaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            popupHost = { MiuixPopupUtils.MiuixPopupHost() },
            containerColor = effectiveContainerColor,
            contentWindowInsets = contentWindowInsets,
            content = content
        )
        }
        AdaptiveScaffoldRenderer.MATERIAL3_SCAFFOLD -> {
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            floatingActionButton = floatingActionButton,
            snackbarHost = snackbarHost,
            containerColor = effectiveContainerColor,
            contentWindowInsets = contentWindowInsets,
            content = content
        )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    largeTitle: String = title,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    style: AdaptiveTopAppBarStyle = AdaptiveTopAppBarStyle.SMALL,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val uiStyle = LocalAppUiStyle.current
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val chromeSpec = resolveAdaptiveTopAppBarChromeSpec(uiStyle)
    val effectiveColors = if (globalWallpaperVisible) {
        colors.copy(
            containerColor = resolveGlobalWallpaperChromeColor(
                requestedColor = colors.containerColor,
                defaultBackgroundColor = MaterialTheme.colorScheme.background,
                defaultSurfaceColor = MaterialTheme.colorScheme.surface,
                globalWallpaperVisible = true
            ),
            scrolledContainerColor = resolveGlobalWallpaperChromeColor(
                requestedColor = colors.scrolledContainerColor,
                defaultBackgroundColor = MaterialTheme.colorScheme.background,
                defaultSurfaceColor = MaterialTheme.colorScheme.surface,
                globalWallpaperVisible = true
            )
        )
    } else {
        colors
    }
    val topAppBarColors = effectiveColors

    if (rememberIsNativeMiuixEnabled()) {
        val navigationContent =
            @Composable {
                CompositionLocalProvider(
                    LocalContentColor provides topAppBarColors.navigationIconContentColor
                ) {
                    navigationIcon()
                }
            }
        val actionsContent: @Composable RowScope.() -> Unit = {
            CompositionLocalProvider(
                LocalContentColor provides topAppBarColors.actionIconContentColor
            ) {
                actions()
            }
        }
        when (style) {
            AdaptiveTopAppBarStyle.LARGE -> {
                MiuixTopAppBar(
                    title = title,
                    largeTitle = largeTitle,
                    modifier = modifier,
                    color = topAppBarColors.containerColor,
                    navigationIcon = navigationContent,
                    actions = actionsContent,
                    // Miuix 标题可用宽度 = (总宽 - 导航 - actions) × 0.9 - titlePadding×2；
                    // 默认 26dp×2 + 多 actions 会把标题挤到省略号。压紧 padding 把空间还给标题。
                    titlePadding = 0.dp,
                    navigationIconPadding = 0.dp,
                    actionIconPadding = 0.dp,
                )
            }

            AdaptiveTopAppBarStyle.SMALL,
            AdaptiveTopAppBarStyle.CENTERED -> {
                MiuixSmallTopAppBar(
                    title = title,
                    modifier = modifier,
                    color = topAppBarColors.containerColor,
                    navigationIcon = navigationContent,
                    actions = actionsContent,
                    titlePadding = 0.dp,
                    navigationIconPadding = 0.dp,
                    actionIconPadding = 0.dp,
                )
            }
        }
        return
    }

    val topBarWindowInsets = WindowInsets.statusBars
    when (style) {
        AdaptiveTopAppBarStyle.SMALL -> {
            TopAppBar(
                modifier = modifier,
                title = { Text(title) },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = topAppBarColors,
                scrollBehavior = scrollBehavior,
                windowInsets = topBarWindowInsets
            )
        }

        AdaptiveTopAppBarStyle.CENTERED -> {
            CenterAlignedTopAppBar(
                modifier = modifier,
                title = { Text(title) },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = topAppBarColors,
                scrollBehavior = scrollBehavior,
                windowInsets = topBarWindowInsets
            )
        }

        AdaptiveTopAppBarStyle.LARGE -> {
            TopAppBar(
                modifier = modifier,
                title = { Text(largeTitle, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = navigationIcon,
                actions = actions,
                colors = topAppBarColors,
                scrollBehavior = scrollBehavior,
                windowInsets = topBarWindowInsets
            )
        }
    }
}

/**
 * Semantic top-tab treatments consumed by feature chrome.
 *
 * The values describe interaction and geometry rather than a vendor renderer, so feature code
 * does not need to inspect the persisted UI style.
 */
enum class AppTopTabPresentation {
    MOVING_CAPSULE,
    MATERIAL_UNDERLINE,
    TONAL_CAPSULE,
}

data class AppTopChromePolicy(
    val tabPresentation: AppTopTabPresentation,
    val iconFamily: AppSemanticIconFamily,
    val iconStyle: AppIconStyle = AppIconStyle.AUTO,
    val compactChromeSpec: CompactCapsuleChromeSpec,
) {
    /** MD3 官方推荐样式强制 Material 官方字形。 */
    val effectiveIconFamily: AppSemanticIconFamily
        get() = if (iconStyle == AppIconStyle.MD3_STANDARD) {
            AppSemanticIconFamily.MATERIAL
        } else {
            iconFamily
        }
}

fun resolveAppTopChromePolicy(
    uiStyle: AppUiStyle,
    iconStyle: AppIconStyle = AppIconStyle.AUTO,
): AppTopChromePolicy = when (uiStyle) {
    AppUiStyle.MIUIX -> AppTopChromePolicy(
        tabPresentation = AppTopTabPresentation.TONAL_CAPSULE,
        iconFamily = AppSemanticIconFamily.MATERIAL,
        iconStyle = iconStyle,
        compactChromeSpec = resolveCompactCapsuleChromeSpec(uiStyle),
    )
    AppUiStyle.MATERIAL3 -> AppTopChromePolicy(
        tabPresentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
        iconFamily = AppSemanticIconFamily.MATERIAL,
        iconStyle = iconStyle,
        compactChromeSpec = resolveCompactCapsuleChromeSpec(uiStyle),
    )
}

@Composable
fun rememberAppTopChromePolicy(): AppTopChromePolicy {
    val uiStyle = LocalAppUiStyle.current
    val iconStyle = rememberResolvedAppIconStyle()
    return remember(uiStyle, iconStyle) {
        resolveAppTopChromePolicy(uiStyle, iconStyle)
    }
}

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentWindowInsets: WindowInsets = WindowInsets.navigationBars,
    content: @Composable (PaddingValues) -> Unit,
) = AdaptiveScaffold(
    modifier = modifier,
    topBar = topBar,
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    snackbarHost = snackbarHost,
    containerColor = containerColor,
    contentWindowInsets = contentWindowInsets,
    content = content,
)

enum class AppTopBarStyle {
    SMALL,
    CENTERED,
    LARGE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    largeTitle: String = title,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    style: AppTopBarStyle = AppTopBarStyle.SMALL,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) = AdaptiveTopAppBar(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    largeTitle = largeTitle,
    navigationIcon = navigationIcon,
    actions = actions,
    colors = colors,
    style = when (style) {
        AppTopBarStyle.SMALL -> AdaptiveTopAppBarStyle.SMALL
        AppTopBarStyle.CENTERED -> AdaptiveTopAppBarStyle.CENTERED
        AppTopBarStyle.LARGE -> AdaptiveTopAppBarStyle.LARGE
    },
    scrollBehavior = scrollBehavior,
)
