package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiNavContentTransformPolicyStructureTest {

    @Test
    fun allNavigationTransformsAreNoOp() {
        val source = contentTransformPolicySource()

        assertTrue(source.contains("EnterTransition.None togetherWith ExitTransition.None"))
        assertFalse(source.contains("slideIn"))
        assertFalse(source.contains("slideOut"))
        assertFalse(source.contains("fadeIn"))
        assertFalse(source.contains("fadeOut"))
        assertFalse(source.contains("scaleIn"))
        assertFalse(source.contains("scaleOut"))
        assertFalse(source.contains("tween("))
    }

    private fun contentTransformPolicySource(): String {
        return File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavContentTransformPolicy.kt")
            .readText()
    }
}
