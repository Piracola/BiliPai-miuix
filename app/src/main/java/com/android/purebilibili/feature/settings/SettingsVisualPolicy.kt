package com.android.purebilibili.feature.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.AppTopBarStyle
import com.android.purebilibili.core.ui.components.AppPreferenceGroupPresentation
import com.android.purebilibili.core.ui.components.AppPreferenceIconTreatment

/**
 * 设置页分隔策略（见 03_THEMES 设置页视觉策略“分隔”合同）。
 * - [GROUP_SPACING_ONLY]：MIUIX 官方——主要依靠组和行距，组内不画分隔线。
 * - [WEAK_GROUP_DIVIDERS]：Material 3 官方——组内可用弱分隔，但不与大留白同时堆叠。
 */
enum class SettingsDividerMode {
    GROUP_SPACING_ONLY,
    WEAK_GROUP_DIVIDERS,
}

/**
 * 设置页内容宽度策略（大屏单列限宽，双栏由 [SettingsTabletShell] 独立处理）。
 */
enum class SettingsContentWidth {
    /** 不额外限宽，内容占满可用宽度（小屏单列默认语义）。 */
    FULL_WIDTH,

    /** 限制最大内容宽度（大屏单列，避免长行无限拉伸）。 */
    LIMITED,
}

/** 设置页大屏单列限宽值，与 App 现有 Expanded 内容限宽一致。 */
val SettingsMaxContentWidthDp: Dp = 800.dp

val SettingsContentWidth.maxContentWidthDp: Dp
    get() = when (this) {
        SettingsContentWidth.FULL_WIDTH -> Dp.Unspecified
        SettingsContentWidth.LIMITED -> SettingsMaxContentWidthDp
    }

/**
 * 用户显式高级外观覆盖（仅出现在“高级外观覆盖”分组，见决策记录 D2）。
 * 空值语义等价于 [AppListItemStyle.AUTO]——跟随主题官方默认。
 */
data class SettingsVisualOverrides(
    val preferenceRowStyle: AppListItemStyle = AppListItemStyle.AUTO,
)

/**
 * 设置页双主题视觉合同（03_THEMES 设置页视觉策略）。
 *
 * 业务页面只消费这份解析结果，不询问当前主题；渲染差异由
 * `AdaptivePreference*` 各自的主题 Renderer 承担。
 */
data class SettingsVisualPolicy(
    val groupPresentation: AppPreferenceGroupPresentation,
    val iconTreatment: AppPreferenceIconTreatment,
    val topBarStyle: AppTopBarStyle,
    val dividerMode: SettingsDividerMode,
    val contentWidth: SettingsContentWidth,
    val preferenceRowStyle: AppListItemStyle,
)

/**
 * 按主题解析设置页官方视觉合同。
 *
 * - MIUIX：Miuix Card 分组、`FILLED` 彩色 squircle 图标、居中标题、无组内分隔。
 * - Material 3：分组 surface、单色无容器图标（配合 `AppIconStyle.MD3_STANDARD` 生效）、
 *   Material TopAppBar、组内可用弱分隔。
 * - 两主题分组呈现均为 `CARD`：不能因兼容历史实现统一降级为 `FLAT`。
 */
fun resolveSettingsVisualPolicy(
    uiStyle: AppUiStyle,
    overrides: SettingsVisualOverrides = SettingsVisualOverrides(),
): SettingsVisualPolicy = when (uiStyle) {
    AppUiStyle.MIUIX -> SettingsVisualPolicy(
        groupPresentation = AppPreferenceGroupPresentation.CARD,
        iconTreatment = AppPreferenceIconTreatment.FILLED,
        topBarStyle = AppTopBarStyle.CENTERED,
        dividerMode = SettingsDividerMode.GROUP_SPACING_ONLY,
        contentWidth = SettingsContentWidth.LIMITED,
        preferenceRowStyle = resolveSettingsPreferenceRowStyle(overrides.preferenceRowStyle, uiStyle),
    )

    AppUiStyle.MATERIAL3 -> SettingsVisualPolicy(
        groupPresentation = AppPreferenceGroupPresentation.CARD,
        iconTreatment = AppPreferenceIconTreatment.TONAL,
        topBarStyle = AppTopBarStyle.SMALL,
        dividerMode = SettingsDividerMode.WEAK_GROUP_DIVIDERS,
        contentWidth = SettingsContentWidth.LIMITED,
        preferenceRowStyle = resolveSettingsPreferenceRowStyle(overrides.preferenceRowStyle, uiStyle),
    )
}

/**
 * 设置页范围内的 Preference 行呈现解析：`AUTO/跟随预设` 解析为主题官方原生条目
 * （MIUIX → Miuix 原生条目，Material 3 → M3 ListItem/Switch/Slider 语义渲染）。
 *
 * 与全局 [com.android.purebilibili.core.ui.resolveAppListItemStyle] 刻意隔离：
 * 全局 `AUTO` 语义保持不变（MATERIAL3 → CUSTOM），官方默认只限制在设置页
 * Preference 范围，不机械修改设置页之外的所有列表。
 */
fun resolveSettingsPreferenceRowStyle(
    style: AppListItemStyle,
    uiStyle: AppUiStyle,
): AppListItemStyle = when (style) {
    AppListItemStyle.AUTO -> when (uiStyle) {
        AppUiStyle.MATERIAL3 -> AppListItemStyle.NATIVE
        AppUiStyle.MIUIX -> AppListItemStyle.NATIVE
    }

    else -> style
}
