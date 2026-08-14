package com.android.purebilibili.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import com.android.purebilibili.core.ui.rememberAppSemanticVisualPolicy
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*

internal enum class SettingsIconRole {
    INTERFACE_THEME,
    HOME_FEED,
    NAVIGATION,
    PLAYBACK_QUALITY,
    FULLSCREEN_GESTURE,
    INTERACTION_COMMENT,
    DATA_BACKUP,
    PRIVACY_PERMISSION,
    DIAGNOSTICS,
    ABOUT_SUPPORT,
    APPEARANCE,
    ANIMATION,
    PLAYBACK,
    BOTTOM_BAR,
    PERMISSION,
    BLOCKED_LIST,
    SETTINGS_SHARE,
    WEBDAV_BACKUP,
    DOWNLOAD_PATH,
    IMAGE_SAVE_PATH,
    CLEAR_CACHE,
    PLUGINS,
    EXPORT_LOGS,
    OPEN_SOURCE_LICENSES,
    OPEN_SOURCE_HOME,
    CHECK_UPDATE,
    VIEW_RELEASE_NOTES,
    REPLAY_ONBOARDING,
    OPEN_LINKS,
    DONATE,
    DISCLAIMER,
    RELEASE_CHANNEL,
    CRASH_TRACKING,
    ANALYTICS,
    FEED_API,
    REFRESH_COUNT,
    DYNAMIC_PREVIEW_TEXT,
    DYNAMIC_TAB_VISIBILITY,
    EASTER_EGG,
    AUTO_CHECK_UPDATE,
    BUILD_SOURCE,
    BUILD_FINGERPRINT,
    BUILD_VERIFICATION,
    ANDROID_LIQUID_GLASS,
    DYNAMIC_COLOR,
    THEME_COLOR_PICKER,
    COLOR_STYLE,
    COLOR_SPEC,
    APP_LANGUAGE,
    FONT_FILE,
    SPLASH_WALLPAPER,
    RANDOM_WALLPAPER,
    DISPLAY_STYLE,
    HOME_COVER_GLASS,
    VIDEO_DURATION_BADGES,
    HOME_INFO_GLASS,
    HOME_WALLPAPER,
    WALLPAPER_EFFECT,
    HOME_UP_BADGES,
    HOME_UP_AVATAR,
    ONLINE_COUNT,
    GRID_COLUMNS,
    HOME_CARD_WIDTH,
    CARD_ENTRANCE_ANIMATION,
    CARD_TRANSITION_ANIMATION,
    LIVE_SURFACE_TRANSITION,
    PREDICTIVE_BACK,
    MIUIX_TRANSITION_BLUR,
    TOP_DOCK_GLASS,
    HOME_SEARCH_GLASS,
    BOTTOM_BAR_GLASS,
    TOP_BAR_BLUR,
    HEADER_COLLAPSE,
    BOTTOM_BAR_BLUR,
    FLOATING_BOTTOM_BAR,
    HARDWARE_DECODER,
    PLAYBACK_SPEED,
    LONG_PRESS_SPEED_HINT,
    RESUME_PLAYBACK_PROMPT,
    STOP_ON_EXIT,
    BACKGROUND_PLAYBACK,
    PLAYLIST_AUTO_CONTINUE,
    AUDIO_FOCUS,
    SLIDE_VOLUME_BRIGHTNESS,
    PIP_DANMAKU,
    DANMAKU_CLOUD_SYNC,
    PLAYER_DIAGNOSTICS,
    QUALITY_WARNING,
    SUBTITLE,
    COMMENT_DECORATION,
    AI_SUMMARY,
    VIDEO_NOTE,
    LIKE_INTERACTION,
    VIDEO_DESCRIPTION,
    FULLSCREEN_ORIENTATION,
    HORIZONTAL_ADAPTATION,
    FULLSCREEN_GESTURE_REVERSE,
    IMMERSIVE_STATUS_BAR,
    AUTO_ENTER_FULLSCREEN,
    AUTO_EXIT_FULLSCREEN,
    FULLSCREEN_LOCK,
    FULLSCREEN_SCREENSHOT,
    CLEAN_SCREENSHOT,
    BATTERY_STATUS,
    TIME_STATUS,
    PLAYER_ACTIONS,
    PRIVACY_CONTENT_AUTHENTICATION,
    PLAYER_STATS,
    PLAYER_DIAGNOSTIC_LOGS,
    QUALITY_WARNING_ONCE,
    DIRECTED_TRAFFIC,
    AUTO_HIGHEST_QUALITY,
    AUTO_PLAY_ON_OPEN,
    HOME_HERO_AUTOPLAY,
    AUTO_PLAY_NEXT,
    VIDEO_NOTE_COLLAPSE,
    INTERACTIVE_COMMANDS,
    PORTRAIT_SWIPE_FULLSCREEN,
    CENTER_SWIPE_FULLSCREEN,
    SYSTEM_BRIGHTNESS,
    APP_ICON,
    HOME_CARD_STATS_COMPACT,
    HOME_HERO_CAROUSEL,
    HOME_ONLINE_COUNT,
    DISPLAY_SCALE,
    UI_ENTRANCE_ANIMATION,
    FULLSCREEN_SWIPE_BACK,
    FOLLOW_BUTTON,
    PRIVACY_HISTORY,
    CUSTOM_MD3_COLOR,
    THEME_LIGHT_BACKGROUND,
    THEME_LIGHT_PRIMARY_TEXT,
    THEME_LIGHT_SECONDARY_TEXT,
    THEME_LIGHT_CONTROL,
    THEME_DARK_BACKGROUND,
    THEME_DARK_PRIMARY_TEXT,
    THEME_DARK_SECONDARY_TEXT,
    THEME_DARK_CONTROL,
    DEVELOPER_CRASH_TRACKING,
    DEVELOPER_ANALYTICS,
    APP_VERSION,
    BOTTOM_BAR_GLASS_PREVIEW,
    ADVANCED_COLOR,
    CAST_BUTTON,
    PROGRESS_PEAK_DANMAKU
}

