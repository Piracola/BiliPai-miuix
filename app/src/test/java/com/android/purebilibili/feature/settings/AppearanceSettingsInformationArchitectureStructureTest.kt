package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * P3 外观页试点：五组信息架构顺序、标题不再重复当前值、说明卡降级（D3）、
 * 恢复主题推荐只清视觉覆盖（D2）、LazyColumn item 稳定 key 化。
 */
class AppearanceSettingsInformationArchitectureStructureTest {

    private val appearanceScreen = "app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt"

    @Test
    fun fiveGroupArchitecture_isInTargetOrder() {
        val source = loadSource(appearanceScreen)
        val expectedOrder = listOf(
            "界面与明暗",
            "颜色",
            "表面与效果",
            "文字与显示",
            "高级外观覆盖",
            "启动画面",
            "开屏与图标",
        )
        val positions = expectedOrder.map { title ->
            source.indexOf("AppPreferenceSectionTitle(\"$title\")")
                .also { index -> assertTrue(index >= 0, "Missing section title: $title") }
        }
        assertTrue(
            positions.zipWithNext().all { (a, b) -> a < b },
            "Section titles out of target order: $positions",
        )
    }

    @Test
    fun everyGroupItem_usesStableLazyColumnKey() {
        val source = loadSource(appearanceScreen)
        val groupKeyNames = listOf(
            "UI_AND_DARK",
            "COLOR",
            "SURFACE_AND_EFFECTS",
            "TEXT_AND_DISPLAY",
            "ADVANCED_OVERRIDES",
            "SPLASH",
            "PERSONALIZATION",
            "HOME_OVERVIEW",
        )
        groupKeyNames.forEach { name ->
            assertTrue(
                source.contains("item(key = AppearanceSettingsGroupKeys.$name)"),
                "Group item missing stable key: $name",
            )
        }
        listOf(
            "appearance_group_ui_dark_title",
            "appearance_group_color_title",
            "appearance_group_surface_effects_title",
            "appearance_group_text_display_title",
            "appearance_group_advanced_overrides_title",
            "appearance_group_splash_title",
            "appearance_group_personalization_title",
            "appearance_group_home_overview_title",
        ).forEach { titleKey ->
            assertTrue(
                source.contains("item(key = \"$titleKey\")"),
                "Section title item missing stable key: $titleKey",
            )
        }
    }

    @Test
    fun titles_noLongerConcatenateCurrentValues() {
        val source = loadSource(appearanceScreen)
        // 标题只写任务名；当前值由 trailing/选中状态/选项弹层呈现一次。
        listOf(
            "title = \"${'$'}{uiPresetTitle}：",
            "title = \"${'$'}{themeModeTitle}：",
            "title = \"${'$'}{darkThemeStyleTitle}：",
            "title = \"${'$'}{appLanguageTitle}：",
            "title = \"MD3 颜色来源：",
            "title = \"主题色：",
            "title = \"字体大小：",
            "title = \"界面缩放：",
            "title = \"显示缩放：",
            "title = \"列表顶部栏：",
            "title = \"卡片封面比例：",
            "title = \"首页视频时长：",
        ).forEach { pattern ->
            assertFalse(source.contains(pattern), "Title still concatenates current value: $pattern")
        }
    }

    @Test
    fun descriptionCard_isDemotedPerDecisionD3() {
        val source = loadSource(appearanceScreen)
        // 说明卡已删除，关键信息降级为组内小字说明。
        assertFalse(source.contains("AppearanceUiPresetDescriptionCard("))
        assertTrue(source.contains("uiPresetDescription.summary"))
    }

    @Test
    fun restoreThemeRecommendation_onlyClearsVisualOverridesPerDecisionD2() {
        val source = loadSource(appearanceScreen)
        assertTrue(source.contains("title = \"恢复主题推荐\""))
        assertTrue(source.contains("setAppIconStyle(AppIconStyle.AUTO)"))
        assertTrue(source.contains("setAppListItemStyle(AppListItemStyle.AUTO)"))
        assertTrue(source.contains("AppSingleChoicePresentation.WINDOW_POPUP"))
        assertTrue(source.contains("SettingsManager.setThemeRoleOverrides(context, ThemeRoleOverrides())"))
        // 恢复入口只在存在覆盖时可见；不清除业务偏好（主题/明暗/字体）。
        assertTrue(source.contains("hasAppearanceOverrides"))
        assertFalse(source.contains("setThemeMode("))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        return listOf(File(path), File(normalizedPath), File("../$path"))
            .firstOrNull(File::exists)
            ?.readText()
            ?.replace("\r\n", "\n")
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
