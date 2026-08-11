package com.android.purebilibili.feature.video.ui.components

import com.android.purebilibili.data.model.response.Owner
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.Stat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RelatedVideoItemPolicyTest {

    @Test
    fun `shared transition mode keeps related card scale stable`() {
        assertEquals(
            1f,
            resolveRelatedVideoCardPressScaleTarget(
                isPressed = true,
                transitionEnabled = true
            )
        )
    }

    @Test
    fun `normal mode also keeps related card scale stable`() {
        assertEquals(
            1f,
            resolveRelatedVideoCardPressScaleTarget(
                isPressed = true,
                transitionEnabled = false
            )
        )
        assertEquals(
            1f,
            resolveRelatedVideoCardPressScaleTarget(
                isPressed = false,
                transitionEnabled = false
            )
        )
    }

    @Test
    fun `cover crossfade is disabled in all modes for list stability`() {
        assertFalse(shouldEnableRelatedVideoCoverCrossfade(transitionEnabled = true))
        assertFalse(shouldEnableRelatedVideoCoverCrossfade(transitionEnabled = false))
    }

    @Test
    fun `related cards preserve the detail source route for detail to detail shared element`() {
        assertEquals("video", resolveRelatedVideoSharedElementSourceRoute(null))
        assertEquals("video", resolveRelatedVideoSharedElementSourceRoute(""))
        assertEquals("video/BV1", resolveRelatedVideoSharedElementSourceRoute("video/BV1?from=related"))
        assertEquals("home", resolveRelatedVideoSharedElementSourceRoute("home"))
        // Miuix source sessions remain host route + target bvid for morph matching.
    }

    @Test
    fun `related detail records a side by side Miuix card source`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt")
            .readText()

        assertTrue(source.contains("RELATED_VIDEO_CARD_COVER_ASPECT_RATIO"))
        assertTrue(source.contains("coverAspectRatio: Float = RELATED_VIDEO_CARD_COVER_ASPECT_RATIO"))
        assertTrue(source.contains("val coverWidth = 144.dp"))
        assertTrue(source.contains("val coverHeight = coverWidth / coverAspectRatio.coerceAtLeast(1f)"))
        assertTrue(source.contains("resolveHomeFeedCardLayout(homeFeedCardStyle)"))
        assertTrue(source.contains("RELATED_VIDEO_GRID_COLUMNS = 1"))
        assertTrue(source.contains("coverAspectRatio = cardLayout.coverAspectRatio"))
        assertTrue(source.contains("modifier = Modifier.fillMaxWidth()"))
        assertFalse(source.contains("videoCardShellSharedBoundsOrEmpty("))
        assertFalse(source.contains("videoCoverSharedBoundsOrEmpty("))
        assertTrue(source.contains("sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE"))
        assertTrue(source.contains("sourceChromeSnapshot = VideoCardSourceChromeSnapshot("))
        assertTrue(source.contains("coverBounds = coverCoordinatesRef.value"))
        assertTrue(source.contains("RelatedVideoGridRow("))
        assertTrue(source.contains("chunkRelatedVideosForHomeStyleGrid("))
        assertFalse(source.contains("relatedCoverWidth = 130.dp"))
        // UP 与播放量/弹幕成组贴底，避免 SpaceBetween 三等分把间距撑开。
        assertTrue(source.contains("verticalArrangement = Arrangement.spacedBy(4.dp)"))
        assertTrue(
            source.indexOf("UpBadgeName(") < source.indexOf("Icons.Filled.PlayArrow")
        )
    }

    @Test
    fun `related videos chunk into single column rows`() {
        val videos = (1..5).map { index ->
            RelatedVideo(
                aid = index.toLong(),
                bvid = "BV$index",
                title = "t$index",
                owner = Owner(),
                stat = Stat(),
            )
        }
        val rows = chunkRelatedVideosForHomeStyleGrid(videos)
        assertEquals(5, rows.size)
        assertTrue(rows.all { it.size == 1 })
        assertEquals("BV5", rows.last().single().bvid)
    }

    @Test
    fun `related skeleton mirrors the single column transition geometry`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/ui/components/SkeletonComponents.kt"
        ).readText()
        val relatedSkeleton = source
            .substringAfter("private fun RelatedVideoGridRowSkeleton()")
            .substringBefore("private fun RelatedVideoItemSkeleton(")

        assertTrue(relatedSkeleton.contains("coverAspectRatio = cardLayout.coverAspectRatio"))
        assertTrue(relatedSkeleton.contains("RelatedVideoItemSkeleton("))
        assertFalse(relatedSkeleton.contains("repeat(2)"))
    }

    @Test
    fun `related video cover uses shared CDN sizing policy`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt")
            .readText()

        assertTrue(source.contains("FormatUtils.resolveVideoCoverUrl(video.pic, useLowQuality = false)"))
        assertFalse(source.contains("FormatUtils.fixImageUrl(video.pic)"))
    }

    @Test
    fun `press haptic is disabled for related cards`() {
        assertFalse(
            shouldTriggerRelatedVideoPressHaptic(
                isPressed = true,
                transitionEnabled = true
            )
        )
        assertFalse(
            shouldTriggerRelatedVideoPressHaptic(
                isPressed = true,
                transitionEnabled = false
            )
        )
    }

    @Test
    fun `related cards expose official style more menu entry`() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt")
            .readText()
        assertTrue(source.contains("onMoreClick: (() -> Unit)? = null"))
        assertTrue(source.contains("RelatedVideoActionSheet("))
        assertTrue(source.contains("onVideoHidden"))
    }
}
