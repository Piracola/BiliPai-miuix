package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiNavEntryProviderPolicyTest {

    @Test
    fun entryProviderOwnsOnlyEntryContentAndNeverRouteAnimationMetadata() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation3/BiliPaiNavEntryProvider.kt"
        ).readText()

        assertTrue(source.contains("entryProvider("))
        assertTrue(source.contains("entry<BiliPaiNavKey.VideoDetail>"))
        assertFalse(source.contains("NavDisplay.transitionSpec"))
        assertFalse(source.contains("NavDisplay.popTransitionSpec"))
        assertFalse(source.contains("BiliPaiNavSourceMetadata"))
        assertFalse(source.contains("cardTransitionEnabled"))
    }
}
