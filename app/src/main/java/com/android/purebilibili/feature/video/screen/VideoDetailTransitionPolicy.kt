package com.android.purebilibili.feature.video.screen

import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase

import androidx.compose.animation.core.Easing
import com.android.purebilibili.core.ui.transition.VIDEO_CARD_RETURN_LIVE_CONTENT_YIELD_START
import com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.isVideoCardLiveReturnMorphOwnership
import com.android.purebilibili.core.ui.transition.resolveReturnSessionLockedCoverOwnership
import com.android.purebilibili.core.ui.transition.resolveVideoCardLiveMorphSecondaryContentAlpha
import com.android.purebilibili.core.ui.transition.resolveVideoCardReturnCoverOwnership
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionEnterEasing
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionReturnEasing
import com.android.purebilibili.core.ui.transition.shouldHandVisualOwnershipToResidentCoverForOwnership
import com.android.purebilibili.core.ui.transition.shouldUseVideoCardLiveReturnMorph

private const val COVER_TAKEOVER_PRE_BACK_DELAY_MILLIS = 0L
/** 已提交返回的最后 12%：实时画面淡到驻留封面，掩盖 shared overlay 卸层。 */
private const val LIVE_RETURN_COVER_HANDOFF_START = 0.88f
internal const val VIDEO_CONTENT_COMMENT_TAB_INDEX = 1

internal fun resolveForceCoverOnlyForReturn(
    forceCoverOnlyOnReturn: Boolean,
    transitionEnabled: Boolean = true,
    isCardReturnExitInProgress: Boolean = false
): Boolean {
    if (!transitionEnabled || isCardReturnExitInProgress) return false
    return forceCoverOnlyOnReturn
}

/**
 * 返回「离开态」（次要内容淡出、标记离开等）。
 * 不等于封面接管：ImmediatePlayback 的 live morph 路径下播放器应保持可见。
 *
 * 预测拖动未提交时 isSessionReturningToCard 应为 false（尚未 markReturning）。
 */
internal fun shouldUseReturningVideoDetailVisualState(
    forceCoverOnlyForReturn: Boolean,
    isCardReturnExitInProgress: Boolean = false,
    isSessionReturningToCard: Boolean = false,
): Boolean {
    return forceCoverOnlyForReturn ||
        isCardReturnExitInProgress ||
        isSessionReturningToCard
}

/**
 * 是否已**提交**卡片返回（可与封面做 landing handoff）。
 *
 * 与 [shouldUseReturningVideoDetailVisualState] 不同：
 * - 预测返回 seek 中 `targetState=PostExit` 会让离开态为 true，但尚未松手提交
 * - 封面/播放器 alpha 的 handoff **只认提交**，否则手势一开始封面会盖死实时画面
 *
 * 提交信号：按钮返回 [isActuallyLeaving]，或导航层 [isSessionReturningToCard]（markReturning）。
 */
internal fun shouldTreatVideoDetailCardReturnAsCommitted(
    isActuallyLeaving: Boolean,
    isSessionReturningToCard: Boolean,
): Boolean {
    return isActuallyLeaving || isSessionReturningToCard
}

/**
 * 详情 → 来源卡片 sharedBounds：实时画面跟手缩小（一镜到底）。
 * 实现收口到 [shouldUseVideoCardLiveReturnMorph]（[VideoCardReturnTimeline]）。
 */
internal fun shouldUseLiveReturnMorph(
    transitionEnabled: Boolean,
    sharedBoundsActive: Boolean,
    keepLoadedContentForBackPreview: Boolean,
    playbackIntent: VideoSharedTransitionPlaybackIntent,
    detailContentReady: Boolean = true,
    hasRenderableLiveFrame: Boolean = true,
): Boolean = shouldUseVideoCardLiveReturnMorph(
    transitionEnabled = transitionEnabled,
    sharedBoundsActive = sharedBoundsActive,
    keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
    playbackIntent = playbackIntent,
    detailContentReady = detailContentReady,
    hasRenderableLiveFrame = hasRenderableLiveFrame,
)

