package com.android.purebilibili.navigation3

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.LocalMiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.MiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionExposure
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionClock
import com.android.purebilibili.core.ui.transition.LocalPredictiveBackBackgroundState
import com.android.purebilibili.core.ui.transition.PredictiveBackBackgroundState
import com.android.purebilibili.core.ui.transition.VideoCardTransitionClock
import com.android.purebilibili.core.ui.transition.VideoCardTransitionHostDepthLayer
import com.android.purebilibili.core.ui.transition.VideoCardTransitionNavBackdrop
import com.android.purebilibili.core.ui.transition.rememberVideoCardTransitionSnapshotHandle
import com.android.purebilibili.core.ui.transition.resolveVideoCardTransitionExposure
import com.android.purebilibili.core.ui.transition.resolveVideoCardTimelineSpec
import com.android.purebilibili.core.ui.transition.resolvePredictiveBackGestureBlurProgress
import com.android.purebilibili.core.ui.transition.shouldReleaseHostOwnedDepthLayer
import com.android.purebilibili.core.ui.transition.shouldShowVideoCardTransitionNavBackdrop
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackExitDirection
import com.android.purebilibili.navigation3.predictiveback.biliPaiMiuixNavTransition
import com.android.purebilibili.navigation3.predictiveback.miuixVideoCardNavTransition
import com.android.purebilibili.navigation3.predictiveback.MiuixVideoCardContentScale
import com.android.purebilibili.navigation3.predictiveback.MiuixVideoCardTransitionProgress
import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal class BiliPaiProgrammaticBackDispatcher {
    private var callback: (() -> Unit)? = null

    fun register(callback: () -> Unit) {
        this.callback = callback
    }

    fun unregister(callback: () -> Unit) {
        if (this.callback === callback) this.callback = null
    }

    fun dispatch(): Boolean {
        val action = callback ?: return false
        action()
        return true
    }
}

