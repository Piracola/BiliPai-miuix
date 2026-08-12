package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.transition.VIDEO_CARD_SHELL_SOURCE_EXIT_FADE_RATIO
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.normalizeSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.resolveVideoCardLiveReturnVisualHandoffAlpha
import com.android.purebilibili.core.ui.transition.resolveVideoCardReturnListCoverContract

internal data class VideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showCoverGradientMask: Boolean,
    val showHistoryProgressBar: Boolean,
    val showCompactStatsOnCover: Boolean,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean,
    compactStatsOnCover: Boolean
): VideoCardScrollLiteVisualPolicy {
    if (scrollLiteModeEnabled) {
        return VideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showCoverGradientMask = false,
            showHistoryProgressBar = false,
            showCompactStatsOnCover = compactStatsOnCover,
            showSecondaryStatsRow = !compactStatsOnCover
        )
    }

    return VideoCardScrollLiteVisualPolicy(
        coverShadowElevationDp = 0f,
        // 统计信息移到封面外时也不需要暗渐变；保持静止和滚动状态一致，避免整批封面明暗闪烁。
        showCoverGradientMask = false,
        showHistoryProgressBar = true,
        showCompactStatsOnCover = compactStatsOnCover,
        showSecondaryStatsRow = !compactStatsOnCover
    )
}

/**
 * 列表封面是否允许 Coil crossfade。
 * 契约收口到 [resolveVideoCardReturnListCoverContract]（[VideoCardReturnTimeline]）。
 */
internal fun shouldEnableVideoCardCoverCrossfade(
    isScrollInProgress: Boolean,
    isReturningFromDetail: Boolean,
    useCoverSharedBounds: Boolean,
    isSharedReturnTarget: Boolean
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedReturnTarget,
    isScrollInProgress = isScrollInProgress,
    isReturningFromDetail = isReturningFromDetail,
    useCoverSharedBounds = useCoverSharedBounds,
).enableCoilCrossfade

/**
 * shared 返回目标卡是否应钉住封面 URL/缓存键。
 * 契约收口到 [resolveVideoCardReturnListCoverContract]。
 */
internal fun shouldPinVideoCardCoverForSharedReturn(
    isSharedReturnTarget: Boolean,
): Boolean = resolveVideoCardReturnListCoverContract(
    isSharedReturnTarget = isSharedReturnTarget,
    isScrollInProgress = false,
    isReturningFromDetail = false,
    useCoverSharedBounds = true,
).pinCoverSource

/**
 * Miuix predictive return may enter its settle while the legacy clock still reports HELD.
 * A moving shared-bounds target in any non-opening phase is therefore also a return context.
 */
internal fun isVideoCardFlyingReturnContext(
    isReturningFromDetail: Boolean,
    isVideoCardReturnGestureInProgress: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isSharedTransitionActive: Boolean,
    transitionBackgroundProgress: Float,
): Boolean {
    val nonOpeningMorphActive =
        transitionBackgroundPhase != VideoCardTransitionBackgroundPhase.OPENING &&
            (isSharedTransitionActive ||
                transitionBackgroundProgress.coerceIn(0f, 1f) < 0.999f)
    return isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING ||
        nonOpeningMorphActive
}

/**
 * Stationary list-card **cover** while a Miuix flying entry owns the morph.
 *
 * Cover pixels stay on the flying media path until park; list cover stays 0 to avoid dual image.
 * Title / UP / stats are drawn on the flying entry during morph (list chrome also stays 0).
 *
 * @return 0 while flying owns cover, 1 when the list cover may show again (or whole-card fallback).
 */
internal fun resolveHomeCardStationaryRevealAlpha(
    isReturnContext: Boolean,
    preferWholeCardReturn: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
    isSharedTransitionActive: Boolean,
    transitionBackgroundProgress: Float,
): Float {
    if (!isReturnContext || preferWholeCardReturn) return 1f
    if (isVideoCardReturnGestureInProgress || isSharedTransitionActive) return 0f
    return when (transitionBackgroundPhase) {
        VideoCardTransitionBackgroundPhase.OPENING,
        VideoCardTransitionBackgroundPhase.RETURNING,
        -> 0f
        VideoCardTransitionBackgroundPhase.HELD ->
            // Predictive seek keeps HELD while depth < 1; only full detail rest may show list cover.
            if (transitionBackgroundProgress.coerceIn(0f, 1f) < 0.999f) 0f else 1f
        VideoCardTransitionBackgroundPhase.IDLE -> 1f
    }
}

/**
 * 来源卡封面在返回期间的可见 alpha。
 *
 * 保留图片请求与布局；Miuix 飞行 entry 拥有像素时列表封面保持 0，卸层后再亮，
 * 避免与飞行层同窗双显导致落位跳动。
 */
internal fun resolveHomeCardReturnSourceVisualAlpha(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float,
    preferWholeCardReturn: Boolean = false,
): Float {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) return 1f
    val isReturnContext = isVideoCardFlyingReturnContext(
        isReturningFromDetail = isReturningFromDetail,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
    )
    return resolveHomeCardStationaryRevealAlpha(
        isReturnContext = isReturnContext,
        preferWholeCardReturn = preferWholeCardReturn,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
    )
}

internal data class HorizontalCardChromeMotionFrame(
    val alpha: Float,
    /** 0=原位，1=向详情方向完成短距离跟随。 */
    val translationProgress: Float,
)

