package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiNavDisplayHostNativeTransitionStructureTest {

    @Test
    fun hostForwardsPredictiveBackProgressAndCancellationToNativeVideoTransition() {
        val source = loadSource()

        assertTrue(source.contains("onNativeVideoBackProgress:"))
        assertTrue(source.contains("NavigationEventTransitionState.InProgress"))
        assertTrue(source.contains("?.latestEvent"))
        assertTrue(source.contains("?.progress"))
        assertTrue(source.contains("onNativeVideoBackCancelled("))
    }

    @Test
    fun scopedEntryContentDoesNotRetainVideoTransitionExposureProvider() {
        val source = loadSource()
        val scopedContentRememberKeys = source
            .substringAfter("val scopedContent:")
            .substringAfter("remember(")
            .substringBefore(") {")

        assertFalse(scopedContentRememberKeys.contains("videoCardExposureProvider"))
        assertFalse(scopedContentRememberKeys.contains("videoCardClock"))
        assertTrue(scopedContentRememberKeys.contains("application"))
    }

    private fun loadSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"),
            File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt")
        ).first { it.exists() }.readText()
    }
}
