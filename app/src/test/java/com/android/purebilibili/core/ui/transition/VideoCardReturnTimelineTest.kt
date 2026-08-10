package com.android.purebilibili.core.ui.transition

import com.android.purebilibili.core.ui.adaptive.MotionTier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoCardReturnTimelineTest {

    @Test
    fun morphRemainingDuration_matchesNavDisplaySeekCompleteFormula() {
        data class Case(val fraction: Float, val fullMs: Int, val expected: Int)

        val cases = listOf(
            Case(0f, 360, 360),
            Case(0.25f, 360, 270),
            Case(0.5f, 360, 180),
            Case(0.75f, 360, 90),
            Case(1f, 360, 0),
            Case(0.5f, 0, 0),
            Case(-0.2f, 360, 360), // clamp
            Case(1.5f, 360, 0),
            Case(0.333f, 300, 200), // (1-0.333)*300 = 200.1 → 200
        )
        cases.forEach { case ->
            assertEquals(
                case.expected,
                resolveVideoCardSharedMorphRemainingDurationMs(
                    seekFraction = case.fraction,
                    fullDurationMs = case.fullMs,
                ),
                "fraction=${case.fraction} full=${case.fullMs}",
            )
        }
    }

    @Test
    fun depthBlurRemainingDuration_shortensWithCommitProgress() {
        assertEquals(
            180,
            resolveVideoCardReturnDepthBlurRemainingDurationMs(
                blurProgressAtCommit = 0.5f,
                fullDurationMs = 360,
                minDurationMs = 1,
            ),
        )
        // 不低于 min
        assertEquals(
            160,
            resolveVideoCardReturnDepthBlurRemainingDurationMs(
                blurProgressAtCommit = 0.01f,
                fullDurationMs = 360,
                minDurationMs = 160,
            ),
        )
    }

    @Test
    fun settleFromMorphDepth_isInverseOfDepth() {
        assertEquals(0f, resolveVideoCardReturnSettleFromMorphDepth(1f), 0.0001f)
        assertEquals(1f, resolveVideoCardReturnSettleFromMorphDepth(0f), 0.0001f)
        assertEquals(0.4f, resolveVideoCardReturnSettleFromMorphDepth(0.6f), 0.0001f)
    }

    @Test
    fun liveMorphContentAlpha_prefersMorphDepthOverDualSourceMax() {
        // 单时钟只认 morphDepth=0.9 → settle 0.1，正文处于前 28% 的让位过程。
        assertEquals(
            1f - 0.1f / 0.28f,
            resolveVideoCardLiveMorphSecondaryContentAlpha(
                transitionProgress = 0.2f,
                depthBlurProgress = 0.2f,
                morphDepthProgress = 0.9f,
            ),
            0.0001f,
        )
        // morphDepth=0.2 → settle 0.8 → 正文已让位
        val late = resolveVideoCardLiveMorphSecondaryContentAlpha(
            morphDepthProgress = 0.2f,
        )
        assertTrue(late < 0.3f)
    }

    @Test
    fun returnSessionLock_allowsUpgradeToLiveAndBlocksDemotion() {
        val first = resolveReturnSessionLockedCoverOwnership(
            lockedOwnership = null,
            isReturnSessionActive = true,
            candidateOwnership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
        )
        assertEquals(VideoCardReturnCoverOwnership.RESIDENT_COVER, first.second)
        // 首帧就绪：必须升到 LIVE，保留一镜到底实时画面
        val upgraded = resolveReturnSessionLockedCoverOwnership(
            lockedOwnership = first.first,
            isReturnSessionActive = true,
            candidateOwnership = VideoCardReturnCoverOwnership.LIVE_SURFACE,
        )
        assertEquals(VideoCardReturnCoverOwnership.LIVE_SURFACE, upgraded.second)
        // LIVE 不得降回封面
        val noDemote = resolveReturnSessionLockedCoverOwnership(
            lockedOwnership = upgraded.first,
            isReturnSessionActive = true,
            candidateOwnership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
        )
        assertEquals(VideoCardReturnCoverOwnership.LIVE_SURFACE, noDemote.second)
        val ended = resolveReturnSessionLockedCoverOwnership(
            lockedOwnership = noDemote.first,
            isReturnSessionActive = false,
            candidateOwnership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
        )
        assertEquals(null, ended.first)
        assertEquals(VideoCardReturnCoverOwnership.RESIDENT_COVER, ended.second)
    }

    @Test
    fun forceCoverOnly_neverWhenLiveOwnership() {
        assertFalse(
            shouldForceCoverOnlyForReturnOwnership(
                ownership = VideoCardReturnCoverOwnership.LIVE_SURFACE,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = true,
            )
        )
        // 非 live 也不得仅因 returning 就 forceCover（会一返回掐 player）
        assertFalse(
            shouldForceCoverOnlyForReturnOwnership(
                ownership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = false,
            )
        )
        assertTrue(
            shouldForceCoverOnlyForReturnOwnership(
                ownership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = true,
            )
        )
    }

    @Test
    fun forceCoverOnly_onlyWhenExplicitlyRequestedNotOnCommitAlone() {
        assertFalse(
            shouldForceCoverOnlyForReturnOwnership(
                ownership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = false,
                isCommittedCardReturn = false,
            )
        )
        assertFalse(
            shouldForceCoverOnlyForReturnOwnership(
                ownership = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = false,
                isCommittedCardReturn = true,
            )
        )
        assertFalse(
            shouldForceCoverOnlyForReturnOwnership(
                ownership = VideoCardReturnCoverOwnership.LIVE_SURFACE,
                useReturningVisualState = true,
                forceCoverOnlyOnReturn = false,
                isCommittedCardReturn = true,
            )
        )
    }

    @Test
    fun coverOwnership_tableCoversLiveResidentAndFallback() {
        data class Case(
            val name: String,
            val transitionEnabled: Boolean = true,
            val sharedBoundsActive: Boolean = true,
            val keepLoadedContentForBackPreview: Boolean = false,
            val playbackIntent: VideoSharedTransitionPlaybackIntent =
                VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
            val detailContentReady: Boolean = true,
            val hasResidentCover: Boolean = true,
            val liveSurfaceCardTransitionEnabled: Boolean = true,
            val expected: VideoCardReturnCoverOwnership,
            val handWhenLeaving: Boolean,
        )

        val cases = listOf(
            Case(
                name = "A live morph Success ImmediatePlayback",
                expected = VideoCardReturnCoverOwnership.LIVE_SURFACE,
                handWhenLeaving = false,
            ),
            Case(
                name = "B CoverFirst with cover",
                playbackIntent = VideoSharedTransitionPlaybackIntent.CoverFirst,
                expected = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                handWhenLeaving = true,
            ),
            Case(
                name = "B Loading skeleton not ready",
                detailContentReady = false,
                expected = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                handWhenLeaving = true,
            ),
            Case(
                name = "B back-preview keep parent",
                keepLoadedContentForBackPreview = true,
                expected = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                handWhenLeaving = true,
            ),
            Case(
                name = "C no shared bounds",
                sharedBoundsActive = false,
                expected = VideoCardReturnCoverOwnership.FALLBACK_NO_SHARED,
                handWhenLeaving = true,
            ),
            Case(
                name = "C transition disabled",
                transitionEnabled = false,
                expected = VideoCardReturnCoverOwnership.FALLBACK_NO_SHARED,
                handWhenLeaving = true,
            ),
            Case(
                name = "live path without cover still LIVE (avoid black)",
                hasResidentCover = false,
                expected = VideoCardReturnCoverOwnership.LIVE_SURFACE,
                handWhenLeaving = false,
            ),
            Case(
                name = "CoverFirst without cover → RESIDENT path but no hand",
                playbackIntent = VideoSharedTransitionPlaybackIntent.CoverFirst,
                hasResidentCover = false,
                expected = VideoCardReturnCoverOwnership.RESIDENT_COVER,
                handWhenLeaving = false,
            ),
        )

        cases.forEach { case ->
            val ownership = resolveVideoCardReturnCoverOwnership(
                transitionEnabled = case.transitionEnabled,
                sharedBoundsActive = case.sharedBoundsActive,
                keepLoadedContentForBackPreview = case.keepLoadedContentForBackPreview,
                playbackIntent = case.playbackIntent,
                detailContentReady = case.detailContentReady,
                hasResidentCover = case.hasResidentCover,
                hasRenderableLiveFrame = true,
                liveSurfaceCardTransitionEnabled = case.liveSurfaceCardTransitionEnabled,
            )
            assertEquals(case.expected, ownership, case.name)

            val liveGate = shouldUseVideoCardLiveReturnMorph(
                transitionEnabled = case.transitionEnabled,
                sharedBoundsActive = case.sharedBoundsActive,
                keepLoadedContentForBackPreview = case.keepLoadedContentForBackPreview,
                playbackIntent = case.playbackIntent,
                detailContentReady = case.detailContentReady,
                liveSurfaceCardTransitionEnabled = case.liveSurfaceCardTransitionEnabled,
            )
            assertEquals(
                liveGate,
                isVideoCardLiveReturnMorphOwnership(ownership),
                "${case.name}: ownership LIVE iff live morph gate",
            )

            assertEquals(
                case.handWhenLeaving,
                shouldHandVisualOwnershipToResidentCoverForOwnership(
                    ownership = ownership,
                    useReturningVisualState = true,
                    hasResidentCover = case.hasResidentCover,
                ),
                "${case.name}: hand when leaving",
            )
            assertFalse(
                shouldHandVisualOwnershipToResidentCoverForOwnership(
                    ownership = ownership,
                    useReturningVisualState = false,
                    hasResidentCover = case.hasResidentCover,
                ),
                "${case.name}: never hand while not leaving",
            )
        }
    }

    @Test
    fun listCoverContract_pinsAndDisablesCrossfadeOnReturnTarget() {
        val target = resolveVideoCardReturnListCoverContract(
            isSharedReturnTarget = true,
            isScrollInProgress = false,
            isReturningFromDetail = false,
            useCoverSharedBounds = true,
        )
        assertTrue(target.pinCoverSource)
        assertFalse(target.enableCoilCrossfade)

        val afterClearReturning = resolveVideoCardReturnListCoverContract(
            isSharedReturnTarget = true,
            isScrollInProgress = false,
            isReturningFromDetail = false, // clearReturning 之后
            useCoverSharedBounds = true,
        )
        assertFalse(
            afterClearReturning.enableCoilCrossfade,
            "clearReturning 后仍禁用 crossfade，避免落位再闪",
        )

        val normal = resolveVideoCardReturnListCoverContract(
            isSharedReturnTarget = false,
            isScrollInProgress = false,
            isReturningFromDetail = false,
            useCoverSharedBounds = true,
        )
        assertFalse(normal.pinCoverSource)
        assertTrue(normal.enableCoilCrossfade)
    }

    @Test
    fun liveReturnVisualHandoff_onlyRevealsSourceInFinalSettleWindow() {
        assertEquals(0f, resolveVideoCardLiveReturnVisualHandoffAlpha(1f), 0.0001f)
        assertEquals(0f, resolveVideoCardLiveReturnVisualHandoffAlpha(0.12f), 0.0001f)
        assertEquals(0.5f, resolveVideoCardLiveReturnVisualHandoffAlpha(0.06f), 0.0001f)
        assertEquals(1f, resolveVideoCardLiveReturnVisualHandoffAlpha(0f), 0.0001f)
    }

    @Test
    fun sessionPhase_mapsBackgroundAndGestureFlags() {
        data class Case(
            val phase: VideoCardTransitionBackgroundPhase,
            val gesture: Boolean = false,
            val restore: Boolean = false,
            val expected: VideoCardReturnSessionPhase,
        )

        val cases = listOf(
            Case(VideoCardTransitionBackgroundPhase.IDLE, expected = VideoCardReturnSessionPhase.Idle),
            Case(VideoCardTransitionBackgroundPhase.OPENING, expected = VideoCardReturnSessionPhase.Opening),
            Case(VideoCardTransitionBackgroundPhase.HELD, expected = VideoCardReturnSessionPhase.Held),
            Case(
                VideoCardTransitionBackgroundPhase.HELD,
                gesture = true,
                expected = VideoCardReturnSessionPhase.PredictiveSeek,
            ),
            Case(
                VideoCardTransitionBackgroundPhase.RETURNING,
                expected = VideoCardReturnSessionPhase.ReturningMorph,
            ),
            Case(
                VideoCardTransitionBackgroundPhase.HELD,
                restore = true,
                expected = VideoCardReturnSessionPhase.CancelRestore,
            ),
            Case(
                VideoCardTransitionBackgroundPhase.RETURNING,
                gesture = true,
                restore = true,
                expected = VideoCardReturnSessionPhase.CancelRestore,
            ),
        )
        cases.forEach { case ->
            assertEquals(
                case.expected,
                resolveVideoCardReturnSessionPhase(
                    backgroundPhase = case.phase,
                    isReturnGestureInProgress = case.gesture,
                    isGestureRestoreInProgress = case.restore,
                ),
            )
        }
    }

    @Test
    fun timeline_alignsMorphDurationSettleAndChromeReveal() {
        val timeline = resolveVideoCardReturnTimeline(morphDurationMillis = 360)
        assertEquals(360, timeline.morphDurationMillis)
        assertEquals(48L, timeline.settleBufferMillis)
        assertEquals(VIDEO_CARD_RETURN_CHROME_REVEAL_START, timeline.chromeRevealStart)
        assertEquals(VIDEO_CARD_RETURN_SOURCE_ENTER_FADE_DELAY_RATIO, timeline.sourceEnterFadeDelayRatio)
        assertEquals(408L, timeline.suppressionWindowMillis)
        assertTrue(shouldUseSeekableLinearReturnBoundsTransform())

        val quick = resolveVideoCardReturnTimeline(morphDurationMillis = 360, isQuickReturn = true)
        assertEquals(0f, quick.chromeRevealStart)
        assertEquals(0f, quick.sourceEnterFadeDelayRatio)
        assertFalse(shouldDelaySourceCardEnterOnReturn(isQuickReturnFromDetail = true))
        assertFalse(shouldDelaySourceCardEnterOnReturn(isQuickReturnFromDetail = false))
        assertTrue(canCoexistLiveSurfaceStableCoverAndChromeOnReturn())
    }

    @Test
    fun sourceChrome_isFullyVisibleBeforeTheLiveCoverHandoffFinishes() {
        assertEquals(0f, resolveVideoCardSourceChromeReturnAlpha(0.32f), 0.001f)
        assertEquals(0.5f, resolveVideoCardSourceChromeReturnAlpha(0.19f), 0.001f)
        assertEquals(1f, resolveVideoCardSourceChromeReturnAlpha(0.06f), 0.001f)
        assertEquals(0.5f, resolveVideoCardLiveReturnVisualHandoffAlpha(0.06f), 0.001f)
    }

    @Test
    fun liveMorphSecondaryContent_yieldsWithinFirstReturnSegment() {
        // settle 0：正文保持完整。
        assertEquals(
            1f,
            resolveVideoCardLiveMorphSecondaryContentAlpha(transitionProgress = 1f),
            0.001f,
        )
        // settle 0.28：前 28% 内完成让位。
        assertEquals(
            0f,
            resolveVideoCardLiveMorphSecondaryContentAlpha(transitionProgress = 0.72f),
            0.001f,
        )
        val mid = resolveVideoCardLiveMorphSecondaryContentAlpha(transitionProgress = 0.86f)
        assertTrue(mid in 0.01f..0.99f)
    }

    @Test
    fun visualTimeline_stagesOpeningReturnAndReducedMotionWithoutScale() {
        val openingStart = resolveVideoCardSecondaryContentVisualFrame(
            morphDepthProgress = 0.24f,
            phase = VideoCardTransitionBackgroundPhase.OPENING,
            isReturnGestureInProgress = false,
            motionTier = MotionTier.Normal,
        )
        val openingEnd = resolveVideoCardSecondaryContentVisualFrame(
            morphDepthProgress = 0.82f,
            phase = VideoCardTransitionBackgroundPhase.OPENING,
            isReturnGestureInProgress = false,
            motionTier = MotionTier.Normal,
        )
        assertEquals(0f, openingStart.alpha, 0.001f)
        assertEquals(8f, openingStart.translationYDp, 0.001f)
        assertEquals(1f, openingEnd.alpha, 0.001f)
        assertEquals(0f, openingEnd.translationYDp, 0.001f)

        val returnEnd = resolveVideoCardSecondaryContentVisualFrame(
            morphDepthProgress = 0.72f,
            phase = VideoCardTransitionBackgroundPhase.RETURNING,
            isReturnGestureInProgress = false,
            motionTier = MotionTier.Normal,
        )
        assertEquals(0f, returnEnd.alpha, 0.001f)

        val reduced = resolveVideoCardSecondaryContentVisualFrame(
            morphDepthProgress = 0.5f,
            phase = VideoCardTransitionBackgroundPhase.OPENING,
            isReturnGestureInProgress = false,
            motionTier = MotionTier.Reduced,
        )
        assertEquals(0.5f, reduced.alpha, 0.001f)
        assertEquals(0f, reduced.translationYDp, 0.001f)
        assertEquals(140, VideoCardTransitionVisualTimeline.REDUCED_MOTION_DURATION_MILLIS)
    }

    @Test
    fun detailChromeAndSourceChromeUseNonOverlappingMilestones() {
        assertEquals(
            0f,
            resolveVideoCardDetailChromeAlpha(
                morphDepthProgress = 0.18f,
                phase = VideoCardTransitionBackgroundPhase.OPENING,
                isReturnGestureInProgress = false,
            ),
            0.001f,
        )
        assertEquals(
            1f,
            resolveVideoCardDetailChromeAlpha(
                morphDepthProgress = 0.72f,
                phase = VideoCardTransitionBackgroundPhase.OPENING,
                isReturnGestureInProgress = false,
            ),
            0.001f,
        )
        assertEquals(0f, resolveVideoCardTimelineWindowProgress(0.68f, 0.68f, 1f))
        assertEquals(1f, resolveVideoCardTimelineWindowProgress(1f, 0.68f, 1f))
    }

    @Test
    fun liveMorph_alwaysUsesLiveSurfaceWhenGateConditionsMet() {
        assertTrue(
            shouldUseVideoCardLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasRenderableLiveFrame = true,
                liveSurfaceCardTransitionEnabled = true,
            )
        )
        assertEquals(
            VideoCardReturnCoverOwnership.LIVE_SURFACE,
            resolveVideoCardReturnCoverOwnership(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasResidentCover = true,
                hasRenderableLiveFrame = true,
                liveSurfaceCardTransitionEnabled = true,
            ),
        )
        // 用户关闭「实时画面转场」：不得走 LIVE 视频帧 morph，改封面路径。
        assertFalse(
            shouldUseVideoCardLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasRenderableLiveFrame = true,
                liveSurfaceCardTransitionEnabled = false,
            )
        )
        assertEquals(
            VideoCardReturnCoverOwnership.RESIDENT_COVER,
            resolveVideoCardReturnCoverOwnership(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasResidentCover = true,
                hasRenderableLiveFrame = true,
                liveSurfaceCardTransitionEnabled = false,
            ),
        )
    }

    @Test
    fun liveMorph_withoutFirstFrameFallsBackToResidentCover() {
        assertFalse(
            shouldTreatLiveSurfaceRenderableForReturnMorph(
                hasRenderedFirstFrame = false,
            )
        )
        assertFalse(
            shouldTreatLiveSurfaceRenderableForReturnMorph(
                hasRenderedFirstFrame = true,
                forceCoverUi = true,
            )
        )
        assertTrue(
            shouldTreatLiveSurfaceRenderableForReturnMorph(
                hasRenderedFirstFrame = true,
                forceCoverUi = false,
            )
        )

        val ownership = resolveVideoCardReturnCoverOwnership(
            transitionEnabled = true,
            sharedBoundsActive = true,
            keepLoadedContentForBackPreview = false,
            playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
            detailContentReady = true,
            hasResidentCover = true,
            hasRenderableLiveFrame = false,
        )
        assertEquals(VideoCardReturnCoverOwnership.RESIDENT_COVER, ownership)
        assertTrue(
            shouldHandVisualOwnershipToResidentCoverForOwnership(
                ownership = ownership,
                useReturningVisualState = true,
                hasResidentCover = true,
            )
        )
        assertFalse(
            shouldUseVideoCardLiveReturnMorph(
                transitionEnabled = true,
                sharedBoundsActive = true,
                keepLoadedContentForBackPreview = false,
                playbackIntent = VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                detailContentReady = true,
                hasRenderableLiveFrame = false,
            )
        )
    }

    @Test
    fun settleProgress_takesLaterOfTransitionAndDepth() {
        // transition 0.8 → settle 0.2；depth 0.3 → settle 0.7 → max 0.7
        assertEquals(
            0.7f,
            resolveVideoCardReturnSettleProgress(
                transitionProgress = 0.8f,
                depthBlurProgress = 0.3f,
            ),
            0.001f,
        )
        assertEquals(
            0.2f,
            resolveVideoCardReturnSettleProgress(transitionProgress = 0.8f),
            0.001f,
        )
        // 景深更靠后时 content 应更早让位
        val withDepth = resolveVideoCardLiveMorphSecondaryContentAlpha(
            transitionProgress = 0.8f,
            depthBlurProgress = 0.3f,
        )
        val withoutDepth = resolveVideoCardLiveMorphSecondaryContentAlpha(
            transitionProgress = 0.8f,
        )
        assertTrue(withDepth < withoutDepth)
    }

    @Test
    fun seekableReturnBounds_areLinearTweensNotSpring() {
        val motion = resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = "home",
            transitionEnabled = true,
        )
        val card = androidx.compose.ui.geometry.Rect(0f, 0f, 100f, 80f)
        val detail = androidx.compose.ui.geometry.Rect(0f, 0f, 400f, 800f)
        val returning = videoSharedElementBoundsTransformSpec(motion, detail, card)
        assertTrue(returning is androidx.compose.animation.core.TweenSpec<*>)
        assertEquals(
            motion.durationMillis,
            (returning as androidx.compose.animation.core.TweenSpec<*>).durationMillis,
        )
        // 半程松手 remaining 必须 > 0
        assertTrue(
            resolveVideoCardSharedMorphRemainingDurationMs(0.5f, motion.durationMillis) > 0,
        )
    }
}
