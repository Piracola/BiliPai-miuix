package com.android.purebilibili.feature.video.screen

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceInfoPresentation
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeVisualFrame
import com.android.purebilibili.data.model.response.Owner
import com.android.purebilibili.data.model.response.Stat
import com.android.purebilibili.data.model.response.ViewInfo
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
        // Measured cover height (not cardTop→coverBottom expansion).
        assertEquals(375f, layout.coverHeightPx, 0.0001f)
        assertEquals(500f, layout.coverWidthPx, 0.0001f)
        assertEquals(0f, layout.coverOffsetYPx, 0.0001f)
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
    fun landedMediaIsRemeasuredToTheExactStationaryCover() {
        val landing = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 0f, top = 0f, right = 500f, bottom = 600f),
            // A 4:3 cover becomes 1000x750 in entry space, taller than a 16:10 player.
            sourceCoverBounds = Rect(left = 0f, top = 0f, right = 500f, bottom = 375f),
        )

        val frame = resolveVideoDetailReturnMediaLayoutFrame(
            containerWidthPx = 1000,
            containerHeightPx = 625,
            landingLayout = landing,
            handoffProgress = 1f,
        )

        assertEquals(0, frame.offsetXPx)
        assertEquals(0, frame.offsetYPx)
        assertEquals(1000, frame.widthPx)
        // Do not clamp to the player height: that would shrink again at the list-card handoff.
        assertEquals(750, frame.heightPx)
    }

    @Test
    fun mediaLayoutInterpolatesTowardInsetCoverWithoutChangingHostSize() {
        val landing = resolveVideoDetailReturnSourceCardLayout(
            viewportWidthPx = 1000f,
            sourceBounds = Rect(left = 10f, top = 50f, right = 510f, bottom = 650f),
            sourceCoverBounds = Rect(left = 30f, top = 70f, right = 480f, bottom = 370f),
        )

        val frame = resolveVideoDetailReturnMediaLayoutFrame(
            containerWidthPx = 1000,
            containerHeightPx = 625,
            landingLayout = landing,
            handoffProgress = 0.5f,
        )

        // sourceScale=0.5, so the target cover is (40,40) + 900x600 in entry space.
        assertEquals(20, frame.offsetXPx)
        assertEquals(20, frame.offsetYPx)
        assertEquals(950, frame.widthPx)
        assertEquals(613, frame.heightPx)
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
        // Exact measured cover box (not expanded to card left).
        assertEquals(166f - 22f, layout.coverWidthPx, 0.0001f)
        assertEquals(22f - 16f, layout.coverOffsetXPx, 0.0001f)
        assertEquals(406f - 400f, layout.coverOffsetYPx, 0.0001f)
        assertEquals(
            (layout.coverOffsetXPx + layout.coverWidthPx) / layout.sourceScale,
            layout.infoAnchorXInViewportPx,
            0.001f,
        )
        assertEquals(0f, layout.infoAnchorYInViewportPx, 0.001f)
        assertEquals(
            layout.coverWidthPx / layout.sourceScale,
            resolveVideoDetailReturnCoverWidthInEntryPx(layout),
            0.001f,
        )
        assertEquals(
            layout.coverOffsetXPx / layout.sourceScale,
            resolveVideoDetailReturnCoverOffsetXInEntryPx(layout),
            0.001f,
        )
    }

    @Test
    fun sideBySideSharesHomeLateChromeHandoffWithStacked() {
        // SIDE_BY_SIDE no longer paints a solid facade for the whole morph; it uses the same
        // late 72%–96% chrome window as STACKED so live media stays visible early.
        val sideMid = com.android.purebilibili.core.ui.transition
            .resolveVideoCardSourceChromeVisualFrame(
                morphDepthProgress = 0.5f,
                phase = com.android.purebilibili.core.ui.transition
                    .VideoCardTransitionBackgroundPhase.RETURNING,
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            )
        val stackedMid = com.android.purebilibili.core.ui.transition
            .resolveVideoCardSourceChromeVisualFrame(
                morphDepthProgress = 0.5f,
                phase = com.android.purebilibili.core.ui.transition
                    .VideoCardTransitionBackgroundPhase.RETURNING,
                sourceLayout = VideoCardSourceLayout.STACKED,
            )
        val sideNearLand = com.android.purebilibili.core.ui.transition
            .resolveVideoCardSourceChromeVisualFrame(
                morphDepthProgress = 0.04f,
                phase = com.android.purebilibili.core.ui.transition
                    .VideoCardTransitionBackgroundPhase.RETURNING,
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            )
        assertEquals(0f, sideMid.alpha, 0.001f)
        assertEquals(stackedMid.alpha, sideMid.alpha, 0.001f)
        assertEquals(1f, sideNearLand.alpha, 0.001f)
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
        assertEquals(240f, layout.coverWidthPx, 0.0001f)
        assertEquals(0f, layout.coverOffsetXPx, 0.0001f)
        assertEquals(760f, layout.infoWidthPx, 0.0001f)
    }

    @Test
    fun infoSurfaceSpecMirrorsTintedHomePlateWhenFlagged() {
        val plain = resolveVideoDetailReturnInfoSurfaceSpec(
            useTintedInfoSurface = false,
            isDarkTheme = true,
            baseContainerColor = Color(0xFF1C1B1F),
        )
        assertFalse(plain.useTintedSurface)

        val tinted = resolveVideoDetailReturnInfoSurfaceSpec(
            useTintedInfoSurface = true,
            isDarkTheme = true,
            baseContainerColor = Color(0xFF1C1B1F),
        )
        assertTrue(tinted.useTintedSurface)
        assertTrue(tinted.containerColor.alpha < 1f)
        assertTrue(tinted.borderWidth.value > 0f)
    }

    @Test
    fun infoSecondaryLineFollowsFrozenListPresentation() {
        val homeLike = VideoDetailReturnSourceCardChromeModel(
            title = "t",
            ownerName = "up",
            viewText = "1.2万",
            danmakuText = "300",
            followed = true,
            infoPresentation = VideoCardSourceInfoPresentation(
                publishTimeText = "发布于 昨天",
                showStatsInInfo = false,
            ),
        )
        assertEquals("发布于 昨天", resolveVideoDetailReturnInfoSecondaryLine(homeLike))
        assertFalse(resolveVideoDetailReturnInfoSecondaryLine(homeLike).contains("弹幕"))

        val withStats = homeLike.copy(
            infoPresentation = VideoCardSourceInfoPresentation(
                publishTimeText = "发布于 昨天",
                showStatsInInfo = true,
            ),
        )
        val statsLine = resolveVideoDetailReturnInfoSecondaryLine(withStats)
        assertTrue(statsLine.contains("播放"))
        assertTrue(statsLine.contains("弹幕"))
        assertFalse(statsLine.contains("发布于"))
    }

    @Test
    fun chromeModelPrefersClickSnapshotPresentationOverDetailStats() {
        val snapshot = VideoCardSourceChromeSnapshot(
            title = "list-title",
            ownerName = "list-up",
            viewText = "9.9万",
            danmakuText = "888",
            durationText = "10:00",
            followed = true,
            infoPresentation = VideoCardSourceInfoPresentation(
                publishTimeText = "发布于 2026-07-30",
                showStatsInInfo = false,
            ),
        )
        val info = ViewInfo(
            title = "detail-title",
            owner = Owner(name = "detail-up"),
            stat = Stat(view = 1, danmaku = 2),
            pubdate = 1_700_000_000L,
        )
        val model = resolveVideoDetailReturnSourceCardChromeModel(info = info, snapshot = snapshot)
        assertNotNull(model)
        assertEquals("list-title", model!!.title)
        assertEquals("list-up", model.ownerName)
        assertTrue(model.followed)
        assertFalse(model.infoPresentation.showStatsInInfo)
        assertEquals("发布于 2026-07-30", resolveVideoDetailReturnInfoSecondaryLine(model))
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
    fun flyingReturnChromeIsDrawnOnOverlayBecauseListIsCovered() {
        val holder = File(
            "app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt",
        ).takeIf { it.isFile }?.readText()
            ?: File(
                "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt",
            ).readText()
        assertTrue(holder.contains("shouldDrawFlyingReturnSourceCardChrome()"))
        assertTrue(shouldDrawFlyingReturnSourceCardChrome())
        assertTrue(holder.contains("VideoDetailReturnSourceCardChrome("))
        assertTrue(holder.contains("returnMediaHandoffProgressProvider"))
        assertTrue(holder.contains(".videoDetailReturnMediaLayout("))
    }
}
