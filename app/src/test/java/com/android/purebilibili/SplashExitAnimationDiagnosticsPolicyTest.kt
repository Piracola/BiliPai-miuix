package com.android.purebilibili

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SplashExitAnimationDiagnosticsPolicyTest {

    @Test
    fun doesNotKeepCustomExitDiagnosticsOrFallbackAnimationPath() {
        val source = loadMainActivitySource()

        assertTrue(source.contains("val splashScreen = installSplashScreen()"))
        assertFalse(source.contains("SplashFlyoutTargetType"))
        assertFalse(source.contains("shouldUseRealtimeSplashBlur"))
        assertFalse(source.contains("clearSplashRealtimeBlur"))
    }

    private fun loadMainActivitySource(): String = File(
        "src/main/java/com/android/purebilibili/MainActivity.kt"
    ).takeIf(File::exists)?.readText()
        ?: File("app/src/main/java/com/android/purebilibili/MainActivity.kt").readText()
}