@Composable
internal fun rememberSettingsSemanticIcon(
    role: SettingsIconRole,
): ImageVector {
    val iconFamily = rememberAppSemanticVisualPolicy().effectiveIconFamily
    return remember(role, iconFamily) {
        resolveSettingsSemanticIcon(role, iconFamily)
    }
}

internal fun resolveSettingsSearchTargetIconRole(
    target: SettingsSearchTarget
): SettingsIconRole = when (target) {
    SettingsSearchTarget.INTERFACE_THEME -> SettingsIconRole.INTERFACE_THEME
    SettingsSearchTarget.HOME_FEED -> SettingsIconRole.HOME_FEED
    SettingsSearchTarget.NAVIGATION -> SettingsIconRole.NAVIGATION
    SettingsSearchTarget.PLAYBACK_QUALITY -> SettingsIconRole.PLAYBACK_QUALITY
    SettingsSearchTarget.FULLSCREEN_GESTURE -> SettingsIconRole.FULLSCREEN_GESTURE
    SettingsSearchTarget.INTERACTION_COMMENT -> SettingsIconRole.INTERACTION_COMMENT
    SettingsSearchTarget.DATA_BACKUP -> SettingsIconRole.DATA_BACKUP
    SettingsSearchTarget.PRIVACY_PERMISSION -> SettingsIconRole.PRIVACY_PERMISSION
    SettingsSearchTarget.DIAGNOSTICS -> SettingsIconRole.DIAGNOSTICS
    SettingsSearchTarget.ABOUT_SUPPORT -> SettingsIconRole.ABOUT_SUPPORT
    SettingsSearchTarget.APPEARANCE -> SettingsIconRole.APPEARANCE
    SettingsSearchTarget.ANIMATION -> SettingsIconRole.ANIMATION
    SettingsSearchTarget.PLAYBACK -> SettingsIconRole.PLAYBACK
    SettingsSearchTarget.BOTTOM_BAR -> SettingsIconRole.BOTTOM_BAR
    SettingsSearchTarget.PERMISSION -> SettingsIconRole.PERMISSION
    SettingsSearchTarget.BLOCKED_LIST -> SettingsIconRole.BLOCKED_LIST
    SettingsSearchTarget.SETTINGS_SHARE -> SettingsIconRole.SETTINGS_SHARE
    SettingsSearchTarget.WEBDAV_BACKUP -> SettingsIconRole.WEBDAV_BACKUP
    SettingsSearchTarget.DOWNLOAD_PATH -> SettingsIconRole.DOWNLOAD_PATH
    SettingsSearchTarget.IMAGE_SAVE_PATH -> SettingsIconRole.IMAGE_SAVE_PATH
    SettingsSearchTarget.CLEAR_CACHE -> SettingsIconRole.CLEAR_CACHE
    SettingsSearchTarget.PLUGINS -> SettingsIconRole.PLUGINS
    SettingsSearchTarget.EXPORT_LOGS -> SettingsIconRole.EXPORT_LOGS
    SettingsSearchTarget.OPEN_SOURCE_LICENSES -> SettingsIconRole.OPEN_SOURCE_LICENSES
    SettingsSearchTarget.OPEN_SOURCE_HOME -> SettingsIconRole.OPEN_SOURCE_HOME
    SettingsSearchTarget.CHECK_UPDATE -> SettingsIconRole.CHECK_UPDATE
    SettingsSearchTarget.VIEW_RELEASE_NOTES -> SettingsIconRole.VIEW_RELEASE_NOTES
    SettingsSearchTarget.REPLAY_ONBOARDING -> SettingsIconRole.REPLAY_ONBOARDING
    SettingsSearchTarget.OPEN_LINKS -> SettingsIconRole.OPEN_LINKS
    SettingsSearchTarget.DONATE -> SettingsIconRole.DONATE
    SettingsSearchTarget.TELEGRAM -> SettingsIconRole.OPEN_LINKS
    SettingsSearchTarget.TWITTER -> SettingsIconRole.OPEN_LINKS
    SettingsSearchTarget.DISCLAIMER -> SettingsIconRole.DISCLAIMER
}

