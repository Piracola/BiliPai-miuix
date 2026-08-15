package com.android.purebilibili.feature.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.AppTopBarStyle
import com.android.purebilibili.core.ui.components.AppPreferenceGroupPresentation
import com.android.purebilibili.core.ui.components.AppPreferenceIconTreatment

/**
 * 设置页分隔策略（Miuix 官方——主要依靠组和行距，组内不画分隔线）。
 */
enum class SettingsDividerMode {
    GROUP_SPACING_ONLY,
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
 * 设置页 Miuix 视觉合同。
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
 * 按 Miuix 主题解析设置页视觉合同：
 * Miuix Card 分组、`FILLED` 彩色 squircle 图标、居中标题、无组内分隔。
 */
fun resolveSettingsVisualPolicy(): SettingsVisualPolicy = SettingsVisualPolicy(
    groupPresentation = AppPreferenceGroupPresentation.CARD,
    iconTreatment = AppPreferenceIconTreatment.FILLED,
    topBarStyle = AppTopBarStyle.CENTERED,
    dividerMode = SettingsDividerMode.GROUP_SPACING_ONLY,
    contentWidth = SettingsContentWidth.LIMITED,
    preferenceRowStyle = AppListItemStyle.NATIVE,
)