/**
 * 详情页下方推荐/简介等是否已可安全参与 live morph。
 * Loading / 错误态仍可能画骨架或空壳，快速返回时不能当「实时组件」。
 */
internal fun shouldTreatVideoDetailContentReadyForLiveReturnMorph(
    hasSuccessfulDetailContent: Boolean,
): Boolean = hasSuccessfulDetailContent

/**
 * 入口播放意图只决定首次展开的视觉路径。
 * 当前视频已有可靠首帧后，返回应优先保留实时 surface，不能再退回静态封面。
 */
internal fun resolveVideoDetailReturnPlaybackIntent(
    entryPlaybackIntent: VideoSharedTransitionPlaybackIntent,
    hasRenderableLiveFrame: Boolean,
): VideoSharedTransitionPlaybackIntent {
    return if (hasRenderableLiveFrame) {
        VideoSharedTransitionPlaybackIntent.ImmediatePlayback
    } else {
        entryPlaybackIntent
    }
}

/**
 * 是否把视觉主导权交给常驻封面（forceCover / 藏 surface）。
 * live morph 时必须为 false，否则会出现「先切封面再缩小」。
 * ownership 真相见 [resolveVideoCardReturnCoverOwnership]。
 */
internal fun shouldHandVisualOwnershipToResidentCover(
    useReturningVisualState: Boolean,
    hasResidentCover: Boolean,
    liveReturnMorph: Boolean,
): Boolean {
    // 保持与 timeline 一致：live 时永不把视觉交给封面
    if (liveReturnMorph) return false
    return useReturningVisualState && hasResidentCover
}

/**
 * 详情返回 ownership 表入口（供 StateHolder / 测试直接断言三条路径）。
 */
internal fun resolveVideoDetailReturnCoverOwnership(
    transitionEnabled: Boolean,
    sharedBoundsActive: Boolean,
    keepLoadedContentForBackPreview: Boolean,
    playbackIntent: VideoSharedTransitionPlaybackIntent,
    detailContentReady: Boolean,
    hasResidentCover: Boolean,
    hasRenderableLiveFrame: Boolean = true,
) = resolveVideoCardReturnCoverOwnership(
    transitionEnabled = transitionEnabled,
    sharedBoundsActive = sharedBoundsActive,
    keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
    playbackIntent = playbackIntent,
    detailContentReady = detailContentReady,
    hasResidentCover = hasResidentCover,
    hasRenderableLiveFrame = hasRenderableLiveFrame,
)

internal fun isLiveReturnMorphFromOwnership(
    ownership: VideoCardReturnCoverOwnership,
): Boolean = isVideoCardLiveReturnMorphOwnership(ownership)

internal fun shouldHandResidentCoverFromOwnership(
    ownership: VideoCardReturnCoverOwnership,
    useReturningVisualState: Boolean,
    hasResidentCover: Boolean,
): Boolean = shouldHandVisualOwnershipToResidentCoverForOwnership(
    ownership = ownership,
    useReturningVisualState = useReturningVisualState,
    hasResidentCover = hasResidentCover,
)

/**
 * 返回会话 ownership 锁定包装（供详情页 state 接线）。
 * @see resolveReturnSessionLockedCoverOwnership
 */
internal fun resolveVideoDetailReturnSessionLockedOwnership(
    lockedOwnership: VideoCardReturnCoverOwnership?,
    isReturnSessionActive: Boolean,
    candidateOwnership: VideoCardReturnCoverOwnership,
): Pair<VideoCardReturnCoverOwnership?, VideoCardReturnCoverOwnership> {
    return resolveReturnSessionLockedCoverOwnership(
        lockedOwnership = lockedOwnership,
        isReturnSessionActive = isReturnSessionActive,
        candidateOwnership = candidateOwnership,
    )
}

