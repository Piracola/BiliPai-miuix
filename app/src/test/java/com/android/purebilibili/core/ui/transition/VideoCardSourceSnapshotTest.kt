package com.android.purebilibili.core.ui.transition

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoCardSourceSnapshotTest {
    @Test
    fun resolvesStackedSourceFromFullWidthCover() {
        assertEquals(
            VideoCardSourceLayout.STACKED,
            resolveVideoCardSourceLayout(
                cardBounds = Rect(0f, 0f, 400f, 500f),
                coverBounds = Rect(0f, 0f, 400f, 300f),
            ),
        )
    }

    @Test
    fun resolvesSideBySideSourceFromFullHeightCover() {
        assertEquals(
            VideoCardSourceLayout.SIDE_BY_SIDE,
            resolveVideoCardSourceLayout(
                cardBounds = Rect(0f, 0f, 900f, 300f),
                coverBounds = Rect(0f, 0f, 360f, 300f),
            ),
        )
    }

    @Test
    fun fallsBackToCoverOnlyWithoutMeasuredCover() {
        assertEquals(
            VideoCardSourceLayout.COVER_ONLY,
            resolveVideoCardSourceLayout(
                cardBounds = Rect(0f, 0f, 400f, 500f),
                coverBounds = null,
            ),
        )
    }
}
