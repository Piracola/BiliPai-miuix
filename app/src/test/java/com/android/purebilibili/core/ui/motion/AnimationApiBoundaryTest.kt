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

    @Test
    fun appSourcesCannotRestoreSharedElementsOrInfiniteDecoration() {
        val violations = mainSources().flatMap { file ->
            val text = file.readText()
            buildList {
                if (SHARED_BOUNDS.containsMatchIn(text)) add("${file.name}: sharedBounds")
                if (SHARED_ELEMENT.containsMatchIn(text)) add("${file.name}: sharedElement")
                if (INFINITE_TRANSITION.containsMatchIn(text)) add("${file.name}: rememberInfiniteTransition")
                if (INFINITE_REPEATABLE.containsMatchIn(text)) add("${file.name}: infiniteRepeatable")
            }
        }

        val violationList = violations.toList()
        assertTrue(
            violationList.isEmpty(),
            "共享元素和无限装饰动画必须保持为零；发现：${violationList.sorted()}",
        )
    }

    @Test
    fun playerFeedbackAndSurfaceRevealStayStatic() {
        val celebration = source("feature/video/ui/components/CelebrationAnimations.kt")
        val playerSection = source("feature/video/ui/section/VideoPlayerSection.kt")
        val surfaceRevealBlock = playerSection
            .substringAfter("val surfaceRevealSpec =")
            .substringBefore("key(isFlippedHorizontal, isFlippedVertical, isPortraitFullscreen)")

        assertFalse(celebration.contains("Animatable("))
        assertFalse(celebration.contains("animateFloatAsState("))
        assertFalse(celebration.contains("Canvas("))
        assertFalse(celebration.contains("graphicsLayer"))
        assertFalse(surfaceRevealBlock.contains("animateFloatAsState("))
        assertTrue(surfaceRevealBlock.contains("val playerSurfaceScale = 1f"))
        assertFalse(playerSection.contains("resolveVideoPlayerRevealMotionSpec()"))
        assertFalse(playerSection.contains("coverRevealHoldDelayMillis"))
    }

    private fun source(relativePath: String): String {
        return File("src/main/java/com/android/purebilibili/$relativePath").readText()
    }

    private fun mainSources(): Sequence<File> =
        File("src/main/java/com/android/purebilibili")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }

    private companion object {
        val SHARED_BOUNDS = Regex("""\.sharedBounds\s*\(""")
        val SHARED_ELEMENT = Regex("""\.sharedElement\s*\(""")
        val INFINITE_TRANSITION = Regex("""rememberInfiniteTransition\s*\(""")
        val INFINITE_REPEATABLE = Regex("""infiniteRepeatable\s*\(""")
    }
}
