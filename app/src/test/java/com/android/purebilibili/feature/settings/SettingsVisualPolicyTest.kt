package com.android.purebilibili.feature.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.AppTopBarStyle
import com.android.purebilibili.core.ui.components.AppPreferenceGroupPresentation
import com.android.purebilibili.core.ui.components.AppPreferenceIconTreatment
import com.android.purebilibili.core.ui.resolveAppListItemStyle
import com.android.purebilibili.core.ui.resolveAppListItemStylePreference
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsVisualPolicyTest {

    @Test
    fun miuix_resolvesOfficialGroupedSettingsLook() {
        val policy = resolveSettingsVisualPolicy(AppUiStyle.MIUIX)

        assertEquals(AppPreferenceGroupPresentation.CARD, policy.groupPresentation)
        assertEquals(AppPreferenceIconTreatment.FILLED, policy.iconTreatment)
        assertEquals(AppTopBarStyle.CENTERED, policy.topBarStyle)
        assertEquals(SettingsDividerMode.GROUP_SPACING_ONLY, policy.dividerMode)
        assertEquals(SettingsContentWidth.LIMITED, policy.contentWidth)
    }

    @Test
    fun material3_resolvesOfficialGroupedSettingsLook() {
        val policy = resolveSettingsVisualPolicy(AppUiStyle.MATERIAL3)

        // 分组呈现两主题均为 CARD，不能因兼容历史实现统一降级为 FLAT。
        assertEquals(AppPreferenceGroupPresentation.CARD, policy.groupPresentation)
        // 单色、无容器由 AppIconStyle.MD3_STANDARD 与 TONAL 组合生效。
        assertEquals(AppPreferenceIconTreatment.TONAL, policy.iconTreatment)
        assertEquals(AppTopBarStyle.SMALL, policy.topBarStyle)
        assertEquals(SettingsDividerMode.WEAK_GROUP_DIVIDERS, policy.dividerMode)
        assertEquals(SettingsContentWidth.LIMITED, policy.contentWidth)
    }

    @Test
    fun autoPreferenceRowStyle_resolvesToThemeOfficialNativeRowInSettingsScope() {
        assertEquals(
            AppListItemStyle.NATIVE,
            resolveSettingsPreferenceRowStyle(AppListItemStyle.AUTO, AppUiStyle.MATERIAL3),
        )
        assertEquals(
            AppListItemStyle.NATIVE,
            resolveSettingsPreferenceRowStyle(AppListItemStyle.AUTO, AppUiStyle.MIUIX),
        )
        listOf(AppUiStyle.MATERIAL3, AppUiStyle.MIUIX).forEach { uiStyle ->
            val policy = resolveSettingsVisualPolicy(uiStyle)
            assertEquals(AppListItemStyle.NATIVE, policy.preferenceRowStyle)
        }
    }

    @Test
    fun explicitPreferenceRowStyleOverride_survivesSettingsResolution() {
        listOf(AppUiStyle.MATERIAL3, AppUiStyle.MIUIX).forEach { uiStyle ->
            assertEquals(
                AppListItemStyle.CUSTOM,
                resolveSettingsVisualPolicy(
                    uiStyle,
                    SettingsVisualOverrides(preferenceRowStyle = AppListItemStyle.CUSTOM),
                ).preferenceRowStyle,
            )
            assertEquals(
                AppListItemStyle.NATIVE,
                resolveSettingsVisualPolicy(
                    uiStyle,
                    SettingsVisualOverrides(preferenceRowStyle = AppListItemStyle.NATIVE),
                ).preferenceRowStyle,
            )
        }
    }

    @Test
    fun invalidPersistedValue_fallsBackToAuto_thenResolvesToThemeOfficial() {
        // 持久化非法值 → AUTO → 设置页官方默认，整条回退链在设置范围内可用。
        val parsed = resolveAppListItemStylePreference("NOT_A_REAL_STYLE")
        assertEquals(AppListItemStyle.AUTO, parsed)
        assertEquals(
            AppListItemStyle.NATIVE,
            resolveSettingsPreferenceRowStyle(parsed, AppUiStyle.MATERIAL3),
        )
    }

    @Test
    fun settingsPolicy_leavesNonSettingsAutoResolutionUntouched() {
        // 回归锁：设置页官方默认仅限 Preference 范围，不得改动全局 AppListItemStyle.AUTO
        // 语义——设置页之外的业务列表（首页、分类、用户列表等）行为必须保持不变。
        assertEquals(
            AppListItemStyle.CUSTOM,
            resolveAppListItemStyle(AppListItemStyle.AUTO, AppUiStyle.MATERIAL3),
        )
        assertEquals(
            AppListItemStyle.NATIVE,
            resolveAppListItemStyle(AppListItemStyle.AUTO, AppUiStyle.MIUIX),
        )
    }

    @Test
    fun contentWidth_limitsExpandedSingleColumn() {
        assertEquals(Dp.Unspecified, SettingsContentWidth.FULL_WIDTH.maxContentWidthDp)
        assertEquals(800.dp, SettingsContentWidth.LIMITED.maxContentWidthDp)
        assertEquals(SettingsMaxContentWidthDp, 800.dp)
    }
}