internal fun resolveSettingsSemanticIcon(
    role: SettingsIconRole,
    iconFamily: AppSemanticIconFamily = AppSemanticIconFamily.MATERIAL,
): ImageVector = when (iconFamily) {
    AppSemanticIconFamily.MATERIAL -> resolveMd3SettingsSemanticIcon(role)
    AppSemanticIconFamily.MIUIX -> resolveMiuixSettingsSemanticIcon(role)
}

/**
 * Miuix 设置页优先使用 miuix-icons 自带字形。这里按设置语义集中映射，避免各页面
 * 自行混入 Material 图标；远隔层级允许复用，同一个可见设置组仍由角色保持区分。
 */
private fun resolveMiuixSettingsSemanticIcon(role: SettingsIconRole): ImageVector = when (role) {
    SettingsIconRole.INTERFACE_THEME -> MiuixIcons.Theme
    SettingsIconRole.HOME_FEED -> MiuixIcons.Th1
    SettingsIconRole.NAVIGATION -> MiuixIcons.Sidebar
    SettingsIconRole.PLAYBACK_QUALITY -> MiuixIcons.Play
    SettingsIconRole.FULLSCREEN_GESTURE -> MiuixIcons.ScreenCapture
    SettingsIconRole.INTERACTION_COMMENT -> MiuixIcons.Messages
    SettingsIconRole.DATA_BACKUP -> MiuixIcons.Backup
    SettingsIconRole.PRIVACY_PERMISSION -> MiuixIcons.Lock
    SettingsIconRole.DIAGNOSTICS -> MiuixIcons.AppRecording
    SettingsIconRole.ABOUT_SUPPORT -> MiuixIcons.Info
    SettingsIconRole.APPEARANCE -> MiuixIcons.Image
    SettingsIconRole.ANIMATION -> MiuixIcons.RecordingTape
    SettingsIconRole.PLAYBACK -> MiuixIcons.Play
    SettingsIconRole.BOTTOM_BAR -> MiuixIcons.HorizontalSplit
    SettingsIconRole.PERMISSION -> MiuixIcons.Unlock
    SettingsIconRole.BLOCKED_LIST -> MiuixIcons.Blocklist
    SettingsIconRole.SETTINGS_SHARE -> MiuixIcons.Share
    SettingsIconRole.WEBDAV_BACKUP -> MiuixIcons.UploadCloud
    SettingsIconRole.DOWNLOAD_PATH -> MiuixIcons.Folder
    SettingsIconRole.IMAGE_SAVE_PATH -> MiuixIcons.Photos
    SettingsIconRole.CLEAR_CACHE -> MiuixIcons.Clear
    SettingsIconRole.PLUGINS -> MiuixIcons.AddFolder
    SettingsIconRole.EXPORT_LOGS -> MiuixIcons.File
    SettingsIconRole.OPEN_SOURCE_LICENSES -> MiuixIcons.Notes
    SettingsIconRole.OPEN_SOURCE_HOME -> MiuixIcons.Link
    SettingsIconRole.CHECK_UPDATE -> MiuixIcons.Update
    SettingsIconRole.VIEW_RELEASE_NOTES -> MiuixIcons.NotesFill
    SettingsIconRole.REPLAY_ONBOARDING -> MiuixIcons.Reset
    SettingsIconRole.OPEN_LINKS -> MiuixIcons.Link
    SettingsIconRole.DONATE -> MiuixIcons.BankCards
    SettingsIconRole.DISCLAIMER -> MiuixIcons.Report
    SettingsIconRole.RELEASE_CHANNEL -> MiuixIcons.Promotions
    SettingsIconRole.CRASH_TRACKING -> MiuixIcons.Report
    SettingsIconRole.ANALYTICS -> MiuixIcons.Th28
    SettingsIconRole.FEED_API -> MiuixIcons.All
    SettingsIconRole.REFRESH_COUNT -> MiuixIcons.Refresh
    SettingsIconRole.DYNAMIC_PREVIEW_TEXT -> MiuixIcons.Show
    SettingsIconRole.DYNAMIC_TAB_VISIBILITY -> MiuixIcons.SelectAll
    SettingsIconRole.EASTER_EGG -> MiuixIcons.Favorites
    SettingsIconRole.AUTO_CHECK_UPDATE -> MiuixIcons.Update
    SettingsIconRole.BUILD_SOURCE -> MiuixIcons.File
    SettingsIconRole.BUILD_FINGERPRINT -> MiuixIcons.Scan
    SettingsIconRole.BUILD_VERIFICATION -> MiuixIcons.Ok
    SettingsIconRole.ANDROID_LIQUID_GLASS -> MiuixIcons.Layers
    SettingsIconRole.DYNAMIC_COLOR -> MiuixIcons.Promotions
    SettingsIconRole.THEME_COLOR_PICKER -> MiuixIcons.Tune
    SettingsIconRole.COLOR_STYLE -> MiuixIcons.Edit
    SettingsIconRole.COLOR_SPEC -> MiuixIcons.Settings
    SettingsIconRole.APP_LANGUAGE -> MiuixIcons.Translate
    SettingsIconRole.FONT_FILE -> MiuixIcons.File
    SettingsIconRole.SPLASH_WALLPAPER -> MiuixIcons.Image
    SettingsIconRole.RANDOM_WALLPAPER -> MiuixIcons.Replace
    SettingsIconRole.DISPLAY_STYLE -> MiuixIcons.GridView
    SettingsIconRole.HOME_COVER_GLASS -> MiuixIcons.Background
    SettingsIconRole.VIDEO_DURATION_BADGES -> MiuixIcons.Timer
    SettingsIconRole.HOME_INFO_GLASS -> MiuixIcons.Info
    SettingsIconRole.HOME_WALLPAPER -> MiuixIcons.MapAlbum
    SettingsIconRole.WALLPAPER_EFFECT -> MiuixIcons.Layers
    SettingsIconRole.HOME_UP_BADGES -> MiuixIcons.Promotions
    SettingsIconRole.HOME_UP_AVATAR -> MiuixIcons.ContactsCircle
    SettingsIconRole.ONLINE_COUNT -> MiuixIcons.Contacts
    SettingsIconRole.GRID_COLUMNS -> MiuixIcons.GridView
    SettingsIconRole.HOME_CARD_WIDTH -> MiuixIcons.HorizontalSplit
    SettingsIconRole.CARD_ENTRANCE_ANIMATION -> MiuixIcons.Forward
    SettingsIconRole.CARD_TRANSITION_ANIMATION -> MiuixIcons.Replace
    SettingsIconRole.LIVE_SURFACE_TRANSITION -> MiuixIcons.ScreenMirroring
    SettingsIconRole.PREDICTIVE_BACK -> MiuixIcons.Back
    SettingsIconRole.MIUIX_TRANSITION_BLUR -> MiuixIcons.Layers
    SettingsIconRole.TOP_DOCK_GLASS -> MiuixIcons.Th1
    SettingsIconRole.HOME_SEARCH_GLASS -> MiuixIcons.Search
    SettingsIconRole.BOTTOM_BAR_GLASS -> MiuixIcons.Th2
    SettingsIconRole.TOP_BAR_BLUR -> MiuixIcons.Th3
    SettingsIconRole.HEADER_COLLAPSE -> MiuixIcons.ExpandLess
    SettingsIconRole.BOTTOM_BAR_BLUR -> MiuixIcons.Th4
    SettingsIconRole.FLOATING_BOTTOM_BAR -> MiuixIcons.Sidebar
    SettingsIconRole.HARDWARE_DECODER -> MiuixIcons.Carrier
    SettingsIconRole.PLAYBACK_SPEED -> MiuixIcons.Stopwatch
    SettingsIconRole.LONG_PRESS_SPEED_HINT -> MiuixIcons.Hide
    SettingsIconRole.RESUME_PLAYBACK_PROMPT -> MiuixIcons.Recent
    SettingsIconRole.STOP_ON_EXIT -> MiuixIcons.Pause
    SettingsIconRole.BACKGROUND_PLAYBACK -> MiuixIcons.Music
    SettingsIconRole.PLAYLIST_AUTO_CONTINUE -> MiuixIcons.Playlist
    SettingsIconRole.AUDIO_FOCUS -> MiuixIcons.VolumeUp
    SettingsIconRole.SLIDE_VOLUME_BRIGHTNESS -> MiuixIcons.VerticalSplit
    SettingsIconRole.PIP_DANMAKU -> MiuixIcons.Messages
    SettingsIconRole.DANMAKU_CLOUD_SYNC -> MiuixIcons.CloudFill
    SettingsIconRole.PLAYER_DIAGNOSTICS -> MiuixIcons.AppRecording
    SettingsIconRole.QUALITY_WARNING -> MiuixIcons.Report
    SettingsIconRole.SUBTITLE -> MiuixIcons.Notes
    SettingsIconRole.COMMENT_DECORATION -> MiuixIcons.Community
    SettingsIconRole.AI_SUMMARY -> MiuixIcons.MindMap
    SettingsIconRole.VIDEO_NOTE -> MiuixIcons.NotesFill
    SettingsIconRole.LIKE_INTERACTION -> MiuixIcons.Favorites
    SettingsIconRole.VIDEO_DESCRIPTION -> MiuixIcons.File
    SettingsIconRole.FULLSCREEN_ORIENTATION -> MiuixIcons.RotateLeft
    SettingsIconRole.HORIZONTAL_ADAPTATION -> MiuixIcons.HorizontalSplit
    SettingsIconRole.FULLSCREEN_GESTURE_REVERSE -> MiuixIcons.VerticalSplit
    SettingsIconRole.IMMERSIVE_STATUS_BAR -> MiuixIcons.ExpandMore
    SettingsIconRole.AUTO_ENTER_FULLSCREEN -> MiuixIcons.ZoomOut
    SettingsIconRole.AUTO_EXIT_FULLSCREEN -> MiuixIcons.Close2
    SettingsIconRole.FULLSCREEN_LOCK -> MiuixIcons.Lock
    SettingsIconRole.FULLSCREEN_SCREENSHOT -> MiuixIcons.ScreenCapture
    SettingsIconRole.CLEAN_SCREENSHOT -> MiuixIcons.Photos
    SettingsIconRole.BATTERY_STATUS -> MiuixIcons.Carrier
    SettingsIconRole.TIME_STATUS -> MiuixIcons.WorldClock
    SettingsIconRole.PLAYER_ACTIONS -> MiuixIcons.More
    SettingsIconRole.PRIVACY_CONTENT_AUTHENTICATION -> MiuixIcons.Unlock
    SettingsIconRole.PLAYER_STATS -> MiuixIcons.Th28
    SettingsIconRole.PLAYER_DIAGNOSTIC_LOGS -> MiuixIcons.Recording
    SettingsIconRole.QUALITY_WARNING_ONCE -> MiuixIcons.Alarm
    SettingsIconRole.DIRECTED_TRAFFIC -> MiuixIcons.SearchDevice
    SettingsIconRole.AUTO_HIGHEST_QUALITY -> MiuixIcons.TopDownloads
    SettingsIconRole.AUTO_PLAY_ON_OPEN -> MiuixIcons.Play
    SettingsIconRole.HOME_HERO_AUTOPLAY -> MiuixIcons.Recording
    SettingsIconRole.AUTO_PLAY_NEXT -> MiuixIcons.Playlist
    SettingsIconRole.VIDEO_NOTE_COLLAPSE -> MiuixIcons.ExpandLess
    SettingsIconRole.INTERACTIVE_COMMANDS -> MiuixIcons.Messages
    SettingsIconRole.PORTRAIT_SWIPE_FULLSCREEN -> MiuixIcons.VerticalSplit
    SettingsIconRole.CENTER_SWIPE_FULLSCREEN -> MiuixIcons.SelectAll
    SettingsIconRole.SYSTEM_BRIGHTNESS -> MiuixIcons.Theme
    SettingsIconRole.APP_ICON -> MiuixIcons.AppRecording
    SettingsIconRole.HOME_CARD_STATS_COMPACT -> MiuixIcons.Th29
    SettingsIconRole.HOME_HERO_CAROUSEL -> MiuixIcons.MapAlbum
    SettingsIconRole.HOME_ONLINE_COUNT -> MiuixIcons.Contacts
    SettingsIconRole.DISPLAY_SCALE -> MiuixIcons.ZoomOut
    SettingsIconRole.UI_ENTRANCE_ANIMATION -> MiuixIcons.Forward
    SettingsIconRole.FULLSCREEN_SWIPE_BACK -> MiuixIcons.Back
    SettingsIconRole.FOLLOW_BUTTON -> MiuixIcons.AddCircle
    SettingsIconRole.PRIVACY_HISTORY -> MiuixIcons.Recent
    SettingsIconRole.CUSTOM_MD3_COLOR -> MiuixIcons.Tune
    SettingsIconRole.THEME_LIGHT_BACKGROUND -> MiuixIcons.Background
    SettingsIconRole.THEME_LIGHT_PRIMARY_TEXT -> MiuixIcons.Show
    SettingsIconRole.THEME_LIGHT_SECONDARY_TEXT -> MiuixIcons.Notes
    SettingsIconRole.THEME_LIGHT_CONTROL -> MiuixIcons.Settings
    SettingsIconRole.THEME_DARK_BACKGROUND -> MiuixIcons.Hide
    SettingsIconRole.THEME_DARK_PRIMARY_TEXT -> MiuixIcons.File
    SettingsIconRole.THEME_DARK_SECONDARY_TEXT -> MiuixIcons.NotesFill
    SettingsIconRole.THEME_DARK_CONTROL -> MiuixIcons.Tune
    SettingsIconRole.DEVELOPER_CRASH_TRACKING -> MiuixIcons.Report
    SettingsIconRole.DEVELOPER_ANALYTICS -> MiuixIcons.Th30
    SettingsIconRole.APP_VERSION -> MiuixIcons.Info
    SettingsIconRole.BOTTOM_BAR_GLASS_PREVIEW -> MiuixIcons.Th2
    SettingsIconRole.ADVANCED_COLOR -> MiuixIcons.Theme
    SettingsIconRole.CAST_BUTTON -> MiuixIcons.ScreenMirroring
    SettingsIconRole.PROGRESS_PEAK_DANMAKU -> MiuixIcons.RecordingTape
}

