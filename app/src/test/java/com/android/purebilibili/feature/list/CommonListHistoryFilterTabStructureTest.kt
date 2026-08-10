package com.android.purebilibili.feature.list

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonListHistoryFilterTabStructureTest {
    @Test
    fun historyFilterRow_centersTabsAndUsesLiquidDockWhenGlobalGlassEnabled() {
        val source = loadSource("src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        val historyFilterSection = source
            .substringAfter("val historyFilterLabels = remember")
            .substringBefore("if (favoriteViewModel != null && subscribedFoldersState.isNotEmpty())")

        assertTrue(
            source.contains("resolveHistoryFilterTabChromeSpec"),
            "历史筛选行必须走统一的 tab chrome 策略"
        )
        assertTrue(
            historyFilterSection.contains("contentAlignment = Alignment.Center"),
            "历史筛选行必须居中布局"
        )
        assertTrue(
            historyFilterSection.contains("BottomBarLiquidSegmentedControl("),
            "开启全局液态玻璃时应复用底栏 dock 分段控件"
        )
        assertTrue(
            historyFilterSection.contains("backdrop = commonListChromeBackdrop"),
            "液态 dock 必须绑定与顶栏一致的 backdrop 源"
        )
        assertTrue(
            historyFilterSection.contains("forceLiquidChrome = homeSettings.androidNativeLiquidGlassEnabled"),
            "液态 dock 必须跟随全局液态玻璃开关"
        )
        assertTrue(
            historyFilterSection.contains("dragSelectionEnabled = historyFilterChrome.dragSelectionEnabled"),
            "个人列表筛选必须使用点击切换，避免与系统返回手势竞争"
        )
        assertTrue(
            historyFilterSection.contains("modifier = Modifier.fillMaxWidth()"),
            "液态 dock 应铺满可用宽度，避免固定 itemWidth 把指示器压扁"
        )
        assertTrue(
            historyFilterSection.contains("itemWidth = historyFilterChrome.itemWidthDp?.dp"),
            "液态 dock 不应再强制固定 tab 宽度"
        )
        val liquidDockBranch = historyFilterSection
            .substringAfter("if (historyFilterChrome.useLiquidDock) {")
            .substringBefore("} else {")
        assertFalse(
            liquidDockBranch.contains("LazyRow("),
            "液态 dock 分支不应继续依赖横向 FilterChip 列表"
        )
        val fallbackBranch = historyFilterSection.substringAfter("} else {")
        assertTrue(
            fallbackBranch.contains("FlowRow("),
            "非液态模式也应自动换行，不能把主筛选放进横向滚动列表"
        )
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath),
            File("app/$normalizedPath")
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
