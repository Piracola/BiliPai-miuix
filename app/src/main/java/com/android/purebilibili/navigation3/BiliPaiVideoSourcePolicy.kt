package com.android.purebilibili.navigation3

internal data class BiliPaiVideoSource(
    val route: String?,
    val key: String?
)

internal fun resolveBiliPaiVideoSource(
    bvid: String,
    explicitSourceRoute: String?,
    currentKey: BiliPaiNavKey?,
    previousSourceRoute: String?
): BiliPaiVideoSource {
    val route = normalizeBiliPaiVideoSourceRoute(
        explicitSourceRoute ?: when (currentKey) {
            is BiliPaiNavKey.VideoDetail -> {
                // Prefer explicit related host `video/{parent}` when provided by callers.
                // Without explicit: keep list origin (home/search/…) so multi-hop returns
                // still land on the original card, not an intermediate detail.
                previousSourceRoute
                    ?.takeIf { it.isNotBlank() }
                    ?: currentKey.sourceRoute
                    ?: "video/${currentKey.bvid}"
            }
            null -> previousSourceRoute
            else -> currentKey.toLegacyRoute()
        }
    )
    return BiliPaiVideoSource(
        route = route,
        key = route?.takeIf { bvid.isNotBlank() }?.let { "$it:$bvid" }
    )
}

internal fun normalizeBiliPaiVideoSourceRoute(route: String?): String? {
    val normalized = route?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (normalized.startsWith("home?category=")) {
        normalized
    } else {
        normalized.substringBefore("?")
    }
}

/**
 * Related-detail hops use source route `video/{parentBvid}` (cover left, text right).
 * Same morph **contract** as home/category (分区); only the landing layout differs
 * ([VideoCardSourceLayout.SIDE_BY_SIDE] vs STACKED).
 */
internal fun isRelatedVideoCardMorphSourceRoute(sourceRoute: String?): Boolean {
    val route = sourceRoute?.substringBefore('?')?.trim().orEmpty()
    return route.startsWith("video/")
}

/**
 * Whole-card Miuix morph gate — **home/category (分区) is the reference path**.
 *
 * Shared contract for every source (home, category, search, related, …):
 * 1. Click freezes cardBounds + coverBounds + layout + chrome snapshot
 * 2. Outer entry morphs host ↔ cardBounds (one opaque flying card)
 * 3. Flying entry draws shell + chrome; list stationary pixels stay alpha 0 until IDLE
 * 4. Inverse scale uses Nav host layout width (same as outer morph)
 * 5. Layout-specific landing only:
 *    - STACKED (分区/双列): cover top, info bottom, FillWidthTop media
 *    - SIDE_BY_SIDE (相关/横卡): cover left, info right, CropCenter media
 */
internal fun shouldUseMiuixVideoCardMorph(
    cardTransitionEnabled: Boolean,
    reduceMotion: Boolean,
    sourceRoute: String?,
    hasUsableSourceBounds: Boolean,
): Boolean = cardTransitionEnabled &&
    !reduceMotion &&
    !sourceRoute?.substringBefore('?').isNullOrBlank() &&
    hasUsableSourceBounds
