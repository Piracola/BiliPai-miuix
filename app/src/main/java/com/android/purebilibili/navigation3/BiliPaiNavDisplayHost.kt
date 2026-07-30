package com.android.purebilibili.navigation3

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplayTransitionEffects
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.navigationevent.NavigationEventTransitionState
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ProvideAnimatedVisibilityScope
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.performance.AppRuntimeVisualGuardTracker
import com.android.purebilibili.core.ui.performance.TrackJankStateValue
import com.android.purebilibili.core.ui.performance.VIDEO_CARD_TRANSITION_JANK_STATE
import com.android.purebilibili.core.ui.transition.LocalPredictiveBackBackgroundState
import com.android.purebilibili.core.ui.transition.LocalVideoCardMorphProgressReporter
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionClock
import com.android.purebilibili.core.ui.transition.LocalTransitionPerformanceSnapshot
import com.android.purebilibili.core.ui.transition.PREDICTIVE_BACK_BACKGROUND_CANCEL_DURATION_MS
import com.android.purebilibili.core.ui.transition.PREDICTIVE_BACK_EXIT_SETTLE_DURATION_MS
import com.android.purebilibili.core.ui.transition.PredictiveBackBackgroundState
import com.android.purebilibili.core.ui.transition.VIDEO_CARD_TRANSITION_BACKGROUND_CANCEL_DURATION_MS
import com.android.purebilibili.core.ui.transition.VideoCardMorphProgressReporter
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionClock
import com.android.purebilibili.core.ui.transition.resolveMorphAlignedFallbackDurationMs
import com.android.purebilibili.core.ui.transition.resolvePredictiveBackCommitBlurDurationMs
import com.android.purebilibili.core.ui.transition.resolvePredictiveBackGestureBlurProgress
import com.android.purebilibili.core.ui.transition.resolveVideoCardTimelineSpec
import com.android.purebilibili.core.ui.transition.resolveVideoCardTransitionReturnFullDurationMillis
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedMorphRemainingDurationMs
import com.android.purebilibili.core.ui.transition.TransitionPerformanceInputs
import com.android.purebilibili.core.ui.transition.TransitionThermalLevel
import com.android.purebilibili.core.ui.transition.resolveTransitionPerformanceSnapshot
import com.android.purebilibili.core.ui.transition.isVideoCardTransitionBackgroundGesturePhase
import com.android.purebilibili.core.ui.transition.shouldApplyPredictiveBackGestureBlur
import com.android.purebilibili.core.ui.transition.shouldShowVideoCardTransitionNavBackdrop
import com.android.purebilibili.core.ui.transition.shouldSnapClearVideoCardDepthBlurOnQuickReturn
import com.android.purebilibili.core.ui.transition.VideoCardTransitionNavBackdrop
import com.android.purebilibili.feature.settings.isSettingsSubtreeNavKey
import com.android.purebilibili.navigation.isVideoCardReturnTargetRoute
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationHandler
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiAutoPredictiveBackExitDirection
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackAnimationHandler
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackExitDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.os.Build
import android.os.PowerManager

internal class BiliPaiProgrammaticBackDispatcher {
    private var callback: (() -> Unit)? = null

