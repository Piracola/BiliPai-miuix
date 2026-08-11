package com.android.purebilibili.core.ui.transition

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect

internal enum class VideoCardSourceLayout {
    STACKED,
    SIDE_BY_SIDE,
    COVER_ONLY,
}

/**
 * Click-time text shown while a Miuix flying entry is still loading its destination detail.
 * Values are already formatted by the source card so the landing frame cannot drift after data
 * refreshes or while the destination is in Loading.
 */
@Immutable
internal data class VideoCardSourceChromeSnapshot(
    val title: String,
    val ownerName: String,
    val ownerFaceUrl: String = "",
    val viewText: String,
    val danmakuText: String,
    val durationText: String,
    val followed: Boolean = false,
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
