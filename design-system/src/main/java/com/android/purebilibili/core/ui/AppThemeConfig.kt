package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.android.purebilibili.core.ui.blur.BlurIntensity

/**
 * UI 组件运行时需要的应用级配置。
 *
 * 数据读取由应用外壳负责，core/ui 只消费已解析的值，避免反向依赖设置存储。
 */
data class AppThemeConfig(
    val blurIntensity: BlurIntensity = BlurIntensity.THIN,
    val hapticFeedbackEnabled: Boolean = true,
    val runtimeVisualGuardEnabled: Boolean = true,
)

val LocalAppThemeConfig = staticCompositionLocalOf { AppThemeConfig() }

@Composable
fun ProvideAppThemeConfig(
    config: AppThemeConfig,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppThemeConfig provides config, content = content)
}