    fun register(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregister(callback: () -> Unit) {
        if (this.callback === callback) {
            this.callback = null
        }
    }

    fun dispatch(): Boolean {
        val action = callback ?: return false
        action()
        return true
    }
}

@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: List<BiliPaiNavKey>,
    cardTransitionEnabled: Boolean = true,
    videoCardDepthEffectEnabled: Boolean = cardTransitionEnabled,
    reduceMotion: Boolean = false,
    videoSharedTransitionDurationMillis: Int,
    videoCardClock: VideoCardTransitionClock,
    predictiveBackEnabled: Boolean = true,
    predictiveBackAnimationStyle: BiliPaiPredictiveBackAnimationStyle = BiliPaiPredictiveBackAnimationStyle.SCALE,
    predictiveBackExitDirectionOverride: String = "auto",
    sourceMetadata: BiliPaiNavSourceMetadata,
    programmaticBackDispatcher: BiliPaiProgrammaticBackDispatcher,
    onBack: () -> Unit,
    onNativeVideoBackProgress: (currentKey: BiliPaiNavKey?, targetKey: BiliPaiNavKey?, progress: Float) -> Unit = { _, _, _ -> },
    onNativeVideoBackCancelled: (currentKey: BiliPaiNavKey?, targetKey: BiliPaiNavKey?) -> Unit = { _, _ -> },
    isQuickReturnFromDetail: Boolean = false,
    /**
     * 为 true 时源卡标题与封面同步落位（不走 live 叠字延迟）。
     * 产品路径固定为实时预览，默认 false；仅供快速返回等内部场景。
     */
    preferWholeCardReturn: Boolean = false,
    /**
     * 系统/预测返回在启动景深收尾前调用：标记返回会话并返回是否快速返回。
     * performBack 里 onBack 更晚，不能只靠 [isQuickReturnFromDetail] 快照。
     */
    onPrepareVideoCardSharedReturn: () -> Boolean = { isQuickReturnFromDetail },
    /**
     * 从相关推荐详情 pop 回父详情后回调：恢复进入 related 前的列表来源 session/key。
     */
    onRelatedVideoDetailReturned: () -> Unit = {},
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    visibleBottomBarRoutes: Set<String> = emptySet(),
    activeMainHostRoute: String? = null,
    isLightBackground: Boolean = false,
    content: @Composable (BiliPaiNavKey) -> Unit
) {
    val safeBackStack = remember(backStack) {
        backStack.ifEmpty { listOf(BiliPaiNavKey.MainHost) }
    }
    val application = LocalContext.current.applicationContext as Application
    var navigationEventState: NavigationEventState<SceneInfo<BiliPaiNavKey>>? = null
    val navigationScope = rememberCoroutineScope()
    // 根层单时钟：景深 / chrome / 壁纸只读 clock.depthProgress()；shared morph 回灌优先。
    val timelineSpec = remember(videoSharedTransitionDurationMillis) {
        resolveVideoCardTimelineSpec(videoSharedTransitionDurationMillis)
    }
    val predictiveBackBackgroundProgress = remember { Animatable(0f) }
    val isQuickReturnFromDetailUpdated by rememberUpdatedState(isQuickReturnFromDetail)
    var videoCardReturnGestureInProgress by remember { mutableStateOf(false) }
    // fallback Animatable 唯一 owner：OPENING / RETURNING / cancel 互斥。
    var videoCardDepthAnimationJob by remember { mutableStateOf<Job?>(null) }
    var navigationBackJob by remember { mutableStateOf<Job?>(null) }
    fun cancelVideoCardDepthAnimation() {
        videoCardDepthAnimationJob?.cancel()
        videoCardDepthAnimationJob = null
    }
    fun launchVideoCardDepthAnimation(block: suspend () -> Unit) {
        cancelVideoCardDepthAnimation()
        var job: Job? = null
        job = navigationScope.launch {
            try {
                block()
            } finally {
                if (videoCardDepthAnimationJob === job) {
                    videoCardDepthAnimationJob = null
                }
            }
        }
        videoCardDepthAnimationJob = job
    }
    val morphProgressReporter = remember(videoCardClock) {
        VideoCardMorphProgressReporter { morphFraction, active ->
            videoCardClock.reportSharedMorphProgress(
                morphFraction = morphFraction,
                active = active,
            )
        }
    }
    val videoCardBackgroundProgressProvider = remember(videoCardClock) {
        { videoCardClock.depthProgress() }
    }
    val runtimeGuardDecision by
        AppRuntimeVisualGuardTracker.decision.collectAsStateWithLifecycle()
    val videoCardTransitionJankState = if (!videoCardDepthEffectEnabled) {
        null
    } else {
        when {
            videoCardReturnGestureInProgress -> "PredictiveReturn"
            videoCardClock.gestureRestoreInProgress -> "GestureRestore"
            videoCardClock.phase == VideoCardTransitionBackgroundPhase.OPENING -> "Opening"
            videoCardClock.phase == VideoCardTransitionBackgroundPhase.RETURNING -> "Returning"
            else -> null
        }
    }
    TrackJankStateValue(
        stateName = VIDEO_CARD_TRANSITION_JANK_STATE,
        stateValue = videoCardTransitionJankState,
    )
    // 默认保留完整 12dp 景深；系统减弱动画或连续转场掉帧时降为 scrim-only。
    val transitionBackgroundMotionTier =
        if (reduceMotion) MotionTier.Reduced else runtimeGuardDecision.effectiveMotionTier
    val powerManager = remember(application) {
        application.getSystemService(Context.POWER_SERVICE) as? PowerManager
    }
    fun captureTransitionPerformanceSnapshot() = resolveTransitionPerformanceSnapshot(
        TransitionPerformanceInputs(
            motionTier = transitionBackgroundMotionTier,
            systemAnimationsEnabled = !reduceMotion,
            powerSaveMode = powerManager?.isPowerSaveMode == true,
            thermalLevel = when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> TransitionThermalLevel.NONE
                (powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE) >=
                    PowerManager.THERMAL_STATUS_CRITICAL -> TransitionThermalLevel.CRITICAL
                (powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE) >=
                    PowerManager.THERMAL_STATUS_SEVERE -> TransitionThermalLevel.SEVERE
                (powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE) >=
                    PowerManager.THERMAL_STATUS_MODERATE -> TransitionThermalLevel.MODERATE
                (powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE) >=
                    PowerManager.THERMAL_STATUS_LIGHT -> TransitionThermalLevel.LIGHT
                else -> TransitionThermalLevel.NONE
            },
            runtimeGuardDowngraded = runtimeGuardDecision.effectiveMotionTier == MotionTier.Reduced ||
                runtimeGuardDecision.forceLowBlurBudget,
        )
    )
    var previousVideoCardTransitionBackStack by remember {
        mutableStateOf(safeBackStack)
    }
    LaunchedEffect(
        safeBackStack,
        videoCardDepthEffectEnabled,
        videoSharedTransitionDurationMillis,
    ) {
        val previousStack = previousVideoCardTransitionBackStack
        val previousTop = previousStack.lastOrNull()
        val currentTop = safeBackStack.lastOrNull()
        val openingSourceRoute = resolveCardMorphDestinationSourceRoute(currentTop)
        val returningSourceRoute = resolveCardMorphDestinationSourceRoute(previousTop)
        val openedVideoDetail = isCardMorphDestinationNavKey(currentTop) &&
            safeBackStack.size > previousStack.size
        val returnedFromVideoDetail = isCardMorphDestinationNavKey(previousTop) &&
            safeBackStack.size < previousStack.size
        previousVideoCardTransitionBackStack = safeBackStack

        if (!videoCardDepthEffectEnabled) {
            cancelVideoCardDepthAnimation()
            videoCardReturnGestureInProgress = false
            videoCardClock.endGesture()
            videoCardClock.snapClearAndIdle()
            return@LaunchedEffect
        }

        when {
            openedVideoDetail -> {
                videoCardClock.beginOpening(
                    sourceRoute = openingSourceRoute,
                    snapshot = captureTransitionPerformanceSnapshot(),
                )
                // 先给详情 AVS 一个 frame 建立 shared 回灌。已建立时不能再启动
                // fallback Animatable，否则两条曲线在中段交接会让背景顿一下再追卡片。
                launchVideoCardDepthAnimation {
                    withFrameNanos { }
                    if (
                        videoCardClock.phase != VideoCardTransitionBackgroundPhase.OPENING ||
                        videoCardClock.hasActiveSharedMorphProgress()
                    ) {
                        return@launchVideoCardDepthAnimation
                    }
                    // 没有 shared 对端时才使用保底路径；参数仍与 shared bounds 完全同源。
                    videoCardClock.snapFallback(0f)
                    videoCardClock.animateFallbackTo(
                        target = 1f,
                        durationMillis = timelineSpec.durationMillis,
                        easing = timelineSpec.enterEasing,
                    )
                    if (videoCardClock.phase == VideoCardTransitionBackgroundPhase.OPENING) {
                        videoCardClock.markHeld()
                        videoCardClock.snapFallback(1f)
                    }
                }
            }

            returnedFromVideoDetail -> {
                if (
                    isRelatedVideoDetailReturn(
                        fromKey = previousTop as? BiliPaiNavKey.VideoDetail,
                        toKey = currentTop,
                    )
                ) {
                    onRelatedVideoDetailReturned()
                }
                if (videoCardClock.phase != VideoCardTransitionBackgroundPhase.RETURNING) {
                    if (
                        shouldSnapClearVideoCardDepthBlurOnQuickReturn(
                            isQuickReturnFromDetail = isQuickReturnFromDetailUpdated,
                            phase = videoCardClock.phase,
                        )
                    ) {
                        cancelVideoCardDepthAnimation()
                        videoCardClock.snapClearAndIdle()
                    } else {
                        videoCardClock.beginReturning(
                            sourceRoute = returningSourceRoute,
                            snapshot = videoCardClock.performanceSnapshot
                                ?: captureTransitionPerformanceSnapshot(),
                        )
                        launchVideoCardDepthAnimation {
                            withFrameNanos { }
                            if (
                                videoCardClock.phase != VideoCardTransitionBackgroundPhase.RETURNING ||
                                videoCardClock.hasActiveSharedMorphProgress()
                            ) {
                                return@launchVideoCardDepthAnimation
                            }
                            val startDepth = videoCardClock.depthProgress()
                            val fullDurationMs = resolveVideoCardTransitionReturnFullDurationMillis(
                                baseDurationMillis = timelineSpec.durationMillis,
                            )
                            val morphRemainingMs = resolveVideoCardSharedMorphRemainingDurationMs(
                                seekFraction = 0f,
                                fullDurationMs = fullDurationMs,
                            )
                            val clearDurationMs = resolveMorphAlignedFallbackDurationMs(
                                timelineDurationMs = morphRemainingMs,
                                startDepth = startDepth,
                                targetDepth = 0f,
                            )
                            videoCardClock.snapFallback(startDepth)
                            // 无 shared 对端时才使用 fallback；返回曲线固定 Linear。
                            videoCardClock.animateFallbackTo(
                                target = 0f,
                                durationMillis = clearDurationMs,
                                easing = timelineSpec.returnEasing,
                            )
                            val parentSourceRoute =
                                resolveCardMorphDestinationSourceRoute(currentTop)
                            if (isVideoCardReturnTargetRoute(parentSourceRoute)) {
                                videoCardClock.sourceRoute = parentSourceRoute
                                videoCardClock.snapFallback(1f)
                                videoCardClock.markHeld()
                            } else if (
                                videoCardClock.phase ==
                                VideoCardTransitionBackgroundPhase.RETURNING
                            ) {
                                videoCardClock.markIdle()
                            }
                        }
                    }
                }
            }

            !isCardMorphDestinationNavKey(currentTop) -> {
                launchVideoCardDepthAnimation {
                    val start = videoCardClock.depthProgress()
                    videoCardClock.snapFallback(start)
                    videoCardClock.animateFallbackTo(
                        target = 0f,
                        durationMillis = VIDEO_CARD_TRANSITION_BACKGROUND_CANCEL_DURATION_MS,
                        easing = FastOutLinearInEasing,
                    )
                    videoCardClock.markIdle()
                }
            }
        }
    }
    val popRouteTransition = remember(
        cardTransitionEnabled,
        reduceMotion,
        sourceMetadata,
        safeBackStack,
        activeMainHostRoute,
    ) {
        if (reduceMotion) {
            BiliPaiNavRouteTransition.REDUCED_MOTION_FADE
        } else {
            resolveBiliPaiNavDisplayPopRouteTransition(
                cardTransitionEnabled = cardTransitionEnabled,
                sourceMetadata = sourceMetadata,
                fromKey = safeBackStack.lastOrNull(),
                toKey = safeBackStack.getOrNull(safeBackStack.lastIndex - 1),
                activeMainHostRoute = activeMainHostRoute,
            )
        }
    }
    val autoPredictiveBackExitDirection = remember(popRouteTransition, sourceMetadata.cardSourceDirection) {
        resolveBiliPaiAutoPredictiveBackExitDirection(
            popRouteTransition = popRouteTransition,
            cardSourceDirection = sourceMetadata.cardSourceDirection,
        )
    }
    val predictiveBackExitDirection = remember(
        autoPredictiveBackExitDirection,
        predictiveBackExitDirectionOverride,
    ) {
        resolveBiliPaiPredictiveBackExitDirection(
            storageValue = predictiveBackExitDirectionOverride,
            autoDerived = autoPredictiveBackExitDirection,
        )
    }
    val predictiveBackHandler: BiliPaiPredictiveBackAnimationHandler = remember(
        popRouteTransition,
        predictiveBackEnabled,
        predictiveBackAnimationStyle,
        predictiveBackExitDirection,
    ) {
        resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = popRouteTransition,
            predictiveBackEnabled = predictiveBackEnabled,
            style = predictiveBackAnimationStyle,
            exitDirection = predictiveBackExitDirection,
        )
    }
    val currentBackKey = safeBackStack.lastOrNull()
    val targetBackKey = safeBackStack.getOrNull(safeBackStack.lastIndex - 1)
    val gestureReturningVideoCard = predictiveBackEnabled &&
        videoCardDepthEffectEnabled &&
        isVideoCardTransitionBackgroundGesturePhase(videoCardClock.phase) &&
        isCardMorphDestinationNavKey(currentBackKey) &&
        targetBackKey != null &&
        isVideoCardReturnTargetRoute(resolveCardMorphDestinationSourceRoute(currentBackKey))
    val predictiveBackGestureBlurEnabled = shouldApplyPredictiveBackGestureBlur(
        routeTransition = popRouteTransition,
        predictiveBackEnabled = predictiveBackEnabled,
        gestureReturningVideoCard = gestureReturningVideoCard,
        motionTier = transitionBackgroundMotionTier,
    )
    val predictiveBackBackgroundProgressProvider = remember(
        predictiveBackBackgroundProgress,
        predictiveBackGestureBlurEnabled,
        popRouteTransition,
    ) {
        {
            val liveBackProgress =
                (navigationEventState?.transitionState as? NavigationEventTransitionState.InProgress)
                    ?.latestEvent
                    ?.progress
            if (predictiveBackGestureBlurEnabled && liveBackProgress != null) {
                resolvePredictiveBackGestureBlurProgress(
                    backProgress = liveBackProgress,
                    routeTransition = popRouteTransition,
                )
            } else {
                predictiveBackBackgroundProgress.value
            }
        }
    }
    val performBack: (() -> Unit) -> Unit = { commitTransitionCallBack ->
        if (navigationBackJob?.isActive != true) {
            navigationBackJob = navigationScope.launch {
            val predictiveBlurAtCommit = predictiveBackBackgroundProgressProvider()
            val shouldFadePredictiveBlur = shouldApplyPredictiveBackGestureBlur(
                routeTransition = popRouteTransition,
                predictiveBackEnabled = predictiveBackEnabled,
                gestureReturningVideoCard = false,
                motionTier = transitionBackgroundMotionTier,
            ) && predictiveBlurAtCommit > 0f
            if (shouldFadePredictiveBlur) {
                launch {
                    predictiveBackBackgroundProgress.snapTo(predictiveBlurAtCommit)
                    predictiveBackBackgroundProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = resolvePredictiveBackCommitBlurDurationMs(
                                predictiveBlurAtCommit
                            ),
                            easing = FastOutLinearInEasing,
                        ),
                    )
                }
            }
            predictiveBackHandler.onBackPressed(
                transitionState = navigationEventState?.transitionState,
                currentPageKey = safeBackStack.lastOrNull(),
            )
            val isVideoCardActiveReturn = videoCardDepthEffectEnabled &&
                (
                    videoCardClock.phase == VideoCardTransitionBackgroundPhase.HELD ||
                        videoCardClock.phase == VideoCardTransitionBackgroundPhase.OPENING
                    ) &&
                isCardMorphDestinationNavKey(currentBackKey)
            if (isVideoCardActiveReturn) {
                cancelVideoCardDepthAnimation()
            }
            if (isVideoCardActiveReturn) {
                val morphSource = resolveCardMorphDestinationSourceRoute(currentBackKey)
                val quickReturnForDepthClear = onPrepareVideoCardSharedReturn()
                if (
                    shouldSnapClearVideoCardDepthBlurOnQuickReturn(
                        isQuickReturnFromDetail = quickReturnForDepthClear,
                        phase = videoCardClock.phase,
                    )
                ) {
                    videoCardClock.snapClearAndIdle()
                } else {
                    val gestureFractionAtCommit = videoCardClock.gestureBackProgress
                    val blurAtCommit = videoCardClock.depthProgress()
                    videoCardClock.beginReturning(morphSource)
                    val fullDurationMs = resolveVideoCardTransitionReturnFullDurationMillis(
                        baseDurationMillis = timelineSpec.durationMillis,
                    )
                    val morphRemainingMs = resolveVideoCardSharedMorphRemainingDurationMs(
                        seekFraction = gestureFractionAtCommit ?: 0f,
                        fullDurationMs = fullDurationMs,
                    )
                    val clearDurationMs = resolveMorphAlignedFallbackDurationMs(
                        timelineDurationMs = morphRemainingMs,
                        startDepth = blurAtCommit,
                        targetDepth = 0f,
                    )
                    launchVideoCardDepthAnimation {
                        videoCardClock.snapFallback(blurAtCommit)
                        videoCardClock.animateFallbackTo(
                            target = 0f,
                            durationMillis = clearDurationMs,
                            easing = timelineSpec.returnEasing,
                        )
                        val parentSourceRoute =
                            resolveCardMorphDestinationSourceRoute(targetBackKey)
                        if (isVideoCardReturnTargetRoute(parentSourceRoute)) {
                            videoCardClock.sourceRoute = parentSourceRoute
                            videoCardClock.snapFallback(1f)
                            videoCardClock.markHeld()
                        } else if (
                            videoCardClock.phase ==
                            VideoCardTransitionBackgroundPhase.RETURNING
                        ) {
                            videoCardClock.markIdle()
                        }
                    }
                }
            }
            videoCardReturnGestureInProgress = false
            videoCardClock.endGesture()
            commitTransitionCallBack()
            onBack()
            // 模糊清理、当前页退出和目标页稳定从这里同时开始；不串行等待模糊动画。
            delay(PREDICTIVE_BACK_EXIT_SETTLE_DURATION_MS.toLong())
            predictiveBackBackgroundProgress.snapTo(0f)
            }
        }
    }
    val latestProgrammaticBackAction = rememberUpdatedState<() -> Unit> {
        performBack { }
    }
    DisposableEffect(programmaticBackDispatcher) {
        val callback = {
            latestProgrammaticBackAction.value()
        }
        programmaticBackDispatcher.register(callback)
        onDispose {
            programmaticBackDispatcher.unregister(callback)
        }
    }
    val quickReturnFromDetailProvider = remember {
        { isQuickReturnFromDetailUpdated }
    }
    val preferWholeCardReturnProvider = rememberUpdatedState(preferWholeCardReturn)
    val scopedContent: @Composable (BiliPaiNavKey) -> Unit = remember(
        content,
        application,
        safeBackStack,
        videoCardClock,
        videoCardBackgroundProgressProvider,
        predictiveBackBackgroundProgressProvider,
        transitionBackgroundMotionTier,
        isLightBackground,
        quickReturnFromDetailProvider,
        preferWholeCardReturnProvider,
        morphProgressReporter,
    ) {
        { key ->
            val entryRoute = key.toLegacyRoute()
            Box(modifier = Modifier.fillMaxSize()) {
                ProvideAnimatedVisibilityScope(
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                ) {
                    CompositionLocalProvider(
                        LocalVideoCardSharedElementSourceRoute provides entryRoute,
                        LocalVideoCardTransitionClock provides videoCardClock,
                        LocalTransitionPerformanceSnapshot provides videoCardClock.performanceSnapshot,
                        LocalVideoCardMorphProgressReporter provides morphProgressReporter,
                        LocalVideoCardTransitionBackgroundState provides VideoCardTransitionBackgroundState(
                            progressProvider = videoCardBackgroundProgressProvider,
                            sourceRouteProvider = {
                                videoCardClock.sourceRoute
                            },
                            phaseProvider = {
                                videoCardClock.phase
                            },
                            isReturnGestureInProgressProvider = {
                                videoCardReturnGestureInProgress
                            },
                            isGestureRestoreInProgressProvider = {
                                videoCardClock.gestureRestoreInProgress
                            },
                            isQuickReturnFromDetailProvider = quickReturnFromDetailProvider,
                            preferWholeCardReturnProvider = {
                                preferWholeCardReturnProvider.value
                            },
                            motionTierProvider = {
                                transitionBackgroundMotionTier
                            },
                            isLightBackgroundProvider = {
                                isLightBackground
                            },
                        ),
                        LocalPredictiveBackBackgroundState provides PredictiveBackBackgroundState(
                            progressProvider = predictiveBackBackgroundProgressProvider,
                            targetKeyProvider = {
                                safeBackStack.getOrNull(safeBackStack.lastIndex - 1)
                            },
                            motionTierProvider = {
                                transitionBackgroundMotionTier
                            },
                            isLightBackgroundProvider = {
                                isLightBackground
                            },
                        ),
                    ) {
                        ProvideNavigation3ViewModelApplicationExtras(application) {
                            content(key)
                        }
                    }
                }
            }
        }
    }
    val entryProvider = remember(
        sourceMetadata,
        cardTransitionEnabled,
        reduceMotion,
        visibleBottomBarRoutes,
        activeMainHostRoute,
        scopedContent,
    ) {
        biliPaiNavEntryProvider(
            sourceMetadata = sourceMetadata,
            cardTransitionEnabled = cardTransitionEnabled,
            reduceMotion = reduceMotion,
            visibleBottomBarRoutes = visibleBottomBarRoutes,
            activeMainHostRoute = activeMainHostRoute,
            content = scopedContent
        )
    }
    val entries = rememberDecoratedNavEntries(
        backStack = safeBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            NavEntryDecorator(
                onPop = { key ->
                    predictiveBackHandler.onPagePop(
                        contentPageKey = key,
                        animationScope = navigationScope,
                    )
                }
            ) { entry ->
                with(predictiveBackHandler) {
                    Box(
                        modifier = Modifier.predictiveBackAnimationDecorator(
                            transitionState = navigationEventState?.transitionState,
                            contentPageKey = entry.contentKey,
                            currentPageKey = safeBackStack.lastOrNull(),
                        )
                    ) {
                        entry.Content()
                    }
                }
            }
        ),
        entryProvider = entryProvider
    )
    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = listOf(SinglePaneSceneStrategy()),
        sceneDecoratorStrategies = emptyList(),
        sharedTransitionScope = sharedTransitionScope,
        onBack = { performBack { } }
    )
    val scene = sceneState.currentScene
    val currentInfo = SceneInfo(scene)
    val previousSceneInfos = sceneState.previousScenes.map { SceneInfo(it) }
    navigationEventState = rememberNavigationEventState(
        currentInfo = currentInfo,
        backInfo = previousSceneInfos
    )
    val transitionState = navigationEventState.transitionState
    val inProgressState = transitionState as? NavigationEventTransitionState.InProgress
    val nativeVideoBackProgress = inProgressState?.latestEvent?.progress
    SideEffect {
        if (nativeVideoBackProgress != null) {
            onNativeVideoBackProgress(currentBackKey, targetBackKey, nativeVideoBackProgress)
        }
    }

    // 预测手势：最高优先级写入 clock，与 shared seek 同一 depth 读口。
    SideEffect {
        val gestureProgress = nativeVideoBackProgress
        val gestureActive = gestureReturningVideoCard && gestureProgress != null
        if (gestureActive && gestureProgress != null) {
            videoCardClock.beginGesture(gestureProgress)
        } else if (videoCardReturnGestureInProgress) {
            videoCardClock.endGesture()
        }
        videoCardReturnGestureInProgress = gestureActive
    }

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = scene.previousEntries.isNotEmpty(),
        // 关闭全局预测性返回时不向 NavDisplay 上报 InProgress，避免 seek 跟手预览；
        // 松手后仍走 performBack + 普通 popTransitionSpec。
        reportPredictiveProgress = predictiveBackEnabled,
        onBackCompleted = performBack,
        onBackCancelled = { commitTransition ->
            onNativeVideoBackCancelled(currentBackKey, targetBackKey)
            val cancelledVideoCardBlur = videoCardBackgroundProgressProvider()
            val cancelledPredictiveBlur = predictiveBackBackgroundProgressProvider()
            videoCardReturnGestureInProgress = false
            videoCardClock.endGesture()
            // 手势取消：depth 复原到满值，与详情回弹一致。
            if (isVideoCardTransitionBackgroundGesturePhase(videoCardClock.phase) &&
                cancelledVideoCardBlur < 1f
            ) {
                navigationScope.launch {
                    videoCardClock.beginGestureRestore()
                    try {
                        videoCardClock.snapFallback(cancelledVideoCardBlur)
                        videoCardClock.animateFallbackTo(
                            target = 1f,
                            durationMillis = VIDEO_CARD_TRANSITION_BACKGROUND_CANCEL_DURATION_MS,
                            easing = FastOutLinearInEasing,
                        )
                        videoCardClock.markHeld()
                    } finally {
                        videoCardClock.endGestureRestore()
                    }
                }
            }
            if (predictiveBackGestureBlurEnabled && cancelledPredictiveBlur > 0f) {
                navigationScope.launch {
                    predictiveBackBackgroundProgress.snapTo(cancelledPredictiveBlur)
                    predictiveBackBackgroundProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = PREDICTIVE_BACK_BACKGROUND_CANCEL_DURATION_MS,
                            easing = FastOutLinearInEasing,
                        ),
                    )
                }
            }
            commitTransition()
        },
    )

    val showVideoCardNavBackdrop = shouldShowVideoCardTransitionNavBackdrop(
        cardTransitionEnabled = videoCardDepthEffectEnabled,
        phase = videoCardClock.phase,
        isVideoDetailOnStack = isCardMorphDestinationNavKey(currentBackKey),
        isReturningToVideoDetail = isCardMorphDestinationNavKey(targetBackKey),
    )

    Box(modifier = modifier.fillMaxSize()) {
        val settingsSubtreeBackdrop =
            (currentBackKey != null && isSettingsSubtreeNavKey(currentBackKey)) ||
                (targetBackKey != null && isSettingsSubtreeNavKey(targetBackKey))
        if (settingsSubtreeBackdrop) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppSurfaceTokens.groupedListContainer()),
            )
        }
        VideoCardTransitionNavBackdrop(
            visible = showVideoCardNavBackdrop,
            progressProvider = videoCardBackgroundProgressProvider,
            phase = videoCardClock.phase,
            isLightBackground = isLightBackground,
        )
        NavDisplay(
            sceneState = sceneState,
            navigationEventState = navigationEventState,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
            sizeTransform = null,
            // 页面自身已经提供了方向、淡入和预测返回效果。关闭 Navigation3 默认的
            // 圆角裁剪与 dim，可避免转场期间额外的离屏层和整页重绘。
            transitionEffects = NavDisplayTransitionEffects(
                enableCornerClip = false,
                dimAmount = 0f,
                blockInputDuringTransition = false,
            ),
            transitionSpec = {
                with(predictiveBackHandler) {
                    onTransitionSpec()
                }
            },
            popTransitionSpec = {
                with(predictiveBackHandler) {
                    onPopTransitionSpec()
                }
            },
            predictivePopTransitionSpec = { swipeEdge ->
                with(predictiveBackHandler) {
                    onPredictivePopTransitionSpec(swipeEdge = swipeEdge)
                }
            },
        )
    }
}

@Composable
private fun ProvideNavigation3ViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }

    val patchedOwner = remember(navEntryOwner, application) {
        buildNavigation3ViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildNavigation3ViewModelStoreOwner(
    navEntryOwner: ViewModelStoreOwner,
    application: Application
): ViewModelStoreOwner {
    val defaultFactoryOwner = navEntryOwner as? HasDefaultViewModelProviderFactory
    val defaultCreationExtras = defaultFactoryOwner?.defaultViewModelCreationExtras
        ?: CreationExtras.Empty
    val patchedCreationExtras = MutableCreationExtras(defaultCreationExtras).apply {
        set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
    }

    return object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = navEntryOwner.viewModelStore
        override val defaultViewModelProviderFactory =
            defaultFactoryOwner?.defaultViewModelProviderFactory
                ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        override val defaultViewModelCreationExtras: CreationExtras = patchedCreationExtras
    }
}
