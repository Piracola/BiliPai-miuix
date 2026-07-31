package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File

class HomeHeroCarouselPolicyTest {

    @Test
    fun `carousel safely ignores stale pager index after feed shrinks`() {
        val items = listOf("a", "b", "c", "d", "e", "f")

        assertEquals(null, resolveHomeHeroCarouselItemOrNull(items, page = 6))
        assertEquals(
            "hero_6",
            resolveHomeHeroCarouselItemKey(items, page = 6) { it }
        )
    }

    @Test
    fun `carousel key uses item identity when pager index is valid`() {
        assertEquals(
            "BV1stable",
            resolveHomeHeroCarouselItemKey(listOf("BV1stable"), page = 0) { it }
        )
    }

    @Test
    fun `visible hero carousel reduces the reserved top gap without affecting other feeds`() {
        assertEquals(
            157f,
            resolveHomeFeedTopPaddingDp(
                reservedTopPaddingDp = 169f,
                showHeroCarousel = true
            ),
            0.001f
        )
        assertEquals(
            169f,
            resolveHomeFeedTopPaddingDp(
                reservedTopPaddingDp = 169f,
                showHeroCarousel = false
            ),
            0.001f
        )
    }

    @Test
    fun `carousel only shows on recommend page with items when enabled`() {
        assertTrue(
            shouldShowHomeHeroCarousel(
                enabled = true,
                category = HomeCategory.RECOMMEND,
                itemCount = 1
            )
        )
        assertFalse(
            shouldShowHomeHeroCarousel(
                enabled = false,
                category = HomeCategory.RECOMMEND,
                itemCount = 1
            )
        )
        assertFalse(
            shouldShowHomeHeroCarousel(
                enabled = true,
                category = HomeCategory.POPULAR,
                itemCount = 1
            )
        )
        assertFalse(
            shouldShowHomeHeroCarousel(
                enabled = true,
                category = HomeCategory.RECOMMEND,
                itemCount = 0
            )
        )
    }

    @Test
    fun `carousel uses bounded leading feed items`() {
        assertEquals(
            listOf(1, 2, 3),
            selectHomeHeroCarouselItems(listOf(1, 2, 3), maxItems = 8)
        )
        assertEquals(
            (1..8).toList(),
            selectHomeHeroCarouselItems((1..20).toList(), maxItems = 8)
        )
        assertEquals(
            emptyList(),
            selectHomeHeroCarouselItems((1..20).toList(), maxItems = 0)
        )
    }

    @Test
    fun `carousel feed removes visible hero items from regular grid`() {
        val items = listOf("a", "b", "c", "d")
        val carouselItems = listOf("a", "b")

        assertEquals(
            listOf("c", "d"),
            excludeHomeHeroCarouselItems(items, carouselItems) { it }
        )
    }

    @Test
    fun `carousel feed keeps regular grid untouched when carousel is empty`() {
        val items = listOf("a", "b", "c")

        assertEquals(
            items,
            excludeHomeHeroCarouselItems(items, emptyList()) { it }
        )
    }

    @Test
    fun `carousel uses no resting side peek so centered cover hides neighbors`() {
        assertEquals(0f, HOME_HERO_CAROUSEL_SIDE_PEEK_DP)
    }

    @Test
    fun `carousel uses adaptive aspect ratio for phone and tablet`() {
        assertEquals(16f / 9f, resolveHomeHeroCarouselAspectRatio(containerWidthDp = 393f), 0.001f)
        assertEquals(2.0f, resolveHomeHeroCarouselAspectRatio(containerWidthDp = 700f), 0.001f)
        assertEquals(21f / 9f, resolveHomeHeroCarouselAspectRatio(containerWidthDp = 900f), 0.001f)
    }

    @Test
    fun `carousel width is capped on large screens`() {
        assertEquals(393f, resolveHomeHeroCarouselWidthDp(containerWidthDp = 393f), 0.001f)
        assertEquals(HOME_HERO_CAROUSEL_MAX_WIDTH_DP, resolveHomeHeroCarouselWidthDp(containerWidthDp = 1200f), 0.001f)
    }

    @Test
    fun `carousel policy contains no visual card transform`() {
        val source = File("src/main/java/com/android/purebilibili/feature/home/HomeHeroCarouselPolicy.kt")
            .readText()

        assertFalse(source.contains("HomeHeroCarouselCardTransform"))
        assertFalse(source.contains("resolveHomeHeroCarouselCardTransform"))
    }

    @Test
    fun `carousel preview stays hidden until first frame is rendered`() {
        assertEquals(0f, resolveHomeHeroCarouselPreviewAlpha(hasRenderedFirstFrame = false))
        assertEquals(1f, resolveHomeHeroCarouselPreviewAlpha(hasRenderedFirstFrame = true))
    }
}
