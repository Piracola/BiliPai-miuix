# 页面唯一目录

> 文档编号：UI-PAGE-INDEX  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-04  
> 适用提交：自动以 `BiliPaiNavKey.kt` 结构测试为准
> 维护角色：导航维护者、设计系统维护者  
> 相关文档：[页面母版](../08_PAGE_TEMPLATES.md) · [总目录](../README.md)

## 初学者解释

本页是 Navigation 3 页面范围的唯一登记表。`BiliPaiNavKey` 像页面的身份证类型；同一个 Key 可以携带不同参数，但仍属于同一种页面档案。例如不同视频都使用同一个视频详情 Key，不需要为每条视频单独写规范。

`[NAVKEY:名称]` 是机器检查标记，只允许在本文件出现一次。领域档案使用稳定的 P 编号，不重复这个标记。

## 规范要求

- **必须**让源码的每个 Key 在本表恰好出现一次，新增、重命名或删除 Key 时同步更新。
- **必须**把每项链接到一个页面母版和一份完整领域档案。
- **必须**将 `MainHost` 标为容器，将 `Unknown` 标为兼容入口，不能伪装成普通独立 Screen。
- **应该**以 `BiliPaiNavEntryContentRole` 与实际渲染符号作为当前代码映射。
- **禁止**把旧 `ScreenRoutes` 单独当成第二份页面目录。

## 页面 Key 目录

