package com.android.purebilibili.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiVideoSourcePolicyTest {

    @Test
    fun partitionAndRelatedShareTheRecordedCardMorphGate() {
        assertTrue(isRelatedVideoCardMorphSourceRoute("video/BV_PARENT"))
        assertFalse(isRelatedVideoCardMorphSourceRoute("partition"))
        assertFalse(isRelatedVideoCardMorphSourceRoute("home?category=1"))
        // 分区横卡与相关推荐横卡都已记录整卡几何，走同一套 Miuix 整卡 morph。
        listOf(
            "partition",
            "video/BV_PARENT",
            "home",
            "home?category=1",
            "search",
            "dynamic/123",
            "space/456",
            "watchlater",
            "favorite",
            "history",
        ).forEach { sourceRoute ->
            assertTrue(
                shouldUseMiuixVideoCardMorph(
                    cardTransitionEnabled = true,
                    reduceMotion = false,
                    sourceRoute = sourceRoute,
                    hasUsableSourceBounds = true,
                ),
                "Expected card morph for source=$sourceRoute",
            )
        }
        assertFalse(
            shouldUseMiuixVideoCardMorph(
                cardTransitionEnabled = true,
                reduceMotion = false,
                sourceRoute = "partition",
                hasUsableSourceBounds = false,
            )
        )
    }

    @Test
    fun searchVideoUsesSearchSourceRouteAndIndependentSourceKey() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV1",
            explicitSourceRoute = null,
            currentKey = BiliPaiNavKey.Search,
            previousSourceRoute = null
        )

        assertEquals("search", source.route)
        assertEquals("search:BV1", source.key)
    }

    @Test
    fun videoToVideoNavigationKeepsPreviousListSourceInsteadOfVideoRoute() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV2",
            explicitSourceRoute = null,
            currentKey = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            previousSourceRoute = "home"
        )

        assertEquals("home", source.route)
        assertEquals("home:BV2", source.key)
    }

    @Test
    fun relatedVideoNavigationUsesExplicitVideoSourceRouteForDetailToDetailSharedElement() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV2",
            explicitSourceRoute = "video/BV1",
            currentKey = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            previousSourceRoute = "home"
        )

        assertEquals("video/BV1", source.route)
        assertEquals("video/BV1:BV2", source.key)
    }

    @Test
    fun videoToVideoWithoutPreviousSourceFallsBackToParentVideoHost() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV2",
            explicitSourceRoute = null,
            currentKey = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = null),
            previousSourceRoute = null
        )

        assertEquals("video/BV1", source.route)
        assertEquals("video/BV1:BV2", source.key)
    }

    @Test
    fun explicitSourceRouteIsNormalizedBeforeKeyGeneration() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV1",
            explicitSourceRoute = "search?keyword=test",
            currentKey = BiliPaiNavKey.Home,
            previousSourceRoute = null
        )

        assertEquals("search", source.route)
        assertEquals("search:BV1", source.key)
    }

    @Test
    fun homeTopTabSourceRouteKeepsQueryForIndependentSourceKey() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV1",
            explicitSourceRoute = "home?category=POPULAR",
            currentKey = BiliPaiNavKey.Home,
            previousSourceRoute = null
        )

        assertEquals("home?category=POPULAR", source.route)
        assertEquals("home?category=POPULAR:BV1", source.key)
    }
}
