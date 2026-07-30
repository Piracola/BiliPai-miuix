package com.android.purebilibili.feature.video.ui.overlay

import com.android.purebilibili.feature.video.danmaku.AdvancedDanmakuData
import kotlin.test.Test
import kotlin.test.assertEquals

class AdvancedDanmakuTimelinePolicyTest {

    @Test
    fun activeWindow_usesStartIndexAndKeepsBoundaryBuffer() {
        val index = buildAdvancedDanmakuTimelineIndex(
            listOf(
                item(id = "late", startMs = 6_000L, durationMs = 400L),
                item(id = "active", startMs = 1_000L, durationMs = 1_000L),
                item(id = "edge", startMs = 2_100L, durationMs = 200L),
                item(id = "expired", startMs = 0L, durationMs = 100L),
            ),
        )

        assertEquals(
            listOf("active", "edge"),
            resolveActiveAdvancedDanmakus(index, positionMs = 2_000L).map { it.id },
        )
    }

    @Test
    fun activeWindow_handlesSeekingBackward() {
        val index = buildAdvancedDanmakuTimelineIndex(
            listOf(
                item(id = "first", startMs = 500L, durationMs = 300L),
                item(id = "second", startMs = 2_000L, durationMs = 300L),
            ),
        )

        assertEquals(listOf("second"), resolveActiveAdvancedDanmakus(index, positionMs = 2_000L).map { it.id })
        assertEquals(listOf("first"), resolveActiveAdvancedDanmakus(index, positionMs = 500L).map { it.id })
    }

    private fun item(id: String, startMs: Long, durationMs: Long) = AdvancedDanmakuData(
        id = id,
        content = id,
        startTimeMs = startMs,
        durationMs = durationMs,
        startX = 0f,
        startY = 0f,
    )
}
