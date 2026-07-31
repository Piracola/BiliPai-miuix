package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveLoadingIndicatorIntegrationTest {

    @Test
    fun sharedEntryPointsRouteThroughAdaptiveLoading() {
        val adaptive = loadSource(
            "design-system/src/main/java/com/android/purebilibili/core/ui/AdaptiveLoadingIndicator.kt"
        )
        val iosRenderer = loadSource(
            "design-system/src/main/java/com/android/purebilibili/core/ui/IosLoadingIndicator.kt"
        )
        val lottie = loadSource(
            "app/src/main/java/com/android/purebilibili/core/ui/LottieComponents.kt"
        )

        assertTrue(adaptive.contains("LoadingIndicator("))
        assertTrue(adaptive.contains("MiuixInfiniteProgressIndicator("))
        assertTrue(adaptive.contains("MiuixCircularProgressIndicator("))
        assertTrue(adaptive.contains("IosCutePersonLoadingIndicator("))
        assertTrue(iosRenderer.contains("internal fun IosCutePersonLoadingIndicator("))
    }

    @Test
    fun cutePersonEntryPointDispatchesThroughAdaptiveLoading() {
        val lottie = loadSource(
            "app/src/main/java/com/android/purebilibili/core/ui/LottieComponents.kt"
        )

        assertTrue(lottie.contains("AdaptiveLoadingIndicator("))
        assertTrue(lottie.contains("fun CutePersonLoadingIndicator("))
        assertFalse(lottie.contains("internal fun IosCutePersonLoadingIndicator("))
    }

    @Test
    fun emptyStateUsesStaticIconWithoutRemoteLottieAnimation() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/core/ui/LottieComponents.kt"
        )

        assertTrue(source.contains("imageVector = Icons.Outlined.Inbox"))
        assertFalse(source.contains("com.airbnb.lottie"))
        assertFalse(source.contains("https://"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        return listOf(
            File(path),
            File("../$path"),
            File(normalizedPath),
        ).first { it.exists() }.readText()
    }
}
