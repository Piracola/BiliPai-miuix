// 文件路径: feature/settings/SettingsViewModel.kt
package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.theme.AppLanguage
import com.android.purebilibili.core.store.theme.AppThemeMode
import com.android.purebilibili.core.store.theme.DarkThemeStyle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.store.player.DEFAULT_AUDIO_QUALITY_FOLLOW_LAST
import com.android.purebilibili.core.store.player.PlayerSettingsStore
import com.android.purebilibili.core.store.AutoExitFullscreenMode
import com.android.purebilibili.core.store.BottomProgressBehavior
import com.android.purebilibili.core.store.FullscreenAspectRatio
import com.android.purebilibili.core.store.FullscreenMode
import com.android.purebilibili.core.store.PlaybackCompletionBehavior
import com.android.purebilibili.core.store.PlayerProgressPlacement
import com.android.purebilibili.core.store.PlayerControlVisibilitySettings
import com.android.purebilibili.core.store.PortraitPlayerCollapseMode
import com.android.purebilibili.core.store.TabletCommentPanelWidthPreset
import com.android.purebilibili.core.video.subtitle.SubtitleAutoPreference
import com.android.purebilibili.core.screenshot.AppScreenshotCaptureMode
import com.android.purebilibili.core.screenshot.AppScreenshotGestureMode
import com.android.purebilibili.core.store.DEFAULT_ANALYTICS_ENABLED
import com.android.purebilibili.core.store.DEFAULT_CRASH_TRACKING_ENABLED
import com.android.purebilibili.core.store.DEFAULT_HOME_REFRESH_COUNT
import com.android.purebilibili.data.repository.VideoRepository
import com.android.purebilibili.feature.dynamic.defaultDynamicTabVisibleIds
import com.android.purebilibili.core.ui.components.AppSingleChoicePresentation
import com.android.purebilibili.core.store.BottomBarSearchAutoExpandMode
import com.android.purebilibili.core.store.BottomBarSearchLayoutMode
import com.android.purebilibili.core.store.CommonListHeaderCollapseMode
import com.android.purebilibili.core.store.HomeDurationStyle
import com.android.purebilibili.core.store.HomeFeedCardStyle
import com.android.purebilibili.core.store.HomeFeedCardWidthPreset
import com.android.purebilibili.core.store.HomeHeaderBlurMode
import com.android.purebilibili.core.store.HomeHeaderCollapseMode
import com.android.purebilibili.core.store.HomeTopLayoutOrder
import com.android.purebilibili.core.store.HomeTopRightAction
import com.android.purebilibili.core.store.HomeWallpaperEffectMode
import com.android.purebilibili.core.store.HomeWallpaperEffectScope
import com.android.purebilibili.core.theme.AppFontSizePreset
import com.android.purebilibili.core.theme.AppUiScalePreset
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_CUSTOM_DEFAULT_MILLIS
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionSpeed
import com.android.purebilibili.core.util.CacheClearTarget
import com.android.purebilibili.core.util.CacheUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class SettingsDiagnosticsLoadState {
    NOT_LOADED,
    LOADING,
    LOADED,
}

// 阶段 4 设置页切片：根页嵌套枚举别名（SettingsManager 嵌套枚举对 UI 层可见，
// 但直接写 SettingsManager.FeedApiType 会命中护栏；经 VM 文件转发为顶层别名）。
internal typealias SettingsFeedApiType = SettingsManager.FeedApiType
internal typealias SettingsAppUpdateChannel = SettingsManager.AppUpdateChannel
internal typealias SettingsDynamicFeedLayoutMode = SettingsManager.DynamicFeedLayoutMode

internal fun shouldStartSettingsDiagnostics(
    loadState: SettingsDiagnosticsLoadState,
    jobActive: Boolean,
): Boolean = loadState != SettingsDiagnosticsLoadState.LOADED && !jobActive

data class SettingsUiState(
    val hwDecode: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val darkThemeStyle: DarkThemeStyle = DarkThemeStyle.DEFAULT,
    val appLanguage: AppLanguage = AppLanguage.FOLLOW_SYSTEM,
    val appFontSizePreset: AppFontSizePreset = AppFontSizePreset.DEFAULT,
    val appFontFileName: String = "",
    val appFontDisplayName: String = "",
    val appUiScalePreset: AppUiScalePreset = AppUiScalePreset.STANDARD,
    val appDpiOverridePercent: Int = 0,
    val bgPlay: Boolean = false,
    val gestureSensitivity: Float = 1.0f,
    val appListItemStyle: AppListItemStyle = AppListItemStyle.AUTO,
    val isBottomBarFloating: Boolean = true,
    val bottomBarLabelMode: Int = 1,  // 0=图标+文字, 1=仅图标, 2=仅文字
    val headerBlurEnabled: Boolean = true,
    val bottomBarBlurEnabled: Boolean = true,
    val blurIntensity: BlurIntensity = BlurIntensity.THIN,  //  模糊强度
    val displayMode: Int = 0,
    val cardAnimationEnabled: Boolean = false,     //  卡片进场动画（默认关闭）
    val cardTransitionEnabled: Boolean = false,    //  卡片过渡动画（默认关闭）
    val videoSharedTransitionSpeed: VideoSharedTransitionSpeed = VideoSharedTransitionSpeed.STANDARD,
    val videoSharedTransitionCustomDurationMillis: Int =
        VIDEO_SHARED_TRANSITION_CUSTOM_DEFAULT_MILLIS,
    val smartVisualGuardEnabled: Boolean = false, // [Retired] 智能流畅优先已下线
    val cacheSize: String = "计算中...",
    val cacheBreakdown: CacheUtils.CacheBreakdown? = null,  //  详细缓存统计
    val installedApkSha256: String? = null,
    val currentReleaseEvidence: AppUpdateCheckResult? = null,
    val diagnosticsLoadState: SettingsDiagnosticsLoadState =
        SettingsDiagnosticsLoadState.NOT_LOADED,
    //  实验性功能
    val auto1080p: Boolean = true,
    val autoSkipOpEd: Boolean = false,
    val prefetchVideo: Boolean = false,
    val doubleTapLike: Boolean = true,

    //  空降助手
    val sponsorBlockEnabled: Boolean = false,
    val sponsorBlockAutoSkip: Boolean = true,
    // [新增] 触感反馈
    val hapticFeedbackEnabled: Boolean = true,
    val bottomBarSearchEnabled: Boolean = false,
    val bottomBarSearchAutoExpandMode: BottomBarSearchAutoExpandMode =
        BottomBarSearchAutoExpandMode.EXPAND_AT_HOME_TOP,
    val bottomBarSearchLayoutMode: BottomBarSearchLayoutMode =
        BottomBarSearchLayoutMode.FULL_DOCK,
    // [New] 平板导航模式
    val tabletUseSidebar: Boolean = false,
    val isHeaderCollapseEnabled: Boolean = true,
    val gridColumnCount: Int = 0, // [New]
    val homeFeedCardWidthPreset: HomeFeedCardWidthPreset = HomeFeedCardWidthPreset.AUTO,
    val homeFeedCardStyle: HomeFeedCardStyle = HomeFeedCardStyle.CURRENT,
    // 阶段 4 设置页切片：动画/效果页直读收拢到 VM
    val videoTransitionRealtimeBlurEnabled: Boolean = false,
    val liveSurfaceCardTransitionEnabled: Boolean = false,
    val fullScreenSwipeBackEnabled: Boolean = false,
    // 阶段 4 设置页切片：外观页直读收拢到 VM
    val singleChoicePresentation: AppSingleChoicePresentation = AppSingleChoicePresentation.WINDOW_POPUP,
    val compactVideoStatsOnCover: Boolean = true,
    val homeWallpaperUri: String = "",
    val homeWallpaperEffectMode: HomeWallpaperEffectMode = HomeWallpaperEffectMode.SOFT_BLUR,
    val homeWallpaperEffectScope: HomeWallpaperEffectScope = HomeWallpaperEffectScope.HOME_ONLY,
    val homeUpBadgesVisible: Boolean = false,
    val homeUpAvatarsVisible: Boolean = false,
    val homeDurationStyle: HomeDurationStyle = HomeDurationStyle.OUTSIDE_COVER,
    val homeHeroCarouselEnabled: Boolean = true,
    val homeHeroCarouselAutoplayEnabled: Boolean = false,
    val commonListHeaderCollapseMode: CommonListHeaderCollapseMode =
        CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
    val showOnlineCount: Boolean = false,
    val splashEnabled: Boolean = false,
    val splashRandomEnabled: Boolean = false,
    val splashWallpaperUri: String = "",
    val splashRandomPoolUris: List<String> = emptyList(),
    val splashIconAnimationEnabled: Boolean = true,
    // 阶段 4 切片：底栏页直读收拢（11 项）
    val bottomBarOrder: List<String> = listOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
    val bottomBarVisibleTabs: Set<String> = setOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
    val topTabOrder: List<String> = emptyList(),
    val topTabVisibleTabs: Set<String> = emptySet(),
    val topTabLabelMode: Int = 2,
    val headerBlurMode: HomeHeaderBlurMode = HomeHeaderBlurMode.FOLLOW_PRESET,
    val homeTopLayoutOrder: HomeTopLayoutOrder = HomeTopLayoutOrder.SEARCH_THEN_TABS,
    val homeHeaderCollapseMode: HomeHeaderCollapseMode = HomeHeaderCollapseMode.BOTH,
    val homeTopRightAction: HomeTopRightAction = HomeTopRightAction.SETTINGS,
    val bottomBarVisibilityMode: SettingsManager.BottomBarVisibilityMode = SettingsManager.BottomBarVisibilityMode.ALWAYS_VISIBLE,
    val sidebarAccountSwitcherEnabled: Boolean = true,
    val bottomBarItemColors: Map<String, Int> = emptyMap(),
    // 阶段 4 切片：播放设置页直读收拢（全量收进 PlaybackSettingsGroup）
    val playback: PlaybackSettingsGroup = PlaybackSettingsGroup(),
    // 阶段 4 切片：设置根页直读收拢（全量收进 SettingsRootGroup）
    val settingsRoot: SettingsRootGroup = SettingsRootGroup()
)

// 内部数据类，用于分批合并流
private data class CoreSettings(
    val hwDecode: Boolean,
    val themeMode: AppThemeMode,
    val darkThemeStyle: DarkThemeStyle,
    val appLanguage: AppLanguage,
    val bgPlay: Boolean
)

data class ExtraSettings(
    val gestureSensitivity: Float,
    val appListItemStyle: AppListItemStyle,
    val appFontSizePreset: AppFontSizePreset,
    val appFontFileName: String,
    val appFontDisplayName: String,
    val appUiScalePreset: AppUiScalePreset,
    val appDpiOverridePercent: Int,
    val isBottomBarFloating: Boolean,
    val bottomBarLabelMode: Int,
    val headerBlurEnabled: Boolean,
    val bottomBarBlurEnabled: Boolean,
    val blurIntensity: BlurIntensity,  //  添加模糊强度
    val displayMode: Int,
    val cardAnimationEnabled: Boolean,
    val cardTransitionEnabled: Boolean,
    val videoSharedTransitionSpeed: VideoSharedTransitionSpeed,
    val videoSharedTransitionCustomDurationMillis: Int,
    val smartVisualGuardEnabled: Boolean,
    val hapticFeedbackEnabled: Boolean, // [Restored]
    val bottomBarSearchEnabled: Boolean = false,
    val bottomBarSearchAutoExpandMode: BottomBarSearchAutoExpandMode =
        BottomBarSearchAutoExpandMode.EXPAND_AT_HOME_TOP,
    val bottomBarSearchLayoutMode: BottomBarSearchLayoutMode =
        BottomBarSearchLayoutMode.FULL_DOCK,
    val tabletUseSidebar: Boolean, // [New]
    val isHeaderCollapseEnabled: Boolean,
    val gridColumnCount: Int, // [New]
    val homeFeedCardWidthPreset: HomeFeedCardWidthPreset,
    // 阶段 4 设置页切片：动画/效果页直读收拢
    val videoTransitionRealtimeBlurEnabled: Boolean,
    val liveSurfaceCardTransitionEnabled: Boolean,
    val fullScreenSwipeBackEnabled: Boolean
)


