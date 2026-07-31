package com.android.purebilibili.navigation3

private const val QUICK_RETURN_THRESHOLD_MILLIS = 500L

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
    /**
     * 进入相关推荐详情前的列表来源（如 home:BV_A）。
     * related 会把 last* 写成 video/BV_A:BV_B，pop 回父详情后需恢复，否则再回列表会丢共享元素。
     */
    val previousListVideoSourceRoute: String? = null,
    val previousListVideoSourceKey: String? = null,
    val transitionSession: VideoCardTransitionSession? = null,
    val previousListTransitionSession: VideoCardTransitionSession? = null,
    val detailEnteredAtMillis: Long? = null
) {
    fun recordVideoSource(source: BiliPaiVideoSource): BiliPaiReturnSessionState {
        val relatedDetailSource = source.route?.substringBefore("?")?.startsWith("video/") == true
        val preserveListSource = relatedDetailSource &&
            lastVideoSourceRoute?.substringBefore("?")?.startsWith("video/") != true
        return copy(
            previousListVideoSourceRoute = if (preserveListSource) {
                lastVideoSourceRoute
            } else {
                previousListVideoSourceRoute
            },
            previousListVideoSourceKey = if (preserveListSource) {
                lastVideoSourceKey
            } else {
                previousListVideoSourceKey
            },
            lastVideoSourceRoute = source.route,
            lastVideoSourceKey = source.key
        )
    }

    fun recordTransitionSession(
        session: VideoCardTransitionSession,
    ): BiliPaiReturnSessionState {
        val relatedDetailSource = session.sourceRoute
            ?.substringBefore("?")
            ?.startsWith("video/") == true
        val preserveListSource = relatedDetailSource &&
            transitionSession?.sourceRoute?.substringBefore("?")?.startsWith("video/") != true
        return copy(
            previousListVideoSourceRoute = if (preserveListSource) {
                transitionSession?.sourceRoute ?: lastVideoSourceRoute
            } else {
                previousListVideoSourceRoute
            },
            previousListVideoSourceKey = if (preserveListSource) {
                transitionSession?.sourceKey ?: lastVideoSourceKey
            } else {
                previousListVideoSourceKey
            },
            previousListTransitionSession = if (preserveListSource) {
                transitionSession
            } else {
                previousListTransitionSession
            },
            lastVideoSourceRoute = session.sourceRoute,
            lastVideoSourceKey = session.sourceKey,
            lastCardSourceDirection = session.cardSourceDirection,
            transitionSession = session,
        )
    }

    fun recordCardSourceDirection(
        direction: BiliPaiNavCardSourceDirection
    ): BiliPaiReturnSessionState {
        return copy(lastCardSourceDirection = direction)
    }

    fun restoreListVideoSourceAfterRelatedReturn(): BiliPaiReturnSessionState {
        val restoredRoute = previousListVideoSourceRoute ?: return this
        val restoredSession = previousListTransitionSession
        return copy(
            lastVideoSourceRoute = restoredSession?.sourceRoute ?: restoredRoute,
            lastVideoSourceKey = restoredSession?.sourceKey ?: previousListVideoSourceKey,
            lastCardSourceDirection = restoredSession?.cardSourceDirection
                ?: lastCardSourceDirection,
            transitionSession = restoredSession,
            previousListVideoSourceRoute = null,
            previousListVideoSourceKey = null,
            previousListTransitionSession = null,
        )
    }

    fun recordVideoSourceRoute(sourceRoute: String?): BiliPaiReturnSessionState {
        return copy(
            lastVideoSourceRoute = normalizeBiliPaiVideoSourceRoute(sourceRoute),
            lastVideoSourceKey = null,
            transitionSession = null,
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
