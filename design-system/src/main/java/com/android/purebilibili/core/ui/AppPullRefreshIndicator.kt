package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshState

internal enum class AppPullRefreshIndicatorRenderer {
    MIUIX,
}

enum class AppPullRefreshMotionStyle { CUPERTINO, PLATFORM }

enum class AppPullRefreshIndicatorStyle {
    CUPERTINO,
    MATERIAL_DEFAULT,
    MATERIAL_SCREENSHOT_HANDLE,
    MIUIX_NATIVE,
}

data class AppPullRefreshProfile(
    val motionStyle: AppPullRefreshMotionStyle,
    val indicatorStyle: AppPullRefreshIndicatorStyle,
)

fun resolveAppPullRefreshProfile(): AppPullRefreshProfile = AppPullRefreshProfile(
    AppPullRefreshMotionStyle.PLATFORM,
    AppPullRefreshIndicatorStyle.MIUIX_NATIVE,
)

@Composable
fun rememberAppPullRefreshProfile(): AppPullRefreshProfile =
    resolveAppPullRefreshProfile()

internal fun resolveAppPullRefreshIndicatorRenderer(): AppPullRefreshIndicatorRenderer =
    AppPullRefreshIndicatorRenderer.MIUIX

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppPullRefreshLoadingIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    AppPullRefreshLoadingIndicator(
        modifier = modifier,
    )
}

@Composable
fun AppPullRefreshLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppSurfaceTokens.primary(),
) {
    AdaptiveLoadingIndicator(
        modifier = modifier,
        size = AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro,
        color = color,
        strokeWidth = AppSpacingTokens.Micro,
    )
}
