package com.android.purebilibili.feature.video.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.data.model.response.BgmInfo
import com.android.purebilibili.feature.video.player.MiniPlayerManager
import com.android.purebilibili.feature.video.viewmodel.VideoCommentViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoComposerViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoEngagementViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackViewModel
import com.android.purebilibili.feature.video.viewmodel.VideoSupplementViewModel

/**
 * Navigation-facing video detail entry point.
 *
 * ViewModel acquisition and the public route contract stay here; screen orchestration lives in
 * [VideoDetailScreenStateHolder], while platform behavior has dedicated owners. Page motion is
 * owned by Navigation3; this screen never coordinates a card-to-detail transition.
 */
@Composable
fun VideoDetailScreen(
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
    onBgmClick: (BgmInfo) -> Unit = {},
) {
    VideoDetailScreenStateHolder(
        bvid = bvid,
        cid = cid,
        coverUrl = coverUrl,
        startInFullscreen = startInFullscreen,
        startAudioFromRoute = startAudioFromRoute,
        autoEnterPortraitFromRoute = autoEnterPortraitFromRoute,
        initialVerticalFromRoute = initialVerticalFromRoute,
        directPortraitEntryFromRoute = directPortraitEntryFromRoute,
        resumePositionMsFromRoute = resumePositionMsFromRoute,
        openCommentRootRpidFromRoute = openCommentRootRpidFromRoute,
        openCommentTargetRpidFromRoute = openCommentTargetRpidFromRoute,
        onBack = onBack,
        onHomeClick = onHomeClick,
        onNavigateToAudioMode = onNavigateToAudioMode,
        onNavigateToSearch = onNavigateToSearch,
        onSearchKeywordClick = onSearchKeywordClick,
        onOpenBilibiliLink = onOpenBilibiliLink,
        onVideoClick = onVideoClick,
        onUpClick = onUpClick,
        onUpClickWithVideo = onUpClickWithVideo,
        miniPlayerManager = miniPlayerManager,
        isInPipMode = isInPipMode,
        isVisible = isVisible,
        viewModel = viewModel,
        engagementViewModel = engagementViewModel,
        composerViewModel = composerViewModel,
        supplementViewModel = supplementViewModel,
        commentViewModel = commentViewModel,
        onBgmClick = onBgmClick,
    )
}
