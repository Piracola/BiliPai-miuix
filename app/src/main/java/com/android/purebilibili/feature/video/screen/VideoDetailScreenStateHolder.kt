// 文件路径: feature/video/screen/VideoDetailScreen.kt
package com.android.purebilibili.feature.video.screen
import com.android.purebilibili.core.ui.resolveFilledButtonContainerColor
import com.android.purebilibili.core.ui.resolveFilledButtonContentColor
import com.android.purebilibili.core.refresh.HistoryRefreshSuppression
import com.android.purebilibili.core.ui.components.AppText

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.OrientationEventListener
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.activity.compose.BackHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.ui.layout.ContentScale
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.android.purebilibili.data.model.response.BgmInfo
import com.android.purebilibili.data.model.CommentFraudStatus
import com.android.purebilibili.data.repository.resolveCommentFraudLightMessage
import com.android.purebilibili.data.repository.shouldShowCommentFraudResultDialog
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import com.android.purebilibili.core.store.PortraitPlayerCollapseMode
import com.android.purebilibili.core.ui.rememberAppPlayerChromeProfile
import com.android.purebilibili.core.ui.AppSurfaceTokens
//  已改用 MaterialTheme.colorScheme.primary

import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.ReplyItem
import com.android.purebilibili.data.model.response.UgcSeason
import com.android.purebilibili.data.model.response.VideoTag
import com.android.purebilibili.data.model.response.ViewInfo
import com.android.purebilibili.data.model.response.ViewPoint
import com.android.purebilibili.feature.common.resolveIndexedVideoLazyKey
// Refactored UI components
import com.android.purebilibili.feature.video.ui.section.VideoTitleSection
import com.android.purebilibili.feature.video.ui.section.VideoTitleWithDesc
import com.android.purebilibili.feature.video.ui.section.UpInfoSection
import com.android.purebilibili.feature.video.ui.section.DescriptionSection
import com.android.purebilibili.feature.video.ui.section.ActionButtonsRow
import com.android.purebilibili.feature.video.ui.section.ActionButton
import com.android.purebilibili.feature.video.ui.components.RelatedVideosHeader
import com.android.purebilibili.feature.video.ui.components.RelatedVideoItem
import com.android.purebilibili.feature.video.ui.components.CoinDialog
import com.android.purebilibili.feature.video.ui.components.CollectionRow
import com.android.purebilibili.feature.video.ui.components.CollectionSheet
import com.android.purebilibili.feature.video.ui.components.PagesSelector
// Imports for moved classes
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackUiState
import com.android.purebilibili.feature.video.viewmodel.VideoComposerViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoEngagementViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoEngagementEvent
import com.android.purebilibili.feature.video.viewmodel.VideoSupplementViewModel
import com.android.purebilibili.feature.video.viewmodel.toEngagementSeed
import com.android.purebilibili.feature.video.viewmodel.toSupplementSeed
import com.android.purebilibili.feature.video.viewmodel.QualitySwitchFailureDialogState
import com.android.purebilibili.feature.video.viewmodel.CommentUiState
import com.android.purebilibili.feature.video.viewmodel.VideoCommentViewModel
import com.android.purebilibili.feature.video.danmaku.rememberDanmakuManager
import com.android.purebilibili.feature.video.state.VideoPlayerState
import com.android.purebilibili.feature.video.state.rememberVideoPlayerState
import com.android.purebilibili.feature.video.state.shouldReuseMiniPlayerAtEntry
import com.android.purebilibili.feature.video.ui.section.VideoPlayerSection
import com.android.purebilibili.feature.video.ui.section.resolveAllowLivePlayerSharedElementForMorph
import com.android.purebilibili.feature.video.ui.section.resolveNavigationLiveSurfaceTextureEnabled
import com.android.purebilibili.feature.video.ui.section.shouldKeepVideoPlaybackAwake
import com.android.purebilibili.feature.video.ui.components.ReplyHeader
import com.android.purebilibili.feature.video.ui.components.ReplyItemView
import com.android.purebilibili.feature.video.ui.components.CommentFraudResultDialog
import com.android.purebilibili.feature.video.ui.components.VideoCommentSheetHost
import com.android.purebilibili.feature.video.ui.components.VideoInlineSubReplyDetailContent

import com.android.purebilibili.feature.video.viewmodel.CommentSortMode  //  新增
import com.android.purebilibili.feature.video.ui.components.LikeBurstAnimation
import com.android.purebilibili.feature.video.ui.components.TripleSuccessAnimation
import com.android.purebilibili.feature.video.ui.components.VideoDetailSkeleton
import com.android.purebilibili.feature.video.ui.components.VideoActionFeedbackHost
import com.android.purebilibili.feature.video.subtitle.SubtitleAutoPreference
import com.android.purebilibili.feature.video.subtitle.SubtitleDisplayMode
import com.android.purebilibili.feature.video.subtitle.resolveSubtitleDisplayModePreference
import com.android.purebilibili.feature.video.progress.PbpProgressData
import com.android.purebilibili.feature.video.usecase.playPlayerFromUserAction
import com.android.purebilibili.feature.video.usecase.seekPlayerFromUserAction
import com.android.purebilibili.feature.video.policy.reduceVideoDetailPostScroll
import com.android.purebilibili.feature.video.policy.reduceVideoDetailPreScroll
import com.android.purebilibili.feature.video.policy.resolveVideoDetailCollapseProgress
import com.android.purebilibili.feature.video.policy.shouldSkipGesturePlayerCollapseForLayout
import com.android.purebilibili.feature.video.policy.shouldTrackVideoDetailCollapseMotion
import com.android.purebilibili.feature.video.subtitle.resolveSubtitlePreferenceSession
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import com.android.purebilibili.core.ui.components.AppButton
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
//  共享元素过渡
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.purebilibili.core.ui.LocalPredictiveBackGestureEnabled
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.resolveVideoDetailShellOverlayCornerDp
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionEnterEasing
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionReturnEasing
import com.android.purebilibili.core.ui.transition.resolveVideoCardDetailChromeAlpha
import com.android.purebilibili.core.ui.transition.resolveVideoCardSecondaryContentVisualFrame
import com.android.purebilibili.core.ui.transition.resolveVideoSharedCoverCacheKey
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionSourceCornerDp
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionVisualSpec
import com.android.purebilibili.core.ui.transition.shouldEnableVideoCoverSharedTransition
import com.android.purebilibili.core.ui.transition.shouldForceCoverOnlyForReturnOwnership
import com.android.purebilibili.core.ui.transition.shouldTreatLiveSurfaceRenderableForReturnMorph
import com.android.purebilibili.core.ui.transition.shouldUseVideoCardShellContainerTransform
import com.android.purebilibili.core.ui.transition.VideoCardShellSharedBoundsRole
import com.android.purebilibili.core.ui.transition.videoCardShellSharedBoundsOrEmpty
import com.android.purebilibili.core.ui.transition.videoSharedElementBoundsTransformSpec
import com.android.purebilibili.core.ui.rememberAppCollectionIcon
import com.android.purebilibili.core.ui.rememberAppDownloadIcon
import com.android.purebilibili.core.ui.rememberAppMusicIcon
import com.android.purebilibili.core.ui.rememberAppPhotoIcon
import com.android.purebilibili.core.ui.rememberAppPlayIcon
import com.android.purebilibili.feature.video.player.MiniPlayerManager
import com.android.purebilibili.feature.video.player.PlaybackService
import com.android.purebilibili.feature.video.player.PlaylistItem
import com.android.purebilibili.feature.video.player.PlaylistManager
import com.android.purebilibili.feature.video.player.PlaylistUiState
import com.android.purebilibili.feature.video.player.ExternalPlaylistSource
import com.android.purebilibili.core.ui.performance.TrackJankStateFlag
// 📱 [新增] 竖屏全屏
import com.android.purebilibili.feature.video.ui.overlay.PortraitFullscreenOverlay
import com.android.purebilibili.feature.video.ui.overlay.PlayerProgress
import com.android.purebilibili.feature.video.ui.components.VideoAspectRatio
import com.android.purebilibili.core.ui.blur.shouldAllowRuntimeShaderBackedHazeEffect
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.FormatUtils
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import com.android.purebilibili.feature.video.ui.components.DanmakuContextMenu
import com.android.purebilibili.feature.video.ui.components.DanmakuBlockActionTarget
import com.android.purebilibili.feature.video.ui.components.resolveDanmakuBlockActionFeedbackMessage
import com.android.purebilibili.feature.video.danmaku.appendDanmakuKeywordBlockRule
import com.android.purebilibili.feature.video.danmaku.appendDanmakuUserHashBlockRule
import com.android.purebilibili.feature.video.ui.components.InteractiveChoiceOverlay
import com.android.purebilibili.feature.video.ui.feedback.VideoFeedbackAnchor
import com.android.purebilibili.feature.video.ui.feedback.TripleCelebrationPlacement
import com.android.purebilibili.feature.video.ui.feedback.resolveQualityReminderPlacement
import com.android.purebilibili.feature.video.ui.feedback.resolveTripleCelebrationPlacement
import com.android.purebilibili.feature.video.ui.feedback.resolveVideoFeedbackPlacement
import com.android.purebilibili.feature.video.ui.section.resolveForcedReturnCoverSharedElementSourceRoute
import com.android.purebilibili.feature.video.share.VideoSharePayload
import com.android.purebilibili.feature.video.share.VideoShareSheet
import com.android.purebilibili.feature.video.viewmodel.PlayerToastPresentation
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 评论区「一键回顶」恢复播放器事件:评论区 host 与竖屏播放器处于不同
 * 代码块/作用域,通过共享事件桥接,播放器侧观察到后恢复全尺寸。
 */
private val commentBackToTopRestoreFlow =
    kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)

private const val CONTINUOUS_PLAYER_MORPH_DURATION_MILLIS = 280

