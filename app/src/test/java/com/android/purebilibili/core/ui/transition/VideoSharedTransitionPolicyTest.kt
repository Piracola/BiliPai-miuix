package com.android.purebilibili.core.ui.transition

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoSharedTransitionPolicyTest {

    @Test
    fun videoCardShellDoesNotRegisterSharedBounds() {
        val source = File(
            "src/main/java/com/android/purebilibili/core/ui/transition/" +
                "VideoCardShellSharedBounds.kt"
        ).readText()
        val function = source.substringAfter("videoCardShellSharedBoundsOrEmpty")

        assertTrue(function.contains("return this"))
        assertFalse(function.contains("Modifier.sharedBounds("))
        assertFalse(function.contains("boundsTransform ="))
    }
}
