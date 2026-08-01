package com.android.purebilibili.feature.video.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailPageStackTransitionStructureTest {
    @Test
    fun `detail page API does not accept card transition state`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreen.kt"
        ).readText()

        assertFalse(source.contains("sourceRouteForSharedElement"))
        assertFalse(source.contains("keepLoadedContentForBackPreview"))
        assertFalse(source.contains("transitionEnabled"))
    }

    @Test
    fun `detail renderer never replaces the player with a return cover`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt"
        ).readText()

        assertFalse(source.contains("alpha = resolveVideoDetailReturnCoverAlpha("))
        assertFalse(source.contains("alpha = resolveVideoDetailReturnPlayerAlpha("))
        assertFalse(source.contains("deferPlaybackStop = detailShellSharedBoundsEnabled"))
    }

    @Test
    fun `programmatic page backs use the Navigation3 dispatcher`() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"
        ).readText()
        val displayHost = File(
            "src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"
        ).readText()

        assertTrue(source.contains("navigation3ProgrammaticBackDispatcher.dispatch()"))
        assertTrue(source.contains("onBack = { commitSystemBackAction() }"))
        assertFalse(displayHost.contains("onNativeVideoBackProgress"))
        assertFalse(displayHost.contains("onNativeVideoBackCancelled"))
    }

    @Test
    fun `video pop does not enter mini player before Navigation3 removes the detail`() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"
        ).readText()
        val systemBackBlock = source
            .substringAfter("AppSystemBackAction.NAVIGATE_UP -> {")
            .substringBefore("AppSystemBackAction.FINISH_ACTIVITY")

        assertFalse(systemBackBlock.contains("prepareVideoPlaybackForNavigationExit("))
        assertTrue(source.contains("onDispose {"))
        assertTrue(source.contains("prepareVideoPlaybackForNavigationExit(videoKey)"))
    }
}
