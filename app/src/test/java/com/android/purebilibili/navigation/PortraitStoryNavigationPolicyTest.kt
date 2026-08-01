package com.android.purebilibili.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PortraitStoryNavigationPolicyTest {

    @Test
    fun resolveSeed_returnsPortraitStoryWhenDirectEntryEnabledForVerticalVideo() {
        val seed = resolvePortraitStoryNavigationSeed(
            directPortraitStoryEntry = true,
            isVerticalVideo = true,
            startAudio = false,
            bvid = "BV1portrait",
            cid = 66L,
            coverUrl = "https://img.test.com/portrait.jpg",
        )

        assertEquals(
            PortraitStoryNavigationSeed(
                bvid = "BV1portrait",
                cid = 66L,
                coverUrl = "https://img.test.com/portrait.jpg"
            ),
            seed
        )
    }

    @Test
    fun resolveSeed_usesStoryIndependentlyFromPageTransitionStyle() {
        val seed = resolvePortraitStoryNavigationSeed(
            directPortraitStoryEntry = true,
            isVerticalVideo = true,
            startAudio = false,
            bvid = "BV1portrait",
        )

        assertEquals("BV1portrait", seed?.bvid)
    }

    @Test
    fun resolveSeed_returnsNullWhenDirectEntryDisabled() {
        val seed = resolvePortraitStoryNavigationSeed(
            directPortraitStoryEntry = false,
            isVerticalVideo = true,
            startAudio = false,
            bvid = "BV1portrait"
        )

        assertNull(seed)
    }

    @Test
    fun resolveSeed_returnsNullForAudioPlayback() {
        val seed = resolvePortraitStoryNavigationSeed(
            directPortraitStoryEntry = true,
            isVerticalVideo = true,
            startAudio = true,
            bvid = "BV1audio"
        )

        assertNull(seed)
    }

    @Test
    fun resolveSeed_returnsNullForHorizontalVideo() {
        val seed = resolvePortraitStoryNavigationSeed(
            directPortraitStoryEntry = true,
            isVerticalVideo = false,
            startAudio = false,
            bvid = "BV1landscape"
        )

        assertNull(seed)
    }

}