/**
 * 横卡 chrome 与 shell 共用主进度。
 *
 * 打开前 28% 上移并淡出；返回时不再额外位移，使用早于封面的文字形变窗口。
 */
internal fun resolveHorizontalCardChromeMotionFrame(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean = false,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
    preferWholeCardReturn: Boolean = false,
): HorizontalCardChromeMotionFrame {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) {
        return HorizontalCardChromeMotionFrame(alpha = 1f, translationProgress = 0f)
    }
    val isReturnContext = isVideoCardFlyingReturnContext(
        isReturningFromDetail = isReturningFromDetail,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
    )
    if (isReturnContext) {
        return HorizontalCardChromeMotionFrame(
            alpha = resolveHomeCardStationaryRevealAlpha(
                isReturnContext = true,
                preferWholeCardReturn = preferWholeCardReturn,
                transitionBackgroundPhase = transitionBackgroundPhase,
                isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
                isSharedTransitionActive = isSharedTransitionActive,
                transitionBackgroundProgress = transitionBackgroundProgress,
            ),
            translationProgress = 0f,
        )
    }
    if (transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.OPENING) {
        val exitProgress = (
            transitionBackgroundProgress.coerceIn(0f, 1f) /
                VIDEO_CARD_SHELL_SOURCE_EXIT_FADE_RATIO
            ).coerceIn(0f, 1f)
        return HorizontalCardChromeMotionFrame(
            alpha = 1f - exitProgress,
            translationProgress = exitProgress,
        )
    }
    if (
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.HELD ||
        isSharedTransitionActive
    ) {
        return HorizontalCardChromeMotionFrame(alpha = 0f, translationProgress = 1f)
    }
    return HorizontalCardChromeMotionFrame(alpha = 1f, translationProgress = 0f)
}

/**
 * 返回 shell morph 期间源卡 **chrome**（标题/UP/信息区）的 alpha。
 *
 * 规则：
 * - 非源卡 / 无 shell：恒 1
 * - 进场（OPENING 或 shared 进行中且非返回）：0，避免字叠播放器
 * - 返回 + 飞行壳：列表原位 **0**（飞行层盖住列表，真卡露不出来）；字在飞行壳绘制
 * - morph 结束后再亮列表真卡
 * - preferWholeCardReturn：列表立即完整显示
 */
internal fun resolveHomeCardChromeAlphaDuringShellReturnMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
    preferWholeCardReturn: Boolean = false,
): Float {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) return 1f

    val isReturnContext = isVideoCardFlyingReturnContext(
        isReturningFromDetail = isReturningFromDetail,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
    )

    if (isReturnContext) {
        return resolveHomeCardStationaryRevealAlpha(
            isReturnContext = true,
            preferWholeCardReturn = preferWholeCardReturn,
            transitionBackgroundPhase = transitionBackgroundPhase,
            isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
            isSharedTransitionActive = isSharedTransitionActive,
            transitionBackgroundProgress = transitionBackgroundProgress,
        )
    }

    // 进场：shared 飞行或 OPENING 时藏字
    if (
        isSharedTransitionActive ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.OPENING
    ) {
        return 0f
    }
    return 1f
}

/**
 * 兼容旧布尔语义：chrome 尚未完全露出版视为仍在抑制。
 */
internal fun shouldSuppressHomeCardVisualDuringShellReturnMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase =
        VideoCardTransitionBackgroundPhase.IDLE,
    isVideoCardReturnGestureInProgress: Boolean = false,
    isSharedTransitionActive: Boolean = false,
    transitionBackgroundProgress: Float = 0f,
    isQuickReturnFromDetail: Boolean = false,
    preferWholeCardReturn: Boolean = false,
): Boolean {
    return resolveHomeCardChromeAlphaDuringShellReturnMorph(
        useCardContainerSharedBounds = useCardContainerSharedBounds,
        isSharedMorphSourceCard = isSharedMorphSourceCard,
        isReturningFromDetail = isReturningFromDetail,
        transitionBackgroundPhase = transitionBackgroundPhase,
        isVideoCardReturnGestureInProgress = isVideoCardReturnGestureInProgress,
        isSharedTransitionActive = isSharedTransitionActive,
        transitionBackgroundProgress = transitionBackgroundProgress,
        isQuickReturnFromDetail = isQuickReturnFromDetail,
        preferWholeCardReturn = preferWholeCardReturn,
    ) < 1f
}

internal fun normalizeVideoCardSourceRouteForSharedKey(sourceRoute: String?): String? {
    return normalizeSharedElementSourceRoute(sourceRoute)
}

internal fun resolveVideoCardSharedReturnTargetKey(
    bvid: String,
    sourceRoute: String?,
): String? {
    val normalizedBvid = bvid.trim()
    val normalizedRoute = normalizeVideoCardSourceRouteForSharedKey(sourceRoute) ?: return null
    if (normalizedBvid.isEmpty()) return null
    return "$normalizedRoute:$normalizedBvid"
}

internal fun isVideoCardSharedReturnTarget(
    bvid: String,
    sourceRoute: String?,
    lastClickedVideoSourceKey: String?,
): Boolean {
    val key = resolveVideoCardSharedReturnTargetKey(bvid, sourceRoute) ?: return false
    return key == lastClickedVideoSourceKey
}

internal data class StoryVideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveStoryVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean
): StoryVideoCardScrollLiteVisualPolicy {
    return if (scrollLiteModeEnabled) {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    } else {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    }
}
