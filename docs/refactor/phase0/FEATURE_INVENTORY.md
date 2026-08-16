# 阶段 0：功能/页面/路由清单与 Feature 分级冻结

日期：2026-08-16。本文件冻结后作为后续阶段工作量依据；调整分级必须显式修改本文件。

## 1. 功能清单（28 个 feature 子包）

包根：`app/src/main/java/com/android/purebilibili/feature/`。

| 功能 | .kt 数 | 行数 | 主要页面 | 核心类（VM/Screen） | 冻结 Tier |
|---|---|---|---|---|---|
| home | 94 | 28883 | HomeScreen（底栏 tab） | HomeViewModel / HomeScreen | A |
| video | 261 | 99612 | VideoDetailScreen、播放器、弹幕、评论 | VideoPlaybackViewModel(8414 行) / VideoDetailScreen | A |
| dynamic | 48 | 13299 | DynamicScreen、DynamicDetailScreen | DynamicViewModel / DynamicScreen | A |
| settings | 81 | 22940 | SettingsScreen + 13 子页 | SettingsViewModel / SettingsScreen | A |
| live | 32 | 11287 | LiveList/LiveSearch/LiveArea*/LiveFollowing/LivePlayer | LivePlayerViewModel / LivePlayerScreen | A |
| search | 19 | 7811 | SearchScreen、SearchTrending、TopicDetail | SearchViewModel / SearchScreen | A |
| login | 9 | 3798 | LoginScreen | LoginViewModel / LoginScreen | A |
| download | 29 | 4788 | DownloadList、OfflineVideoPlayer | 无独立 VM（DownloadManager 单例） | A |
| space | 12 | 8901 | SpaceScreen、SeasonSeriesDetail | SpaceViewModel / SpaceScreen | B |
| bangumi | 25 | 10030 | Bangumi、BangumiDetail、BangumiPlayer、Timeline | BangumiViewModel、BangumiPlayerViewModel | B |
| article | 5 | 716 | ArticleDetailScreen | 无 VM | B |
| following | 4 | 1438 | FollowingListScreen | embedded FollowingListViewModel | B |
| watchlater | 3 | 1532 | WatchLaterScreen | embedded WatchLaterViewModel | B |
| list | 23 | 6773 | CommonListScreen 承载 History/Favorite/LikedVideos/SeasonSeries | BaseListViewModel 等 4 VM | B（默认） |
| partition | 1 | 975 | PartitionScreen | 无 VM | B |
| category | 1 | 305 | CategoryScreen | 无 VM | B |
| personal | 2 | 171 | 无页面（共享叶子组件 PersonalMediaCard 等） | 无 | B |
| profile | 13 | 8346 | ProfileScreen（底栏"我的"） | ProfileViewModel / ProfileScreen | B |
| plugin | 28 | 10200 | PluginsSettings、JsPluginContent、ExternalMediaPlayer | 无独立 VM | B |
| privacy | 1 | 56 | 无页面（隐私鉴权拦截层） | PrivacyAuthenticationPolicy | C |
| onboarding | 6 | 1389 | OnboardingScreen | 无 VM | C |
| screenshot | 6 | 746 | 无页面（截图手势 overlay，MainActivity 挂载） | AppScreenshotCapture | C |
| cast | 6 | 1412 | 无路由页面（DLNA/投屏对话框） | DeviceListDialog | C |
| message | 15 | 4721 | Inbox/ReplyMe/AtMe/LikeMe/SystemNotice/Chat | InboxViewModel、ChatViewModel | C |
| web | 2 | 330 | WebViewScreen | 无 VM | C |
| livesite | 4 | 524 | 无路由页面（外部直播站点接口，休眠） | 无 | C |
| common | 1 | 20 | —（VideoLazyKeyPolicy 共享策略） | — | 不分级 |

## 2. 路由清单（57 NavKey + Unknown 兜底，全部注册无悬空）

路由定义：`navigation3/BiliPaiNavKey.kt`（sealed `BiliPaiNavKey : NavKey`）；内容角色映射 `navigation3/BiliPaiNavEntryContentPolicy.kt`；legacy 字符串路由 `navigation/ScreenRoutes.kt`（38 个）经 `navigation3/BiliPaiNavKeyMappingPolicy.kt` 双向转换；底栏定义 `feature/home/components/BottomBar.kt`（BottomNavItem 9 项）。渲染中心：`navigation/AppNavigation.kt`（2992 行）。