internal fun resolveVideoDetailReturnCoverAlpha(
    transitionProgress: Float,
    isCommittedCardReturn: Boolean,
    hasResidentCover: Boolean,
    liveReturnMorph: Boolean = false,
): Float {
    if (!hasResidentCover) return 0f
    // 一镜到底：仅 settle 末段抬封面，禁止一点返回就盖住实时播放器。
    if (liveReturnMorph) {
        return resolveVideoDetailLiveReturnLandingHandoffAlpha(
            transitionProgress = transitionProgress,
            isCommittedCardReturn = isCommittedCardReturn,
        )
    }
    val progress = transitionProgress.coerceIn(0f, 1f)
    // CoverFirst / 无 live 帧：提交后封面立即接管，避免黑壳。
    return if (isCommittedCardReturn) 1f else 1f - progress
}

internal fun resolveVideoDetailReturnPlayerAlpha(
    transitionProgress: Float,
    isCommittedCardReturn: Boolean,
    hasResidentCover: Boolean,
    liveReturnMorph: Boolean = false,
): Float {
    if (liveReturnMorph) {
        if (!hasResidentCover) return 1f
        return 1f - resolveVideoDetailLiveReturnLandingHandoffAlpha(
            transitionProgress = transitionProgress,
            isCommittedCardReturn = isCommittedCardReturn,
        )
    }
    if (isCommittedCardReturn) return if (hasResidentCover) 0f else 1f
    return transitionProgress.coerceIn(0f, 1f)
}

/**
 * live morph 保持实时画面缩回；仅已提交返回的最后一小段，与驻留封面交叉淡化。
 * 两层仍读同一个 shared transition progress，取消或预测返回不会产生独立补间。
 */
private fun resolveVideoDetailLiveReturnLandingHandoffAlpha(
    transitionProgress: Float,
    isCommittedCardReturn: Boolean,
): Float {
    if (!isCommittedCardReturn) return 0f
    val settle = 1f - transitionProgress.coerceIn(0f, 1f)
    return ((settle - LIVE_RETURN_COVER_HANDOFF_START) /
        (1f - LIVE_RETURN_COVER_HANDOFF_START)).coerceIn(0f, 1f)
}

internal fun resolveVideoDetailReturnContentAlpha(
    transitionProgress: Float,
    isCommittedCardReturn: Boolean,
    holdFullyOpaqueAfterBackPreview: Boolean = false,
    liveReturnMorph: Boolean = false,
    depthBlurProgress: Float? = null,
    isQuickReturn: Boolean = false,
    /**
     * 单时钟 morph 深度（[VideoCardTransitionClock.depthProgress]）：1=详情，0=列表。
     * 提供时 live morph 正文只跟这一路，与源卡 chrome settle 对齐。
     */
    morphDepthProgress: Float? = null,
): Float {
    // live morph：正文与源卡 chrome 共用 morphDepth settle，禁止 AVS/depth 双源 max 叠字。
    if (liveReturnMorph) {
        // 快速返回 + 已提交：源卡 chrome 立刻 1，详情正文立刻 0。
        if (isCommittedCardReturn && isQuickReturn) {
            return 0f
        }
        val yieldStart = if (isQuickReturn) {
            0f
        } else {
            VIDEO_CARD_RETURN_LIVE_CONTENT_YIELD_START
        }
        return resolveVideoCardLiveMorphSecondaryContentAlpha(
            transitionProgress = transitionProgress,
            depthBlurProgress = depthBlurProgress,
            yieldStart = yieldStart,
            morphDepthProgress = morphDepthProgress,
        )
    }
    if (isCommittedCardReturn) return 0f
    if (holdFullyOpaqueAfterBackPreview) return 1f
    return transitionProgress.coerceIn(0f, 1f)
}

internal fun shouldTreatVideoDetailCardExitAsReturning(
    isExitTransitionInProgress: Boolean,
    sharedBoundsActive: Boolean,
    keepLoadedContentForBackPreview: Boolean = false,
): Boolean {
    return isExitTransitionInProgress &&
        sharedBoundsActive &&
        !keepLoadedContentForBackPreview
}

