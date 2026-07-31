package com.android.purebilibili.navigation3

import androidx.compose.ui.geometry.Rect

/**
 * A click-time snapshot of the source card used for the complete detail round trip.
 *
 * Feed state is intentionally not consulted after this object is created. Lists can refresh,
 * reorder or leave composition while a detail page is open; using their mutable click globals
 * at return time can otherwise redirect the whole-card morph to a different card.
 */
internal data class VideoCardTransitionSession(
    val bvid: String,
    val sourceRoute: String?,
    val sourceKey: String?,
    val cardBounds: Rect?,
    val sourceCornerDp: Int?,
    val cardSourceDirection: BiliPaiNavCardSourceDirection,
    val coverIdentity: String?,
    val cardFullyVisible: Boolean,
    val isSingleColumnCard: Boolean,
) {
    val hasUsableSourceGeometry: Boolean
        get() = cardBounds != null && cardFullyVisible

    companion object {
        fun create(
            bvid: String,
            source: BiliPaiVideoSource,
            cardBounds: Rect?,
            sourceCornerDp: Int?,
            cardSourceDirection: BiliPaiNavCardSourceDirection,
            coverIdentity: String?,
            cardFullyVisible: Boolean,
            isSingleColumnCard: Boolean,
        ): VideoCardTransitionSession {
            val normalizedBvid = bvid.trim()
            val sourceBvid = source.key
                ?.substringAfterLast(':', missingDelimiterValue = "")
                ?.trim()
            val ownsRecordedGeometry = normalizedBvid.isNotEmpty() &&
                sourceBvid == normalizedBvid
            return VideoCardTransitionSession(
                bvid = normalizedBvid,
                sourceRoute = normalizeBiliPaiVideoSourceRoute(source.route),
                sourceKey = source.key?.trim()?.takeIf(String::isNotEmpty),
                cardBounds = cardBounds?.takeIf { ownsRecordedGeometry }?.let {
                    Rect(it.left, it.top, it.right, it.bottom)
                },
                sourceCornerDp = sourceCornerDp
                    ?.takeIf { ownsRecordedGeometry }
                    ?.coerceAtLeast(0),
                cardSourceDirection = cardSourceDirection.takeIf { ownsRecordedGeometry }
                    ?: BiliPaiNavCardSourceDirection.NONE,
                coverIdentity = coverIdentity?.trim()?.takeIf(String::isNotEmpty),
                cardFullyVisible = ownsRecordedGeometry && cardFullyVisible,
                isSingleColumnCard = isSingleColumnCard,
            )
        }
    }
}