| 功能 | NavKey / route | 底栏 | Composable |
|---|---|---|---|
| 壳 | MainHost (`main_host`) | — | AppNavigation 内 HorizontalPager |
| home | Home (`home`) | ✅ | feature/home/HomeScreen.kt |
| dynamic | Dynamic (`dynamic`) | ✅ | feature/dynamic/DynamicScreen.kt |
| search | Search / SearchTrending / TopicDetail | — | SearchScreen / SearchTrendingScreen / TopicDetailScreen |
| settings | Settings / SettingsCategory / SettingsSearch / OpenSourceLicenses / AppearanceSettings / HomeSettings / AnimationSettings / PlaybackSettings / PermissionSettings / PluginsSettings(底栏"插件"✅) / BottomBarSettings / SettingsShare / WebDavBackup | Settings✅ | feature/settings/screen/*.kt |
| plugin | JsPluginContent (`js_plugin/{id}`) / ExternalMedia (`external_media/{launchId}`) | — | feature/plugin/js/*.kt |
| login | Login | — | feature/login/LoginScreen.kt |
| profile | Profile | ✅ | feature/profile/ProfileScreen.kt |
| list | History✅ / HistorySearch / Favorite✅ / FavoriteSearch / LikedVideos | — | feature/list/CommonListScreen.kt |
| watchlater | WatchLater✅ / WatchLaterSearch | — | feature/watchlater/WatchLaterScreen.kt |
| onboarding | Onboarding | — | feature/onboarding/OnboardingScreen.kt |
| following | Following (`following/{mid}`) | — | feature/following/FollowingListScreen.kt |
| download | DownloadList / OfflineVideoPlayer (`offline_video/{taskId}`) | — | feature/download/*.kt |
| live | LiveList✅ / LiveSearch / LiveArea / LiveAreaDetail / LiveFollowing / Live (`live/{roomId}`) | — | feature/live/*.kt |
| message | Inbox / ReplyMe / AtMe / LikeMe / SystemNotice / Chat (`chat/{talkerId}/{sessionType}`) | — | feature/message/*.kt |
| partition | Partition | — | feature/partition/PartitionScreen.kt |
| space | Space (`space/{mid}`) / SeasonSeriesDetail (`season_series_detail/{type}/{id}`) | — | feature/space/SpaceScreen.kt + CommonListScreen |
| bangumi | Bangumi / BangumiDetail (`bangumi/{seasonId}?epId=`) / BangumiPlayer (`bangumi/play/…`) | — | feature/bangumi/*.kt |
| video | VideoDetail (`video/{bvid}?…`) | — | feature/video/screen/VideoDetailScreen.kt |
| article | ArticleDetail (`article/{articleId}`) | — | feature/article/ArticleDetailScreen.kt |
| category | Category (`category/{tid}`) | — | feature/category/CategoryScreen.kt |
| web | Web (`web?url=`) | — | feature/web/WebViewScreen.kt |
| — | Unknown 兜底 → 回退 HOME | — | — |

## 3. Feature 分级冻结说明（watch items，分级本身未偏离计划书）

计划书 §3.3 清单原样冻结，**零修改**。审计发现的结构性异常记录如下，不改变冻结分级，仅在对应切片阶段处理：

1. `list` 未列入计划清单，按"未列出的新 Feature 默认 Tier B"冻结为 B。它承载历史/收藏两个底栏 tab + 4 个 VM，体量接近 A，后续若要上调必须显式修改本清单。
2. `personal` 无页面无 VM，实为共享叶子组件（被 list/watchlater 引用），Tier B 名下结构上是 common。切片时按 common 处理，不引入 Coordinator。
3. `message` 含 2 个正式 VM（InboxViewModel/ChatViewModel），比典型 Tier C 重，但严格按清单冻结为 C；若按结构成熟度再分级可上提 B。
4. `partition`/`category` 各 1 个纯 Composable 文件、无 VM，结构上像 C，按清单冻结为 B，切片时按 B 的常规结构补 Coordinator 或按实际情况在阶段 4 切片计划中说明。
5. `download` 无独立 VM（依赖 DownloadManager 单例），契约层切片时需补。
6. `livesite` 是未接入的休眠基础设施（LiveSiteRegistry 无外部消费方），切片时确认是否保留。
7. `ScreenRoutes.VideoPlayer`（`video_player/…`）是死路由：仅定义于 `ScreenRoutes.kt:107`，映射策略与 AppNavigation 均未引用。
8. `Bangumi` 与 `BangumiDetail` routeBase 同为 `"bangumi"`，隐私鉴权 routeBase 匹配可能误判，阶段 5 处理。
9. `common/VideoLazyKeyPolicy` 被 video/list/category/partition 引用，保留。

## 4. 每功能直接 M3 / Miuix import 文件数（app feature 包）

统计口径：`rg -l 'androidx.compose.material3' / 'top.yukonga.miuix' -g '*.kt'`。

| 功能 | M3 | Miuix | 功能 | M3 | Miuix |
|---|---|---|---|---|---|
| video | 74 | 5 | settings | 21 | 2 |
| home | 24 | 12 | live | 20 | 0 |
| dynamic | 16 | 1 | plugin | 11 | 0 |
| bangumi | 9 | 0 | message | 7 | 0 |
| search | 6 | 1 | list | 4 | 0 |
| download | 5 | 0 | profile | 5 | 0 |
| login | 2 | 0 | onboarding | 2 | 0 |
| space | 2 | 0 | article / category / following / watchlater / web / personal / cast / screenshot | 各 1 | 0 |
| partition | 1 | 1 | privacy / livesite / common | 0 | 0 |

M3 计数多数是 `MaterialTheme`/类型导入而非组件；app 内无任何文件直接 import `miuix.kmp.basic` 组件（Miuix 组件全部经 design-system 或导航库暴露）。M3 vs Miuix 的全局分布、design-system 双 renderer 痕迹见 `ARCHITECTURE_AUDIT.md` §2。
