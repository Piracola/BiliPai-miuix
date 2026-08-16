// 文件路径: core/video/pager/PortraitPlaybackBootstrap.kt
package com.android.purebilibili.core.video.pager

/**
 * 阶段 6 前置：从 feature/video/ui/pager/PortraitVideoLoadPolicy.kt 拆出的
 * 纯常量与判定函数（零 Compose/Media3 依赖），供 data 层与 feature 共用。
 */

/**
 * Fallback when no user preference is available (e.g. unit tests / cold path).
 * Live portrait playback should pass the detail-page playable default quality instead.
 */
const val PORTRAIT_PLAYBACK_TARGET_QUALITY = 64

/**
 * Resolve the playurl qn for portrait pager / Story.
 *
 * Prefer the same playable default used by video detail so Wi‑Fi 1080P / VIP 4K-HDR
 * settings are honored. Cap invalid values to the safe fallback.
 */
fun resolvePortraitPlaybackTargetQuality(
    preferredQuality: Int? = null
): Int {
    val quality = preferredQuality ?: return PORTRAIT_PLAYBACK_TARGET_QUALITY
    return quality.takeIf { it > 0 } ?: PORTRAIT_PLAYBACK_TARGET_QUALITY
}

fun shouldUsePortraitParallelPlaybackBootstrap(
    bvid: String,
    requestedCid: Long
): Boolean = bvid.trim().startsWith("BV", ignoreCase = true) && requestedCid > 0L
