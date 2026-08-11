package com.android.purebilibili.feature.video.screen

import androidx.compose.ui.geometry.Rect
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeVisualFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VideoDetailReturnSourceCardChromeTest {
    @Test
    fun nonWidescreenGridCardUsesItsMeasuredCoverBottom() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 20f, top = 100f, right = 520f, bottom = 700f),
            // 4:3 cover: the anchor must come from this measured bottom, not 16:9 math.
            sourceCoverBounds = Rect(left = 20f, top = 100f, right = 520f, bottom = 475f),
        )

        assertTrue(layout.canRender)
        assertEquals(VideoCardSourceLayout.STACKED, layout.layout)
        assertEquals(0.5f, layout.sourceScale, 0.0001f)
        assertEquals(500f, layout.cardWidthPx, 0.0001f)
        assertEquals(600f, layout.cardHeightPx, 0.0001f)
        assertEquals(375f, layout.coverHeightPx, 0.0001f)
        assertEquals(500f, layout.coverWidthPx, 0.0001f)
        assertEquals(500f, layout.infoWidthPx, 0.0001f)
        assertEquals(225f, layout.infoHeightPx, 0.0001f)
        // Info top = cover bottom in entry space: 375 / 0.5 = 750.
        assertEquals(0f, layout.infoAnchorXInViewportPx, 0.0001f)
        assertEquals(750f, layout.infoAnchorYInViewportPx, 0.0001f)
        assertEquals(0f, layout.cardAnchorXInViewportPx, 0.0001f)
        assertEquals(0f, layout.cardAnchorYInViewportPx, 0.0001f)
    }

    @Test
    fun landedGeometryMatchesStationaryCardCoverInfoSplit() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 20f, top = 100f, right = 520f, bottom = 700f),
            sourceCoverBounds = Rect(left = 20f, top = 100f, right = 520f, bottom = 475f),
        )
        // Cover + info fill the full card (home stacked card).
        assertEquals(
            layout.coverHeightPx + layout.infoHeightPx,
            layout.cardHeightPx,
            0.0001f,
        )
        assertEquals(
            layout.coverHeightPx / layout.sourceScale,
            resolveVideoDetailReturnCoverHeightInEntryPx(layout),
            0.0001f,
        )
        // Resting chrome frame: alpha 1, scale multiplier 1 (no boost at land).
        val landed = resolveVideoCardSourceChromeVisualFrame(morphDepthProgress = 0f)
        assertEquals(1f, landed.alpha, 0.0001f)
        assertEquals(1f, landed.layoutScaleMultiplier, 0.0001f)
        assertEquals(1f, landed.handoffProgress, 0.0001f)
    }

    @Test
    fun coverOnlyOrHorizontalLandingDoesNotInventABelowCoverRegion() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 50f, top = 200f, right = 950f, bottom = 380f),
            sourceCoverBounds = Rect(left = 50f, top = 200f, right = 950f, bottom = 380f),
        )

        assertFalse(layout.canRender)
        assertEquals(0f, layout.infoHeightPx, 0.0001f)
        assertEquals(VideoCardSourceLayout.COVER_ONLY, layout.layout)
    }

    @Test
    fun sideBySideRelatedCardLandsInfoToTheRightOfMeasuredCover() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 16f, top = 400f, right = 984f, bottom = 580f),
            sourceCoverBounds = Rect(left = 22f, top = 406f, right = 166f, bottom = 574f),
            sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
        )

        assertTrue(layout.canRender)
        assertEquals(VideoCardSourceLayout.SIDE_BY_SIDE, layout.layout)
        assertEquals(0.968f, layout.sourceScale, 0.001f)
        assertEquals(984f - 166f, layout.infoWidthPx, 0.0001f)
        assertEquals(580f - 400f, layout.infoHeightPx, 0.0001f)
        assertEquals(166f - 16f, layout.coverWidthPx, 0.0001f)
        assertEquals((166f - 16f) / layout.sourceScale, layout.infoAnchorXInViewportPx, 0.001f)
        assertEquals(0f, layout.infoAnchorYInViewportPx, 0.001f)
        assertEquals(
            (166f - 16f) / layout.sourceScale,
            resolveVideoDetailReturnCoverWidthInEntryPx(layout),
            0.001f,
        )
    }

    @Test
    fun sideBySideLandingFacadeIsSolidForWholeReturnMorph() {
        val sideStart = com.android.purebilibili.core.ui.transition
            .resolveVideoCardSourceChromeVisualFrame(
                morphDepthProgress = 0.99f,
                phase = com.android.purebilibili.core.ui.transition
                    .VideoCardTransitionBackgroundPhase.HELD,
                isReturnGestureInProgress = true,
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            )
        val sideMid = com.android.purebilibili.core.ui.transition
            .resolveVideoCardSourceChromeVisualFrame(
                morphDepthProgress = 0.5f,
                phase = com.android.purebilibili.core.ui.transition
                    .VideoCardTransitionBackgroundPhase.RETURNING,
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            )
        val stackedLate = com.android.purebilibili.core.ui.transition
            .resolveVideoCardSourceChromeVisualFrame(
                morphDepthProgress = 0.82f,
                phase = com.android.purebilibili.core.ui.transition
                    .VideoCardTransitionBackgroundPhase.RETURNING,
                sourceLayout = VideoCardSourceLayout.STACKED,
            )
        assertEquals(1f, sideStart.alpha, 0.001f)
        assertEquals(1f, sideMid.alpha, 0.001f)
        assertEquals(0f, stackedLate.alpha, 0.001f)
    }

    @Test
    fun sideBySideWithoutExplicitLayoutStillDetectsFromBounds() {
        val layout = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 0f, top = 100f, right = 1000f, bottom = 280f),
            sourceCoverBounds = Rect(left = 0f, top = 100f, right = 240f, bottom = 280f),
        )

        assertTrue(layout.canRender)
        assertEquals(VideoCardSourceLayout.SIDE_BY_SIDE, layout.layout)
        assertEquals(760f, layout.infoWidthPx, 0.0001f)
    }

    @Test
    fun chromeModelPrefersLiveDetailThenFallsBackToClickSnapshot() {
        val snapshot = VideoCardSourceChromeSnapshot(
            title = "snap-title",
            ownerName = "snap-up",
            viewText = "1万",
            danmakuText = "200",
            durationText = "03:21",
            followed = true,
        )
        val fromSnapshot = resolveVideoDetailReturnSourceCardChromeModel(
            info = null,
            snapshot = snapshot,
        )
        assertNotNull(fromSnapshot)
        assertEquals("snap-title", fromSnapshot!!.title)
        assertEquals("03:21", fromSnapshot.durationText)
        assertTrue(fromSnapshot.followed)

        assertNull(
            resolveVideoDetailReturnSourceCardChromeModel(info = null, snapshot = null),
        )
    }

    @Test
    fun sourceChromeIsHostedOnFullViewportEntryNotUnderPlayerColumn() {
        val holder = File(
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt",
        ).takeIf { it.isFile }?.readText()
            ?: File(
                "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt",
            ).readText()
        val chromeCall = holder
            .substringAfter("VideoDetailReturnSourceCardChrome(")
            .substringBefore("Full-viewport source-card chrome host")
        assertTrue(holder.contains("手机竖屏布局结束（Column）"))
        assertTrue(holder.contains("Full-viewport source-card chrome host (phone + tablet)"))
        val phoneBranchEnd = holder.indexOf("phone portrait branch of useTabletLayout")
        val chromeStart = holder.indexOf("VideoDetailReturnSourceCardChrome(")
        assertTrue(phoneBranchEnd in 1 until chromeStart)
        assertTrue(chromeCall.contains("align(Alignment.TopStart)"))
        assertTrue(chromeCall.contains("sourceLayout = miuixCardTransitionState.sourceLayout"))
        assertTrue(chromeCall.contains("sourceChromeSnapshot = miuixCardTransitionState.sourceChromeSnapshot"))
        assertTrue(holder.contains("sourceChromeSnapshot != null"))
        // Whole-card shell behind player + cover height clip for flush land.
        assertTrue(holder.contains("landingCoverHeightEntryPx"))
        assertTrue(holder.contains("resolveVideoDetailReturnCoverHeightInEntryPx"))
    }
}