/**
 * Whether the detail page should treat the current frame as an exit/return morph.
 *
 * Primary signal: AnimatedVisibility [EnterExitState.PostExit].
 * Fallback for Navigation3 1.2 + [ExitTransition.None]: AVS may settle without a durable
 * PostExit observation, while [VideoCardTransitionBackgroundPhase.RETURNING] is still true.
 */
internal fun shouldTreatVideoDetailExitTransitionInProgress(
    animatedVisibilityTargetIsPostExit: Boolean,
    videoCardBackgroundPhase: VideoCardTransitionBackgroundPhase?,
): Boolean {
    if (animatedVisibilityTargetIsPostExit) return true
    return videoCardBackgroundPhase == VideoCardTransitionBackgroundPhase.RETURNING
}

/**
 * 返回 morph 期间是否保活播放会话（surface）。
 *
 * 预测返回 **轻滑即松手** 时：栈先 pop → 入口 `isVisible=false`，但详情仍在
 * sharedBounds overlay 里缩回。若此时掐掉 playbackSession，surface 变黑，
 * 慢放可见「动画前半段卡片消失」；手指拖到一半再松手时，seek 阶段仍 visible，
 * 所以往往复现不了。
 *
 * 规则：栈顶可见 **或**（shell shared 且正在 exit transition）→ 保活。
 */
internal fun shouldKeepPlaybackSessionActiveForSharedReturnMorph(
    isVisible: Boolean,
    sharedBoundsActive: Boolean,
    isExitTransitionInProgress: Boolean,
): Boolean {
    if (isVisible) return true
    return sharedBoundsActive && isExitTransitionInProgress
}

internal fun shouldForceBackPreviewPlayerCover(
    keepLoadedContentForBackPreview: Boolean,
    bindLivePlayerForBackPreview: Boolean
): Boolean {
    return keepLoadedContentForBackPreview && !bindLivePlayerForBackPreview
}

/**
 * 相关推荐「详情压详情」返回：父页刚从 back-preview 恢复时，
 * 若立刻按进场过渡把内容 alpha 从 0 淡入，会整页闪一下（滚动位置仍在）。
 */
internal fun shouldSuppressVideoDetailEnterFadeAfterBackPreview(
    wasKeptAsBackPreview: Boolean,
    keepLoadedContentForBackPreview: Boolean,
): Boolean {
    return wasKeptAsBackPreview && !keepLoadedContentForBackPreview
}

internal fun shouldUseVideoDetailRootTransitionProgress(
    detailShellSharedBoundsEnabled: Boolean,
    hasAnimatedVisibilityScope: Boolean,
    keepLoadedContentForBackPreview: Boolean,
): Boolean {
    return detailShellSharedBoundsEnabled &&
        hasAnimatedVisibilityScope &&
        !keepLoadedContentForBackPreview
}

internal fun shouldShowVideoDetailContent(
    isTransitionFinished: Boolean,
    isLeaving: Boolean,
    rootTransitionOwnsContentAlpha: Boolean,
    keepContentVisibleAfterBackPreview: Boolean = false,
): Boolean {
    if (keepContentVisibleAfterBackPreview && !isLeaving) return true
    return isTransitionFinished && (!isLeaving || rootTransitionOwnsContentAlpha)
}

internal fun resolveCoverTakeoverDelayBeforeBackNavigationMillis(): Long {
    // 封面常驻并直接读取根过渡进度，不再需要先抢一帧切换封面再导航。
    return COVER_TAKEOVER_PRE_BACK_DELAY_MILLIS
}

internal data class VideoDetailRouteSheetMotion(
    val enabled: Boolean,
    val durationMillis: Int,
    val mainDurationMillis: Int,
    val settleDurationMillis: Int,
    val initialScale: Float,
    val initialTranslationYDp: Float,
    val initialCornerDp: Float,
    val initialBackgroundScrimAlpha: Float,
    val settleScaleDelta: Float,
    val settleTranslationDp: Float,
    val enterEasing: Easing,
    val returnEasing: Easing
)

