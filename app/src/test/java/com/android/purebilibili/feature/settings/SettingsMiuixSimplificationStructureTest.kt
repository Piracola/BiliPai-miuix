package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsMiuixSimplificationStructureTest {

    @Test
    fun `appearance settings drop ui style preset selection keeping miuix scaffold`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt")

        // 极简版：主题固定 Miuix，不再提供 MD3/Miuix 预设切换。
        assertFalse(source.contains("resolveThemeSelectionOptions("))
        assertFalse(source.contains("resolveAppearanceUiPresetDescription("))
        assertFalse(source.contains("onSelectionChange = viewModel::setThemeSelection"))
        assertFalse(source.contains("resolveAndroidNativeVariantSegmentOptions("))
        assertFalse(source.contains("viewModel.setUiPreset("))
        assertFalse(source.contains("viewModel.setAndroidNativeVariant("))
        assertFalse(source.contains("安卓原生液态玻璃"))
        assertFalse(source.contains("toggleAndroidNativeLiquidGlass("))
        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("MiuixScaffold("))
        assertFalse(source.contains("MiuixSmallTopAppBar("))
    }

    @Test
    fun `animation settings keep only haze blur toggles after liquid glass removal`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt")

        assertFalse(source.contains("previewLiquidGlassProgress"))
        assertFalse(source.contains("通透到磨砂"))
        assertFalse(source.contains("顶部标签栏液态玻璃"))
        assertFalse(source.contains("toggleTopBarLiquidGlass("))
        assertFalse(source.contains("首页搜索框液态玻璃"))
        assertFalse(source.contains("toggleHomeSearchLiquidGlass("))
        assertFalse(source.contains("底栏液态玻璃"))
        assertFalse(source.contains("title = \"安卓原生液态玻璃\""))
        assertFalse(source.contains("toggleAndroidNativeLiquidGlass("))
        assertTrue(source.contains("转场时模糊背景"))
        assertTrue(source.contains("toggleVideoTransitionRealtimeBlur("))
        assertTrue(source.contains("SettingsPageScaffold("))
    }

    @Test
    fun `animation glass section keeps only blur section title`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt")

        assertTrue(source.contains("AppPreferenceSectionTitle(\"玻璃效果\")"))
        assertFalse(source.contains("SettingsIconRole.TOP_DOCK_GLASS"))
        assertFalse(source.contains("SettingsIconRole.HOME_SEARCH_GLASS"))
        assertFalse(source.contains("SettingsIconRole.BOTTOM_BAR_GLASS"))
        assertTrue(source.contains("SettingsIconRole.TOP_BAR_BLUR"))
        assertTrue(source.contains("SettingsIconRole.BOTTOM_BAR_BLUR"))
    }

    @Test
    fun `settings groups avoid duplicate setting icons`() {
        val paths = listOf(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/PermissionSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/PlaybackSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupScreen.kt"
        )

        val duplicateIcons = paths.flatMap { path ->
            findDuplicateSettingIcons(path, loadSource(path))
        }

        assertTrue(
            duplicateIcons.isEmpty(),
            duplicateIcons.joinToString(separator = "\n")
        )
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText().replace("\r\n", "\n")
    }

    private fun findDuplicateSettingIcons(path: String, source: String): List<String> {
        val lines = source.lines()
        val duplicates = mutableListOf<String>()
        var inGroup = false
        var groupStartLine = 0
        var braceDepth = 0
        var pendingIcon: PendingIcon? = null
        val entries = mutableListOf<SettingIconEntry>()

        fun flushGroup() {
            entries
                .groupBy { it.icon }
                .filterValues { it.size > 1 }
                .forEach { (icon, repeatedEntries) ->
                    duplicates += "$path:$groupStartLine repeats $icon for ${
                        repeatedEntries.joinToString { "${it.title}@${it.lineNumber}" }
                    }"
                }
            entries.clear()
            pendingIcon = null
        }

        lines.forEachIndexed { index, line ->
            if (line.contains("IOSGroup") || line.contains("AppPreferenceGroup")) {
                inGroup = true
                groupStartLine = index + 1
                braceDepth = 0
                entries.clear()
                pendingIcon = null
            }

            if (inGroup) {
                braceDepth += line.count { it == '{' }
                braceDepth -= line.count { it == '}' }

                Regex("""icon\s*=\s*([^,]+),?""")
                    .find(line)
                    ?.let { match ->
                        pendingIcon = PendingIcon(
                            icon = match.groupValues[1].trim(),
                            lineNumber = index + 1
                        )
                    }

                Regex("""title\s*=\s*"([^"]+)"""")
                    .find(line)
                    ?.let { match ->
                        val icon = pendingIcon ?: return@let
                        entries += SettingIconEntry(
                            icon = icon.icon,
                            title = match.groupValues[1],
                            lineNumber = icon.lineNumber
                        )
                        pendingIcon = null
                    }

                if (braceDepth <= 0 && index + 1 > groupStartLine) {
                    flushGroup()
                    inGroup = false
                }
            }
        }

        return duplicates
    }

    private data class PendingIcon(
        val icon: String,
        val lineNumber: Int
    )

    private data class SettingIconEntry(
        val icon: String,
        val title: String,
        val lineNumber: Int
    )
}
