package com.android.purebilibili.core.ui.transition

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

internal enum class VideoCardSourceLayout {
    STACKED,
    SIDE_BY_SIDE,
    COVER_ONLY,
}

/**
 * Which info-region rows the list card actually paints under the cover (not on-cover badges).
 *
 * Frozen at click time so return chrome can mirror the real card without inventing fields
 * (e.g. home dual-column: title + UP + 发布于; danmaku stays on cover, not in info).
 */
@Immutable
internal data class VideoCardSourceInfoPresentation(
    /** Preformatted publish row as shown on the card, e.g. "发布于 昨天"; blank = no row. */
    val publishTimeText: String = "",
    /**
     * When true, info region paints view/danmaku (and optional duration) text.
     * When false, those live on the cover overlay and must not appear under the title.
     */
    val showStatsInInfo: Boolean = false,
    /**
     * When true, list info sits on the home themed/tinted plate
     * ([resolveHomeCardInfoSurfaceAppearance]); flying chrome must paint the same plate.
     */
    val useTintedInfoSurface: Boolean = false,
)

/**
 * Click-time chrome + cover frozen from the **stationary list card**.
 *
 * Text is already formatted by the source card so the landing frame cannot drift after data
 * refreshes or while the destination is in Loading.
 *
 * [infoPresentation] records **which** rows were visible so flying chrome can match
 * dynamically when home layout / scroll-lite policy changes.
 *
 * [coverUrl] / [coverCacheKey] / decode size must be the exact Coil request the list
 * AsyncImage uses at rest (including [com.android.purebilibili.feature.home.HomeCoverRequestSpec]
 * sized URL + `size(w,h)`). Detail resident / player-section covers reuse these so handoff
 * pixels match the stationary card — not `fixImageUrl` / default cache key.
 */
@Immutable
internal data class VideoCardSourceChromeSnapshot(
    val title: String,
    val ownerName: String,
    val ownerFaceUrl: String = "",
    val viewText: String = "",
    val danmakuText: String = "",
    val durationText: String = "",
    val followed: Boolean = false,
    /** What the list info column actually showed at click. */
    val infoPresentation: VideoCardSourceInfoPresentation = VideoCardSourceInfoPresentation(),
    /** Exact list-card cover request URL (sized / quality resolved). */
    val coverUrl: String = "",
    /** Exact list-card Coil memoryCacheKey / diskCacheKey. */
    val coverCacheKey: String = "",
    /** Coil `size(w,h)` from list HomeCoverRequestSpec; 0 = omit size(). */
    val coverDecodeWidthPx: Int = 0,
    val coverDecodeHeightPx: Int = 0,
)

/** Build info-presentation from the list card’s live display flags (call at click). */
internal fun resolveVideoCardSourceInfoPresentation(
    publishTimeText: String,
    showStatsInInfo: Boolean,
    useTintedInfoSurface: Boolean = false,
): VideoCardSourceInfoPresentation = VideoCardSourceInfoPresentation(
    publishTimeText = publishTimeText.trim(),
    showStatsInInfo = showStatsInInfo,
    useTintedInfoSurface = useTintedInfoSurface,
)

internal fun resolveVideoCardSourceLayout(
    cardBounds: Rect?,
    coverBounds: Rect?,
): VideoCardSourceLayout {
    val card = cardBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return VideoCardSourceLayout.COVER_ONLY
    val cover = coverBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return VideoCardSourceLayout.COVER_ONLY
    val horizontalTolerance = card.width * 0.1f
    val verticalTolerance = card.height * 0.1f
    val coverSpansCardWidth = cover.left <= card.left + horizontalTolerance &&
        cover.right >= card.right - horizontalTolerance
    val coverSpansCardHeight = cover.top <= card.top + verticalTolerance &&
        cover.bottom >= card.bottom - verticalTolerance
    return when {
        coverSpansCardWidth && !coverSpansCardHeight -> VideoCardSourceLayout.STACKED
        coverSpansCardHeight && !coverSpansCardWidth -> VideoCardSourceLayout.SIDE_BY_SIDE
        else -> VideoCardSourceLayout.COVER_ONLY
    }
}
