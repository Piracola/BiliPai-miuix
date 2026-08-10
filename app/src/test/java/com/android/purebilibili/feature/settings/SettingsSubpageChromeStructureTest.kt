package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsSubpageChromeStructureTest {

    private val settingsPageScaffoldScreens = listOf(
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/PermissionSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/TipsSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/PlaybackSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/BottomBarSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/PluginsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/IconSettingsScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/BlockedListScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/JsonPluginEditorScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/screen/OpenSourceLicensesScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareScreen.kt",
        "app/src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupScreen.kt",
    )

    @Test
    fun settingsPageScaffoldScreens_useSharedScaffold() {
        settingsPageScaffoldScreens.forEach { path ->
            val source = loadSource(path)
            assertTrue(
                source.contains("SettingsPageScaffold("),
                "$path should use SettingsPageScaffold",
            )
            assertFalse(
                source.contains("AppScaffold("),
                "$path should not declare its own AppScaffold",
            )
        }
    }

    @Test
    fun settingsSubpageChromePolicy_isCentralized() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/SettingsSubpageChromePolicy.kt",
        )
        assertTrue(source.contains("AppSurfaceTokens.groupedListContainer()"))
        assertFalse(source.contains("cardContainer()"))
    }

    @Test
    fun permissionSettingsScreen_usesExternalScrollHostForScrollableContent() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/PermissionSettingsScreen.kt",
        )

        assertTrue(source.contains("scrollHost = SettingsPageScrollHost.External"))
        assertTrue(source.contains(".verticalScroll("))
    }

    @Test
    fun settingsScaffold_consumesVisualPolicyInsteadOfHardcodedValues() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsPageScaffold.kt",
        )

        assertTrue(source.contains("resolveSettingsVisualPolicy("))
        assertTrue(source.contains("LocalAppPreferenceIconTreatment provides visualPolicy.iconTreatment"))
        assertTrue(source.contains("LocalAppPreferenceGroupPresentation provides visualPolicy.groupPresentation"))
        assertTrue(source.contains("LocalAppListItemStyle provides visualPolicy.preferenceRowStyle"))
        assertTrue(source.contains("style = visualPolicy.topBarStyle"))

        // 历史无条件强制值必须消失，两主题呈现由 P1 策略决定。
        assertFalse(source.contains("LocalAppPreferenceIconTreatment provides AppPreferenceIconTreatment.FILLED"))
        assertFalse(source.contains("LocalAppPreferenceGroupPresentation provides AppPreferenceGroupPresentation.FLAT"))
    }

    @Test
    fun detailPaneSuppressesSecondTopBar_inSplitLayout() {
        val scaffold = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsPageScaffold.kt",
        )
        assertTrue(scaffold.contains("val LocalSettingsDetailPane = staticCompositionLocalOf { false }"))
        assertTrue(scaffold.contains("val inDetailPane = LocalSettingsDetailPane.current"))
        assertTrue(scaffold.contains("if (inDetailPane)"))

        val shell = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsTabletShell.kt",
        )
        assertTrue(shell.contains("LocalSettingsDetailPane provides true"))
        assertTrue(shell.contains("windowInsetsPadding(WindowInsets.statusBars)"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath),
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
