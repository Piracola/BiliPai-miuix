package com.android.purebilibili.core.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppSemanticIconFamily {
    MATERIAL,
    MIUIX,
}

/**
 * 全局图标呈现样式(用户可切换的两套方案)。
 * - [AUTO]:保持现状 —— Miuix 图标(设置图标多彩色等既有外观)。
 * - [THEME_CONTAINER]:主题色容器 —— 图标置于主题色(secondaryContainer)
 *   圆角容器内,图标用 onSecondaryContainer,对齐官方 Settings 容器图标规范。
 * - [MD3_STANDARD]:MD3 官方推荐 —— onSurfaceVariant 单色图标、无容器。
 */
enum class AppIconStyle {
    AUTO,
    THEME_CONTAINER,
    MD3_STANDARD,
}

/**
 * AUTO 表示"保持现状":Miuix 预设不引入容器化与单色化(设置图标保持多彩色等既有外观)。
 */
fun resolveAppIconStyle(
    iconStyle: AppIconStyle,
): AppIconStyle = when (iconStyle) {
    AppIconStyle.AUTO -> AppIconStyle.AUTO
    else -> iconStyle
}

/** 从持久化字符串解析 [AppIconStyle],非法或缺失值回退 [AppIconStyle.AUTO]。 */
fun resolveAppIconStylePreference(rawValue: String?): AppIconStyle {
    return runCatching {
        rawValue?.let(AppIconStyle::valueOf)
    }.getOrNull() ?: AppIconStyle.AUTO
}

/**
 * 全局图标呈现样式 CompositionLocal。
 * 默认 [AppIconStyle.AUTO] 由 UI 预设推导;主题层提供用户显式选择后全局生效。
 */
val LocalAppIconStyle = staticCompositionLocalOf {
    AppIconStyle.AUTO
}

/** 解析当前生效的图标呈现样式(处理 AUTO 跟随运行时主题)。 */
@Composable
fun rememberResolvedAppIconStyle(): AppIconStyle {
    val iconStyle = LocalAppIconStyle.current
    return remember(iconStyle) {
        resolveAppIconStyle(iconStyle)
    }
}

enum class AppSemanticAccentRole {
    PRIMARY,
    SECONDARY,
    TERTIARY,
    ERROR,
}

data class AppSemanticAccentPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
)

data class AppSemanticVisualPolicy(
    val iconFamily: AppSemanticIconFamily,
    val iconStyle: AppIconStyle = AppIconStyle.AUTO,
    val accentPalette: AppSemanticAccentPalette?,
    val prefersNativeChrome: Boolean,
    val supportsIndependentLiquidGlass: Boolean,
    val prefersGroupedListCards: Boolean = false,
) {
    /**
     * MD3 官方推荐样式强制使用 Material 官方字形(运行时字形已收敛为单值)。
     */
    val effectiveIconFamily: AppSemanticIconFamily
        get() = if (iconStyle == AppIconStyle.MD3_STANDARD) {
            AppSemanticIconFamily.MATERIAL
        } else {
            iconFamily
        }

    fun resolveAccent(role: AppSemanticAccentRole, fallback: Color): Color {
        val palette = accentPalette ?: return fallback
        return when (role) {
            AppSemanticAccentRole.PRIMARY -> palette.primary
            AppSemanticAccentRole.SECONDARY -> palette.secondary
            AppSemanticAccentRole.TERTIARY -> palette.tertiary
            AppSemanticAccentRole.ERROR -> palette.error
        }
    }

    companion object {
        fun material(palette: AppSemanticAccentPalette) = AppSemanticVisualPolicy(
            iconFamily = AppSemanticIconFamily.MATERIAL,
            accentPalette = palette,
            prefersNativeChrome = true,
            supportsIndependentLiquidGlass = false,
        )
    }
}

fun resolveAppSemanticAccentPalette(
    colorScheme: ColorScheme,
    useSemanticAccentRoles: Boolean,
): AppSemanticAccentPalette = AppSemanticAccentPalette(
    primary = colorScheme.primary,
    secondary = if (useSemanticAccentRoles) colorScheme.secondary else colorScheme.primary,
    tertiary = if (useSemanticAccentRoles) colorScheme.tertiary else colorScheme.primary,
    error = colorScheme.error,
)

fun resolveAppSemanticVisualPolicy(
    materialPalette: AppSemanticAccentPalette,
    iconStyle: AppIconStyle = AppIconStyle.AUTO,
): AppSemanticVisualPolicy = AppSemanticVisualPolicy.material(materialPalette).copy(
    iconFamily = AppSemanticIconFamily.MIUIX,
    prefersGroupedListCards = true,
    iconStyle = iconStyle,
)

@Composable
fun rememberAppSemanticVisualPolicy(): AppSemanticVisualPolicy {
    val colorScheme = MaterialTheme.colorScheme
    val iconStyle = rememberResolvedAppIconStyle()
    return remember(colorScheme, iconStyle) {
        resolveAppSemanticVisualPolicy(
            materialPalette = resolveAppSemanticAccentPalette(
                colorScheme = colorScheme,
                useSemanticAccentRoles = false,
            ),
            iconStyle = iconStyle,
        )
    }
}

fun resolveAppChromeLiquidGlassEnabled(
    supportsIndependentLiquidGlass: Boolean,
    individualEnabled: Boolean,
    androidNativeEnabled: Boolean,
): Boolean = androidNativeEnabled || (supportsIndependentLiquidGlass && individualEnabled)

@Composable
fun rememberAppChromeLiquidGlassEnabled(
    individualEnabled: Boolean,
    androidNativeEnabled: Boolean,
): Boolean {
    val policy = rememberAppSemanticVisualPolicy()
    return remember(policy.supportsIndependentLiquidGlass, individualEnabled, androidNativeEnabled) {
        resolveAppChromeLiquidGlassEnabled(
            supportsIndependentLiquidGlass = policy.supportsIndependentLiquidGlass,
            individualEnabled = individualEnabled,
            androidNativeEnabled = androidNativeEnabled,
        )
    }
}
