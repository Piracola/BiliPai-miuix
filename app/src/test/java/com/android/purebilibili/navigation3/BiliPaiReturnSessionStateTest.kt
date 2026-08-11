package com.android.purebilibili.navigation3

import androidx.compose.ui.geometry.Rect
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiReturnSessionStateTest {

    @Test
    fun relatedSourceRestoreWaitsForMiuixSettle() {
        assertEquals(
            332L,
            resolveRelatedReturnSourceRestoreDelayMillis(
                cardTransitionEnabled = true,
                reduceMotion = false,
                transitionDurationMillis = 300,
            ),
        )
        assertEquals(
            0L,
            resolveRelatedReturnSourceRestoreDelayMillis(
                cardTransitionEnabled = true,
                reduceMotion = true,
                transitionDurationMillis = 300,
            ),
        )
    }

    private fun transitionSession(
        bvid: String,
        sourceRoute: String,
        sourceKey: String,
        left: Float = 10f,
    ) = VideoCardTransitionSession.create(
        bvid = bvid,
        source = BiliPaiVideoSource(route = sourceRoute, key = sourceKey),
        cardBounds = Rect(left, 20f, left + 100f, 180f),
        sourceCornerDp = 12,
        cardSourceDirection = BiliPaiNavCardSourceDirection.SOURCE_LEFT,
        coverIdentity = "cover-$bvid",
        cardFullyVisible = true,
        isSingleColumnCard = false,
        sourceLayout = if (sourceRoute.startsWith("video/")) {
            VideoCardSourceLayout.SIDE_BY_SIDE
        } else {
            VideoCardSourceLayout.STACKED
        },
        sourceChromeSnapshot = VideoCardSourceChromeSnapshot(
            title = "title-$bvid",
            ownerName = "owner-$bvid",
            viewText = "1万",
            danmakuText = "200",
            durationText = "01:23",
        ),
    )

    @Test
    fun videoSourceRouteIsStoredOutsideCardPositionManager() {
        val state = BiliPaiReturnSessionState()
            .recordVideoSource(
                BiliPaiVideoSource(
                    route = "search",
                    key = "search:BV1"
                )
            )

        assertEquals("search", state.lastVideoSourceRoute)
        assertEquals("search:BV1", state.lastVideoSourceKey)
        assertFalse(state.isReturningFromDetail)
        assertFalse(state.isQuickReturnFromDetail)
    }

    @Test
    fun returnSessionMarksQuickReturnFromElapsedTime() {
        val state = BiliPaiReturnSessionState()
            .markDetailEntered(nowMillis = 1_000L)
            .markReturning(nowMillis = 1_450L)

        assertTrue(state.isReturningFromDetail)
        assertTrue(state.isQuickReturnFromDetail)
    }

    @Test
    fun clearReturningKeepsSourceRouteForNextSharedElementMatch() {
        val state = BiliPaiReturnSessionState()
            .recordVideoSource(
                BiliPaiVideoSource(
                    route = "home",
                    key = "home:BV1"
                )
            )
            .markDetailEntered(nowMillis = 1_000L)
            .markReturning(nowMillis = 2_000L)
            .clearReturning()

        assertEquals("home", state.lastVideoSourceRoute)
        assertEquals("home:BV1", state.lastVideoSourceKey)
        assertFalse(state.isReturningFromDetail)
        assertFalse(state.isQuickReturnFromDetail)
    }

    @Test
    fun legacyRouteRecordingClearsPreviousSourceKey() {
        val state = BiliPaiReturnSessionState()
            .recordVideoSource(
                BiliPaiVideoSource(
                    route = "search",
                    key = "search:BV1"
                )
            )
            .recordVideoSourceRoute("history?from=tab")

        assertEquals("history", state.lastVideoSourceRoute)
        assertEquals(null, state.lastVideoSourceKey)
    }

    @Test
    fun relatedDetailSourcePreservesAndRestoresListSource() {
        val state = BiliPaiReturnSessionState()
            .recordVideoSource(
                BiliPaiVideoSource(
                    route = "home",
                    key = "home:BV_A"
                )
            )
            .recordVideoSource(
                BiliPaiVideoSource(
                    route = "video/BV_A",
                    key = "video/BV_A:BV_B"
                )
            )

        assertEquals("video/BV_A", state.lastVideoSourceRoute)
        assertEquals("video/BV_A:BV_B", state.lastVideoSourceKey)
        assertEquals("home", state.previousVideoSources.single().route)
        assertEquals("home:BV_A", state.previousVideoSources.single().key)

        val restored = state.restorePreviousVideoSourceAfterRelatedReturn()
        assertEquals("home", restored.lastVideoSourceRoute)
        assertEquals("home:BV_A", restored.lastVideoSourceKey)
        assertTrue(restored.previousVideoSources.isEmpty())
    }

    @Test
    fun immutableTransitionSessionKeepsClickGeometryAfterLiveSourceChanges() {
        val clicked = transitionSession(
            bvid = "BV_A",
            sourceRoute = "home",
            sourceKey = "home:BV_A",
            left = 20f,
        )
        val state = BiliPaiReturnSessionState().recordTransitionSession(clicked)

        val originalBounds = clicked.cardBounds
        val unrelatedLaterCard = transitionSession(
            bvid = "BV_B",
            sourceRoute = "home",
            sourceKey = "home:BV_B",
            left = 500f,
        )

        assertEquals(originalBounds, state.transitionSession?.cardBounds)
        assertFalse(state.transitionSession?.cardBounds == unrelatedLaterCard.cardBounds)
        assertEquals("cover-BV_A", state.transitionSession?.coverIdentity)
        assertEquals(12, state.transitionSession?.sourceCornerDp)
        assertEquals(VideoCardSourceLayout.STACKED, state.transitionSession?.sourceLayout)
        assertEquals("title-BV_A", state.transitionSession?.sourceChromeSnapshot?.title)
    }

    @Test
    fun staleOrAdjacentCardGeometryIsRejectedByBvidOwnership() {
        val session = VideoCardTransitionSession.create(
            bvid = "BV_TARGET",
            source = BiliPaiVideoSource(route = "home", key = "home:BV_ADJACENT"),
            cardBounds = Rect(0f, 0f, 100f, 100f),
            sourceCornerDp = 12,
            cardSourceDirection = BiliPaiNavCardSourceDirection.SOURCE_RIGHT,
            coverIdentity = "target-cover",
            cardFullyVisible = true,
            isSingleColumnCard = false,
        )

        assertEquals(null, session.cardBounds)
        assertEquals(null, session.sourceCornerDp)
        assertEquals(BiliPaiNavCardSourceDirection.NONE, session.cardSourceDirection)
        assertFalse(session.hasUsableSourceGeometry)
    }

    @Test
    fun relatedReturnRestoresTheCompleteListTransitionSession() {
        val listSession = transitionSession("BV_A", "home", "home:BV_A")
        val relatedSession = transitionSession(
            "BV_B",
            "video/BV_A",
            "video/BV_A:BV_B",
            left = 300f,
        )
        val restored = BiliPaiReturnSessionState()
            .recordTransitionSession(listSession)
            .recordTransitionSession(relatedSession)
            .restorePreviousVideoSourceAfterRelatedReturn()

        assertEquals(listSession, restored.transitionSession)
        assertEquals("home", restored.lastVideoSourceRoute)
        assertEquals("home:BV_A", restored.lastVideoSourceKey)
        assertTrue(restored.previousTransitionSessions.isEmpty())
    }

    @Test
    fun multiHopRelatedReturnRestoresEveryIntermediateSourceSession() {
        val homeToA = transitionSession("BV_A", "home", "home:BV_A")
        val aToB = transitionSession("BV_B", "video/BV_A", "video/BV_A:BV_B")
        val bToC = transitionSession("BV_C", "video/BV_B", "video/BV_B:BV_C")
        val atC = BiliPaiReturnSessionState()
            .recordTransitionSession(homeToA)
            .recordTransitionSession(aToB)
            .recordTransitionSession(bToC)

        assertEquals(listOf(homeToA, aToB), atC.previousTransitionSessions)

        val backAtB = atC.restorePreviousVideoSourceAfterRelatedReturn()
        assertEquals(aToB, backAtB.transitionSession)
        assertEquals("video/BV_A:BV_B", backAtB.lastVideoSourceKey)

        val backAtA = backAtB.restorePreviousVideoSourceAfterRelatedReturn()
        assertEquals(homeToA, backAtA.transitionSession)
        assertEquals("home:BV_A", backAtA.lastVideoSourceKey)
        assertTrue(backAtA.previousTransitionSessions.isEmpty())
    }
}