internal enum class VideoDetailRouteSheetSettleDirection {
    None,
    Enter,
    Return
}

internal data class VideoDetailRouteSheetFrame(
    val scale: Float,
    val translationYDp: Float,
    val cornerDp: Float,
    val backgroundScrimAlpha: Float,
    val settleProgress: Float
)

internal data class VideoDetailSecondaryContentTiming(
    val enterDelayMillis: Int,
    val enterDurationMillis: Int,
    val returnDelayMillis: Int,
    val returnDurationMillis: Int
)

internal fun resolveVideoDetailSecondaryContentTiming(
    fullDurationMillis: Int,
    contentDelayMillis: Int,
    contentDurationMillis: Int,
): VideoDetailSecondaryContentTiming {
    val safeDuration = fullDurationMillis.coerceAtLeast(0)
    val safeEnterDelay = contentDelayMillis.coerceIn(0, safeDuration)
    val safeEnterDuration = contentDurationMillis
        .coerceAtLeast(0)
        .coerceAtMost(safeDuration - safeEnterDelay)
    val safeReturnDuration = contentDurationMillis.coerceIn(0, safeDuration)
    return VideoDetailSecondaryContentTiming(
        enterDelayMillis = safeEnterDelay,
        enterDurationMillis = safeEnterDuration,
        returnDelayMillis = 0,
        returnDurationMillis = safeReturnDuration
    )
}

internal data class VideoDetailMotionSpec(
    val entryPhaseDurationMillis: Int,
    val contentSwapFadeDurationMillis: Int,
    val contentRevealFadeDurationMillis: Int
)

private const val VIDEO_DETAIL_ENTRY_PHASE_MIN_DURATION_MILLIS = 120
private const val VIDEO_DETAIL_CONTENT_PHASE_MIN_DURATION_MILLIS = 180
private const val HOME_VIDEO_ROUTE_SHEET_MAIN_DURATION_MILLIS = 320
private const val HOME_VIDEO_ROUTE_SHEET_SETTLE_DURATION_MILLIS = 96
private const val HOME_VIDEO_ROUTE_SHEET_DURATION_MILLIS =
    HOME_VIDEO_ROUTE_SHEET_MAIN_DURATION_MILLIS + HOME_VIDEO_ROUTE_SHEET_SETTLE_DURATION_MILLIS
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_SCALE = 0.965f
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_TRANSLATION_Y_DP = 56f
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_CORNER_DP = 28f
private const val HOME_VIDEO_ROUTE_SHEET_INITIAL_SCRIM_ALPHA = 0.18f
private const val HOME_VIDEO_ROUTE_SHEET_SETTLE_SCALE_DELTA = 0.0015f
private const val HOME_VIDEO_ROUTE_SHEET_SETTLE_TRANSLATION_DP = 1.5f

internal fun resolveVideoDetailMotionSpec(
    transitionEnterDurationMillis: Int
): VideoDetailMotionSpec {
    return VideoDetailMotionSpec(
        entryPhaseDurationMillis = transitionEnterDurationMillis
            .coerceAtLeast(VIDEO_DETAIL_ENTRY_PHASE_MIN_DURATION_MILLIS),
        contentSwapFadeDurationMillis = transitionEnterDurationMillis
            .coerceAtLeast(VIDEO_DETAIL_CONTENT_PHASE_MIN_DURATION_MILLIS),
        contentRevealFadeDurationMillis = transitionEnterDurationMillis
            .coerceAtLeast(VIDEO_DETAIL_CONTENT_PHASE_MIN_DURATION_MILLIS)
    )
}

/**
 * 相关推荐卡片的 shell sharedBounds 嵌在父详情壳内。
 * 父壳若在子转场期间仍注册 sharedBounds，会嵌套劫持导致「点相关推荐没过渡」。
 * 仅在「本页是相关来源宿主 + 共享过渡进行中」时临时摘掉父壳。
 */