private fun resolveMd3SettingsSemanticIcon(role: SettingsIconRole): ImageVector = when (role) {
    SettingsIconRole.INTERFACE_THEME -> Icons.Outlined.ColorLens
    SettingsIconRole.HOME_FEED -> Icons.Outlined.Home
    SettingsIconRole.NAVIGATION -> Icons.Outlined.Dashboard
    SettingsIconRole.PLAYBACK_QUALITY -> Icons.Outlined.HighQuality
    SettingsIconRole.FULLSCREEN_GESTURE -> Icons.Outlined.TouchApp
    SettingsIconRole.INTERACTION_COMMENT -> Icons.Outlined.ChatBubbleOutline
    SettingsIconRole.DATA_BACKUP -> Icons.Outlined.Backup
    SettingsIconRole.PRIVACY_PERMISSION -> Icons.Outlined.Lock
    SettingsIconRole.DIAGNOSTICS -> Icons.Outlined.Terminal
    SettingsIconRole.ABOUT_SUPPORT -> Icons.Outlined.Info
    SettingsIconRole.APPEARANCE -> Icons.Outlined.Palette
    SettingsIconRole.ANIMATION -> Icons.Outlined.Animation
    SettingsIconRole.PLAYBACK -> Icons.Outlined.PlayCircle
    SettingsIconRole.BOTTOM_BAR -> Icons.Outlined.Widgets
    SettingsIconRole.PERMISSION -> Icons.Outlined.Security
    SettingsIconRole.BLOCKED_LIST -> Icons.Outlined.Block
    SettingsIconRole.SETTINGS_SHARE -> Icons.Outlined.Share
    SettingsIconRole.WEBDAV_BACKUP -> Icons.Outlined.CloudUpload
    SettingsIconRole.DOWNLOAD_PATH -> Icons.Outlined.Folder
    SettingsIconRole.IMAGE_SAVE_PATH -> Icons.Outlined.Photo
    SettingsIconRole.CLEAR_CACHE -> Icons.Outlined.DeleteOutline
    SettingsIconRole.PLUGINS -> Icons.Outlined.Extension
    SettingsIconRole.EXPORT_LOGS -> Icons.AutoMirrored.Outlined.Article
    SettingsIconRole.OPEN_SOURCE_LICENSES -> Icons.Outlined.Gavel
    SettingsIconRole.OPEN_SOURCE_HOME -> Icons.AutoMirrored.Outlined.OpenInNew
    SettingsIconRole.CHECK_UPDATE -> Icons.Outlined.SystemUpdate
    SettingsIconRole.VIEW_RELEASE_NOTES -> Icons.Outlined.Newspaper
    SettingsIconRole.REPLAY_ONBOARDING -> Icons.Outlined.Replay
    SettingsIconRole.OPEN_LINKS -> Icons.Outlined.Link
    SettingsIconRole.DONATE -> Icons.Outlined.CardGiftcard
    SettingsIconRole.DISCLAIMER -> Icons.Outlined.WarningAmber
    SettingsIconRole.RELEASE_CHANNEL -> Icons.Outlined.Rocket
    SettingsIconRole.CRASH_TRACKING -> Icons.Outlined.BugReport
    SettingsIconRole.ANALYTICS -> Icons.Outlined.Analytics
    SettingsIconRole.FEED_API -> Icons.Outlined.RssFeed
    SettingsIconRole.REFRESH_COUNT -> Icons.Outlined.Refresh
    SettingsIconRole.DYNAMIC_PREVIEW_TEXT -> Icons.AutoMirrored.Outlined.TextSnippet
    SettingsIconRole.DYNAMIC_TAB_VISIBILITY -> Icons.Outlined.Visibility
    SettingsIconRole.EASTER_EGG -> Icons.Outlined.AutoAwesome
    SettingsIconRole.AUTO_CHECK_UPDATE -> Icons.Outlined.Update
    SettingsIconRole.BUILD_SOURCE -> Icons.Outlined.Tag
    SettingsIconRole.BUILD_FINGERPRINT -> Icons.Outlined.Fingerprint
    SettingsIconRole.BUILD_VERIFICATION -> Icons.Outlined.VerifiedUser
    SettingsIconRole.ANDROID_LIQUID_GLASS -> Icons.Outlined.WaterDrop
    SettingsIconRole.DYNAMIC_COLOR -> Icons.Outlined.FormatColorText
    SettingsIconRole.THEME_COLOR_PICKER -> Icons.Outlined.Colorize
    SettingsIconRole.COLOR_STYLE -> Icons.Outlined.Brush
    SettingsIconRole.COLOR_SPEC -> Icons.Outlined.AutoFixHigh
    SettingsIconRole.APP_LANGUAGE -> Icons.Outlined.Language
    SettingsIconRole.FONT_FILE -> Icons.Outlined.FontDownload
    SettingsIconRole.SPLASH_WALLPAPER -> Icons.Outlined.Wallpaper
    SettingsIconRole.RANDOM_WALLPAPER -> Icons.Outlined.Shuffle
    SettingsIconRole.DISPLAY_STYLE -> Icons.Outlined.ViewCarousel
    SettingsIconRole.HOME_COVER_GLASS -> Icons.Outlined.Opacity
    SettingsIconRole.VIDEO_DURATION_BADGES -> Icons.Outlined.Timer
    SettingsIconRole.HOME_INFO_GLASS -> Icons.Outlined.Badge
    SettingsIconRole.HOME_WALLPAPER -> Icons.Outlined.Image
    SettingsIconRole.WALLPAPER_EFFECT -> Icons.Outlined.BlurOn
    SettingsIconRole.HOME_UP_BADGES -> Icons.Outlined.WorkspacePremium
    SettingsIconRole.HOME_UP_AVATAR -> Icons.Outlined.AccountCircle
    SettingsIconRole.ONLINE_COUNT -> Icons.Outlined.OnlinePrediction
    SettingsIconRole.GRID_COLUMNS -> Icons.Outlined.GridView
    SettingsIconRole.HOME_CARD_WIDTH -> Icons.Outlined.WidthNormal
    SettingsIconRole.CARD_ENTRANCE_ANIMATION -> Icons.Outlined.AutoAwesomeMotion
    SettingsIconRole.CARD_TRANSITION_ANIMATION -> Icons.Outlined.SyncAlt
    SettingsIconRole.LIVE_SURFACE_TRANSITION -> Icons.Outlined.Movie
    SettingsIconRole.PREDICTIVE_BACK -> Icons.AutoMirrored.Outlined.ArrowBack
    SettingsIconRole.MIUIX_TRANSITION_BLUR -> Icons.Outlined.Gradient
    SettingsIconRole.TOP_DOCK_GLASS -> Icons.Outlined.Layers
    SettingsIconRole.HOME_SEARCH_GLASS -> Icons.AutoMirrored.Outlined.ManageSearch
    SettingsIconRole.BOTTOM_BAR_GLASS -> Icons.Outlined.BlurCircular
    SettingsIconRole.TOP_BAR_BLUR -> Icons.Outlined.ViewHeadline
    SettingsIconRole.HEADER_COLLAPSE -> Icons.Outlined.KeyboardArrowUp
    SettingsIconRole.BOTTOM_BAR_BLUR -> Icons.Outlined.BlurLinear
    SettingsIconRole.FLOATING_BOTTOM_BAR -> Icons.Outlined.ViewAgenda
    SettingsIconRole.HARDWARE_DECODER -> Icons.Outlined.Memory
    SettingsIconRole.PLAYBACK_SPEED -> Icons.Outlined.Speed
    SettingsIconRole.LONG_PRESS_SPEED_HINT -> Icons.Outlined.VisibilityOff
    SettingsIconRole.RESUME_PLAYBACK_PROMPT -> Icons.Outlined.Restore
    SettingsIconRole.STOP_ON_EXIT -> Icons.Outlined.StopCircle
    SettingsIconRole.BACKGROUND_PLAYBACK -> Icons.Outlined.MusicNote
    SettingsIconRole.PLAYLIST_AUTO_CONTINUE -> Icons.Outlined.QueuePlayNext
    SettingsIconRole.AUDIO_FOCUS -> Icons.Outlined.Headphones
    SettingsIconRole.SLIDE_VOLUME_BRIGHTNESS -> Icons.Outlined.SwapVert
    SettingsIconRole.PIP_DANMAKU -> Icons.Outlined.Textsms
    SettingsIconRole.DANMAKU_CLOUD_SYNC -> Icons.Outlined.CloudSync
    SettingsIconRole.PLAYER_DIAGNOSTICS -> Icons.Outlined.QueryStats
    SettingsIconRole.QUALITY_WARNING -> Icons.Outlined.ReportProblem
    SettingsIconRole.SUBTITLE -> Icons.Outlined.Subtitles
    SettingsIconRole.COMMENT_DECORATION -> Icons.Outlined.ModeComment
    SettingsIconRole.AI_SUMMARY -> Icons.Outlined.SmartToy
    SettingsIconRole.VIDEO_NOTE -> Icons.AutoMirrored.Outlined.Notes
    SettingsIconRole.LIKE_INTERACTION -> Icons.Outlined.ThumbUpOffAlt
    SettingsIconRole.VIDEO_DESCRIPTION -> Icons.AutoMirrored.Outlined.Subject
    SettingsIconRole.FULLSCREEN_ORIENTATION -> Icons.Outlined.ScreenRotation
    SettingsIconRole.HORIZONTAL_ADAPTATION -> Icons.Outlined.AspectRatio
    SettingsIconRole.FULLSCREEN_GESTURE_REVERSE -> Icons.Outlined.SwipeVertical
    SettingsIconRole.IMMERSIVE_STATUS_BAR -> Icons.Outlined.Fullscreen
    // 自动进入全屏用 OpenInFull(向外展开),与 FullscreenExit 保持可区分。
    SettingsIconRole.AUTO_ENTER_FULLSCREEN -> Icons.Outlined.OpenInFull
    SettingsIconRole.AUTO_EXIT_FULLSCREEN -> Icons.Outlined.FullscreenExit
    SettingsIconRole.FULLSCREEN_LOCK -> Icons.Outlined.ScreenLockRotation
    SettingsIconRole.FULLSCREEN_SCREENSHOT -> Icons.Outlined.Screenshot
    SettingsIconRole.CLEAN_SCREENSHOT -> Icons.Outlined.ScreenshotMonitor
    SettingsIconRole.BATTERY_STATUS -> Icons.Outlined.BatteryFull
    SettingsIconRole.TIME_STATUS -> Icons.Outlined.AccessTime
    SettingsIconRole.PLAYER_ACTIONS -> Icons.Outlined.MoreHoriz
    SettingsIconRole.PRIVACY_CONTENT_AUTHENTICATION -> Icons.Outlined.Verified
    SettingsIconRole.PLAYER_STATS -> Icons.Outlined.InsertChartOutlined
    SettingsIconRole.PLAYER_DIAGNOSTIC_LOGS -> Icons.Outlined.ReportGmailerrorred
    SettingsIconRole.QUALITY_WARNING_ONCE -> Icons.Outlined.NotificationImportant
    SettingsIconRole.DIRECTED_TRAFFIC -> Icons.Outlined.NetworkLocked
    SettingsIconRole.AUTO_HIGHEST_QUALITY -> Icons.Outlined.SettingsSuggest
    SettingsIconRole.AUTO_PLAY_ON_OPEN -> Icons.Outlined.PlayArrow
    SettingsIconRole.HOME_HERO_AUTOPLAY -> Icons.Outlined.SmartDisplay
    SettingsIconRole.AUTO_PLAY_NEXT -> Icons.AutoMirrored.Outlined.PlaylistPlay
    SettingsIconRole.VIDEO_NOTE_COLLAPSE -> Icons.AutoMirrored.Outlined.ShortText
    SettingsIconRole.INTERACTIVE_COMMANDS -> Icons.Outlined.CommentsDisabled
    SettingsIconRole.PORTRAIT_SWIPE_FULLSCREEN -> Icons.Outlined.SwipeUp
    SettingsIconRole.CENTER_SWIPE_FULLSCREEN -> Icons.Outlined.Swipe
    SettingsIconRole.SYSTEM_BRIGHTNESS -> Icons.Outlined.BrightnessMedium
    SettingsIconRole.APP_ICON -> Icons.Outlined.Apps
    SettingsIconRole.HOME_CARD_STATS_COMPACT -> Icons.Outlined.StackedBarChart
    SettingsIconRole.HOME_HERO_CAROUSEL -> Icons.Outlined.ViewDay
    SettingsIconRole.HOME_ONLINE_COUNT -> Icons.Outlined.Groups
    SettingsIconRole.DISPLAY_SCALE -> Icons.Outlined.ZoomOutMap
    SettingsIconRole.UI_ENTRANCE_ANIMATION -> Icons.Outlined.MotionPhotosOn
    SettingsIconRole.FULLSCREEN_SWIPE_BACK -> Icons.Outlined.SwipeRight
    SettingsIconRole.FOLLOW_BUTTON -> Icons.Outlined.PersonAdd
    SettingsIconRole.PRIVACY_HISTORY -> Icons.Outlined.HistoryToggleOff
    SettingsIconRole.CUSTOM_MD3_COLOR -> Icons.Outlined.FormatPaint
    SettingsIconRole.THEME_LIGHT_BACKGROUND -> Icons.Outlined.LightMode
    SettingsIconRole.THEME_LIGHT_PRIMARY_TEXT -> Icons.Outlined.TextFields
    SettingsIconRole.THEME_LIGHT_SECONDARY_TEXT -> Icons.Outlined.FormatSize
    SettingsIconRole.THEME_LIGHT_CONTROL -> Icons.Outlined.Tune
    SettingsIconRole.THEME_DARK_BACKGROUND -> Icons.Outlined.DarkMode
    SettingsIconRole.THEME_DARK_PRIMARY_TEXT -> Icons.Outlined.TextFormat
    SettingsIconRole.THEME_DARK_SECONDARY_TEXT -> Icons.Outlined.Notes
    SettingsIconRole.THEME_DARK_CONTROL -> Icons.Outlined.ControlPoint
    SettingsIconRole.DEVELOPER_CRASH_TRACKING -> Icons.Outlined.HealthAndSafety
    SettingsIconRole.DEVELOPER_ANALYTICS -> Icons.Outlined.DataUsage
    SettingsIconRole.APP_VERSION -> Icons.Outlined.NewReleases
    SettingsIconRole.BOTTOM_BAR_GLASS_PREVIEW -> Icons.Outlined.LensBlur
    SettingsIconRole.ADVANCED_COLOR -> Icons.Outlined.InvertColors
    SettingsIconRole.CAST_BUTTON -> Icons.Outlined.Cast
    SettingsIconRole.PROGRESS_PEAK_DANMAKU -> Icons.Outlined.GraphicEq
}
