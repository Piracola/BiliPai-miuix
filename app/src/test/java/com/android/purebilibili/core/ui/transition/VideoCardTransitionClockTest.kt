package com.android.purebilibili.core.ui.transition

import kotlin.test.Test
import kotlin.test.assertEquals

class VideoCardTransitionClockTest {

    @Test
    fun compatibilityClockCanReturnToIdleWithoutSnapshotState() {
        val clock = VideoCardTransitionClock()

        clock.beginOpening(sourceRoute = "home")
        clock.markIdle()

        assertEquals(VideoCardTransitionBackgroundPhase.IDLE, clock.phase)
        assertEquals(null, clock.sourceRoute)
    }
}
