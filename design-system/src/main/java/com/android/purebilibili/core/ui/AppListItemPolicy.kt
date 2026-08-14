package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 列表条目呈现样式(用户可切换)。
 * - [AUTO]:跟随运行时主题 —— 当前仅 MIUIX 运行时,解析为 [NATIVE]。
 * - [CUSTOM]:项目自定义条目(圆角图标容器 + 自定义 Row)。
 * - [NATIVE]:Miuix 原生条目。
 */
enum class AppListItemStyle {
    AUTO,
    CUSTOM,
    NATIVE,
}

fun resolveAppListItemStyle(
    style: AppListItemStyle,
): AppListItemStyle = when (style) {
    AppListItemStyle.AUTO -> AppListItemStyle.NATIVE
    else -> style
}

/** 从持久化字符串解析 [AppListItemStyle],非法或缺失值回退 [AppListItemStyle.AUTO]。 */
fun resolveAppListItemStylePreference(rawValue: String?): AppListItemStyle {
    return runCatching {
        rawValue?.let(AppListItemStyle::valueOf)
    }.getOrNull() ?: AppListItemStyle.AUTO
}

/** 全局列表条目样式 CompositionLocal,主题层提供用户显式选择。 */
val LocalAppListItemStyle = staticCompositionLocalOf {
    AppListItemStyle.AUTO
}

/** 解析当前生效的列表条目样式(处理 AUTO 跟随运行时主题)。 */
@Composable
fun rememberResolvedAppListItemStyle(): AppListItemStyle {
    val style = LocalAppListItemStyle.current
    return remember(style) {
        resolveAppListItemStyle(style)
    }
}
