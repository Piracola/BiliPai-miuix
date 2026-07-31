package com.android.purebilibili.core.ui.motion

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationApiBoundaryTest {

    @Test
    fun navigationAndVideoCardHotPathCannotRestoreDecorativeMotion() {
        val navigationTransform = source("navigation3/BiliPaiNavContentTransformPolicy.kt")
        val videoCardBounds = source("core/ui/transition/VideoCardShellSharedBounds.kt")
        val homeSkeleton = source("feature/home/HomeFeedSkeletonCard.kt")
        val videoSkeleton = source("feature/video/ui/components/SkeletonComponents.kt")

        assertTrue(navigationTransform.contains("EnterTransition.None togetherWith ExitTransition.None"))
        assertFalse(navigationTransform.contains("slideIn"))
        assertFalse(navigationTransform.contains("fadeIn"))
        assertFalse(videoCardBounds.substringAfter("videoCardShellSharedBoundsOrEmpty")
            .contains("Modifier.sharedBounds("))
        assertFalse(homeSkeleton.contains("rememberInfiniteTransition"))
        assertFalse(videoSkeleton.contains("val transition = rememberInfiniteTransition"))
    }

    private fun source(relativePath: String): String {
        return File("src/main/java/com/android/purebilibili/$relativePath").readText()
    }
}
