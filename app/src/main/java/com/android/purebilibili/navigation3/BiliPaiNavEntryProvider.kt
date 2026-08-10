package com.android.purebilibili.navigation3

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.nav.core.NavEntryBuilder
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransition

internal fun NavEntryBuilder.biliPaiNavEntries(
    swipeBackDirection: NavSwipeDirection,
    videoCardTransition: NavTransition,
    fullscreenVideoCardTransition: NavTransition,
    content: @Composable (BiliPaiNavKey) -> Unit,
) {
    entry<BiliPaiNavKey.MainHost>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Home>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.ListenVideo>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Dynamic>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Search>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.SearchTrending>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.TopicDetail>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Settings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.SettingsCategory>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.SettingsSearch>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.OpenSourceLicenses>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.AppearanceSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.HomeSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.IconSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.AnimationSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.PlaybackSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.PermissionSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.PluginsSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.JsPluginContent>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.ExternalMedia>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.BottomBarSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.SettingsShare>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.WebDavBackup>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.TipsSettings>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Login>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Profile>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.History>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.HistorySearch>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Favorite>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.FavoriteSearch>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.LikedVideos>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.WatchLater>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.WatchLaterSearch>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Onboarding>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Following>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.DownloadList>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.OfflineVideoPlayer>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.LiveList>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.LiveSearch>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.LiveArea>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.LiveAreaDetail>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.LiveFollowing>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Inbox>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.ReplyMe>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.AtMe>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.LikeMe>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.SystemNotice>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Chat>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Partition>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Story>(
        transition = fullscreenVideoCardTransition,
        swipeDismiss = NavSwipeDirection.None,
        content = content,
    )
    entry<BiliPaiNavKey.AudioMode>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.SeasonSeriesDetail>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Bangumi>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.BangumiPlayer>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.MusicDetail>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.NativeMusic>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.VideoDetail>(
        transition = videoCardTransition,
        swipeDismiss = NavSwipeDirection.None,
        content = content,
    )
    entry<BiliPaiNavKey.ArticleDetail>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.DynamicDetail>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Space>(swipeDismiss = swipeBackDirection, content = content)
    entry<BiliPaiNavKey.Category>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Live>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.BangumiDetail>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Web>(swipeDismiss = NavSwipeDirection.None, content = content)
    entry<BiliPaiNavKey.Unknown>(swipeDismiss = NavSwipeDirection.None, content = content)
}
