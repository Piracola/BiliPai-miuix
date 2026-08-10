package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalNavigationBackHandlerStructureTest {

    @Test
    fun fullscreenPlayersUseNavigationEventBackHandling() {
        val paths = listOf(
            "feature/bangumi/BangumiPlayerScreen.kt",
            "feature/live/LivePlayerScreen.kt",
            "feature/download/OfflineVideoPlayerScreen.kt",
        )

        paths.forEach { relativePath ->
            val source = File("src/main/java/com/android/purebilibili/$relativePath").readText()
            assertTrue(source.contains("LocalNavigationBackHandler("), relativePath)
            assertFalse(source.contains("import androidx.activity.compose.BackHandler"), relativePath)
        }
    }

    @Test
    fun localNavigationBackHandlerRespectsGlobalPredictivePreference() {
        val source = File(
            "src/main/java/com/android/purebilibili/core/ui/LocalNavigationBackHandler.kt"
        ).readText()

        // 跟手预览由 NavigationBackHandler 统一承担；enabled 由调用方按全局
        // 预测性返回偏好传入。
        assertTrue(source.contains("NavigationBackHandler("))
        assertTrue(source.contains("rememberNavigationEventState(NavigationEventInfo.None)"))
        assertTrue(source.contains("isBackEnabled = enabled"))
        assertTrue(source.contains("onBackCompleted = onBackCompleted"))
    }
}
