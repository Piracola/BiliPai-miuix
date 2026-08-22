package com.android.purebilibili.architecture

/**
 * 阶段 1 架构护栏白名单（冻结于 2026-08-16，只减不增）。
 *
 * 这些 feature 文件在护栏接入前就引用了 Core 全局对象（SettingsManager、
 * NetworkModule、TokenManager、data/repository 单例等）。文件级豁免而非调用点级，
 * 是因为接线前需要先把 UI 调用迁到 Coordinator/ViewModel，阶段 2/4 按切片逐步
 * 把条目从本表移除。每移除一条都必须同步调小 ArchitectureAllowlistRatchetTest
 * 的上限并更新 SHA 摘要。
 */
internal object ArchitectureAllowlist {

    /** feature UI 文件引用 Core 全局对象的存量（ViewModel 文件由护栏规则豁免）。 */
    val CORE_GLOBAL_OBJECT_FILES: Set<String> = setOf(
        "src/main/java/com/android/purebilibili/feature/article/ArticleDetailScreen.kt",
        "src/main/java/com/android/purebilibili/feature/bangumi/BangumiPlayerScreen.kt",
        "src/main/java/com/android/purebilibili/feature/bangumi/policy/BangumiFollowStatusPolicy.kt",
        "src/main/java/com/android/purebilibili/feature/bangumi/ui/player/BangumiPlayerComponents.kt",
        "src/main/java/com/android/purebilibili/feature/category/CategoryScreen.kt",
        "src/main/java/com/android/purebilibili/feature/download/DownloadDanmakuAssetService.kt",
        "src/main/java/com/android/purebilibili/feature/download/DownloadListScreen.kt",
        "src/main/java/com/android/purebilibili/feature/download/DownloadManager.kt",
        "src/main/java/com/android/purebilibili/feature/download/DownloadRequestHeaders.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/DynamicDetailScreen.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCard.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicEmoteCatalog.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/ImagePreviewDialog.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/ImageSaveLocationPolicy.kt",
        "src/main/java/com/android/purebilibili/feature/dynamic/components/VideoCards.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/CrashTrackingConsentDialog.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/HomeHeader.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt",
        "src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCardOnlineCountStore.kt",
        "src/main/java/com/android/purebilibili/feature/home/policy/HomeBottomBarModePolicy.kt",
        "src/main/java/com/android/purebilibili/feature/home/policy/HomeFeedScrollRestorePolicy.kt",
        "src/main/java/com/android/purebilibili/feature/live/components/LiveContributionRankSheet.kt",
        "src/main/java/com/android/purebilibili/feature/message/feed/AtMeScreen.kt",
        "src/main/java/com/android/purebilibili/feature/message/feed/LikeMeScreen.kt",
        "src/main/java/com/android/purebilibili/feature/message/feed/ReplyMeScreen.kt",
        "src/main/java/com/android/purebilibili/feature/message/feed/SystemNoticeScreen.kt",
        "src/main/java/com/android/purebilibili/feature/onboarding/OnboardingSettingsGuidePolicy.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/AdFilterPlugin.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/CdnRegionPlugin.kt",
        "src/main/java/com/android/purebilibili/feature/plugin/SponsorBlockPlugin.kt",
        "src/main/java/com/android/purebilibili/feature/profile/SplashWallpaperPickerSheet.kt",
        "src/main/java/com/android/purebilibili/feature/search/SearchArticleNavigationPolicy.kt",
        "src/main/java/com/android/purebilibili/feature/settings/PlaybackSettingsSelectionPolicy.kt",
        "src/main/java/com/android/purebilibili/feature/settings/screen/PluginsScreen.kt",
        "src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareService.kt",
        "src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt",
        "src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupService.kt",
        "src/main/java/com/android/purebilibili/feature/video/VideoActivity.kt",
        "src/main/java/com/android/purebilibili/feature/video/danmaku/DanmakuManager.kt",
        "src/main/java/com/android/purebilibili/feature/video/playback/policy/PlaybackPowerPolicy.kt",
        "src/main/java/com/android/purebilibili/feature/video/player/MiniPlayerManager.kt",
        "src/main/java/com/android/purebilibili/feature/video/player/PlaybackService.kt",
        "src/main/java/com/android/purebilibili/feature/video/state/VideoPlayerState.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/BottomInputBar.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CollectionRow.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CollectionSheet.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CollectionSubscriptionButton.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/CommentSortFilterBar.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/RelatedVideoItem.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/ReplyComponents.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/SkeletonComponents.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/UpPreviewSheet.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/VideoCommentSheetHost.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/components/VideoSettingsPanel.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/FullscreenPlayerOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/PlayerOverlayModels.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/PortraitSubtitleOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/overlay/VideoPlayerOverlay.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/pager/PortraitVideoLoadPolicy.kt",
        "src/main/java/com/android/purebilibili/feature/video/ui/pager/PortraitVideoPager.kt",
    )
}
