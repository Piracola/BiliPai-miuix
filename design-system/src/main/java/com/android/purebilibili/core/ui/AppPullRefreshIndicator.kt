package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
