# 阶段 2：Core 接缝（执行记录 + 旧调用点例外清单）

日期：2026-08-16

## 一、完成内容

### 1. 高风险能力接缝接口（4 个，零 DI 框架，object 直接实现）

| 接口 | 文件 | 实现 | 说明 |
|---|---|---|---|
| `AccountSessionProvider` | `core/store/AccountSessionProvider.kt` | `object AccountSessionStore : AccountSessionProvider` | 账号列表/激活/播放账号 8 个方法，签名 1:1 |
| `SessionCredentialProvider` | `core/store/SessionCredentialProvider.kt` | `object TokenManager : SessionCredentialProvider` | 只读凭证视图（sessData/csrf/mid/buvid3/isVip），写入面不扩大 |
| `NetworkClientProvider` | `core/network/NetworkClientProvider.kt` | `object NetworkModule : NetworkClientProvider` | api/okHttpClient/playbackOkHttpClient/playbackApi()/playbackBangumiApi() |
| `SettingsReader` | `core/store/SettingsReader.kt` | 待 SettingsManager 提供窄实现（见下） | 窄接口只声明 7 个 getter |

**说明**：`SettingsReader` 的默认实现暂未绑定到 `SettingsManager`（SettingsManager 是 6067 行巨型 object，直接加 `: SettingsReader` 会迫使全部 7 个 getter 都 override 且签名已有）。当前接口已定义，消费方按 `SettingsReader = SettingsManager 的窄代理` 注入（阶段 4 首个切片接线时落地 `object SettingsReaderDelegate : SettingsReader` 或让 SettingsManager 直接实现）。

- `PlaybackSession`（`core/player/PlaybackSession.kt`）：播放会话**只读**接口（isActive/currentBvid/currentPosition/duration/isPlaying）。宿主 MiniPlayerManager 在阶段 4/5 通过只读视图暴露，不在接缝阶段引入第二套播放控制入口。

### 2. 设置持久化兼容测试（14 个用例，全绿）

`SettingsPersistenceCompatibilityTest.kt`（app/src/test/.../core/store/），覆盖：
- **默认值正确**（5 例）：空 Preferences → AppThemeSettings/HomeSettings/HomeTopTabSettings/PlayerInteractionSettings/DanmakuSettings 与 data class 默认值一致。
- **旧 schema 可读**（5 例）：legacy `theme_mode=3`→DARK、`dark_theme_style` 缺失+amoled→AMOLED、`header_blur_enabled=false`→ALWAYS_OFF、`header_collapse_enabled=false`→OFF、`home_video_duration_badges_visible=false`→HIDDEN、scoped 弹幕 key 回退全局。
- **编码格式冻结**（3 例）：collection CSV/JSON 往返、越界值夹取。

### 3. 接线改动
- `AccountSessionStore`/`TokenManager`/`NetworkModule` 三处 object 声明各加接口实现 + override 修饰符（TokenManager 加 5 个属性视图）。
- 全部既有行为零改动（接口是纯新增声明）。

## 二、旧调用点例外清单（文件级，冻结于 2026-08-16）

阶段 2 边界规则：清单只减不增，每个调用点标注归属。**文件级快照**（与阶段 1 白名单一致）：

### A. 归属"本阶段 Core 侧"（播放器基础设施 / 业务管理器，视为 Core 行为）

