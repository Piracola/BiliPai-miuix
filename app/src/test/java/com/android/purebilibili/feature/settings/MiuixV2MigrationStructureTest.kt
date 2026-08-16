package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiuixV2MigrationStructureTest {

    @Test
    fun webDavBackupScreen_usesSettingsPageScaffold_notLargeTitleBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupScreen.kt")
        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("iOSLargeTitleBar("))
        assertFalse(source.contains("globalWallpaperAwareBackground("))
    }

    @Test
    fun settingsShareScreen_usesSettingsPageScaffold_notLargeTitleBar() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareScreen.kt")
        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("iOSLargeTitleBar("))
    }

    @Test
    fun iosSectionTitle_usesMiuixSmallTitle() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
        assertTrue(source.contains("SmallTitle("))
    }

    @Test
    fun appAlertDialog_routesToWindowDialog() {
        // 单主题迁移：对话框统一路由到窗口级 Dialog；renderer 枚举已随收敛删除。
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AdaptiveDialogComponents.kt")
        assertTrue(source.contains("Dialog("))
        assertFalse(source.contains("AppAlertDialogRenderer"))
    }

    @Test
    fun appSurfaceTokens_exposesMiuixSemanticColors() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AppSurfaceTokens.kt")
        assertTrue(source.contains("fun onSurfaceVariantSummary()"))
        assertTrue(source.contains("fun onSurfaceVariantActions()"))
        assertTrue(source.contains("MiuixTheme.colorScheme.onSurfaceVariantSummary"))
    }

    @Test
    fun gradleCatalog_pinsMiuixVersionTo093Snapshot() {
        val source = loadSource("gradle/libs.versions.toml")
        assertTrue(source.contains("miuix = \"0.9.3-a370b370-SNAPSHOT\""))
    }

    @Test
    fun gradleCatalog_includesMiuixShaderArtifact() {
        val source = loadSource("gradle/libs.versions.toml")
        assertTrue(source.contains("miuix-shader-android"))
    }

    @Test
    fun gradleCatalog_includesMiuixSquircleArtifact() {
        val source = loadSource("gradle/libs.versions.toml")
        assertTrue(source.contains("miuix-squircle-android"))
    }

    @Test
    fun gradleCatalog_includesMiuixIconsArtifact() {
        val source = loadSource("gradle/libs.versions.toml")
        assertTrue(source.contains("miuix-icons-android"))
    }

    @Test
    fun featureLayer_avoidsDirectMiuixThemeColorSchemeReads() {
        val allowed = setOf(
            "app/src/main/java/com/android/purebilibili/core/theme/Theme.kt",
            "app/src/main/java/com/android/purebilibili/core/ui/AppSurfaceTokens.kt",
            "design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt"
        )
        val offenders = listOf(
            "app/src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt",
            "app/src/main/java/com/android/purebilibili/feature/message/InboxScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/message/feed/MessageFeedCommon.kt",
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/VideoSettingsPanel.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/AppSegmentedControl.kt"
        ).filter { path ->
            loadSource(path).contains("MiuixTheme.colorScheme")
        }
        assertTrue(
            offenders.isEmpty(),
            "Direct MiuixTheme.colorScheme reads should route through AppSurfaceTokens:\n" +
                offenders.joinToString("\n")
        )
    }

    @Test
    fun buildGradle_includesMiuixSquircleArtifact() {
        val source = loadSource("gradle/libs.versions.toml")
        assertTrue(source.contains("miuix-squircle-android"))
    }

    @Test
    fun buildGradle_includesMiuixIconsArtifact() {
        val source = loadSource("gradle/libs.versions.toml")
        assertTrue(source.contains("miuix-icons-android"))
    }

    @Test
    fun md3SegmentedControl_routesToMiuixTabRow() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/renderer/miuix/AppMiuixSegmentedControl.kt")
        val componentSource = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/components/AppSegmentedControl.kt")
        // 单主题政策：分段控件统一走 Miuix TabRow；renderer/chrome 枚举已随收敛删除。
        assertTrue(componentSource.contains("AppNativeTabRow("))
        assertFalse(componentSource.contains("resolveAppSegmentedRenderer()"))
        assertFalse(componentSource.contains("AppMiuixSegmentedControl("))
        assertTrue(source.contains("TabRow("))
    }

    @Test
    fun adaptivePullToRefreshBox_routesToMiuixPullToRefresh() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AdaptivePullToRefreshBox.kt")
        assertTrue(source.contains("MiuixPullToRefresh("))
        assertTrue(source.contains("indicatorTopInset"))
        assertTrue(source.contains("mergedContentPadding"))
    }

    @Test
    fun homeScreen_usesAdaptivePullToRefreshBox() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        assertTrue(source.contains("AdaptivePullToRefreshBox("))
        assertFalse(source.contains("ComfortablePullToRefreshBox("))
    }

    @Test
    fun ioSearchBar_miuixBranchUsesOfficialInputField() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
        assertTrue(source.contains("InputField("))
        assertTrue(source.contains("shouldUseNativeMiuixSearchBar("))
    }

    @Test
    fun iosClickableItem_routesThroughAdaptiveListItemPolicy() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
        assertTrue(source.contains("resolveAppClickableItemRenderer("))
        assertTrue(source.contains("AppClickableItemRenderer.MIUIX_ARROW"))
        assertTrue(source.contains("MiuixSliderPreference("))
    }

    @Test
    fun themeController_remembersUserThemeInputsWithMiuixBridgeColors() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/core/theme/Theme.kt")
        assertTrue(source.contains("ThemeController("))
        assertTrue(source.contains("customPrimaryColor,"))
        assertTrue(source.contains("amoledDarkTheme,"))
        assertTrue(source.contains("resolveMiuixColorsFromMaterialBridge("))
    }

    @Test
    fun miuixDockedBottomBar_routesStandardItemsThroughPlatformNavigationFacade() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")
        assertTrue(source.contains("AppPlatformNavigationBarItem("))
        assertTrue(source.contains("shouldUseMiuixOfficialNavigationBarItem("))
    }

    @Test
    fun searchTopBar_usesNeutralSearchField() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt")
        // 迁移后的搜索页：共享 BasicTextField 实现 + 中性 App* 组件，
        // 不再使用 AppSearchField，也不再有原生 chrome 分发（该组件仍服务于其余列表页）。
        assertTrue(source.contains("SearchTopBarInputField("))
        assertTrue(source.contains("AppIconButton("))
        assertFalse(source.contains("resolveSearchNativeChrome("))
        assertFalse(source.contains("SearchNativeChrome"))
    }

    @Test
    fun homePullRefreshPolicy_routesMiuixToNativeIndicator() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AppPullRefreshIndicator.kt")
        assertTrue(source.contains("AppPullRefreshIndicatorStyle.MIUIX_NATIVE"))
    }

    @Test
    fun adaptiveScaffold_miuixPathMountsPopupHostForOverlayDialogs() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AdaptiveChrome.kt")
        assertTrue(source.contains("MiuixPopupUtils.MiuixPopupHost()"))
        assertTrue(source.contains("popupHost ="))
    }

    @Test
    fun featureLayer_doesNotCallIosLargeTitleBarDirectly() {
        val featureRoot = File("app/src/main/java/com/android/purebilibili/feature")
        val offenders = featureRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("iOSLargeTitleBar(") }
            .map { it.path }
            .toList()
        assertTrue(
            offenders.isEmpty(),
            "iOSLargeTitleBar should remain iOS-only chrome; migrate feature screens to AdaptiveScaffold:\n" +
                offenders.joinToString("\n")
        )
    }

    @Test
    fun legacyIosLargeTitleBar_isRemovedAfterFeatureMigration() {
        val source = File("app/src/main/java/com/android/purebilibili/core/ui/iOSLargeTitleBar.kt")
        assertFalse(source.exists())
    }

    @Test
    fun md3SegmentedControl_usesMiuixTabRowRenderer() {
        val source = loadSource(
            "design-system/src/main/java/com/android/purebilibili/core/ui/renderer/miuix/AppMiuixSegmentedControl.kt"
        )
        assertTrue(source.contains("TabRow("))
        assertTrue(source.contains("Miuix"))
    }

    @Test
    fun appSurfaceTokens_exposesFullMiuixSemanticPalette() {
        val source = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AppSurfaceTokens.kt")
        listOf(
            "fun background()",
            "fun surface()",
            "fun surfaceContainer()",
            "fun surfaceContainerHigh()",
            "fun onSecondaryContainer()",
            "fun onSurfaceContainerHigh()",
            "fun onSurfaceContainerHighest()",
            "fun primary()",
            "fun resolveMiuixSemanticColor("
        ).forEach { token ->
            assertTrue(source.contains(token), "Missing token: $token")
        }
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File("../$path"),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