//  实验性功能设置
data class ExperimentalSettings(
    val auto1080p: Boolean,
    val autoSkipOpEd: Boolean,
    val prefetchVideo: Boolean,
    val doubleTapLike: Boolean,
    //  空降助手
    val sponsorBlockEnabled: Boolean,
    val sponsorBlockAutoSkip: Boolean
)

private data class BaseSettings(
    val hwDecode: Boolean,
    val themeMode: AppThemeMode,
    val darkThemeStyle: DarkThemeStyle,
    val appLanguage: AppLanguage,
    val appFontSizePreset: AppFontSizePreset,
    val appFontFileName: String,
    val appFontDisplayName: String,
    val appUiScalePreset: AppUiScalePreset,
    val appDpiOverridePercent: Int,
    val bgPlay: Boolean,
    val gestureSensitivity: Float,
    val appListItemStyle: AppListItemStyle,
    val isBottomBarFloating: Boolean,
    val bottomBarLabelMode: Int,
    val headerBlurEnabled: Boolean,
    val bottomBarBlurEnabled: Boolean,
    val blurIntensity: BlurIntensity,  //  模糊强度
    val displayMode: Int, //  新增
    val cardAnimationEnabled: Boolean, //  卡片进场动画
    val cardTransitionEnabled: Boolean, //  卡片过渡动画
    val videoSharedTransitionSpeed: VideoSharedTransitionSpeed,
    val videoSharedTransitionCustomDurationMillis: Int,
    val smartVisualGuardEnabled: Boolean, // [New]
    val hapticFeedbackEnabled: Boolean, // [新增]
    val bottomBarSearchEnabled: Boolean,
    val bottomBarSearchAutoExpandMode: BottomBarSearchAutoExpandMode,
    val bottomBarSearchLayoutMode: BottomBarSearchLayoutMode,
    val tabletUseSidebar: Boolean, // [New]
    val isHeaderCollapseEnabled: Boolean,
    val gridColumnCount: Int, // [New]
    val homeFeedCardWidthPreset: HomeFeedCardWidthPreset,
    // 阶段 4 设置页切片：动画/效果页直读收拢
    val videoTransitionRealtimeBlurEnabled: Boolean,
    val liveSurfaceCardTransitionEnabled: Boolean,
    val fullScreenSwipeBackEnabled: Boolean
)

/**
 * 阶段 4 设置页切片：外观页直读收拢分组（15 项）。
 */
private data class AppearanceSettingsGroup(
    val singleChoicePresentation: AppSingleChoicePresentation,
    val compactVideoStatsOnCover: Boolean,
    val homeWallpaperUri: String,
    val homeWallpaperEffectMode: HomeWallpaperEffectMode,
    val homeWallpaperEffectScope: HomeWallpaperEffectScope,
    val homeUpBadgesVisible: Boolean,
    val homeUpAvatarsVisible: Boolean,
    val homeDurationStyle: HomeDurationStyle,
    val homeHeroCarouselEnabled: Boolean,
    val homeHeroCarouselAutoplayEnabled: Boolean,
    val commonListHeaderCollapseMode: CommonListHeaderCollapseMode,
    val homeFeedCardStyle: HomeFeedCardStyle,
    val showOnlineCount: Boolean,
    val splashEnabled: Boolean,
    val splashRandomEnabled: Boolean,
    val splashWallpaperUri: String,
    val splashRandomPoolUris: List<String>,
    val splashIconAnimationEnabled: Boolean
)

/**
 * 阶段 4 设置页切片：底栏页直读收拢分组（11 项）。
 */
private data class BottomBarSettingsGroup(
    val bottomBarOrder: List<String>,
    val bottomBarVisibleTabs: Set<String>,
    val topTabOrder: List<String>,
    val topTabVisibleTabs: Set<String>,
    val topTabLabelMode: Int,
    val headerBlurMode: HomeHeaderBlurMode,
    val homeTopLayoutOrder: HomeTopLayoutOrder,
    val homeHeaderCollapseMode: HomeHeaderCollapseMode,
    val homeTopRightAction: HomeTopRightAction,
    val bottomBarVisibilityMode: SettingsManager.BottomBarVisibilityMode,
    val sidebarAccountSwitcherEnabled: Boolean,
    val bottomBarItemColors: Map<String, Int>
)

/**
 * 阶段 4 设置页切片：播放设置页直读收拢分组（约 78 项，含 4 个 composable）。
 */
data class PlaybackSettingsGroup(
    val miniPlayerMode: SettingsManager.MiniPlayerMode = SettingsManager.MiniPlayerMode.OFF,
    val stopPlaybackOnExit: Boolean = false,
    val backgroundPlaybackEnabled: Boolean = true,
    val audioFocusEnabled: Boolean = true,
    val playerDiagnosticLoggingEnabled: Boolean = true,
    val dashSegmentRequestsEnabled: Boolean = false,
    val qualitySwitchFailureDialogEnabled: Boolean = true,
    val qualitySwitchFailureDialogOnceEnabled: Boolean = false,
    val defaultPlaybackSpeed: Float = 1.0f,
    val rememberLastPlaybackSpeed: Boolean = false,
    val longPressSpeedHintHidden: Boolean = false,
    val longPressSpeedHintScale: Float = 1.0f,
    val longPressSpeedHintAlpha: Float = 0.5f,
    val videoCodecPreference: String = "hev1",
    val videoSecondCodecPreference: String = "avc1",
    val playerInsightMode: PlayerSettingsStore.PlayerInsightMode =
        PlayerSettingsStore.PlayerInsightMode.OFF,
    val defaultAudioQuality: Int = DEFAULT_AUDIO_QUALITY_FOLLOW_LAST,
    val wifiQuality: Int = 80,
    val mobileQuality: Int = 64,
    val autoHighestQualityEnabled: Boolean = false,
    val directedTrafficEnabled: Boolean = false,
    val dataSaverMode: SettingsManager.DataSaverMode =
        SettingsManager.DataSaverMode.MOBILE_ONLY,
    val lowQualityHomeCoverInDataSaver: Boolean = false,
    val pipNoDanmakuEnabled: Boolean = false,
    val isPlaybackLoggedIn: Boolean = false,
    val isPlaybackVip: Boolean = false,
    val autoPlayEnabled: Boolean = true,
    val externalPlaylistAutoContinueEnabled: Boolean = true,
    val resumePlaybackPromptEnabled: Boolean = true,
    val spacePlayedVideoLocatePromptEnabled: Boolean = true,
    val playbackCompletionBehavior: PlaybackCompletionBehavior =
        PlaybackCompletionBehavior.CONTINUE_CURRENT_LOGIC,
    val subtitleAutoPreference: SubtitleAutoPreference = SubtitleAutoPreference.OFF,
    val videoAiSummaryEntryEnabled: Boolean = true,
    val videoNoteEnabled: Boolean = true,
    val videoNoteDefaultCollapsed: Boolean = false,
    val videoInfoDefaultExpanded: Boolean = true,
    val commentFraudDetectionEnabled: Boolean = true,
    val commentMemberDecorationsEnabled: Boolean = false,
    val imagePreviewLongPressSaveEnabled: Boolean = true,
    val commentCollapsedReplyPreviewLimit: Int = 3,
    val clickToPlayEnabled: Boolean = true,
    val portraitPlayerCollapseMode: PortraitPlayerCollapseMode =
        PortraitPlayerCollapseMode.INTRO_ONLY,
    val portraitSwipeToFullscreenEnabled: Boolean = true,
    val centerSwipeToFullscreenEnabled: Boolean = true,
    val slideVolumeBrightnessEnabled: Boolean = true,
    val setSystemBrightnessEnabled: Boolean = false,
    val inlineSwipeSeekSeconds: Int = 30,
    val fullscreenSwipeSeekEnabled: Boolean = true,
    val fullscreenSwipeSeekSeconds: Int = 15,
    val doubleTapSeekEnabled: Boolean = false,
    val seekForwardSeconds: Int = 10,
    val seekBackwardSeconds: Int = 10,
    val hideInteractiveCommandDanmaku: Boolean = false,
    val danmakuCloudSyncEnabled: Boolean = true,
    val pauseOnPlayerCollapseEnabled: Boolean = true,
    val autoRotateEnabled: Boolean = false,
    val fullscreenGestureReverse: Boolean = false,
    val autoEnterFullscreen: Boolean = false,
    val showFullscreenLockButton: Boolean = true,
    val showFullscreenScreenshotButton: Boolean = true,
    val appGestureScreenshotEnabled: Boolean = false,
    val appScreenshotGestureMode: AppScreenshotGestureMode =
        AppScreenshotGestureMode.TOP_RIGHT_TWO_FINGER_LONG_PRESS,
    val appScreenshotCaptureMode: AppScreenshotCaptureMode =
        AppScreenshotCaptureMode.FULL_WINDOW,
    val showFullscreenBatteryLevel: Boolean = true,
    val showFullscreenTime: Boolean = true,
    val showFullscreenActionItems: Boolean = true,
    val bottomProgressBehavior: BottomProgressBehavior = BottomProgressBehavior.ALWAYS_HIDE,
    val progressPeakDanmakuEnabled: Boolean = false,
    val showPlayerCastButton: Boolean = true,
    val showVideoFollowButton: Boolean = true,
    val playerProgressPlacement: PlayerProgressPlacement = PlayerProgressPlacement.ABOVE_CONTROLS,
    val horizontalAdaptationEnabled: Boolean = false,
    val immersiveVideoPageStatusBar: Boolean = false,
    val tabletCommentPanelWidthPreset: TabletCommentPanelWidthPreset =
        TabletCommentPanelWidthPreset.STANDARD,
    val fullscreenMode: FullscreenMode = FullscreenMode.AUTO,
    val fullscreenAspectRatio: FullscreenAspectRatio = FullscreenAspectRatio.FIT,
    val portraitLetterboxAmbientHaze: Boolean = true,
    val autoExitFullscreenMode: AutoExitFullscreenMode = AutoExitFullscreenMode.ALL_PARTS
)

/**
 * 阶段 4 设置页切片：设置根页直读收拢分组（18 项）。
 */
data class SettingsRootGroup(
    val privacyModeEnabled: Boolean = false,
    val privacyContentAuthenticationEnabled: Boolean = false,
    val crashTrackingEnabled: Boolean = DEFAULT_CRASH_TRACKING_ENABLED,
    val analyticsEnabled: Boolean = DEFAULT_ANALYTICS_ENABLED,
    val customDownloadPath: String? = null,
    val downloadExportTreeUri: String? = null,
    val imageSaveTreeUri: String? = null,
    val feedApiType: SettingsFeedApiType = SettingsFeedApiType.WEB,
    val autoCheckUpdateEnabled: Boolean = true,
    val appUpdateChannel: SettingsAppUpdateChannel = SettingsAppUpdateChannel.STABLE,
    val incrementalTimelineRefreshEnabled: Boolean = false,
    val homeRefreshCount: Int = DEFAULT_HOME_REFRESH_COUNT,
    val dynamicVisibleTabIds: Set<String> = defaultDynamicTabVisibleIds,
    val dynamicImagePreviewTextVisible: Boolean = true,
    val dynamicAllTabHorizontalUserListVisible: Boolean = false,
    val dynamicTopBarCollapseOnScroll: Boolean = false,
    val dynamicFeedLayoutMode: SettingsDynamicFeedLayoutMode =
        SettingsDynamicFeedLayoutMode.WATERFALL,
    val defaultDownloadPath: String = "",
)

