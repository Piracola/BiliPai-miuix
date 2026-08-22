package com.android.purebilibili.feature.video.screen

import androidx.compose.runtime.Immutable
import com.android.purebilibili.data.model.response.FavFolder
import com.android.purebilibili.data.model.response.ReplyItem
import com.android.purebilibili.feature.video.note.VideoNoteEditorDocument
import com.android.purebilibili.feature.video.viewmodel.CommentSortMode

@Immutable
internal data class VideoDetailPlaybackActions(
    val changeQuality: (Int) -> Unit,
    val reloadVideo: () -> Unit,
    val switchCdn: () -> Unit,
    val switchCdnTo: (Int) -> Unit,
    val probeCdnCandidates: () -> Unit,
    val setSleepTimer: (Int?) -> Unit,
    val switchPage: (Int) -> Unit,
    val openDownloadDialog: () -> Unit,
    val showDanmakuSendDialog: () -> Unit,
    val skipSponsorSegment: () -> Unit,
    val dismissSponsorSkipButton: () -> Unit,
    val voteSponsorSegment: (Int) -> Unit,
    val markSponsorContributionBoundary: () -> Unit,
    val setSponsorContributionCategory: (String) -> Unit,
    val setSponsorContributionActionType: (String) -> Unit,
    val submitSponsorContribution: () -> Unit,
    val cancelSponsorContribution: () -> Unit,
    val notifyExplicitSeek: (Long) -> Unit,
    val setVideoCodec: (String) -> Unit,
    val setVideoSecondCodec: (String) -> Unit,
    val setAudioQuality: (Int) -> Unit,
    val applyPlaybackSpeed: (Float) -> Boolean,
    val changeAudioLanguage: (String?) -> Unit,
    val saveCover: () -> Unit,
    val downloadAudio: () -> Unit,
    val selectSubtitleTrack: (String) -> Unit,
    val showFavoriteFolderDialog: () -> Unit,
    val toggleFavoriteFolderSelection: (FavFolder) -> Unit,
    val saveFavoriteFolderSelection: () -> Unit,
    val dismissFavoriteFolderDialog: () -> Unit,
    val createFavoriteFolder: (String, String, Boolean) -> Unit,
    val retryAiSummary: () -> Unit,
    val createVideoNoteDraftFromAiSummary: () -> Unit,
    val openVideoNoteEditor: () -> Unit,
    val closeVideoNoteEditor: () -> Unit,
    val updateVideoNoteEditorDocument: (VideoNoteEditorDocument) -> Unit,
    val insertCurrentPlaybackTimestampIntoNote: () -> Unit,
    val seekTo: (Long) -> Unit,
    val saveVideoNote: (VideoNoteEditorDocument?) -> Unit,
    val deleteVideoNote: () -> Unit,
    val retryVideoNote: () -> Unit,
    val openRootCommentComposer: () -> Unit,
    val replyTo: (ReplyItem) -> Unit,
    val markVideoNotInterested: () -> Unit
)

@Immutable
internal data class VideoDetailEngagementActions(
    val toggleFollow: () -> Unit,
    val toggleFavorite: () -> Unit,
    val toggleLike: () -> Unit,
    val openCoinDialog: () -> Unit,
    val doTripleAction: () -> Unit,
    val toggleWatchLater: () -> Unit
)

@Immutable
internal data class VideoDetailCommentActions(
    val loadComments: () -> Unit,
    val setSortMode: (CommentSortMode) -> Unit,
    val deleteComment: (Long) -> Unit,
    val startDissolve: (Long) -> Unit,
    val loadMoreSubReplies: () -> Unit,
    val openSubReply: (ReplyItem, Long) -> Unit,
    val openSubReplyConversation: (ReplyItem) -> Unit,
    val closeSubReplyConversation: () -> Unit,
    val closeSubReply: () -> Unit,
    val startSubDissolve: (Long) -> Unit,
    val deleteSubComment: (Long) -> Unit,
    val likeComment: (Long) -> Unit,
    val reportComment: (Long, Int) -> Unit,
    val toggleTopComment: (ReplyItem) -> Unit
)

@Immutable
internal data class VideoDetailNavigationActions(
    val back: () -> Unit,
    val home: () -> Unit,
    val toggleFullscreen: () -> Unit,
    val enterPortraitFullscreen: () -> Unit,
    val enterPip: () -> Unit
)