@Composable
internal fun BiliPaiNavDisplayHost(
    backStack: SnapshotStateList<BiliPaiNavKey>,
    cardTransitionEnabled: Boolean = true,
    videoTransitionRealtimeBlurEnabled: Boolean = false,
    isLightBackground: Boolean = false,
    reduceMotion: Boolean = false,
    videoSharedTransitionDurationMillis: Int,
    videoCardClock: VideoCardTransitionClock,
    predictiveBackAnimationStyle: BiliPaiPredictiveBackAnimationStyle =
        BiliPaiPredictiveBackAnimationStyle.MIUIX,
    predictiveBackExitDirection: BiliPaiPredictiveBackExitDirection =
        BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT,
    sourceMetadata: BiliPaiNavSourceMetadata,
    programmaticBackDispatcher: BiliPaiProgrammaticBackDispatcher,
    onBack: () -> Unit,
    onPrepareVideoCardSharedReturn: () -> Boolean = { false },
    onRelatedVideoDetailReturned: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (BiliPaiNavKey) -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val stackSnapshot = backStack.toList()
    val currentKey = stackSnapshot.lastOrNull()
    val latestOnBack by rememberUpdatedState(onBack)
    val latestPrepareReturn by rememberUpdatedState(onPrepareVideoCardSharedReturn)
    val latestRelatedReturn by rememberUpdatedState(onRelatedVideoDetailReturned)
    val performBack = remember(backStack) {
        {
            val leavingKey = backStack.lastOrNull()
            if (leavingKey is BiliPaiNavKey.VideoDetail) {
                latestPrepareReturn()
            }
            val returningFromRelated = (leavingKey as? BiliPaiNavKey.VideoDetail)
                ?.sourceRoute
                ?.substringBefore('?')
                ?.startsWith("video/") == true
            latestOnBack()
            if (returningFromRelated) latestRelatedReturn()
        }
    }

    DisposableEffect(programmaticBackDispatcher, performBack) {
        programmaticBackDispatcher.register(performBack)
        onDispose { programmaticBackDispatcher.unregister(performBack) }
    }

    val style = if (reduceMotion) {
        BiliPaiPredictiveBackAnimationStyle.NONE
    } else {
        predictiveBackAnimationStyle
    }
    val globalTransition = remember(style, predictiveBackExitDirection) {
        biliPaiMiuixNavTransition(
            animation = style,
            exitDirection = predictiveBackExitDirection,
        )
    }
    val videoCardTransitionProgress = remember { MiuixVideoCardTransitionProgress() }
    val videoCardTransition = remember(
        cardTransitionEnabled,
        reduceMotion,
        sourceMetadata.sourceBounds,
        sourceMetadata.sourceCornerDp,
        videoSharedTransitionDurationMillis,
        globalTransition,
    ) {
        if (cardTransitionEnabled && !reduceMotion) {
            miuixVideoCardNavTransition(
                sourceBounds = sourceMetadata.sourceBounds,
                sourceCornerDp = sourceMetadata.sourceCornerDp,
                durationMillis = videoSharedTransitionDurationMillis,
                fallback = globalTransition,
                progress = videoCardTransitionProgress,
                contentScale = MiuixVideoCardContentScale.FillWidthTop,
            )
        } else {
            globalTransition
        }
    }
    val fullscreenVideoCardTransition = remember(
        cardTransitionEnabled,
        reduceMotion,
        sourceMetadata.sourceBounds,
        sourceMetadata.sourceCornerDp,
        videoSharedTransitionDurationMillis,
        globalTransition,
    ) {
        if (cardTransitionEnabled && !reduceMotion) {
            miuixVideoCardNavTransition(
                sourceBounds = sourceMetadata.sourceBounds,
                sourceCornerDp = sourceMetadata.sourceCornerDp,
                durationMillis = videoSharedTransitionDurationMillis,
                fallback = globalTransition,
                progress = videoCardTransitionProgress,
                contentScale = MiuixVideoCardContentScale.CropCenter,
            )
        } else {
            globalTransition
        }
    }

    val timeline = remember(videoSharedTransitionDurationMillis) {
        resolveVideoCardTimelineSpec(videoSharedTransitionDurationMillis)
    }
    var previousStack by remember { mutableStateOf(stackSnapshot) }
    LaunchedEffect(stackSnapshot, cardTransitionEnabled, reduceMotion, timeline) {
        val previous = previousStack
        previousStack = stackSnapshot
        if (!cardTransitionEnabled || reduceMotion) {
            videoCardClock.snapClearAndIdle()
            return@LaunchedEffect
        }
        val previousTop = previous.lastOrNull()
        val openedCardDestination = isCardMorphDestinationNavKey(currentKey) &&
            stackSnapshot.size > previous.size
        val returnedFromCardDestination = isCardMorphDestinationNavKey(previousTop) &&
            stackSnapshot.size < previous.size
        when {
            openedCardDestination -> {
                videoCardClock.beginOpening(sourceMetadata.sourceRoute)
                videoCardClock.snapFallback(0f)
                videoCardClock.animateFallbackTo(
                    target = 1f,
                    durationMillis = timeline.durationMillis,
                    easing = timeline.enterEasing,
                )
                videoCardClock.markHeld()
            }
            returnedFromCardDestination -> {
                videoCardClock.beginReturning(sourceMetadata.sourceRoute, startDepth = 1f)
                videoCardClock.snapFallback(1f)
                videoCardClock.animateFallbackTo(
                    target = 0f,
                    durationMillis = timeline.durationMillis,
                    easing = timeline.returnEasing,
                )
                videoCardClock.markIdle()
            }
        }
    }

    val videoCardSnapshotHandle = rememberVideoCardTransitionSnapshotHandle()
    val transitionMotionTier = if (reduceMotion) MotionTier.Reduced else MotionTier.Normal
    val videoCardProgressProvider = remember(videoCardClock, videoCardTransitionProgress) {
        {
            videoCardTransitionProgress.depthOr(videoCardClock.depthProgress())
        }
    }
    val videoCardGestureProvider = remember(videoCardTransitionProgress) {
        { videoCardTransitionProgress.isGestureInProgress() }
    }
    val videoCardExposureProvider = remember(videoCardClock, videoCardGestureProvider) {
        {
            resolveVideoCardTransitionExposure(
                phase = videoCardClock.phase,
                predictiveBackInProgress = videoCardGestureProvider(),
                gestureRestoreInProgress = videoCardClock.gestureRestoreInProgress,
            )
        }
    }
    val effectiveVideoCardExposure = videoCardExposureProvider()
    LaunchedEffect(effectiveVideoCardExposure) {
        if (shouldReleaseHostOwnedDepthLayer(effectiveVideoCardExposure)) {
            videoCardSnapshotHandle.releaseSession()
        }
    }
    val currentBackTarget = stackSnapshot.getOrNull(stackSnapshot.lastIndex - 1)
    val showVideoCardNavBackdrop = shouldShowVideoCardTransitionNavBackdrop(
        cardTransitionEnabled = cardTransitionEnabled,
        exposure = effectiveVideoCardExposure,
        isVideoDetailOnStack = isCardMorphDestinationNavKey(currentKey),
        isReturningToVideoDetail = isCardMorphDestinationNavKey(currentBackTarget),
    )
    val cardMorphAvailable = cardTransitionEnabled &&
        !reduceMotion &&
        sourceMetadata.sourceBounds?.let { it.width > 1f && it.height > 1f } == true
    val transitionBackgroundState = remember(
        sourceMetadata.sourceRoute,
        sourceMetadata.sourceCornerDp,
        videoCardProgressProvider,
        videoCardExposureProvider,
        videoCardSnapshotHandle,
        transitionMotionTier,
        isLightBackground,
    ) {
        VideoCardTransitionBackgroundState(
            progressProvider = videoCardProgressProvider,
            sourceRouteProvider = { sourceMetadata.sourceRoute },
            phaseProvider = { videoCardClock.phase },
            exposureProvider = videoCardExposureProvider,
            sourceCornerDpProvider = { sourceMetadata.sourceCornerDp },
            snapshotHandle = videoCardSnapshotHandle,
            isReturnGestureInProgressProvider = videoCardGestureProvider,
            isGestureRestoreInProgressProvider = { videoCardClock.gestureRestoreInProgress },
            motionTierProvider = { transitionMotionTier },
            isLightBackgroundProvider = { isLightBackground },
        )
    }
    val miuixCardTransitionState = remember(
        cardMorphAvailable,
        videoCardProgressProvider,
        videoCardGestureProvider,
    ) {
        MiuixVideoCardTransitionState(
            enabled = cardMorphAvailable,
            progressProvider = videoCardProgressProvider,
            isGestureInProgressProvider = videoCardGestureProvider,
        )
    }
    // 恢复 0.2.2 的预测返回背景链路：目标返回页（栈前一 key）在预测返回手势中
    // 随手势进度模糊/消退，迁移到 Miuix 导航时该 provide 曾丢失。
    val predictiveBackBackgroundState = remember(
        videoCardTransitionProgress,
        currentBackTarget,
        transitionMotionTier,
        isLightBackground,
    ) {
        PredictiveBackBackgroundState(
            progressProvider = {
                videoCardTransitionProgress.gestureBackProgress()
                    ?.let { resolvePredictiveBackGestureBlurProgress(it) }
                    ?: 0f
            },
            targetKeyProvider = { currentBackTarget },
            motionTierProvider = { transitionMotionTier },
            isLightBackgroundProvider = { isLightBackground },
        )
    }

    val navCornerRadius = rememberDeviceCornerRadius(defaultRadius = 0.dp)
    val roundAllCorners = style == BiliPaiPredictiveBackAnimationStyle.AOSP ||
        style == BiliPaiPredictiveBackAnimationStyle.SCALE ||
        style == BiliPaiPredictiveBackAnimationStyle.CLASSIC
    // Video-card morph owns all four corners. Keeping NavDisplay's Leading clip enabled here
    // applies a second, device-radius clip only to the left edge and makes it visibly rounder
    // than the right edge during return.
    val videoCardMorphOwnsCorners = cardMorphAvailable && (
        isCardMorphDestinationNavKey(currentKey) ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.Opening ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.BackPreview ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.Returning ||
            effectiveVideoCardExposure == VideoCardTransitionExposure.Restoring
    )
    val enableHostCornerClip = !videoCardMorphOwnsCorners
    // The retained source page already owns blur/scrim through the video-card depth layer.
    // Miuix's generic covered-entry dim can be resolved from the lower VideoDetail transition
    // during nested related-video navigation, which darkens that page a second time.
    val hostDimAmount = if (videoCardMorphOwnsCorners) 0f else 0.5f
    val backdropColor = MiuixTheme.colorScheme.surface
    val effects = remember(
        navCornerRadius,
        roundAllCorners,
        enableHostCornerClip,
        hostDimAmount,
        backdropColor,
    ) {
        NavDisplayEffects(
            enableCornerClip = enableHostCornerClip,
            cornerClipRadius = if (roundAllCorners && navCornerRadius <= 0.dp) 32.dp else navCornerRadius,
            cornerClipMode = if (roundAllCorners) {
                NavCornerClipMode.All
            } else {
                NavCornerClipMode.Leading
            },
            dimAmount = hostDimAmount,
            backdropColor = backdropColor,
            blockInputDuringTransition = false,
        )
    }
    // 全屏滑动返回默认关闭（仅系统边缘预测返回），可在设置中开启。
    // 开启后仅对列表/设置等纵向页面生效，播放器、详情、WebView 等
    // 横滑冲突页面始终禁用（见 BiliPaiNavEntryProvider）。
    val fullScreenSwipeBackEnabled by
        com.android.purebilibili.core.store.SettingsManager
            .getFullScreenSwipeBackEnabled(LocalContext.current)
            .collectAsStateWithLifecycle(initialValue = false)
    val swipeBackDirection = if (fullScreenSwipeBackEnabled) {
        when (LocalLayoutDirection.current) {
            LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
            LayoutDirection.Ltr -> NavSwipeDirection.LeftToRight
        }
    } else {
        NavSwipeDirection.None
    }
    val interceptPredictiveBack =
        style == BiliPaiPredictiveBackAnimationStyle.NONE && backStack.size > 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppSurfaceTokens.groupedListContainer()),
    ) {
        VideoCardTransitionHostDepthLayer(
            enabled = cardMorphAvailable,
            snapshotHandle = videoCardSnapshotHandle,
            progressProvider = videoCardProgressProvider,
            phaseProvider = { videoCardClock.phase },
            exposureProvider = videoCardExposureProvider,
            isGestureRestoreInProgressProvider = { videoCardClock.gestureRestoreInProgress },
            motionTierProvider = { transitionMotionTier },
            isLightBackgroundProvider = { isLightBackground },
            realtimeBlurEnabledProvider = { videoTransitionRealtimeBlurEnabled },
        )
        VideoCardTransitionNavBackdrop(
            visible = showVideoCardNavBackdrop,
            progressProvider = videoCardProgressProvider,
            phase = videoCardClock.phase,
            isLightBackground = isLightBackground,
        )
        @Suppress("UNCHECKED_CAST")
        NavDisplay(
            backStack = backStack as NavBackStack,
            onBack = performBack,
            transition = globalTransition,
            effects = effects,
        ) {
            biliPaiNavEntries(
                swipeBackDirection = swipeBackDirection,
                videoCardTransition = videoCardTransition,
                fullscreenVideoCardTransition = fullscreenVideoCardTransition,
            ) { key ->
                BiliPaiMiuixNavEntry(
                    interceptPredictiveBack = interceptPredictiveBack,
                    onBack = performBack,
                ) {
                    CompositionLocalProvider(
                        LocalVideoCardSharedElementSourceRoute provides key.toLegacyRoute(),
                        LocalVideoCardTransitionClock provides videoCardClock,
                        LocalVideoCardTransitionBackgroundState provides transitionBackgroundState,
                        LocalMiuixVideoCardTransitionState provides miuixCardTransitionState,
                        LocalPredictiveBackBackgroundState provides predictiveBackBackgroundState,
                    ) {
                        ProvideMiuixNavViewModelApplicationExtras(application) {
                            content(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BiliPaiMiuixNavEntry(
    interceptPredictiveBack: Boolean,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = interceptPredictiveBack,
        onBackCompleted = onBack,
    )
    content()
}

@Composable
private fun ProvideMiuixNavViewModelApplicationExtras(
    application: Application,
    content: @Composable () -> Unit,
) {
    val navEntryOwner = LocalViewModelStoreOwner.current
    if (navEntryOwner == null) {
        content()
        return
    }
    val patchedOwner = remember(navEntryOwner, application) {
        buildMiuixNavViewModelStoreOwner(navEntryOwner, application)
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides patchedOwner) {
        content()
    }
}

private fun buildMiuixNavViewModelStoreOwner(
    navEntryOwner: ViewModelStoreOwner,
    application: Application,
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
