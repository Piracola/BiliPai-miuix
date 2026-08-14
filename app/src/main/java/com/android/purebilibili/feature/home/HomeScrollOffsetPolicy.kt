package com.android.purebilibili.feature.home

import kotlin.math.abs

internal fun resolveNextHomeGlobalScrollOffset(
    currentOffset: Float,
    scrollDeltaY: Float,
    minUpdateDeltaPx: Float = 0.5f
): Float? {
    @Suppress("UNUSED_PARAMETER")
    val unused = listOf(currentOffset, scrollDeltaY, minUpdateDeltaPx)
    // Liquid-glass scroll coupling is retired: no global scroll offset is published.
    return null
}
