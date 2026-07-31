package com.android.purebilibili.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BiliPaiCardMorphDestinationPolicyTest {

    @Test
    fun videoDetailWithCardSourceIsMorphDestination() {
        val key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home")
        assertEquals("home", resolveCardMorphDestinationSourceRoute(key))
        assertTrue(isCardMorphDestinationNavKey(key))
    }

    @Test
    fun seededStoryIsNotSharedBoundsMorphDestination() {
        val key = BiliPaiNavKey.Story(
            seedBvid = "BV1",
            seedCover = "https://example.com/cover.jpg",
            sourceRoute = "home?category=RECOMMEND"
        )
        assertNull(resolveCardMorphDestinationSourceRoute(key))
        assertFalse(isCardMorphDestinationNavKey(key))
        assertFalse(shouldSilenceRouteTransitionForSeededStory(key))
    }

    @Test
    fun bottomTabStoryWithoutSeedIsNotMorphDestination() {
        val key = BiliPaiNavKey.Story()
        assertNull(resolveCardMorphDestinationSourceRoute(key))
        assertFalse(isCardMorphDestinationNavKey(key))
        assertFalse(shouldSilenceRouteTransitionForSeededStory(key))
    }

    @Test
    fun sharedReadyPushOnlyMatchesVideoDetail() {
        assertTrue(
            isSharedReadyCardMorphPush(
                key = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
                sourceMetadata = BiliPaiNavSourceMetadata(
                    sourceKey = "home:BV1",
                    sourceRoute = "home",
                    clickedBoundsRecorded = true,
                    cardFullyVisible = true
                )
            )
        )
        assertFalse(
            isSharedReadyCardMorphPush(
                key = BiliPaiNavKey.Story(seedBvid = "BV1", sourceRoute = "home"),
                sourceMetadata = BiliPaiNavSourceMetadata(
                    sourceKey = "home:BV1",
                    sourceRoute = "home",
                    clickedBoundsRecorded = true,
                    cardFullyVisible = true
                )
            )
        )
    }

    @Test
    fun storyPushUsesFallbackForwardAndPop() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = BiliPaiNavKey.Story(
                seedBvid = "BV1",
                sourceRoute = "home"
            ),
            cardTransitionEnabled = true,
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = true,
                cardFullyVisible = true
            )
        )
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transitions.pop)
    }

    @Test
    fun navDisplayPop_storyReturn_usesNoOp() {
        val transition = resolveBiliPaiNavDisplayPopRouteTransition(
            cardTransitionEnabled = true,
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceKey = "home:BV1",
                sourceRoute = "home",
                clickedBoundsRecorded = true,
                cardFullyVisible = true
            ),
            fromKey = BiliPaiNavKey.Story(seedBvid = "BV1", sourceRoute = "home"),
            toKey = BiliPaiNavKey.MainHost
        )
        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, transition)
    }

    @Test
    fun storySaveableStateKeyIgnoresSourceRouteAndUsesOpenId() {
        assertEquals(
            "story:seed:BV1:0:42",
            resolveNavigation3SaveableStateKey(
                BiliPaiNavKey.Story(seedBvid = "BV1", sourceRoute = "home", openId = 42L)
            )
        )
        assertNotEquals(
            resolveNavigation3SaveableStateKey(
                BiliPaiNavKey.Story(seedBvid = "BV1", openId = 1L)
            ),
            resolveNavigation3SaveableStateKey(
                BiliPaiNavKey.Story(seedBvid = "BV1", openId = 2L)
            )
        )
        assertEquals(
            "story:tab",
            resolveNavigation3SaveableStateKey(BiliPaiNavKey.Story())
        )
    }
}
