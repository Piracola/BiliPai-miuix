package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiNavContentTransformPolicyStructureTest {

    @Test
    fun navigationTransformsUseHorizontalStackMotionAndRespectReducedMotion() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("EnterTransition.None togetherWith ExitTransition.None"))
        assertTrue(source.contains("BiliPaiNavRouteTransition.REDUCED_MOTION"))
        assertTrue(source.contains("slideInHorizontally("))
        assertTrue(source.contains("slideOutHorizontally("))
        assertTrue(source.contains("NAVIGATION_PUSH_DURATION_MILLIS = 280"))
        assertTrue(source.contains("NAVIGATION_POP_DURATION_MILLIS = 240"))
        assertTrue(source.contains("NAVIGATION_BACKGROUND_PARALLAX_RATIO = 0.2f"))
        assertTrue(source.contains("LayoutDirection"))
        assertTrue(source.contains("targetContentZIndex = 1f"))
        assertTrue(source.contains("targetContentZIndex = -1f"))
        assertFalse(source.contains("fadeIn("))
        assertFalse(source.contains("fadeOut("))
        assertFalse(source.contains("scaleIn"))
        assertFalse(source.contains("scaleOut"))
    }

    private fun contentTransformPolicySource(): String {
        return File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavContentTransformPolicy.kt")
            .readText()
    }
}