private const val VIDEO_DETAIL_COLLAPSE_SIGNAL_IDLE_TIMEOUT_MS = 120L

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun VideoDetailScreenStateHolder(
    bvid: String,
    cid: Long = 0L,
    coverUrl: String = "",
    startInFullscreen: Boolean = false,
    startAudioFromRoute: Boolean = false,
    autoEnterPortraitFromRoute: Boolean = false,
    initialVerticalFromRoute: Boolean = false,
    directPortraitEntryFromRoute: Boolean = false,
    resumePositionMsFromRoute: Long = 0L,
    openCommentRootRpidFromRoute: Long = 0L,
    openCommentTargetRpidFromRoute: Long = 0L,
    sourceRouteForSharedElement: String? = null,
    keepLoadedContentForBackPreview: Boolean = false,
    bindLivePlayerForBackPreview: Boolean = keepLoadedContentForBackPreview,
    predictiveBackCancelRecoveryGeneration: Int = 0,
    isReturningFromDetail: Boolean = false,
    isQuickReturningFromDetail: Boolean = false,
    onMarkReturningFromDetail: () -> Unit = {},
    onClearReturningFromDetail: () -> Unit = {},
    transitionEnabled: Boolean = false,
    transitionEnterDurationMillis: Int = 320,
    onBack: () -> Unit,
    onHomeClick: () -> Unit = onBack,
    onNavigateToAudioMode: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onSearchKeywordClick: (String) -> Unit = {},
    onOpenBilibiliLink: ((String) -> Unit)? = null,
    onVideoClick: (String, android.os.Bundle?) -> Unit,
    onUpClick: (Long) -> Unit = {},
    onUpClickWithVideo: ((Long, String) -> Unit)? = null,
    miniPlayerManager: MiniPlayerManager? = null,
    isInPipMode: Boolean = false,
    isVisible: Boolean = true,
    viewModel: VideoPlaybackViewModel = viewModel(),
    engagementViewModel: VideoEngagementViewModel = viewModel(),
    composerViewModel: VideoComposerViewModel = viewModel(),
    supplementViewModel: VideoSupplementViewModel = viewModel(),
    commentViewModel: VideoCommentViewModel = viewModel(),
    onBgmClick: (BgmInfo) -> Unit = {}
) {
    // 详情页打开期间抑制历史/收藏列表刷新:播放心跳会持续触发
    // HistoryRefreshBus,若父级列表在预测性返回手势动画中重新加载,
    // 元素位置中途平移会导致返回表现异常。退出详情页(含返回完成)
    // 后恢复,抑制期间遗漏的刷新在恢复时补发一次。
    DisposableEffect(Unit) {
        HistoryRefreshSuppression.suppress()
        onDispose {
            HistoryRefreshSuppression.resume()
        }
    }

    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val homeUpBadgesVisible by com.android.purebilibili.core.store.SettingsManager
        .getHomeUpBadgesVisible(context)
        .collectAsStateWithLifecycle(initialValue = true
        )
    val liveSurfaceCardTransitionEnabled by com.android.purebilibili.core.store.SettingsManager
        .getLiveSurfaceCardTransitionEnabled(context)
        .collectAsStateWithLifecycle(initialValue = false)
    // SDR live morph TextureView only when both master transition + live-surface switch are on.
    // HDR still forces SurfaceView inside shouldUseTextureSurfaceForFlip (no quality sacrifice).
    val useTextureSurfaceForNavigation = remember(
        transitionEnabled,
        liveSurfaceCardTransitionEnabled,
    ) {
        resolveNavigationLiveSurfaceTextureEnabled(
            cardTransitionEnabled = transitionEnabled,
            liveSurfaceCardTransitionEnabled = liveSurfaceCardTransitionEnabled,
        )
    }
    val allowLivePlayerSharedElement = remember(
        transitionEnabled,
        liveSurfaceCardTransitionEnabled,
    ) {
        resolveAllowLivePlayerSharedElementForMorph(
            cardTransitionEnabled = transitionEnabled,
            liveSurfaceCardTransitionEnabled = liveSurfaceCardTransitionEnabled,
        )
    }
    val motionSpec = remember(transitionEnterDurationMillis) {
        resolveVideoDetailMotionSpec(transitionEnterDurationMillis)
    }
    val sharedTransitionSpeedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val homeSharedTransitionMotionSpec = remember(
        sourceRouteForSharedElement,
        transitionEnabled,
        sharedTransitionSpeedSettings,
        isQuickReturningFromDetail,
    ) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRouteForSharedElement,
            transitionEnabled = transitionEnabled,
            speedSettings = sharedTransitionSpeedSettings,
            isQuickReturn = isQuickReturningFromDetail,
        )
    }
    val frozenTransitionSourceCornerDp =
        LocalVideoCardTransitionBackgroundState.current.sourceCornerDpProvider()
    val sharedTransitionSourceCornerDp = remember(
        sourceRouteForSharedElement,
        frozenTransitionSourceCornerDp,
    ) {
        frozenTransitionSourceCornerDp
            ?: resolveVideoSharedTransitionSourceCornerDp(sourceRouteForSharedElement)
    }
    val videoSharedPlaybackIntent = remember(context, startAudioFromRoute) {
        resolveVideoSharedTransitionPlaybackIntent(
            clickToPlayEnabled = com.android.purebilibili.core.store.SettingsManager.getClickToPlaySync(context),
            forceImmediatePlayback = startAudioFromRoute
        )
    }
    val routeSheetMotion = remember(sourceRouteForSharedElement, transitionEnabled) {
        resolveVideoDetailRouteSheetMotion(
            sourceRoute = sourceRouteForSharedElement,
            transitionEnabled = transitionEnabled
        )
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val subjectSnapshot by viewModel.subjectSnapshot.collectAsStateWithLifecycle()
    val engagementState by engagementViewModel.uiState.collectAsStateWithLifecycle()
    val favoriteFolderSaveEvent by viewModel.favoriteFolderSaveEvent.collectAsStateWithLifecycle()
    val playbackActions = remember(viewModel, context) {
        VideoDetailPlaybackActions(
            changeQuality = viewModel::changeQuality,
            reloadVideo = viewModel::reloadVideo,
            switchCdn = viewModel::switchCdn,
            switchCdnTo = viewModel::switchCdnTo,
            probeCdnCandidates = viewModel::probeCurrentCdnCandidates,
            setAudioMode = viewModel::setAudioMode,
            setSleepTimer = viewModel::setSleepTimer,
            switchPage = viewModel::switchPage,
            openDownloadDialog = viewModel::openDownloadDialog,
            showDanmakuSendDialog = viewModel::showDanmakuSendDialog,
            skipSponsorSegment = viewModel::skipCurrentSponsorSegment,
            dismissSponsorSkipButton = viewModel::dismissSponsorSkipButton,
            voteSponsorSegment = viewModel::voteCurrentSponsorSegment,
            markSponsorContributionBoundary = viewModel::markSponsorContributionBoundary,
            setSponsorContributionCategory = viewModel::setSponsorContributionCategory,
            setSponsorContributionActionType = viewModel::setSponsorContributionActionType,
            submitSponsorContribution = viewModel::submitSponsorContribution,
            cancelSponsorContribution = viewModel::cancelSponsorContribution,
            notifyExplicitSeek = viewModel::notifyPluginsOfExplicitSeek,
            setVideoCodec = viewModel::setVideoCodec,
            setVideoSecondCodec = viewModel::setVideoSecondCodec,
            setAudioQuality = viewModel::setAudioQuality,
            applyPlaybackSpeed = viewModel::applyPlaybackSpeedFromUi,
            changeAudioLanguage = viewModel::changeAudioLanguage,
            saveCover = { viewModel.saveCover(context) },
            downloadAudio = { viewModel.downloadAudio(context) },
            selectSubtitleTrack = viewModel::selectSubtitleTrack,
            showFavoriteFolderDialog = viewModel::showFavoriteFolderDialog,
            toggleFavoriteFolderSelection = viewModel::toggleFavoriteFolderSelection,
            saveFavoriteFolderSelection = viewModel::saveFavoriteFolderSelection,
            dismissFavoriteFolderDialog = viewModel::dismissFavoriteFolderDialog,
            createFavoriteFolder = viewModel::createFavoriteFolder,
            retryAiSummary = viewModel::retryAiSummary,
            createVideoNoteDraftFromAiSummary = viewModel::createVideoNoteDraftFromAiSummary,
            openVideoNoteEditor = viewModel::openVideoNoteEditor,
            closeVideoNoteEditor = viewModel::closeVideoNoteEditor,
            updateVideoNoteEditorDocument = viewModel::updateVideoNoteEditorDocument,
            insertCurrentPlaybackTimestampIntoNote = viewModel::insertCurrentPlaybackTimestampIntoNote,
            seekTo = viewModel::seekTo,
            saveVideoNote = viewModel::saveVideoNote,
            deleteVideoNote = viewModel::deleteVideoNote,
            retryVideoNote = viewModel::retryVideoNote,
            openRootCommentComposer = viewModel::openRootCommentComposer,
            replyTo = {
                viewModel.setReplyingTo(it)
                viewModel.showCommentInputDialog()
            },
            markVideoNotInterested = viewModel::markVideoNotInterested
        )
    }
    val engagementActions = remember(engagementViewModel) {
        VideoDetailEngagementActions(
            toggleFollow = engagementViewModel::toggleFollow,
            toggleFavorite = engagementViewModel::toggleFavorite,
            toggleLike = engagementViewModel::toggleLike,
            openCoinDialog = engagementViewModel::openCoinDialog,
            doTripleAction = engagementViewModel::doTripleAction,
            toggleWatchLater = engagementViewModel::toggleWatchLater
        )
    }
    val commentActions = remember(commentViewModel) {
        VideoDetailCommentActions(
            loadComments = commentViewModel::loadComments,
            setSortMode = commentViewModel::setSortMode,
            deleteComment = commentViewModel::deleteComment,
            startDissolve = commentViewModel::startDissolve,
            loadMoreSubReplies = commentViewModel::loadMoreSubReplies,
            openSubReply = commentViewModel::openSubReply,
            openSubReplyConversation = commentViewModel::openSubReplyConversation,
            closeSubReplyConversation = commentViewModel::closeSubReplyConversation,
            closeSubReply = commentViewModel::closeSubReply,
            startSubDissolve = commentViewModel::startSubDissolve,
            deleteSubComment = commentViewModel::deleteSubComment,
            likeComment = commentViewModel::likeComment,
            reportComment = { rpid, reason -> commentViewModel.reportComment(rpid, reason) },
            toggleTopComment = commentViewModel::toggleTopComment
        )
    }
    VideoDetailDomainEffects(
        context = context,
        isVisible = isVisible,
        uiState = uiState,
        subjectSnapshot = subjectSnapshot,
        favoriteFolderSaveEvent = favoriteFolderSaveEvent,
        playbackViewModel = viewModel,
        engagementViewModel = engagementViewModel,
        composerViewModel = composerViewModel,
        supplementViewModel = supplementViewModel,
    )
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val presentationState = rememberVideoDetailPresentationState(
        routeBvid = bvid,
        initialCid = 0L,
        initialPortraitFullscreen = shouldStartInPortraitFullscreenFromRouteHint(
            autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
            startAudioFromRoute = startAudioFromRoute,
            initialVerticalFromRoute = initialVerticalFromRoute,
            directPortraitEntryFromRoute = directPortraitEntryFromRoute,
        ),
        initialPipMode = isInPipMode,
    )
    var isNavigatingToVideo by presentationState.navigatingToVideoState
    // `isNavigatingToVideo` 仅覆盖共享元素动画，动画结束会提前复位；NavHost 旧 entry
    // 仍可能再存活几帧。弹幕主机离开态必须保持到旧 entry 真正销毁。
    var hasCommittedRelatedVideoNavigation by remember(bvid) { mutableStateOf(false) }
    var isNavigatingToAudioMode by presentationState.navigatingToAudioModeState
    var isNavigatingToMiniMode by presentationState.navigatingToMiniModeState
    var hasAutoEnteredAudioMode by rememberSaveable { mutableStateOf(false) }
    var hasAutoEnteredPortraitFromRoute by rememberSaveable(bvid) { mutableStateOf(false) }
    // 路由要求直达竖屏全屏时，立刻盖过可能被 saveable 复写的详情态。
    LaunchedEffect(
        bvid,
        autoEnterPortraitFromRoute,
        initialVerticalFromRoute,
        directPortraitEntryFromRoute,
        startAudioFromRoute,
    ) {
        if (
            shouldStartInPortraitFullscreenFromRouteHint(
                autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
                startAudioFromRoute = startAudioFromRoute,
                initialVerticalFromRoute = initialVerticalFromRoute,
                directPortraitEntryFromRoute = directPortraitEntryFromRoute,
            )
        ) {
            presentationState.setPortraitFullscreen(true)
            hasAutoEnteredPortraitFromRoute = true
        }
    }
    var hasHandledCommentRootFromRoute by rememberSaveable(
        bvid,
        openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute
    ) { mutableStateOf(false) }
    // 🔄 [Seamless Playback] Internal BVID state to support seamless switching in portrait mode
    var currentBvid by presentationState.currentBvidState
    var currentBvidCid by presentationState.currentCidState
    var landscapeCommentPanelVisible by rememberSaveable(currentBvid) { mutableStateOf(false) }
    var landscapeCommentPanelOnLeft by rememberSaveable(currentBvid) { mutableStateOf(false) }
    // Episode cover captured at collection/playlist in-page switch (survives Loading.Initial).
    var pendingInPageSwitchCoverUrl by rememberSaveable { mutableStateOf("") }
    var isPipMode by presentationState.pipModeState
    var isPortraitFullscreen by presentationState.portraitFullscreenState
    var selectedVideoContentTabIndex by presentationState.selectedTabIndexState
    val playbackTargetCid = resolveVideoDetailPlaybackTargetCid(
        routeBvid = bvid,
        routeCid = cid,
        currentBvid = currentBvid,
        currentBvidCid = currentBvidCid
    )
    val introListState = rememberSaveable(currentBvid, saver = LazyListState.Saver) {
        LazyListState()
    }
    val commentListState = rememberSaveable(currentBvid, saver = LazyListState.Saver) {
        LazyListState()
    }
    val videoContentPagerState: PagerState = key(currentBvid) {
        rememberPagerState(pageCount = { 2 })
    }

    val entryRootAnimatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val entryRootSharedTransitionScope = LocalSharedTransitionScope.current
    val detailShellSharedBoundsEnabledForEntry = shouldUseVideoCardShellContainerTransform(
        sourceRoute = sourceRouteForSharedElement,
        transitionEnabled = transitionEnabled,
        hasSharedTransitionScope = entryRootSharedTransitionScope != null,
        hasAnimatedVisibilityScope = entryRootAnimatedVisibilityScope != null
    )
    // 有列表来源时默认按 shell morph 进场：即使首帧 AVS 尚未挂上，也先 defer 重内容。
    val shellSharedBoundsLikely =
        transitionEnabled && !sourceRouteForSharedElement.isNullOrBlank()
    val reuseFromMiniPlayerAtEntry = remember(currentBvid, playbackTargetCid, miniPlayerManager) {
        val manager = miniPlayerManager
        if (manager == null) {
            false
        } else {
            shouldReuseMiniPlayerAtEntry(
                isMiniPlayerActive = manager.isActive,
                miniPlayerBvid = manager.currentBvid,
                miniPlayerCid = manager.currentCid,
                hasMiniPlayerInstance = manager.player != null,
                requestBvid = currentBvid,
                requestCid = playbackTargetCid
            )
        }
    }
    val deferVideoDetailEntryLoad = shouldDeferVideoDetailLoadUntilEntryTransitionFinished(
        transitionEnabled = transitionEnabled,
        detailShellSharedBoundsEnabled = detailShellSharedBoundsEnabledForEntry ||
            shellSharedBoundsLikely,
        reuseFromMiniPlayerAtEntry = reuseFromMiniPlayerAtEntry,
        isReturningFromDetail = isReturningFromDetail,
    )
    val entryTransitionFinished = rememberVideoDetailEntryTransitionFinished(
        deferLoad = deferVideoDetailEntryLoad,
        sharedTransitionScope = entryRootSharedTransitionScope,
        animatedVisibilityScope = entryRootAnimatedVisibilityScope,
        fallbackDurationMillis = homeSharedTransitionMotionSpec.durationMillis,
    )
    val entryPlaybackReady = rememberVideoDetailEntryPlaybackReady(
        deferLoad = deferVideoDetailEntryLoad,
        morphDurationMillis = homeSharedTransitionMotionSpec.durationMillis,
    )

    fun markSecondaryNavigationLeave(expectedBvid: String = currentBvid) {
        miniPlayerManager?.markLeavingByNavigation(expectedBvid = expectedBvid)
    }

    var hasStartedCurrentVideoPlayback by rememberSaveable(currentBvid) {
        mutableStateOf(false)
    }
    val navigateToUserSpaceFromVideo: (Long) -> Unit = { mid ->
        val locateBvid = currentBvid.takeIf { hasStartedCurrentVideoPlayback }.orEmpty()
        onUpClickWithVideo?.invoke(mid, locateBvid) ?: onUpClick(mid)
    }

    val navigateToSearchFromVideo: () -> Unit = {
        onNavigateToSearch()
    }

    val navigateToSearchKeywordFromVideo: (String) -> Unit = { keyword ->
        onSearchKeywordClick(keyword)
    }

    // 与 VideoPlayerSection 共用单例；同页切集/相关推荐 push 前清掉旧弹幕会话，
    // 避免新页 DanmakuView 绑定时把旧片缓存闪上去或卡在未重放状态。
    val sharedDanmakuManager = rememberDanmakuManager()

    fun switchVideoInCurrentDetailPage(
        targetBvid: String,
        targetCid: Long,
        autoPlay: Boolean = true
    ) {
        val normalizedBvid = targetBvid.trim()
        if (normalizedBvid.isBlank()) return
        val safeCid = targetCid.coerceAtLeast(0L)
        val success = uiState as? VideoPlaybackUiState.Success
        if (success?.info?.bvid == normalizedBvid && (safeCid <= 0L || success.info.cid == safeCid)) {
            return
        }
        // Capture episode cover before Success is replaced by Loading.
        val switchedCover = resolveUgcSeasonEpisodeCoverUrl(
            ugcSeason = success?.info?.ugc_season,
            targetBvid = normalizedBvid,
            targetCid = safeCid
        )
        if (switchedCover.isNotBlank()) {
            pendingInPageSwitchCoverUrl = switchedCover
        }
        sharedDanmakuManager.clearForVideoChange()
        presentationState.switchVideo(normalizedBvid, safeCid)
        viewModel.loadVideo(
            bvid = normalizedBvid,
            cid = safeCid,
            autoPlay = autoPlay
        )
    }

    val relatedNavigationScope = rememberCoroutineScope()
    val navigateToRelatedVideo = remember(
        onVideoClick,
        miniPlayerManager,
        uiState,
        currentBvid,
        relatedNavigationScope,
        sharedDanmakuManager,
    ) {
        { targetBvid: String, options: android.os.Bundle? ->
            val success = uiState as? VideoPlaybackUiState.Success
            val explicitCid = options?.getLong(VIDEO_NAV_TARGET_CID_KEY) ?: 0L
            val resolvedCid = resolveNavigationTargetCid(
                targetBvid = targetBvid,
                explicitCid = explicitCid,
                relatedVideos = success?.related.orEmpty(),
                ugcSeason = success?.info?.ugc_season
            )
            com.android.purebilibili.core.util.Logger.d(
                "VideoDetailScreen",
                "navigateToRelatedVideo: current=${success?.info?.bvid ?: "unknown"} target=$targetBvid explicitCid=$explicitCid resolvedCid=$resolvedCid"
            )
            if (
                shouldSwitchCollectionVideoInsideCurrentDetailPage(
                    targetBvid = targetBvid,
                    currentBvid = success?.info?.bvid ?: currentBvid,
                    ugcSeason = success?.info?.ugc_season
                )
            ) {
                miniPlayerManager?.isNavigatingToVideo = false
                switchVideoInCurrentDetailPage(
                    targetBvid = targetBvid,
                    targetCid = resolvedCid,
                    autoPlay = true
                )
            } else {
                // 先摘掉父详情壳 sharedBounds，再 push，避免相关卡嵌套在父壳内吃不到 morph。
                // 同时清掉单例弹幕缓存，防止新页 attach 时重放旧片或 load 结果落到旧 controller。
                sharedDanmakuManager.clearForVideoChange()
                hasCommittedRelatedVideoNavigation = true
                presentationState.markNavigatingToVideo()
                miniPlayerManager?.isNavigatingToVideo = true
                markSecondaryNavigationLeave(expectedBvid = success?.info?.bvid ?: currentBvid)
                val navOptions = android.os.Bundle(options ?: android.os.Bundle.EMPTY)
                if (resolvedCid > 0L) {
                    navOptions.putLong(VIDEO_NAV_TARGET_CID_KEY, resolvedCid)
                }
                relatedNavigationScope.launch {
                    androidx.compose.runtime.withFrameNanos { }
                    onVideoClick(targetBvid, navOptions)
                }
                Unit
            }
        }
    }

    LaunchedEffect(bvid, cid) {
        com.android.purebilibili.core.util.Logger.d(
            "VideoDetailScreen",
            "SUB_DBG screen entry args: bvid=$bvid, cid=$cid"
        )
    }

    val openCommentUrl: (String) -> Unit = openCommentUrl@{ rawUrl ->
        val url = rawUrl.trim()
        if (url.isEmpty()) return@openCommentUrl
        if (onOpenBilibiliLink != null) {
            onOpenBilibiliLink(url)
            return@openCommentUrl
        }

        when (val target = resolveCommentUrlNavigationTarget(url)) {
            is CommentUrlNavigationTarget.Video -> {
                navigateToRelatedVideo(target.videoId, null)
                return@openCommentUrl
            }

            is CommentUrlNavigationTarget.Search -> {
                navigateToSearchKeywordFromVideo(target.keyword)
                return@openCommentUrl
            }

            is CommentUrlNavigationTarget.Space -> {
                navigateToUserSpaceFromVideo(target.mid)
                return@openCommentUrl
            }

            null -> Unit
        }

        runCatching { uriHandler.openUri(url) }
    }

    // 🎭 [性能优化] 进场以动画为主：shell morph 期间不挂载简介/相关/评论等重型内容，
    // loadVideo 也等 entryTransitionFinished。不再叠加二级 fadeIn/slide，避免与 sharedBounds 冲突。
    val entryVisualEnabled = transitionEnabled && !deferVideoDetailEntryLoad
    // 注意：不要把 entryTransitionFinished 放进 remember key，否则返回时 finished 回抖会重建状态并隐藏内容。
    var isTransitionFinished by remember(deferVideoDetailEntryLoad, entryVisualEnabled) {
        mutableStateOf(
            when {
                deferVideoDetailEntryLoad -> false
                !transitionEnabled -> true
                else -> false
            }
        )
    }
    val entryVisualProgress = remember(entryVisualEnabled) {
        Animatable(if (entryVisualEnabled) 0f else 1f)
    }
    val detailInfoRevealProgress = remember(transitionEnabled) {
        Animatable(if (transitionEnabled) 0f else 1f)
    }

    LaunchedEffect(transitionEnabled, motionSpec.entryPhaseDurationMillis) {
        if (!transitionEnabled) {
            detailInfoRevealProgress.snapTo(1f)
        } else {
            detailInfoRevealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = motionSpec.entryPhaseDurationMillis.coerceAtLeast(1),
                    delayMillis = 48,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    LaunchedEffect(
        deferVideoDetailEntryLoad,
        entryTransitionFinished,
        entryVisualEnabled,
        motionSpec.entryPhaseDurationMillis,
        transitionEnabled
    ) {
        when {
            deferVideoDetailEntryLoad -> {
                // 只允许 true 锁存：相关推荐返回的二次 morph 不得把内容区重新藏起来。
                if (entryTransitionFinished) {
                    entryVisualProgress.snapTo(1f)
                    isTransitionFinished = true
                } else if (!isTransitionFinished) {
                    entryVisualProgress.snapTo(0f)
                }
            }

            !transitionEnabled -> {
                entryVisualProgress.snapTo(1f)
                isTransitionFinished = true
            }

            else -> {
                if (isTransitionFinished) {
                    entryVisualProgress.snapTo(1f)
                    return@LaunchedEffect
                }
                entryVisualProgress.snapTo(0f)
                entryVisualProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = motionSpec.entryPhaseDurationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
                isTransitionFinished = true
            }
        }
    }

    //  监听评论状态
    val commentState by commentViewModel.commentState.collectAsStateWithLifecycle()
    val subReplyState by commentViewModel.subReplyState.collectAsStateWithLifecycle()

    LaunchedEffect(
        openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute,
        commentState.replies,
        commentState.isRepliesLoading,
        subReplyState.visible
    ) {
        if (openCommentRootRpidFromRoute <= 0L || hasHandledCommentRootFromRoute || subReplyState.visible) {
            return@LaunchedEffect
        }

        val rootReply = commentState.replies.firstOrNull { it.rpid == openCommentRootRpidFromRoute }
        if (rootReply != null) {
            commentViewModel.openSubReply(rootReply, openCommentTargetRpidFromRoute)
            hasHandledCommentRootFromRoute = true
        } else if (!commentState.isRepliesLoading) {
            val openStarted = commentViewModel.openSubReplyFromRoute(
                rootReplyId = openCommentRootRpidFromRoute,
                targetReplyId = openCommentTargetRpidFromRoute
            )
            if (openStarted) {
                hasHandledCommentRootFromRoute = true
            }
        }
    }
    val commentDefaultSortMode by com.android.purebilibili.core.store.SettingsManager
        .getCommentDefaultSortMode(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.SettingsManager.getCommentDefaultSortModeSync(context),
            lifecycle = lifecycleOwner.lifecycle
        )
    val commentFraudDetectionEnabled by com.android.purebilibili.core.store.SettingsManager
        .getCommentFraudDetectionEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )
    val commentMemberDecorationsEnabled by com.android.purebilibili.core.store.SettingsManager
        .getCommentMemberDecorationsEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = false,
            lifecycle = lifecycleOwner.lifecycle
        )
    val homeSettings by com.android.purebilibili.core.store.SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.HomeSettings(),
            lifecycle = lifecycleOwner.lifecycle
        )
    val tabletCommentPanelWidthPreset by com.android.purebilibili.core.store.SettingsManager
        .getTabletCommentPanelWidthPreset(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.TabletCommentPanelWidthPreset.STANDARD,
            lifecycle = lifecycleOwner.lifecycle
        )
    val videoAiSummaryEntryEnabled by com.android.purebilibili.core.store.SettingsManager
        .getVideoAiSummaryEntryEnabled(context)
        .collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycleOwner.lifecycle)
    val videoNoteEnabled by com.android.purebilibili.core.store.SettingsManager
        .getVideoNoteEnabled(context)
        .collectAsStateWithLifecycle(initialValue = true, lifecycle = lifecycleOwner.lifecycle)
    val videoNoteDefaultCollapsed by com.android.purebilibili.core.store.SettingsManager
        .getVideoNoteDefaultCollapsed(context)
        .collectAsStateWithLifecycle(initialValue = false, lifecycle = lifecycleOwner.lifecycle)
    val preferredCommentSortMode = remember(commentDefaultSortMode) {
        CommentSortMode.fromApiMode(commentDefaultSortMode)
    }
    VideoDetailCommentFraudOverlayAdapter(
        context = context,
        playbackViewModel = viewModel,
        commentViewModel = commentViewModel,
        aid = (uiState as? VideoPlaybackUiState.Success)?.info?.aid,
        fraudDetectionEnabled = commentFraudDetectionEnabled,
    )
    val sortPreferenceScope = rememberCoroutineScope()
    val danmakuEnabledForDetail by com.android.purebilibili.core.store.SettingsManager
        .getDanmakuEnabled(
            context,
            com.android.purebilibili.core.store.DanmakuSettingsScope.PORTRAIT
        )
        .collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )
    val showFavoriteFolderDialog by viewModel.favoriteFolderDialogVisible.collectAsStateWithLifecycle()
    val showCommentInput by viewModel.showCommentDialog.collectAsStateWithLifecycle()
    // [Blur] Haze State
    val hazeState = rememberRecoverableHazeState()

    val sponsorSegment by viewModel.currentSponsorSegment.collectAsStateWithLifecycle()
    val showSponsorSkipButton by viewModel.showSkipButton.collectAsStateWithLifecycle()
    val sponsorContributionState by viewModel.sponsorContributionUiState.collectAsStateWithLifecycle()

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val interactiveChoicePanel by viewModel.interactiveChoicePanel.collectAsStateWithLifecycle()

    // 📐 [大屏适配] 仅 Expanded 才启用平板分栏布局
    val windowSizeClass = com.android.purebilibili.core.util.LocalWindowSizeClass.current
    val isFlatFoldable = com.android.purebilibili.core.util.rememberIsFlatFoldable()
    val horizontalAdaptationEnabled by com.android.purebilibili.core.store.SettingsManager
        .getHorizontalAdaptationEnabled(context)
        .collectAsStateWithLifecycle(
            initialValue = windowSizeClass.isTabletDevice,
            lifecycle = lifecycleOwner.lifecycle
        )
    val immersiveVideoPageStatusBar by com.android.purebilibili.core.store.SettingsManager
        .getHideVideoPageStatusBar(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.SettingsManager
                .getHideVideoPageStatusBarSync(context),
            lifecycle = lifecycleOwner.lifecycle
        )
    val useTabletLayout = shouldUseTabletVideoLayout(
        isExpandedScreen = windowSizeClass.isExpandedScreen,
        isTabletDevice = windowSizeClass.isTabletDevice
    ) && horizontalAdaptationEnabled

    // 🔧 [修复] 追踪用户是否主动请求全屏（点击全屏按钮）
    // 使用 rememberSaveable 确保状态在横竖屏切换时保持
    var userRequestedFullscreen by rememberSaveable { mutableStateOf(false) }
    var manualPortraitHoldActive by rememberSaveable { mutableStateOf(false) }
    var preserveCurrentFrameOnFullscreenChange by remember { mutableStateOf(false) }
    var pendingFullscreenPositionRestoreMs by remember { mutableLongStateOf(-1L) }
    val activity = remember { context.findActivity() }
    val isActivityInMultiWindowMode = activity?.let { host ->
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && host.isInMultiWindowMode
    } ?: false

    // 📐 全屏模式逻辑：
    // - 手机：横屏时自动进入全屏
    // - 平板：仅用户主动切换全屏
    val fullscreenMode by com.android.purebilibili.core.store.SettingsManager
        .getFullscreenMode(context)
        .collectAsStateWithLifecycle(
            initialValue = com.android.purebilibili.core.store.FullscreenMode.AUTO,
            lifecycle = lifecycleOwner.lifecycle
        )
    val prefersManualFullscreenMode = remember(fullscreenMode) {
        fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.NONE ||
            fullscreenMode == com.android.purebilibili.core.store.FullscreenMode.VERTICAL
    }
    val isOrientationDrivenFullscreen = !prefersManualFullscreenMode &&
        shouldUseOrientationDrivenFullscreen(
        isCompactDevice = windowSizeClass.isCompactDevice
    )
    val isFullscreenMode = resolveVideoDetailFullscreenMode(
        isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
        isLandscape = isLandscape,
        userRequestedFullscreen = userRequestedFullscreen,
        isInMultiWindowMode = isActivityInMultiWindowMode
    )
    ManualFullscreenRequestLifecycleEffect(
        manualFullscreenRequested = userRequestedFullscreen,
        isFullscreenMode = isFullscreenMode,
        onReleaseManualFullscreenRequest = { userRequestedFullscreen = false }
    )
    LaunchedEffect(isFullscreenMode) {
        if (preserveCurrentFrameOnFullscreenChange) {
            preserveCurrentFrameOnFullscreenChange = false
        }
    }
    var previousPipMode by remember { mutableStateOf(isInPipMode) }
    LaunchedEffect(isInPipMode) { presentationState.syncPipMode(isInPipMode) }
    LaunchedEffect(isPipMode, subReplyState.visible) {
        val shouldDismissThreadDetail = shouldDismissCommentThreadDetailForPip(
            wasInPipMode = previousPipMode,
            isInPipMode = isPipMode,
            subReplyVisible = subReplyState.visible
        )
        previousPipMode = isPipMode
        if (shouldDismissThreadDetail) {
            commentViewModel.closeSubReply()
        }
    }
    val openFavoriteFolders: (VideoFavoriteEntryPoint) -> Unit = { entryPoint ->
        when (resolveVideoFavoriteAction(entryPoint)) {
            VideoFavoriteAction.ToggleFavorite -> engagementViewModel.toggleFavorite()
            VideoFavoriteAction.OpenFavoriteFolders -> viewModel.showFavoriteFolderDialog()
        }
    }

    //  [新增] 监听定时关闭状态
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsStateWithLifecycle()

    // 📖 [新增] 监听视频章节数据
    // 📖 [新增] 监听视频章节数据
    val viewPoints by viewModel.viewPoints.collectAsStateWithLifecycle()
    val pbpProgressData by viewModel.pbpProgressData.collectAsStateWithLifecycle()
    val sponsorProgressMarkers by viewModel.sponsorProgressMarkers.collectAsStateWithLifecycle()

    // [New] Codec & Audio Preferences
    val codecPreference by viewModel.videoCodecPreference.collectAsStateWithLifecycle()
    val secondCodecPreference by viewModel.videoSecondCodecPreference.collectAsStateWithLifecycle()
    val audioQualityPreference by viewModel.audioQualityPreference.collectAsStateWithLifecycle()

    //  [PiP修复] 记录视频播放器在屏幕上的位置，用于PiP窗口只显示视频区域
    var videoPlayerBounds by remember { mutableStateOf<android.graphics.Rect?>(null) }
    var videoPlayerRootBottomPx by remember { mutableIntStateOf(0) }

    // 📱 [优化] isPortraitFullscreen 和 isVerticalVideo 现在从 playerState 获取（见 playerState 定义后）

    // 🔁 [优化] 合并播放队列状态订阅，减少同帧多次重组
    val playlistUiState by PlaylistManager.uiState.collectAsStateWithLifecycle(
        initialValue = PlaylistUiState(),
        lifecycle = lifecycleOwner.lifecycle
    )
    val currentPlayMode = playlistUiState.playMode
    val playlistItems = playlistUiState.playlist
    val playlistCurrentIndex = playlistUiState.currentIndex
    val isExternalPlaylist = playlistUiState.isExternalPlaylist
    val externalPlaylistSource = playlistUiState.externalPlaylistSource
    val shouldShowExternalPlaylistQueueBar = shouldShowExternalPlaylistQueueBarByPolicy(
        isExternalPlaylist = isExternalPlaylist,
        externalPlaylistSource = externalPlaylistSource,
        playlistSize = playlistItems.size
    )
    val externalPlaylistQueueTitle = resolveExternalPlaylistQueueTitle(externalPlaylistSource)
    var showExternalPlaylistQueueSheet by rememberSaveable { mutableStateOf(false) }
    var pendingVideoShare by remember { mutableStateOf<VideoSharePayload?>(null) }
    val externalPlaylistQueueSheetPresentation = remember {
        resolveExternalPlaylistQueueSheetPresentation(requireRealtimeHaze = true)
    }

    LaunchedEffect(shouldShowExternalPlaylistQueueBar) {
        if (!shouldShowExternalPlaylistQueueBar) {
            showExternalPlaylistQueueSheet = false
        }
    }

    LaunchedEffect(startAudioFromRoute, hasAutoEnteredAudioMode, uiState) {
        if (shouldAutoEnterAudioModeFromRoute(
                startAudioFromRoute = startAudioFromRoute,
                hasAutoEnteredAudioMode = hasAutoEnteredAudioMode,
                isVideoLoadSuccess = uiState is VideoPlaybackUiState.Success
            )
        ) {
            hasAutoEnteredAudioMode = true
            presentationState.markNavigatingToAudioMode()
            viewModel.setAudioMode(true)
            onNavigateToAudioMode()
        }
    }

    //  从小窗展开时自动进入全屏
    LaunchedEffect(startInFullscreen, isOrientationDrivenFullscreen, isLandscape) {
        if (startInFullscreen) {
            if (!isOrientationDrivenFullscreen) {
                userRequestedFullscreen = true
            } else {
                context.findActivity()?.let { activity ->
                    val isInMultiWindowMode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        activity.isInMultiWindowMode
                    if (!shouldApplyStartFullscreenOrientationRequest(
                            startInFullscreen = startInFullscreen,
                            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                            isLandscape = isLandscape,
                            isInMultiWindowMode = isInMultiWindowMode
                        )
                    ) {
                        if (isInMultiWindowMode) {
                            userRequestedFullscreen = true
                        }
                        return@let
                    }
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
            }
        }
    }

    //  用于跟踪组件是否正在退出，防止 SideEffect 覆盖恢复操作
    var isScreenActive by rememberSaveable(currentBvid) { mutableStateOf(true) }

    //  [关键] 保存进入前的状态栏配置（在 DisposableEffect 外部定义以便复用）
    val window = remember { activity?.window }
    var entryRequestedOrientation by rememberSaveable {
        mutableIntStateOf(
            resolveVideoDetailEntryOrientationSnapshot(
                currentRequestedOrientation = activity?.requestedOrientation
            )
        )
    }
    val insetsController = remember {
        if (window != null && activity != null) {
            WindowCompat.getInsetsController(window, window.decorView)
        } else null
    }
    val originalSystemBarsSnapshot = remember(window, insetsController) {
        resolveVideoDetailSystemBarsSnapshot(
            statusBarColor = window?.statusBarColor,
            navigationBarColor = window?.navigationBarColor,
            lightStatusBars = insetsController?.isAppearanceLightStatusBars,
            lightNavigationBars = insetsController?.isAppearanceLightNavigationBars,
            systemBarsBehavior = insetsController?.systemBarsBehavior,
            fallbackColor = android.graphics.Color.TRANSPARENT,
            fallbackLightBars = true,
            fallbackSystemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        )
    }

    //  [新增] 恢复状态栏的函数（可复用）
    val restoreStatusBar = remember {
        {
            if (window != null && insetsController != null) {
                if (shouldShowSystemBarsOnVideoDetailExit()) {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
                insetsController.systemBarsBehavior = originalSystemBarsSnapshot.systemBarsBehavior
                insetsController.isAppearanceLightStatusBars = originalSystemBarsSnapshot.lightStatusBars
                insetsController.isAppearanceLightNavigationBars = originalSystemBarsSnapshot.lightNavigationBars
                window.statusBarColor = originalSystemBarsSnapshot.statusBarColor
                window.navigationBarColor = originalSystemBarsSnapshot.navigationBarColor
            }
        }
    }

    //  [修复] 包装的 onBack，在导航之前立即恢复状态栏并通知小窗管理器
    val latestOnBack by rememberUpdatedState(onBack)
    val latestOnHomeClick by rememberUpdatedState(onHomeClick)
    val latestOnMarkReturningFromDetail by rememberUpdatedState(onMarkReturningFromDetail)
    val topBarActionHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    var pendingTopBarActionRunnable by remember { mutableStateOf<Runnable?>(null) }
    var isActuallyLeaving by rememberSaveable(currentBvid) { mutableStateOf(false) }
    var forceCoverOnlyOnReturn by remember { mutableStateOf(false) }
    var systemBarsReapplyGeneration by remember(currentBvid) { mutableIntStateOf(0) }
    val transitionState = rememberVideoDetailTransitionState(
        bvid = bvid,
        sourceRoute = sourceRouteForSharedElement,
        transitionEnabled = transitionEnabled,
        keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
        motionSpec = homeSharedTransitionMotionSpec,
        routeSheetMotion = routeSheetMotion,
    )
    val rootAnimatedVisibilityScope = transitionState.animatedVisibilityScope
    val rootSharedTransitionScope = transitionState.sharedTransitionScope
    val isExitTransitionInProgress = transitionState.isExitTransitionInProgress
    val detailShellSharedBoundsEnabled = transitionState.detailShellSharedBoundsEnabled
    val suppressEnterFadeAfterBackPreview = transitionState.suppressEnterFadeAfterBackPreview
    val detailTransitionProgress = transitionState.progress
    val detailChildTransitionEnabled = transitionState.detailChildTransitionEnabled
    val coverSharedBoundsActive = transitionState.coverSharedBoundsActive
    val sharedBoundsActive = transitionState.sharedBoundsActive
    val routeSheetFrameProvider = transitionState.routeSheetFrameProvider
    val detailShellShape = remember(sharedTransitionSourceCornerDp) {
        RoundedCornerShape(sharedTransitionSourceCornerDp.dp)
    }
    val isSharedTransitionActive = rootSharedTransitionScope?.isTransitionActive == true
    val suppressDetailShellForRelatedChild = shouldSuppressDetailShellSharedBoundsForRelatedChildTransition(
        detailBvid = bvid,
        lastClickedVideoSourceKey = CardPositionManager.lastClickedVideoSourceKey,
        isSharedTransitionActive = isSharedTransitionActive,
    )
    LaunchedEffect(isNavigatingToVideo, homeSharedTransitionMotionSpec.durationMillis) {
        if (!isNavigatingToVideo) return@LaunchedEffect
        // 进场 morph 结束后恢复父壳，避免长期禁用导致再回列表时丢 shell。
        kotlinx.coroutines.delay(homeSharedTransitionMotionSpec.durationMillis.toLong() + 48L)
        if (isNavigatingToVideo) {
            presentationState.clearNavigatingToVideo()
        }
    }
    val detailShellModifier = Modifier.videoCardShellSharedBoundsOrEmpty(
        enabled = detailShellSharedBoundsEnabled &&
            !isNavigatingToVideo &&
            !suppressDetailShellForRelatedChild,
        sharedTransitionScope = rootSharedTransitionScope,
        animatedVisibilityScope = rootAnimatedVisibilityScope,
        bvid = bvid,
        sourceRoute = sourceRouteForSharedElement,
        motionSpec = homeSharedTransitionMotionSpec,
        clipShape = detailShellShape,
        role = VideoCardShellSharedBoundsRole.DetailShell,
        // 竖屏全屏（点赞/关注/发弹幕那套）：整卡展开到全屏，不要按顶部横屏播放器 TopCenter 落点。
        fillFullscreenShell = isPortraitFullscreen ||
            shouldStartInPortraitFullscreenFromRouteHint(
                autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
                startAudioFromRoute = startAudioFromRoute,
                initialVerticalFromRoute = initialVerticalFromRoute,
                directPortraitEntryFromRoute = directPortraitEntryFromRoute,
            ),
    )
    val coverTakeoverBeforeBackDelayMillis = remember {
        resolveCoverTakeoverDelayBeforeBackNavigationMillis()
    }
    // 仅当详情页自身正在回收到来源卡片时进入离开态。详情页作为上层页面的
    // 直接返回目标时必须保留已加载的播放器与内容，避免预测返回预览变成封面占位。
    val isCardReturnExitInProgress = shouldTreatVideoDetailCardExitAsReturning(
        isExitTransitionInProgress = isExitTransitionInProgress,
        sharedBoundsActive = sharedBoundsActive,
        keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
    )
    val forceCoverOnlyForReturn = resolveForceCoverOnlyForReturn(
        forceCoverOnlyOnReturn = forceCoverOnlyOnReturn,
        transitionEnabled = transitionEnabled,
        isCardReturnExitInProgress = isCardReturnExitInProgress
    )
    // 离开态：次要内容淡出等。ImmediatePlayback live morph 时不把视觉交给常驻封面。
    // 注意：预测 seek 中 isCardReturnExitInProgress 会为 true，但这不是「已提交」。
    val isSessionReturningToCard = isReturningFromDetail &&
        transitionEnabled &&
        sharedBoundsActive &&
        !keepLoadedContentForBackPreview
    val useReturningVideoDetailVisualState = shouldUseReturningVideoDetailVisualState(
        forceCoverOnlyForReturn = forceCoverOnlyForReturn,
        isCardReturnExitInProgress = isCardReturnExitInProgress,
        isSessionReturningToCard = isSessionReturningToCard,
    )
    // 封面/播放器 handoff 只认已提交（按钮返回或 markReturning），预测跟手阶段保持实时画面。
    val isCommittedCardReturn = shouldTreatVideoDetailCardReturnAsCommitted(
        isActuallyLeaving = isActuallyLeaving,
        isSessionReturningToCard = isSessionReturningToCard,
    )
    val hasResidentReturnCover = coverUrl.isNotBlank()
    val detailContentReadyForLiveReturnMorph = shouldTreatVideoDetailContentReadyForLiveReturnMorph(
        hasSuccessfulDetailContent = uiState is VideoPlaybackUiState.Success,
    )
    // liveReturnMorph / ownership 依赖 player 首帧，见 playerState 定义之后。

    val handleTopBarAction = remember(
        miniPlayerManager,
        currentBvid,
        coverTakeoverBeforeBackDelayMillis,
        topBarActionHandler,
        detailShellSharedBoundsEnabled,
    ) {
        action@{ action: VideoDetailTopBarAction ->
            if (isActuallyLeaving) return@action
            isActuallyLeaving = true // 标记确实是用户通过点击或返回键离开
            isScreenActive = false  // 标记页面正在退出
            latestOnMarkReturningFromDetail()
            // 🎯 通知小窗管理器这是用户主动导航离开。
            // 有 shell sharedBounds 时延后停播，避免一镜到底落位前 surface 被掐掉。
            miniPlayerManager?.markLeavingByNavigation(
                expectedBvid = currentBvid,
                deferPlaybackStop = shouldDeferPlaybackStopForSharedLiveReturn(
                    cardTransitionEnabled = detailShellSharedBoundsEnabled,
                    hasSourceRoute = true,
                ),
            )

            restoreStatusBar() // 立即恢复状态栏（动画开始前）
            pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
            val navigationRunnable = Runnable {
                pendingTopBarActionRunnable = null
                when (action) {
                    VideoDetailTopBarAction.BACK -> latestOnBack()
                    VideoDetailTopBarAction.HOME -> latestOnHomeClick()
                }
            }
            pendingTopBarActionRunnable = navigationRunnable
            if (coverTakeoverBeforeBackDelayMillis > 0L) {
                topBarActionHandler.postDelayed(navigationRunnable, coverTakeoverBeforeBackDelayMillis)
            } else {
                navigationRunnable.run()
            }
        }
    }
    val handleBack = remember(handleTopBarAction) {
        {
            handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = false))
        }
    }

    LaunchedEffect(isExitTransitionInProgress, isActuallyLeaving) {
        if (shouldRestoreSystemBarsDuringVideoDetailExitTransition(
                isExitTransitionInProgress = isExitTransitionInProgress,
                isActuallyLeaving = isActuallyLeaving
            )
        ) {
            isScreenActive = false
            restoreStatusBar()
            return@LaunchedEffect
        }
        if (
            shouldReactivateVideoDetailSystemBarsAfterCancelledExit(
                isExitTransitionInProgress = isExitTransitionInProgress,
                isActuallyLeaving = isActuallyLeaving,
                isScreenActive = isScreenActive,
            )
        ) {
            isScreenActive = true
            systemBarsReapplyGeneration += 1
        }
    }

    var wasKeepLoadedContentForBackPreview by remember(currentBvid) {
        mutableStateOf(keepLoadedContentForBackPreview)
    }
    LaunchedEffect(keepLoadedContentForBackPreview, isActuallyLeaving) {
        if (
            shouldReapplyVideoDetailSystemBarsAfterBecomingTop(
                wasKeepLoadedContentForBackPreview = wasKeepLoadedContentForBackPreview,
                keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
                isActuallyLeaving = isActuallyLeaving,
            )
        ) {
            isScreenActive = true
            systemBarsReapplyGeneration += 1
        }
        wasKeepLoadedContentForBackPreview = keepLoadedContentForBackPreview
    }

    LaunchedEffect(predictiveBackCancelRecoveryGeneration) {
        if (predictiveBackCancelRecoveryGeneration <= 0) return@LaunchedEffect
        isScreenActive = true
        systemBarsReapplyGeneration += 1
    }

    LaunchedEffect(currentBvid) {
        pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
        pendingTopBarActionRunnable = null
        isScreenActive = true
        forceCoverOnlyOnReturn = false
        if (shouldClearStaleReturningStateOnVideoDetailEnter(isReturningFromDetail)) {
            onClearReturningFromDetail()
        }
    }

    DisposableEffect(lifecycleOwner, currentBvid) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
                pendingTopBarActionRunnable = null
                isScreenActive = true
                forceCoverOnlyOnReturn = false
                if (shouldClearStaleReturningStateOnVideoDetailEnter(isReturningFromDetail)) {
                    onClearReturningFromDetail()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 🔄 [新增] 自动横竖屏切换 - 跟随手机传感器方向
    val autoRotateEnabled by com.android.purebilibili.core.store.SettingsManager
        .getAutoRotateEnabled(context).collectAsStateWithLifecycle(
            initialValue = false,
            lifecycle = lifecycleOwner.lifecycle
        )
    val systemAutoRotateEnabled by rememberSystemAutoRotateEnabled(context)
    val cardAnimationEnabled by com.android.purebilibili.core.store.SettingsManager
        .getCardAnimationEnabled(context).collectAsStateWithLifecycle(
            initialValue = true,
            lifecycle = lifecycleOwner.lifecycle
        )

    VideoDetailHighRefreshRateEffect(
        activity = activity,
        isScreenActive = isScreenActive,
    )

    DisposableEffect(Unit) {
        //  [沉浸式] 启用边到边显示，让内容延伸到状态栏下方
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        onDispose {
            pendingTopBarActionRunnable?.let(topBarActionHandler::removeCallbacks)
            pendingTopBarActionRunnable = null
            //  [关键] 标记页面正在退出，防止 SideEffect 覆盖
            isScreenActive = false

            // ⚡ [性能优化] Phase 1: 同步执行 — 仅保留影响视觉的关键操作
            val layoutParams = window?.attributes
            layoutParams?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window?.attributes = layoutParams
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            restoreStatusBar()

            // ⚡ [性能优化] Phase 1b: CardPositionManager 状态（影响首页卡片动画，必须同步）
            val shouldHandleAsNavigationExit = shouldHandleVideoDetailDisposeAsNavigationExit(
                isNavigatingToAudioMode = isNavigatingToAudioMode,
                isNavigatingToMiniMode = isNavigatingToMiniMode,
                isMiniModeActive = miniPlayerManager?.isMiniMode == true,
                isChangingConfigurations = activity?.isChangingConfigurations == true,
                isNavigatingToVideo = resolveIsNavigatingToVideoDuringDispose(
                    localNavigatingToVideo = isNavigatingToVideo,
                    managerNavigatingToVideo = miniPlayerManager?.isNavigatingToVideo == true
                )
            )
            if (shouldMarkReturningStateOnVideoDetailDispose(shouldHandleAsNavigationExit)) {
                onMarkReturningFromDetail()
            } else {
                onClearReturningFromDetail()
            }

            // ⚡ [性能优化] Phase 2: 延迟执行 — 非视觉的系统调用推迟到下一帧
            // PiP 重置、通知清理、Service 停止、屏幕方向恢复等操作不影响退出动画
            // 将它们 post 到主线程 Handler，在导航转场动画完成后再执行
            val deferredActivity = activity
            val deferredContext = context
            val deferredShouldHandleAsNavExit = shouldHandleAsNavigationExit
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                // 🔧 重置 PiP 参数
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    deferredActivity?.let { act ->
                        try {
                            val pipParams = android.app.PictureInPictureParams.Builder()
                                .setAutoEnterEnabled(false)
                                .build()
                            act.setPictureInPictureParams(pipParams)
                        } catch (_: Exception) {}
                    }
                }

                // 🔕 通知清理 + Service 停止
                if (deferredShouldHandleAsNavExit) {
                    val notificationManager = deferredContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE)
                        as android.app.NotificationManager
                    notificationManager.cancel(1001)
                    notificationManager.cancel(PlaybackService.NOTIFICATION_ID)
                    try {
                        deferredContext.startService(
                            android.content.Intent(deferredContext, PlaybackService::class.java).apply {
                                action = PlaybackService.ACTION_STOP_FOREGROUND
                            }
                        )
                    } catch (_: Exception) {}
                }

                // 恢复进入详情页前的方向请求，避免平板误横屏后退不回去。
                deferredActivity?.requestedOrientation = resolveVideoDetailExitRequestedOrientation(
                    originalRequestedOrientation = entryRequestedOrientation
                )
            }
        }
    }

    val playbackEventState = rememberVideoDetailPlaybackEventState()
    VideoDetailPlaybackEventEffects(
        context = context,
        viewModel = viewModel,
        state = playbackEventState,
    )
    val danmakuManager = playbackEventState.danmakuManager

    //  [PiP修复] 当视频播放器位置更新时，同步更新PiP参数
    //  [修复] 只有支持系统 PiP 的模式才启用自动进入 PiP
    val pipModeEnabled = remember {
        com.android.purebilibili.core.store.SettingsManager.getMiniPlayerModeSync(context)
            .supportsSystemPip
    }
    val isReducedActionMotion = !cardAnimationEnabled

    VideoDetailPipParamsEffect(
        context = context,
        activity = activity,
        playerBounds = videoPlayerBounds,
        pipModeEnabled = pipModeEnabled,
        player = miniPlayerManager?.player,
    )

    // 📱 [修复] 提升竖屏全屏状态到 Screen 级别，防止 VideoPlayerState 重建时状态丢失
    val useSharedPortraitPlayer = shouldUseSharedPlayerForPortraitFullscreen()
    val portraitPagerMotionSpec = remember {
        resolveStandalonePortraitPagerMotionSpec()
    }
    val shouldAnimatePortraitPager = remember(useSharedPortraitPlayer, directPortraitEntryFromRoute) {
        shouldAnimateStandalonePortraitPager(
            useSharedPlayer = useSharedPortraitPlayer,
            directPortraitEntry = directPortraitEntryFromRoute
        )
    }
    val inlineReturnAnimMs = if (shouldAnimatePortraitPager) {
        portraitPagerMotionSpec.inlineReturnDurationMillis
    } else {
        0
    }
    val inlinePlayerAlpha = animateFloatAsState(
        targetValue = if (isPortraitFullscreen) 0f else 1f,
        animationSpec = tween(
            durationMillis = inlineReturnAnimMs,
            easing = FastOutSlowInEasing
        ),
        label = "inline-player-alpha"
    )
    val inlinePlayerScale = animateFloatAsState(
        targetValue = if (isPortraitFullscreen) {
            portraitPagerMotionSpec.inlineReturnInitialScale
        } else {
            1f
        },
        animationSpec = tween(
            durationMillis = inlineReturnAnimMs,
            easing = FastOutSlowInEasing
        ),
        label = "inline-player-return-scale"
    )
    var portraitSyncSnapshotBvid by rememberSaveable { mutableStateOf<String?>(null) }
    var portraitSyncSnapshotCid by remember { mutableLongStateOf(0L) }
    var portraitSyncSnapshotPositionMs by remember { mutableLongStateOf(0L) }
    var hasPendingPortraitSync by remember { mutableStateOf(false) }
    var hasDeferredPortraitRestoreAfterExternalNavigation by rememberSaveable { mutableStateOf(false) }
    var pendingMainReloadBvidAfterPortrait by rememberSaveable { mutableStateOf<String?>(null) }
    var portraitPendingSelectionBvid by rememberSaveable { mutableStateOf<String?>(null) }
    // 返回 morph 中栈已 pop 时 isVisible=false，仍须保活 surface，避免壳缩前半段黑掉。
    val playbackSessionActiveForMorph = shouldKeepPlaybackSessionActiveForSharedReturnMorph(
        isVisible = isVisible,
        sharedBoundsActive = sharedBoundsActive,
        isExitTransitionInProgress = isExitTransitionInProgress,
    )
    // 初始化播放器状态
    val playerState = rememberVideoPlayerState(
        context = context,
        viewModel = viewModel,
        bvid = currentBvid,
        cid = playbackTargetCid,
        fallbackResumePositionMs = resumePositionMsFromRoute,
        startPaused = isPortraitFullscreen && !useSharedPortraitPlayer,
        entryTransitionFinished = entryPlaybackReady,
        playbackSessionActive = playbackSessionActiveForMorph,
    )
    LaunchedEffect(isFullscreenMode) {
        val restorePositionMs = pendingFullscreenPositionRestoreMs
        if (restorePositionMs <= 0L) return@LaunchedEffect

        val player = playerState.player
        kotlinx.coroutines.withTimeoutOrNull(3_000L) {
            while (isActive) {
                if (
                    player.mediaItemCount > 0 &&
                    player.playbackState == Player.STATE_READY
                ) {
                    val currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                    if (currentPositionMs + 750L < restorePositionMs) {
                        player.seekTo(restorePositionMs)
                        com.android.purebilibili.core.util.Logger.d(
                            "VideoDetailScreen",
                            "Restored playback position after fullscreen change: " +
                                "${currentPositionMs}ms -> ${restorePositionMs}ms"
                        )
                    }
                    break
                }
                kotlinx.coroutines.delay(32L)
            }
        }
        pendingFullscreenPositionRestoreMs = -1L
    }
    VideoDetailKeepScreenOnEffect(
        window = window,
        player = playerState.player,
    )
    val isVideoPlaying by produceState(
        initialValue = playerState.player.isPlaying,
        key1 = playerState.player
    ) {
        val player = playerState.player
        value = player.isPlaying
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                value = player.isPlaying
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                value = player.isPlaying
            }
        }
        player.addListener(listener)
        awaitDispose {
            player.removeListener(listener)
        }
    }
    LaunchedEffect(currentBvid, isVideoPlaying) {
        if (isVideoPlaying) {
            hasStartedCurrentVideoPlayback = true
        }
    }
    val playerDebugInfo by playerState.debugInfo.collectAsStateWithLifecycle()
    val hasRenderedFirstFrameForReturn = remember(playerDebugInfo.firstFrame) {
        playerDebugInfo.firstFrame.equals("rendered", ignoreCase = true)
    }
    // 全量 Success 但无首帧 / 强制封面 UI 时禁止 LIVE，避免黑壳缩回。
    val hasRenderableLiveFrameForReturn = shouldTreatLiveSurfaceRenderableForReturnMorph(
        hasRenderedFirstFrame = hasRenderedFirstFrameForReturn,
        forceCoverUi = forceCoverOnlyForReturn,
    )
    val returnPlaybackIntent = resolveVideoDetailReturnPlaybackIntent(
        entryPlaybackIntent = videoSharedPlaybackIntent,
        hasRenderableLiveFrame = hasRenderableLiveFrameForReturn,
    )
    val candidateReturnCoverOwnership = resolveVideoDetailReturnCoverOwnership(
        transitionEnabled = transitionEnabled,
        sharedBoundsActive = sharedBoundsActive,
        keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
        playbackIntent = returnPlaybackIntent,
        detailContentReady = detailContentReadyForLiveReturnMorph,
        hasResidentCover = hasResidentReturnCover,
        hasRenderableLiveFrame = hasRenderableLiveFrameForReturn,
        liveSurfaceCardTransitionEnabled = liveSurfaceCardTransitionEnabled,
    )
    // 返回会话 ownership：可升 LIVE（保实时画面），禁止 LIVE 降级（防闪）。
    var lockedReturnCoverOwnership by remember(bvid) {
        mutableStateOf<com.android.purebilibili.core.ui.transition.VideoCardReturnCoverOwnership?>(null)
    }
    val isReturnCoverOwnershipSessionActive =
        useReturningVideoDetailVisualState || isCardReturnExitInProgress
    val (nextLockedReturnCoverOwnership, returnCoverOwnership) =
        resolveVideoDetailReturnSessionLockedOwnership(
            lockedOwnership = lockedReturnCoverOwnership,
            isReturnSessionActive = isReturnCoverOwnershipSessionActive,
            candidateOwnership = candidateReturnCoverOwnership,
        )
    SideEffect {
        if (lockedReturnCoverOwnership != nextLockedReturnCoverOwnership) {
            lockedReturnCoverOwnership = nextLockedReturnCoverOwnership
        }
    }
    val liveReturnMorph = isLiveReturnMorphFromOwnership(returnCoverOwnership)
    val useResidentCoverForCommittedReturn = shouldHandResidentCoverFromOwnership(
        ownership = returnCoverOwnership,
        useReturningVisualState = isCommittedCardReturn,
        hasResidentCover = hasResidentReturnCover,
    )
    // live morph 时绝不 forceCoverOnly；预测 seek 未提交时也不 forceCover，避免封面瞬间盖住播放器。
    val forceCoverOnlyForLiveSafeReturn = shouldForceCoverOnlyForReturnOwnership(
        ownership = returnCoverOwnership,
        useReturningVisualState = useReturningVideoDetailVisualState,
        forceCoverOnlyOnReturn = forceCoverOnlyForReturn,
        isCommittedCardReturn = isCommittedCardReturn,
    )
    val videoCardDepthBackgroundState = LocalVideoCardTransitionBackgroundState.current
    val videoCardTransitionDensity = LocalDensity.current
    val videoCardDetailChromeAlphaProvider = remember(videoCardDepthBackgroundState) {
        {
            resolveVideoCardDetailChromeAlpha(
                morphDepthProgress = videoCardDepthBackgroundState.progressProvider(),
                phase = videoCardDepthBackgroundState.phaseProvider(),
                isReturnGestureInProgress =
                    videoCardDepthBackgroundState.isReturnGestureInProgressProvider(),
            )
        }
    }
    // Settled 返回运动预算：保 LIVE 一镜到底，旁路减负。
    // 注意：不在 composition 读 morph progress（会每帧重绘整棵详情树）；
    // 相位只用 committed / exit 布尔信号；细粒度 alpha 仍在 graphicsLayer 内读 progress。
    val returnSessionPhase = resolveVideoDetailReturnSessionPhase(
        isCommittedCardReturn = isCommittedCardReturn,
        isExitTransitionInProgress = isCardReturnExitInProgress,
        settleProgress = when {
            !isCommittedCardReturn -> 0f
            // 退出过渡进行中：按 Morph 预算（弹幕/控制层减负，不停播）。
            isCardReturnExitInProgress -> 0.4f
            // 提交后过渡信号已结束：Settle，允许停播收尾。
            else -> 1f
        },
    )
    // 已提交返回时，播放器由 shared morph 接管；非共享正文立即让位，避免与来源卡标题叠层。
    val returnSecondaryContentAlphaPreview =
        resolveVideoDetailReturnSecondaryContentAlphaPreview(
            isCommittedCardReturn = isCommittedCardReturn,
        )
    val returnVisualBudget = resolveVideoDetailReturnVisualBudget(
        phase = returnSessionPhase,
        hasRenderableLiveFrame = hasRenderableLiveFrameForReturn,
        reduceMotion = videoCardDepthBackgroundState.motionTierProvider() ==
            com.android.purebilibili.core.ui.adaptive.MotionTier.Reduced,
        secondaryContentAlpha = returnSecondaryContentAlphaPreview,
    )
    // Live morph 强制 playerMode=LiveMorph 时，有帧才 Live；无帧 Resident（与 ownership 一致）。
    val effectiveDanmakuEnabledForDetail =
        danmakuEnabledForDetail && !shouldPauseHideDanmakuForReturnBudget(returnVisualBudget)
    val detachSecondaryContentForReturn =
        shouldDetachSecondaryContentForReturnBudget(returnVisualBudget)
    val suppressOverlayControlsForReturn =
        shouldSuppressOverlayControlsForReturnBudget(returnVisualBudget)
    val routedCommentInteractionActive =
        openCommentRootRpidFromRoute > 0L &&
            (subReplyState.visible || subReplyState.isLoading)
    val commentInteractionActive =
        routedCommentInteractionActive || subReplyState.visible || showCommentInput
    LaunchedEffect(commentInteractionActive) {
        viewModel.setCommentInteractionActive(commentInteractionActive)
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.setCommentInteractionActive(false)
        }
    }
    var hasAutoPausedForRoutedComment by rememberSaveable(
        bvid,
        openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute
    ) {
        mutableStateOf(false)
    }
    LaunchedEffect(
        openCommentRootRpidFromRoute,
        videoSharedPlaybackIntent,
        isVideoPlaying,
        hasAutoPausedForRoutedComment
    ) {
        val shouldAutoPause =
            openCommentRootRpidFromRoute > 0L &&
                videoSharedPlaybackIntent == VideoSharedTransitionPlaybackIntent.ImmediatePlayback &&
                isVideoPlaying &&
                !hasAutoPausedForRoutedComment
        if (shouldAutoPause) {
            com.android.purebilibili.feature.video.usecase.pausePlayerFromUserAction(playerState.player)
            hasAutoPausedForRoutedComment = true
        }
    }
    val isPlaybackPaused by produceState(
        initialValue = resolveIsPlaybackPausedForCollapse(
            playWhenReady = playerState.player.playWhenReady,
            playbackState = playerState.player.playbackState
        ),
        key1 = playerState.player,
        key2 = currentBvid,
        key3 = currentBvidCid
    ) {
        val player = playerState.player

        fun updatePausedState() {
            value = resolveIsPlaybackPausedForCollapse(
                playWhenReady = player.playWhenReady,
                playbackState = player.playbackState
            )
        }

        updatePausedState()
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePausedState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePausedState()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                updatePausedState()
            }
        }
        player.addListener(listener)
        awaitDispose {
            player.removeListener(listener)
        }
    }
    val subtitleAutoPreference by com.android.purebilibili.core.store.SettingsManager
        .getSubtitleAutoPreference(context)
        .collectAsStateWithLifecycle(
            initialValue = SubtitleAutoPreference.OFF,
            lifecycle = lifecycleOwner.lifecycle
        )
    val subtitleAudioManager = remember {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    }
    val subtitleAutoModeMuted = remember(playerState.player, subtitleAudioManager, currentBvid) {
        val systemMuted = runCatching {
            subtitleAudioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC) <= 0
        }.getOrDefault(false)
        systemMuted || playerState.player.volume <= 0f
    }
    val subtitlePreferenceSession = remember(uiState, currentBvid, subtitleAutoPreference, subtitleAutoModeMuted) {
        val success = uiState as? VideoPlaybackUiState.Success
        if (success == null) {
            resolveSubtitlePreferenceSession(
                bvid = currentBvid,
                cid = 0L,
                primaryLanguage = null,
                secondaryLanguage = null,
                primaryTrackLikelyAi = false,
                secondaryTrackLikelyAi = false,
                hasPrimaryTrack = false,
                hasSecondaryTrack = false,
                preference = subtitleAutoPreference,
                isMuted = subtitleAutoModeMuted
            )
        } else {
            val subtitleBelongsToCurrentVideo =
                success.subtitleOwnerBvid == success.info.bvid &&
                    success.subtitleOwnerCid == success.info.cid &&
                    success.info.cid > 0L
            val hasPrimaryTrack = subtitleBelongsToCurrentVideo &&
                (!success.subtitlePrimaryTrackKey.isNullOrBlank() || !success.subtitlePrimaryLanguage.isNullOrBlank())
            val hasSecondaryTrack = subtitleBelongsToCurrentVideo &&
                (!success.subtitleSecondaryTrackKey.isNullOrBlank() || !success.subtitleSecondaryLanguage.isNullOrBlank())
            resolveSubtitlePreferenceSession(
                bvid = success.info.bvid,
                cid = success.info.cid,
                primaryLanguage = success.subtitlePrimaryLanguage,
                secondaryLanguage = success.subtitleSecondaryLanguage,
                primaryTrackLikelyAi = subtitleBelongsToCurrentVideo && success.subtitlePrimaryLikelyAi,
                secondaryTrackLikelyAi = subtitleBelongsToCurrentVideo && success.subtitleSecondaryLikelyAi,
                hasPrimaryTrack = hasPrimaryTrack,
                hasSecondaryTrack = hasSecondaryTrack,
                preference = subtitleAutoPreference,
                isMuted = subtitleAutoModeMuted
            )
        }
    }
    var subtitlePreferenceSessionKey by rememberSaveable { mutableStateOf<String?>(null) }
    var subtitleDisplayModeOverride by rememberSaveable { mutableStateOf(SubtitleDisplayMode.OFF) }
    LaunchedEffect(subtitlePreferenceSession.key) {
        subtitleDisplayModeOverride = resolveSubtitleDisplayModePreference(
            previousSessionKey = subtitlePreferenceSessionKey,
            nextSessionKey = subtitlePreferenceSession.key,
            previousMode = subtitleDisplayModeOverride,
            nextInitialMode = subtitlePreferenceSession.initialMode
        )
        subtitlePreferenceSessionKey = subtitlePreferenceSession.key
    }

    var hasAppliedInitialPageSwitch by remember(currentBvid, playbackTargetCid) { mutableStateOf(false) }
    LaunchedEffect(uiState, currentBvid, playbackTargetCid, hasAppliedInitialPageSwitch) {
        if (hasAppliedInitialPageSwitch) return@LaunchedEffect
        val success = uiState as? VideoPlaybackUiState.Success ?: return@LaunchedEffect
        if (success.info.bvid != currentBvid) return@LaunchedEffect

        val targetPageIndex = resolveInitialPageIndex(
            requestedCid = playbackTargetCid,
            currentCid = success.info.cid,
            pages = success.info.pages
        )
        hasAppliedInitialPageSwitch = true
        if (targetPageIndex != null) {
            viewModel.switchPage(targetPageIndex)
        }
    }

    // 🎯 [修复] 确保在 VideoPlayerState 销毁之前通知 MiniPlayerManager 页面退出
    // 必须在 playerState 之后声明此 Effect，这样它会在 playerState.onDispose 之前执行（LIFO 顺序）
    DisposableEffect(playerState) {
        onDispose {
            // 标记页面正在退出
            // 配置切换不标记离开；音频模式/小窗模式为主动保活场景，也不标记离开。
            val isChangingConfigurations = activity?.isChangingConfigurations == true
            val shouldHandleAsNavigationExit = shouldHandleVideoDetailDisposeAsNavigationExit(
                isNavigatingToAudioMode = isNavigatingToAudioMode,
                isNavigatingToMiniMode = isNavigatingToMiniMode,
                isMiniModeActive = miniPlayerManager?.isMiniMode == true,
                isChangingConfigurations = isChangingConfigurations,
                isNavigatingToVideo = resolveIsNavigatingToVideoDuringDispose(
                    localNavigatingToVideo = isNavigatingToVideo,
                    managerNavigatingToVideo = miniPlayerManager?.isNavigatingToVideo == true
                )
            )
            if (shouldHandleAsNavigationExit) {
                com.android.purebilibili.core.util.Logger.d(
                    "VideoDetailScreen",
                    "🛑 Disposing screen as navigation exit, notifying MiniPlayerManager"
                )
                miniPlayerManager?.markLeavingByNavigation(expectedBvid = currentBvid)
            } else {
                com.android.purebilibili.core.util.Logger.d(
                    "VideoDetailScreen",
                    "💤 Screen disposed without navigation-exit mark (audioMode=$isNavigatingToAudioMode, miniMode=$isNavigatingToMiniMode, changingConfig=$isChangingConfigurations)"
                )
            }
        }
    }

    //  [性能优化] 生命周期感知：进入后台时暂停播放，返回前台时继续
    //  [修复] 此处逻辑已移至 VideoPlayerState.kt 统一处理
    // 删除冗余的暂停逻辑，避免与 VideoPlayerState 中的生命周期处理冲突
    // VideoPlayerState 会检查 PiP/小窗模式来决定是否暂停

    // 📱 [优化] 竖屏视频检测已移至 VideoPlayerState 集中管理
    val isVerticalVideo by playerState.isVerticalVideo.collectAsStateWithLifecycle()
    val continuousFullscreenTransitionEnabled = transitionEnabled &&
        isOrientationDrivenFullscreen &&
        windowSizeClass.isCompactDevice &&
        !isActivityInMultiWindowMode &&
        !isVerticalVideo
    var continuousPlayerPhase by rememberSaveable(currentBvid) {
        mutableStateOf(
            if (isLandscape) {
                ContinuousPlayerTransitionPhase.Fullscreen
            } else {
                ContinuousPlayerTransitionPhase.Inline
            }
        )
    }
    val continuousPlayerProgress = remember(currentBvid) {
        Animatable(if (isLandscape) 1f else 0f)
    }
    val isContinuousPlayerMorphing = continuousFullscreenTransitionEnabled &&
        (continuousPlayerPhase == ContinuousPlayerTransitionPhase.Expanding ||
            continuousPlayerPhase == ContinuousPlayerTransitionPhase.Collapsing)

    fun applyContinuousPlayerDecision(decision: ContinuousPlayerTransitionDecision) {
        continuousPlayerPhase = decision.phase
        when (decision.orientationRequest) {
            ContinuousPlayerOrientationRequest.None -> Unit
            ContinuousPlayerOrientationRequest.Landscape -> {
                userRequestedFullscreen = true
                manualPortraitHoldActive = false
                activity?.requestedOrientation = resolvePhoneFullscreenEnterOrientation(
                    fullscreenMode = fullscreenMode,
                    isVerticalVideo = false,
                ) ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            ContinuousPlayerOrientationRequest.Portrait -> {
                userRequestedFullscreen = false
                manualPortraitHoldActive = true
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    LaunchedEffect(
        continuousFullscreenTransitionEnabled,
        continuousPlayerPhase,
        isLandscape,
    ) {
        // 关闭 continuous morph 时也要清掉可能残留的全屏进度，否则再次启用/回竖屏会铺满。
        if (!continuousFullscreenTransitionEnabled) {
            if (!isLandscape && continuousPlayerProgress.value > 0.001f) {
                continuousPlayerProgress.snapTo(0f)
                continuousPlayerPhase = ContinuousPlayerTransitionPhase.Inline
            }
            return@LaunchedEffect
        }
        when {
            // 系统旋进横屏：从 inline/半途相位直接落到全屏高度。
            isLandscape &&
                continuousPlayerPhase != ContinuousPlayerTransitionPhase.Fullscreen &&
                continuousPlayerPhase != ContinuousPlayerTransitionPhase.AwaitingPortrait -> {
                continuousPlayerProgress.snapTo(1f)
                continuousPlayerPhase = ContinuousPlayerTransitionPhase.Fullscreen
            }
            // 仅清理「已处于 Fullscreen 但窗口已回竖屏」的陈旧进度。
            // Expanding/AwaitingLandscape 仍是点击进入全屏的有效链路；在方向切换前
            // 提前把它们改回 Inline 会跳过 ExpansionFinished，导致横屏请求永远不发出。
            !isLandscape &&
                continuousPlayerPhase == ContinuousPlayerTransitionPhase.Fullscreen -> {
                continuousPlayerProgress.snapTo(0f)
                continuousPlayerPhase = ContinuousPlayerTransitionPhase.Inline
            }
            // 进入全屏时方向仍暂时是竖屏；这两个阶段是等待横屏请求完成，
            // 不能把初始竖屏状态误派发成「回竖屏」事件，否则会被策略收起为 Collapsing。
            shouldKeepContinuousPlayerEnterPhaseWhilePortrait(
                phase = continuousPlayerPhase,
                isLandscape = isLandscape,
            ) -> Unit
            else -> applyContinuousPlayerDecision(
                reduceContinuousPlayerTransition(
                    phase = continuousPlayerPhase,
                    event = ContinuousPlayerTransitionEvent.OrientationChanged(isLandscape),
                )
            )
        }
    }

    LaunchedEffect(continuousFullscreenTransitionEnabled, continuousPlayerPhase) {
        if (!continuousFullscreenTransitionEnabled) return@LaunchedEffect
        when (continuousPlayerPhase) {
            ContinuousPlayerTransitionPhase.Expanding -> {
                val remaining = (1f - continuousPlayerProgress.value).coerceIn(0f, 1f)
                continuousPlayerProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = (CONTINUOUS_PLAYER_MORPH_DURATION_MILLIS * remaining)
                            .roundToInt()
                            .coerceAtLeast(1),
                        easing = FastOutSlowInEasing,
                    ),
                )
                applyContinuousPlayerDecision(
                    reduceContinuousPlayerTransition(
                        continuousPlayerPhase,
                        ContinuousPlayerTransitionEvent.ExpansionFinished,
                    )
                )
            }
            ContinuousPlayerTransitionPhase.Collapsing -> {
                val remaining = continuousPlayerProgress.value.coerceIn(0f, 1f)
                continuousPlayerProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = (CONTINUOUS_PLAYER_MORPH_DURATION_MILLIS * remaining)
                            .roundToInt()
                            .coerceAtLeast(1),
                        easing = FastOutSlowInEasing,
                    ),
                )
                applyContinuousPlayerDecision(
                    reduceContinuousPlayerTransition(
                        continuousPlayerPhase,
                        ContinuousPlayerTransitionEvent.CollapseFinished,
                    )
                )
            }
            else -> Unit
        }
    }
    val activeVideoSharedTransitionVisualSpec = remember(
        sourceRouteForSharedElement,
        sharedTransitionSourceCornerDp,
        videoSharedPlaybackIntent,
        startInFullscreen,
        autoEnterPortraitFromRoute,
        initialVerticalFromRoute,
        isVerticalVideo,
        useReturningVideoDetailVisualState
    ) {
        resolveVideoSharedTransitionVisualSpec(
            sourceRoute = sourceRouteForSharedElement,
            sourceCornerDp = sharedTransitionSourceCornerDp,
            playbackIntent = videoSharedPlaybackIntent,
            fullscreen = startInFullscreen,
            autoPortrait = autoEnterPortraitFromRoute,
            initialVertical = initialVerticalFromRoute,
            isVerticalVideo = isVerticalVideo,
            isReturning = useReturningVideoDetailVisualState
        )
    }
    LaunchedEffect(
        autoRotateEnabled,
        systemAutoRotateEnabled,
        fullscreenMode,
        useTabletLayout,
        isOrientationDrivenFullscreen,
        isFullscreenMode,
        windowSizeClass.isCompactDevice,
        isActivityInMultiWindowMode,
        userRequestedFullscreen,
        manualPortraitHoldActive,
        isVerticalVideo,
        isPortraitFullscreen,
        isFlatFoldable
    ) {
        val requestedOrientation = resolvePhoneVideoRequestedOrientation(
            autoRotateEnabled = autoRotateEnabled,
            systemAutoRotateEnabled = systemAutoRotateEnabled,
            fullscreenMode = fullscreenMode,
            isCompactDevice = windowSizeClass.isCompactDevice,
            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
            isFullscreenMode = isFullscreenMode,
            manualFullscreenRequested = userRequestedFullscreen,
            manualPortraitHoldActive = manualPortraitHoldActive,
            isVerticalVideo = isVerticalVideo,
            isPortraitFullscreen = isPortraitFullscreen,
            currentRequestedOrientation = activity?.requestedOrientation,
            isInMultiWindowMode = isActivityInMultiWindowMode,
            // 仅折叠屏完全展开的内屏沿用原版默认竖屏。不要以窗口宽度推断：它会随旋转
            // 改变，也无法区分普通平板和大屏手机。
            preferPortraitForFlatFoldable = isFlatFoldable
        ) ?: return@LaunchedEffect

        if (activity?.requestedOrientation != requestedOrientation) {
            activity?.requestedOrientation = requestedOrientation
        }
        com.android.purebilibili.core.util.Logger.d(
            "VideoDetailScreen",
            "🔄 Auto-rotate: enabled=$autoRotateEnabled, system=$systemAutoRotateEnabled, hold=$manualPortraitHoldActive, mode=$fullscreenMode, horizontal=$horizontalAdaptationEnabled, requested=$requestedOrientation, fullscreen=$isFullscreenMode, portraitFs=$isPortraitFullscreen, verticalVideo=$isVerticalVideo, isCompactDevice=${windowSizeClass.isCompactDevice}, multiWindow=$isActivityInMultiWindowMode"
        )
    }
    var lastPhoneAutoRotateLandscapeAppliedAtMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(
        autoRotateEnabled,
        systemAutoRotateEnabled,
        windowSizeClass.isCompactDevice,
        isOrientationDrivenFullscreen,
        fullscreenMode,
        manualPortraitHoldActive,
        isActivityInMultiWindowMode,
        isPortraitFullscreen
    ) {
        if (!shouldObservePhoneAutoRotate(
                autoRotateEnabled = autoRotateEnabled,
                systemAutoRotateEnabled = systemAutoRotateEnabled,
                isCompactDevice = windowSizeClass.isCompactDevice,
                isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                fullscreenMode = fullscreenMode,
                manualPortraitHoldActive = manualPortraitHoldActive,
                isInMultiWindowMode = isActivityInMultiWindowMode,
                isPortraitFullscreen = isPortraitFullscreen
            )
        ) {
            lastPhoneAutoRotateLandscapeAppliedAtMs = null
        }
    }

    DisposableEffect(
        activity,
        autoRotateEnabled,
        systemAutoRotateEnabled,
        fullscreenMode,
        useTabletLayout,
        isOrientationDrivenFullscreen,
        manualPortraitHoldActive,
        isActivityInMultiWindowMode,
        isPortraitFullscreen
    ) {
        val hostActivity = activity
        if (
            hostActivity == null ||
            !shouldObservePhoneAutoRotate(
                autoRotateEnabled = autoRotateEnabled,
                systemAutoRotateEnabled = systemAutoRotateEnabled,
                isCompactDevice = windowSizeClass.isCompactDevice,
                isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                fullscreenMode = fullscreenMode,
                manualPortraitHoldActive = manualPortraitHoldActive,
                isInMultiWindowMode = isActivityInMultiWindowMode,
                isPortraitFullscreen = isPortraitFullscreen
            ) ||
            !isOrientationDrivenFullscreen
        ) {
            return@DisposableEffect onDispose {}
        }

        val orientationListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (manualPortraitHoldActive) {
                    if (shouldReleasePhoneManualPortraitHold(orientation)) {
                        manualPortraitHoldActive = false
                    }
                    return
                }
                val isCurrentlyLandscape =
                    hostActivity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                val targetOrientation = resolvePhoneAutoRotateRequestedOrientation(
                    orientationDegrees = orientation,
                    isCurrentlyLandscape = isCurrentlyLandscape
                )
                val nowMs = SystemClock.elapsedRealtime()
                val targetToApply = resolvePhoneAutoRotateTargetToApply(
                    candidateOrientation = targetOrientation,
                    lastLandscapeAppliedAtMs = lastPhoneAutoRotateLandscapeAppliedAtMs,
                    nowMs = nowMs
                ) ?: return
                if (hostActivity.requestedOrientation != targetToApply) {
                    hostActivity.requestedOrientation = targetToApply
                }
                lastPhoneAutoRotateLandscapeAppliedAtMs =
                    if (isLandscapeRequestedOrientation(targetToApply)) nowMs else null
            }
        }

        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }

        onDispose {
            orientationListener.disable()
            lastPhoneAutoRotateLandscapeAppliedAtMs = null
        }
    }
    val portraitExperienceEnabled = shouldEnablePortraitExperience()
    val useOfficialInlinePortraitDetailExperience = shouldUseOfficialInlinePortraitDetailExperience(
        useTabletLayout = useTabletLayout,
        isVerticalVideo = isVerticalVideo,
        portraitExperienceEnabled = portraitExperienceEnabled,
        directPortraitEntry = directPortraitEntryFromRoute
    )
    val allowStandalonePortraitExperience = portraitExperienceEnabled &&
        !useOfficialInlinePortraitDetailExperience
    // Direct morph: hide phone intro/comment body under the full-bleed shell so only
    // card→fullscreen motion + entry cover / portrait pager are visible.
    val suppressPhoneDetailBodyForDirectPortrait = shouldSuppressPhoneDetailBodyForDirectPortraitEntry(
        directPortraitEntry = directPortraitEntryFromRoute,
        isPortraitFullscreen = isPortraitFullscreen
    )
    val isCurrentRouteVideoLoaded = remember(uiState, currentBvid) {
        val success = uiState as? VideoPlaybackUiState.Success
        success?.info?.bvid == currentBvid
    }
    val enterPortraitFullscreen = {
        if (shouldActivatePortraitFullscreenState(portraitExperienceEnabled)) {
            portraitSyncSnapshotBvid = (uiState as? VideoPlaybackUiState.Success)?.info?.bvid
            portraitSyncSnapshotCid = (uiState as? VideoPlaybackUiState.Success)?.info?.cid ?: 0L
            portraitSyncSnapshotPositionMs = playerState.player.currentPosition.coerceAtLeast(0L)
            hasPendingPortraitSync = false
            presentationState.setPortraitFullscreen(true)
        }
    }
    LaunchedEffect(
        autoEnterPortraitFromRoute,
        startAudioFromRoute,
        portraitExperienceEnabled,
        useOfficialInlinePortraitDetailExperience,
        windowSizeClass.widthSizeClass,
        isCurrentRouteVideoLoaded,
        isVerticalVideo,
        isPortraitFullscreen,
        hasAutoEnteredPortraitFromRoute,
        initialVerticalFromRoute,
        directPortraitEntryFromRoute,
    ) {
        if (
            shouldAutoEnterPortraitFullscreenFromRoute(
                autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
                startAudioFromRoute = startAudioFromRoute,
                portraitExperienceEnabled = portraitExperienceEnabled,
                useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
                allowStandalonePortraitAutoEnter = windowSizeClass.widthSizeClass ==
                    com.android.purebilibili.core.util.WindowWidthSizeClass.Compact,
                isCurrentRouteVideoLoaded = isCurrentRouteVideoLoaded,
                isVerticalVideo = isVerticalVideo,
                isPortraitFullscreen = isPortraitFullscreen,
                hasAutoEnteredPortraitFromRoute = hasAutoEnteredPortraitFromRoute,
                initialVerticalFromRoute = initialVerticalFromRoute,
                directPortraitEntryFromRoute = directPortraitEntryFromRoute,
            )
        ) {
            enterPortraitFullscreen()
            hasAutoEnteredPortraitFromRoute = true
        }
    }
    val shouldMirrorPortraitProgressToMainPlayer = com.android.purebilibili.feature.video.ui.pager
        .shouldMirrorPortraitProgressToMainPlayer(useSharedPlayer = useSharedPortraitPlayer)

    val tryApplyPortraitProgressSync = remember(playerState, viewModel) {
        { snapshotBvid: String?, snapshotPositionMs: Long ->
            val currentSuccess = viewModel.uiState.value as? VideoPlaybackUiState.Success
            val currentBvid = currentSuccess?.info?.bvid
            val currentCid = currentSuccess?.info?.cid ?: 0L
            if (!com.android.purebilibili.feature.video.ui.pager.shouldApplyPortraitProgressSync(
                    snapshotBvid = snapshotBvid,
                    snapshotCid = portraitSyncSnapshotCid,
                    currentBvid = currentBvid,
                    currentCid = currentCid
                )
            ) {
                false
            } else {
                playerState.player.seekTo(snapshotPositionMs.coerceAtLeast(0L))
                true
            }
        }
    }

    fun applyPortraitExitRestore() {
        val target = com.android.purebilibili.feature.video.ui.pager.resolvePortraitExitRestoreTarget(
            pendingMainReloadBvidAfterPortrait = pendingMainReloadBvidAfterPortrait,
            portraitPendingSelectionBvid = portraitPendingSelectionBvid,
            portraitSyncSnapshotBvid = portraitSyncSnapshotBvid,
            portraitSyncSnapshotCid = portraitSyncSnapshotCid,
            currentBvidCid = currentBvidCid
        ) ?: return
        presentationState.switchVideo(target.bvid, target.cid)
    }



    // 同步状态到 playerState (可选，用于日志或内部逻辑)
    LaunchedEffect(isPortraitFullscreen) {
        playerState.setPortraitFullscreen(isPortraitFullscreen)
        viewModel.setPortraitPlaybackSessionActive(isPortraitFullscreen)
        val shouldPauseMainPlayer = com.android.purebilibili.feature.video.ui.pager
            .shouldPauseMainPlayerOnPortraitEnter(useSharedPlayer = useSharedPortraitPlayer)
        if (isPortraitFullscreen) {
            if (shouldPauseMainPlayer) {
                playerState.player.pause()
                playerState.player.volume = 0f
                playerState.player.playWhenReady = false
            }
            portraitSyncSnapshotBvid = (uiState as? VideoPlaybackUiState.Success)?.info?.bvid
            portraitSyncSnapshotCid = (uiState as? VideoPlaybackUiState.Success)?.info?.cid ?: 0L
            portraitSyncSnapshotPositionMs = playerState.player.currentPosition.coerceAtLeast(0L)
            hasPendingPortraitSync = shouldPauseMainPlayer
        } else {
             if (shouldPauseMainPlayer) {
                 // 退出时恢复音量 (不自动播放，等待用户操作或 onResume)
                 com.android.purebilibili.core.player.PlayerVolumeController
                     .applyPreferredVolume(playerState.player)
             }
            if (!com.android.purebilibili.feature.video.ui.pager
                    .shouldApplyDeferredPortraitRestoreOnResume(
                        hasDeferredRestore = hasDeferredPortraitRestoreAfterExternalNavigation,
                        isPortraitFullscreen = isPortraitFullscreen
                    )
            ) {
                applyPortraitExitRestore()
                pendingMainReloadBvidAfterPortrait = null
                portraitPendingSelectionBvid = null
            }
        }
    }

    DisposableEffect(
        lifecycleOwner,
        isPortraitFullscreen,
        hasDeferredPortraitRestoreAfterExternalNavigation,
        pendingMainReloadBvidAfterPortrait,
        portraitPendingSelectionBvid,
        portraitSyncSnapshotBvid,
        portraitSyncSnapshotCid,
        currentBvidCid
    ) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event != androidx.lifecycle.Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (!com.android.purebilibili.feature.video.ui.pager
                    .shouldApplyDeferredPortraitRestoreOnResume(
                        hasDeferredRestore = hasDeferredPortraitRestoreAfterExternalNavigation,
                        isPortraitFullscreen = isPortraitFullscreen
                    )
            ) {
                return@LifecycleEventObserver
            }
            applyPortraitExitRestore()
            pendingMainReloadBvidAfterPortrait = null
            portraitPendingSelectionBvid = null
            hasDeferredPortraitRestoreAfterExternalNavigation = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.setPortraitPlaybackSessionActive(false)
        }
    }

    LaunchedEffect(
        uiState,
        hasPendingPortraitSync,
        portraitSyncSnapshotBvid,
        portraitSyncSnapshotPositionMs
    ) {
        if (hasPendingPortraitSync && tryApplyPortraitProgressSync(
                portraitSyncSnapshotBvid,
                portraitSyncSnapshotPositionMs
            )
        ) {
            hasPendingPortraitSync = false
        }
    }

    LaunchedEffect(uiState, currentBvid, currentBvidCid, isPortraitFullscreen, bvid, isVisible) {
        if (!isVisible) return@LaunchedEffect
        val success = uiState as? VideoPlaybackUiState.Success ?: return@LaunchedEffect
        if (!shouldSyncMainPlayerToInternalBvid(
                isPortraitFullscreen = isPortraitFullscreen,
                routeBvid = bvid,
                currentBvid = currentBvid,
                currentBvidCid = currentBvidCid,
                loadedBvid = success.info.bvid,
                loadedCid = success.info.cid
            )
        ) {
            return@LaunchedEffect
        }
        viewModel.loadVideo(
            bvid = currentBvid,
            cid = currentBvidCid.takeIf { it > 0L } ?: 0L,
            autoPlay = resolveAutoPlayOverrideForInternalBvidSync(forceAutoPlay = false)
        )
    }

    // 📲 小窗模式（手机/平板统一逻辑）
    val handlePipClick = {
        // 使用 MiniPlayerManager 进入应用内小窗模式
        miniPlayerManager?.let { manager ->
            val stopPlaybackOnExit = com.android.purebilibili.core.store.SettingsManager
                .getStopPlaybackOnExitSync(context)
            if (stopPlaybackOnExit) {
                com.android.purebilibili.core.util.Logger.d(
                    "VideoDetailScreen",
                    "Stop-on-exit enabled, skip mini mode and leave page directly"
                )
                manager.markLeavingByNavigation(expectedBvid = currentBvid)
                onBack()
                return@let
            }

            //  [埋点] PiP 进入事件
            com.android.purebilibili.core.util.AnalyticsHelper.logPictureInPicture(
                videoId = currentBvid,
                action = "enter_mini"
            )

            // 1. 将当前播放器信息传递给小窗管理器
            val info = uiState as? VideoPlaybackUiState.Success
            manager.setVideoInfo(
                bvid = currentBvid,
                title = info?.info?.title ?: "",
                cover = info?.info?.pic ?: "",
                owner = info?.info?.owner?.name ?: "",
                cid = info?.info?.cid ?: 0L,
                aid = info?.info?.aid ?: 0L,
                externalPlayer = playerState.player
            )

            // 2. 进入小窗模式（强制，不管当前模式设置）
            manager.enterMiniMode(forced = true)

            // 3. 返回上一页（首页）
            presentationState.markNavigatingToMiniMode()
            onBack()
        } ?: run {
            // 如果 miniPlayerManager 不存在，直接返回
            com.android.purebilibili.core.util.Logger.w("VideoDetailScreen", "⚠️ miniPlayerManager 为 null，无法进入小窗")
            onBack()
        }
    }

    // 🔧 [性能优化] 记录上次缓存的 bvid，避免重复缓存 MiniPlayer 信息
    var lastCachedMiniPlayerBvid by remember { mutableStateOf<String?>(null) }

    //  核心修改：初始化评论 & 媒体中心信息
    LaunchedEffect(uiState, isVisible) {
        if (uiState is VideoPlaybackUiState.Success) {
            val info = (uiState as VideoPlaybackUiState.Success).info
            val success = uiState as VideoPlaybackUiState.Success

            // 初始化评论（传入 UP 主 mid 用于筛选）- 保持在主线程
            commentViewModel.init(
                aid = info.aid,
                upMid = info.owner.mid,
                preferredSortMode = preferredCommentSortMode,
                expectedReplyCount = info.stat.reply
            )

            playerState.updateMediaMetadata(
                title = info.title,
                artist = info.owner.name,
                coverUrl = info.pic
            )

            // 📱 [双重验证] 从 API dimension 字段设置预判断值
            info.dimension?.let { dim ->
                playerState.setApiDimension(dim.width, dim.height, dim.rotate)
            }

            //  同步视频信息到小窗管理器（为小窗模式做准备）
            //  🚀 [性能优化] 将繁重的序列化和缓存操作移至后台线程，防止主线程卡顿
            // 🔧 [性能优化] 只有首次加载或视频切换时才缓存 MiniPlayer 信息
            val shouldCacheMiniPlayer = lastCachedMiniPlayerBvid != currentBvid

            if (miniPlayerManager != null && shouldCacheMiniPlayer && isVisible) {
                lastCachedMiniPlayerBvid = currentBvid

                // PiP 判断依赖 MiniPlayerManager 的 active/player/playing/bvid 状态。
                // 这一步必须先于后台缓存完成，避免竖屏全屏快速进入 PiP 时拿到旧状态而暂停。
                miniPlayerManager.setVideoInfo(
                    bvid = currentBvid,
                    title = info.title,
                    cover = info.pic,
                    owner = info.owner.name,
                    cid = info.cid,  //  传递 cid 用于弹幕加载
                    aid = info.aid,
                    externalPlayer = playerState.player,
                    fromLeft = com.android.purebilibili.core.util.CardPositionManager.isCardOnLeft  //  传递入场方向
                )

                launch(Dispatchers.Default) {
                    com.android.purebilibili.core.util.Logger.d("VideoDetailScreen", "🔄 [Background] Caching MiniPlayer UI state...")

                    // 序列化缓存 (Heavy Operation)
                    miniPlayerManager.cacheUiState(success)
                    com.android.purebilibili.core.util.Logger.d("VideoDetailScreen", "✅ [Background] MiniPlayer info cached")
                }
            } else if (miniPlayerManager == null) {
                android.util.Log.w("VideoDetailScreen", " miniPlayerManager 是 null!")
            }
        } else if (uiState is VideoPlaybackUiState.Loading) {
            playerState.updateMediaMetadata(
                title = "加载中...",
                artist = "",
                coverUrl = coverUrl
            )
        }
    }

    //  弹幕加载逻辑已移至 VideoPlayerState 内部处理
    // 避免在此处重复消耗 InputStream

    // 辅助函数：切换全屏状态
    val toggleFullscreen = {
        val activity = context.findActivity()
        val currentPositionMs = playerState.player.currentPosition.coerceAtLeast(0L)
        val shouldPreserveCurrentFrame = activity != null &&
            (!isVerticalVideo || isFullscreenMode) &&
            playerState.player.playWhenReady &&
            currentPositionMs > 0L
        preserveCurrentFrameOnFullscreenChange = shouldPreserveCurrentFrame
        if (shouldPreserveCurrentFrame) {
            pendingFullscreenPositionRestoreMs = currentPositionMs
        } else {
            pendingFullscreenPositionRestoreMs = -1L
        }
        if (continuousFullscreenTransitionEnabled) {
            applyContinuousPlayerDecision(
                reduceContinuousPlayerTransition(
                    phase = continuousPlayerPhase,
                    event = ContinuousPlayerTransitionEvent.Toggle,
                )
            )
        } else toggleVideoDetailFullscreen(
            activity = activity,
            isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
            isLandscape = isLandscape,
            isFullscreenMode = isFullscreenMode,
            isCompactDevice = windowSizeClass.isCompactDevice,
            fullscreenMode = fullscreenMode,
            isVerticalVideo = isVerticalVideo,
            portraitExperienceEnabled = portraitExperienceEnabled,
            onEnterPortraitFullscreen = { enterPortraitFullscreen() },
            onUserRequestedFullscreenChange = { requested -> userRequestedFullscreen = requested },
            onManualPortraitHoldActiveChange = { active -> manualPortraitHoldActive = active }
        )
    }

    val localBackTarget = resolveVideoDetailLocalBackTarget(
        isLandscapeFullscreen = isFullscreenMode,
        isPortraitFullscreen = isPortraitFullscreen,
    )
    val localBackEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = localBackEventState,
        isBackEnabled = localBackTarget != VideoDetailLocalBackTarget.NAVIGATE_BACK,
        onBackCompleted = {
            when (localBackTarget) {
                VideoDetailLocalBackTarget.EXIT_PORTRAIT_FULLSCREEN -> presentationState.setPortraitFullscreen(false)
                VideoDetailLocalBackTarget.EXIT_LANDSCAPE_FULLSCREEN -> toggleFullscreen()
                VideoDetailLocalBackTarget.NAVIGATE_BACK -> Unit
            }
        },
    )

    // 以下 BackHandler 会阻止 Compose Navigation 的返回路由动画，由根导航统一处理。
    // 显式点击返回时由 handleBack 提前标记 returning，系统路径仍由 onDispose 兜底标记。
    // BackHandler(enabled = !isFullscreenMode && !isPortraitFullscreen, onBack = handleBack)


    // 清理逻辑（markLeavingByNavigation、restoreStatusBar）已移至 DisposableEffect.onDispose

    // 沉浸式状态栏控制
    val backgroundColor = AppSurfaceTokens.background()
    val isLightBackground = remember(backgroundColor) { backgroundColor.luminance() > 0.5f }
    val systemBarsVisibilityPolicy = remember(
        isFullscreenMode,
        isPortraitFullscreen,
        immersiveVideoPageStatusBar,
        isPipMode,
        isScreenActive
    ) {
        resolveVideoDetailSystemBarsVisibilityPolicy(
            isFullscreenMode = isFullscreenMode,
            hideVideoPageStatusBar = immersiveVideoPageStatusBar,
            isInPipMode = isPipMode,
            isScreenActive = isScreenActive,
            isPortraitFullscreen = isPortraitFullscreen
        )
    }
    val systemBarsApplySpec = remember(
        systemBarsVisibilityPolicy,
        useTabletLayout,
        isLightBackground,
        backgroundColor
    ) {
        resolveVideoDetailSystemBarsApplySpec(
            visibilityPolicy = systemBarsVisibilityPolicy,
            useTabletLayout = useTabletLayout,
            isLightBackground = isLightBackground,
            backgroundColor = backgroundColor.toArgb(),
            transparentColor = Color.Transparent.toArgb(),
            blackColor = Color.Black.toArgb(),
            transientBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        )
    }

    VideoDetailSystemBarsEffect(
        view = view,
        window = window,
        insetsController = insetsController,
        isScreenActive = isScreenActive,
        spec = systemBarsApplySpec,
        reapplyGeneration = systemBarsReapplyGeneration,
    )

    val uiSuccessState = uiState as? VideoPlaybackUiState.Success
    LaunchedEffect(uiSuccessState?.info?.bvid, currentBvid) {
        // Loaded target matched; drop transitional cover.
        if (
            pendingInPageSwitchCoverUrl.isNotBlank() &&
            uiSuccessState?.info?.bvid == currentBvid
        ) {
            pendingInPageSwitchCoverUrl = ""
        }
    }
    val videoPlayerSectionTarget = remember(
        bvid,
        coverUrl,
        currentBvid,
        currentBvidCid,
        pendingInPageSwitchCoverUrl,
        uiSuccessState?.info?.ugc_season,
        uiSuccessState?.info?.pic,
        uiSuccessState?.info?.bvid
    ) {
        val successCover = uiSuccessState
            ?.takeIf { it.info.bvid == currentBvid }
            ?.info
            ?.pic
            .orEmpty()
        val seasonCover = resolveUgcSeasonEpisodeCoverUrl(
            ugcSeason = uiSuccessState?.info?.ugc_season,
            targetBvid = currentBvid,
            targetCid = currentBvidCid
        )
        resolveVideoPlayerSectionTarget(
            routeBvid = bvid,
            routeCoverUrl = coverUrl,
            currentBvid = currentBvid,
            switchedCoverUrl = listOf(
                pendingInPageSwitchCoverUrl,
                seasonCover,
                successCover
            ).firstOrNull { it.isNotBlank() }.orEmpty()
        )
    }
    val shouldSuppressSubtitleOverlay = suppressOverlayControlsForReturn || (
        useSharedPortraitPlayer &&
        !isPortraitFullscreen &&
        pendingMainReloadBvidAfterPortrait != null &&
        (
            pendingMainReloadBvidAfterPortrait != uiSuccessState?.info?.bvid ||
                (portraitSyncSnapshotCid > 0L && portraitSyncSnapshotCid != (uiSuccessState?.info?.cid ?: 0L))
            )
        )
    val showDanmakuDialog by viewModel.showDanmakuDialog.collectAsStateWithLifecycle()
    val isSendingDanmaku by viewModel.isSendingDanmaku.collectAsStateWithLifecycle()
    val composerDrafts by viewModel.composerDrafts.collectAsStateWithLifecycle()
    val danmakuSendPreferenceScope = rememberCoroutineScope()
    val rememberedDanmakuSendColor by com.android.purebilibili.core.store.SettingsManager
        .getDanmakuSendColor(context)
        .collectAsStateWithLifecycle(initialValue = 16777215)
    val rememberedDanmakuSendMode by com.android.purebilibili.core.store.SettingsManager
        .getDanmakuSendMode(context)
        .collectAsStateWithLifecycle(initialValue = 1)
    val rememberedDanmakuSendFontSize by com.android.purebilibili.core.store.SettingsManager
        .getDanmakuSendFontSize(context)
        .collectAsStateWithLifecycle(initialValue = 25)
    val continuousPlayerUnitState = remember { mutableFloatStateOf(1f) }
    val continuousPlayerRenderer = rememberUpdatedState<@Composable (ContinuousPlayerHostLayout) -> Unit> { layout ->
        PortraitInlineVideoPlayerHost(
            modifier = layout.modifier,
            animatedViewportWidth = layout.viewportWidth,
            contentTopInset = layout.contentTopInset,
            inlinePlayerAlpha = layout.alpha,
            inlinePlayerScale = layout.scale,
            isFullscreen = layout.isFullscreen,
            playerState = playerState,
            uiState = uiState,
            isPipMode = isPipMode,
            transitionEnabled = detailChildTransitionEnabled,
            transitionChromeAlphaProvider = videoCardDetailChromeAlphaProvider,
            danmakuHostActive = !hasCommittedRelatedVideoNavigation,
            onToggleFullscreen = { toggleFullscreen() },
            playbackActions = playbackActions,
            onDoubleTapLike = engagementViewModel::toggleLike,
            onBack = if (layout.isFullscreen) ({ toggleFullscreen() }) else handleBack,
            onHomeClick = {
                handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
            },
            videoPlayerSectionTarget = videoPlayerSectionTarget,
            sponsorSegment = sponsorSegment,
            showSponsorSkipButton = showSponsorSkipButton,
            sponsorContributionState = sponsorContributionState,
            sleepTimerMinutes = sleepTimerMinutes,
            viewPoints = viewPoints,
            pbpProgressData = pbpProgressData,
            sponsorProgressMarkers = sponsorProgressMarkers,
            isVerticalVideo = isVerticalVideo &&
                (allowStandalonePortraitExperience || useOfficialInlinePortraitDetailExperience),
            onPortraitFullscreen = { enterPortraitFullscreen() },
            isPortraitFullscreen = isPortraitFullscreen,
            onPipClick = handlePipClick,
            codecPreference = codecPreference,
            secondCodecPreference = secondCodecPreference,
            audioQualityPreference = audioQualityPreference,
            onNavigateToAudioMode = {
                viewModel.setAudioMode(true)
                presentationState.markNavigatingToAudioMode()
                onNavigateToAudioMode()
            },
            forceCoverOnly = forceCoverOnlyForLiveSafeReturn ||
                shouldForceBackPreviewPlayerCover(
                    keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
                    bindLivePlayerForBackPreview = bindLivePlayerForBackPreview,
                ),
            preserveCurrentFrameOnFullscreenChange = preserveCurrentFrameOnFullscreenChange,
            liveBackPreview = bindLivePlayerForBackPreview,
            useTextureSurfaceForNavigation = useTextureSurfaceForNavigation,
            predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
            allowLivePlayerSharedElement = allowLivePlayerSharedElement,
            sourceRouteForSharedElement = sourceRouteForSharedElement,
            preserveSourceCardCornerDuringSharedReturn =
                detailShellSharedBoundsEnabled && useReturningVideoDetailVisualState,
            suppressSubtitleOverlay = shouldSuppressSubtitleOverlay,
            subtitleDisplayModePreferenceOverride = subtitleDisplayModeOverride,
            onSubtitleDisplayModePreferenceOverrideChange = { subtitleDisplayModeOverride = it },
            fullscreenExtras = ContinuousPlayerFullscreenExtras(
                danmakuComposerVisible = showDanmakuDialog,
                onDismissDanmakuComposer = viewModel::hideDanmakuSendDialog,
                onSendDanmakuComposer = viewModel::sendDanmaku,
                isSendingDanmakuComposer = isSendingDanmaku,
                danmakuComposerInitialText = composerDrafts.danmaku.text,
                danmakuComposerInitialAttentionCommand =
                    composerDrafts.danmaku.attentionCommand,
                danmakuComposerInitialColor = rememberedDanmakuSendColor,
                danmakuComposerInitialMode = rememberedDanmakuSendMode,
                danmakuComposerInitialFontSize = rememberedDanmakuSendFontSize,
                onDanmakuComposerDraftChange = viewModel::updateDanmakuDraft,
                onDanmakuComposerSelectionChange = { color, mode, fontSize ->
                    danmakuSendPreferenceScope.launch {
                        com.android.purebilibili.core.store.SettingsManager
                            .setDanmakuSendColor(context, color)
                        com.android.purebilibili.core.store.SettingsManager
                            .setDanmakuSendMode(context, mode)
                        com.android.purebilibili.core.store.SettingsManager
                            .setDanmakuSendFontSize(context, fontSize)
                    }
                },
                currentPlayMode = currentPlayMode,
                onPlayModeClick = { PlaylistManager.togglePlayMode() },
                onSaveCover = { viewModel.saveCover(context) },
                onDownloadAudio = { viewModel.downloadAudio(context) },
                relatedVideos = uiSuccessState?.related.orEmpty(),
                ugcSeason = uiSuccessState?.info?.ugc_season,
                isFollowed = engagementState.isFollowing,
                isLiked = engagementState.isLiked,
                isCoined = engagementState.coinCount > 0,
                isFavorited = engagementState.isFavorited,
                onToggleFollow = engagementViewModel::toggleFollow,
                onToggleLike = engagementViewModel::toggleLike,
                onDislike = viewModel::markVideoNotInterested,
                onCoin = engagementViewModel::openCoinDialog,
                onToggleFavorite = {
                    openFavoriteFolders(VideoFavoriteEntryPoint.FullscreenOverlay)
                },
                onTriple = engagementViewModel::doTripleAction,
                onRelatedVideoClick = navigateToRelatedVideo,
                onPageSelect = viewModel::switchPage,
                hasFavoritePlaylist = isExternalPlaylist &&
                    externalPlaylistSource == ExternalPlaylistSource.FAVORITE &&
                    playlistItems.size > 1,
                onFavoritePlaylistClick = { showExternalPlaylistQueueSheet = true },
                onLandscapeCommentClick = {
                    landscapeCommentPanelVisible = !landscapeCommentPanelVisible
                },
                landscapeCommentPanelVisible = landscapeCommentPanelVisible,
                landscapeCommentPanelOnLeft = landscapeCommentPanelOnLeft,
            ),
        )
    }
    val continuousPlayerContent = remember {
        movableContentOf<ContinuousPlayerHostLayout> { layout ->
            continuousPlayerRenderer.value(layout)
        }
    }
    // Android 16 ART 曾拒绝校验由 VideoDetailRouteSheetHost 尾随 lambda 生成的超大合成方法
    // （VerifyError: VideoDetailScreen$lambda$N(...BoxScope, Composer, int) 参数过多）。
    // 主布局与覆盖层必须使用两个内容槽，单个局部函数仍会捕获全部状态并生成百参数方法。
    // 分槽后 Compose 编译器会分别生成合成方法，避免一个方法聚合整页状态。
    @Composable
    fun BoxScope.VideoDetailRouteSheetMainContent() {
            // 📐 [平板适配] 全屏模式过渡动画（只有手机横屏才进入全屏）
        if (isFullscreenMode) {
                val useInlineDanmakuComposer =
                    com.android.purebilibili.feature.video.ui.components.shouldUseInlineDanmakuComposer(
                        isFullscreenMode = isFullscreenMode
                    )
                if (continuousFullscreenTransitionEnabled) {
                    continuousPlayerContent(
                        ContinuousPlayerHostLayout(
                            modifier = Modifier.fillMaxSize(),
                            viewportWidth = configuration.screenWidthDp.dp,
                            alpha = continuousPlayerUnitState,
                            scale = continuousPlayerUnitState,
                            isFullscreen = true,
                        )
                    )
                    val success = uiState as? VideoPlaybackUiState.Success
                    if (landscapeCommentPanelVisible && success != null) {
                        LandscapeCommentPanel(
                            info = success.info, listState = commentListState,
                            replies = commentState.replies, replyCount = commentState.replyCount,
                            emoteMap = success.emoteMap, isRepliesLoading = commentState.isRepliesLoading,
                            isRepliesEnd = commentState.isRepliesEnd, videoTags = success.videoTags,
                            sortMode = commentState.sortMode,
                            currentMid = commentState.currentMid, showUpFlag = commentState.showUpFlag,
                            showIdentityDecorations = commentMemberDecorationsEnabled,
                            dissolvingIds = commentState.dissolvingIds, likedComments = commentState.likedComments,
                            onSortModeChange = commentActions.setSortMode,
                            onUpClick = navigateToUserSpaceFromVideo,
                            onSubReplyClick = commentActions.openSubReply,
                            onCommentReplyClick = playbackActions.replyTo, onLoadMoreReplies = commentActions.loadComments,
                            onDeleteComment = commentActions.deleteComment, onDissolveStart = commentActions.startDissolve,
                            onCommentLike = commentActions.likeComment, onCommentUrlClick = openCommentUrl,
                            onReportComment = commentActions.reportComment, onToggleTopComment = commentActions.toggleTopComment,
                            onTimestampClick = { position -> seekPlayerFromUserAction(playerState.player, position) },
                            onDismiss = {
                                commentActions.closeSubReply()
                                landscapeCommentPanelVisible = false
                            },
                            onSwitchSide = { landscapeCommentPanelOnLeft = !landscapeCommentPanelOnLeft },
                            isOnLeft = landscapeCommentPanelOnLeft,
                            drawerWidth = com.android.purebilibili.feature.video.ui.overlay
                                .resolveLandscapeEndDrawerLayoutPolicy(configuration.screenWidthDp)
                                .drawerWidthDp.dp,
                            threadContent = if (subReplyState.visible && subReplyState.rootReply != null) {
                                { onImagePreview ->
                                    VideoInlineSubReplyDetailContent(
                                        state = subReplyState,
                                        commentState = commentState,
                                        emoteMap = success.emoteMap,
                                        maxTimestampMs = success.videoDurationMs.takeIf { it > 0L },
                                        onLoadMore = commentActions.loadMoreSubReplies,
                                        onDismiss = commentActions.closeSubReply,
                                        onRootCommentClick = playbackActions.openRootCommentComposer,
                                        onTimestampClick = { positionMs ->
                                            seekPlayerFromUserAction(playerState.player, positionMs)
                                            commentActions.closeSubReply()
                                        },
                                        onImagePreview = onImagePreview,
                                        onReplyClick = playbackActions.replyTo,
                                        onConversationClick = commentActions.openSubReplyConversation,
                                        onConversationBack = commentActions.closeSubReplyConversation,
                                        onDissolveStart = commentActions.startSubDissolve,
                                        onDeleteComment = commentActions.deleteSubComment,
                                        onCommentLike = commentActions.likeComment,
                                        onReportComment = commentActions.reportComment,
                                        onUrlClick = openCommentUrl,
                                        showIdentityDecorations = commentMemberDecorationsEnabled,
                                        onAvatarClick = { mid ->
                                            mid.toLongOrNull()?.let(navigateToUserSpaceFromVideo) ?: Unit
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .align(if (landscapeCommentPanelOnLeft) Alignment.CenterStart else Alignment.CenterEnd)
                                .fillMaxHeight(),
                        )
                    }
                } else {
                    VideoPlayerSection(
                    playerState = playerState,
                    uiState = uiState,
                    isFullscreen = true,
                    isInPipMode = isPipMode,
                    danmakuHostActive = !hasCommittedRelatedVideoNavigation,
                    transitionEnabled = detailChildTransitionEnabled,
                    onToggleFullscreen = { toggleFullscreen() },
                    onQualityChange = { qid -> viewModel.changeQuality(qid) },
                    onBack = { toggleFullscreen() },
                    onHomeClick = {
                        handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                    },
                    onLandscapeCommentClick = {
                        landscapeCommentPanelVisible = !landscapeCommentPanelVisible
                    },
                    landscapeCommentPanelVisible = landscapeCommentPanelVisible,
                    landscapeCommentPanelOnLeft = landscapeCommentPanelOnLeft,
                    onDanmakuInputClick = { viewModel.showDanmakuSendDialog() },
                    danmakuComposerVisible = showDanmakuDialog && useInlineDanmakuComposer,
                    onDismissDanmakuComposer = { viewModel.hideDanmakuSendDialog() },
                    onSendDanmakuComposer = { message, color, mode, fontSize, encourage ->
                        viewModel.sendDanmaku(message, color, mode, fontSize, encourage)
                    },
                    isSendingDanmakuComposer = isSendingDanmaku,
                    danmakuComposerInitialText = composerDrafts.danmaku.text,
                    danmakuComposerInitialAttentionCommand = composerDrafts.danmaku.attentionCommand,
                    danmakuComposerInitialColor = rememberedDanmakuSendColor,
                    danmakuComposerInitialMode = rememberedDanmakuSendMode,
                    danmakuComposerInitialFontSize = rememberedDanmakuSendFontSize,
                    onDanmakuComposerDraftChange = viewModel::updateDanmakuDraft,
                    onDanmakuComposerSelectionChange = { color, mode, fontSize ->
                        danmakuSendPreferenceScope.launch {
                            com.android.purebilibili.core.store.SettingsManager.setDanmakuSendColor(context, color)
                            com.android.purebilibili.core.store.SettingsManager.setDanmakuSendMode(context, mode)
                            com.android.purebilibili.core.store.SettingsManager.setDanmakuSendFontSize(context, fontSize)
                        }
                    },
                    // 🔗 [新增] 分享功能
                    bvid = videoPlayerSectionTarget.bvid,
                    coverUrl = videoPlayerSectionTarget.entryCoverUrl,
                    sharedElementBvid = videoPlayerSectionTarget.sharedElementBvid,
                    //  实验性功能：双击点赞
                    onDoubleTapLike = { engagementViewModel.toggleLike() },
                    sponsorSegment = sponsorSegment,
                    showSponsorSkipButton = showSponsorSkipButton,
                    onSponsorSkip = { viewModel.skipCurrentSponsorSegment() },
                    onSponsorDismiss = { viewModel.dismissSponsorSkipButton() },
                    onSponsorVote = viewModel::voteCurrentSponsorSegment,
                    sponsorContributionState = sponsorContributionState,
                    onSponsorContributionMarkBoundary = viewModel::markSponsorContributionBoundary,
                    onSponsorContributionCategoryChange = viewModel::setSponsorContributionCategory,
                    onSponsorContributionActionTypeChange = viewModel::setSponsorContributionActionType,
                    onSponsorContributionSubmit = viewModel::submitSponsorContribution,
                    onSponsorContributionCancel = viewModel::cancelSponsorContribution,
                    //  [新增] 重载视频
                    onReloadVideo = { viewModel.reloadVideo() },
                    //  [新增] CDN 线路切换
                    cdnCount = (uiState as? VideoPlaybackUiState.Success)?.cdnCount ?: 1,
                    cdnLineDiagnostics = (uiState as? VideoPlaybackUiState.Success)?.cdnLineDiagnostics.orEmpty(),
                    isCdnProbing = (uiState as? VideoPlaybackUiState.Success)?.isCdnProbing ?: false,
                    onSwitchCdn = { viewModel.switchCdn() },
                    onSwitchCdnTo = { viewModel.switchCdnTo(it) },
                    onProbeCdnCandidates = { viewModel.probeCurrentCdnCandidates() },

                    // [New] Codec & Audio (Fullscreen)
                    currentCodec = codecPreference,
                    onCodecChange = { viewModel.setVideoCodec(it) },
                    currentSecondCodec = secondCodecPreference,
                    onSecondCodecChange = { viewModel.setVideoSecondCodec(it) },
                    currentAudioQuality = audioQualityPreference,
                    onAudioQualityChange = { viewModel.setAudioQuality(it) },
                    onPlaybackSpeedChange = { viewModel.applyPlaybackSpeedFromUi(it) },
                    // [New] Audio Language
                    onAudioLangChange = { viewModel.changeAudioLanguage(it) },

                    //  [新增] 音频模式
                    isAudioOnly = false, // 全屏模式只有视频
                    onAudioOnlyToggle = {
                        viewModel.setAudioMode(true)
                        presentationState.markNavigatingToAudioMode()
                        onNavigateToAudioMode()
                    },

                    //  [新增] 定时关闭
                    sleepTimerMinutes = sleepTimerMinutes,
                    onSleepTimerChange = { viewModel.setSleepTimer(it) },

                    // 🖼️ [新增] 视频预览图数据
                        videoshotData = (uiState as? VideoPlaybackUiState.Success)?.videoshotData,

                    // 📖 [新增] 视频章节数据
                        viewPoints = viewPoints,
                        pbpProgressData = pbpProgressData,
                        sponsorMarkers = sponsorProgressMarkers,
                        onUserSeek = { position -> viewModel.notifyPluginsOfExplicitSeek(position) },
                    // 📱 [新增] 竖屏全屏模式
                    isVerticalVideo = isVerticalVideo && allowStandalonePortraitExperience,
                    isPortraitFullscreen = isPortraitFullscreen,
                    onPortraitFullscreen = {
                        if (allowStandalonePortraitExperience) {
                            if (!isPortraitFullscreen) {
                                if (isFullscreenMode) {
                                    toggleFullscreen()
                                }
                                enterPortraitFullscreen()
                            } else {
                                presentationState.setPortraitFullscreen(false)
                            }
                        }
                    },
                    // 🔁 [新增] 播放模式
                    currentPlayMode = currentPlayMode,
                    onPlayModeClick = { com.android.purebilibili.feature.video.player.PlaylistManager.togglePlayMode() },

                    // [New Actions]
                    onSaveCover = { viewModel.saveCover(context) },
                    onDownloadAudio = { viewModel.downloadAudio(context) },

                    // [新增] 侧边栏抽屉数据与交互
                    relatedVideos = (uiState as? VideoPlaybackUiState.Success)?.related ?: emptyList(),
                    ugcSeason = (uiState as? VideoPlaybackUiState.Success)?.info?.ugc_season,
                    isFollowed = engagementState.isFollowing,
                    isLiked = engagementState.isLiked,
                    isCoined = engagementState.coinCount > 0,
                    isFavorited = engagementState.isFavorited,
                    onToggleFollow = { engagementViewModel.toggleFollow() },
                    onToggleLike = { engagementViewModel.toggleLike() },
                    onDislike = { viewModel.markVideoNotInterested() },
                    onCoin = { engagementViewModel.openCoinDialog() },
                    onToggleFavorite = {
                        openFavoriteFolders(VideoFavoriteEntryPoint.FullscreenOverlay)
                    },
                    onTriple = { engagementViewModel.doTripleAction() },
                    onRelatedVideoClick = navigateToRelatedVideo,
                    onPageSelect = { viewModel.switchPage(it) },
                    hasFavoritePlaylist = isExternalPlaylist &&
                        externalPlaylistSource == ExternalPlaylistSource.FAVORITE &&
                        playlistItems.size > 1,
                    onFavoritePlaylistClick = {
                        showExternalPlaylistQueueSheet = true
                    },
                    forceCoverOnly = forceCoverOnlyForLiveSafeReturn,
                    preserveCurrentFrameOnFullscreenChange = preserveCurrentFrameOnFullscreenChange,
                    useTextureSurfaceForNavigation = useTextureSurfaceForNavigation,
                    predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
                    allowLivePlayerSharedElement = allowLivePlayerSharedElement,
                    sourceRouteForSharedElement = sourceRouteForSharedElement,
                    suppressSubtitleOverlay = shouldSuppressSubtitleOverlay,
                    subtitleDisplayModePreferenceOverride = subtitleDisplayModeOverride,
                    onSubtitleDisplayModePreferenceOverrideChange = { subtitleDisplayModeOverride = it },
                        onSubtitleTrackSelected = viewModel::selectSubtitleTrack
                    )
                    val success = uiState as? VideoPlaybackUiState.Success
                    if (landscapeCommentPanelVisible && success != null) {
                        LandscapeCommentPanel(
                            info = success.info,
                            listState = commentListState,
                            replies = commentState.replies,
                            replyCount = commentState.replyCount,
                            emoteMap = success.emoteMap,
                            isRepliesLoading = commentState.isRepliesLoading,
                            isRepliesEnd = commentState.isRepliesEnd,
                            videoTags = success.videoTags,
                            sortMode = commentState.sortMode,
                            currentMid = commentState.currentMid,
                            showUpFlag = commentState.showUpFlag,
                            showIdentityDecorations = commentMemberDecorationsEnabled,
                            dissolvingIds = commentState.dissolvingIds,
                            likedComments = commentState.likedComments,
                            onSortModeChange = commentActions.setSortMode,
                            onUpClick = navigateToUserSpaceFromVideo,
                            onSubReplyClick = commentActions.openSubReply,
                            onCommentReplyClick = playbackActions.replyTo,
                            onLoadMoreReplies = commentActions.loadComments,
                            onDeleteComment = commentActions.deleteComment,
                            onDissolveStart = commentActions.startDissolve,
                            onCommentLike = commentActions.likeComment,
                            onCommentUrlClick = openCommentUrl,
                            onReportComment = commentActions.reportComment,
                            onToggleTopComment = commentActions.toggleTopComment,
                            onTimestampClick = { position -> seekPlayerFromUserAction(playerState.player, position) },
                            onDismiss = {
                                commentActions.closeSubReply()
                                landscapeCommentPanelVisible = false
                            },
                            onSwitchSide = { landscapeCommentPanelOnLeft = !landscapeCommentPanelOnLeft },
                            isOnLeft = landscapeCommentPanelOnLeft,
                            drawerWidth = com.android.purebilibili.feature.video.ui.overlay
                                .resolveLandscapeEndDrawerLayoutPolicy(configuration.screenWidthDp)
                                .drawerWidthDp.dp,
                            threadContent = if (subReplyState.visible && subReplyState.rootReply != null) {
                                { onImagePreview ->
                                    VideoInlineSubReplyDetailContent(
                                        state = subReplyState,
                                        commentState = commentState,
                                        emoteMap = success.emoteMap,
                                        maxTimestampMs = success.videoDurationMs.takeIf { it > 0L },
                                        onLoadMore = commentActions.loadMoreSubReplies,
                                        onDismiss = commentActions.closeSubReply,
                                        onRootCommentClick = playbackActions.openRootCommentComposer,
                                        onTimestampClick = { positionMs ->
                                            seekPlayerFromUserAction(playerState.player, positionMs)
                                            commentActions.closeSubReply()
                                        },
                                        onImagePreview = onImagePreview,
                                        onReplyClick = playbackActions.replyTo,
                                        onConversationClick = commentActions.openSubReplyConversation,
                                        onConversationBack = commentActions.closeSubReplyConversation,
                                        onDissolveStart = commentActions.startSubDissolve,
                                        onDeleteComment = commentActions.deleteSubComment,
                                        onCommentLike = commentActions.likeComment,
                                        onReportComment = commentActions.reportComment,
                                        onUrlClick = openCommentUrl,
                                        showIdentityDecorations = commentMemberDecorationsEnabled,
                                        onAvatarClick = { mid ->
                                            mid.toLongOrNull()?.let(navigateToUserSpaceFromVideo) ?: Unit
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .align(
                                    if (landscapeCommentPanelOnLeft) Alignment.CenterStart else Alignment.CenterEnd
                                )
                                .fillMaxHeight(),
                        )
                    }
                }

            } else {
                    //  沉浸式布局：视频延伸到状态栏 + 内容区域
                    //  📐 [大屏适配] 仅 Expanded 使用分栏布局

                    //  📐 [大屏适配] 根据设备类型选择布局
                    if (useTabletLayout) {
                        // 🖥️ 平板：左右分栏布局（视频+信息 | 评论/推荐）
                        TabletCinemaLayout(
                            playerState = playerState,
                            uiState = uiState,
                            commentState = commentState,
                            engagementState = engagementState,
                            subReplyState = subReplyState,
                            downloadProgress = downloadProgress,
                            tabletCommentPanelWidthPreset = tabletCommentPanelWidthPreset,
                            commentMemberDecorationsEnabled = commentMemberDecorationsEnabled,
                            videoAiSummaryEntryEnabled = videoAiSummaryEntryEnabled,
                            videoNoteEnabled = videoNoteEnabled,
                            videoNoteDefaultCollapsed = videoNoteDefaultCollapsed,
                            playbackActions = playbackActions,
                            engagementActions = engagementActions,
                            commentActions = commentActions,
                            configuration = configuration,
                            isVerticalVideo = isVerticalVideo,
                            sleepTimerMinutes = sleepTimerMinutes,

                            viewPoints = viewPoints,
                            pbpProgressData = pbpProgressData,
                            bvid = bvid,
                            coverUrl = coverUrl,
                            onBack = {
                                com.android.purebilibili.core.util.Logger.d(
                                    "VideoDetailScreen",
                                    "📱 Calling handleBack()"
                                )
                                handleBack()
                            },
                            onUpClick = navigateToUserSpaceFromVideo,
                            onBgmClick = onBgmClick,
                            onNavigateToAudioMode = {
                                presentationState.markNavigatingToAudioMode()
                                onNavigateToAudioMode()
                            },
                            onToggleFullscreen = { toggleFullscreen() },  // 📺 平板全屏切换
                            isInPipMode = isPipMode,
                            onPipClick = handlePipClick,
                            isPortraitFullscreen = isPortraitFullscreen,
                            onHomeClick = {
                                handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                            },

                            transitionEnabled = detailChildTransitionEnabled,  //  传递过渡动画开关
                            danmakuHostActive = !hasCommittedRelatedVideoNavigation,
                            // [New] Codec & Audio
                            currentCodec = codecPreference,
                            onCodecChange = { viewModel.setVideoCodec(it) },
                            currentSecondCodec = secondCodecPreference,
                            onSecondCodecChange = { viewModel.setVideoSecondCodec(it) },
                            currentAudioQuality = audioQualityPreference,
                            onAudioQualityChange = { viewModel.setAudioQuality(it) },
                            onRelatedVideoClick = navigateToRelatedVideo,
                            showUpBadge = homeUpBadgesVisible,
                            onSearchKeywordClick = navigateToSearchKeywordFromVideo,
                            onOpenBilibiliLink = onOpenBilibiliLink,
                            // 🔁 [新增] 播放模式
                            currentPlayMode = currentPlayMode,
                            onPlayModeClick = { com.android.purebilibili.feature.video.player.PlaylistManager.togglePlayMode() },
                            forceCoverOnlyOnReturn = forceCoverOnlyForLiveSafeReturn,
                            predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
                            sponsorContributionState = sponsorContributionState,
                        )
                    } else {
                        // 📱 手机竖屏：原有单列布局
                        val stableStatusBarHeight = resolveVideoDetailStableStatusBarHeightDp(
                            visibleStatusBarHeightDp = WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding()
                                .value,
                            statusBarIgnoringVisibilityHeightDp = WindowInsets.statusBarsIgnoringVisibility
                                .asPaddingValues()
                                .calculateTopPadding()
                                .value,
                            hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars
                        ).dp
                        val playerTopInset = resolveVideoDetailPortraitPlayerTopInsetDp(
                            stableStatusBarHeightDp = stableStatusBarHeight.value,
                            hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars,
                            immersiveStatusBarBackdropEnabled = true,
                            isSharedCardTransition = detailShellSharedBoundsEnabled,
                        ).dp
                        val screenWidthDp = configuration.screenWidthDp.dp
                        val screenHeightDp = configuration.screenHeightDp.dp
                        val videoHeight = screenWidthDp * 9f / 16f  // 16:9 比例
                        val playerChromeProfile = rememberAppPlayerChromeProfile()
                        val videoContentTabSwitchAnimationSpec = remember(playerChromeProfile.tabPresentation) {
                            resolveVideoContentTabSwitchAnimationSpec(playerChromeProfile.tabPresentation)
                        }

                        //  读取竖屏播放器滚动缩小模式
                        val portraitPlayerCollapseMode by com.android.purebilibili.core.store.SettingsManager
                            .getPortraitPlayerCollapseMode(context)
                            .collectAsStateWithLifecycle(initialValue = PortraitPlayerCollapseMode.OFF
            )
                        val inlinePortraitScrollEnabled = shouldEnableInlinePortraitScrollTransform(
                            collapseMode = portraitPlayerCollapseMode,
                            selectedTabIndex = selectedVideoContentTabIndex,
                            isVerticalVideo = isVerticalVideo,
                            isPlaybackPaused = isPlaybackPaused
                        )
                        // 父层只关心折叠阈值，避免列表每个像素的滚动都触发整页重组。
                        var introScrollPastCollapseThreshold by rememberSaveable(currentBvid) {
                            mutableStateOf(
                                isVideoDetailIntroScrollPastCollapseThreshold(
                                    firstVisibleItemIndex = introListState.firstVisibleItemIndex,
                                    firstVisibleItemScrollOffset = introListState.firstVisibleItemScrollOffset
                                )
                            )
                        }
                        val inlinePlayerCollapseState = rememberInlinePortraitPlayerCollapseState(currentBvid)
                        // 评论区「一键回顶」时恢复被压缩的播放器(共享事件)。
                        LaunchedEffect(Unit) {
                            commentBackToTopRestoreFlow.collect {
                                inlinePlayerCollapseState.restore()
                            }
                        }
                        val compactInlinePlayerForCommentTab =
                            shouldUseCompactInlinePortraitPlayerForCommentTab(
                                useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
                                selectedTabIndex = selectedVideoContentTabIndex,
                                isPortraitFullscreen = isPortraitFullscreen,
                                isCommentThreadVisible = subReplyState.visible,
                                collapseMode = portraitPlayerCollapseMode,
                                isVerticalVideo = isVerticalVideo,
                                isPlaybackPaused = isPlaybackPaused
                            )
                        val compactInlinePlayerForIntroScroll =
                            shouldUseCompactInlinePortraitPlayerForIntroScroll(
                                useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
                                selectedTabIndex = selectedVideoContentTabIndex,
                                isPortraitFullscreen = isPortraitFullscreen,
                                firstVisibleItemIndex = if (introScrollPastCollapseThreshold) 1 else 0,
                                firstVisibleItemScrollOffset = 0,
                                collapseMode = portraitPlayerCollapseMode,
                                isVerticalVideo = isVerticalVideo,
                                isPlaybackPaused = isPlaybackPaused
                            )

                        // 📏 [Collapsing Player] 上滑隐藏播放器逻辑
                        val expandedPortraitInlineSpec = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
                            resolvePortraitInlinePlayerLayoutSpec(
                                screenWidthDp = configuration.screenWidthDp.toFloat(),
                                screenHeightDp = configuration.screenHeightDp.toFloat(),
                                isCollapsed = false
                            )
                        }
                        val collapsedPortraitInlineSpec = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
                            resolvePortraitInlinePlayerLayoutSpec(
                                screenWidthDp = configuration.screenWidthDp.toFloat(),
                                screenHeightDp = configuration.screenHeightDp.toFloat(),
                                isCollapsed = true
                            )
                        }
                        val collapseRangePx = with(LocalDensity.current) {
                            if (useOfficialInlinePortraitDetailExperience) {
                                (expandedPortraitInlineSpec.heightDp.dp - collapsedPortraitInlineSpec.heightDp.dp)
                                    .toPx()
                                    .coerceAtLeast(0f)
                            } else {
                                videoHeight.toPx()
                            }
                        }
                        LaunchedEffect(
                            selectedVideoContentTabIndex,
                            compactInlinePlayerForCommentTab,
                            compactInlinePlayerForIntroScroll,
                            portraitPlayerCollapseMode
                        ) {
                            if (!compactInlinePlayerForCommentTab && !compactInlinePlayerForIntroScroll) {
                                inlinePlayerCollapseState.reset()
                            }
                        }
                        var previousTrackedCollapseOffsetPx by remember(currentBvid) {
                            mutableFloatStateOf(inlinePlayerCollapseState.offsetPx)
                        }
                        var collapseMotionSignalActive by remember(currentBvid) {
                            mutableStateOf(false)
                        }
                        LaunchedEffect(inlinePortraitScrollEnabled, inlinePlayerCollapseState.offsetPx) {
                            val currentOffsetPx = inlinePlayerCollapseState.offsetPx
                            val offsetMoved = shouldTrackVideoDetailCollapseMotion(
                                inlinePortraitScrollEnabled = inlinePortraitScrollEnabled,
                                previousOffsetPx = previousTrackedCollapseOffsetPx,
                                currentOffsetPx = currentOffsetPx,
                            )
                            previousTrackedCollapseOffsetPx = currentOffsetPx
                            collapseMotionSignalActive = offsetMoved
                            if (offsetMoved) {
                                kotlinx.coroutines.delay(VIDEO_DETAIL_COLLAPSE_SIGNAL_IDLE_TIMEOUT_MS)
                                collapseMotionSignalActive = false
                            }
                        }
                        TrackJankStateFlag(
                            stateName = "video_detail:player_swipe_collapse",
                            isActive = collapseMotionSignalActive,
                        )
                        val isPlayerCollapsed by remember(inlinePortraitScrollEnabled, collapseRangePx) {
                            derivedStateOf {
                                resolveIsPlayerCollapsed(
                                    swipeHidePlayerEnabled = inlinePortraitScrollEnabled,
                                    playerHeightOffsetPx = inlinePlayerCollapseState.offsetPx,
                                    videoHeightPx = collapseRangePx
                                )
                            }
                        }

                        // 当设置关闭时，重置高度
                        LaunchedEffect(inlinePortraitScrollEnabled) {
                            if (!inlinePortraitScrollEnabled) inlinePlayerCollapseState.reset()
                        }

                        // 简介/相关列表过阈值或评论页已把播放器压扁时，手势 nestedScroll 不得再消费位移，
                        // 否则相关推荐上滑会先空吃一整段折叠距离，表现为不跟手、突然回弹。
                        val skipGesturePlayerCollapse = shouldSkipGesturePlayerCollapseForLayout(
                            compactForIntroScroll = compactInlinePlayerForIntroScroll,
                            compactForCommentTab = compactInlinePlayerForCommentTab,
                        )
                        LaunchedEffect(skipGesturePlayerCollapse, collapseRangePx) {
                            if (skipGesturePlayerCollapse && collapseRangePx > 0f) {
                                // 与视觉折叠对齐，避免之后阈值解除时 offset 仍停在半途。
                                inlinePlayerCollapseState.updateOffset(-collapseRangePx)
                            }
                        }
                        val nestedScrollConnection = remember(
                            inlinePortraitScrollEnabled,
                            isPortraitFullscreen,
                            inlinePlayerCollapseState,
                            skipGesturePlayerCollapse,
                            collapseRangePx,
                        ) {
                            object : NestedScrollConnection {
                                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                    if (available.y != 0f) inlinePlayerCollapseState.beginScroll()
                                    val scrollUpdate = reduceVideoDetailPreScroll(
                                        currentOffsetPx = inlinePlayerCollapseState.offsetPx,
                                        deltaPx = available.y,
                                        minOffsetPx = -collapseRangePx,
                                        inlinePortraitScrollEnabled = inlinePortraitScrollEnabled,
                                        isPortraitFullscreen = isPortraitFullscreen,
                                        layoutAlreadyCollapsed = skipGesturePlayerCollapse,
                                    ) ?: return Offset.Zero
                                    inlinePlayerCollapseState.updateOffset(scrollUpdate.nextOffsetPx)
                                    return Offset(0f, scrollUpdate.consumedDeltaPx)
                                }

                                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                                    if (available.y != 0f) inlinePlayerCollapseState.beginScroll()
                                    val scrollUpdate = reduceVideoDetailPostScroll(
                                        currentOffsetPx = inlinePlayerCollapseState.offsetPx,
                                        deltaPx = available.y,
                                        minOffsetPx = -collapseRangePx,
                                        inlinePortraitScrollEnabled = inlinePortraitScrollEnabled,
                                        isPortraitFullscreen = isPortraitFullscreen,
                                        layoutAlreadyCollapsed = skipGesturePlayerCollapse,
                                    ) ?: return Offset.Zero
                                    inlinePlayerCollapseState.updateOffset(scrollUpdate.nextOffsetPx)
                                    return Offset(0f, scrollUpdate.consumedDeltaPx)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(nestedScrollConnection)
                        ) {

                        //  播放器隐藏状态（用于动画）
                        //  播放器隐藏状态（用于动画）
                        //  当 playerHeightOffsetPx 为 -videoHeightPx 时，高度只剩 statusBarHeight
                        //  [Fix] 竖屏全屏模式下强制高度不受偏移影响
                        val playerHeightOffset = if (isPortraitFullscreen) {
                            0f
                        } else {
                            inlinePlayerCollapseState.offsetPx
                        }
                        val collapseProgress = resolveVideoDetailCollapseProgress(
                            playerHeightOffsetPx = playerHeightOffset,
                            collapseRangePx = collapseRangePx,
                            isPortraitFullscreen = isPortraitFullscreen
                        )
                        val commentTabCollapseProgress by animateFloatAsState(
                            targetValue = if (compactInlinePlayerForCommentTab || compactInlinePlayerForIntroScroll) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = resolveInlinePortraitPlayerCommentCollapseDurationMillis(
                                    videoContentTabSwitchAnimationSpec
                                ),
                                easing = FastOutSlowInEasing
                            ),
                            label = "inline_portrait_comment_tab_collapse"
                        )
                        val effectiveCollapseProgress = resolveInlinePortraitPlayerCollapseProgress(
                            manualCollapseProgress = collapseProgress,
                            compactForCommentTabProgress = commentTabCollapseProgress,
                            restoreRequested = inlinePlayerCollapseState.restoreRequested
                        )
                        val expandedViewportHeight = when {
                            suppressPhoneDetailBodyForDirectPortrait -> screenHeightDp
                            useOfficialInlinePortraitDetailExperience -> expandedPortraitInlineSpec.heightDp.dp
                            else -> videoHeight
                        }
                        val collapsedViewportHeight = if (useOfficialInlinePortraitDetailExperience) {
                            collapsedPortraitInlineSpec.heightDp.dp
                        } else {
                            0.dp
                        }
                        val inlineViewportHeight = lerp(
                            expandedViewportHeight,
                            collapsedViewportHeight,
                            effectiveCollapseProgress
                        )
                        val expandedViewportWidth = if (useOfficialInlinePortraitDetailExperience) {
                            expandedPortraitInlineSpec.widthDp.dp
                        } else {
                            screenWidthDp
                        }
                        val collapsedViewportWidth = if (useOfficialInlinePortraitDetailExperience) {
                            collapsedPortraitInlineSpec.widthDp.dp
                        } else {
                            screenWidthDp
                        }
                        val inlineViewportWidth = lerp(
                            expandedViewportWidth,
                            collapsedViewportWidth,
                            effectiveCollapseProgress
                        )
                        val inlinePlayerHeight = inlineViewportHeight + playerTopInset
                        val fullscreenPlayerHeight = screenHeightDp.coerceAtLeast(1.dp)

                        //  注意：移除了状态栏黑色 Spacer
                        // 播放器将延伸到状态栏下方，共享元素过渡更流畅

                        //  注意：移除了状态栏黑色 Spacer
                        // 播放器将延伸到状态栏下方，共享元素过渡更流畅

                        //  视频播放器区域：状态栏可见时避让，隐藏时让画面沉浸到顶部。
                        //  尝试获取共享元素作用域
                        val sharedTransitionScope = LocalSharedTransitionScope.current
                        val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
                        val coverSharedElementSourceRoute = resolveForcedReturnCoverSharedElementSourceRoute(
                            sourceRouteForSharedElement
                        )

                        //  为播放器容器添加共享元素标记（封面 ↔ 播放器区域映射）
                        // shell 已接管时禁止再挂 cover sharedBounds（默认 Center 会往屏幕中心飞）。
                        val isFullscreenTarget = activeVideoSharedTransitionVisualSpec.fillTargetViewport
                        val attachPlayerCoverSharedBounds =
                            com.android.purebilibili.core.ui.transition.shouldAttachVideoDetailCoverSharedBounds(
                                coverSharedBoundsEnabled =
                                    shouldEnableVideoCoverSharedTransition(
                                        transitionEnabled = detailChildTransitionEnabled,
                                        hasSharedTransitionScope = sharedTransitionScope != null,
                                        hasAnimatedVisibilityScope = animatedVisibilityScope != null
                                    ) &&
                                        activeVideoSharedTransitionVisualSpec.useCoverSharedBounds,
                                detailShellSharedBoundsEnabled = detailShellSharedBoundsEnabled,
                                immediatePlayback = videoSharedPlaybackIntent ==
                                    VideoSharedTransitionPlaybackIntent.ImmediatePlayback,
                                forceCoverOnlyForReturn = forceCoverOnlyForReturn,
                            )
                        val playerContainerModifier = if (attachPlayerCoverSharedBounds) {
                            with(requireNotNull(sharedTransitionScope)) {
                                Modifier
                                    .sharedBounds(
                                        sharedContentState = rememberSharedContentState(
                                            key = com.android.purebilibili.core.ui.transition.videoCoverSharedElementKey(
                                                bvid,
                                                sourceRoute = coverSharedElementSourceRoute
                                            )
                                        ),
                                        animatedVisibilityScope = requireNotNull(animatedVisibilityScope),
                                        boundsTransform = { initialBounds, targetBounds ->
                                            val duration = if (
                                                homeSharedTransitionMotionSpec.enabled && isFullscreenTarget
                                            ) {
                                                homeSharedTransitionMotionSpec.fullscreenDurationMillis
                                            } else {
                                                homeSharedTransitionMotionSpec.durationMillis
                                            }
                                            videoSharedElementBoundsTransformSpec(
                                                motion = homeSharedTransitionMotionSpec,
                                                initialBounds = initialBounds,
                                                targetBounds = targetBounds,
                                                durationMillis = duration
                                            )
                                        },
                                        resizeMode = com.android.purebilibili.core.ui.transition
                                            .resolveVideoCardSharedBoundsResizeMode(
                                                fillFullscreenShell = isFullscreenTarget,
                                            ),
                                        clipInOverlayDuringTransition = OverlayClip(
                                            RoundedCornerShape(
                                                resolveVideoDetailShellOverlayCornerDp(
                                                    visualSpec = activeVideoSharedTransitionVisualSpec,
                                                    liveReturnMorph = liveReturnMorph,
                                                    isReturningVisualState =
                                                        useReturningVideoDetailVisualState,
                                                ).dp
                                            )
                                        )
                                    )
                            }
                        } else {
                            Modifier
                        }

                        // isLeaving：离开态（正文让位等）；封面/播放器 handoff 用 isCommittedCardReturn。
                        val isLeaving = useReturningVideoDetailVisualState
                        val crossfadeCoverUrl = remember(coverUrl) {
                            if (coverUrl.isNotBlank()) {
                                val url = coverUrl.trim()
                                when {
                                    url.startsWith("https://") -> url
                                    url.startsWith("http://") -> url.replace("http://", "https://")
                                    url.startsWith("//") -> "https:$url"
                                    else -> url
                                }
                            } else {
                                ""
                            }
                        }
                        val sharedCoverCacheKey = remember(bvid) {
                            resolveVideoSharedCoverCacheKey(bvid)
                        }
                        val residentCoverImageRequest = remember(
                            context,
                            crossfadeCoverUrl,
                            sharedCoverCacheKey,
                        ) {
                            if (crossfadeCoverUrl.isBlank()) {
                                null
                            } else {
                                coil.request.ImageRequest.Builder(context)
                                    .data(crossfadeCoverUrl)
                                    .placeholderMemoryCacheKey(sharedCoverCacheKey)
                                    .crossfade(false)
                                    .memoryCacheKey(sharedCoverCacheKey)
                                    .diskCacheKey(sharedCoverCacheKey)
                                    .build()
                            }
                        }

                        //  播放器容器按当前顶部避让高度计算，避免隐藏状态栏后留下黑边。
                        //  [修复] 始终保持播放器在 Composition 中，避免隐藏时重新创建导致重载
                        Box(
                            modifier = playerContainerModifier
                                .fillMaxWidth()
                                .continuousPlayerViewportHeight(
                                    progressProvider = { continuousPlayerProgress.value },
                                    inlineHeight = inlinePlayerHeight,
                                    fullscreenHeight = fullscreenPlayerHeight,
                                    enabled = continuousFullscreenTransitionEnabled,
                                    // 横屏 16:9：按真实布局宽度算高度，消除 vivo 等机型左右黑边。
                                    preferLayoutWidth16x9Inline = !useOfficialInlinePortraitDetailExperience &&
                                        !suppressPhoneDetailBodyForDirectPortrait,
                                    inlineTopInset = if (!useOfficialInlinePortraitDetailExperience) {
                                        playerTopInset
                                    } else {
                                        0.dp
                                    },
                                )
                                .background(Color.Black)  // 黑色背景
                                .clipToBounds()
                                //  [PiP修复] 捕获视频播放器在屏幕上的位置
                                .onGloballyPositioned { layoutCoordinates ->
                                    // Morph height changes every frame. PiP and system-bar bounds only need
                                    // the settled geometry, so avoid feeding this layout work back into composition.
                                    if (isContinuousPlayerMorphing) {
                                        return@onGloballyPositioned
                                    }
                                    val position = layoutCoordinates.positionInWindow()
                                    val rootPosition = layoutCoordinates.positionInRoot()
                                    val size = layoutCoordinates.size
                                    val nextBounds = android.graphics.Rect(
                                        position.x.toInt(),
                                        position.y.toInt(),
                                        position.x.toInt() + size.width,
                                        position.y.toInt() + size.height
                                    )
                                    val nextRootBottomPx = (rootPosition.y + size.height).roundToInt()
                                    if (
                                        videoPlayerRootBottomPx == 0 ||
                                        abs(videoPlayerRootBottomPx - nextRootBottomPx) > 3
                                    ) {
                                        videoPlayerRootBottomPx = nextRootBottomPx
                                    }
                                    if (!hasMeaningfulVideoPlayerBoundsChange(videoPlayerBounds, nextBounds)) {
                                        return@onGloballyPositioned
                                    }
                                    videoPlayerBounds = nextBounds
                                }
                        ) {
                            // 常驻封面叠层：仅已提交的 CoverFirst 返回才接管；预测 seek / cancel
                            // 始终保持 cover=0、player=1，避免回到详情页时闪出一帧封面。
                            if (residentCoverImageRequest != null) {
                                AsyncImage(
                                    model = residentCoverImageRequest,
                                    contentDescription = "cover",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            // 预测返回手势进行中（含未提交 seek 与取消恢复）：保 player、不画封面，
                                            // 避免手势过程中画面实时消失（实时画面转场开关关闭时同样生效）。
                                            val gestureKeepLivePlayer =
                                                videoCardDepthBackgroundState
                                                    .isReturnGestureInProgressProvider() ||
                                                    videoCardDepthBackgroundState
                                                        .isGestureRestoreInProgressProvider()
                                            alpha = resolveVideoDetailReturnCoverAlpha(
                                                transitionProgress =
                                                    resolveVideoDetailReturnVisualProgress(
                                                        animatedVisibilityProgress =
                                                            detailTransitionProgress.value,
                                                        morphDepthProgress =
                                                            videoCardDepthBackgroundState
                                                                .progressProvider(),
                                                        liveReturnMorph = liveReturnMorph,
                                                    ),
                                                isCommittedCardReturn = isCommittedCardReturn,
                                                hasResidentCover = hasResidentReturnCover,
                                                liveReturnMorph = liveReturnMorph,
                                                // 仅实时视频 morph 在预测返回时保 player；
                                                // 关闭实时画面时走封面/截图垫层，避免 SurfaceView 黑块。
                                                keepLivePlayerForPredictiveBack = gestureKeepLivePlayer,
                                            )
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val gestureKeepLivePlayer =
                                            videoCardDepthBackgroundState
                                                .isReturnGestureInProgressProvider() ||
                                                videoCardDepthBackgroundState
                                                    .isGestureRestoreInProgressProvider()
                                        alpha = resolveVideoDetailReturnPlayerAlpha(
                                            transitionProgress =
                                                resolveVideoDetailReturnVisualProgress(
                                                    animatedVisibilityProgress =
                                                        detailTransitionProgress.value,
                                                    morphDepthProgress =
                                                        videoCardDepthBackgroundState
                                                            .progressProvider(),
                                                    liveReturnMorph = liveReturnMorph,
                                                ),
                                            isCommittedCardReturn = isCommittedCardReturn,
                                            hasResidentCover = hasResidentReturnCover,
                                            liveReturnMorph = liveReturnMorph,
                                            keepLivePlayerForPredictiveBack = gestureKeepLivePlayer,
                                        )
                                    }
                            ) {
                            if (continuousFullscreenTransitionEnabled) {
                                continuousPlayerContent(
                                    ContinuousPlayerHostLayout(
                                        modifier = Modifier.align(Alignment.TopCenter),
                                        viewportWidth = screenWidthDp,
                                        alpha = inlinePlayerAlpha,
                                        scale = inlinePlayerScale,
                                        isFullscreen = false,
                                        contentTopInset = playerTopInset,
                                    )
                                )
                            } else {
                            PortraitInlineVideoPlayerHost(
                                modifier = Modifier.align(Alignment.TopCenter),
                                animatedViewportWidth = inlineViewportWidth,
                                contentTopInset = playerTopInset,
                                inlinePlayerAlpha = inlinePlayerAlpha,
                                inlinePlayerScale = inlinePlayerScale,
                                playerState = playerState,
                                uiState = uiState,
                                isPipMode = isPipMode,
                                transitionEnabled = detailChildTransitionEnabled,
                                transitionChromeAlphaProvider =
                                    videoCardDetailChromeAlphaProvider,
                                danmakuHostActive = !hasCommittedRelatedVideoNavigation,
                                onToggleFullscreen = { toggleFullscreen() },
                                playbackActions = playbackActions,
                                onDoubleTapLike = engagementViewModel::toggleLike,
                                onBack = handleBack,
                                onHomeClick = {
                                    handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
                                },
                                videoPlayerSectionTarget = videoPlayerSectionTarget,
                                sponsorSegment = sponsorSegment,
                                showSponsorSkipButton = showSponsorSkipButton,
                                sponsorContributionState = sponsorContributionState,
                                sleepTimerMinutes = sleepTimerMinutes,
                                viewPoints = viewPoints,
                                pbpProgressData = pbpProgressData,
                                sponsorProgressMarkers = sponsorProgressMarkers,
                                isVerticalVideo = isVerticalVideo && (allowStandalonePortraitExperience || useOfficialInlinePortraitDetailExperience),
                                onPortraitFullscreen = {
                                    when (
                                        resolvePortraitFullscreenButtonAction(
                                            useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience
                                        )
                                    ) {
                                        PortraitFullscreenButtonAction.ENTER_PORTRAIT_FULLSCREEN -> {
                                            enterPortraitFullscreen()
                                        }
                                    }
                                },
                                isPortraitFullscreen = isPortraitFullscreen,
                                onPipClick = handlePipClick,
                                codecPreference = codecPreference,
                                secondCodecPreference = secondCodecPreference,
                                audioQualityPreference = audioQualityPreference,
                                onNavigateToAudioMode = {
                                    viewModel.setAudioMode(true)
                                    presentationState.markNavigatingToAudioMode()
                                    onNavigateToAudioMode()
                                },
                                forceCoverOnly = forceCoverOnlyForLiveSafeReturn ||
                                    shouldForceBackPreviewPlayerCover(
                                        keepLoadedContentForBackPreview = keepLoadedContentForBackPreview,
                                        bindLivePlayerForBackPreview = bindLivePlayerForBackPreview
                                    ),
                                preserveCurrentFrameOnFullscreenChange = preserveCurrentFrameOnFullscreenChange,
                                liveBackPreview = bindLivePlayerForBackPreview,
                                useTextureSurfaceForNavigation = useTextureSurfaceForNavigation,
                                predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
                                allowLivePlayerSharedElement = allowLivePlayerSharedElement,
                                sourceRouteForSharedElement = sourceRouteForSharedElement,
                                preserveSourceCardCornerDuringSharedReturn =
                                    detailShellSharedBoundsEnabled &&
                                        useReturningVideoDetailVisualState,
                                suppressSubtitleOverlay = shouldSuppressSubtitleOverlay,
                                subtitleDisplayModePreferenceOverride = subtitleDisplayModeOverride,
                                onSubtitleDisplayModePreferenceOverrideChange = { subtitleDisplayModeOverride = it }
                            )
                            }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (suppressPhoneDetailBodyForDirectPortrait) {
                                        Color.Black
                                    } else {
                                        AppSurfaceTokens.background()
                                    }
                                )
                                .drawWithContent {
                                    val reveal = detailInfoRevealProgress.value.coerceIn(0f, 1f)
                                    clipRect(
                                        left = 0f,
                                        top = 0f,
                                        right = size.width,
                                        bottom = size.height * reveal,
                                    ) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                                .graphicsLayer {
                                    val reveal = detailInfoRevealProgress.value.coerceIn(0f, 1f)
                                    val holdFullyOpaque =
                                        suppressEnterFadeAfterBackPreview && !isLeaving
                                    if (liveReturnMorph && !holdFullyOpaque) {
                                        val frame = resolveVideoCardSecondaryContentVisualFrame(
                                            morphDepthProgress =
                                                videoCardDepthBackgroundState.progressProvider(),
                                            phase = videoCardDepthBackgroundState.phaseProvider(),
                                            isReturnGestureInProgress =
                                                videoCardDepthBackgroundState
                                                    .isReturnGestureInProgressProvider(),
                                            motionTier =
                                                videoCardDepthBackgroundState.motionTierProvider(),
                                        )
                                        alpha = if (
                                            isLeaving && isQuickReturningFromDetail
                                        ) {
                                            0f
                                        } else {
                                            frame.alpha
                                        }
                                        translationY = with(videoCardTransitionDensity) {
                                            (frame.translationYDp + (1f - reveal) * 12f).dp.toPx()
                                        }
                                    } else {
                                        alpha = resolveVideoDetailReturnContentAlpha(
                                            transitionProgress = detailTransitionProgress.value,
                                            isCommittedCardReturn = isCommittedCardReturn,
                                            holdFullyOpaqueAfterBackPreview = holdFullyOpaque,
                                            liveReturnMorph = false,
                                            isQuickReturn = isQuickReturningFromDetail,
                                        )
                                        translationY = with(videoCardTransitionDensity) {
                                            ((1f - reveal) * 12f).dp.toPx()
                                        }
                                    }
                                }
                                // .nestedScroll(nestedScrollConnection) // [Remove] 移除嵌套滚动，确保 Tabs 正常滑动
                        ) {
                            // 「竖屏直达」morph 期间不绘制详情 body，避免先露出简介/评论再跳竖全屏。
                            // 错误态仍展示，避免黑屏无法重试。
                            when {
                                suppressPhoneDetailBodyForDirectPortrait &&
                                    uiState !is VideoPlaybackUiState.Error -> Unit
                                // 返回 morph 次要内容 alpha 已近 0：跳过 composition，壳仍 fillMaxSize。
                                detachSecondaryContentForReturn &&
                                    uiState !is VideoPlaybackUiState.Error -> Unit
                                uiState is VideoPlaybackUiState.Loading -> {
                                    val loadingState = uiState as VideoPlaybackUiState.Loading
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        //  显示重试进度
                                        if (loadingState.retryAttempt > 0) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    AdaptiveLoadingIndicator()
                                                    Spacer(Modifier.height(16.dp))
                                                    AppText(
                                                        text = "正在重试 ${loadingState.retryAttempt}/${loadingState.maxAttempts}...",
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            VideoDetailSkeleton(
                                                animated = isTransitionFinished,
                                            )
                                        }
                                    }
                                }

                                uiState is VideoPlaybackUiState.Success -> {
                                    val success = uiState as VideoPlaybackUiState.Success
                                    VideoDetailPhoneSuccessContentLayer(
                                        success = success,
                                        introListState = introListState,
                                        commentListState = commentListState,
                                        videoContentPagerState = videoContentPagerState,
                                        commentState = commentState,
                                        engagementState = engagementState,
                                        androidNativeLiquidGlassEnabled =
                                            homeSettings.androidNativeLiquidGlassEnabled,
                                        commentMemberDecorationsEnabled = commentMemberDecorationsEnabled,
                                        playbackActions = playbackActions,
                                        engagementActions = engagementActions,
                                        commentActions = commentActions,
                                        context = context,
                                        sortPreferenceScope = sortPreferenceScope,
                                        playerState = playerState,
                                        motionSpec = motionSpec,
                                        hazeState = hazeState,
                                        isTransitionFinished = isTransitionFinished ||
                                            keepLoadedContentForBackPreview ||
                                            suppressEnterFadeAfterBackPreview,
                                        isLeaving = isLeaving,
                                        rootTransitionOwnsContentAlpha = detailShellSharedBoundsEnabled,
                                        keepContentVisibleAfterBackPreview =
                                            suppressEnterFadeAfterBackPreview ||
                                                keepLoadedContentForBackPreview,
                                        shouldShowExternalPlaylistQueueBar = shouldShowExternalPlaylistQueueBar,
                                        selectedVideoContentTabIndex = selectedVideoContentTabIndex,
                                        useTabletLayout = useTabletLayout,
                                        isFullscreenMode = isFullscreenMode,
                                        isPortraitFullscreen = isPortraitFullscreen,
                                        showCommentInput = showCommentInput,
                                        isCommentThreadVisible = subReplyState.visible,
                                        showFavoriteFolderDialog = showFavoriteFolderDialog,
                                        downloadProgress = downloadProgress,
                                        danmakuEnabledForDetail = effectiveDanmakuEnabledForDetail,
                                        isQuickReturnLimitedForSharedElements =
                                            isReturningFromDetail && isQuickReturningFromDetail,
                                        transitionEnabled = detailChildTransitionEnabled,
                                        sourceRouteForSharedElement = sourceRouteForSharedElement,
                                        isPlayerCollapsed = isPlayerCollapsed,
                                        onRestorePlayer = inlinePlayerCollapseState::restore,
                                        onBgmClick = onBgmClick,
                                        homeUpBadgesVisible = homeUpBadgesVisible,
                                        isVideoPlaying = isVideoPlaying,
                                        onSelectedTabChange = presentationState::selectTab,
                                        onIntroScrollThresholdChange = {
                                            introScrollPastCollapseThreshold = it
                                        },
                                        openFavoriteFolders = openFavoriteFolders,
                                        navigateToUserSpaceFromVideo = navigateToUserSpaceFromVideo,
                                        navigateToRelatedVideo = navigateToRelatedVideo,
                                        openCommentUrl = openCommentUrl,
                                        onSearchKeywordClick = navigateToSearchKeywordFromVideo,
                                        onOpenBilibiliLink = onOpenBilibiliLink,
                                        onShareVideo = { payload -> pendingVideoShare = payload },
                                        externalPlaylistQueueTitle = externalPlaylistQueueTitle,
                                        playlistItems = playlistItems,
                                        onShowExternalPlaylistQueueSheet = {
                                            showExternalPlaylistQueueSheet = true
                                        }
                                    )
                                } // End of Success block

                                uiState is VideoPlaybackUiState.Error -> {
                                    val errorState = uiState as VideoPlaybackUiState.Error
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(32.dp)
                                        ) {
                                            //  根据错误类型显示不同图标
                                            AppText(
                                                text = when (errorState.error) {
                                                    is com.android.purebilibili.data.model.VideoLoadError.NetworkError -> "📡"
                                                    is com.android.purebilibili.data.model.VideoLoadError.VideoNotFound -> "🔍"
                                                    is com.android.purebilibili.data.model.VideoLoadError.RegionRestricted -> "🌐"
                                                    is com.android.purebilibili.data.model.VideoLoadError.RateLimited -> "⏳"
                                                    is com.android.purebilibili.data.model.VideoLoadError.GlobalCooldown -> ""
                                                    is com.android.purebilibili.data.model.VideoLoadError.PlayUrlEmpty -> "⚡"
                                                    else -> ""
                                                },
                                                fontSize = 48.sp
                                            )
                                            Spacer(Modifier.height(16.dp))
                                            AppText(
                                                text = errorState.msg,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 16.sp,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )

                                            //  针对风控错误显示额外建议
                                            when (errorState.error) {
                                                is com.android.purebilibili.data.model.VideoLoadError.GlobalCooldown,
                                                is com.android.purebilibili.data.model.VideoLoadError.PlayUrlEmpty -> {
                                                    Spacer(Modifier.height(8.dp))
                                                    AppText(
                                                        text = " 建议：切换 WiFi/移动数据 或 清除缓存后重试",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 13.sp,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                                is com.android.purebilibili.data.model.VideoLoadError.RateLimited -> {
                                                    Spacer(Modifier.height(8.dp))
                                                    AppText(
                                                        text = " 该视频可能暂时不可用，请尝试其他视频",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 13.sp,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                                else -> {}
                                            }

                                            //  只有可重试的错误才显示重试按钮（或者风控错误允许强制重试）
                                            val showRetryButton = errorState.canRetry ||
                                                errorState.error is com.android.purebilibili.data.model.VideoLoadError.RateLimited ||
                                                errorState.error is com.android.purebilibili.data.model.VideoLoadError.PlayUrlEmpty
                                            if (showRetryButton) {
                                                Spacer(Modifier.height(24.dp))
                                                AppButton(
                                                    onClick = { viewModel.retry() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = resolveFilledButtonContainerColor(MaterialTheme.colorScheme),

                                                        contentColor = resolveFilledButtonContentColor(MaterialTheme.colorScheme)
                                                    )
                                                ) {
                                                    AppText(
                                                        text = when (errorState.error) {
                                                            is com.android.purebilibili.data.model.VideoLoadError.RateLimited -> "强制重试"
                                                            is com.android.purebilibili.data.model.VideoLoadError.GlobalCooldown -> "清除冷却并重试"
                                                            else -> "重试"
                                                        }
                                                    )
                                                }
                                            }

                                            //  🎬 [引导] 大会员受限时，提示用已保存的大会员账号播放
                                            val isVipRequiredError =
                                                errorState.error is com.android.purebilibili.data.model.VideoLoadError.VipRequired ||
                                                    (errorState.error as? com.android.purebilibili.data.model.VideoLoadError.ApiError)?.code == -10403 ||
                                                    (errorState.error as? com.android.purebilibili.data.model.VideoLoadError.UnknownError)
                                                        ?.throwable?.message?.contains("大会员") == true
                                            if (isVipRequiredError) {
                                                val vipPlaybackCandidates = remember(errorState.error) {
                                                    val currentPlaybackMid = com.android.purebilibili.core.network.NetworkModule
                                                        .playbackAccount()?.mid
                                                    com.android.purebilibili.core.store.AccountSessionStore
                                                        .getAccounts(context)
                                                        .filter { account ->
                                                            account.isVip &&
                                                                account.sessData.isNotBlank() &&
                                                                account.mid != currentPlaybackMid
                                                        }
                                                }
                                                val vipCandidate = vipPlaybackCandidates.firstOrNull()
                                                if (vipCandidate != null) {
                                                    Spacer(Modifier.height(16.dp))
                                                    AppButton(
                                                        onClick = {
                                                            com.android.purebilibili.core.store.AccountSessionStore
                                                                .setPlaybackAccountMid(context, vipCandidate.mid)
                                                            android.widget.Toast.makeText(
                                                                context,
                                                                "已使用「${vipCandidate.name.ifBlank { "UID ${vipCandidate.mid}" }}」的大会员播放",
                                                                android.widget.Toast.LENGTH_SHORT
                                                            ).show()
                                                            viewModel.retry()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.tertiary
                                                        )
                                                    ) {
                                                        AppText("使用「${vipCandidate.name.ifBlank { "UID ${vipCandidate.mid}" }}」的大会员播放")
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    AppText(
                                                        text = "可在「我的 - 账号与播放」中更换播放账号",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 12.sp,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                    }
                    }  // 📱 手机竖屏布局结束（Column）
                    }  // Box with nested scroll
                }  // else shouldUseSplitLayout
            }  // else targetIsLandscape
    }

    @Composable
    fun BoxScope.VideoDetailRouteSheetOverlayContent() {
        VideoDetailPortraitOverlayAdapter(
            uiState = uiState,
            portraitExperienceEnabled = portraitExperienceEnabled,
            isPortraitFullscreen = isPortraitFullscreen,
            useOfficialInlinePortraitDetailExperience = useOfficialInlinePortraitDetailExperience,
            isLandscape = isLandscape,
            shouldAnimatePortraitPager = shouldAnimatePortraitPager,
            motionSpec = portraitPagerMotionSpec,
            initialBvidOverride = pendingMainReloadBvidAfterPortrait,
            initialStartPositionMs = portraitSyncSnapshotPositionMs,
            entryCoverUrl = coverUrl,
            playbackViewModel = viewModel,
            engagementViewModel = engagementViewModel,
            sharedPlayer = if (useSharedPortraitPlayer) playerState.player else null,
            useTextureSurfaceForNavigation = useTextureSurfaceForNavigation,
            onBack = { presentationState.setPortraitFullscreen(false) },
            onHomeClick = {
                presentationState.setPortraitFullscreen(false)
                handleTopBarAction(resolveVideoDetailTopBarAction(isHomeButton = true))
            },
            onVideoChange = { portraitPendingSelectionBvid = it },
            onProgressUpdate = { updatedBvid, positionMs, updatedCid ->
                portraitPendingSelectionBvid = updatedBvid
                portraitSyncSnapshotBvid = updatedBvid
                portraitSyncSnapshotCid = updatedCid
                portraitSyncSnapshotPositionMs = positionMs.coerceAtLeast(0L)
                if (shouldMirrorPortraitProgressToMainPlayer) {
                    hasPendingPortraitSync = true
                    if (tryApplyPortraitProgressSync(updatedBvid, portraitSyncSnapshotPositionMs)) {
                        hasPendingPortraitSync = false
                    }
                }
            },
            onExitSnapshot = { updatedBvid, positionMs, updatedCid ->
                presentationState.switchVideo(updatedBvid, updatedCid)
                portraitPendingSelectionBvid = updatedBvid
                portraitSyncSnapshotBvid = updatedBvid
                portraitSyncSnapshotCid = updatedCid
                portraitSyncSnapshotPositionMs = positionMs.coerceAtLeast(0L)
                pendingMainReloadBvidAfterPortrait = updatedBvid
                if (shouldMirrorPortraitProgressToMainPlayer) {
                    hasPendingPortraitSync = true
                    if (tryApplyPortraitProgressSync(updatedBvid, portraitSyncSnapshotPositionMs)) {
                        hasPendingPortraitSync = false
                    }
                }
            },
            onSearchClick = {
                hasDeferredPortraitRestoreAfterExternalNavigation =
                    com.android.purebilibili.feature.video.ui.pager
                        .shouldDeferPortraitRestoreUntilForegroundResume(
                            isPortraitFullscreen = isPortraitFullscreen,
                            isExternalNavigation = true,
                        )
                if (com.android.purebilibili.feature.video.ui.pager
                        .shouldExitPortraitForExternalNavigation(isPortraitFullscreen)
                ) {
                    presentationState.setPortraitFullscreen(false)
                }
                navigateToSearchFromVideo()
            },
            onUserClick = { mid ->
                val anchorBvid = portraitPendingSelectionBvid
                    ?: pendingMainReloadBvidAfterPortrait
                    ?: portraitSyncSnapshotBvid
                    ?: (uiState as? VideoPlaybackUiState.Success)?.info?.bvid
                if (!anchorBvid.isNullOrBlank()) {
                    val anchorCid = if (anchorBvid == portraitSyncSnapshotBvid) {
                        portraitSyncSnapshotCid
                    } else {
                        0L
                    }
                    presentationState.switchVideo(anchorBvid, anchorCid)
                    pendingMainReloadBvidAfterPortrait = anchorBvid
                }
                hasDeferredPortraitRestoreAfterExternalNavigation =
                    com.android.purebilibili.feature.video.ui.pager
                        .shouldDeferPortraitRestoreUntilForegroundResume(
                            isPortraitFullscreen = isPortraitFullscreen,
                            isExternalNavigation = true,
                        )
                if (com.android.purebilibili.feature.video.ui.pager
                        .shouldExitPortraitForUserSpaceNavigation(isPortraitFullscreen)
                ) {
                    presentationState.setPortraitFullscreen(false)
                }
                navigateToUserSpaceFromVideo(mid)
            },
            onRotateToLandscape = {
                presentationState.setPortraitFullscreen(false)
                val hostActivity = context.findActivity()
                val targetOrientation = resolvePortraitRotateTargetOrientation(
                    isOrientationDrivenFullscreen = isOrientationDrivenFullscreen,
                    manualPortraitHoldActive = manualPortraitHoldActive,
                )
                if (hostActivity != null && targetOrientation != null) {
                    userRequestedFullscreen = true
                    manualPortraitHoldActive = false
                    hostActivity.requestedOrientation = targetOrientation
                } else {
                    toggleFullscreen()
                }
            },
        )

        VideoDetailCommonOverlayAdapter(
            interactiveChoicePanel = interactiveChoicePanel,
            engagementState = engagementState,
            playbackViewModel = viewModel,
            engagementViewModel = engagementViewModel,
            queueVisible = shouldShowExternalPlaylistQueueBar && showExternalPlaylistQueueSheet,
            queueTitle = externalPlaylistQueueTitle,
            playlist = playlistItems,
            playlistCurrentIndex = playlistCurrentIndex,
            hazeState = hazeState,
            queuePresentation = externalPlaylistQueueSheetPresentation,
            pendingVideoShare = pendingVideoShare,
            player = playerState.player,
            onDismissQueue = { showExternalPlaylistQueueSheet = false },
            onVideoSelected = { index, item ->
                PlaylistManager.playAt(index)
                showExternalPlaylistQueueSheet = false
                switchVideoInCurrentDetailPage(
                    targetBvid = item.bvid,
                    targetCid = 0L,
                    autoPlay = true,
                )
            },
            onDismissShare = { pendingVideoShare = null },
        )

        val inputOverlayLayoutInfo = VideoDetailInputOverlayAdapter(
            context = context,
            configuration = configuration,
            viewModel = viewModel,
            commentState = commentState,
            showCommentInput = showCommentInput,
            isLandscape = isLandscape,
            isFullscreenMode = isFullscreenMode,
            isPortraitFullscreen = isPortraitFullscreen,
            videoPlayerRootBottomPx = videoPlayerRootBottomPx,
            hideStatusBars = systemBarsVisibilityPolicy.hideStatusBars,
            immersiveStatusBarBackdropEnabled = true,
            currentVideoPositionMsProvider = {
                playerState.player.currentPosition.coerceAtLeast(0L)
            },
        )
        val screenHeightPx = inputOverlayLayoutInfo.screenHeightPx
        val danmakuDialogTopReservePx = inputOverlayLayoutInfo.topReservedPx

        VideoDetailDownloadOverlayAdapter(
            viewModel = viewModel,
            uiState = uiState,
        )

        val successState = uiState as? VideoPlaybackUiState.Success
        DetachedVideoCommentThreadHost(
            visible = shouldShowDetachedVideoCommentThreadHost(useTabletLayout = useTabletLayout) &&
                !(isFullscreenMode && landscapeCommentPanelVisible),
            successState = successState,
            commentState = commentState,
            commentViewModel = commentViewModel,
            forceInitialize = shouldForceInitializeDetachedCommentThreadHostForRoute(
                routeCommentRootRpid = openCommentRootRpidFromRoute,
                aid = successState?.info?.aid ?: 0L,
                hasHandledRouteComment = hasHandledCommentRootFromRoute
            ),
            viewModel = viewModel,
            onUpClick = navigateToUserSpaceFromVideo,
            onNavigateToRelatedVideo = { targetVideoId ->
                navigateToRelatedVideo(targetVideoId, null)
            },
            onSearchKeywordClick = navigateToSearchKeywordFromVideo,
            onOpenBilibiliLink = onOpenBilibiliLink,
            screenHeightPx = screenHeightPx,
            topReservedPx = danmakuDialogTopReservePx,
            onTimestampClick = { positionMs ->
                seekPlayerFromUserAction(playerState.player, positionMs)
                commentViewModel.closeSubReply()
            },
            onBackToTop = {
                // 评论区下滑缩小播放器后,一键回顶同时恢复播放器全尺寸。
                commentBackToTopRestoreFlow.tryEmit(Unit)
            }
        )

        VideoDetailFavoriteFolderOverlayAdapter(
            visible = showFavoriteFolderDialog,
            viewModel = viewModel,
        )

        VideoDetailFeedbackOverlayAdapter(
            playbackViewModel = viewModel,
            engagementViewModel = engagementViewModel,
            engagementState = engagementState,
            playbackEventState = playbackEventState,
            hazeState = hazeState,
            isFullscreenMode = isFullscreenMode,
            isLandscape = isLandscape,
            reducedMotion = isReducedActionMotion,
        )

        VideoDetailPlayerSettingsOverlayAdapter(
            context = context,
            viewModel = viewModel,
            isFullscreenMode = isFullscreenMode,
            isPortraitFullscreen = isPortraitFullscreen,
            danmakuManager = danmakuManager,
        )
    }

    VideoDetailScreenContent(
        transitionState = transitionState,
        routeSheetMotion = routeSheetMotion,
        isFullscreenMode = isFullscreenMode,
        backgroundColor = AppSurfaceTokens.background(),
        modifier = detailShellModifier,
        mainContent = { VideoDetailRouteSheetMainContent() },
        overlayContent = { VideoDetailRouteSheetOverlayContent() }
    )
}