/** 详情页弹幕设置面板的持久化写入（阶段 4 切片：UI 通过 VM 落盘设置，不直连 Core）。 */
@Immutable
data class VideoDanmakuSettingsActions(
    val setOpacity: (Float) -> Unit,
    val setFontScale: (Float) -> Unit,
    val setSpeed: (Float) -> Unit,
    val setDisplayArea: (Float) -> Unit,
    val setMergeDuplicates: (Boolean) -> Unit,
    val setDuplicateMergeWindowMs: (Int) -> Unit,
    val setDuplicateMergeCountThreshold: (Int) -> Unit,
    val setAllowScroll: (Boolean) -> Unit,
    val setAllowTop: (Boolean) -> Unit,
    val setAllowBottom: (Boolean) -> Unit,
    val setAllowColorful: (Boolean) -> Unit,
    val setAllowSpecial: (Boolean) -> Unit,
    val setHideInteractiveCommands: (Boolean) -> Unit,
    val setPortraitDisplayAreaMode: (com.android.purebilibili.core.store.PortraitDanmakuDisplayAreaMode) -> Unit,
    val setBlockRulesRaw: (String) -> Unit
) {
    companion object {
        val NoOp = VideoDanmakuSettingsActions(
            setOpacity = {},
            setFontScale = {},
            setSpeed = {},
            setDisplayArea = {},
            setMergeDuplicates = {},
            setDuplicateMergeWindowMs = {},
            setDuplicateMergeCountThreshold = {},
            setAllowScroll = {},
            setAllowTop = {},
            setAllowBottom = {},
            setAllowColorful = {},
            setAllowSpecial = {},
            setHideInteractiveCommands = {},
            setPortraitDisplayAreaMode = {},
            setBlockRulesRaw = {}
        )
    }
}

/**
 * 播放器控制层设置快照（阶段 4 切片）。
 * 全部由 [com.android.purebilibili.feature.video.viewmodel.VideoPlaybackViewModel]
 * 的状态流解析后经 StateHolder 传入，UI 通过 VM 落盘设置，不直连 Core。
 */
@Immutable
data class VideoPlayerSettingsSnapshot(
    val playerInsightMode: com.android.purebilibili.feature.video.viewmodel.PlayerInsightMode,
    val playerInteractionSettings: com.android.purebilibili.core.store.PlayerInteractionSettings,
    val playbackCompletionBehavior: com.android.purebilibili.core.store.PlaybackCompletionBehavior,
    val playerDiagnosticLoggingEnabled: Boolean,
    val statusBarHazeEnabled: Boolean,
    val autoPlayOnOpenEnabled: Boolean,
    val danmakuSettings: com.android.purebilibili.core.store.DanmakuSettings,
    val danmakuFullscreenPanelWidthMode: com.android.purebilibili.core.store.DanmakuPanelWidthMode,
    val danmakuCloudSyncEnabled: Boolean,
    val longPressSpeedLockHintShown: Boolean,
    val hiResLongPressCompatHintShown: Boolean,
    val defaultPlaybackSpeed: Float,
    val rememberLastPlaybackSpeed: Boolean,
)

