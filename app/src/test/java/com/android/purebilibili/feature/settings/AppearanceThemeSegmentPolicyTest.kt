package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.AppListItemStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceThemeSegmentPolicyTest {

    @Test
    fun `resolveThemeModeSegmentOptions should keep expected order and use provided labels`() {
        val options = resolveThemeModeSegmentOptions(
            followSystemLabel = "Follow System",
            lightLabel = "Light",
            darkLabel = "Dark"
        )

        assertEquals(3, options.size)
        assertEquals(AppThemeMode.FOLLOW_SYSTEM, options[0].value)
        assertEquals("Follow System", options[0].label)
        assertEquals(AppThemeMode.LIGHT, options[1].value)
        assertEquals("Light", options[1].label)
        assertEquals(AppThemeMode.DARK, options[2].value)
        assertEquals("Dark", options[2].label)
    }

    @Test
    fun `resolveDarkThemeStyleSegmentOptions should keep expected order and use provided labels`() {
        val options = resolveDarkThemeStyleSegmentOptions(
            defaultLabel = "Standard Black",
            amoledLabel = "AMOLED Black"
        )

        assertEquals(2, options.size)
        assertEquals(DarkThemeStyle.DEFAULT, options[0].value)
        assertEquals("Standard Black", options[0].label)
        assertEquals(DarkThemeStyle.AMOLED, options[1].value)
        assertEquals("AMOLED Black", options[1].label)
    }

    @Test
    fun `resolveAppListItemStyleOptions exposes auto plus custom and native`() {
        val options = resolveAppListItemStyleOptions()

        assertEquals(3, options.size)
        assertEquals(AppListItemStyle.AUTO, options[0].value)
        assertEquals("跟随预设", options[0].label)
        assertEquals(AppListItemStyle.CUSTOM, options[1].value)
        assertEquals("自定义条目", options[1].label)
        assertEquals(AppListItemStyle.NATIVE, options[2].value)
        assertEquals("原生组件", options[2].label)
    }

    @Test
    fun `resolveAppLanguageSegmentOptions keeps four compact language slots`() {
        val options = resolveAppLanguageSegmentOptions(
            followSystemLabel = "系统",
            simplifiedChineseLabel = "简体",
            traditionalChineseLabel = "繁體",
            englishLabel = "EN"
        )

        assertEquals(4, options.size)
        assertEquals(listOf("系统", "简体", "繁體", "EN"), options.map { it.label })
        assertTrue(options.all { it.label.length <= 2 })
    }
}
