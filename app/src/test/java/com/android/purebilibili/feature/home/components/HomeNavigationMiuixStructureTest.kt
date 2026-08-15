package com.android.purebilibili.feature.home.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeNavigationMiuixStructureTest {

    @Test
    fun `home navigation runtime does not select Cupertino or Material icons`() {
        // 顶栏类别图标是刻意例外：Miuix 图标集（156 个）缺少推荐/关注/直播/番剧/游戏/
        // 知识/科技等类别图标，TopBar 类别解析器沿用 Material（与 BottomBar 同样的文档化
        // 例外）；因此严格检查只覆盖 HomeHeader 与导航图标策略，TopBar 仅禁止 Cupertino
        // 与 fallbackIconFamily 回退机制。
        val strictSources = listOf(
            "HomeHeader.kt",
            "HomeNavigationIconPolicy.kt",
        ).map(::sourceText)

        strictSources.forEach { source ->
            assertFalse(source.contains("CupertinoIcons"))
            assertFalse(source.contains("androidx.compose.material.icons"))
            assertFalse(source.contains("fallbackIconFamily"))
        }

        val topBar = sourceText("TopBar.kt")
        assertFalse(topBar.contains("CupertinoIcons"))
        assertFalse(topBar.contains("fallbackIconFamily"))
    }

    @Test
    fun `bottom bar renders a single Miuix glyph tinted by selection state`() {
        val source = sourceText("BottomBar.kt")

        assertTrue(source.contains("enum class BottomNavItem"))
        // 选中/未选中使用同一字形，颜色由内容色承载（Miuix 原生契约）。
        assertTrue(source.contains("internal fun resolveHomeNavigationBarIcon("))
        assertFalse(source.contains("resolveMaterialBottomBarIcon("))
        assertFalse(source.contains("SharedFloatingBottomBarIconStyle"))
        assertFalse(source.contains("BottomBarBlendedMiuixIcon("))
        assertFalse(source.contains("BottomBarBlendedMaterialIcon("))
        assertFalse(source.contains("CupertinoIcons"))
    }

    @Test
    fun `miuix floating bottom bar uses single Miuix glyphs with color states`() {
        val source = sourceText("BottomBar.kt")
        val floatingSource = sourceText("FloatingBottomBar.kt")
        val iconPolicySource = sourceText("HomeNavigationIconPolicy.kt")

        assertTrue(source.contains("resolveHomeNavigationBarIcon("))
        assertTrue(source.contains("resolveMiuixPreferredHomeNavigationIcon(tabId = \"PARTITION\")"))
        assertTrue(source.contains("MiuixIcons.Search"))
        assertTrue(source.contains("LocalFloatingBottomBarActiveContent.current"))
        assertFalse(source.contains("SharedFloatingBottomBarIconStyle"))
        assertFalse(source.contains("CupertinoIcons"))
        assertFalse(source.contains("BottomBarBlendedCupertinoIcon("))
        assertTrue(floatingSource.contains("LocalFloatingBottomBarActiveContent provides true"))
        // 同字形：Miuix 分支不再按 selected 切换 Medium 变体。
        assertTrue(
            iconPolicySource.contains(
                "HomeNavigationIconRole.HOME -> MiuixIcons.Home"
            )
        )
        assertTrue(iconPolicySource.contains("R.drawable.ic_home_nav_dynamic"))
        assertTrue(iconPolicySource.contains("R.drawable.ic_home_nav_live"))
        assertTrue(iconPolicySource.contains("R.drawable.ic_home_nav_game"))
        assertFalse(iconPolicySource.contains("ic_home_nav_dynamic_filled"))
        assertFalse(iconPolicySource.contains("ic_home_nav_live_filled"))
        assertFalse(iconPolicySource.contains("ic_home_nav_game_filled"))
    }

    @Test
    fun `home header actions use Miuix search settings and messages icons`() {
        val source = sourceText("HomeHeader.kt")

        assertTrue(source.contains("val searchIcon = MiuixIcons.Search"))
        assertTrue(source.contains("val settingsIcon = MiuixIcons.Settings"))
        assertTrue(source.contains("val inboxIcon = MiuixIcons.Messages"))
    }

    private fun sourceText(fileName: String): String = listOf(
        File("app/src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
        File("src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
    ).first { it.exists() }.readText()
}
