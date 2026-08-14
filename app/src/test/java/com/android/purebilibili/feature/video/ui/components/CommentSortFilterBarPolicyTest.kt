package com.android.purebilibili.feature.video.ui.components

import com.android.purebilibili.feature.video.viewmodel.CommentSortMode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentSortFilterBarPolicyTest {

    @Test
    fun `comment sort exposes only hot and newest without up filter`() {
        assertEquals(
            listOf(CommentSortMode.HOT, CommentSortMode.NEWEST),
            CommentSortMode.entries
        )

        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt"
        )
        assertTrue(source.contains("listOf(CommentSortMode.HOT, CommentSortMode.NEWEST)"))
        assertFalse(source.contains("CommentToggleButton"))
        assertFalse(source.contains("onUpOnlyToggle"))
    }

    @Test
    fun `sort segmented control leaves room for bottom bar matched indicator scale`() {
        val spec = resolveCommentSortSegmentedControlSpec(itemCount = 2)

        assertEquals(66, spec.itemWidthDp)
        assertEquals(40, spec.heightDp)
        assertEquals(27, spec.indicatorHeightDp)
        assertTrue(
            hasCommentSortIndicatorScaleClearance(
                containerHeightDp = spec.heightDp,
                indicatorHeightDp = spec.indicatorHeightDp
            )
        )
    }

    @Test
    fun `sort segmented control disables tap press refraction`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt"
        )

        assertFalse(source.contains("tapPressRefractionEnabled = true"))
    }

    @Test
    fun `sort segmented control accepts a stable page sized miuix sibling capture`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt"
        )

        assertTrue(source.contains("backdrop: Backdrop? = null"))
        assertTrue(source.contains("backdrop = backdrop"))
        assertTrue(source.contains("miuixBackdrop = miuixBackdrop"))
        assertTrue(source.contains("MiuixBackdrop"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
