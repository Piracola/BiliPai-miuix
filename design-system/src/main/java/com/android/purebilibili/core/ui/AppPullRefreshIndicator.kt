package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import io.github.alexzhirkevich.cupertino.CupertinoActivityIndicator

internal enum class AppPullRefreshIndicatorRenderer {
    CUPERTINO,
    MATERIAL3,
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

fun resolveAppPullRefreshProfile(
    renderer: PresetPrimitiveRenderer,
): AppPullRefreshProfile = when (renderer) {
    PresetPrimitiveRenderer.IOS -> AppPullRefreshProfile(
        AppPullRefreshMotionStyle.CUPERTINO,
        AppPullRefreshIndicatorStyle.CUPERTINO,
    )
    PresetPrimitiveRenderer.MATERIAL3 -> AppPullRefreshProfile(
        AppPullRefreshMotionStyle.PLATFORM,
        AppPullRefreshIndicatorStyle.MATERIAL_DEFAULT,
    )
    PresetPrimitiveRenderer.MIUIX_BRIDGED -> AppPullRefreshProfile(
        AppPullRefreshMotionStyle.PLATFORM,
        AppPullRefreshIndicatorStyle.MIUIX_NATIVE,
    )
}

@Composable
fun rememberAppPullRefreshProfile(): AppPullRefreshProfile =
    resolveAppPullRefreshProfile(rememberPresetPrimitiveRenderer())

internal fun resolveAppPullRefreshIndicatorRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
): AppPullRefreshIndicatorRenderer = when (
    resolvePresetPrimitiveRenderer(uiPreset, androidNativeVariant)
) {
    PresetPrimitiveRenderer.IOS -> AppPullRefreshIndicatorRenderer.CUPERTINO
    PresetPrimitiveRenderer.MATERIAL3 -> AppPullRefreshIndicatorRenderer.MATERIAL3
    PresetPrimitiveRenderer.MIUIX_BRIDGED -> AppPullRefreshIndicatorRenderer.MIUIX
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppPullRefreshLoadingIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    when (rememberPresetPrimitiveRenderer()) {
        PresetPrimitiveRenderer.MATERIAL3 -> PullToRefreshDefaults.LoadingIndicator(
            state = state,
            isRefreshing = isRefreshing,
            modifier = modifier,
        )

        PresetPrimitiveRenderer.IOS,
        PresetPrimitiveRenderer.MIUIX_BRIDGED -> AppPullRefreshLoadingIndicator(
            modifier = modifier,
        )
    }
}

@Composable
fun AppPullRefreshLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = AppSurfaceTokens.primary(),
) {
    when (rememberPresetPrimitiveRenderer()) {
        PresetPrimitiveRenderer.IOS -> CupertinoActivityIndicator(
            modifier = modifier.size(AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall),
            color = color,
        )
        PresetPrimitiveRenderer.MATERIAL3 -> AdaptiveLoadingIndicator(
            modifier = modifier,
            size = AppSpacingTokens.DoubleExtraLarge + AppSpacingTokens.ExtraSmall,
            color = color,
            density = AdaptiveLoadingDensity.PAGE,
        )
        PresetPrimitiveRenderer.MIUIX_BRIDGED -> AdaptiveLoadingIndicator(
            modifier = modifier,
            size = AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro,
            color = color,
            strokeWidth = AppSpacingTokens.Micro,
        )
    }
}
