package com.android.purebilibili.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.LocalBottomBarContentPadding
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.core.ui.LocalSetBottomBarVisible
import com.android.purebilibili.core.ui.TopReadabilityChrome
import com.android.purebilibili.core.ui.blur.BlurStyles
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.ui.LocalAppListItemStyle
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.LocalAppPreferenceIconTreatment
import com.android.purebilibili.core.ui.components.LocalAppPreferenceGroupPresentation
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.feature.settings.SettingsBottomBarScrollState
import com.android.purebilibili.feature.settings.SettingsBottomBarScrollTracker
import com.android.purebilibili.feature.settings.SettingsContentWidth
import com.android.purebilibili.feature.settings.SettingsMaxContentWidthDp
import com.android.purebilibili.feature.settings.SettingsPageScrollHost
import com.android.purebilibili.feature.settings.SettingsVisualOverrides
import com.android.purebilibili.feature.settings.reduceSettingsBottomBarScroll
import com.android.purebilibili.feature.settings.resolveSettingsVisualPolicy
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 是否处于 840dp 双栏设置的详情面板（`SettingsTabletShell` 的 rightPane）。
 *
 * 双栏顶部栏契约：详情面板内抑制子页面 Scaffold 的第二套顶栏，只保留
 * master 面板一套顶栏和详情面板一套表面（见 UI-GAP-008）。
 */
val LocalSettingsDetailPane = staticCompositionLocalOf { false }

@Composable
internal fun SettingsBottomBarScrollEffect(listState: LazyListState) {
    val setBottomBarVisible = LocalSetBottomBarVisible.current
    val density = LocalDensity.current
    val topRevealThresholdPx = with(density) { 24.dp.roundToPx() }
    val directionThresholdPx = with(density) { 32.dp.roundToPx() }

    LaunchedEffect(
        listState,
        setBottomBarVisible,
        topRevealThresholdPx,
        directionThresholdPx,
    ) {
        var tracker = SettingsBottomBarScrollTracker(
            previousState = SettingsBottomBarScrollState(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
            ),
        )
        snapshotFlow {
            SettingsBottomBarScrollState(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset,
            )
        }
            .distinctUntilChanged()
            .collect { currentState ->
                val update = reduceSettingsBottomBarScroll(
                    tracker = tracker,
                    currentState = currentState,
                    topRevealThresholdPx = topRevealThresholdPx,
                    directionThresholdPx = directionThresholdPx,
                )
                update.bottomBarVisible?.let(setBottomBarVisible)
                tracker = update.tracker
            }
    }

    DisposableEffect(setBottomBarVisible) {
        onDispose { setBottomBarVisible(true) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsPageScaffold(
    title: String,
    onBack: () -> Unit,
    backContentDescription: String,
    bottomContentPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    scrollHost: SettingsPageScrollHost = SettingsPageScrollHost.LazyColumn,
    topBarBlurEnabled: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    header: (@Composable () -> Unit)? = null,
    lazyListContent: (LazyListScope.() -> Unit)? = null,
    content: @Composable () -> Unit = {},
) {
    if (scrollHost == SettingsPageScrollHost.LazyColumn) {
        SettingsBottomBarScrollEffect(listState)
    }
    val resolvedBottomContentPadding = maxOf(
        bottomContentPadding,
        LocalBottomBarContentPadding.current,
    )
    val hazeState = rememberRecoverableHazeState()
    val blurIntensity = currentUnifiedBlurIntensity()
    val topBarSurfaceAlpha = if (topBarBlurEnabled) {
        BlurStyles.getBackgroundAlpha(blurIntensity)
    } else {
        0.86f
    }
    val pageContainerColor = AppSurfaceTokens.groupedListContainer()

    // P1 双主题视觉合同：分组/图标/顶栏/分隔/宽度由纯策略解析，Scaffold 不再
    // 无条件强制 FLAT/FILLED。行样式覆盖取主题层提供的原始用户选择，
    // 在设置页范围内解析为主题官方默认（显式 CUSTOM/NATIVE 原样保留）。
    val uiStyle = LocalAppUiStyle.current
    val userPreferenceRowStyle = LocalAppListItemStyle.current
    val visualPolicy = remember(uiStyle, userPreferenceRowStyle) {
        resolveSettingsVisualPolicy(
            uiStyle = uiStyle,
            overrides = SettingsVisualOverrides(preferenceRowStyle = userPreferenceRowStyle),
        )
    }
    val inDetailPane = LocalSettingsDetailPane.current

    CompositionLocalProvider(
        LocalGlobalWallpaperBackdropVisible provides false,
        LocalAppPreferenceIconTreatment provides visualPolicy.iconTreatment,
        LocalAppPreferenceGroupPresentation provides visualPolicy.groupPresentation,
        LocalAppListItemStyle provides visualPolicy.preferenceRowStyle,
    ) {
        AppScaffold(
            modifier = modifier,
            topBar = {
                if (inDetailPane) {
                    // 双栏详情面板：只保留状态栏同色块，不渲染第二套顶栏
                    // （master 面板顶栏即该窗格唯一顶部栏）。
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .background(pageContainerColor),
                    )
                } else {
                    Box {
                        TopReadabilityChrome(
                            height = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp,
                            surfaceColor = pageContainerColor,
                            surfaceAlpha = topBarSurfaceAlpha,
                            hazeState = hazeState,
                            hazeEnabled = topBarBlurEnabled,
                        )
                        AppTopBar(
                            title = title,
                            navigationIcon = {
                                AppIconButton(onClick = onBack) {
                                    AppIcon(
                                        imageVector = rememberAppBackIcon(),
                                        contentDescription = backContentDescription,
                                    )
                                }
                            },
                            actions = actions,
                            style = visualPolicy.topBarStyle,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            },
            containerColor = pageContainerColor,
            contentWindowInsets = WindowInsets(0.dp),
        ) { padding ->
            val scrollModifier = Modifier
                .padding(padding)
                .let { modifier ->
                    if (visualPolicy.contentWidth == SettingsContentWidth.LIMITED) {
                        modifier.responsiveContentWidth(maxWidth = SettingsMaxContentWidthDp)
                    } else {
                        modifier
                    }
                }
                .fillMaxSize()
                .hazeSourceCompat(state = hazeState)

            when (scrollHost) {
                SettingsPageScrollHost.LazyColumn -> {
                    LazyColumn(
                        state = listState,
                        modifier = scrollModifier,
                        contentPadding = PaddingValues(bottom = resolvedBottomContentPadding),
                    ) {
                        if (header != null) {
                            item {
                                header()
                            }
                        }
                        if (lazyListContent != null) {
                            lazyListContent()
                        } else {
                            item {
                                content()
                            }
                        }
                    }
                }

                SettingsPageScrollHost.External -> {
                    Column(modifier = scrollModifier) {
                        header?.invoke()
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxSize(),
                        ) {
                            content()
                        }
                    }
                }
            }
        }
    }
}