private data class SettingsGroups(
    val appearance: AppearanceSettingsGroup,
    val bottomBar: BottomBarSettingsGroup,
    val playback: PlaybackSettingsGroup,
    val settingsRoot: SettingsRootGroup,
)

private fun <T> Flow<T>.asAnyFlow(): Flow<Any?> = map { it }


class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private data class DiagnosticsState(
        val installedApkSha256: String? = null,
        val currentReleaseEvidence: AppUpdateCheckResult? = null,
        val loadState: SettingsDiagnosticsLoadState = SettingsDiagnosticsLoadState.NOT_LOADED,
    )

    private data class UiSettingsGroup1(
        val gestureSensitivity: Float,
        val appListItemStyle: AppListItemStyle,
        val appFontSizePreset: AppFontSizePreset,
        val appFontFileName: String,
        val appFontDisplayName: String,
        val appUiScalePreset: AppUiScalePreset,
        val appDpiOverridePercent: Int
    )

    // 本地状态流：缓存大小
    private val _cacheSize = MutableStateFlow("计算中...")
    private val _cacheBreakdown = MutableStateFlow<CacheUtils.CacheBreakdown?>(null)
    private val _diagnosticsState = MutableStateFlow(DiagnosticsState())
    private var diagnosticsLoadJob: Job? = null

    //  [核心修复] 分步合并，解决 combine 参数限制报错
    // 第 1 步：合并前 4 个设置
    private val coreSettingsFlow = combine(
        SettingsManager.getHwDecode(context).asAnyFlow(),
        SettingsManager.getThemeMode(context).asAnyFlow(),
        SettingsManager.getDarkThemeStyle(context).asAnyFlow(),
        SettingsManager.getAppLanguage(context).asAnyFlow(),
        SettingsManager.getMiniPlayerMode(context)
            .map { it != SettingsManager.MiniPlayerMode.OFF }
            .asAnyFlow()
    ) { values ->
        CoreSettings(
            hwDecode = values[0] as Boolean,
            themeMode = values[1] as AppThemeMode,
            darkThemeStyle = values[2] as DarkThemeStyle,
            appLanguage = values[3] as AppLanguage,
            bgPlay = values[4] as Boolean
        )
    }
    
    // 第 2 步：合并界面设置 (分两组，每组最多5个)
    private val uiSettingsFlow1 = combine(
        SettingsManager.getGestureSensitivity(context).asAnyFlow(),
        SettingsManager.getAppListItemStyle(context).asAnyFlow(),
        SettingsManager.getAppFontSizePreset(context).asAnyFlow(),
        SettingsManager.getAppFontFileName(context).asAnyFlow(),
        SettingsManager.getAppFontDisplayName(context).asAnyFlow(),
        SettingsManager.getAppUiScalePreset(context).asAnyFlow(),
        SettingsManager.getAppDpiOverridePercent(context).asAnyFlow()
    ) { values ->
        UiSettingsGroup1(
            gestureSensitivity = values[0] as Float,
            appListItemStyle = values[1] as AppListItemStyle,
            appFontSizePreset = values[2] as AppFontSizePreset,
            appFontFileName = values[3] as String,
            appFontDisplayName = values[4] as String,
            appUiScalePreset = values[5] as AppUiScalePreset,
            appDpiOverridePercent = values[6] as Int
        )
    }
    
    private val uiSettingsFlow2 = combine(
        SettingsManager.getBottomBarFloating(context).asAnyFlow(),
        SettingsManager.getBottomBarLabelMode(context).asAnyFlow(),
        SettingsManager.getDisplayMode(context).asAnyFlow(),
        SettingsManager.getCardAnimationEnabled(context).asAnyFlow(), // [Restored]
        SettingsManager.getCardTransitionEnabled(context).asAnyFlow(),
        SettingsManager.getVideoSharedTransitionSpeed(context).asAnyFlow(),
        SettingsManager.getVideoSharedTransitionCustomDurationMillis(context).asAnyFlow(),
        SettingsManager.getSmartVisualGuardEnabled(context).asAnyFlow(), // [New]
        SettingsManager.getHapticFeedbackEnabled(context).asAnyFlow(), // [新增]
        SettingsManager.getBottomBarSearchEnabled(context).asAnyFlow(),
        SettingsManager.getBottomBarSearchAutoExpandMode(context).asAnyFlow(),
        SettingsManager.getBottomBarSearchLayoutMode(context).asAnyFlow(),
        SettingsManager.getTabletUseSidebar(context).asAnyFlow(), // [New]
        SettingsManager.getHeaderCollapseEnabled(context).asAnyFlow(),
        SettingsManager.getGridColumnCount(context).asAnyFlow(), // [New]
        SettingsManager.getHomeFeedCardWidthPreset(context).asAnyFlow(),
        // 阶段 4 设置页切片：动画/效果页三项
        SettingsManager.getVideoTransitionRealtimeBlurEnabled(context).asAnyFlow(),
        SettingsManager.getLiveSurfaceCardTransitionEnabled(context).asAnyFlow(),
        SettingsManager.getFullScreenSwipeBackEnabled(context).asAnyFlow()
    ) { values ->
        val isBottomBarFloating = values[0] as Boolean
        val labelMode = values[1] as Int
        val displayMode = values[2] as Int
        val cardAnimation = values[3] as Boolean
        val cardTransition = values[4] as Boolean
        val videoSharedTransitionSpeed = values[5] as VideoSharedTransitionSpeed
        val videoSharedTransitionCustomDurationMillis = values[6] as Int
        val smartVisualGuard = values[7] as Boolean
        val hapticFeedback = values[8] as Boolean
        val bottomBarSearch = values[9] as Boolean
        val bottomBarSearchAutoExpandMode = values[10] as BottomBarSearchAutoExpandMode
        val bottomBarSearchLayoutMode = values[11] as BottomBarSearchLayoutMode
        val tabletUseSidebar = values[12] as Boolean
        val headerCollapse = values[13] as Boolean
        val gridColumnCount = values[14] as Int
        val homeFeedCardWidthPreset = values[15] as HomeFeedCardWidthPreset
        val videoTransitionRealtimeBlur = values[16] as Boolean
        val liveSurfaceCardTransition = values[17] as Boolean
        val fullScreenSwipeBack = values[18] as Boolean
        
        data class Ui2(
            val f: Boolean,
            val l: Int,
            val d: Int,
            val ca: Boolean,
            val ct: Boolean,
            val vsts: VideoSharedTransitionSpeed,
            val vstcdm: Int,
            val svg: Boolean,
            val h: Boolean,
            val bbs: Boolean,
            val bbsam: BottomBarSearchAutoExpandMode,
            val bbslm: BottomBarSearchLayoutMode,
            val tus: Boolean,
            val hc: Boolean,
            val gcc: Int,
            val hfcwp: HomeFeedCardWidthPreset,
            val vtrb: Boolean,
            val lsct: Boolean,
            val fssb: Boolean
        )
        Ui2(
            isBottomBarFloating,
            labelMode,
            displayMode,
            cardAnimation,
            cardTransition,
            videoSharedTransitionSpeed,
            videoSharedTransitionCustomDurationMillis,
            smartVisualGuard,
            hapticFeedback,
            bottomBarSearch,
            bottomBarSearchAutoExpandMode,
            bottomBarSearchLayoutMode,
            tabletUseSidebar,
            headerCollapse,
            gridColumnCount,
            homeFeedCardWidthPreset,
            videoTransitionRealtimeBlur,
            liveSurfaceCardTransition,
            fullScreenSwipeBack
        )
    }

    // 合并所有 UI 设置
    private val uiSettingsFlow = combine(uiSettingsFlow1, uiSettingsFlow2) { ui1, ui2 ->
        // ui2: Ui2 class
        ExtraSettings(
            gestureSensitivity = ui1.gestureSensitivity,
            appListItemStyle = ui1.appListItemStyle,
            appFontSizePreset = ui1.appFontSizePreset,
            appFontFileName = ui1.appFontFileName,
            appFontDisplayName = ui1.appFontDisplayName,
            appUiScalePreset = ui1.appUiScalePreset,
            appDpiOverridePercent = ui1.appDpiOverridePercent,
            isBottomBarFloating = ui2.f,
            bottomBarLabelMode = ui2.l,
            displayMode = ui2.d,
            cardAnimationEnabled = ui2.ca,
            cardTransitionEnabled = ui2.ct,
            videoSharedTransitionSpeed = ui2.vsts,
            videoSharedTransitionCustomDurationMillis = ui2.vstcdm,
            smartVisualGuardEnabled = ui2.svg,
            hapticFeedbackEnabled = ui2.h, // [新增]
            bottomBarSearchEnabled = ui2.bbs,
            bottomBarSearchAutoExpandMode = ui2.bbsam,
            bottomBarSearchLayoutMode = ui2.bbslm,
            tabletUseSidebar = ui2.tus, // [New]
            isHeaderCollapseEnabled = ui2.hc,
            gridColumnCount = ui2.gcc, // [New]
            homeFeedCardWidthPreset = ui2.hfcwp,
            videoTransitionRealtimeBlurEnabled = ui2.vtrb,
            liveSurfaceCardTransitionEnabled = ui2.lsct,
            fullScreenSwipeBackEnabled = ui2.fssb,
            headerBlurEnabled = false, // 暂存，将在下一步合并
            bottomBarBlurEnabled = false, // 暂存
            blurIntensity = BlurIntensity.THIN // 暂存
        )
    }
    
    // 第 3 步：合并模糊设置 (3个)
    private val blurSettingsFlow = combine(
        SettingsManager.getHeaderBlurEnabled(context),
        SettingsManager.getBottomBarBlurEnabled(context),
        SettingsManager.getBlurIntensity(context)  //  添加模糊强度
    ) { headerBlur, bottomBarBlur, blurIntensity ->
        Triple(headerBlur, bottomBarBlur, blurIntensity)
    }
    
    // 第 4 步：合并 UI 和 模糊设置
    private val extraSettingsFlow = combine(uiSettingsFlow, blurSettingsFlow) { uiSettings, blur ->
        uiSettings.copy(
            headerBlurEnabled = blur.first,
            bottomBarBlurEnabled = blur.second,
            blurIntensity = blur.third
        )
    }
    
    //  第 4.5 步：合并实验性功能设置
    private val experimentalSettingsFlow = combine(
        SettingsManager.getAuto1080p(context),
        SettingsManager.getAutoSkipOpEd(context),
        SettingsManager.getPrefetchVideo(context),
        SettingsManager.getDoubleTapLike(context),
        SettingsManager.getSponsorBlockEnabled(context),
        SettingsManager.getSponsorBlockAutoSkip(context)
    ) { values ->
        ExperimentalSettings(
            auto1080p = values[0],
            autoSkipOpEd = values[1],
            prefetchVideo = values[2],
            doubleTapLike = values[3],
            sponsorBlockEnabled = values[4],
            sponsorBlockAutoSkip = values[5]
        )
    }
    
    // 第 5 步：合并两组设置
    private val baseSettingsFlow = combine(coreSettingsFlow, extraSettingsFlow) { core, extra ->
        BaseSettings(
            hwDecode = core.hwDecode,
            themeMode = core.themeMode,
            darkThemeStyle = core.darkThemeStyle,
            appLanguage = core.appLanguage,
            appFontSizePreset = extra.appFontSizePreset,
            appFontFileName = extra.appFontFileName,
            appFontDisplayName = extra.appFontDisplayName,
            appUiScalePreset = extra.appUiScalePreset,
            appDpiOverridePercent = extra.appDpiOverridePercent,
            bgPlay = core.bgPlay,
            gestureSensitivity = extra.gestureSensitivity,
            appListItemStyle = extra.appListItemStyle,
            isBottomBarFloating = extra.isBottomBarFloating,
            bottomBarLabelMode = extra.bottomBarLabelMode,
            headerBlurEnabled = extra.headerBlurEnabled,
            bottomBarBlurEnabled = extra.bottomBarBlurEnabled,
            blurIntensity = extra.blurIntensity,  //  模糊强度
            displayMode = extra.displayMode,
            cardAnimationEnabled = extra.cardAnimationEnabled,
            cardTransitionEnabled = extra.cardTransitionEnabled,
            videoSharedTransitionSpeed = extra.videoSharedTransitionSpeed,
            videoSharedTransitionCustomDurationMillis =
                extra.videoSharedTransitionCustomDurationMillis,
            smartVisualGuardEnabled = extra.smartVisualGuardEnabled,
            hapticFeedbackEnabled = extra.hapticFeedbackEnabled, // [新增]
            bottomBarSearchEnabled = extra.bottomBarSearchEnabled,
            bottomBarSearchAutoExpandMode = extra.bottomBarSearchAutoExpandMode,
            bottomBarSearchLayoutMode = extra.bottomBarSearchLayoutMode,
            tabletUseSidebar = extra.tabletUseSidebar, // [New]
            isHeaderCollapseEnabled = extra.isHeaderCollapseEnabled,
            gridColumnCount = extra.gridColumnCount, // [New]
            homeFeedCardWidthPreset = extra.homeFeedCardWidthPreset,
            videoTransitionRealtimeBlurEnabled = extra.videoTransitionRealtimeBlurEnabled,
            liveSurfaceCardTransitionEnabled = extra.liveSurfaceCardTransitionEnabled,
            fullScreenSwipeBackEnabled = extra.fullScreenSwipeBackEnabled
        )

    }

    // 第 6 步：与缓存大小和实验性功能合并
    private val cacheFlow = combine(_cacheSize, _cacheBreakdown) { size, breakdown ->
        Pair(size, breakdown)
    }
    
    // 阶段 4 设置页切片：外观页直读收拢（13+ 项）
    private val appearanceSettingsFlow = combine(
        SettingsManager.getSingleChoicePresentation(context).asAnyFlow(),
        SettingsManager.getCompactVideoStatsOnCover(context).asAnyFlow(),
        SettingsManager.getHomeWallpaperUri(context).asAnyFlow(),
        SettingsManager.getHomeWallpaperEffectMode(context).asAnyFlow(),
        SettingsManager.getHomeWallpaperEffectScope(context).asAnyFlow(),
        SettingsManager.getHomeUpBadgesVisible(context).asAnyFlow(),
        SettingsManager.getHomeUpAvatarsVisible(context).asAnyFlow(),
        SettingsManager.getHomeDurationStyle(context).asAnyFlow(),
        SettingsManager.getHomeHeroCarouselEnabled(context).asAnyFlow(),
        SettingsManager.getHomeHeroCarouselAutoplayEnabled(context).asAnyFlow(),
        SettingsManager.getCommonListHeaderCollapseMode(context).asAnyFlow(),
        SettingsManager.getHomeFeedCardStyle(context).asAnyFlow(),
        SettingsManager.getShowOnlineCount(context).asAnyFlow(),
        SettingsManager.isSplashEnabled(context).asAnyFlow(),
        SettingsManager.getSplashRandomEnabled(context).asAnyFlow(),
        SettingsManager.getSplashWallpaperUri(context).asAnyFlow(),
        SettingsManager.getSplashRandomPoolUris(context).asAnyFlow(),
        SettingsManager.getSplashIconAnimationEnabled(context).asAnyFlow(),
        // 阶段 4 切片：底栏页直读收拢（11 项，并入 appearance flow 保持 combine ≤5 参数）
        SettingsManager.getBottomBarOrder(context).asAnyFlow(),
        SettingsManager.getBottomBarVisibleTabs(context).asAnyFlow(),
        SettingsManager.getTopTabOrder(context).asAnyFlow(),
        SettingsManager.getTopTabVisibleTabs(context).asAnyFlow(),
        SettingsManager.getTopTabLabelMode(context).asAnyFlow(),
        SettingsManager.getHomeHeaderBlurMode(context).asAnyFlow(),
        SettingsManager.getHomeTopLayoutOrder(context).asAnyFlow(),
        SettingsManager.getHomeHeaderCollapseMode(context).asAnyFlow(),
        SettingsManager.getHomeTopRightAction(context).asAnyFlow(),
        SettingsManager.getBottomBarVisibilityMode(context).asAnyFlow(),
        SettingsManager.getSidebarAccountSwitcherEnabled(context).asAnyFlow(),
        SettingsManager.getBottomBarItemColors(context).asAnyFlow(),
    ) { values ->
        val appearance = AppearanceSettingsGroup(
            singleChoicePresentation = values[0] as AppSingleChoicePresentation,
            compactVideoStatsOnCover = values[1] as Boolean,
            homeWallpaperUri = values[2] as String,
            homeWallpaperEffectMode = values[3] as HomeWallpaperEffectMode,
            homeWallpaperEffectScope = values[4] as HomeWallpaperEffectScope,
            homeUpBadgesVisible = values[5] as Boolean,
            homeUpAvatarsVisible = values[6] as Boolean,
            homeDurationStyle = values[7] as HomeDurationStyle,
            homeHeroCarouselEnabled = values[8] as Boolean,
            homeHeroCarouselAutoplayEnabled = values[9] as Boolean,
            commonListHeaderCollapseMode = values[10] as CommonListHeaderCollapseMode,
            homeFeedCardStyle = values[11] as HomeFeedCardStyle,
            showOnlineCount = values[12] as Boolean,
            splashEnabled = values[13] as Boolean,
            splashRandomEnabled = values[14] as Boolean,
            splashWallpaperUri = values[15] as String,
            splashRandomPoolUris = values[16] as List<String>,
            splashIconAnimationEnabled = values[17] as Boolean
        )
        val bottomBar = BottomBarSettingsGroup(
            bottomBarOrder = values[18] as List<String>,
            bottomBarVisibleTabs = values[19] as Set<String>,
            topTabOrder = values[20] as List<String>,
            topTabVisibleTabs = values[21] as Set<String>,
            topTabLabelMode = values[22] as Int,
            headerBlurMode = values[23] as HomeHeaderBlurMode,
            homeTopLayoutOrder = values[24] as HomeTopLayoutOrder,
            homeHeaderCollapseMode = values[25] as HomeHeaderCollapseMode,
            homeTopRightAction = values[26] as HomeTopRightAction,
            bottomBarVisibilityMode = values[27] as SettingsManager.BottomBarVisibilityMode,
            sidebarAccountSwitcherEnabled = values[28] as Boolean,
            bottomBarItemColors = values[29] as Map<String, Int>
        )
        appearance to bottomBar
    }

    // 阶段 4 设置页切片：播放设置页直读收拢（78 项，独立 flow 避免撑大外观 combine）
    private val playbackSettingsFlow = combine(
        SettingsManager.getMiniPlayerMode(context).asAnyFlow(),
        SettingsManager.getStopPlaybackOnExit(context).asAnyFlow(),
        SettingsManager.getBackgroundPlaybackEnabled(context).asAnyFlow(),
        SettingsManager.getAudioFocusEnabled(context).asAnyFlow(),
        SettingsManager.getPlayerDiagnosticLoggingEnabled(context).asAnyFlow(),
        SettingsManager.getDashSegmentRequestsEnabled(context).asAnyFlow(),
        SettingsManager.getQualitySwitchFailureDialogEnabled(context).asAnyFlow(),
        SettingsManager.getQualitySwitchFailureDialogOnceEnabled(context).asAnyFlow(),
        SettingsManager.getDefaultPlaybackSpeed(context).asAnyFlow(),
        SettingsManager.getRememberLastPlaybackSpeed(context).asAnyFlow(),
        SettingsManager.getLongPressSpeedHintHidden(context).asAnyFlow(),
        SettingsManager.getLongPressSpeedHintScale(context).asAnyFlow(),
        SettingsManager.getLongPressSpeedHintAlpha(context).asAnyFlow(),
        SettingsManager.getVideoCodec(context).asAnyFlow(),
        SettingsManager.getVideoSecondCodec(context).asAnyFlow(),
        SettingsManager.getPlayerInsightMode(context).asAnyFlow(),
        PlayerSettingsStore.getDefaultAudioQuality(context).asAnyFlow(),
        SettingsManager.getWifiQuality(context).asAnyFlow(),
        SettingsManager.getMobileQuality(context).asAnyFlow(),
        SettingsManager.getAutoHighestQuality(context).asAnyFlow(),
        SettingsManager.getBiliDirectedTrafficEnabled(context).asAnyFlow(),
        SettingsManager.getDataSaverMode(context).asAnyFlow(),
        SettingsManager.getHomeSettings(context)
            .map { it.lowQualityHomeCoverInDataSaver }.asAnyFlow(),
        SettingsManager.getPipNoDanmakuEnabled(context).asAnyFlow(),
        flowOf(VideoRepository.isPlaybackLoggedIn()).asAnyFlow(),
        flowOf(VideoRepository.isPlaybackVip()).asAnyFlow(),
        SettingsManager.getAutoPlay(context).asAnyFlow(),
        SettingsManager.getExternalPlaylistAutoContinue(context).asAnyFlow(),
        SettingsManager.getResumePlaybackPromptEnabled(context).asAnyFlow(),
        SettingsManager.getSpacePlayedVideoLocatePromptEnabled(context).asAnyFlow(),
        SettingsManager.getPlaybackCompletionBehavior(context).asAnyFlow(),
        SettingsManager.getSubtitleAutoPreference(context).asAnyFlow(),
        SettingsManager.getVideoAiSummaryEntryEnabled(context).asAnyFlow(),
        SettingsManager.getVideoNoteEnabled(context).asAnyFlow(),
        SettingsManager.getVideoNoteDefaultCollapsed(context).asAnyFlow(),
        SettingsManager.getVideoInfoDefaultExpanded(context).asAnyFlow(),
        SettingsManager.getCommentFraudDetectionEnabled(context).asAnyFlow(),
        SettingsManager.getCommentMemberDecorationsEnabled(context).asAnyFlow(),
        SettingsManager.getImagePreviewLongPressSaveEnabled(context).asAnyFlow(),
        SettingsManager.getCommentCollapsedReplyPreviewLimit(context).asAnyFlow(),
        SettingsManager.getClickToPlay(context).asAnyFlow(),
        SettingsManager.getPortraitPlayerCollapseMode(context).asAnyFlow(),
        SettingsManager.getPortraitSwipeToFullscreenEnabled(context).asAnyFlow(),
        SettingsManager.getCenterSwipeToFullscreenEnabled(context).asAnyFlow(),
        SettingsManager.getSlideVolumeBrightnessEnabled(context).asAnyFlow(),
        SettingsManager.getSetSystemBrightnessEnabled(context).asAnyFlow(),
        SettingsManager.getInlineSwipeSeekSeconds(context).asAnyFlow(),
        SettingsManager.getFullscreenSwipeSeekEnabled(context).asAnyFlow(),
        SettingsManager.getFullscreenSwipeSeekSeconds(context).asAnyFlow(),
        SettingsManager.getDoubleTapSeekEnabled(context).asAnyFlow(),
        SettingsManager.getSeekForwardSeconds(context).asAnyFlow(),
        SettingsManager.getSeekBackwardSeconds(context).asAnyFlow(),
        SettingsManager.getDanmakuHideInteractiveCommands(context).asAnyFlow(),
        SettingsManager.getDanmakuCloudSyncEnabled(context).asAnyFlow(),
        SettingsManager.getPauseOnPlayerCollapseEnabled(context).asAnyFlow(),
        SettingsManager.getAutoRotateEnabled(context).asAnyFlow(),
        SettingsManager.getFullscreenGestureReverse(context).asAnyFlow(),
        SettingsManager.getAutoEnterFullscreen(context).asAnyFlow(),
        SettingsManager.getShowFullscreenLockButton(context).asAnyFlow(),
        SettingsManager.getShowFullscreenScreenshotButton(context).asAnyFlow(),
        SettingsManager.getAppGestureScreenshotEnabled(context).asAnyFlow(),
        SettingsManager.getAppScreenshotGestureMode(context).asAnyFlow(),
        SettingsManager.getAppScreenshotCaptureMode(context).asAnyFlow(),
        SettingsManager.getShowFullscreenBatteryLevel(context).asAnyFlow(),
        SettingsManager.getShowFullscreenTime(context).asAnyFlow(),
        SettingsManager.getShowFullscreenActionItems(context).asAnyFlow(),
        SettingsManager.getBottomProgressBehavior(context).asAnyFlow(),
        SettingsManager.getProgressPeakDanmakuEnabled(context).asAnyFlow(),
        SettingsManager.getPlayerControlVisibilitySettings(context).asAnyFlow(),
        SettingsManager.getPlayerProgressPlacement(context).asAnyFlow(),
        SettingsManager.getHorizontalAdaptationEnabled(context).asAnyFlow(),
        SettingsManager.getHideVideoPageStatusBar(context).asAnyFlow(),
        SettingsManager.getTabletCommentPanelWidthPreset(context).asAnyFlow(),
        SettingsManager.getFullscreenMode(context).asAnyFlow(),
        SettingsManager.getFullscreenAspectRatio(context).asAnyFlow(),
        SettingsManager.getPortraitLetterboxAmbientHaze(context).asAnyFlow(),
        SettingsManager.getAutoExitFullscreenMode(context).asAnyFlow()
    ) { values ->
        PlaybackSettingsGroup(
            miniPlayerMode = values[0] as SettingsManager.MiniPlayerMode,
            stopPlaybackOnExit = values[1] as Boolean,
            backgroundPlaybackEnabled = values[2] as Boolean,
            audioFocusEnabled = values[3] as Boolean,
            playerDiagnosticLoggingEnabled = values[4] as Boolean,
            dashSegmentRequestsEnabled = values[5] as Boolean,
            qualitySwitchFailureDialogEnabled = values[6] as Boolean,
            qualitySwitchFailureDialogOnceEnabled = values[7] as Boolean,
            defaultPlaybackSpeed = values[8] as Float,
            rememberLastPlaybackSpeed = values[9] as Boolean,
            longPressSpeedHintHidden = values[10] as Boolean,
            longPressSpeedHintScale = values[11] as Float,
            longPressSpeedHintAlpha = values[12] as Float,
            videoCodecPreference = values[13] as String,
            videoSecondCodecPreference = values[14] as String,
            playerInsightMode = values[15] as PlayerSettingsStore.PlayerInsightMode,
            defaultAudioQuality = values[16] as Int,
            wifiQuality = values[17] as Int,
            mobileQuality = values[18] as Int,
            autoHighestQualityEnabled = values[19] as Boolean,
            directedTrafficEnabled = values[20] as Boolean,
            dataSaverMode = values[21] as SettingsManager.DataSaverMode,
            lowQualityHomeCoverInDataSaver = values[22] as Boolean,
            pipNoDanmakuEnabled = values[23] as Boolean,
            isPlaybackLoggedIn = values[24] as Boolean,
            isPlaybackVip = values[25] as Boolean,
            autoPlayEnabled = values[26] as Boolean,
            externalPlaylistAutoContinueEnabled = values[27] as Boolean,
            resumePlaybackPromptEnabled = values[28] as Boolean,
            spacePlayedVideoLocatePromptEnabled = values[29] as Boolean,
            playbackCompletionBehavior = values[30] as PlaybackCompletionBehavior,
            subtitleAutoPreference = values[31] as SubtitleAutoPreference,
            videoAiSummaryEntryEnabled = values[32] as Boolean,
            videoNoteEnabled = values[33] as Boolean,
            videoNoteDefaultCollapsed = values[34] as Boolean,
            videoInfoDefaultExpanded = values[35] as Boolean,
            commentFraudDetectionEnabled = values[36] as Boolean,
            commentMemberDecorationsEnabled = values[37] as Boolean,
            imagePreviewLongPressSaveEnabled = values[38] as Boolean,
            commentCollapsedReplyPreviewLimit = values[39] as Int,
            clickToPlayEnabled = values[40] as Boolean,
            portraitPlayerCollapseMode = values[41] as PortraitPlayerCollapseMode,
            portraitSwipeToFullscreenEnabled = values[42] as Boolean,
            centerSwipeToFullscreenEnabled = values[43] as Boolean,
            slideVolumeBrightnessEnabled = values[44] as Boolean,
            setSystemBrightnessEnabled = values[45] as Boolean,
            inlineSwipeSeekSeconds = values[46] as Int,
            fullscreenSwipeSeekEnabled = values[47] as Boolean,
            fullscreenSwipeSeekSeconds = values[48] as Int,
            doubleTapSeekEnabled = values[49] as Boolean,
            seekForwardSeconds = values[50] as Int,
            seekBackwardSeconds = values[51] as Int,
            hideInteractiveCommandDanmaku = values[52] as Boolean,
            danmakuCloudSyncEnabled = values[53] as Boolean,
            pauseOnPlayerCollapseEnabled = values[54] as Boolean,
            autoRotateEnabled = values[55] as Boolean,
            fullscreenGestureReverse = values[56] as Boolean,
            autoEnterFullscreen = values[57] as Boolean,
            showFullscreenLockButton = values[58] as Boolean,
            showFullscreenScreenshotButton = values[59] as Boolean,
            appGestureScreenshotEnabled = values[60] as Boolean,
            appScreenshotGestureMode = values[61] as AppScreenshotGestureMode,
            appScreenshotCaptureMode = values[62] as AppScreenshotCaptureMode,
            showFullscreenBatteryLevel = values[63] as Boolean,
            showFullscreenTime = values[64] as Boolean,
            showFullscreenActionItems = values[65] as Boolean,
            bottomProgressBehavior = values[66] as BottomProgressBehavior,
            progressPeakDanmakuEnabled = values[67] as Boolean,
            showPlayerCastButton =
                (values[68] as PlayerControlVisibilitySettings).showCastButton,
            showVideoFollowButton =
                (values[68] as PlayerControlVisibilitySettings).showFollowButton,
            playerProgressPlacement = values[69] as PlayerProgressPlacement,
            horizontalAdaptationEnabled = values[70] as Boolean,
            immersiveVideoPageStatusBar = values[71] as Boolean,
            tabletCommentPanelWidthPreset = values[72] as TabletCommentPanelWidthPreset,
            fullscreenMode = values[73] as FullscreenMode,
            fullscreenAspectRatio = values[74] as FullscreenAspectRatio,
            portraitLetterboxAmbientHaze = values[75] as Boolean,
            autoExitFullscreenMode = values[76] as AutoExitFullscreenMode
        )
    }

    // 阶段 4 设置页切片：设置根页直读收拢（18 项，独立 flow）
    private val settingsRootFlow = combine(
        SettingsManager.getPrivacyModeEnabled(context).asAnyFlow(),
        SettingsManager.getPrivacyContentAuthenticationEnabled(context).asAnyFlow(),
        SettingsManager.getCrashTrackingEnabled(context).asAnyFlow(),
        SettingsManager.getAnalyticsEnabled(context).asAnyFlow(),
        SettingsManager.getDownloadPath(context).asAnyFlow(),
        SettingsManager.getDownloadExportTreeUri(context).asAnyFlow(),
        SettingsManager.getImageSaveTreeUri(context).asAnyFlow(),
        SettingsManager.getFeedApiType(context).asAnyFlow(),
        SettingsManager.getAutoCheckAppUpdate(context).asAnyFlow(),
        SettingsManager.getAppUpdateChannel(context).asAnyFlow(),
        SettingsManager.getIncrementalTimelineRefresh(context).asAnyFlow(),
        SettingsManager.getHomeRefreshCount(context).asAnyFlow(),
        SettingsManager.getDynamicTabVisibleTabs(context).asAnyFlow(),
        SettingsManager.getDynamicImagePreviewTextVisible(context).asAnyFlow(),
        SettingsManager.getDynamicAllTabHorizontalUserListVisible(context).asAnyFlow(),
        SettingsManager.getDynamicTopBarCollapseOnScroll(context).asAnyFlow(),
        SettingsManager.getDynamicFeedLayoutMode(context).asAnyFlow(),
        flowOf(SettingsManager.getDefaultDownloadPath(context)).asAnyFlow(),
    ) { values ->
        SettingsRootGroup(
            privacyModeEnabled = values[0] as Boolean,
            privacyContentAuthenticationEnabled = values[1] as Boolean,
            crashTrackingEnabled = values[2] as Boolean,
            analyticsEnabled = values[3] as Boolean,
            customDownloadPath = values[4] as String?,
            downloadExportTreeUri = values[5] as String?,
            imageSaveTreeUri = values[6] as String?,
            feedApiType = values[7] as SettingsFeedApiType,
            autoCheckUpdateEnabled = values[8] as Boolean,
            appUpdateChannel = values[9] as SettingsAppUpdateChannel,
            incrementalTimelineRefreshEnabled = values[10] as Boolean,
            homeRefreshCount = values[11] as Int,
            dynamicVisibleTabIds = values[12] as Set<String>,
            dynamicImagePreviewTextVisible = values[13] as Boolean,
            dynamicAllTabHorizontalUserListVisible = values[14] as Boolean,
            dynamicTopBarCollapseOnScroll = values[15] as Boolean,
            dynamicFeedLayoutMode = values[16] as SettingsDynamicFeedLayoutMode,
            defaultDownloadPath = values[17] as String,
        )
    }

    private val settingsGroupFlow = combine(
        appearanceSettingsFlow,
        playbackSettingsFlow,
        settingsRootFlow,
    ) { appearancePair, playback, settingsRoot ->
        SettingsGroups(
            appearance = appearancePair.first,
            bottomBar = appearancePair.second,
            playback = playback,
            settingsRoot = settingsRoot,
        )
    }

    val state: StateFlow<SettingsUiState> = combine(
        baseSettingsFlow,
        cacheFlow,
        experimentalSettingsFlow,
        _diagnosticsState,
        settingsGroupFlow,
    ) { settings, cache, experimental, diagnostics, settingsGroups ->
        val appearance = settingsGroups.appearance
        val bottomBar = settingsGroups.bottomBar
        val playback = settingsGroups.playback
        val settingsRoot = settingsGroups.settingsRoot
        SettingsUiState(
            hwDecode = settings.hwDecode,
            themeMode = settings.themeMode,
            darkThemeStyle = settings.darkThemeStyle,
            appLanguage = settings.appLanguage,
            appFontSizePreset = settings.appFontSizePreset,
            appFontFileName = settings.appFontFileName,
            appFontDisplayName = settings.appFontDisplayName,
            appUiScalePreset = settings.appUiScalePreset,
            appDpiOverridePercent = settings.appDpiOverridePercent,
            bgPlay = settings.bgPlay,
            gestureSensitivity = settings.gestureSensitivity,
            appListItemStyle = settings.appListItemStyle,
            isBottomBarFloating = settings.isBottomBarFloating,
            bottomBarLabelMode = settings.bottomBarLabelMode,
            headerBlurEnabled = settings.headerBlurEnabled,
            bottomBarBlurEnabled = settings.bottomBarBlurEnabled,
            blurIntensity = settings.blurIntensity,  //  模糊强度
            displayMode = settings.displayMode,
            cardAnimationEnabled = settings.cardAnimationEnabled,
            cardTransitionEnabled = settings.cardTransitionEnabled,
            videoSharedTransitionSpeed = settings.videoSharedTransitionSpeed,
            videoSharedTransitionCustomDurationMillis =
                settings.videoSharedTransitionCustomDurationMillis,
            smartVisualGuardEnabled = settings.smartVisualGuardEnabled,
            hapticFeedbackEnabled = settings.hapticFeedbackEnabled, // [新增]
            bottomBarSearchEnabled = settings.bottomBarSearchEnabled,
            bottomBarSearchAutoExpandMode = settings.bottomBarSearchAutoExpandMode,
            bottomBarSearchLayoutMode = settings.bottomBarSearchLayoutMode,
            tabletUseSidebar = settings.tabletUseSidebar, // [New]
            isHeaderCollapseEnabled = settings.isHeaderCollapseEnabled,
            gridColumnCount = settings.gridColumnCount, // [New]
            homeFeedCardWidthPreset = settings.homeFeedCardWidthPreset,
            videoTransitionRealtimeBlurEnabled = settings.videoTransitionRealtimeBlurEnabled,
            liveSurfaceCardTransitionEnabled = settings.liveSurfaceCardTransitionEnabled,
            fullScreenSwipeBackEnabled = settings.fullScreenSwipeBackEnabled,
            singleChoicePresentation = appearance.singleChoicePresentation,
            compactVideoStatsOnCover = appearance.compactVideoStatsOnCover,
            homeWallpaperUri = appearance.homeWallpaperUri,
            homeWallpaperEffectMode = appearance.homeWallpaperEffectMode,
            homeWallpaperEffectScope = appearance.homeWallpaperEffectScope,
            homeUpBadgesVisible = appearance.homeUpBadgesVisible,
            homeUpAvatarsVisible = appearance.homeUpAvatarsVisible,
            homeDurationStyle = appearance.homeDurationStyle,
            homeHeroCarouselEnabled = appearance.homeHeroCarouselEnabled,
            homeHeroCarouselAutoplayEnabled = appearance.homeHeroCarouselAutoplayEnabled,
            commonListHeaderCollapseMode = appearance.commonListHeaderCollapseMode,
            homeFeedCardStyle = appearance.homeFeedCardStyle,
            showOnlineCount = appearance.showOnlineCount,
            splashEnabled = appearance.splashEnabled,
            splashRandomEnabled = appearance.splashRandomEnabled,
            splashWallpaperUri = appearance.splashWallpaperUri,
            splashRandomPoolUris = appearance.splashRandomPoolUris,
            splashIconAnimationEnabled = appearance.splashIconAnimationEnabled,
            bottomBarOrder = bottomBar.bottomBarOrder,
            bottomBarVisibleTabs = bottomBar.bottomBarVisibleTabs,
            topTabOrder = bottomBar.topTabOrder,
            topTabVisibleTabs = bottomBar.topTabVisibleTabs,
            topTabLabelMode = bottomBar.topTabLabelMode,
            headerBlurMode = bottomBar.headerBlurMode,
            homeTopLayoutOrder = bottomBar.homeTopLayoutOrder,
            homeHeaderCollapseMode = bottomBar.homeHeaderCollapseMode,
            homeTopRightAction = bottomBar.homeTopRightAction,
            bottomBarVisibilityMode = bottomBar.bottomBarVisibilityMode,
            sidebarAccountSwitcherEnabled = bottomBar.sidebarAccountSwitcherEnabled,
            bottomBarItemColors = bottomBar.bottomBarItemColors,
            playback = playback,
            settingsRoot = settingsRoot,

            cacheSize = cache.first,
            cacheBreakdown = cache.second,  //  详细缓存统计
            installedApkSha256 = diagnostics.installedApkSha256,
            currentReleaseEvidence = diagnostics.currentReleaseEvidence,
            diagnosticsLoadState = diagnostics.loadState,
            //  实验性功能
            auto1080p = experimental.auto1080p,
            autoSkipOpEd = experimental.autoSkipOpEd,
            prefetchVideo = experimental.prefetchVideo,
            doubleTapLike = experimental.doubleTapLike,
            //  空降助手
            sponsorBlockEnabled = experimental.sponsorBlockEnabled,
            sponsorBlockAutoSkip = experimental.sponsorBlockAutoSkip
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        refreshCacheSize()
    }

    // --- 功能方法 ---

    //  优化：同时获取缓存大小和详细统计
    fun refreshCacheSize() {
        viewModelScope.launch { 
            val breakdown = CacheUtils.getCacheBreakdown(context)
            _cacheSize.value = breakdown.format()
            _cacheBreakdown.value = breakdown
        }
    }

    fun ensureDiagnosticsLoaded() {
        if (!shouldStartSettingsDiagnostics(
                loadState = _diagnosticsState.value.loadState,
                jobActive = diagnosticsLoadJob?.isActive == true,
            )
        ) {
            return
        }
        diagnosticsLoadJob = viewModelScope.launch {
            _diagnosticsState.update {
                it.copy(loadState = SettingsDiagnosticsLoadState.LOADING)
            }
            try {
                val (installedApkSha256, releaseEvidence) = coroutineScope {
                    val digest = async { calculateInstalledApkSha256(context) }
                    val release = async {
                        AppUpdateChecker
                            .check(
                                currentVersion = com.android.purebilibili.BuildConfig.VERSION_NAME,
                                currentVersionCode = com.android.purebilibili.BuildConfig.VERSION_CODE
                            )
                            .getOrNull()
                    }
                    digest.await() to release.await()
                }
                _diagnosticsState.value = DiagnosticsState(
                    installedApkSha256 = installedApkSha256,
                    currentReleaseEvidence = releaseEvidence,
                    loadState = SettingsDiagnosticsLoadState.LOADED,
                )
            } catch (error: CancellationException) {
                _diagnosticsState.update {
                    it.copy(loadState = SettingsDiagnosticsLoadState.NOT_LOADED)
                }
                throw error
            }
        }
    }

    fun recordReleaseEvidence(evidence: AppUpdateCheckResult) {
        _diagnosticsState.update { it.copy(currentReleaseEvidence = evidence) }
    }

    suspend fun clearCache(
        targets: Set<CacheClearTarget> = CacheClearTarget.entries.toSet()
    ): Result<CacheUtils.CacheBreakdown> {
        return CacheUtils.clearCache(context, targets).mapCatching {
            // 清理后立即刷新
            val breakdown = CacheUtils.getCacheBreakdown(context)
            _cacheSize.value = breakdown.format()
            _cacheBreakdown.value = breakdown
            breakdown
        }
    }

    fun toggleHwDecode(value: Boolean) { viewModelScope.launch { SettingsManager.setHwDecode(context, value) } }
    fun setThemeMode(mode: AppThemeMode) { 
        viewModelScope.launch { 
            SettingsManager.setThemeMode(context, mode)
        } 
    }
    fun setDarkThemeStyle(style: DarkThemeStyle) {
        viewModelScope.launch {
            SettingsManager.setDarkThemeStyle(context, style)
        }
    }
    fun setAppLanguage(appLanguage: AppLanguage) {
        viewModelScope.launch {
            SettingsManager.setAppLanguage(context, appLanguage)
        }
    }
    fun setAppListItemStyle(style: AppListItemStyle) {
        viewModelScope.launch {
            SettingsManager.setAppListItemStyle(context, style)
        }
    }
    fun setAppFontSizePreset(preset: AppFontSizePreset) {
        viewModelScope.launch { SettingsManager.setAppFontSizePreset(context, preset) }
    }
    fun setAppFontFile(fileName: String, displayName: String) {
        viewModelScope.launch { SettingsManager.setAppFontFile(context, fileName, displayName) }
    }
    fun clearAppFontFile() {
        viewModelScope.launch { SettingsManager.clearAppFontFile(context) }
    }
    fun setAppUiScalePreset(preset: AppUiScalePreset) {
        viewModelScope.launch { SettingsManager.setAppUiScalePreset(context, preset) }
    }
    fun setAppDpiOverridePercent(percent: Int) {
        viewModelScope.launch { SettingsManager.setAppDpiOverridePercent(context, percent) }
    }
    fun toggleBgPlay(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setMiniPlayerMode(
                context,
                if (value) SettingsManager.MiniPlayerMode.SYSTEM_PIP else SettingsManager.MiniPlayerMode.OFF
            )
        }
    }
    //  [新增] 手势灵敏度
    fun setGestureSensitivity(value: Float) { viewModelScope.launch { SettingsManager.setGestureSensitivity(context, value) } }

    //  [新增] 切换底栏样式
    fun toggleBottomBarFloating(value: Boolean) { viewModelScope.launch { SettingsManager.setBottomBarFloating(context, value) } }
    
    //  [新增] 底栏显示模式 (0=图标+文字, 1=仅图标, 2=仅文字)
    fun setBottomBarLabelMode(mode: Int) { viewModelScope.launch { SettingsManager.setBottomBarLabelMode(context, mode) } }
    


    fun toggleHeaderBlur(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setHeaderBlurEnabled(context, value)
        }
    }
    fun toggleHeaderCollapse(value: Boolean) { viewModelScope.launch { SettingsManager.setHeaderCollapseEnabled(context, value) } }
    fun toggleBottomBarBlur(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setBottomBarVisualEffects(
                context = context,
                blurEnabled = value
            )
        }
    }
    fun setBlurIntensity(intensity: BlurIntensity) { viewModelScope.launch { SettingsManager.setBlurIntensity(context, intensity) } }  //  模糊强度设置
    
    //  [新增] 卡片进场动画开关
    fun toggleCardAnimation(value: Boolean) { viewModelScope.launch { SettingsManager.setCardAnimationEnabled(context, value) } }
    
    //  [新增] 卡片过渡动画开关
    fun toggleCardTransition(value: Boolean) { viewModelScope.launch { SettingsManager.setCardTransitionEnabled(context, value) } }

    fun toggleLiveSurfaceCardTransition(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setLiveSurfaceCardTransitionEnabled(context, value)
        }
    }

    fun toggleVideoTransitionRealtimeBlur(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setVideoTransitionRealtimeBlurEnabled(context, value)
        }
    }

    // 阶段 4 设置页切片：全屏滑动返回写入收拢到 VM
    fun setFullScreenSwipeBackEnabled(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setFullScreenSwipeBackEnabled(context, value)
        }
    }

    fun setVideoSharedTransitionSpeed(speed: VideoSharedTransitionSpeed) {
        viewModelScope.launch {
            SettingsManager.setVideoSharedTransitionSpeed(context, speed)
        }
    }

    fun setVideoSharedTransitionCustomDurationMillis(durationMillis: Int) {
        viewModelScope.launch {
            SettingsManager.setVideoSharedTransitionCustomDurationMillis(context, durationMillis)
        }
    }

    fun toggleSmartVisualGuard(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setSmartVisualGuardEnabled(context, value)
        }
    }
    
    //  [新增] 首页展示模式
    fun setDisplayMode(mode: Int) { 
        viewModelScope.launch { 
            // 兼容旧的 shared preferences
            context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                .edit().putInt("display_mode", mode).apply()
            // 触发 flow 更新 (如果需要，或者仅仅依赖 prefs 监听? 这里简化处理，假设 ViewModel 只负责写，读在 flow 中)
            // 实际上这里的 flow 是基于 SettingsManager (DataStore) 的。
            // 如果 display_mode 还是 SharedPreferences，我们需要一个 flow 来通过 DataStore 或者手动构建。
            //为了简单统一，建议迁移到 SettingsManager。但为了不破坏 HomeScreen 读取，我们先保持 Prefs，
            // 并在 SettingsManager 中增加对 display_mode 的支持 (或者直接在这里用 MutableStateFlow 桥接?)
            // 鉴于 HomeScreen 可能直接读 Prefs，我们这里只需写 Prefs。
            // 但为了 UI 响应，我们需要通知 UIState。
            // 由于 SettingsManager 目前不管理 display_mode，我们需要添加它。
            // 既然要 refactor，就彻底点。
            SettingsManager.setDisplayMode(context, mode)
        } 
    }
    
    //  [新增] 实验性功能
    fun toggleAuto1080p(value: Boolean) { viewModelScope.launch { SettingsManager.setAuto1080p(context, value) } }
    fun toggleAutoSkipOpEd(value: Boolean) { viewModelScope.launch { SettingsManager.setAutoSkipOpEd(context, value) } }
    fun togglePrefetchVideo(value: Boolean) { viewModelScope.launch { SettingsManager.setPrefetchVideo(context, value) } }
    fun toggleDoubleTapLike(value: Boolean) { viewModelScope.launch { SettingsManager.setDoubleTapLike(context, value) } }
    
    //  [新增] 空降助手
    fun toggleSponsorBlock(value: Boolean) { viewModelScope.launch { SettingsManager.setSponsorBlockEnabled(context, value) } }
    fun toggleSponsorBlockAutoSkip(value: Boolean) { viewModelScope.launch { SettingsManager.setSponsorBlockAutoSkip(context, value) } }
    
    // [New] Splash Screen
    // 阶段 4 切片：外观页写入收拢
    fun setSingleChoicePresentation(presentation: AppSingleChoicePresentation) { viewModelScope.launch { SettingsManager.setSingleChoicePresentation(context, presentation) } }
    // 阶段 4 切片：外观页写入收拢
    fun setCommonListHeaderCollapseMode(mode: CommonListHeaderCollapseMode) { viewModelScope.launch { SettingsManager.setCommonListHeaderCollapseMode(context, mode) } }
    fun setCompactVideoStatsOnCover(value: Boolean) { viewModelScope.launch { SettingsManager.setCompactVideoStatsOnCover(context, value) } }
    fun setHomeHeroCarouselEnabled(value: Boolean) { viewModelScope.launch { SettingsManager.setHomeHeroCarouselEnabled(context, value) } }
    fun setHomeHeroCarouselAutoplayEnabled(value: Boolean) { viewModelScope.launch { SettingsManager.setHomeHeroCarouselAutoplayEnabled(context, value) } }
    fun setHomeFeedCardStyle(style: HomeFeedCardStyle) { viewModelScope.launch { SettingsManager.setHomeFeedCardStyle(context, style) } }
    fun setHomeDurationStyle(style: HomeDurationStyle) { viewModelScope.launch { SettingsManager.setHomeDurationStyle(context, style) } }
    fun setHomeWallpaperEffectMode(mode: HomeWallpaperEffectMode) { viewModelScope.launch { SettingsManager.setHomeWallpaperEffectMode(context, mode) } }
    fun setHomeWallpaperEffectScope(scope: HomeWallpaperEffectScope) { viewModelScope.launch { SettingsManager.setHomeWallpaperEffectScope(context, scope) } }
    fun setHomeUpBadgesVisible(value: Boolean) { viewModelScope.launch { SettingsManager.setHomeUpBadgesVisible(context, value) } }
    fun setHomeUpAvatarsVisible(value: Boolean) { viewModelScope.launch { SettingsManager.setHomeUpAvatarsVisible(context, value) } }
    fun setShowOnlineCount(value: Boolean) { viewModelScope.launch { SettingsManager.setShowOnlineCount(context, value) } }

    // 阶段 4 切片：底栏页显示模式选项（避开 UI 层直接引用 SettingsManager 嵌套枚举）
    val bottomBarVisibilityModeOptions: List<SettingsManager.BottomBarVisibilityMode> =
        SettingsManager.BottomBarVisibilityMode.entries

    // 阶段 4 切片：底栏页写入收拢
    fun setBottomBarOrder(order: List<String>) { viewModelScope.launch { SettingsManager.setBottomBarOrder(context, order) } }
    fun setBottomBarVisibleTabs(tabs: Set<String>) { viewModelScope.launch { SettingsManager.setBottomBarVisibleTabs(context, tabs) } }
    fun setTopTabOrder(order: List<String>) { viewModelScope.launch { SettingsManager.setTopTabOrder(context, order) } }
    fun setTopTabVisibleTabs(tabs: Set<String>) { viewModelScope.launch { SettingsManager.setTopTabVisibleTabs(context, tabs) } }
    fun setBottomBarItemColor(itemId: String, colorIndex: Int) { viewModelScope.launch { SettingsManager.setBottomBarItemColor(context, itemId, colorIndex) } }
    fun setTopTabLabelMode(mode: Int) { viewModelScope.launch { SettingsManager.setTopTabLabelMode(context, mode) } }
    fun setHomeTopRightAction(action: HomeTopRightAction) { viewModelScope.launch { SettingsManager.setHomeTopRightAction(context, action) } }
    fun setHomeHeaderBlurMode(mode: HomeHeaderBlurMode) { viewModelScope.launch { SettingsManager.setHomeHeaderBlurMode(context, mode) } }
    fun setHomeTopLayoutOrder(order: HomeTopLayoutOrder) { viewModelScope.launch { SettingsManager.setHomeTopLayoutOrder(context, order) } }
    fun setHomeHeaderCollapseMode(mode: HomeHeaderCollapseMode) { viewModelScope.launch { SettingsManager.setHomeHeaderCollapseMode(context, mode) } }
    fun setBottomBarVisibilityMode(mode: SettingsManager.BottomBarVisibilityMode) { viewModelScope.launch { SettingsManager.setBottomBarVisibilityMode(context, mode) } }
    fun setTabletUseSidebar(value: Boolean) { viewModelScope.launch { SettingsManager.setTabletUseSidebar(context, value) } }
    fun setSidebarAccountSwitcherEnabled(value: Boolean) { viewModelScope.launch { SettingsManager.setSidebarAccountSwitcherEnabled(context, value) } }

    fun toggleSplashEnabled(value: Boolean) { viewModelScope.launch { SettingsManager.setSplashEnabled(context, value) } }
    fun toggleSplashRandomEnabled(value: Boolean) { viewModelScope.launch { SettingsManager.setSplashRandomEnabled(context, value) } }
    fun toggleSplashIconAnimationEnabled(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setSplashIconAnimationEnabled(context, value)
        }
    }

    // [New] 触感反馈
    fun toggleHapticFeedback(value: Boolean) { viewModelScope.launch { SettingsManager.setHapticFeedbackEnabled(context, value) } }
    
    fun toggleBottomBarSearch(enabled: Boolean) {
        viewModelScope.launch {
            SettingsManager.setBottomBarSearchEnabled(context, enabled)
        }
    }

    fun setBottomBarSearchAutoExpandMode(mode: BottomBarSearchAutoExpandMode) {
        viewModelScope.launch {
            SettingsManager.setBottomBarSearchAutoExpandMode(context, mode)
        }
    }

    fun setBottomBarSearchLayoutMode(mode: BottomBarSearchLayoutMode) {
        viewModelScope.launch {
            SettingsManager.setBottomBarSearchLayoutMode(context, mode)
        }
    }

    // [New] 平板导航模式
    fun toggleTabletUseSidebar(value: Boolean) {
        viewModelScope.launch {
            SettingsManager.setTabletUseSidebar(context, value)
        }
    }

    // [New] 网格列数
    fun setGridColumnCount(count: Int) {
        viewModelScope.launch {
            SettingsManager.setGridColumnCount(context, count)
        }
    }

    fun setHomeFeedCardWidthPreset(preset: HomeFeedCardWidthPreset) {
        viewModelScope.launch {
            SettingsManager.setHomeFeedCardWidthPreset(context, preset)
        }
    }

    // 阶段 4 切片：播放设置页选项（避开 UI 层直接引用 SettingsManager 嵌套枚举）
    val miniPlayerModeOptions: List<SettingsManager.MiniPlayerMode> =
        SettingsManager.MiniPlayerMode.entries
    val dataSaverModeOptions: List<SettingsManager.DataSaverMode> =
        SettingsManager.DataSaverMode.entries
    val playerInsightModeOptions: List<PlayerSettingsStore.PlayerInsightMode> =
        PlayerSettingsStore.PlayerInsightMode.entries

    val playerInsightModeLabels: Map<PlayerSettingsStore.PlayerInsightMode, String> = mapOf(
        PlayerSettingsStore.PlayerInsightMode.OFF to "关闭",
        PlayerSettingsStore.PlayerInsightMode.SMART to "智能显示",
        PlayerSettingsStore.PlayerInsightMode.ALWAYS to "始终显示",
    )

    val playerInsightModeSubtitle: Map<PlayerSettingsStore.PlayerInsightMode, String> = mapOf(
        PlayerSettingsStore.PlayerInsightMode.OFF to "不显示播放状态信息",
        PlayerSettingsStore.PlayerInsightMode.SMART to "打开控制栏时显示；发生掉帧或软件解码时保持可见",
        PlayerSettingsStore.PlayerInsightMode.ALWAYS to "始终显示编码、码率、掉帧等播放信息",
    )

    // 阶段 4 切片：播放设置页写入收拢
    fun setMiniPlayerMode(mode: SettingsManager.MiniPlayerMode) {
        viewModelScope.launch { SettingsManager.setMiniPlayerMode(context, mode) }
    }
    fun setStopPlaybackOnExit(value: Boolean) {
        viewModelScope.launch { SettingsManager.setStopPlaybackOnExit(context, value) }
    }
    fun setBackgroundPlaybackEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setBackgroundPlaybackEnabled(context, value) }
    }
    fun setAudioFocusEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAudioFocusEnabled(context, value) }
    }
    fun setPipNoDanmakuEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPipNoDanmakuEnabled(context, value) }
    }
    fun setPlayerInsightMode(mode: PlayerSettingsStore.PlayerInsightMode) {
        viewModelScope.launch { SettingsManager.setPlayerInsightMode(context, mode) }
    }
    fun setPlayerDiagnosticLoggingEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPlayerDiagnosticLoggingEnabled(context, value) }
    }
    fun setDashSegmentRequestsEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDashSegmentRequestsEnabled(context, value) }
    }
    fun setQualitySwitchFailureDialogEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setQualitySwitchFailureDialogEnabled(context, value) }
    }
    fun setQualitySwitchFailureDialogOnceEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setQualitySwitchFailureDialogOnceEnabled(context, value) }
    }
    fun setVideoCodec(codec: String) {
        viewModelScope.launch { SettingsManager.setVideoCodec(context, codec) }
    }
    fun setVideoSecondCodec(codec: String) {
        viewModelScope.launch { SettingsManager.setVideoSecondCodec(context, codec) }
    }
    fun setRememberLastPlaybackSpeed(value: Boolean) {
        viewModelScope.launch { SettingsManager.setRememberLastPlaybackSpeed(context, value) }
    }
    fun setLongPressSpeedHintHidden(value: Boolean) {
        viewModelScope.launch { SettingsManager.setLongPressSpeedHintHidden(context, value) }
    }
    fun setLongPressSpeedHintScale(value: Float) {
        viewModelScope.launch { SettingsManager.setLongPressSpeedHintScale(context, value) }
    }
    fun setLongPressSpeedHintAlpha(value: Float) {
        viewModelScope.launch { SettingsManager.setLongPressSpeedHintAlpha(context, value) }
    }
    fun setDefaultPlaybackSpeed(value: Float) {
        viewModelScope.launch { SettingsManager.setDefaultPlaybackSpeed(context, value) }
    }
    fun setBiliDirectedTrafficEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setBiliDirectedTrafficEnabled(context, value) }
    }
    fun setAutoHighestQuality(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAutoHighestQuality(context, value) }
    }
    fun setWifiQuality(value: Int) {
        viewModelScope.launch { SettingsManager.setWifiQuality(context, value) }
    }
    fun setMobileQuality(value: Int) {
        viewModelScope.launch { SettingsManager.setMobileQuality(context, value) }
    }
    fun setDefaultAudioQuality(value: Int) {
        viewModelScope.launch { PlayerSettingsStore.setDefaultAudioQuality(context, value) }
    }
    fun setDataSaverMode(mode: SettingsManager.DataSaverMode) {
        viewModelScope.launch { SettingsManager.setDataSaverMode(context, mode) }
    }
    fun setLowQualityHomeCoverInDataSaver(value: Boolean) {
        viewModelScope.launch { SettingsManager.setLowQualityHomeCoverInDataSaver(context, value) }
    }
    fun setAutoPlay(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAutoPlay(context, value) }
    }
    fun setExternalPlaylistAutoContinue(value: Boolean) {
        viewModelScope.launch { SettingsManager.setExternalPlaylistAutoContinue(context, value) }
    }
    fun setResumePlaybackPromptEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setResumePlaybackPromptEnabled(context, value) }
    }
    fun setSpacePlayedVideoLocatePromptEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setSpacePlayedVideoLocatePromptEnabled(context, value) }
    }
    fun setPlaybackCompletionBehavior(value: PlaybackCompletionBehavior) {
        viewModelScope.launch { SettingsManager.setPlaybackCompletionBehavior(context, value) }
    }
    fun setSubtitleAutoPreference(value: SubtitleAutoPreference) {
        viewModelScope.launch { SettingsManager.setSubtitleAutoPreference(context, value) }
    }
    fun setVideoAiSummaryEntryEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setVideoAiSummaryEntryEnabled(context, value) }
    }
    fun setVideoNoteEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setVideoNoteEnabled(context, value) }
    }
    fun setVideoNoteDefaultCollapsed(value: Boolean) {
        viewModelScope.launch { SettingsManager.setVideoNoteDefaultCollapsed(context, value) }
    }
    fun setVideoInfoDefaultExpanded(value: Boolean) {
        viewModelScope.launch { SettingsManager.setVideoInfoDefaultExpanded(context, value) }
    }
    fun setCommentFraudDetectionEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setCommentFraudDetectionEnabled(context, value) }
    }
    fun setCommentMemberDecorationsEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setCommentMemberDecorationsEnabled(context, value) }
    }
    fun setImagePreviewLongPressSaveEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setImagePreviewLongPressSaveEnabled(context, value) }
    }
    fun setCommentCollapsedReplyPreviewLimit(value: Int) {
        viewModelScope.launch { SettingsManager.setCommentCollapsedReplyPreviewLimit(context, value) }
    }
    fun setClickToPlay(value: Boolean) {
        viewModelScope.launch { SettingsManager.setClickToPlay(context, value) }
    }
    fun setPortraitPlayerCollapseMode(value: PortraitPlayerCollapseMode) {
        viewModelScope.launch { SettingsManager.setPortraitPlayerCollapseMode(context, value) }
    }
    fun setPortraitSwipeToFullscreenEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPortraitSwipeToFullscreenEnabled(context, value) }
    }
    fun setCenterSwipeToFullscreenEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setCenterSwipeToFullscreenEnabled(context, value) }
    }
    fun setSlideVolumeBrightnessEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setSlideVolumeBrightnessEnabled(context, value) }
    }
    fun setSetSystemBrightnessEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setSetSystemBrightnessEnabled(context, value) }
    }
    fun setInlineSwipeSeekSeconds(value: Int) {
        viewModelScope.launch { SettingsManager.setInlineSwipeSeekSeconds(context, value) }
    }
    fun setFullscreenSwipeSeekEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setFullscreenSwipeSeekEnabled(context, value) }
    }
    fun setFullscreenSwipeSeekSeconds(value: Int) {
        viewModelScope.launch { SettingsManager.setFullscreenSwipeSeekSeconds(context, value) }
    }
    fun setDoubleTapSeekEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDoubleTapSeekEnabled(context, value) }
    }
    fun setSeekForwardSeconds(value: Int) {
        viewModelScope.launch { SettingsManager.setSeekForwardSeconds(context, value) }
    }
    fun setSeekBackwardSeconds(value: Int) {
        viewModelScope.launch { SettingsManager.setSeekBackwardSeconds(context, value) }
    }
    fun setDanmakuHideInteractiveCommands(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDanmakuHideInteractiveCommands(context, value) }
    }
    fun setDanmakuCloudSyncEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDanmakuCloudSyncEnabled(context, value) }
    }
    fun setPauseOnPlayerCollapseEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPauseOnPlayerCollapseEnabled(context, value) }
    }
    fun setAutoRotateEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAutoRotateEnabled(context, value) }
    }
    fun setFullscreenGestureReverse(value: Boolean) {
        viewModelScope.launch { SettingsManager.setFullscreenGestureReverse(context, value) }
    }
    fun setAutoEnterFullscreen(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAutoEnterFullscreen(context, value) }
    }
    fun setAutoExitFullscreenMode(value: AutoExitFullscreenMode) {
        viewModelScope.launch { SettingsManager.setAutoExitFullscreenMode(context, value) }
    }
    fun setShowFullscreenLockButton(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowFullscreenLockButton(context, value) }
    }
    fun setShowFullscreenScreenshotButton(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowFullscreenScreenshotButton(context, value) }
    }
    fun setAppGestureScreenshotEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAppGestureScreenshotEnabled(context, value) }
    }
    fun setAppScreenshotGestureMode(value: AppScreenshotGestureMode) {
        viewModelScope.launch { SettingsManager.setAppScreenshotGestureMode(context, value) }
    }
    fun setAppScreenshotCaptureMode(value: AppScreenshotCaptureMode) {
        viewModelScope.launch { SettingsManager.setAppScreenshotCaptureMode(context, value) }
    }
    fun setShowFullscreenBatteryLevel(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowFullscreenBatteryLevel(context, value) }
    }
    fun setShowFullscreenTime(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowFullscreenTime(context, value) }
    }
    fun setShowFullscreenActionItems(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowFullscreenActionItems(context, value) }
    }
    fun setShowPlayerCastButton(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowPlayerCastButton(context, value) }
    }
    fun setShowVideoFollowButton(value: Boolean) {
        viewModelScope.launch { SettingsManager.setShowVideoFollowButton(context, value) }
    }
    fun setBottomProgressBehavior(value: BottomProgressBehavior) {
        viewModelScope.launch { SettingsManager.setBottomProgressBehavior(context, value) }
    }
    fun setProgressPeakDanmakuEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setProgressPeakDanmakuEnabled(context, value) }
    }
    fun setPlayerProgressPlacement(value: PlayerProgressPlacement) {
        viewModelScope.launch { SettingsManager.setPlayerProgressPlacement(context, value) }
    }
    fun setHorizontalAdaptationEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setHorizontalAdaptationEnabled(context, value) }
    }
    fun setHideVideoPageStatusBar(value: Boolean) {
        viewModelScope.launch { SettingsManager.setHideVideoPageStatusBar(context, value) }
    }
    fun setTabletCommentPanelWidthPreset(value: TabletCommentPanelWidthPreset) {
        viewModelScope.launch { SettingsManager.setTabletCommentPanelWidthPreset(context, value) }
    }
    fun setFullscreenMode(value: FullscreenMode) {
        viewModelScope.launch { SettingsManager.setFullscreenMode(context, value) }
    }
    fun setFullscreenAspectRatio(value: FullscreenAspectRatio) {
        viewModelScope.launch { SettingsManager.setFullscreenAspectRatio(context, value) }
    }
    fun setPortraitLetterboxAmbientHaze(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPortraitLetterboxAmbientHaze(context, value) }
    }

    // 阶段 4 切片：设置根页写入收拢
    fun setPrivacyModeEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPrivacyModeEnabled(context, value) }
    }
    fun setPrivacyContentAuthenticationEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setPrivacyContentAuthenticationEnabled(context, value) }
    }
    fun setCrashTrackingEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setCrashTrackingEnabled(context, value) }
    }
    fun setAnalyticsEnabled(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAnalyticsEnabled(context, value) }
    }
    fun setDownloadExportTreeUri(value: String) {
        viewModelScope.launch {
            SettingsManager.setDownloadExportTreeUri(context, value)
            SettingsManager.setDownloadPath(context, null)
        }
    }
    fun clearDownloadExportTreeUri() {
        viewModelScope.launch {
            SettingsManager.setDownloadPath(context, null)
            SettingsManager.setDownloadExportTreeUri(context, null)
        }
    }
    fun setImageSaveTreeUri(value: String) {
        viewModelScope.launch { SettingsManager.setImageSaveTreeUri(context, value) }
    }
    fun clearImageSaveTreeUri() {
        viewModelScope.launch { SettingsManager.setImageSaveTreeUri(context, null) }
    }
    fun setAutoCheckAppUpdate(value: Boolean) {
        viewModelScope.launch { SettingsManager.setAutoCheckAppUpdate(context, value) }
    }
    fun setAppUpdateChannel(value: SettingsAppUpdateChannel) {
        viewModelScope.launch { SettingsManager.setAppUpdateChannel(context, value) }
    }
    fun setFeedApiType(value: SettingsFeedApiType) {
        viewModelScope.launch { SettingsManager.setFeedApiType(context, value) }
    }
    fun setIncrementalTimelineRefresh(value: Boolean) {
        viewModelScope.launch { SettingsManager.setIncrementalTimelineRefresh(context, value) }
    }
    fun setDynamicImagePreviewTextVisible(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDynamicImagePreviewTextVisible(context, value) }
    }
    fun setDynamicAllTabHorizontalUserListVisible(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDynamicAllTabHorizontalUserListVisible(context, value) }
    }
    fun setDynamicTopBarCollapseOnScroll(value: Boolean) {
        viewModelScope.launch { SettingsManager.setDynamicTopBarCollapseOnScroll(context, value) }
    }
    fun setDynamicFeedLayoutMode(value: SettingsDynamicFeedLayoutMode) {
        viewModelScope.launch { SettingsManager.setDynamicFeedLayoutMode(context, value) }
    }
    fun setDynamicTabVisibleTabs(value: Set<String>) {
        viewModelScope.launch { SettingsManager.setDynamicTabVisibleTabs(context, value) }
    }
    fun setHomeRefreshCount(value: Int) {
        viewModelScope.launch { SettingsManager.setHomeRefreshCount(context, value) }
    }

}

internal class SettingsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// Move DisplayMode enum here to be accessible
enum class DisplayMode(val title: String, val description: String, val value: Int) {
    DoubleGrid(title = "双列网格", description = "经典双列瀑布流布局", value = 0),
    SingleColumn(title = "单列视频", description = "类似信息流的单列布局", value = 1)
}
