package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.transition.VIDEO_CARD_SHELL_SOURCE_EXIT_FADE_RATIO
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.normalizeSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.resolveVideoCardLiveReturnVisualHandoffAlpha
import com.android.purebilibili.core.ui.transition.resolveVideoCardReturnListCoverContract
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeReturnAlpha

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
 * 来源卡封面在返回期间的可见 alpha。
 *
 * 保留图片请求、缓存和布局以避免卸层黑闪，但 LIVE surface 主导时不绘制来源卡像素；
 * 到最后交接窗口才与详情侧常驻封面使用同一 alpha 一起落位；正文另走提前回显窗口。
 */
internal fun resolveHomeCardReturnSourceVisualAlpha(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
    transitionBackgroundProgress: Float,
    preferWholeCardReturn: Boolean = false,
): Float {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard) return 1f
    val isReturnContext = isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING
    if (!isReturnContext || preferWholeCardReturn) return 1f
    return resolveVideoCardLiveReturnVisualHandoffAlpha(
        morphDepthProgress = transitionBackgroundProgress,
    )
}

internal data class HorizontalCardChromeMotionFrame(
    val alpha: Float,
    /** 0=原位，1=向详情方向完成短距离跟随。 */
    val translationProgress: Float,
)

/**
 * 横卡 chrome 与 shell 共用主进度，但不进入共享 overlay。
 *
 * 打开前 28% 上移并淡出；返回时不再额外位移，alpha 与来源封面使用同一交接窗口。
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
    val isReturnContext = isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING
    if (isReturnContext) {
        return HorizontalCardChromeMotionFrame(
            alpha = if (preferWholeCardReturn) {
                1f
            } else {
                resolveVideoCardSourceChromeReturnAlpha(transitionBackgroundProgress)
            },
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
 * 规则（以代码为准，不依赖「morph 结束硬切 1」）：
 * - 非源卡 / 无 shell：恒 1
 * - 进场（OPENING 或 shared 进行中且非返回）：0，避免字叠播放器
 * - 返回上下文：正文使用独立的 68%–94% 落位窗口，在封面像素交接前开始恢复
 * - 整卡回退（关闭 live 预览）可直接全显
 *
 * 正文与封面使用同一 morph 时钟，但各自拥有明确的交接窗口；不再依赖转场结束硬切。
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

    val isReturnContext = isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING

    if (isReturnContext) {
        return if (preferWholeCardReturn) {
            1f
        } else {
            resolveVideoCardSourceChromeReturnAlpha(transitionBackgroundProgress)
        }
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
