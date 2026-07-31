package com.android.purebilibili

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SplashExitAnimationSpecTest {

    @Test
    fun usesPlatformSplashExitWithoutCustomVisualTimeline() {
        val source = loadMainActivitySource()

        assertTrue(source.contains("installSplashScreen()"))
        assertFalse(source.contains("setOnExitAnimationListener"))
        assertFalse(source.contains("ValueAnimator.ofFloat"))
        assertFalse(source.contains("RenderEffect.createBlurEffect"))
    }

    private fun loadMainActivitySource(): String = File(
        "src/main/java/com/android/purebilibili/MainActivity.kt"
    ).takeIf(File::exists)?.readText()
        ?: File("app/src/main/java/com/android/purebilibili/MainActivity.kt").readText()
}