internal fun shouldSuppressDetailShellSharedBoundsForRelatedChildTransition(
    detailBvid: String,
    lastClickedVideoSourceKey: String?,
    isSharedTransitionActive: Boolean,
): Boolean {
    if (!isSharedTransitionActive) return false
    val normalizedBvid = detailBvid.trim()
    if (normalizedBvid.isEmpty()) return false
    val hostRoute = lastClickedVideoSourceKey
        ?.substringBefore(":")
        ?.substringBefore("?")
        ?.takeIf { it.isNotBlank() }
        ?: return false
    return hostRoute == "video/$normalizedBvid"
}

internal fun resolveVideoDetailRouteSheetMotion(
    sourceRoute: String?,
    transitionEnabled: Boolean
): VideoDetailRouteSheetMotion {
    val enabled = transitionEnabled &&
        com.android.purebilibili.navigation.isVideoCardReturnTargetRoute(sourceRoute)
    return VideoDetailRouteSheetMotion(
        enabled = enabled,
        durationMillis = HOME_VIDEO_ROUTE_SHEET_DURATION_MILLIS,
        mainDurationMillis = HOME_VIDEO_ROUTE_SHEET_MAIN_DURATION_MILLIS,
        settleDurationMillis = HOME_VIDEO_ROUTE_SHEET_SETTLE_DURATION_MILLIS,
        initialScale = HOME_VIDEO_ROUTE_SHEET_INITIAL_SCALE,
        initialTranslationYDp = HOME_VIDEO_ROUTE_SHEET_INITIAL_TRANSLATION_Y_DP,
        initialCornerDp = HOME_VIDEO_ROUTE_SHEET_INITIAL_CORNER_DP,
        initialBackgroundScrimAlpha = HOME_VIDEO_ROUTE_SHEET_INITIAL_SCRIM_ALPHA,
        settleScaleDelta = HOME_VIDEO_ROUTE_SHEET_SETTLE_SCALE_DELTA,
        settleTranslationDp = HOME_VIDEO_ROUTE_SHEET_SETTLE_TRANSLATION_DP,
        enterEasing = resolveVideoCardSharedTransitionEnterEasing(),
        returnEasing = resolveVideoCardSharedTransitionReturnEasing()
    )
}

internal fun resolveVideoDetailRouteSheetFrame(
    rawProgress: Float,
    settleProgress: Float = 0f,
    settleDirection: VideoDetailRouteSheetSettleDirection = VideoDetailRouteSheetSettleDirection.None,
    motion: VideoDetailRouteSheetMotion
): VideoDetailRouteSheetFrame {
    if (!motion.enabled) {
        return VideoDetailRouteSheetFrame(
            scale = 1f,
            translationYDp = 0f,
            cornerDp = 0f,
            backgroundScrimAlpha = 0f,
            settleProgress = 0f
        )
    }
    val progress = rawProgress.coerceIn(0f, 1f)
    val safeSettleProgress = settleProgress.coerceIn(0f, 1f)
    val settleScale = when (settleDirection) {
        VideoDetailRouteSheetSettleDirection.Enter -> motion.settleScaleDelta * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.Return -> -motion.settleScaleDelta * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.None -> 0f
    }
    val settleTranslation = when (settleDirection) {
        VideoDetailRouteSheetSettleDirection.Enter -> -motion.settleTranslationDp * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.Return -> motion.settleTranslationDp * safeSettleProgress
        VideoDetailRouteSheetSettleDirection.None -> 0f
    }
    return VideoDetailRouteSheetFrame(
        scale = lerpVideoDetailFloat(motion.initialScale, 1f, progress) + settleScale,
        translationYDp = lerpVideoDetailFloat(motion.initialTranslationYDp, 0f, progress) + settleTranslation,
        cornerDp = lerpVideoDetailFloat(motion.initialCornerDp, 0f, progress),
        backgroundScrimAlpha = lerpVideoDetailFloat(motion.initialBackgroundScrimAlpha, 0f, progress),
        settleProgress = safeSettleProgress
    )
}

private fun lerpVideoDetailFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
