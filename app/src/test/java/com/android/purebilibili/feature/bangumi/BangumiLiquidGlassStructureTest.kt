package com.android.purebilibili.feature.bangumi

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BangumiLiquidGlassStructureTest {

    @Test
    fun `bangumi hub uses adaptive native controls and independent poster ratio`() {
        val screenSource = sourceOf("BangumiScreen.kt")
        val contentSource = sourceOf("BangumiHubContent.kt")

        assertTrue(screenSource.contains("AppNativeTabRow("))
        assertTrue(contentSource.contains("AppNativeTabRow("))
        assertTrue(contentSource.contains("AdaptivePullToRefreshBox("))
        assertTrue(contentSource.contains("AnimatedContent("))
        assertTrue(contentSource.contains("slideInHorizontally("))
        assertTrue(contentSource.contains("slideOutHorizontally("))
        assertTrue(contentSource.contains("BANGUMI_POSTER_ASPECT_RATIO = 0.75f"))
        assertTrue(!screenSource.contains("BottomBarLiquidSegmentedControl"))
        assertTrue(!contentSource.contains("BottomBarLiquidSegmentedControl"))
        assertTrue(!contentSource.contains("HomeFeedCardStyle"))
        assertTrue(!contentSource.contains("SettingsManager"))
        assertTrue(!screenSource.contains("TopAppBarScrollBehavior"))
    }

    private fun sourceOf(path: String): String =
        File("src/main/java/com/android/purebilibili/feature/bangumi/$path").readText()
}
