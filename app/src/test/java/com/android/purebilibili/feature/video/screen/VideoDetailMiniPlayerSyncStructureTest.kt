package com.android.purebilibili.feature.video.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailMiniPlayerSyncStructureTest {

    @Test
    fun videoInfoSyncHappensBeforeBackgroundUiStateCache() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
        val miniPlayerSyncBlock = source
            .substringAfter("val shouldCacheMiniPlayer = lastCachedMiniPlayerBvid != currentBvid")
            .substringBefore("} else if (miniPlayerManager == null)")

        val setVideoInfoIndex = miniPlayerSyncBlock.indexOf("miniPlayerManager.setVideoInfo(")
        val backgroundLaunchIndex = miniPlayerSyncBlock.indexOf("launch(Dispatchers.Default)")
        val cacheUiStateIndex = miniPlayerSyncBlock.indexOf("miniPlayerManager.cacheUiState(success)")

        assertTrue(setVideoInfoIndex >= 0)
        assertTrue(backgroundLaunchIndex >= 0)
        assertTrue(cacheUiStateIndex >= 0)
        assertTrue(setVideoInfoIndex < backgroundLaunchIndex)
        assertTrue(backgroundLaunchIndex < cacheUiStateIndex)
        assertFalse(miniPlayerSyncBlock.contains("withContext(Dispatchers.Main)"))
    }

    @Test
    fun pageStackOnlyKeepsTheTopDetailPlaybackSessionActive() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
        val playerStateBlock = source
            .substringAfter("val playerState = rememberVideoPlayerState(")
            .substringBefore("val shouldKeepVideoScreenAwake")
        val syncEffect = source
            .substringAfter("LaunchedEffect(uiState, isVisible)")
            .substringBefore("//  弹幕加载逻辑已移至 VideoPlayerState")

        assertTrue(playerStateBlock.contains("cid = playbackTargetCid"))
        assertTrue(source.contains("currentBvidCid"))
        assertTrue(source.contains("requestedCid = playbackTargetCid"))
        assertTrue(playerStateBlock.contains("playbackSessionActive = isVisible"))
        assertFalse(source.contains("shouldKeepPlaybackSessionActiveForSharedReturnMorph("))
        assertTrue(syncEffect.contains("miniPlayerManager != null && shouldCacheMiniPlayer && isVisible"))
    }

    private fun loadSource(path: String): String {
        val candidates = listOf(
            File(path),
            File("app", path.removePrefix("app/")),
            File(path.removePrefix("app/")),
            File("..", path)
        )
        return candidates.first { it.exists() }.readText()
    }
}
