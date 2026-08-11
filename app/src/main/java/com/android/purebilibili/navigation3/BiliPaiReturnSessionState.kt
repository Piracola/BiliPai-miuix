package com.android.purebilibili.navigation3

private const val QUICK_RETURN_THRESHOLD_MILLIS = 500L
private const val RELATED_RETURN_SOURCE_RESTORE_SETTLE_BUFFER_MILLIS = 32L

/**
 * Related-video pop must keep its related-card geometry until Miuix finishes settling the entry.
 * Restoring the earlier list source sooner retargets the same outgoing transition mid-flight.
 */
internal fun resolveRelatedReturnSourceRestoreDelayMillis(
    cardTransitionEnabled: Boolean,
    reduceMotion: Boolean,
    transitionDurationMillis: Int,
): Long = if (cardTransitionEnabled && !reduceMotion) {
    transitionDurationMillis.coerceAtLeast(0).toLong() +
        RELATED_RETURN_SOURCE_RESTORE_SETTLE_BUFFER_MILLIS
} else {
    0L
}

internal data class BiliPaiReturnSessionState(
    val isReturningFromDetail: Boolean = false,
    val isQuickReturnFromDetail: Boolean = false,
    val lastVideoSourceRoute: String? = null,
    val lastVideoSourceKey: String? = null,
    /**
     * Dual-column left/right origin captured at detail enter. Used when live
     * CardPositionManager state is gone or key-matching fails on pop.
     */
    val lastCardSourceDirection: BiliPaiNavCardSourceDirection =
        BiliPaiNavCardSourceDirection.NONE,
    val transitionSession: VideoCardTransitionSession? = null,
    /** 每次详情压详情前冻结当前来源，pop settle 后逐层恢复。 */
    val previousVideoSources: List<BiliPaiVideoSource> = emptyList(),
    val previousTransitionSessions: List<VideoCardTransitionSession> = emptyList(),
    val detailEnteredAtMillis: Long? = null
) {
    fun recordVideoSource(source: BiliPaiVideoSource): BiliPaiReturnSessionState {
        val relatedDetailSource = source.route?.substringBefore("?")?.startsWith("video/") == true
        val previousSource = BiliPaiVideoSource(lastVideoSourceRoute, lastVideoSourceKey)
        val updatedHistory = if (relatedDetailSource && previousSource.route != null) {
            previousVideoSources + previousSource
        } else if (relatedDetailSource) {
            previousVideoSources
        } else {
            emptyList()
        }
        return copy(
            lastVideoSourceRoute = source.route,
            lastVideoSourceKey = source.key,
            previousVideoSources = updatedHistory,
        )
    }

    fun recordTransitionSession(
        session: VideoCardTransitionSession,
    ): BiliPaiReturnSessionState {
        val relatedDetailSource = session.sourceRoute
            ?.substringBefore("?")
            ?.startsWith("video/") == true
        val currentSession = transitionSession
        val updatedSessionHistory = if (relatedDetailSource && currentSession != null) {
            previousTransitionSessions + currentSession
        } else if (relatedDetailSource) {
            previousTransitionSessions
        } else {
            emptyList()
        }
        val currentSource = BiliPaiVideoSource(lastVideoSourceRoute, lastVideoSourceKey)
        val updatedSourceHistory = if (relatedDetailSource && currentSource.route != null) {
            previousVideoSources + currentSource
        } else if (relatedDetailSource) {
            previousVideoSources
        } else {
            emptyList()
        }
        return copy(
            lastVideoSourceRoute = session.sourceRoute,
            lastVideoSourceKey = session.sourceKey,
            lastCardSourceDirection = session.cardSourceDirection,
            transitionSession = session,
            previousVideoSources = updatedSourceHistory,
            previousTransitionSessions = updatedSessionHistory,
        )
    }

    fun recordCardSourceDirection(
        direction: BiliPaiNavCardSourceDirection
    ): BiliPaiReturnSessionState {
        return copy(lastCardSourceDirection = direction)
    }

    fun restorePreviousVideoSourceAfterRelatedReturn(): BiliPaiReturnSessionState {
        val restoredSession = previousTransitionSessions.lastOrNull()
        val restoredSource = previousVideoSources.lastOrNull()
        if (restoredSession == null && restoredSource == null) return this
        return copy(
            lastVideoSourceRoute = restoredSession?.sourceRoute ?: restoredSource?.route,
            lastVideoSourceKey = restoredSession?.sourceKey ?: restoredSource?.key,
            lastCardSourceDirection = restoredSession?.cardSourceDirection
                ?: lastCardSourceDirection,
            transitionSession = restoredSession,
            previousVideoSources = previousVideoSources.dropLast(1),
            previousTransitionSessions = previousTransitionSessions.dropLast(1),
        )
    }

    fun recordVideoSourceRoute(sourceRoute: String?): BiliPaiReturnSessionState {
        return copy(
            lastVideoSourceRoute = normalizeBiliPaiVideoSourceRoute(sourceRoute),
            lastVideoSourceKey = null,
            transitionSession = null,
            previousVideoSources = emptyList(),
            previousTransitionSessions = emptyList(),
        )
    }

    fun markDetailEntered(nowMillis: Long): BiliPaiReturnSessionState {
        return copy(
            isReturningFromDetail = false,
            isQuickReturnFromDetail = false,
            detailEnteredAtMillis = nowMillis
        )
    }

    fun markReturning(nowMillis: Long): BiliPaiReturnSessionState {
        val elapsed = detailEnteredAtMillis?.let { nowMillis - it } ?: Long.MAX_VALUE
        return copy(
            isReturningFromDetail = true,
            isQuickReturnFromDetail = elapsed in 0L..QUICK_RETURN_THRESHOLD_MILLIS
        )
    }

    fun clearReturning(): BiliPaiReturnSessionState {
        return copy(
            isReturningFromDetail = false,
            isQuickReturnFromDetail = false
        )
    }
}
