package com.android.purebilibili.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.rememberAppSegmentedControlPolicy
import com.android.purebilibili.core.ui.renderer.miuix.AppMiuixTabRow

data class AppSegmentOption<T>(
    val value: T,
    val label: String,
)

data class AppSegmentedControlColors(
    val outerContainerColor: Color,
    val activeContainerColor: Color,
    val activeContentColor: Color,
    val inactiveContentColor: Color,
)

data class AppMiuixSegmentedColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val selectedBackgroundColor: Color,
    val selectedContentColor: Color,
)

data class AppLiquidSegmentedControlSpec(
    val itemWidthDp: Int,
    val heightDp: Int,
    val indicatorHeightDp: Int,
    val labelFontSizeSp: Int,
    val liquidGlassEffectsEnabled: Boolean,
    val tapPressRefractionEnabled: Boolean,
)

fun resolveAppSegmentedLabelFontSizeSp(
    optionCount: Int,
    longestLabelLength: Int,
): Float = when {
    optionCount >= 5 -> 12f
    optionCount >= 4 && longestLabelLength >= 3 -> 12f
    optionCount >= 4 -> 13f
    optionCount >= 3 && longestLabelLength >= 4 -> 13f
    longestLabelLength >= 7 -> 13f
    longestLabelLength >= 5 -> 14f
    else -> 15f
}

fun shouldFillMaxWidthAppSegmentedControl(
    optionCount: Int,
    longestLabelLength: Int,
): Boolean = optionCount >= 2 || longestLabelLength >= 1

fun resolveAppLiquidSegmentedControlSpec(
    itemCount: Int,
    hasExternalBackdrop: Boolean,
    longestLabelLength: Int = 0,
): AppLiquidSegmentedControlSpec {
    // 液金分段控件为固定视觉形态，不随主题风格变化（历史实现固定使用 iOS 紧凑尺寸 44dp）。
    val liquidControlHeightDp = 44
    return AppLiquidSegmentedControlSpec(
        itemWidthDp = if (itemCount >= 4) 56 else 66,
        heightDp = liquidControlHeightDp,
        indicatorHeightDp = 30,
        labelFontSizeSp = resolveAppSegmentedLabelFontSizeSp(
            optionCount = itemCount,
            longestLabelLength = longestLabelLength,
        ).toInt(),
        liquidGlassEffectsEnabled = hasExternalBackdrop,
        tapPressRefractionEnabled = false,
    )
}

fun resolveAppSegmentedLiquidGlassRequest(
    forceLiquidIndicator: Boolean,
    hasExternalBackdrop: Boolean,
): Boolean? = if (forceLiquidIndicator && hasExternalBackdrop) true else null

fun resolveAppSegmentedControlColors(
    miuixSecondaryContainer: Color,
    miuixOnSecondaryContainer: Color,
    miuixSurfaceContainerHigh: Color,
    miuixOnSurfaceVariantSummary: Color,
): AppSegmentedControlColors = AppSegmentedControlColors(
    outerContainerColor = miuixSurfaceContainerHigh,
    activeContainerColor = miuixSecondaryContainer,
    activeContentColor = miuixOnSecondaryContainer,
    inactiveContentColor = miuixOnSurfaceVariantSummary,
)

fun resolveAppMiuixSegmentedColors(
    colors: AppSegmentedControlColors,
): AppMiuixSegmentedColors = AppMiuixSegmentedColors(
    backgroundColor = Color.Transparent,
    contentColor = colors.inactiveContentColor,
    selectedBackgroundColor = colors.activeContainerColor,
    selectedContentColor = colors.activeContentColor,
)

fun <T> resolveAppSegmentedSelectionIndex(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
): Int {
    if (options.isEmpty()) return 0
    return options.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)
}

@Composable
fun <T> AppNativeTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scrollable: Boolean = false,
    minTabWidth: Dp = 72.dp,
    onSelectionChange: (T) -> Unit,
) {
    if (options.isEmpty()) return
    val policy = rememberAppSegmentedControlPolicy()
    val colors = resolveAppSegmentedControlColors(
        miuixSecondaryContainer = AppSurfaceTokens.secondaryContainer(),
        miuixOnSecondaryContainer = AppSurfaceTokens.onSecondaryContainer(),
        miuixSurfaceContainerHigh = AppSurfaceTokens.surfaceContainerHigh(),
        miuixOnSurfaceVariantSummary = AppSurfaceTokens.onSurfaceVariantSummary(),
    )
    AppMiuixTabRow(
        options = options,
        selectedValue = selectedValue,
        enabled = enabled,
        scrollable = scrollable,
        minTabWidth = minTabWidth,
        colors = colors,
        pillCornerRadius = policy.pillCornerRadius,
        modifier = modifier,
        onSelectionChange = onSelectionChange,
    )
}
