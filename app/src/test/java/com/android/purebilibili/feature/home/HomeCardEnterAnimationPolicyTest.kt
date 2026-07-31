package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertFalse

class HomeCardEnterAnimationPolicyTest {

    @Test
    fun cardEntranceIsDisabledForEveryHomeState() {
        assertFalse(
            resolveHomeCardEnterAnimationEnabledAtMount(
                baseAnimationEnabled = true,
                isReturningFromDetail = false,
                isSwitchingCategory = false,
                isScrollInProgress = false,
            )
        )
    }

    @Test
    fun cardEntranceNeverCoordinatesWithSharedTransition() {
        assertFalse(
            shouldCoordinateCardEnterWithSharedTransition(
                cardAnimationEnabled = true,
                cardTransitionEnabled = true,
            )
        )
    }
}
