package com.android.purebilibili.feature.video.screen

import kotlin.test.Test
import kotlin.test.assertEquals

class ContinuousPlayerTransitionPolicyTest {

    @Test
    fun enteringFullscreenRequestsLandscapeImmediately() {
        val awaitingLandscape = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.Inline,
            event = ContinuousPlayerTransitionEvent.Toggle,
        )
        assertEquals(
            ContinuousPlayerTransitionPhase.AwaitingLandscape,
            awaitingLandscape.phase,
        )
        assertEquals(
            ContinuousPlayerOrientationRequest.Landscape,
            awaitingLandscape.orientationRequest,
        )
    }

    @Test
    fun systemLandscapeCompletesFullscreenWithoutRestartingAnimation() {
        val result = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
            event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = true),
        )

        assertEquals(ContinuousPlayerTransitionPhase.Fullscreen, result.phase)
        assertEquals(ContinuousPlayerOrientationRequest.None, result.orientationRequest)
    }

    @Test
    fun exitingFullscreenCompletesWhenSystemReachesPortrait() {
        val awaitingPortrait = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.Fullscreen,
            event = ContinuousPlayerTransitionEvent.Toggle,
        )
        assertEquals(
            ContinuousPlayerTransitionPhase.AwaitingPortrait,
            awaitingPortrait.phase,
        )
        assertEquals(
            ContinuousPlayerOrientationRequest.Portrait,
            awaitingPortrait.orientationRequest,
        )

        val inline = reduceContinuousPlayerTransition(
            phase = awaitingPortrait.phase,
            event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape = false),
        )
        assertEquals(ContinuousPlayerTransitionPhase.Inline, inline.phase)
    }

    @Test
    fun togglingWhileSystemRotationIsPendingRequestsTheOppositeOrientation() {
        val result = reduceContinuousPlayerTransition(
            phase = ContinuousPlayerTransitionPhase.AwaitingLandscape,
            event = ContinuousPlayerTransitionEvent.Toggle,
        )

        assertEquals(ContinuousPlayerTransitionPhase.AwaitingPortrait, result.phase)
        assertEquals(ContinuousPlayerOrientationRequest.Portrait, result.orientationRequest)
    }
}