| ID / Key | 中文名称 | `routeBase` | 当前内容入口 | 母版 | 完整档案 | 状态 / 最后核对 |
|---|---|---|---|---|---|---|
| P001 `[NAVKEY:MainHost]` | 主应用容器 | `main_host` | `MAIN_HOST` / 主 Pager 容器 | T01 应用外壳 | [P001](APP_SHELL_HOME.md#p001) | 当前 / 2026-08-02 |
| P002 `[NAVKEY:Home]` | 首页 | `home` | `HOME` / `HomeScreen` | T02 信息流 | [P002](APP_SHELL_HOME.md#p002) | 当前 / 2026-08-02 |
| P003 `[NAVKEY:ListenVideo]` | 听视频首页 | `listen_video` | `LISTEN_VIDEO` / `ListenVideoRoute` | T03 列表管理 | [P003](LIVE_BANGUMI_AUDIO.md#p003) | 当前 / 2026-08-02 |
| P004 `[NAVKEY:Dynamic]` | 动态 | `dynamic` | `DYNAMIC` / `DynamicScreen` | T02 信息流 | [P004](COMMUNITY_MESSAGE.md#p004) | 当前 / 2026-08-02 |
| P005 `[NAVKEY:Search]` | 综合搜索 | `search` | `SEARCH` / `SearchScreen` | T04 搜索发现 | [P005](SEARCH_DISCOVERY.md#p005) | 当前 / 2026-08-02 |
| P006 `[NAVKEY:SearchTrending]` | 搜索热榜 | `search_trending` | `SEARCH_TRENDING` / `SearchTrendingScreen` | T02 信息流 | [P006](SEARCH_DISCOVERY.md#p006) | 当前 / 2026-08-02 |
| P007 `[NAVKEY:TopicDetail]` | 话题详情 | `topic` | `TOPIC_DETAIL` / `TopicDetailScreen` | T05 内容详情 | [P007](SEARCH_DISCOVERY.md#p007) | 当前 / 2026-08-02 |
| P008 `[NAVKEY:Settings]` | 设置首页 | `settings` | `SETTINGS` / `SettingsScreen` | T08 设置 | [P008](SETTINGS.md#p008) | 当前 / 2026-08-02 |
| P009 `[NAVKEY:SettingsCategory]` | 设置分类 | `settings_category` | `SETTINGS_CATEGORY` / `SettingsCategoryScreen` | T08 设置 | [P009](SETTINGS.md#p009) | 当前 / 2026-08-02 |
| P010 `[NAVKEY:SettingsSearch]` | 设置搜索 | `settings_search` | `SETTINGS_SEARCH` / `SettingsSearchScreen` | T04 搜索发现 | [P010](SETTINGS.md#p010) | 当前 / 2026-08-02 |
| P011 `[NAVKEY:OpenSourceLicenses]` | 开源许可 | `open_source_licenses` | `OPEN_SOURCE_LICENSES` / `OpenSourceLicensesScreen` | T03 列表管理 | [P011](SETTINGS.md#p011) | 当前 / 2026-08-02 |
| P012 `[NAVKEY:AppearanceSettings]` | 外观设置 | `appearance_settings` | `APPEARANCE_SETTINGS` / `AppearanceSettingsScreen` | T08 设置 | [P012](SETTINGS.md#p012) | 当前 / 2026-08-02 |
| P013 `[NAVKEY:IconSettings]` | 图标设置 | `icon_settings` | `ICON_SETTINGS` / `IconSettingsScreen` | T08 设置 | [P013](SETTINGS.md#p013) | 当前 / 2026-08-02 |
| P014 `[NAVKEY:AnimationSettings]` | 动画设置 | `animation_settings` | `ANIMATION_SETTINGS` / `AnimationSettingsScreen` | T08 设置 | [P014](SETTINGS.md#p014) | 当前 / 2026-08-02 |
| P015 `[NAVKEY:PlaybackSettings]` | 播放设置 | `playback_settings` | `PLAYBACK_SETTINGS` / `PlaybackSettingsScreen` | T08 设置 | [P015](SETTINGS.md#p015) | 当前 / 2026-08-02 |
| P016 `[NAVKEY:PermissionSettings]` | 权限设置 | `permission_settings` | `PERMISSION_SETTINGS` / `PermissionSettingsScreen` | T08 设置 | [P016](SETTINGS.md#p016) | 当前 / 2026-08-02 |
| P017 `[NAVKEY:PluginsSettings]` | 插件中心 | `plugins_settings` | `PLUGINS_SETTINGS` / `PluginsScreen` | T08 设置 | [P017](SETTINGS.md#p017) | 当前 / 2026-08-02 |
| P018 `[NAVKEY:JsPluginContent]` | JS 插件内容 | `js_plugin` | `JS_PLUGIN_CONTENT` / `BiliPaiJsPluginContentScreen` | T05 内容详情 | [P018](ACCOUNT_TOOLS_WEB.md#p018) | 当前 / 2026-08-02 |
| P019 `[NAVKEY:ExternalMedia]` | 外部媒体播放 | `external_media` | `EXTERNAL_MEDIA` / `ExternalMediaPlayerScreen` | T09 媒体播放器 | [P019](ACCOUNT_TOOLS_WEB.md#p019) | 当前 / 2026-08-02 |
| P020 `[NAVKEY:BottomBarSettings]` | 底栏设置 | `bottom_bar_settings` | `BOTTOM_BAR_SETTINGS` / `BottomBarSettingsScreen` | T08 设置 | [P020](SETTINGS.md#p020) | 当前 / 2026-08-02 |
| P021 `[NAVKEY:SettingsShare]` | 设置分享 | `settings_share` | `SETTINGS_SHARE` / `SettingsShareScreen` | T10 引导任务流 | [P021](SETTINGS.md#p021) | 当前 / 2026-08-02 |
| P022 `[NAVKEY:WebDavBackup]` | WebDAV 备份 | `webdav_backup` | `WEB_DAV_BACKUP` / `WebDavBackupScreen` | T10 引导任务流 | [P022](ACCOUNT_TOOLS_WEB.md#p022) | 当前 / 2026-08-02 |
| P023 `[NAVKEY:TipsSettings]` | 提示设置 | `tips_settings` | `TIPS_SETTINGS` / `TipsSettingsScreen` | T08 设置 | [P023](SETTINGS.md#p023) | 当前 / 2026-08-02 |
| P024 `[NAVKEY:Login]` | 登录 | `login` | `LOGIN` / `LoginScreen` | T10 引导任务流 | [P024](ACCOUNT_TOOLS_WEB.md#p024) | 当前 / 2026-08-02 |
| P025 `[NAVKEY:Profile]` | 我的 | `profile` | `PROFILE` / `ProfileScreen` | T06 个人空间 | [P025](PROFILE_LIBRARY.md#p025) | 当前 / 2026-08-02 |
| P026 `[NAVKEY:History]` | 历史记录 | `history` | `HISTORY` / `CommonListScreen` | T03 列表管理 | [P026](PROFILE_LIBRARY.md#p026) | 当前 / 2026-08-02 |
| P027 `[NAVKEY:Favorite]` | 收藏 | `favorite` | `FAVORITE` / `CommonListScreen` | T03 列表管理 | [P027](PROFILE_LIBRARY.md#p027) | 当前 / 2026-08-02 |
| P028 `[NAVKEY:LikedVideos]` | 点赞视频 | `liked_videos` | `LIKED_VIDEOS` / `CommonListScreen` | T03 列表管理 | [P028](PROFILE_LIBRARY.md#p028) | 当前 / 2026-08-02 |
| P029 `[NAVKEY:WatchLater]` | 稍后再看 | `watch_later` | `WATCH_LATER` / `WatchLaterScreen` | T03 列表管理 | [P029](PROFILE_LIBRARY.md#p029) | 当前 / 2026-08-02 |
| P030 `[NAVKEY:Onboarding]` | 首次引导 | `onboarding` | `ONBOARDING` / `OnboardingScreen` | T10 引导任务流 | [P030](ACCOUNT_TOOLS_WEB.md#p030) | 当前 / 2026-08-02 |
| P031 `[NAVKEY:Following]` | 关注列表 | `following` | `FOLLOWING` / `FollowingListScreen` | T03 列表管理 | [P031](PROFILE_LIBRARY.md#p031) | 当前 / 2026-08-02 |
| P032 `[NAVKEY:DownloadList]` | 下载列表 | `download_list` | `DOWNLOAD_LIST` / `DownloadListScreen` | T03 列表管理 | [P032](PROFILE_LIBRARY.md#p032) | 当前 / 2026-08-02 |
| P033 `[NAVKEY:OfflineVideoPlayer]` | 离线视频播放 | `offline_video` | `OFFLINE_VIDEO_PLAYER` / `OfflineVideoPlayerScreen` | T09 媒体播放器 | [P033](VIDEO_PLAYBACK.md#p033) | 当前 / 2026-08-02 |
| P034 `[NAVKEY:LiveList]` | 直播首页 | `live_list` | `LIVE_LIST` / `LiveListScreen` | T02 信息流 | [P034](LIVE_BANGUMI_AUDIO.md#p034) | 当前 / 2026-08-02 |
| P035 `[NAVKEY:LiveSearch]` | 直播搜索 | `live_search` | `LIVE_SEARCH` / `LiveSearchScreen` | T04 搜索发现 | [P035](LIVE_BANGUMI_AUDIO.md#p035) | 当前 / 2026-08-02 |
| P036 `[NAVKEY:LiveArea]` | 直播分区 | `live_area` | `LIVE_AREA` / `LiveAreaScreen` | T02 信息流 | [P036](LIVE_BANGUMI_AUDIO.md#p036) | 当前 / 2026-08-02 |
| P037 `[NAVKEY:LiveAreaDetail]` | 直播分区详情 | `live_area_detail` | `LIVE_AREA_DETAIL` / `LiveAreaDetailScreen` | T02 信息流 | [P037](LIVE_BANGUMI_AUDIO.md#p037) | 当前 / 2026-08-02 |
| P038 `[NAVKEY:LiveFollowing]` | 关注的直播 | `live_following` | `LIVE_FOLLOWING` / `LiveFollowingScreen` | T03 列表管理 | [P038](LIVE_BANGUMI_AUDIO.md#p038) | 当前 / 2026-08-02 |
| P039 `[NAVKEY:Inbox]` | 消息中心 | `inbox` | `INBOX` / `InboxScreen` | T07 消息会话 | [P039](COMMUNITY_MESSAGE.md#p039) | 当前 / 2026-08-02 |
| P040 `[NAVKEY:ReplyMe]` | 回复我的 | `message/reply_me` | `REPLY_ME` / `ReplyMeScreen` | T03 列表管理 | [P040](COMMUNITY_MESSAGE.md#p040) | 当前 / 2026-08-02 |
| P041 `[NAVKEY:AtMe]` | @ 我的 | `message/at_me` | `AT_ME` / `AtMeScreen` | T03 列表管理 | [P041](COMMUNITY_MESSAGE.md#p041) | 当前 / 2026-08-02 |
| P042 `[NAVKEY:LikeMe]` | 收到的赞 | `message/like_me` | `LIKE_ME` / `LikeMeScreen` | T03 列表管理 | [P042](COMMUNITY_MESSAGE.md#p042) | 当前 / 2026-08-02 |
| P043 `[NAVKEY:SystemNotice]` | 系统通知 | `message/system_notice` | `SYSTEM_NOTICE` / `SystemNoticeScreen` | T03 列表管理 | [P043](COMMUNITY_MESSAGE.md#p043) | 当前 / 2026-08-02 |
| P044 `[NAVKEY:Chat]` | 私信会话 | `chat` | `CHAT` / `ChatScreen` | T07 消息会话 | [P044](COMMUNITY_MESSAGE.md#p044) | 当前 / 2026-08-02 |
| P045 `[NAVKEY:Partition]` | 内容分区 | `partition` | `PARTITION` / `PartitionScreen` | T02 信息流 | [P045](SEARCH_DISCOVERY.md#p045) | 当前 / 2026-08-02 |
| P046 `[NAVKEY:Story]` | 竖屏故事流 | `story` | `STORY` / `StoryScreen` | T09 媒体播放器 | [P046](VIDEO_PLAYBACK.md#p046) | 当前 / 2026-08-02 |
| P047 `[NAVKEY:AudioMode]` | 视频音频模式 | `audio_mode` | `AUDIO_MODE` / `AudioModeScreen` | T09 媒体播放器 | [P047](LIVE_BANGUMI_AUDIO.md#p047) | 当前 / 2026-08-02 |
| P048 `[NAVKEY:SeasonSeriesDetail]` | 合集/收藏夹详情 | `season_series_detail` | `SEASON_SERIES_DETAIL` / 合集详情内容 | T03 列表管理 | [P048](PROFILE_LIBRARY.md#p048) | 当前 / 2026-08-02 |
| P049 `[NAVKEY:Bangumi]` | 番剧首页 | `bangumi` | `BANGUMI` / `BangumiScreen` | T02 信息流 | [P049](LIVE_BANGUMI_AUDIO.md#p049) | 当前 / 2026-08-02 |
| P050 `[NAVKEY:BangumiPlayer]` | 番剧播放器 | `bangumi/play` | `BANGUMI_PLAYER` / `BangumiPlayerScreen` | T09 媒体播放器 | [P050](LIVE_BANGUMI_AUDIO.md#p050) | 当前 / 2026-08-02 |
| P051 `[NAVKEY:MusicDetail]` | 音乐详情 | `music` | `MUSIC_DETAIL` / `MusicDetailScreen` | T05 内容详情 | [P051](LIVE_BANGUMI_AUDIO.md#p051) | 当前 / 2026-08-02 |
| P052 `[NAVKEY:NativeMusic]` | 原生音乐播放 | `native_music` | `NATIVE_MUSIC` / `MusicDetailScreen` | T09 媒体播放器 | [P052](LIVE_BANGUMI_AUDIO.md#p052) | 当前 / 2026-08-02 |
| P053 `[NAVKEY:VideoDetail]` | 视频详情与播放 | `video` | `VIDEO_DETAIL` / `VideoDetailScreen` | T05 + T09 | [P053](VIDEO_PLAYBACK.md#p053) | 当前 / 2026-08-02 |
| P054 `[NAVKEY:ArticleDetail]` | 专栏文章详情 | `article` | `ARTICLE_DETAIL` / `ArticleDetailScreen` | T05 内容详情 | [P054](COMMUNITY_MESSAGE.md#p054) | 当前 / 2026-08-02 |
| P055 `[NAVKEY:DynamicDetail]` | 动态详情 | `dynamic_detail` | `DYNAMIC_DETAIL` / `DynamicDetailScreen` | T05 内容详情 | [P055](COMMUNITY_MESSAGE.md#p055) | 当前 / 2026-08-02 |
| P056 `[NAVKEY:Space]` | 用户空间 | `space` | `SPACE` / `SpaceScreen` | T06 个人空间 | [P056](PROFILE_LIBRARY.md#p056) | 当前 / 2026-08-02 |
| P057 `[NAVKEY:Category]` | 视频分类 | `category` | `CATEGORY` / `CategoryScreen` | T02 信息流 | [P057](SEARCH_DISCOVERY.md#p057) | 当前 / 2026-08-02 |
| P058 `[NAVKEY:Live]` | 直播间 | `live` | `LIVE` / `LivePlayerScreen` | T09 媒体播放器 | [P058](LIVE_BANGUMI_AUDIO.md#p058) | 当前 / 2026-08-02 |
| P059 `[NAVKEY:BangumiDetail]` | 番剧详情 | `bangumi` | `BANGUMI_DETAIL` / `BangumiDetailScreen` | T05 内容详情 | [P059](LIVE_BANGUMI_AUDIO.md#p059) | 当前 / 2026-08-02 |
| P060 `[NAVKEY:Web]` | 应用内网页 | `web` | `WEB` / `WebViewScreen` | T05 内容详情 | [P060](ACCOUNT_TOOLS_WEB.md#p060) | 当前 / 2026-08-02 |
| P061 `[NAVKEY:Unknown]` | 未知旧路由兼容入口 | 动态解析 | 当前回退 `HOME` 内容角色 | T10 兼容回退 | [P061](ACCOUNT_TOOLS_WEB.md#p061) | 兼容 / 2026-08-02 |
| P062 `[NAVKEY:HomeSettings]` | 首页设置 | `home_settings` | `HOME_SETTINGS` / `HomeSettingsScreen` | T08 设置 | [P062](SETTINGS.md#p062) | 当前 / 2026-08-04 |
| P063 `[NAVKEY:HistorySearch]` | 历史搜索 | `history_search` | `HISTORY` / `CommonListScreen` 搜索模式 | T04 搜索发现 | [P063](PROFILE_LIBRARY.md#p063) | 当前 / 2026-08-10 |
| P064 `[NAVKEY:FavoriteSearch]` | 收藏搜索 | `favorite_search` | `FAVORITE` / `CommonListScreen` 搜索模式 | T04 搜索发现 | [P064](PROFILE_LIBRARY.md#p064) | 当前 / 2026-08-10 |
| P065 `[NAVKEY:WatchLaterSearch]` | 稍后再看搜索 | `watch_later_search` | `WATCH_LATER` / `CommonListScreen` 搜索模式 | T04 搜索发现 | [P065](PROFILE_LIBRARY.md#p065) | 当前 / 2026-08-10 |

## Compose 短示例

```kotlin
val key: BiliPaiNavKey = BiliPaiNavKey.VideoDetail(bvid = bvid)
// Key 只承载导航身份与参数；页面视觉规则来自 P053 档案。
```

## 代码映射

- Key 定义：`BiliPaiNavKey.kt`
- 内容角色：`BiliPaiNavEntryContentPolicy.kt`
- 实际渲染：`AppNavigation.kt` 中 `RenderNavigationContent`
- 旧路由兼容：`BiliPaiNavKeyMappingPolicy.kt`、`ScreenRoutes.kt`

## 当前差距

`Bangumi` 与 `BangumiDetail` 当前共享 `routeBase = "bangumi"`，依靠 Key 类型区分；`Unknown` 回退到 HOME 内容角色。这是当前事实，后续若调整路由必须同步目录和映射测试。

## 验收方法

运行结构测试，源码 Key 集合与 `[NAVKEY:*]` 集合必须完全相等；每个 P 编号必须链接到存在的领域文件锚点，领域档案必须包含固定字段。

