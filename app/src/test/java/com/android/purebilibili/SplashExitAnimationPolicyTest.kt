package com.android.purebilibili

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SplashExitAnimationPolicyTest {

    @Test
    fun keepsOnlySystemSplashAndPreloadHoldPolicy() {
        val source = loadMainActivitySource()

        assertTrue(source.contains("setKeepOnScreenCondition"))
        assertTrue(source.contains("splashMaxKeepOnScreenMs()"))
        assertFalse(source.contains("splashExitDurationMs"))
        assertFalse(source.contains("splashTrailPrimaryAlpha"))
        assertFalse(source.contains("applySplashRealtimeBlur"))
    }

    private fun loadMainActivitySource(): String = File(
        "src/main/java/com/android/purebilibili/MainActivity.kt"
    ).takeIf(File::exists)?.readText()
        ?: File("app/src/main/java/com/android/purebilibili/MainActivity.kt").readText()
}
