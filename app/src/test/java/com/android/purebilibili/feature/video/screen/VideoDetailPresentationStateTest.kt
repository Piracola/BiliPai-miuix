package com.android.purebilibili.feature.video.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoDetailPresentationStateTest {

    @Test
    fun switchingVideoUpdatesTargetAndResetsSelectedTab() {
        val state = VideoDetailPresentationState.create(
            initialBvid = "BV1",
            initialCid = 11L,
            initialPortraitFullscreen = false,
            initialPipMode = false,
        )

        state.selectTab(1)
        state.switchVideo("BV2", 22L)

        assertEquals("BV2", state.currentBvidState.value)
        assertEquals(22L, state.currentCidState.longValue)
        assertEquals(0, state.selectedTabIndexState.intValue)
    }

    @Test
    fun namedSessionOperationsOwnTransientPresentationFlags() {
        val state = VideoDetailPresentationState.create(
            initialBvid = "BV1",
            initialCid = 0L,
            initialPortraitFullscreen = false,
            initialPipMode = false,
        )

        state.setPortraitFullscreen(true)
        state.syncPipMode(true)
        state.markNavigatingToVideo()
        state.markNavigatingToMiniMode()

        assertTrue(state.portraitFullscreenState.value)
        assertTrue(state.pipModeState.value)
        assertTrue(state.navigatingToVideoState.value)
        assertTrue(state.navigatingToMiniModeState.value)
    }

}
