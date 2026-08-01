package com.android.purebilibili.feature.video.ui.section

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class VideoInfoSectionAnimationStructureTest {

    @Test
    fun videoInfoExpandUsesOneConsistentNativeMotionContract() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/ui/section/VideoInfoSection.kt"
        ).readText()

        assertTrue(source.contains("VIDEO_INFO_EXPAND_DURATION_MILLIS = 220"))
        assertTrue(source.contains("VIDEO_INFO_INDICATOR_DURATION_MILLIS = 200"))
        assertTrue(source.contains("Modifier.animateContentSize("))
        assertTrue(source.contains("AnimatedVisibility("))
        assertTrue(source.contains("expandVertically("))
        assertTrue(source.contains("shrinkVertically("))
        assertTrue(source.contains("fadeIn("))
        assertTrue(source.contains("fadeOut("))
        assertTrue(source.contains("VideoInfoExpandIndicator"))
        assertTrue(source.contains("tween(durationMillis = 0)"))
    }
}
