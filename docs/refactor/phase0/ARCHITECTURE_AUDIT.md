# 阶段 0：架构审计（模块依赖图 / M3-Miuix 分布 / 全局对象访问）

日期：2026-08-16。本文件是阶段 1（架构护栏）、阶段 2（Core 接缝）和阶段 6（Gradle 模块化）的输入。

## 1. 模块清单与依赖图

`settings.gradle.kts` include 7 个模块。`examples/`、`plugins/` 在磁盘上但**未 include**（JS 示例与独立仓库源码，非 Gradle 模块）。

| 模块 | 依赖的 project | 主要外部依赖类别 | main 文件数 | test 文件数 |
|---|---|---|---|---|
| `:app` | settings-core, network-core, plugin-sdk, design-system, dolby-ffmpeg-decoder | compose/ui、miuix、media3、retrofit/okhttp、room、datastore、coil、haze、cupertino、danmaku、cast、firebase | 1001 | 874 |
| `:design-system` | 无 | compose/ui(api)、miuix-ui(api)、miuix-preference、haze、cupertino | 81 | 50 |
| `:baselineprofile` | 无（targetProjectPath=:app） | benchmark-macro-junit4 | 8 | 0 |
| `:settings-core` | 无 | 无（测试 junit/jupiter） | 1 | 1 |
| `:network-core` | 无 | 无（测试） | 3 | 1 |
| `:plugin-sdk` | 无 | kotlinx-serialization-json | 4 | 2 |
| `:dolby-ffmpeg-decoder` | 无 | media3-decoder(api)、media3-exoplayer | 6(.java) | 0 |

依赖图（app 汇聚、库间零互依、无环 DAG）：

```
:app → :settings-core :network-core :plugin-sdk :design-system :dolby-ffmpeg-decoder
:baselineprofile → (targetProjectPath) :app
```

版本：AGP 9.3.1、Kotlin 2.4.0、compose BOM 2026.06.00(beta)、material3 1.5.0-alpha25、miuix 0.9.3-a370b370-SNAPSHOT、media3 1.10.1、room 2.8.4、haze 2.0.0-alpha03。**无 DI 框架**（app 手写 `di.kt` + 全局 object）。

## 2. M3 / Miuix 使用分布（阶段 3 输入）

### app 模块（1001 文件）
- import `androidx.compose.material3`：**231 文件**（feature 217 / 导航壳 2 / 其它 12）；导入行 343 条，其中通配导入 88 文件。
- import `top.yukonga.miuix`：**30 文件**（feature 22 / 导航壳 7 / 其它 1）；导入行 109 条。Miuix 集中在 blur(45)、nav(28)、icon(25)、theme(9)、overlay(2)。**app 无文件直接 import `miuix.kmp.basic` 组件**。
- import `androidx.compose.material.`（M2）：125 文件，**全部是 `material.icons`**（581 导入行）；无 M2 组件残留。
- 同时 import M3+Miuix：16 文件（HomeScreen、BottomBar、TopBar、FloatingBottomBar、HomeHeader、BottomInputBar、VideoContentSection、AppNavigation、core/theme/Theme.kt 等）。

### design-system（131 文件，main 94 / test 37）
- M3 36 文件、Miuix 15、M2 4（全是 icons）。同时 import 两者 8 个文件：AppDisplayPolicy、AdaptiveChrome、AdaptivePullToRefreshBox、AppSurfaceTokens、AdaptiveContentCardComponents、AdaptivePreferenceComponents、AppNavigationComponents、AppSelectionPreferenceComponents。

### 双 renderer 兼容痕迹（阶段 3 收敛目标）
- 真双实现：`core/ui/renderer/material3/AppMaterial3SegmentedControl.kt`（**已无调用者**）vs `core/ui/renderer/miuix/AppMiuixSegmentedControl.kt`（唯一在用）。
- 收敛为单值枚举：`PresetPrimitiveRenderer { MIUIX_BRIDGED }`、`AppSegmentedRenderer { MIUIX }`（`AppSegmentedControl.kt` 仍保留 `usesMaterialColorTokens` 分支）。
- 仍存活的 M3 类型/枚举：`AdaptiveScaffoldRenderer`、`AppAlertDialogRenderer`、`AdaptiveSideNavigationRailRenderer`、`AdaptiveTooltipRenderer`、`AdaptiveTopAppBarStyle`/`AppTopBarStyle`、`AppClickableItemRenderer`、M3 TopAppBarDefaults/ModalBottomSheet/pulltorefresh 类型。
- Theme bridge：`core/theme/AppDisplayPolicy.kt`、`AppSurfaceTokens.kt`、`MiuixThemePolicy.kt`、app 侧 `core/theme/Theme.kt` 均 M3 ColorScheme ↔ MiuixTheme 互转。
- app 仍在用的真实 M3 组件类别：TopAppBar(6)、ModalBottomSheet(3)、SnackbarHost(3)、pulltorefresh(4)、Linear/CircularProgressIndicator(3)、Slider(2)、AlertDialog/DatePicker(2)、NavigationBarItemDefaults(1)、DrawerValue(1) + design-system `AppPrimitiveComponents.kt` 单文件 import 72 个 M3 符号（feature 243 文件走 App* 包装器）。

## 3. 模块边界风险

