package com.android.purebilibili.core.ui.renderer.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.adaptiveSquircleBackground
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.components.AppSegmentedControlColors
import com.android.purebilibili.core.ui.components.resolveAppMiuixSegmentedColors
import com.android.purebilibili.core.ui.components.resolveAppSegmentedSelectionIndex
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults

@Composable
internal fun <T> AppMiuixSegmentedControl(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    colors: AppSegmentedControlColors,
    pillCornerRadius: Dp,
    modifier: Modifier,
    onSelectionChange: (T) -> Unit,
) {
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val tabColors = resolveAppMiuixSegmentedColors(colors)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .adaptiveSquircleBackground(
                color = colors.outerContainerColor,
                cornerRadius = pillCornerRadius,
            )
            .padding(4.dp),
    ) {
        TabRow(
            tabs = options.map { it.label },
            selectedTabIndex = selectedIndex,
            onTabSelected = { index ->
                if (enabled) options.getOrNull(index)?.let { onSelectionChange(it.value) }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TabRowDefaults.tabRowColors(
                backgroundColor = tabColors.backgroundColor,
                contentColor = tabColors.contentColor,
                selectedBackgroundColor = tabColors.selectedBackgroundColor,
                selectedContentColor = tabColors.selectedContentColor,
            ),
            height = 40.dp,
            cornerRadius = pillCornerRadius,
            itemSpacing = 4.dp,
            // 与 AppMiuixTabRow 一致：交给 Miuix 按容器宽度均分，避免默认 minWidth=76dp
            // 在选项较多时压缩文字。
            minWidth = 0.dp,
            maxWidth = Dp.Infinity,
        )
    }
}

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
        // 一致，避免固定 48dp 造成文字截断与左侧堆叠；scrollable（如时间表/分类）：
        // minTabWidth 兜底保证可读，maxWidth 放开允许内容扩展。
        minWidth = if (scrollable) minTabWidth else 0.dp,
        maxWidth = Dp.Infinity,
        height = 48.dp,
        cornerRadius = pillCornerRadius,
        itemSpacing = 8.dp,
    )
}