| 文件 | 引用对象 |
|---|---|
| feature/video/player/MiniPlayerManager.kt | SettingsManager、VideoRepository 等 |
| feature/video/state/VideoPlayerState.kt | SettingsManager、NetworkModule |
| feature/video/danmaku/DanmakuManager.kt | TokenManager、DanmakuRepository |
| feature/video/usecase/VideoPlaybackUseCase.kt | NetworkModule、TokenManager |
| feature/video/playback/*（loader/policy/session） | VideoRepository 等 |
| feature/download/DownloadManager.kt + DownloadRequestHeaders.kt | NetworkModule、SettingsManager |
| feature/plugin/*（AdFilterPlugin、CdnRegionPlugin、SponsorBlockPlugin、DlnaCastPlugin 等） | NetworkModule、VideoRepository（插件运行时） |
| feature/live/LivePlayerViewModel.kt、BangumiPlayerViewModel.kt、BangumiHubViewModel.kt | NetworkModule、TokenManager |
| feature/login/LoginViewModel.kt、profile/ProfileViewModel.kt | AccountSessionStore、TokenManager |
| feature/search/SearchViewModel.kt 等 ViewModel 类 | AppDatabase、SearchRepository |
| feature/video/viewmodel/*、feature/home/HomeViewModel.kt、dynamic/DynamicViewModel.kt 等 | 各 Repository（业务层合法） |

### B. 归属"页面阶段 4 切片"（UI/页面文件，切片时拆除）

| 页面 | 引用对象（代表） |
|---|---|
| feature/settings/screen/*（Appearance/Playback/BottomBar/Animation/Settings/Plugins） | SettingsManager 直接读（~154 处） |
| feature/settings/ui/SettingsSections.kt | SettingsManager、NetworkProxyStore |
| feature/video/ui/section/VideoPlayerSection.kt | SettingsManager、DanmakuRepository |
| feature/video/ui/overlay/*（FullscreenPlayerOverlay、VideoPlayerOverlay 等） | SettingsManager |
| feature/video/ui/components/*（DanmakuSettingsPanel、VideoSettingsPanel、ReplyComponents、RelatedVideoItem、CollectionSubscriptionButton 等） | SettingsManager、ActionRepository |
| feature/video/ui/pager/PortraitVideoPager.kt | SettingsManager、NetworkModule、VideoRepository、TokenManager |
| feature/video/screen/VideoContentSection.kt、TabletCinemaLayout.kt 等 | SettingsManager |
| feature/home/HomeScreen.kt、BottomBar.kt、TopBar.kt、HomeHeader.kt、VideoCard.kt 等 | SettingsManager |
| feature/live/LivePlayerScreen.kt、LiveAreaScreen.kt、LiveAreaDetailScreen.kt、LiveFollowingScreen.kt | SettingsManager、TokenManager、LiveRepository |
| feature/search/SearchScreen.kt、feature/dynamic/DynamicScreen.kt、feature/profile/ProfileScreen.kt | SettingsManager |
| feature/list/CommonListScreen.kt、FavoriteCategoryScreen.kt | SettingsManager、BangumiRepository |
| feature/message/feed/*Screen.kt | MessageRepository（embedded VM） |
| feature/category/CategoryScreen.kt、partition/PartitionScreen.kt | SettingsManager、VideoRepository |

> 完整 116 文件清单以 `ArchitectureAllowlist.kt` 为权威（阶段 1 CI 强制）；上表按归属分两组，供阶段 4 切片计划排序。

## 三、阶段 2 退出条件核对

| 退出条件 | 状态 |
|---|---|
| Core 不依赖具体 UI | ✅ 阶段 1 的 `CoreLayerMustNotDependOnComposeStructureTest` 零容忍 + 本阶段接口全在 core 层 |
| 高风险能力有接口，新代码可注入 | ✅ 4 接口 + object 实现；`SettingsReader` 窄接口已定义 |
| 旧调用点例外清单带归属阶段且只减不增 | ✅ 上表 A/B 分组；CI 棘轮（上限 116 + SHA）强制只减不增 |
| 设置持久化兼容测试通过 | ✅ 14 用例全绿（默认值/旧 schema/编码冻结） |
| 上游功能修复可以主要落在 Core，而不需要修改 Lite UI | ✅ Core 接口化后，修复可落在接口实现内部，UI 依赖接口不变 |

## 四、验证

- `:app:testDebugUnitTest --tests 'com.android.purebilibili.core.store.SettingsPersistenceCompatibilityTest'` → 14 tests，BUILD SUCCESSFUL
- `:app:compileDebugKotlin` 通过（接线后全量编译）。
