package com.android.purebilibili.core.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

enum class AppSemanticIconFamily {
    MATERIAL,
    MIUIX,
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
    val accentPalette: AppSemanticAccentPalette?,
    val prefersNativeChrome: Boolean,
    val supportsIndependentLiquidGlass: Boolean,
    val prefersGroupedListCards: Boolean = false,
) {
    /** 图标字形固定为 MIUIX（主题色容器 / MD3 官方推荐已删除）。 */
    val effectiveIconFamily: AppSemanticIconFamily
        get() = iconFamily

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
            iconFamily = AppSemanticIconFamily.MIUIX,
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
): AppSemanticVisualPolicy = AppSemanticVisualPolicy.material(materialPalette).copy(
    iconFamily = AppSemanticIconFamily.MIUIX,
    prefersGroupedListCards = true,
)

@Composable
fun rememberAppSemanticVisualPolicy(): AppSemanticVisualPolicy {
    val colorScheme = MaterialTheme.colorScheme
    return remember(colorScheme) {
        resolveAppSemanticVisualPolicy(
            materialPalette = resolveAppSemanticAccentPalette(
                colorScheme = colorScheme,
                useSemanticAccentRoles = false,
            ),
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
