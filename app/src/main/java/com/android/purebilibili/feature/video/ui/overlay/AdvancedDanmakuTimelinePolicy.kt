package com.android.purebilibili.feature.video.ui.overlay

import com.android.purebilibili.feature.video.danmaku.AdvancedDanmakuData

internal const val ADVANCED_DANMAKU_EDGE_BUFFER_MS = 100L

internal data class AdvancedDanmakuTimelineIndex(
    val itemsByStartTime: List<AdvancedDanmakuData>,
    val maximumDurationMs: Long,
)

internal fun buildAdvancedDanmakuTimelineIndex(
    items: List<AdvancedDanmakuData>,
): AdvancedDanmakuTimelineIndex {
    val sorted = items.sortedBy { it.startTimeMs }
    return AdvancedDanmakuTimelineIndex(
        itemsByStartTime = sorted,
        maximumDurationMs = sorted.maxOfOrNull { it.durationMs.coerceAtLeast(0L) } ?: 0L,
    )
}

internal fun resolveActiveAdvancedDanmakus(
    index: AdvancedDanmakuTimelineIndex,
    positionMs: Long,
    edgeBufferMs: Long = ADVANCED_DANMAKU_EDGE_BUFFER_MS,
): List<AdvancedDanmakuData> {
    val lowerBound = positionMs - index.maximumDurationMs - edgeBufferMs
    val upperBound = positionMs + edgeBufferMs
    val first = index.itemsByStartTime.lowerBoundStartTime(lowerBound)
    val endExclusive = index.itemsByStartTime.upperBoundStartTime(upperBound)
    if (first >= endExclusive) return emptyList()

    return index.itemsByStartTime.subList(first, endExclusive).filter { item ->
        positionMs >= item.startTimeMs - edgeBufferMs &&
            positionMs <= item.startTimeMs + item.durationMs + edgeBufferMs
    }
}

private fun List<AdvancedDanmakuData>.lowerBoundStartTime(value: Long): Int {
    var low = 0
    var high = size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (this[mid].startTimeMs < value) low = mid + 1 else high = mid
    }
    return low
}

private fun List<AdvancedDanmakuData>.upperBoundStartTime(value: Long): Int {
    var low = 0
    var high = size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (this[mid].startTimeMs <= value) low = mid + 1 else high = mid
    }
    return low
}