/** 播放器控制层设置写入（scope 随横竖屏由调用方显式传入）。 */
@Immutable
data class VideoPlayerSettingsActions(
    val setDanmakuSettingsScope: (com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuEnabled: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuOpacity: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuFontScale: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuFontWeight: (Int, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuSpeed: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuArea: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuStrokeWidth: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuLineHeight: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuScrollDurationSeconds: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuStaticDurationSeconds: (Float, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuScrollFixedVelocity: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuStaticToScroll: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuMassiveMode: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuMergeDuplicates: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuDuplicateMergeWindowMs: (Int, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuDuplicateMergeCountThreshold: (Int, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuAllowScroll: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuAllowTop: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuAllowBottom: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuAllowColorful: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuAllowSpecial: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuHideInteractiveCommands: (Boolean) -> Unit,
    val setDanmakuSmartOcclusion: (Boolean, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuFullscreenPanelWidthMode: (com.android.purebilibili.core.store.DanmakuPanelWidthMode) -> Unit,
    val setPortraitDanmakuDisplayAreaMode: (com.android.purebilibili.core.store.PortraitDanmakuDisplayAreaMode) -> Unit,
    val setDanmakuBlockRulesRaw: (String, com.android.purebilibili.core.store.DanmakuSettingsScope) -> Unit,
    val setDanmakuCloudSyncEnabled: (Boolean) -> Unit,
    val setSubtitleVerticalOffsetFraction: (Float) -> Unit,
    val setLongPressSpeedLockEnabled: (Boolean) -> Unit,
    val setLongPressSpeedLockHintShown: (Boolean) -> Unit,
    val setHiResLongPressCompatHintShown: (Boolean) -> Unit,
    val setFullscreenAspectRatio: (com.android.purebilibili.core.store.FullscreenAspectRatio) -> Unit,
    val setLastPlaybackSpeed: (Float) -> Unit,
    val setRememberLastPlaybackSpeed: (Boolean) -> Unit,
    val setDefaultPlaybackSpeed: (Float) -> Unit,
    val setDoubleTapSeekEnabled: (Boolean) -> Unit,
    val setSeekForwardSeconds: (Int) -> Unit,
    val setSeekBackwardSeconds: (Int) -> Unit,
    val setLongPressSpeed: (Float) -> Unit,
    val setTwoFingerVerticalSpeedEnabled: (Boolean) -> Unit,
    val setTwoFingerHorizontalSpeedEnabled: (Boolean) -> Unit,
    val syncDanmakuCloudConfig: suspend (com.android.purebilibili.data.repository.DanmakuCloudSyncSettings) -> Result<Unit>,
    val submitGradeDanmaku: suspend (Long, Long, Long, String, Int) -> Result<Unit>,
) {
    companion object {
        val NoOp = VideoPlayerSettingsActions(
            setDanmakuSettingsScope = {},
            setDanmakuEnabled = { _, _ -> },
            setDanmakuOpacity = { _, _ -> },
            setDanmakuFontScale = { _, _ -> },
            setDanmakuFontWeight = { _, _ -> },
            setDanmakuSpeed = { _, _ -> },
            setDanmakuArea = { _, _ -> },
            setDanmakuStrokeWidth = { _, _ -> },
            setDanmakuLineHeight = { _, _ -> },
            setDanmakuScrollDurationSeconds = { _, _ -> },
            setDanmakuStaticDurationSeconds = { _, _ -> },
            setDanmakuScrollFixedVelocity = { _, _ -> },
            setDanmakuStaticToScroll = { _, _ -> },
            setDanmakuMassiveMode = { _, _ -> },
            setDanmakuMergeDuplicates = { _, _ -> },
            setDanmakuDuplicateMergeWindowMs = { _, _ -> },
            setDanmakuDuplicateMergeCountThreshold = { _, _ -> },
            setDanmakuAllowScroll = { _, _ -> },
            setDanmakuAllowTop = { _, _ -> },
            setDanmakuAllowBottom = { _, _ -> },
            setDanmakuAllowColorful = { _, _ -> },
            setDanmakuAllowSpecial = { _, _ -> },
            setDanmakuHideInteractiveCommands = {},
            setDanmakuSmartOcclusion = { _, _ -> },
            setDanmakuFullscreenPanelWidthMode = {},
            setPortraitDanmakuDisplayAreaMode = {},
            setDanmakuBlockRulesRaw = { _, _ -> },
            setDanmakuCloudSyncEnabled = {},
            setSubtitleVerticalOffsetFraction = {},
            setLongPressSpeedLockEnabled = {},
            setLongPressSpeedLockHintShown = {},
            setHiResLongPressCompatHintShown = {},
            setFullscreenAspectRatio = {},
            setLastPlaybackSpeed = {},
            setRememberLastPlaybackSpeed = {},
            setDefaultPlaybackSpeed = {},
            setDoubleTapSeekEnabled = {},
            setSeekForwardSeconds = {},
            setSeekBackwardSeconds = {},
            setLongPressSpeed = {},
            setTwoFingerVerticalSpeedEnabled = {},
            setTwoFingerHorizontalSpeedEnabled = {},
            syncDanmakuCloudConfig = { Result.success(Unit) },
            submitGradeDanmaku = { _, _, _, _, _ -> Result.success(Unit) },
        )
    }
}

/** 预览/预布局场景的设置快照缺省值，与各设置的存储默认值保持一致。 */
internal fun resolveDefaultVideoPlayerSettingsSnapshot(): VideoPlayerSettingsSnapshot =
    VideoPlayerSettingsSnapshot(
        playerInsightMode = com.android.purebilibili.feature.video.viewmodel.PlayerInsightMode.OFF,
        playerInteractionSettings = com.android.purebilibili.core.store.PlayerInteractionSettings(),
        playbackCompletionBehavior = com.android.purebilibili.core.store.PlaybackCompletionBehavior.CONTINUE_CURRENT_LOGIC,
        playerDiagnosticLoggingEnabled = true,
        statusBarHazeEnabled = false,
        autoPlayOnOpenEnabled = true,
        danmakuSettings = com.android.purebilibili.core.store.DanmakuSettings(),
        danmakuFullscreenPanelWidthMode = com.android.purebilibili.core.store.DanmakuPanelWidthMode.THIRD,
        danmakuCloudSyncEnabled = true,
        longPressSpeedLockHintShown = false,
        hiResLongPressCompatHintShown = false,
        defaultPlaybackSpeed = 1.0f,
        rememberLastPlaybackSpeed = false,
    )
