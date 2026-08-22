// 文件路径: feature/video/VideoPlayerSection.kt
package com.android.purebilibili.feature.video.ui.section
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.feature.video.danmaku.DanmakuManager
import com.android.purebilibili.feature.video.danmaku.DanmakuCloudSyncUiState
import com.android.purebilibili.feature.video.danmaku.rememberDanmakuManager
import com.android.purebilibili.feature.video.danmaku.resolveDanmakuCloudSyncStateAfterQueued
import com.android.purebilibili.feature.video.danmaku.resolveDanmakuCloudSyncStateAfterResult
import com.android.purebilibili.feature.video.danmaku.resolveDanmakuCloudSyncStateAfterStarted
import com.android.purebilibili.feature.video.danmaku.shouldRunDanmakuManualCloudSync
import com.android.purebilibili.feature.video.danmaku.filterVisibleCommandDanmakuItems
import com.android.purebilibili.feature.video.danmaku.configureAsPassiveDanmakuOverlay
import com.android.purebilibili.feature.video.player.MiniPlayerManager
import com.android.purebilibili.feature.video.state.VideoPlayerState
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackUiState
import com.android.purebilibili.feature.video.ui.overlay.FullscreenDoubleTapAction
import com.android.purebilibili.feature.video.ui.overlay.VideoPlayerOverlay
import com.android.purebilibili.feature.video.ui.overlay.SubtitleControlCallbacks
import com.android.purebilibili.feature.video.ui.overlay.SubtitleControlUiState
import com.android.purebilibili.feature.video.ui.overlay.nextFullscreenSeekFeedbackEvent
import com.android.purebilibili.feature.video.ui.overlay.resolveFullscreenDoubleTapAction
import com.android.purebilibili.feature.video.ui.overlay.resolveBottomControlBarLayoutPolicy
import com.android.purebilibili.feature.video.ui.overlay.resolveVideoProgressBarLayoutPolicy
import com.android.purebilibili.feature.video.ui.overlay.resolveLandscapeEndDrawerReservedWidthDp
import com.android.purebilibili.feature.video.ui.overlay.resolveLandscapeEndDrawerLayoutPolicy
import com.android.purebilibili.feature.video.ui.overlay.VIDEO_STATUS_BAR_AMBIENT_CAPTURE_INTERVAL_MS
import com.android.purebilibili.feature.video.ui.overlay.VIDEO_STATUS_BAR_AMBIENT_SAMPLE_HEIGHT_PX
import com.android.purebilibili.feature.video.ui.overlay.VIDEO_STATUS_BAR_AMBIENT_SAMPLE_WIDTH_PX
import com.android.purebilibili.feature.video.ui.components.SponsorSkipButton
import com.android.purebilibili.feature.video.ui.components.SponsorContributionOverlay
import com.android.purebilibili.feature.video.viewmodel.SponsorContributionUiState
import com.android.purebilibili.feature.video.ui.components.TwoFingerSpeedFeedbackOverlay
import com.android.purebilibili.feature.video.ui.components.VideoAspectRatio
import com.android.purebilibili.feature.video.ui.components.GesturePercentTransitionDirection
import com.android.purebilibili.feature.video.ui.components.resolveGesturePercentTransitionDirection
import com.android.purebilibili.feature.video.ui.components.shouldTriggerGesturePercentHaptic
import com.android.purebilibili.feature.video.ui.components.applyPlayerViewResizeMode
import com.android.purebilibili.feature.video.ui.components.resolveSafeVideoAspectRatio
import com.android.purebilibili.feature.video.ui.components.resolveVideoViewportLayout
import com.android.purebilibili.feature.video.ui.components.schedulePlayerViewViewportRefresh
import com.android.purebilibili.feature.video.ui.components.shouldUseFillMaxPlayerViewport
import com.android.purebilibili.feature.video.ui.components.toFullscreenAspectRatio
import com.android.purebilibili.feature.video.ui.components.toVideoAspectRatio
import com.android.purebilibili.feature.video.ui.gesture.GestureLevelOverlayHost
import com.android.purebilibili.feature.video.ui.gesture.LockedTwoFingerSpeedAxis
import com.android.purebilibili.feature.video.ui.gesture.TwoFingerSpeedGestureMode
import com.android.purebilibili.feature.video.ui.gesture.resolveGestureLevelIcon
import com.android.purebilibili.feature.video.ui.gesture.resolveGestureLevelKind
import com.android.purebilibili.feature.video.ui.gesture.resolveGestureLevelOverlayStyle
import com.android.purebilibili.feature.video.ui.gesture.resolveLockedTwoFingerSpeedAxis
import com.android.purebilibili.feature.video.ui.gesture.resolveTwoFingerGesturePlaybackSpeed
import com.android.purebilibili.feature.video.ui.gesture.resolveTwoFingerSpeedGestureMode
import com.android.purebilibili.feature.video.playback.policy.resolveDisplayedQualityId
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.data.model.response.ViewPoint
import com.android.purebilibili.core.video.progress.PbpProgressData
import com.android.purebilibili.core.video.progress.buildPbpRidgeSamples
import com.bytedance.danmaku.render.engine.DanmakuView

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.media.AudioManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
// 🌈 Material Icons Extended - 亮度图标
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.media3.common.Player
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.VideoSize
import androidx.media3.ui.PlayerView
import com.android.purebilibili.core.store.FullscreenAspectRatio
import com.android.purebilibili.core.ui.rememberAppPlayerChromeProfile
import com.android.purebilibili.core.ui.performance.TrackJankStateFlag
import com.android.purebilibili.core.ui.performance.TrackJankStateValue
import com.android.purebilibili.core.ui.blur.unifiedBlur
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_COVER_ASPECT_RATIO
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoSharedCoverCacheKey
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionPlaybackIntent
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionSourceCornerDp
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionVisualSpec
import com.android.purebilibili.core.ui.transition.videoSharedElementBoundsTransformSpec
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.Logger
import com.android.purebilibili.core.util.rememberHapticFeedback
import com.android.purebilibili.feature.screenshot.AppScreenshotGestureBlockState
import com.android.purebilibili.core.video.subtitle.SubtitleDisplayMode
import com.android.purebilibili.core.video.subtitle.SubtitleAutoPreference
import com.android.purebilibili.core.video.subtitle.buildSubtitleTrackOptions
import com.android.purebilibili.feature.video.subtitle.isSubtitleFeatureEnabledForUser
import com.android.purebilibili.core.video.subtitle.normalizeSubtitleDisplayMode
import com.android.purebilibili.core.video.subtitle.normalizeSubtitleVerticalOffsetFraction
import com.android.purebilibili.core.video.subtitle.resolveDefaultSubtitleDisplayMode
import com.android.purebilibili.core.video.subtitle.resolveSubtitleControlAvailability
import com.android.purebilibili.core.video.subtitle.resolveSubtitleDisplayModeByAutoPreference
import com.android.purebilibili.core.video.subtitle.resolveSubtitleTextAt
import com.android.purebilibili.core.video.subtitle.resolveSubtitleTrackDisplayLabel
import com.android.purebilibili.core.video.subtitle.shouldRenderPrimarySubtitle
import com.android.purebilibili.core.video.subtitle.shouldRenderSecondarySubtitle
import com.android.purebilibili.feature.video.usecase.playPlayerFromUserAction
import com.android.purebilibili.feature.video.usecase.seekPlayerFromUserAction
import com.android.purebilibili.feature.video.usecase.togglePlayerPlaybackFromUserAction
import com.android.purebilibili.feature.video.util.captureAndSaveVideoScreenshot
import com.android.purebilibili.feature.video.util.captureVideoAmbientFrame
import com.android.purebilibili.feature.video.playback.session.PlaybackSeekSessionState
import com.android.purebilibili.feature.video.playback.session.SEEK_PLAYBACK_RECOVERY_DELAY_MS
import com.android.purebilibili.feature.video.playback.session.shouldAttemptPlaybackRecoveryAfterSeek
import com.android.purebilibili.feature.video.playback.session.cancelPlaybackSeekInteraction
import com.android.purebilibili.feature.video.playback.session.commitPlaybackSeekInteraction
import com.android.purebilibili.feature.video.playback.session.finishPlaybackSeekInteraction
import com.android.purebilibili.feature.video.playback.session.resetPlaybackSeekSessionForActivePlayback
import com.android.purebilibili.feature.video.playback.session.startPlaybackSeekInteraction
import com.android.purebilibili.feature.video.playback.session.syncPlaybackSeekSession
import com.android.purebilibili.feature.video.playback.session.updatePlaybackSeekInteraction
import dev.chrisbanes.haze.HazeState
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel

@Composable
private fun GesturePercentDigit(
    digit: Char?,
    shouldAnimate: Boolean,
    transitionDirection: GesturePercentTransitionDirection,
    textStyle: TextStyle,
    textShadow: Shadow,
    slotWidth: androidx.compose.ui.unit.Dp,
    motionSpec: VideoGestureMotionSpec
) {
    val blurAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(digit, shouldAnimate) {
        if (!initialized) {
            initialized = true
            return@LaunchedEffect
        }
        if (!shouldAnimate || digit == null) {
            blurAnim.snapTo(0f)
            alphaAnim.snapTo(1f)
            return@LaunchedEffect
        }
        blurAnim.snapTo(motionSpec.digitInitialBlurRadiusDp)
        alphaAnim.snapTo(motionSpec.digitInitialAlpha)
        launch {
            blurAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = motionSpec.digitBlurResetDurationMillis,
                    delayMillis = motionSpec.digitBlurHoldDurationMillis,
                    easing = AppMotionEasing.EmphasizedEnter
                )
            )
        }
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = motionSpec.digitAlphaResetDurationMillis,
                easing = AppMotionEasing.EmphasizedEnter
            )
        )
    }

    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            val enterOffset: (Int) -> Int = { height ->
                when (transitionDirection) {
                    GesturePercentTransitionDirection.Increase -> height / 2
                    GesturePercentTransitionDirection.Decrease -> -height / 2
                    GesturePercentTransitionDirection.None -> 0
                }
            }
            val exitOffset: (Int) -> Int = { height ->
                when (transitionDirection) {
                    GesturePercentTransitionDirection.Increase -> -height / 2
                    GesturePercentTransitionDirection.Decrease -> height / 2
                    GesturePercentTransitionDirection.None -> 0
                }
            }
            (slideInVertically(
                animationSpec = spring(
                    dampingRatio = motionSpec.digitSlideSpringDampingRatio,
                    stiffness = motionSpec.digitSlideSpringStiffness
                ),
                initialOffsetY = enterOffset
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = motionSpec.digitEnterFadeDurationMillis,
                    easing = AppMotionEasing.EmphasizedEnter
                )
            ))
                .togetherWith(
                    slideOutVertically(
                        animationSpec = spring(
                            dampingRatio = motionSpec.digitSlideSpringDampingRatio,
                            stiffness = motionSpec.digitSlideSpringStiffness
                        ),
                        targetOffsetY = exitOffset
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = motionSpec.digitExitFadeDurationMillis,
                            easing = AppMotionEasing.EmphasizedExit
                        )
                    )
                )
        },
        label = "gesture-percent-digit"
    ) { target ->
        if (target == null) {
            Spacer(modifier = Modifier.width(slotWidth))
        } else {
            AppText(
                text = target.toString(),
                color = Color.White,
                style = textStyle.copy(shadow = textShadow),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(slotWidth)
                    .graphicsLayer { alpha = alphaAnim.value }
                    .blur(
                        radius = blurAnim.value.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                    )
            )
        }
    }
}

