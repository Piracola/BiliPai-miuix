package com.android.purebilibili.navigation

import com.android.purebilibili.data.model.response.resolveKnownVerticalVideo

internal data class PortraitStoryNavigationSeed(
    val bvid: String,
    val cid: Long,
    val coverUrl: String
)

/** 是否短路进 Story 刷视频页。 */
internal fun resolvePortraitStoryNavigationSeed(
    directPortraitStoryEntry: Boolean,
    isVerticalVideo: Boolean,
    startAudio: Boolean,
    bvid: String,
    cid: Long = 0L,
    coverUrl: String = "",
    verticalRatioThreshold: Float = 1.0f,
): PortraitStoryNavigationSeed? {
    val normalizedBvid = bvid.trim()
    val resolvedVertical = resolveKnownVerticalVideo(
        isVerticalVideo = isVerticalVideo,
        coverUrl = coverUrl,
        verticalRatioThreshold = verticalRatioThreshold
    )
    if (!directPortraitStoryEntry || !resolvedVertical || startAudio || normalizedBvid.isEmpty()) {
        return null
    }
    return PortraitStoryNavigationSeed(
        bvid = normalizedBvid,
        cid = cid.coerceAtLeast(0L),
        coverUrl = coverUrl
    )
}
