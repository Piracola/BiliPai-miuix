package com.android.purebilibili.navigation3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider

/**
 * Navigation3 条目只负责内容和状态保存。
 *
 * 页面动画集中由 [BiliPaiNavDisplayHost] 提供，避免单个路由用卡片位置、来源方向
 * 或页面类型覆盖全局 Push/Pop 行为。
 */
internal fun biliPaiNavEntryProvider(
    content: @Composable (BiliPaiNavKey) -> Unit,
): (BiliPaiNavKey) -> NavEntry<BiliPaiNavKey> {
    return entryProvider(
        fallback = { key -> NavEntry(key = key, content = content) },
    ) {
        entry<BiliPaiNavKey.MainHost>(content = content)
        entry<BiliPaiNavKey.Home>(content = content)
        entry<BiliPaiNavKey.Dynamic>(content = content)
        entry<BiliPaiNavKey.Search>(content = content)
        entry<BiliPaiNavKey.SearchTrending>(content = content)
        entry<BiliPaiNavKey.TopicDetail>(content = content)
        entry<BiliPaiNavKey.Settings>(content = content)
        entry<BiliPaiNavKey.SettingsSearch>(content = content)
        entry<BiliPaiNavKey.SettingsCategory>(content = content)
        entry<BiliPaiNavKey.OpenSourceLicenses>(content = content)
        entry<BiliPaiNavKey.AppearanceSettings>(content = content)
        entry<BiliPaiNavKey.IconSettings>(content = content)
        entry<BiliPaiNavKey.AnimationSettings>(content = content)
        entry<BiliPaiNavKey.PlaybackSettings>(content = content)
        entry<BiliPaiNavKey.PermissionSettings>(content = content)
        entry<BiliPaiNavKey.PluginsSettings>(content = content)
        entry<BiliPaiNavKey.JsPluginContent>(content = content)
        entry<BiliPaiNavKey.ExternalMedia>(content = content)
        entry<BiliPaiNavKey.BottomBarSettings>(content = content)
        entry<BiliPaiNavKey.SettingsShare>(content = content)
        entry<BiliPaiNavKey.WebDavBackup>(content = content)
        entry<BiliPaiNavKey.TipsSettings>(content = content)
        entry<BiliPaiNavKey.Login>(content = content)
        entry<BiliPaiNavKey.Profile>(content = content)
        entry<BiliPaiNavKey.History>(content = content)
        entry<BiliPaiNavKey.Favorite>(content = content)
        entry<BiliPaiNavKey.WatchLater>(content = content)
        entry<BiliPaiNavKey.Onboarding>(content = content)
        entry<BiliPaiNavKey.Following>(content = content)
        entry<BiliPaiNavKey.DownloadList>(content = content)
        entry<BiliPaiNavKey.OfflineVideoPlayer>(content = content)
        entry<BiliPaiNavKey.LiveList>(content = content)
        entry<BiliPaiNavKey.LiveSearch>(content = content)
        entry<BiliPaiNavKey.LiveArea>(content = content)
        entry<BiliPaiNavKey.LiveAreaDetail>(content = content)
        entry<BiliPaiNavKey.LiveFollowing>(content = content)
        entry<BiliPaiNavKey.Inbox>(content = content)
        entry<BiliPaiNavKey.ReplyMe>(content = content)
        entry<BiliPaiNavKey.AtMe>(content = content)
        entry<BiliPaiNavKey.LikeMe>(content = content)
        entry<BiliPaiNavKey.SystemNotice>(content = content)
        entry<BiliPaiNavKey.Chat>(content = content)
        entry<BiliPaiNavKey.Partition>(content = content)
        entry<BiliPaiNavKey.Story>(content = content)
        entry<BiliPaiNavKey.AudioMode>(content = content)
        entry<BiliPaiNavKey.SeasonSeriesDetail>(content = content)
        entry<BiliPaiNavKey.Bangumi>(content = content)
        entry<BiliPaiNavKey.BangumiPlayer>(content = content)
        entry<BiliPaiNavKey.MusicDetail>(content = content)
        entry<BiliPaiNavKey.NativeMusic>(content = content)
        entry<BiliPaiNavKey.VideoDetail>(content = content)
        entry<BiliPaiNavKey.ArticleDetail>(content = content)
        entry<BiliPaiNavKey.DynamicDetail>(content = content)
        entry<BiliPaiNavKey.Space>(content = content)
        entry<BiliPaiNavKey.Category>(content = content)
        entry<BiliPaiNavKey.Live>(content = content)
        entry<BiliPaiNavKey.BangumiDetail>(content = content)
        entry<BiliPaiNavKey.Web>(content = content)
        entry<BiliPaiNavKey.Unknown>(content = content)
    }
}