1. **split-package（最高风险，阶段 6 最先处理）**：`design-system`（namespace=com.android.purebilibili.designsystem 但源码在 `core.ui`/`core.theme`）、`settings-core`（`core.store`）、`network-core`（`core.network.policy`）与 app 的 `core/ui`、`core/store`、`core/network` **同包名跨模块共存**。当前靠类名不重名编译通过，任何同名 FQCN 即冲突。Gradle 层隔离但包名层未隔离。
2. **app 内部无分层边界**：`domain` 仅 2 个 usecase 文件形同虚设；`data/repository` 20 个全局 object（VideoRepository 2260 行、ApiClient 2928 行）与 731 个 feature 文件同模块互引。
3. **巨型文件集中**：最大 10 文件约 4.7 万行（见 `BASELINE.md` §4）。
4. **两套导航栈并存**：`navigation/`(12 文件) 与 `navigation3/`(19 文件)，AppNavigation 2992 行。
5. **baselineprofile 强耦合 :app**（targetProjectPath），拆 app 需同步改。
6. `examples/`、`plugins/` 未 include，阶段 6 可忽略。

## 4. UI 层对全局对象的直接访问审计（阶段 1/2 例外清单输入）

### 全局对象清单

| 对象 | 定义位置 | 单例 | 引用文件数 | 其中含 @Composable 引用 |
|---|---|---|---|---|
| SettingsManager | core/store/SettingsManager.kt:910 | object | 96 | 49 |
| NetworkModule | core/network/ApiClient.kt:2423 | object | 60 | 6 |
| TokenManager | core/store/TokenManager.kt:19 | object | 39 | 4 |
| AccountSessionStore | core/store/AccountSessionStore.kt:26 | object | 5 | 1 |
| DatabaseModule/AppDatabase | core/database/*.kt | object/companion | 4 | 1 |
| PlayerSettingsStore | core/store/player/*.kt | object | 6 | 3 |
| NetworkProxyStore | core/store/NetworkProxyStore.kt:13 | object | 3 | 1 |
| data/repository 20 个单例 | data/repository/*.kt | object | 15 | 10 |

### 违规总量
- **硬违规（@Composable 体内）约 370 处** + 软违规（UI 包内 embedded VM/helper）约 210 处 ≈ **580 处**。
- SettingsManager 是绝对大头：settings(154)+video(110)+其他 feature(87)+AppNavigation(16)+MainActivity(~15) ≈ **355 处 UI 调用**。
- 次之：VideoRepository(~23)、MessageRepository(22 embedded VM)、AccountSessionStore(8)、NetworkModule(17)、TokenManager(12)、DanmakuRepository(2)、LiveRepository(7)、SearchRepository(4)。

### 按 feature 违规密度排名
settings(~169) > video(~145) > live(36) > navigation/AppNavigation(23) > message(22) > home(18) > list(15) > following(12) > watchlater(11) > bangumi(10) > search(9) > dynamic(8) > profile(6) > download(6) > category/partition(5) > plugin(4) > article(1)。

### 典型硬违规示例（阶段 1 护栏必须拦住的）
- `feature/video/ui/components/UpPreviewSheet.kt:136-164` — composable 内直连 `NetworkModule.api/spaceApi`。
- `feature/video/ui/pager/PortraitVideoPager.kt:520` — composable 内 `NetworkModule.api.getWatchLaterList()`；另有 10 处直调 VideoRepository。
- `feature/live/LivePlayerScreen.kt:436-437,1337` — composable 内直读 TokenManager 拼 Cookie/判断登录态。
- `feature/video/screen/VideoDetailScreenStateHolder.kt:4210-4223` — composition 内筛选 VIP 候选 + 写播放账号。
- `navigation/AppNavigation.kt` — 23 处（设置注入 + 账号切换 + 登出 `TokenManager.clear`）。

### 阶段 1 例外清单建议（首批豁免，避免 PR 默认路径全红）
1. **App 壳**：MainActivity.kt:817-1476（语言/主题/水印/启动图壳内初始化）。
2. **导航桥接层**：AppNavigation.kt 全部（23 处）单列"导航桥接层"过渡角色，不按 feature UI 处理。
3. **播放器基础设施**：VideoPlayerState.kt:976-1009/994（播放器核心态构建直读设置与 playbackOkHttpClient 属引擎初始化）；MiniPlayerManager、DownloadManager、DanmakuManager、PlaybackUserActionTracker 归业务管理器；plugin 运行时（AdFilterPlugin/CdnRegionPlugin/core/plugin）豁免。
4. **embedded ViewModel 形态**：feature/message/feed/*Screen、FollowingListScreen、WatchLaterScreen、LiveListScreen、FavoriteCategoryScreen — 护栏规则为"文件内 ViewModel 类豁免、@Composable 函数体禁止"。

### 拆除优先级
(a) SettingsManager 在 settings/video/live Screen 的 300+ 直接读 → 状态注入（部分文件已用 collectAsState，路径已存在）；(b) composable 内直连 NetworkModule.api → 上移 ViewModel/UseCase；(c) composable 内直读 TokenManager → 业务层；(d) VideoDetailScreenStateHolder VIP 筛选 → ViewModel+produceState；(e) PortraitVideoPager 的 VideoRepository 直调 → 上移。
