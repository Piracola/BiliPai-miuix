package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BiliPaiNavMotionPolicyTest {

    @Test
    fun navigationDecisionIsNoOpEvenWhenLegacyCardSettingIsEnabled() {
        val decision = resolveBiliPaiNavMotionDecision(
            fromKey = BiliPaiNavKey.MainHost,
            toKey = BiliPaiNavKey.VideoDetail("BV1"),
            cardTransitionEnabled = true,
            sharedTransitionReady = true,
        )

        assertEquals(BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT, decision.routeTransition)
    }

    @Test
    fun navigationPolicyDoesNotChooseLegacyVisualBranches() {
        val source = File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavMotionPolicy.kt")
            .readText()

        assertFalse(source.contains("BiliPaiNavRouteTransition.CLASSIC_CARD"))
        assertFalse(source.contains("BiliPaiNavRouteTransition.LIGHT_SIBLING_POP"))
        assertFalse(source.contains("resolveCardDisabledReturnTransition(sourceMetadata"))
    }
}
