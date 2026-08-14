package com.android.purebilibili.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.components.resolveAppLiquidSegmentedControlSpec
import com.android.purebilibili.core.ui.components.resolveAppSegmentedSelectionIndex
import com.android.purebilibili.feature.home.components.BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP
import com.android.purebilibili.feature.home.components.BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.kyant.backdrop.Backdrop

@Composable
internal fun <T> AppSegmentedControl(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @Suppress("UNUSED_PARAMETER") forceLiquidIndicator: Boolean = false,
    height: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP.dp,
    indicatorHeight: Dp = BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP.dp,
    labelFontSize: TextUnit = 14.sp,
    @Suppress("UNUSED_PARAMETER") backdrop: Backdrop? = null,
    @Suppress("UNUSED_PARAMETER") tapPressRefractionEnabled: Boolean = true,
    dragSelectionEnabled: Boolean = true,
    containerColorOverride: Color? = null,
    indicatorIdleSurfaceColorOverride: Color? = null,
    onSelectionChange: (T) -> Unit,
) {
    if (options.isEmpty()) return
    AppLiquidSegmentedControlHost(
        options = options,
        selectedValue = selectedValue,
        modifier = modifier,
        enabled = enabled,
        height = height,
        indicatorHeight = indicatorHeight,
        labelFontSize = labelFontSize,
        dragSelectionEnabled = dragSelectionEnabled,
        containerColorOverride = containerColorOverride,
        indicatorIdleSurfaceColorOverride = indicatorIdleSurfaceColorOverride,
        onSelectionChange = onSelectionChange,
    )
}

@Composable
private fun <T> AppLiquidSegmentedControlHost(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    modifier: Modifier,
    enabled: Boolean,
    height: Dp,
    indicatorHeight: Dp,
    labelFontSize: TextUnit,
    dragSelectionEnabled: Boolean,
    containerColorOverride: Color?,
    indicatorIdleSurfaceColorOverride: Color?,
    onSelectionChange: (T) -> Unit,
) {
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val longestLabelLength = options.maxOfOrNull { it.label.length } ?: 0
    val spec = resolveAppLiquidSegmentedControlSpec(
        itemCount = options.size,
        hasExternalBackdrop = false,
        longestLabelLength = longestLabelLength,
    )
    val usesDefaultBottomBarSizing =
        height == BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_HEIGHT_DP.dp &&
            indicatorHeight == BOTTOM_BAR_LIQUID_SEGMENTED_CONTROL_INDICATOR_HEIGHT_DP.dp &&
            labelFontSize == 14.sp
    val resolvedHeight = if (usesDefaultBottomBarSizing) spec.heightDp.dp else height
    val resolvedIndicatorHeight =
        if (usesDefaultBottomBarSizing) spec.indicatorHeightDp.dp else indicatorHeight
    val resolvedLabelFontSize =
        if (usesDefaultBottomBarSizing) spec.labelFontSizeSp.sp else labelFontSize

    BottomBarLiquidSegmentedControl(
        items = options.map { it.label },
        selectedIndex = selectedIndex,
        onSelected = { index ->
            options.getOrNull(index)?.let { onSelectionChange(it.value) }
        },
        modifier = modifier,
        enabled = enabled,
        itemWidth = null,
        height = resolvedHeight,
        indicatorHeight = resolvedIndicatorHeight,
        labelFontSize = resolvedLabelFontSize,
        dragSelectionEnabled = dragSelectionEnabled,
        containerColorOverride = containerColorOverride,
        indicatorIdleSurfaceColorOverride = indicatorIdleSurfaceColorOverride,
    )
}
