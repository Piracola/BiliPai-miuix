package com.android.purebilibili.feature.video.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class VideoDetailRouteSheetPolicyTest {

    @Test
    fun secondaryContentTiming_isDisabledForFormerGeometryTimeline() {
        val timing = resolveVideoDetailSecondaryContentTiming(
            fullDurationMillis = 320,
            contentDelayMillis = 40,
            contentDurationMillis = 220,
        )

        assertEquals(0, timing.enterDelayMillis)
        assertEquals(0, timing.enterDurationMillis)
        assertEquals(0, timing.returnDelayMillis)
        assertEquals(0, timing.returnDurationMillis)
    }

    @Test
    fun secondaryContentTiming_ignoresFormerCustomTimeline() {
        val timing = resolveVideoDetailSecondaryContentTiming(
            fullDurationMillis = 240,
            contentDelayMillis = 40,
            contentDurationMillis = 220,
        )

        assertEquals(0, timing.enterDelayMillis)
        assertEquals(0, timing.enterDurationMillis)
        assertEquals(0, timing.returnDelayMillis)
        assertEquals(0, timing.returnDurationMillis)
    }

    @Test
    fun allSourcesDisableRouteSheetMotion() {
        listOf(
            "home",
            "dynamic",
            "search",
            "watch_later",
            "dynamic_detail/123",
            "space/42",
            "category/1",
            "season_series_detail/favorite_season/1324105"
        ).forEach { route ->
            val motion = resolveVideoDetailRouteSheetMotion(
                sourceRoute = route,
                transitionEnabled = true
            )

            assertFalse(motion.enabled, "route sheet motion must stay disabled for $route")
            assertEquals(0, motion.durationMillis)
            assertEquals(0, motion.mainDurationMillis)
            assertEquals(0, motion.settleDurationMillis)
            assertEquals(1f, motion.initialScale)
            assertEquals(0f, motion.initialTranslationYDp)
            assertEquals(0f, motion.initialCornerDp)
            assertEquals(0f, motion.initialBackgroundScrimAlpha)
            assertEquals(0f, motion.settleScaleDelta)
            assertEquals(0f, motion.settleTranslationDp)
        }
    }

    @Test
    fun nonCardSourceDoesNotUseRouteSheetMotion() {
        assertFalse(
            resolveVideoDetailRouteSheetMotion(
                sourceRoute = "settings",
                transitionEnabled = true
            ).enabled
        )
        assertFalse(
            resolveVideoDetailRouteSheetMotion(
                sourceRoute = "video",
                transitionEnabled = true
            ).enabled
        )
        assertFalse(
            resolveVideoDetailRouteSheetMotion(
                sourceRoute = "home",
                transitionEnabled = false
            ).enabled
        )
    }

    @Test
    fun routeSheetFrame_staysAtStaticFullscreenStateForEveryProgress() {
        val motion = resolveVideoDetailRouteSheetMotion(
            sourceRoute = "home",
            transitionEnabled = true
        )
        val start = resolveVideoDetailRouteSheetFrame(
            rawProgress = 0f,
            settleProgress = 0f,
            settleDirection = VideoDetailRouteSheetSettleDirection.None,
            motion = motion
        )
        val end = resolveVideoDetailRouteSheetFrame(
            rawProgress = 1f,
            settleProgress = 0f,
            settleDirection = VideoDetailRouteSheetSettleDirection.None,
            motion = motion
        )

        listOf(start, end).forEach { frame ->
            assertEquals(1f, frame.scale)
            assertEquals(0f, frame.translationYDp)
            assertEquals(0f, frame.cornerDp)
            assertEquals(0f, frame.backgroundScrimAlpha)
            assertEquals(0f, frame.settleProgress)
        }
    }

    @Test
    fun routeSheetFrame_ignoresFormerSettleDirections() {
        val motion = resolveVideoDetailRouteSheetMotion(
            sourceRoute = "home",
            transitionEnabled = true
        )

        val enterSettle = resolveVideoDetailRouteSheetFrame(
            rawProgress = 1f,
            settleProgress = 1f,
            settleDirection = VideoDetailRouteSheetSettleDirection.Enter,
            motion = motion
        )
        val returnSettle = resolveVideoDetailRouteSheetFrame(
            rawProgress = 0f,
            settleProgress = 1f,
            settleDirection = VideoDetailRouteSheetSettleDirection.Return,
            motion = motion
        )

        listOf(enterSettle, returnSettle).forEach { frame ->
            assertEquals(1f, frame.scale)
            assertEquals(0f, frame.translationYDp)
            assertEquals(0f, frame.cornerDp)
            assertEquals(0f, frame.backgroundScrimAlpha)
            assertEquals(0f, frame.settleProgress)
        }
    }
}
