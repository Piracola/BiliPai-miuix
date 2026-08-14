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
    fun `bottom bar keeps Material pairs only for explicit MD3 style`() {
        val source = sourceText("BottomBar.kt")

        assertTrue(source.contains("enum class BottomNavItem"))
        assertTrue(source.contains("internal fun resolveMaterialBottomBarIcon("))
        assertTrue(source.contains("if (selected) Icons.Filled.Home else Icons.Outlined.Home"))
        assertFalse(source.contains("CupertinoIcons"))
        assertFalse(source.contains("val selectedIcon:"))
        assertFalse(source.contains("val unselectedIcon:"))
    }

    @Test
    fun `miuix auto floating bottom bar uses native Miuix icon pairs`() {
        val source = sourceText("BottomBar.kt")
        val floatingSource = sourceText("FloatingBottomBar.kt")
        val iconPolicySource = sourceText("HomeNavigationIconPolicy.kt")

        assertTrue(source.contains("SharedFloatingBottomBarIconStyle.MIUIX"))
        assertTrue(source.contains("BottomBarBlendedMiuixIcon("))
        assertTrue(source.contains("resolveHomeNavigationBarIcon(item, selected = false)"))
        assertTrue(source.contains("resolveHomeNavigationBarIcon(item, selected = true)"))
        assertTrue(source.contains("resolveMiuixPreferredHomeNavigationIcon(tabId = \"PARTITION\")"))
        assertFalse(source.contains("SharedFloatingBottomBarIconStyle.CUPERTINO"))
        assertFalse(source.contains("BottomBarBlendedCupertinoIcon("))
        assertTrue(source.contains("LocalFloatingBottomBarActiveContent.current"))
        assertTrue(source.contains("MiuixIcons.Search"))
        assertTrue(source.contains("resolveHomeNavigationBarIcon("))
        assertTrue(floatingSource.contains("LocalFloatingBottomBarActiveContent provides true"))
        assertTrue(
            iconPolicySource.contains(
                "HomeNavigationIconRole.PLUGINS -> if (selected) MiuixIcons.FolderFill else MiuixIcons.Folder"
            )
        )
        assertTrue(iconPolicySource.contains("R.drawable.ic_home_nav_dynamic_filled"))
        assertTrue(iconPolicySource.contains("R.drawable.ic_home_nav_live_filled"))
        assertTrue(iconPolicySource.contains("R.drawable.ic_home_nav_game_filled"))
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