@Composable
private fun GesturePercentValue(
    percent: Int,
    previousPercent: Int,
    textStyle: TextStyle,
    textShadow: Shadow,
    modifier: Modifier = Modifier,
    motionSpec: VideoGestureMotionSpec
) {
    val digits = remember(percent) { resolveGesturePercentDigits(percent) }
    val changeMask = remember(previousPercent, percent) {
        resolveGesturePercentDigitChangeMask(previousPercent, percent)
    }
    val transitionDirection = remember(previousPercent, percent) {
        resolveGesturePercentTransitionDirection(previousPercent, percent)
    }
    val haptic = rememberHapticFeedback()

    LaunchedEffect(previousPercent, percent) {
        if (shouldTriggerGesturePercentHaptic(previousPercent, percent)) {
            haptic(HapticType.SELECTION)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        digits.forEachIndexed { index, digit ->
            key(index) {
                GesturePercentDigit(
                    digit = digit,
                    shouldAnimate = changeMask.getOrElse(index) { false },
                    transitionDirection = transitionDirection,
                    textStyle = textStyle,
                    textShadow = textShadow,
                    slotWidth = 16.dp,
                    motionSpec = motionSpec
                )
            }
        }
        AppText(
            text = "%",
            color = Color.White,
            style = textStyle.copy(shadow = textShadow),
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

// 相关推荐/同页切集后新播放器 duration 就绪等待参数：
// 最长等待 4s（20 × 200ms），超时按当前可用值加载（仓库层会回退）。
private const val DANMAKU_DURATION_WAIT_ATTEMPTS = 20
private const val DANMAKU_DURATION_WAIT_INTERVAL_MS = 200L
// Success 状态可能先于 ExoPlayer.setMediaItem 可见；暂停态切合集时等待输出绑定就绪，
// 避免一次性检查 mediaItemCount=0 后永远错过 Surface 重绑。
private const val MEDIA_SWITCH_SURFACE_REBIND_ATTEMPTS = 40
private const val MEDIA_SWITCH_SURFACE_REBIND_INTERVAL_MS = 50L

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerSection(
    playerState: VideoPlayerState,
    uiState: VideoPlaybackUiState,
    isFullscreen: Boolean,
    isInPipMode: Boolean,
    contentTopInset: Dp = 0.dp,
    transitionEnabled: Boolean = true,
    transitionChromeAlphaProvider: () -> Float = { 1f },
    danmakuHostActive: Boolean = true,
    onToggleFullscreen: () -> Unit,
    onQualityChange: (Int) -> Unit,
    onBack: () -> Unit,
    onHomeClick: (() -> Unit)? = null,
    onLandscapeCommentClick: () -> Unit = {},
    landscapeCommentPanelVisible: Boolean = false,
    landscapeCommentPanelOnLeft: Boolean = true,
    onDanmakuInputClick: () -> Unit = {},
    danmakuComposerVisible: Boolean = false,
    onDismissDanmakuComposer: () -> Unit = {},
    onSendDanmakuComposer: (
        message: String,
        color: Int,
        mode: Int,
        fontSize: Int,
        attentionCommand: Boolean
    ) -> Unit = { _, _, _, _, _ -> },
    isSendingDanmakuComposer: Boolean = false,
    danmakuComposerInitialText: String = "",
    danmakuComposerInitialAttentionCommand: Boolean = false,
    danmakuComposerInitialColor: Int = 16777215,
    danmakuComposerInitialMode: Int = 1,
    danmakuComposerInitialFontSize: Int = 25,
    onDanmakuComposerDraftChange: (String, Boolean) -> Unit = { _, _ -> },
    onDanmakuComposerSelectionChange: (Int, Int, Int) -> Unit = { _, _, _ -> },
    // 🔗 [新增] 分享功能
    bvid: String = "",
    coverUrl: String = "",
    /**
     * Stationary list-card Coil request frozen at click. When set, player cover uses this
     * exact URL + cache key + size — never [com.android.purebilibili.core.util.FormatUtils.fixImageUrl].
     */
    stationaryListCoverUrl: String = "",
    stationaryListCoverCacheKey: String = "",
    stationaryListCoverDecodeWidthPx: Int = 0,
    stationaryListCoverDecodeHeightPx: Int = 0,
    /**
     * Shared-element key identity. Prefer route-entry bvid during in-page collection switches so
     * SharedTransition does not rekey the live player surface into a black frame.
     */
    sharedElementBvid: String = "",
    //  实验性功能：双击点赞
    onDoubleTapLike: () -> Unit = {},
    //  空降助手
    sponsorSegment: com.android.purebilibili.data.model.response.SponsorSegment? = null,
    showSponsorSkipButton: Boolean = false,
    onSponsorSkip: () -> Unit = {},
    onSponsorDismiss: () -> Unit = {},
    onSponsorVote: (Int) -> Unit = {},
    sponsorContributionState: SponsorContributionUiState = SponsorContributionUiState(),
    onSponsorContributionMarkBoundary: () -> Unit = {},
    onSponsorContributionCategoryChange: (String) -> Unit = {},
    onSponsorContributionActionTypeChange: (String) -> Unit = {},
    onSponsorContributionSubmit: () -> Unit = {},
    onSponsorContributionCancel: () -> Unit = {},
    //  [新增] 重载视频回调
    onReloadVideo: () -> Unit = {},
    //  [新增] CDN 线路切换
    currentCdnIndex: Int = 0,
    cdnCount: Int = 1,
    cdnLineDiagnostics: List<com.android.purebilibili.feature.plugin.CdnLineDiagnostic> = emptyList(),
    isCdnProbing: Boolean = false,
    onSwitchCdn: () -> Unit = {},
    onSwitchCdnTo: (Int) -> Unit = {},
    onProbeCdnCandidates: () -> Unit = {},
    
    //  [新增] 定时关闭
    sleepTimerMinutes: Int? = null,
    onSleepTimerChange: (Int?) -> Unit = {},
    
    // 🖼️ [新增] 视频预览图数据
    videoshotData: com.android.purebilibili.data.model.response.VideoshotData? = null,
    
    // 📖 [新增] 视频章节数据
    viewPoints: List<ViewPoint> = emptyList(),
    sponsorMarkers: List<com.android.purebilibili.data.model.response.SponsorProgressMarker> = emptyList(),
    pbpProgressData: PbpProgressData? = null,
    onUserSeek: (Long) -> Unit = {},
    
    // 📱 [新增] 竖屏全屏模式
    isVerticalVideo: Boolean = false,
    onPortraitFullscreen: () -> Unit = {},
    isPortraitFullscreen: Boolean = false,
    viewportWidthDpOverride: Int? = null,
    // 📲 [新增] 小窗模式
    // 📲 [新增] 小窗模式
    onPipClick: () -> Unit = {},
    // [New] Codec & Audio Params
    currentCodec: String = "hev1", 
    onCodecChange: (String) -> Unit = {},
    currentSecondCodec: String = "avc1",
    onSecondCodecChange: (String) -> Unit = {},
    currentAudioQuality: Int = -1,
    onAudioQualityChange: (Int) -> Unit = {},
    onPlaybackSpeedChange: (Float) -> Boolean = { false },
    // [New] Audio Language
    onAudioLangChange: (String) -> Unit = {},
    // 👀 [新增] 在线观看人数
    onlineCount: String = "",
    // [New Actions]
    onSaveCover: () -> Unit = {},
    onDownloadAudio: () -> Unit = {},
    // 🔁 [新增] 播放模式
    currentPlayMode: com.android.purebilibili.feature.video.player.PlayMode = com.android.purebilibili.feature.video.player.PlayMode.SEQUENTIAL,
    onPlayModeClick: () -> Unit = {},

    // [新增] 侧边栏抽屉数据与交互
    onRelatedVideoClick: (String, android.os.Bundle?) -> Unit = {_,_ -> },
    relatedVideos: List<com.android.purebilibili.data.model.response.RelatedVideo> = emptyList(),
    ugcSeason: com.android.purebilibili.data.model.response.UgcSeason? = null,
    isFollowed: Boolean = false,
    isLiked: Boolean = false,
    isCoined: Boolean = false,
    isFavorited: Boolean = false,
    onToggleFollow: () -> Unit = {},
    onToggleLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onCoin: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onTriple: () -> Unit = {},  // [新增] 一键三连回调
    onPageSelect: (Int) -> Unit = {},
    hasFavoritePlaylist: Boolean = false,
    onFavoritePlaylistClick: () -> Unit = {},
    forceCoverOnly: Boolean = false,
    preserveCurrentFrameOnFullscreenChange: Boolean = false,
    liveBackPreview: Boolean = false,
    useTextureSurfaceForNavigation: Boolean = false,
    predictiveBackCancelRecoveryGeneration: Int = 0,
    allowLivePlayerSharedElement: Boolean = true,
    sourceRouteForSharedElement: String? = null,
    preserveSourceCardCornerDuringSharedReturn: Boolean = false,
    suppressSubtitleOverlay: Boolean = false,
    subtitleDisplayModePreferenceOverride: SubtitleDisplayMode? = null,
    onSubtitleDisplayModePreferenceOverrideChange: (SubtitleDisplayMode) -> Unit = {},
    onSubtitleTrackSelected: (String) -> Unit = {},
    settings: com.android.purebilibili.feature.video.screen.VideoPlayerSettingsSnapshot =
        com.android.purebilibili.feature.video.screen.resolveDefaultVideoPlayerSettingsSnapshot(),
    settingsActions: com.android.purebilibili.feature.video.screen.VideoPlayerSettingsActions =
        com.android.purebilibili.feature.video.screen.VideoPlayerSettingsActions.NoOp,
) {
    val context = LocalContext.current
    val localDensity = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    val hostLifecycleStarted = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)
    val videoInputFormat by playerState.videoInputFormat.collectAsStateWithLifecycle()
    val videoOutputRouter = remember(playerState.player) { VideoOutputRouter(playerState.player) }
    DisposableEffect(videoOutputRouter) {
        onDispose { videoOutputRouter.release() }
    }
    val configuration = LocalConfiguration.current
    val uiLayoutWidthDp = remember(configuration.screenWidthDp, viewportWidthDpOverride) {
        (viewportWidthDpOverride ?: configuration.screenWidthDp).coerceAtLeast(1)
    }
    val uiLayoutPolicy = remember(uiLayoutWidthDp) {
        resolveVideoPlayerUiLayoutPolicy(
            widthDp = uiLayoutWidthDp
        )
    }
    val bottomControlBarLayoutPolicy = remember(uiLayoutWidthDp) {
        resolveBottomControlBarLayoutPolicy(widthDp = uiLayoutWidthDp)
    }
    val videoProgressBarLayoutPolicy = remember(uiLayoutWidthDp) {
        resolveVideoProgressBarLayoutPolicy(widthDp = uiLayoutWidthDp)
    }
    val bottomGestureExclusionHeightDp = remember(uiLayoutWidthDp) {
        resolveVideoPlayerBottomGestureExclusionHeightDp(
            controlBarBottomPaddingDp = bottomControlBarLayoutPolicy.bottomPaddingDp,
            progressSpacingDp = bottomControlBarLayoutPolicy.progressSpacingDp,
            progressContainerHeightDp = videoProgressBarLayoutPolicy.baseHeightWithChapterDp,
            controlRowHeightDp = bottomControlBarLayoutPolicy.playButtonSizeDp
        )
    }
    val gestureSeekFallbackDurationMs = remember(uiState) {
        (uiState as? VideoPlaybackUiState.Success)?.videoDurationMs ?: 0L
    }
    val pbpRidgeSamples = remember(pbpProgressData, gestureSeekFallbackDurationMs) {
        pbpProgressData
            ?.let { data ->
                buildPbpRidgeSamples(
                    data = data,
                    durationMs = gestureSeekFallbackDurationMs
                )
            }
            .orEmpty()
    }
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val settingsScope = rememberCoroutineScope()

    val playerInsightMode = settings.playerInsightMode

    val playerInteractionSettings = settings.playerInteractionSettings

    val gestureSensitivity = playerInteractionSettings.gestureSensitivity
    val longPressSpeedHintScale = playerInteractionSettings.longPressSpeedHintScale
    val longPressSpeedHintAlpha = playerInteractionSettings.longPressSpeedHintAlpha

    // 📱 [优化] realResolution 现在从 playerState.videoSize 计算（见下方）
    val doubleTapLikeEnabled = playerInteractionSettings.doubleTapLikeEnabled
    val doubleTapSeekEnabled = playerInteractionSettings.doubleTapSeekEnabled
    val portraitSwipeToFullscreenEnabled = playerInteractionSettings.portraitSwipeToFullscreenEnabled
    val centerSwipeToFullscreenEnabled = playerInteractionSettings.centerSwipeToFullscreenEnabled
    val slideVolumeBrightnessEnabled = playerInteractionSettings.slideVolumeBrightnessEnabled
    val setSystemBrightnessEnabled = playerInteractionSettings.setSystemBrightnessEnabled
    val pipNoDanmakuEnabled = playerInteractionSettings.pipNoDanmakuEnabled
    val seekForwardSeconds = playerInteractionSettings.seekForwardSeconds
    val seekBackwardSeconds = playerInteractionSettings.seekBackwardSeconds
    val inlineSwipeSeekSeconds = playerInteractionSettings.inlineSwipeSeekSeconds
    val fullscreenSwipeSeekSeconds = playerInteractionSettings.fullscreenSwipeSeekSeconds
    val fullscreenSwipeSeekEnabled = playerInteractionSettings.fullscreenSwipeSeekEnabled
    val fullscreenGestureReverse = playerInteractionSettings.fullscreenGestureReverse
    val autoEnterFullscreenEnabled = playerInteractionSettings.autoEnterFullscreenEnabled
    val autoExitFullscreenEnabled = playerInteractionSettings.autoExitFullscreenEnabled
    val autoExitFullscreenMode = playerInteractionSettings.autoExitFullscreenMode
    val allowPlaybackStateAutoFullscreen = remember(configuration.smallestScreenWidthDp) {
        shouldAllowPlaybackStateAutoFullscreen(
            smallestScreenWidthDp = configuration.smallestScreenWidthDp
        )
    }
    val playbackCompletionBehavior = settings.playbackCompletionBehavior
    val willContinueToNextAfterEnd = remember(uiState, playbackCompletionBehavior) {
        val success = uiState as? VideoPlaybackUiState.Success
        if (success == null) {
            false
        } else {
            val pages = success.info.pages
            val currentIndex = pages.indexOfFirst { it.cid == success.info.cid }
            val hasNextPage = pages.size > 1 && currentIndex >= 0 && currentIndex < pages.lastIndex
            val hasUgcSeasonNext = success.info.ugc_season?.let { season ->
                val episodes = season.sections.flatMap { it.episodes }
                if (episodes.isEmpty()) {
                    false
                } else {
                    val idx = episodes.indexOfFirst {
                        it.bvid == success.info.bvid || it.cid == success.info.cid
                    }
                    idx >= 0 && idx < episodes.lastIndex
                }
            } ?: false
            val hasPlaylistNext = com.android.purebilibili.feature.video.player.PlaylistManager
                .isExternalPlaylist.value &&
                com.android.purebilibili.feature.video.player.PlaylistManager.hasNext()
            val completionAdvances = playbackCompletionBehavior !=
                com.android.purebilibili.core.store.PlaybackCompletionBehavior.STOP_AFTER_CURRENT &&
                playbackCompletionBehavior !=
                com.android.purebilibili.core.store.PlaybackCompletionBehavior.REPEAT_ONE
            resolveWillContinuePlaybackAfterCurrentItem(
                pageCount = pages.size,
                currentPageIndex = currentIndex,
                hasUgcSeasonNext = hasUgcSeasonNext,
                hasPlaylistNext = hasPlaylistNext,
                completionAdvancesToNext = completionAdvances || hasNextPage || hasUgcSeasonNext,
            )
        }
    }
    val fixedFullscreenAspectRatio = playerInteractionSettings.fixedFullscreenAspectRatio
    val subtitleAutoPreference = playerInteractionSettings.subtitleAutoPreference
    
    //  [新增] 双击跳转视觉反馈状态
    var seekFeedbackText by remember { mutableStateOf<String?>(null) }
    var seekFeedbackVisible by remember { mutableStateOf(false) }
    var seekFeedbackGeneration by remember { mutableLongStateOf(0L) }
    
    //  [新增] 长按倍速设置和状态
    val longPressSpeed = playerInteractionSettings.longPressSpeed
    val longPressSpeedLockEnabled = playerInteractionSettings.longPressSpeedLockEnabled
    val longPressSpeedHintCloseEnabled = playerInteractionSettings.longPressSpeedHintCloseEnabled
    val longPressSpeedHintHidden = playerInteractionSettings.longPressSpeedHintHidden
    val twoFingerVerticalSpeedEnabled = playerInteractionSettings.twoFingerVerticalSpeedEnabled
    val twoFingerHorizontalSpeedEnabled = playerInteractionSettings.twoFingerHorizontalSpeedEnabled
    val twoFingerSpeedMode = remember(
        twoFingerVerticalSpeedEnabled,
        twoFingerHorizontalSpeedEnabled
    ) {
        resolveTwoFingerSpeedGestureMode(
            verticalEnabled = twoFingerVerticalSpeedEnabled,
            horizontalEnabled = twoFingerHorizontalSpeedEnabled
        )
    }
    val hiResCompatHintShownPersisted = playerInteractionSettings.hiResLongPressCompatHintShown
    var isLongPressing by remember { mutableStateOf(false) }
    var originalPlaybackParameters by remember(bvid) { mutableStateOf(PlaybackParameters.DEFAULT) }
    var effectiveLongPressSpeed by remember { mutableFloatStateOf(longPressSpeed) }
    var longPressSpeedFeedbackVisible by remember { mutableStateOf(false) }
    var longPressSpeedHintDismissed by remember(bvid) { mutableStateOf(false) }
    // 锁定状态不随 bvid 重置：切换合集（bvid 变化 → 播放器重建 → 速度回到
    // 设置播放速度）后仍保持锁定，由下方 LaunchedEffect(observedPlaybackSpeed, …)
    // 在新播放器就绪后自动把锁定倍速写回。
    var longPressSpeedLocked by remember { mutableStateOf(false) }
    var lockedLongPressSpeed by remember { mutableFloatStateOf(1.0f) }
    var longPressSpeedEndedAtMs by remember { mutableLongStateOf(0L) }
    var longPressSpeedStartedAtMs by remember { mutableLongStateOf(0L) }
    var longPressSpeedStartX by remember { mutableFloatStateOf(-1f) }
    var longPressSpeedStartY by remember { mutableFloatStateOf(-1f) }
    val longPressSpeedLockSensitivity = remember(isFullscreen) {
        resolveLongPressSpeedLockSensitivityPolicy(isFullscreen = isFullscreen)
    }
    var isMultiTouchActive by remember { mutableStateOf(false) }
    var twoFingerSpeedFeedbackVisible by remember { mutableStateOf(false) }
    var twoFingerSpeedFeedbackRevision by remember { mutableIntStateOf(0) }
    var twoFingerFeedbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var hasShownHiResCompatHintLocally by remember {
        mutableStateOf(settings.hiResLongPressCompatHintShown)
    }
    val hasShownHiResCompatHint = hiResCompatHintShownPersisted || hasShownHiResCompatHintLocally
    val longPressSpeedLockHintShownPersisted = playerInteractionSettings.longPressSpeedLockHintShown
    var hasShownLongPressSpeedLockHintLocally by remember {
        mutableStateOf(settings.longPressSpeedLockHintShown)
    }
    val hasShownLongPressSpeedLockHint =
        longPressSpeedLockHintShownPersisted || hasShownLongPressSpeedLockHintLocally
    var showLongPressSpeedLockHint by remember { mutableStateOf(false) }
    var longPressSpeedLockHintGeneration by remember { mutableIntStateOf(0) }
    var hasAutoEnteredFullscreen by remember(bvid) { mutableStateOf(false) }
    var previousPlayWhenReady by remember(playerState.player, bvid) {
        mutableStateOf(playerState.player.playWhenReady)
    }

    LaunchedEffect(hiResCompatHintShownPersisted) {
        if (hiResCompatHintShownPersisted) {
            hasShownHiResCompatHintLocally = true
        }
    }

    LaunchedEffect(twoFingerSpeedFeedbackRevision) {
        if (twoFingerSpeedFeedbackRevision > 0) {
            delay(900)
            twoFingerSpeedFeedbackVisible = false
        }
    }
    
    //  [新增] 缓冲状态监听
    var isBuffering by remember { mutableStateOf(false) }
    var bufferingStartedAtMs by remember { mutableLongStateOf(0L) }
    var foregroundRecoveryGeneration by remember { mutableIntStateOf(0) }
    var foregroundRecoveryStartedAtMs by remember { mutableLongStateOf(0L) }
    var foregroundRecoveryStartPositionMs by remember { mutableLongStateOf(0L) }
    var foregroundRecoveryNeedsSurface by remember { mutableStateOf(false) }
    var hasRenderedFirstFrameSinceForegroundRecovery by remember { mutableStateOf(true) }
    var observedPlaybackSpeed by remember(playerState.player) {
        mutableFloatStateOf(playerState.player.playbackParameters.speed)
    }
    // Player 的 playWhenReady/isPlaying 不是 Snapshot 状态：必须镜像进 Compose，
    // 否则合集 halt / 换片后封面与 surface 可见性不会跟着刷新。
    var observedPlayWhenReady by remember(playerState.player) {
        mutableStateOf(playerState.player.playWhenReady)
    }
    var observedIsPlaying by remember(playerState.player) {
        mutableStateOf(playerState.player.isPlaying)
    }
    var keepVideoPlaybackAwake by remember(playerState.player) {
        mutableStateOf(
            shouldKeepVideoPlaybackAwake(
                playWhenReady = playerState.player.playWhenReady,
                isPlaying = playerState.player.isPlaying,
                playbackState = playerState.player.playbackState
            )
        )
    }
    DisposableEffect(playerState.player) {
        fun syncPlayerObservation() {
            val player = playerState.player
            observedPlayWhenReady = player.playWhenReady
            observedIsPlaying = player.isPlaying
            keepVideoPlaybackAwake = shouldKeepVideoPlaybackAwake(
                playWhenReady = player.playWhenReady,
                isPlaying = player.isPlaying,
                playbackState = player.playbackState
            )
        }
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                syncPlayerObservation()
                val now = android.os.SystemClock.elapsedRealtime()
                if (playbackState == Player.STATE_BUFFERING) {
                    if (bufferingStartedAtMs == 0L) {
                        bufferingStartedAtMs = now
                        Logger.d("VideoPlayerSection") {
                            "🎬 Playback buffering started: pos=${playerState.player.currentPosition}, " +
                                "buffered=${playerState.player.bufferedPosition}, playWhenReady=${playerState.player.playWhenReady}"
                        }
                    }
                } else if (bufferingStartedAtMs != 0L) {
                    val bufferingDurationMs = (now - bufferingStartedAtMs).coerceAtLeast(0L)
                    if (shouldLogPlaybackStall(
                            bufferingDurationMs = bufferingDurationMs,
                            playWhenReady = playerState.player.playWhenReady,
                            currentPositionMs = playerState.player.currentPosition
                        )
                    ) {
                        Logger.w(
                            "VideoPlayerSection",
                            "⚠️ Playback stall recovered after ${bufferingDurationMs}ms: " +
                                "state=$playbackState, pos=${playerState.player.currentPosition}, " +
                                "buffered=${playerState.player.bufferedPosition}, speed=${playerState.player.playbackParameters.speed}"
                        )
                    }
                    bufferingStartedAtMs = 0L
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                syncPlayerObservation()
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                syncPlayerObservation()
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                observedPlaybackSpeed = playbackParameters.speed
            }
        }
        playerState.player.addListener(listener)
        // 初始化状态
        isBuffering = playerState.player.playbackState == Player.STATE_BUFFERING
        observedPlaybackSpeed = playerState.player.playbackParameters.speed
        syncPlayerObservation()
        onDispose {
            playerState.player.removeListener(listener)
        }
    }

    LaunchedEffect(observedPlaybackSpeed, longPressSpeedLocked, lockedLongPressSpeed, isLongPressing) {
        if (shouldReapplyLockedLongPressSpeed(
                longPressSpeedLocked = longPressSpeedLocked,
                isLongPressing = isLongPressing,
                observedPlaybackSpeed = observedPlaybackSpeed,
                lockedLongPressSpeed = lockedLongPressSpeed
            )
        ) {
            if (!onPlaybackSpeedChange(lockedLongPressSpeed)) {
                playerState.player.playbackParameters = resolveSpeedSafePlaybackParameters(
                    requestedSpeed = lockedLongPressSpeed,
                    currentAudioQuality = currentAudioQuality
                )
            }
        }
    }

    LaunchedEffect(playerState.player, currentAudioQuality) {
        val currentParameters = playerState.player.playbackParameters
        val safeParameters = resolveSpeedSafePlaybackParameters(
            requestedSpeed = currentParameters.speed,
            currentAudioQuality = currentAudioQuality
        )
        if (
            abs(currentParameters.speed - safeParameters.speed) > 0.001f ||
            abs(currentParameters.pitch - safeParameters.pitch) > 0.001f
        ) {
            playerState.player.playbackParameters = safeParameters
        }
    }

    val latestIsFullscreen by rememberUpdatedState(isFullscreen)
    val latestOnToggleFullscreen by rememberUpdatedState(onToggleFullscreen)
    val latestOnPortraitFullscreen by rememberUpdatedState(onPortraitFullscreen)
    val latestWillContinueToNextAfterEnd by rememberUpdatedState(willContinueToNextAfterEnd)
    val latestAutoExitFullscreenMode by rememberUpdatedState(autoExitFullscreenMode)
    DisposableEffect(
        playerState.player,
        autoEnterFullscreenEnabled,
        autoExitFullscreenEnabled,
        autoExitFullscreenMode,
        allowPlaybackStateAutoFullscreen,
        willContinueToNextAfterEnd,
        bvid
    ) {
        previousPlayWhenReady = playerState.player.playWhenReady
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (shouldToggleAutoFullscreenForPlaybackEvent(
                        autoEnterFullscreenEnabled = autoEnterFullscreenEnabled,
                        autoExitFullscreenEnabled = autoExitFullscreenEnabled,
                        allowPlaybackStateAutoFullscreen = allowPlaybackStateAutoFullscreen,
                        playbackState = playbackState,
                        playWhenReady = playerState.player.playWhenReady,
                        hasAutoEnteredFullscreen = hasAutoEnteredFullscreen,
                        isFullscreen = latestIsFullscreen,
                        previousPlayWhenReady = previousPlayWhenReady,
                        willContinueToNextItem = latestWillContinueToNextAfterEnd,
                        autoExitFullscreenMode = latestAutoExitFullscreenMode,
                    )
                ) {
                    if (
                        playbackState == Player.STATE_READY &&
                        playerState.player.playWhenReady &&
                        !hasAutoEnteredFullscreen &&
                        !latestIsFullscreen
                    ) {
                        hasAutoEnteredFullscreen = true
                    }
                    latestOnToggleFullscreen()
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                val previousValue = previousPlayWhenReady
                previousPlayWhenReady = playWhenReady
                if (shouldToggleAutoFullscreenForPlaybackEvent(
                        autoEnterFullscreenEnabled = autoEnterFullscreenEnabled,
                        autoExitFullscreenEnabled = autoExitFullscreenEnabled,
                        allowPlaybackStateAutoFullscreen = allowPlaybackStateAutoFullscreen,
                        playbackState = playerState.player.playbackState,
                        playWhenReady = playWhenReady,
                        hasAutoEnteredFullscreen = hasAutoEnteredFullscreen,
                        isFullscreen = latestIsFullscreen,
                        previousPlayWhenReady = previousValue,
                        willContinueToNextItem = latestWillContinueToNextAfterEnd,
                        autoExitFullscreenMode = latestAutoExitFullscreenMode,
                    )
                ) {
                    hasAutoEnteredFullscreen = true
                    latestOnToggleFullscreen()
                }
            }
        }
        playerState.player.addListener(listener)
        onDispose {
            playerState.player.removeListener(listener)
        }
    }

    // 📱 [优化] 复用 VideoPlayerState 中的视频尺寸状态，避免重复监听
    val videoSizeState by playerState.videoSize.collectAsStateWithLifecycle()
    val debugInfo by playerState.debugInfo.collectAsStateWithLifecycle()
    val diagnosticEvents by playerState.diagnosticEvents.collectAsStateWithLifecycle()
    val pendingUserAction by playerState.pendingUserAction.collectAsStateWithLifecycle()
    val playerDiagnosticLoggingEnabled = settings.playerDiagnosticLoggingEnabled
    val currentPlaybackIdentity = remember(bvid, uiState) {
        val success = uiState as? VideoPlaybackUiState.Success
        "${bvid}_${success?.info?.cid ?: 0L}"
    }

    // 控制器显示状态
    var showControls by remember(bvid) { mutableStateOf(INITIAL_PLAYER_CONTROLS_VISIBLE) }
    var hasAutoHiddenControlsForCurrentVideo by remember(bvid) {
        mutableStateOf(INITIAL_PLAYER_CHROME_AUTO_HIDE_HANDLED)
    }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var measuredPlayerViewportSize by remember(bvid) { mutableStateOf(IntSize.Zero) }
    var measuredBottomControlsHeightPx by remember(bvid) { mutableIntStateOf(0) }
    val statusBarAmbientFrame = remember(bvid) { mutableStateOf<ImageBitmap?>(null) }
    
    // 🔒 [新增] 屏幕锁定状态（全屏时防误触）
    var isScreenLocked by remember { mutableStateOf(false) }
    LaunchedEffect(isScreenLocked, showControls) {
        if (isScreenLocked && showControls) {
            delay(2_000L)
            showControls = false
        }
    }
    DisposableEffect(isFullscreen, isScreenLocked) {
        val shouldBlockAppScreenshot = isFullscreen && isScreenLocked
        AppScreenshotGestureBlockState.fullscreenPlayerLocked = shouldBlockAppScreenshot
        onDispose {
            if (shouldBlockAppScreenshot) {
                AppScreenshotGestureBlockState.fullscreenPlayerLocked = false
            }
        }
    }

    // 「播放页沉浸状态栏」开启时，实时采样播放画面作为状态栏背景模糊源；
    // 关闭（默认）时背景为纯黑，不采样，零开销。
    val statusBarHazeEnabled = settings.statusBarHazeEnabled
    val shouldCaptureStatusBarAmbientFrame = contentTopInset.value > 0f &&
        !isFullscreen &&
        !isInPipMode &&
        hostLifecycleStarted &&
        statusBarHazeEnabled
    LaunchedEffect(
        playerViewRef,
        shouldCaptureStatusBarAmbientFrame,
        observedIsPlaying,
        currentPlaybackIdentity,
    ) {
        if (!shouldCaptureStatusBarAmbientFrame) {
            statusBarAmbientFrame.value = null
            return@LaunchedEffect
        }
        val playerView = playerViewRef ?: return@LaunchedEffect
        while (isActive) {
            if (playerView.isAttachedToWindow && playerView.width > 0 && playerView.height > 0) {
                statusBarAmbientFrame.value = captureVideoAmbientFrame(
                    playerView = playerView,
                    targetWidth = VIDEO_STATUS_BAR_AMBIENT_SAMPLE_WIDTH_PX,
                    targetHeight = VIDEO_STATUS_BAR_AMBIENT_SAMPLE_HEIGHT_PX,
                )?.asImageBitmap()
            }
            if (!observedIsPlaying) break
            delay(VIDEO_STATUS_BAR_AMBIENT_CAPTURE_INTERVAL_MS)
        }
    }

    var gestureMode by remember { mutableStateOf<VideoGestureMode>(VideoGestureMode.None) }
    var gestureIcon by remember { mutableStateOf<ImageVector?>(null) }
    var gesturePercent by remember { mutableFloatStateOf(0f) }
    val gesturePercentDisplay by remember {
        derivedStateOf { (gesturePercent * 100f).roundToInt().coerceIn(0, 100) }
    }
    var previousGesturePercentDisplay by remember { mutableIntStateOf(gesturePercentDisplay) }
    var orientationHintVisible by remember { mutableStateOf(false) }
    var orientationHintText by remember { mutableStateOf(resolveOrientationSwitchHintText(isFullscreen)) }
    var hasObservedOrientationChange by remember { mutableStateOf(false) }
    val gestureMotionSpec = remember { resolveVideoGestureMotionSpec() }
    val playerChromeProfile = rememberAppPlayerChromeProfile()
    val gestureLevelOverlayStyle = remember(playerChromeProfile.tabPresentation) {
        resolveGestureLevelOverlayStyle(playerChromeProfile.tabPresentation)
    }
    val forceCoverDuringReturnAnimation = shouldForceCoverDuringReturnAnimation(
        forceCoverOnly = forceCoverOnly
    )
    var hasUsedLiveBackPreviewTexture by remember(bvid) { mutableStateOf(false) }
    LaunchedEffect(bvid, liveBackPreview) {
        if (liveBackPreview) {
            // Keep the chosen TextureView for the rest of this detail entry. Switching back to a
            // SurfaceView immediately after landing recreates PlayerView and exposes its cover
            // underlay for one frame. HDR still overrides this choice in the policy below.
            hasUsedLiveBackPreviewTexture = true
        }
    }
    val shouldBindInlinePlayerView = remember(
        isPortraitFullscreen,
        hostLifecycleStarted,
        isInPipMode,
        liveBackPreview
    ) {
        shouldBindInlinePlayerViewToPlayer(
            isPortraitFullscreen = isPortraitFullscreen,
            hostLifecycleStarted = hostLifecycleStarted,
            isInPipMode = isInPipMode,
            liveBackPreview = liveBackPreview
        )
    }
    // In-page collection switches replace the player/router while reusing the same PlayerView.
    // Key the effect by the router so the new player always receives the existing video surface.
    LaunchedEffect(
        videoOutputRouter,
        playerViewRef,
        shouldBindInlinePlayerView
    ) {
        videoOutputRouter.update(
            playerView = playerViewRef,
            shouldBindDirectPlayerView = shouldBindInlinePlayerView
        )
    }

    // 进度手势相关状态
    var seekTargetTime by remember { mutableLongStateOf(0L) }
    var lastSeekHapticTargetMs by remember { mutableLongStateOf(0L) }
    var startPosition by remember { mutableLongStateOf(0L) }
    val currentSeekSessionCid = (uiState as? VideoPlaybackUiState.Success)?.info?.cid ?: 0L
    var sharedSeekSession by remember(bvid, currentSeekSessionCid) {
        mutableStateOf(
            syncPlaybackSeekSession(
                state = PlaybackSeekSessionState(),
                playbackPositionMs = playerState.player.currentPosition.coerceAtLeast(0L)
            )
        )
    }
    var isGestureVisible by remember { mutableStateOf(false) }
    TrackJankStateFlag(
        stateName = "video_player:gesture_visible",
        isActive = isGestureVisible
    )
    TrackJankStateValue(
        stateName = "video_player:gesture_mode",
        stateValue = gestureMode.takeUnless { it == VideoGestureMode.None }?.name
    )
    
    //  视频比例状态
    var currentAspectRatio by remember {
        mutableStateOf(
            resolveSafeVideoAspectRatio(
                preferred = fixedFullscreenAspectRatio.toVideoAspectRatio(),
                isVerticalVideo = isVerticalVideo
            )
        )
    }
    
    //  [新增] 视频翻转状态
    var isFlippedHorizontal by remember { mutableStateOf(false) }
    var isFlippedVertical by remember { mutableStateOf(false) }

    // 记录手势开始时的初始值
    var startVolumeStep by remember { mutableIntStateOf(0) }
    var startBrightness by remember { mutableFloatStateOf(0f) }

    // 记录累计拖动距离
    var totalDragDistanceY by remember { mutableFloatStateOf(0f) }
    var totalDragDistanceX by remember { mutableFloatStateOf(0f) }
    // 记录手势起点 X（用于锁定分区，避免拖动过程横向漂移导致误判）
    var dragStartX by remember { mutableFloatStateOf(-1f) }

    var subtitleVerticalOffsetFraction by rememberSaveable(bvid) {
        mutableFloatStateOf(playerInteractionSettings.subtitleVerticalOffsetFraction)
    }
    var isDraggingSubtitleOffset by remember { mutableStateOf(false) }

    LaunchedEffect(playerInteractionSettings.subtitleVerticalOffsetFraction, bvid) {
        if (!isDraggingSubtitleOffset) {
            subtitleVerticalOffsetFraction = playerInteractionSettings.subtitleVerticalOffsetFraction
        }
    }

    LaunchedEffect(playerState.player, bvid, currentSeekSessionCid) {
        while (isActive) {
            sharedSeekSession = syncPlaybackSeekSession(
                state = sharedSeekSession,
                playbackPositionMs = playerState.player.currentPosition.coerceAtLeast(0L),
                hasPlaybackResumedAfterPendingSeek = playerState.player.isPlaying
            )
            delay(200)
        }
    }

    LaunchedEffect(
        sharedSeekSession.pendingSeekPositionMs,
        playerState.player.playWhenReady,
        playerState.player.isPlaying,
        playerState.player.playbackState
    ) {
        if (!shouldAttemptPlaybackRecoveryAfterSeek(
                state = sharedSeekSession,
                playWhenReady = playerState.player.playWhenReady,
                isPlaying = playerState.player.isPlaying,
                playbackState = playerState.player.playbackState
            )
        ) {
            return@LaunchedEffect
        }

        delay(SEEK_PLAYBACK_RECOVERY_DELAY_MS)
        val player = playerState.player
        if (!shouldAttemptPlaybackRecoveryAfterSeek(
                state = sharedSeekSession,
                playWhenReady = player.playWhenReady,
                isPlaying = player.isPlaying,
                playbackState = player.playbackState
            )
        ) {
            return@LaunchedEffect
        }

        if (player.playbackState == Player.STATE_IDLE && player.mediaItemCount > 0) {
            player.prepare()
        }
        player.playWhenReady = true
        player.play()
        Logger.d("VideoPlayerSection") {
            "▶️ Seek recovery kicked playback: state=${player.playbackState}, " +
                "playWhenReady=${player.playWhenReady}, playing=${player.isPlaying}, pos=${player.currentPosition}"
        }
    }

    fun getActivity(): Activity? = when (context) {
        is Activity -> context
        is ContextWrapper -> context.baseContext as? Activity
        else -> null
    }

    //  [新增] 缩放和平移状态
    var scale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(fixedFullscreenAspectRatio, isVerticalVideo) {
        currentAspectRatio = resolveSafeVideoAspectRatio(
            preferred = fixedFullscreenAspectRatio.toVideoAspectRatio(),
            isVerticalVideo = isVerticalVideo
        )
    }

    // Changing forced aspect ratio invalidates free pinch/pan offsets from the prior frame.
    LaunchedEffect(currentAspectRatio) {
        scale = 1f
        panX = 0f
        panY = 0f
    }
    // 上滑/按钮进全屏也必须清掉 free-form 缩放，否则残留 scale 会造成右/下黑边。
    LaunchedEffect(isFullscreen, isPortraitFullscreen) {
        scale = 1f
        panX = 0f
        panY = 0f
    }

    DisposableEffect(Unit) {
        onDispose { playerViewRef = null }
    }

    LaunchedEffect(gesturePercentDisplay) {
        if (gesturePercentDisplay != previousGesturePercentDisplay) {
            previousGesturePercentDisplay = gesturePercentDisplay
        }
    }

    LaunchedEffect(showLongPressSpeedLockHint, isLongPressing, longPressSpeedLockHintGeneration) {
        if (showLongPressSpeedLockHint && !isLongPressing) {
            delay(5_000L)
            showLongPressSpeedLockHint = false
        }
    }

    // [新增] 共享元素过渡支持
    val sharedTransitionScope = com.android.purebilibili.core.ui.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    //  共享弹幕管理器（用于所有 seek 路径的一致同步）
    val danmakuManager = rememberDanmakuManager()
    val overlayDrawerHazeState = com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState()
    var showEndDrawer by remember { mutableStateOf(false) }
    var endDrawerInitialTab by remember { mutableIntStateOf(0) }
    val endDrawerReservedWidthDp = resolveLandscapeEndDrawerReservedWidthDp(
        drawerVisible = showEndDrawer,
        isFullscreen = isFullscreen,
        screenWidthDp = configuration.screenWidthDp
    )
    val animatedEndDrawerReservedWidth by animateDpAsState(
        targetValue = endDrawerReservedWidthDp.dp,
        animationSpec = tween(durationMillis = 220),
        label = "landscape_end_drawer_reserved_width"
    )
    val landscapeCommentReservedWidthDp = remember(
        isFullscreen,
        landscapeCommentPanelVisible,
        configuration.screenWidthDp,
    ) {
        if (isFullscreen && landscapeCommentPanelVisible) {
            resolveLandscapeEndDrawerLayoutPolicy(configuration.screenWidthDp).drawerWidthDp
        } else {
            0
        }
    }
    val animatedLandscapeCommentReservedWidth by animateDpAsState(
        targetValue = landscapeCommentReservedWidthDp.dp,
        animationSpec = tween(durationMillis = 220),
        label = "landscape_comment_panel_reserved_width",
    )

    fun commitExplicitSeek(positionMs: Long) {
        val commitResult = commitPlaybackSeekInteraction(
            state = sharedSeekSession,
            player = playerState.player,
            positionMs = positionMs
        )
        sharedSeekSession = commitResult.state
        seekPlayerFromUserAction(
            player = playerState.player,
            positionMs = commitResult.committedPositionMs,
            shouldResumePlaybackOverride = commitResult.shouldResumePlayback
        )
        danmakuManager.seekTo(commitResult.committedPositionMs)
        onUserSeek(commitResult.committedPositionMs)
    }

    fun applyLongPressPlaybackParameters(parameters: PlaybackParameters) {
        // 长按倍速是临时手势；不要走手动改倍速的音轨兼容刷新通路，避免按下/松开时重建播放源。
        playerState.player.playbackParameters = parameters
    }

    fun startLongPressSpeedGesture(startOffset: Offset? = null) {
        if (
            !shouldEnableLongPressSpeedGesture(
                isScreenLocked = isScreenLocked,
                scale = scale,
                isMultiTouchActive = isMultiTouchActive
            )
        ) {
            return
        }
        val player = playerState.player
        val startDecision = resolveLongPressSpeedStartDecision(
            currentPlaybackParameters = player.playbackParameters,
            previousOriginalPlaybackParameters = originalPlaybackParameters,
            longPressSpeedLocked = longPressSpeedLocked,
            requestedSpeed = longPressSpeed,
            currentAudioQuality = currentAudioQuality
        )
        originalPlaybackParameters = startDecision.originalPlaybackParameters
        if (startDecision.clearExistingLock) {
            longPressSpeedLocked = false
        }
        effectiveLongPressSpeed = startDecision.targetPlaybackParameters.speed
        applyLongPressPlaybackParameters(startDecision.targetPlaybackParameters)
        if (!longPressSpeedLockEnabled && !hasShownLongPressSpeedLockHint) {
            hasShownLongPressSpeedLockHintLocally = true
            showLongPressSpeedLockHint = true
            longPressSpeedLockHintGeneration += 1
            settingsScope.launch {
                settingsActions.setLongPressSpeedLockHintShown(true)
            }
        }
        if (
            shouldShowHiResLongPressCompatHint(
                requestedSpeed = longPressSpeed,
                effectiveSpeed = effectiveLongPressSpeed,
                hasShownHint = hasShownHiResCompatHint
            )
        ) {
            hasShownHiResCompatHintLocally = true
            Toast.makeText(
                context,
                "当前音源已降低长按倍速，以降低失真",
                Toast.LENGTH_SHORT
            ).show()
            settingsScope.launch {
                settingsActions.setHiResLongPressCompatHintShown(true)
            }
        }
        isLongPressing = true
        totalDragDistanceY = 0f
        totalDragDistanceX = 0f
        longPressSpeedStartedAtMs = android.os.SystemClock.elapsedRealtime()
        longPressSpeedStartX = startOffset?.x ?: -1f
        longPressSpeedStartY = startOffset?.y ?: -1f
        longPressSpeedHintDismissed = false
        longPressSpeedFeedbackVisible = true
        gestureMode = VideoGestureMode.None
        isGestureVisible = false
        dragStartX = -1f
        sharedSeekSession = resetPlaybackSeekSessionForActivePlayback(
            state = sharedSeekSession,
            playbackPositionMs = player.currentPosition
        )
        com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
            "⏩ LongPress: speed ${effectiveLongPressSpeed}x (requested=${longPressSpeed}x, audio=$currentAudioQuality)"
        }
    }

    fun unlockLockedLongPressSpeedFromGesture() {
        if (!longPressSpeedLocked) return
        longPressSpeedLocked = false
        lockedLongPressSpeed = originalPlaybackParameters.speed
        applyLongPressPlaybackParameters(originalPlaybackParameters)
        isLongPressing = false
        longPressSpeedFeedbackVisible = false
        longPressSpeedEndedAtMs = android.os.SystemClock.elapsedRealtime()
        totalDragDistanceY = 0f
        totalDragDistanceX = 0f
        longPressSpeedStartedAtMs = 0L
        longPressSpeedStartX = -1f
        longPressSpeedStartY = -1f
        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        Toast.makeText(
            context,
            "已解除倍速锁定",
            Toast.LENGTH_SHORT
        ).show()
        com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
            "🔓 LongPress unlocked: speed ${originalPlaybackParameters.speed}x"
        }
    }

    fun finishLongPressSpeedGesture(gestureEnded: Boolean) {
        if (!isLongPressing) return
        if (
            shouldRestorePlaybackParametersAfterLongPressRelease(
                wasLongPressing = isLongPressing,
                longPressSpeedLocked = longPressSpeedLocked,
                gestureEnded = gestureEnded
            )
        ) {
            applyLongPressPlaybackParameters(originalPlaybackParameters)
        }
        isLongPressing = false
        longPressSpeedFeedbackVisible = false
        longPressSpeedEndedAtMs = android.os.SystemClock.elapsedRealtime()
        totalDragDistanceY = 0f
        totalDragDistanceX = 0f
        longPressSpeedStartedAtMs = 0L
        longPressSpeedStartX = -1f
        longPressSpeedStartY = -1f
        if (gestureMode != VideoGestureMode.Seek) {
            gestureMode = VideoGestureMode.None
            isGestureVisible = false
            dragStartX = -1f
            sharedSeekSession = resetPlaybackSeekSessionForActivePlayback(
                state = sharedSeekSession,
                playbackPositionMs = playerState.player.currentPosition
            )
        }
        com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
            if (longPressSpeedLocked) {
                "🔒 LongPress locked: speed ${lockedLongPressSpeed}x"
            } else {
                "⏹️ LongPress released: speed ${originalPlaybackParameters.speed}x"
            }
        }
    }

    // 换集/换片后收口侧栏与手势中间态，避免全屏遮罩或 multi-touch 标志卡住导致触摸无响应。
    LaunchedEffect(currentPlaybackIdentity) {
        showEndDrawer = false
        isScreenLocked = false
        isMultiTouchActive = false
        gestureMode = VideoGestureMode.None
        isGestureVisible = false
        scale = 1f
        panX = 0f
        panY = 0f
        if (isLongPressing || longPressSpeedLocked) {
            finishLongPressSpeedGesture(gestureEnded = true)
        }
    }

    fun applyExplicitPlaybackSpeedChange(speed: Float) {
        if (
            shouldClearLockedLongPressSpeedForExplicitSpeedChange(
                longPressSpeedLocked = longPressSpeedLocked,
                isLongPressing = isLongPressing
            )
        ) {
            longPressSpeedLocked = false
            lockedLongPressSpeed = speed
        }
        if (!onPlaybackSpeedChange(speed)) {
            playerState.player.playbackParameters = resolveSpeedSafePlaybackParameters(
                requestedSpeed = speed,
                currentAudioQuality = currentAudioQuality
            )
        }
    }

    var rootModifier = Modifier
        .fillMaxSize()
        .clipToBounds()
        .background(Color.Black)
        .hazeSourceCompat(overlayDrawerHazeState)
    val playerContentModifier = Modifier
        .fillMaxSize()
        .padding(top = contentTopInset)
        .padding(
            start = if (landscapeCommentPanelOnLeft) animatedLandscapeCommentReservedWidth else 0.dp,
            end = animatedEndDrawerReservedWidth +
                if (landscapeCommentPanelOnLeft) 0.dp else animatedLandscapeCommentReservedWidth,
        )

    // HDR 下 SurfaceView 不能参与 Compose sharedElement；实时 morph 仅 SDR TextureView 路径。
    val navigationHdrSurfaceRequired = requiresHdrSurfaceOutput(
        currentQualityId = (uiState as? VideoPlaybackUiState.Success)?.currentQuality ?: 0,
        colorTransfer = videoInputFormat?.colorInfo?.colorTransfer ?: 0
    )
    // 应用共享元素
    val livePlayerSharedElementEnabled = shouldEnableLivePlayerSharedElement(
            transitionEnabled = transitionEnabled,
            allowLivePlayerSharedElement = allowLivePlayerSharedElement,
            hasSharedTransitionScope = sharedTransitionScope != null,
            hasAnimatedVisibilityScope = animatedVisibilityScope != null,
            forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
            requiresHdrSurfaceOutput = navigationHdrSurfaceRequired
        )
    val resolvedSharedElementBvid = sharedElementBvid.trim().ifBlank { bvid }
    if (resolvedSharedElementBvid.isNotEmpty() && livePlayerSharedElementEnabled) {
         with(requireNotNull(sharedTransitionScope)) {
             rootModifier = rootModifier.sharedElement(
                 sharedContentState = rememberSharedContentState(
                     key = com.android.purebilibili.core.ui.transition.videoPlayerSharedElementKey(
                         resolvedSharedElementBvid
                     )
                 ),
                 animatedVisibilityScope = requireNotNull(animatedVisibilityScope),
                 boundsTransform = { _, _ ->
                     com.android.purebilibili.core.ui.motion.AppMotionTokens.spatialSpec()
                 }
             )
         }
    }

    Box(
        modifier = rootModifier
            //  [新增] 处理双指缩放/平移，并在全屏时支持双指调倍速
            .pointerInput(playerState.player, isFullscreen, isInPipMode, isScreenLocked, twoFingerSpeedMode) {
                try {
                    awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var totalPanX = 0f
                    var totalPanY = 0f
                    var lockedAxis: LockedTwoFingerSpeedAxis? = null
                    var gestureStartSpeed = playerState.player.playbackParameters.speed
                    val directionThresholdPx = viewConfiguration.touchSlop * 1.5f
                    var observedMultiTouch = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedCount = event.changes.count { it.pressed }
                        if (pressedCount == 0) {
                            isMultiTouchActive = false
                            break
                        }
                        if (pressedCount < 2) {
                            if (observedMultiTouch) {
                                isMultiTouchActive = false
                                break
                            }
                            continue
                        }
                        observedMultiTouch = true
                        isMultiTouchActive = true

                        if (isLongPressing) {
                            finishLongPressSpeedGesture(gestureEnded = true)
                        }

                        val pan = event.calculatePan()
                        val zoom = event.calculateZoom()
                        totalPanX += pan.x
                        totalPanY += pan.y

                        val speedModeAllowed = isFullscreen &&
                            !isInPipMode &&
                            !isScreenLocked &&
                            twoFingerSpeedMode != TwoFingerSpeedGestureMode.Off

                        if (speedModeAllowed && lockedAxis == null) {
                            lockedAxis = resolveLockedTwoFingerSpeedAxis(
                                mode = twoFingerSpeedMode,
                                totalDragX = totalPanX,
                                totalDragY = totalPanY,
                                thresholdPx = directionThresholdPx
                            )
                            if (lockedAxis != null) {
                                gestureStartSpeed = playerState.player.playbackParameters.speed
                                showControls = false
                                isGestureVisible = false
                            }
                        }

                        if (speedModeAllowed && lockedAxis != null) {
                            val resolvedSpeed = resolveTwoFingerGesturePlaybackSpeed(
                                startSpeed = gestureStartSpeed,
                                mode = twoFingerSpeedMode,
                                totalDragX = totalPanX,
                                totalDragY = totalPanY,
                                containerWidthPx = size.width.toFloat(),
                                containerHeightPx = size.height.toFloat()
                            )
                            val effectiveSpeed = resolveEffectivePlaybackSpeed(
                                requestedSpeed = resolvedSpeed,
                                currentAudioQuality = currentAudioQuality
                            )
                            if (abs(playerState.player.playbackParameters.speed - effectiveSpeed) > 0.001f) {
                                applyExplicitPlaybackSpeedChange(resolvedSpeed)
                            }
                            twoFingerFeedbackSpeed = effectiveSpeed
                            twoFingerSpeedFeedbackVisible = true
                            twoFingerSpeedFeedbackRevision++
                            event.changes.forEach { change ->
                                if (change.position != change.previousPosition) {
                                    change.consume()
                                }
                            }
                            continue
                        }

                        if (
                            shouldEnableViewportTransformGesture(
                                isScreenLocked = isScreenLocked
                            ) && (zoom != 1f || pan != Offset.Zero)
                        ) {
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            if (scale > 1f) {
                                val maxPanX = (size.width * scale - size.width) / 2
                                val maxPanY = (size.height * scale - size.height) / 2
                                panX = (panX + pan.x * scale).coerceIn(-maxPanX, maxPanX)
                                panY = (panY + pan.y * scale).coerceIn(-maxPanY, maxPanY)
                                isGestureVisible = false
                                showControls = false
                            } else {
                                panX = 0f
                                panY = 0f
                            }

                            event.changes.forEach { change ->
                                if (change.position != change.previousPosition) {
                                    change.consume()
                                }
                            }
                        }
                    }
                    }
                } finally {
                    isMultiTouchActive = false
                }
            }
            //  先处理拖拽手势 (音量/亮度/进度)
            .pointerInput(
                playerState.player,
                isInPipMode,
                isScreenLocked,
                isFullscreen,
                isVerticalVideo,
                showControls,
                portraitSwipeToFullscreenEnabled,
                centerSwipeToFullscreenEnabled,
                slideVolumeBrightnessEnabled,
                fullscreenSwipeSeekEnabled,
                gestureSensitivity,
                inlineSwipeSeekSeconds,
                fullscreenSwipeSeekSeconds,
                fullscreenGestureReverse,
                bottomGestureExclusionHeightDp,
                gestureSeekFallbackDurationMs
            ) {
                if (!isInPipMode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // [新增] 如果处于缩放状态，禁用常规拖拽手势，优先处理平移
                            if (scale > 1.01f) {  // 留一点浮点数buffer
                                return@detectDragGestures
                            }
                            
                            // 🔒 锁定时禁用拖拽手势
                            if (isScreenLocked) {
                                return@detectDragGestures
                            }
                            if (isLongPressing || longPressSpeedLocked) {
                                return@detectDragGestures
                            }
                            //  [新增] 边缘防误触检测
                            //  如果在屏幕顶部或底部区域开始滑动，则视为系统手势（如下拉通知栏），不触发播放器手势
                            val requestedBottomGestureExclusionPx = if (showControls) {
                                with(localDensity) { bottomGestureExclusionHeightDp.dp.toPx() }
                            } else {
                                0f
                            }
                            val gestureExclusions = resolveVideoPlayerGestureVerticalExclusions(
                                containerHeightPx = size.height.toFloat(),
                                isFullscreen = isFullscreen,
                                controlsVisible = showControls,
                                requestedBottomControlsExclusionPx = requestedBottomGestureExclusionPx,
                                inlineTopExclusionPx = with(localDensity) { 24.dp.toPx() },
                                inlineBottomExclusionPx = with(localDensity) { 48.dp.toPx() },
                                fullscreenEdgeExclusionPx = with(localDensity) { 48.dp.toPx() }
                            )
                            val shouldIgnoreDragStart = shouldIgnoreVideoPlayerDragStart(
                                offsetY = offset.y,
                                containerHeightPx = size.height.toFloat(),
                                topGestureExclusionPx = gestureExclusions.topPx,
                                bottomGestureExclusionPx = gestureExclusions.bottomPx
                            )

                            if (shouldIgnoreDragStart) {
                                isGestureVisible = false
                                gestureMode = VideoGestureMode.None
                                dragStartX = -1f
                                // 不需要 return，直接不执行下面的初始化逻辑即可
                            } else {
                                isGestureVisible = true
                                gestureMode = VideoGestureMode.None
                                dragStartX = offset.x
                                totalDragDistanceY = 0f
                                totalDragDistanceX = 0f

                                startVolumeStep = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                startPosition = resolveGestureSeekStartPositionMs(
                                    seekSession = sharedSeekSession,
                                    playbackPositionMs = playerState.player.currentPosition
                                )
                                seekTargetTime = startPosition

                                val attributes = getActivity()?.window?.attributes
                                val currentWindowBrightness = attributes?.screenBrightness ?: -1f

                                if (currentWindowBrightness < 0) {
                                    try {
                                        val sysBrightness = Settings.System.getInt(
                                            context.contentResolver,
                                            Settings.System.SCREEN_BRIGHTNESS
                                        )
                                        startBrightness = sysBrightness / 255f
                                    } catch (e: Exception) {
                                        startBrightness = 0.5f
                                    }
                                } else {
                                    startBrightness = currentWindowBrightness
                                }
                            }
                        },
                        onDragEnd = {
                            val completedGestureMode = gestureMode
                            if (isLongPressing) {
                                finishLongPressSpeedGesture(gestureEnded = true)
                                isGestureVisible = false
                                gestureMode = VideoGestureMode.None
                                dragStartX = -1f
                                sharedSeekSession = resetPlaybackSeekSessionForActivePlayback(
                                    state = sharedSeekSession,
                                    playbackPositionMs = playerState.player.currentPosition
                                )
                                return@detectDragGestures
                            }
                            if (completedGestureMode == VideoGestureMode.Seek) {
                                val currentPosition = playerState.player.currentPosition
                                if (shouldCommitGestureSeek(
                                        currentPositionMs = currentPosition,
                                        targetPositionMs = sharedSeekSession.sliderPositionMs
                                    )
                                ) {
                                    val commitResult = finishPlaybackSeekInteraction(
                                        updatePlaybackSeekInteraction(
                                            state = sharedSeekSession,
                                            positionMs = sharedSeekSession.sliderPositionMs
                                        )
                                    )
                                    sharedSeekSession = commitResult.state
                                    seekPlayerFromUserAction(
                                        player = playerState.player,
                                        positionMs = commitResult.committedPositionMs,
                                        shouldResumePlaybackOverride = commitResult.shouldResumePlayback
                                    )
                                    danmakuManager.seekTo(commitResult.committedPositionMs)
                                } else {
                                    sharedSeekSession = cancelPlaybackSeekInteraction(sharedSeekSession)
                                }
                            } else if (completedGestureMode == VideoGestureMode.SwipeToFullscreen) {
                                //  阈值判定：上滑超过一定距离触发全屏
                                val swipeThreshold = 50.dp.toPx()
                                if (
                                    shouldTriggerFullscreenBySwipe(
                                        isFullscreen = isFullscreen,
                                        reverseGesture = fullscreenGestureReverse,
                                        totalDragDistanceY = totalDragDistanceY,
                                        thresholdPx = swipeThreshold
                                    )
                                ) {
                                    if (
                                        shouldEnterPortraitFullscreenFromSwipe(
                                            isFullscreen = isFullscreen,
                                            isVerticalVideo = isVerticalVideo,
                                        )
                                    ) {
                                        latestOnPortraitFullscreen()
                                    } else {
                                        latestOnToggleFullscreen()
                                    }
                                    // 震动反馈 (可选)
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                        if (isFullscreen) {
                                            "👇 Swipe to exit fullscreen triggered"
                                        } else {
                                            "👆 Swipe to fullscreen triggered"
                                        }
                                    }
                                }
                            }
                            isGestureVisible = false
                            gestureMode = VideoGestureMode.None
                            dragStartX = -1f
                        },
                        onDragCancel = {
                            if (isLongPressing) {
                                finishLongPressSpeedGesture(gestureEnded = true)
                                isGestureVisible = false
                                gestureMode = VideoGestureMode.None
                                dragStartX = -1f
                                sharedSeekSession = resetPlaybackSeekSessionForActivePlayback(
                                    state = sharedSeekSession,
                                    playbackPositionMs = playerState.player.currentPosition
                                )
                                return@detectDragGestures
                            }
                            isGestureVisible = false
                            if (gestureMode == VideoGestureMode.Seek) {
                                sharedSeekSession = cancelPlaybackSeekInteraction(sharedSeekSession)
                            }
                            gestureMode = VideoGestureMode.None
                            dragStartX = -1f
                        },
                        //  [修复点] 使用 dragAmount 而不是 change.positionChange()
                        onDrag = { change, dragAmount ->
                            if (shouldConsumeExclusiveLongPressSpeedDrag(
                                    isLongPressing = isLongPressing,
                                    longPressSpeedLocked = longPressSpeedLocked
                                )
                            ) {
                                if (
                                    !shouldEnableLongPressSpeedGesture(
                                        isScreenLocked = isScreenLocked,
                                        scale = scale,
                                        isMultiTouchActive = isMultiTouchActive
                                    )
                                ) {
                                    change.consume()
                                    return@detectDragGestures
                                }
                                totalDragDistanceY += dragAmount.y
                                val lockZoneHeightPx = longPressSpeedLockSensitivity.lockZoneHeightDp.dp.toPx()
                                val minLockDragDistancePx = longPressSpeedLockSensitivity.minDragDistanceDp.dp.toPx()
                                if (
                                    shouldLockLongPressSpeedInTargetZone(
                                        longPressSpeedLockEnabled = longPressSpeedLockEnabled,
                                        isLongPressing = isLongPressing,
                                        alreadyLocked = longPressSpeedLocked,
                                        currentPointerY = change.position.y,
                                        containerHeightPx = size.height.toFloat(),
                                        lockZoneHeightPx = lockZoneHeightPx,
                                        accumulatedDragYPx = totalDragDistanceY,
                                        minDragDistancePx = minLockDragDistancePx
                                    )
                                ) {
                                    longPressSpeedLocked = true
                                    lockedLongPressSpeed = effectiveLongPressSpeed
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    Toast.makeText(
                                        context,
                                        "已锁定 ${effectiveLongPressSpeed}x",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                change.consume()
                                return@detectDragGestures
                            }
                            // 如果手势不可见（即在 safe zone 中启动被忽略），则停止处理
                            if (!isGestureVisible && gestureMode == VideoGestureMode.None) {
                                // do nothing
                            } else {
                            
                            // [修复] 累积拖动距离，用于更准确的方向判断
                            totalDragDistanceX += dragAmount.x
                            totalDragDistanceY += dragAmount.y
                            
                            // [修复] 等待累积一定距离后再确定手势类型，避免初始噪声导致误判
                            val minDragThreshold = 20.dp.toPx()
                            val totalDrag = kotlin.math.hypot(totalDragDistanceX, totalDragDistanceY)

                            if (gestureMode == VideoGestureMode.None && totalDrag >= minDragThreshold) {
                                // [修复] 使用累积距离判断方向，而非单帧增量
                                if (shouldEngageHorizontalPlayerSeek(totalDragDistanceX, totalDragDistanceY)) {
                                    gestureMode = VideoGestureMode.Seek
                                    // Lock-in haptic so landscape seek always feels responsive.
                                    haptic.performHapticFeedback(
                                        androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                                    )
                                    lastSeekHapticTargetMs = startPosition
                                    com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                        "🎯 Gesture: Seek (cumDx=$totalDragDistanceX, cumDy=$totalDragDistanceY)"
                                    }
                                } else {
                                    // 根据起始 X 坐标判断区域 (左1/3=亮度, 右1/3=音量, 中间1/3=功能区)
                                    val width = size.width.toFloat()
                                    // 使用 onDragStart 锁定的起点 X，避免拖动中横向偏移导致误触
                                    val startX = if (dragStartX >= 0f) dragStartX else change.position.x
                                    // 分区边界增加缓冲，避免中间区域在边界附近被误判
                                    val boundaryPadding = uiLayoutPolicy.gestureBoundaryPaddingDp.dp.toPx()
                                    val leftZoneEnd = (width / 3f - boundaryPadding).coerceAtLeast(0f)
                                    val rightZoneStart = (width * 2f / 3f + boundaryPadding).coerceAtMost(width)
                                    val isSwipeUp = totalDragDistanceY < -minDragThreshold

                                    gestureMode = resolveVerticalGestureMode(
                                        isFullscreen = isFullscreen,
                                        isSwipeUp = isSwipeUp,
                                        startX = startX,
                                        leftZoneEnd = leftZoneEnd,
                                        rightZoneStart = rightZoneStart,
                                        portraitSwipeToFullscreenEnabled = portraitSwipeToFullscreenEnabled,
                                        centerSwipeToFullscreenEnabled = centerSwipeToFullscreenEnabled,
                                        slideVolumeBrightnessEnabled = slideVolumeBrightnessEnabled
                                    )

                                    // Seed level UI with the starting value as soon as the mode locks in,
                                    // so the overlay never opens blank or stuck on a previous gesture percent.
                                    when (gestureMode) {
                                        VideoGestureMode.Brightness -> {
                                            gesturePercent = startBrightness.coerceIn(0f, 1f)
                                            gestureIcon = resolveGestureLevelIcon(
                                                style = gestureLevelOverlayStyle,
                                                kind = com.android.purebilibili.feature.video.ui.gesture.GestureLevelKind.Brightness,
                                                percent = gesturePercent
                                            )
                                        }
                                        VideoGestureMode.Volume -> {
                                            val maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                            gesturePercent = if (maxVolumeStep > 0) {
                                                startVolumeStep.toFloat() / maxVolumeStep.toFloat()
                                            } else {
                                                0f
                                            }
                                            gestureIcon = resolveGestureLevelIcon(
                                                style = gestureLevelOverlayStyle,
                                                kind = com.android.purebilibili.feature.video.ui.gesture.GestureLevelKind.Volume,
                                                percent = gesturePercent
                                            )
                                        }
                                        else -> Unit
                                    }

                                    // 横屏中间 1/3 的垂直手势直接忽略，避免误触亮度/音量
                                    if (isFullscreen && gestureMode == VideoGestureMode.None) {
                                        isGestureVisible = false
                                        com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                            "🎯 Gesture ignored in center zone (fullscreen, startX=$startX, width=$width)"
                                        }
                                        return@detectDragGestures
                                    }

                                    com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                        "🎯 Gesture: $gestureMode (startX=$startX, width=$width, isFullscreen=$isFullscreen)"
                                    }
                                }
                            }

                            when (gestureMode) {
                                VideoGestureMode.SwipeToFullscreen -> {
                                    // 累积 Y 轴距离已在上方处理
                                }
                                VideoGestureMode.Seek -> {
                                    if (!sharedSeekSession.isSliderMoving) {
                                        sharedSeekSession = startPlaybackSeekInteraction(
                                            state = sharedSeekSession,
                                            player = playerState.player,
                                            positionMs = startPosition
                                        )
                                    }
                                    // 距离已在上方累积，直接计算目标位置
                                    val duration = resolveGestureSeekableDurationMs(
                                        playbackDurationMs = playerState.player.duration,
                                        fallbackDurationMs = gestureSeekFallbackDurationMs
                                    )
                                    val seekDelta = resolveHorizontalSeekDeltaMs(
                                        isFullscreen = isFullscreen,
                                        fullscreenSwipeSeekEnabled = fullscreenSwipeSeekEnabled,
                                        totalDragDistanceX = totalDragDistanceX,
                                        containerWidthPx = size.width.toFloat(),
                                        fullscreenSwipeSeekSeconds = fullscreenSwipeSeekSeconds,
                                        inlineSwipeSeekSeconds = inlineSwipeSeekSeconds,
                                        gestureSensitivity = gestureSensitivity
                                    )
                                    if (seekDelta != null) {
                                        seekTargetTime = (startPosition + seekDelta).coerceIn(0L, duration)
                                        if (
                                            shouldTriggerSeekStepHaptic(
                                                previousTargetMs = lastSeekHapticTargetMs,
                                                currentTargetMs = seekTargetTime
                                            )
                                        ) {
                                            haptic.performHapticFeedback(
                                                androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                                            )
                                            lastSeekHapticTargetMs = seekTargetTime
                                        }
                                        sharedSeekSession = updatePlaybackSeekInteraction(
                                            state = sharedSeekSession,
                                            positionMs = seekTargetTime
                                        )
                                    }
                                }
                                VideoGestureMode.Brightness -> {
                                    // 距离已在上方累积，使用负值因为上滑是负 Y
                                    val screenHeight = context.resources.displayMetrics.heightPixels
                                    //  应用灵敏度
                                    val deltaPercent = -totalDragDistanceY / screenHeight * gestureSensitivity
                                    val newBrightness = (startBrightness + deltaPercent).coerceIn(0f, 1f)
                                    
                                    //  优化：仅在变化超过阈值时更新（减少 WindowManager 调用）
                                    if (kotlin.math.abs(newBrightness - gesturePercent) > 0.02f) {
                                        getActivity()?.window?.attributes = getActivity()?.window?.attributes?.apply {
                                            screenBrightness = newBrightness
                                        }
                                        if (setSystemBrightnessEnabled) {
                                            runCatching {
                                                if (Settings.System.canWrite(context)) {
                                                    val value = (newBrightness * 255f).roundToInt().coerceIn(1, 255)
                                                    Settings.System.putInt(
                                                        context.contentResolver,
                                                        Settings.System.SCREEN_BRIGHTNESS,
                                                        value
                                                    )
                                                }
                                            }
                                        }
                                        gesturePercent = newBrightness
                                    }
                                    gestureIcon = resolveGestureLevelIcon(
                                        style = gestureLevelOverlayStyle,
                                        kind = com.android.purebilibili.feature.video.ui.gesture.GestureLevelKind.Brightness,
                                        percent = gesturePercent
                                    )
                                }
                                VideoGestureMode.Volume -> {
                                    val maxVolumeStep = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val newVolumeStep = resolveSystemStreamVolumeFromGesture(
                                        startVolumeStep = startVolumeStep,
                                        maxVolumeStep = maxVolumeStep,
                                        totalDragDistanceY = totalDragDistanceY,
                                        screenHeightPx = context.resources.displayMetrics.heightPixels.toFloat(),
                                        gestureSensitivity = gestureSensitivity
                                    )
                                    audioManager.setStreamVolume(
                                        AudioManager.STREAM_MUSIC,
                                        newVolumeStep,
                                        0
                                    )
                                    gesturePercent = if (maxVolumeStep > 0) {
                                        newVolumeStep.toFloat() / maxVolumeStep.toFloat()
                                    } else {
                                        0f
                                    }
                                    gestureIcon = resolveGestureLevelIcon(
                                        style = gestureLevelOverlayStyle,
                                        kind = com.android.purebilibili.feature.video.ui.gesture.GestureLevelKind.Volume,
                                        percent = gesturePercent
                                    )
                                }
                                else -> {}
                            }
                            }
                        }
                    )
                }
            }
            //  长按倍速和拖动锁定必须在同一个手势探测器内处理。
            .pointerInput(
                playerState.player,
                longPressSpeed,
                isScreenLocked,
                currentAudioQuality,
                hasShownHiResCompatHint,
                scale,
                isMultiTouchActive,
                isFullscreen,
                longPressSpeedLockEnabled
            ) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { startOffset ->
                        startLongPressSpeedGesture(startOffset)
                    },
                    onDragEnd = {
                        finishLongPressSpeedGesture(gestureEnded = true)
                    },
                    onDragCancel = {
                        finishLongPressSpeedGesture(gestureEnded = true)
                    },
                    onDrag = { change, dragAmount ->
                        if (
                            !shouldEnableLongPressSpeedGesture(
                                isScreenLocked = isScreenLocked,
                                scale = scale,
                                isMultiTouchActive = isMultiTouchActive
                            )
                        ) {
                            change.consume()
                            return@detectDragGesturesAfterLongPress
                        }
                        totalDragDistanceY += dragAmount.y
                        totalDragDistanceX += dragAmount.x
                        if (!longPressSpeedLockEnabled) {
                            change.consume()
                            return@detectDragGesturesAfterLongPress
                        }
                        val lockZoneHeightPx = longPressSpeedLockSensitivity.lockZoneHeightDp.dp.toPx()
                        val minLockDragDistancePx = longPressSpeedLockSensitivity.minDragDistanceDp.dp.toPx()
                        val holdDurationMs = (
                            android.os.SystemClock.elapsedRealtime() - longPressSpeedStartedAtMs
                            ).coerceAtLeast(0L)
                        if (
                            shouldUnlockLockedLongPressSpeedFromRightDownDrag(
                                longPressSpeedLocked = longPressSpeedLocked,
                                isLongPressing = isLongPressing,
                                startX = longPressSpeedStartX,
                                startY = longPressSpeedStartY,
                                currentY = change.position.y,
                                containerWidthPx = size.width.toFloat(),
                                holdDurationMs = holdDurationMs,
                                minDownDragPx = minLockDragDistancePx
                            )
                        ) {
                            unlockLockedLongPressSpeedFromGesture()
                            change.consume()
                            return@detectDragGesturesAfterLongPress
                        }
                        if (longPressSpeedLocked) {
                            change.consume()
                            return@detectDragGesturesAfterLongPress
                        }
                        if (
                            shouldLockLongPressSpeedInTargetZone(
                                longPressSpeedLockEnabled = longPressSpeedLockEnabled,
                                isLongPressing = isLongPressing,
                                alreadyLocked = longPressSpeedLocked,
                                currentPointerY = change.position.y,
                                containerHeightPx = size.height.toFloat(),
                                lockZoneHeightPx = lockZoneHeightPx,
                                accumulatedDragYPx = totalDragDistanceY,
                                minDragDistancePx = minLockDragDistancePx
                            )
                        ) {
                            longPressSpeedLocked = true
                            lockedLongPressSpeed = effectiveLongPressSpeed
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            Toast.makeText(
                                context,
                                "已锁定 ${effectiveLongPressSpeed}x",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        change.consume()
                    }
                )
            }
            //  点击/双击手势在拖拽之后处理
            .pointerInput(
                playerState.player,
                seekForwardSeconds,
                seekBackwardSeconds,
                doubleTapSeekEnabled,
                isScreenLocked
            ) {
                detectTapGestures(
                    onTap = { 
                        // 🔒 锁定时点击只显示解锁按钮
                        if (
                            !shouldToggleControlsForVideoTap(
                                longPressSpeedEndedAtMs = longPressSpeedEndedAtMs,
                                nowMs = android.os.SystemClock.elapsedRealtime()
                            )
                        ) {
                            return@detectTapGestures
                        }
                        if (isScreenLocked) {
                            showControls = !showControls  // 显示/隐藏解锁按钮
                        } else {
                            showControls = !showControls
                        }
                    },
                    onDoubleTap = { offset ->
                        // 🔒 锁定时禁用双击
                        if (isScreenLocked) return@detectTapGestures
                        
                        val screenWidth = size.width.toFloat()
                        val player = playerState.player
                        val relativeX = if (screenWidth > 0f) {
                            offset.x / screenWidth
                        } else {
                            0.5f
                        }
                        when (
                            resolveFullscreenDoubleTapAction(
                                relativeX = relativeX,
                                doubleTapSeekEnabled = doubleTapSeekEnabled,
                                playWhenReady = player.playWhenReady,
                                isPlaying = player.isPlaying,
                                playbackState = player.playbackState
                            )
                        ) {
                            FullscreenDoubleTapAction.SeekForward -> {
                                val seekMs = seekForwardSeconds * 1000L
                                val newPos = resolveRelativeSeekTargetPosition(
                                    currentPositionMs = player.currentPosition,
                                    deltaMs = seekMs,
                                    durationMs = player.duration
                                )
                                commitExplicitSeek(newPos)
                                val feedback = nextFullscreenSeekFeedbackEvent(
                                    previousGeneration = seekFeedbackGeneration,
                                    deltaSeconds = seekForwardSeconds
                                )
                                seekFeedbackGeneration = feedback.generation
                                seekFeedbackText = feedback.text
                                seekFeedbackVisible = true
                                com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                    "⏩ DoubleTap right: +${seekForwardSeconds}s"
                                }
                            }
                            FullscreenDoubleTapAction.SeekBackward -> {
                                val seekMs = seekBackwardSeconds * 1000L
                                val newPos = resolveRelativeSeekTargetPosition(
                                    currentPositionMs = player.currentPosition,
                                    deltaMs = -seekMs,
                                    durationMs = player.duration
                                )
                                commitExplicitSeek(newPos)
                                val feedback = nextFullscreenSeekFeedbackEvent(
                                    previousGeneration = seekFeedbackGeneration,
                                    deltaSeconds = -seekBackwardSeconds
                                )
                                seekFeedbackGeneration = feedback.generation
                                seekFeedbackText = feedback.text
                                seekFeedbackVisible = true
                                com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                    "⏪ DoubleTap left: -${seekBackwardSeconds}s"
                                }
                            }
                            FullscreenDoubleTapAction.TogglePlayPause -> {
                                togglePlayerPlaybackFromUserAction(player)
                                com.android.purebilibili.core.util.Logger.d("VideoPlayerSection") {
                                    "⏯️ DoubleTap: toggle play/pause"
                                }
                            }
                        }
                    }
                )
            }
    ) {
        val scope = rememberCoroutineScope()  //  用于设置弹幕开关
        val activeDanmakuScope = remember(isFullscreen, isPortraitFullscreen) {
            resolveVideoPlayerDanmakuSettingsScope(
                isFullscreen = isFullscreen,
                isPortraitFullscreen = isPortraitFullscreen
            )
        }

        val danmakuSettings = settings.danmakuSettings
        LaunchedEffect(activeDanmakuScope) {
            settingsActions.setDanmakuSettingsScope(activeDanmakuScope)
        }
        val danmakuEnabled = danmakuSettings.enabled
        val danmakuOpacity = danmakuSettings.opacity
        val danmakuFontScale = danmakuSettings.fontScale
        val danmakuFontWeight = danmakuSettings.fontWeight
        val danmakuSpeed = danmakuSettings.speed
        val danmakuDisplayArea = danmakuSettings.displayArea
        val danmakuStrokeWidth = danmakuSettings.strokeWidth
        val danmakuLineHeight = danmakuSettings.lineHeight
        val danmakuScrollDurationSeconds = danmakuSettings.scrollDurationSeconds
        val danmakuStaticDurationSeconds = danmakuSettings.staticDurationSeconds
        val danmakuScrollFixedVelocity = danmakuSettings.scrollFixedVelocity
        val danmakuStaticToScroll = danmakuSettings.staticDanmakuToScroll
        val danmakuMassiveMode = danmakuSettings.massiveMode
        val danmakuMergeDuplicates = danmakuSettings.mergeDuplicates
        val danmakuDuplicateMergeWindowMs = danmakuSettings.duplicateMergeWindowMs
        val danmakuDuplicateMergeCountThreshold = danmakuSettings.duplicateMergeCountThreshold
        val danmakuAllowScroll = danmakuSettings.allowScroll
        val danmakuAllowTop = danmakuSettings.allowTop
        val danmakuAllowBottom = danmakuSettings.allowBottom
        val danmakuAllowColorful = danmakuSettings.allowColorful
        val danmakuAllowSpecial = danmakuSettings.allowSpecial
        val danmakuHideInteractiveCommands = danmakuSettings.hideInteractiveCommands
        val danmakuSmartOcclusion = danmakuSettings.smartOcclusion
        val portraitDanmakuDisplayAreaMode = danmakuSettings.portraitDisplayAreaMode
        val danmakuFullscreenPanelWidthMode = settings.danmakuFullscreenPanelWidthMode
        val danmakuBlockRulesRaw = danmakuSettings.blockRulesRaw
        val danmakuBlockRules = danmakuSettings.blockRules
        val isLoggedIn = (uiState as? VideoPlaybackUiState.Success)?.isLoggedIn == true
        val danmakuCloudSyncEnabled = settings.danmakuCloudSyncEnabled
        val canSyncDanmakuCloud = com.android.purebilibili.feature.video.danmaku
            .shouldSyncDanmakuSettingsToCloud(
                isLoggedIn = isLoggedIn,
                cloudSyncEnabled = danmakuCloudSyncEnabled
            )
        var pendingDanmakuCloudSync by remember {
            mutableStateOf<com.android.purebilibili.data.repository.DanmakuCloudSyncSettings?>(null)
        }
        var danmakuCloudSyncUiState by remember {
            mutableStateOf(DanmakuCloudSyncUiState())
        }
        var danmakuManualSyncRequestVersion by remember {
            mutableStateOf<Long?>(null)
        }
        var lastHandledDanmakuManualSyncRequestVersion by remember {
            mutableStateOf<Long?>(null)
        }

        fun buildDanmakuCloudSyncSettings(
            enabled: Boolean = danmakuEnabled,
            allowScroll: Boolean = danmakuAllowScroll,
            allowTop: Boolean = danmakuAllowTop,
            allowBottom: Boolean = danmakuAllowBottom,
            allowColorful: Boolean = danmakuAllowColorful,
            allowSpecial: Boolean = danmakuAllowSpecial,
            opacity: Float = danmakuOpacity,
            displayAreaRatio: Float = danmakuDisplayArea,
            speed: Float = danmakuSpeed,
            fontScale: Float = danmakuFontScale
        ): com.android.purebilibili.data.repository.DanmakuCloudSyncSettings {
            return com.android.purebilibili.data.repository.DanmakuCloudSyncSettings(
                enabled = enabled,
                allowScroll = allowScroll,
                allowTop = allowTop,
                allowBottom = allowBottom,
                allowColorful = allowColorful,
                allowSpecial = allowSpecial,
                opacity = opacity,
                displayAreaRatio = displayAreaRatio,
                speed = speed,
                fontScale = fontScale
            )
        }

        fun queueDanmakuCloudSync(
            enabled: Boolean = danmakuEnabled,
            allowScroll: Boolean = danmakuAllowScroll,
            allowTop: Boolean = danmakuAllowTop,
            allowBottom: Boolean = danmakuAllowBottom,
            allowColorful: Boolean = danmakuAllowColorful,
            allowSpecial: Boolean = danmakuAllowSpecial,
            opacity: Float = danmakuOpacity,
            displayAreaRatio: Float = danmakuDisplayArea,
            speed: Float = danmakuSpeed,
            fontScale: Float = danmakuFontScale
        ) {
            if (!canSyncDanmakuCloud) return
            pendingDanmakuCloudSync = buildDanmakuCloudSyncSettings(
                enabled = enabled,
                allowScroll = allowScroll,
                allowTop = allowTop,
                allowBottom = allowBottom,
                allowColorful = allowColorful,
                allowSpecial = allowSpecial,
                opacity = opacity,
                displayAreaRatio = displayAreaRatio,
                speed = speed,
                fontScale = fontScale
            )
            danmakuCloudSyncUiState = resolveDanmakuCloudSyncStateAfterQueued(danmakuCloudSyncUiState)
        }

        fun requestDanmakuCloudSyncNow() {
            if (!canSyncDanmakuCloud) return
            pendingDanmakuCloudSync = buildDanmakuCloudSyncSettings()
            danmakuManualSyncRequestVersion = android.os.SystemClock.elapsedRealtime()
            danmakuCloudSyncUiState = resolveDanmakuCloudSyncStateAfterQueued(danmakuCloudSyncUiState)
        }

        //  当视频/开关状态变化时更新弹幕加载策略
        val cid = (uiState as? VideoPlaybackUiState.Success)?.info?.cid ?: 0L
        val aid = (uiState as? VideoPlaybackUiState.Success)?.info?.aid ?: 0L
        val danmakuDurationHintMs = playerState.player.duration.takeIf { it > 0 } ?: 0L
        val danmakuLoadPolicy = remember(cid, danmakuEnabled) {
            resolveVideoPlayerDanmakuLoadPolicy(
                cid = cid,
                danmakuEnabled = danmakuEnabled,
                durationHintMs = danmakuDurationHintMs
            )
        }
        //  直接加载弹幕，不再等待 duration；仓库层会回退到 metadata/fallback 段数。
        val runDanmakuHostEffects = shouldRunVideoPlayerDanmakuHostEffects(
            danmakuHostActive = danmakuHostActive,
            hostLifecycleStarted = hostLifecycleStarted,
        )
        LaunchedEffect(cid, aid, danmakuEnabled, runDanmakuHostEffects) {
            // 相关推荐 push 会让新旧详情页在转场期间同时处于 STARTED。旧页不得再次
            // Enable/load 单例引擎，否则会取消新 cid 请求或把新数据同步到旧播放器。
            if (!runDanmakuHostEffects) return@LaunchedEffect
            when (
                resolveVideoPlayerDanmakuEngineSyncAction(
                    danmakuEnabled = danmakuEnabled,
                    cid = cid
                )
            ) {
                VideoPlayerDanmakuEngineSyncAction.Enable -> {
                    danmakuManager.isEnabled = true
                }
                VideoPlayerDanmakuEngineSyncAction.DisableAndClear -> {
                    danmakuManager.isEnabled = false
                    danmakuManager.clear()
                }
            }
            if (!shouldLoadDanmakuForForegroundHost(
                    hostLifecycleStarted = hostLifecycleStarted,
                    shouldLoadImmediately = danmakuLoadPolicy.shouldLoadImmediately
                )
            ) {
                return@LaunchedEffect
            }

            // 相关推荐/同页切集时新播放器可能尚未就绪（duration=0），
            // 若立刻按 0 加载会降级到 fallback 导致弹幕为空。
            // 等待 duration 就绪后按完整分段加载；超时则按当前可用值加载。
            var durationHintMs = danmakuLoadPolicy.durationHintMs
            if (durationHintMs <= 0L && cid > 0L && danmakuEnabled) {
                var attempts = 0
                while (attempts < DANMAKU_DURATION_WAIT_ATTEMPTS) {
                    val currentDuration = playerState.player.duration
                    if (currentDuration > 0L) {
                        durationHintMs = currentDuration
                        break
                    }
                    attempts += 1
                    delay(DANMAKU_DURATION_WAIT_INTERVAL_MS)
                }
            }

            android.util.Log.d(
                "VideoPlayerSection",
                "🎯 Loading danmaku for cid=$cid, aid=$aid, durationHint=${durationHintMs}ms"
            )
            danmakuManager.loadDanmaku(cid, aid, durationHintMs)
        }

        //  横竖屏/小窗切换后，重绑 surface 并在需要时主动恢复播放。
        LaunchedEffect(
            isFullscreen,
            isInPipMode,
            playerViewRef,
            shouldBindInlinePlayerView
        ) {
            val player = playerState.player
            if (!shouldBindInlinePlayerView) {
                playerViewRef?.player = null
                return@LaunchedEffect
            }
            // 横竖屏/小窗切换始终重绑；短后台跳过只作用于 ON_RESUME 恢复路径。
            val shouldRebindSurface = shouldRebindPlayerSurfaceOnForeground(
                hasPlayerView = playerViewRef != null,
                isInPipMode = isInPipMode,
                videoWidth = player.videoSize.width,
                videoHeight = player.videoSize.height,
                needsSurfaceRecovery = false
            )
            if (shouldRebindSurface) {
                playerViewRef?.let { playerView ->
                    videoOutputRouter.rebindDirectSurfaceIfNeeded()
                    Logger.d("VideoPlayerSection") {
                        "🎬 Foreground surface rebind applied to avoid audio-only resume"
                    }
                }
            }
        }

        // 合集/页内换片：bvid 或 Success 媒体就绪后强制重绑 surface，避免只听声音、画面黑屏。
        val successPlaybackIdentity = (uiState as? VideoPlaybackUiState.Success)?.let { success ->
            "${success.info.bvid}_${success.info.cid}_${success.playUrl.hashCode()}"
        }
        LaunchedEffect(
            bvid,
            successPlaybackIdentity,
            playerState.player,
            videoOutputRouter,
            playerViewRef,
            shouldBindInlinePlayerView,
            isInPipMode
        ) {
            var waitAttempts = 0
            while (isActive) {
                val player = playerState.player
                when (
                    resolveMediaSwitchSurfaceRebindAction(
                        hasSuccessPlaybackIdentity = !successPlaybackIdentity.isNullOrBlank(),
                        shouldBindInlinePlayerView = shouldBindInlinePlayerView,
                        isInPipMode = isInPipMode,
                        hasPlayerView = playerViewRef != null,
                        mediaItemCount = player.mediaItemCount
                    )
                ) {
                    MediaSwitchSurfaceRebindAction.SKIP -> return@LaunchedEffect
                    MediaSwitchSurfaceRebindAction.WAIT_FOR_OUTPUT -> {
                        if (waitAttempts >= MEDIA_SWITCH_SURFACE_REBIND_ATTEMPTS) {
                            Logger.w(
                                "VideoPlayerSection",
                                "⚠️ Media switch surface rebind timed out: " +
                                    "bvid=$bvid identity=$successPlaybackIdentity"
                            )
                            return@LaunchedEffect
                        }
                        waitAttempts += 1
                        delay(MEDIA_SWITCH_SURFACE_REBIND_INTERVAL_MS)
                    }
                    MediaSwitchSurfaceRebindAction.REBIND -> break
                }
            }
            val player = playerState.player
            videoOutputRouter.update(
                playerView = playerViewRef,
                shouldBindDirectPlayerView = shouldBindInlinePlayerView
            )
            videoOutputRouter.rebindDirectSurfaceIfNeeded()
            if (
                shouldKickPlaybackAfterSurfaceRecovery(
                    playWhenReady = player.playWhenReady,
                    isPlaying = player.isPlaying,
                    playbackState = player.playbackState,
                    hasPlaybackResumeIntent = player.playWhenReady
                )
            ) {
                player.play()
            }
            Logger.d("VideoPlayerSection") {
                "🎬 In-page media switch surface rebind: bvid=$bvid identity=$successPlaybackIdentity"
            }
        }

        LaunchedEffect(
            predictiveBackCancelRecoveryGeneration,
            playerViewRef,
            shouldBindInlinePlayerView,
            isInPipMode
        ) {
            if (!shouldRecoverInlinePlayerAfterPredictiveBackCancel(
                    recoveryGeneration = predictiveBackCancelRecoveryGeneration,
                    hasPlayerView = playerViewRef != null,
                    shouldBindInlinePlayerView = shouldBindInlinePlayerView,
                    isInPipMode = isInPipMode
                )
            ) {
                return@LaunchedEffect
            }
            val player = playerState.player
            playerViewRef?.let { playerView ->
                videoOutputRouter.rebindDirectSurfaceIfNeeded()
            }
            if (shouldKickPlaybackAfterSurfaceRecovery(
                    playWhenReady = player.playWhenReady,
                    isPlaying = player.isPlaying,
                    playbackState = player.playbackState,
                    hasPlaybackResumeIntent = true
                )
            ) {
                player.play()
            }
            danmakuManager.recoverAfterForeground(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                playWhenReady = player.playWhenReady,
                playbackState = player.playbackState
            )
            Logger.d("VideoPlayerSection") {
                "↩️ Predictive back cancel restored current video surface: " +
                    "generation=$predictiveBackCancelRecoveryGeneration, pos=${player.currentPosition}"
            }
        }

        LaunchedEffect(
            foregroundRecoveryGeneration,
            playerViewRef,
            shouldBindInlinePlayerView,
            isInPipMode
        ) {
            if (foregroundRecoveryGeneration <= 0) return@LaunchedEffect
            if (!shouldStartForegroundSurfaceRecovery(
                    hasPlayerView = playerViewRef != null,
                    shouldBindInlinePlayerView = shouldBindInlinePlayerView,
                    isInPipMode = isInPipMode,
                    needsSurfaceRecovery = foregroundRecoveryNeedsSurface,
                    videoWidth = playerState.player.videoSize.width,
                    videoHeight = playerState.player.videoSize.height
                )
            ) {
                return@LaunchedEffect
            }

            delay(FOREGROUND_SURFACE_RECOVERY_DELAY_MS)
            val player = playerState.player
            playerViewRef?.let { playerView ->
                videoOutputRouter.rebindDirectSurfaceIfNeeded()
                Logger.d("VideoPlayerSection") {
                    "🎬 Foreground recovery retry: surface=${playerView.videoSurfaceView?.javaClass?.simpleName}, " +
                        "pos=${player.currentPosition}, state=${player.playbackState}, playing=${player.isPlaying}"
                }
            }
            if (shouldKickPlaybackAfterSurfaceRecovery(
                    playWhenReady = player.playWhenReady,
                    isPlaying = player.isPlaying,
                    playbackState = player.playbackState,
                    hasPlaybackResumeIntent = true
                )
            ) {
                player.play()
                Logger.d("VideoPlayerSection") {
                    "▶️ Foreground recovery kicked playback to rebuild render chain"
                }
            }
            danmakuManager.recoverAfterForeground(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                playWhenReady = player.playWhenReady,
                playbackState = player.playbackState
            )

            delay(FOREGROUND_SURFACE_RECOVERY_TIMEOUT_MS)
            if (!shouldLogForegroundSurfaceRecoveryTimeout(
                    hasRenderedFirstFrameSinceRecovery = hasRenderedFirstFrameSinceForegroundRecovery,
                    playWhenReady = player.playWhenReady,
                    playbackState = player.playbackState
                )
            ) {
                return@LaunchedEffect
            }

            val elapsedMs = (android.os.SystemClock.elapsedRealtime() - foregroundRecoveryStartedAtMs)
                .coerceAtLeast(0L)
            val advancedPositionMs = (player.currentPosition - foregroundRecoveryStartPositionMs)
                .coerceAtLeast(0L)
            Logger.w(
                "VideoPlayerSection",
                "⚠️ Foreground recovery still missing first frame after ${elapsedMs}ms: " +
                    "state=${player.playbackState}, playing=${player.isPlaying}, playWhenReady=${player.playWhenReady}, " +
                    "pos=${player.currentPosition}, advanced=${advancedPositionMs}, buffered=${player.bufferedPosition}, " +
                    "surface=${playerViewRef?.videoSurfaceView?.javaClass?.simpleName}, viewAttached=${playerViewRef?.isAttachedToWindow}"
            )

            playerViewRef?.let { playerView ->
                videoOutputRouter.rebindDirectSurfaceIfNeeded()
            }
            if (shouldKickPlaybackAfterSurfaceRecovery(
                    playWhenReady = player.playWhenReady,
                    isPlaying = player.isPlaying,
                    playbackState = player.playbackState,
                    hasPlaybackResumeIntent = true
                )
            ) {
                player.play()
            }
        }

        LaunchedEffect(isFullscreen) {
            if (!hasObservedOrientationChange) {
                hasObservedOrientationChange = true
                return@LaunchedEffect
            }
            orientationHintText = resolveOrientationSwitchHintText(isFullscreen)
            orientationHintVisible = true
            delay(760)
            orientationHintVisible = false
        }
        
        //  弹幕设置变化时实时应用
        LaunchedEffect(
            danmakuOpacity,
            danmakuFontScale,
            danmakuFontWeight,
            danmakuSpeed,
            danmakuDisplayArea,
            danmakuStrokeWidth,
            danmakuLineHeight,
            danmakuScrollDurationSeconds,
            danmakuStaticDurationSeconds,
            danmakuScrollFixedVelocity,
            danmakuStaticToScroll,
            danmakuMassiveMode,
            danmakuMergeDuplicates,
            danmakuDuplicateMergeWindowMs,
            danmakuDuplicateMergeCountThreshold,
            danmakuAllowScroll,
            danmakuAllowTop,
            danmakuAllowBottom,
            danmakuAllowColorful,
            danmakuAllowSpecial,
            danmakuBlockRules,
            danmakuSmartOcclusion
        ) {
            danmakuManager.updateSettings(
                opacity = danmakuOpacity,
                fontScale = danmakuFontScale,
                fontWeight = danmakuFontWeight,
                speed = danmakuSpeed,
                scrollDurationSeconds = danmakuScrollDurationSeconds,
                displayArea = danmakuDisplayArea,
                strokeWidth = danmakuStrokeWidth,
                lineHeight = danmakuLineHeight,
                staticDurationSeconds = danmakuStaticDurationSeconds,
                scrollFixedVelocity = danmakuScrollFixedVelocity,
                staticDanmakuToScroll = danmakuStaticToScroll,
                massiveMode = danmakuMassiveMode,
                mergeDuplicates = danmakuMergeDuplicates,
                duplicateMergeWindowMs = danmakuDuplicateMergeWindowMs,
                duplicateMergeCountThreshold = danmakuDuplicateMergeCountThreshold,
                allowScroll = danmakuAllowScroll,
                allowTop = danmakuAllowTop,
                allowBottom = danmakuAllowBottom,
                allowColorful = danmakuAllowColorful,
                allowSpecial = danmakuAllowSpecial,
                blockedRules = danmakuBlockRules,
                // Mask-only mode: keep lane layout fixed, do not move danmaku tracks.
                smartOcclusion = false
            )
        }

        LaunchedEffect(canSyncDanmakuCloud, danmakuCloudSyncEnabled) {
            if (canSyncDanmakuCloud) return@LaunchedEffect
            pendingDanmakuCloudSync = null
            danmakuCloudSyncUiState = DanmakuCloudSyncUiState()
        }

        // 账号云同步：用户修改弹幕设置后防抖上云，避免滑杆拖动时高频请求
        LaunchedEffect(pendingDanmakuCloudSync, canSyncDanmakuCloud, danmakuManualSyncRequestVersion) {
            val settings = pendingDanmakuCloudSync ?: return@LaunchedEffect
            if (!canSyncDanmakuCloud) return@LaunchedEffect

            val manualSyncRequested = shouldRunDanmakuManualCloudSync(
                manualRequestVersion = danmakuManualSyncRequestVersion,
                lastHandledManualRequestVersion = lastHandledDanmakuManualSyncRequestVersion
            )
            if (!manualSyncRequested) {
                kotlinx.coroutines.delay(700)
            }
            danmakuCloudSyncUiState = resolveDanmakuCloudSyncStateAfterStarted(danmakuCloudSyncUiState)
            val result = settingsActions.syncDanmakuCloudConfig(settings)
            val completedAtMillis = System.currentTimeMillis()
            danmakuCloudSyncUiState = resolveDanmakuCloudSyncStateAfterResult(
                previous = danmakuCloudSyncUiState,
                result = result,
                completedAtMillis = completedAtMillis
            )
            if (manualSyncRequested) {
                lastHandledDanmakuManualSyncRequestVersion = danmakuManualSyncRequestVersion
            }
            if (pendingDanmakuCloudSync == settings) {
                pendingDanmakuCloudSync = null
            }
            if (result.isFailure) {
                android.util.Log.w(
                    "VideoPlayerSection",
                    "Danmaku cloud sync failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
        
        //  绑定 Player（不在 onDispose 中释放，单例保持状态）
        DisposableEffect(playerState.player, runDanmakuHostEffects) {
            if (runDanmakuHostEffects) {
                android.util.Log.d("VideoPlayerSection", " attachPlayer, isFullscreen=$isFullscreen")
                danmakuManager.attachPlayer(playerState.player)
            } else if (!danmakuHostActive) {
                // 相关推荐转场的旧页面仍可能保持 STARTED；立即让出播放器监听器。
                danmakuManager.detachPlayerIfCurrent(playerState.player)
            }
            onDispose {
                // 单例模式不需要释放
            }
        }
        
        // Activity 生命周期监听必须只跟随 LifecycleOwner。合集内换片会替换 Player，若把 Player
        // 作为 effect key，重新注册的 observer 会立刻收到当前 ON_RESUME，误触发前台 Surface 恢复。
        val lifecyclePlayer by rememberUpdatedState(playerState.player)
        val lifecycleIsPortraitFullscreen by rememberUpdatedState(isPortraitFullscreen)
        val lifecycleIsInPipMode by rememberUpdatedState(isInPipMode)
        val lifecyclePlayerView by rememberUpdatedState(playerViewRef)
        val lifecycleVideoOutputRouter by rememberUpdatedState(videoOutputRouter)
        val lifecycleDanmakuHostActive by rememberUpdatedState(danmakuHostActive)
        DisposableEffect(lifecycleOwner) {
            var hasObservedHostPause = false
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                        if (!lifecycleDanmakuHostActive) {
                            android.util.Log.d(
                                "VideoPlayerSection",
                                " ON_RESUME: Skip danmaku binding for outgoing detail host"
                            )
                            return@LifecycleEventObserver
                        }
                        android.util.Log.d("VideoPlayerSection", " ON_RESUME: Re-attaching danmaku player")
                        val player = lifecyclePlayer
                        danmakuManager.attachPlayer(player)
                        if (!hasObservedHostPause) {
                            Logger.d("VideoPlayerSection") {
                                "ON_RESUME skipped foreground recovery (initial lifecycle sync)"
                            }
                            return@LifecycleEventObserver
                        }
                        hasObservedHostPause = false
                        if (!shouldBindInlinePlayerViewToPlayer(
                                isPortraitFullscreen = lifecycleIsPortraitFullscreen,
                                hostLifecycleStarted = true,
                                isInPipMode = lifecycleIsInPipMode
                            )
                        ) {
                            return@LifecycleEventObserver
                        }
                        val needsSurfaceRecovery = MiniPlayerManager.getInstance(context)
                            .consumeForegroundSurfaceRecoveryNeed()
                        foregroundRecoveryNeedsSurface = needsSurfaceRecovery
                        foregroundRecoveryGeneration += 1
                        foregroundRecoveryStartedAtMs = android.os.SystemClock.elapsedRealtime()
                        foregroundRecoveryStartPositionMs = player.currentPosition.coerceAtLeast(0L)
                        hasRenderedFirstFrameSinceForegroundRecovery = false
                        Logger.d("VideoPlayerSection") {
                            "🌅 ON_RESUME recovery start: pos=${player.currentPosition}, buffered=${player.bufferedPosition}, " +
                                "state=${player.playbackState}, playing=${player.isPlaying}, playWhenReady=${player.playWhenReady}, " +
                                "needsSurfaceRecovery=$needsSurfaceRecovery, " +
                                "surface=${lifecyclePlayerView?.videoSurfaceView?.javaClass?.simpleName}"
                        }
                        val shouldRebindSurface = shouldRebindPlayerSurfaceOnForeground(
                            hasPlayerView = lifecyclePlayerView != null,
                            isInPipMode = lifecycleIsInPipMode,
                            videoWidth = player.videoSize.width,
                            videoHeight = player.videoSize.height,
                            needsSurfaceRecovery = needsSurfaceRecovery
                        )
                        if (shouldRebindSurface) {
                            lifecyclePlayerView?.let {
                                lifecycleVideoOutputRouter.rebindDirectSurfaceIfNeeded()
                                Logger.d("VideoPlayerSection") {
                                    "🎬 ON_RESUME surface rebind applied"
                                }
                            }
                        } else {
                            Logger.d("VideoPlayerSection") {
                                "🌅 ON_RESUME skipped surface rebind (short-background light mode)"
                            }
                        }
                        if (shouldKickPlaybackAfterSurfaceRecovery(
                                playWhenReady = player.playWhenReady,
                                isPlaying = player.isPlaying,
                                playbackState = player.playbackState,
                                hasPlaybackResumeIntent = true
                            )
                        ) {
                            player.play()
                            Logger.d("VideoPlayerSection") {
                                "▶️ ON_RESUME kicked playback after surface recovery"
                            }
                        }
                        danmakuManager.recoverAfterForeground(
                            positionMs = player.currentPosition.coerceAtLeast(0L),
                            playWhenReady = player.playWhenReady,
                            playbackState = player.playbackState
                        )
                    }
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                        hasObservedHostPause = true
                    }
                    androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> {
                        // NavHost 会在新详情页已经 attach 后才销毁旧 entry。这里只能按播放器
                        // 身份解绑监听器；DanmakuView 由 AndroidView.onRelease 做 identity-safe 释放。
                        android.util.Log.d("VideoPlayerSection", " ON_DESTROY: Releasing owned danmaku player")
                        danmakuManager.detachPlayerIfCurrent(lifecyclePlayer)
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        
        // --- [优化] 视频封面逻辑 ---
        // 使用 isFirstFrameRendered + smooth reveal 确保只有在首帧稳定后才揭开封面，避免黑屏和硬切。
        val persistedRenderedFirstFrame = remember(debugInfo.firstFrame) {
            debugInfo.firstFrame.equals("rendered", ignoreCase = true)
        }
        val autoPlayOnOpenEnabled = settings.autoPlayOnOpenEnabled
        var hasManualStartPlaybackIntent by remember(bvid) {
            mutableStateOf(
                playerState.player.mediaItemCount > 0 &&
                    (observedPlayWhenReady || observedIsPlaying)
            )
        }
        LaunchedEffect(uiState, observedPlayWhenReady, observedIsPlaying) {
            if (
                playerState.player.mediaItemCount > 0 &&
                (observedPlayWhenReady || observedIsPlaying)
            ) {
                hasManualStartPlaybackIntent = true
            }
        }
        val playFromManualStartCover = {
            hasManualStartPlaybackIntent = true
            playPlayerFromUserAction(playerState.player)
        }
        val keepCoverForManualStart = shouldKeepCoverForManualStart(
            playWhenReady = observedPlayWhenReady,
            currentPositionMs = playerState.player.currentPosition,
            autoPlayEnabled = autoPlayOnOpenEnabled,
            hasManualStartPlaybackIntent = hasManualStartPlaybackIntent
        )
        // 勿把 currentPosition 放进 remember key：进度推进会反复重建 bootstrap，打乱揭开状态机。
        val coverBootstrapState = remember(
            bvid,
            forceCoverDuringReturnAnimation,
            persistedRenderedFirstFrame,
            keepCoverForManualStart,
            preserveCurrentFrameOnFullscreenChange,
        ) {
            resolveVideoPlayerCoverBootstrapState(
                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                shouldKeepCoverForManualStart = keepCoverForManualStart,
                hasPersistedRenderedFirstFrame = persistedRenderedFirstFrame,
                preserveCurrentFrameOnFullscreenChange = preserveCurrentFrameOnFullscreenChange,
            )
        }
        var isFirstFrameRendered by remember(bvid) {
            mutableStateOf(coverBootstrapState.isFirstFrameRendered)
        }
        var hasStartedSmoothReveal by remember(bvid) {
            mutableStateOf(coverBootstrapState.hasStartedSmoothReveal)
        }
        val revealMotionSpec = remember {
            resolveVideoPlayerRevealMotionSpec()
        }
        val surfaceRevealSpec = remember(
            forceCoverDuringReturnAnimation,
            keepCoverForManualStart,
            hasStartedSmoothReveal,
            revealMotionSpec.surfaceRevealInitialScale
        ) {
            resolveVideoPlayerSurfaceRevealSpec(
                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                shouldKeepCoverForManualStart = keepCoverForManualStart,
                hasStartedSmoothReveal = hasStartedSmoothReveal,
                surfaceRevealInitialScale = revealMotionSpec.surfaceRevealInitialScale
            )
        }
        val playerSurfaceAlpha by animateFloatAsState(
            targetValue = surfaceRevealSpec.alpha,
            animationSpec = tween(revealMotionSpec.surfaceRevealDurationMillis)
        )
        val playerSurfaceScale by animateFloatAsState(
            targetValue = surfaceRevealSpec.scale,
            animationSpec = tween(revealMotionSpec.surfaceRevealDurationMillis)
        )

        // 1. PlayerView (底层) - key 触发 graphicsLayer 强制更新
        //  [修复] 添加 isPortraitFullscreen 到 key，确保从全屏返回时重建 PlayerView 并重新绑定 Surface (解决黑屏问题)
        // HDR/Dolby 必须 SurfaceView：升级到 125/126 后重建 PlayerView 才能把色彩元数据送到屏幕。
        val currentQualityId =
            (uiState as? VideoPlaybackUiState.Success)?.currentQuality ?: 0
        val requiresHdrSurface = requiresHdrSurfaceOutput(
            currentQualityId = currentQualityId,
            colorTransfer = videoInputFormat?.colorInfo?.colorTransfer ?: 0
        )
        val useTextureSurface = shouldUseTextureSurfaceForFlip(
            isFlippedHorizontal = isFlippedHorizontal,
            isFlippedVertical = isFlippedVertical,
            liveBackPreview = liveBackPreview || hasUsedLiveBackPreviewTexture,
            navigationTransformEnabled = useTextureSurfaceForNavigation,
            requiresHdrSurfaceOutput = requiresHdrSurface
        )
        key(isFlippedHorizontal, isFlippedVertical, isPortraitFullscreen, useTextureSurface) {
            val viewportAspectRatio = if (isFullscreen) currentAspectRatio else VideoAspectRatio.FIT
            val playerVideoSize = playerState.player.videoSize
            BoxWithConstraints(
                modifier = playerContentModifier,
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val viewportLayout = remember(maxWidth, maxHeight, viewportAspectRatio) {
                    with(density) {
                        resolveVideoViewportLayout(
                            containerWidth = maxWidth.roundToPx(),
                            containerHeight = maxHeight.roundToPx(),
                            aspectRatio = viewportAspectRatio
                        )
                    }
                }
                val fillMaxViewport = shouldUseFillMaxPlayerViewport(viewportAspectRatio)
                val targetResizeMode = viewportAspectRatio.playerResizeMode

                // 上滑全屏 / 比例切换：容器尺寸与 resizeMode 可能不同步。
                // Media3 仅在 mode 变化时 remeasure；FILL 右下黑边多为旧 measure 残留。
                LaunchedEffect(
                    playerViewRef,
                    viewportLayout.width,
                    viewportLayout.height,
                    targetResizeMode,
                    isFullscreen,
                    isPortraitFullscreen,
                    playerVideoSize.width,
                    playerVideoSize.height,
                    measuredPlayerViewportSize,
                ) {
                    val playerView = playerViewRef ?: return@LaunchedEffect
                    schedulePlayerViewViewportRefresh(
                        playerView = playerView,
                        resizeMode = targetResizeMode,
                        expectedWidth = measuredPlayerViewportSize.width,
                        expectedHeight = measuredPlayerViewportSize.height,
                    )
                }

                AndroidView(
                    factory = { ctx ->
                        val basePlayerView = if (useTextureSurface) {
                            LayoutInflater.from(ctx)
                                .inflate(com.android.purebilibili.R.layout.view_player_texture, null, false) as PlayerView
                        } else {
                            PlayerView(ctx)
                        }
                        basePlayerView.apply {
                            playerViewRef = this
                            // 普通直出同步绑定 PlayerView。
                            // 合集换片会替换 Player，不能等待后续 effect 才补绑，否则解码器可能无输出窗口。
                            player = if (shouldBindInlinePlayerView) playerState.player else null
                            setKeepContentOnPlayerReset(
                                shouldKeepInlinePlayerContentOnReset(
                                    isPortraitFullscreen = isPortraitFullscreen,
                                    forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation
                                )
                            )
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                            useController = false
                            keepScreenOn = keepVideoPlaybackAwake
                            // 非 opaque TextureView：sharedBounds overlay 里短暂无帧时不涂死黑，
                            // 底下的封面垫层/壳背景仍可透出，避免预测返回大黑块。
                            (videoSurfaceView as? TextureView)?.isOpaque = false
                            applyPlayerViewResizeMode(
                                playerView = this,
                                resizeMode = targetResizeMode,
                                forceRelayout = false,
                            )
                            visibility = if (shouldShowInlinePlayerView(
                                    isPortraitFullscreen = isPortraitFullscreen,
                                    forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                                    shouldKeepCoverForManualStart = keepCoverForManualStart
                                )
                            ) {
                                View.VISIBLE
                            } else {
                                View.INVISIBLE
                            }
                        }
                    },
                    update = { playerView ->
                        playerViewRef = playerView
                        playerView.player = if (shouldBindInlinePlayerView) playerState.player else null
                        playerView.setKeepContentOnPlayerReset(
                            shouldKeepInlinePlayerContentOnReset(
                                isPortraitFullscreen = isPortraitFullscreen,
                                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation
                            )
                        )
                        (playerView.videoSurfaceView as? TextureView)?.isOpaque = false
                        applyPlayerViewResizeMode(
                            playerView = playerView,
                            resizeMode = targetResizeMode,
                            forceRelayout = false,
                        )
                        playerView.keepScreenOn = keepVideoPlaybackAwake
                        playerView.visibility = if (shouldShowInlinePlayerView(
                                isPortraitFullscreen = isPortraitFullscreen,
                                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                                shouldKeepCoverForManualStart = keepCoverForManualStart
                            )
                        ) {
                            View.VISIBLE
                        } else {
                            View.INVISIBLE
                        }
                    },
                    modifier = with(density) {
                        val sizeModifier = if (fillMaxViewport) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.size(
                                width = viewportLayout.width.toDp(),
                                height = viewportLayout.height.toDp()
                            )
                        }
                        sizeModifier
                            .onSizeChanged { measuredPlayerViewportSize = it }
                            .alpha(playerSurfaceAlpha)
                            .graphicsLayer {
                                val revealAwareScaleX = scale * playerSurfaceScale
                                val revealAwareScaleY = scale * playerSurfaceScale
                                scaleX = if (isFlippedHorizontal) -revealAwareScaleX else revealAwareScaleX
                                scaleY = if (isFlippedVertical) -revealAwareScaleY else revealAwareScaleY
                                translationX = panX
                                translationY = panY
                            }
                    }
                )

            }
        }

        DisposableEffect(playerState.player) {
            val listener = object : Player.Listener {
                override fun onRenderedFirstFrame() {
                android.util.Log.d("VideoPlayerCover", "🎬 onRenderedFirstFrame triggered")
                isFirstFrameRendered = true
                if (!hasRenderedFirstFrameSinceForegroundRecovery) {
                    hasRenderedFirstFrameSinceForegroundRecovery = true
                    val costMs = (android.os.SystemClock.elapsedRealtime() - foregroundRecoveryStartedAtMs)
                        .coerceAtLeast(0L)
                    Logger.d("VideoPlayerSection") {
                        "✅ Foreground recovery first frame rendered in ${costMs}ms: " +
                            "pos=${playerState.player.currentPosition}, buffered=${playerState.player.bufferedPosition}"
                    }
                }
            }
            
            // 兼容性：同时也监听 Events
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.contains(Player.EVENT_RENDERED_FIRST_FRAME)) {
                    android.util.Log.d("VideoPlayerCover", "🎬 EVENT_RENDERED_FIRST_FRAME triggered")
                    isFirstFrameRendered = true
                    if (!hasRenderedFirstFrameSinceForegroundRecovery) {
                        hasRenderedFirstFrameSinceForegroundRecovery = true
                        val costMs = (android.os.SystemClock.elapsedRealtime() - foregroundRecoveryStartedAtMs)
                            .coerceAtLeast(0L)
                        Logger.d("VideoPlayerSection") {
                            "✅ Foreground recovery first frame event received in ${costMs}ms: " +
                                "pos=${playerState.player.currentPosition}, buffered=${playerState.player.bufferedPosition}"
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // 播放结束显示重播状态（通常由上层逻辑处理，这里不复位封面以免闪烁）
                    // isFirstFrameRendered = false 
                }
            }
        }
        
        playerState.player.addListener(listener)
        
        // 初始化检查：如果播放器已经开始播放且有进度，可能错过了事件
        // [Debug] Log initial check
        if (playerState.player.isPlaying && playerState.player.currentPosition > 0) {
             android.util.Log.d("VideoPlayerCover", "⚠️ Initial check: Already playing at ${playerState.player.currentPosition}, hiding cover. (Might be previous video?)")
             isFirstFrameRendered = true
        } else {
             android.util.Log.d("VideoPlayerCover", "✅ Initial check: Not playing or at start. Keeping cover.")
        }

        onDispose {
            playerState.player.removeListener(listener)
        }
        }

        LaunchedEffect(coverBootstrapState) {
            if (coverBootstrapState.isFirstFrameRendered) {
                isFirstFrameRendered = true
            }
        }
        // Media swap 会清空 debug firstFrame；同步清掉揭开状态，避免旧首帧标志立刻揭开成黑屏。
        LaunchedEffect(persistedRenderedFirstFrame, bvid) {
            if (!persistedRenderedFirstFrame) {
                isFirstFrameRendered = false
                hasStartedSmoothReveal = false
            }
        }
        // 换片或返回强制封面时清掉揭开标记，保证下次进场重新走封面→画面。
        LaunchedEffect(bvid, forceCoverDuringReturnAnimation) {
            if (forceCoverDuringReturnAnimation) {
                hasStartedSmoothReveal = false
            }
        }
    
    // 4. 封面图 — prefer stationary list Coil request (same pixels as home card at rest).
    val detailCoverUrl = (uiState as? VideoPlaybackUiState.Success)?.info?.pic.orEmpty()
    val stationaryListCover = stationaryListCoverUrl.trim()
    val stationaryListKey = stationaryListCoverCacheKey.trim()
    val useStationaryListCover = stationaryListCover.isNotEmpty() && stationaryListKey.isNotEmpty()
    val rawCoverUrl = if (useStationaryListCover) {
        stationaryListCover
    } else {
        resolvePreferredVideoCoverUrl(
            entryCoverUrl = coverUrl,
            detailCoverUrl = detailCoverUrl,
            preferDetailCoverUrl = keepCoverForManualStart && isVerticalVideo
        )
    }
    // Never re-size via fixImageUrl when we already have the list card URL.
    val currentCoverUrl = if (useStationaryListCover) {
        stationaryListCover
    } else {
        FormatUtils.fixImageUrl(rawCoverUrl)
    }
    
    LaunchedEffect(playerState.player, bvid, forceCoverDuringReturnAnimation) {
        if (forceCoverDuringReturnAnimation || isFirstFrameRendered) return@LaunchedEffect
        while (isActive && !isFirstFrameRendered) {
            val player = playerState.player
            if (shouldPromoteFirstFrameByPlaybackFallback(
                    isFirstFrameRendered = isFirstFrameRendered,
                    forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                    playbackState = player.playbackState,
                    playWhenReady = player.playWhenReady,
                    currentPositionMs = player.currentPosition,
                    videoWidth = player.videoSize.width,
                    videoHeight = player.videoSize.height
                )
            ) {
                android.util.Log.d(
                    "VideoPlayerCover",
                    "🎬 Fallback promoted first-frame state by playback progress"
                )
                isFirstFrameRendered = true
                break
            }
            delay(120L)
        }
    }
    // 封面揭开状态机：仅在 forceCover / 手动起播垫底时回退；首帧抖动不得清掉揭开。
    LaunchedEffect(
        bvid,
        isFirstFrameRendered,
        forceCoverDuringReturnAnimation,
        keepCoverForManualStart,
    ) {
        if (
            shouldResetSmoothCoverReveal(
                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                shouldKeepCoverForManualStart = keepCoverForManualStart,
            )
        ) {
            hasStartedSmoothReveal = false
            return@LaunchedEffect
        }
        if (!isFirstFrameRendered) {
            // 等首帧；不要把 hasStartedSmoothReveal 清掉（避免与 bootstrap 竞态）
            return@LaunchedEffect
        }
        if (hasStartedSmoothReveal) return@LaunchedEffect
        delay(revealMotionSpec.coverRevealHoldDelayMillis.toLong())
        if (
            shouldCommitSmoothCoverReveal(
                isFirstFrameRendered = isFirstFrameRendered,
                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                shouldKeepCoverForManualStart = keepCoverForManualStart,
            )
        ) {
            hasStartedSmoothReveal = true
            android.util.Log.d("VideoPlayerCover", "✨ Smooth cover reveal committed for bvid=$bvid")
        }
    }
    val holdEntryCoverUnderlay = shouldHoldEntryCoverUnderlay(
        isFirstFrameRendered = isFirstFrameRendered,
        forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
        shouldKeepCoverForManualStart = keepCoverForManualStart,
        hasStartedSmoothReveal = hasStartedSmoothReveal,
    )
    val showCover = shouldShowCoverImage(
        isFirstFrameRendered = isFirstFrameRendered,
        forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
        shouldKeepCoverForManualStart = keepCoverForManualStart,
        hasStartedSmoothReveal = hasStartedSmoothReveal
    )
    val manualStartPlayButtonLayoutSpec = remember {
        resolveManualStartPlayButtonLayoutSpec()
    }

    LaunchedEffect(
        showControls,
        hasAutoHiddenControlsForCurrentVideo,
        isFirstFrameRendered,
        forceCoverDuringReturnAnimation,
        playerState.player.isPlaying,
        sharedSeekSession.isSliderMoving
    ) {
        if (
            shouldAutoHidePlayerChromeOnPlaybackStart(
                showControls = showControls,
                hasAutoHiddenForCurrentVideo = hasAutoHiddenControlsForCurrentVideo,
                isPlaying = playerState.player.isPlaying,
                isFirstFrameRendered = isFirstFrameRendered,
                forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                isSeekScrubbing = sharedSeekSession.isSliderMoving
            )
        ) {
            showControls = false
            hasAutoHiddenControlsForCurrentVideo = true
        }
    }

    val videoSharedPlaybackIntent = remember(
        keepCoverForManualStart,
        autoPlayOnOpenEnabled,
        hasManualStartPlaybackIntent
    ) {
        val coverFirstBySetting = !autoPlayOnOpenEnabled && !hasManualStartPlaybackIntent
        if (keepCoverForManualStart || coverFirstBySetting) {
            VideoSharedTransitionPlaybackIntent.CoverFirst
        } else {
            resolveVideoSharedTransitionPlaybackIntent(
                clickToPlayEnabled = autoPlayOnOpenEnabled
            )
        }
    }
    val transitionSourceCornerDp =
        LocalVideoCardTransitionBackgroundState.current.sourceCornerDpProvider()
    val videoSharedTransitionVisualSpec = remember(
        sourceRouteForSharedElement,
        transitionSourceCornerDp,
        forceCoverDuringReturnAnimation,
        playerState.player.currentPosition,
        isFullscreen,
        isPortraitFullscreen,
        isVerticalVideo,
        videoSharedPlaybackIntent,
    ) {
        resolveVideoSharedTransitionVisualSpec(
            sourceRoute = sourceRouteForSharedElement,
            sourceCornerDp = transitionSourceCornerDp
                ?: resolveVideoSharedTransitionSourceCornerDp(sourceRouteForSharedElement),
            playbackIntent = videoSharedPlaybackIntent,
            fullscreen = isFullscreen && !isPortraitFullscreen,
            autoPortrait = isPortraitFullscreen || isVerticalVideo,
            initialVertical = isPortraitFullscreen || isVerticalVideo,
            isVerticalVideo = isVerticalVideo,
            isReturning = forceCoverDuringReturnAnimation
        )
    }
    val entryPresentationSpec = remember(
        keepCoverForManualStart,
        forceCoverDuringReturnAnimation,
        isVerticalVideo,
        videoSharedTransitionVisualSpec.targetMode
    ) {
        resolveVideoPlayerEntryPresentationSpec(
            shouldKeepCoverForManualStart = keepCoverForManualStart,
            forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
            isVerticalVideo = isVerticalVideo,
            targetMode = videoSharedTransitionVisualSpec.targetMode
        )
    }
    val fillPlayerViewportForManualStartCover = entryPresentationSpec.fillCoverViewport
    val suppressCoverFade = forceCoverDuringReturnAnimation ||
        videoSharedTransitionVisualSpec.suppressCoverFade ||
        holdEntryCoverUnderlay
    val coverMotionSpec = remember(suppressCoverFade, holdEntryCoverUnderlay) {
        resolveVideoPlayerCoverMotionSpec(
            forceCoverDuringReturnAnimation = suppressCoverFade,
            holdEntryCoverUnderlay = holdEntryCoverUnderlay,
        )
    }
    // 返回强制封面 / 垫底 hold 时硬切；揭开阶段允许淡出，避免「永远盖着封面」。
    val disableCoverFadeAnimation = !coverMotionSpec.shouldAnimateFade
    val coverOverlaySharedBoundsEnabled = shouldEnableCoverOverlaySharedBounds(
        useCoverOverlaySharedBounds = entryPresentationSpec.coverUsesSharedBounds,
        transitionEnabled = transitionEnabled,
        hasSharedTransitionScope = sharedTransitionScope != null,
        hasAnimatedVisibilityScope = animatedVisibilityScope != null,
        sourceRoute = sourceRouteForSharedElement
    )
    val sharedTransitionSpeedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val coverOverlaySharedTransitionMotionSpec = remember(
        sourceRouteForSharedElement,
        transitionEnabled,
        sharedTransitionSpeedSettings
    ) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRouteForSharedElement,
            transitionEnabled = transitionEnabled,
            speedSettings = sharedTransitionSpeedSettings
        )
    }
    val forcedReturnCoverSharedElementSourceRoute = resolveForcedReturnCoverSharedElementSourceRoute(
        sourceRouteForSharedElement
    )
    val coverLayerZIndex = resolveVideoPlayerCoverLayerZIndex(
        playbackIntent = videoSharedPlaybackIntent,
        forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
        shouldKeepCoverForManualStart = keepCoverForManualStart,
    )

    AnimatedVisibility(
        visible = showCover && (currentCoverUrl.isNotEmpty() || entryPresentationSpec.showManualStartPlayButton),
        enter = if (disableCoverFadeAnimation) {
            EnterTransition.None
        } else {
            fadeIn(animationSpec = tween(coverMotionSpec.enterFadeDurationMillis))
        },
        exit = if (disableCoverFadeAnimation) {
            ExitTransition.None
        } else {
            fadeOut(animationSpec = tween(coverMotionSpec.exitFadeDurationMillis))
        },
        modifier = Modifier.zIndex(coverLayerZIndex)
    ) {
        val coverCardShape = RoundedCornerShape(
            resolveVideoPlayerCoverCornerDp(
                sourceCornerDp = videoSharedTransitionVisualSpec.sourceCornerDp,
                playerCornerDp = videoSharedTransitionVisualSpec.targetCornerDp,
                preserveSourceCardCornerDuringSharedReturn =
                    preserveSourceCardCornerDuringSharedReturn,
            ).dp
        )
        val sharedCoverOverlayModifier = if (coverOverlaySharedBoundsEnabled) {
            with(requireNotNull(sharedTransitionScope)) {
                Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState(
                        key = com.android.purebilibili.core.ui.transition.videoCoverSharedElementKey(
                            bvid,
                            sourceRoute = forcedReturnCoverSharedElementSourceRoute
                        )
                    ),
                    animatedVisibilityScope = requireNotNull(animatedVisibilityScope),
                    boundsTransform = { initialBounds, targetBounds ->
                        videoSharedElementBoundsTransformSpec(
                            motion = coverOverlaySharedTransitionMotionSpec,
                            initialBounds = initialBounds,
                            targetBounds = targetBounds
                        )
                    },
                    resizeMode = com.android.purebilibili.core.ui.transition
                        .resolveVideoCardSharedBoundsResizeMode(),
                    clipInOverlayDuringTransition = OverlayClip(coverCardShape)
                )
            }
        } else {
            Modifier
        }

        Box(modifier = playerContentModifier) {
            val coverContainerModifier = if (fillPlayerViewportForManualStartCover) {
                sharedCoverOverlayModifier
                    .matchParentSize()
                    .background(Color.Black)
            } else {
                sharedCoverOverlayModifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .aspectRatio(VIDEO_SHARED_COVER_ASPECT_RATIO)
                    .clip(coverCardShape)
                    .background(Color.Black)
            }
            Box(
                modifier = coverContainerModifier
                    .clickable(enabled = entryPresentationSpec.enableManualStartCoverOverlay) {
                        playFromManualStartCover()
                    }
            ) {
                if (currentCoverUrl.isNotEmpty()) {
                    val sharedCoverCacheKey = if (useStationaryListCover) {
                        stationaryListKey
                    } else {
                        resolveVideoSharedCoverCacheKey(bvid)
                    }
                    val decodeW = stationaryListCoverDecodeWidthPx
                    val decodeH = stationaryListCoverDecodeHeightPx
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(currentCoverUrl)
                            .apply {
                                if (useStationaryListCover && decodeW > 0 && decodeH > 0) {
                                    size(decodeW, decodeH)
                                }
                            }
                            // Same memory/disk key as list AsyncImage when stationary is set.
                            .placeholderMemoryCacheKey(sharedCoverCacheKey)
                            .memoryCacheKey(sharedCoverCacheKey)
                            .diskCacheKey(sharedCoverCacheKey)
                            .crossfade(
                                shouldEnableCoverImageCrossfade(
                                    forceCoverDuringReturnAnimation = forceCoverDuringReturnAnimation,
                                    holdEntryCoverUnderlay = holdEntryCoverUnderlay,
                                )
                            )
                            .build(),
                        contentDescription = null,
                        contentScale = when (entryPresentationSpec.coverContentScaleMode) {
                            VideoPlayerCoverContentScaleMode.Crop -> ContentScale.Crop
                            VideoPlayerCoverContentScaleMode.Fit -> ContentScale.Fit
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                if (entryPresentationSpec.showManualStartPlayButton) {
                    if (manualStartPlayButtonLayoutSpec.showCoverScrim) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.18f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(
                                when (manualStartPlayButtonLayoutSpec.anchor) {
                                    ManualStartPlayButtonAnchor.Center -> Alignment.Center
                                    ManualStartPlayButtonAnchor.CenterEnd -> Alignment.CenterEnd
                                    ManualStartPlayButtonAnchor.BottomEnd -> Alignment.BottomEnd
                                }
                            )
                            .padding(
                                end = manualStartPlayButtonLayoutSpec.endPaddingDp.dp,
                                bottom = if (manualStartPlayButtonLayoutSpec.anchor == ManualStartPlayButtonAnchor.BottomEnd) {
                                    24.dp
                                } else {
                                    0.dp
                                }
                            )
                            .size(
                                width = manualStartPlayButtonLayoutSpec.iconWidthDp.dp,
                                height = manualStartPlayButtonLayoutSpec.iconHeightDp.dp
                            )
                            .clickable {
                                playFromManualStartCover()
                            },
                    ) {
                        if (manualStartPlayButtonLayoutSpec.showTopDecorations) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(x = (-11).dp, y = 4.dp)
                                    .size(width = 12.dp, height = 6.dp)
                                    .clip(AppShapes.container(ContainerLevel.Pill))
                                    .background(Color.White.copy(alpha = 0.96f))
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(x = 11.dp, y = 4.dp)
                                    .size(width = 12.dp, height = 6.dp)
                                    .clip(AppShapes.container(ContainerLevel.Pill))
                                    .background(Color.White.copy(alpha = 0.96f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(if (manualStartPlayButtonLayoutSpec.showTopDecorations) Alignment.BottomCenter else Alignment.Center)
                                .size(width = 58.dp, height = 46.dp)
                                .clip(AppShapes.container(ContainerLevel.Card))
                                .background(Color.White.copy(alpha = 0.96f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play video",
                                tint = Color(0xFF4D5160),
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 2. DanmakuView (使用 ByteDance DanmakuRenderEngine - 覆盖在 PlayerView 上方)
    val shouldShowDanmakuLayer = danmakuHostActive &&
        !forceCoverDuringReturnAnimation && shouldShowDanmakuLayers(
        isInPipMode = isInPipMode,
        danmakuEnabled = danmakuEnabled,
        isPortraitFullscreen = isPortraitFullscreen,
        pipNoDanmakuEnabled = pipNoDanmakuEnabled,
        hostLifecycleStarted = hostLifecycleStarted
    )
    Logger.d("VideoPlayerSection") {
        "DanmakuView check: isInPipMode=$isInPipMode, danmakuEnabled=$danmakuEnabled, pipNoDanmakuEnabled=$pipNoDanmakuEnabled"
    }
        if (shouldShowDanmakuLayer) {
            Logger.d("VideoPlayerSection") { "Conditions met, creating DanmakuView" }
            //  计算状态栏高度
            val statusBarHeightPx = remember(context) {
                val resourceId = context.resources.getIdentifier(
                    "status_bar_height", "dimen", "android"
                )
                if (resourceId > 0) {
                    context.resources.getDimensionPixelSize(resourceId)
                } else {
                    (24 * context.resources.displayMetrics.density).toInt()
                }
            }
            
            // 竖屏「屏幕顶部」模式：弹幕覆盖整个播放器容器；默认仍贴合视频画面，避免落在黑边里。
            val useScreenTopDanmakuSurface = shouldUseScreenTopDanmakuSurface(
                portraitDisplayAreaMode = portraitDanmakuDisplayAreaMode,
                isLandscapeFullscreen = isFullscreen && !isPortraitFullscreen
            )
            val topOffset = resolveDanmakuLayerTopOffsetPx(
                isFullscreen = isFullscreen,
                statusBarHeightPx = statusBarHeightPx,
                useScreenTopSurface = useScreenTopDanmakuSurface
            )
            
            //  [修复] 移除 key(isFullscreen)，避免横竖屏切换时重建 DanmakuView 导致弹幕消失
            // 使用 remember 保存 DanmakuView 引用，在 update 回调中处理尺寸变化
            val viewportAspectRatio = if (isFullscreen) currentAspectRatio else VideoAspectRatio.FIT
            BoxWithConstraints(
                modifier = playerContentModifier
                    .then(
                        if (topOffset > 0) {
                            Modifier.padding(top = with(LocalContext.current.resources.displayMetrics) {
                                (topOffset / density).dp
                            })
                        } else Modifier
                    )
                    .clipToBounds(),
                contentAlignment = if (useScreenTopDanmakuSurface) {
                    Alignment.TopCenter
                } else {
                    Alignment.Center
                }
            ) {
                val density = LocalDensity.current
                val viewportLayout = remember(maxWidth, maxHeight, viewportAspectRatio) {
                    with(density) {
                        resolveVideoViewportLayout(
                            containerWidth = maxWidth.roundToPx(),
                            containerHeight = maxHeight.roundToPx(),
                            aspectRatio = viewportAspectRatio
                        )
                    }
                }
                val danmakuSurfaceModifier = if (useScreenTopDanmakuSurface) {
                    Modifier.fillMaxSize()
                } else {
                    with(density) {
                        Modifier.size(
                            width = viewportLayout.width.toDp(),
                            height = viewportLayout.height.toDp()
                        )
                    }
                }
                AndroidView(
                    factory = { ctx ->
                        DanmakuView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.TRANSPARENT)
                            configureAsPassiveDanmakuOverlay()
                            danmakuManager.attachView(this)
                            Logger.d("VideoPlayerSection") {
                                "DanmakuView (RenderEngine) created, isFullscreen=$isFullscreen"
                            }
                        }
                    },
                    update = { view ->
                        //  [关键] 横竖屏切换后视图尺寸变化时，重新 attachView 确保弹幕正确显示
                        Logger.d("VideoPlayerSection") {
                            "DanmakuView update: size=${view.width}x${view.height}, isFullscreen=$isFullscreen"
                        }
                        // 只有当视图有有效尺寸时才 re-attach
                        if (view.width > 0 && view.height > 0) {
                            val sizeTag = "${view.width}x${view.height}"
                            if (view.tag != sizeTag) {
                                view.tag = sizeTag
                                danmakuManager.attachView(view)
                            }
                        }
                    },
                    onRelease = { view ->
                        // 仅当本 view 仍是当前绑定的弹幕视图时才解绑；
                        // 相关推荐跳转后旧页面销毁不能清掉新页面已接管的 view/controller。
                        danmakuManager.releaseViewIfCurrent(view)
                    },
                    modifier = danmakuSurfaceModifier
                )
            }
        }

        // 3. 高级弹幕层 (Mode 7) - 覆盖在标准弹幕上方
        val advancedDanmakuList by danmakuManager.advancedDanmakuFlow.collectAsStateWithLifecycle()

        if (shouldShowDanmakuLayer && advancedDanmakuList.isNotEmpty()) {
             Box(
                modifier = playerContentModifier
                    .clipToBounds()
            ) {
                com.android.purebilibili.feature.video.ui.overlay.AdvancedDanmakuOverlay(
                    danmakuList = advancedDanmakuList,
                    player = playerState.player,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val commandDanmakuList by danmakuManager.commandDanmakuFlow.collectAsStateWithLifecycle()
        val visibleCommandDanmakuList = remember(commandDanmakuList, danmakuHideInteractiveCommands) {
            filterVisibleCommandDanmakuItems(
                items = commandDanmakuList,
                hideInteractiveCommands = danmakuHideInteractiveCommands
            )
        }
        if (shouldShowDanmakuLayer && visibleCommandDanmakuList.isNotEmpty()) {
            Box(
                modifier = playerContentModifier
                    .clipToBounds()
            ) {
                com.android.purebilibili.feature.video.ui.overlay.CommandDanmakuOverlay(
                    items = visibleCommandDanmakuList,
                    player = playerState.player,
                    onFollowClick = onToggleFollow,
                    onTripleClick = onTriple,
                    onVoteSubmit = { item, option ->
                        val success = uiState as? VideoPlaybackUiState.Success
                        val score = option.score
                        if (success != null && score != null && item.voteId.isNotBlank()) {
                            settingsScope.launch {
                                val result = settingsActions.submitGradeDanmaku(
                                    success.info.aid,
                                    success.info.cid,
                                    item.startTimeMs,
                                    item.voteId,
                                    score
                                )
                                if (result.isFailure) {
                                    android.util.Log.w(
                                        "VideoPlayerSection",
                                        "Vote submit failed: ${result.exceptionOrNull()?.message}"
                                    )
                                }
                            }
                        }
                    },
                    isFollowing = isFollowed,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 4. B站字幕叠加层（支持中英双语）
        val subtitleFeatureEnabled = isSubtitleFeatureEnabledForUser()
        val subtitleBelongsToCurrentVideo = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            success.subtitleOwnerBvid == success.info.bvid &&
                success.subtitleOwnerCid == success.info.cid &&
                success.info.cid > 0L
        }
        val subtitlePrimaryAvailable = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            subtitleBelongsToCurrentVideo && success.subtitlePrimaryCues.isNotEmpty()
        }
        val subtitleSecondaryAvailable = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            subtitleBelongsToCurrentVideo && success.subtitleSecondaryCues.isNotEmpty()
        }
        val subtitlePrimaryTrackBound = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            subtitleBelongsToCurrentVideo &&
                (!success.subtitlePrimaryTrackKey.isNullOrBlank() || !success.subtitlePrimaryLanguage.isNullOrBlank())
        }
        val subtitleSecondaryTrackBound = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            subtitleBelongsToCurrentVideo &&
                (!success.subtitleSecondaryTrackKey.isNullOrBlank() || !success.subtitleSecondaryLanguage.isNullOrBlank())
        }
        val subtitlePrimaryLikelyAi = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            subtitleBelongsToCurrentVideo && success.subtitlePrimaryLikelyAi
        }
        val subtitleSecondaryLikelyAi = remember(uiState, subtitleFeatureEnabled) {
            if (!subtitleFeatureEnabled) return@remember false
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember false
            subtitleBelongsToCurrentVideo && success.subtitleSecondaryLikelyAi
        }
        val subtitleControlAvailability = remember(
            subtitleFeatureEnabled,
            subtitlePrimaryTrackBound,
            subtitleSecondaryTrackBound,
            subtitlePrimaryAvailable,
            subtitleSecondaryAvailable
        ) {
            if (!subtitleFeatureEnabled) {
                com.android.purebilibili.core.video.subtitle.SubtitleControlAvailability(
                    trackAvailable = false,
                    primarySelectable = false,
                    secondarySelectable = false
                )
            } else {
                resolveSubtitleControlAvailability(
                    primaryTrackBound = subtitlePrimaryTrackBound,
                    secondaryTrackBound = subtitleSecondaryTrackBound,
                    primaryCueAvailable = subtitlePrimaryAvailable,
                    secondaryCueAvailable = subtitleSecondaryAvailable
                )
            }
        }
        val subtitleAutoModeMuted = runCatching {
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0
        }.getOrDefault(false) || playerState.player.volume <= 0f
        val subtitleToggleKey = remember(uiState, bvid, subtitleAutoPreference) {
            val success = uiState as? VideoPlaybackUiState.Success
            if (success == null) {
                "no-subtitle"
            } else {
                "${bvid}_${success.info.cid}_${success.subtitlePrimaryLanguage}_${success.subtitleSecondaryLanguage}_${success.subtitlePrimaryLikelyAi}_${success.subtitleSecondaryLikelyAi}_${subtitleAutoPreference.name}"
            }
        }
        var localSubtitleDisplayModePreference by rememberSaveable("${subtitleToggleKey}_mode") {
            mutableStateOf(
                if (subtitleFeatureEnabled) {
                    resolveSubtitleDisplayModeByAutoPreference(
                        preference = subtitleAutoPreference,
                        hasPrimaryTrack = subtitlePrimaryTrackBound,
                        hasSecondaryTrack = subtitleSecondaryTrackBound,
                        primaryTrackLikelyAi = subtitlePrimaryLikelyAi,
                        secondaryTrackLikelyAi = subtitleSecondaryLikelyAi,
                        isMuted = subtitleAutoModeMuted
                    )
                } else {
                    SubtitleDisplayMode.OFF
                }
            )
        }
        var subtitleLargeTextByUser by rememberSaveable("${subtitleToggleKey}_large") {
            mutableStateOf(false)
        }
        val subtitleTextSizeSpec = remember(uiLayoutWidthDp, subtitleLargeTextByUser) {
            com.android.purebilibili.core.video.subtitle.resolveSubtitleTextSizeSpec(
                playerWidthDp = uiLayoutWidthDp,
                largeTextEnabled = subtitleLargeTextByUser
            )
        }
        val subtitleDisplayModePreference = subtitleDisplayModePreferenceOverride ?: localSubtitleDisplayModePreference
        val applySubtitleDisplayModePreferenceChange: (SubtitleDisplayMode) -> Unit = remember(
            subtitleDisplayModePreferenceOverride,
            onSubtitleDisplayModePreferenceOverrideChange
        ) {
            if (subtitleDisplayModePreferenceOverride != null) {
                onSubtitleDisplayModePreferenceOverrideChange
            } else {
                { mode -> localSubtitleDisplayModePreference = mode }
            }
        }
        val subtitleDisplayMode = remember(
            subtitleFeatureEnabled,
            subtitleBelongsToCurrentVideo,
            subtitleDisplayModePreference,
            subtitlePrimaryAvailable,
            subtitleSecondaryAvailable
        ) {
            if (!subtitleFeatureEnabled || !subtitleBelongsToCurrentVideo) {
                SubtitleDisplayMode.OFF
            } else {
                normalizeSubtitleDisplayMode(
                    preferredMode = subtitleDisplayModePreference,
                    hasPrimaryTrack = subtitlePrimaryAvailable,
                    hasSecondaryTrack = subtitleSecondaryAvailable
                )
            }
        }
        val subtitleOverlayEnabled = subtitleFeatureEnabled && subtitleDisplayMode != SubtitleDisplayMode.OFF
        val subtitlePrimaryLabel = remember(uiState) {
            val success = uiState as? VideoPlaybackUiState.Success
            val selectedTrack = success?.subtitleTracks?.firstOrNull {
                it.trackKey == success.subtitlePrimaryTrackKey
            }
            if (selectedTrack != null) {
                return@remember resolveSubtitleTrackDisplayLabel(selectedTrack)
            }
            resolveSubtitleLanguageLabel(
                languageCode = success?.takeIf {
                    it.subtitleOwnerBvid == it.info.bvid && it.subtitleOwnerCid == it.info.cid
                }?.subtitlePrimaryLanguage,
                fallbackLabel = "中文"
            )
        }
        val subtitleSecondaryLabel = remember(uiState) {
            val success = uiState as? VideoPlaybackUiState.Success
            val selectedTrack = success?.subtitleTracks?.firstOrNull {
                it.trackKey == success.subtitleSecondaryTrackKey
            }
            if (selectedTrack != null) {
                return@remember resolveSubtitleTrackDisplayLabel(selectedTrack)
            }
            resolveSubtitleLanguageLabel(
                languageCode = success?.takeIf {
                    it.subtitleOwnerBvid == it.info.bvid && it.subtitleOwnerCid == it.info.cid
                }?.subtitleSecondaryLanguage,
                fallbackLabel = "英文"
            )
        }
        val subtitleTrackOptions = remember(uiState) {
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember emptyList()
            if (success.subtitleOwnerBvid != success.info.bvid || success.subtitleOwnerCid != success.info.cid) {
                return@remember emptyList()
            }
            buildSubtitleTrackOptions(
                tracks = success.subtitleTracks,
                selectedTrackKey = success.subtitlePrimaryTrackKey
            )
        }

        val subtitlePollingIdentity = remember(uiState) {
            val success = uiState as? VideoPlaybackUiState.Success
            com.android.purebilibili.core.video.subtitle.resolveSubtitlePositionPollingIdentity(
                bvid = success?.info?.bvid,
                cid = success?.info?.cid ?: 0L,
            )
        }
        // 禁止 key=uiState：Success 频繁替换会把 progress 重置为 0 → 字幕疯狂闪。
        val subtitlePositionMs by produceState(
            initialValue = playerState.player.currentPosition.coerceAtLeast(0L),
            key1 = playerState.player,
            key2 = subtitlePollingIdentity,
        ) {
            value = playerState.player.currentPosition.coerceAtLeast(0L)
            while (isActive) {
                value = playerState.player.currentPosition.coerceAtLeast(0L)
                delay(if (playerState.player.isPlaying) 120L else 260L)
            }
        }
        val subtitlePrimaryRawText = remember(
            uiState,
            subtitleFeatureEnabled,
            subtitlePositionMs,
            subtitleDisplayMode,
        ) {
            if (!subtitleFeatureEnabled) return@remember null
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember null
            if (success.subtitleOwnerBvid != success.info.bvid || success.subtitleOwnerCid != success.info.cid) {
                return@remember null
            }
            if (!shouldRenderPrimarySubtitle(subtitleDisplayMode)) return@remember null
            resolveSubtitleTextAt(success.subtitlePrimaryCues, subtitlePositionMs)
        }
        val subtitleSecondaryRawText = remember(
            uiState,
            subtitleFeatureEnabled,
            subtitlePositionMs,
            subtitleDisplayMode,
        ) {
            if (!subtitleFeatureEnabled) return@remember null
            val success = uiState as? VideoPlaybackUiState.Success ?: return@remember null
            if (success.subtitleOwnerBvid != success.info.bvid || success.subtitleOwnerCid != success.info.cid) {
                return@remember null
            }
            if (!shouldRenderSecondarySubtitle(subtitleDisplayMode)) return@remember null
            resolveSubtitleTextAt(success.subtitleSecondaryCues, subtitlePositionMs)
        }
        // 句间短空窗 sticky，避免 120ms 轮询在边界 null↔有字 来回切。
        var stickyPrimaryText by remember(subtitlePollingIdentity) { mutableStateOf<String?>(null) }
        var stickySecondaryText by remember(subtitlePollingIdentity) { mutableStateOf<String?>(null) }
        var primaryBlankSinceMs by remember(subtitlePollingIdentity) { mutableLongStateOf(-1L) }
        var secondaryBlankSinceMs by remember(subtitlePollingIdentity) { mutableLongStateOf(-1L) }
        val nowForSticky = subtitlePositionMs
        val subtitlePrimaryText = remember(subtitlePrimaryRawText, stickyPrimaryText, primaryBlankSinceMs, nowForSticky) {
            val blankGap = if (subtitlePrimaryRawText.isNullOrBlank() && primaryBlankSinceMs >= 0L) {
                (nowForSticky - primaryBlankSinceMs).coerceAtLeast(0L)
            } else {
                0L
            }
            com.android.purebilibili.core.video.subtitle.resolveStickySubtitleText(
                currentText = subtitlePrimaryRawText,
                previousText = stickyPrimaryText,
                blankGapMs = blankGap,
            )
        }
        val subtitleSecondaryText = remember(subtitleSecondaryRawText, stickySecondaryText, secondaryBlankSinceMs, nowForSticky) {
            val blankGap = if (subtitleSecondaryRawText.isNullOrBlank() && secondaryBlankSinceMs >= 0L) {
                (nowForSticky - secondaryBlankSinceMs).coerceAtLeast(0L)
            } else {
                0L
            }
            com.android.purebilibili.core.video.subtitle.resolveStickySubtitleText(
                currentText = subtitleSecondaryRawText,
                previousText = stickySecondaryText,
                blankGapMs = blankGap,
            )
        }
        SideEffect {
            if (!subtitlePrimaryRawText.isNullOrBlank()) {
                stickyPrimaryText = subtitlePrimaryRawText
                primaryBlankSinceMs = -1L
            } else if (primaryBlankSinceMs < 0L) {
                primaryBlankSinceMs = nowForSticky
            }
            if (!subtitleSecondaryRawText.isNullOrBlank()) {
                stickySecondaryText = subtitleSecondaryRawText
                secondaryBlankSinceMs = -1L
            } else if (secondaryBlankSinceMs < 0L) {
                secondaryBlankSinceMs = nowForSticky
            }
        }
        val keepSubtitleOverlayMounted =
            uiState is VideoPlaybackUiState.Success &&
                com.android.purebilibili.core.video.subtitle.shouldKeepSubtitleOverlayMounted(
                    overlayEnabled = subtitleOverlayEnabled,
                    isInPipMode = isInPipMode,
                    isAudioOnly = false,
                    suppressOverlay = suppressSubtitleOverlay,
                )
        if (keepSubtitleOverlayMounted) {
            val navigationBottomInsetPx = WindowInsets.navigationBars.getBottom(localDensity)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(end = animatedEndDrawerReservedWidth)
                    .offset {
                        val viewportHeightPx = measuredPlayerViewportSize.height
                            .takeIf { it > 0 }
                            ?: with(localDensity) { configuration.screenHeightDp.dp.roundToPx() }
                        val subtitleBottomOffsetPx = resolveSubtitleBottomOffsetPx(
                            isFullscreen = isFullscreen,
                            controlsVisible = showControls,
                            navigationInsetPx = navigationBottomInsetPx,
                            bottomControlsHeightPx = measuredBottomControlsHeightPx,
                            density = localDensity.density
                        )
                        IntOffset(
                            x = 0,
                            y = (viewportHeightPx * subtitleVerticalOffsetFraction).roundToInt() -
                                subtitleBottomOffsetPx
                        )
                    }
                    .fillMaxWidth(0.9f)
                    .padding(horizontal = 10.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    // 横屏全屏 / 详情播放器均可拖动字幕纵向位置，松手写入偏好。
                    .pointerInput(measuredPlayerViewportSize.height) {
                        detectDragGestures(
                            onDragStart = {
                                isDraggingSubtitleOffset = true
                            },
                            onDragEnd = {
                                isDraggingSubtitleOffset = false
                                settingsActions.setSubtitleVerticalOffsetFraction(
                                    subtitleVerticalOffsetFraction
                                )
                            },
                            onDragCancel = {
                                isDraggingSubtitleOffset = false
                            },
                            onDrag = { change, dragAmount ->
                                val screenHeightPx = measuredPlayerViewportSize.height
                                    .takeIf { it > 0 }
                                    ?.toFloat()
                                    ?: with(localDensity) {
                                        configuration.screenHeightDp.dp.toPx()
                                    }.coerceAtLeast(1f)
                                subtitleVerticalOffsetFraction =
                                    normalizeSubtitleVerticalOffsetFraction(
                                        subtitleVerticalOffsetFraction + dragAmount.y / screenHeightPx
                                    )
                                change.consume()
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val subtitleShadow = Shadow(
                    color = Color.Black.copy(alpha = 0.85f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 6f
                )
                val showPrimaryLine = !subtitlePrimaryText.isNullOrBlank()
                val showSecondaryLine = !subtitleSecondaryText.isNullOrBlank()
                val secondaryAsPrimaryLine = showSecondaryLine && !showPrimaryLine
                // 行容器常驻：用空串占位而不是 if 拆装 Text，减少 quantize 边界闪一下。
                AppText(
                    text = subtitleSecondaryText.orEmpty(),
                    color = Color.White.copy(alpha = if (showSecondaryLine) 0.88f else 0f),
                    fontSize = if (secondaryAsPrimaryLine) {
                        subtitleTextSizeSpec.primarySp.sp
                    } else {
                        subtitleTextSizeSpec.secondarySp.sp
                    },
                    fontWeight = if (secondaryAsPrimaryLine) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(shadow = subtitleShadow),
                    modifier = Modifier.then(
                        if (showSecondaryLine) Modifier else Modifier.height(0.dp)
                    )
                )
                AppText(
                    text = subtitlePrimaryText.orEmpty(),
                    color = Color.White.copy(alpha = if (showPrimaryLine) 1f else 0f),
                    fontSize = subtitleTextSizeSpec.primarySp.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    style = LocalTextStyle.current.copy(shadow = subtitleShadow),
                    modifier = Modifier.then(
                        if (showPrimaryLine) Modifier else Modifier.height(0.dp)
                    )
                )
            }
        }

        // 🖼️ [修复] 手势指示器：仅在亮度/音量/Seek 模式显示，避免上滑全屏时误显示亮度图标
        val shouldShowGestureIndicator = isGestureVisible &&
            !isInPipMode &&
            (gestureMode == VideoGestureMode.Seek ||
                gestureMode == VideoGestureMode.Brightness ||
                gestureMode == VideoGestureMode.Volume)
        val shouldShowSeekIndicator = shouldShowGestureIndicator && gestureMode == VideoGestureMode.Seek
        val shouldShowLevelIndicator = shouldShowGestureIndicator &&
            (gestureMode == VideoGestureMode.Brightness || gestureMode == VideoGestureMode.Volume)

        if (shouldShowSeekIndicator) {
            // 🖼️ Seek 模式：显示带缩略图的预览气泡
            // zIndex must sit above forced return cover (100f) so landscape seek feedback is never buried.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(120f),
                contentAlignment = Alignment.Center
            ) {
                if (videoshotData != null && videoshotData.isValid) {
                    // 🖼️ 有缩略图：显示完整预览
                    com.android.purebilibili.feature.video.ui.components.SeekPreviewBubble(
                        videoshotData = videoshotData,
                        targetPositionMs = seekTargetTime,
                        currentPositionMs = startPosition,
                        durationMs = playerState.player.duration,
                        offsetX = 0f,
                        containerWidth = 0f,
                        placement = com.android.purebilibili.feature.video.ui.components.SeekPreviewBubblePlacement.Centered
                    )
                } else {
                    com.android.purebilibili.feature.video.ui.components.SeekPreviewBubbleSimple(
                        targetPositionMs = seekTargetTime,
                        currentPositionMs = startPosition,
                        offsetX = 0f,
                        containerWidth = 0f,
                        placement = com.android.purebilibili.feature.video.ui.components.SeekPreviewBubblePlacement.Centered
                    )
                }
            }
        }

        // Theme-native volume / brightness feedback (MD3 / iOS / MIUIX).
        GestureLevelOverlayHost(
            visible = shouldShowLevelIndicator,
            mode = gestureMode,
            percent = gesturePercent
        )

        AnimatedVisibility(
            visible = orientationHintVisible && !isInPipMode,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 196.dp),
            enter = fadeIn(animationSpec = tween(gestureMotionSpec.orientationHintEnterFadeDurationMillis)) +
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(gestureMotionSpec.orientationHintEnterTransformDurationMillis)
                ) +
                slideInVertically(
                    initialOffsetY = { -it / 5 },
                    animationSpec = tween(gestureMotionSpec.orientationHintEnterTransformDurationMillis)
                ),
            exit = fadeOut(animationSpec = tween(gestureMotionSpec.orientationHintExitDurationMillis)) +
                scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(gestureMotionSpec.orientationHintExitDurationMillis)
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppIcon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                AppText(
                    text = orientationHintText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
        
        //  [新增] 双击跳转视觉反馈 (±Ns 提示)
        LaunchedEffect(seekFeedbackGeneration) {
            if (seekFeedbackGeneration > 0L) {
                kotlinx.coroutines.delay(800)
                seekFeedbackVisible = false
            }
        }
        
        AnimatedVisibility(
            visible = seekFeedbackVisible && !isInPipMode,
            modifier = Modifier.align(Alignment.Center),
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut(targetScale = 0.8f) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(uiLayoutPolicy.seekFeedbackSizeDp.dp)
                    .background(Color.Black.copy(0.75f), AppShapes.container(ContainerLevel.Floating)),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = seekFeedbackText ?: "",
                    color = if (seekFeedbackText?.startsWith("+") == true) com.android.purebilibili.core.theme.iOSGreen else com.android.purebilibili.core.theme.iOSRed,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        TwoFingerSpeedFeedbackOverlay(
            visible = twoFingerSpeedFeedbackVisible && !isInPipMode,
            speed = twoFingerFeedbackSpeed,
            mode = twoFingerSpeedMode,
            hazeState = overlayDrawerHazeState
        )

        //  [新增] 缩放还原按钮 (仅在放大时显示)
        AnimatedVisibility(
            visible = scale > 1.05f && !isInPipMode,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = uiLayoutPolicy.restoreButtonBottomOffsetDp.dp), // 避开底部进度条位置
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            AppButton(
                onClick = {
                    scale = 1f
                    panX = 0f
                    panY = 0f
                    // showControls = true // 可选：还原后显示控制栏
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(
                    horizontal = uiLayoutPolicy.restoreButtonHorizontalPaddingDp.dp,
                    vertical = uiLayoutPolicy.restoreButtonVerticalPaddingDp.dp
                ),
                shape = AppShapes.container(ContainerLevel.Floating)
            ) {
                AppIcon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "还原画面",
                    modifier = Modifier.size(uiLayoutPolicy.restoreButtonIconSizeDp.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppText(
                    text = "还原画面",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        
        AnimatedVisibility(
            visible = longPressSpeedLockEnabled && isLongPressing && !longPressSpeedLocked && !isInPipMode,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(animationSpec = tween(gestureMotionSpec.longPressHintDurationMillis)),
            exit = fadeOut(animationSpec = tween(gestureMotionSpec.longPressHintDurationMillis))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val lockZoneVisual = resolveLongPressSpeedLockZoneVisualPolicy()
                val zoneModifier = Modifier
                    .fillMaxWidth()
                    .height(longPressSpeedLockSensitivity.lockZoneHeightDp.dp)
                val markerColor = MaterialTheme.colorScheme.primary
                Box(modifier = zoneModifier.align(Alignment.TopCenter)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(lockZoneVisual.edgeGradientHeightDp.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        markerColor.copy(alpha = lockZoneVisual.edgeGradientAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(lockZoneVisual.centerMarkerWidthFraction)
                            .height(lockZoneVisual.centerMarkerHeightDp.dp)
                            .clip(AppShapes.container(ContainerLevel.Pill))
                            .background(markerColor.copy(alpha = lockZoneVisual.centerMarkerAlpha))
                    )
                }
                Box(
                    modifier = zoneModifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = lockZoneVisual.bottomVisualOffsetDp.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(lockZoneVisual.edgeGradientHeightDp.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        markerColor.copy(alpha = lockZoneVisual.edgeGradientAlpha)
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(lockZoneVisual.centerMarkerWidthFraction)
                            .height(lockZoneVisual.centerMarkerHeightDp.dp)
                            .clip(AppShapes.container(ContainerLevel.Pill))
                            .background(markerColor.copy(alpha = lockZoneVisual.centerMarkerAlpha))
                    )
                }
            }
        }

        // 长按倍速提示保持轻量，避免遮挡视频主体内容。
        AnimatedVisibility(
            visible = shouldShowLongPressSpeedFeedback(
                isLongPressing = isLongPressing,
                isPlaybackSurfaceActive = !isInPipMode,
                hintDismissed = longPressSpeedHintDismissed,
                hintHidden = longPressSpeedHintHidden,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                // 关闭「播放页沉浸状态栏」后 contentTopInset 为 0，此时按实时状态栏 inset 避让，
                // 避免提示落入系统状态栏区域被遮挡。
                .padding(
                    top = contentTopInset
                        .coerceAtLeast(WindowInsets.statusBars.asPaddingValues().calculateTopPadding()) +
                        16.dp
                ),
            enter = fadeIn(animationSpec = tween(gestureMotionSpec.longPressHintDurationMillis)) +
                slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(animationSpec = tween(gestureMotionSpec.longPressHintDurationMillis)) +
                slideOutVertically(targetOffsetY = { -it })
        ) {
            AppSurface(
                shape = AppShapes.scaledContainer(ContainerLevel.Field, longPressSpeedHintScale),
                color = Color.Black.copy(alpha = longPressSpeedHintAlpha),
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppText(
                        text = if (longPressSpeedLocked) {
                            "已锁定 ${effectiveLongPressSpeed}x"
                        } else {
                            "倍速播放中 ${effectiveLongPressSpeed}x"
                        },
                        modifier = Modifier.padding(
                            start = 8.dp * longPressSpeedHintScale,
                            end = if (shouldShowLongPressSpeedHintCloseButton(longPressSpeedHintCloseEnabled)) {
                                2.dp
                            } else {
                                8.dp * longPressSpeedHintScale
                            },
                            top = 5.dp * longPressSpeedHintScale,
                            bottom = 5.dp * longPressSpeedHintScale,
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * longPressSpeedHintScale,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (shouldShowLongPressSpeedHintCloseButton(longPressSpeedHintCloseEnabled)) {
                        AppIconButton(
                            onClick = { longPressSpeedHintDismissed = true },
                            modifier = Modifier.size(36.dp * longPressSpeedHintScale),
                        ) {
                            AppIcon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "关闭倍速提示",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp * longPressSpeedHintScale),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = shouldShowLongPressSpeedLockHint(
                hintRequested = showLongPressSpeedLockHint,
                isLongPressing = isLongPressing,
                isInPipMode = isInPipMode,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 62.dp, start = 16.dp, end = 16.dp),
            enter = fadeIn(animationSpec = tween(gestureMotionSpec.longPressHintDurationMillis)) +
                slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut(animationSpec = tween(gestureMotionSpec.longPressHintDurationMillis))
        ) {
            AppSurface(
                shape = AppShapes.scaledContainer(ContainerLevel.Floating, longPressSpeedHintScale),
                color = Color.Black.copy(alpha = (0.62f * longPressSpeedHintAlpha).coerceIn(0f, 1f)),
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(
                        horizontal = 12.dp * longPressSpeedHintScale,
                        vertical = 8.dp * longPressSpeedHintScale
                    )
                ) {
                    AppText(
                        text = "需要长按锁定倍速吗？",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize * longPressSpeedHintScale
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextButton(
                            onClick = {
                                showLongPressSpeedLockHint = false
                                hasShownLongPressSpeedLockHintLocally = true
                                settingsActions.setLongPressSpeedLockEnabled(true)
                                settingsActions.setLongPressSpeedLockHintShown(true)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            AppText("开启锁定")
                        }
                        AppTextButton(
                            onClick = {
                                showLongPressSpeedLockHint = false
                                hasShownLongPressSpeedLockHintLocally = true
                                finishLongPressSpeedGesture(gestureEnded = true)
                                settingsActions.setLongPressSpeedLockHintShown(true)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            AppText("不再提示")
                        }
                    }
                }
            }
        }

        if (uiState is VideoPlaybackUiState.Success && !isInPipMode) {
            val currentPageIndex = uiState.info.pages.indexOfFirst { it.cid == uiState.info.cid }.coerceAtLeast(0)
            val displayedQualityId = resolveDisplayedQualityId(
                currentQuality = uiState.currentQuality,
                requestedQuality = uiState.requestedQuality,
                isQualitySwitching = uiState.isQualitySwitching
            )
            @Composable
            fun RenderVideoPlayerOverlay() {
                VideoPlayerOverlay(
                player = playerState.player,
                title = uiState.info.title,
                // [修复] 竖屏全屏模式下隐藏底部 Overlay，避免进度状态冲突
                // 手势调节音量/亮度/进度时隐藏控制栏，避免盖住中间手势 UI
                isVisible = showControls &&
                    !isPortraitFullscreen &&
                    gestureMode == VideoGestureMode.None,
                onToggleVisible = { showControls = !showControls },
                isFullscreen = isFullscreen,
                currentQualityLabel = uiState.qualityLabels.getOrNull(uiState.qualityIds.indexOf(displayedQualityId)) ?: "自动",
                qualityLabels = uiState.qualityLabels,
                qualityIds = uiState.qualityIds,
                switchableQualityIds = uiState.switchableQualityIds,
                onQualitySelected = { index ->
                    val id = uiState.qualityIds.getOrNull(index) ?: 0
                    onQualityChange(id)
                },
                onBack = onBack,
                onHomeClick = resolveVideoPlayerOverlayHomeClick(
                    onBack = onBack,
                    onHomeClick = onHomeClick
                ),
                onToggleFullscreen = onToggleFullscreen,
                
                // 🔒 [新增] 屏幕锁定
                isScreenLocked = isScreenLocked,
                onLockToggle = { isScreenLocked = !isScreenLocked },
                //  [关键] 传入设置状态和调试信息
                insightMode = playerInsightMode,
                debugInfo = debugInfo,
                playerViewportSize = measuredPlayerViewportSize,
                diagnosticEvents = diagnosticEvents,
                pendingUserAction = pendingUserAction,
                hasPendingSeekResume = sharedSeekSession.pendingSeekPositionMs != null,
                playerDiagnosticLoggingEnabled = playerDiagnosticLoggingEnabled,
                //  [新增] 传入清晰度切换状态和会员状态
                isQualitySwitching = uiState.isQualitySwitching,
                isBuffering = isBuffering,  // 缓冲状态
                onBottomControlsSizeChanged = { measuredBottomControlsHeightPx = it },
                isLoggedIn = uiState.isLoggedIn,
                isVip = uiState.isVip,
                //  [新增] 弹幕开关和设置
                danmakuEnabled = danmakuEnabled,
                onDanmakuToggle = {
                    val newState = !danmakuEnabled
                    danmakuManager.isEnabled = newState
                    if (!newState) {
                        danmakuManager.clear()
                    }
                    settingsActions.setDanmakuEnabled(newState, activeDanmakuScope)
                    queueDanmakuCloudSync(enabled = newState)
                    //  记录弹幕开关事件
                    com.android.purebilibili.core.util.AnalyticsHelper.logDanmakuToggle(newState)
                },
                onDanmakuInputClick = onDanmakuInputClick,
                danmakuComposerVisible = danmakuComposerVisible,
                onDismissDanmakuComposer = onDismissDanmakuComposer,
                onSendDanmakuComposer = onSendDanmakuComposer,
                isSendingDanmakuComposer = isSendingDanmakuComposer,
                danmakuComposerInitialText = danmakuComposerInitialText,
                danmakuComposerInitialAttentionCommand = danmakuComposerInitialAttentionCommand,
                danmakuComposerInitialColor = danmakuComposerInitialColor,
                danmakuComposerInitialMode = danmakuComposerInitialMode,
                danmakuComposerInitialFontSize = danmakuComposerInitialFontSize,
                onDanmakuComposerDraftChange = onDanmakuComposerDraftChange,
                onDanmakuComposerSelectionChange = onDanmakuComposerSelectionChange,
                danmakuOpacity = danmakuOpacity,
                danmakuFontScale = danmakuFontScale,
                danmakuFontWeight = danmakuFontWeight,
                danmakuSpeed = danmakuSpeed,
                danmakuDisplayArea = danmakuDisplayArea,
                danmakuStrokeWidth = danmakuStrokeWidth,
                danmakuLineHeight = danmakuLineHeight,
                danmakuScrollDurationSeconds = danmakuScrollDurationSeconds,
                danmakuStaticDurationSeconds = danmakuStaticDurationSeconds,
                danmakuScrollFixedVelocity = danmakuScrollFixedVelocity,
                danmakuStaticToScroll = danmakuStaticToScroll,
                danmakuMassiveMode = danmakuMassiveMode,
                danmakuMergeDuplicates = danmakuMergeDuplicates,
                danmakuDuplicateMergeWindowMs = danmakuDuplicateMergeWindowMs,
                danmakuDuplicateMergeCountThreshold = danmakuDuplicateMergeCountThreshold,
                danmakuAllowScroll = danmakuAllowScroll,
                danmakuAllowTop = danmakuAllowTop,
                danmakuAllowBottom = danmakuAllowBottom,
                danmakuAllowColorful = danmakuAllowColorful,
                danmakuAllowSpecial = danmakuAllowSpecial,
                danmakuHideInteractiveCommands = danmakuHideInteractiveCommands,
                danmakuBlockRulesRaw = danmakuBlockRulesRaw,
                danmakuSmartOcclusion = danmakuSmartOcclusion,
                danmakuFullscreenPanelWidthMode = danmakuFullscreenPanelWidthMode,
                portraitDanmakuDisplayAreaMode = portraitDanmakuDisplayAreaMode,
                danmakuSettingsScope = activeDanmakuScope,
                showDanmakuSyncSection = isLoggedIn,
                danmakuCloudSyncEnabled = danmakuCloudSyncEnabled,
                danmakuSyncUiState = danmakuCloudSyncUiState,
                onDanmakuOpacityChange = { value ->
                    danmakuManager.opacity = value
                    settingsActions.setDanmakuOpacity(value, activeDanmakuScope)
                    queueDanmakuCloudSync(opacity = value)
                },
                onDanmakuFontScaleChange = { value ->
                    danmakuManager.fontScale = value
                    settingsActions.setDanmakuFontScale(value, activeDanmakuScope)
                    queueDanmakuCloudSync(fontScale = value)
                },
                onDanmakuFontWeightChange = { value ->
                    danmakuManager.fontWeight = value
                    settingsActions.setDanmakuFontWeight(value, activeDanmakuScope)
                },
                onDanmakuSpeedChange = { value ->
                    danmakuManager.speedFactor = value
                    settingsActions.setDanmakuSpeed(value, activeDanmakuScope)
                    queueDanmakuCloudSync(speed = value)
                },
                onDanmakuDisplayAreaChange = { value ->
                    danmakuManager.displayArea = value
                    settingsActions.setDanmakuArea(value, activeDanmakuScope)
                    queueDanmakuCloudSync(displayAreaRatio = value)
                },
                onDanmakuStrokeWidthChange = { value ->
                    danmakuManager.strokeWidth = value
                    settingsActions.setDanmakuStrokeWidth(value, activeDanmakuScope)
                },
                onDanmakuLineHeightChange = { value ->
                    danmakuManager.lineHeight = value
                    settingsActions.setDanmakuLineHeight(value, activeDanmakuScope)
                },
                onDanmakuScrollDurationSecondsChange = { value ->
                    danmakuManager.scrollDurationSeconds = value
                    settingsActions.setDanmakuScrollDurationSeconds(value, activeDanmakuScope)
                },
                onDanmakuStaticDurationSecondsChange = { value ->
                    danmakuManager.staticDurationSeconds = value
                    settingsActions.setDanmakuStaticDurationSeconds(value, activeDanmakuScope)
                },
                onDanmakuScrollFixedVelocityChange = { value ->
                    danmakuManager.scrollFixedVelocity = value
                    settingsActions.setDanmakuScrollFixedVelocity(value, activeDanmakuScope)
                },
                onDanmakuStaticToScrollChange = { value ->
                    danmakuManager.staticDanmakuToScroll = value
                    settingsActions.setDanmakuStaticToScroll(value, activeDanmakuScope)
                },
                onDanmakuMassiveModeChange = { value ->
                    danmakuManager.massiveMode = value
                    settingsActions.setDanmakuMassiveMode(value, activeDanmakuScope)
                },
                onDanmakuMergeDuplicatesChange = { value ->
                    settingsActions.setDanmakuMergeDuplicates(value, activeDanmakuScope)
                },
                onDanmakuDuplicateMergeWindowMsChange = { value ->
                    settingsActions.setDanmakuDuplicateMergeWindowMs(value, activeDanmakuScope)
                },
                onDanmakuDuplicateMergeCountThresholdChange = { value ->
                    settingsActions.setDanmakuDuplicateMergeCountThreshold(value, activeDanmakuScope)
                },
                onDanmakuAllowScrollChange = { value ->
                    settingsActions.setDanmakuAllowScroll(value, activeDanmakuScope)
                    queueDanmakuCloudSync(allowScroll = value)
                },
                onDanmakuAllowTopChange = { value ->
                    settingsActions.setDanmakuAllowTop(value, activeDanmakuScope)
                    queueDanmakuCloudSync(allowTop = value)
                },
                onDanmakuAllowBottomChange = { value ->
                    settingsActions.setDanmakuAllowBottom(value, activeDanmakuScope)
                    queueDanmakuCloudSync(allowBottom = value)
                },
                onDanmakuAllowColorfulChange = { value ->
                    settingsActions.setDanmakuAllowColorful(value, activeDanmakuScope)
                    queueDanmakuCloudSync(allowColorful = value)
                },
                onDanmakuAllowSpecialChange = { value ->
                    settingsActions.setDanmakuAllowSpecial(value, activeDanmakuScope)
                    queueDanmakuCloudSync(allowSpecial = value)
                },
                onDanmakuHideInteractiveCommandsChange = { value ->
                    settingsActions.setDanmakuHideInteractiveCommands(value)
                },
                onDanmakuSmartOcclusionChange = { value ->
                    settingsActions.setDanmakuSmartOcclusion(value, activeDanmakuScope)
                },
                onDanmakuFullscreenPanelWidthModeChange = { value ->
                    settingsActions.setDanmakuFullscreenPanelWidthMode(value)
                },
                onPortraitDanmakuDisplayAreaModeChange = { value ->
                    settingsActions.setPortraitDanmakuDisplayAreaMode(value)
                },
                onDanmakuCloudSyncEnabledChange = { enabled ->
                    settingsActions.setDanmakuCloudSyncEnabled(enabled)
                    if (!enabled) {
                        pendingDanmakuCloudSync = null
                        danmakuCloudSyncUiState = DanmakuCloudSyncUiState()
                    }
                },
                onDanmakuSyncNowClick = {
                    requestDanmakuCloudSyncNow()
                },
                onDanmakuBlockRulesRawChange = { value ->
                    settingsActions.setDanmakuBlockRulesRaw(value, activeDanmakuScope)
                },
                //  视频比例调节

                currentAspectRatio = currentAspectRatio,
                onAspectRatioChange = { ratio ->
                    val safeRatio = resolveSafeVideoAspectRatio(
                        preferred = ratio,
                        isVerticalVideo = isVerticalVideo
                    )
                    currentAspectRatio = safeRatio
                    settingsActions.setFullscreenAspectRatio(safeRatio.toFullscreenAspectRatio())
                },
                // 🕺 [新增] 分享功能
                bvid = bvid,
                cid = uiState.info.cid,
                videoOwnerName = uiState.info.owner.name,
                videoOwnerFace = uiState.info.owner.face,
                videoDuration = uiState.videoDurationMs,
                videoTitle = uiState.info.title,
                currentAid = uiState.info.aid,
                currentQuality = uiState.currentQuality,
                currentVideoUrl = uiState.playUrl,
                currentAudioUrl = uiState.audioUrl ?: "",
                coverUrl = uiState.info.pic,
                //  [新增] 视频设置面板回调
                onReloadVideo = onReloadVideo,
                isFlippedHorizontal = isFlippedHorizontal,
                isFlippedVertical = isFlippedVertical,
                onFlipHorizontal = { isFlippedHorizontal = !isFlippedHorizontal },
                onFlipVertical = { isFlippedVertical = !isFlippedVertical },
                //  [新增] 画质切换（用于设置面板）
                onQualityChange = { qid ->
                    onQualityChange(qid)
                },
                //  [新增] CDN 线路切换
                currentCdnIndex = currentCdnIndex,
                cdnCount = cdnCount,
                cdnLineDiagnostics = cdnLineDiagnostics,
                isCdnProbing = isCdnProbing,
                onSwitchCdn = onSwitchCdn,
                onSwitchCdnTo = onSwitchCdnTo,
                onProbeCdnCandidates = onProbeCdnCandidates,
                
                subtitleControlState = SubtitleControlUiState(
                    trackAvailable = subtitleControlAvailability.trackAvailable,
                    primaryAvailable = subtitleControlAvailability.primarySelectable,
                    secondaryAvailable = subtitleControlAvailability.secondarySelectable,
                    enabled = subtitleFeatureEnabled && subtitleOverlayEnabled,
                    displayMode = if (subtitleFeatureEnabled) subtitleDisplayMode else SubtitleDisplayMode.OFF,
                    primaryLabel = subtitlePrimaryLabel,
                    secondaryLabel = subtitleSecondaryLabel,
                    trackOptions = subtitleTrackOptions,
                    largeTextEnabled = subtitleLargeTextByUser
                ),
                subtitleControlCallbacks = SubtitleControlCallbacks(
                    onDisplayModeChange = { mode ->
                        com.android.purebilibili.core.util.Logger.d(
                            "VideoPlayerSection",
                            "字幕显示模式切换: mode=$mode"
                        )
                        applySubtitleDisplayModePreferenceChange(mode)
                    },
                    onEnabledChange = { enabled ->
                        com.android.purebilibili.core.util.Logger.d(
                            "VideoPlayerSection",
                            "字幕总开关切换: enabled=$enabled"
                        )
                        val nextMode = if (enabled) {
                            resolveDefaultSubtitleDisplayMode(
                                hasPrimaryTrack = subtitleControlAvailability.primarySelectable,
                                hasSecondaryTrack = subtitleControlAvailability.secondarySelectable
                            )
                        } else {
                            SubtitleDisplayMode.OFF
                        }
                        applySubtitleDisplayModePreferenceChange(nextMode)
                    },
                    onTrackSelected = { trackKey ->
                        onSubtitleTrackSelected(trackKey)
                    },
                    onLargeTextChange = { enabled ->
                        com.android.purebilibili.core.util.Logger.d(
                            "VideoPlayerSection",
                            "字幕大字号切换: enabled=$enabled"
                        )
                        subtitleLargeTextByUser = enabled
                    }
                ),
                
                //  [新增] 定时关闭
                sleepTimerMinutes = sleepTimerMinutes,
                onSleepTimerChange = onSleepTimerChange,
                
                // 🖼️ [新增] 视频预览图数据
                videoshotData = videoshotData,
                
                // 📖 [新增] 视频章节数据
                viewPoints = viewPoints,
                sponsorMarkers = sponsorMarkers,
                pbpRidgeSamples = pbpRidgeSamples,
                
                // 📱 [新增] 竖屏全屏模式
                isVerticalVideo = isVerticalVideo,
                onPortraitFullscreen = onPortraitFullscreen,
                // 📲 [新增] 小窗模式
                // 📲 [新增] 小窗模式
                onPipClick = onPipClick,
                //  [新增] 拖动进度条开始时清除弹幕
                onSeekStart = { danmakuManager.prepareForSeekScrub() },
                onSeekDragStart = { position ->
                    sharedSeekSession = startPlaybackSeekInteraction(
                        state = sharedSeekSession,
                        player = playerState.player,
                        positionMs = position
                    )
                },
                onSeekDragUpdate = { position ->
                    sharedSeekSession = updatePlaybackSeekInteraction(
                        state = sharedSeekSession,
                        positionMs = position
                    )
                },
                onSeekDragCancel = {
                    sharedSeekSession = cancelPlaybackSeekInteraction(sharedSeekSession)
                    danmakuManager.cancelSeekScrub()
                },
                isSeekScrubbing = sharedSeekSession.isSliderMoving && gestureMode != VideoGestureMode.Seek,
                //  [加固] 显式同步弹幕到新进度，避免某些设备 seek 回调时机差导致短暂不同步
                onSeekTo = { position ->
                    val commitResult = commitPlaybackSeekInteraction(
                        state = sharedSeekSession,
                        player = playerState.player,
                        positionMs = position
                    )
                    sharedSeekSession = commitResult.state
                    seekPlayerFromUserAction(
                        player = playerState.player,
                        positionMs = commitResult.committedPositionMs,
                        shouldResumePlaybackOverride = commitResult.shouldResumePlayback
                    )
                    danmakuManager.seekTo(commitResult.committedPositionMs)
                    onUserSeek(commitResult.committedPositionMs)
                },
                progressDisplayOverridePositionMs = resolveProgressDisplayOverridePositionMs(
                    seekSession = sharedSeekSession,
                    pendingPlaybackTransitionPositionMs = uiState.pendingPlaybackTransitionPositionMs,
                    isLongPressing = isLongPressing,
                    longPressSpeedLocked = longPressSpeedLocked
                ),
                isPlaybackTransitionPending = uiState.pendingPlaybackTransitionPositionMs != null,
                highFrequencyProgressActive = isLongPressing,
                // [New] Codec & Audio
                currentCodec = currentCodec,
                onCodecChange = onCodecChange,
                currentSecondCodec = currentSecondCodec,
                onSecondCodecChange = onSecondCodecChange,
                currentAudioQuality = currentAudioQuality,
                selectedAudioQuality = uiState.selectedAudioQuality,
                availableAudioQualities = uiState.availableAudioQualities,
                onAudioQualityChange = onAudioQualityChange,
                // [New] AI Audio
                aiAudioInfo = uiState.aiAudio,
                currentAudioLang = uiState.currentAudioLang,
                onAudioLangChange = onAudioLangChange,
                // 👀 [新增] 在线观看人数
                onlineCount = uiState.onlineCount,
                // [New]
                onSaveCover = onSaveCover,
                onCaptureScreenshot = {
                    val playerView = playerViewRef
                    if (playerView == null) {
                        Toast.makeText(context, "截图失败：播放器未就绪", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            val success = captureAndSaveVideoScreenshot(
                                context = context,
                                playerView = playerView,
                                videoWidth = videoSizeState.first,
                                videoHeight = videoSizeState.second,
                                videoTitle = uiState.info.title,
                            )
                            Toast.makeText(
                                context,
                                if (success) "截图已保存到相册（PNG）" else "截图失败，请稍后重试",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onDownloadAudio = onDownloadAudio,
                // 🔁 [新增] 播放模式
                currentPlayMode = currentPlayMode,
                onPlayModeClick = onPlayModeClick,
                onPlaybackSpeedChange = ::applyExplicitPlaybackSpeedChange,
                endDrawerVisible = showEndDrawer,
                endDrawerInitialTab = endDrawerInitialTab,
                endDrawerReservedWidth = animatedEndDrawerReservedWidth,
                onShowEndDrawer = { initialTab ->
                    endDrawerInitialTab = initialTab
                    showEndDrawer = true
                },
                onDismissEndDrawer = {
                    showEndDrawer = false
                },
                
                // [新增] 侧边栏抽屉数据与交互
                relatedVideos = relatedVideos,
                ugcSeason = ugcSeason,
                isFollowed = isFollowed,
                isLiked = isLiked,
                isCoined = isCoined,
                isFavorited = isFavorited,
                likeCount = uiState.info.stat.like.toLong(),
                favoriteCount = uiState.info.stat.favorite.toLong(),
                coinCount = uiState.coinCount,
                onToggleFollow = onToggleFollow,
                onToggleLike = onToggleLike,
                onDislike = onDislike,
                onCoin = onCoin,
                onToggleFavorite = onToggleFavorite,
                onDrawerVideoClick = { vid, options ->
                    onRelatedVideoClick(vid, options) 
                },
                pages = uiState.info.pages,
                currentPageIndex = currentPageIndex,
                onPageSelect = onPageSelect,
                hasFavoritePlaylist = hasFavoritePlaylist,
                onFavoritePlaylistClick = onFavoritePlaylistClick,
                drawerHazeState = overlayDrawerHazeState,
                statusBarAmbientFrame = statusBarAmbientFrame,
                statusBarBackdropHeight = contentTopInset,
                onLandscapeCommentClick = onLandscapeCommentClick,
                landscapeCommentPanelVisible = landscapeCommentPanelVisible,
                landscapeCommentPanelOnLeft = landscapeCommentPanelOnLeft,
            )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = transitionChromeAlphaProvider()
                    }
            ) {
                RenderVideoPlayerOverlay()
            }

            SponsorSkipButton(
                segment = sponsorSegment,
                visible = showSponsorSkipButton,
                onSkip = onSponsorSkip,
                onDismiss = onSponsorDismiss,
                onVote = onSponsorVote,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 60.dp, end = 16.dp)
            )
            SponsorContributionOverlay(
                state = sponsorContributionState,
                onMarkBoundary = onSponsorContributionMarkBoundary,
                onCategoryChange = onSponsorContributionCategoryChange,
                onActionTypeChange = onSponsorContributionActionTypeChange,
                onSubmit = onSponsorContributionSubmit,
                onCancel = onSponsorContributionCancel,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 60.dp, start = 16.dp),
            )
    }



    // 拦截系统返回事件 (仅在全屏时拦截以处理退出全屏，否则交给系统处理预测性返回)
    BackHandler(enabled = !isScreenLocked && isFullscreen) {
        onToggleFullscreen()
    }
}
}
