# 阶段 0：upstream/main churn 分布与同步承诺校准

日期：2026-08-16。审计对象：`upstream/main`（@ `379c968b9`）。命令均为 `git log upstream/main` 只读统计。

## 0. 数据口径（务必先读）

- `upstream/main` 分支全历史 **2748 个提交**，最早提交 **2025-12-03**（`重新梳理了项目结构`）。`--since=2025-08-16` 实际捕获了分支全部历史；有效数据窗口为 **8.5 个月（2025-12-03 → 2026-08-15）**，"月均"按 8.5 个月估算。
- 提交消息：2025-12~2026-04 大量中文（`尝试修改首页UI`、`修bug`）；2026-05 起转英文 Conventional Commits（feat/fix/refactor/perf/chore）。
- 分类按"提交涉及的文件路径前缀"归属，一个提交可命中多类；文件级口径按 commit×file 出现次数。

## 1. 月份分布（提交数）

| 月份 | 提交 | 备注 |
|---|---|---|
| 2025-12 | 70 | 分支起点 12-03 |
| 2026-01 | 71 | |
| 2026-02 | 86 | |
| 2026-03 | 79 | |
| 2026-04 | 118 | |
| 2026-05 | **710** | 爆发（backdrop 迁移、liquid chrome、danmaku v2） |
| 2026-06 | 387 | |
| 2026-07 | 633 | |
| 2026-08 | 594 | 截至 08-15，未满月 |

**关键信号：近 4 个月（05–08）2324 个提交，占全窗口 84.6%，月均约 580 提交/月**（此前月均约 85）。上游处于持续高强度重构期，churn 非平稳、近期加速。

## 2. Core / UI / Shell / Build 分布

### 按提交（重叠口径，一个提交可命中多类）
- **UI**（feature/*、design-system、core/ui、core/theme、artwork）：2221 提交，**80.8%**
- **Core**（core/network|store|util|plugin|player|database|cache、data/*、settings-core、network-core、PlayerViewModel.kt）：632，23.0%
- **Shell**（MainActivity、navigation/*、navigation3/*、AndroidManifest.xml）：558，20.3%
- **Build/CI/其他**（*.gradle*、.github、docs、scripts、plugins、baselineprofile 等）：693，25.2%

单类别专用提交（排除跨类）：UI-only 2218（80.7%）、Core-only 595（21.7%）、Shell-only 558（20.3%）。**类别几乎互斥**，单提交极少跨 Core/UI/Shell。

### 按文件出现次数（commit×file，更接近体积）
- 全部出现 25,605 次，唯一文件 3,663；`app/src/main` 占 **66.0%**。
- **测试文件占 24.4%**（6,249 次，唯一 1,168 个）——上游 UI 改动几乎同步改 UI 结构测试，UI fork 必须连带取舍测试，同步成本加倍。
- app/src/main 内部：feature 68.3%、core 10.3%、data 3.6%、navigation+navigation3 6.3%、app 0.5%、domain 0.04%。
- **UI : Core : Shell（体积）≈ 10 : 1.2 : 1**。

## 3. 高 churn 目录 / Top 文件

目录（出现次数）：feature/video 4084(308 文件)、feature/home 2302(121)、feature/settings 1563(123)、navigation+navigation3 1060、core/ui 750、feature/dynamic 686、feature/live 381、core/store 357、data/repository 335、feature/bangumi 319、data/model 270。

Top 20 热文件：`app/build.gradle.kts`(365)、`navigation/AppNavigation.kt`(**348**)、`CHANGELOG.md`(297)、`feature/video/screen/VideoDetailScreen.kt`(281)、`feature/home/components/BottomBar.kt`(278)、`core/store/SettingsManager.kt`(**276**)、`feature/video/ui/section/VideoPlayerSection.kt`(273)、`feature/home/HomeScreen.kt`(268)、`feature/home/components/TopBar.kt`(219)、`feature/home/components/cards/VideoCard.kt`(174)、`core/network/ApiClient.kt`(**145**)、`feature/video/ui/overlay/VideoPlayerOverlay.kt`(144)、`feature/search/SearchScreen.kt`(137)、`feature/space/SpaceScreen.kt`(136)、`feature/home/components/iOSHomeHeader.kt`(134)、`feature/video/ui/pager/PortraitVideoPager.kt`(133)、`feature/video/viewmodel/PlayerViewModel.kt`(**129**)、`MainActivity.kt`(129)。

注意 3 个"Core"文件在最热榜：SettingsManager(276)、ApiClient(145)、PlayerViewModel(129)——**上游的设置存储/网络/播放核心同样高度 churn，不是稳定层**。

## 4. Lite 敏感区域行为改动提交数

| 区域 | 路径 | 提交数 |
|---|---|---|
| 播放器覆盖层/播放状态 | feature/video/ui/overlay/*、VideoPlayerSection、PlayerViewModel、core/player | 443 |
| 导航/路由 | navigation/*、navigation3/* | 465 |
| 设置页 | feature/settings/* | 504 |
| 首页 | feature/home/* | 900 |
| 整个 feature/video | feature/video/* | 1005 |

四区域合计去重前 ≈ 2312 提交相关，占全量 84%。**几乎每次上游提交都踩到 Lite fork 需要盯守的地盘。**

## 5. 对计划 §6 同步承诺的校准建议

1. **"Core 可持续同步"基本成立，但需收敛定义**：纯 Core 数据面提交占比约 15–20%，且与 UI 几乎互斥。**Tier 1 同步窗口建议 2–4 周**（Core 近 4 个月月均 100–160 相关提交，两周约 50–80 个）。**但 `core/store/SettingsManager.kt`(276) 是全仓第 6 热文件**，与 feature/settings(504) 联动紧密，应从 Tier 1 剥离或明确归"设置 UI 受管理 fork"侧。
2. **"UI 受管理 fork"确认并扩权到含测试与路由**：UI 占 80%+ 提交、73% 文件 churn，近 4 个月月均 580 提交高温窗口下"同步 UI"数量上不可行。a) 24.4% churn 是测试文件且紧跟 UI 结构改动；b) `navigation/AppNavigation.kt`(348) 是全仓第二热文件，**路由不是稳定边界**——应把路由也明确列为"受管理"而非"可同步"；c) 播放器核心 PlayerViewModel(129) 与覆盖层(443) 耦合，"player core 非覆盖层"在上游实际代码里并不干净，降低对其可同步性期望。
3. **同步节律按滚动 90 天速度自动调整**：全窗口月均 323，但 5–8 月月均 580。按 2025-12~04 平静期（月均 85）设计会在重构洪峰下失效；洪峰期应降频。
4. **体积比修正**：UI:Core:Shell ≈ 10:1.2:1（文件口径）或 3.5:1（提交口径）。Core 占比远低 UI，但绝非法外之地——播放器+设置+网络三块占最热榜 3 席。

## 6. 结论摘要（供 §6 修订）

- Tier 1 快速跟随：网络/模型/账号/Repository 可同步（窗口 2–4 周）；**SettingsManager 除外**。
- Tier 2 手工移植：路由/UiState/Activity 壳——路由实际是全仓第二热区域（465 提交），按"受管理"而非"可同步"对待。
- Tier 3 事实 fork：Lite 页面 UI + **连带测试取舍** + 路由；port backlog 按滚动 90 天速度更新，洪峰期降频。
