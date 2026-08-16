package com.android.purebilibili.core.ui.renderer.miuix

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppNativeTabRowHeightDp
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.components.AppSegmentedControlColors
import com.android.purebilibili.core.ui.components.resolveAppMiuixSegmentedColors
import com.android.purebilibili.core.ui.components.resolveAppSegmentedSelectionIndex
import com.android.purebilibili.core.ui.resolveHeightCappedCornerRadius
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults

@Composable
internal fun <T> AppMiuixTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    scrollable: Boolean,
    minTabWidth: Dp,
    colors: AppSegmentedControlColors,
    pillCornerRadius: Dp,
    modifier: Modifier,
    onSelectionChange: (T) -> Unit,
) {
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val tabColors = resolveAppMiuixSegmentedColors(colors)
    // 40dp (not 48): still ≥ min touch via outer padding; avoids half-height capsule look.
    val itemHeight = AppNativeTabRowHeightDp.dp
    val corner = resolveHeightCappedCornerRadius(itemHeight, pillCornerRadius)
    TabRow(
        tabs = options.map { it.label },
        selectedTabIndex = selectedIndex,
        onTabSelected = { index ->
            if (enabled) options.getOrNull(index)?.let { onSelectionChange(it.value) }
        },
        modifier = modifier.fillMaxWidth(),
        colors = TabRowDefaults.tabRowColors(
            backgroundColor = tabColors.backgroundColor,
            contentColor = tabColors.contentColor,
            selectedBackgroundColor = tabColors.selectedBackgroundColor,
            selectedContentColor = tabColors.selectedContentColor,
        ),
        // 非 scrollable（如频道/状态切换）：交给 Miuix 按容器宽度均分，与 Material TabRow
        // 一致；scrollable（如时间表/分类）：minTabWidth 兜底保证可读。
        minWidth = if (scrollable) minTabWidth else 0.dp,
        maxWidth = Dp.Infinity,
        height = itemHeight,
        cornerRadius = corner,
        itemSpacing = 8.dp,
    )
}
